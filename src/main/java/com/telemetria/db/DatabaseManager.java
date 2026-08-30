package com.telemetria.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {

    private final String urlConexao;

    public DatabaseManager(String caminhoArquivoBanco) {
        this.urlConexao = "jdbc:sqlite:" + caminhoArquivoBanco;
    }

    /** chama e responsavel por fechar. */
    public Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(urlConexao);
    }

    /**
     * Cria a tabela de telemetria Chamado uma vez no boot da aplicacao
     */
    public void inicializarSchema() throws SQLException {
        String ddl = """
            CREATE TABLE IF NOT EXISTS telemetria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                latency_ms REAL NOT NULL,
                packet_loss_percent REAL NOT NULL,
                bandwidth_mbps REAL NOT NULL,
                status TEXT NOT NULL
            )
            """;

        try (Connection conexao = abrirConexao();
             Statement stmt = conexao.createStatement()) {
            stmt.execute(ddl);
            // Indice para acelerar consultas por dispositivo (usadas na maioria dos relatorios).
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_device ON telemetria(device_id)");
        }
    }
}