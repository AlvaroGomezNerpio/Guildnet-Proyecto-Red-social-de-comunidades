import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LogoComponent } from '../../../../core/layout/logo/logo.component';

/** Marco visual de las pantallas de inicio de sesión y registro. */
@Component({
  selector: 'app-auth-shell',
  imports: [LogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="container py-5">
      <div class="card gn-auth-card mx-auto overflow-hidden">
        <div class="row g-0">
          <aside class="col-lg-5 gn-auth-aside d-none d-lg-flex flex-column justify-content-between p-5">
            <div class="d-flex align-items-center gap-2">
              <app-logo [size]="40" />
              <span class="fs-4 fw-bold">GuildNet</span>
            </div>
            <div>
              <h2 class="fw-bold mb-3">Tu gremio te espera</h2>
              <ul class="list-unstyled gn-auth-features mb-0">
                <li><i class="bi bi-people-fill"></i>Comunidades sobre lo que te apasiona</li>
                <li><i class="bi bi-chat-dots-fill"></i>Chat en tiempo real con los miembros</li>
                <li><i class="bi bi-award-fill"></i>Roles y títulos para destacar</li>
              </ul>
            </div>
            <small class="opacity-75">Hecho para compartir.</small>
          </aside>
          <div class="col-lg-7 p-4 p-md-5">
            <h1 class="h3 fw-bold mb-1">{{ title() }}</h1>
            <p class="text-body-secondary mb-4">{{ subtitle() }}</p>
            <ng-content />
          </div>
        </div>
      </div>
    </div>
  `,
  styles: `
    .gn-auth-card {
      max-width: 960px;
      border: 0;
      box-shadow: var(--gn-shadow-lg);
    }
    .gn-auth-aside {
      position: relative;
      background: var(--gn-gradient);
      color: #fff;
      min-height: 520px;
    }
    .gn-auth-aside::after {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at 80% 15%, rgb(255 255 255 / 0.2), transparent 45%);
      pointer-events: none;
    }
    .gn-auth-features li {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.9rem;
      font-weight: 500;
    }
    .gn-auth-features .bi {
      width: 2.25rem;
      height: 2.25rem;
      display: grid;
      place-items: center;
      border-radius: 0.75rem;
      background: rgb(255 255 255 / 0.18);
    }
  `,
})
export class AuthShellComponent {
  readonly title = input.required<string>();
  readonly subtitle = input('');
}
