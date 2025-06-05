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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: CommunityProfileService,
    private postService: PostService,
    private commentService: ProfileCommentService,
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private titleService: TitleService
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

  canEdit(comment: ProfileCommentDTO): boolean {
    return comment.authorProfile.id === this.myProfileId;
  }

  canDelete(comment: ProfileCommentDTO): boolean {
    return (
      this.profileId === this.myProfileId ||
      comment.authorProfile.id === this.myProfileId
    );
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

    this.titleService.deleteTitle(titleId).subscribe({
      next: () => {
        if (this.profile?.titles) {
          this.profile.titles = this.profile.titles.filter(
            (t) => t.id !== titleId
          );
        }
        this.successMessage = 'Título eliminado';
        setTimeout(() => (this.successMessage = null), 3000);
      },
      error: () => {
        this.errorMessage = 'Error al eliminar el título';
        setTimeout(() => (this.errorMessage = null), 3000);
      },
    });
  }
}
