import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CommunityProfileDTO, UpdateCommunityProfileRequest } from '../models/profile.models';

@Injectable({ providedIn: 'root' })
export class CommunityProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/profiles`;

  /** Unirse a una comunidad con los datos del perfil global. */
  /** Unirse con el perfil global. Si la comunidad lo exige, hay que aceptar sus normas. */
  joinCommunity(communityId: number, acceptRules = false): Observable<CommunityProfileDTO> {
    return this.http.post<CommunityProfileDTO>(`${this.apiUrl}/auto-create/community/${communityId}`, null, {
      params: { acceptRules },
    });
  }

  /** Perfil del usuario autenticado en la comunidad (404 si no es miembro). */
  getMyProfileInCommunity(communityId: number): Observable<CommunityProfileDTO> {
    return this.http.get<CommunityProfileDTO>(`${this.apiUrl}/me/community/${communityId}`);
  }

  getProfilesByCommunity(communityId: number): Observable<CommunityProfileDTO[]> {
    return this.http.get<CommunityProfileDTO[]>(`${this.apiUrl}/community/${communityId}`);
  }

  getProfileById(profileId: number): Observable<CommunityProfileDTO> {
    return this.http.get<CommunityProfileDTO>(`${this.apiUrl}/${profileId}`);
  }

  /** Abandonar la comunidad (borra el propio perfil). */
  deleteProfile(profileId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}`, { responseType: 'text' });
  }

  assignTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${profileId}/titles/${titleId}`, null, { responseType: 'text' });
  }

  removeTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}/titles/${titleId}`, { responseType: 'text' });
  }

  changeFeaturedTitle(profileId: number, titleId: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${profileId}/featured-title/${titleId}`, null, { responseType: 'text' });
  }

  assignRoleToProfile(profileId: number, roleId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${profileId}/roles/${roleId}`, null, { responseType: 'text' });
  }

  removeRoleFromProfile(profileId: number, roleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${profileId}/roles/${roleId}`, { responseType: 'text' });
  }

  updateProfile(profileId: number, request: UpdateCommunityProfileRequest, imageFile?: File | null): Observable<CommunityProfileDTO> {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (imageFile) {
      formData.append('image', imageFile);
    }
    return this.http.put<CommunityProfileDTO>(`${this.apiUrl}/${profileId}`, formData);
  }
}
