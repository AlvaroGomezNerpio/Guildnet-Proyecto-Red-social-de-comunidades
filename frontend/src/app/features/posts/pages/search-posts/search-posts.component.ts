import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { injectCommunityTheme } from '../../../../core/theme/community-theme.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, merge } from 'rxjs';
import { PostService } from '../../services/post.service';
import { PostDTO } from '../../models/post.models';
import { PostCardComponent } from '../../components/post-card/post-card.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';

/** Búsqueda de publicaciones dentro de una comunidad por título y etiquetas. */
@Component({
  selector: 'app-search-posts',
  imports: [ReactiveFormsModule, RouterLink, PostCardComponent, EmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search-posts.component.html',
  styleUrl: './search-posts.component.css',
})
export class SearchPostsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly postService = inject(PostService);
  private readonly theme = injectCommunityTheme();

  communityId = 0;
  readonly titleControl = new FormControl('', { nonNullable: true });
  readonly tagsControl = new FormControl('', { nonNullable: true });

  readonly results = signal<PostDTO[]>([]);
  readonly total = signal(0);
  readonly hasMore = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private page = 0;

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('communityId'));
    this.theme.useCommunity(this.communityId);
    this.search();
    merge(this.titleControl.valueChanges, this.tagsControl.valueChanges)
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.search());
  }

  search(more = false): void {
    const title = this.titleControl.value.trim();
    const tags = this.tagsControl.value
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);
    const page = more ? this.page + 1 : 0;

    this.loading.set(true);
    this.postService.searchPostsInCommunity(this.communityId, title, tags, page).subscribe({
      next: (result) => {
        this.results.update((current) => (more ? [...current, ...result.content] : result.content));
        this.page = result.page;
        this.total.set(result.totalElements);
        this.hasMore.set(!result.last);
        this.error.set(null);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Error al buscar publicaciones.');
        this.loading.set(false);
      },
    });
  }
}
