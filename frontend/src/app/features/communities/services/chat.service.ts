import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ChatChannelDTO, ChatChannelRequest, ChatMessageDTO } from '../models/chat.models';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1`;

  /** Canales de la comunidad (solo miembros). */
  getChannels(communityId: number): Observable<ChatChannelDTO[]> {
    return this.http.get<ChatChannelDTO[]>(`${this.apiUrl}/communities/${communityId}/channels`);
  }

  createChannel(communityId: number, channel: ChatChannelRequest): Observable<ChatChannelDTO> {
    return this.http.post<ChatChannelDTO>(`${this.apiUrl}/communities/${communityId}/channels`, channel);
  }

  updateChannel(channelId: number, channel: ChatChannelRequest): Observable<ChatChannelDTO> {
    return this.http.put<ChatChannelDTO>(`${this.apiUrl}/channels/${channelId}`, channel);
  }

  /** Borra el canal y todos sus mensajes. */
  deleteChannel(channelId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/channels/${channelId}`);
  }

  /** Marca como leídos los mensajes actuales del canal. */
  markRead(channelId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/channels/${channelId}/read`, null);
  }

  /** Últimos mensajes del canal. */
  getHistory(channelId: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.apiUrl}/channels/${channelId}/messages`);
  }
}
