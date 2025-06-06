import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/user/login/login.component';
import { CommunityListComponent } from './pages/communities/community-list/community-list.component';
import { RegisterComponent } from './pages/user/register/register.component';
import { CreateCommunityComponent } from './pages/communities/create-community/create-community.component';
import { SearchResultsComponent } from './pages/communities/search-results/search-results.component';
import { ProfileComponent } from './pages/user/profile/profile.component';
import { CommunityDetailComponent } from './pages/communities/community-detail/community-detail.component';
import { CommunityProfileDetailComponent } from './pages/communities/community-profile-detail/community-profile-detail.component';
import { PostCreateComponent } from './pages/posts/post-create/post-create.component';
import { PostDetailComponent } from './pages/posts/post-detail/post-detail.component';
import { SearchPostsComponent } from './pages/posts/search-posts/search-posts.component';
import { CommunityNotificationsComponent } from './pages/communities/community-notifications/community-notifications.component';
import { CreateRoleComponent } from './pages/rol/create-role/create-role.component';
import { EditCommunityComponent } from './pages/communities/edit-community/edit-community.component';

const routes: Routes = [
  { path: 'communities', component: CommunityListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'communities/create', component: CreateCommunityComponent },
  { path: 'communities/search', component: SearchResultsComponent },
  { path: 'communities/:id', component: CommunityDetailComponent },
  { path: 'communities/profile/:id', component: CommunityProfileDetailComponent },
  { path: 'communities/:id/create-post/:profileId', component: PostCreateComponent },
  { path: 'communities/:communityId/search-posts', component: SearchPostsComponent },
  { path: 'communities/:id/notifications', component: CommunityNotificationsComponent},
  { path: 'communities/:id/create-role',component: CreateRoleComponent},
  { path: 'posts/:id', component: PostDetailComponent },
  { path: 'communities/:id/edit', component: EditCommunityComponent },
  { path: '', redirectTo: '/communities', pathMatch: 'full' },
  { path: '**', redirectTo: '/communities' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

