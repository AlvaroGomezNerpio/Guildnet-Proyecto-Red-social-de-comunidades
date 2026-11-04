import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../../../core/http/page.model';

/** Publicaciones por página en los listados. */
export const PAGE_SIZE = 10;
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

  getPostsByProfile(profileId: number, page = 0, size = PAGE_SIZE): Observable<Page<PostDTO>> {
    return this.http.get<Page<PostDTO>>(`${this.apiUrl}/profile/${profileId}`, { params: { page, size } });
  }

  getPostsByCommunity(communityId: number, page = 0, size = PAGE_SIZE): Observable<Page<PostDTO>> {
    return this.http.get<Page<PostDTO>>(`${this.apiUrl}/community/${communityId}`, { params: { page, size } });
  }

  /** Publicaciones fijadas de la comunidad (no aparecen en el muro normal). */
  getPinnedPosts(communityId: number): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.apiUrl}/community/${communityId}/pinned`);
  }

  pinPost(id: number): Observable<PostDTO> {
    return this.http.put<PostDTO>(`${this.apiUrl}/${id}/pin`, null);
  }

  unpinPost(id: number): Observable<PostDTO> {
    return this.http.delete<PostDTO>(`${this.apiUrl}/${id}/pin`);
  }

  searchPostsInCommunity(communityId: number, title?: string, tags?: string[], page = 0, size = PAGE_SIZE): Observable<Page<PostDTO>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (title) params = params.set('title', title);
    (tags ?? []).forEach((tag) => (params = params.append('tag', tag)));
    return this.http.get<Page<PostDTO>>(`${this.apiUrl}/search/${communityId}`, { params });
  }
}
