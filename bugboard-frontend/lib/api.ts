import {
    AuthResponse,
    CommentCreateRequest,
    CommentResponse,
    ErrorResponse,
    IssueCreateRequest,
    IssueFilterParams,
    IssueHistoryResponse,
    IssueResponse,
    LoginRequest,
    RegisterRequest,
    UserResponse,
} from "@/types/api";
import { clearSession, getToken } from "@/lib/auth";

const API_BASE_URL = (
    process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
).replace(/\/$/, "");

export class ApiError extends Error {
    status: number;

    constructor(message: string, status: number) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

interface RequestOptions extends RequestInit {
    auth?: boolean;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { auth = true, headers, ...rest } = options;
    const requestHeaders = new Headers(headers);

    if (rest.body && !requestHeaders.has("Content-Type")) {
        requestHeaders.set("Content-Type", "application/json");
    }

    if (auth) {
        const token = getToken();
        if (token) {
            requestHeaders.set("Authorization", `Bearer ${token}`);
        }
    }

    let response: Response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            ...rest,
            headers: requestHeaders,
        });
    } catch {
        throw new ApiError(
            "Impossibile contattare il server. Verifica che il backend sia avviato.",
            0
        );
    }

    const text = await response.text();
    let parsed: unknown = null;
    if (text) {
        try {
            parsed = JSON.parse(text);
        } catch {
            parsed = null;
        }
    }

    if (!response.ok) {
        if (response.status === 401 && auth) {
            clearSession();
        }
        const errBody = parsed as Partial<ErrorResponse> | null;
        const message =
            errBody?.message ||
            (response.status === 401
                ? "Sessione scaduta o non autorizzata."
                : response.status === 403
                    ? "Non disponi dei permessi necessari per questa operazione."
                    : `Errore del server (${response.status}).`);
        throw new ApiError(message, response.status);
    }

    return parsed as T;
}

export function login(payload: LoginRequest): Promise<AuthResponse> {
    return request<AuthResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(payload),
        auth: false,
    });
}

export function createUser(payload: RegisterRequest): Promise<UserResponse> {
    return request<UserResponse>("/api/users", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export function getIssues(params?: IssueFilterParams): Promise<IssueResponse[]> {
    const searchParams = new URLSearchParams();
    if (params?.type) searchParams.set("type", params.type);
    if (params?.status) searchParams.set("status", params.status);
    if (params?.priority) searchParams.set("priority", params.priority);
    if (params?.sortBy) searchParams.set("sortBy", params.sortBy);
    if (params?.direction) searchParams.set("direction", params.direction);

    const query = searchParams.toString();
    return request<IssueResponse[]>(`/api/issues${query ? `?${query}` : ""}`);
}

export function getIssue(id: number): Promise<IssueResponse> {
    return request<IssueResponse>(`/api/issues/${id}`);
}

export function createIssue(payload: IssueCreateRequest): Promise<IssueResponse> {
    return request<IssueResponse>("/api/issues", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export function markIssueAsDuplicate(
    id: number,
    originalId: number
): Promise<IssueResponse> {
    const searchParams = new URLSearchParams({ originalId: String(originalId) });
    return request<IssueResponse>(`/api/issues/${id}/duplicate?${searchParams.toString()}`, {
        method: "PATCH",
    });
}

export function setIssueDeadline(
    id: number,
    deadline: string
): Promise<IssueResponse> {
    const searchParams = new URLSearchParams({ deadline });
    return request<IssueResponse>(`/api/issues/${id}/deadline?${searchParams.toString()}`, {
        method: "PATCH",
    });
}

export function getComments(issueId: number): Promise<CommentResponse[]> {
    return request<CommentResponse[]>(`/api/issues/${issueId}/comments`);
}

export function addComment(
    issueId: number,
    payload: CommentCreateRequest
): Promise<CommentResponse> {
    return request<CommentResponse>(`/api/issues/${issueId}/comments`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export function getHistory(issueId: number): Promise<IssueHistoryResponse[]> {
    return request<IssueHistoryResponse[]>(`/api/issues/${issueId}/history`);
}