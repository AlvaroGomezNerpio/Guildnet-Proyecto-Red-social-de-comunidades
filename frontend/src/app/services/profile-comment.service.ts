import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProfileCommentDTO } from '../models/profileComment/ProfileCommentDTO';
import { ProfileCommentCreateUpdateDTO } from '../models/profileComment/ProfileCommentCreateUpdateDTO';


@Injectable({
  providedIn: 'root'
})
export class ProfileCommentService {

  private apiUrl = '${environment.apiUrl}/api/profile-comments';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  createComment(authorProfileId: number, targetProfileId: number, dto: ProfileCommentCreateUpdateDTO): Observable<ProfileCommentDTO> {
    return this.http.post<ProfileCommentDTO>(
      `${this.apiUrl}/${authorProfileId}/${targetProfileId}`,
      dto,
      { headers: this.getAuthHeaders() }
    );
  }

  updateComment(commentId: number, dto: ProfileCommentCreateUpdateDTO): Observable<ProfileCommentDTO> {
    return this.http.put<ProfileCommentDTO>(
      `${this.apiUrl}/${commentId}`,
      dto,
      { headers: this.getAuthHeaders() }
    );
  }

  getCommentsByTarget(targetProfileId: number): Observable<ProfileCommentDTO[]> {
    return this.http.get<ProfileCommentDTO[]>(
      `${this.apiUrl}/target/${targetProfileId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${commentId}`,
      { headers: this.getAuthHeaders() }
    );
  }
}

