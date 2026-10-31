# CodeTrack Backend

A RESTful backend for the **CodeTrack** application, developed using Kotlin, Spring Boot, Spring Data JPA and MySQL.

The backend manages coding problems and their revision schedules. It provides REST APIs for creating, retrieving, updating, deleting and filtering problems, as well as managing revision records.

---

## 1. Project Overview

CodeTrack helps users maintain a record of coding problems they solve and schedule revisions to improve their problem-solving skills.

The backend is responsible for:

- Managing coding problems.
- Managing revision schedules.
- Storing and retrieving application data from MySQL.
- Providing RESTful APIs.
- Validating incoming requests.
- Handling application errors and invalid requests.

### Architecture

```text
Android Application
        |
        | HTTP REST API
        v
Kotlin + Spring Boot
        |
        v
Controller Layer
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
Spring Data JPA
        |
        v
MySQL Database
```

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Backend programming language |
| Spring Boot 4.0.9 | Backend application framework |
| Spring Web MVC | REST API development |
| Spring Data JPA | Database operations |
| Hibernate | Object-relational mapping |
| MySQL 8.0 | Relational database |
| Gradle Kotlin DSL | Build and dependency management |
| Jakarta Validation | Request validation |
| Postman | API testing |
| IntelliJ IDEA | Backend development environment |
| JDK 21 | Java development environment |

---

## 3. Project Structure

```text
codetrack-backend/
│
├── gradle/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/codetrack_backend/
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── ProblemController.kt
│   │   │       │   └── RevisionController.kt
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── ProblemRequest.kt
│   │   │       │   ├── ProblemResponse.kt
│   │   │       │   ├── RevisionRequest.kt
│   │   │       │   └── RevisionResponse.kt
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── Problem.kt
│   │   │       │   └── Revision.kt
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.kt
│   │   │       │   └── ResourceNotFoundException.kt
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ProblemRepository.kt
│   │   │       │   └── RevisionRepository.kt
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ProblemService.kt
│   │   │       │   └── RevisionService.kt
│   │   │       │
│   │   │       └── CodetrackBackendApplication.kt
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   │
│   └── test/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

---

## 4. Database Design

**Database Name:** `codetrack`

The application uses two relational database tables:

### 4.1 Problems Table

Stores coding problem information.

| Column | Data Type | Description |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| title | VARCHAR(255) | Problem title |
| platform | VARCHAR(100) | Coding platform |
| topic | VARCHAR(100) | Problem topic |
| difficulty | VARCHAR(50) | Problem difficulty |
| problem_url | VARCHAR(500) | Problem URL |
| solved | BOOLEAN | Solved status |
| solved_date | DATE | Date solved |
| notes | TEXT | Additional notes |
| created_at | TIMESTAMP | Record creation time |

### 4.2 Revisions Table

Stores revision information associated with coding problems.

| Column | Data Type | Description |
|---|---|---|
| id | BIGINT | Primary key, auto-increment |
| problem_id | BIGINT | Foreign key referencing problems |
| revision_number | INT | Revision sequence number |
| revision_date | DATE | Scheduled revision date |
| completed | BOOLEAN | Revision completion status |
| completed_at | TIMESTAMP | Completion timestamp |
| notes | TEXT | Revision notes |

### 4.3 Relationship

- One problem can have multiple revisions.
- Each revision belongs to one problem.
- The `problem_id` column establishes the relationship.
- The foreign key uses `ON DELETE CASCADE`, so deleting a problem also deletes its associated revisions.

---

## 5. Database Setup

### Prerequisites

- MySQL Server 8.0 or later.
- A MySQL user with permission to create and access the database.

### Create the database

The SQL schema is available at:

`src/main/resources/schema.sql`

Execute the schema using MySQL Workbench or the MySQL command-line client.

```sql
CREATE DATABASE IF NOT EXISTS codetrack;

