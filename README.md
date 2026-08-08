# SchemaGenie

> **Turn natural-language app ideas into complete database schemas with AI.**

SchemaGenie is an **AI-powered database schema designer** that converts a plain-English application description into a structured, production-oriented database design.

Simply describe what you want to build, select **MongoDB** or **PostgreSQL**, and SchemaGenie generates the database architecture, model classes, migration scripts, and an interactive ER diagram — all in seconds.

It also supports **JWT authentication, schema refinement, guest generation, generation history, and ZIP export**.

---

## ✨ Features

### 🤖 AI-Powered Schema Generation

Describe your application in natural language:

```text
Create an e-commerce platform with users, products,
categories, carts, orders, payments, reviews,
and product ratings.
```

SchemaGenie analyzes the requirements and generates a structured database schema using **Anthropic Claude**.

---

### 🗄️ Multi-Database Support

Choose your preferred database:

* **MongoDB**
* **PostgreSQL**

The selected database determines the generated models and migration strategy.

| Database   | Generated Output                                     |
| ---------- | ---------------------------------------------------- |
| MongoDB    | Spring Data `@Document` classes + Mongock migrations |
| PostgreSQL | JPA `@Entity` classes + Liquibase migrations         |

---

### 🔐 Authentication & User Accounts

SchemaGenie includes a complete authentication system:

* User registration
* User login
* JWT authentication
* BCrypt password hashing
* Stateless Spring Security
* Protected user history
* Save generated schemas to your account

---

### 👤 Guest Mode

Users don't need an account to try SchemaGenie.

They can:

1. Enter an application idea
2. Generate a schema
3. Explore the results
4. Refine the schema
5. Download the generated files

Users can create an account later and save their generated schemas to their history.

---

### 🔄 Schema Refinement

Already generated a schema but want to change something?

Users can provide additional instructions such as:

```text
Add a wishlist feature and allow users
to have multiple shipping addresses.
```

SchemaGenie refines the existing schema based on the new requirements.

---

### 📊 Interactive ER Diagram

Every generated schema includes a Mermaid-based ER diagram.

The frontend provides a live visualization of:

* Entities / collections
* Primary keys
* Relationships
* Foreign keys
* Entity connections

---

### 💻 Automatic Code Generation

SchemaGenie automatically generates database-specific source code.

#### MongoDB

```java
@Document(collection = "users")
public class User {
    // generated fields
}
```

And corresponding **Mongock migration change units**.

#### PostgreSQL

```java
@Entity
@Table(name = "users")
public class User {
    // generated fields
}
```

And corresponding **Liquibase migration changelogs**.

---

### 📦 ZIP Export

Download the complete generated schema as a ZIP file.

```text
schema/
├── models/
├── migrations/
├── diagram/
└── schema.json
```

The package includes:

* Model/entity classes
* Migration scripts
* Mermaid diagram source
* Raw schema JSON

---

## 🧠 How It Works

```text
              User's App Idea
                     │
                     ▼
          ┌─────────────────────┐
          │   SchemaGenie UI    │
          │    React + Vite     │
          └──────────┬──────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │   Spring Boot API   │
          └──────────┬──────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │    Claude API       │
          │  Schema Generation  │
          └──────────┬──────────┘
                     │
                     ▼
              Schema Validation
                     │
                     ▼
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
      MongoDB              PostgreSQL
          │                     │
          ▼                     ▼
     Model Classes        JPA Entities
     + Mongock            + Liquibase
          │                     │
          └──────────┬──────────┘
                     ▼
              Mermaid ER Diagram
                     │
                     ▼
                ZIP Download
```

---

# 🛠️ Tech Stack

## Frontend

* React 18
* Vite
* React Router
* Tailwind CSS
* JavaScript / JSX
* Mermaid.js
* Syntax highlighting

## Backend

* Java 17
* Spring Boot 3
* Spring Security
* JWT
* BCrypt
* Spring Data MongoDB
* Maven

