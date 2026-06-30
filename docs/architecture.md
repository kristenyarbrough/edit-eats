# Architecture

Edit Eats follows a layered architecture.

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

## Package Structure

```
controller
domain
dto
repository
service
util
```

### Responsibilities

- Controller - REST endpoints
- DTO - Request and response models
- Service - Business logic
- Repository - Database access
- Domain - JPA entities