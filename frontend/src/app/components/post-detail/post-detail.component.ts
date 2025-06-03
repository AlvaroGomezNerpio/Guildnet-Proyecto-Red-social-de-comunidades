import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../services/post.service';
import { PostDetailDTO } from '../../models/post/PostDetailDTO';
import { PostCommentService } from '../../services/post-comment.service';
import { PostComentDTO } from '../../models/postComment/PostComentDTO';
import { PostCommentCreateDTO } from '../../models/postComment/PostCommentCreateDTO';
import { PostCommentUpdateDTO } from '../../models/postComment/PostCommentUpdateDTO';
import { PostUpdateDTO } from '../../models/post/PostUpdateDTO';
import { LikeService } from '../../services/like.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationCreateDTO } from '../../models/notification/NotificationCreateDTO';
import { NotificationType } from '../../models/notification/NotificationType';
import { CommunityProfileDTO } from '../../models/communityProfile/CommunityProfileDTO';
import { CommunityProfileService } from '../../services/community-profile.service';


@Component({
  selector: 'app-post-detail',
  standalone: false,
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.css',
})
export class PostDetailComponent implements OnInit {
  postId!: number;
  post?: PostDetailDTO;
  myProfileId!: number;

  newCommentContent: string = '';
  editingCommentId: number | null = null;
  editingContent: string = '';

  editingPost: boolean = false;
  editingPostTitle: string = '';
  editingPostContent: string = '';
  editingPostTags: string = '';

  likeCount: number = 0;
  hasLiked: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private postCommentService: PostCommentService,
    private likeService: LikeService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.postId = Number(this.route.snapshot.paramMap.get('id'));

    this.route.queryParams.subscribe((params) => {
      const myProfileIdParam = Number(params['myProfileId']);
      if (myProfileIdParam) {
        this.myProfileId = myProfileIdParam;
      }
    });

    this.likeService
      .countLikes(this.postId)
      .subscribe((count) => (this.likeCount = count));

    if (this.myProfileId) {
      this.likeService
        .hasUserLiked(this.postId, this.myProfileId)
        .subscribe((status) => (this.hasLiked = status));
    }

    this.loadPost();
  }

  loadPost(): void {
    this.postService.getPostById(this.postId).subscribe({
      next: (data) => {
        this.post = data;

        // Cargar valores iniciales de edición
        this.editingPostTitle = data.title;
        this.editingPostContent = data.content;
        this.editingPostTags = data.tags.join(', ');
      },
      error: (err) => console.error('Error al cargar el post', err),
    });
  }

  createComment(): void {
    if (!this.post || !this.myProfileId || !this.newCommentContent.trim())
      return;

    const dto: PostCommentCreateDTO = {
      postId: this.post.id,
      profileId: this.myProfileId,
      content: this.newCommentContent.trim(),
    };

    this.postCommentService.createComment(dto).subscribe({
      next: (created) => {
        this.post?.postComment.push(created);
        this.newCommentContent = '';

        // ✅ Crear notificación si el autor del comentario NO es el dueño del post
        if (this.post && this.myProfileId !== this.post.communityProfile.id) {
          const notification: NotificationCreateDTO = {
            message: 'Ha comentado tu publicación.',
            type: NotificationType.COMMENT_POST,
            receiverProfileId: this.post.communityProfile.id,
            senderProfileId: this.myProfileId,
          };
          this.notificationService.createNotification(notification).subscribe();
        }
      },
      error: (err) => console.error('Error al crear comentario', err),
    });
  }

  canEdit(comment: PostComentDTO): boolean {
    return this.myProfileId === comment.communityProfile.id;
  }

  startEditing(comment: PostComentDTO): void {
    this.editingCommentId = comment.id;
    this.editingContent = comment.content;
  }

  cancelEditing(): void {
    this.editingCommentId = null;
    this.editingContent = '';
  }

  updateComment(commentId: number): void {
    const dto: PostCommentUpdateDTO = { content: this.editingContent.trim() };

    this.postCommentService.updateComment(commentId, dto).subscribe({
      next: (updated) => {
        const comment = this.post?.postComment.find((c) => c.id === commentId);
        if (comment) comment.content = updated.content;
        this.cancelEditing();
      },
      error: (err) => console.error('Error al actualizar comentario', err),
    });
  }

  deleteComment(commentId: number): void {
    this.postCommentService.deleteComment(commentId).subscribe({
      next: () => {
        if (this.post) {
          this.post.postComment = this.post.postComment.filter(
            (c) => c.id !== commentId
          );
        }
      },
      error: (err) => console.error('Error al eliminar comentario', err),
    });
  }

  isPostOwner(): boolean {
    return this.post?.communityProfile.id === this.myProfileId;
  }

  startEditingPost(): void {
    this.editingPost = true;
  }

  cancelEditingPost(): void {
    this.editingPost = false;
    // Restaurar los valores originales
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
      communityId: this.post.communityId,
    };

    this.postService.updatePost(this.postId, dto).subscribe({
      next: (updated) => {
        this.loadPost();
        this.editingPost = false;
      },
      error: (err) => console.error('Error al actualizar el post', err),
    });
  }

  deletePost(): void {
  if (!confirm('¿Estás seguro de que quieres eliminar esta publicación?'))
    return;

  this.postService.deletePost(this.postId).subscribe({
    next: () => {
      alert('Publicación eliminada');
      this.router.navigate(['/communities', this.post?.communityId]); // Redirige al detalle de la comunidad
    },
    error: (err) => console.error('Error al eliminar el post', err),
  });
}


  toggleLike(): void {
    if (!this.post || !this.myProfileId) return;

    if (this.hasLiked) {
      this.likeService
        .unlikePost(this.post.id, this.myProfileId)
        .subscribe(() => {
          this.hasLiked = false;
          this.likeCount--;
        });
    } else {
      this.likeService
        .likePost(this.post.id, this.myProfileId)
        .subscribe(() => {
          this.hasLiked = true;
          this.likeCount++;

          // ✅ Crear notificación si no es el autor del post
          if (this.post && this.myProfileId !== this.post.communityProfile.id) {
            const notification: NotificationCreateDTO = {
              message: 'Ha dado like a tu publicación.',
              type: NotificationType.LIKE_POST,
              receiverProfileId: this.post.communityProfile.id,
              senderProfileId: this.myProfileId,
            };
            this.notificationService.createNotification(notification).subscribe();
          }
        });
    }
  }

  viewProfile(profileId: number): void {
  if (this.myProfileId) {
    this.router.navigate(['/communities/profile', profileId], {
      queryParams: { myProfileId: this.myProfileId },
    });
  } else {
    this.router.navigate(['/communities/profile', profileId]);
  }
}
}
