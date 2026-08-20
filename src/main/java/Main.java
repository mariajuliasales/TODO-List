import model.StatusEnum;
import model.Tarefa;
import service.TarefaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final TarefaService service = new TarefaService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public static void main(String[] args) {

        int opc = -2;

        while (opc != 0) {
            exibirMenu();

            opc = Integer.parseInt(scanner.nextLine());
            switch (opc) {
                case 1 -> cadastrarTarefa();
                case 2 -> listarTarefas();
                case 3 -> atualizarTarefa();
                case 4 -> atualizarStatusTarefa();
                case 5 -> deletarTarefa();
                case 0 -> System.out.println("Saindo do sistema...");
                default -> System.out.println("Opção inválida!");
            }
        }

    }

    private static void exibirMenu() {
        System.out.println("\n--- TODO LIST ---");
        System.out.println("1. Cadastrar Tarefa");
        System.out.println("2. Listar Tarefas");
        System.out.println("3. Atualizar Tarefa");
        System.out.println("4. Atualizar Status da Tarefa");
        System.out.println("5. Deletar Tarefa");
        System.out.println("0. Sair do programa");
        System.out.print("Escolha uma opção: ");
    }

    private static void cadastrarTarefa() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();
        System.out.print("Data Término (dd/MM/yyyy): ");
        LocalDate dataTermino = LocalDate.parse(scanner.nextLine(), formatter);
        System.out.print("Categoria: ");
        String categoria = scanner.nextLine();
        System.out.print("Prioridade (1 a 5): ");
        int prioridade = Integer.parseInt(scanner.nextLine());

        service.adicionar(nome, descricao, dataTermino, prioridade, categoria);
        System.out.println("Tarefa cadastrada com sucesso!");
    }

    private static void listarTarefas(){
        System.out.println("\n--- LISTAR TAREFAS ---");
        System.out.println("1. Listar todas as tarefas");
        System.out.println("2. Listar por categoria");
        System.out.println("3. Listar por prioridade");
        System.out.println("4. Listar por status");
        System.out.print("Escolha uma opção: ");
        int opcao = Integer.parseInt(scanner.nextLine());

        switch (opcao){
            case 1 -> { List<Tarefa> tarefas = service.listar();
                if (tarefas.isEmpty()) {
                    System.out.println("Nenhuma tarefa cadastrada!");
                } else {
                    System.out.println("\n--- LISTA DE TAREFAS ---");
                    tarefas.forEach(t -> System.out.println(t));
                }
            }
            case 2 -> {
                System.out.print("Digite a categoria: ");
                String categoria = scanner.nextLine();
                System.out.println("Listando tarefas da categoria: " + categoria);
                service.listarPorCategoria(categoria).forEach(t -> System.out.println(t));
            }
            case 3 -> {
                System.out.print("Digite a prioridade (1 a 5): ");
                int prioridade = Integer.parseInt(scanner.nextLine());
                System.out.println("Listando tarefas com prioridade: " + prioridade);
                service.listarPorPrioridade(prioridade).forEach(t -> System.out.println(t));
            }
            case 4 -> {
                System.out.print("Digite o status (TODO, DOING, DONE): ");
                StatusEnum status = StatusEnum.valueOf(scanner.nextLine());
                System.out.println("Listando tarefas com status: " + status);
                service.listarPorStatus(status).forEach(t -> System.out.println(t));
            }
            default -> System.out.println("Opção inválida!");
        }

    }

    private static void atualizarTarefa() {
        System.out.print("Digite o ID da tarefa que deseja atualizar: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Novo nome: ");
        String novoNome = scanner.nextLine();
        System.out.print("Nova descrição: ");
        String novaDescricao = scanner.nextLine();
        System.out.print("Nova data de término (dd/MM/yyyy): ");
        LocalDate novaDataTermino = LocalDate.parse(scanner.nextLine(), formatter);
        System.out.print("Nova prioridade (1 a 5): ");
        int novaPrioridade = Integer.parseInt(scanner.nextLine());
        System.out.print("Nova categoria: ");
        String novaCategoria = scanner.nextLine();

        boolean atualizado = service.atualizar(id, novoNome, novaDescricao, novaDataTermino, novaPrioridade, novaCategoria);
        if (atualizado) {
            System.out.println("Tarefa atualizada com sucesso!");
        } else {
            System.out.println("Tarefa não encontrada!");
        }
    }

    private static void deletarTarefa() {
        System.out.print("Digite o ID da tarefa que deseja deletar: ");
        int id = Integer.parseInt(scanner.nextLine());

        boolean deletado = service.deletar(id);
        if (deletado) {
            System.out.println("Tarefa deletada com sucesso!");
        } else {
            System.out.println("Tarefa não encontrada!");
        }
    }

    private static void atualizarStatusTarefa() {
        System.out.print("Digite o ID da tarefa que deseja atualizar o status: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Novo status (TODO, DOING, DONE): ");
        StatusEnum novoStatus = StatusEnum.valueOf(scanner.nextLine());

        Tarefa tarefaAtualizada = service.atualizarStatus(id, novoStatus);
        if (tarefaAtualizada != null) {
            System.out.println("Status da tarefa atualizado com sucesso!");
            System.out.println(tarefaAtualizada);
        } else {
            System.out.println("Tarefa não encontrada!");
        }
    }

}
