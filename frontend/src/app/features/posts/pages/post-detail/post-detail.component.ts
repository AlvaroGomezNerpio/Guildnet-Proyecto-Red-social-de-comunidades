import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../services/post.service';
import { PostDetailDTO, PostUpdateDTO } from '../../models/post.models';
import { PostCommentService } from '../../services/post-comment.service';
import { PostCommentDTO } from '../../models/post-comment.models';
import { LikeService } from '../../services/like.service';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { CommunityPermissionsService, Permission } from '../../../communities/services/community-permissions.service';
import { errorMessage } from '../../../../core/http/http-error';
import { FormsModule } from '@angular/forms';
import { QuillEditorComponent } from 'ngx-quill';
import { DatePipe } from '@angular/common';

@Component({
    selector: 'app-post-detail',
    templateUrl: './post-detail.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './post-detail.component.css',
    imports: [
        FormsModule,
        QuillEditorComponent,
        DatePipe,
    ],
})
export class PostDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly postService = inject(PostService);
  private readonly postCommentService = inject(PostCommentService);
  private readonly likeService = inject(LikeService);
  private readonly profileService = inject(CommunityProfileService);
  private readonly permissionsService = inject(CommunityPermissionsService);

  postId!: number;
  post?: PostDetailDTO;
  /** Perfil del usuario en la comunidad del post (null si no es miembro). */
  myProfileId: number | null = null;
  error: string | null = null;

  newCommentContent = '';
  editingCommentId: number | null = null;
  editingContent = '';

  editingPost = false;
  editingPostTitle = '';
  editingPostContent = '';
  editingPostTags = '';

  likeCount = 0;
  hasLiked = false;

  permissions: Permission[] = [];

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.postId = Number(params.get('id'));
      this.likeService.countLikes(this.postId).subscribe((count) => (this.likeCount = count));
      this.loadPost(true);
    });
  }

  loadPost(loadViewer = false): void {
    this.postService.getPostById(this.postId).subscribe({
      next: (data) => {
        this.post = data;
        this.editingPostTitle = data.title;
        this.editingPostContent = data.content;
        this.editingPostTags = data.tags.join(', ');
        if (loadViewer) this.loadViewerContext(data.communityId);
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo cargar la publicación.')),
    });
  }

  private loadViewerContext(communityId: number): void {
    this.permissionsService.getMyPermissions(communityId).subscribe((perms) => (this.permissions = perms));
    this.profileService.getMyProfileInCommunity(communityId).subscribe({
      next: (me) => {
        this.myProfileId = me.id;
        this.likeService.hasUserLiked(this.postId, me.id).subscribe((liked) => (this.hasLiked = liked));
      },
      error: () => (this.myProfileId = null),
    });
  }

  hasPermission(permission: Permission): boolean {
    return this.permissions.includes(permission);
  }

  isPostOwner(): boolean {
    return !!this.post && this.post.communityProfile.id === this.myProfileId;
  }

  canEditPost(): boolean {
    return this.isPostOwner() || this.hasPermission('EDIT_POST');
  }

  canDeletePost(): boolean {
    return this.isPostOwner() || this.hasPermission('DELETE_POST');
  }

  canEditComment(comment: PostCommentDTO): boolean {
    return comment.communityProfile.id === this.myProfileId || this.hasPermission('EDIT_COMMENTS');
  }

  canDeleteComment(comment: PostCommentDTO): boolean {
    return comment.communityProfile.id === this.myProfileId || this.hasPermission('DELETE_COMMENTS');
  }

  // ------------------------------------------------------------------ Comentarios

  createComment(): void {
    const content = this.newCommentContent.trim();
    if (!this.post || !this.myProfileId || !content) return;

    this.postCommentService.createComment({ postId: this.post.id, content }).subscribe({
      next: (created) => {
        this.post?.postComment.push(created);
        this.newCommentContent = '';
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo publicar el comentario.')),
    });
  }

  startEditing(comment: PostCommentDTO): void {
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

    this.postCommentService.updateComment(commentId, { content }).subscribe({
      next: (updated) => {
        const comment = this.post?.postComment.find((c) => c.id === commentId);
        if (comment) comment.content = updated.content;
        this.cancelEditing();
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo actualizar el comentario.')),
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('¿Eliminar este comentario?')) return;

    this.postCommentService.deleteComment(commentId).subscribe({
      next: () => {
        if (this.post) {
          this.post.postComment = this.post.postComment.filter((c) => c.id !== commentId);
        }
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo eliminar el comentario.')),
    });
  }

  // ------------------------------------------------------------------ Publicación

  startEditingPost(): void {
    this.editingPost = true;
  }

  cancelEditingPost(): void {
    this.editingPost = false;
    if (this.post) {
      this.editingPostTitle = this.post.title;
      this.editingPostContent = this.post.content;
      this.editingPostTags = this.post.tags.join(', ');
    }
  }

  updatePost(): void {
    if (!this.post) return;

    const dto: PostUpdateDTO = {
      title: this.editingPostTitle.trim(),
      content: this.editingPostContent.trim(),
      tags: this.editingPostTags
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0),
    };

    this.postService.updatePost(this.postId, dto).subscribe({
      next: () => {
        this.editingPost = false;
        this.loadPost();
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo actualizar la publicación.')),
    });
  }

  deletePost(): void {
    if (!this.post || !confirm('¿Estás seguro de que quieres eliminar esta publicación?')) return;

    const communityId = this.post.communityId;
    this.postService.deletePost(this.postId).subscribe({
      next: () => this.router.navigate(['/communities', communityId]),
      error: (err) => (this.error = errorMessage(err, 'No se pudo eliminar la publicación.')),
    });
  }

  toggleLike(): void {
    if (!this.post || !this.myProfileId) return;

    const request = this.hasLiked
      ? this.likeService.unlikePost(this.post.id, this.myProfileId)
      : this.likeService.likePost(this.post.id, this.myProfileId);

    request.subscribe({
      next: () => {
        this.likeCount += this.hasLiked ? -1 : 1;
        this.hasLiked = !this.hasLiked;
      },
      error: (err) => (this.error = errorMessage(err, 'No se pudo registrar el like.')),
    });
  }

  viewProfile(profileId: number): void {
    this.router.navigate(['/communities/profile', profileId]);
  }

  goToCommunity(): void {
    if (this.post) {
      this.router.navigate(['/communities', this.post.communityId]);
    }
  }
}
