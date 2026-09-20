import { STORAGE_KEY } from "../config/constants.js";

function readTasks() {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);

        if (saved == null) {
            return [];
        }

        const list = JSON.parse(saved);

        if (!Array.isArray(list)) {
            return [];
        }

        // ignora itens estragados
        const validTasks = [];
        for (let i = 0; i < list.length; i++) {
            const item = list[i];
            if (item && typeof item.nome == "string" && typeof item.status == "string") {
                validTasks.push(item);
            }
        }
        return validTasks;
    } catch (error) {
        return [];
    }
}

function writeTasks(tasks) {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
    } catch (error) {
        throw new Error("Não foi possível salvar as tarefas.");
    }
}

export const taskRepository = {
    async getAll() {
        return readTasks();
    },

    async create(task) {
        const tasks = readTasks();

        const newTask = {
            id: Date.now(),
            nome: task.nome,
            descricao: task.descricao,
            dataTermino: task.dataTermino,
            categoria: task.categoria,
            status: task.status,
            prioridade: task.prioridade
        };

        tasks.push(newTask);
        writeTasks(tasks);
        return newTask;
    },

    async update(id, data) {
        const tasks = readTasks();

        for (let i = 0; i < tasks.length; i++) {
            if (tasks[i].id == id) {
                Object.assign(tasks[i], data);
                writeTasks(tasks);
                return tasks[i];
            }
        }

        throw new Error("Tarefa não encontrada.");
    },

    async remove(id) {
        const tasks = readTasks();
        const remaining = [];

        for (let i = 0; i < tasks.length; i++) {
            if (tasks[i].id != id) {
                remaining.push(tasks[i]);
            }
        }

        writeTasks(remaining);
    }
};