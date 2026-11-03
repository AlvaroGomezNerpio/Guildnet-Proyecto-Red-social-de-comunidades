import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RolePermissionCreateDTO, RolePermissionDTO } from '../models/role.models';

@Injectable({ providedIn: 'root' })
export class RolePermissionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/role-permissions`;

  assignPermission(dto: RolePermissionCreateDTO): Observable<RolePermissionDTO> {
    return this.http.post<RolePermissionDTO>(this.baseUrl, dto);
  }

  getPermissionsByRole(roleId: number): Observable<RolePermissionDTO[]> {
    return this.http.get<RolePermissionDTO[]>(`${this.baseUrl}/role/${roleId}`);
  }

  removeRolePermission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
