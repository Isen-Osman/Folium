import { Component, inject, output, signal } from '@angular/core';
import { DocumentsService } from '../../../core/services/documents.service';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';

interface FailedFile {
  name: string;
  reason: string;
}

@Component({
  selector: 'app-document-upload',
  imports: [StatusMessage],
  styleUrl: './document-upload.scss',
  templateUrl: './document-upload.html',
})
export class DocumentUpload {
  private readonly documentsService = inject(DocumentsService);

  readonly uploaded = output<number[]>();

  protected readonly selectedFiles = signal<File[]>([]);
  protected readonly uploading = signal(false);
  protected readonly progress = signal('');
  protected readonly uploadError = signal('');
  protected readonly uploadSuccess = signal('');
  protected readonly failedFiles = signal<FailedFile[]>([]);

  protected onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles.set(Array.from(input.files ?? []));
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.selectedFiles.set(Array.from(event.dataTransfer?.files ?? []));
  }

  protected upload(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const files = this.selectedFiles();
    if (files.length === 0) return;

    this.uploading.set(true);
    this.uploadError.set('');
    this.uploadSuccess.set('');
    this.failedFiles.set([]);

    const createdIds: number[] = [];
    const failed: FailedFile[] = [];
    let index = 0;

    const next = (): void => {
      if (index >= files.length) {
        this.finish(form, createdIds, failed);
        return;
      }

      const file = files[index];
      index += 1;
      this.progress.set(`Прикачувам ${index}/${files.length}: ${file.name}`);

      this.documentsService.createDocument(file).subscribe({
        next: (document) => {
          createdIds.push(document.id);
          next();
        },
        error: (error: unknown) => {
          failed.push({
            name: file.name,
            reason: apiErrorMessage(error, 'Не можам да го прикачам документот.'),
          });
          next();
        },
      });
    };

    next();
  }

  private finish(form: HTMLFormElement, createdIds: number[], failed: FailedFile[]): void {
    this.failedFiles.set(failed);
    this.selectedFiles.set([]);
    form.reset();

    if (createdIds.length === 0) {
      this.uploading.set(false);
      this.progress.set('');
      this.uploadError.set('Ниту еден документ не е прикачен.');
      return;
    }

    this.progress.set(`Започнувам обработка на ${createdIds.length} документ(и)...`);

    this.documentsService.processBatch(createdIds).subscribe({
      next: () => {
        this.uploading.set(false);
        this.progress.set('');
        this.uploadSuccess.set(
          `Прикачени ${createdIds.length} документ(и). Обработката е во тек.`,
        );
        this.uploaded.emit(createdIds);
      },
      error: (error: unknown) => {
        this.uploading.set(false);
        this.progress.set('');
        this.uploadError.set(
          apiErrorMessage(error, 'Документите се прикачени, но обработката не започна.'),
        );
        this.uploaded.emit([]);
      },
    });
  }
}
