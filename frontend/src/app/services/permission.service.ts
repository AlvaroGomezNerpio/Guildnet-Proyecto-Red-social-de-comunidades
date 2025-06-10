import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PermissionDTO } from '../models/rol/PermissionDTO';
import { PermissionCreateDTO } from '../models/rol/PermissionCreateDTO';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private baseUrl = '${environment.apiUrl}/api/v1/permissions';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  createPermission(dto: PermissionCreateDTO): Observable<PermissionDTO> {
    return this.http.post<PermissionDTO>(this.baseUrl, dto, { headers: this.getHeaders() });
  }

  getAllPermissions(): Observable<PermissionDTO[]> {
    return this.http.get<PermissionDTO[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getPermissionById(id: number): Observable<PermissionDTO> {
    return this.http.get<PermissionDTO>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  deletePermission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}

