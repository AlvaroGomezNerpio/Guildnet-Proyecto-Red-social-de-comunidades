import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NotificationDTO } from '../models/notification/NotificationDTO';
import { NotificationCreateDTO } from '../models/notification/NotificationCreateDTO';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private baseUrl = 'http://localhost:8080/api/v1/notifications';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getNotificationsByProfile(profileId: number): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(`${this.baseUrl}/profile/${profileId}`, {
      headers: this.getAuthHeaders()
    });
  }

  countUnread(profileId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/profile/${profileId}/unread/count`, {
      headers: this.getAuthHeaders()
    });
  }

  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${notificationId}/read`, {}, {
      headers: this.getAuthHeaders()
    });
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${notificationId}`, {
      headers: this.getAuthHeaders()
    });
  }

  createNotification(dto: NotificationCreateDTO): Observable<NotificationDTO> {
    return this.http.post<NotificationDTO>(`${this.baseUrl}`, dto, {
      headers: this.getAuthHeaders()
    });
  }
}

