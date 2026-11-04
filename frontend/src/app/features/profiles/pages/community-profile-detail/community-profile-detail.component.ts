import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { QuillEditorComponent } from 'ngx-quill';
import { CommunityProfileDTO } from '../../models/profile.models';
import { ProfileCommentDTO } from '../../models/profile-comment.models';
import { TitleDTO } from '../../models/title.models';
import { RoleDTO } from '../../../roles/models/role.models';
import { PostDTO } from '../../../posts/models/post.models';
import { CommunityProfileService } from '../../services/community-profile.service';
import { ProfileCommentService } from '../../services/profile-comment.service';
import { TitleService } from '../../services/title.service';
import { RoleService } from '../../../roles/services/role.service';
import { PostService } from '../../../posts/services/post.service';
import { CommunityService } from '../../../communities/services/community.service';
import { CommunityPermissionsService, Permission } from '../../../communities/services/community-permissions.service';
import { errorMessage } from '../../../../core/http/http-error';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { ModalComponent } from '../../../../shared/ui/modal/modal.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { PostCardComponent } from '../../../posts/components/post-card/post-card.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';

type Tab = 'posts' | 'comments';
type Dialog = 'edit' | 'titles' | 'role' | null;

/** Perfil de un miembro dentro de una comunidad. */
@Component({
  selector: 'app-community-profile-detail',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    QuillEditorComponent,
    AvatarComponent,
    TagBadgeComponent,
    ModalComponent,
    EmptyStateComponent,
    PostCardComponent,
    RelativeTimePipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-profile-detail.component.html',
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
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  profileId = 0;
  /** Perfil que se está visitando. */
  readonly profile = signal<CommunityProfileDTO | null>(null);
  /** Perfil del usuario autenticado en la misma comunidad (null si no es miembro). */
  readonly myProfile = signal<CommunityProfileDTO | null>(null);
  readonly permissions = signal<Permission[]>([]);
  readonly roles = signal<RoleDTO[]>([]);

  readonly tab = signal<Tab>('posts');
  readonly dialog = signal<Dialog>(null);

  readonly posts = signal<PostDTO[]>([]);
  readonly postsTotal = signal(0);
  readonly hasMorePosts = signal(false);
  private postsPage = 0;

  readonly comments = signal<ProfileCommentDTO[]>([]);
  newComment = '';
  readonly editingCommentId = signal<number | null>(null);
  editingContent = '';

  readonly isOwnProfile = computed(() => {
    const me = this.myProfile();
    return !!me && me.id === this.profileId;
  });
  readonly canManageTitles = computed(() => this.permissions().includes('ASSIGN_TITLES'));
  readonly canManageRoles = computed(() => this.permissions().includes('ASSIGN_ROLES') && !this.isOwnProfile());
  readonly canBan = computed(() => this.permissions().includes('BAN_USER') && !this.isOwnProfile());

  readonly editForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    description: [''],
  });
  private selectedImage: File | null = null;

  titleText = '';
  titleTextColor = '#ffffff';
  titleBgColor = '#6f42c1';
  selectedRoleId: number | null = null;

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) return;
      this.profileId = id;
      this.tab.set('posts');
      this.loadProfile(true);
      this.loadPosts();
      this.loadComments();
    });
  }

  private loadProfile(withViewer = false): void {
    this.profileService.getProfileById(this.profileId).subscribe({
      next: (data) => {
        this.profile.set(data);
        if (withViewer) this.loadViewerContext(data.communityId);
      },
      error: () => this.toast.error('No se pudo cargar el perfil.'),
    });
  }

  /** Perfil del usuario autenticado en esta comunidad, sus permisos y los roles disponibles. */
  private loadViewerContext(communityId: number): void {
    this.profileService.getMyProfileInCommunity(communityId).subscribe({
      next: (me) => this.myProfile.set(me),
      error: () => this.myProfile.set(null),
    });
    this.permissionsService.getMyPermissions(communityId).subscribe((perms) => this.permissions.set(perms));
    this.roleService.getRolesByCommunity(communityId).subscribe((roles) => this.roles.set(roles));
  }

  loadPosts(more = false): void {
    const page = more ? this.postsPage + 1 : 0;
    this.postService.getPostsByProfile(this.profileId, page).subscribe({
      next: (result) => {
        this.posts.update((current) => (more ? [...current, ...result.content] : result.content));
        this.postsPage = result.page;
        this.postsTotal.set(result.totalElements);
        this.hasMorePosts.set(!result.last);
      },
      error: () => this.toast.error('No se pudieron cargar las publicaciones.'),
    });
  }

  private loadComments(): void {
    this.commentService.getCommentsByTarget(this.profileId).subscribe({
      next: (data) => this.comments.set(data),
      error: () => this.toast.error('No se pudieron cargar los comentarios.'),
    });
  }

  // ------------------------------------------------------------------ Edición del propio perfil

  openEditor(): void {
    const p = this.profile();
    if (!p) return;
    this.editForm.setValue({ username: p.username, description: p.description ?? '' });
    this.selectedImage = null;
    this.dialog.set('edit');
  }

  onImageSelected(event: Event): void {
    this.selectedImage = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  saveProfile(): void {
    if (this.editForm.invalid) return;
    const value = this.editForm.getRawValue();
    this.profileService.updateProfile(this.profileId, { userName: value.username, description: value.description }, this.selectedImage).subscribe({
      next: (updated) => {
        this.profile.set(updated);
        this.myProfile.set(updated);
        this.dialog.set(null);
        this.toast.success('Perfil actualizado.');
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al actualizar el perfil.')),
    });
  }

  setFeaturedTitle(titleId: number): void {
    this.profileService.changeFeaturedTitle(this.profileId, titleId).subscribe({
      next: () => this.loadProfile(),
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo destacar el título.')),
    });
  }

  async leaveCommunity(): Promise<void> {
    const p = this.profile();
    if (!p) return;
    const accepted = await this.confirm.ask({
      title: 'Abandonar comunidad',
      message: 'Se borrará tu perfil en esta comunidad, junto con tus publicaciones y comentarios.',
      confirmLabel: 'Abandonar',
      danger: true,
    });
    if (!accepted) return;
    this.profileService.deleteProfile(p.id).subscribe({
      next: () => {
        this.toast.info('Has abandonado la comunidad.');
        this.router.navigate(['/communities', p.communityId]);
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo abandonar la comunidad.')),
    });
  }

  // ------------------------------------------------------------------ Comentarios

  submitComment(): void {
    const content = this.newComment.trim();
    const me = this.myProfile();
    if (!content || !me) return;
    this.commentService.createComment(me.id, this.profileId, { content }).subscribe({
      next: (comment) => {
        this.comments.update((list) => [comment, ...list]);
        this.newComment = '';
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo enviar el comentario.')),
    });
  }

  canEdit(comment: ProfileCommentDTO): boolean {
    return comment.authorProfile.id === this.myProfile()?.id || this.permissions().includes('EDIT_COMMENTS');
  }

  canDelete(comment: ProfileCommentDTO): boolean {
    return this.isOwnProfile() || comment.authorProfile.id === this.myProfile()?.id || this.permissions().includes('DELETE_COMMENTS');
  }

  startEditing(comment: ProfileCommentDTO): void {
    this.editingCommentId.set(comment.id);
    this.editingContent = comment.content;
  }

  saveComment(commentId: number): void {
    const content = this.editingContent.trim();
    if (!content) return;
    this.commentService.updateComment(commentId, { content }).subscribe({
      next: (updated) => {
        this.comments.update((list) => list.map((c) => (c.id === commentId ? updated : c)));
        this.editingCommentId.set(null);
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al actualizar el comentario.')),
    });
  }

  async deleteComment(commentId: number): Promise<void> {
    if (!(await this.confirm.ask({ message: '¿Eliminar este comentario?', confirmLabel: 'Eliminar', danger: true }))) return;
    this.commentService.deleteComment(commentId).subscribe({
      next: () => this.comments.update((list) => list.filter((c) => c.id !== commentId)),
      error: (err) => this.toast.error(errorMessage(err, 'Error al eliminar el comentario.')),
    });
  }

  // ------------------------------------------------------------------ Roles

  assignRole(): void {
    if (!this.selectedRoleId) return;
    this.profileService.assignRoleToProfile(this.profileId, this.selectedRoleId).subscribe({
      next: () => {
        this.toast.success('Rol asignado.');
        this.selectedRoleId = null;
        this.dialog.set(null);
        this.loadProfile();
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al asignar el rol.')),
    });
  }

  removeRole(role: RoleDTO): void {
    this.profileService.removeRoleFromProfile(this.profileId, role.id).subscribe({
      next: () => {
        this.toast.success('Rol retirado.');
        this.loadProfile();
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al quitar el rol.')),
    });
  }

  // ------------------------------------------------------------------ Títulos

  createTitle(): void {
    const p = this.profile();
    const title = this.titleText.trim();
    if (!p || !title) return;

    this.titleService
      .createTitle({ title, textColor: this.titleTextColor, backgroundColor: this.titleBgColor, communityId: p.communityId })
      .subscribe({
        next: (created) => {
          this.profileService.assignTitle(p.id, created.id).subscribe({
            next: () => {
              this.titleText = '';
              this.toast.success('Título creado y asignado.');
              this.loadProfile();
            },
            error: (err) => this.toast.error(errorMessage(err, 'Error al asignar el título.')),
          });
        },
        error: (err) => this.toast.error(errorMessage(err, 'Error al crear el título.')),
      });
  }

  removeTitle(title: TitleDTO): void {
    this.profileService.removeTitle(this.profileId, title.id).subscribe({
      next: () => {
        this.toast.success('Título retirado del perfil.');
        this.loadProfile();
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al retirar el título.')),
    });
  }

  async deleteTitle(title: TitleDTO): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar título',
      message: `Se eliminará "${title.title}" de la comunidad y de todos los miembros que lo tengan.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!accepted) return;
    this.titleService.deleteTitle(title.id).subscribe({
      next: () => {
        this.toast.success('Título eliminado.');
        this.loadProfile();
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al eliminar el título.')),
    });
  }

  // ------------------------------------------------------------------ Moderación

  async banUser(): Promise<void> {
    const p = this.profile();
    if (!p) return;
    const accepted = await this.confirm.ask({
      title: 'Expulsar miembro',
      message: `¿Expulsar a ${p.username} de la comunidad? Se borrarán su perfil y su contenido.`,
      confirmLabel: 'Expulsar',
      danger: true,
    });
    if (!accepted) return;
    this.communityService.removeUserFromCommunity(p.communityId, p.id).subscribe({
      next: () => {
        this.toast.info(`${p.username} ha sido expulsado.`);
        this.router.navigate(['/communities', p.communityId]);
      },
      error: (err) => this.toast.error(errorMessage(err, 'Error al expulsar al usuario.')),
    });
  }
}
