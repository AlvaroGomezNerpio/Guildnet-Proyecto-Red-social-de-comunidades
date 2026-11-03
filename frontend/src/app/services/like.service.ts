import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LikeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/likes`;

  likePost(postId: number, profileId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${postId}/${profileId}`, null);
  }

  unlikePost(postId: number, profileId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}/${profileId}`);
  }

  countLikes(postId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${postId}/count`);
  }

  hasUserLiked(postId: number, profileId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${postId}/liked-by/${profileId}`);
  }

  getProfilesWhoLiked(postId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${postId}/profiles`);
  }
}
