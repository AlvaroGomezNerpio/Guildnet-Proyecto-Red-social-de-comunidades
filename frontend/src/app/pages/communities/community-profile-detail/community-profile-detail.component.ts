import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { CommunityProfileDTO } from '../../../models/communityProfile/CommunityProfileDTO';
import { CommunityProfileService } from '../../../services/community-profile.service';
import { PostDTO } from '../../../models/post/PostDTO';
import { PostService } from '../../../services/post.service';
import { UpdateCommunityProfileRequest } from '../../../models/communityProfile/UpdateCommunityProfileRequest';
import { ProfileCommentService } from '../../../services/profile-comment.service';
import { ProfileCommentDTO } from '../../../models/profileComment/ProfileCommentDTO';
import { TitleService } from '../../../services/title.service';
import { TitleDTO } from '../../../models/title/TitleDTO';
import { RoleService } from '../../../services/role.service';
import { RoleDTO } from '../../../models/role/RoleDTO';
import { CommunityService } from '../../../services/community.service';
import { CommunityPermissionsService, Permission } from '../../../services/community-permissions.service';
import { errorMessage } from '../../../core/http-error';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
  selector: 'app-community-profile-detail',
  standalone: false,
  templateUrl: './community-profile-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './community-profile-detail.component.css',
})
export class CommunityProfileDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(CommunityProfileService);
  private readonly postService = inject(PostService);
  private readonly commentService = inject(ProfileCommentService);
  private readonly titleService = inject(TitleService);
  private readonly roleService = inject(RoleService);
  private readonly communityService = inject(CommunityService);
  private readonly permissionsService = inject(CommunityPermissionsService);

  /** Perfil que se está visitando. */
  profileId!: number;
  profile: CommunityProfileDTO | null = null;
  /** Perfil del usuario autenticado en la misma comunidad (null si no es miembro). */
  myProfile: CommunityProfileDTO | null = null;

  posts: PostWithToggle[] = [];
  comments: ProfileCommentDTO[] = [];
  newComment = '';

  readonly editForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    description: [''],
  });
  selectedImage: File | null = null;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  editingCommentId: number | null = null;
  editingContent = '';

  titleText = '';
  titleBgColor = '#ffffff';
  titleTextColor = '#000000';

  roles: RoleDTO[] = [];
  selectedRoleId: number | null = null;

  permissions: Permission[] = [];

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) return;
      this.profileId = id;
      this.loadProfile();
      this.loadPosts();
      this.loadComments();
    });
  }

  get myProfileId(): number | null {
    return this.myProfile?.id ?? null;
  }

  get isOwnProfile(): boolean {
    return this.myProfileId !== null && this.myProfileId === this.profileId;
  }

  loadProfile(): void {
    this.profileService.getProfileById(this.profileId).subscribe({
      next: (data) => {
        this.profile = data;
        this.editForm.patchValue({ username: data.username, description: data.description || '' });
        this.loadViewerContext(data.communityId);
        this.roleService.getRolesByCommunity(data.communityId).subscribe({
          next: (roles) => (this.roles = roles),
        });
      },
      error: () => this.showError('No se pudo cargar el perfil.'),
    });
  }

  /** Carga el perfil del usuario autenticado en esta comunidad y sus permisos. */
  private loadViewerContext(communityId: number): void {
    this.profileService.getMyProfileInCommunity(communityId).subscribe({
      next: (me) => (this.myProfile = me),
      error: () => (this.myProfile = null),
    });
    this.permissionsService.getMyPermissions(communityId).subscribe((perms) => (this.permissions = perms));
  }

  loadPosts(): void {
    this.postService.getPostsByProfile(this.profileId).subscribe({
      next: (data) => (this.posts = data.map((post) => ({ ...post, showContent: false }))),
      error: () => this.showError('No se pudieron cargar los posts.'),
    });
  }

  loadComments(): void {
    this.commentService.getCommentsByTarget(this.profileId).subscribe({
      next: (data) => (this.comments = data),
      error: () => this.showError('No se pudieron cargar los comentarios.'),
    });
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }

  goToPostDetail(postId: number): void {
    this.router.navigate(['/posts', postId]);
  }

  goToCommunity(): void {
    if (this.profile) {
      this.router.navigate(['/communities', this.profile.communityId]);
    }
  }

  onImageSelected(event: Event): void {
    this.selectedImage = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onUpdateProfile(): void {
    if (this.editForm.invalid || !this.profile) return;

    const request: UpdateCommunityProfileRequest = {
      userName: this.editForm.getRawValue().username,
      description: this.editForm.getRawValue().description,
    };

    this.profileService.updateProfile(this.profileId, request, this.selectedImage).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.showSuccess('Perfil actualizado correctamente.');
      },
      error: (err) => this.showError(errorMessage(err, 'Error al actualizar el perfil.')),
    });
  }

  leaveCommunity(): void {
    if (!this.profile || !confirm('¿Seguro que quieres abandonar esta comunidad?')) return;
    const communityId = this.profile.communityId;
    this.profileService.deleteProfile(this.profile.id).subscribe({
      next: () => this.router.navigate(['/communities', communityId]),
      error: (err) => this.showError(errorMessage(err, 'No se pudo abandonar la comunidad.')),
    });
  }

  // ------------------------------------------------------------------ Comentarios

  onSubmitComment(): void {
    const content = this.newComment.trim();
    if (!content || !this.myProfileId) return;

    this.commentService.createComment(this.myProfileId, this.profileId, { content }).subscribe({
      next: (comment) => {
        this.comments.unshift(comment);
        this.newComment = '';
      },
      error: (err) => this.showError(errorMessage(err, 'No se pudo enviar el comentario.')),
    });
  }

  canEdit(comment: ProfileCommentDTO): boolean {
    return comment.authorProfile.id === this.myProfileId || this.hasPermission('EDIT_COMMENTS');
  }

  canDelete(comment: ProfileCommentDTO): boolean {
    return this.isOwnProfile || comment.authorProfile.id === this.myProfileId || this.hasPermission('DELETE_COMMENTS');
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
    const content = this.editingContent.trim();
    if (!content) return;

    this.commentService.updateComment(commentId, { content }).subscribe({
      next: (updated) => {
        const comment = this.comments.find((c) => c.id === commentId);
        if (comment) comment.content = updated.content;
        this.cancelEditing();
      },
      error: (err) => this.showError(errorMessage(err, 'Error al actualizar el comentario.')),
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('¿Estás seguro de eliminar este comentario?')) return;

    this.commentService.deleteComment(commentId).subscribe({
      next: () => (this.comments = this.comments.filter((c) => c.id !== commentId)),
      error: (err) => this.showError(errorMessage(err, 'Error al eliminar el comentario.')),
    });
  }

  // ------------------------------------------------------------------ Roles

  assignRoleToProfile(): void {
    if (!this.selectedRoleId || !this.profile) return;

    this.profileService.assignRoleToProfile(this.profile.id, this.selectedRoleId).subscribe({
      next: () => {
        this.showSuccess('Rol asignado correctamente.');
        this.selectedRoleId = null;
        this.loadProfile();
      },
      error: (err) => this.showError(errorMessage(err, 'Error al asignar el rol.')),
    });
  }

  removeRoleFromProfile(roleId: number): void {
    if (!this.profile) return;
    const roleName = this.roles.find((r) => r.id === roleId)?.name || 'el rol';
    if (!confirm(`¿Quitar "${roleName}" a este usuario?`)) return;

    this.profileService.removeRoleFromProfile(this.profile.id, roleId).subscribe({
      next: () => {
        this.showSuccess('Rol eliminado correctamente.');
        this.loadProfile();
      },
      error: (err) => this.showError(errorMessage(err, 'Error al eliminar el rol.')),
    });
  }

  // ------------------------------------------------------------------ Títulos

  createTitle(): void {
    const title = this.titleText.trim();
    if (!this.profile || !title) return;

    this.titleService
      .createTitle({
        title,
        textColor: this.titleTextColor,
        backgroundColor: this.titleBgColor,
        communityId: this.profile.communityId,
      })
      .subscribe({
        next: (createdTitle) => {
          this.profileService.assignTitle(this.profile!.id, createdTitle.id).subscribe({
            next: () => {
              this.profile?.titles.push(createdTitle);
              this.titleText = '';
              this.showSuccess('Título creado y asignado.');
            },
            error: (err) => this.showError(errorMessage(err, 'Error al asignar el título.')),
          });
        },
        error: (err) => this.showError(errorMessage(err, 'Error al crear el título.')),
      });
  }

  /** Aplica el texto y los colores del formulario al título indicado. */
  updateTitle(title: TitleDTO): void {
    this.titleService
      .updateTitle(title.id, {
        title: this.titleText.trim() || title.title,
        textColor: this.titleTextColor,
        backgroundColor: this.titleBgColor,
      })
      .subscribe({
        next: (updated) => {
          if (this.profile) {
            this.profile.titles = this.profile.titles.map((t) => (t.id === updated.id ? updated : t));
          }
          this.showSuccess('Título actualizado.');
        },
        error: (err) => this.showError(errorMessage(err, 'Error al actualizar el título.')),
      });
  }

  deleteTitle(titleId: number): void {
    if (!confirm('¿Eliminar este título? Se quitará a todos los miembros que lo tengan.')) return;

    this.titleService.deleteTitle(titleId).subscribe({
      next: () => {
        if (this.profile) {
          this.profile.titles = this.profile.titles.filter((t) => t.id !== titleId);
        }
        this.showSuccess('Título eliminado.');
      },
      error: (err) => this.showError(errorMessage(err, 'Error al eliminar el título.')),
    });
  }

  setFeaturedTitle(titleId: number): void {
    if (!this.profile) return;
    this.profileService.changeFeaturedTitle(this.profile.id, titleId).subscribe({
      next: () => this.loadProfile(),
      error: (err) => this.showError(errorMessage(err, 'No se pudo destacar el título.')),
    });
  }

  // ------------------------------------------------------------------ Moderación

  removeUserFromCommunity(): void {
    if (!this.profile) return;
    if (!confirm('¿Estás seguro de expulsar a este usuario de la comunidad?')) return;

    const communityId = this.profile.communityId;
    this.communityService.removeUserFromCommunity(communityId, this.profile.id).subscribe({
      next: () => this.router.navigate(['/communities', communityId]),
      error: (err) => this.showError(errorMessage(err, 'Error al expulsar al usuario.')),
    });
  }

  hasPermission(permission: Permission): boolean {
    return this.permissions.includes(permission);
  }

  get canManageTitles(): boolean {
    return this.hasPermission('ASSIGN_TITLES');
  }

  get canManageRoles(): boolean {
    return this.hasPermission('ASSIGN_ROLES') && !this.isOwnProfile;
  }

  get canBanUser(): boolean {
    return this.hasPermission('BAN_USER') && !this.isOwnProfile;
  }

  private showSuccess(message: string): void {
    this.errorMessage = null;
    this.successMessage = message;
    setTimeout(() => (this.successMessage = null), 3000);
  }

  private showError(message: string): void {
    this.successMessage = null;
    this.errorMessage = message;
    setTimeout(() => (this.errorMessage = null), 4000);
  }
}
