import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';

@Component({
  selector: 'app-login',
  imports: [RouterLink, StatusMessage],
  styleUrl: '../auth.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected onEmail(event: Event): void {
    this.email.set((event.target as HTMLInputElement).value);
  }

  protected onPassword(event: Event): void {
    this.password.set((event.target as HTMLInputElement).value);
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (!this.email().trim() || !this.password()) return;

    this.loading.set(true);
    this.error.set('');

    this.auth.login({ email: this.email().trim(), password: this.password() }).subscribe({
      next: () => this.router.navigate(['/documents']),
      error: (error: unknown) => {
        this.error.set(apiErrorMessage(error, 'Погрешно email или лозинка.'));
        this.loading.set(false);
      },
    });
  }
}
