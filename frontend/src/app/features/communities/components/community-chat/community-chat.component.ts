import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  computed,
  inject,
  input,
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
 * WebSocket). Una sola conexión; al cambiar de canal se cambia la suscripción.
 * Quien tenga MANAGE_CHANNELS puede crear, renombrar y borrar canales.
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

  private client?: Client;
  private subscription?: StompSubscription;

  ngOnInit(): void {
    this.chatService.getChannels(this.communityId()).subscribe({
      next: (channels) => {
        this.channels.set(channels);
        this.channelsLoaded.set(true);
        if (channels.length) this.select(channels[0]);
      },
      error: () => {
        this.channelsLoaded.set(true);
        this.error.set('No se pudieron cargar los canales.');
      },
    });
    this.connect();
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
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
      },
      error: () => this.error.set('No se pudo cargar el historial del canal.'),
    });
    this.subscribeToActive();
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
        this.subscribeToActive();
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => {
        this.connected.set(false);
        this.error.set('Se ha perdido la conexión con el chat.');
      },
    });
    this.client.activate();
  }

  /** Suscribe al canal activo (se llama al conectar, al reconectar y al cambiar de canal). */
  private subscribeToActive(): void {
    this.subscription?.unsubscribe();
    this.subscription = undefined;
    const channelId = this.activeId();
    if (channelId === null || !this.client?.connected) return;
    this.subscription = this.client.subscribe(`/topic/chat/channel/${channelId}`, (frame: IMessage) => {
      const message = JSON.parse(frame.body) as ChatMessageDTO;
      if (message.channelId !== this.activeId()) return;
      this.messages.update((list) => [...list, message]);
      this.scrollToBottom();
    });
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
        if (!editing) this.select(saved);
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
        if (this.activeId() === channel.id) {
          this.activeId.set(null);
          this.messages.set([]);
          this.subscribeToActive();
          if (remaining.length) this.select(remaining[0]);
        }
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar el canal.')),
    });
  }
}
