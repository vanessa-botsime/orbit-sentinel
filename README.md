# Orbit Graduate System: Event-Driven Tracker & Sentinel Audit Engine

An advanced, event-driven ecosystem designed to streamline graduate job applications and provide enterprise-grade, tamper-evident security auditing. 

This ecosystem brings together your core business tracker and an autonomous security monitor communicating asynchronously via Apache Kafka.

---

## Why Orbit Beats a Traditional Spreadsheet

Juggling job hunting on a standard spreadsheet quickly falls apart. Orbit solves these core pain points:

* **Automated Logging**: Spreadsheets require manual tracking for every stage change. Orbit automatically captures application history and timeline events seamlessly.
* **Proactive Security Auditing**: A spreadsheet has no access history. Orbit streams active authentication changes and system updates directly to an immutable engine, proving exactly who did what.
* **Granular User Isolation**: On a shared spreadsheet, data can easily be viewed or modified by unauthorized parties. Orbit enforces strict JWT security boundaries—applications are isolated per-user.
* **Tamper Evidence**: If anyone alters your spreadsheet history, there's no way to know. Orbit's security logs use a cryptographic chain; any database manipulation breaks the chain instantly.

---

## System Architecture

---

## System Folder Structure

Following the exact blueprint of your workspace layout, your project repository is organized as follows:

orbit-graduate-system/

├── backend/                        # Upgraded core graduate application tracking service
├── orbit-sentinel/                 # Autonomous security audit logging service
│   ├── scripts/                    # Contains automation scripts (e.g., demo.sh)
│   ├── src/main/java/              # Core logic (alerts, audit logs, rules, security)
│   └── src/main/resources/         # Sentinel configuration (application.yml, db scripts)
├── .env.example                    # Template for environment credentials & JWT secrets
├── .gitignore                      # Git exclusion rules
├── docker-compose.yml              # Services orchestrator (Kafka, Zookeeper, PostgreSQL)
├── pom.xml                         # Root configuration manager
├── START-HERE.txt                  # Setup workflow checklist
└── README.md                       # Main ecosystem documentation

---

## How to Run the Ecosystem Offline

This project is fully optimized to run in an offline environment by caching dependencies beforehand.

### 1. Boot up the Infrastructure Broker
From your root workspace folder, boot up your databases and Apache Kafka messaging broker in detached mode:
```bash
docker compose up -d
```

### 2. Cache Dependencies (Do this while online)
Before disconnecting from the internet, run the clean compilation verification in both application roots to download and store your Maven cache:
```bash
# Cache Core Tracker
cd backend
./mvnw clean verify

# Cache Sentinel Engine
cd ../orbit-sentinel
./mvnw clean verify
```

### 3. Spin up the Software Services
Open two side-by-side terminal tabs to execute your programs simultaneously:

* **Run the Graduate Backend (Port 8080):**
  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```
* **Run the Sentinel Engine (Port 8081):**
  ```bash
  cd orbit-sentinel
  ```
  *(Note: Check your Sentinel README or script instructions if you need to run `./mvnw spring-boot:run` or a dedicated test framework setup here).*

---

## Sentinel Threat Detection Matrix

Orbit Sentinel sniffs incoming events streaming through Kafka channels and monitors for critical alerts:
1. **Failed Login Bursts**: Catches automated brute force attacks if login failures exceed standard metrics.
2. **After-Hours Actions**: Highlights actions taken during high-risk anomaly time frames (e.g., between midnight and 5 AM).
3. **Privilege Escalation**: Dispatches emergency notifications to connected Webhooks (Discord/Slack) instantly if a user account role shifts to `ADMIN` or `AUDITOR`.
4. **Cryptographic Validation**: Links entries sequentially using hash pointers. If log history is compromised or manually deleted in the database layer, the chain invalidates itself immediately.
