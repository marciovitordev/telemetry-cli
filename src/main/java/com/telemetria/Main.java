package com.telemetria;

import com.telemetria.cli.MenuCLI;
import com.telemetria.db.DatabaseManager;
import com.telemetria.db.TelemetriaRepository;

public class Main {
    public static void main(String[] args) throws Exception {
        DatabaseManager databaseManager = new DatabaseManager("telemetria.db");
        databaseManager.inicializarSchema();

        TelemetriaRepository repository = new TelemetriaRepository(databaseManager);
        MenuCLI cli = new MenuCLI(repository);
        cli.executar();
    }
}