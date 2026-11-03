import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TitleDTO } from '../models/title/TitleDTO';
import { CreateTitleRequest } from '../models/title/CreateTitleRequest';
import { UpdateTitleRequest } from '../models/title/UpdateTitleRequest';

@Injectable({ providedIn: 'root' })
export class TitleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/titles`;

  createTitle(request: CreateTitleRequest): Observable<TitleDTO> {
    return this.http.post<TitleDTO>(this.baseUrl, request);
  }

  updateTitle(id: number, request: UpdateTitleRequest): Observable<TitleDTO> {
    return this.http.put<TitleDTO>(`${this.baseUrl}/${id}`, request);
  }

  deleteTitle(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  getTitleById(id: number): Observable<TitleDTO> {
    return this.http.get<TitleDTO>(`${this.baseUrl}/${id}`);
  }

  getTitlesByCommunity(communityId: number): Observable<TitleDTO[]> {
    return this.http.get<TitleDTO[]>(`${this.baseUrl}/community/${communityId}`);
  }
}
