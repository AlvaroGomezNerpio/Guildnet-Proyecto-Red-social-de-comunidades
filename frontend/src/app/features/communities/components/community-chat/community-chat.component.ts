import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../../../environments/environment';
import { ChatChannelDTO, ChatMessageDTO } from '../../models/chat.models';
import { AuthService } from '../../../../core/auth/auth.service';
import { ChatService } from '../../services/chat.service';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { ModalComponent } from '../../../../shared/ui/modal/modal.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { errorMessage } from '../../../../core/http/http-error';

/**
 * Chat en tiempo real de una comunidad, organizado en canales (STOMP sobre
 * WebSocket). Una sola conexión suscrita a todos los canales: el abierto
 * muestra sus mensajes y el resto cuenta los nuevos. Quien tenga
 * MANAGE_CHANNELS puede crear, renombrar y borrar canales.
 *
 * Puede seguir montado aunque no se vea ({@link visible} = false) para que el
 * contador de mensajes nuevos siga al día.
 */
@Component({
  selector: 'app-community-chat',
  templateUrl: './community-chat.component.html',
  styleUrl: './community-chat.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, AvatarComponent, ModalComponent, RelativeTimePipe],
})
export class CommunityChatComponent implements OnInit, OnDestroy {
  private readonly chatService = inject(ChatService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  readonly communityId = input.required<number>();
  readonly myProfileId = input<number | null>(null);
  /** Puede crear y gestionar canales (MANAGE_CHANNELS). */
  readonly canManage = input(false);
  /** Si el chat está a la vista; si no, los mensajes del canal abierto también cuentan como nuevos. */
  readonly visible = input(true);
  /** Total de mensajes nuevos en todos los canales. */
  readonly unreadTotalChange = output<number>();

  private readonly messageList = viewChild<ElementRef<HTMLElement>>('messageList');

  readonly channels = signal<ChatChannelDTO[]>([]);
  readonly channelsLoaded = signal(false);
  readonly activeId = signal<number | null>(null);
  readonly active = computed(() => this.channels().find((c) => c.id === this.activeId()) ?? null);
  readonly messages = signal<ChatMessageDTO[]>([]);
  readonly connected = signal(false);
  readonly error = signal<string | null>(null);
  draft = '';

  /** Diálogo para crear o editar un canal. */
  readonly dialogOpen = signal(false);
  readonly editingChannel = signal<ChatChannelDTO | null>(null);
  channelName = '';
  channelDescription = '';

  /** Mensajes nuevos por canal. */
  readonly unread = signal<Record<number, number>>({});
  readonly unreadTotal = computed(() => Object.values(this.unread()).reduce((sum, n) => sum + n, 0));

  private client?: Client;
  private readonly subscriptions = new Map<number, StompSubscription>();
  private markReadTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    effect(() => this.unreadTotalChange.emit(this.unreadTotal()));
    // Al volver a ver el chat, el canal abierto queda leído
    effect(() => {
      const id = this.activeId();
      if (this.visible() && id !== null && (this.unread()[id] ?? 0) > 0) this.clearUnread(id);
    });
  }

  ngOnInit(): void {
    this.chatService.getChannels(this.communityId()).subscribe({
      next: (channels) => {
        this.channels.set(channels);
        this.unread.set(Object.fromEntries(channels.map((c) => [c.id, c.unreadCount ?? 0])));
        this.channelsLoaded.set(true);
        // Se abre el primer canal con mensajes nuevos (o el primero)
        if (channels.length) this.select(channels.find((c) => c.unreadCount > 0) ?? channels[0]);
        this.subscribeAll();
      },
      error: () => {
        this.channelsLoaded.set(true);
        this.error.set('No se pudieron cargar los canales.');
      },
    });
    this.connect();
  }

  ngOnDestroy(): void {
    clearTimeout(this.markReadTimer);
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.client?.deactivate();
  }

  // ------------------------------------------------------------------ Mensajes

  select(channel: ChatChannelDTO): void {
    if (this.activeId() === channel.id) return;
    this.activeId.set(channel.id);
    this.messages.set([]);
    this.chatService.getHistory(channel.id).subscribe({
      next: (history) => {
        if (this.activeId() !== channel.id) return;
        this.messages.set(history);
        this.scrollToBottom();
        if (this.visible()) this.clearUnread(channel.id);
      },
      error: () => this.error.set('No se pudo cargar el historial del canal.'),
    });
  }

