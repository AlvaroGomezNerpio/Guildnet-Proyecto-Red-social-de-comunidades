import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityService } from '../../services/community.service';
import { CreateCommunityRequest } from '../../models/community.models';
import { errorMessage } from '../../../../core/http/http-error';
import { FormsModule } from '@angular/forms';
import { QuillEditorComponent } from 'ngx-quill';

@Component({
    selector: 'app-create-community',
    templateUrl: './create-community.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './create-community.component.css',
    imports: [FormsModule, QuillEditorComponent],
})
export class CreateCommunityComponent {
  community: CreateCommunityRequest = {
    name: '',
    description: '',
    rules: '',
    tags: [],
  };

  tagsInput = '';
  saving = false;
  error: string | null = null;
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

    this.saving = true;
    this.error = null;
    this.communityService.createCommunity(this.community, this.image, this.banner).subscribe({
      next: (created) => this.router.navigate(['/communities', created.id]),
      error: (err) => {
        this.saving = false;
        this.error = errorMessage(err, 'No se pudo crear la comunidad.');
      },
    });
  }
}