## AI

* Anthropic Claude API

## Database

* MongoDB / MongoDB Atlas

## Migration & Code Generation

* Mongock
* Liquibase
* Spring Data MongoDB
* Spring Data JPA

---

# 📁 Project Structure

```text
schemagenie/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── routes/
│   │   └── ...
│   │
│   ├── package.json
│   ├── vite.config.js
│   └── ...
│
└── README.md
```

---

# 🚀 Getting Started

## Prerequisites

Install the following:

* **Java 17+**
* **Maven**
* **Node.js 18+**
* **npm**
* **MongoDB / MongoDB Atlas**
* **Anthropic API Key**

---

# ⚙️ Backend Setup

Navigate to the backend:

```bash
cd backend
```

Set the required environment variables.

### Linux / macOS

```bash
export MONGODB_URI="your-mongodb-uri"
export JWT_SECRET="your-long-random-secret"
export ANTHROPIC_API_KEY="your-anthropic-api-key"
```

### MongoDB Atlas Example

```bash
export MONGODB_URI="mongodb+srv://<username>:<password>@cluster0.yj1mlth.mongodb.net/schemagenie"
```

> **Security:** Never commit your MongoDB password, JWT secret, or Anthropic API key to GitHub.

Start the backend:

```bash
mvn spring-boot:run
```

The backend will run on:

```text
http://localhost:8080
```

---

# 🎨 Frontend Setup

Open a new terminal:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create the environment file:

```bash
cp .env.example .env
```

Configure:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Start the development server:

```bash
npm run dev
```

Open:

```text
http://localhost:3000
```

---

# 🔌 API Endpoints

| Method | Endpoint                               | Description            | Auth           |
| ------ | -------------------------------------- | ---------------------- | -------------- |
| `GET`  | `/api/health`                          | Check API status       | Public         |
| `POST` | `/api/auth/signup`                     | Create account         | Public         |
| `POST` | `/api/auth/login`                      | Login user             | Public         |
| `GET`  | `/api/auth/me`                         | Get current user       | JWT            |
| `POST` | `/api/schema/generate`                 | Generate schema        | Optional       |
| `POST` | `/api/schema/refine/{sessionId}`       | Refine schema          | Owner / Guest  |
| `GET`  | `/api/schema/session/{sessionId}`      | Get schema session     | Public / Owner |
| `GET`  | `/api/schema/history`                  | Get generation history | JWT            |
| `POST` | `/api/schema/session/{sessionId}/save` | Save schema            | JWT            |
| `GET`  | `/api/schema/download/{sessionId}`     | Download ZIP           | Public         |

---

# 🗃️ Data Architecture

An important design decision in SchemaGenie is the separation between **SchemaGenie's own database** and the **database selected for schema generation**.

SchemaGenie's internal MongoDB stores:

```text
Users
Authentication
Sessions
Generation History
Saved Schemas
```

The user's selected database:

```text
MongoDB
      OR
PostgreSQL
```

only determines the **generated database design**.

It does not change how SchemaGenie stores its own application data.

---

# 🔒 Security

SchemaGenie implements:

* JWT-based authentication
* BCrypt password hashing
* Stateless Spring Security
* Protected user endpoints
* Owner-based schema access
* Environment-based secrets
* Guest and authenticated session separation

Production deployments should additionally configure:

* HTTPS
* Secure CORS policies
* Rate limiting
* Secret management
* MongoDB network restrictions
* API request validation

---

# 🧩 Schema Generation Pipeline

The backend follows a database-independent generation architecture.

```text
Request
   │
   ▼
Schema Controller
   │
   ▼
Schema Generation Service
   │
   ▼
Claude API
   │
   ▼
JSON Schema
   │
   ▼
Schema Validator
   │
   ▼
CodeGeneratorFactory
   │
   ├───────────────┐
   ▼               ▼
MongoGenerator   PostgreSQLGenerator
   │               │
   ▼               ▼
MongoDB Code     JPA Code
Mongock          Liquibase
```

