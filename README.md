# Microservices
## API Gateway · Saga Orchestration · Circuit Breaker · Spring Cloud Config · Eureka Discovery

A fully runnable multi-module Spring Boot project demonstrating **5 microservices design
patterns** working together in a single e-commerce checkout flow.

---

## Patterns at a glance

| Pattern | Where it lives | What it solves |
|---|---|---|
| **API Gateway** | `api-gateway` | Single client entry point, `lb://` routing |
| **Saga Orchestration** | `order-service` | Distributed transactions with compensating rollback |
| **Circuit Breaker + Retry** | `order-service` → `PaymentClient` | Fail fast instead of cascading failure |
| **Config Server** | `config-server` + GitHub repo | Centralised properties, hot reload without restart |
| **Service Discovery + Load Balancing** | `eureka-server` + all clients | Dynamic routing, no hardcoded URLs, auto-scaling |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│               Config Server  :8888                               │
│    Serves all service properties from GitHub on startup          │
│    github.com/Jeev19/ConfigurationProperties                     │
└───────────────────────┬──────────────────────────────────────────┘
                        │ fetch config on startup
        ┌───────────────▼──────────────────────────────────────┐
        │              Eureka Server  :8761                    │
        │   Live registry of all running service instances     │
        │   Dashboard → http://localhost:8761                  │
        └───┬───────────────┬──────────────────┬──────────────┘
            │               │                  │   all services register here
     ┌──────▼──────┐ ┌──────▼──────┐  ┌───────▼──────┐
     │ Inventory   │ │  Payment    │  │    Order     │
     │ Svc :8082   │ │  Svc :8083  │  │    Svc :8081 │
     │ reserve /   │ │  charge /   │  │  Saga Orch   │
     │ release     │ │  simulate   │  │  + Circ Brkr │
     └──────▲──────┘ └──────▲──────┘  └──────▲───────┘
            │               │                 │
            └───────────────┴─────────────────┘
                    lb:// (Eureka resolved)
                            ▲
                   ┌────────┴────────┐
                   │   API Gateway   │
                   │    :8080        │
                   │  lb:// routes   │
                   └────────▲────────┘
                            │
                         Client
```

### Checkout Saga Flow

```
Client → Gateway → Order Service (Saga starts)
                        │
                        ├─── Step 1: reserve stock  ──▶ Inventory Service
                        │         [success] ──────────────────────────────────────────┐
                        │         [fail]    ──▶ FAILED (stock insufficient)            │
                        │                                                              │
                        └─── Step 2: charge payment ──▶ [Circuit Breaker] ──▶ Payment Service
                                  [success] ──▶ CONFIRMED ◀────────────────────────────┘
                                  [fail]    ──▶ compensate: release stock ──▶ COMPENSATED
