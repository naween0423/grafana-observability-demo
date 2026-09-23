# End-to-End Microservice Observability Stack

A production-grade local environment demonstrating end-to-end log collection, processing, and visualization using **Spring Boot**, **Grafana Alloy**, **Grafana Loki**, and **Grafana**. 

This repository serves as a functional reference architecture for building modern observability pipelines in microservice systems.

---

## Architecture Overview

```
+------------------+         HTTP POST         +-----------------------+
|  Frontend Client | ------------------------> | Spring Boot Backend   |
| (Interactive UI) |   /api/v1/logs            | (REST API + Logger)   |
+------------------+                           +-----------+-----------+
                                                           |
                                                (Writes Structured JSON)
                                                           v
                                               /var/log/springboot-app/
                                                     application.log
                                                           |
                                                   (Shared Volume)
                                                           |
+------------------+      OTLP / Push API      +-----------v-----------+
|     Grafana      | <------------------------ |     Grafana Alloy     |
| (Visualization)  |   http://loki:3100        |   (Collector Engine)  |
+------------------+                           +-----------------------+
```

### Components

1. **Frontend App (`frontend`)**: Lightweight Nginx web UI providing an interactive dashboard to trigger client events and simulate logs.
2. **Spring Boot Backend (`spring-backend`)**: Java 17 REST API that accepts client telemetry payloads and writes formatted JSON logs to disk.
3. **Grafana Alloy (`alloy`)**: Vendor-agnostic telemetry collector configured via River syntax. Scrapes log files from shared volumes, parses structured metadata, and forwards logs to storage.
4. **Grafana Loki (`loki`)**: Horizontal, multi-tenant log aggregation system optimized for chunked storage and fast LogQL querying.
5. **Grafana (`grafana`)**: Unified visualization dashboard pre-configured with Loki as the default data source.

---

## Repository Structure

```text
.
├── docker-compose.yml             # Container orchestration and network layout
├── README.md                      # Project documentation
├── alloy/
│   └── config.river              # Alloy pipeline processing rules (River DSL)
├── backend/
│   ├── Dockerfile                # Multi-stage JDK 17 Maven build configuration
│   ├── pom.xml                   # Spring Boot dependencies
│   └── src/
│       └── main/
│           ├── java/             # REST Controller and application entrypoint
│           └── resources/        # Application properties and log file settings
├── frontend/
│   └── index.html                # Interactive UI client for triggering logs
└── grafana/
    └── provisioning/
        └── datasources/
            └── datasources.yaml    # Automated Loki datasource registration
```

---

## Quick Start

### Prerequisites

* [Docker Engine](https://docs.docker.com/get-docker/) (v20.10 or higher)
* [Docker Compose](https://docs.docker.com/compose/install/) (v2.0 or higher)
* `curl` or Postman (optional, for manual API testing)

### Installation & Launch

1. **Clone or Extract the Project:**
   ```bash
   cd observability_stack
   ```

2. **Start the Stack:**
   ```bash
   docker-compose up --build -d
   ```

3. **Verify Running Containers:**
   ```bash
   docker-compose ps
   ```

---

## Service Directory & Web Interfaces

| Service Name | Local URL | Default Credentials | Description |
| :--- | :--- | :--- | :--- |
| **Interactive Client UI** | [http://localhost:8000](http://localhost:8000) | N/A | Trigger `INFO` and `ERROR` events |
| **Spring Boot API** | [http://localhost:8080](http://localhost:8080) | N/A | REST log ingestion endpoint |
| **Grafana Alloy Inspector** | [http://localhost:12345](http://localhost:12345) | N/A | Live pipeline component inspector |
| **Grafana Dashboards** | [http://localhost:3000](http://localhost:3000) | `admin` / `admin` | Telemetry visualization and LogQL |
| **Grafana Loki Engine** | [http://localhost:3100](http://localhost:3100) | N/A | Direct Loki HTTP storage endpoint |

---

## How to Test and View Logs

### Option 1: Via Web UI
1. Navigate to `http://localhost:8000` in your browser.
2. Click **Trigger Info Log** or **Trigger Error Log**.

### Option 2: Via `curl` API Call
Send a sample log directly to the Spring Boot endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "INFO",
    "message": "User completed order checkout successfully",
    "appVersion": "1.0.0",
    "userId": "usr_78341"
  }'
```

### Viewing Logs in Grafana

1. Open **Grafana** at `http://localhost:3000` (Log in with `admin`/`admin`).
2. Navigate to **Explore** from the side navigation panel (or press `G` then `E`).
3. Ensure **Loki** is selected in the top-left data source selector.
4. Run the following LogQL query:
   ```logql
   {job="springboot-backend"}
   ```
5. Set the time range to **Last 15 minutes** or toggle **Live** mode.

---

## Useful LogQL Query Examples

* **Filter by `ERROR` log level:**
  ```logql
  {job="springboot-backend"} |= "level=ERROR"
  ```

* **Filter by a specific user:**
  ```logql
  {job="springboot-backend"} |= "userId=usr_78341"
  ```

* **Count log entries over time (Rate metric):**
  ```logql
  rate({job="springboot-backend"}[1m])
  ```

---

## Troubleshooting

### Logs aren't showing up in Grafana?

1. **Verify Spring Boot Log File Creation:**
   Exec into the backend container to check if log files are being written:
   ```bash
   docker-compose exec spring-backend cat /app/logs/application.log
   ```

2. **Check Grafana Alloy Status:**
   Visit the Alloy Inspector UI at `http://localhost:12345` to verify that `local.file_match.springboot_logs` has discovered the log target.

3. **Check Container Logs:**
   ```bash
   docker-compose logs alloy
   docker-compose logs loki
   ```

---

## Stopping the Environment

To stop and remove all containers, networks, and volumes:

```bash
docker-compose down -v
```