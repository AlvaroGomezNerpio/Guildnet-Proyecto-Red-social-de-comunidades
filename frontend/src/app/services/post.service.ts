import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostDTO } from '../models/post/PostDTO';
import { PostCreateDTO } from '../models/post/PostCreateDTO';
import { PostUpdateDTO } from '../models/post/PostUpdateDTO';
import { PostDetailDTO } from '../models/post/PostDetailDTO';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private apiUrl = 'http://localhost:8080/api/v1/posts';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  createPost(dto: PostCreateDTO): Observable<PostDTO> {
    return this.http.post<PostDTO>(`${this.apiUrl}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  updatePost(id: number, dto: PostUpdateDTO): Observable<PostDTO> {
    return this.http.put<PostDTO>(`${this.apiUrl}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPostById(id: number): Observable<PostDetailDTO> {
    return this.http.get<PostDetailDTO>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPostsByProfile(profileId: number): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.apiUrl}/profile/${profileId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPostsByCommunity(communityId: number): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.apiUrl}/community/${communityId}`, {
      headers: this.getAuthHeaders(),
    });
  }

searchPostsInCommunity(
  communityId: number,
  title?: string,
  tags?: string[]
): Observable<PostDTO[]> {
  const params: any = {};
  if (title) params.title = title;
  if (tags && tags.length > 0) params.tag = tags;

  return this.http.get<PostDTO[]>(`${this.apiUrl}/search/${communityId}`, {
    params,
    headers: this.getAuthHeaders()
  });
}

}