USE codetrack;
```

Execute the complete `schema.sql` file to create the `problems` and `revisions` tables.

Verify the tables:

```sql
USE codetrack;

SHOW TABLES;

DESCRIBE problems;

DESCRIBE revisions;
```

---

## 6. Application Configuration

The database connection is configured in:

`src/main/resources/application.properties`

```properties
spring.application.name=codetrack-backend

server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/codetrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

**Important:** The database password is supplied through the `DB_PASSWORD` environment variable instead of being stored directly in the project.

The Hibernate setting `ddl-auto=validate` checks whether the database schema matches the entity mappings. It does not automatically create or update the tables.

---

## 7. Running the Application

### Prerequisites

- JDK 21
- MySQL Server
- Gradle Wrapper included in the project

### Step 1: Configure the database password

Open PowerShell inside the `codetrack-backend` directory.

Set your MySQL password for the current terminal session:

```powershell
$env:DB_PASSWORD="your_mysql_password"
```

Replace the example value with your actual local MySQL password.

### Step 2: Build the project

```powershell
.\gradlew.bat build
```

### Step 3: Start the backend

```powershell
.\gradlew.bat bootRun
```

When the application starts successfully, the backend will be available at:

`http://localhost:8080`

### Base API URL

```text
http://localhost:8080/api
```

---

## 8. REST API Documentation

All endpoints use JSON request and response bodies where applicable.

### 8.1 Problem APIs

Base endpoint:

`/api/problems`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/problems` | Retrieve all problems |
| GET | `/api/problems/{id}` | Retrieve a problem by ID |
| POST | `/api/problems` | Create a new problem |
| PUT | `/api/problems/{id}` | Update an existing problem |
| DELETE | `/api/problems/{id}` | Delete a problem |
| GET | `/api/problems/filter/solved?solved=true` | Filter by solved status |
| GET | `/api/problems/filter/platform?platform=LeetCode` | Filter by platform |
| GET | `/api/problems/filter/topic?topic=Array` | Filter by topic |
| GET | `/api/problems/filter/difficulty?difficulty=Easy` | Filter by difficulty |

### Create a Problem

**Request**

```http
POST /api/problems
Content-Type: application/json
```

**Request Body**

```json
{
  "title": "Two Sum",
  "platform": "LeetCode",
  "topic": "Array",
  "difficulty": "Easy",
  "problemUrl": "https://leetcode.com/problems/two-sum/",
  "solved": true,
  "solvedDate": "2026-10-20",
  "notes": "Solved using HashMap"
}
```

**Expected Status:** `201 Created`

### Update a Problem

**Request**

```http
PUT /api/problems/1
Content-Type: application/json
```

Use the same JSON structure as the create request, with the updated values.

### Retrieve All Problems

**Request**

```http
GET /api/problems
```

### Retrieve a Problem by ID

**Request**

```http
GET /api/problems/1
```

### Delete a Problem

**Request**

```http
DELETE /api/problems/1
```

Deleting a problem also deletes its associated revisions through the configured foreign key cascade.

---

### 8.2 Revision APIs

Base endpoint:

`/api/revisions`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/revisions` | Retrieve all revisions |
| GET | `/api/revisions/{id}` | Retrieve a revision by ID |
| GET | `/api/revisions/problem/{problemId}` | Retrieve revisions for a problem |
| GET | `/api/revisions/date/{date}` | Retrieve revisions by date |
| POST | `/api/revisions/problem/{problemId}` | Create a revision for a problem |
| PUT | `/api/revisions/{id}` | Update a revision |
| PATCH | `/api/revisions/{id}/complete` | Mark a revision as completed |
| DELETE | `/api/revisions/{id}` | Delete a revision |

### Create a Revision

**Request**

```http
POST /api/revisions/problem/1
Content-Type: application/json
```

**Request Body**

```json
{
  "revisionNumber": 1,
  "revisionDate": "2026-10-25",
  "completed": false,
  "notes": "First revision"
}
```

