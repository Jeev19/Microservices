# Microservices Saga Demo — API Gateway + Saga Orchestration + Circuit Breaker

A fully runnable multi-module Spring Boot project demonstrating 3 microservices design
patterns working together in a single checkout flow:

| Pattern | Where it lives |
|---|---|
| **API Gateway** | `api-gateway` — single entry point, routes requests to the right service |
| **Saga Orchestration** | `order-service` — coordinates Inventory + Payment, runs compensation on failure |
| **Circuit Breaker** | `order-service` (`PaymentClient`) — wraps the call to `payment-service` using Resilience4j |

## API Documentation (Swagger / OpenAPI 3)

Every service exposes OpenAPI 3.0 docs via springdoc-openapi.

**Per-service Swagger UI** (each service documents only its own API):

| Service | Swagger UI | Raw OpenAPI JSON |
|---|---|---|
| Order Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Inventory Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Payment Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |

**Aggregated Swagger UI at the Gateway** — one single page with a dropdown to switch
between all 3 services' docs, proxied through the gateway (no CORS issues):

```
http://localhost:8080/swagger-ui.html
```

The gateway has extra routes (`/order-service/**`, `/inventory-service/**`,
`/payment-service/**`) purely to proxy each service's `/v3/api-docs` through to the
aggregated Swagger UI — these aren't part of the business API.

All endpoints are annotated with `@Operation`, `@ApiResponses`, and `@Schema` so the
generated docs include descriptions, example payloads, and response codes — fully valid
OpenAPI 3.0 output you can also export and import into Postman/Insomnia.

## Project layout

```
microservices-saga-demo/
├── api-gateway/        (port 8080) — Spring Cloud Gateway
├── order-service/       (port 8081) — Saga Orchestrator + Circuit Breaker
├── inventory-service/    (port 8082) — Stock reserve/release
├── payment-service/      (port 8083) — Simulated payment gateway (with failure toggle)
└── README.md
```

Each service is an **independent Maven project** with its own `pom.xml` — you can build/run
each one separately. They talk to each other over plain REST.

## Prerequisites

- Java 17+
- Maven 3.6+
- Ports 8080–8083 free on your machine

## How to run (4 separate terminals)

Start them in this order — inventory and payment first, since order-service calls them:

```bash
# Terminal 1
cd inventory-service
mvn spring-boot:run

# Terminal 2
cd payment-service
mvn spring-boot:run

# Terminal 3
cd order-service
mvn spring-boot:run

# Terminal 4
cd api-gateway
mvn spring-boot:run
```

Wait for all 4 to print `Started ...Application` before testing.

## Demo 1 — Happy path (everything succeeds)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUST-100",
        "productId": "PROD-001",
        "quantity": 2,
        "amount": 49.98
      }'
```

Expected: `"status": "CONFIRMED"`. Inventory for `PROD-001` drops by 2 units.

Check stock:
```bash
curl http://localhost:8082/api/inventory
```

## Demo 2 — Inventory failure (Saga stops before Payment is even called)

`PROD-003` only has 5 units in stock. Order more than that:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUST-101",
        "productId": "PROD-003",
        "quantity": 10,
        "amount": 199.90
      }'
```

Expected: `"status": "FAILED"`, reason `"Inventory reservation failed - insufficient stock"`.
Payment is never called — this is the Saga short-circuiting early.

## Demo 3 — Payment failure → Saga compensation (release inventory)

Force the payment service to fail:

```bash
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=true"
```

Now place an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUST-102",
        "productId": "PROD-002",
        "quantity": 1,
        "amount": 89.00
      }'
```

Expected: `"status": "COMPENSATED"`. Check `inventory-service` — stock for `PROD-002` is
unchanged because the Saga released what it reserved.

Turn the failure mode back off:
```bash
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=false"
```

## Demo 4 — Tripping the Circuit Breaker

With force-failure still `true`, fire 5+ orders quickly (the breaker's sliding window is 5
calls, failure threshold 50%):

```bash
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=true"

for i in 1 2 3 4 5 6; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"customerId":"CUST-200","productId":"PROD-001","quantity":1,"amount":24.99}'
  echo ""
done
```

Check the breaker's state directly on order-service's actuator endpoint:

```bash
curl http://localhost:8081/actuator/circuitbreakers
```

After enough failures you'll see `"state": "OPEN"` — at that point `PaymentClient.charge()`
**stops calling payment-service entirely** and immediately returns the fallback (`false`),
so the Saga compensates fast instead of waiting on a struggling downstream service.

After ~10 seconds (the configured `wait-duration-in-open-state`) it moves to `HALF_OPEN`
and tries a few test calls again.

## Key files to read first

- `order-service/.../service/OrderSagaOrchestrator.java` — the Saga steps + compensation logic
- `order-service/.../client/PaymentClient.java` — `@CircuitBreaker` + `@Retry` annotations
- `order-service/src/main/resources/application.yml` — Resilience4j tuning
- `api-gateway/src/main/resources/application.yml` — route definitions
