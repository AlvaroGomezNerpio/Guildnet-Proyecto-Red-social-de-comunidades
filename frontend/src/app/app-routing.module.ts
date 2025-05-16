import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { CommunityListComponent } from './pages/communities/community-list/community-list.component';
import { RegisterComponent } from './pages/register/register.component';
import { CreateCommunityComponent } from './pages/communities/create-community/create-community.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'communities', component: CommunityListComponent },
  { path: 'communities/create', component: CreateCommunityComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

