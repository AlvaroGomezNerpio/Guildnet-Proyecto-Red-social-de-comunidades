import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './pages/user/login/login.component';
import { CommunityListComponent } from './pages/communities/community-list/community-list.component';
import { RegisterComponent } from './pages/user/register/register.component';
import { CreateCommunityComponent } from './pages/communities/create-community/create-community.component';
import { SearchResultsComponent } from './pages/communities/search-results/search-results.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HeaderComponent } from './shared/header/header.component';
import { FooterComponent } from './shared/footer/footer.component';
import { ProfileComponent } from './pages/user/profile/profile.component';
import { CommunityDetailComponent } from './pages/communities/community-detail/community-detail.component';
import { CommunityProfileDetailComponent } from './pages/communities/community-profile-detail/community-profile-detail.component';
import { QuillModule } from 'ngx-quill';
import { PostCreateComponent } from './pages/posts/post-create/post-create.component';
import { PostDetailComponent } from './pages/posts/post-detail/post-detail.component';
import { SearchPostsComponent } from './pages/posts/search-posts/search-posts.component';
import { CommunityNotificationsComponent } from './pages/communities/community-notifications/community-notifications.component';
import { CreateRoleComponent } from './pages/rol/create-role/create-role.component';
import { EditCommunityComponent } from './pages/communities/edit-community/edit-community.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    CommunityListComponent,
    RegisterComponent,
    CreateCommunityComponent,
    SearchResultsComponent,
    HeaderComponent,
    FooterComponent,
    ProfileComponent,
    CommunityDetailComponent,
    CommunityProfileDetailComponent,
    PostCreateComponent,
    PostDetailComponent,
    SearchPostsComponent,
    CommunityNotificationsComponent,
    CreateRoleComponent,
    EditCommunityComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    QuillModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }


