import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomePage } from './features/home/home-page/home-page';
import { DocumentList } from './features/documents/document-list/document-list';
import { DocumentRegister } from './features/documents/document-register/document-register';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';

export const routes: Routes = [
  { path: '', component: HomePage },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'documents/register', component: DocumentRegister, canActivate: [authGuard] },
  { path: 'documents', component: DocumentList, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
