# Task & Ticket Management System API

A comprehensive Spring Boot REST API for managing tasks, users, and comments with role-based access control (RBAC).

---

## 📋 Table of Contents

- [Overview](#overview)
- [Technologies Used](#technologies-used)
- [System Roles](#system-roles)
- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Authentication Endpoints](#authentication-endpoints)
  - [User Management Endpoints](#user-management-endpoints)
  - [Task Management Endpoints](#task-management-endpoints)
  - [Comment Endpoints](#comment-endpoints)
- [Data Models](#data-models)
- [Role-Based Access Control](#role-based-access-control)
- [Error Handling](#error-handling)
- [Swagger API Documentation](#swagger-api-documentation)

---

## Overview

This Task & Ticket Management System provides a secure, role-based platform for managing tasks within an organization. The system enforces strict access controls based on user roles, ensuring proper separation of duties.

### Key Features

- ✅ **JWT-based Authentication** - Secure stateless authentication
- ✅ **Role-Based Access Control** - ADMIN, MANAGER, and USER roles with distinct permissions
- ✅ **Task Management** - Create, assign, update, and track tasks
- ✅ **Comment System** - Collaborative commenting on tasks
- ✅ **User Management** - Admin controls for user administration
- ✅ **Audit Tracking** - Track who created and updated tasks
- ✅ **Dedicated Mapper Layer** - Clean separation of mapping logic from business logic
- ✅ **Role-Based DTO Responses** - Each role gets a tailored response shape (Admin, Manager, User)
- ✅ **Shared Security Utility** - Centralized `SecurityUtils` for authenticated user resolution

---

## Technologies Used

- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **JPA/Hibernate** - ORM for database operations
- **PostgreSQL** - Relational database
- **Lombok** - Reduce boilerplate code
- **Jakarta Validation** - Input validation
- **Swagger/OpenAPI** - API documentation

---

## Architecture & Design Pattern

### Layered Architecture

The application follows a **layered architecture** with dedicated Mapper and Utility layers for clean separation of concerns:

```
┌─────────────────────────────────────┐
│     Controller Layer (REST API)     │  ← Handles HTTP requests/responses
├─────────────────────────────────────┤
│      Service Layer (Business)       │  ← Business logic & validation
├──────────────┬──────────────────────┤
│ Mapper Layer │   Utility Layer      │  ← DTO conversion & shared helpers
│ (UserMapper, │   (SecurityUtils)    │
│  TaskMapper, │                      │
│  CommentMap) │                      │
├──────────────┴──────────────────────┤
│   Repository Layer (Data Access)    │  ← Database operations
└─────────────────────────────────────┘
            ↓
      PostgreSQL Database
```

**Mapper Layer** (`com.example.apiproject.mapper`):
- `UserMapper` — converts `User` entity to `UserProfileDTO`, `UserAdminDTO`, or `UserSummaryDTO`
- `TaskMapper` — converts `Task` entity to `TaskResponseDTO` with null-safe user references
- `CommentMapper` — converts `Comment` entity to `CommentResponseDTO`

**Utility Layer** (`com.example.apiproject.util`):
- `SecurityUtils` — single source of truth for `getCurrentUser()`, injected into all services

### Strategy Design Pattern

The application implements the **Strategy Pattern** for task assignment to allow flexible assignment algorithms.

**Implementation:**

```java
// Strategy Interface
public interface TaskAssignmentStrategy {
    void assign(Task task, Long userId);
}

// Concrete Strategy
@Component
public class ManualAssignmentStrategy implements TaskAssignmentStrategy {
    @Override
    public void assign(Task task, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(...));
        task.setAssignedTo(user);
    }
}
```

**Benefits:**
- ✅ **Open/Closed Principle** - Easy to add new assignment strategies (e.g., auto-assign, round-robin) without modifying existing code
- ✅ **Single Responsibility** - Each strategy focuses on one assignment algorithm
- ✅ **Extensibility** - Future assignment strategies can be plugged in without changing the service layer

**Current Implementation:**
- `TaskAssignmentStrategy` - Interface defining the assignment contract
- `ManualAssignmentStrategy` - Concrete implementation for manual user assignment by MANAGER

**Future Extensions:**
- `AutoAssignmentStrategy` - Auto-assign to least busy user
- `RoundRobinStrategy` - Distribute tasks evenly across users
- `PriorityBasedStrategy` - Assign based on user skill level and task priority

---

## System Roles

### ADMIN
**Oversight and User Management**
- **Can**: View all tasks, view all users, create/update/delete users, change user roles, activate/deactivate users
- **Cannot**: Create tasks, assign tasks, update tasks

### MANAGER
**Task Coordination and Execution**
- **Can**: Create tasks, assign tasks to USERS, view **all** tasks, update **all** tasks, comment on tasks
- **Cannot**: Manage users, assign tasks to ADMIN or MANAGER roles

### USER
**Task Execution**
- **Can**: View assigned tasks, update status of assigned tasks, comment on assigned tasks
- **Cannot**: View other users' tasks, create tasks, assign tasks, update task details (only status)

---

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd api-project-springboot
   ```

2. **Set up PostgreSQL Database**
   
   Create a database named `taskdb`:
   ```sql
   CREATE DATABASE taskdb;
   ```

3. **Configure Database Credentials** (if needed)
   
   The default configuration uses:
   - **Database**: `taskdb`
   - **Username**: `postgres`
   - **Password**: `postgres`
   
   If your PostgreSQL credentials are different, update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
   spring.datasource.username=your_postgres_username
   spring.datasource.password=your_postgres_password
   ```

4. **Run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

### Automatic Admin User Creation

The application uses **DataInitializer** to automatically create a default admin user on first startup if no admin exists.

**Default Credentials:**
- **Email**: `admin@system.com`
- **Password**: `admin123`

The credentials are configured in `application.properties`:
```properties
admin.default.name=System Admin
admin.default.email=admin@system.com
admin.default.password=admin123
```

**On First Startup**, you'll see this in the logs:
```
==========================================
⚠️  DEFAULT ADMIN ACCOUNT CREATED
==========================================
Email: admin@system.com
Password: admin123
⚠️  PLEASE CHANGE THIS PASSWORD IMMEDIATELY!
==========================================
```

**Security Note:** Change the default admin password immediately after first login using the `/api/users/me/password` endpoint.

**To Use Custom Admin Credentials:**
Update the values in `application.properties` before first run:
```properties
admin.default.name=Your Name
admin.default.email=your.email@company.com
admin.default.password=YourSecurePassword
```

---

## Authentication

### How It Works

1. **Login** with email and password → Receive JWT token
2. **Include token** in all subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Token Expiration

Tokens expire after a configured duration. Client must handle 401 Unauthorized responses and re-authenticate.

---

## API Endpoints

### Base URL
```
http://localhost:8080
```

---

## Authentication Endpoints

### 1. Login

**POST** `/api/auth/login`

**Access**: Public (no authentication required)

**Description**: Authenticate user and receive JWT token

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "Login successful"
  },
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Bad credentials",
  "data": null,
  "error": "Authentication failed",
  "timestamp": "2026-02-04T12:00:00"
}
```

---

### 2. Register User

**POST** `/api/auth/register`

**Access**: **ADMIN ONLY** (requires authentication)

**Description**: Create a new user with specified role

**Headers**:
```
Authorization: Bearer <admin-jwt-token>
```

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "TempPassword123",
  "role": "USER"
}
```

**Fields**:
- `name` (required): User's full name
- `email` (required): Unique email address
- `password` (required): Password (will be hashed)
- `role` (optional): `USER`, `MANAGER`, or `ADMIN` (defaults to `USER`)

**Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Email already in use",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

## User Management Endpoints

### 3. Get All Users

**GET** `/api/users`

**Access**: **ADMIN, MANAGER** (with role-based filtering)

**Description**: Retrieve users visible to current role
- **ADMIN**: Sees all users (ADMIN, MANAGER, USER) with full details
- **MANAGER**: Sees only active USER role accounts (for task assignment)

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Response for ADMIN** (200 OK) — returns `UserAdminDTO`:
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Admin User",
        "email": "admin@example.com",
        "role": "ADMIN",
        "isActive": true,
        "createdAt": "2026-01-01T10:00:00"
      },
      {
        "id": 2,
        "name": "Manager User",
        "email": "manager@example.com",
        "role": "MANAGER",
        "isActive": true,
        "createdAt": "2026-01-05T14:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "error": null,
  "timestamp": "2026-02-09T12:00:00"
}
```

**Response for MANAGER** (200 OK) — returns `UserSummaryDTO` (only active USERs):
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 3,
        "name": "John User",
        "email": "john@example.com"
      },
      {
        "id": 4,
        "name": "Jane User",
        "email": "jane@example.com"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "error": null,
  "timestamp": "2026-02-09T12:00:00"
}
```

---

### 4. Get Current User Profile

**GET** `/api/users/me`

**Access**: **All authenticated users**

**Description**: Get profile of the currently logged-in user

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Response** (200 OK) — returns `UserProfileDTO`:
```json
{
  "id": 3,
  "name": "John User",
  "email": "john@example.com",
  "role": "USER"
}
```

---

### 6. Update Password

**PUT** `/api/users/me/password`

**Access**: **All authenticated users**

**Description**: Change own password

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Request Body**:
```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewSecurePassword456"
}
```

**Response** (200 OK):
```json
"Password updated successfully"
```

**Error Response** (400 Bad Request):
```json
"Current password is incorrect"
```

---

### 7. Update User Role

**PUT** `/api/users/{id}/role?newRole={role}`

**Access**: **ADMIN ONLY**

**Description**: Change a user's role

**Headers**:
```
Authorization: Bearer <admin-jwt-token>
```

**Path Parameters**:
- `id`: User ID (Long)

**Query Parameters**:
- `newRole`: New role (`USER`, `MANAGER`, or `ADMIN`)

**Example**:
```
PUT /api/users/5/role?newRole=MANAGER
```

**Response** (200 OK):
```json
{
  "id": 5,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "MANAGER",
  "isActive": true,
  "createdAt": "2026-01-15T09:00:00"
}
```

---

### 8. Toggle User Status

**PUT** `/api/users/{id}/status?isActive={true|false}`

**Access**: **ADMIN ONLY**

**Description**: Activate or deactivate a user

**Headers**:
```
Authorization: Bearer <admin-jwt-token>
```

**Path Parameters**:
- `id`: User ID (Long)

**Query Parameters**:
- `isActive`: `true` or `false`

**Example**:
```
PUT /api/users/5/status?isActive=false
```

**Response** (200 OK):
```json
{
  "id": 5,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "MANAGER",
  "isActive": false,
  "createdAt": "2026-01-15T09:00:00"
}
```

---

## Task Management Endpoints

### 9. Create Task

**POST** `/api/tasks`

**Access**: **MANAGER ONLY**

**Description**: Create a new task and optionally assign it to a USER

**Headers**:
```
Authorization: Bearer <manager-jwt-token>
```

**Request Body**:
```json
{
  "title": "Implement Login Feature",
  "description": "Create JWT-based authentication for the API",
  "priority": "HIGH",
  "assignedToUserId": 3
}
```

**Fields**:
- `title` (required): Task title
- `description` (optional): Detailed description
- `priority` (required): `LOW`, `MEDIUM`, or `HIGH`
- `assignedToUserId` (optional): ID of USER to assign task to (must be USER role)

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": 1,
    "title": "Implement Login Feature",
    "description": "Create JWT-based authentication for the API",
    "status": "OPEN",
    "priority": "HIGH",
    "createdAt": "2026-02-04T12:00:00",
    "assignedUserId": 3,
    "assignedUserName": "John User",
    "createdByUserId": 2,
    "createdByUserName": "Manager User",
    "updatedByUserId": null,
    "updatedByUserName": null
  },
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Tasks can only be assigned to a user",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

### 10. Get All Tasks

**GET** `/api/tasks`

**Access**: **All authenticated users** (filtered by role)

**Description**: Get tasks visible to current user

**Visibility Rules**:
- **ADMIN**: Sees ALL tasks
- **MANAGER**: Sees ALL tasks
- **USER**: Sees only tasks assigned to them

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Tasks retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Implement Login Feature",
      "description": "Create JWT-based authentication for the API",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "createdAt": "2026-02-04T12:00:00",
      "assignedUserId": 3,
      "assignedUserName": "John User",
      "createdByUserId": 2,
      "createdByUserName": "Manager User",
      "updatedByUserId": 3,
      "updatedByUserName": "John User"
    }
  ],
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

### 11. Get Task by ID

**GET** `/api/tasks/{id}`

**Access**: **All authenticated users** (with role-based restrictions)

**Access Rules**:
- **ADMIN**: Can view any task
- **MANAGER**: Can view any task
- **USER**: Can only view tasks assigned to them

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `id`: Task ID (Long)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Task retrieved successfully",
  "data": {
    "id": 1,
    "title": "Implement Login Feature",
    "description": "Create JWT-based authentication for the API",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "createdAt": "2026-02-04T12:00:00",
    "assignedUserId": 3,
    "assignedUserName": "John User",
    "createdByUserId": 2,
    "createdByUserName": "Manager User",
    "updatedByUserId": 3,
    "updatedByUserName": "John User"
  },
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied: Users can only view tasks assigned to them",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

### 12. Update Task

**PUT** `/api/tasks/{id}`

**Access**: **MANAGER, USER** (with different permissions)

**Update Permissions**:
- **ADMIN**: Cannot update tasks (read-only)
- **MANAGER**: Can update all fields of any task
- **USER**: Can only update `status` of tasks assigned to them

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `id`: Task ID (Long)

**Request Body** (MANAGER):
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

**Request Body** (USER - status only):
```json
{
  "status": "DONE"
}
```

**Fields** (all optional):
- `title`: New title (MANAGER only)
- `description`: New description (MANAGER only)
- `status`: `OPEN`, `IN_PROGRESS`, or `DONE`
- `priority`: `LOW`, `MEDIUM`, or `HIGH` (MANAGER only)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Task updated successfully",
  "data": {
    "id": 1,
    "title": "Updated Title",
    "description": "Updated description",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "createdAt": "2026-02-04T12:00:00",
    "assignedUserId": 3,
    "assignedUserName": "John User",
    "createdByUserId": 2,
    "createdByUserName": "Manager User",
    "updatedByUserId": 2,
    "updatedByUserName": "Manager User"
  },
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (403 Forbidden - USER updating non-status field):
```json
{
  "success": false,
  "message": "Access denied: Users can only update task status",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (403 Forbidden - ADMIN trying to update):
```json
{
  "success": false,
  "message": "Access denied: Admin cannot update tasks",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

### 13. Assign Task

**PUT** `/api/tasks/{id}/assign?userId={userId}`

**Access**: **MANAGER ONLY**

**Description**: Assign a task to a USER (not MANAGER or ADMIN)

**Headers**:
```
Authorization: Bearer <manager-jwt-token>
```

**Path Parameters**:
- `id`: Task ID (Long)

**Query Parameters**:
- `userId`: ID of USER to assign task to (must be USER role)

**Example**:
```
PUT /api/tasks/1/assign?userId=3
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Task assigned successfully",
  "data": {
    "id": 1,
    "title": "Implement Login Feature",
    "description": "Create JWT-based authentication for the API",
    "status": "OPEN",
    "priority": "HIGH",
    "createdAt": "2026-02-04T12:00:00",
    "assignedUserId": 3,
    "assignedUserName": "John User",
    "createdByUserId": 2,
    "createdByUserName": "Manager User",
    "updatedByUserId": 2,
    "updatedByUserName": "Manager User"
  },
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Tasks can only be assigned to a user",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

## Comment Endpoints

### 14. Create Comment

**POST** `/api/tasks/{taskId}/comments`

**Access**: **All authenticated users who can view the task**

**Description**: Add a comment to a task

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `taskId`: Task ID (Long)

**Request Body**:
```json
{
  "message": "I've started working on this task."
}
```

**Response** (201 Created) — returns `CommentResponseDTO`:
```json
{
  "success": true,
  "message": "Comment created successfully",
  "data": {
    "id": 1,
    "message": "I've started working on this task.",
    "commentedById": 3,
    "commentedByName": "John User",
    "createdAt": "2026-02-04T12:30:00"
  },
  "error": null,
  "timestamp": "2026-02-04T12:30:00"
}
```

---

### 15. Get Task Comments

**GET** `/api/tasks/{taskId}/comments`

**Access**: **All authenticated users who can view the task**

**Description**: Retrieve all comments for a task

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `taskId`: Task ID (Long)

**Response** (200 OK) — returns paginated `CommentResponseDTO`:
```json
{
  "success": true,
  "message": "Comments retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "message": "I've started working on this task.",
        "commentedById": 3,
        "commentedByName": "John User",
        "createdAt": "2026-02-04T12:30:00"
      },
      {
        "id": 2,
        "message": "Please prioritize this task.",
        "commentedById": 2,
        "commentedByName": "Manager User",
        "createdAt": "2026-02-04T13:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "error": null,
  "timestamp": "2026-02-04T13:00:00"
}
```

---

### 16. Delete Comment

**DELETE** `/api/tasks/{taskId}/comments/{commentId}`

**Access**: **Comment author or ADMIN**

**Description**: Delete a comment

**Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `taskId`: Task ID (Long)
- `commentId`: Comment ID (Long)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Comment deleted successfully",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T13:15:00"
}
```

---

## Data Models

### Role Enum
```
ADMIN, MANAGER, USER
```

### TaskStatus Enum
```
OPEN, IN_PROGRESS, DONE
```

### TaskPriority Enum
```
LOW, MEDIUM, HIGH
```

### User Entity
| Field | Type | Description |
|-------|------|-------------|
| id | Long | User ID (auto-generated) |
| name | String | Full name |
| email | String | Unique email address |
| password | String | Hashed password (bcrypt) |
| role | Role | User role (ADMIN, MANAGER, USER) |
| isActive | Boolean | Account status (default: true) |
| createdAt | LocalDateTime | Account creation timestamp |

### Task Entity
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Task ID (auto-generated) |
| title | String | Task title |
| description | String | Detailed description |
| status | TaskStatus | Current status (default: OPEN) |
| priority | TaskPriority | Task priority |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |
| assignedTo | User | User assigned to this task |
| createdBy | User | User who created this task |
| updatedBy | User | User who last updated this task |

### Comment Entity
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Comment ID (auto-generated) |
| message | String | Comment text |
| createdAt | LocalDateTime | Creation timestamp |
| task | Task | Associated task |
| commentedBy | User | User who commented |

---

## Data Transfer Objects (DTOs)

The API uses role-specific DTOs to return only the data each role needs. All mapping logic lives in dedicated `@Component` mapper classes.

### User DTOs

| DTO | Used By | Fields | Mapper Method |
|-----|---------|--------|---------------|
| `UserProfileDTO` | `/api/users/me` | id, name, email, role | `UserMapper.toProfileDTO()` |
| `UserAdminDTO` | ADMIN list view, role/status updates | id, name, email, role, isActive, createdAt | `UserMapper.toAdminDTO()` |
| `UserSummaryDTO` | MANAGER list view | id, name, email | `UserMapper.toSummaryDTO()` |

### Task DTOs

| DTO | Fields | Mapper Method |
|-----|--------|---------------|
| `TaskResponseDTO` | id, title, description, status, priority, createdAt, assignedUserId, assignedUserName, createdByUserId, createdByUserName, updatedByUserId, updatedByUserName | `TaskMapper.toResponseDTO()` |

### Comment DTOs

| DTO | Fields | Mapper Method |
|-----|--------|---------------|
| `CommentResponseDTO` | id, message, commentedById, commentedByName, createdAt | `CommentMapper.toResponseDTO()` |

---

## Role-Based Access Control

### Endpoint Access Matrix

| Endpoint | ADMIN | MANAGER | USER |
|----------|-------|---------|------|
| **Authentication** |
| POST /api/auth/login | ✅ | ✅ | ✅ |
| POST /api/auth/register | ✅ | ❌ | ❌ |
| **Users** |
| GET /api/users | ✅ (UserAdminDTO) | ✅ (UserSummaryDTO) | ❌ |
| GET /api/users/me | ✅ | ✅ | ✅ |
| PUT /api/users/me/password | ✅ | ✅ | ✅ |
| PUT /api/users/{id}/role | ✅ | ❌ | ❌ |
| PUT /api/users/{id}/status | ✅ | ❌ | ❌ |
| **Tasks** |
| POST /api/tasks | ❌ | ✅ | ❌ |
| GET /api/tasks | ✅ (all) | ✅ (all) | ✅ (own) |
| GET /api/tasks/{id} | ✅ (all) | ✅ (all) | ✅ (own) |
| PUT /api/tasks/{id} | ❌ | ✅ (all fields) | ✅ (status only) |
| PUT /api/tasks/{id}/assign | ❌ | ✅ | ❌ |
| **Comments** |
| POST /api/tasks/{id}/comments | ✅ † | ✅ † | ✅ † |
| GET /api/tasks/{id}/comments | ✅ † | ✅ † | ✅ † |
| DELETE /api/tasks/{id}/comments/{cid} | ✅ ‡ | ✅ ‡ | ✅ ‡ |

**Legend**:
- ✅ = Allowed
- ❌ = Forbidden
- † = If user can view the task
- ‡ = If user is comment author or ADMIN

---

## Error Handling

### HTTP Status Codes

| Status | Meaning | When Used |
|--------|---------|-----------|
| 200 OK | Success | GET, PUT, DELETE successful |
| 201 Created | Created | POST successful |
| 400 Bad Request | Invalid input | Validation errors, business rule violations |
| 401 Unauthorized | Authentication failed | Invalid credentials, missing/expired token |
| 403 Forbidden | Access denied | Role-based access violation |
| 404 Not Found | Resource not found | Task, user, or comment doesn't exist |
| 500 Internal Server Error | Server error | Unexpected server-side errors |

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "error": "Additional error details",
  "timestamp": "2026-02-04T12:00:00"
}
```

### Common Errors

**Email already in use**:
```json
{
  "success": false,
  "message": "Email already in use",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Access denied**:
```json
{
  "success": false,
  "message": "Access denied: Users can only view tasks assigned to them",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

**Resource not found**:
```json
{
  "success": false,
  "message": "Task not found with id: 999",
  "data": null,
  "error": null,
  "timestamp": "2026-02-04T12:00:00"
}
```

---

## 📖 Swagger API Documentation

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Using Swagger with JWT

1. Login via `/api/auth/login` to get JWT token
2. Click **"Authorize"** button in Swagger UI
3. Enter: `Bearer <your-jwt-token>`
4. Click **"Authorize"** and **"Close"**
5. All requests will now include the token

---

## 🧪 Example Workflows

### Workflow 1: Manager Creates and Assigns Task

```bash
# 1. Login as Manager
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@example.com","password":"manager123"}'

# Response includes token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 2. Create and assign task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Fix Bug #123",
    "description": "Fix null pointer exception in login",
    "priority": "HIGH",
    "assignedToUserId": 3
  }'
```

### Workflow 2: User Updates Task Status

```bash
# 1. Login as User
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"user123"}'

# 2. View assigned tasks
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <user-token>"

# 3. Update task status
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <user-token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'

# 4. Add comment
curl -X POST http://localhost:8080/api/tasks/1/comments \
  -H "Authorization: Bearer <user-token>" \
  -H "Content-Type: application/json" \
  -d '{"message": "Started working on this bug fix"}'
```

### Workflow 3: Admin Creates New Manager

```bash
# 1. Login as Admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'

# 2. Register new manager
curl -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Manager",
    "email": "newmanager@example.com",
    "password": "TempPassword123",
    "role": "MANAGER"
  }'

# 3. View all users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <admin-token>"
```

---

## 🔍 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL
);
```

### Tasks Table
```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    assigned_to BIGINT REFERENCES users(id),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);
```

### Comments Table
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    author_id BIGINT NOT NULL REFERENCES users(id)
);
```

---

## Notes

### Security Considerations

- Passwords are hashed using BCrypt
- JWT tokens are stateless and include user email
- All endpoints except `/api/auth/login` require authentication
- Role-based access is enforced at both controller and service layers

### Business Rules

1. Tasks can **only** be assigned to users with `USER` role
2. MANAGER can view and update **all** tasks (not just ones they created)
3. ADMIN has **read-only** access to tasks (cannot create, assign, or update)
4. USER can **only** update the `status` field of tasks assigned to them
5. Comments can only be deleted by the author or ADMIN

### Audit Tracking

- `createdBy` tracks who created each task
- `updatedBy` tracks who last modified each task
- All timestamps use `LocalDateTime` in ISO-8601 format

---

##  License

This project is licensed under the MIT License.

---

##  Support

For issues or questions:
- Open an issue on GitHub
- Contact: support@example.com

---

