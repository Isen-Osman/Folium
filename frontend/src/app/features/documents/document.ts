export type DocumentType = 'PDF' | 'IMAGE' | 'DOCX';

export interface Document {
  id: number;
  fileName: string;
  type: DocumentType;
  status: 'UPLOADED' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  uploadedAt: string;
}
