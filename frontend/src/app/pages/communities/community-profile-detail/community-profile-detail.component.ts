import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommunityProfileDTO } from '../../../models/communityProfile/CommunityProfileDTO';
import { CommunityProfileService } from '../../../services/community-profile.service';
import { PostDTO } from '../../../models/post/PostDTO';
import { PostService } from '../../../services/post.service';
import { UpdateCommunityProfileRequest } from '../../../models/communityProfile/UpdateCommunityProfileRequest';
import { ProfileCommentService } from '../../../services/profile-comment.service';
import { ProfileCommentDTO } from '../../../models/profileComment/ProfileCommentDTO';
import { ProfileCommentCreateUpdateDTO } from '../../../models/profileComment/ProfileCommentCreateUpdateDTO';
import { NotificationService } from '../../../services/notification.service';
import { NotificationCreateDTO } from '../../../models/notification/NotificationCreateDTO';
import { NotificationType } from '../../../models/notification/NotificationType';
import { TitleService } from '../../../services/title.service';
import { TitleDTO } from '../../../models/title/TitleDTO';
import { CreateTitleRequest } from '../../../models/title/CreateTitleRequest';
import { UpdateTitleRequest } from '../../../models/title/UpdateTitleRequest';
import { RoleService } from '../../../services/role.service';
import { RoleDTO } from '../../../models/role/RoleDTO';
import { ActiveRoleService } from '../../../services/active-role.service';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
  selector: 'app-community-profile-detail',
  standalone: false,
  templateUrl: './community-profile-detail.component.html',
  styleUrl: './community-profile-detail.component.css',
})
export class CommunityProfileDetailComponent implements OnInit {
  profileId!: number; // Perfil que estás visitando
  myProfileId!: number; // Tu perfil en esa comunidad
  profile: CommunityProfileDTO | null = null;
  posts: PostWithToggle[] = [];
  comments: ProfileCommentDTO[] = [];
  newComment: string = '';

  editForm!: FormGroup;
  selectedImage: File | undefined;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isOwnProfile: boolean = false;

  editingCommentId: number | null = null;
  editingContent: string = '';

  titleText = '';
  titleBgColor = '#ffffff';
  titleTextColor = '#000000';

  roles: RoleDTO[] = [];
  selectedRoleId: number | null = null;
  showRoleModal: boolean = false;

