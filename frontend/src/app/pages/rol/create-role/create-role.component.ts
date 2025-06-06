import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RoleService } from '../../../services/role.service';
import { PermissionService } from '../../../services/permission.service';
import { RolePermissionService } from '../../../services/role-permission.service';
import { RoleDTO } from '../../../models/rol/RoleDTO';
import { PermissionDTO } from '../../../models/rol/PermissionDTO';
import { RolePermissionCreateDTO } from '../../../models/rol/RolePermissionCreateDTO';
import { ActiveRoleService } from '../../../services/active-role.service';


@Component({
  selector: 'app-create-role',
  standalone: false,
  templateUrl: './create-role.component.html',
  styleUrl: './create-role.component.css'
})
export class CreateRoleComponent implements OnInit {
  roleForm!: FormGroup;
  permissions: PermissionDTO[] = [];
  selectedPermissions: number[] = [];
  communityId!: number;

  constructor(
    private fb: FormBuilder,
    private roleService: RoleService,
    private permissionService: PermissionService,
    private rolePermissionService: RolePermissionService,
    private route: ActivatedRoute,
    private router: Router,
    private activeRoleService: ActiveRoleService
  ) {}

  ngOnInit(): void {
  this.communityId = +this.route.snapshot.paramMap.get('id')!;

  // Comprobar permisos
  if (
    !this.activeRoleService.isForCommunity(this.communityId) ||
    !this.activeRoleService.hasPermission('ASSIGN_ROLES')
  ) {
    this.router.navigate(['/communities']);
    return;
  }

  // Inicializar el formulario
  this.roleForm = this.fb.group({
    name: ['', Validators.required],
    textColor: ['#000000', Validators.required],
    backgroundColor: ['#ffffff', Validators.required],
  });

  this.loadPermissions();
}


  loadPermissions(): void {
    this.permissionService.getAllPermissions().subscribe({
      next: (data) => this.permissions = data,
      error: () => alert('Error al cargar los permisos')
    });
  }

  onPermissionToggle(permissionId: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedPermissions.push(permissionId);
    } else {
      this.selectedPermissions = this.selectedPermissions.filter(id => id !== permissionId);
    }
  }

  onSubmit(): void {
    if (this.roleForm.invalid) return;

    const roleData: RoleDTO = {
      ...this.roleForm.value,
      communityId: this.communityId
    };

    this.roleService.createRole(roleData).subscribe({
      next: (createdRole) => {
        this.selectedPermissions.forEach(permissionId => {
          const dto: RolePermissionCreateDTO = {
            roleId: createdRole.id,
            permissionId
          };
          this.rolePermissionService.assignPermission(dto).subscribe();
        });

        alert('Rol creado con éxito');
        this.roleForm.reset();
        this.selectedPermissions = [];
      },
      error: () => alert('Error al crear el rol')
    });
  }
}
