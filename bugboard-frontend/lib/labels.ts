import { IssuePriority, IssueStatus, IssueType } from "@/types/api";

export const ISSUE_TYPE_LABELS: Record<IssueType, string> = {
    BUG: "Bug",
    FEATURE: "Feature",
    QUESTION: "Question",
    DOCUMENTATION: "Documentation",
};

export const ISSUE_STATUS_LABELS: Record<IssueStatus, string> = {
    TODO: "Todo",
    IN_PROGRESS: "In Progress",
    RESOLVED: "Resolved",
    CLOSED: "Closed",
};

export const ISSUE_STATUS_STYLES: Record<IssueStatus, string> = {
    TODO: "bg-blue-100 text-blue-700 border-blue-200",
    IN_PROGRESS: "bg-amber-100 text-amber-800 border-amber-200",
    RESOLVED: "bg-emerald-100 text-emerald-700 border-emerald-200",
    CLOSED: "bg-gray-200 text-gray-700 border-gray-300",
};

export const ISSUE_PRIORITY_LABELS: Record<IssuePriority, string> = {
    LOW: "Bassa",
    MEDIUM: "Media",
    HIGH: "Alta",
    CRITICAL: "Critica",
};

export const ISSUE_PRIORITY_STYLES: Record<IssuePriority, string> = {
    LOW: "bg-green-100 text-green-700",
    MEDIUM: "bg-yellow-100 text-yellow-700",
    HIGH: "bg-orange-100 text-orange-700",
    CRITICAL: "bg-red-100 text-red-700",
};

export function formatDateTime(isoString?: string | null): string {
    if (!isoString) return "";
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return "";
    return date.toLocaleString("it-IT", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export function formatLocalDate(dateStr?: string | null): string {
    if (!dateStr) return "";
    const parts = dateStr.split("-");
    if (parts.length !== 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}