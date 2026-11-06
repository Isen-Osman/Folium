import { Component, input } from '@angular/core';

@Component({
  selector: 'app-status-message',
  styleUrl: './status-message.scss',
  template: '<p class="message" [class.error]="isError()">{{ message() }}</p>',
})
export class StatusMessage {
  readonly message = input.required<string>();
  readonly isError = input(false);
}
