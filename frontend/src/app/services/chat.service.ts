import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMessageDTO } from '../models/chat/ChatMessageDTO';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);

  /** Últimos mensajes del chat de la comunidad (solo miembros). */
  getHistory(communityId: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${environment.apiUrl}/api/v1/chat/${communityId}/messages`);
  }
}
