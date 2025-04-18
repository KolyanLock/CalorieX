# CalorieX REST API

CalorieX is a Spring Boot RESTful API for calorie tracking.

---

## ⚡ Features
- REST API with `/api` prefix
- Swagger UI at `/swagger-ui`
- PostgreSQL integration
- Profiles: `dev` (includes test dataset preload)

---

## ⚙ Requirements

### For Docker Compose

You can choose **one** of the following:

- **[Docker Desktop](https://www.docker.com)**  
  Recommended for most platforms. It includes both Docker Engine and Docker Compose.


- **[Docker Engine](https://docs.docker.com/engine/install/) + [Docker Compose](https://docs.docker.com/compose/)**  
  Install separately, typically on Linux servers.

> ✅ Java, Maven, and PostgreSQL are **not required** when running via Docker Compose.

### For Local Development (without Docker)
- Java 21
- Maven 3.9+
- PostgreSQL installed locally or in Docker

---

## 🚀 Run with Docker Compose

1. Create a `.env` file with:
   ```env
   DB_USER=your_user
   DB_PASSWORD=your_password
   ```

2. Build and run:
   ```bash
   docker-compose up --build
   ```

3. Access the API:
    - API root: http://localhost:8080/api
    - Swagger UI: http://localhost:8080/swagger-ui

---

## 🪖 Run locally (without Docker)

1. Set up PostgreSQL locally and create a `caloriex_db` database.

2. Create `secret/secret.properties (replace port `5432` if your PostgreSQL uses a different one)`:
   ```properties
   DB_URL=localhost:5432/caloriex_db
   DB_USER=your_user
   DB_PASSWORD=your_password
   ```

3. Run with Maven:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The `dev` profile will preload the database with test data.

4. Alternatively, build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/CalorieX-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
   ```

---

## ✍ Notes
- The `application.properties` imports `secret/secret.properties` for DB config.
- Database is not initialized automatically unless you use the `dev` profile.

---

## 📄 Useful Paths
- Swagger docs: `/v1/api-docs`
- Swagger UI: `/swagger-ui`
- API base: `/api`

---

## 🧪 Testing

To run **unit tests**, use:
```bash  
  mvn test -P unit
```
To run integration tests, use:
```bash  
  mvn test -P integration
```
To run all tests (unit and integration), use:
```bash  
  mvn test
```

Note: RestAssured tests are currently under development.

---

## ⚖ License
Apache 2.0

---

## 📊 Version
Defined by `@project.version@` in Maven.