  permissions: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: CommunityProfileService,
    private postService: PostService,
    private commentService: ProfileCommentService,
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private titleService: TitleService,
    private roleService: RoleService,
    private activeRoleService: ActiveRoleService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (idParam) {
        this.profileId = +idParam;

        // Obtenemos también el query param con tu perfil
        this.route.queryParamMap.subscribe((queryParams) => {
          const myProfileParam = queryParams.get('myProfileId');
          if (myProfileParam) {
            this.myProfileId = +myProfileParam;
            this.isOwnProfile = this.myProfileId === this.profileId;
          }
        });

        this.loadProfile();
        this.loadPosts();
        this.loadComments();

        const role = this.activeRoleService.getActiveRole();
        if (role) {
          this.permissions = role.permissions;
        }
      }
    });

    this.initForm();
  }

  initForm(): void {
    this.editForm = this.fb.group({
      username: [''],
      email: [''],
      description: [''],
    });
  }

  loadProfile(): void {
    this.profileService.getProfileById(this.profileId).subscribe({
      next: (data) => {
        this.profile = data;
        this.editForm.patchValue({
          username: data.username,
          description: data.description || '',
        });

        if (data.communityId) {
          this.roleService.getRolesByCommunity(data.communityId).subscribe({
            next: (roles) => (this.roles = roles),
            error: () => console.error('Error al cargar roles'),
          });
        }
      },
      error: () => (this.errorMessage = 'No se pudo cargar el perfil.'),
    });
  }

  loadPosts(): void {
    this.postService.getPostsByProfile(this.profileId).subscribe({
      next: (data) => {
        this.posts = data.map((post) => ({
          ...post,
          showContent: false,
        }));
      },
      error: () => (this.errorMessage = 'No se pudieron cargar los posts.'),
    });
  }

  loadComments(): void {
    this.commentService.getCommentsByTarget(this.profileId).subscribe({
      next: (data) => (this.comments = data),
      error: () =>
        (this.errorMessage = 'No se pudieron cargar los comentarios.'),
    });
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }

  goToPostDetail(postId: number): void {
    if (this.myProfileId) {
      this.router.navigate(['/posts', postId], {
        queryParams: { myProfileId: this.myProfileId },
      });
    } else {
      this.router.navigate(['/posts', postId]);
    }
  }

  onImageSelected(event: any): void {
    const file = event.target.files[0];
    this.selectedImage = file ?? undefined;
  }

  onUpdateProfile(): void {
    if (!this.editForm.valid || !this.profile) return;

    const request: UpdateCommunityProfileRequest = {
      userName: this.editForm.value.username,
      description: this.editForm.value.description,
    };

    this.profileService
      .updateProfile(this.profileId, request, this.selectedImage)
      .subscribe({
        next: (updated) => {
          this.profile = updated;
          this.successMessage = 'Perfil actualizado correctamente.';
          this.errorMessage = null;
        },
        error: () => {
          this.successMessage = null;
          this.errorMessage = 'Error al actualizar el perfil.';
        },
      });
  }

  onSubmitComment(): void {
    if (!this.newComment.trim() || !this.profile || !this.myProfileId) return;

    const dto: ProfileCommentCreateUpdateDTO = {
      content: this.newComment.trim(),
    };

    this.commentService
      .createComment(this.myProfileId, this.profileId, dto)
      .subscribe({
        next: (comment) => {
          this.comments.unshift(comment);
          this.newComment = '';

          if (this.myProfileId !== this.profileId) {
            const notification: NotificationCreateDTO = {
              message: 'Ha comentado tu perfil.',
              type: NotificationType.COMMENT_PROFILE,
              receiverProfileId: this.profileId,
              senderProfileId: this.myProfileId,
            };

            this.notificationService
              .createNotification(notification)
              .subscribe({
                next: () => console.log('Notificación de comentario enviada'),
                error: (err) =>
                  console.error('Error al crear notificación:', err),
              });
          }
        },
        error: () => (this.errorMessage = 'No se pudo enviar el comentario.'),
      });
  }

  assignRoleToProfile(): void {
    if (!this.selectedRoleId || !this.profile) return;

    const selectedRole = this.roles.find((r) => r.id === this.selectedRoleId);
    const roleName = selectedRole?.name || 'un rol';

    this.profileService
      .assignRoleToProfile(this.profile.id, this.selectedRoleId)
      .subscribe({
        next: () => {
          this.successMessage = 'Rol asignado correctamente.';
          this.showRoleModal = false;

          if (this.myProfileId !== this.profileId) {
            const notification: NotificationCreateDTO = {
              message: `Te ha asignado el rol "${roleName}".`,
              type: NotificationType.ROLE_ASSIGNED,
              receiverProfileId: this.profile!.id,
              senderProfileId: this.myProfileId,
            };
            this.notificationService
              .createNotification(notification)
              .subscribe();
          }
        },
        error: () => {
          this.errorMessage = 'Error al asignar el rol.';
          setTimeout(() => (this.errorMessage = null), 3000);
        },
      });
  }

  removeRoleFromProfile(roleId: number): void {
    if (!this.profile) return;

    const roleName = this.roles.find((r) => r.id === roleId)?.name || 'un rol';

    if (!confirm(`¿Eliminar el rol "${roleName}" del usuario?`)) return;

    this.profileService
      .removeRoleFromProfile(this.profile.id, roleId)
      .subscribe({
        next: () => {
          this.successMessage = 'Rol eliminado correctamente.';
          this.loadProfile();

          if (this.myProfileId !== this.profileId) {
            const notification: NotificationCreateDTO = {
              message: `Te ha eliminado el rol "${roleName}".`,
              type: NotificationType.ROLE_REMOVED,
              receiverProfileId: this.profile!.id,
              senderProfileId: this.myProfileId,
            };
            this.notificationService
              .createNotification(notification)
              .subscribe();
          }
        },
        error: () => {
          this.errorMessage = 'Error al eliminar el rol.';
          setTimeout(() => (this.errorMessage = null), 3000);
        },
      });
  }

  hasPermission(permission: string): boolean {
    return this.permissions.includes(permission);
  }

  canManageTitles(): boolean {
    return this.hasPermission('ASSIGN_TITLES');
  }

  canManageRoles(): boolean {
    return this.hasPermission('ASSIGN_ROLES');
  }

  canEdit(comment: ProfileCommentDTO): boolean {
    return (
      comment.authorProfile.id === this.myProfileId ||
      this.hasPermission('EDIT_COMMENTS')
    );
  }

  canDelete(comment: ProfileCommentDTO): boolean {
    return (
      this.profileId === this.myProfileId ||
      comment.authorProfile.id === this.myProfileId ||
      this.hasPermission('DELETE_COMMENTS')
    );
  }

  startEditing(comment: ProfileCommentDTO): void {
    this.editingCommentId = comment.id;
    this.editingContent = comment.content;
  }

  cancelEditing(): void {
    this.editingCommentId = null;
    this.editingContent = '';
  }

  updateComment(commentId: number): void {
    const dto: ProfileCommentCreateUpdateDTO = {
      content: this.editingContent.trim(),
    };

    this.commentService.updateComment(commentId, dto).subscribe({
      next: (updated) => {
        const comment = this.comments.find((c) => c.id === commentId);
        if (comment) comment.content = updated.content;
        this.cancelEditing();
      },
      error: () => (this.errorMessage = 'Error al actualizar el comentario.'),
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('¿Estás seguro de eliminar este comentario?')) return;

    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter((c) => c.id !== commentId);
      },
      error: () => (this.errorMessage = 'Error al eliminar el comentario.'),
    });
  }

  createTitle(): void {
    if (!this.profile || !this.profile.communityId) return;

    const request: CreateTitleRequest = {
      title: this.titleText,
      textColor: this.titleTextColor,
      backgroundColor: this.titleBgColor,
      communityId: this.profile.communityId,
    };

    this.titleService.createTitle(request).subscribe({
      next: (createdTitle) => {
        this.profileService
          .assignTitle(this.profile!.id, createdTitle.id)
          .subscribe({
            next: () => {
              this.profile?.titles?.push(createdTitle);
              this.titleText = '';
              this.successMessage = 'Título creado y asignado';
              setTimeout(() => (this.successMessage = null), 3000);

              // Notificación si es otro perfil
              if (this.myProfileId !== this.profile!.id) {
                const notification: NotificationCreateDTO = {
                  message: `Te ha asignado el título "${createdTitle.title}".`,
                  type: NotificationType.TITLE_ASSIGNED,
                  receiverProfileId: this.profile!.id,
                  senderProfileId: this.myProfileId,
                };
                this.notificationService
                  .createNotification(notification)
                  .subscribe();
              }
            },
            error: () => {
              this.errorMessage = 'Error al asignar el título';
              setTimeout(() => (this.errorMessage = null), 3000);
            },
          });
      },
      error: () => {
        this.errorMessage = 'Error al crear el título';
        setTimeout(() => (this.errorMessage = null), 3000);
      },
    });
  }

  updateTitle(title: TitleDTO): void {
    const request: UpdateTitleRequest = {
      title: this.titleText,
      textColor: this.titleTextColor,
      backgroundColor: this.titleBgColor,
    };

    this.titleService.updateTitle(title.id, request).subscribe({
      next: (updated) => {
        const index = this.profile?.titles?.findIndex(
          (t) => t.id === updated.id
        );
        if (index !== undefined && index !== -1 && this.profile?.titles) {
          this.profile.titles[index] = updated;
        }
        this.successMessage = 'Título actualizado';
        setTimeout(() => (this.successMessage = null), 3000);
      },
      error: () => {
        this.errorMessage = 'Error al actualizar el título';
        setTimeout(() => (this.errorMessage = null), 3000);
      },
    });
  }

  deleteTitle(titleId: number): void {
    if (!confirm('¿Eliminar este título?')) return;

    const titleName =
      this.profile?.titles?.find((t) => t.id === titleId)?.title || 'un título';

    this.titleService.deleteTitle(titleId).subscribe({
      next: () => {
        if (this.profile?.titles) {
          this.profile.titles = this.profile.titles.filter(
            (t) => t.id !== titleId
          );
        }
        this.successMessage = 'Título eliminado';
        setTimeout(() => (this.successMessage = null), 3000);

        // Notificación si es otro perfil
        if (this.myProfileId !== this.profile!.id) {
          const notification: NotificationCreateDTO = {
            message: `Te ha eliminado el título "${titleName}".`,
            type: NotificationType.TITLE_REMOVED,
            receiverProfileId: this.profile!.id,
            senderProfileId: this.myProfileId,
          };
          this.notificationService.createNotification(notification).subscribe();
        }
      },
      error: () => {
        this.errorMessage = 'Error al eliminar el título';
        setTimeout(() => (this.errorMessage = null), 3000);
      },
    });
  }
}
