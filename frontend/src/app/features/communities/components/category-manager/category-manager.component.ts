import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PostCategoryService } from '../../../posts/services/post-category.service';
import { PostCategoryDTO } from '../../../posts/models/post.models';
import { BadgeColorPickerComponent } from '../../../../shared/ui/badge-color-picker/badge-color-picker.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { errorMessage } from '../../../../core/http/http-error';
import { readableOn } from '../../../../core/theme/community-palette';

/** Máximo de categorías por comunidad (lo valida también el backend). */
const MAX_CATEGORIES = 12;

/**
 * Categorías de publicaciones de una comunidad: crear, editar y borrar.
 * Los cambios se guardan al momento (no dependen del botón "Guardar cambios").
 */
@Component({
  selector: 'app-category-manager',
  imports: [FormsModule, BadgeColorPickerComponent, TagBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './category-manager.component.html',
})
export class CategoryManagerComponent implements OnInit {
  private readonly categoryService = inject(PostCategoryService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  readonly communityId = input.required<number>();
  /** Color principal de la comunidad: color inicial de las categorías nuevas. */
  readonly communityColor = input<string>('#7c3aed');

  protected readonly categories = signal<PostCategoryDTO[]>([]);
  protected readonly maxCategories = MAX_CATEGORIES;

  /** Categoría que se está editando (null: se está creando una nueva). */
  protected readonly editingId = signal<number | null>(null);
  protected readonly name = signal('');
  protected readonly background = signal('#7c3aed');
  protected readonly text = signal('#ffffff');
  protected readonly saving = signal(false);

  ngOnInit(): void {
    this.resetForm();
    this.categoryService.getByCommunity(this.communityId()).subscribe((list) => this.categories.set(list));
  }

  protected edit(category: PostCategoryDTO): void {
    this.editingId.set(category.id);
    this.name.set(category.name);
    this.background.set(category.backgroundColor);
    this.text.set(category.textColor);
  }

  protected resetForm(): void {
    this.editingId.set(null);
    this.name.set('');
    this.background.set(this.communityColor());
    this.text.set(readableOn(this.communityColor()));
  }

  protected save(): void {
    const name = this.name().trim();
    if (!name) return;
    const data = { name, textColor: this.text(), backgroundColor: this.background() };
    const id = this.editingId();
    this.saving.set(true);
    const request = id === null ? this.categoryService.create(this.communityId(), data) : this.categoryService.update(id, data);
    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.categories.update((list) => (id === null ? [...list, saved] : list.map((c) => (c.id === id ? saved : c))));
        this.toast.success(id === null ? `Categoría «${saved.name}» creada.` : 'Categoría actualizada.');
        this.resetForm();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(errorMessage(err, 'No se pudo guardar la categoría.'));
      },
    });
  }

  protected async remove(category: PostCategoryDTO): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar categoría',
      message: `Las publicaciones de «${category.name}» se quedarán sin categoría, pero no se borran.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!accepted) return;
    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.categories.update((list) => list.filter((c) => c.id !== category.id));
        if (this.editingId() === category.id) this.resetForm();
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar la categoría.')),
    });
  }
}
