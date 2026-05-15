# 🇰🇭 Bakong KHQR Payment System (Spring Boot + PostgreSQL + PostgREST)

A premium Spring Boot application integrated with the **Bakong KHQR** payment system, featuring persistent storage in **PostgreSQL**, a real-time **PostgREST API**, and an automated **transaction expiry** system.

---

## Key Features

*   **KHQR Generation**: Manual EMVCo-compliant KHQR string generation with CRC16-CCITT validation.
*   **PostgreSQL Persistence**: All transactions are saved with detailed metadata (customer info, status, timestamps).
*   **Premium Web UI**: A glassmorphic, responsive payment page with real-time status polling.
*   **PostgREST Integration**: Instant RESTful API layer over your PostgreSQL tables on port `3000`.
*   **Automated Expiry**: A background task automatically marks pending transactions as `EXPIRED` after 15 minutes.
*   **Admin Tools**: Full transaction history endpoints and **pgAdmin 4** for database management.
*   **Dockerized**: One-command deployment for the entire stack.

---

## 🛠 Tech Stack

*   **Backend**: Spring Boot 3.3.6, Spring Data JPA, Spring WebFlux.
*   **Database**: PostgreSQL 18 Beta.
*   **API Layer**: PostgREST (Automatic REST API).
*   **Frontend**: HTML5, CSS3 (Glassmorphism), JavaScript (Async/Polling).
*   **Infrastructure**: Docker, Docker Compose.

---

## Environment Configuration (`.env`)

Before running, ensure your `.env` file is configured:

```env
DB_NAME=docker_spring
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5431
APP_PORT=9092

# Bakong Settings
BAKONG_ACCOUNT_ID=your_id@bank
BAKONG_TOKEN=your_token
```

---
##  Getting Started

### 1. Start the Stack
Run the following command to build and start all services:

```bash
docker-compose up --build -d
```

### 2. Access the Services

| Service | URL | Description |
| :--- | :--- | :--- |
| **Payment UI** | [http://localhost:9092](http://localhost:9092) | Main payment interface |
| **Spring Swagger** | [http://localhost:9092/swagger-ui/index.html](http://localhost:9092/swagger-ui/index.html) | Backend API Documentation |
| **PostgREST API** | [http://localhost:3000/transactions](http://localhost:3000/transactions) | Direct DB-to-REST API |
| **pgAdmin 4** | [http://localhost:5050](http://localhost:5050) | DB Management (Admin/Admin) |

---

## 🔌 API Endpoints (Spring Boot)

### Payment API
*   `POST /api/payment/generate-qr`: Generates a new KHQR and saves a `PENDING` transaction.
*   `GET /api/payment/check-status/{id}`: Polls the Bakong API and updates the DB status.
*   `POST /api/payment/callback`: Webhook endpoint for Bakong to notify payment success.

### Admin API
*   `GET /api/payment/transactions`: Get all transaction records.
*   `GET /api/payment/transactions/{id}`: Get details of a specific transaction.

---

---

## Troubleshooting

*   **Conflict Errors**: If you see container name conflicts, run:
    `docker rm -f pgadmin4 meangsreang-app postgres18-db postgrest-api`
*   **Database Not Found**: The app uses `docker_spring`. If it wasn't created automatically, run:
    `docker exec postgres18-db psql -U postgres -c "CREATE DATABASE docker_spring;"`

---
