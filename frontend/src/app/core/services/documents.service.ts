import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BatchProcessResponse,
  Document,
  ExtractedData,
  ExtractedField,
  ProcessingJob,
  RegisterRow,
} from '../models/document';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>('/api/documents');
  }

  getDocument(id: number): Observable<Document> {
    return this.http.get<Document>(`/api/documents/${id}`);
  }

  getDocumentFile(id: number): Observable<Blob> {
    return this.http.get(`/api/documents/${id}/file`, { responseType: 'blob' });
  }

  createDocument(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Document>('/api/documents', formData);
  }

  renameDocument(id: number, fileName: string): Observable<Document> {
    return this.http.patch<Document>(`/api/documents/${id}`, { fileName });
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`/api/documents/${id}`);
  }

  processDocument(id: number): Observable<ProcessingJob> {
    return this.http.post<ProcessingJob>(`/api/processing/${id}`, null);
  }

  processBatch(ids: number[]): Observable<BatchProcessResponse> {
    return this.http.post<BatchProcessResponse>('/api/processing/batch', { documentIds: ids });
  }

  getExtractedData(id: number): Observable<ExtractedData> {
    return this.http.get<ExtractedData>(`/api/documents/${id}/extracted-data`);
  }

  updateFields(id: number, fields: ExtractedField[]): Observable<ExtractedData> {
    return this.http.put<ExtractedData>(`/api/documents/${id}/extracted-data/fields`, {
      fields: fields.map((field) => ({ name: field.name, value: field.value })),
    });
  }

  getRegister(): Observable<RegisterRow[]> {
    return this.http.get<RegisterRow[]>('/api/documents/register');
  }
}
