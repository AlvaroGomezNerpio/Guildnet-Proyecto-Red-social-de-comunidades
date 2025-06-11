import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RolePermissionDTO } from '../models/rol/RolePermissionDTO';
import { RolePermissionCreateDTO } from '../models/rol/RolePermissionCreateDTO';

@Injectable({ providedIn: 'root' })
export class RolePermissionService {
  private baseUrl = 'http://localhost:8080/api/v1/role-permissions';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  assignPermission(dto: RolePermissionCreateDTO): Observable<RolePermissionDTO> {
    return this.http.post<RolePermissionDTO>(this.baseUrl, dto, { headers: this.getHeaders() });
  }

  getPermissionsByRole(roleId: number): Observable<RolePermissionDTO[]> {
    return this.http.get<RolePermissionDTO[]>(`${this.baseUrl}/role/${roleId}`, { headers: this.getHeaders() });
  }

  removeRolePermission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
