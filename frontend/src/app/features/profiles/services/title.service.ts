import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { TitleDTO, CreateTitleRequest, UpdateTitleRequest, TitleRuleRequest } from '../models/title.models';

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

  /** Convierte el título en automático; se asigna también a quien ya cumple la regla. */
  setRule(id: number, rule: TitleRuleRequest): Observable<TitleDTO> {
    return this.http.put<TitleDTO>(`${this.baseUrl}/${id}/rule`, rule);
  }

  /** Vuelve a ser manual; quien ya lo tiene lo conserva. */
  removeRule(id: number): Observable<TitleDTO> {
    return this.http.delete<TitleDTO>(`${this.baseUrl}/${id}/rule`);
  }
}
