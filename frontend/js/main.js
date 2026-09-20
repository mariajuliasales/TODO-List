import { validateTask } from "./models/task.js";
import { taskService } from "./services/taskService.js";
import { openModal, closeModal, setupModal } from "./ui/modal.js";
import { getFormData, fillForm, resetForm, setupForm } from "./ui/taskForm.js";
import { getFilters, setupFilters } from "./ui/filters.js";
import { showTasks, showCounters, setupTaskList } from "./ui/taskList.js";


let tasks = [];
let editingId = null;

function findTask(id) {
    for (let i = 0; i < tasks.length; i++) {
        if (tasks[i].id == id) {
            return tasks[i];
        }
    }
    return null;
}

function updateScreen() {
    const filteredTasks = taskService.filterTasks(tasks, getFilters());
    showTasks(filteredTasks, tasks.length);
    showCounters(taskService.countTasks(tasks));
}

async function loadTasks() {
    tasks = await taskService.getAll();
    updateScreen();
}

function cancelForm() {
    editingId = null;
    resetForm();
    closeModal();
}

async function handleSubmit() {
    const data = getFormData();
    const errors = validateTask(data);

    if (errors.length > 0) {
        alert(errors.join("\n"));
        return;
    }

    try {
        if (editingId == null) {
            await taskService.add(data);
        } else {
            await taskService.edit(editingId, data);
        }

        cancelForm();
        await loadTasks();
    } catch (error) {
        alert(error.message);
    }
}

function handleEdit(id) {
    const task = findTask(id);

    if (task == null) {
        return;
    }

    editingId = task.id;
    fillForm(task);
    openModal();
}

async function handleDelete(id) {
    const task = findTask(id);

    if (task == null) {
        return;
    }

    const confirmed = confirm('Tem certeza que deseja excluir a tarefa "' + task.nome + '"?');

    if (confirmed) {
        try {
            await taskService.remove(id);
            await loadTasks();
        } catch (error) {
            alert(error.message);
        }
    }
}

async function handleStatusChange(id, newStatus) {
    try {
        await taskService.changeStatus(id, newStatus);
        await loadTasks();
    } catch (error) {
        alert(error.message);
    }
}

setupModal(cancelForm);
setupForm(handleSubmit);
setupFilters(updateScreen);
setupTaskList(handleEdit, handleDelete, handleStatusChange);

loadTasks();
