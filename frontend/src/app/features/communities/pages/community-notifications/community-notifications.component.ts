import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { NotificationDTO, NotificationType } from '../../models/notification.models';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';

/** Icono de Bootstrap Icons para cada tipo de notificación. */
const ICONS: Record<NotificationType, string> = {
  [NotificationType.COMMENT_PROFILE]: 'chat-square-text',
  [NotificationType.COMMENT_POST]: 'chat-left-text',
  [NotificationType.LIKE_POST]: 'heart-fill',
  [NotificationType.SYSTEM]: 'megaphone',
  [NotificationType.WELCOME]: 'stars',
  [NotificationType.ACHIEVEMENT_UNLOCKED]: 'trophy',
  [NotificationType.TITLE_ASSIGNED]: 'award',
  [NotificationType.TITLE_REMOVED]: 'award',
  [NotificationType.ROLE_ASSIGNED]: 'shield-check',
  [NotificationType.ROLE_REMOVED]: 'shield-x',
};

@Component({
  selector: 'app-community-notifications',
  imports: [RouterLink, AvatarComponent, EmptyStateComponent, RelativeTimePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-notifications.component.html',
  styleUrl: './community-notifications.component.css',
})
export class CommunityNotificationsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly notificationService = inject(NotificationService);
  private readonly profileService = inject(CommunityProfileService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  communityId = 0;
  readonly notifications = signal<NotificationDTO[]>([]);
  readonly loaded = signal(false);
  readonly unread = computed(() => this.notifications().filter((n) => !n.read).length);

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('id'));
    this.profileService.getMyProfileInCommunity(this.communityId).subscribe({
      next: (me) => this.load(me.id),
      error: () => {
        this.loaded.set(true);
        this.toast.error('No perteneces a esta comunidad.');
      },
    });
  }

  icon(type: NotificationType): string {
    return ICONS[type] ?? 'bell';
  }

  private load(profileId: number): void {
    this.notificationService.getNotificationsByProfile(profileId).subscribe({
      next: (data) => {
        this.notifications.set(data);
        this.loaded.set(true);
      },
      error: () => {
        this.loaded.set(true);
        this.toast.error('Error al cargar las notificaciones.');
      },
    });
  }

  markAsRead(notification: NotificationDTO): void {
    if (notification.read) return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => this.notifications.update((list) => list.map((n) => (n.id === notification.id ? { ...n, read: true } : n))),
      error: () => this.toast.error('Error al marcar como leída.'),
    });
  }

  remove(notification: NotificationDTO): void {
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => this.notifications.update((list) => list.filter((n) => n.id !== notification.id)),
      error: () => this.toast.error('Error al eliminar la notificación.'),
    });
  }

  markAllAsRead(): void {
    const unread = this.notifications().filter((n) => !n.read);
    if (!unread.length) return;
    forkJoin(unread.map((n) => this.notificationService.markAsRead(n.id))).subscribe({
      next: () => this.notifications.update((list) => list.map((n) => ({ ...n, read: true }))),
      error: () => this.toast.error('Error al marcar todas como leídas.'),
    });
  }

  async deleteAll(): Promise<void> {
    const all = this.notifications();
    if (!all.length) return;
    if (!(await this.confirm.ask({ message: '¿Eliminar todas las notificaciones?', confirmLabel: 'Eliminar todas', danger: true }))) return;
    forkJoin(all.map((n) => this.notificationService.deleteNotification(n.id))).subscribe({
      next: () => this.notifications.set([]),
      error: () => this.toast.error('Error al eliminar las notificaciones.'),
    });
  }
}
