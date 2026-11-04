import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { CommunityService } from '../../../communities/services/community.service';
import { UserDTO } from '../../../../core/auth/auth.models';
import { CommunityResponseDTO } from '../../../communities/models/community.models';
import { AuthService } from '../../../../core/auth/auth.service';
import { errorMessage } from '../../../../core/http/http-error';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { ModalComponent } from '../../../../shared/ui/modal/modal.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { CommunityCardComponent } from '../../../communities/components/community-card/community-card.component';

/** Perfil global del usuario: datos de la cuenta, intereses y comunidades. */
@Component({
  selector: 'app-user-profile',
  imports: [ReactiveFormsModule, RouterLink, AvatarComponent, ModalComponent, EmptyStateComponent, CommunityCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css',
})
export class UserProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly communityService = inject(CommunityService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  readonly user = signal<UserDTO | null>(null);
  readonly communities = signal<CommunityResponseDTO[]>([]);
  readonly editing = signal(false);
  readonly saving = signal(false);

  readonly editForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    tags: [''],
  });
  private selectedImage: File | null = null;

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.user.set(user),
      error: () => this.toast.error('No se pudo cargar el perfil.'),
    });
    this.communityService.getSubscribedCommunities().subscribe({
      next: (data) => this.communities.set(data),
    });
  }

  openEditor(): void {
    const user = this.user();
    if (!user) return;
    this.editForm.setValue({ username: user.username, email: user.email, tags: user.tags.join(', ') });
    this.selectedImage = null;
    this.editing.set(true);
  }

  onImageSelected(event: Event): void {
    this.selectedImage = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  save(): void {
    if (this.editForm.invalid) return;
    const value = this.editForm.getRawValue();
    const tags = value.tags
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    this.saving.set(true);
    this.userService.updateUser({ username: value.username.trim(), email: value.email.trim(), tags }, this.selectedImage).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.saving.set(false);
        this.editing.set(false);
        this.authService.loadCurrentUser();
        this.toast.success('Perfil actualizado.');
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(errorMessage(err, 'Error al actualizar el perfil.'));
      },
    });
  }

  openCommunity(community: CommunityResponseDTO): void {
    this.router.navigate(['/communities', community.id]);
  }

  async deleteAccount(): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar cuenta',
      message: 'Se borrarán tus perfiles, tus publicaciones y las comunidades que hayas creado. Esta acción no se puede deshacer.',
      confirmLabel: 'Eliminar mi cuenta',
      danger: true,
    });
    if (!accepted) return;

    this.userService.deleteAccount().subscribe({
      next: () => {
        this.toast.info('Tu cuenta se ha eliminado.');
        this.authService.logout(true);
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar la cuenta.')),
    });
  }
}
