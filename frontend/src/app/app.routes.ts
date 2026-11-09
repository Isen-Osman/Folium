import { Routes } from '@angular/router';
import { HomePage } from './features/home/home-page/home-page';
import { DocumentList } from './features/documents/document-list/document-list';

export const routes: Routes = [
  { path: '', component: HomePage },
  { path: 'documents', component: DocumentList },
  { path: '**', redirectTo: '' },
];
