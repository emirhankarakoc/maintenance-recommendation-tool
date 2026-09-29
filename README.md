# Maintenance Recommendation Tool

A technician-focused application for turning a vehicle's free-form service history into maintenance recommendations. I built it around a problem I encountered in automotive service: identifying what has already been done, then checking what may be due at the vehicle's current mileage and date.

## How it works

1. A user records a repair order with vehicle details and current mileage.
2. The user defines tracked services, mileage/month intervals, and recommendation text.
3. `POST /services/run` sends the service history and tracked service names to an AI parser. The parser returns the most recent mileage/date it can identify for each service.
4. `ServiceRunManager` evaluates each interval, collects due recommendations, and saves the result for the repair order. It checks ownership and prevents processing the same repair order twice.
5. The React client shows repair orders and the resulting recommendations.

The interval decision is application code in `backend-springboot/src/main/java/com/karakoc/sofra/services/ServiceRunManager.java`. AI is used to extract history data; it does not make the final due/not-due decision.

## Stack

- Java 17, Spring Boot 3.3, Spring Security/JWT, Spring Data JPA, MySQL
- React, TypeScript, Vite
- External AI API for history parsing
- AWS SDK `S3Client` configured for **Cloudflare R2** image storage (S3-compatible API, not an AWS deployment)

## Explore the code

| Area | Path |
| --- | --- |
| Interval evaluation and saved results | `backend-springboot/src/main/java/com/karakoc/sofra/services/ServiceRunManager.java` |
| Repair order API | `backend-springboot/src/main/java/com/karakoc/sofra/ro/` |
| Services and recommendations | `backend-springboot/src/main/java/com/karakoc/sofra/services/`, `serviceRecommendation/` |
| Client | `frontend-reactjs/src/` |

## Local setup

Requires Java 17, Maven, MySQL, Node.js, and your own AI API credentials.

1. Create a local MySQL database or let the configured JDBC URL create `dealership_rec`.
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `OPENAI_API_KEY` for the backend. Defaults and other integration settings are in `backend-springboot/src/main/resources/application.properties`. R2 upload requires your own `R2_*` values.
3. Start the backend and frontend:

```bash
cd backend-springboot
mvn spring-boot:run

# In another terminal, from the repository root:
cd frontend-reactjs
npm install
VITE_API_URL=http://localhost:8080 npm run dev
```

The public source has no deployment credentials. The existing backend test is a Spring context smoke test; interval decisions and the complete AI workflow do not have an automated end-to-end test here. This snapshot contains the original service-history application, without the later experimental Kafka addition.
