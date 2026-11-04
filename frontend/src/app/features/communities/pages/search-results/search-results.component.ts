import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, merge } from 'rxjs';
import { CommunityService } from '../../services/community.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { AuthService } from '../../../../core/auth/auth.service';
import { CommunityCardComponent } from '../../components/community-card/community-card.component';
import { CommunityPreviewComponent } from '../../components/community-preview/community-preview.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';

/** Búsqueda de comunidades por nombre y etiquetas (pública: no requiere sesión). */
@Component({
  selector: 'app-search-results',
  imports: [ReactiveFormsModule, CommunityCardComponent, CommunityPreviewComponent, EmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css',
})
export class SearchResultsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly communityService = inject(CommunityService);
  private readonly authService = inject(AuthService);

  readonly nameControl = new FormControl('', { nonNullable: true });
  readonly tagsControl = new FormControl('', { nonNullable: true });

  readonly results = signal<CommunityResponseDTO[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly selected = signal<CommunityResponseDTO | null>(null);
  private readonly memberIds = signal<Set<number>>(new Set());
  readonly selectedIsMember = computed(() => {
    const c = this.selected();
    return !!c && this.memberIds().has(c.id);
  });

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.communityService.getSubscribedCommunities().subscribe({
        next: (data) => this.memberIds.set(new Set(data.map((c) => c.id))),
      });
    }

    const params = this.route.snapshot.queryParamMap;
    this.nameControl.setValue(params.get('name') ?? '', { emitEvent: false });
    this.tagsControl.setValue(params.getAll('tag').join(', '), { emitEvent: false });
    this.performSearch();

    merge(this.nameControl.valueChanges, this.tagsControl.valueChanges)
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.performSearch());
  }

  isMember(community: CommunityResponseDTO): boolean {
    return this.memberIds().has(community.id);
  }

  performSearch(): void {
    const name = this.nameControl.value.trim();
    const tags = this.tagsControl.value
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    // Mantiene la búsqueda en la URL para poder compartirla o volver atrás
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { name: name || null, tag: tags.length ? tags : null },
      replaceUrl: true,
    });

    this.loading.set(true);
    this.communityService.searchCommunities(name, tags).subscribe({
      next: (data) => {
        this.results.set(data);
        this.error.set(null);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Error al buscar comunidades.');
        this.loading.set(false);
      },
    });
  }
}
