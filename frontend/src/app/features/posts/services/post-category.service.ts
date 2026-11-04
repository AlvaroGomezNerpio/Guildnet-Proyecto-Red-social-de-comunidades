import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PostCategoryDTO } from '../models/post.models';

/** Categorías oficiales de publicaciones de una comunidad. */
@Injectable({ providedIn: 'root' })
export class PostCategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1`;

  getByCommunity(communityId: number): Observable<PostCategoryDTO[]> {
    return this.http.get<PostCategoryDTO[]>(`${this.apiUrl}/communities/${communityId}/categories`);
  }

  create(communityId: number, category: Pick<PostCategoryDTO, 'name' | 'textColor' | 'backgroundColor'>): Observable<PostCategoryDTO> {
    return this.http.post<PostCategoryDTO>(`${this.apiUrl}/communities/${communityId}/categories`, category);
  }

  update(id: number, category: Pick<PostCategoryDTO, 'name' | 'textColor' | 'backgroundColor'>): Observable<PostCategoryDTO> {
    return this.http.put<PostCategoryDTO>(`${this.apiUrl}/categories/${id}`, category);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }
}
