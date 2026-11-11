import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  styleUrl: './app-header.scss',
  templateUrl: './app-header.html',
})
export class AppHeader {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly menuOpen = signal(false);
  protected readonly initials = computed(() => {
    const user = this.auth.user();
    const first = (user?.name ?? '').trim().charAt(0);
    const last = (user?.surname ?? '').trim().charAt(0);
    const letters = `${first}${last}`.trim();
    return (letters || (user?.email ?? 'П').charAt(0)).toUpperCase();
  });

  protected toggleMenu(event: Event): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    const target = event.target as Element | null;
    if (target === null || target.closest('.user-menu') === null) {
      this.menuOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    this.menuOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
    this.menuOpen.set(false);
    this.router.navigate(['/login']);
  }
}
