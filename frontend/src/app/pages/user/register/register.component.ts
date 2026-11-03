import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { errorMessage } from '../../../core/http-error';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './register.component.css'
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
