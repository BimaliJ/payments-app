# Payments Service

A Spring Boot REST API that accepts payment instructions, stores them in an H2 database, and processes payment-submitted events through three simulated downstream consumers.

## Technology

- Java 17, Spring Boot 4.1.2, Gradle
- Spring Web, Spring Data JPA, Jakarta Bean Validation
- H2 file-based database
- MapStruct and Lombok
- Jackson XML for fraud-provider responses

## Requirements

- JDK 17 or later
- No external database or message broker required

## Build and run

From the project root:

**Windows**

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

**macOS / Linux**

```bash
./gradlew clean build
./gradlew bootRun
```

The API uses `http://localhost:8080` by default. To run tests separately, use `./gradlew test` (or `.\gradlew.bat test` on Windows).

## API

### Create a payment

`POST /payments`

```bash
curl -i -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -d '{
    "sourceAccount": "1234567890",
    "destinationAccount": {
      "name": "Example Recipient",
      "accountNumber": "9876543210"
    },
    "amount": 125.50,
    "currency": "NZD",
    "reference": "Invoice 1001",
    "requestedExecutionDate": "2026-12-01"
  }'
```

A successful request returns HTTP `201 Created` with the stored payment representation, including its generated `id`. Use an execution date that is today or later when running the example.

### Get a payment

`GET /payments/{id}`

```bash
curl -i http://localhost:8080/payments/YOUR_PAYMENT_UUID
```

The response includes payment details, status, fraud decision, risk score, and fraud reasons where available. Fraud screening happens asynchronously, so the initial result may show a pending fraud decision. A nonexistent payment returns `404 Not Found`.

### Input validation

- Source and destination account numbers: 10–20 digits.
- Destination account name and payment reference: required.
- Amount: greater than zero, with up to two decimal places.
- Currency: three uppercase letters and one of `NZD`, `AUD`, `USD`, or `GBP` by default.
- Requested execution date: today or a future date.

Validation failures and unsupported currencies return HTTP `400` with a structured error response.

## Event processing

When a payment is created, `PaymentService.createPayment` saves the following in one database transaction:

1. The payment entity.
2. A `PaymentSubmitted` event record.
3. A delivery record for each downstream consumer.

This database-backed event/delivery approach avoids depending on an external message broker. `DeliveryService` runs on a scheduled interval (currently hard-coded to **5 seconds**) and processes pending or retryable deliveries.

| Consumer | Behaviour |
| --- | --- |
| Notification | Logs a notification for the payment. |
| Fraud | Calls the local XML fraud-screening stub and updates the payment's fraud result. |
| Ledger | Creates a ledger entry if one does not already exist for the payment. |

On a delivery exception, the delivery is marked for retry and `nextRetryAt` is set to 10 seconds later. The dispatcher processes up to 30 ready deliveries per run. **Implementation limitation:** the current dispatcher wraps a batch in one transaction, so failures and transaction rollback behaviour should be reviewed before production use. This is a simulated delivery mechanism, not a production message broker.

## Fraud-screening stub

The application exposes `POST /screening/v2/check` as a local simulated third-party provider. The endpoint returns XML loaded from:

- `src/main/resources/fraud/success.xml`
- `src/main/resources/fraud/error.xml`

The stub randomly selects outcomes: approximately 70% success and 30% error, with a simulated 3-second delay for 20% of requests. Error XML may be returned with HTTP 200 or 500. `FraudConsumer` parses the XML and records a decision, risk score and reasons on the payment for successful responses; exceptions cause the delivery to be retried.

## Configuration

Configuration is in `src/main/resources/application.properties`.

| Property | Current value / purpose |
| --- | --- |
| `payment.supported-currencies` | `NZD,AUD,USD,GBP` |
| `spring.datasource.url` | `jdbc:h2:file:./data/payments;DB_CLOSE_ON_EXIT=FALSE` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `spring.h2.console.enabled` | `true` |
| `payment.fraud.url` | `http://localhost:${server.port:8080}/screening/v2/check` |
| `payment.delivery.fixed-delay-ms` | `1500` (currently not used by the scheduler; the scheduler uses 5000 ms directly) |

The H2 database is file-based, so payment data can persist across application restarts. Its default username is `sa`, with an empty password in the supplied development configuration.

## Project layout

```text
src/main/java/com/demo/app/payments/
├── configuration/  # Supported-currency configuration
├── consumer/       # Notification, fraud and ledger consumers
├── controller/     # Payment API and fraud-provider stub
├── exception/      # Domain exceptions and API error handling
├── mapper/         # MapStruct payment mapper
├── model/          # Requests, responses, entities and enums
├── repositories/   # Spring Data JPA repositories
└── service/        # Payment creation, lookup and delivery dispatch

src/main/resources/
├── application.properties
└── fraud/           # Sample success/error XML responses
```

## Scope and limitations

This is a demonstration service. It includes persistent payment/event/delivery records and simulated downstream processing, but it does **not** implement payment-request idempotency, a real message broker, production-grade authentication, or Kubernetes deployment configuration. The current automated test suite contains a Spring Boot application-context test; additional API, retry, failure-recovery and integration tests are not implemented.
