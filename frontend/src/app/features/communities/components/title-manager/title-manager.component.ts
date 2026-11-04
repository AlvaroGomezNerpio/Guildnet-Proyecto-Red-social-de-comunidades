import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, of, switchMap } from 'rxjs';
import { TitleService } from '../../../profiles/services/title.service';
import { TITLE_METRICS, TitleDTO, TitleMetric, describeRule } from '../../../profiles/models/title.models';
import { BadgeColorPickerComponent } from '../../../../shared/ui/badge-color-picker/badge-color-picker.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { errorMessage } from '../../../../core/http/http-error';
import { readableOn } from '../../../../core/theme/community-palette';

/**
 * Títulos de la comunidad: manuales (los asigna un moderador desde el perfil)
 * o automáticos (se consiguen solos al llegar a una actividad).
 * Los cambios se guardan al momento.
 */
@Component({
  selector: 'app-title-manager',
  imports: [FormsModule, BadgeColorPickerComponent, TagBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './title-manager.component.html',
})
export class TitleManagerComponent implements OnInit {
  private readonly titleService = inject(TitleService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  readonly communityId = input.required<number>();
  readonly communityColor = input<string>('#7c3aed');

  protected readonly metrics = TITLE_METRICS;
  protected readonly describe = describeRule;
  protected readonly titles = signal<TitleDTO[]>([]);

  protected readonly editingId = signal<number | null>(null);
  protected readonly name = signal('');
  protected readonly background = signal('#7c3aed');
  protected readonly text = signal('#ffffff');
  protected readonly automatic = signal(false);
  protected readonly metric = signal<TitleMetric>('POSTS');
  protected readonly threshold = signal(10);
  protected readonly saving = signal(false);

  ngOnInit(): void {
    this.resetForm();
    this.titleService.getTitlesByCommunity(this.communityId()).subscribe((list) => this.titles.set(list));
  }

  protected edit(title: TitleDTO): void {
    this.editingId.set(title.id);
    this.name.set(title.title);
    this.background.set(title.backgroundColor);
    this.text.set(title.textColor);
    this.automatic.set(!!title.autoMetric);
    this.metric.set(title.autoMetric ?? 'POSTS');
    this.threshold.set(title.autoThreshold ?? 10);
  }

  protected resetForm(): void {
    this.editingId.set(null);
    this.name.set('');
    this.background.set(this.communityColor());
    this.text.set(readableOn(this.communityColor()));
    this.automatic.set(false);
    this.metric.set('POSTS');
    this.threshold.set(10);
  }

  protected validThreshold(): boolean {
    const n = this.threshold();
    return Number.isInteger(n) && n >= 1 && n <= 100000;
  }

  protected save(): void {
    const name = this.name().trim();
    if (!name || (this.automatic() && !this.validThreshold())) return;

    const id = this.editingId();
    const colors = { title: name, textColor: this.text(), backgroundColor: this.background() };
    const saveTitle: Observable<TitleDTO> =
      id === null
        ? this.titleService.createTitle({ ...colors, communityId: this.communityId() })
        : this.titleService.updateTitle(id, colors);

    this.saving.set(true);
    saveTitle
      .pipe(
        switchMap((saved) => {
          if (this.automatic()) return this.titleService.setRule(saved.id, { metric: this.metric(), threshold: this.threshold() });
          return saved.autoMetric ? this.titleService.removeRule(saved.id) : of(saved);
        })
      )
      .subscribe({
        next: (saved) => {
          this.saving.set(false);
          this.titles.update((list) => (id === null ? [...list, saved] : list.map((t) => (t.id === id ? saved : t))));
          this.toast.success(
            saved.autoMetric
              ? `«${saved.title}» se asignará solo. Quien ya cumpla la regla lo ha recibido.`
              : id === null
                ? `Título «${saved.title}» creado. Asígnalo desde el perfil de un miembro.`
                : 'Título actualizado.'
          );
          this.resetForm();
        },
        error: (err) => {
          this.saving.set(false);
          this.toast.error(errorMessage(err, 'No se pudo guardar el título.'));
        },
      });
  }

  protected async remove(title: TitleDTO): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar título',
      message: `Se quitará «${title.title}» a todos los miembros que lo tengan.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!accepted) return;
    this.titleService.deleteTitle(title.id).subscribe({
      next: () => {
        this.titles.update((list) => list.filter((t) => t.id !== title.id));
        if (this.editingId() === title.id) this.resetForm();
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar el título.')),
    });
  }
}
