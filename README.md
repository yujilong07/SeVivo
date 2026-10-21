# Website Uptime Monitoring Application

## Abstract

This project implements a containerized client-server application for monitoring the availability of user-defined websites. A REST API allows clients to register and list monitoring targets, remove targets, and request availability statistics. A scheduled background process sends HTTP requests to registered targets and stores the results in a PostgreSQL database. Docker Compose coordinates the application and database containers, while a named volume preserves database files across container recreation.

## 1. Purpose and Scope

The application is intended to automate periodic availability checks. For each target, it records the check time, HTTP status code when a response is received, response time, success state, and an error message when a request fails.

The system has two principal workflows:

1. **Scheduled monitoring:** the scheduler retrieves registered targets, checks them through HTTP or HTTPS, and stores each result.
2. **Client requests:** an API client registers targets and requests aggregated statistics for a selected time interval.

The current implementation considers HTTP status codes from 200 through 299 successful. The uptime percentage is calculated as the proportion of successful recorded checks among all recorded checks in the requested interval. It does not estimate availability during intervals for which no check was recorded.

## 2. Architecture

The application is organized into the following packages:

| Package | Responsibility |
| --- | --- |
| `controller` | Defines REST endpoints and translates application exceptions into HTTP responses. |
| `dto` | Defines request and response data structures used by the REST API. |
| `model` | Defines JPA entities corresponding to database tables. |
| `repository` | Provides persistence operations through Spring Data JPA. |
| `scheduler` | Runs periodic checks of registered targets. |
| `service` | Implements target management, HTTP checks, and statistics calculation. |

The principal data entities are `Target`, stored in the `targets` table, and `HealthCheck`, stored in the `health_checks` table. Each health-check record refers to a target.

## 3. Requirements

- Java 21 and Maven, for building or running the application directly on the host.
- Docker Engine with the Docker Compose plugin, for containerized deployment.
- Network access from the application container to the monitored HTTP or HTTPS targets.

## 4. Deployment with Docker Compose

From the project root, start the application using the deployment script:

```bash
bash deploy.sh
```

The script builds the application image, starts the application and PostgreSQL services in detached mode, and prints their status. To invoke it directly as `./deploy.sh`, first make it executable:

```bash
chmod +x deploy.sh
./deploy.sh
```

The application is exposed at `http://localhost:8080` by default. Confirm that it is running by requesting the health endpoint:

```bash
curl -i http://localhost:8080/actuator/health
```

The expected response is HTTP 200 with an `UP` status. The root path `/` is not an application page and may return 404; use the API endpoints described below.

Docker Compose runs two services:

- `app`: the Spring Boot application, built from the multi-stage `Dockerfile`.
- `db`: the PostgreSQL database.

The services communicate over the `monitor_network` bridge network. The database uses the named volume `postgres_data` to persist its files. The application waits for the database health check before starting.

To inspect service status and logs:

```bash
docker compose ps
docker compose logs --tail=100 app db
```

To stop the services while preserving database data:

```bash
docker compose down
```

Do not add `--volumes` (or `-v`) to this command when database data must be retained; that option removes named volumes.

## 5. REST API

The base API path is `/api/targets`. Requests and responses use JSON unless otherwise stated.

### 5.1 Register a target

```http
POST /api/targets
Content-Type: application/json
```

Example using `curl`:

```bash
curl -i -X POST http://localhost:8080/api/targets \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com"}'
```

The request body must contain a full HTTP or HTTPS URL. A successful request returns HTTP 201 and a JSON representation containing the target identifier and URL.

### 5.2 List targets

```bash
curl -i http://localhost:8080/api/targets
```

This endpoint returns the registered targets as a JSON array.

### 5.3 Request target statistics

```http
GET /api/targets/{id}/stats
```

If no interval is specified, the API uses the preceding 24 hours. Optional `from` and `to` parameters accept ISO-8601 date-time values, preferably expressed in UTC:

```bash
curl -i \
  'http://localhost:8080/api/targets/1/stats?from=2026-10-19T00:00:00Z&to=2026-10-20T00:00:00Z'
```

The response includes the target identifier and URL, the selected interval, the total number of recorded checks, the number of successful checks, and the uptime percentage. A newly registered target may initially have no check records; the scheduler runs at the configured interval.

### 5.4 Delete a target

```bash
curl -i -X DELETE http://localhost:8080/api/targets/1
```

A successful deletion returns HTTP 204. A target that has associated health-check records may not be deletable under the current entity relationship and database constraints.

### 5.5 Error responses

The API returns structured error responses for selected failures:

| HTTP status | Meaning |
| --- | --- |
| 400 Bad Request | Invalid URL or invalid statistics interval. |
| 404 Not Found | The requested target does not exist. |
| 409 Conflict | A target with the same URL already exists. |

## 6. Adding a URL from a Client

`curl` is one way to send the registration request; it is not the only way. Any HTTP client capable of sending a JSON `POST` request can be used, including Postman or Insomnia. In such a client, send a `POST` request to `http://localhost:8080/api/targets`, set the `Content-Type` header to `application/json`, and use a body such as:

```json
{
  "url": "https://example.com"
}
```

A browser address bar can issue a simple `GET` request, for example to list targets, but it does not provide a convenient way to submit the JSON `POST` request required to add one.

## 7. Configuration

Configuration is provided through environment variables. Docker Compose supplies the database connection using the service name `db`.

| Variable | Default | Description |
| --- | --- | --- |
| `APP_PORT` | `8080` | Host port mapped to the application container. |
| `DB_USER` | `monitor` | PostgreSQL user. |
| `DB_PASSWORD` | `monitor` | PostgreSQL password. |
| `CHECK_INTERVAL_MS` | `60000` | Delay between scheduled check cycles, in milliseconds. |
| `CONNECT_TIMEOUT_MS` | `3000` | Connection timeout, in milliseconds. |
| `REQUEST_TIMEOUT_MS` | `5000` | HTTP request timeout, in milliseconds. |

The credentials shown above are development defaults. Set non-default credentials before using the deployment outside a local educational environment. Docker Compose reads variables from the process environment and, by default, from a `.env` file in the project directory. Do not commit secrets to version control.

## 8. Local Build and Execution

To build and verify the Maven project:

```bash
mvn clean verify
```

Running the JAR directly on the host requires a reachable PostgreSQL instance configured through `DB_URL`, `DB_USER`, and `DB_PASSWORD`. For example, with a local PostgreSQL service already configured:

```bash
mvn spring-boot:run
```

The default local JDBC URL is `jdbc:postgresql://localhost:5432/monitor`. For a complete deployment that also provisions PostgreSQL and persistent storage, use `bash deploy.sh`.

## 9. Containerization Files

- `Dockerfile` uses a Maven/JDK build stage to package the application and a Java 21 runtime image to run the resulting JAR.
- `docker-compose.yml` defines the application and PostgreSQL services, their network, database health check, port mapping, environment variables, and persistent volume.
- `deploy.sh` builds and starts the Compose application.
- `.dockerignore` excludes build output, IDE settings, Git metadata, and the local `.env` file from the Docker build context.

## 10. Limitations

- The API does not currently provide a browser-based graphical interface.
- Uptime is calculated from recorded checks only; missed or not-yet-run checks are not counted as failures.
- The scheduler checks targets sequentially within a cycle.
- Deleting a target with associated check history may fail because the current implementation does not define a history-retention or cascading-deletion policy.
- The Compose credentials are intended only as local defaults and should be changed for other environments.
