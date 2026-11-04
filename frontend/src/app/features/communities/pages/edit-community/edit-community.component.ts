import { Component, OnInit, ChangeDetectionStrategy, effect, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommunityService } from '../../services/community.service';
import { CommunityPermissionsService } from '../../services/community-permissions.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { CommunityResponseDTO } from '../../models/community.models';
import { errorMessage } from '../../../../core/http/http-error';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { QuillEditorComponent } from 'ngx-quill';
import { toSignal } from '@angular/core/rxjs-interop';
import { PalettePickerComponent } from '../../components/palette-picker/palette-picker.component';
import { injectCommunityTheme } from '../../../../core/theme/community-theme.service';
import { DEFAULT_PALETTE } from '../../../../core/theme/community-palette';

@Component({
    selector: 'app-edit-community',
    templateUrl: './edit-community.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './edit-community.component.css',
    imports: [FormsModule, ReactiveFormsModule, QuillEditorComponent, RouterLink, PalettePickerComponent]
})
export class EditCommunityComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly communityService = inject(CommunityService);
  private readonly permissionsService = inject(CommunityPermissionsService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

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
    primaryColor: [DEFAULT_PALETTE.primary],
    accentColor: [DEFAULT_PALETTE.accent],
  });

  private readonly theme = injectCommunityTheme();
  private readonly colors = toSignal(this.editForm.valueChanges, { initialValue: this.editForm.getRawValue() });

  constructor() {
    // Vista previa en vivo: toda la página adopta la paleta mientras se elige
    effect(() => {
      const { primaryColor, accentColor } = this.colors();
      if (this.community && primaryColor && accentColor) {
        this.theme.set({ primary: primaryColor, accent: accentColor });
      }
    });
  }

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
          primaryColor: c.primaryColor ?? DEFAULT_PALETTE.primary,
          accentColor: c.accentColor ?? DEFAULT_PALETTE.accent,
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
      primaryColor: value.primaryColor,
      accentColor: value.accentColor,
    };

    this.saving = true;
    this.communityService.updateCommunity(this.community.id, data, this.imageFile, this.bannerFile).subscribe({
      next: (updated) => {
        this.theme.apply(updated.id, { primary: updated.primaryColor, accent: updated.accentColor });
        this.toast.success('Comunidad actualizada.');
        this.router.navigate(['/communities', updated.id]);
      },
      error: (err) => {
        this.saving = false;
        this.error = errorMessage(err, 'Ocurrió un error al actualizar la comunidad.');
      },
    });
  }

  async onDelete(): Promise<void> {
    const community = this.community;
    if (!community) return;
    const accepted = await this.confirm.ask({
      title: 'Eliminar comunidad',
      message: `Se borrará "${community.name}" con todas sus publicaciones, miembros, roles y mensajes. Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar comunidad',
      danger: true,
    });
    if (!accepted) return;

    this.communityService.deleteCommunity(community.id).subscribe({
      next: () => {
        this.toast.info('Comunidad eliminada.');
        this.router.navigate(['/communities']);
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar la comunidad.')),
    });
  }
}
