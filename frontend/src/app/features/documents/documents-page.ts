import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DocumentsService } from './documents.service';
import { Document, DocumentType } from './document';
import { apiErrorMessage } from '../../core/api-error-message';
import { StatusMessage } from '../../shared/status-message';

@Component({
  imports: [DatePipe, StatusMessage],
  selector: 'app-documents-page',
  styleUrl: './documents-page.scss',
  templateUrl: './documents-page.html',
})
export class DocumentsPage implements OnInit {
  private readonly documentsService = inject(DocumentsService);

  protected readonly documents = signal<Document[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly selectedDocument = signal<Document | null>(null);
  protected readonly detailLoading = signal(false);
  protected readonly detailError = signal('');
  protected readonly selectedFile = signal<File | null>(null);
  protected readonly selectedType = signal<DocumentType>('PDF');
  protected readonly uploading = signal(false);
  protected readonly uploadError = signal('');
  protected readonly uploadSuccess = signal('');

  ngOnInit(): void {
    this.loadDocuments();
  }

  private loadDocuments(): void {
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

  protected onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  protected onType(event: Event): void {
    this.selectedType.set((event.target as HTMLSelectElement).value as DocumentType);
  }

  protected upload(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const file = this.selectedFile();
    if (!file) return;

    this.uploading.set(true);
    this.uploadError.set('');
    this.uploadSuccess.set('');

    this.documentsService.createDocument(file, this.selectedType()).subscribe({
      next: (document) => {
        this.uploading.set(false);
        this.selectedFile.set(null);
        this.selectedType.set('PDF');
        form.reset();
        this.uploadSuccess.set(`Документот „${document.fileName}“ е прикачен.`);
        this.loadDocuments();
      },
      error: (error: unknown) => {
        this.uploadError.set(apiErrorMessage(error, 'Не можам да го прикачам документот.'));
        this.uploading.set(false);
      },
    });
  }

  protected openDocument(id: number): void {
    this.selectedDocument.set(null);
    this.detailError.set('');
    this.detailLoading.set(true);

    this.documentsService.getDocument(id).subscribe({
      next: (document) => {
        this.selectedDocument.set(document);
        this.detailLoading.set(false);
      },
      error: (error: unknown) => {
        this.detailError.set(apiErrorMessage(error, 'Не можам да го вчитам документот.'));
        this.detailLoading.set(false);
      },
    });
  }

  protected backToDocuments(): void {
    this.selectedDocument.set(null);
    this.detailError.set('');
  }
}
