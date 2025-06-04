import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDTO } from '../../../models/notification/NotificationDTO';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-community-notifications',
  standalone: false,
  templateUrl: './community-notifications.component.html',
  styleUrl: './community-notifications.component.css'
})
export class CommunityNotificationsComponent implements OnInit {
  myProfileId!: number;
  notifications: NotificationDTO[] = [];
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const myProfileIdParam = params.get('myProfileId');
      if (myProfileIdParam) {
        this.myProfileId = +myProfileIdParam;
        this.loadNotifications();
      }
    });
  }

  loadNotifications(): void {
    this.notificationService.getNotificationsByProfile(this.myProfileId).subscribe({
      next: data => this.notifications = data,
      error: () => this.errorMessage = 'Error al cargar notificaciones.'
    });
  }

  markAsRead(notificationId: number): void {
    this.notificationService.markAsRead(notificationId).subscribe({
      next: () => {
        const notif = this.notifications.find(n => n.id === notificationId);
        if (notif) notif.read = true;
      },
      error: () => this.errorMessage = 'Error al marcar como leída.'
    });
  }

  deleteNotification(notificationId: number): void {
    this.notificationService.deleteNotification(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notificationId);
      },
      error: () => this.errorMessage = 'Error al eliminar notificación.'
    });
  }

  markAllAsRead(): void {
    const unread = this.notifications.filter(n => !n.read);
    if (unread.length === 0) return;

    const requests = unread.map(n => this.notificationService.markAsRead(n.id));

    forkJoin(requests).subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      },
      error: () => {
        this.errorMessage = 'Error al marcar todas como leídas.';
      }
    });
  }

  deleteAll(): void {
    if (!confirm('¿Estás seguro de eliminar todas las notificaciones?')) return;

    const requests = this.notifications.map(n => this.notificationService.deleteNotification(n.id));

    if (requests.length === 0) return;

    forkJoin(requests).subscribe({
      next: () => {
        this.notifications = [];
      },
      error: () => {
        this.errorMessage = 'Error al eliminar todas las notificaciones.';
      }
    });
  }
}
