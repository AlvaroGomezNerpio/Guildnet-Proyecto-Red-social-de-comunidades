import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CommunityService } from '../../services/community.service';
import { CommunityResponseDTO } from '../../models/community/CommunityResponse.dto';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-search-results',
  standalone: false,
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.css']
})
export class SearchResultsComponent implements OnInit {

  searchNameControl = new FormControl('');
  searchTagsControl = new FormControl('');

  results: CommunityResponseDTO[] = [];
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private communityService: CommunityService
  ) {}

  ngOnInit(): void {
    // Cargar valores iniciales desde queryParams si existen
    this.route.queryParams.subscribe((params) => {
      const name = params['name'];
      const tag = params['tag'];

      this.searchNameControl.setValue(name || '');
      this.searchTagsControl.setValue(Array.isArray(tag) ? tag.join(', ') : tag || '');
    });

    // Escucha cambios y busca automáticamente
    this.searchNameControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.performSearch());

    this.searchTagsControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.performSearch());
  }

  performSearch(): void {
    const name = this.searchNameControl.value?.trim();
    const tags = this.searchTagsControl.value
      ?.split(',')
      .map((t: string) => t.trim())
      .filter((t: string) => t.length > 0);

    this.communityService.searchCommunities(name, tags).subscribe({
      next: (data) => this.results = data,
      error: () => this.error = 'Error al buscar comunidades.'
    });
  }

  onJoinCommunity(id: number): void {
    this.communityService.joinCommunity(id).subscribe({
      next: () => {
        alert('Te has unido correctamente.');
        this.results = this.results.filter(c => c.id !== id);
      },
      error: () => {
        alert('No se pudo unir a la comunidad.');
      }
    });
  }
}
