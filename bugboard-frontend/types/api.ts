export type Role = "USER" | "ADMIN";

export type IssueType = "QUESTION" | "BUG" | "DOCUMENTATION" | "FEATURE";

export type IssueStatus = "TODO" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";

export type IssuePriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface UserResponse {
    id: number;
    name: string;
    email: string;
    role: Role;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    role?: Role;
}

export interface AuthResponse {
    token: string;
    user: UserResponse;
}

export interface IssueCreateRequest {
    title: string;
    description: string;
    type: IssueType;
    priority?: IssuePriority | null;
    imageUrl?: string | null;
}

export interface IssueResponse {
    id: number;
    title: string;
    description: string;
    type: IssueType;
    status: IssueStatus;
    priority: IssuePriority | null;
    imageUrl: string | null;
    deadline: string | null;
    reporter: UserResponse;
    assignee: UserResponse | null;
    duplicateOfId: number | null;
    createdAt: string;
    updatedAt: string;
}

export interface CommentCreateRequest {
    text: string;
}

export interface CommentResponse {
    id: number;
    text: string;
    authorName: string;
    authorEmail: string;
    createdAt: string;
}

export interface IssueHistoryResponse {
    id: number;
    actorName: string;
    actorEmail: string;
    actionDescription: string;
    timestamp: string;
}

export interface ErrorResponse {
    status: number;
    message: string;
}

export interface IssueFilterParams {
    type?: IssueType;
    status?: IssueStatus;
    priority?: IssuePriority;
    sortBy?: string;
    direction?: "asc" | "desc";
}