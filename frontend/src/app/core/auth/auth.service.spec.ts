import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
  });

  afterEach(() => localStorage.clear());

  it('recupera el usuario al recargar la página si hay un token guardado', () => {
    localStorage.setItem('token', 'abc');

    const auth = TestBed.inject(AuthService);
    const httpMock = TestBed.inject(HttpTestingController);

    const req = httpMock.expectOne(`${environment.apiUrl}/api/v1/user/me`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush({ id: 7, displayName: 'Ana', email: 'ana@guildnet.local' });

    expect(auth.currentUser?.id).toBe(7);
    httpMock.verify();
  });

  it('no pide el usuario si no hay sesión', () => {
    const auth = TestBed.inject(AuthService);
    TestBed.inject(HttpTestingController).verify();
    expect(auth.currentUser).toBeNull();
  });
});
