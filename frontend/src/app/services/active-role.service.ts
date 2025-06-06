import { Injectable } from '@angular/core';
import { ActiveRole } from '../models/communityProfile/ActiveRole';

@Injectable({
  providedIn: 'root'
})
export class ActiveRoleService {
  private activeRole: ActiveRole | null = null;

  setActiveRole(role: ActiveRole): void {
    this.activeRole = role;
  }

  getActiveRole(): ActiveRole | null {
    return this.activeRole;
  }

  hasPermission(permission: string): boolean {
    return this.activeRole?.permissions.includes(permission) ?? false;
  }

  isForCommunity(communityId: number): boolean {
    return this.activeRole?.communityId === communityId;
  }

  clear(): void {
    this.activeRole = null;
  }
}