  unreadOf(channelId: number): number {
    return this.unread()[channelId] ?? 0;
  }

  /** Pone el contador del canal a cero y lo guarda en el servidor. */
  private clearUnread(channelId: number): void {
    this.unread.update((map) => ({ ...map, [channelId]: 0 }));
    clearTimeout(this.markReadTimer);
    // Pequeña espera para no llamar al servidor por cada mensaje si llegan varios seguidos
    this.markReadTimer = setTimeout(() => this.chatService.markRead(channelId).subscribe({ error: () => undefined }), 400);
  }

  send(): void {
    const content = this.draft.trim();
    const channelId = this.activeId();
    if (!content || channelId === null || !this.client?.connected) return;
    this.client.publish({ destination: `/app/chat/channel/${channelId}`, body: JSON.stringify({ content }) });
    this.draft = '';
  }

  isMine(message: ChatMessageDTO): boolean {
    return message.profileId === this.myProfileId();
  }

  private connect(): void {
    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: { Authorization: `Bearer ${this.authService.getToken()}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected.set(true);
        this.error.set(null);
        this.subscriptions.clear(); // tras reconectar, las suscripciones anteriores ya no existen
        this.subscribeAll();
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => {
        this.connected.set(false);
        this.error.set('Se ha perdido la conexión con el chat.');
      },
    });
    this.client.activate();
  }

  /** Se suscribe a los canales que aún no lo estén (al conectar, al cargarlos y al crear uno). */
  private subscribeAll(): void {
    if (!this.client?.connected) return;
    for (const channel of this.channels()) {
      if (this.subscriptions.has(channel.id)) continue;
      const sub = this.client.subscribe(`/topic/chat/channel/${channel.id}`, (frame: IMessage) =>
        this.onMessage(JSON.parse(frame.body) as ChatMessageDTO)
      );
      this.subscriptions.set(channel.id, sub);
    }
  }

  private onMessage(message: ChatMessageDTO): void {
    const isActive = message.channelId === this.activeId();
    if (isActive) {
      this.messages.update((list) => [...list, message]);
      this.scrollToBottom();
    }
    if (this.isMine(message)) return;
    if (isActive && this.visible()) {
      this.clearUnread(message.channelId);
    } else {
      this.unread.update((map) => ({ ...map, [message.channelId]: (map[message.channelId] ?? 0) + 1 }));
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const el = this.messageList()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }

  // ------------------------------------------------------------------ Canales

  openCreate(): void {
    this.editingChannel.set(null);
    this.channelName = '';
    this.channelDescription = '';
    this.dialogOpen.set(true);
  }

  openEdit(channel: ChatChannelDTO): void {
    this.editingChannel.set(channel);
    this.channelName = channel.name;
    this.channelDescription = channel.description ?? '';
    this.dialogOpen.set(true);
  }

  saveChannel(): void {
    const name = this.channelName.trim();
    if (!name) return;
    const data = { name, description: this.channelDescription.trim() || null };
    const editing = this.editingChannel();
    const request = editing
      ? this.chatService.updateChannel(editing.id, data)
      : this.chatService.createChannel(this.communityId(), data);
    request.subscribe({
      next: (saved) => {
        this.dialogOpen.set(false);
        this.channels.update((list) => (editing ? list.map((c) => (c.id === saved.id ? saved : c)) : [...list, saved]));
        if (!editing) {
          this.subscribeAll();
          this.select(saved);
        }
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo guardar el canal.')),
    });
  }

  async deleteChannel(channel: ChatChannelDTO): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar canal',
      message: `Se borrará #${channel.name} con todos sus mensajes. No se puede deshacer.`,
      confirmLabel: 'Eliminar canal',
      danger: true,
    });
    if (!accepted) return;
    this.chatService.deleteChannel(channel.id).subscribe({
      next: () => {
        this.dialogOpen.set(false);
        const remaining = this.channels().filter((c) => c.id !== channel.id);
        this.channels.set(remaining);
        this.subscriptions.get(channel.id)?.unsubscribe();
        this.subscriptions.delete(channel.id);
        this.unread.update(({ [channel.id]: _removed, ...rest }) => rest);
        if (this.activeId() === channel.id) {
          this.activeId.set(null);
          this.messages.set([]);
          if (remaining.length) this.select(remaining[0]);
        }
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar el canal.')),
    });
  }
}
