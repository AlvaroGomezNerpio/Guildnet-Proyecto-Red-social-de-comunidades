import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('guards de autenticación', () => {
  const auth = { isAuthenticated: vi.fn<() => boolean>() };
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/communities/3' } as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));

  it('authGuard deja pasar con sesión', () => {
    auth.isAuthenticated.mockReturnValue(true);
    expect(run(authGuard)).toBe(true);
  });

  it('authGuard redirige al login recordando la URL', () => {
    auth.isAuthenticated.mockReturnValue(false);
    const result = run(authGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/login?returnUrl=%2Fcommunities%2F3');
  });

  it('guestGuard manda a las comunidades si ya hay sesión', () => {
    auth.isAuthenticated.mockReturnValue(true);
    const result = run(guestGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/communities');
  });

  it('guestGuard deja entrar sin sesión', () => {
    auth.isAuthenticated.mockReturnValue(false);
    expect(run(guestGuard)).toBe(true);
  });
});
