import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommunityService } from '../../../services/community.service';
import { CommunityProfileService } from '../../../services/community-profile.service';
import { PostService } from '../../../services/post.service';
import { NotificationService } from '../../../services/notification.service';
import { CommunityResponseDTO } from '../../../models/community/CommunityResponse.dto';
import { CommunityProfileDTO } from '../../../models/communityProfile/CommunityProfileDTO';
import { PostDTO } from '../../../models/post/PostDTO';
import { ActiveRoleService } from '../../../services/active-role.service';
import { RolePermissionService } from '../../../services/role-permission.service';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
  selector: 'app-community-detail',
  standalone: false,
  templateUrl: './community-detail.component.html',
  styleUrl: './community-detail.component.css',
})
export class CommunityDetailComponent {
  communityId!: number;
  community: CommunityResponseDTO | null = null;
  profile: CommunityProfileDTO | null = null;
  posts: PostWithToggle[] = [];

  isLoggedIn: boolean = false;
  error: string | null = null;

  members: CommunityProfileDTO[] = [];
  filteredMembers: CommunityProfileDTO[] = [];
  searchTerm: string = '';
  showMembersModal: boolean = false;

  unreadCount: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private communityService: CommunityService,
    private communityProfileService: CommunityProfileService,
    private postService: PostService,
    private notificationService: NotificationService,
    private activeRoleService: ActiveRoleService,
    private rolePermissionService: RolePermissionService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = !!localStorage.getItem('token');

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (idParam) {
        this.communityId = +idParam;
        this.loadCommunity();
        this.loadPosts();
        if (this.isLoggedIn) {
          this.loadUserProfile();
        }
      }
    });
  }

  loadCommunity(): void {
    this.communityService.getCommunityById(this.communityId).subscribe({
      next: (data) => (this.community = data),
      error: () => (this.error = 'No se pudo cargar la comunidad.'),
    });
  }

  loadUserProfile(): void {
  this.communityProfileService.getMyProfileInCommunity(this.communityId).subscribe({
    next: (profile) => {
      this.profile = profile;

      if (profile.rol?.id) {
        this.rolePermissionService.getPermissionsByRole(profile.rol.id).subscribe({
          next: (permissions) => {
            const permissionNames = permissions.map(p => p.permissionName);
            this.activeRoleService.setActiveRole({
              communityId: this.communityId,
              roleId: profile.rol.id,
              roleName: profile.rol.name,
              permissions: permissionNames
            });

            this.loadUnreadNotifications();
          },
          error: () => {
            this.activeRoleService.clear();
            this.loadUnreadNotifications();
          }
        });
      } else {
        this.activeRoleService.clear();
        this.loadUnreadNotifications();
      }
    },
    error: () => {
      this.profile = null;
      this.activeRoleService.clear();
    }
  });
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
      next: (data) => {
        this.posts = data.map((post) => ({
          ...post,
          showContent: false,
        }));
      },
      error: () => (this.error = 'No se pudieron cargar los posts.'),
    });
  }

  goToProfile(): void {
    if (this.profile) {
      this.router.navigate(['/communities/profile', this.communityId]);
    }
  }

  loadCommunityMembers(): void {
    this.communityProfileService
      .getProfilesByCommunity(this.communityId)
      .subscribe({
        next: (data) => {
          this.members = data;
          this.filteredMembers = data;
          this.showMembersModal = true;
        },
        error: () => (this.error = 'No se pudieron cargar los miembros.'),
      });
  }

  filterMembers(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredMembers = this.members.filter((m) =>
      m.username.toLowerCase().includes(term)
    );
  }

  viewProfile(profileId: number): void {
    if (this.profile) {
      this.router.navigate(['/communities/profile', profileId], {
        queryParams: { myProfileId: this.profile.id },
      });
    } else {
      this.router.navigate(['/communities/profile', profileId]);
    }
  }

  goToCreatePost(): void {
    if (this.profile) {
      this.router.navigate([
        '/communities',
        this.communityId,
        'create-post',
        this.profile.id,
      ]);
    }
  }

  goToSearchPosts(): void {
    this.router.navigate(['/communities', this.communityId, 'search-posts']);
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }

  goToPostDetail(postId: number): void {
    if (this.profile) {
      this.router.navigate(['/posts', postId], {
        queryParams: { myProfileId: this.profile.id },
      });
    } else {
      this.router.navigate(['/posts', postId]);
    }
  }

  goToNotifications(): void {
    this.router.navigate(['/communities', this.communityId, 'notifications'], {
      queryParams: { myProfileId: this.profile?.id },
    });
  }

  goToCreateRole(): void {
    this.router.navigate(['/communities', this.communityId, 'create-role']);
  }

  goToEditCommunity() {
  this.router.navigate(['/communities', this.communityId, 'edit']);
}

canAssignRoles(): boolean {
  return (
    this.activeRoleService.isForCommunity(this.communityId) &&
    this.activeRoleService.hasPermission('ASSIGN_ROLES')
  );
}

canEditCommunity(): boolean {
  return (
    this.activeRoleService.isForCommunity(this.communityId) &&
    this.activeRoleService.hasPermission('MANAGE_COMMUNITY_SETTINGS')
  );
}

}
