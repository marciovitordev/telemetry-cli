# 📡 Telemetry Network CLI

CLI em **Java 17+** para simulação de telemetria e diagnóstico de rede. Importa dados via CSV, classifica a severidade, analisa métricas, persiste o histórico com **JDBC puro** e gera relatórios.

## ✨ Funcionalidades

* 📥 Importação resiliente de telemetria via CSV
* 🚦 Classificação `OK` / `ALERTA` / `CRÍTICO`
* 🧠 Análises: dispositivos, estatísticas, Top N e anomalias
* 🗄️ Persistência com JDBC, transações e batch inserts
* 📄 Exportação de relatórios
* 🖥️ CLI interativa

## 🏗️ Arquitetura

```text
com.telemetria
├── model/     → Dados e regras
├── io/        → CSV e relatórios
├── db/        → JDBC e persistência
├── service/   → Regras de negócio e Collections
├── cli/       → Interface de terminal
└── Main.java  → Inicialização
```

Fluxo:

```text
CSV → Importer → Service → Repository → SQLite
                    ↓
                Relatórios
```

## 🧰 Stack

* **Java 17+**
* **Maven**
* **JDBC puro**
* **SQLite**
* **Collections:** `HashMap`, `TreeMap`, `TreeSet`, `PriorityQueue`, `ArrayList`
* **CLI:** `Scanner`

## 📁 Estrutura

```text
telemetry-cli/
├── pom.xml
├── README.md
├── TUTORIAL.md
├── data/
└── src/main/java/com/telemetria/
    ├── Main.java
    ├── model/
    ├── io/
    ├── db/
    ├── service/
    └── cli/
```

## 🚀 Como executar

Pré-requisitos: **JDK 17+** e **Maven**.
Pré-requisitos: **JDK 17+** e **Maven**.

```bash
git clone https://github.com/marciovitordev/telemetry-network-cli.git
cd telemetry-network-cli
mvn clean package
java -jar target/telemetry-cli-jar-with-dependencies.jar
```

Execute a partir da raiz do projeto.

Para testar, importe:

```text
data/telemetria_amostra.csv
```

## 📊 CSV

```csv
device_id,timestamp,latency_ms,packet_loss_percent,bandwidth_mbps
rtr-01,2024-05-10T08:00:00,42.5,0.2,850.0
```

Campos: `device_id`, `timestamp`, `latency_ms`, `packet_loss_percent` e `bandwidth_mbps`.

## 🔄 Banco de dados

O projeto utiliza **SQLite**, sem necessidade de servidor externo. Para usar PostgreSQL ou MySQL, basta trocar o driver no `pom.xml` e a URL de conexão no `DatabaseManager`.

## 📄 Licença

Projeto de estudo/portfólio. Livre para uso como referência.
