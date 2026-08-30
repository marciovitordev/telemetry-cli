package com.telemetria.io;

import com.telemetria.model.TelemetryRecord;

import java.io.IOException;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

///Responsavel por LER o arquivo bruto de telemetria (CSV) e transformar cada linha em um historico de telemetria.

public class CsvImporter {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Guarda quantas linhas foram ignoradas na ultima importacao (para relatorio ao usuario). */
    private int linhasComErro = 0;

    public List<TelemetryRecord> importar(Path caminhoArquivo) throws IOException {
        List<TelemetryRecord> registros = new ArrayList<>();
        linhasComErro = 0;

        try (BufferedReader leitor = Files.newBufferedReader(caminhoArquivo, StandardCharsets.UTF_8)) {
            String linha = leitor.readLine(); // descarta cabecalho
            int numeroLinha = 1;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                if (linha.isBlank()) continue;

                try {
                    registros.add(parseLinha(linha));
                } catch (Exception ex) {
                    linhasComErro++;
                    System.err.printf("Aviso: linha %d ignorada (%s) -> %s%n",
                            numeroLinha, ex.getMessage(), linha);
                }
            }
        }
        return registros;
    }

    private TelemetryRecord parseLinha(String linha) {
        String[] campos = linha.split(",");
        if (campos.length != 5) {
            throw new IllegalArgumentException("esperado 5 campos, encontrado " + campos.length);
        }

        String deviceId = campos[0].trim();
        LocalDateTime timestamp = LocalDateTime.parse(campos[1].trim(), FORMATO_DATA);
        double latencia = Double.parseDouble(campos[2].trim());
        double perda = Double.parseDouble(campos[3].trim());
        double banda = Double.parseDouble(campos[4].trim());

        return new TelemetryRecord(deviceId, timestamp, latencia, perda, banda);
    }

    public int getLinhasComErro() {
        return linhasComErro;
    }
}