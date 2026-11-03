import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RoleDTO } from '../models/role/RoleDTO';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/roles`;

  createRole(role: Partial<RoleDTO>): Observable<RoleDTO> {
    return this.http.post<RoleDTO>(this.baseUrl, role);
  }

  getRolesByCommunity(communityId: number): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(`${this.baseUrl}/community/${communityId}`);
  }

  getRoleById(id: number): Observable<RoleDTO> {
    return this.http.get<RoleDTO>(`${this.baseUrl}/${id}`);
  }

  updateRole(id: number, role: Partial<RoleDTO>): Observable<RoleDTO> {
    return this.http.put<RoleDTO>(`${this.baseUrl}/${id}`, role);
  }

  deleteRole(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
