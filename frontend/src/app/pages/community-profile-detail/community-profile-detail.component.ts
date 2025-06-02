import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommunityProfileDTO } from '../../models/communityProfile/CommunityProfileDTO';
import { CommunityProfileService } from '../../services/community-profile.service';
import { PostDTO } from '../../models/post/PostDTO';
import { PostService } from '../../services/post.service';
import { UpdateCommunityProfileRequest } from '../../models/communityProfile/UpdateCommunityProfileRequest';
import { ProfileCommentService } from '../../services/profile-comment.service';
import { ProfileCommentDTO } from '../../models/profileComment/ProfileCommentDTO';
import { ProfileCommentCreateUpdateDTO } from '../../models/profileComment/ProfileCommentCreateUpdateDTO';
import { AuthService } from '../../services/auth.service';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
  selector: 'app-community-profile-detail',
  standalone: false,
  templateUrl: './community-profile-detail.component.html',
  styleUrl: './community-profile-detail.component.css'
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: CommunityProfileService,
    private postService: PostService,
    private commentService: ProfileCommentService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.profileId = +idParam;

        // Obtenemos también el query param con tu perfil
        this.route.queryParamMap.subscribe(queryParams => {
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
      description: ['']
    });
  }

  loadProfile(): void {
    this.profileService.getProfileById(this.profileId).subscribe({
      next: data => {
        this.profile = data;
        this.editForm.patchValue({
          username: data.username,
          description: data.description || ''
        });
      },
      error: () => this.errorMessage = 'No se pudo cargar el perfil.'
    });
  }

  loadPosts(): void {
    this.postService.getPostsByProfile(this.profileId).subscribe({
      next: data => {
        this.posts = data.map(post => ({
          ...post,
          showContent: false
        }));
      },
      error: () => this.errorMessage = 'No se pudieron cargar los posts.'
    });
  }

  loadComments(): void {
    this.commentService.getCommentsByTarget(this.profileId).subscribe({
      next: data => this.comments = data,
      error: () => this.errorMessage = 'No se pudieron cargar los comentarios.'
    });
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }

  goToPostDetail(postId: number): void {
    if (this.myProfileId) {
      this.router.navigate(['/posts', postId], {
        queryParams: { myProfileId: this.myProfileId }
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
      description: this.editForm.value.description
    };

    this.profileService.updateProfile(this.profileId, request, this.selectedImage).subscribe({
      next: updated => {
        this.profile = updated;
        this.successMessage = 'Perfil actualizado correctamente.';
        this.errorMessage = null;
      },
      error: () => {
        this.successMessage = null;
        this.errorMessage = 'Error al actualizar el perfil.';
      }
    });
  }

  onSubmitComment(): void {
    if (!this.newComment.trim() || !this.profile || !this.myProfileId) return;

    const dto: ProfileCommentCreateUpdateDTO = {
      content: this.newComment.trim()
    };

    this.commentService.createComment(this.myProfileId, this.profileId, dto).subscribe({
      next: comment => {
        this.comments.unshift(comment);
        this.newComment = '';
      },
      error: () => this.errorMessage = 'No se pudo enviar el comentario.'
    });
  }
}
