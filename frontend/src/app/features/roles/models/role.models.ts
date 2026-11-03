export interface RoleDTO {
  id: number;
  name: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
}

export interface PermissionDTO {
  id: number;
  name: string; 
  description: string;
}

export interface RolePermissionDTO {
  id: number;
  roleId: number;
  roleName: string;
  permissionId: number;
  permissionName: string;
}

export interface RolePermissionCreateDTO {
  roleId: number;
  permissionId: number;
}
