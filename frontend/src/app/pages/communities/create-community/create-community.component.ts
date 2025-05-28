import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityService } from '../../../services/community.service';
import { CreateCommunityRequest } from '../../../models/community/CreateCommunityRequest.dto';

@Component({
  selector: 'app-create-community',
  standalone: false,
  templateUrl: './create-community.component.html',
  styleUrl: './create-community.component.css',
})
export class CreateCommunityComponent {
  community: CreateCommunityRequest = {
    name: '',
    description: '',
    rules: '',
    tags: [],
  };

  tagsInput: string = '';
  image: File | null = null;
  banner: File | null = null;

  constructor(
    private communityService: CommunityService,
    private router: Router
  ) {}

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.image = input.files[0];
    }
  }

  onBannerSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.banner = input.files[0];
    }
  }

  onSubmit(): void {
    this.community.tags = this.tagsInput
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    this.communityService
      .createCommunity(this.community, this.image!, this.banner!)
      .subscribe({
        next: () => {
          alert('Comunidad creada correctamente');
          this.router.navigate(['/communities']);
        },
        error: () => {
          alert('No se pudo crear la comunidad');
        },
      });
  }
}
