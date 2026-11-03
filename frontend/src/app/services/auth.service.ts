import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserDTO } from '../models/user/UserDTO';
import { LoginRequest } from '../models/user/LoginRequest';
import { LoginResponse } from '../models/user/LoginResponse';
import { RegisterRequest } from '../models/user/RegisterRequest';

const TOKEN_KEY = 'token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/auth`;

  private readonly currentUserSubject = new BehaviorSubject<UserDTO | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.loadCurrentUser();
  }

  get currentUser(): UserDTO | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserId(): number | null {
    return this.currentUserSubject.value?.id ?? null;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(tap((response) => this.startSession(response.token)));
  }

  register(request: RegisterRequest, image?: File | null): Observable<LoginResponse> {
    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/register`, formData)
      .pipe(tap((response) => this.startSession(response.token)));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /** Cierra la sesion local. Si se indica, redirige a la pantalla de login. */
  logout(redirect = false): void {
    localStorage.removeItem(TOKEN_KEY);
    this.currentUserSubject.next(null);
    if (redirect) {
      this.router.navigate(['/login']);
    }
  }

  /** Vuelve a pedir los datos del usuario (p. ej. tras editar el perfil). */
  loadCurrentUser(): void {
    if (!this.getToken()) {
      this.currentUserSubject.next(null);
      return;
    }
    this.http.get<UserDTO>(`${environment.apiUrl}/api/v1/user/me`).subscribe({
      next: (user) => this.currentUserSubject.next(user),
      error: () => this.currentUserSubject.next(null),
    });
  }

  private startSession(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.loadCurrentUser();
  }
}
