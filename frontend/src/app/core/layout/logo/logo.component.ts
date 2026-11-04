import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Logotipo de GuildNet: un escudo con nodos conectados. */
@Component({
  selector: 'app-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 48 48" aria-hidden="true">
      <defs>
        <linearGradient [attr.id]="gradientId" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#7c3aed" />
          <stop offset="0.55" stop-color="#4f46e5" />
          <stop offset="1" stop-color="#06b6d4" />
        </linearGradient>
      </defs>
      <path d="M24 3 42 10v13c0 11-7.6 19.4-18 22C13.6 42.4 6 34 6 23V10z" [attr.fill]="'url(#' + gradientId + ')'" />
      <g stroke="#fff" stroke-width="2.4" stroke-linecap="round" fill="none" opacity="0.95">
        <path d="M16 20 24 15 32 20M16 20v9l8 5 8-5v-9M24 15v19" />
      </g>
      <g fill="#fff">
        <circle cx="24" cy="15" r="2.6" />
        <circle cx="16" cy="20" r="2.6" />
        <circle cx="32" cy="20" r="2.6" />
        <circle cx="24" cy="34" r="2.6" />
      </g>
    </svg>
  `,
  styles: `
    :host {
      display: inline-flex;
    }
  `,
})
export class LogoComponent {
  private static nextId = 0;
  readonly size = input(32);
  /** Id único del degradado (puede haber varios logos en la página). */
  protected readonly gradientId = `gn-logo-grad-${LogoComponent.nextId++}`;
}
