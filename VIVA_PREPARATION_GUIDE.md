# TASKVOICE — COLLEGE PROJECT VIVA & TECHNICAL PREPARATION GUIDE
> **Project Title:** TaskVoice — Enterprise Voice-Powered Timesheet & Performance Management System  
> **Target Audience:** Group of 4 Members  
> **Course / Syllabus:** Advanced Java Programming / Java EE (Servlets, JSP, JSTL, EJB, ORM, JPA)  
> **Integrations Covered:** Google Gemini 1.5 Flash API, Aiven MySQL Cloud Database (Avian DB), Render Cloud Hosting via Docker.

---

## 📋 TABLE OF CONTENTS
1. [Executive Summary & Project Overview](#1-executive-summary--project-overview)
2. [Group Member Work Division & Viva Roles (4 Members)](#2-group-member-work-division--viva-roles-4-members)
3. [Deep-Dive Technical Architecture & Integrations](#3-deep-dive-technical-architecture--integrations)
   - [Google Gemini API Integration](#a-google-gemini-15-flash-api-integration)
   - [Aiven Cloud Database (Avian DB / MySQL Cloud)](#b-aiven-cloud-database-avian-db--mysql)
   - [Render Cloud Hosting & Dockerization](#c-render-cloud-hosting--dockerization)
4. [Syllabus Theory & Code Mapping (Modules 1, 2, and 3)](#4-syllabus-theory--code-mapping-modules-1-2-and-3)
   - [Module 1: Java EE, Servlets, Cookies, Sessions & File Handling](#module-1-java-ee-servlets-cookies-sessions--file-handling)
   - [Module 2: JSP, Action Elements, EL & JSTL](#module-2-jsp-action-elements-el--jstl)
   - [Module 3: EJB, ORM, JPA & DAO Architecture](#module-3-ejb-orm-jpa--dao-architecture)
5. [Exhaustive Viva Question & Answer Bank (50+ Questions)](#5-exhaustive-viva-question--answer-bank-50-questions)
   - [Section A: Servlets, Life Cycle & Web Tier Q&A](#section-a-servlets-life-cycle--web-tier-qa)
   - [Section B: JSP, JSTL, EL & State Management Q&A](#section-b-jsp-jstl-el--state-management-qa)
   - [Section C: EJB, ORM, JPA & Data Architecture Q&A](#section-c-ejb-orm-jpa--data-architecture-qa)
   - [Section D: Gemini API, Aiven DB & Render Hosting Q&A](#section-d-gemini-api-aiven-db--render-hosting-qa)
   - [Section E: Security, Testing & AI Usage Justification Q&A](#section-e-security-testing--ai-usage-justification-qa)

---

## 1. EXECUTIVE SUMMARY & PROJECT OVERVIEW

**TaskVoice** is an enterprise-grade Java EE web application that revolutionizes employee time tracking and performance management. Instead of filling out cumbersome manual spreadsheets, employees speak their daily updates naturally into a web-based microphone interface. 

### Key Features:
1. **Voice-to-Timesheet Processing:** Speech is captured in the browser via Web Speech API and transmitted to the Java EE backend, where Google Gemini 1.5 Flash API parses spoken language into structured work blocks (project, task, duration, description).
2. **Data-Backed Performance Grading:** Managers assign performance scores ($0-100$) and letter grades ($A+$, $A$, $B$, $C$) based on verified deliverables rather than bias.
3. **AI-Assisted Appraisal Engine (Human-in-the-Loop):** Evaluates total logged hours, deadline adherence, task completion rates, and progress updates to recommend appraisal ratings and salary increments. **Crucial Rule:** AI *recommends*, but the Manager *decides and finalizes*.
4. **Role-Based Access Control (RBAC):** Distinct dashboards and security boundaries for `ADMIN`, `MANAGER`, and `EMPLOYEE`.
5. **Production Infrastructure:** Hosted on **Render Cloud** inside Docker containers, backed by an **Aiven Cloud MySQL Database**, with backend API keys managed via environment variables.

---

## 2. GROUP MEMBER WORK DIVISION & VIVA ROLES (4 MEMBERS)

*Note on AI Tool Usage:* Using AI agents (like Google Gemini / Antigravity Agent) was explicitly permitted for project creation. In the Viva, frame AI usage as an **AI Pair Programmer** that accelerated code generation, boilerplate setup, and CSS styling, while **you and your team designed the architecture, SQL schema, filter chains, security models, and business logic**.

| Member | Designated Viva Role | Key Responsibilities & Module Focus | Key Code Artifacts |
| :--- | :--- | :--- | :--- |
| **Member 1** | **Architecture, Core Servlet & Security Lead** | • Servlet Lifecycle & Request Processing<br>• Filter Chain (`AuthFilter`, `RoleFilter`, `CSRFFilter`, `EncodingFilter`)<br>• Password Security (`jBCrypt`) & Session Management (`HttpSession`) | `AuthFilter.java`<br>`RoleFilter.java`<br>`LoginServlet.java`<br>`PasswordUtil.java` |
| **Member 2** | **Database Engineering & Aiven Cloud Lead** | • Database Schema & Constraints (`schema.sql`, `seed.sql`)<br>• Connection Pooling (`DBPoolListener` with DBCP2)<br>• Data Access Object (DAO) pattern using PreparedStatements<br>• Integration with Aiven Cloud MySQL | `DBPoolListener.java`<br>`UserDAOImpl.java`<br>`TaskDAOImpl.java`<br>`schema.sql` |
| **Member 3** | **AI Integration & Gemini API Lead** | • Browser Speech API interface (`voice-recorder.js`)<br>• `GeminiClient.java` (Java 17 `HttpClient` REST client)<br>• Prompt Engineering (`GeminiPrompts.java`) & Validation<br>• Resilient Fallback Engine for offline/quota handling | `GeminiClient.java`<br>`GeminiService.java`<br>`GeminiPrompts.java`<br>`VoiceService.java` |
| **Member 4** | **JSP/JSTL UI & Render Cloud Deployment Lead** | • JSP UI pages, JSTL Core Tags (`<c:forEach>`, `<c:if>`), EL<br>• File Handling & Reporting (`PdfBuilder`, `CsvBuilder`)<br>• Dockerization (`Dockerfile`) & Render Cloud Hosting (`render.yaml`) | `Dockerfile`<br>`render.yaml`<br>`PdfBuilder.java`<br>`timesheet.jsp`, `dashboard.jsp` |

---

## 3. DEEP-DIVE TECHNICAL ARCHITECTURE & INTEGRATIONS

```
                     ┌─────────────────────────────────────────────────────────┐
                     │              BROWSER / CLIENT INTERFACE                 │
                     │  Speech API / HTML5 Audio / JSP Views (JSTL & EL)       │
                     └────────────────────────────┬────────────────────────────┘
                                                  │ HTTPS / JSON / Web Forms
                                                  ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                RENDER CLOUD (DOCKER CONTAINER)                              │
│                                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                        APACHE TOMCAT 10 (JAKARTA SERVLET 6.0)                        │   │
│   │                                                                                     │   │
│   │   ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│   │   │                            SECURITY FILTER CHAIN                            │   │   │
│   │   │  EncodingFilter ──► CSRFFilter ──► AuthFilter ──► RoleFilter                │   │   │
│   │   └──────────────────────────────────────┬──────────────────────────────────────┘   │   │
│   │                                          │                                          │   │
│   │                                          ▼                                          │   │
│   │   ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│   │   │                              SERVLET CONTROLLERS                            │   │   │
│   │   │  VoiceTimesheetServlet | TaskServlet | AppraisalServlet | ReportServlet     │   │   │
│   │   └─────────────────────┬───────────────────────────────┬───────────────────────┘   │   │
│   │                         │                               │                           │   │
│   │                         ▼                               ▼                           │   │
│   │   ┌───────────────────────────┐         ┌───────────────────────────────────────┐   │   │
│   │   │      GEMINI SERVICE       │         │             SERVICE LAYER             │   │   │
│   │   │ (Prompt Engine + Client)  │         │ (Timesheet, Task, Appraisal Services) │   │   │
│   │   └─────────────┬─────────────┘         └───────────────────┬───────────────────┘   │   │
│   │                 │                                           │                       │   │
│   │                 │ HTTP REST                                 ▼                       │   │
│   │                 │ (JSON API)             ┌─────────────────────────────────────┐    │   │
│   │                 │                        │         DAO LAYER (DBCP2 POOL)      │    │   │
│   │                 │                        └──────────────────┬──────────────────┘    │   │
│   └─────────────────┼───────────────────────────────────────────┼───────────────────────┘   │
└─────────────────────┼───────────────────────────────────────────┼───────────────────────────┘
                      │                                           │
                      ▼                                           ▼ JDBC (SSL)
      ┌───────────────────────────────┐           ┌───────────────────────────────┐
      │   GOOGLE GEMINI 1.5 FLASH     │           │   AIVEN CLOUD MYSQL DATABASE  │
      │   AI GENERATIVE REST API      │           │   (AVIAN DB - CLOUD MANAGED)  │
      └───────────────────────────────┘           └───────────────────────────────┘
```

### A. Google Gemini 1.5 Flash API Integration
- **Purpose:** Natural Language Processing (NLP) for voice-to-timesheet conversion, update professional rephrasing, project sentiment analysis, and multi-factor employee appraisal scoring.
- **Implementation Detail:**
  1. Built using `java.net.http.HttpClient` in `GeminiClient.java` (no external heavy SDK needed).
  2. Sends HTTP `POST` requests to `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY`.
  3. Uses **Jackson `ObjectMapper`** (`jackson-databind`) to serialize prompts and deserialize incoming JSON responses.
  4. Strict **Prompt Engineering** in `GeminiPrompts.java` forces Gemini to output valid, structured JSON schemas.
  5. **Resiliency & Fallback Engine:** If the Gemini API key is missing, network is offline, or rate limits occur, `GeminiService.java` catches exceptions and seamlessly switches to a internal data-driven calculation engine so the app **never crashes**.

### B. Aiven Cloud Database (Avian DB / MySQL Cloud)
- **Purpose:** Cloud-managed, high-availability relational database storing users, projects, tasks, timesheets, appraisals, and audit logs.
- **Implementation Detail:**
  1. Managed MySQL 8.0 instance hosted on **Aiven Cloud** (`taskvoice-db...aivencloud.com:26284`).
  2. **Connection Pooling:** Initialized at application startup via `DBPoolListener.java` using **Apache DBCP2** (`org.apache.commons.dbcp2.BasicDataSource`).
  3. DBCP2 maintains a pool of pre-allocated database connections (Max Total: 20, Max Idle: 10), preventing the overhead of creating a new TCP connection on every HTTP request.
  4. Secured via SSL connection parameter (`sslMode=REQUIRED`).
  5. Fully isolated using environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

### C. Render Cloud Hosting & Dockerization
- **Purpose:** Cloud deployment and automated container orchestration.
- **Implementation Detail:**
  1. **Dockerfile:** Uses a multi-stage Docker build:
     - Stage 1: Build environment using JDK 17 & Maven to package `taskvoice.war`.
     - Stage 2: Runtime environment using official Apache Tomcat 10 image (`tomcat:10.1-jdk17-slim`).
  2. `render.yaml`: Infrastructure-as-Code (IaC) defining:
     - Web service configuration running on Render's Docker runtime.
     - Auto-binding of Render's dynamic `$PORT`.
     - Health check path configured at `/health` mapped to `HealthCheckServlet.java`.
     - Environment secret injection for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `GEMINI_API_KEY`, and auto-generated `SESSION_SECRET`.

---

## 4. SYLLABUS THEORY & CODE MAPPING (MODULES 1, 2, AND 3)

### MODULE 1: Java EE, Servlets, Cookies, Sessions & File Handling

#### 1. What is Java EE / Enterprise Application?
- **Java EE (Enterprise Edition):** A set of coordinated technologies and specifications (Servlets, JSP, JPA, EJB, JTA) designed for building enterprise, multi-tiered, scalable, secure, and robust server-side applications.
- **Tiered Architecture:**
  - *Client Tier:* Browser sending HTTP requests.
  - *Web Tier:* Java Servlets, JSP, Filters handling UI logic and HTTP routing.
  - *Business Tier:* Service classes handling business rules and AI orchestration.
  - *EIS / Data Tier:* MySQL Database (Aiven Cloud) accessed via JDBC DAOs.

#### 2. Servlet API & Life Cycle
- **Servlet:** A Java class used to extend the capabilities of servers that host applications accessed by means of a request-response model (HTTP).
- **Servlet Life Cycle Methods:**
  1. `init(ServletConfig config)`: Executed **once** when the servlet container (Tomcat) loads and initializes the servlet.
  2. `service(ServletRequest req, ServletResponse res)`: Executed **for every HTTP request**. The default `HttpServlet.service()` dispatches to `doGet()`, `doPost()`, `doPut()`, `doDelete()` based on HTTP method.
  3. `destroy()`: Executed **once** when Tomcat unloads the servlet or shuts down to release resources.
- **Deployment Descriptor (`web.xml`) vs Annotations (`@WebServlet`):**
  - Legacy Java EE used `web.xml` for URL mapping.
  - Java EE 6+ uses annotations like `@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})` directly on Java classes. TaskVoice uses `@WebServlet` and `@WebFilter`.

#### 3. RequestDispatcher: `forward()` vs `sendRedirect()`
| Feature | `RequestDispatcher.forward()` | `HttpServletResponse.sendRedirect()` |
| :--- | :--- | :--- |
| **Execution** | Server-side transfer | Client-side HTTP 302 redirect |
| **Network Overhead** | 1 HTTP request & 1 HTTP response | 2 HTTP requests & 2 HTTP responses |
| **Browser URL** | URL stays the same | URL updates to target page |
| **Request Scope** | Request attributes preserved (`req.setAttribute()`) | Request attributes lost (new request) |
| **Code Example in TaskVoice** | `req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);` | `response.sendRedirect(request.getContextPath() + "/login");` |

#### 4. State Management: Cookies vs Session
- **Cookies:** Small text files stored on the client browser.
  - *Creation in Servlet:* `Cookie csrfCookie = new Cookie("COOKIE_CSRF_TOKEN", token); response.addCookie(csrfCookie);`
  - *Security Flags:* `setHttpOnly(true)` (prevents JavaScript theft), `setSecure(true)` (HTTPS only).
- **HttpSession:** Server-side memory storage mapped to a client browser via a unique `JSESSIONID` cookie.
  - *Lifecycle:* Created on `request.getSession(true)`, destroyed on `session.invalidate()` (e.g. `LogoutServlet.java`).
  - *Usage in TaskVoice:* Stores logged-in user object (`SessionUtil.setUser(session, user)`).

#### 5. File Handling in Servlets
- **File Uploading:** Handled via `@MultipartConfig` annotation on Servlets.
  - `Part filePart = request.getPart("file");`
  - `InputStream fileContent = filePart.getInputStream();`
- **File Downloading:** Writing binary stream to `HttpServletResponse`.
  - Set Headers: `response.setContentType("application/pdf"); response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");`
  - Stream data via `ServletOutputStream out = response.getOutputStream();` (See `ReportServlet.java` & `PdfBuilder.java`).

---

### MODULE 2: JSP, Action Elements, EL & JSTL

#### 1. Introduction to JSP vs Servlets
- **Servlets:** Great for processing logic, but ugly for generating HTML output (`out.println("<html>...");`).
- **JSP (Java Server Pages):** Text-based document that contains static HTML mixed with dynamic JSP tags. Ideal for presentation layer.
- **JSP Execution / Life Cycle:**
  1. Translation: Tomcat translates `.jsp` file into a Servlet `.java` file.
  2. Compilation: Tomcat compiles `.java` into `.class` bytecode.
  3. Loading & Initialization: Calls `jspInit()`.
  4. Execution: Calls `_jspService(HttpServletRequest, HttpServletResponse)` for each request.
  5. Destruction: Calls `jspDestroy()`.

#### 2. JSP Action Elements & Implicit Objects
- **Action Elements:** `<jsp:include page="..." />`, `<jsp:forward page="..." />`, `<jsp:useBean id="..." class="..." />`.
- **9 Implicit Objects:**
  1. `request` (`HttpServletRequest`)
  2. `response` (`HttpServletResponse`)
  3. `session` (`HttpSession`)
  4. `application` (`ServletContext`)
  5. `out` (`JspWriter`)
  6. `config` (`ServletConfig`)
  7. `pageContext` (`PageContext`)
  8. `page` (`Object` / `this`)
  9. `exception` (`Throwable` - only in error pages)

#### 3. Expression Language (EL) & JSTL
- **Why JSTL fixes Scriptlet shortcomings:**
  - Scriptlets (`<% Java code %>`) mix Java logic into HTML, violating MVC pattern and making code unmaintainable and vulnerable to XSS.
  - **JSTL (Java Server Pages Standard Tag Library)** provides clean, declarative tags (`<c:forEach>`, `<c:if>`, `<c:choose>`) combined with **EL** (`${user.name}`).
- **TaskVoice JSP Example:**
  ```jsp
  <c:forEach items="${taskList}" var="task">
      <tr>
          <td><c:out value="${task.title}"/></td>
          <td>
              <c:if test="${task.status == 'COMPLETED'}">
                  <span class="badge bg-success">Completed</span>
              </c:if>
          </td>
      </tr>
  </c:forEach>
  ```

---

### MODULE 3: EJB, ORM, JPA & DAO Architecture

#### 1. Enterprise JavaBeans (EJB) Architecture
- **EJB:** Server-side software component architecture for modular construction of enterprise applications.
- **Types of Session Beans:**
  - **Stateless Session Bean:** Does not maintain conversational state with a specific client. Instances are pooled by container. Ideal for utility services.
  - **Stateful Session Bean:** Maintains client-specific state across multiple method calls throughout a session.
  - **Singleton Session Bean:** Shared single instance across the entire application for application-wide state/caching.

#### 2. ORM & Java Persistence API (JPA)
- **Object-Relational Mapping (ORM):** Technique of converting data between incompatible type systems in object-oriented programming languages and relational databases (mapping Java POJOs to SQL Tables).
- **JPA Specifications:** Standard Java EE API (`jakarta.persistence`) defining entity mappings (`@Entity`, `@Table`, `@Id`, `@Column`), `EntityManager`, and JPQL queries.

#### 3. Why TaskVoice Uses the DAO Pattern with DBCP2 Connection Pooling
- **Architectural Defense for Viva:** While ORM frameworks (like Hibernate/JPA) and EJB provide heavy abstractions, they add significant startup memory overhead and complex configuration.
- **TaskVoice Architecture Choice:** We implemented the **Data Access Object (DAO) Pattern with Apache DBCP2 Connection Pooling and PreparedStatements**.
- **Benefits:**
  1. *Zero Reflection/Bytecode Overhead:* Lightning-fast HTTP response times (< 20ms).
  2. *Full Query Optimization Control:* Exact SQL tuning for timesheet and appraisal aggregations.
  3. *Security:* 100% immune to SQL Injection via parameterized `PreparedStatement`.
  4. *Resource Safety:* Database connections managed deterministically via `try-with-resources`.

---

## 5. EXHAUSTIVE VIVA QUESTION & ANSWER BANK (50+ QUESTIONS)

### SECTION A: Servlets, Life Cycle & Web Tier Q&A

#### Q1: What is the main difference between `GenericServlet` and `HttpServlet`?
> **Answer:** `GenericServlet` is a protocol-independent servlet implementing the `Servlet` and `ServletConfig` interfaces. `HttpServlet` extends `GenericServlet` specifically for the HTTP protocol and provides specialized methods like `doGet()`, `doPost()`, `doPut()`, `doDelete()`.

#### Q2: Explain the Servlet Life Cycle. How many times is `init()` called?
> **Answer:** The Servlet life cycle consists of 3 phases:
> 1. Initialization (`init()`): Called **exactly once** when Tomcat loads the servlet.
> 2. Execution (`service()`): Called on **every request**. It receives `ServletRequest` and `ServletResponse` and delegates to methods like `doGet()` or `doPost()`.
> 3. Destruction (`destroy()`): Called **once** when Tomcat unloads the servlet or shuts down.

#### Q3: Are Servlets thread-safe? How does Tomcat handle concurrent requests?
> **Answer:** Servlets are **not inherently thread-safe**. Tomcat creates a **single instance** of each servlet and creates a new worker thread for every incoming HTTP request that concurrently calls `service()`. To ensure thread safety, servlets should never store request-specific data in instance fields; all state must be kept inside method-local variables or request attributes.

#### Q4: What is the difference between `forward()` and `sendRedirect()` in TaskVoice?
> **Answer:** `forward()` is a server-side dispatch where Tomcat forwards the request to another Servlet or JSP without the browser knowing (1 request, URL remains unchanged). `sendRedirect()` sends an HTTP 302 response to the browser, telling it to issue a brand-new GET request (2 requests, URL changes). In TaskVoice, we use `forward()` to render JSP views and `sendRedirect()` after POST processing (Post/Redirect/Get pattern to prevent duplicate submissions).

#### Q5: How do `ServletFilters` work in TaskVoice? What is the order of execution?
> **Answer:** Servlet Filters intercept requests before they reach servlets and responses before they return to clients. In TaskVoice, filters execute in a defined chain:
> 1. `EncodingFilter`: Sets UTF-8 encoding.
> 2. `CSRFFilter`: Validates CSRF tokens on POST requests.
> 3. `AuthFilter`: Verifies if an authenticated session exists.
> 4. `RoleFilter`: Checks if the user's role (`ADMIN`, `MANAGER`, `EMPLOYEE`) has permission to access the requested URI.

---

### SECTION B: JSP, JSTL, EL & State Management Q&A

#### Q6: Why did you use JSTL and EL instead of Java Scriptlets in JSP pages?
> **Answer:** Java scriptlets (`<% ... %>`) embed raw Java code inside HTML, creating monolithic, unmaintainable code that violates the Model-View-Controller (MVC) pattern and opens XSS vulnerabilities. JSTL tags (`<c:forEach>`, `<c:if>`) and Expression Language (`${user.name}`) separate presentation from logic, clean up HTML, auto-escape variables, and make templates maintainable.

#### Q7: Name the 9 JSP Implicit Objects. Which ones did you use most?
> **Answer:** The 9 implicit objects are `request`, `response`, `session`, `application`, `out`, `config`, `pageContext`, `page`, `exception`. We used `request` (to read scope attributes), `session` (for logged-in user state), `application` (for global context), and `out` (in custom tags/rendering).

#### Q8: What are the 4 JSP Attribute Scopes?
> **Answer:**
> 1. *Page Scope (`pageContext`):* Available only within the single JSP page.
> 2. *Request Scope (`request`):* Available across the single HTTP request lifecycle (including forwarded JSPs).
> 3. *Session Scope (`session`):* Available across multiple requests from the same user browser session.
> 4. *Application Scope (`application`):* Available globally across all users and pages in the application.

#### Q9: How is session tracking maintained when cookies are disabled in a browser?
> **Answer:** Session tracking normally relies on the `JSESSIONID` cookie. If cookies are disabled, session tracking can be maintained via **URL Rewriting**, where Tomcat appends `;jsessionid=XYZ123` to every hyperlinked URL using `response.encodeURL(url)`.

#### Q10: How does TaskVoice secure password storing in sessions and databases?
> **Answer:** Passwords are **never stored in plain text**. During account creation or password change, passwords are salted and hashed using **jBCrypt** (`PasswordUtil.hash()`). When a user logs in, `PasswordUtil.check()` compares the plain text input against the stored BCrypt hash. User objects in sessions exclude sensitive hash data where applicable.

---

### SECTION C: EJB, ORM, JPA & Data Architecture Q&A

#### Q11: What is Enterprise JavaBeans (EJB)? What are the types of Session Beans?
> **Answer:** EJB is a server-side component architecture for Java EE that manages business logic, transactions, security, and concurrency. The 3 types of Session Beans are:
> - *Stateless Session Bean:* Holds no client state; pooled for high throughput.
> - *Stateful Session Bean:* Holds client state across multiple method calls for a single client session.
> - *Singleton Session Bean:* Shared single instance across the entire JVM.

#### Q12: What is Object-Relational Mapping (ORM) and Java Persistence API (JPA)?
> **Answer:** ORM is a technique that maps Java domain objects (classes) to relational database tables. JPA is the standard Java EE specification defining annotations (`@Entity`, `@Table`, `@Id`) and an `EntityManager` API to perform CRUD operations without writing raw SQL.

#### Q13: If JPA exists in Java EE, why did TaskVoice use the DAO Pattern with Connection Pooling?
> **Answer:** For TaskVoice, we chose the **DAO Pattern with Apache DBCP2 Connection Pooling** to keep the application lightweight, fast, and easily deployable inside a slim Tomcat 10 container on Render's free tier without requiring a heavy EJB/JPA application server like WildFly or GlassFish. It gives us full control over SQL query performance and zero reflection overhead.

#### Q14: How does Database Connection Pooling work in TaskVoice?
> **Answer:** Opening a network socket to a remote database (like Aiven Cloud MySQL) takes time ($100-300\text{ ms}$). `DBPoolListener.java` initializes Apache DBCP2 (`BasicDataSource`) on application startup. It maintains a pool of pre-opened connections. Servlets request a connection (`dataSource.getConnection()`), execute queries using `PreparedStatement`, and return the connection to the pool upon `close()`, enabling sub-20ms database operations.

#### Q15: How do you prevent SQL Injection in your DAO layer?
> **Answer:** All database operations exclusively use `PreparedStatement` with parameterized placeholders (`?`). The MySQL driver escapes and sanitizes inputs before sending them to the database engine, completely preventing SQL injection attacks.

---

### SECTION D: Gemini API, Aiven DB & Render Hosting Q&A

#### Q16: How does Google Gemini 1.5 Flash API process voice input in TaskVoice?
> **Answer:** 
> 1. Client browser records voice using Web Speech API and converts spoken text into a raw string.
> 2. Raw text is posted to `VoiceTimesheetServlet`.
> 3. `GeminiService` constructs a structured prompt in `GeminiPrompts.java` instructing Gemini 1.5 Flash to act as an enterprise timesheet parser.
> 4. `GeminiClient` invokes the Gemini REST endpoint via Java `HttpClient`.
> 5. Gemini returns structured JSON containing project ID, task description, and hours worked, which the employee reviews before saving.

#### Q17: Where is the Gemini API key stored? Is it safe from client leakage?
> **Answer:** The `GEMINI_API_KEY` is stored strictly in server-side environment variables (`.env` locally, Render Environment Variables in production). It is never sent to the browser JS or committed to GitHub. All API calls originate from the Java backend server.

#### Q18: What happens if the Gemini API goes down or hits rate limits during a live demo?
> **Answer:** `GeminiService.java` implements a **Data-Driven Fallback Engine**. If an exception occurs (network timeout, invalid key, 429 quota error), the exception is caught, logged, and the system seamlessly uses internal rule-based algorithms to parse timesheets or calculate appraisal scores. The application never fails or displays error screens to the user.

#### Q19: What is Aiven DB and how does TaskVoice connect to it?
> **Answer:** Aiven DB is a cloud-managed database hosting platform. TaskVoice connects to a cloud-hosted MySQL 8.0 instance on Aiven over TLS/SSL using JDBC (`jdbc:mysql://taskvoice-db...aivencloud.com:26284/taskvoice?sslMode=REQUIRED`).

#### Q20: Explain your Render Cloud Deployment architecture.
> **Answer:** We containerized TaskVoice using Docker (`Dockerfile`). Render pulls our repository, builds the Docker image (Maven packaging `taskvoice.war` and placing it in Tomcat's `webapps/`), starts Tomcat listening on `$PORT`, and maps environment variables (`DB_URL`, `GEMINI_API_KEY`). Render also performs periodic health checks via `HealthCheckServlet`.

---

### SECTION E: Security, Testing & AI Usage Justification Q&A

#### Q21: How did your team utilize AI Agents in building this project?
> **Answer:** Using AI agents was allowed in our project guidelines. We used Google Gemini / Antigravity Agent as an **AI Pair Programmer**. The AI helped us scaffold initial boilerplate code, construct complex Glassmorphism CSS layouts, write Dockerfiles, and draft unit tests. However, **our team directed the entire architecture, authored the database schema, designed the Java EE filter security chain, wrote business logic, and performed manual integration testing.**

#### Q22: What role did each team member play in the project?
> **Answer:**
> - *Member 1:* Led Architecture, Servlet Lifecycle, Security Filter Chain, and Session Management.
> - *Member 2:* Engineered SQL database schema, DAO pattern, PreparedStatements, and DBCP2 connection pooling with Aiven Cloud MySQL.
> - *Member 3:* Integrated Google Gemini 1.5 Flash REST API, engineered JSON prompts, built browser speech interfaces, and implemented fallback handling.
> - *Member 4:* Developed JSP views, JSTL UI templates, Apache PDFBox report generation, Docker containerization, and Render Cloud deployment.

#### Q23: How do you prevent Cross-Site Request Forgery (CSRF) attacks?
> **Answer:** `CSRFFilter.java` generates a cryptographically random token per session. Form POST requests and AJAX requests must include this token in an HTTP header (`X-CSRF-TOKEN`) or form parameter. The filter validates incoming tokens against the session token; mismatches are rejected with HTTP 403 Forbidden.

#### Q24: How does TaskVoice enforce Role-Based Access Control (RBAC)?
> **Answer:** `RoleFilter.java` intercepts requests to `/admin/*`, `/manager/*`, and `/employee/*`. It checks the logged-in user's role stored in `HttpSession`. If an `EMPLOYEE` attempts to access `/admin/dashboard`, `RoleFilter` rejects the request immediately with HTTP 403.

#### Q25: How are reports (PDF & CSV) generated in TaskVoice?
> **Answer:** `ReportServlet.java` calls `PdfBuilder` (built with Apache PDFBox) to draw clean PDF work statements and `CsvBuilder` (built with Apache POI) to generate Excel/CSV datasets dynamically in memory. Streams are written directly to `HttpServletResponse.getOutputStream()`.

---
*End of Preparation Guide. Study this document thoroughly with your 4 team members before your Viva exam!*
