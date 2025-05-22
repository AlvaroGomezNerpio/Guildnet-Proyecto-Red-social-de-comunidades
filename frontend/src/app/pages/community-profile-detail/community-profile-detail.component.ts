import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommunityProfileDTO } from '../../models/communityProfile/CommunityProfileDTO';
import { CommunityProfileService } from '../../services/community-profile.service';
import { PostDTO } from '../../models/post/PostDTO';
import { PostService } from '../../services/post.service';
import { UpdateCommunityProfileRequest } from '../../models/communityProfile/UpdateCommunityProfileRequest';
import { ProfileCommentService } from '../../services/profile-comment.service';
import { ProfileCommentDTO } from '../../models/profileComment/ProfileCommentDTO';
import { ProfileCommentCreateUpdateDTO } from '../../models/profileComment/ProfileCommentCreateUpdateDTO';

@Component({
  selector: 'app-community-profile-detail',
  standalone: false,
  templateUrl: './community-profile-detail.component.html',
  styleUrl: './community-profile-detail.component.css'
})
export class CommunityProfileDetailComponent implements OnInit {
  profileId!: number;
  profile: CommunityProfileDTO | null = null;
  posts: PostDTO[] = [];
  comments: ProfileCommentDTO[] = [];
  newComment: string = '';

  editForm!: FormGroup;
  selectedImage: File | undefined;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private profileService: CommunityProfileService,
    private postService: PostService,
    private commentService: ProfileCommentService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.profileId = +idParam;
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
    this.profileService.getMyProfileInCommunity(this.profileId).subscribe({
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
      next: data => this.posts = data,
      error: () => this.errorMessage = 'No se pudieron cargar los posts.'
    });
  }

  loadComments(): void {
    this.commentService.getCommentsByTarget(this.profileId).subscribe({
      next: data => this.comments = data,
      error: () => this.errorMessage = 'No se pudieron cargar los comentarios.'
    });
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
    if (!this.newComment.trim() || !this.profile) return;

    const dto: ProfileCommentCreateUpdateDTO = {
      content: this.newComment.trim()
    };

    this.commentService.createComment(this.profile.id, this.profileId, dto).subscribe({
      next: comment => {
        this.comments.unshift(comment);
        this.newComment = '';
      },
      error: () => this.errorMessage = 'No se pudo enviar el comentario.'
    });
  }
}
