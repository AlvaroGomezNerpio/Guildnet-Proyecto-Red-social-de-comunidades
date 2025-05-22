import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { CommunityListComponent } from './pages/communities/community-list/community-list.component';
import { RegisterComponent } from './pages/register/register.component';
import { CreateCommunityComponent } from './pages/communities/create-community/create-community.component';
import { SearchResultsComponent } from './pages/search-results/search-results.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { CommunityDetailComponent } from './pages/communities/community-detail/community-detail.component';
import { CommunityProfileDetailComponent } from './pages/community-profile-detail/community-profile-detail.component';

const routes: Routes = [
  { path: 'communities', component: CommunityListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'communities/create', component: CreateCommunityComponent },
  { path: 'communities/search', component: SearchResultsComponent },
  { path: 'communities/:id', component: CommunityDetailComponent },
  { path: 'communities/profile/:id', component: CommunityProfileDetailComponent },
  { path: '', redirectTo: '/communities', pathMatch: 'full' },
  { path: '**', redirectTo: '/communities' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

