package com.telemetria.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Objeto que representa uma unica leitura de telemetria
 * de um dispositivo de rede (roteador, switch, host, etc).
 */
public final class TelemetryRecord implements Comparable<TelemetryRecord> {

    private final String deviceId;
    private final LocalDateTime timestamp;
    private final double latencyMs;
    private final double packetLossPercent;
    private final double bandwidthMbps;
    private final StatusRede status;

    public TelemetryRecord(String deviceId, LocalDateTime timestamp, double latencyMs,
                           double packetLossPercent, double bandwidthMbps) {
        this.deviceId = Objects.requireNonNull(deviceId, "deviceId nao pode ser nulo");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp nao pode ser nulo");
        this.latencyMs = latencyMs;
        this.packetLossPercent = packetLossPercent;
        this.bandwidthMbps = bandwidthMbps;
        // status e derivado automaticamente a partir dos indicadores brutos.
        this.status = StatusRede.classificar(latencyMs, packetLossPercent);
    }

    //construtor usado para persistir nos dados gerados pela empresa.
    public TelemetryRecord(String deviceId, LocalDateTime timestamp, double latencyMs,
                           double packetLossPercent, double bandwidthMbps, StatusRede status) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.latencyMs = latencyMs;
        this.packetLossPercent = packetLossPercent;
        this.bandwidthMbps = bandwidthMbps;
        this.status = status;
    }

    public String getDeviceId() { return deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getLatencyMs() { return latencyMs; }
    public double getPacketLossPercent() { return packetLossPercent; }
    public double getBandwidthMbps() { return bandwidthMbps; }
    public StatusRede getStatus() { return status; }

    /**
     * Ordenacao natural: por timestamp. Usada, por exemplo, quando colocamos
     * registros num TreeSet/TreeMap para reconstituir a linha do tempo.
     */
    @Override
    public int compareTo(TelemetryRecord outro) {
        return this.timestamp.compareTo(outro.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TelemetryRecord)) return false;
        TelemetryRecord that = (TelemetryRecord) o;
        return deviceId.equals(that.deviceId) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, timestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | lat=%.1fms | perda=%.1f%% | banda=%.1fMbps | %s",
                deviceId, timestamp, latencyMs, packetLossPercent, bandwidthMbps, status);
    }
}