import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommunityService } from '../../../services/community.service';
import { CommunityProfileService } from '../../../services/community-profile.service';
import { PostService } from '../../../services/post.service';
import { CommunityResponseDTO } from '../../../models/community/CommunityResponse.dto';
import { CommunityProfileDTO } from '../../../models/communityProfile/CommunityProfileDTO';
import { PostDTO } from '../../../models/post/PostDTO';
import { AuthService } from '../../../services/auth.service';


@Component({
  selector: 'app-community-detail',
  standalone: false,
  templateUrl: './community-detail.component.html',
  styleUrl: './community-detail.component.css'
})
export class CommunityDetailComponent {
  communityId!: number;
  community: CommunityResponseDTO | null = null;
  profile: CommunityProfileDTO | null = null;
  posts: PostDTO[] = [];

  isLoggedIn: boolean = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private communityService: CommunityService,
    private communityProfileService: CommunityProfileService,
    private postService: PostService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = !!localStorage.getItem('token');

    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.communityId = +idParam;
        this.loadCommunity();
        this.loadPosts();
        if (this.isLoggedIn) {
          this.loadUserProfile();
        }
      }
    });
  }

  loadCommunity(): void {
    this.communityService.getCommunityById(this.communityId).subscribe({
      next: data => this.community = data,
      error: () => this.error = 'No se pudo cargar la comunidad.'
    });
  }

  loadUserProfile(): void {
    this.communityProfileService.getMyProfileInCommunity(this.communityId).subscribe({
      next: data => this.profile = data,
      error: () => this.profile = null // No mostrar error si no está unido
    });
  }

  loadPosts(): void {
    this.postService.getPostsByCommunity(this.communityId).subscribe({
      next: data => this.posts = data,
      error: () => this.error = 'No se pudieron cargar los posts.'
    });
  }

  goToProfile(): void {
    if (this.profile) {
      this.router.navigate(['/communities/profile', this.communityId]);
    }
  }

  goToCreatePost(): void {
  if (this.profile) {
    this.router.navigate(['/communities', this.communityId, 'create-post', this.profile.id]);
  }
}



}
