import { Component, OnInit, ChangeDetectionStrategy, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommunityService } from '../../services/community.service';
import { CommunityPermissionsService } from '../../services/community-permissions.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { errorMessage } from '../../../../core/http/http-error';
import { QuillEditorComponent } from 'ngx-quill';

@Component({
    selector: 'app-edit-community',
    templateUrl: './edit-community.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './edit-community.component.css',
    imports: [FormsModule, ReactiveFormsModule, QuillEditorComponent, RouterLink]
})
export class EditCommunityComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly communityService = inject(CommunityService);
  private readonly permissionsService = inject(CommunityPermissionsService);
  private readonly authService = inject(AuthService);

  community: CommunityResponseDTO | null = null;
  imageFile: File | null = null;
  bannerFile: File | null = null;
  saving = false;
  error: string | null = null;

  readonly editForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
    rules: [''],
    tags: [''],
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.permissionsService.getMyPermissions(id).subscribe((permissions) => {
      if (!permissions.includes('MANAGE_COMMUNITY_SETTINGS')) {
        this.router.navigate(['/communities', id]);
        return;
      }
      this.communityService.getCommunityById(id).subscribe((c) => {
        this.community = c;
        this.editForm.setValue({
          name: c.name,
          description: c.description ?? '',
          rules: c.rules ?? '',
          tags: c.tags.join(', '),
        });
      });
    });
  }

  /** Solo el creador puede borrar la comunidad. */
  get isOwner(): boolean {
    return !!this.community && this.community.ownerId === this.authService.getCurrentUserId();
  }

  onImageSelected(event: Event): void {
    this.imageFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onBannerSelected(event: Event): void {
    this.bannerFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onSubmit(): void {
    if (!this.community || this.editForm.invalid) return;

    const value = this.editForm.getRawValue();
    const data = {
      name: value.name.trim(),
      description: value.description,
      rules: value.rules,
      tags: value.tags.split(',').map((t) => t.trim()).filter((t) => t.length > 0),
    };

    this.saving = true;
    this.communityService.updateCommunity(this.community.id, data, this.imageFile, this.bannerFile).subscribe({
      next: (updated) => this.router.navigate(['/communities', updated.id]),
      error: (err) => {
        this.saving = false;
        this.error = errorMessage(err, 'Ocurrió un error al actualizar la comunidad.');
      },
    });
  }

  onDelete(): void {
    if (!this.community || !confirm('¿Seguro que quieres eliminar esta comunidad? Se borrará todo su contenido.')) return;

    this.communityService.deleteCommunity(this.community.id).subscribe({
      next: () => this.router.navigate(['/communities']),
      error: (err) => (this.error = errorMessage(err, 'No se pudo eliminar la comunidad.')),
    });
  }
}
