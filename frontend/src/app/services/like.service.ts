import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LikeService {
  private apiUrl = '${environment.apiUrl}/api/likes';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // Dar like a un post
  likePost(postId: number, profileId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${postId}/${profileId}`, null, {
      headers: this.getAuthHeaders()
    });
  }

  // Quitar like de un post
  unlikePost(postId: number, profileId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}/${profileId}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Obtener número de likes de un post
  countLikes(postId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${postId}/count`, {
      headers: this.getAuthHeaders()
    });
  }

  // Comprobar si el perfil actual ha dado like
  hasUserLiked(postId: number, profileId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${postId}/liked-by/${profileId}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Obtener los perfiles que han dado like (opcional)
  getProfilesWhoLiked(postId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${postId}/profiles`, {
      headers: this.getAuthHeaders()
    });
  }
}
