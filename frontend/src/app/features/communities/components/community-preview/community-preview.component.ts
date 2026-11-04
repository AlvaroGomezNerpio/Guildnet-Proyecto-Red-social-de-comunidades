import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityResponseDTO } from '../../models/community.models';
import { CommunityProfileService } from '../../../profiles/services/community-profile.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { errorMessage } from '../../../../core/http/http-error';
import { ModalComponent } from '../../../../shared/ui/modal/modal.component';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';

/**
 * Vista previa de una comunidad en un modal, con las acciones de unirse o entrar.
 * Se usa en el listado de comunidades, en la búsqueda y en el perfil del usuario.
 */
@Component({
  selector: 'app-community-preview',
  imports: [ModalComponent, AvatarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-preview.component.html',
})
export class CommunityPreviewComponent {
  private readonly router = inject(Router);
  private readonly profileService = inject(CommunityProfileService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);

  /** Comunidad a mostrar; null cierra el modal. */
  readonly community = input<CommunityResponseDTO | null>(null);
  /** El usuario ya es miembro de la comunidad. */
  readonly member = input(false);
  readonly closed = output<void>();

  protected readonly joining = signal(false);

  get loggedIn(): boolean {
    return this.authService.isAuthenticated();
  }

  enter(id: number): void {
    this.closed.emit();
    this.router.navigate(['/communities', id]);
  }

  join(community: CommunityResponseDTO): void {
    if (!this.loggedIn) {
      this.closed.emit();
      this.router.navigate(['/login']);
      return;
    }
    this.joining.set(true);
    this.profileService.joinCommunity(community.id).subscribe({
      next: () => {
        this.joining.set(false);
        this.toast.success(`¡Te has unido a ${community.name}!`);
        this.enter(community.id);
      },
      error: (err) => {
        this.joining.set(false);
        this.toast.error(errorMessage(err, 'No se pudo unir a la comunidad.'));
      },
    });
  }
}
