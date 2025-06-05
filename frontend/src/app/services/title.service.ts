import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TitleDTO } from '../models/title/TitleDTO';
import { CreateTitleRequest } from '../models/title/CreateTitleRequest';
import { UpdateTitleRequest } from '../models/title/UpdateTitleRequest';


@Injectable({
  providedIn: 'root'
})
export class TitleService {
  private baseUrl = 'http://localhost:8080/api/v1/titles';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  createTitle(request: CreateTitleRequest): Observable<TitleDTO> {
    return this.http.post<TitleDTO>(this.baseUrl, request, {
      headers: this.getHeaders()
    });
  }

  updateTitle(id: number, request: UpdateTitleRequest): Observable<TitleDTO> {
    return this.http.put<TitleDTO>(`${this.baseUrl}/${id}`, request, {
      headers: this.getHeaders()
    });
  }

  deleteTitle(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders(),
      responseType: 'text'
    });
  }

  getTitleById(id: number): Observable<TitleDTO> {
    return this.http.get<TitleDTO>(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  getTitlesByCommunity(communityId: number): Observable<TitleDTO[]> {
    return this.http.get<TitleDTO[]>(`${this.baseUrl}/community/${communityId}`, {
      headers: this.getHeaders()
    });
  }
}
