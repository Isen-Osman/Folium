import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../../core/api-error-message';
import { RegisterRow } from '../../../core/models/document';
import { DocumentsService } from '../../../core/services/documents.service';
import { StatusBadge } from '../../../shared/components/status-badge';
import { StatusMessage } from '../../../shared/components/status-message';

type SortField = 'date' | 'amount';
type SortDirection = 'asc' | 'desc';

@Component({
  imports: [RouterLink, StatusBadge, StatusMessage],
  selector: 'app-document-register',
  styleUrl: './document-register.scss',
  templateUrl: './document-register.html',
})
export class DocumentRegister implements OnInit {
  private readonly documentsService = inject(DocumentsService);
  private readonly amountFormat = new Intl.NumberFormat('mk-MK', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  protected readonly rows = signal<RegisterRow[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');

  protected readonly search = signal('');
  protected readonly issuerFilter = signal('');
  protected readonly dateFrom = signal('');
  protected readonly dateTo = signal('');

  protected readonly sortField = signal<SortField>('date');
  protected readonly sortDirection = signal<SortDirection>('desc');

  protected readonly issuers = computed(() => {
    const values = this.rows()
      .map((row) => row.issuer)
      .filter((issuer): issuer is string => issuer !== null);
    return [...new Set(values)].sort((a, b) => a.localeCompare(b));
  });

  protected readonly hasFilters = computed(
    () =>
      this.search().trim() !== '' ||
      this.issuerFilter() !== '' ||
      this.dateFrom() !== '' ||
      this.dateTo() !== '',
  );

  protected readonly filteredRows = computed(() => {
    const term = this.search().trim().toLowerCase();
    const issuer = this.issuerFilter();
    const from = this.dateFrom();
    const to = this.dateTo();

    return this.rows().filter((row) => {
      const matchesSearch =
        term.length === 0 ||
        (row.invoiceNumber ?? '').toLowerCase().includes(term) ||
        (row.issuer ?? '').toLowerCase().includes(term) ||
        row.fileName.toLowerCase().includes(term);
      if (!matchesSearch) {
        return false;
      }

      if (issuer !== '' && row.issuer !== issuer) {
        return false;
      }

      if (from === '' && to === '') {
        return true;
      }
      if (row.date === null) {
        return false;
      }
      return (from === '' || row.date >= from) && (to === '' || row.date <= to);
    });
  });

  protected readonly sortedRows = computed(() => {
    const field = this.sortField();
    const direction = this.sortDirection() === 'asc' ? 1 : -1;

    return [...this.filteredRows()].sort((a, b) => {
      if (field === 'amount') {
        if (a.amount === null && b.amount === null) {
          return 0;
        }
        if (a.amount === null) {
          return 1;
        }
        if (b.amount === null) {
          return -1;
        }
        return (a.amount - b.amount) * direction;
      }

      if (a.date === null && b.date === null) {
        return 0;
      }
      if (a.date === null) {
        return 1;
      }
      if (b.date === null) {
        return -1;
      }
      return a.date.localeCompare(b.date) * direction;
    });
  });

  protected readonly totalAmount = computed(() =>
    this.filteredRows().reduce((sum, row) => sum + (row.amount ?? 0), 0),
  );

  protected readonly needsReviewCount = computed(
    () => this.filteredRows().filter((row) => row.needsReview).length,
  );

  ngOnInit(): void {
    this.loadRegister();
  }

  protected loadRegister(): void {
    this.loading.set(true);
    this.error.set('');

    this.documentsService.getRegister().subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(apiErrorMessage(error, 'Не можам да го вчитам регистарот.'));
        this.loading.set(false);
      },
    });
  }

  protected onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected onIssuer(event: Event): void {
    this.issuerFilter.set((event.target as HTMLSelectElement).value);
  }

  protected onDateFrom(event: Event): void {
    this.dateFrom.set((event.target as HTMLInputElement).value);
  }

  protected onDateTo(event: Event): void {
    this.dateTo.set((event.target as HTMLInputElement).value);
  }

  protected clearFilters(): void {
    this.search.set('');
    this.issuerFilter.set('');
    this.dateFrom.set('');
    this.dateTo.set('');
  }

  protected toggleSort(field: SortField): void {
    if (this.sortField() === field) {
      this.sortDirection.update((direction) => (direction === 'asc' ? 'desc' : 'asc'));
      return;
    }
    this.sortField.set(field);
    this.sortDirection.set('desc');
  }

  protected sortIndicator(field: SortField): string {
    if (this.sortField() !== field) {
      return '';
    }
    return this.sortDirection() === 'asc' ? '↑' : '↓';
  }

  protected formatDate(value: string | null): string {
    if (value === null) {
      return '—';
    }
    const [year, month, day] = value.split('-');
    return `${day}.${month}.${year}`;
  }

  protected formatAmount(value: number | null): string {
    return value === null ? '—' : this.amountFormat.format(value);
  }
}
