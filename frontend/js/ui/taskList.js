import { STATUSES } from "../config/constants.js";
import { formatDate, escapeHtml } from "../utils/format.js";

// Desenha a lista de tarefas e os contadores na tela

const taskList = document.getElementById("task-list");
const emptyState = document.getElementById("empty-state");
const emptyMessage = document.getElementById("empty-message");

const totalCount = document.getElementById("total-count");
const todoCount = document.getElementById("todo-count");
const doingCount = document.getElementById("doing-count");
const doneCount = document.getElementById("done-count");

function getPriorityClass(priority) {
    if (priority >= 4) {
        return "priority-high";
    }
    if (priority == 3) {
        return "priority-medium";
    }
    return "priority-low";
}

function buildStatusOptions(currentStatus) {
    let html = "";

    for (let i = 0; i < STATUSES.length; i++) {
        let selected = "";
        if (STATUSES[i] == currentStatus) {
            selected = "selected";
        }
        html += `<option value="${STATUSES[i]}" ${selected}>${STATUSES[i]}</option>`;
    }

    return html;
}

function buildCard(task) {
    return `
        <article class="task-card status-${task.status.toLowerCase()}" data-id="${task.id}">
            <h3 class="task-title">${escapeHtml(task.nome)}</h3>
            <p class="task-description">${escapeHtml(task.descricao)}</p>

            <div class="task-meta">
                <span class="badge status-badge status-${task.status.toLowerCase()}">Status: ${task.status}</span>
                <span class="badge">Categoria: ${escapeHtml(task.categoria)}</span>
                <span class="badge">Término: ${formatDate(task.dataTermino)}</span>
                <span class="badge priority-badge ${getPriorityClass(task.prioridade)}">Prioridade ${task.prioridade}</span>
            </div>

            <div class="task-actions">
                <label class="status-change">
                    Mudar status
                    <select class="status-select">${buildStatusOptions(task.status)}</select>
                </label>
                <button class="button button-secondary" type="button" data-action="edit">Editar</button>
                <button class="button button-danger" type="button" data-action="delete">Excluir</button>
            </div>
        </article>
    `;
}

export function showTasks(tasks, allTasksCount) {
    let html = "";

    for (let i = 0; i < tasks.length; i++) {
        html += buildCard(tasks[i]);
    }

    taskList.innerHTML = html;

    if (tasks.length == 0) {
        emptyState.classList.remove("hidden");

        if (allTasksCount == 0) {
            emptyMessage.textContent = "Nenhuma tarefa cadastrada";
        } else {
            emptyMessage.textContent = "Nenhum resultado encontrado";
        }
    } else {
        emptyState.classList.add("hidden");
    }
}

export function showCounters(counts) {
    totalCount.textContent = counts.total;
    todoCount.textContent = counts.TODO;
    doingCount.textContent = counts.DOING;
    doneCount.textContent = counts.DONE;
}

export function setupTaskList(onEdit, onDelete, onStatusChange) {
    taskList.addEventListener("click", function (event) {
        const button = event.target.closest("[data-action]");

        if (button == null) {
            return;
        }

        const id = button.closest(".task-card").dataset.id;

        if (button.dataset.action == "edit") {
            onEdit(id);
        } else if (button.dataset.action == "delete") {
            onDelete(id);
        }
    });

    taskList.addEventListener("change", function (event) {
        if (event.target.classList.contains("status-select")) {
            const id = event.target.closest(".task-card").dataset.id;
            onStatusChange(id, event.target.value);
        }
    });
}
