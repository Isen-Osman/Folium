import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DocumentsService } from '../../../core/services/documents.service';
import { Document } from '../../../core/models/document';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';
import { DocumentDetails } from '../document-details/document-details';
import { DocumentUpload } from '../document-upload/document-upload';

@Component({
  imports: [DatePipe, StatusMessage, DocumentDetails, DocumentUpload],
  selector: 'app-document-list',
  styleUrl: './document-list.scss',
  templateUrl: './document-list.html',
})
export class DocumentList implements OnInit {
  private readonly documentsService = inject(DocumentsService);

  protected readonly documents = signal<Document[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly selectedDocumentId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadDocuments();
  }

  protected loadDocuments(): void {
    this.loading.set(true);
    this.error.set('');

    this.documentsService.getDocuments().subscribe({
      next: (documents) => {
        this.documents.set(documents);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(apiErrorMessage(error, 'Не можам да ги вчитам документите.'));
        this.loading.set(false);
      },
    });
  }

  protected openDocument(id: number): void {
    this.selectedDocumentId.set(id);
  }

  protected closeDocument(): void {
    this.selectedDocumentId.set(null);
    this.loadDocuments();
  }
}
