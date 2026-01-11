# Peaks-Hikes Service - Technical Documentation

## Overview

The **Peaks-Hikes Service** is a microservice responsible for managing geographic data related to peaks and hiking trails within the HIKU hiking application. It stores spatial data using PostGIS, provides REST and gRPC endpoints for peak and trail information retrieval, and implements automatic geospatial linking between peaks and trails based on proximity. The service supports trail imports from GPX files and serves as the authoritative source for peak metadata consumed by the Badge Service.

## Table of Contents

1. [Architecture](#architecture)
2. [Technology Stack](#technology-stack)
3. [Database Schema](#database-schema)
4. [API Endpoints](#api-endpoints)
5. [Authentication & Authorization](#authentication--authorization)
6. [gRPC Integration](#grpc-integration)
7. [Health Checks](#health-checks)
8. [Configuration](#configuration)
9. [Deployment](#deployment)
10. [Local Development](#local-development)
11. [Error Handling](#error-handling)
12. [Troubleshooting](#troubleshooting)

---

## Architecture

### Key Components

- **REST Controllers**: Handle HTTP requests for peaks and trails (PeakController, TrailController)
- **gRPC Server**: Exposes PeakService on port 9090 for Badge Service integration
- **Repository Layer**: Encapsulates database operations using JPA with PostGIS spatial extensions
- **Entities**: JPA-mapped domain models (Peak, Trail) with geospatial geometry fields
- **Database Triggers**: Automatic linking of peaks to nearby trails based on 50m proximity
- **Trail Import Service**: Processes GPX files to populate trails table

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java (Eclipse Temurin) | 17+ |
| **Build Tool** | Maven | 3.9 |
| **Framework** | KumuluzEE | 4.1.0 |
| **JPA Provider** | Hibernate | 5.6.15.Final |
| **Database** | PostgreSQL | 14+ |
| **Migration** | Flyway | 9.16.1 |
| **Authentication** | MicroProfile JWT | 2.1 |
| **Spatial Database** | PostGIS | 3.x |
| **gRPC** | gRPC Java | 1.58.0 |
| **Protobuf** | Protocol Buffers | 3.x |
| **Containerization** | Docker | - |
| **Orchestration** | Kubernetes (via Helm) | - |

## Database Schema

### Schema: `peaks_hikes_service`

#### Table: `trails`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | SERIAL | PRIMARY KEY | Auto-increment trail ID |
| `name` | VARCHAR | NOT NULL | Trail name |
| `length_km` | FLOAT | - | Trail length in kilometers |
| `geometry` | GEOMETRY(LineString, 4326) | NOT NULL | Path geometry (WGS84) |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| `source_file` | TEXT | - | Originating GPX/Geo source |

**Indexes**: GIST on `geometry` for spatial queries

#### Table: `peaks`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | SERIAL | PRIMARY KEY | Auto-increment peak ID |
| `name` | VARCHAR | NOT NULL | Peak name |
| `territory` | VARCHAR | - | Region/territory |
| `latitude` | DOUBLE PRECISION | NOT NULL | Latitude (WGS84) |
| `longitude` | DOUBLE PRECISION | NOT NULL | Longitude (WGS84) |
| `elevation_m` | DOUBLE PRECISION | - | Elevation in meters |
| `geom` | GEOMETRY(Point, 4326) | GENERATED ALWAYS STORED | Point from `longitude`,`latitude` |

**Indexes**: GIST on `geom` for spatial queries

#### Table: `trails_peaks`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `trail_id` | INTEGER | NOT NULL, FK → `trails(id)` ON DELETE CASCADE | Linked trail |
| `peak_id` | INTEGER | NOT NULL, FK → `peaks(id)` ON DELETE CASCADE | Linked peak |

**Primary Key**: (`trail_id`, `peak_id`) — composite key preventing duplicates

**Functions**: `link_peak_to_nearby_trails(p_peak_id)` — links peaks to trails within 50 m (`ST_DWithin`).

**Triggers**: `trg_peak_insert` (after insert on `peaks` auto-links nearby trails), `trg_trail_insert` (after insert on `trails` auto-links nearby peaks).

These triggers implement robust connectivity between peaks and trails by relying solely on precise geospatial proximity (location data) rather than textual similarity of peak and trail names. This reduces manual effort needed to curate relationships, ensuring consistent, automatic linking whenever new peaks or trails are inserted.

### Database Access Layer Architecture

The service implements a clean **Data Access Object (DAO) pattern** with clear separation of concerns:

**Structure:**
- **`/db/models/`** — JPA entity classes (`Peak.java`, `Trail.java`) annotated with `@Entity`, mapping directly to PostgreSQL tables with PostGIS geometry columns
- **`/db/dao/`** — Data Access Objects (`PeakDao.java`, `TrailDao.java`) encapsulating JPQL queries for CRUD operations
- **`EntityManagerProducer.java`** — CDI producer that manages the JPA `EntityManager` lifecycle, creating request-scoped instances from the persistence unit

**Database Access Flow:**
```
Controller → Service → DAO (@Inject EntityManager) → JPA/Hibernate → PostgreSQL/PostGIS
```

**Architectural Benefits:**

1. **Separation of Concerns** — Business logic (controllers/services) is isolated from database access code. DAOs handle only queries and persistence operations.

2. **Testability** — DAOs can be mocked or stubbed in unit tests; EntityManager injection allows test-scoped persistence contexts.

3. **Reusability** — Common queries (`findById`, `searchByName`) are centralized in DAOs and shared across multiple controllers.

4. **Type Safety** — JPA entities provide compile-time validation of field names and types, reducing runtime SQL errors.

5. **Transaction Management** — EntityManager lifecycle is managed by CDI container, ensuring proper transaction boundaries and connection pooling without manual resource management.

6. **PostGIS Integration** — JPA entities use `@Column(columnDefinition = "geometry(Point,4326)")` to seamlessly map PostGIS spatial types (Point, LineString) to Java objects (JTS library), enabling spatial queries without raw SQL.

This layered approach follows **Repository pattern** best practices, making the codebase maintainable, scalable, and adaptable to future database changes (e.g., switching ORM providers or adding caching layers).

### Database Migration

Database schema is managed using **Flyway** migrations located in `src/main/resources/db/migration/`.

Rules when working with migrations:
- Each migration has to follow the naming convention: VX__\<short name\>, where X is the next number that hasn't been used yet.
- Database migrations must be idempotent. You must not delete already existing and applied migrations.

Migrations run automatically via Kubernetes Job (see `helm/templates/migrate-job.yaml`).

---

## Configuration

### Application Configuration

Configuration file: `src/main/resources/config.yaml`

### JPA Configuration

Configuration file: `src/main/resources/META-INF/persistence.xml`

## Authentication & Authorization

### JWT-Based Authentication

The Peaks-Hikes Service uses **MicroProfile JWT** with Keycloak as the identity provider. All REST endpoints require `@RolesAllowed("user")` for authenticated access.

## gRPC Integration

### gRPC: Badge Service ↔ Peaks-Hikes Service Communication

#### Architectural Overview

The Badge Service uses gRPC for synchronous, type-safe communication with the Peaks-Hikes Service to enrich user logbook entries with peak metadata. This inter-service communication requires three foundational components:

1. **Shared Protobuffer Contract** — Both services must compile the same `.proto` schema (`PeakService`), ensuring identical message structures and RPC method signatures. This contract defines the `GetPeakById` RPC endpoint and response fields (peak name, elevation, territory).

2. **Server Implementation** — Peaks-Hikes Service exposes a gRPC server on port 9090 with `PeakServiceImpl`, which implements the service logic REST endpoint call for peak details.

3. **Client Stub & Connection Pool** — Badge Service instantiates a `PeakServiceBlockingStub` on application startup, creating a persistent managed channel to the Peaks-Hikes Service. This stub handles serialization, HTTP/2 transport, and synchronous call blocking.

#### Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                     Badge Service                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ When displaying "Your Logbook" page:                    │ │
│  │  1. Controller retrieves user's peak entries (IDs only) │ │
│  │  2. For each peak_id, invoke PeakServiceClient          │ │
│  │  3. Blocking call waits for gRPC response               │ │
│  │  4. Peak name, elevation, territory enriched in UI      │ │
│  └─────────────────────────────────────────────────────────┘ │
│            │                                                 │
│            │ gRPC Request (HTTP/2 binary)                    │
│            │ PeakRequest{peak_id: 5}                         │
│            ▼                                                 │
└──────────────────────────────────────────────────────────────┘
                      Network (port 9090)
┌──────────────────────────────────────────────────────────────┐
│                  Peaks-Hikes Service                         │
│            ▲                                                 │
│            │ gRPC Response (HTTP/2 binary)                   │
│            │ PeakResponse{id, name, territory, elevation_m}  │
│            │                                                 │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ GrpcServer listens on :9090                             │ │
│  │ PeakServiceImpl.getPeakById(peak_id) executes:          │ │
│  │  1. Query peaks table via REST endpoint /getpeaks       │ │
│  │  2. Build PeakResponse{name, territory, elevation_m}    │ │
│  │  3. Return via gRPC                                     │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

#### Key Architectural Requirements

- **Environment-Based Service Discovery** — Client reads `PEAKS_GRPC_HOST` and `PEAKS_GRPC_PORT` from environment, enabling Kubernetes pod-to-pod networking without hardcoded IPs.
- **Resilience & Fallback** — On communication failure, client returns partial response with `found=false`, preventing logbook display failures.
- **Type Safety & Versioning** — Changes to peak data structure (e.g., adding a new field) require updating `.proto`, recompiling both services, and coordinated deployment to avoid incompatibility.
- **HTTP/2 Multiplexing** — Single connection handles multiple concurrent peak requests efficiently, reducing per-call overhead compared to REST.

## Deployment

#### Database Migrator Image

**Dockerfile**: `Dockerfile.migrator`

Runs Flyway migrations as a Kubernetes Job.

---

### Kubernetes (Helm)

#### Chart Structure

```
helm/
├── Chart.yaml              # Chart metadata
├── values-dev.yaml         # Development values
└── templates/
    ├── _helpers.tpl        # Template helpers
    ├── deployment.yaml     # Main application deployment
    ├── service-clusterip.yaml  # Internal service
    ├── service-nodeport.yaml   # External service (dev)
    ├── migrate-job.yaml    # Database migration job
    ├── secret.yaml         # Database credentials
    └── secretsproviderclass.yaml  # Azure Key Vault integration
```

---

### CI/CD Pipelines

#### Test Environment Pipeline

**File**: `.github/workflows/test-build-deploy.yaml`

**Triggers**:
- Push to `test` branch

**Steps**:
1. Checkout code
2. Build Docker images (app + migrator)
3. Push to Azure Container Registry (ACR)
4. Deploy to AKS test environment using ArgoCD

---

#### Production Promotion Pipeline

**File**: `.github/workflows/prod-promote.yaml`

**Triggers**:
- Manual workflow dispatch with image tag selection

**Steps**:
1. Pull images from test ACR
2. Retag images for production
3. Push to production ACR
4. Deploy to AKS production environment

Secrets used in the GitHub Actions workflows are saved as secrets in our GitHub Organization. Secrets used for deployment on the Azure cluster are provided by our Azure Key Vault.

---

## Local Development

Building images and deployment for local development is handled by Skaffold. By running the command **skaffold dev** in the root folder of the repository in a terminal window will make Skaffold automatically build and deploy the service to your local Minikube cluster. Skaffold watches your local files and when you save a change, Skaffold automatically applies it.  

### Prerequisites

#### Required Tools & Services
- **Java 17+**
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Docker Desktop**
- **Keycloak**
- **RabbitMQ**
- **Minikube**
- **Skaffold**

#### Other requirements
- Docker Desktop is running,
- Minikube cluster is running on Docker Desktop,
- The database is deployed on your local cluster,
- The Traefik ingress controller is deployed on your local cluster,
- Keycloak is deployed on your local cluster.

### Steps performed by Skaffold
- Builds docker image for microservice,
- Builds docker image for database migrations,
- Deploys both images,
- Portforwards NodePort to the default port setting.



## Error Handling

### Common HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| `200 OK` | Request successful | GET peaks, trails |
| `201 Created` | Resource created | POST new trail |
| `204 No Content` | Success, no response body | - |
| `400 Bad Request` | Invalid request data | Malformed geometry |
| `401 Unauthorized` | Missing or invalid JWT | No Authorization header |
| `403 Forbidden` | Insufficient permissions | - |
| `404 Not Found` | Resource not found | Peak/trail doesn't exist |
| `500 Internal Server Error` | Server error | Database connection failed |
| `503 Service Unavailable` | Service unhealthy | Health check failed |

### Exception Handling

The service uses JAX-RS exception handling:

- **Validation errors**: Return `400 Bad Request`
- **Resource not found**: Return `404 Not Found` (explicit in delete)
- **Database errors**: Return `500 Internal Server Error`
- **Authentication errors**: Return `401 Unauthorized`

## Contact

For questions or issues, contact the development team.


**Last Updated**: January 11, 2026  
**Version**: 0.1.0
