import model.Alarme;
import model.StatusEnum;
import model.Tarefa;
import repository.TarefaRepository;
import service.TarefaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final TarefaService service = new TarefaService(new TarefaRepository("tarefas.dat"));
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime HORA_PADRAO = LocalTime.of(23, 59);
    private static final int INTERVALO_VERIFICACAO_SEGUNDOS = 30;


    public static void main(String[] args) {

        verificarAlarmes();
        iniciarVerificacaoPeriodica();

        int opc = -2;

        while (opc != 0) {
            exibirMenu();

            try {
                opc = Integer.parseInt(scanner.nextLine().trim());
                switch (opc) {
                    case 1 -> cadastrarTarefa();
                    case 2 -> listarTarefas();
                    case 3 -> atualizarTarefa();
                    case 4 -> atualizarStatusTarefa();
                    case 5 -> deletarTarefa();
                    case 6 -> gerenciarAlarmes();
                    case 0 -> System.out.println("Saindo do sistema...");
                    default -> System.out.println("Opção inválida!");
                }
            } catch (NoSuchElementException e) {
                break; // entrada encerrada
            } catch (RuntimeException e) {
                System.out.println("Entrada inválida: " + e.getMessage());
            }
        }

        System.exit(0);
    }

    private static void exibirMenu() {
        System.out.println("\n--- TODO LIST ---");
        System.out.println("1. Cadastrar Tarefa");
        System.out.println("2. Listar Tarefas");
        System.out.println("3. Atualizar Tarefa");
        System.out.println("4. Atualizar Status da Tarefa");
        System.out.println("5. Deletar Tarefa");
        System.out.println("6. Gerenciar Alarmes da Tarefa");
        System.out.println("0. Sair do programa");
        System.out.print("Escolha uma opção: ");
    }

    // ---------- alarmes ----------

    private static void verificarAlarmes() {
        List<String> avisos = service.verificarAlarmes(LocalDateTime.now());
        if (avisos.isEmpty()) {
            return;
        }

        System.out.println("\n****************** ALARMES ******************");
        avisos.forEach(aviso -> System.out.println("🔔 " + aviso));
        System.out.println("*********************************************");
    }

    private static void iniciarVerificacaoPeriodica() {
        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "verificador-alarmes");
            t.setDaemon(true);
            return t;
        });
        agendador.scheduleAtFixedRate(Main::verificarAlarmes,
                INTERVALO_VERIFICACAO_SEGUNDOS, INTERVALO_VERIFICACAO_SEGUNDOS, TimeUnit.SECONDS);
    }

    private static List<Integer> lerAlarmes(int prioridade) {
        List<Integer> antecedencias = new ArrayList<>();

        System.out.print("Acionar alarme? (s/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
            return antecedencias;
        }

        int sugestao = service.sugerirAntecedencia(prioridade);
        do {
            int minutos = lerAntecedencia(sugestao);
            if (antecedencias.contains(minutos)) {
                System.out.println("Já existe um alarme com essa antecedência.");
            } else {
                antecedencias.add(minutos);
            }
            System.out.print("Adicionar outro alarme? (s/n): ");
        } while (scanner.nextLine().trim().equalsIgnoreCase("s"));

        return antecedencias;
    }

    private static int lerAntecedencia(int sugestao) {
        System.out.print("Antecedência (ex.: 30m, 2h, 1d) [Enter = " + Alarme.formatarMinutos(sugestao) + "]: ");
        String entrada = scanner.nextLine().trim().toLowerCase();
        if (entrada.isEmpty()) {
            return sugestao;
        }
        return converterAntecedencia(entrada);
    }

    private static int converterAntecedencia(String entrada) {
        char unidade = entrada.charAt(entrada.length() - 1);
        int multiplicador = switch (unidade) {
            case 'm' -> 1;
            case 'h' -> 60;
            case 'd' -> 1440;
            default -> 0;
        };

        if (multiplicador == 0) {
            return Integer.parseInt(entrada);
        }
        return Integer.parseInt(entrada.substring(0, entrada.length() - 1).trim()) * multiplicador;
    }

    private static LocalTime lerHora(String mensagem) {
        System.out.print(mensagem + " (HH:mm) [Enter = " + HORA_PADRAO.format(horaFormatter) + "]: ");
        String entrada = scanner.nextLine().trim();
        return entrada.isEmpty() ? HORA_PADRAO : LocalTime.parse(entrada, horaFormatter);
    }

    private static void gerenciarAlarmes() {
        System.out.print("Digite o ID da tarefa: ");
        int id = Integer.parseInt(scanner.nextLine());

        Tarefa tarefa = service.buscarPorId(id);
        if (tarefa == null) {
            System.out.println("Tarefa não encontrada!");
            return;
        }

        System.out.println("\n--- ALARMES DE \"" + tarefa.getNome() + "\" (prazo: "
                + tarefa.getDataTermino().format(formatter) + " às " + tarefa.getHoraTermino().format(horaFormatter) + ") ---");
        List<Alarme> alarmes = tarefa.getAlarmes();
        if (alarmes.isEmpty()) {
            System.out.println("Nenhum alarme configurado.");
        } else {
            for (int i = 0; i < alarmes.size(); i++) {
                System.out.println((i + 1) + ". " + alarmes.get(i));
            }
        }

        System.out.println("\n1. Adicionar alarme");
        System.out.println("2. Remover alarme");
        System.out.println("0. Voltar");
        System.out.print("Escolha uma opção: ");
        int opcao = Integer.parseInt(scanner.nextLine());

        switch (opcao) {
            case 1 -> {
                int minutos = lerAntecedencia(service.sugerirAntecedencia(tarefa.getPrioridade()));
                service.adicionarAlarme(id, minutos);
                System.out.println("Alarme adicionado com sucesso!");
                verificarAlarmes();
            }
            case 2 -> {
                System.out.print("Número do alarme a remover: ");
                int indice = Integer.parseInt(scanner.nextLine()) - 1;
                if (service.removerAlarme(id, indice)) {
                    System.out.println("Alarme removido com sucesso!");
                } else {
                    System.out.println("Alarme não encontrado!");
                }
            }
            case 0 -> { }
            default -> System.out.println("Opção inválida!");
        }
    }

    // ---------- tarefas ----------

    private static void cadastrarTarefa() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();
        System.out.print("Data Término (dd/MM/yyyy): ");
        LocalDate dataTermino = LocalDate.parse(scanner.nextLine(), formatter);
        LocalTime horaTermino = lerHora("Hora Término");
        System.out.print("Categoria: ");
        String categoria = scanner.nextLine();
        System.out.print("Prioridade (1 a 5): ");
        int prioridade = Integer.parseInt(scanner.nextLine());
        List<Integer> alarmes = lerAlarmes(prioridade);

        service.adicionar(nome, descricao, dataTermino, horaTermino, prioridade, categoria, alarmes);
        System.out.println("Tarefa cadastrada com sucesso!");

        verificarAlarmes();
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
        LocalTime novaHoraTermino = lerHora("Nova hora de término");
        System.out.print("Nova prioridade (1 a 5): ");
        int novaPrioridade = Integer.parseInt(scanner.nextLine());
        System.out.print("Nova categoria: ");
        String novaCategoria = scanner.nextLine();

        boolean atualizado = service.atualizar(id, novoNome, novaDescricao, novaDataTermino, novaHoraTermino, novaPrioridade, novaCategoria);
        if (atualizado) {
            System.out.println("Tarefa atualizada com sucesso!");
            verificarAlarmes();
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
