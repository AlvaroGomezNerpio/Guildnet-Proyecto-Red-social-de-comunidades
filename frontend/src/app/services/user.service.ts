import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../models/user/UserDTO';
import { UpdateUserRequest } from '../models/user/UpdateUserRequest';
import { UpdateUserTagsRequest } from '../models/user/UpdateUserTagsRequest';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = '${environment.apiUrl}/api/v1/user';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  getCurrentUser(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/me`, {
      headers: this.getAuthHeaders(),
    });
  }

  updateUser(request: UpdateUserRequest, image?: File): Observable<UserDTO> {
    const formData = new FormData();
    formData.append(
      'user',
      new Blob([JSON.stringify(request)], { type: 'application/json' })
    );

    if (image) {
      formData.append('image', image);
    }

    return this.http.put<UserDTO>(`${this.apiUrl}/update`, formData, {
      headers: this.getAuthHeaders(),
    });
  }

  updateTags(request: UpdateUserTagsRequest): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/tags`, request, {
      headers: this.getAuthHeaders(),
    });
  }
}
