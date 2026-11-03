import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PostDTO, PostCreateDTO, PostUpdateDTO, PostDetailDTO } from '../models/post.models';

@Injectable({ providedIn: 'root' })
export class PostService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/posts`;

  createPost(dto: PostCreateDTO): Observable<PostDTO> {
    return this.http.post<PostDTO>(this.apiUrl, dto);
  }

  updatePost(id: number, dto: PostUpdateDTO): Observable<PostDTO> {
    return this.http.put<PostDTO>(`${this.apiUrl}/${id}`, dto);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getPostById(id: number): Observable<PostDetailDTO> {
    return this.http.get<PostDetailDTO>(`${this.apiUrl}/${id}`);
  }

  getPostsByProfile(profileId: number): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.apiUrl}/profile/${profileId}`);
  }

  getPostsByCommunity(communityId: number): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.apiUrl}/community/${communityId}`);
  }

  searchPostsInCommunity(communityId: number, title?: string, tags?: string[]): Observable<PostDTO[]> {
    let params = new HttpParams();
    if (title) params = params.set('title', title);
    (tags ?? []).forEach((tag) => (params = params.append('tag', tag)));
    return this.http.get<PostDTO[]>(`${this.apiUrl}/search/${communityId}`, { params });
  }
}
