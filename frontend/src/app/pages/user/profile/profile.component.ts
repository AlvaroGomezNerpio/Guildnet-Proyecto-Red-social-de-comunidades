import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { CommunityService } from '../../../services/community.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { UserDTO } from '../../../models/user/UserDTO';
import { UpdateUserRequest } from '../../../models/user/UpdateUserRequest';
import { CommunityResponseDTO } from '../../../models/community/CommunityResponse.dto';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { errorMessage } from '../../../core/http-error';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserDTO | null = null;
  subscribedCommunities: CommunityResponseDTO[] = [];
  editForm!: FormGroup;
  selectedImage: File | null = null;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  selectedCommunity: CommunityResponseDTO | null = null;

  constructor(
    private userService: UserService,
    private communityService: CommunityService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
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

  onImageSelected(event: Event): void {
    this.selectedImage = (event.target as HTMLInputElement).files?.[0] ?? null;
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
        // Refresca los datos del usuario en la cabecera
        this.authService.loadCurrentUser();
      },
      error: (err) => {
        this.successMessage = null;
        this.errorMessage = errorMessage(err, 'Error al actualizar el perfil.');
      }
    });
  }

  deleteAccount(): void {
    if (!confirm('¿Seguro que quieres eliminar tu cuenta? Se borrarán tus perfiles y las comunidades que hayas creado. Esta acción no se puede deshacer.')) {
      return;
    }
    this.userService.deleteAccount().subscribe({
      next: () => {
        document.querySelector('.modal-backdrop')?.remove();
        document.body.classList.remove('modal-open');
        this.authService.logout(true);
      },
      error: (err) => (this.errorMessage = errorMessage(err, 'No se pudo eliminar la cuenta.')),
    });
  }

  enterCommunity(communityId: number): void {
    this.router.navigate(['/communities', communityId]);
  }

  goToSearchPosts(communityId: number): void {
    this.router.navigate(['/communities', communityId, 'search-posts']);
  }
}
