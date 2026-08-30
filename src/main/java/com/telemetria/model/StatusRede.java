package com.telemetria.model;


public enum StatusRede {
    OK,
    ALERTA,
    CRITICO;

    // Limiares de classificacao (poderiam vir de um arquivo de configuracao,
    // mas ficam como constantes aqui para manter o exemplo simples e didatico).
    private static final double LATENCIA_CRITICA_MS = 200.0;
    private static final double LATENCIA_ALERTA_MS = 100.0;
    private static final double PERDA_CRITICA_PCT = 10.0;
    private static final double PERDA_ALERTA_PCT = 5.0;

    /*
     * Classifica um registro de telemetria em OK / ALERTA / CRITICO.
     */
    public static StatusRede classificar(double latenciaMs, double perdaPacotesPct) {
        if (latenciaMs >= LATENCIA_CRITICA_MS || perdaPacotesPct >= PERDA_CRITICA_PCT) {
            return CRITICO;
        }
        if (latenciaMs >= LATENCIA_ALERTA_MS || perdaPacotesPct >= PERDA_ALERTA_PCT) {
            return ALERTA;
        }
        return OK;
    }
}