```

---

## Project layout

```
microservices-saga-demo/
│
├── config-server/              (port 8888)  Spring Cloud Config Server
│   └── src/main/resources/
│       ├── application.yml                  Config server setup (git backend)
│       └── config/                          Fallback native configs (dev only)
│           ├── order-service.yml
│           ├── inventory-service.yml
│           ├── payment-service.yml
│           └── api-gateway.yml
│
├── eureka-server/              (port 8761)  Eureka Discovery Server + Dashboard
│
├── order-service/              (port 8081)  Saga Orchestrator + Circuit Breaker
│   └── key files:
│       ├── service/OrderSagaOrchestrator.java    ← Saga steps + compensation logic
│       ├── client/PaymentClient.java              ← @CircuitBreaker + @Retry
│       └── controller/ConfigRefreshController.java ← @RefreshScope live config view
│
├── inventory-service/          (port 8082)  Stock reserve / release (Saga participant)
│
├── payment-service/            (port 8083)  Simulated payment gateway (failure toggle)
│
├── api-gateway/                (port 8080)  Spring Cloud Gateway (lb:// routes)
│
└── README.md
```

Each service is an **independent Maven project** with its own `pom.xml`.
All environment-specific properties live in the **GitHub config repo** — not inside the services.

---

## Prerequisites

- Java 17+
- Maven 3.6+
- Ports `8080`, `8081`, `8082`, `8083`, `8761`, `8888` free on your machine

---

## How to run — start strictly in this order

> **Config Server must start first.** All other services fetch their properties from it on
> startup — if it isn't ready, services will fail to boot.

```bash
# Terminal 1 — Config Server (always first)
cd config-server
mvn spring-boot:run

# Terminal 2 — Eureka Discovery Server
cd eureka-server
mvn spring-boot:run

# Terminal 3 — Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 4 — Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 5 — Order Service
cd order-service
mvn spring-boot:run

# Terminal 6 — API Gateway (always last)
cd api-gateway
mvn spring-boot:run
```

**Verify everything is up before running demos:**

| Check | URL | Expected |
|---|---|---|
| Config Server health | http://localhost:8888/actuator/health | `"status":"UP"` |
| Order Service config loaded | http://localhost:8888/order-service/default | Full YAML properties |
| Eureka dashboard | http://localhost:8761 | All 4 services registered |
| Gateway health | http://localhost:8080/actuator/health | `"status":"UP"` |

---

## API Documentation (Swagger / OpenAPI 3)

Every service exposes OpenAPI 3.0 docs via springdoc-openapi.

**Aggregated Swagger UI at the Gateway** — one page, dropdown to switch between all 3 service APIs:

```
http://localhost:8080/swagger-ui.html
```

**Per-service Swagger UI:**

| Service | Swagger UI | Raw OpenAPI JSON |
|---|---|---|
| Order Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Inventory Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Payment Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |

All endpoints are annotated with `@Operation`, `@ApiResponses`, and `@Schema` — valid
OpenAPI 3.0 output, importable directly into Postman or Insomnia.

---

## Demo 1 — Happy path

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

**Expected:** `"status": "CONFIRMED"` — stock reserved, payment charged, order confirmed.

```bash
# Verify stock dropped by 2
curl http://localhost:8082/api/inventory
```

---

## Demo 2 — Inventory failure (Saga stops before Payment is called)

`PROD-003` has only 5 units. Order 10:

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

**Expected:** `"status": "FAILED"` — insufficient stock. Payment is never called.
This is the Saga short-circuiting at Step 1 without needing any compensation.

---

## Demo 3 — Payment fails → Saga compensates (releases inventory)

```bash
# Step 1: Force payment service to fail
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=true"

# Step 2: Place an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUST-102",
        "productId": "PROD-002",
        "quantity": 1,
        "amount": 89.00
      }'
```

**Expected:** `"status": "COMPENSATED"` — inventory was reserved, payment failed, Order
Service ran the compensating transaction (release stock). Check inventory — `PROD-002`
quantity is unchanged.

```bash
# Reset payment service
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=false"
```

---

## Demo 4 — Tripping the Circuit Breaker

```bash
# Force payment to fail
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=true"

# Fire 6 orders rapidly to exceed the sliding window failure threshold
for i in 1 2 3 4 5 6; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"customerId":"CUST-200","productId":"PROD-001","quantity":1,"amount":24.99}'
  echo ""
done

# Check Circuit Breaker state — look for "state": "OPEN"
curl http://localhost:8081/actuator/circuitbreakers
```

**Expected:** After enough failures (sliding window: 5 calls, failure threshold: 50%),
the breaker trips to `OPEN`. All subsequent Payment calls short-circuit immediately to
the fallback — no network call made, Saga compensates instantly.

After ~10 seconds (`wait-duration-in-open-state`) the breaker moves to `HALF_OPEN`
and tests a few calls. On success it resets to `CLOSED`.

```bash
# Reset
curl -X PUT "http://localhost:8080/api/payments/simulate-failure?enabled=false"
```

---

## Demo 5 — Config Server: view live properties

All properties are fetched from the GitHub config repo on startup.
Verify what Order Service actually loaded:

```bash
# Config as seen by the Config Server (straight from GitHub)
curl http://localhost:8888/order-service/default

