import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  inject,
} from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { environment } from '../../../../../environments/environment';
import { ChatMessageDTO } from '../../models/chat.models';
import { AuthService } from '../../../../core/auth/auth.service';
import { ChatService } from '../../services/chat.service';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

/**
 * Chat en tiempo real de una comunidad (STOMP sobre WebSocket).
 * Carga el historial por REST y luego recibe los mensajes nuevos al momento.
 */
@Component({
    selector: 'app-community-chat',
    templateUrl: './community-chat.component.html',
    styleUrl: './community-chat.component.css',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormsModule, DatePipe],
})
export class CommunityChatComponent implements OnInit, OnDestroy {
  private readonly chatService = inject(ChatService);
  private readonly authService = inject(AuthService);

  @Input({ required: true }) communityId!: number;
  @Input() myProfileId: number | null = null;

  @ViewChild('messageList') private messageList?: ElementRef<HTMLElement>;

  messages: ChatMessageDTO[] = [];
  draft = '';
  connected = false;
  error: string | null = null;

  private client?: Client;

  ngOnInit(): void {
    this.chatService.getHistory(this.communityId).subscribe({
      next: (history) => {
        this.messages = history;
        this.scrollToBottom();
      },
      error: () => (this.error = 'No se pudo cargar el historial del chat.'),
    });
    this.connect();
  }

  ngOnDestroy(): void {
    this.client?.deactivate();
  }

  send(): void {
    const content = this.draft.trim();
    if (!content || !this.client?.connected) return;

    this.client.publish({
      destination: `/app/chat/${this.communityId}`,
      body: JSON.stringify({ content }),
    });
    this.draft = '';
  }

  isMine(message: ChatMessageDTO): boolean {
    return message.profileId === this.myProfileId;
  }

  private connect(): void {
    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: { Authorization: `Bearer ${this.authService.getToken()}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected = true;
        this.error = null;
        this.client!.subscribe(`/topic/chat/${this.communityId}`, (frame: IMessage) => {
          this.messages = [...this.messages, JSON.parse(frame.body) as ChatMessageDTO];
          this.scrollToBottom();
        });
      },
      onWebSocketClose: () => (this.connected = false),
      onStompError: () => {
        this.connected = false;
        this.error = 'Se ha perdido la conexión con el chat.';
      },
    });
    this.client.activate();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const el = this.messageList?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}
