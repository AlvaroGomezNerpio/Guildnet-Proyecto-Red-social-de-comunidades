import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CommunityResponseDTO } from '../models/community/CommunityResponse.dto';
import { CreateCommunityRequest } from '../models/community/CreateCommunityRequest.dto';
import { UpdateCommunityRequest } from '../models/community/UpdateCommunityRequest';

@Injectable({ providedIn: 'root' })
export class CommunityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/communities`;

  getSubscribedCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/subscribed`);
  }

  getSuggestedCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/suggested`);
  }

  getPopularCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/popular`);
  }

  createCommunity(data: CreateCommunityRequest, image?: File | null, banner?: File | null): Observable<CommunityResponseDTO> {
    return this.http.post<CommunityResponseDTO>(`${this.apiUrl}/create`, this.toFormData('community', data, image, banner));
  }

  searchCommunities(name?: string, tags?: string[]): Observable<CommunityResponseDTO[]> {
    let params = new HttpParams();
    if (name) params = params.set('name', name);
    (tags ?? []).forEach((tag) => (params = params.append('tag', tag)));
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/search`, { params });
  }

  getCommunityById(id: number): Observable<CommunityResponseDTO> {
    return this.http.get<CommunityResponseDTO>(`${this.apiUrl}/${id}`);
  }

  updateCommunity(id: number, data: UpdateCommunityRequest, image?: File | null, banner?: File | null): Observable<CommunityResponseDTO> {
    return this.http.put<CommunityResponseDTO>(`${this.apiUrl}/${id}`, this.toFormData('data', data, image, banner));
  }

  deleteCommunity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  removeUserFromCommunity(communityId: number, profileId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${communityId}/remove-user/${profileId}`);
  }

  private toFormData(part: string, data: unknown, image?: File | null, banner?: File | null): FormData {
    const formData = new FormData();
    formData.append(part, new Blob([JSON.stringify(data)], { type: 'application/json' }));
    if (image) formData.append('image', image);
    if (banner) formData.append('banner', banner);
    return formData;
  }
}
