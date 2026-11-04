import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommunityService } from '../../services/community.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { CommunityCardComponent } from '../../components/community-card/community-card.component';
import { CommunityPreviewComponent } from '../../components/community-preview/community-preview.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { ToastService } from '../../../../shared/ui/toast/toast.service';

/** Página de inicio: comunidades del usuario, sugeridas y populares. */
@Component({
  selector: 'app-community-list',
  imports: [FormsModule, RouterLink, CommunityCardComponent, CommunityPreviewComponent, EmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-list.component.html',
  styleUrl: './community-list.component.css',
})
export class CommunityListComponent implements OnInit {
  private readonly communityService = inject(CommunityService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly subscribed = signal<CommunityResponseDTO[]>([]);
  readonly suggested = signal<CommunityResponseDTO[]>([]);
  readonly popular = signal<CommunityResponseDTO[]>([]);
  readonly loading = signal(true);
  readonly selected = signal<CommunityResponseDTO | null>(null);

  private readonly subscribedIds = computed(() => new Set(this.subscribed().map((c) => c.id)));
  /** Populares a las que el usuario todavía no pertenece. */
  readonly popularToDiscover = computed(() => this.popular().filter((c) => !this.subscribedIds().has(c.id)).slice(0, 9));

  searchText = '';

  ngOnInit(): void {
    this.communityService.getSubscribedCommunities().subscribe({
      next: (data) => {
        this.subscribed.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('No se pudieron cargar tus comunidades.');
      },
    });
    this.communityService.getSuggestedCommunities().subscribe({
      next: (data) => this.suggested.set(data.slice(0, 6)),
    });
    this.communityService.getPopularCommunities().subscribe({
      next: (data) => this.popular.set(data),
    });
  }

  isMember(community: CommunityResponseDTO): boolean {
    return this.subscribedIds().has(community.id);
  }

  open(community: CommunityResponseDTO): void {
    if (this.isMember(community)) {
      this.router.navigate(['/communities', community.id]);
    } else {
      this.selected.set(community);
    }
  }

  search(): void {
    const name = this.searchText.trim();
    this.router.navigate(['/communities/search'], { queryParams: name ? { name } : {} });
  }
}
