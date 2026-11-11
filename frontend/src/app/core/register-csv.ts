import { DocumentStatus, RegisterRow } from './models/document';

const HEADER = [
  'Број на фактура',
  'Датум',
  'Издавач',
  'Износ',
  'Статус',
  'Документ',
  'За проверка',
];

const STATUS_LABELS: Record<DocumentStatus, string> = {
  UPLOADED: 'Прикачена',
  PENDING: 'Во обработка',
  PROCESSING: 'Во обработка',
  COMPLETED: 'Обработена',
  FAILED: 'Неуспешна',
};

export function registerRowsToCsv(rows: RegisterRow[]): string {
  const lines = [HEADER.map(escapeCell).join(';')];

  for (const row of rows) {
    lines.push(
      [
        row.invoiceNumber ?? '',
        formatDate(row.date),
        row.issuer ?? '',
        formatAmount(row.amount),
        STATUS_LABELS[row.status],
        row.fileName,
        row.needsReview ? 'да' : 'не',
      ]
        .map(escapeCell)
        .join(';'),
    );
  }

  return '\uFEFF' + lines.join('\r\n') + '\r\n';
}

function formatDate(value: string | null): string {
  if (value === null) {
    return '';
  }
  const [year, month, day] = value.split('-');
  return `${day}.${month}.${year}`;
}

function formatAmount(value: number | null): string {
  return value === null ? '' : value.toFixed(2).replace('.', ',');
}

function escapeCell(value: string): string {
  let text = value;
  if (/^[=+@]/.test(text) || /^-(?!\d)/.test(text)) {
    text = "'" + text;
  }
  if (/[;"\r\n]/.test(text)) {
    return `"${text.replace(/"/g, '""')}"`;
  }
  return text;
}
