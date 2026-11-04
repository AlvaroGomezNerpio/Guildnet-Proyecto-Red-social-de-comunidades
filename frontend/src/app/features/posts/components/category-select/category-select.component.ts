import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';
import { PostCategoryDTO } from '../../models/post.models';

/**
 * Elegir la categoría de una publicación (o ninguna). Cada opción se muestra
 * con los colores de la categoría.
 *
 *   <app-category-select [categories]="categories" [(selected)]="categoryId" />
 */
@Component({
  selector: 'app-category-select',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (categories().length) {
      <div class="d-flex flex-wrap gap-2" role="radiogroup" [attr.aria-label]="label()">
        <button
          type="button"
          class="gn-cat-option gn-cat-none"
          role="radio"
          [attr.aria-checked]="selected() === null"
          [class.active]="selected() === null"
          (click)="selected.set(null)"
        >
          Sin categoría
        </button>
        @for (category of categories(); track category.id) {
          <button
            type="button"
            class="gn-cat-option"
            role="radio"
            [attr.aria-checked]="selected() === category.id"
            [class.active]="selected() === category.id"
            [style.background-color]="category.backgroundColor"
            [style.color]="category.textColor"
            (click)="selected.set(category.id)"
          >
            @if (selected() === category.id) {
              <i class="bi bi-check-lg me-1"></i>
            }
            {{ category.name }}
          </button>
        }
      </div>
    }
  `,
  styles: `
    .gn-cat-option {
      border: 0;
      border-radius: 999px;
      padding: 0.35rem 0.85rem;
      font-size: 0.82rem;
      font-weight: 600;
      opacity: 0.55;
      box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
      transition: opacity 0.12s ease, transform 0.12s ease;
    }
    .gn-cat-option:hover {
      opacity: 0.85;
    }
    .gn-cat-option.active {
      opacity: 1;
      box-shadow: 0 0 0 2px var(--bs-body-bg), 0 0 0 4px var(--bs-primary);
    }
    .gn-cat-none {
      background: var(--bs-secondary-bg);
      color: var(--bs-body-color);
    }
  `,
})
export class CategorySelectComponent {
  readonly categories = input.required<PostCategoryDTO[]>();
  readonly selected = model<number | null>(null);
  readonly label = input('Categoría');
}
