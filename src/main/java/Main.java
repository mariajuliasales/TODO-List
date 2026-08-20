import service.TarefaService;

import java.util.Scanner;

public class Main {

    private static final TarefaService service = new TarefaService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        int opc = -2;

        while (opc != 0) {
            exibirMenu();

            opc = Integer.parseInt(scanner.nextLine());
            switch (opc) {
                case 1 -> System.out.println("implementar cadastro");
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
}