**Expected Status:** `201 Created`

The `problemId` in the URL identifies the problem to which the revision belongs.

### Retrieve Revisions for a Problem

**Request**

```http
GET /api/revisions/problem/1
```

### Retrieve Revisions by Date

**Request**

```http
GET /api/revisions/date/2026-10-25
```

### Mark a Revision as Completed

**Request**

```http
PATCH /api/revisions/1/complete
```

The backend sets `completed` to `true` and records the completion timestamp.

### Update a Revision

**Request**

```http
PUT /api/revisions/1
Content-Type: application/json
```

**Request Body**

```json
{
  "revisionNumber": 1,
  "revisionDate": "2026-10-26",
  "completed": false,
  "notes": "Updated revision schedule"
}
```

### Delete a Revision

**Request**

```http
DELETE /api/revisions/1
```

---

## 9. Data Validation

The backend uses Jakarta Bean Validation to validate incoming requests.

### Problem Validation

- Problem title cannot be blank.
- Title cannot exceed 255 characters.
- Platform cannot exceed 100 characters.
- Topic cannot exceed 100 characters.
- Difficulty cannot exceed 50 characters.
- Problem URL cannot exceed 500 characters.

### Revision Validation

- Revision number must be greater than zero.

Invalid requests return:

`400 Bad Request`

---

## 10. Exception Handling

The application uses a global exception handler through `GlobalExceptionHandler.kt`.

### Supported Error Responses

| HTTP Status | Description |
|---|---|
| 400 Bad Request | Invalid request data or failed validation |
| 404 Not Found | Requested problem or revision does not exist |
| 500 Internal Server Error | Unexpected server-side error |

### Example: Resource Not Found

```json
{
  "timestamp": "2026-10-31T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Problem not found with id: 9999"
}
```

### Example: Validation Error

```json
{
  "timestamp": "2026-10-31T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "messages": [
    {
      "field": "title",
      "message": "Problem title is required"
    }
  ]
}
```

---

## 11. API Testing

The REST APIs can be tested using Postman.

### Testing Steps

1. Start the MySQL Server.
2. Configure the `DB_PASSWORD` environment variable.
3. Start the Spring Boot backend.
4. Open Postman.
5. Use `http://localhost:8080` as the base URL.
6. Select the appropriate HTTP method.
7. Enter the API endpoint.
8. Add the JSON request body when required.
9. Send the request and inspect the response.

The implemented Problem and Revision endpoints have been tested using Postman, including CRUD operations, filters, validation and not-found error handling.

---

## 12. Android Integration Notes

The Android application communicates with the backend using HTTP REST APIs.

The backend must be running while the Android application makes requests.

For Android Studio Emulator:

```text
http://10.0.2.2:8080/api
```

For a physical Android device, use the computer's local network IP address instead of `localhost`, provided both devices can reach each other on the network.

Native Android HTTP clients do not require browser CORS configuration.

---

## 13. Current Implementation Scope

### Implemented

- MySQL database integration.
- Problem management.
- Revision management.
- CRUD operations.
- Problem filtering by solved status.
- Problem filtering by platform.
- Problem filtering by topic.
- Problem filtering by difficulty.
- Request validation.
- Global exception handling.
- REST API testing using Postman.

### Not currently implemented

- User registration and authentication.
- JWT-based security.
- User-specific problem records.
- Automated database migrations.
- Production deployment configuration.

---

## 14. GitHub Repository

**Repository:** [DIVINE-7/CodeTrack-Android](https://github.com/DIVINE-7/CodeTrack-Android)

**Backend Branch:** `backend`

The Spring Boot backend is maintained in the `codetrack-backend` directory.

---

## 15. Developer

**Name:** Ankit Kumar Mohanty

**Role:** Backend and Database Developer

**Project:** CodeTrack

**University:** Centurion University of Technology and Management

---