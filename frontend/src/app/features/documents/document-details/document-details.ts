import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { DocumentsService } from '../../../core/services/documents.service';
import { Document, ExtractedData } from '../../../core/models/document';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';

@Component({
  selector: 'app-document-details',
  imports: [DatePipe, StatusMessage],
  styleUrl: './document-details.scss',
  templateUrl: './document-details.html',
})
export class DocumentDetails implements OnInit {
  private readonly documentsService = inject(DocumentsService);

  readonly documentId = input.required<number>();
  readonly back = output<void>();

  protected readonly document = signal<Document | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly extractedData = signal<ExtractedData | null>(null);
  protected readonly extractedLoading = signal(false);
  protected readonly extractedError = signal('');
  protected readonly processing = signal(false);
  protected readonly processingError = signal('');
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal('');

  ngOnInit(): void {
    this.loadDocument();
  }

  protected processDocument(): void {
    const document = this.document();
    if (!document) return;

    this.processing.set(true);
    this.processingError.set('');

    this.documentsService.processDocument(document.id).subscribe({
      next: () => {
        this.processing.set(false);
        this.reloadDocument();
      },
      error: (error: unknown) => {
        this.processingError.set(apiErrorMessage(error, 'Не можам да го процесирам документот.'));
        this.processing.set(false);
      },
    });
  }

  protected deleteDocument(): void {
    const document = this.document();
    if (!document) return;
    if (!window.confirm(`Да се избрише „${document.fileName}“?`)) return;

    this.deleting.set(true);
    this.deleteError.set('');

    this.documentsService.deleteDocument(document.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.back.emit();
      },
      error: (error: unknown) => {
        this.deleteError.set(apiErrorMessage(error, 'Не можам да го избришам документот.'));
        this.deleting.set(false);
      },
    });
  }

  protected goBack(): void {
    this.back.emit();
  }

  private loadDocument(): void {
    this.loading.set(true);
    this.error.set('');

    this.documentsService.getDocument(this.documentId()).subscribe({
      next: (document) => {
        this.document.set(document);
        this.loading.set(false);
        if (document.status === 'COMPLETED') {
          this.loadExtractedData();
        }
      },
      error: (error: unknown) => {
        this.error.set(apiErrorMessage(error, 'Не можам да го вчитам документот.'));
        this.loading.set(false);
      },
    });
  }

  private reloadDocument(): void {
    this.documentsService.getDocument(this.documentId()).subscribe({
      next: (document) => {
        this.document.set(document);
        if (document.status === 'COMPLETED') {
          this.loadExtractedData();
        }
      },
      error: (error: unknown) => {
        this.error.set(apiErrorMessage(error, 'Не можам да го вчитам документот.'));
      },
    });
  }

  private loadExtractedData(): void {
    this.extractedLoading.set(true);
    this.extractedError.set('');

    this.documentsService.getExtractedData(this.documentId()).subscribe({
      next: (data) => {
        this.extractedData.set(data);
        this.extractedLoading.set(false);
      },
      error: (error: unknown) => {
        this.extractedError.set(apiErrorMessage(error, 'Не можам да ги вчитам извлечените податоци.'));
        this.extractedLoading.set(false);
      },
    });
  }
}
