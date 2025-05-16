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


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    CommunityListComponent,
    RegisterComponent,
    CreateCommunityComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }


