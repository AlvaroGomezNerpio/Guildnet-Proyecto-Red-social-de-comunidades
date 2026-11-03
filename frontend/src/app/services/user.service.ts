import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserDTO } from '../models/user/UserDTO';
import { UpdateUserRequest } from '../models/user/UpdateUserRequest';
import { UpdateUserTagsRequest } from '../models/user/UpdateUserTagsRequest';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/user`;

  getCurrentUser(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/me`);
  }

  updateUser(request: UpdateUserRequest, image?: File | null): Observable<UserDTO> {
    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http.put<UserDTO>(`${this.apiUrl}/update`, formData);
  }

  updateTags(request: UpdateUserTagsRequest): Observable<string> {
    return this.http.put(`${this.apiUrl}/tags`, request, { responseType: 'text' });
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/me`);
  }
}
