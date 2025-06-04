import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  image: File | null = null;
  error: string | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.image = input.files[0];
    }
  }

  onSubmit(): void {
    const formData = new FormData();
    formData.append('user', JSON.stringify({
      username: this.username,
      email: this.email,
      password: this.password
    }));

    if (this.image) {
      formData.append('image', this.image);
    }

    this.http.post<any>('http://localhost:8080/api/v1/auth/register', formData)
      .subscribe({
        next: (res) => {
          localStorage.setItem('token', res.token);
          this.router.navigate(['/communities']);
        },
        error: () => {
          this.error = 'No se pudo registrar el usuario.';
        }
      });
  }

}
