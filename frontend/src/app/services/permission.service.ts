import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PermissionDTO } from '../models/role/PermissionDTO';

/** Catalogo de permisos (solo lectura). */
@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/permissions`;

  getAllPermissions(): Observable<PermissionDTO[]> {
    return this.http.get<PermissionDTO[]>(this.baseUrl);
  }

  getPermissionById(id: number): Observable<PermissionDTO> {
    return this.http.get<PermissionDTO>(`${this.baseUrl}/${id}`);
  }
}
