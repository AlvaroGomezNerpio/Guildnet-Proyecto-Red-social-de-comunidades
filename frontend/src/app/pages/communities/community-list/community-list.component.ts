import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';

import { CommunityService } from '../../../services/community.service';
import { CommunityResponseDTO } from '../../../models/community/CommunityResponse.dto';
import { CommunityProfileService } from '../../../services/community-profile.service';

@Component({
  selector: 'app-community-list',
  standalone: false,
  templateUrl: './community-list.component.html',
  styleUrls: ['./community-list.component.css']
})
export class CommunityListComponent implements OnInit {
  subscribedCommunities: CommunityResponseDTO[] = [];
  suggestedCommunities: CommunityResponseDTO[] = [];
  popularCommunities: CommunityResponseDTO[] = [];

  filteredPopularCommunities: CommunityResponseDTO[] = [];

  selectedCommunity: CommunityResponseDTO | null = null;
  isLoggedIn: boolean = false;
  error: string | null = null;

  searchForm!: FormGroup;

  constructor(
    private communityService: CommunityService,
    private communityProfileService: CommunityProfileService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = !!localStorage.getItem('token');

    this.searchForm = this.fb.group({
      name: [''],
      tags: ['']
    });

    if (this.isLoggedIn) {
      this.loadSubscribedCommunities();
      this.loadSuggestedCommunities();
    }

    this.loadPopularCommunities();
  }

  loadSubscribedCommunities(): void {
    this.communityService.getSubscribedCommunities().subscribe({
      next: data => {
        this.subscribedCommunities = data;
        this.filterPopularCommunities(); // filtrar populares al obtener subscritas
      },
      error: () => this.error = 'No se pudieron cargar las comunidades suscritas.'
    });
  }

  loadSuggestedCommunities(): void {
    this.communityService.getSuggestedCommunities().subscribe({
      next: data => this.suggestedCommunities = data.slice(0, 10),
      error: () => this.error = 'No se pudieron cargar las comunidades sugeridas.'
    });
  }

  loadPopularCommunities(): void {
    this.communityService.getPopularCommunities().subscribe({
      next: data => {
        this.popularCommunities = data.slice(0, 10);
        this.filterPopularCommunities();
      },
      error: () => this.error = 'No se pudieron cargar las comunidades populares.'
    });
  }

  filterPopularCommunities(): void {
    const subscribedIds = this.subscribedCommunities.map(c => c.id);
    this.filteredPopularCommunities = this.popularCommunities.filter(
      community => !subscribedIds.includes(community.id)
    );
  }

  onCreateCommunity(): void {
    this.router.navigate(['/communities/create']);
  }

  onJoinCommunity(communityId: number): void {
    this.communityProfileService.joinCommunity(communityId).subscribe({
      next: () => {
        this.router.navigate(['/communities', communityId]); // redirige tras unirse
      },
      error: () => alert('No se pudo unir a la comunidad.')
    });
  }

  openCommunityModal(community: CommunityResponseDTO): void {
    this.selectedCommunity = community;
  }

  enterCommunity(communityId: number): void {
    this.router.navigate(['/communities/', communityId]);
  }
}
