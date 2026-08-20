import service.TarefaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                case 2 -> System.out.println("implementar listagem");
                case 3 -> System.out.println("implementar atualização");
                case 4 -> System.out.println("implementar deleção");
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
        System.out.println("4. Deletar Tarefa");
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
}
