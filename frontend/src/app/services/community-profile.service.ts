import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommunityProfileDTO } from '../models/communityProfile/CommunityProfileDTO';
import { UpdateCommunityProfileRequest } from '../models/communityProfile/UpdateCommunityProfileRequest';

@Injectable({
  providedIn: 'root'
})
export class CommunityProfileService {

  private apiUrl = '${environment.apiUrl}/api/v1/profiles';

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

  // 🔹 Obtener todos los perfiles de una comunidad
  getProfilesByCommunity(communityId: number): Observable<CommunityProfileDTO[]> {
    return this.http.get<CommunityProfileDTO[]>(
      `${this.apiUrl}/community/${communityId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 🔹 Obtener perfil por ID
  getProfileById(profileId: number): Observable<CommunityProfileDTO> {
    return this.http.get<CommunityProfileDTO>(
      `${this.apiUrl}/${profileId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 🔹 Eliminar un perfil
  deleteProfile(profileId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // 🔹 Asignar un título a un perfil
  assignTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${profileId}/titles/${titleId}`, null, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // 🔹 Quitar un título de un perfil
  removeTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}/titles/${titleId}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // 🔹 Cambiar el título destacado
  changeFeaturedTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${profileId}/featured-title/${titleId}`, null, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // 🔹 Asignar rol
  assignRoleToProfile(profileId: number, roleId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${profileId}/roles/${roleId}`, null, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  // 🔹 Quitar rol
  removeRoleFromProfile(profileId: number, roleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}/roles/${roleId}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
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
