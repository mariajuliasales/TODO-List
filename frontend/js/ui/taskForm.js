const form = document.getElementById("task-form");
const formTitle = document.getElementById("form-title");
const submitButton = document.getElementById("submit-button");

const nameInput = document.getElementById("task-name");
const descriptionInput = document.getElementById("task-description");
const dateInput = document.getElementById("task-date");
const categorySelect = document.getElementById("task-category");
const statusSelect = document.getElementById("task-status");
const prioritySelect = document.getElementById("task-priority");

export function getFormData() {
    return {
        nome: nameInput.value.trim(),
        descricao: descriptionInput.value.trim(),
        dataTermino: dateInput.value,
        categoria: categorySelect.value,
        status: statusSelect.value,
        prioridade: Number(prioritySelect.value)
    };
}

export function fillForm(task) {
    nameInput.value = task.nome;
    descriptionInput.value = task.descricao;
    dateInput.value = task.dataTermino;
    categorySelect.value = task.categoria;
    statusSelect.value = task.status;
    prioritySelect.value = task.prioridade;

    formTitle.textContent = "Editar tarefa";
    submitButton.textContent = "Salvar alterações";
}

export function resetForm() {
    form.reset();

    formTitle.textContent = "Criar tarefa";
    submitButton.textContent = "Adicionar tarefa";
}

export function setupForm(onSubmit) {
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        onSubmit();
    });
}