Using a `CodeGeneratorFactory` keeps database-specific generation logic separate from the controller and makes the architecture easier to extend.

---

# 🧪 Verification

## Frontend

The frontend was successfully verified with:

```bash
npm install
npm run build
```

During development, the initial build exposed an ES module configuration issue.

The project uses:

```json
"type": "module"
```

while the Tailwind and PostCSS configuration files were using CommonJS syntax.

The configuration files were updated to use ES module syntax:

```javascript
export default
```

After the change, the frontend built successfully.

---

## Backend

A complete Maven compilation could not be performed in the original sandbox because access to Maven Central was restricted.

A static inspection was performed across the Java source files, including:

* Package structure
* Class/file naming
* Bracket and parenthesis balance
* Java source organization

No structural problems were identified during that inspection.

However, **static inspection does not replace an actual Maven build**.

Run the following locally:

```bash
cd backend
mvn clean compile
```

For a stronger verification:

```bash
mvn clean test
```

---

# ✅ Pre-Production Checklist

Before deploying SchemaGenie:

### Backend

*  `mvn clean compile`
*  `mvn clean test`
*  Verify MongoDB Atlas connection
*  Test JWT authentication
*  Test signup/login
*  Test guest generation
*  Test authenticated generation
*  Test schema refinement
*  Test history
*  Test ZIP download

### AI Generation

*  Test MongoDB generation
*  Test PostgreSQL generation
*  Test one-to-one relationships
*  Test one-to-many relationships
*  Test many-to-many relationships
*  Test nested/complex schemas
*  Test malformed AI responses
*  Verify automatic retry behavior

### Frontend

*  `npm run build`
*  Test authentication pages
*  Test schema generation
*  Test ER diagram rendering
*  Test code copying
*  Test schema refinement
*  Test ZIP download
*  Test history page
*  Test responsive UI

### Security

* Remove all hardcoded credentials
* Rotate exposed MongoDB credentials
* Configure production JWT secret
* Configure CORS
* Enable HTTPS
* Add rate limiting
* Restrict MongoDB network access

---

# 📌 Environment Variables

Create environment variables instead of hardcoding secrets.

```env
MONGODB_URI=your-mongodb-connection-string
JWT_SECRET=your-long-random-secret
ANTHROPIC_API_KEY=your-anthropic-api-key
```

For the frontend:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Add environment files to `.gitignore`:

```gitignore
.env
.env.local
.env.production
```

---

# 📈 Future Improvements

Potential future enhancements include:

*  PostgreSQL database execution
*  MySQL schema generation
*  Database schema import
*  Schema versioning
*  Visual drag-and-drop schema editor
*  Team collaboration
*  Public schema sharing
*  More advanced relationship validation
*  Complete dependency graph / topological migration ordering
*  Automated integration testing
*  Docker deployment
*  CI/CD pipeline
*  Production monitoring and logging

---

# 🎯 Project Status

**SchemaGenie is an end-to-end AI database schema generation platform with support for MongoDB and PostgreSQL.**

The current implementation includes:

```text
✅ AI Schema Generation
✅ MongoDB Support
✅ PostgreSQL Support
✅ JWT Authentication
✅ Guest Mode
✅ Schema Refinement
✅ Schema Validation
✅ ER Diagram Generation
✅ Code Generation
✅ Mongock Migrations
✅ Liquibase Migrations
✅ Schema History
✅ ZIP Export
✅ React + Vite Frontend
```

Additional integration testing, production security hardening, and deployment validation are recommended before using the application in a production environment.

---

## 📄 License

Add your preferred license here, for example:

```text
MIT License
```

---

## 👨‍💻 Author

**Puneet Yadav**

B.Tech — Computer Science & Engineering (AI & ML)

Built with **Java, Spring Boot, React, MongoDB, and AI**.
