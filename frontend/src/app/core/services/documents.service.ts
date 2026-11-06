import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document, DocumentType, ExtractedData, ProcessingJob } from '../models/document';

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

  processDocument(id: number): Observable<ProcessingJob> {
    return this.http.post<ProcessingJob>(`/api/processing/${id}`, null);
  }

  getExtractedData(id: number): Observable<ExtractedData> {
    return this.http.get<ExtractedData>(`/api/documents/${id}/extracted-data`);
  }
}
