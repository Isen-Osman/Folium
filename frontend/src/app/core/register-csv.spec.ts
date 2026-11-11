import { describe, expect, it } from 'vitest';
import { RegisterRow } from './models/document';
import { registerRowsToCsv } from './register-csv';

const HEADER = 'Број на фактура;Датум;Издавач;Износ;Статус;Документ;За проверка';

function row(overrides: Partial<RegisterRow> = {}): RegisterRow {
  return {
    documentId: 1,
    fileName: 'invoice.pdf',
    status: 'COMPLETED',
    invoiceNumber: '2024-17',
    date: '2026-03-12',
    issuer: 'Acme',
    amount: 1250,
    needsReview: false,
    ...overrides,
  };
}

describe('registerRowsToCsv', () => {
  it('starts with a BOM and the header', () => {
    const csv = registerRowsToCsv([]);

    expect(csv.startsWith('\uFEFF')).toBe(true);
    expect(csv).toBe(`\uFEFF${HEADER}\r\n`);
  });

  it('formats date, amount, status and review flag', () => {
    const csv = registerRowsToCsv([row()]);

    expect(csv).toContain('2024-17;12.03.2026;Acme;1250,00;Обработена;invoice.pdf;не');
  });

  it('leaves empty cells for missing values', () => {
    const csv = registerRowsToCsv([
      row({
        invoiceNumber: null,
        date: null,
        issuer: null,
        amount: null,
        status: 'UPLOADED',
        needsReview: true,
      }),
    ]);

    expect(csv).toContain(';;;;Прикачена;invoice.pdf;да');
  });

  it('quotes cells that contain separators or quotes', () => {
    const csv = registerRowsToCsv([row({ issuer: 'Acme; "North"' })]);

    expect(csv).toContain('"Acme; ""North"""');
  });

  it('prevents spreadsheet formula injection', () => {
    const csv = registerRowsToCsv([row({ invoiceNumber: '=SUM(A1)' })]);

    expect(csv).toContain("'=SUM(A1);12.03.2026");
  });

  it('keeps negative amounts numeric', () => {
    const csv = registerRowsToCsv([row({ amount: -500 })]);

    expect(csv).toContain('-500,00;Обработена');
  });
});
