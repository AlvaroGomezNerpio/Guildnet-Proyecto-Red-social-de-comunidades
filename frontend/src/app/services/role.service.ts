import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoleDTO } from '../models/rol/RoleDTO';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private baseUrl = '${environment.apiUrl}/api/v1/roles';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  createRole(role: RoleDTO): Observable<RoleDTO> {
    return this.http.post<RoleDTO>(this.baseUrl, role, { headers: this.getHeaders() });
  }

  getRolesByCommunity(communityId: number): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(`${this.baseUrl}/community/${communityId}`, { headers: this.getHeaders() });
  }

  getRoleById(id: number): Observable<RoleDTO> {
    return this.http.get<RoleDTO>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  updateRole(id: number, role: RoleDTO): Observable<RoleDTO> {
    return this.http.put<RoleDTO>(`${this.baseUrl}/${id}`, role, { headers: this.getHeaders() });
  }

  deleteRole(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { headers: this.getHeaders(), responseType: 'text' });
  }
}
