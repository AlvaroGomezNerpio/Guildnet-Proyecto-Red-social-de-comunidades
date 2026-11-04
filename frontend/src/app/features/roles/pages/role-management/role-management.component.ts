import { Component, OnInit, ChangeDetectionStrategy, computed, effect, inject } from '@angular/core';
import { injectCommunityTheme } from '../../../../core/theme/community-theme.service';
import { FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { RoleService } from '../../services/role.service';
import { PermissionService } from '../../services/permission.service';
import { RolePermissionService } from '../../services/role-permission.service';
import { CommunityPermissionsService } from '../../../communities/services/community-permissions.service';
import { RoleDTO, PermissionDTO, RolePermissionDTO } from '../../models/role.models';
import { errorMessage } from '../../../../core/http/http-error';
import { ToastService } from '../../../../shared/ui/toast/toast.service';
import { ConfirmService } from '../../../../shared/ui/confirm/confirm.service';
import { BadgeColorPickerComponent } from '../../../../shared/ui/badge-color-picker/badge-color-picker.component';
import { DEFAULT_PALETTE, readableOn } from '../../../../core/theme/community-palette';

interface RoleWithPermissions extends RoleDTO {
  permissions: RolePermissionDTO[];
}

/** Gestión de roles de una comunidad: listar, crear y eliminar. Requiere ASSIGN_ROLES. */
@Component({
    selector: 'app-role-management',
    templateUrl: './role-management.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './role-management.component.css',
    imports: [RouterLink, FormsModule, ReactiveFormsModule, BadgeColorPickerComponent]
})
export class RoleManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly roleService = inject(RoleService);
  private readonly permissionService = inject(PermissionService);
  private readonly rolePermissionService = inject(RolePermissionService);
  private readonly communityPermissions = inject(CommunityPermissionsService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);
  private readonly theme = injectCommunityTheme();

  communityId!: number;
  permissions: PermissionDTO[] = [];
  selectedPermissions = new Set<number>();
  roles: RoleWithPermissions[] = [];

  saving = false;
  error: string | null = null;

  readonly roleForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    textColor: [readableOn(DEFAULT_PALETTE.primary), Validators.required],
    backgroundColor: [DEFAULT_PALETTE.primary, Validators.required],
  });

  /** Color principal de la comunidad: color inicial de los roles nuevos. */
  readonly communityColor = computed(() => this.theme.palette()?.primary ?? null);
  /** Si el usuario ya ha tocado los colores, no se le pisan al cargar la paleta. */
  private colorsTouched = false;

  constructor() {
    effect(() => {
      const color = this.communityColor();
      if (color && !this.colorsTouched) this.applyDefaultColors(color);
    });
  }

  setTextColor(value: string): void {
    this.colorsTouched = true;
    this.roleForm.controls.textColor.setValue(value);
  }

  setBackgroundColor(value: string): void {
    this.colorsTouched = true;
    this.roleForm.controls.backgroundColor.setValue(value);
  }

  private applyDefaultColors(color: string): void {
    this.roleForm.patchValue({ backgroundColor: color, textColor: readableOn(color) });
  }

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('id'));
    this.theme.useCommunity(this.communityId);

    this.communityPermissions.getMyPermissions(this.communityId).subscribe((perms) => {
      if (!perms.includes('ASSIGN_ROLES')) {
        this.router.navigate(['/communities', this.communityId]);
        return;
      }
      this.permissionService.getAllPermissions().subscribe({
        next: (data) => (this.permissions = data),
        error: () => (this.error = 'Error al cargar los permisos.'),
      });
      this.loadRoles();
    });
  }

  loadRoles(): void {
    this.roleService
      .getRolesByCommunity(this.communityId)
      .pipe(
        switchMap((roles) =>
          roles.length === 0
            ? of([] as RoleWithPermissions[])
            : forkJoin(
                roles.map((role) =>
                  this.rolePermissionService
                    .getPermissionsByRole(role.id)
                    .pipe(switchMap((permissions) => of({ ...role, permissions })))
                )
              )
        )
      )
      .subscribe({
        next: (roles) => (this.roles = roles),
        error: () => (this.error = 'Error al cargar los roles.'),
      });
  }

  togglePermission(permissionId: number, checked: boolean): void {
    if (checked) {
      this.selectedPermissions.add(permissionId);
    } else {
      this.selectedPermissions.delete(permissionId);
    }
  }

  onSubmit(): void {
    if (this.roleForm.invalid) return;
    this.saving = true;
    this.error = null;

    this.roleService
      .createRole({ ...this.roleForm.getRawValue(), communityId: this.communityId })
      .pipe(
        switchMap((created) =>
          this.selectedPermissions.size === 0
            ? of([])
            : forkJoin(
                [...this.selectedPermissions].map((permissionId) =>
                  this.rolePermissionService.assignPermission({ roleId: created.id, permissionId })
                )
              )
        )
      )
      .subscribe({
        next: () => {
          this.saving = false;
          this.toast.success('Rol creado.');
          this.roleForm.reset();
          this.colorsTouched = false;
          this.applyDefaultColors(this.communityColor() ?? DEFAULT_PALETTE.primary);
          this.selectedPermissions.clear();
          this.loadRoles();
        },
        error: (err) => {
          this.saving = false;
          this.error = errorMessage(err, 'Error al crear el rol.');
        },
      });
  }

  async deleteRole(role: RoleDTO): Promise<void> {
    const accepted = await this.confirm.ask({
      title: 'Eliminar rol',
      message: `¿Eliminar el rol "${role.name}"? Se quitará a todos los miembros que lo tengan.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!accepted) return;

    this.roleService.deleteRole(role.id).subscribe({
      next: () => {
        this.toast.success('Rol eliminado.');
        this.loadRoles();
      },
      error: (err) => this.toast.error(errorMessage(err, 'No se pudo eliminar el rol.')),
    });
  }

  permissionLabel(name: string): string {
    return this.permissions.find((p) => p.name === name)?.description ?? name;
  }
}
