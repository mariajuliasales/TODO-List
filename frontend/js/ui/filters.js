const searchInput = document.getElementById("search-input");
const statusFilter = document.getElementById("status-filter");
const categoryFilter = document.getElementById("category-filter");
const priorityFilter = document.getElementById("priority-filter");
const clearButton = document.getElementById("clear-filters-button");

export function getFilters() {
    return {
        search: searchInput.value.trim(),
        status: statusFilter.value,
        category: categoryFilter.value,
        priority: priorityFilter.value
    };
}

export function setupFilters(onChange) {
    searchInput.addEventListener("input", onChange);
    statusFilter.addEventListener("change", onChange);
    categoryFilter.addEventListener("change", onChange);
    priorityFilter.addEventListener("change", onChange);

    clearButton.addEventListener("click", function () {
        searchInput.value = "";
        statusFilter.value = "ALL";
        categoryFilter.value = "ALL";
        priorityFilter.value = "ALL";
        onChange();
    });
}