import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { NotificationDTO } from '../models/notification.models';

/** Las notificaciones las genera el backend; aqui solo se consultan y gestionan. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/notifications`;

  getNotificationsByProfile(profileId: number): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(`${this.baseUrl}/profile/${profileId}`);
  }

  countUnread(profileId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/profile/${profileId}/unread/count`);
  }

  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${notificationId}/read`, {});
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${notificationId}`);
  }
}
