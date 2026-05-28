Certainly. Here is the updated `README.md` formatted in Markdown, ready to copy and paste directly into your repository.

---

# Career Builder

A robust, backend-driven career tracking application designed to automate job application management, featuring a Spring Boot backend and a React-based frontend.

## System Architecture

The application is structured in distinct layers to ensure security, maintainability, and scalability.

### 1. Frontend Layer

* **Platform**: Deployed on Vercel.
* **Functionality**: Manages UI state, handles user authentication via Supabase, and communicates with the backend via RESTful endpoints using Basic token authentication.

### 2. Backend API Layer (Spring Boot)

* **Security Layer**:
* `WebhookAuthFilter`: Intercepts and validates requests, explicitly configured to ignore `OPTIONS` pre-flight requests to maintain CORS compliance.
* `GlobalCorsConfig`: Orchestrates cross-origin access, explicitly allowing communication from your Vercel-hosted frontend.
* `SecurityFilterChain`: Integrates CORS settings to act as a secure gateway for all incoming API traffic.


* **Service Layer**: Manages business logic and data transformation for job application entities.

### 3. Persistence Layer

* **Database**: Managed PostgreSQL instance hosted on Supabase.
* **Configuration**: Connection pooling via HikariCP, utilizing a consolidated `SPRING_DATASOURCE_URL` within the Render environment for seamless tenant identification.

## Requirements

* Java 17+
* Maven 3.6+
* Node.js 18+ (for frontend)
* Supabase account for PostgreSQL and Auth

## Configuration

Sensitive credentials must be managed via environment variables (in Render for backend, Vercel for frontend) and referenced in `application.properties`:

* `SPRING_DATASOURCE_URL`: Full PostgreSQL connection string (user, password, sslmode, etc.)
* `REACT_APP_BACKEND_URL`: URL of the deployed backend service
* `GEMINI_API_KEY`: API key for generative features
* `WEBHOOK_SECRET`: Secret for securing internal hooks

## Build & Deployment

### Backend

From the project root:

```bash
mvn clean package

```

Deployment is automated via Render, which consumes the environment variables defined in your project dashboard.

---
