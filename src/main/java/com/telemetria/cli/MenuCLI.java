package com.telemetria.cli;

import com.telemetria.db.TelemetriaRepository;
import com.telemetria.io.CsvImporter;
import com.telemetria.io.RelatorioExporter;
import com.telemetria.model.TelemetryRecord;
import com.telemetria.service.TelemetriaService;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class MenuCLI {

    private final Scanner scanner = new Scanner(System.in);
    private final CsvImporter csvImporter = new CsvImporter();
    private final RelatorioExporter relatorioExporter = new RelatorioExporter();
    private final TelemetriaService service = new TelemetriaService();
    private final TelemetriaRepository repository;

    private List<TelemetryRecord> registrosCarregados = new ArrayList<>();

    public MenuCLI(TelemetriaRepository repository) {
        this.repository = repository;
    }

    public void executar() {
        boolean continuar = true;
        while (continuar) {
            imprimirMenu();
            String opcao = scanner.nextLine().trim();

            try {
                switch (opcao) {
                    case "1" -> importarCsv();
                    case "2" -> persistirNoBanco();
                    case "3" -> listarDispositivos();
                    case "4" -> mostrarEstatisticas();
                    case "5" -> mostrarTopPioresLatencias();
                    case "6" -> mostrarAnomalias();
                    case "7" -> exportarRelatorio();
                    case "8" -> consultarBancoPorDispositivo();
                    case "9" -> consultarCriticosNoBanco();
                    case "0" -> continuar = false;
                    default -> System.out.println("Opcao invalida.");
                }
            } catch (Exception ex) {
                System.err.println("Erro ao executar a operacao: " + ex.getMessage());
            }
        }
        System.out.println("Encerrando. Ate mais!");
    }

    private void imprimirMenu() {
        System.out.println();
        System.out.println("------- Telemetria de Rede - CLI -------");
        System.out.println("1) Importar telemetria de um arquivo CSV");
        System.out.println("2) Persistir dados carregados no banco (JDBC)");
        System.out.println("3) Listar dispositivos unicos (carregados)");
        System.out.println("4) Estatisticas por dispositivo (carregados)");
        System.out.println("5) Top N piores latencias (carregados)");
        System.out.println("6) Detectar anomalias (ALERTA/CRITICO) (carregados)");
        System.out.println("7) Exportar relatorio para arquivo");
        System.out.println("8) Consultar banco: historico de um dispositivo");
        System.out.println("9) Consultar banco: todos os registros CRITICOS");
        System.out.println("0) Sair");
        System.out.print("Escolha: ");
    }

    private void importarCsv() throws Exception {
        System.out.print("Caminho do arquivo CSV: ");
        String caminho = scanner.nextLine().trim();
        registrosCarregados = csvImporter.importar(Path.of(caminho));
        System.out.printf("Importados %d registros (%d linhas ignoradas por erro).%n",
                registrosCarregados.size(), csvImporter.getLinhasComErro());
    }

    private void persistirNoBanco() throws SQLException {
        if (registrosCarregados.isEmpty()) {
            System.out.println("Nada carregado ainda. Use a opcao 1 primeiro.");
            return;
        }
        repository.inserirEmLote(registrosCarregados);
        System.out.printf("%d registros gravados no banco com sucesso.%n", registrosCarregados.size());
    }

    private void listarDispositivos() {
        if (avisarSeVazio()) return;
        service.dispositivosUnicos(registrosCarregados).forEach(System.out::println);
    }

    private void mostrarEstatisticas() {
        if (avisarSeVazio()) return;
        Map<String, double[]> estatisticas = service.calcularEstatisticasPorDispositivo(registrosCarregados);
        for (var entrada : estatisticas.entrySet()) {
            double[] s = entrada.getValue();
            System.out.printf("%-12s | latencia media: %6.2fms | max: %6.2fms | min: %6.2fms | perda media: %5.2f%%%n",
                    entrada.getKey(), s[0], s[1], s[2], s[3]);
        }
    }

    private void mostrarTopPioresLatencias() {
        if (avisarSeVazio()) return;
        System.out.print("Quantos registros (N)? ");
        int n = Integer.parseInt(scanner.nextLine().trim());
        service.topPioresLatencias(registrosCarregados, n).forEach(System.out::println);
    }

    private void mostrarAnomalias() {
        if (avisarSeVazio()) return;
        List<TelemetryRecord> anomalias = service.detectarAnomalias(registrosCarregados);
        System.out.printf("%d anomalias encontradas:%n", anomalias.size());
        anomalias.forEach(System.out::println);
    }

    private void exportarRelatorio() throws Exception {
        if (avisarSeVazio()) return;
        System.out.print("Nome do arquivo de saida (ex: relatorio.txt): ");
        String nomeArquivo = scanner.nextLine().trim();
        Map<String, double[]> estatisticas = service.calcularEstatisticasPorDispositivo(registrosCarregados);
        relatorioExporter.exportarResumoPorDispositivo(Path.of(nomeArquivo), estatisticas);
        System.out.println("Relatorio salvo em " + nomeArquivo);
    }

    private void consultarBancoPorDispositivo() throws SQLException {
        System.out.print("device_id: ");
        String deviceId = scanner.nextLine().trim();
        List<TelemetryRecord> historico = repository.buscarPorDispositivo(deviceId);
        System.out.printf("%d registros encontrados no banco:%n", historico.size());
        historico.forEach(System.out::println);
    }

    private void consultarCriticosNoBanco() throws SQLException {
        List<TelemetryRecord> criticos = repository.buscarCriticos();
        System.out.printf("%d registros CRITICOS no banco:%n", criticos.size());
        criticos.forEach(System.out::println);
    }

    private boolean avisarSeVazio() {
        if (registrosCarregados.isEmpty()) {
            System.out.println("Nenhum dado carregado. Use a opcao 1 (Importar CSV) primeiro.");
            return true;
        }
        return false;
    }
}