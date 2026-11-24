import { AuthResponse, UserResponse } from "@/types/api";

const TOKEN_KEY = "bugboard_token";
const USER_KEY = "bugboard_user";

export function saveSession(auth: AuthResponse): void {
    if (typeof window === "undefined") return;
    localStorage.setItem(TOKEN_KEY, auth.token);
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
}

export function getToken(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser(): UserResponse | null {
    if (typeof window === "undefined") return null;
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw) as UserResponse;
    } catch {
        return null;
    }
}

export function clearSession(): void {
    if (typeof window === "undefined") return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

export function isAdminUser(user: UserResponse | null): boolean {
    return user?.role === "ADMIN";
}