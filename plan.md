# TASKVOICE — COMPLETE ENTERPRISE BUILD PLAN
> Voice-powered project, task and performance management for teams.

## 1. ROLE & MANDATE
Build a complete, working application — not a prototype, not a UI mockup.
- No fake data flows, no placeholder pages, no "Coming Soon" buttons
- Every button/form/chart/export performs a real operation against the real database or Gemini API
- Feels like a professional SaaS tool a company would actually pay for

## 2. PRODUCT VISION
Employee speaks once about their day → transcript → Gemini segments into structured work entries → employee reviews/confirms → saved as timesheet_entries.

Roles:
- ADMIN — owns org: creates people, assigns roles/managers, sees everything, changes nothing operational
- MANAGER — owns delivery: creates projects/tasks, grades work, runs appraisals for own team only
- EMPLOYEE — owns execution: logs work by voice, updates tasks, sees own grades/appraisal

## 3. TECHNOLOGY STACK
Backend: Java Servlets + JSP, JDBC, Maven, Apache Tomcat
Frontend: HTML5, CSS3, Vanilla JS, Web Speech API, Chart.js (CDN)
Database: MySQL (taskvoice), no localStorage/JSON files as system of record
AI: Google Gemini API — backend only, key never reaches browser

## 4. ENVIRONMENT & SECRETS
.env (never committed):
  GEMINI_API_KEY=your_key_here
  DB_URL=jdbc:mysql://localhost:3306/taskvoice
  DB_USERNAME=root
  DB_PASSWORD=your_password
  SESSION_SECRET=random_generated_value

.env.example (committed, no real values):
  GEMINI_API_KEY=
  DB_URL=jdbc:mysql://localhost:3306/taskvoice
  DB_USERNAME=
  DB_PASSWORD=
  SESSION_SECRET=

Rules: Never hardcode secrets. Load via ConfigListener (ServletContextListener) once at startup.

## 5. ARCHITECTURE
Strict layering:
  controller/   — Servlets: HTTP in/out only, delegate to services
  service/      — business rules, validation, orchestration
  dao/          — JDBC access, prepared statements only (interfaces + Impl)
  model/        — POJOs (pure data)
  filter/       — AuthFilter, RoleFilter, CSRFFilter, EncodingFilter
  ai/           — GeminiClient + prompt builders + response validators
  util/         — date, password hashing, JSON helpers, PDF/CSV builders
  listener/     — ConfigListener (env), DBPoolListener (connection pool)

DAOs: UserDAO, ProjectDAO, TaskDAO, TimesheetDAO, VoiceRecordDAO, CategoryDAO,
      AppraisalDAO, ProgressUpdateDAO, NotificationDAO, AuditLogDAO

Services: AuthenticationService, EmployeeService, ProjectService, TaskService,
          TimesheetService, VoiceService, GeminiService, AppraisalService,
          ReportService, NotificationService

## 6. DATABASE DESIGN (taskvoice)
All tables: PKs, FKs, indexes on lookup cols, created_at/updated_at, status enums.

Tables:
  roles              — ADMIN/MANAGER/EMPLOYEE
  departments        — org structure
  users              — login, name, email, role, status, joining_date, force_pw_change
  manager_assignments — employee_id → manager_id
  categories         — default + custom work categories, created_by
  projects           — title, manager_id, status, dates, ai_summary, ai_sentiment (fires ONCE on completion)
  project_members    — employee ↔ project
  tasks              — project_id, assignee_id, title, desc, category, priority, status, dates,
                       expected_hours, actual_hours (rolled up), completion_pct,
                       manager_grade, manager_score, manager_remark, graded_by
  task_updates       — append-only: raw_text, ai_rephrased_text, completion_pct, problems_faced,
                       update_seq, voice_record_id
  voice_records      — audio_file_ref, transcript, ai_parsed_json, processing_status (PENDING/PROCESSING/SUCCESS/FAILED/DRAFT)
  timesheet_entries  — user_id, voice_record_id, task_id, project_id, category_id,
                       entry_date, title, description, duration_hours, is_confirmed
  appraisal_periods  — MONTHLY/QUARTERLY/HALF_YEARLY/YEARLY/CUSTOM, start/end dates
  appraisals         — period_id, employee_id, manager_id,
                       ai_score + ai_grade + ai_promotion_rec + ai_increment_range + ai_* fields,
                       manager_score + manager_grade + manager_decision + manager_remark
                       (AI and manager values stored side by side, NEVER overwritten)
  manager_remarks    — task_id or appraisal_id, manager_id, remark
  notifications      — user_id, type, title, message, link, is_read
  audit_logs         — actor_id, actor_name, action, entity_type, entity_id, detail, ip_address

