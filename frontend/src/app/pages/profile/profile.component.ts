import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { CommunityService } from '../../services/community.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { UserDTO } from '../../models/user/UserDTO';
import { UpdateUserRequest } from '../../models/user/UpdateUserRequest';
import { CommunityResponseDTO } from '../../models/community/CommunityResponse.dto';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserDTO | null = null;
  subscribedCommunities: CommunityResponseDTO[] = [];
  editForm!: FormGroup;
  selectedImage: File | undefined;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  selectedCommunity: CommunityResponseDTO | null = null;

  constructor(
    private userService: UserService,
    private communityService: CommunityService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadUser();
    this.loadCommunities();
  }

  initForm(): void {
    this.editForm = this.fb.group({
      username: [''],
      email: [''],
      tags: ['']
    });
  }

  loadUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (data) => {
        this.user = data;
        this.editForm.patchValue({
          username: data.username,
          email: data.email,
          tags: data.tags?.join(', ') || ''
        });
      },
      error: () => this.errorMessage = 'No se pudo cargar el perfil.'
    });
  }

  loadCommunities(): void {
    this.communityService.getSubscribedCommunities().subscribe({
      next: (data) => this.subscribedCommunities = data,
      error: () => this.errorMessage = 'No se pudieron cargar las comunidades.'
    });
  }

  onImageSelected(event: any): void {
    const file = event.target.files[0];
    this.selectedImage = file ?? undefined;
  }

  onUpdateProfile(): void {
    if (!this.editForm.valid) return;

    const rawTags: string = this.editForm.value.tags;
    const tagsArray = rawTags
      ?.split(',')
      .map((tag: string) => tag.trim())
      .filter((tag: string) => tag.length > 0);

    const request: UpdateUserRequest = {
      username: this.editForm.value.username,
      email: this.editForm.value.email,
      tags: tagsArray
    };

    this.userService.updateUser(request, this.selectedImage).subscribe({
      next: (updated) => {
        this.user = updated;
        this.successMessage = 'Perfil actualizado correctamente.';
        this.errorMessage = null;
      },
      error: () => {
        this.successMessage = null;
        this.errorMessage = 'Error al actualizar el perfil.';
      }
    });
  }

  enterCommunity(communityId: number): void {
    this.router.navigate(['/communities', communityId]);
  }

  goToSearchPosts(communityId: number): void {
    this.router.navigate(['/communities', communityId, 'search-posts']);
  }
}
