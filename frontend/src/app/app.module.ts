import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './pages/login/login.component';
import { CommunityListComponent } from './pages/communities/community-list/community-list.component';
import { RegisterComponent } from './pages/register/register.component';
import { CreateCommunityComponent } from './pages/communities/create-community/create-community.component';
import { SearchResultsComponent } from './pages/search-results/search-results.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HeaderComponent } from './shared/header/header.component';
import { FooterComponent } from './shared/footer/footer.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { CommunityDetailComponent } from './pages/communities/community-detail/community-detail.component';
import { CommunityProfileDetailComponent } from './pages/community-profile-detail/community-profile-detail.component';
import { QuillModule } from 'ngx-quill';
import { PostCreateComponent } from './pages/posts/post-create/post-create.component';
import { PostDetailComponent } from './components/post-detail/post-detail.component';
import { SearchPostsComponent } from './components/post/search-posts/search-posts.component';
import { CommunityNotificationsComponent } from './components/community-notifications/community-notifications.component';

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
    CommunityNotificationsComponent
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