Constraints: end_date >= start_date, duration_hours > 0, completion_pct 0-100,
             email UNIQUE, all FKs ON DELETE RESTRICT (never silently cascade-delete history)

## 7. ROLE SYSTEM & AUTHORIZATION
- RoleFilter on every /admin/*, /manager/*, /employee/* before servlet runs
- Manager can only query their own team's data — enforced at DAO/service level, not just UI
- Wrong-role URL → 403 page (hard redirect, no soft rendering)
- First admin: seeded row in seed.sql with force_pw_change=TRUE, no public registration page

## 8. ADMIN MODULE
Can: create employees/managers, assign roles+managers, edit/disable/enable/reset-password,
     create/edit departments, view org-wide analytics, view full audit log, manage categories
Cannot: touch day-to-day project/task work

Employee table columns:
  Employee ID | Name | Email | Department | Role | Assigned Manager | Joining Date | Status | Actions

Admin Dashboard: total employees, total managers, active projects, org-wide completion rate,
                 departments overview, recent 10 audit events

Pages: Dashboard, Employees, Managers, Departments, Organization, Audit Logs, Settings

## 9. MANAGER MODULE
Dashboard: team size, active/completed/overdue tasks, logged hours, pending evaluations,
           upcoming deadlines, team productivity trend chart

Can: create projects+assign employees, create tasks (title/desc/category/priority/dates/hours/assignee),
     review all progress updates, grade completed tasks (grade+score+remark),
     revise grade/remark before appraisal fold-in, define+run appraisal periods (own team only),
     compare employees

Employee master table columns:
  Employee No | Employee Name | Task Assigned | Start Date | End Date | 1st Update | 2nd Update |
  ...Nth Update | Problems Faced | Status | Grade | Remark
  (1st/2nd/3rd + latest pinned; older updates collapse into expandable history)

Pages: Dashboard, Projects, Project Detail, Team, Employee Detail, Tasks, Task Detail/Grade,
       Timesheets, Calendar, Analytics, Appraisal Periods, Appraisal Review, Reports, Settings

## 10. EMPLOYEE MODULE
Dashboard: active projects, tasks due soon, today's logged hours, latest manager feedback, appraisal status

Can: view own projects/tasks ONLY, log work by voice (text fallback), submit progress updates
     including problems faced, view grades/remarks (read-only), view own completed appraisal (read-only)

My Tasks columns:
  Employee No | Employee Name | Project | Task | Start Date | End Date | Progress % |
  Update history | Problems Faced | Status | Manager Grade | Manager Remark

Pages: Dashboard, My Projects, Project Detail (read), My Tasks, Task Detail, Voice Timesheet,
       Calendar/Daily Logs, Reports, My Appraisal, Settings

## 11. VOICE TIMESHEET SYSTEM (Signature Feature — centerpiece, not widget)
Flow:
  1. Press "Record Work"
  2. Mic permission → recording begins (live waveform + timer)
  3. Employee speaks naturally (one continuous input, NOT per-task)
  4. Stop → transcript generated (Web Speech API or MediaRecorder fallback)
  5. Transcript → backend → Gemini
     Loading: "Transcribing…" → "Organizing entries…" → "Almost ready…"
  6. Gemini returns JSON array: [{title, category, durationHours, description}, ...]
  7. Employee reviews editable Generated Work Blocks:
     - ALWAYS show original transcript alongside AI-rephrased version
     - Editable: title, category, duration, description
     - [Remove] button per block
  8. Employee edits/removes/confirms
  9. Saved to timesheet_entries + smart-matched to task/project (fuzzy match, employee confirms)

Smart matching: fuzzy match spoken block vs assigned task titles → pre-link, employee confirms/unlinks
Duration tracking: date + duration + activity (start/end time only if explicitly spoken)
Graceful degradation: text fallback with same review-step UI shape
Never lose input: failed AI → transcript saved as DRAFT in voice_records, retry or manual convert
End-of-day nudge: ONE notification ~4:30 PM if no entries today

## 12. TASK & PROGRESS UPDATE SYSTEM
Task statuses: NOT_STARTED → IN_PROGRESS → BLOCKED → UNDER_REVIEW → COMPLETED (CANCELLED manager-only)
Actual hours = rolled up sum of timesheet_entries.duration_hours linked to task
Progress updates: append-only, each has raw_text + ai_rephrased_text + completion_pct + problems_faced + timestamp + seq#

## 13. PROJECT TRACKING
Project detail: overview, live progress %, task list, team members, timeline, total hours,
                all updates feed, problems raised, AI summary + sentiment (once on completion)

Lifecycle shown on page:
  Created → Task Assigned → Progress Updates (n) → Task Completed
  → Project Completed → AI Analysis → Manager Evaluation → Appraisal Feed-in

AI Sentiment: fires ONCE on project completion. Analyzes full project history.
Returns: sentiment (POSITIVE/NEUTRAL/NEGATIVE), confidence %, short factual explanation.
Stored in projects table permanently. sentinel_generated_at prevents re-trigger.

## 14. CALENDAR & DAILY LOGS
Views: Month / Week / Day
Markers: color + icon (not color-only — accessibility)
Daily Logs: total hours, category breakdown (chart), project/task breakdown (chart),
            day's voice transcripts, each timesheet entry (editable inline)

## 15. AI / GEMINI INTEGRATION
5 distinct prompts, never reused across purposes:
  1. Voice Segmentation — transcript → [{title, category, durationHours, description}]
  2. Rephrasing — raw text → professional one-line description (original always preserved)
  3. Project Summary — completed project history → short factual narrative
  4. Project Sentiment — full project history → {sentiment, confidence, explanation}
  5. Appraisal Analysis — period data → full performance analysis JSON

Reliability: always validate JSON shape before touching DB, 2 retries exponential backoff
for transient (429/500/503/timeout), fail-fast for 400/401/403, log failures with correlation IDs,
never expose raw API errors, never lose underlying transcript/text.

## 16. APPRAISAL & PERFORMANCE SYSTEM
Periods: MONTHLY/QUARTERLY/HALF_YEARLY/YEARLY/CUSTOM — created by Admin or Manager

Manager grading (ongoing, per task): A+, A, B+, B, C, Needs Improvement + numeric score + remark

AI Appraisal produces (stored in appraisals table):
  - Overall performance summary, strengths, improvements
  - Productivity, reliability, deadline-adherence analysis
  - Work consistency and problem-solving assessment
  - Overall Performance Score (0–100)
  - Suggested Grade: Outstanding/Excellent/Very Good/Good/Average/Needs Improvement
  - Promotion Recommendation: Strongly Recommend/Recommend/Consider/Not Recommended
  - Increment Range — labeled "AI RECOMMENDATION — MANAGER DECISION REQUIRED"

HARD SAFETY RULE: AI never auto-approves anything. Manager must Accept/Modify/Reject.
Stored appraisal keeps: AI score + AI grade + AI rec | manager score + manager grade + manager decision
(side by side, always distinguishable, never overwritten)

Employee view: read-only access to finalized appraisal only.

## 17. ANALYTICS & INFOGRAPHICS
Manager analytics (own team only):
  Completion rate (donut), productivity trend (line), logged hours trend (bar),
  grade distribution (bar), project status (donut), deadline adherence (metric),
  category/time distribution (pie), project sentiment overview (pills),
  employee comparison (horizontal bar — framed as coaching, NOT leaderboard)

Admin analytics (org-level only — no task-level grading detail):
  Headcount by role, department breakdown, org-wide completion/deadline metrics

## 18. NOTIFICATIONS & AUDIT LOG
Notification types:
  TASK_ASSIGNED, PROJECT_ASSIGNED, TASK_DUE_SOON, TASK_OVERDUE,
  FEEDBACK_RECEIVED, APPRAISAL_AVAILABLE, PROJECT_COMPLETED, NO_TIMESHEET_TODAY

Bell icon + unread count badge. Panel slides in. Mark read/all-read.

Audit log captures:
  USER_LOGIN, USER_LOGOUT, USER_CREATED, ROLE_CHANGED, MANAGER_REASSIGNED,
  PROJECT_CREATED, TASK_ASSIGNED, TASK_UPDATED, MANAGER_GRADE_CHANGED,
  APPRAISAL_GENERATED, APPRAISAL_FINALIZED
Each row: actor_id, actor_name, action, entity_type, entity_id, detail, ip, timestamp
Visible to: Admin only

## 19. REPORTS & EXPORTS
Date range presets: Past Week / Past Month / Past 3 Months / Custom Range
Exports (REAL files — no button ships unless it produces a real file):
  PDF — styled corporate work statement: branding, employee, manager, period, projects,
        tasks, hours, status, grades, remarks, AI summary, appraisal. Proper headers/footers.
  CSV — full tabular data (timesheet entries, tasks, project memberships)
  Excel — mirrors CSV with formatting (Apache POI, if time permits)

## 20. SECURITY
Passwords: bcrypt hashing (jBCrypt), never stored/logged in plaintext
SQL: prepared statements ONLY, zero string-concatenated queries
Sessions: server-side, configurable timeout (default 30min), secure+HttpOnly cookies
CSRF: token per session, validated by CSRFFilter on ALL state-changing forms
XSS: output encoding on every JSP (JSTL c:out), server-side validation mirrors client-side
Roles: RoleFilter intercepts every protected path BEFORE servlet logic
Errors: generic human-readable to users, full stack traces to server logs only

## 21. DESIGN SYSTEM
Aesthetic: "Enterprise Glass" — Linear, Notion, Vercel, Stripe, Apple HIG restraint.
NO: generic AI-dashboard, Bootstrap template, uniform card grids, heavy gradients, emojis, cartoon illustrations

Palette:
  --bg-base:       #050505  (near-black, not pure black) + subtle ambient lighting
  --bg-surface:    rgba(255,255,255,0.04) with backdrop-filter: blur(35px) saturate(150%)
  --border:        1px rgba(255,255,255,0.10)
  --text-primary:  #FFFFFF
  --text-secondary:#94A3B8
  --accent-blue:   #3B82F6  (primary actions)
  --accent-emerald:#10B981  (success, positive sentiment)
  --accent-amber:  #F59E0B  (warnings, due-soon)
  --accent-rose:   #F43F5E  (errors, overdue, negative)
  Accents: semantic only, NEVER decorative

Typography:
  Outfit (Google Fonts) — page titles, headline numbers
  Inter (Google Fonts)  — body, nav, forms, tables
  JetBrains Mono        — hours, metrics, IDs, technical values

Spacing: 8px scale. Radius: 8/12/16/20px. Motion: cubic-bezier(.16,1,.3,1) 300-600ms, purposeful only.

Layout:
  Sidebar: role-adaptive, collapsible, SVG line icons (NO emojis)
    Employee:  Dashboard · My Projects · My Tasks · Voice Timesheet · Calendar · Reports · Appraisal · Settings
    Manager:   Dashboard · Projects · Team · Tasks · Timesheets · Calendar · Analytics · Appraisals · Reports · Settings
    Admin:     Dashboard · Employees · Managers · Departments · Projects(read) · Organization · Audit Logs · Settings

  Dashboards: Bento-style (one large primary panel + smaller supporting panels — NOT uniform grid)
  Tables: search + filter + sort + pagination + sticky header + row hover + status pills + drill-in
  Voice UI: large central mic, live animated waveform, live transcript, stage-aware loading, editable block cards
  Empty states: informative (explanation + action), never blank
  Loading: skeleton loaders for data, stage labels for AI ("Transcribing…" not bare spinner)

Auth: single login page for all roles → routes to correct dashboard. NO three separate login pages.

Accessibility: keyboard nav, visible focus states, semantic HTML, ARIA labels, WCAG AA contrast,
               responsive across desktop/tablet/mobile — genuinely designed mobile layout.

## 22. PAGES (~35 real functional pages)
Shared: login.jsp, error.jsp, notifications-panel.jsp
Admin:  dashboard, employees, employee-form, managers, departments, organization, audit-logs, settings
Manager: dashboard, projects, project-form, project-detail, team, employee-detail, tasks, task-detail,
         timesheets, calendar, analytics, appraisal-periods, appraisal-review, reports, settings
Employee: dashboard, projects, project-detail, tasks, task-detail, voice-timesheet,
          calendar, reports, appraisal, settings

## 23. NON-FUNCTIONAL REQUIREMENTS
- Indexed queries, prepared statements, pagination (default 25/page) on ALL list endpoints
- Connection pooling (Apache DBCP2)
- Timeouts (30s) + capped retries (2, exponential backoff) on all Gemini calls
- try-with-resources on ALL JDBC (Connection, PreparedStatement, ResultSet) — no leaks
- Seed data visibly flagged in seed.sql, easy to strip

## 24. TESTING CHECKLIST
- Login works all 3 roles; wrong-role URL blocked server-side (→403)
- Admin: create employee+manager, assign, disable/enable, reset-pw, department CRUD
- Manager: create project, assign employees, create tasks, data isolation verified
- Manager: grade task → visible read-only to employee
- Manager: create period → AI appraisal → Accept/Modify/Reject → both AI+manager values stored
- Employee: voice record → generated blocks → edit → save → appears in calendar
- AI failure: transcript preserved as DRAFT, retry works, no data loss
- Progress updates append-only (no overwrite)
- Project sentiment fires ONCE on completion, not per update
- PDF + CSV exports produce real downloadable files with correct data
- Search/filter/pagination return DB-backed results
- Notifications fire for each event, mark-read works
- Audit log captures all listed actions with correct actor+timestamp (Admin only)
- Responsive: desktop/tablet/mobile layouts hold up; voice recorder + tables work on mobile

## 25. SETUP & DELIVERY (README.md content)
Prerequisites: Java 17+, Maven 3.8+, MySQL 8.0+, Apache Tomcat 10.x

Database:
  mysql -u root -p < database/schema.sql
  mysql -u root -p < database/seed.sql   # dev only

Configure:
  cp .env.example .env  # fill in GEMINI_API_KEY, DB creds, SESSION_SECRET

Build & Deploy:
  mvn clean package
  # copy target/taskvoice.war to Tomcat webapps/

First Admin: admin@taskvoice.com / TaskVoice@123!  (forced pw change on first login)

Guided Test:
  1. Admin: create employee + manager → assign
  2. Manager: create project → assign employee → create task
  3. Employee: Voice Timesheet → record → review blocks → save
  4. Manager: see timesheet → grade task
  5. Manager: create appraisal period → run AI → finalize
  6. Employee: view My Appraisal (read-only)
  7. Manager: export Reports → verify PDF + CSV download

## 26. DEFINITION OF DONE
The build is complete ONLY when ALL are true against real DB + real Gemini key:
  - Auth + role boundaries hold under manual URL testing
  - Admin can create and assign people
  - Managers build projects/tasks, see ONLY their own team data (visualized)
  - Employees log work by voice+text, see own grades/appraisal only
  - Progress updates preserve full history (append-only)
  - Project-level sentiment fires once on completion
  - Appraisals run AI analysis, require manager sign-off, AI+human values kept distinct
  - Reports export real PDF+CSV
  - Search/filter/notifications/audit-log all function
  - Interface is original, minimal, emoji-free — reads as real enterprise product

## 27. IMPLEMENTATION PHASES
Phase 1 — Foundation: Maven setup, schema.sql+seed.sql, ConfigListener, DBPoolListener,
                       AuthFilter/RoleFilter/CSRFFilter/EncodingFilter, DAO shells, Service shells, design-system.css
Phase 2 — Authentication: Login servlet+JSP, AuthenticationService, bcrypt util, session mgmt, force-pw-change, error pages
Phase 3 — Admin Module: Employee CRUD, manager assignment, department CRUD, admin dashboard, categories, audit log viewer
Phase 4 — Manager Module: Project CRUD+members, task CRUD+status, team view, employee detail, manager dashboard
Phase 5 — Employee Module: My Projects, My Tasks, task detail+updates, employee dashboard
Phase 6 — Voice Timesheet: recorder UI (waveform+timer), VoiceService, Gemini segmentation prompt,
                            generated blocks UI, smart task matching, save flow, text fallback, draft/retry, calendar+daily logs
Phase 7 — AI Integration: GeminiClient (retry/timeout), all 5 prompts, response validators, project sentiment, appraisal trigger
Phase 8 — Appraisal System: period CRUD, AI analysis→appraisals table, manager review (Accept/Modify/Reject), employee view
Phase 9 — Analytics: manager analytics page (all charts), admin org analytics, employee comparison
Phase 10 — Notifications+Audit: NotificationService, all trigger points, bell+panel+mark-read, audit log integration, daily nudge
Phase 11 — Reports+Exports: reports page, PDF (iText/PDFBox), CSV, Excel (POI if time permits)
Phase 12 — Polish+Testing: full testing checklist, responsive verification, empty states, skeleton loaders, error handling, README

## 28. FILE TREE
taskvoice/
├── .env.example
├── .gitignore
├── README.md
├── pom.xml
├── database/
│   ├── schema.sql
│   └── seed.sql
└── src/main/
    ├── java/com/taskvoice/
    │   ├── ai/           GeminiClient, GeminiPrompts, GeminiResponseValidator
    │   ├── controller/
    │   │   ├── admin/    AdminDashboardServlet, EmployeeServlet, DepartmentServlet, AuditLogServlet, CategoryServlet
    │   │   ├── manager/  ManagerDashboardServlet, ProjectServlet, TaskServlet, TeamServlet,
    │   │                  TimesheetManagerServlet, AnalyticsServlet, AppraisalServlet, ReportManagerServlet
    │   │   ├── employee/ EmployeeDashboardServlet, MyProjectServlet, MyTaskServlet, VoiceTimesheetServlet,
    │   │                  CalendarServlet, MyAppraisalServlet, ReportEmployeeServlet
    │   │   └── shared/   LoginServlet, LogoutServlet, NotificationServlet, SettingsServlet
    │   ├── dao/          UserDAO+Impl, ProjectDAO+Impl, TaskDAO+Impl, TimesheetDAO+Impl,
    │   │                  VoiceRecordDAO+Impl, CategoryDAO+Impl, AppraisalDAO+Impl,
    │   │                  ProgressUpdateDAO+Impl, NotificationDAO+Impl, AuditLogDAO+Impl
    │   ├── filter/       AuthFilter, RoleFilter, CSRFFilter, EncodingFilter
    │   ├── listener/     ConfigListener, DBPoolListener
    │   ├── model/        User, Role, Department, Project, Task, TaskUpdate, TimesheetEntry,
    │   │                  VoiceRecord, Category, AppraisalPeriod, Appraisal, Notification, AuditLog
    │   ├── service/      AuthenticationService, EmployeeService, ProjectService, TaskService,
    │   │                  TimesheetService, VoiceService, GeminiService, AppraisalService,
    │   │                  ReportService, NotificationService
    │   └── util/         PasswordUtil, DateUtil, JsonUtil, PdfBuilder, CsvBuilder, SessionUtil
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   └── views/
        │       ├── admin/     dashboard, employees, employee-form, managers, departments,
        │       │               organization, audit-logs, settings
        │       ├── manager/   dashboard, projects, project-form, project-detail, team, employee-detail,
        │       │               tasks, task-detail, timesheets, calendar, analytics,
        │       │               appraisal-periods, appraisal-review, reports, settings
        │       ├── employee/  dashboard, projects, project-detail, tasks, task-detail,
        │       │               voice-timesheet, calendar, reports, appraisal, settings
        │       └── shared/    login, error, notifications-panel
        ├── css/
        │   ├── design-system.css
        │   ├── components.css
        │   ├── pages/  auth, dashboard, voice-timesheet, analytics, calendar
        │   └── responsive.css
        ├── js/
        │   ├── voice-recorder.js
        │   ├── waveform.js
        │   ├── charts.js
        │   ├── notifications.js
        │   ├── csrf.js
        │   └── utils.js
        └── assets/
            └── icons/  (SVG line icons — no emojis)
