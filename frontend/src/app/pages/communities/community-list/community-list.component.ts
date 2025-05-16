import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityService } from '../../../services/community.service';
import { CommunityResponseDTO } from '../../../models/community/CommunityResponse.dto';
interface Community {
  id: number;
  name: string;
  description: string;
}

@Component({
  selector: 'app-community-list',
  standalone: false,
  templateUrl: './community-list.component.html',
  styleUrl: './community-list.component.css'
})
export class CommunityListComponent implements OnInit {
  subscribedCommunities: CommunityResponseDTO[] = [];
  suggestedCommunities: CommunityResponseDTO[] = [];
  popularCommunities: CommunityResponseDTO[] = [];
  error: string | null = null;

  constructor(private communityService: CommunityService, private router: Router) {}

  ngOnInit(): void {
    this.communityService.getSubscribedCommunities().subscribe({
      next: (data) => this.subscribedCommunities = data,
      error: () => this.error = 'No se pudieron cargar las comunidades suscritas.'
    });

    this.communityService.getSuggestedCommunities().subscribe({
      next: (data) => this.suggestedCommunities = data.slice(0, 10), // Limitar a 10 comunidades sugeridas
      error: () => this.error = 'No se pudieron cargar las comunidades sugeridas.'
    });

    this.communityService.getPopularCommunities().subscribe({
      next: (data) => this.popularCommunities = data.slice(0, 10), // Limitar a 10 comunidades populares
      error: () => this.error = 'No se pudieron cargar las comunidades sugeridas.'
    });
  }

  onCreateCommunity(): void {
    this.router.navigate(['/communities/create']);
  }

  onJoinCommunity(id: number): void {
    this.communityService.joinCommunity(id).subscribe({
      next: () => {
        // Puedes recargar o mostrar mensaje
        alert('Te has unido correctamente.');
      },
      error: () => {
        alert('No se pudo unir a la comunidad.');
      }
    });
  }
}
