import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PostCommentDTO } from '../models/postComment/PostCommentDTO';
import { PostCommentCreateDTO } from '../models/postComment/PostCommentCreateDTO';
import { PostCommentUpdateDTO } from '../models/postComment/PostCommentUpdateDTO';

@Injectable({ providedIn: 'root' })
export class PostCommentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/comments`;

  createComment(dto: PostCommentCreateDTO): Observable<PostCommentDTO> {
    return this.http.post<PostCommentDTO>(this.apiUrl, dto);
  }

  updateComment(id: number, dto: PostCommentUpdateDTO): Observable<PostCommentDTO> {
    return this.http.put<PostCommentDTO>(`${this.apiUrl}/${id}`, dto);
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`);
  }
}
