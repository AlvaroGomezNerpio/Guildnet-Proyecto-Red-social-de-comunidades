import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PostService } from '../../services/post.service';
import { PostDTO } from '../../models/post.models';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ActivatedRoute, RouterLink } from '@angular/router';

interface PostWithToggle extends PostDTO {
  showContent: boolean;
}

@Component({
    selector: 'app-search-posts',
    templateUrl: './search-posts.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './search-posts.component.css',
    imports: [FormsModule, ReactiveFormsModule, RouterLink]
})
export class SearchPostsComponent implements OnInit {
  searchTitleControl = new FormControl('');
  searchTagsControl = new FormControl('');

  results: PostWithToggle[] = [];
  error: string | null = null;

  communityId!: number;

  constructor(
    private postService: PostService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.communityId = +this.route.snapshot.paramMap.get('communityId')!;

    this.performSearch();

    this.searchTitleControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.performSearch());

    this.searchTagsControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.performSearch());
  }

  performSearch(): void {
    const title = this.searchTitleControl.value?.trim();
    const tags = this.searchTagsControl.value
      ?.split(',')
      .map((t: string) => t.trim())
      .filter((t: string) => t.length > 0);

    this.postService.searchPostsInCommunity(this.communityId, title, tags, 0, 50).subscribe({
      next: (data) => {
        this.results = data.content.map(post => ({
          ...post,
          showContent: false
        }));
        this.error = null;
      },
      error: () => this.error = 'Error al buscar publicaciones.'
    });
  }

  toggleContent(post: PostWithToggle): void {
    post.showContent = !post.showContent;
  }
}
