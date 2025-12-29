# API Project - Spring Boot

A Spring Boot REST API project with PostgreSQL database integration.

## Tech Stack

- **Java**: 17
- **Spring Boot**: 3.2.1
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Dependencies**:
  - Spring Web
  - Spring Data JPA
  - PostgreSQL Driver
  - Lombok

## Project Structure

```
api-project-springboot/
├── src/
│   ├── main/
│   │   ├── java/com/example/apiproject/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data repositories
│   │   │   └── ApiProjectApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/apiproject/
├── docker/
│   └── docker-compose.yml        # PostgreSQL + pgAdmin
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose

### Database Setup

1. Start PostgreSQL and pgAdmin using Docker:
```bash
cd docker
docker-compose up -d
```

2. Access pgAdmin at http://localhost:5050
   - Email: `admin@admin.com`
   - Password: `admin`

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

### Test the API

Health check endpoint:
```bash
curl http://localhost:8080/api/health
```

## Database Connection

The application connects to PostgreSQL with these settings:
- **URL**: jdbc:postgresql://localhost:5432/devdb
- **Username**: devuser
- **Password**: devpass

## Available Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Build package
mvn clean package

# Run application
mvn spring-boot:run
```

## Development

The project includes a sample:
- **Entity**: `User.java` - demonstrates JPA and Lombok
- **Repository**: `UserRepository.java` - demonstrates Spring Data JPA
- **Controller**: `HealthController.java` - demonstrates REST API

Start building your API by adding more entities, repositories, services, and controllers!
