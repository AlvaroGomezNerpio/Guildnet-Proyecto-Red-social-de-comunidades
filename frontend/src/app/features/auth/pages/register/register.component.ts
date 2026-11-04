import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { errorMessage } from '../../../../core/http/http-error';
import { FormsModule } from '@angular/forms';
import { AuthShellComponent } from '../../components/auth-shell/auth-shell.component';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './register.component.css',
    imports: [FormsModule, RouterLink, AuthShellComponent]
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  email = '';
  password = '';
  image: File | null = null;
  error: string | null = null;
  loading = false;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.image = input.files?.[0] ?? null;
  }

  onSubmit(): void {
    this.loading = true;
    this.error = null;
    this.authService
      .register({ username: this.username, email: this.email, password: this.password }, this.image)
      .subscribe({
        next: () => this.router.navigate(['/communities']),
        error: (err) => {
          this.loading = false;
          this.error = errorMessage(err, 'No se pudo registrar el usuario.');
        },
      });
  }
}
