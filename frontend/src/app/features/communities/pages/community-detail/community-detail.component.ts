import { Component, ChangeDetectionStrategy, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { CommunityService } from '../../services/community.service';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { PostService } from '../../../posts/services/post.service';
import { NotificationService } from '../../services/notification.service';
import { CommunityPermissionsService, Permission } from '../../services/community-permissions.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { CommunityProfileDTO } from '../../../profiles/models/profile.models';
import { PostDTO } from '../../../posts/models/post.models';
import { errorMessage } from '../../../../core/http/http-error';
import { CommunityChatComponent } from '../../components/community-chat/community-chat.component';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
    selector: 'app-community-detail',
    templateUrl: './community-detail.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './community-detail.component.css',
    imports: [
        CommunityChatComponent,
        FormsModule,
        DatePipe,
    ],
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

  communityId!: number;
  community: CommunityResponseDTO | null = null;
  /** Perfil del usuario en esta comunidad; null si no es miembro. */
  profile: CommunityProfileDTO | null = null;
  profileLoaded = false;
  posts: PostWithToggle[] = [];
  permissions: Permission[] = [];

  error: string | null = null;
  joining = false;

  members: CommunityProfileDTO[] = [];
  filteredMembers: CommunityProfileDTO[] = [];
  searchTerm = '';
  showMembersModal = false;

  unreadCount = 0;

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) return;
      this.communityId = id;
      this.profile = null;
      this.profileLoaded = false;
      this.loadCommunity();
      this.loadPosts();
      this.loadMembership();
    });
  }

  get isMember(): boolean {
    return this.profile !== null;
  }

  loadCommunity(): void {
    this.communityService.getCommunityById(this.communityId).subscribe({
      next: (data) => (this.community = data),
      error: () => (this.error = 'No se pudo cargar la comunidad.'),
    });
  }

  loadMembership(): void {
    this.profileService.getMyProfileInCommunity(this.communityId).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileLoaded = true;
        this.loadPermissions();
        this.loadUnreadNotifications();
      },
      error: () => {
        this.profile = null;
        this.profileLoaded = true;
        this.permissions = [];
      },
    });
  }

  loadPermissions(): void {
    this.permissionsService.getMyPermissions(this.communityId).subscribe((perms) => (this.permissions = perms));
  }

  loadUnreadNotifications(): void {
    if (!this.profile) return;
    this.notificationService.countUnread(this.profile.id).subscribe({
      next: (count) => (this.unreadCount = count),
      error: () => (this.unreadCount = 0),
    });
  }

  loadPosts(): void {
    this.postService.getPostsByCommunity(this.communityId).subscribe({
      next: (data) => (this.posts = data.map((post) => ({ ...post, showContent: false }))),
      error: () => (this.error = 'No se pudieron cargar los posts.'),
    });
  }

  join(): void {
    this.joining = true;
    this.profileService.joinCommunity(this.communityId).subscribe({
      next: () => {
        this.joining = false;
        this.loadMembership();
      },
      error: (err) => {
        this.joining = false;
        this.error = errorMessage(err, 'No se pudo unir a la comunidad.');
      },
    });
  }

  loadCommunityMembers(): void {
    this.profileService.getProfilesByCommunity(this.communityId).subscribe({
      next: (data) => {
        this.members = data;
        this.filteredMembers = data;
        this.searchTerm = '';
        this.showMembersModal = true;
      },
      error: () => (this.error = 'No se pudieron cargar los miembros.'),
    });
  }

  filterMembers(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredMembers = this.members.filter((m) => m.username.toLowerCase().includes(term));
  }

  viewProfile(profileId: number): void {
    this.showMembersModal = false;
    this.router.navigate(['/communities/profile', profileId]);
  }

  goToCreatePost(): void {
    if (this.profile) {
      this.router.navigate(['/communities', this.communityId, 'create-post', this.profile.id]);
    }
  }

  goToSearchPosts(): void {
    this.router.navigate(['/communities', this.communityId, 'search-posts']);
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }

  goToPostDetail(postId: number): void {
    this.router.navigate(['/posts', postId]);
  }

  goToNotifications(): void {
    this.router.navigate(['/communities', this.communityId, 'notifications']);
  }

  goToCreateRole(): void {
    this.router.navigate(['/communities', this.communityId, 'roles']);
  }

  goToEditCommunity(): void {
    this.router.navigate(['/communities', this.communityId, 'edit']);
  }

  hasPermission(permission: Permission): boolean {
    return this.permissions.includes(permission);
  }
}
