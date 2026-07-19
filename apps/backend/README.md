# CodeJudge Pro — Backend API Service

This is the Spring Boot backend service for **CodeJudge Pro**, built using Java 21, Spring Boot 3.5.x, Spring Security 6, PostgreSQL, Redis, and Flyway. It exposes RESTful APIs to handle student examinations, code evaluation sandboxes, notifications, and real-time leaderboard statistics.

---

## 🏛️ Layered & Modular Architecture

The backend follows a **strict layered architecture** where requests always flow in a single direction:

```
Client ──► Controller ──► Service ──► Repository ──► Database
```

### Module Structure
Every feature is implemented as an independent package under `com.ajith.codejudge` conforming to this uniform folder structure:
```text
com.ajith.codejudge.[module-name]/
│
├── controller/        # REST controllers (expose only DTOs, no JPA entities)
├── service/
│     ├── interfaces/  # Service interface definitions
│     └── impl/        # Service business logic implementation
├── repository/        # Spring Data JPA Repository classes
├── entity/            # JPA/Hibernate Entities
├── dto/               # Request/Response data wrappers
│     ├── request/
│     └── response/
├── mapper/            # MapStruct converters (DTO ◄─► Entity)
├── validator/         # Custom bean validation logic
├── exception/         # Module-specific exceptions
└── event/             # Application event listeners and triggers
```

---

## 🛠️ Configuration & Environments

Spring profiles are used to isolate database, redis, and mail configurations:
- **`dev` (Default)**: Intended for local development. Connects to PostgreSQL container on port `5435`, Redis on `6380`, and Mailpit on `1026`.
- **`test`**: Active during test runs. Employs an in-memory H2 database (with Flyway disabled) to run quick unit test checks.
- **`prod`**: Production settings with credentials passed exclusively via environment variables.

To launch the application under a specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
```bash
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

---

## 💾 Database Migrations (Flyway)

Flyway handles all schema operations. Auto-generation from Hibernate is disabled (`ddl-auto: validate`). 
- Migration files are stored under `src/main/resources/db/migration/`.
- File naming conventions: `V1__init_security_schema.sql`, `V2__init_exam_schema.sql`, etc.
- **Rule**: Existing migration scripts must never be modified once committed. Any future schema alterations require a new `V` migration script.

---

## 🔒 Coding Conventions & Guidelines

To ensure code maintainability, the following principles are enforced:
1. **Lombok Usage**: Never use Lombok `@Data` on JPA Entities. It can lead to circular dependencies, memory leaks, and issues with lazy loading fields. Instead, use explicit annotations:
   ```java
   @Getter
   @Setter
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   @Entity
   ```
2. **DTO Mappings**: Centralize all mapping conversions using **MapStruct**. Manual conversions should be minimized.
3. **Transaction Management**: Mark write/update service functions with `@Transactional`. Keep transactions out of the controller and repository layers.
4. **Field Injection**: Field injection (`@Autowired`) is strictly prohibited. Always use **Constructor Injection** (or Lombok `@RequiredArgsConstructor`).
5. **No System Output**: Debug logs must use SLF4J loggers. `System.out.println` is forbidden.

---

## 🧪 Testing & Verification

The project is configured to use Testcontainers for integration tests to verify database performance under real-world conditions.
- **Run all unit & integration tests**:
  ```bash
  ./mvnw clean test
  ```
- **Build compile output package**:
  ```bash
  ./mvnw clean package
  ```

---

## 📖 API Documentation (OpenAPI / Swagger UI)

API documentation is generated dynamically. Start the service with the `dev` profile and access:
- **Swagger Interactive Interface**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **JSON OpenAPI Contract**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
