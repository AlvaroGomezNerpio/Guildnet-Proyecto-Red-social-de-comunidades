import { Component, OnInit, ChangeDetectionStrategy, inject } from '@angular/core';
import { FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { RoleService } from '../../services/role.service';
import { PermissionService } from '../../services/permission.service';
import { RolePermissionService } from '../../services/role-permission.service';
import { CommunityPermissionsService } from '../../../communities/services/community-permissions.service';
import { RoleDTO, PermissionDTO, RolePermissionDTO } from '../../models/role.models';
import { errorMessage } from '../../../../core/http/http-error';

interface RoleWithPermissions extends RoleDTO {
  permissions: RolePermissionDTO[];
}

/** Gestión de roles de una comunidad: listar, crear y eliminar. Requiere ASSIGN_ROLES. */
@Component({
    selector: 'app-role-management',
    templateUrl: './role-management.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './role-management.component.css',
    imports: [RouterLink, FormsModule, ReactiveFormsModule]
})
export class RoleManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly roleService = inject(RoleService);
  private readonly permissionService = inject(PermissionService);
  private readonly rolePermissionService = inject(RolePermissionService);
  private readonly communityPermissions = inject(CommunityPermissionsService);

  communityId!: number;
  permissions: PermissionDTO[] = [];
  selectedPermissions = new Set<number>();
  roles: RoleWithPermissions[] = [];

  saving = false;
  success: string | null = null;
  error: string | null = null;

  readonly roleForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    textColor: ['#000000', Validators.required],
    backgroundColor: ['#ffffff', Validators.required],
  });

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('id'));

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
          this.success = 'Rol creado con éxito.';
          setTimeout(() => (this.success = null), 3000);
          this.roleForm.reset();
          this.selectedPermissions.clear();
          this.loadRoles();
        },
        error: (err) => {
          this.saving = false;
          this.error = errorMessage(err, 'Error al crear el rol.');
        },
      });
  }

  deleteRole(role: RoleDTO): void {
    if (!confirm(`¿Eliminar el rol "${role.name}"? Se quitará a todos los miembros que lo tengan.`)) return;

    this.roleService.deleteRole(role.id).subscribe({
      next: () => this.loadRoles(),
      error: (err) => (this.error = errorMessage(err, 'No se pudo eliminar el rol.')),
    });
  }

  permissionLabel(name: string): string {
    return this.permissions.find((p) => p.name === name)?.description ?? name;
  }
}
