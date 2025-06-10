import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostComentDTO } from '../models/postComment/PostComentDTO';
import { PostCommentCreateDTO } from '../models/postComment/PostCommentCreateDTO';
import { PostCommentUpdateDTO } from '../models/postComment/PostCommentUpdateDTO';

@Injectable({
  providedIn: 'root'
})
export class PostCommentService {
  private apiUrl = '${environment.apiUrl}/api/v1/comments';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  // Crear comentario
  createComment(dto: PostCommentCreateDTO): Observable<PostComentDTO> {
    return this.http.post<PostComentDTO>(this.apiUrl, dto, {
      headers: this.getAuthHeaders()
    });
  }

  // Actualizar comentario
  updateComment(id: number, dto: PostCommentUpdateDTO): Observable<PostComentDTO> {
    return this.http.put<PostComentDTO>(`${this.apiUrl}/${id}`, dto, {
      headers: this.getAuthHeaders()
    });
  }

  // Eliminar comentario
  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`, {
      headers: this.getAuthHeaders()
    });
  }
}
