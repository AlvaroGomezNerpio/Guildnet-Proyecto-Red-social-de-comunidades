import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommunityService } from '../../services/community.service';
import { CommunityProfileService } from '../../services/community-profile.service';
import { CommunityResponseDTO } from '../../models/community/CommunityResponse.dto';

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
  selectedCommunity: CommunityResponseDTO | null = null;
  subscribedCommunities: CommunityResponseDTO[] = [];

  isLoggedIn: boolean = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private communityService: CommunityService,
    private communityProfileService: CommunityProfileService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = !!localStorage.getItem('token');

    if (this.isLoggedIn) {
      this.communityService.getSubscribedCommunities().subscribe({
        next: data => this.subscribedCommunities = data,
        error: () => this.subscribedCommunities = []
      });
    }

    this.route.queryParams.subscribe((params) => {
      const name = params['name'];
      const tag = params['tag'];

      this.searchNameControl.setValue(name || '');
      this.searchTagsControl.setValue(Array.isArray(tag) ? tag.join(', ') : tag || '');
      this.performSearch();
    });

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
      next: (data) => {
        this.results = data;
        this.error = null;
      },
      error: () => this.error = 'Error al buscar comunidades.'
    });
  }

  onJoinCommunity(communityId: number): void {
    this.communityProfileService.joinCommunity(communityId).subscribe({
      next: () => {
        this.router.navigate(['/communities', communityId]);
      },
      error: () => alert('No se pudo unir a la comunidad.')
    });
  }

  enterCommunity(communityId: number): void {
    this.router.navigate(['/communities', communityId]);
  }

  openCommunityModal(community: CommunityResponseDTO): void {
    this.selectedCommunity = community;
  }

  isSubscribed(communityId: number): boolean {
    return this.subscribedCommunities.some(c => c.id === communityId);
  }
}
