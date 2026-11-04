import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  const auth = { logout: vi.fn() };
  const setToken = (token: string) => localStorage.setItem('token', token);

  beforeEach(() => {
    localStorage.clear();
    auth.logout.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('añade el token a las peticiones al backend', () => {
    setToken('abc');
    http.get(`${environment.apiUrl}/api/v1/user/me`).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/api/v1/user/me`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush({});
  });

  it('no envía el token a otros dominios', () => {
    setToken('abc');
    http.get('https://example.com/data').subscribe();
    const req = httpMock.expectOne('https://example.com/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('cierra la sesión si el backend responde 401', () => {
    setToken('caducado');
    http.get(`${environment.apiUrl}/api/v1/posts/1`).subscribe({ error: () => undefined });
    httpMock.expectOne(`${environment.apiUrl}/api/v1/posts/1`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(auth.logout).toHaveBeenCalledWith(true);
  });

  it('no cierra la sesión por un login fallido', () => {
    setToken('abc');
    http.post(`${environment.apiUrl}/api/v1/auth/login`, {}).subscribe({ error: () => undefined });
    httpMock.expectOne(`${environment.apiUrl}/api/v1/auth/login`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(auth.logout).not.toHaveBeenCalled();
  });
});
