import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Mensaje para listas vacías, con icono y espacio opcional para una acción. */
@Component({
  selector: 'app-empty-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="gn-empty text-center py-5 px-3">
      <i class="bi bi-{{ icon() }} gn-empty-icon"></i>
      <p class="mt-3 mb-3 text-body-secondary">{{ message() }}</p>
      <ng-content />
    </div>
  `,
  styles: `
    .gn-empty {
      border: 2px dashed var(--bs-border-color);
      border-radius: var(--gn-radius, 0.75rem);
    }
    .gn-empty-icon {
      font-size: 2.5rem;
      color: var(--bs-secondary-color);
    }
  `,
})
export class EmptyStateComponent {
  readonly icon = input('inbox');
  readonly message = input.required<string>();
}
