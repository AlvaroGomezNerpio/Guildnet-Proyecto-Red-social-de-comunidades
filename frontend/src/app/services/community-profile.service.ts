import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommunityProfileDTO } from '../models/communityProfile/CommunityProfileDTO';
import { UpdateCommunityProfileRequest } from '../models/communityProfile/UpdateCommunityProfileRequest';

@Injectable({
  providedIn: 'root'
})
export class CommunityProfileService {

  private apiUrl = 'http://localhost:8080/api/v1/profiles';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // 🔹 Unirse automáticamente a una comunidad
  joinCommunity(communityId: number): Observable<CommunityProfileDTO> {
    return this.http.post<CommunityProfileDTO>(
      `${this.apiUrl}/auto-create/community/${communityId}`,
      null,
      { headers: this.getAuthHeaders() }
    );
  }

  // 🔹 Obtener tu perfil en una comunidad usando el token
  getMyProfileInCommunity(communityId: number): Observable<CommunityProfileDTO> {
    return this.http.get<CommunityProfileDTO>(
      `${this.apiUrl}/me/community/${communityId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 🔹 Actualizar perfil de comunidad con imagen
  updateProfile(
    profileId: number,
    request: UpdateCommunityProfileRequest,
    imageFile?: File
  ): Observable<CommunityProfileDTO> {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(request)], { type: 'application/json' }));

    if (imageFile) {
      formData.append('image', imageFile);
    }

    return this.http.put<CommunityProfileDTO>(
      `${this.apiUrl}/${profileId}`,
      formData,
      { headers: this.getAuthHeaders() }
    );
  }
}
