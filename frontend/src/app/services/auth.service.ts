// ✅ auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { UserDTO } from '../models/user/UserDTO';
import { LoginRequest } from '../models/user/LoginRequest';
import { LoginResponse } from '../models/user/LoginResponse';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = '${environment.apiUrl}/api/v1/auth';
  private meUrl = '${environment.apiUrl}/api/v1/user/me';

  private currentUserSubject = new BehaviorSubject<UserDTO | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadCurrentUser();
  }

  getCurrentUserId(): number | null {
    return this.currentUserSubject.value?.id ?? null;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((response) => {
          this.saveToken(response.token);
          this.loadCurrentUser();
        })
      );
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  loadCurrentUser(): void {
    const token = this.getToken();
    if (!token) {
      this.currentUserSubject.next(null);
      return;
    }

    this.http
      .get<UserDTO>(this.meUrl, {
        headers: this.getAuthHeaders(),
      })
      .subscribe({
        next: (user) => this.currentUserSubject.next(user),
        error: () => this.currentUserSubject.next(null),
      });
  }
}
