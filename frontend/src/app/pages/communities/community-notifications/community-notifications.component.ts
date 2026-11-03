import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDTO } from '../../../models/notification/NotificationDTO';
import { forkJoin } from 'rxjs';
import { CommunityProfileService } from '../../../services/community-profile.service';

@Component({
  selector: 'app-community-notifications',
  standalone: false,
  templateUrl: './community-notifications.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './community-notifications.component.css'
})
export class CommunityNotificationsComponent implements OnInit {
  communityId!: number;
  myProfileId: number | null = null;
  notifications: NotificationDTO[] = [];
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private profileService: CommunityProfileService
  ) {}

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('id'));
    this.profileService.getMyProfileInCommunity(this.communityId).subscribe({
      next: (me) => {
        this.myProfileId = me.id;
        this.loadNotifications();
      },
      error: () => (this.errorMessage = 'No perteneces a esta comunidad.'),
    });
  }

  loadNotifications(): void {
    if (!this.myProfileId) return;
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
