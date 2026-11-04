import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommunityResponseDTO } from '../../models/community.models';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { ExcerptPipe } from '../../../../shared/pipes/excerpt.pipe';

/** Tarjeta de comunidad para los listados. Emite {@link selected} al pulsarla. */
@Component({
  selector: 'app-community-card',
  imports: [AvatarComponent, ExcerptPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-card.component.html',
  styleUrl: './community-card.component.css',
})
export class CommunityCardComponent {
  readonly community = input.required<CommunityResponseDTO>();
  /** Muestra la marca de "miembro". */
  readonly member = input(false);
  readonly selected = output<CommunityResponseDTO>();
}
