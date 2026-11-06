import { Component, input } from '@angular/core';
import { DocumentStatus } from '../../core/models/document';

@Component({
  selector: 'app-status-badge',
  styleUrl: './status-badge.scss',
  template: `<span [class]="'badge ' + status().toLowerCase()">{{ status() }}</span>`,
})
export class StatusBadge {
  readonly status = input.required<DocumentStatus>();
}
