package com.telemetria.db;

import com.telemetria.model.StatusRede;
import com.telemetria.model.TelemetryRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TelemetriaRepository {

    private final DatabaseManager databaseManager;

    public TelemetriaRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    //Insere varios registros numa unica transação
    public void inserirEmLote(List<TelemetryRecord> registros) throws SQLException {
        String sql = """
            INSERT INTO telemetria (device_id, timestamp, latency_ms, packet_loss_percent, bandwidth_mbps, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = databaseManager.abrirConexao()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                for (TelemetryRecord r : registros) {
                    ps.setString(1, r.getDeviceId());
                    ps.setString(2, r.getTimestamp().toString());
                    ps.setDouble(3, r.getLatencyMs());
                    ps.setDouble(4, r.getPacketLossPercent());
                    ps.setDouble(5, r.getBandwidthMbps());
                    ps.setString(6, r.getStatus().name());
                    ps.addBatch(); // acumula o comando em vez de enviar imediatamente
                }
                ps.executeBatch(); // envia tudo de uma vez ao banco
                conexao.commit();
            } catch (SQLException ex) {
                conexao.rollback(); // se algo falhar no meio, desfaz tudo
                throw ex;
            }
        }
    }

    /** Busca todos os registros de um dispositivo especifico, ordenados por tempo. */
    public List<TelemetryRecord> buscarPorDispositivo(String deviceId) throws SQLException {
        String sql = "SELECT * FROM telemetria WHERE device_id = ? ORDER BY timestamp ASC";
        List<TelemetryRecord> resultado = new ArrayList<>();

        try (Connection conexao = databaseManager.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, deviceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearLinha(rs));
                }
            }
        }
        return resultado;
    }

    /**  todos os registros classificados como CRITICO, mais recentes primeiro. */
    public List<TelemetryRecord> buscarCriticos() throws SQLException {
        String sql = "SELECT * FROM telemetria WHERE status = ? ORDER BY timestamp DESC";
        List<TelemetryRecord> resultado = new ArrayList<>();

        try (Connection conexao = databaseManager.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, StatusRede.CRITICO.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearLinha(rs));
                }
            }
        }
        return resultado;
    }

    //registros por dispositivo
    public Map<String, Integer> contarPorDispositivo() throws SQLException {
        String sql = "SELECT device_id, COUNT(*) AS total FROM telemetria GROUP BY device_id ORDER BY device_id";
        Map<String, Integer> contagem = new LinkedHashMap<>();

        try (Connection conexao = databaseManager.abrirConexao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contagem.put(rs.getString("device_id"), rs.getInt("total"));
            }
        }
        return contagem;
    }

     //Mapeamento manual ResultSet -> objeto de dominio
    private TelemetryRecord mapearLinha(ResultSet rs) throws SQLException {
        return new TelemetryRecord(
                rs.getString("device_id"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getDouble("latency_ms"),
                rs.getDouble("packet_loss_percent"),
                rs.getDouble("bandwidth_mbps"),
                StatusRede.valueOf(rs.getString("status"))
        );
    }
}