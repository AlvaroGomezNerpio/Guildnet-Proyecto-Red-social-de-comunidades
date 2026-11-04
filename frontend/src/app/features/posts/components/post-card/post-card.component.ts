import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostDTO } from '../../models/post.models';
import { AvatarComponent } from '../../../../shared/ui/avatar/avatar.component';
import { TagBadgeComponent } from '../../../../shared/ui/tag-badge/tag-badge.component';
import { ExcerptPipe } from '../../../../shared/pipes/excerpt.pipe';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';

/** Tarjeta de publicación para los listados (resumen desplegable, autor, etiquetas y contadores). */
@Component({
  selector: 'app-post-card',
  imports: [RouterLink, AvatarComponent, TagBadgeComponent, ExcerptPipe, RelativeTimePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.css',
})
export class PostCardComponent {
  readonly post = input.required<PostDTO>();
  /** Oculta el autor (p. ej. en la página de perfil, donde ya se sabe quién es). */
  readonly showAuthor = input(true);

  protected readonly expanded = signal(false);

  toggle(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.expanded.update((v) => !v);
  }
}
