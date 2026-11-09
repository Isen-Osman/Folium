import { Component, inject, output, signal } from '@angular/core';
import { DocumentsService } from '../../../core/services/documents.service';
import { DocumentType } from '../../../core/models/document';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';

@Component({
  selector: 'app-document-upload',
  imports: [StatusMessage],
  styleUrl: './document-upload.scss',
  templateUrl: './document-upload.html',
})
export class DocumentUpload {
  private readonly documentsService = inject(DocumentsService);

  readonly uploaded = output<void>();

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly selectedType = signal<DocumentType>('PDF');
  protected readonly uploading = signal(false);
  protected readonly uploadError = signal('');
  protected readonly uploadSuccess = signal('');

  protected onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  protected onType(event: Event): void {
    this.selectedType.set((event.target as HTMLSelectElement).value as DocumentType);
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0] ?? null;
    if (file) {
      this.selectedFile.set(file);
    }
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
        this.uploaded.emit();
      },
      error: (error: unknown) => {
        this.uploadError.set(apiErrorMessage(error, 'Не можам да го прикачам документот.'));
        this.uploading.set(false);
      },
    });
  }
}
