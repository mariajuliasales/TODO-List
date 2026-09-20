// 2026-09-19 em 19/09/2026
export function formatDate(dateString) {
    if (!dateString) {
        return "Não informada";
    }

    const parts = dateString.split("-");
    return parts[2] + "/" + parts[1] + "/" + parts[0];
}

// Troca caracteres especiais por texto seguro
export function escapeHtml(text) {
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}
