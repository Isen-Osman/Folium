import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DocumentsService } from '../../../core/services/documents.service';
import { Document, DocumentStatus } from '../../../core/models/document';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';
import { StatusBadge } from '../../../shared/components/status-badge';
import { DocumentDetails } from '../document-details/document-details';
import { DocumentUpload } from '../document-upload/document-upload';

function isPendingStatus(status: DocumentStatus): boolean {
  return status === 'UPLOADED' || status === 'PENDING' || status === 'PROCESSING';
}

@Component({
  imports: [DatePipe, StatusMessage, StatusBadge, DocumentDetails, DocumentUpload],
  selector: 'app-document-list',
  styleUrl: './document-list.scss',
  templateUrl: './document-list.html',
})
export class DocumentList implements OnInit, OnDestroy {
  private readonly documentsService = inject(DocumentsService);
  private pollTimer: ReturnType<typeof setInterval> | null = null;

  protected readonly documents = signal<Document[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly selectedDocumentId = signal<number | null>(null);
  private readonly activeBatchIds = signal<number[]>([]);

  protected readonly processingCount = computed(() => {
    const ids = this.activeBatchIds();
    return this.documents().filter(
      (document) => ids.includes(document.id) && isPendingStatus(document.status),
    ).length;
  });

  ngOnInit(): void {
    this.loadDocuments();
    this.pollTimer = setInterval(() => this.pollBatch(), 2000);
  }

  ngOnDestroy(): void {
    if (this.pollTimer !== null) {
      clearInterval(this.pollTimer);
    }
  }

  protected loadDocuments(showLoading = true): void {
    if (showLoading) {
      this.loading.set(true);
    }
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

  protected onUploaded(ids: number[]): void {
    this.activeBatchIds.set(ids);
    this.loadDocuments(false);
  }

  private pollBatch(): void {
    const ids = this.activeBatchIds();
    if (ids.length === 0 || this.selectedDocumentId() !== null) {
      return;
    }

    const byId = new Map(this.documents().map((document) => [document.id, document]));
    const allDone = ids.every((id) => {
      const document = byId.get(id);
      return document !== undefined && !isPendingStatus(document.status);
    });

    if (allDone) {
      this.activeBatchIds.set([]);
    }
    this.loadDocuments(false);
  }

  protected openDocument(id: number): void {
    this.selectedDocumentId.set(id);
  }

  protected closeDocument(): void {
    this.selectedDocumentId.set(null);
    this.loadDocuments();
  }
}
