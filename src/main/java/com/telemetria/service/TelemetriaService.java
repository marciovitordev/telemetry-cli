package com.telemetria.service;

import com.telemetria.model.StatusRede;
import com.telemetria.model.TelemetryRecord;

import java.util.*;

public class TelemetriaService {

    //Agrupa os registros por dispositivo.
    public Map<String, List<TelemetryRecord>> indexarPorDispositivo(List<TelemetryRecord> registros) {
        Map<String, List<TelemetryRecord>> indice = new HashMap<>();
        for (TelemetryRecord r : registros) {
            indice.computeIfAbsent(r.getDeviceId(), chave -> new ArrayList<>()).add(r);
        }
        return indice;
    }

    //Retorna o conjunto de dispositivos distintos, ja ORDENADOS.

    public SortedSet<String> dispositivosUnicos(List<TelemetryRecord> registros) {
        SortedSet<String> dispositivos = new TreeSet<>();
        for (TelemetryRecord r : registros) {
            dispositivos.add(r.getDeviceId());
        }
        return dispositivos;
    }

    /**
     * Reconstroi a linha do tempo global ordenada por instante de coleta.
     * TreeMap mantem as chaves (timestamp) sempre ordenadas automaticamente,
     * diferente de um HashMap onde a ordem seria imprevisivel.
     */
    public TreeMap<java.time.LocalDateTime, TelemetryRecord> linhaDoTempo(List<TelemetryRecord> registros) {
        TreeMap<java.time.LocalDateTime, TelemetryRecord> linha = new TreeMap<>();
        for (TelemetryRecord r : registros) {
            linha.put(r.getTimestamp(), r);
        }
        return linha;
    }

    //Encontra os N registros com PIOR latencia (maior valor), sem precisar

    public List<TelemetryRecord> topPioresLatencias(List<TelemetryRecord> registros, int n) {
        Comparator<TelemetryRecord> porLatenciaAsc = Comparator.comparingDouble(TelemetryRecord::getLatencyMs);
        PriorityQueue<TelemetryRecord> heapMinimo = new PriorityQueue<>(porLatenciaAsc);

        for (TelemetryRecord r : registros) {
            if (heapMinimo.size() < n) {
                heapMinimo.offer(r);
            } else if (!heapMinimo.isEmpty() && r.getLatencyMs() > heapMinimo.peek().getLatencyMs()) {
                heapMinimo.poll();
                heapMinimo.offer(r);
            }
        }

        // heapMinimo tem os N piores, mas em ordem crescente (o pior por ultimo
        // ao ser removido). Convertendo para lista e ordenando decrescente
        // para apresentacao (o "campeao" de latencia aparece primeiro).
        List<TelemetryRecord> resultado = new ArrayList<>(heapMinimo);
        resultado.sort(porLatenciaAsc.reversed());
        return resultado;
    }

    // Filtra apenas os registros que NAO estao em estado OK (ALERTA ou CRITICO).
    public List<TelemetryRecord> detectarAnomalias(List<TelemetryRecord> registros) {
        List<TelemetryRecord> anomalias = new ArrayList<>();
        for (TelemetryRecord r : registros) {
            if (r.getStatus() != StatusRede.OK) {
                anomalias.add(r);
            }
        }
        return anomalias;
    }

    //Calcula estatisticas (media, maximo, minimo de latencia + media de perda de pacotes) por dispositivo.

        public Map<String, double[]> calcularEstatisticasPorDispositivo(List<TelemetryRecord> registros) {
        Map<String, List<TelemetryRecord>> porDispositivo = indexarPorDispositivo(registros);
        Map<String, double[]> estatisticas = new LinkedHashMap<>();

        for (String dispositivo : new TreeSet<>(porDispositivo.keySet())) {
            List<TelemetryRecord> lista = porDispositivo.get(dispositivo);

            double somaLatencia = 0, maxLatencia = Double.MIN_VALUE, minLatencia = Double.MAX_VALUE;
            double somaPerda = 0;

            for (TelemetryRecord r : lista) {
                somaLatencia += r.getLatencyMs();
                somaPerda += r.getPacketLossPercent();
                maxLatencia = Math.max(maxLatencia, r.getLatencyMs());
                minLatencia = Math.min(minLatencia, r.getLatencyMs());
            }

            double mediaLatencia = somaLatencia / lista.size();
            double mediaPerda = somaPerda / lista.size();

            estatisticas.put(dispositivo, new double[]{mediaLatencia, maxLatencia, minLatencia, mediaPerda});
        }
        return estatisticas;
    }
}