# Event-Driven Order Processing System

An event-driven microservices system for order processing using Spring Boot and Apache Kafka. It consists of 6 services: an API Gateway, Eureka service discovery, and 4 domain services for orders, inventory, payments, and notifications.

## Table of Contents

- [System Architecture](#system-architecture)
- [Services Overview](#services-overview)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Services](#running-the-services)
- [Event Flow](#event-flow)
- [API Endpoints](#api-endpoints)
- [Technology Stack](#technology-stack)
- [Contributing](#contributing)
- [License](#license)

## System Architecture

```
┌────────────────────────────────────────────────────���────────┐
│                      Client Applications                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                    HTTP/REST
                         │
        ┌────────────────▼────────────────┐
        │        API Gateway (Port 8080)  │
        │       (Zuul / Spring Cloud)     │
        └────────────────┬────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
    ┌───▼────┐    ┌─────▼────┐    ┌─────▼────┐
    │ Order   │    │ Inventory │   │ Payment  │
    │ Service │    │ Service   │   │ Service  │
    │(8081)   │    │ (8082)    │   │ (8083)   │
    └───┬────┘    └─────┬────┘    └─────┬────┘
        │                │               │
        │    ┌───────────┴───────────┬───┘
        │    │                       │
    ┌───▼────────────────────────────▼────┐
    │     Apache Kafka Message Broker      │
    │      (Event Stream Platform)         │
    └────┬──────────────────────────┬─────┘
         │                          │
    ┌────▼──────┐             ┌────▼──────┐
    │ Eureka    │             │ Notification
    │ Registry  │             │ Service
    │ (8761)    │             │ (8084)
    └───────────┘             └───────────┘
```

## Services Overview

### 1. API Gateway (Port 8080)
- Single entry point for all client requests
- Request routing to appropriate microservices
- Load balancing and failover handling
- Authentication and rate limiting
- Built with Spring Cloud Gateway or Netflix Zuul

### 2. Order Service (Port 8081)
- Manages order creation and lifecycle
- Publishes `OrderCreated`, `OrderConfirmed`, `OrderShipped` events
- Consumes payment and inventory confirmation events
- Stores order data in PostgreSQL/MySQL

### 3. Inventory Service (Port 8082)
- Manages product inventory
- Publishes `InventoryReserved`, `InventoryReleased` events
- Consumes `OrderCreated` events for stock validation
- Updates inventory levels based on order events

### 4. Payment Service (Port 8083)
- Processes payment transactions
- Publishes `PaymentProcessed`, `PaymentFailed` events
- Consumes `OrderCreated` events to initiate payments
- Integrates with payment gateways (Stripe, PayPal, etc.)

### 5. Notification Service (Port 8084)
- Sends email and SMS notifications to users
- Consumes all order-related events
- Notifies customers of order updates
- Maintains notification history and templates

### 6. Eureka Service Discovery (Port 8761)
- Service registry for dynamic service discovery
- Enables inter-service communication without hardcoded URLs
- Health check monitoring
- Automatic service registration/deregistration

## Prerequisites

- **Java**: JDK 11 or higher
- **Maven**: 3.6+
- **Docker**: (Optional, for containerization)
- **Docker Compose**: (For running Kafka and other dependencies)
- **Apache Kafka**: 2.8+ or higher
- **MySQL/PostgreSQL**: 5.7+ or 10+

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/MAROOTS/event-driven-order-processing.git
cd event-driven-order-processing
```

### 2. Start Infrastructure (Docker Compose)

```bash
docker-compose up -d
```

This starts:
- Apache Kafka & Zookeeper
- MySQL Database
- Eureka Server
- Redis Cache (optional)

### 3. Build All Services

```bash
mvn clean install
```

Or build individual services:

```bash
mvn clean install -DskipTests
```

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Kafka Configuration
KAFKA_BROKER=localhost:9092
KAFKA_ZOOKEEPER=localhost:2181

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=order_processing
DB_USER=root
DB_PASSWORD=password

# Eureka Configuration
EUREKA_HOST=localhost
EUREKA_PORT=8761

# Service Ports
ORDER_SERVICE_PORT=8081
INVENTORY_SERVICE_PORT=8082
PAYMENT_SERVICE_PORT=8083
NOTIFICATION_SERVICE_PORT=8084
API_GATEWAY_PORT=8080
```

### Application Properties

Each service has an `application.yml` configuration file. Example for Order Service:

```yaml
spring:
  application:
    name: order-service
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: order-service-group
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
  datasource:
    url: jdbc:mysql://localhost:3306/order_processing
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

server:
  port: 8081
```

## Running the Services

### Start Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

Access Eureka Dashboard: `http://localhost:8761`

### Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### Start Individual Microservices

In separate terminal windows:

```bash
cd order-service
mvn spring-boot:run

cd inventory-service
mvn spring-boot:run

cd payment-service
mvn spring-boot:run

cd notification-service
mvn spring-boot:run
```

### Or use Docker Compose

```bash
docker-compose up
```

## Event Flow

### Order Processing Flow

```
1. Client submits order via API Gateway
   ↓
2. Order Service receives request → Creates Order (Status: PENDING)
   ├─→ Publishes: OrderCreated Event
   ↓
3. Inventory Service receives OrderCreated Event
   ├─→ Validates stock availability
   ├─→ Publishes: InventoryReserved or InventoryFailed Event
   ↓
4. Payment Service receives OrderCreated Event
   ├─→ Processes payment
   ├─→ Publishes: PaymentProcessed or PaymentFailed Event
   ↓
5. Order Service receives confirmation events
   ├─→ Updates Order Status (CONFIRMED or FAILED)
   ├─→ Publishes: OrderConfirmed or OrderFailed Event
   ↓
6. Notification Service receives all events
   └─→ Sends confirmation email/SMS to customer
```

## API Endpoints

### Order Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create a new order |
| GET | `/api/v1/orders/{id}` | Get order details |
| GET | `/api/v1/orders` | List all orders |
| PUT | `/api/v1/orders/{id}` | Update order status |
| DELETE | `/api/v1/orders/{id}` | Cancel order |

### Inventory Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/inventory` | List all products |
| GET | `/api/v1/inventory/{productId}` | Get product stock |
| POST | `/api/v1/inventory/{productId}/reserve` | Reserve stock |
| POST | `/api/v1/inventory/{productId}/release` | Release reservation |

### Payment Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process payment |
| GET | `/api/v1/payments/{id}` | Get payment status |
| POST | `/api/v1/payments/{id}/refund` | Refund payment |

### Notification Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/notifications` | List notifications |
| GET | `/api/v1/notifications/{id}` | Get notification details |

## Technology Stack

- **Framework**: Spring Boot 2.7+
- **API**: Spring Web MVC, Spring REST
- **Message Queue**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Gateway**: Spring Cloud Gateway / Zuul
- **Database**: MySQL / PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Container**: Docker
- **Monitoring**: Spring Boot Actuator, Prometheus, Grafana (optional)

## Development Guidelines

### Adding a New Service

1. Create a new Maven module
2. Add Spring Boot and Kafka dependencies
3. Define Kafka topics and event classes
4. Implement business logic
5. Register with Eureka
6. Add to docker-compose.yml

### Event Naming Convention

- Event names should follow the pattern: `{Entity}{Action}Event`
- Examples: `OrderCreatedEvent`, `PaymentProcessedEvent`, `InventoryReservedEvent`

## Troubleshooting

### Services not registering with Eureka
- Check Eureka server is running on port 8761
- Verify `eureka.client.service-url.defaultZone` in each service config

### Kafka connection issues
- Ensure Kafka broker is running on `localhost:9092`
- Check Zookeeper is running on `localhost:2181`
- Verify `spring.kafka.bootstrap-servers` configuration

### Database connection errors
- Verify MySQL/PostgreSQL is running and accessible
- Check database credentials in application.yml
- Ensure database exists and migrations have run

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

**Last Updated**: June 2026
