import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Insignia de color para roles y títulos (colores definidos por la comunidad). */
@Component({
  selector: 'app-tag-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="badge rounded-pill gn-tag-badge" [style.background-color]="background()" [style.color]="color()">
      @if (icon()) {
        <i class="bi bi-{{ icon() }} me-1"></i>
      }
      {{ label() }}
    </span>
  `,
  styles: `
    .gn-tag-badge {
      font-weight: 600;
      letter-spacing: 0.01em;
      padding: 0.4em 0.8em;
      box-shadow: 0 0 0 1px rgb(0 0 0 / 0.06) inset;
    }
  `,
})
export class TagBadgeComponent {
  readonly label = input.required<string>();
  readonly background = input('#6c757d');
  readonly color = input('#ffffff');
  readonly icon = input<string | null>(null);
}
