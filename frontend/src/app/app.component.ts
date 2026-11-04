import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/layout/header/header.component';
import { FooterComponent } from './core/layout/footer/footer.component';
import { ToastContainerComponent } from './shared/ui/toast/toast-container.component';
import { ConfirmDialogComponent } from './shared/ui/confirm/confirm-dialog.component';
import { CommunityThemeService } from './core/theme/community-theme.service';
import { PaletteDirective } from './core/theme/palette.directive';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [HeaderComponent, RouterOutlet, FooterComponent, ToastContainerComponent, ConfirmDialogComponent, PaletteDirective],
})
export class AppComponent {
  /** Paleta de la comunidad que se está viendo; la cabecera y el pie quedan fuera. */
  protected readonly communityTheme = inject(CommunityThemeService);
}
