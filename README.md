# Social Media Platform — Backend

A production-grade RESTfull backend for a social media platform, built with a modular domain-driven architecture, fully containerized with Docker, and secured with Keycloak. Continuous delivery to **Azure Cloud via GitHub Actions** is currently being configured and will be available in the near future.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Modules](#modules)
- [API Documentation](#api-documentation)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Deployment](#deployment)
- [Roadmap](#roadmap)

---

## Overview

This backend powers a full-featured social media platform supporting user registration and authentication, content creation and management, a social graph (follow system), real-time notifications, and media storage. The service is secured with **Keycloak** as the identity provider, integrates with **Supabase** for media storage, and uses both relational and document-oriented databases to match the access patterns of each domain.

---

## Architecture

The codebase is organized around clearly separated vertical slices, each owning its own API layer, application services, domain model, and persistence. Cross-cutting concerns such as security, async execution, caching, and scheduling are isolated in dedicated configuration packages.

```
SocialMediaApp/
├── Configurations/       # Security, Redis, WebSocket, Swagger, Async
├── Content/              # Posts, comments, likes, stories, media
├── Messaging/            # Real-time chat (WebSocket / STOMP)
├── Notification/         # In-app and email notifications
├── Profile/              # User profile management
├── Scheduling/           # Quartz-based job scheduling
├── SocialGraph/          # Follow / unfollow relationships
├── Storage/              # Supabase storage integration
├── Upload/               # Upload gateway and session management
├── User/                 # Registration, authentication, Keycloak provisioning
├── Validation/           # Custom constraint annotations
└── Shared/               # Shared utilities and exceptions
```

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Build Tool | Apache Maven |
| Primary Database | PostgreSQL 15 — Spring Data JPA / Hibernate |
| Document Store | MongoDB — Spring Data MongoDB |
| Cache | Redis — Spring Data Redis |
| Identity Provider | Keycloak 26 — OAuth2 / JWT |
| Object Storage | Supabase Storage |
| Scheduler | Quartz |
| Real-time | WebSocket with STOMP over SockJS |
| Email | Spring Mail + Thymeleaf templates |
| API Documentation | SpringDoc OpenAPI 3 — Swagger UI |
| Reactive HTTP Client | Spring WebFlux — WebClient |
| Mapping | MapStruct |
| Boilerplate Reduction | Lombok |
| Containerization | Docker + Docker Compose |

---

## Modules

### User & Authentication

Handles user registration and lifecycle. On sign-up, the service provisions an account in Keycloak and persists a local user record in PostgreSQL. All protected endpoints require a valid JWT issued by the configured Keycloak realm. User activity is tracked asynchronously.

### Content

The richest module in the application, covering:

- **Post lifecycle** — create (as draft), publish, update, delete, and restore posts with full media attachment support.
- **Post visibility** — toggle posts between public and private.
- **Post scheduling** — schedule posts for future publication using Quartz. Scheduled jobs can be cancelled at any time and a notification email is sent to the author when a scheduled post goes live.
- **Cursor-based pagination** — bidirectional feed traversal using keyset pagination for efficient, stable page results.
- **Comments** — threaded comments with like support.
- **Likes** — like and unlike posts and comments, with optimistic concurrency control.
- **Stories** — ephemeral content with view tracking and configurable audience settings.
- **Media** — images and videos attached to posts or stories, with thumbnail generation.

### Social Graph

Manages follower / following relationships between users. Includes complete unit tests and integration tests backed by Testcontainers and a real PostgreSQL instance.

### Upload

A two-phase upload gateway that keeps the main server out of the media data path:

1. The client requests a signed upload URL for the target Supabase bucket.
2. The client uploads the file directly to Supabase.
3. Supabase notifies the backend via a signed webhook, which the server verifies before finalizing the upload session.

Upload sessions are tracked in Redis and expire automatically. File-type validation, size enforcement, and MIME-type checking are applied before issuing signed URLs.

### Storage

Internal abstraction over the Supabase Storage REST API. Provides signed download URLs, folder management, and temporary-to-permanent content promotion following a successful upload finalization.

### Notification

Sends both in-app and email notifications for relevant social events:

- A new follower notification when a user is followed.
- Content interaction notifications (likes, comments).
- A scheduled post publication email rendered with a Thymeleaf HTML template.

Notification preferences can be managed per-user through the notification settings service.

### Scheduling

A dedicated module that wraps Quartz to persist and execute time-based content jobs. Supports dynamic scheduling, rescheduling, and cancellation of post publication jobs without a server restart.

### Configurations

- **Security** — Spring Security configured as an OAuth2 resource server, validating JWTs against the Keycloak JWKS endpoint.
- **WebSocket** — STOMP message broker with a JWT-authenticated channel interceptor that authorizes connections before the handshake completes.
- **Redis** — Jackson-based serialization configuration for Redis caching.
- **Async** — custom thread pool executor for non-blocking notification dispatch and activity tracking.
- **Swagger** — OpenAPI 3 documentation with Keycloak Authorization Code flow integration.

---

## API Documentation

When the application is running, the interactive Swagger UI is served from:

```
http://localhost:8080/UI.html
```

Authentication is integrated directly into the UI using the **OAuth2 Authorization Code flow** backed by Keycloak. Click **Authorize**, and the UI will redirect you through the Keycloak login page. Once authenticated, the access token is automatically attached to every request — no manual token copying required. All protected endpoints are fully testable from the browser.

> **Note — Client-side only endpoints:** The create post, edit post, and create story workflows cannot be exercised through Swagger. They rely on client-side JavaScript (media chunking, upload session coordination, signed URL handling) and must be accessed through their dedicated HTML interfaces. These are reachable directly from the navigation buttons in `UI.html`.

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This starts the following containers:

| Service | Port |
|---|---|
| PostgreSQL | `5432` |
| Redis | `6379` |
| MongoDB | `27017` |
| Keycloak | `8180` |

# ⚙️ 2. Configure the Application

Set up your environment variables in a **`.env`** file *(recommended for local development)* or in **`application.properties`** / deployment secrets for production.

<br>

### 🗄️ Database Configuration

```env
DB_URL=           # JDBC connection string (e.g. jdbc:postgresql://host:5432/db)
DB_USERNAME=      # Database user
DB_PASSWORD=      # Database password
```

<br>

### 🔐 Authentication & Security

```env
JWT_ISSUER=       # Token issuer URL (e.g. your Keycloak realm URL)
KEYCLOAK_CLIENT=  # Keycloak client ID
KEYCLOAK_SECRET=  # Keycloak client secret
```

<br>

### 🗃️ Supabase Storage & Webhooks

```env
SUPABASE_URL=            # Your Supabase project URL
SUPABASE_APIKEY=         # Supabase service role API key
SUPABASE_PUBLIC_BUCKET=  # Name of the public storage bucket
SUPABASE_PRIVATE_BUCKET= # Name of the private storage bucket
SUPABASE_WEBHOOK_SECRET= # Secret used to verify webhook payloads
```

<br>

### 📧 Email Service (Brevo)

```env
BREVO_APIKEY=        # Brevo (formerly Sendinblue) API key
BREVO_EMAIL_SENDER=  # Verified sender email address
```

<br>

### 📁 File Storage Paths

```env
FILE_MOVE=           # Destination path for permanent file storage
TEMPORARY_FILE_MOVE= # Staging path for temporary file uploads
```

<br>

> [!WARNING]
> Never commit your `.env` file to version control. Add it to `.gitignore` and use a secrets manager or CI/CD environment variables for production deployments.
### 3. Build and Run

```bash
./mvnw clean package -DskipTests
java -jar target/SocialMediaApp-0.0.1-SNAPSHOT.jar
```

The application will be available at `http://localhost:8080`.

### Docker Build

```bash
docker build -t social-media-backend .
docker run -p 8080:8080 social-media-backend
```

---

## Running Tests

The project includes both unit tests and integration tests.

**Unit tests** — pure JVM, no external dependencies:

```bash
./mvnw test -Dgroups=unit
```

**Integration tests** — require Docker. Testcontainers spins up a real PostgreSQL instance automatically:

```bash
./mvnw verify
```

Integration tests cover the social graph follow service and user account management workflows.

---

## Deployment

Automated deployment via **GitHub Actions to Azure Cloud** is currently being configured and will be available in the near future. The planned pipeline will:

1. Run the full test suite on every push to `main`.
2. Build a Docker image and push it to **Azure Container Registry**.
3. Deploy the new image to **Azure Container Apps**.
4. Validate the deployment with a health-check probe before marking the release as successful.

---

## Roadmap

The following features are actively under development:

| Feature | Status |
|---|---|
| Real-time messaging (WebSocket / STOMP) | 🔄 In development |
| Stories — full viewer and expiry pipeline | 🔄 In development |
| Profile search | 🔄 In development |
| GitHub Actions → Azure CD pipeline | 🔄 Being configured |
