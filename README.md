# Freelance Collaboration Platform

Java 21 / Spring Boot, React, and MySQL application. The normal development setup runs each service as a separate local program; Docker is optional.

## 1. Download and install the tools

- **Java 21 JDK:** install the Windows x64 MSI for [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21). Choose **JDK**, not JRE.
- **Maven 3.9 or newer:** download the binary ZIP from [Apache Maven](https://maven.apache.org/download.cgi), extract it, and add its `bin` folder to your Windows `PATH`. Maven’s Windows guide describes the PATH setup: [Maven on Windows](https://maven.apache.org/guides/getting-started/windows-prerequisites.html).
- **Node.js 22 LTS:** install the Windows MSI from [Node.js downloads](https://nodejs.org/en/download/). This app uses Vite 7, which requires Node 20.19+ or 22.12+.
- **MySQL Server 8.4 LTS:** download the Windows x64 MSI from [MySQL Community Server 8.4](https://dev.mysql.com/downloads/mysql/8.4.html). Run the bundled MySQL Configurator after installation; see the [Windows installation guide](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html).
- **Mailpit:** download the Windows executable from [Mailpit releases](https://github.com/axllent/mailpit/releases). It catches development emails such as password reset links; it does not send them to real users.

Close and reopen PowerShell after installing Java, Maven, and Node so the updated PATH is loaded. Check the tools:

```powershell
java -version
mvn -version
node --version
npm --version
mysql --version
```

## 2. Create the local database

Start the MySQL service you configured in the installer. In PowerShell, connect as the MySQL administrator:

```powershell
mysql -u root -p
```

At the MySQL prompt, create the development database and application user:

```sql
CREATE DATABASE IF NOT EXISTS freelance_platform;
CREATE USER IF NOT EXISTS 'freelance'@'localhost' IDENTIFIED BY 'freelance_dev_only';
ALTER USER 'freelance'@'localhost' IDENTIFIED BY 'freelance_dev_only';
GRANT ALL PRIVILEGES ON freelance_platform.* TO 'freelance'@'localhost';
```

Type `exit` to leave MySQL. The API applies database migrations automatically when it starts.

## 3. Start Mailpit

Extract the downloaded Mailpit ZIP. In a new PowerShell window, change to the extracted folder and run the executable:

```powershell
.\mailpit.exe
```

Mailpit listens for SMTP on port `1025` and shows captured messages at [http://localhost:8025](http://localhost:8025).

## 4. Start the Java backend

Open a new PowerShell window in the repository folder and load the development environment values:

```powershell
cd "C:\Users\Saranya\OneDrive\Documents\ChatGPT\SkillSync"
. .\scripts\set-dev-env.ps1
cd backend
mvn spring-boot:run
```

The first Maven run downloads the Java libraries. Leave this window open while using the app. Flyway creates/updates the schema on startup.

## 5. Start the React frontend

Open another PowerShell window:

```powershell
cd "C:\Users\Saranya\OneDrive\Documents\ChatGPT\SkillSync\frontend"
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The frontend calls the API at `http://localhost:8080/api` by default. API documentation is at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## Stop the app

Press **Ctrl+C** in the frontend and backend windows. Stop Mailpit with **Ctrl+C** as well. MySQL can remain running as a Windows service.

## Included functionality

- **Accounts:** client/freelancer registration, JWT login, password reset email (captured by Mailpit), role-based access, profile editing, photo, resume, skills, portfolio, and ratings.
- **Projects:** create/edit/draft/publish, browse/search, skill matching, freelancer search, proposals, accept/reject/withdraw, and assignments.
- **Delivery:** assignment milestones, work submission and revision history, private project/work uploads, and participant-only downloads.
- **Collaboration:** project conversations, messages, read status, in-app notifications, and scheduled milestone reminders.
- **Payments:** sandbox checkout/status handling, signed sandbox webhook, payment history, and downloadable text receipts. This does not move real money.
- **Trust and operations:** completion reviews and user-submitted reports.
- **Developer support:** Flyway migrations, OpenAPI/Swagger UI, Postman collection, and repeatable development demo data.

## Demo accounts

The `dev` profile seeds demo records only when the database has no user accounts. All seeded accounts use `DemoPass123!`:

| Role | Email |
| --- | --- |
| Clients | `client1@example.test` through `client5@example.test` |
| Freelancers | `freelancer1@example.test` through `freelancer10@example.test` |

Log in at [http://localhost:5173](http://localhost:5173). To reseed a fresh demo database, stop the app and clear the local development database before restarting; this deletes local data.

## API and verification

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Postman: import `postman/FreelancePlatform.postman_collection.json`, set `baseUrl` to `http://localhost:8080/api`, then register/login and save the returned `token` collection variable.
- Backend checks: from `backend`, run `mvn test`.
- Frontend production build: from `frontend`, run `npm run build`.

Keep MySQL and Mailpit running, then keep the backend and Vite terminal windows open while using the site. The sandbox payment flow is for development demonstration only.

## Tests

The frontend production build was verified successfully in the workspace. Maven is not available in the workspace shell, so run the backend tests from the PowerShell window where Maven is installed; this also checks the recent backend changes and database startup on your machine.

## Optional Docker setup

Docker files remain available, but are not needed for the steps above. To use containers instead, copy `.env.example` to `.env` and run `docker compose up --build`.
