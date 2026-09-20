import { STATUSES } from "../config/constants.js";
import { taskRepository } from "./taskRepository.js";

export const taskService = {
    async getAll() {
        return taskRepository.getAll();
    },

    async add(data) {
        return taskRepository.create(data);
    },

    async edit(id, data) {
        return taskRepository.update(id, data);
    },

    async changeStatus(id, newStatus) {
        if (!STATUSES.includes(newStatus)) {
            throw new Error("Status inválido.");
        }
        return taskRepository.update(id, { status: newStatus });
    },

    async remove(id) {
        return taskRepository.remove(id);
    },

    // Devolve só as tarefas que passam nos filtros
    filterTasks(tasks, filters) {
        const search = filters.search.toLowerCase();
        const result = [];

        for (let i = 0; i < tasks.length; i++) {
            const task = tasks[i];

            const matchesSearch = task.nome.toLowerCase().includes(search);
            const matchesStatus = filters.status == "ALL" || task.status == filters.status;
            const matchesCategory = filters.category == "ALL" || task.categoria == filters.category;
            const matchesPriority = filters.priority == "ALL" || String(task.prioridade) == filters.priority;

            if (matchesSearch && matchesStatus && matchesCategory && matchesPriority) {
                result.push(task);
            }
        }

        return result;
    },

    // Conta o total e quantas tarefas tem em cada status
    countTasks(tasks) {
        const counts = { total: tasks.length, TODO: 0, DOING: 0, DONE: 0 };

        for (let i = 0; i < tasks.length; i++) {
            if (counts[tasks[i].status] !== undefined) {
                counts[tasks[i].status]++;
            }
        }

        return counts;
    }
};