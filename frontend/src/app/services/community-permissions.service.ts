import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export type Permission =
  | 'EDIT_POST'
  | 'DELETE_POST'
  | 'EDIT_COMMENTS'
  | 'DELETE_COMMENTS'
  | 'ASSIGN_ROLES'
  | 'ASSIGN_TITLES'
  | 'BAN_USER'
  | 'MANAGE_COMMUNITY_SETTINGS';

/**
 * Permisos del usuario autenticado en una comunidad. Los calcula el backend
 * (el creador tiene todos; el resto, los de su rol). Solo sirven para mostrar u
 * ocultar opciones: el backend vuelve a comprobarlos en cada operación.
 */
@Injectable({ providedIn: 'root' })
export class CommunityPermissionsService {
  private readonly http = inject(HttpClient);

  getMyPermissions(communityId: number): Observable<Permission[]> {
    return this.http
      .get<Permission[]>(`${environment.apiUrl}/api/v1/communities/${communityId}/my-permissions`)
      .pipe(catchError(() => of([] as Permission[])));
  }
}
