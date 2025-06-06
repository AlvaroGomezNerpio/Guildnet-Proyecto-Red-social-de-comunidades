import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommunityResponseDTO } from '../models/community/CommunityResponse.dto';
import { CreateCommunityRequest } from '../models/community/CreateCommunityRequest.dto';

@Injectable({
  providedIn: 'root',
})
export class CommunityService {
  private apiUrl = 'http://localhost:8080/api/v1/communities';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (token) {
      return new HttpHeaders({
        Authorization: `Bearer ${token}`,
      });
    }
    return new HttpHeaders(); // sin cabecera Authorization
  }

  getSubscribedCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/subscribed`, {
      headers: this.getAuthHeaders(),
    });
  }

  getSuggestedCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/suggested`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPopularCommunities(): Observable<CommunityResponseDTO[]> {
    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/popular`, {
      headers: this.getAuthHeaders(),
    });
  }

  createCommunity(
    data: CreateCommunityRequest,
    image?: File,
    banner?: File
  ): Observable<any> {
    const formData = new FormData();

    formData.append(
      'community',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );

    if (image) {
      formData.append('image', image);
    }

    if (banner) {
      formData.append('banner', banner);
    }

    return this.http.post(`${this.apiUrl}/create`, formData, {
      headers: this.getAuthHeaders(),
    });
  }

  searchCommunities(
    name?: string,
    tags?: string[]
  ): Observable<CommunityResponseDTO[]> {
    let params: any = {};
    if (name) params.name = name;
    if (tags && tags.length > 0) params.tag = tags;

    return this.http.get<CommunityResponseDTO[]>(`${this.apiUrl}/search`, {
      headers: this.getAuthHeaders(),
      params: params,
    });
  }

  getCommunityById(id: number): Observable<CommunityResponseDTO> {
    return this.http.get<CommunityResponseDTO>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  updateCommunity(
    id: number,
    data: any,
    image?: File,
    banner?: File
  ): Observable<string> {
    const formData = new FormData();
    formData.append(
      'data',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );
    if (image) formData.append('image', image);
    if (banner) formData.append('banner', banner);

    return this.http.put<string>(`${this.apiUrl}/${id}`, formData, {
      headers: this.getAuthHeaders(),
      responseType: 'text' as 'json',
    });
  }

  deleteCommunity(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text' as 'json',
    });
  }

  removeUserFromCommunity(
    communityId: number,
    profileId: number
  ): Observable<string> {
    return this.http.delete<string>(
      `${this.apiUrl}/${communityId}/remove-user/${profileId}`,
      {
        headers: this.getAuthHeaders(),
        responseType: 'text' as 'json',
      }
    );
  }
}
