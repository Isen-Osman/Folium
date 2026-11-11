export type DocumentType = 'PDF' | 'IMAGE' | 'DOCX';

export type DocumentStatus = 'UPLOADED' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface Document {
  id: number;
  fileName: string;
  type: DocumentType;
  status: DocumentStatus;
  uploadedAt: string;
}

export interface BatchProcessResponse {
  queued: number;
}

export interface ProcessingJob {
  id: number;
  documentId: number;
  status: DocumentStatus;
  startedAt: string;
  finishedAt: string | null;
  errorMessage: string | null;
}

export interface ExtractedField {
  id: number;
  name: string;
  value: string;
}

export interface ExtractedData {
  id: number;
  documentId: number;
  rawText: string;
  createdAt: string;
  fields: ExtractedField[];
}

export interface RegisterRow {
  documentId: number;
  fileName: string;
  status: DocumentStatus;
  invoiceNumber: string | null;
  date: string | null;
  issuer: string | null;
  amount: number | null;
  needsReview: boolean;
}
