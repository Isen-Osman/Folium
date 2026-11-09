import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'Backend-от не е достапен. Провери дали работи на порт 8082.';
  if (error.status === 404) return 'Документот не е пронајден.';
  return fallback;
}
