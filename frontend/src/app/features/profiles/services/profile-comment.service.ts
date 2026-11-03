import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProfileCommentDTO, ProfileCommentCreateUpdateDTO } from '../models/profile-comment.models';

@Injectable({ providedIn: 'root' })
export class ProfileCommentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/profile-comments`;

  createComment(authorProfileId: number, targetProfileId: number, dto: ProfileCommentCreateUpdateDTO): Observable<ProfileCommentDTO> {
    return this.http.post<ProfileCommentDTO>(`${this.apiUrl}/${authorProfileId}/${targetProfileId}`, dto);
  }

  updateComment(commentId: number, dto: ProfileCommentCreateUpdateDTO): Observable<ProfileCommentDTO> {
    return this.http.put<ProfileCommentDTO>(`${this.apiUrl}/${commentId}`, dto);
  }

  getCommentsByTarget(targetProfileId: number): Observable<ProfileCommentDTO[]> {
    return this.http.get<ProfileCommentDTO[]>(`${this.apiUrl}/target/${targetProfileId}`);
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`);
  }
}
