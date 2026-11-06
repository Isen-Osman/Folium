import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document, DocumentType } from './document';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>('/api/documents');
  }

  getDocument(id: number): Observable<Document> {
    return this.http.get<Document>(`/api/documents/${id}`);
  }

  createDocument(file: File, type: DocumentType): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<Document>('/api/documents', formData);
  }
}
