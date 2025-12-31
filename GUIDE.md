# 📘 Fraud Detection System - Quick Start Guide

This guide provides all the necessary information to run, access, and test the Fraud Detection System.

## 🚀 1. How to Run the Application

The system requires two parts:
1.  **Infrastructure (Docker):** Runs the Database, Kafka, and Management UIs.
2.  **Application (Spring Boot):** Runs the logical backend.

### Step 1: Start Infrastructure
Open a terminal and run:
```bash
docker-compose up -d zookeeper kafka postgres adminer kafka-ui
```
*Note: We exclude `fraud-service` from docker to run it locally for development.*

### Step 2: Start Spring Boot Backend
Open a **new** terminal window and run:
```bash
./mvnw clean spring-boot:run
```
*Wait for the logs to show "Started FraudDetectionApplication".*

### Step 3: Start Angular Frontend (New!)
Open a **third** terminal window and run:
```bash
cd frontend
npm start
```
*Wait for the compilation to finish, then open `http://localhost:4201` in your browser.*

---

## 🌐 2. Access Points & Ports

| Service | URL / Address | Description |
| :--- | :--- | :--- |
| **Angular Dashboard** | `http://localhost:4201` | **(New)** The real-time interactive dashboard. |
| **Backend API** | `http://localhost:8088` | The main REST API. |
| **Data Viewer** | `viewer.html` | (Legacy) Simple HTML file to view data. |
| **Kafka UI** | `http://localhost:8090` | Web interface to monitor Kafka topics and consumers. |
| **Adminer (DB)** | `http://localhost:8082` | Web interface to manage the PostgreSQL database. |
| **PostgreSQL** | `localhost:5433` | Database direct connection port. |
| **Kafka Broker** | `localhost:9095` | Kafka direct connection port. |

---

## 🧪 3. How to Test & Check

### A. Automatic Testing Script
We have provided a script to run various fraud scenarios automatically.
run:
```bash
chmod +x test_api.sh
./test_api.sh
```

### B. Manual API Check (Curl)
You can manually send a transaction to check for fraud:
```bash
curl -X POST http://localhost:8088/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000.0,
    "oldBalance": 500000.0,
    "newBalance": 0.0,
    "type": 2,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 500000.0,
    "ip": "5.6.7.8",
    "email": "test@example.com"
  }'
```

---

## 🐘 4. Database Access (PostgreSQL)

You can access the database using **Adminer** (`http://localhost:8082`) or any DB Client (DBeaver, IntelliJ).

*   **System:** PostgreSQL
*   **Server:** `postgres` (if inside docker network) or `localhost` (if local)
*   **Port:** `5433` (External access)
*   **Username:** `postgres`
*   **Password:** `password`
*   **Database:** `fraud_db`
*   **Main Table:** `fraud_checks` (Stores all checked transactions)

---

## 📨 5. Kafka Consumer Access

You can monitor the messages flowing through the system using **Kafka UI** (`http://localhost:8090`).

1.  Open `http://localhost:8090` in your browser.
2.  Go to **Topics** -> `fraud-checks`.
3.  Click on the **Messages** tab to see real-time JSON messages sent by the application.
4.  Go to **Consumers** to see the `fraud-consumer-group` status.

---

## 🛑 Stopping the Application

To stop the Spring Boot app, press `Ctrl+C` in its terminal.
To stop the infrastructure, run:
```bash
docker-compose stop
```
