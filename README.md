# TASKVOICE — ENTERPRISE VOICE-POWERED TIMESHEET & PERFORMANCE MANAGEMENT

> **Voice-powered project, task and performance management for teams.**

TaskVoice replaces manual timesheet entry with natural spoken updates, gives managers real, data-backed visibility into team output, and provides an AI-assisted (never AI-decided) appraisal system.

---

## TABLE OF CONTENTS

1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Environment Configuration](#environment-configuration)
4. [Build & Deployment](#build--deployment)
5. [First Admin Bootstrap](#first-admin-bootstrap)
6. [Guided Test Workflow](#guided-test-workflow)
7. [Architecture Overview](#architecture-overview)
8. [Role & Security Boundaries](#role--security-boundaries)

---

## PREREQUISITES

- **Java JDK 17+** (JDK 17, 21, or 25)
- **Apache Maven 3.8+** (or Maven wrapper)
- **MySQL Server 8.0+** running locally on port 3306
- **Apache Tomcat 10.x** (Jakarta Servlet 6.0 / JSP 3.1)
- **Google Gemini API Key** (from Google AI Studio)

---

## DATABASE SETUP

1. Start your local MySQL server.
2. Import the schema and constraints:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
3. (Optional for Development) Import sample seed data:
   ```bash
   mysql -u root -p < database/seed.sql
   ```

---

## ENVIRONMENT CONFIGURATION

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Fill in your environment secrets in `.env`:
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key
   DB_URL=jdbc:mysql://localhost:3306/taskvoice
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password
   SESSION_SECRET=a_random_secure_secret_value
   ```
   > ⚠️ **Note:** `.env` is listed in `.gitignore` and is never committed to VCS. The Gemini API key is accessed strictly on the backend.

---

## BUILD & DEPLOYMENT

1. Build the WAR package using Maven:
   ```bash
   mvn clean package
   ```
2. Copy `target/taskvoice.war` to your Apache Tomcat `webapps/` folder:
   ```bash
   cp target/taskvoice.war /path/to/tomcat/webapps/
   ```
3. Start Tomcat. The application will be accessible at:
   `http://localhost:8080/taskvoice/`

---

## FIRST ADMIN BOOTSTRAP

- Initial Admin Credentials (seeded in `seed.sql`):
  - **Email:** `admin@taskvoice.com`
  - **Password:** `TaskVoice@123!`
- On first login, the user will be forced to change their password via the Security flow before accessing any operational features.
- Only existing Admins can create new Manager or Employee accounts. There is **no public registration page**.

---

## GUIDED TEST WORKFLOW

To verify the full end-to-end loop in minutes:

1. **Login as Admin** (`admin@taskvoice.com` / `TaskVoice@123!`).
   - Go to **Employees** → Click **+ Create Account** → Add a Manager & an Employee, and assign the Employee to the Manager.
2. **Login as Manager** (`priya@taskvoice.com` / `TaskVoice@123!`).
   - Go to **Projects** → Create a project ("Mobile Redesign") → Assign employee.
   - Go to **Tasks** → Create a task ("Build Voice Interface") → Assign employee.
3. **Login as Employee** (`ananya@taskvoice.com` / `TaskVoice@123!`).
   - Click **Voice Timesheet** → Click Mic → Speak: _"Today I spent 4 hours coding the voice recorder component and 2 hours testing."_
   - Review the AI-generated work blocks, edit if desired, and click **Confirm & Save Entries**.
   - Check **Calendar** to see the entry visually logged.
4. **Login as Manager**.
   - Go to **Timesheets** → Verify the employee's logged entry.
   - Go to **Tasks** → Grade completed task (e.g. Grade: `A`, Score: `90`, Remark: _"Great work"_).
   - Go to **Appraisals** → Create a period → Click **Run AI Appraisal** → Review side-by-side AI recommendation vs Manager Decision → Click **Finalize Appraisal Decision**.
5. **Login as Employee**.
   - Go to **My Appraisal** → View read-only finalized appraisal results.
6. **Export Reports**.
   - Go to **Reports** → Select Date Range → Download **Official Work Statement (PDF)** and **Dataset (CSV)**.

---

## ARCHITECTURE OVERVIEW

```
src/main/java/com/taskvoice/
├── ai/          # GeminiClient + 5 distinct prompt builders & validators
├── controller/  # Servlets (Admin, Manager, Employee, Shared)
├── dao/         # JDBC interfaces + prepared-statement implementations
├── filter/      # AuthFilter, RoleFilter, CSRFFilter, EncodingFilter
├── listener/    # ConfigListener (.env) & DBPoolListener (DBCP2 pool)
├── model/       # POJOs (User, Project, Task, Appraisal, etc.)
├── service/     # Business rules & orchestration
└── util/        # PasswordUtil (bcrypt), PdfBuilder, CsvBuilder, JsonUtil
```

---

## ROLE & SECURITY BOUNDARIES

- **ADMIN**: Creates accounts, assigns managers, manages departments, views audit logs.
- **MANAGER**: Creates projects/tasks for own team, grades work, runs appraisals. Data isolated to assigned team (`manager_assignments`).
- **EMPLOYEE**: Logs work via voice/text, updates tasks, views own grades/appraisals (read-only).
- **ROLE FILTER**: Intercepts all requests server-side BEFORE servlets run. Invalid URL access returns `HTTP 403`.
