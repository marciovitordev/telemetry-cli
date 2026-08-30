package com.telemetria.io;

import com.telemetria.model.TelemetryRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**escreve relatorios em disco a partir dos dados ja
 * processados pelo TelemetriaService.
 */
public class RelatorioExporter {

    public void exportarResumoPorDispositivo(Path destino, Map<String, double[]> estatisticasPorDispositivo)
            throws IOException {
        // double[] = {mediaLatencia, maxLatencia, minLatencia, mediaPerda}
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write("Relatorio de Telemetria - gerado em " + LocalDateTime.now());
            escritor.newLine();
            escritor.write("=".repeat(70));
            escritor.newLine();

            for (Map.Entry<String, double[]> entrada : estatisticasPorDispositivo.entrySet()) {
                double[] stats = entrada.getValue();
                escritor.write(String.format(
                        "Dispositivo: %-12s | latencia media: %6.2fms | max: %6.2fms | min: %6.2fms | perda media: %5.2f%%",
                        entrada.getKey(), stats[0], stats[1], stats[2], stats[3]));
                escritor.newLine();
            }
        }
    }

    public void exportarAnomalias(Path destino, List<TelemetryRecord> anomalias) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write("device_id,timestamp,latency_ms,packet_loss_percent,bandwidth_mbps,status");
            escritor.newLine();
            for (TelemetryRecord r : anomalias) {
                escritor.write(String.format("%s,%s,%.2f,%.2f,%.2f,%s",
                        r.getDeviceId(), r.getTimestamp(), r.getLatencyMs(),
                        r.getPacketLossPercent(), r.getBandwidthMbps(), r.getStatus()));
                escritor.newLine();
            }
        }
    }
}