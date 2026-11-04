import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommunityService } from '../../services/community.service';
import { NotificationService } from '../../services/notification.service';
import { CommunityPermissionsService, Permission } from '../../services/community-permissions.service';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { PostService } from '../../../posts/services/post.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { CommunityProfileDTO } from '../../../profiles/models/profile.models';
import { PostDTO } from '../../../posts/models/post.models';
import { errorMessage } from '../../../../core/http/http-error';
import { CommunityChatComponent } from '../../components/community-chat/community-chat.component';
import { PostCardComponent } from '../../../posts/components/post-card/post-card.component';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { ModalComponent } from '../../../../shared/ui/modal/modal.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';

type Tab = 'posts' | 'chat';

@Component({
  selector: 'app-community-detail',
  imports: [
    FormsModule,
    RouterLink,
    CommunityChatComponent,
    PostCardComponent,
    AvatarComponent,
    TagBadgeComponent,
    ModalComponent,
    EmptyStateComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-detail.component.html',
  styleUrl: './community-detail.component.css',
})
export class CommunityDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly communityService = inject(CommunityService);
  private readonly profileService = inject(CommunityProfileService);
  private readonly postService = inject(PostService);
  private readonly notificationService = inject(NotificationService);
  private readonly permissionsService = inject(CommunityPermissionsService);
  private readonly toast = inject(ToastService);

  communityId = 0;
  readonly community = signal<CommunityResponseDTO | null>(null);
  /** Perfil del usuario en esta comunidad; null si no es miembro. */
  readonly profile = signal<CommunityProfileDTO | null>(null);
  readonly membershipLoaded = signal(false);
  readonly permissions = signal<Permission[]>([]);
  readonly unreadCount = signal(0);
  readonly tab = signal<Tab>('posts');
  readonly joining = signal(false);

  readonly posts = signal<PostDTO[]>([]);
  readonly totalPosts = signal(0);
  readonly hasMorePosts = signal(false);
  readonly loadingPosts = signal(false);
  private postsPage = 0;

  readonly membersOpen = signal(false);
  readonly members = signal<CommunityProfileDTO[]>([]);
  readonly memberFilter = signal('');
  readonly filteredMembers = computed(() => {
    const term = this.memberFilter().toLowerCase();
    return this.members().filter((m) => m.username.toLowerCase().includes(term));
  });

  readonly canManageRoles = computed(() => this.permissions().includes('ASSIGN_ROLES'));
  readonly canEditCommunity = computed(() => this.permissions().includes('MANAGE_COMMUNITY_SETTINGS'));

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) return;
      this.communityId = id;
      this.profile.set(null);
      this.membershipLoaded.set(false);
      this.tab.set('posts');
      this.loadCommunity();
      this.loadPosts();
      this.loadMembership();
    });
  }

  private loadCommunity(): void {
    this.communityService.getCommunityById(this.communityId).subscribe({
      next: (data) => this.community.set(data),
      error: () => this.toast.error('No se pudo cargar la comunidad.'),
    });
  }

  private loadMembership(): void {
    this.profileService.getMyProfileInCommunity(this.communityId).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.membershipLoaded.set(true);
        this.permissionsService.getMyPermissions(this.communityId).subscribe((perms) => this.permissions.set(perms));
        this.notificationService.countUnread(profile.id).subscribe((count) => this.unreadCount.set(count));
      },
      error: () => {
        this.profile.set(null);
        this.permissions.set([]);
        this.membershipLoaded.set(true);
      },
    });
  }

  /** Carga la primera página de publicaciones, o la siguiente si {@code more} es true. */
  loadPosts(more = false): void {
    const page = more ? this.postsPage + 1 : 0;
    this.loadingPosts.set(true);
    this.postService.getPostsByCommunity(this.communityId, page).subscribe({
      next: (result) => {
        this.posts.update((current) => (more ? [...current, ...result.content] : result.content));
        this.postsPage = result.page;
        this.totalPosts.set(result.totalElements);
        this.hasMorePosts.set(!result.last);
        this.loadingPosts.set(false);
      },
      error: () => {
        this.loadingPosts.set(false);
        this.toast.error('No se pudieron cargar las publicaciones.');
      },
    });
  }

  join(): void {
    this.joining.set(true);
    this.profileService.joinCommunity(this.communityId).subscribe({
      next: () => {
        this.joining.set(false);
        this.toast.success('¡Bienvenido a la comunidad!');
        this.loadMembership();
      },
      error: (err) => {
        this.joining.set(false);
        this.toast.error(errorMessage(err, 'No se pudo unir a la comunidad.'));
      },
    });
  }

  openMembers(): void {
    this.profileService.getProfilesByCommunity(this.communityId).subscribe({
      next: (data) => {
        this.members.set(data);
        this.memberFilter.set('');
        this.membersOpen.set(true);
      },
      error: () => this.toast.error('No se pudieron cargar los miembros.'),
    });
  }

  viewProfile(profileId: number): void {
    this.membersOpen.set(false);
    this.router.navigate(['/communities/profile', profileId]);
  }

  goToCreatePost(): void {
    const profile = this.profile();
    if (profile) {
      this.router.navigate(['/communities', this.communityId, 'create-post', profile.id]);
    }
  }
}
