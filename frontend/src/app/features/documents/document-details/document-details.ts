import { DatePipe } from '@angular/common';
import {
  Component,
  DestroyRef,
  OnDestroy,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { DocumentsService } from '../../../core/services/documents.service';
import { Document, ExtractedData, ExtractedField } from '../../../core/models/document';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';
import { StatusBadge } from '../../../shared/components/status-badge';

@Component({
  selector: 'app-document-details',
  imports: [DatePipe, StatusMessage, StatusBadge],
  styleUrl: './document-details.scss',
  templateUrl: './document-details.html',
})
export class DocumentDetails implements OnInit, OnDestroy {
  private readonly documentsService = inject(DocumentsService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly destroyRef = inject(DestroyRef);

  readonly documentId = input.required<number>();
  readonly back = output<void>();

  protected readonly document = signal<Document | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly extractedData = signal<ExtractedData | null>(null);
  protected readonly extractedLoading = signal(false);
  protected readonly extractedError = signal('');
  protected readonly editableFields = signal<ExtractedField[]>([]);
  protected readonly savingFields = signal(false);
  protected readonly fieldsError = signal('');
  protected readonly fieldsSaved = signal(false);
  protected readonly processing = signal(false);
  protected readonly processingError = signal('');
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal('');
  protected readonly editingName = signal(false);
  protected readonly name = signal('');
  protected readonly renaming = signal(false);
  protected readonly renameError = signal('');
  protected readonly previewLoading = signal(false);
  protected readonly previewError = signal('');
  protected readonly pdfSource = signal<SafeResourceUrl | null>(null);
  protected readonly imageSource = signal<SafeUrl | null>(null);
  protected readonly fileDownloading = signal(false);
  private previewObjectUrl: string | null = null;

  protected readonly missingFieldsCount = computed(
    () => this.editableFields().filter((field) => !field.value.trim()).length,
  );

  protected readonly hasFieldChanges = computed(() => {
    const data = this.extractedData();
    if (!data) {
      return false;
    }
    const current = this.editableFields();
    if (current.length !== data.fields.length) {
      return true;
    }
    return current.some(
      (field, index) =>
        field.name !== data.fields[index].name || field.value !== data.fields[index].value,
    );
  });

  ngOnInit(): void {
    this.loadDocument();
  }

  ngOnDestroy(): void {
    this.releasePreview();
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

  protected startRename(): void {
    const document = this.document();
    if (!document) return;

    this.name.set(document.fileName);
    this.renameError.set('');
    this.editingName.set(true);
  }

  protected onName(event: Event): void {
    this.name.set((event.target as HTMLInputElement).value);
  }

  protected cancelRename(): void {
    this.editingName.set(false);
    this.renameError.set('');
  }

  protected saveName(event: Event): void {
    event.preventDefault();
    const document = this.document();
    const fileName = this.name().trim();
    if (!document || !fileName) return;

    this.renaming.set(true);
    this.renameError.set('');

    this.documentsService.renameDocument(document.id, fileName).subscribe({
      next: (updated) => {
        this.document.set(updated);
        this.renaming.set(false);
        this.editingName.set(false);
      },
      error: (error: unknown) => {
        this.renameError.set(apiErrorMessage(error, 'Не можам да го преименувам документот.'));
        this.renaming.set(false);
      },
    });
  }

  protected onFieldValue(event: Event, index: number): void {
    const value = (event.target as HTMLInputElement).value;
    this.fieldsSaved.set(false);
    this.editableFields.update((fields) =>
      fields.map((field, i) => (i === index ? { ...field, value } : field)),
    );
  }

  protected saveFields(): void {
    const document = this.document();
    if (!document || !this.hasFieldChanges()) return;

    this.savingFields.set(true);
    this.fieldsError.set('');
    this.fieldsSaved.set(false);

    this.documentsService.updateFields(document.id, this.editableFields()).subscribe({
      next: (data) => {
        this.extractedData.set(data);
        this.editableFields.set(data.fields.map((field) => ({ ...field })));
        this.savingFields.set(false);
        this.fieldsSaved.set(true);
      },
      error: (error: unknown) => {
        this.fieldsError.set(apiErrorMessage(error, 'Не можам да ги зачувам полињата.'));
        this.savingFields.set(false);
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

  protected downloadFile(): void {
    const document = this.document();
    if (!document) return;

    if (this.previewObjectUrl) {
      this.triggerDownload(this.previewObjectUrl, document.fileName);
      return;
    }

    this.fileDownloading.set(true);
    this.previewError.set('');

    this.documentsService
      .getDocumentFile(document.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          this.triggerDownload(url, document.fileName);
          URL.revokeObjectURL(url);
          this.fileDownloading.set(false);
        },
        error: (error: unknown) => {
          this.previewError.set(apiErrorMessage(error, 'Не можам да го преземам фајлот.'));
          this.fileDownloading.set(false);
        },
      });
  }

  private triggerDownload(url: string, fileName: string): void {
    const link = window.document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
  }

  private loadPreview(): void {
    const document = this.document();
    if (!document || document.type === 'DOCX') return;

    this.releasePreview();
    this.previewLoading.set(true);
    this.previewError.set('');

    this.documentsService
      .getDocumentFile(document.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          this.previewObjectUrl = url;
          if (document.type === 'PDF') {
            this.pdfSource.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
          } else {
            this.imageSource.set(this.sanitizer.bypassSecurityTrustUrl(url));
          }
          this.previewLoading.set(false);
        },
        error: (error: unknown) => {
          this.previewError.set(apiErrorMessage(error, 'Не можам да го вчитам фајлот.'));
          this.previewLoading.set(false);
        },
      });
  }

  private releasePreview(): void {
    if (this.previewObjectUrl) {
      URL.revokeObjectURL(this.previewObjectUrl);
      this.previewObjectUrl = null;
    }
    this.pdfSource.set(null);
    this.imageSource.set(null);
  }

  private loadDocument(): void {
    this.loading.set(true);
    this.error.set('');

    this.documentsService.getDocument(this.documentId()).subscribe({
      next: (document) => {
        this.document.set(document);
        this.loading.set(false);
        this.loadPreview();
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
        this.editableFields.set(data.fields.map((field) => ({ ...field })));
        this.extractedLoading.set(false);
      },
      error: (error: unknown) => {
        this.extractedError.set(apiErrorMessage(error, 'Не можам да ги вчитам извлечените податоци.'));
        this.extractedLoading.set(false);
      },
    });
  }
}
