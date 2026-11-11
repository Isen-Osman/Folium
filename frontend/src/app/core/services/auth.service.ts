import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AuthUser,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from '../models/auth';

const TOKEN_KEY = 'folium.accessToken';
const USER_KEY = 'folium.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly currentUser = signal<AuthUser | null>(readStoredUser());

  readonly isAuthenticated = computed(() => this.token() !== null);
  readonly user = this.currentUser.asReadonly();
  readonly displayName = computed(() => {
    const user = this.currentUser();
    if (user === null) {
      return '';
    }
    return `${user.name ?? ''} ${user.surname ?? ''}`.trim() || user.email;
  });

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', request).pipe(
      tap((response) => {
        this.setToken(response.accessToken);
        this.setUser({
          id: response.id,
          name: response.name,
          surname: response.surname,
          email: response.email,
        });
      }),
    );
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>('/api/auth/register', request);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return this.token();
  }

  private setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.token.set(token);
  }

  private setUser(user: AuthUser): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }
}

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (raw === null) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}
