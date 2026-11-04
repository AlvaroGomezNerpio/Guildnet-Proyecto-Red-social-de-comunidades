import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { ThemePreference, ThemeService } from '../../theme/theme.service';
import { LogoComponent } from '../logo/logo.component';
import { AvatarComponent } from '../../../shared/ui/avatar/avatar.component';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, LogoComponent, AvatarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  readonly theme = inject(ThemeService);

  readonly user = toSignal(this.authService.currentUser$, { initialValue: null });

  readonly themeOptions: { value: ThemePreference; label: string; icon: string }[] = [
    { value: 'light', label: 'Claro', icon: 'sun' },
    { value: 'dark', label: 'Oscuro', icon: 'moon-stars' },
    { value: 'auto', label: 'Según el sistema', icon: 'circle-half' },
  ];

  logout(): void {
    this.authService.logout(true);
  }
}
