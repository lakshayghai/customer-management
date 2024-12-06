# Customer Management Application

A Spring Boot application for managing customer information with phone number management capabilities.

## Features

- Create, read, update, and delete customers
- Manage multiple phone numbers per customer
- Phone number validation with country code support
- Email validation
- Database schema management with Liquibase
- OpenAPI documentation with Springdoc
- Docker support for easy deployment
- Spring Boot Actuator for monitoring

## Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven (for local development)

## Technology Stack

- Spring Boot 3.4.0
- PostgreSQL
- Docker
- Java 21
- Maven
- Lombok
- Hibernate
- Liquibase for database migrations
- Google libphonenumber for phone validation
- Springdoc OpenAPI for API documentation
- Spring Boot Buildpacks for container image creation
- Testcontainers for integration testing

## Getting Started

### Running with Docker Compose

1. Clone the repository:
```bash
git clone <repository-url>
cd customer-management
```

2. Build and run the application using Spring Boot Buildpacks:
```bash
./mvnw spring-boot:build-image
docker-compose up -d
```

The application will be available at:
- API Endpoints: `http://localhost:8080`
- API Documentation: `http://localhost:8080/swagger-ui.html`
- Actuator Endpoints: `http://localhost:8080/actuator`

### Database Schema Management

The application uses Liquibase for database schema management. Migration scripts are located in:
```
src/main/resources/db/changelog/
```

Migrations will be automatically applied when the application starts. You can find the current database state in the `databasechangelog` table.

### API Documentation

The API documentation is available through Springdoc OpenAPI. You can access:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`

### API Endpoints

#### Customer Management
- **Create Customer**: `POST /customers`
- **Get All Customers**: `GET /customers`
- **Get Customer by ID**: `GET /customers/{id}`
- **Update Customer**: `PUT /customers/{id}`
- **Delete Customer**: `DELETE /customers/{id}`


### Request/Response Examples

#### Create Customer Request
```json
{
  "firstName": "John",
  "middleName": "Robert",
  "lastName": "Doe",
  "emailAddress": "john.doe@example.com",
  "phoneNumbers": [
    {
      "phoneNumber": "+12345678901",
      "phoneType": "MOBILE",
      "countryCode": "US",
      "verified": false
    }
  ]
}
```

#### Success Response
```json
{
  "createdAt": "2024-12-02T19:11:54.200321",
  "modifiedAt": "2024-12-02T19:11:54.216798",
  "id": "dab86128-a528-4aa1-838e-8ccdd6f13f36",
  "firstName": "John",
  "middleName": null,
  "lastName": "Doe",
  "emailAddress": "john.doe@example.com",
  "phoneNumbers": [
    {
      "createdAt": "2024-12-02T19:11:54.213965",
      "modifiedAt": "2024-12-02T19:11:54.214052",
      "id": "eb06143a-5945-4734-a027-d684f648a4a6",
      "phoneNumber": "+12345678901",
      "phoneType": "MOBILE",
      "countryCode": "US",
      "verified": false
    }
  ]
}
```

## Validation Rules

### Customer
- First name and last name are required
- Email address must be valid and unique
- At least one phone number is required

### Phone Number
- Must be in E.164 format
- Type must be one of: MOBILE, HOME, or WORK
- Country code is required
- Verification status is required

## Development

### Local Development Setup

1. Comment ou this part in the docker-compose.yaml
```yml
  application:
    image: 'customer-management:0.0.1'
    depends_on:
      - postgres
    environment:
      - 'SPRING_PROFILES_ACTIVE=dev'
      - 'DATABASE_URL=jdbc:postgresql://postgres:5432/customerdb'
      - 'DATABASE_USERNAME=myuser'
      - 'DATABASE_PASSWORD=secret'
    ports:
      - '8080:8080'
    networks:
      - 'customer-management-default'
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

### Building

```bash
# Build JAR
./mvnw clean package

# Build Docker image using buildpacks
./mvnw spring-boot:build-image
```

### Running Tests

The project includes integration tests using Testcontainers:

```bash
./mvnw test
```

## Monitoring

Spring Boot Actuator endpoints are available at `/actuator/*`. Key endpoints include:
- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`
