import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CommunityResponseDTO } from '../../models/community.models';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { ExcerptPipe } from '../../../../shared/pipes/excerpt.pipe';
import { PaletteDirective } from '../../../../core/theme/palette.directive';
import { paletteOf } from '../../../../core/theme/community-palette';

/** Tarjeta de comunidad para los listados. Emite {@link selected} al pulsarla. */
@Component({
  selector: 'app-community-card',
  imports: [AvatarComponent, ExcerptPipe, PaletteDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-card.component.html',
  styleUrl: './community-card.component.css',
})
export class CommunityCardComponent {
  readonly community = input.required<CommunityResponseDTO>();
  /** Muestra la marca de "miembro". */
  readonly member = input(false);
  readonly selected = output<CommunityResponseDTO>();

  protected readonly palette = computed(() => paletteOf(this.community()));
  protected readonly membersText = computed(() => {
    const c = this.community();
    const label = c.membersLabel?.trim() || (c.memberCount === 1 ? 'miembro' : 'miembros');
    return `${(c.memberCount ?? 0).toLocaleString('es-ES')} ${label}`;
  });
}
