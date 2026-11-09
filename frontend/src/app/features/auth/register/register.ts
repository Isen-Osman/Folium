import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/api-error-message';
import { StatusMessage } from '../../../shared/components/status-message';

@Component({
  selector: 'app-register',
  imports: [RouterLink, StatusMessage],
  styleUrl: '../auth.scss',
  templateUrl: './register.html',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly name = signal('');
  protected readonly surname = signal('');
  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected onName(event: Event): void {
    this.name.set((event.target as HTMLInputElement).value);
  }

  protected onSurname(event: Event): void {
    this.surname.set((event.target as HTMLInputElement).value);
  }

  protected onEmail(event: Event): void {
    this.email.set((event.target as HTMLInputElement).value);
  }

  protected onPassword(event: Event): void {
    this.password.set((event.target as HTMLInputElement).value);
  }

  protected onConfirmPassword(event: Event): void {
    this.confirmPassword.set((event.target as HTMLInputElement).value);
  }

  protected submit(event: Event): void {
    event.preventDefault();
    const email = this.email().trim();
    const name = this.name().trim();
    const surname = this.surname().trim();
    const password = this.password();

    if (!name || !surname || !email || !password) return;
    if (password.length < 8) {
      this.error.set('Лозинката мора да има најмалку 8 знаци.');
      return;
    }
    if (password !== this.confirmPassword()) {
      this.error.set('Лозинките не се совпаѓаат.');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.auth
      .register({ name, surname, email, password, confirmPassword: this.confirmPassword() })
      .pipe(switchMap(() => this.auth.login({ email, password })))
      .subscribe({
        next: () => this.router.navigate(['/documents']),
        error: (error: unknown) => {
          this.error.set(apiErrorMessage(error, 'Регистрацијата не успеа. Email-от можеби е зафатен.'));
          this.loading.set(false);
        },
      });
  }
}
