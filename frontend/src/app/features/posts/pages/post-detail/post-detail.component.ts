import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { QuillEditorComponent } from 'ngx-quill';
import { PostService } from '../../services/post.service';
import { PostCommentService } from '../../services/post-comment.service';
import { LikeService } from '../../services/like.service';
import { PostDetailDTO } from '../../models/post.models';
import { PostCommentDTO } from '../../models/post-comment.models';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { CommunityPermissionsService, Permission } from '../../../communities/services/community-permissions.service';
import { errorMessage } from '../../../../core/http/http-error';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';

@Component({
  selector: 'app-post-detail',
  imports: [FormsModule, RouterLink, QuillEditorComponent, AvatarComponent, TagBadgeComponent, RelativeTimePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.css',
})
export class PostDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly postService = inject(PostService);
  private readonly commentService = inject(PostCommentService);
  private readonly likeService = inject(LikeService);
  private readonly profileService = inject(CommunityProfileService);
  private readonly permissionsService = inject(CommunityPermissionsService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  postId = 0;
  readonly post = signal<PostDetailDTO | null>(null);
  readonly notFound = signal(false);
  /** Perfil del usuario en la comunidad del post (null si no es miembro). */
  readonly myProfileId = signal<number | null>(null);
  readonly permissions = signal<Permission[]>([]);
  readonly likeCount = signal(0);
  readonly hasLiked = signal(false);

  readonly editing = signal(false);
  editTitle = '';
  editContent = '';
  editTags = '';

  newComment = '';
  readonly editingCommentId = signal<number | null>(null);
  editingCommentText = '';

  readonly isAuthor = computed(() => {
    const p = this.post();
    return !!p && p.communityProfile.id === this.myProfileId();
  });
  readonly canEditPost = computed(() => this.isAuthor() || this.permissions().includes('EDIT_POST'));
  readonly canDeletePost = computed(() => this.isAuthor() || this.permissions().includes('DELETE_POST'));
  readonly wasEdited = computed(() => {
    const p = this.post();
    if (!p?.createdAt || !p.updatedAt) return false;
    return new Date(p.updatedAt).getTime() - new Date(p.createdAt).getTime() > 60_000;
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.postId = Number(params.get('id'));
      this.editing.set(false);
      this.likeService.countLikes(this.postId).subscribe((count) => this.likeCount.set(count));
      this.loadPost(true);
    });
  }

  private loadPost(withViewer = false): void {
    this.postService.getPostById(this.postId).subscribe({
      next: (data) => {
        this.post.set(data);
        if (withViewer) this.loadViewerContext(data.communityId);
      },
      error: () => this.notFound.set(true),
    });
  }

  private loadViewerContext(communityId: number): void {
    this.permissionsService.getMyPermissions(communityId).subscribe((perms) => this.permissions.set(perms));
    this.profileService.getMyProfileInCommunity(communityId).subscribe({
      next: (me) => {
        this.myProfileId.set(me.id);
        this.likeService.hasUserLiked(this.postId, me.id).subscribe((liked) => this.hasLiked.set(liked));
      },
      error: () => this.myProfileId.set(null),
    });
  }

  // ------------------------------------------------------------------ Publicación

  startEditing(): void {
    const p = this.post();
    if (!p) return;
    this.editTitle = p.title;
    this.editContent = p.content;
    this.editTags = p.tags.join(', ');
    this.editing.set(true);
  }

  savePost(): void {
    const tags = this.editTags
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);
    this.postService.updatePost(this.postId, { title: this.editTitle.trim(), content: this.editContent, tags }).subscribe({
      next: () => {
        this.editing.set(false);
        this.toast.success('Publicación actualizada.');
        this.loadPost();
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo actualizar la publicación.')),
    });
  }

  async deletePost(): Promise<void> {
    const p = this.post();
    if (!p) return;
    const accepted = await this.confirm.ask({
      title: 'Eliminar publicación',
      message: 'Se borrarán también sus comentarios y likes.',
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!accepted) return;
    this.postService.deletePost(this.postId).subscribe({
      next: () => {
        this.toast.info('Publicación eliminada.');
        this.router.navigate(['/communities', p.communityId]);
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar la publicación.')),
    });
  }

  toggleLike(): void {
    const profileId = this.myProfileId();
    if (!profileId) return;
    const liked = this.hasLiked();
    // Actualización optimista: se revierte si falla
    this.hasLiked.set(!liked);
    this.likeCount.update((n) => n + (liked ? -1 : 1));
    const request = liked ? this.likeService.unlikePost(this.postId, profileId) : this.likeService.likePost(this.postId, profileId);
    request.subscribe({
      error: (err) => {
        this.hasLiked.set(liked);
        this.likeCount.update((n) => n + (liked ? 1 : -1));
        this.toast.error(errorMessage(err, 'No se pudo registrar el like.'));
      },
    });
  }

  // ------------------------------------------------------------------ Comentarios

  canEditComment(comment: PostCommentDTO): boolean {
    return comment.communityProfile.id === this.myProfileId() || this.permissions().includes('EDIT_COMMENTS');
  }

  canDeleteComment(comment: PostCommentDTO): boolean {
    return comment.communityProfile.id === this.myProfileId() || this.permissions().includes('DELETE_COMMENTS');
  }

  submitComment(): void {
    const content = this.newComment.trim();
    if (!content) return;
    this.commentService.createComment({ postId: this.postId, content }).subscribe({
      next: (created) => {
        this.post.update((p) => (p ? { ...p, postComment: [...p.postComment, created] } : p));
        this.newComment = '';
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo publicar el comentario.')),
    });
  }

  startEditingComment(comment: PostCommentDTO): void {
    this.editingCommentId.set(comment.id);
    this.editingCommentText = comment.content;
  }

  saveComment(commentId: number): void {
    const content = this.editingCommentText.trim();
    if (!content) return;
    this.commentService.updateComment(commentId, { content }).subscribe({
      next: (updated) => {
        this.post.update((p) => (p ? { ...p, postComment: p.postComment.map((c) => (c.id === commentId ? updated : c)) } : p));
        this.editingCommentId.set(null);
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo actualizar el comentario.')),
    });
  }

  async deleteComment(commentId: number): Promise<void> {
    if (!(await this.confirm.ask({ message: '¿Eliminar este comentario?', confirmLabel: 'Eliminar', danger: true }))) return;
    this.commentService.deleteComment(commentId).subscribe({
      next: () => this.post.update((p) => (p ? { ...p, postComment: p.postComment.filter((c) => c.id !== commentId) } : p)),
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar el comentario.')),
    });
  }
}