# Live values currently active inside the running Order Service
curl http://localhost:8081/api/config
```

---

## Demo 6 — Hot reload config without restart

1. Edit `order-service.yml` in [github.com/Jeev19/ConfigurationProperties](https://github.com/Jeev19/ConfigurationProperties)
   (e.g. change `failure-rate-threshold` from `50` to `80`)
2. Commit and push
3. Trigger refresh on the running service — **no restart needed:**

```bash
curl -X POST http://localhost:8081/actuator/refresh
```

4. Verify the new value is live:

```bash
curl http://localhost:8081/api/config
```

`@RefreshScope` re-injects all `@Value` fields from the updated config. The service
keeps running — zero downtime, zero restart.

---

## Demo 7 — Eureka load balancing with multiple instances

```bash
# Start a second Inventory Service instance on a different port
cd inventory-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"
```

Both instances appear in the Eureka dashboard under `INVENTORY-SERVICE`.
Order Service's `@LoadBalanced` RestTemplate now round-robins between ports 8082 and 8085
automatically — no config change anywhere.

Stop one instance — Eureka evicts it within ~30 seconds, traffic stops going there.

---

## Key URLs reference

| URL | What it shows |
|---|---|
| http://localhost:8761 | Eureka dashboard — all registered instances |
| http://localhost:8888/order-service/default | Raw config for order-service from GitHub |
| http://localhost:8888/inventory-service/default | Raw config for inventory-service |
| http://localhost:8888/payment-service/default | Raw config for payment-service |
| http://localhost:8888/api-gateway/default | Raw config for api-gateway |
| http://localhost:8081/api/config | Live property values inside running Order Service |
| http://localhost:8081/actuator/circuitbreakers | Circuit Breaker state (CLOSED / OPEN / HALF_OPEN) |
| http://localhost:8081/actuator/circuitbreakerevents | Circuit Breaker event history |
| http://localhost:8080/swagger-ui.html | Aggregated Swagger UI (all 3 services) |

---

## Config repo on GitHub

All service properties live at **[github.com/Jeev19/ConfigurationProperties](https://github.com/Jeev19/ConfigurationProperties)**:

```
ConfigurationProperties/
├── api-gateway.yml         ← lb:// routes, Swagger aggregation config
├── order-service.yml       ← Resilience4j config, service URLs, JPA, H2
├── inventory-service.yml   ← DB config, actuator
└── payment-service.yml     ← actuator, logging
```

This is the **single source of truth** for all environment-specific config.
To change any property across all running instances: edit → push → `POST /actuator/refresh`.

For private repos, use a GitHub PAT via environment variable — never hardcode it:
```yaml
# config-server/application.yml
git:
  uri: https://github.com/Jeev19/ConfigurationProperties
  password: ${GITHUB_TOKEN}
```
```bash
GITHUB_TOKEN=ghp_yourtoken mvn spring-boot:run
```

---

## How Eureka + Load Balancing works

1. Every service registers itself with Eureka on startup (name, IP, port, health URL)
2. API Gateway routes use `lb://service-name` — resolved through Eureka at request time
3. Order Service's `RestTemplate` is annotated `@LoadBalanced` — same mechanism for
   internal calls to Inventory and Payment
4. Spring Cloud LoadBalancer picks an instance (Round Robin by default)
5. Dead instances are evicted from the registry after missing heartbeats

---

## Notes

- H2 in-memory DB is used everywhere — **zero external infrastructure** needed to run
- Self-preservation is disabled in Eureka (`enable-self-preservation: false`) for clean
  local dev — re-enable it in production
- To switch Config Server to **native (local)** mode for offline dev, change
  `config-server/application.yml` to use `spring.profiles.active: native` — configs
  are then read from `config-server/src/main/resources/config/` instead of GitHub