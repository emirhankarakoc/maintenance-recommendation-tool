# Maintenance Recommendation Tool

I built this for a problem I saw while working on cars. A technician has a vehicle's service history, but the history is often long and hard to check. This app helps find services that may be due.

## What happens

1. The user adds a repair order with the car and its current mileage.
2. The user sets service rules, such as every 30,000 miles or every 24 months.
3. An AI parser reads the service history and finds the last mileage and date for each service.
4. Java code compares those values with the rules. It saves the due services and their recommendations for the repair order.

The AI reads the text. The due/not-due decision is made in `ServiceRunManager.java`. The backend checks that the repair order belongs to the logged-in user and does not run the same order twice.

## Tech

Java 17, Spring Boot, MySQL, JPA, Spring Security/JWT, React, TypeScript, and an external AI API. Images use the AWS S3 SDK with **Cloudflare R2**. This is S3-compatible storage; the app is not hosted on AWS. There is no Kafka in this version.

## Where to look

- Backend rules: `backend-springboot/src/main/java/com/karakoc/sofra/services/ServiceRunManager.java`
- Repair orders: `backend-springboot/src/main/java/com/karakoc/sofra/ro/`
- Services and recommendations: `backend-springboot/src/main/java/com/karakoc/sofra/services/` and `serviceRecommendation/`
- React app: `frontend-reactjs/src/`

## Run locally

You need Java 17, Maven, MySQL, and Node.js. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `OPENAI_API_KEY`. The full list is in `backend-springboot/src/main/resources/application.properties`. Uploads also need your own `R2_*` settings.

```bash
cd backend-springboot
mvn spring-boot:run
```

In a second terminal, open `frontend-reactjs/` and run `npm install` and `npm run dev`. The frontend uses `VITE_API_URL`; it defaults to `http://localhost:8080`.

This is a public copy of my original service history project.
