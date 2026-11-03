import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';

/** Cada página se carga bajo demanda (lazy loading), así el bundle inicial es pequeño. */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/pages/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'communities/search',
    loadComponent: () =>
      import('./features/communities/pages/search-results/search-results.component').then((m) => m.SearchResultsComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'communities',
        loadComponent: () =>
          import('./features/communities/pages/community-list/community-list.component').then((m) => m.CommunityListComponent),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/user/pages/user-profile/user-profile.component').then((m) => m.UserProfileComponent),
      },
      {
        path: 'communities/create',
        loadComponent: () =>
          import('./features/communities/pages/create-community/create-community.component').then(
            (m) => m.CreateCommunityComponent
          ),
      },
      {
        path: 'communities/profile/:id',
        loadComponent: () =>
          import('./features/profiles/pages/community-profile-detail/community-profile-detail.component').then(
            (m) => m.CommunityProfileDetailComponent
          ),
      },
      {
        path: 'communities/:id',
        loadComponent: () =>
          import('./features/communities/pages/community-detail/community-detail.component').then(
            (m) => m.CommunityDetailComponent
          ),
      },
      {
        path: 'communities/:id/edit',
        loadComponent: () =>
          import('./features/communities/pages/edit-community/edit-community.component').then((m) => m.EditCommunityComponent),
      },
      {
        path: 'communities/:id/notifications',
        loadComponent: () =>
          import('./features/communities/pages/community-notifications/community-notifications.component').then(
            (m) => m.CommunityNotificationsComponent
          ),
      },
      {
        path: 'communities/:id/roles',
        loadComponent: () =>
          import('./features/roles/pages/role-management/role-management.component').then((m) => m.RoleManagementComponent),
      },
      {
        path: 'communities/:id/create-post/:profileId',
        loadComponent: () =>
          import('./features/posts/pages/post-create/post-create.component').then((m) => m.PostCreateComponent),
      },
      {
        path: 'communities/:communityId/search-posts',
        loadComponent: () =>
          import('./features/posts/pages/search-posts/search-posts.component').then((m) => m.SearchPostsComponent),
      },
      {
        path: 'posts/:id',
        loadComponent: () =>
          import('./features/posts/pages/post-detail/post-detail.component').then((m) => m.PostDetailComponent),
      },
      { path: '', redirectTo: 'communities', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'communities' },
];
