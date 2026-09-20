import { STATUSES, CATEGORIES } from "../config/constants.js";

// Formato de uma tarefa:
// {
//   id: número,
//   nome: texto,
//   descricao: texto,
//   dataTermino: "AAAA-MM-DD",
//   categoria: uma das CATEGORIES,
//   status: "TODO" | "DOING" | "DONE",
//   prioridade: número de 1 a 5
// }

export function validateTask(task) {
    const errors = [];

    if (task.nome == "") {
        errors.push("Informe o nome da tarefa.");
    }

    if (task.descricao == "") {
        errors.push("Informe a descrição da tarefa.");
    }

    if (task.dataTermino == "") {
        errors.push("Informe a data de término.");
    }

    if (!CATEGORIES.includes(task.categoria)) {
        errors.push("Selecione uma categoria.");
    }

    if (!STATUSES.includes(task.status)) {
        errors.push("Selecione um status válido.");
    }

    if (task.prioridade < 1 || task.prioridade > 5) {
        errors.push("Selecione a prioridade.");
    }

    return errors;
}
