import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  email = '';
  password = '';
  error: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.authService.login({ email: this.email, password: this.password })
      .subscribe({
        next: (res) => {
          this.authService.saveToken(res.token);
          this.router.navigate(['/communities']);
        },
        error: (err) => {
          this.error = 'Credenciales incorrectas';
        }
      });
  }
}
