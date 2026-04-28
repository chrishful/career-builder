# Career Builder

Lightweight Spring Boot application to help track job applications using a local spreadsheet and an external API.

## Key features
- Spring Boot service (Maven)
- Reads a job-tracking spreadsheet specified in configuration
- Integrates with an external API via a configured API key

## Requirements
- Java 17+ (or project JDK)
- Maven 3.6+
- IntelliJ IDEA 2025.2 (development)
- Windows (development instructions assume Windows paths)

## Configuration
Edit `src/main/resources/application.properties` (or provide environment variables) with the following keys:
- `spring.application.name` – application name
- `gemini.api.key` – external API key (keep secret)
- `job.hunt.spreadsheet.location` – absolute path to your `.xlsx` job tracker

Do not commit real secrets. Prefer one of:
- Use environment variables and map them in `application.properties` (e.g. `gemini.api.key=${GEMINI_API_KEY}`)
- Use a secrets manager or OS-level protected storage

## Build
From the project root:
```bash
mvn clean package
