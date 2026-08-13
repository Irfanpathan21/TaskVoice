-- =============================================================================
-- TASKVOICE — DATABASE SCHEMA
-- Run: mysql -u root -p < database/schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS taskvoice
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE taskvoice;

SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- ROLES
-- =============================================================================
CREATE TABLE IF NOT EXISTS roles (
  id         TINYINT      PRIMARY KEY AUTO_INCREMENT,
  name       ENUM('ADMIN','MANAGER','EMPLOYEE') NOT NULL UNIQUE,
  created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- DEPARTMENTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS departments (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================================================
-- USERS
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
  id              INT          PRIMARY KEY AUTO_INCREMENT,
  employee_no     VARCHAR(20)  UNIQUE,
  name            VARCHAR(150) NOT NULL,
  email           VARCHAR(255) NOT NULL UNIQUE,
  password_hash   VARCHAR(255) NOT NULL,
  role_id         TINYINT      NOT NULL,
  department_id   INT,
  joining_date    DATE,
  status          ENUM('ACTIVE','DISABLED') DEFAULT 'ACTIVE',
  force_pw_change BOOLEAN      DEFAULT TRUE,
  created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role       (role_id),
  INDEX idx_users_department (department_id),
  INDEX idx_users_status     (status),
  FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE RESTRICT,
  FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- =============================================================================
-- MANAGER ASSIGNMENTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS manager_assignments (
  id          INT      PRIMARY KEY AUTO_INCREMENT,
  employee_id INT      NOT NULL,
  manager_id  INT      NOT NULL,
  assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_employee_manager (employee_id, manager_id),
  INDEX idx_ma_manager  (manager_id),
  INDEX idx_ma_employee (employee_id),
  FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE RESTRICT,
  FOREIGN KEY (manager_id)  REFERENCES users(id) ON DELETE RESTRICT
);

-- =============================================================================
-- CATEGORIES
-- =============================================================================
CREATE TABLE IF NOT EXISTS categories (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  is_default  BOOLEAN      DEFAULT FALSE,
  created_by  INT,
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================================================
-- PROJECTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS projects (
  id                       INT          PRIMARY KEY AUTO_INCREMENT,
  title                    VARCHAR(255) NOT NULL,
  description              TEXT,
  manager_id               INT          NOT NULL,
  status                   ENUM('PLANNING','ACTIVE','ON_HOLD','COMPLETED','CANCELLED') DEFAULT 'PLANNING',
  start_date               DATE         NOT NULL,
  end_date                 DATE         NOT NULL,
  ai_summary               TEXT,
  ai_sentiment             ENUM('POSITIVE','NEUTRAL','NEGATIVE'),
  ai_sentiment_confidence  DECIMAL(5,2),
  ai_sentiment_explanation TEXT,
  sentiment_generated_at   DATETIME,
  created_at               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_project_dates CHECK (end_date >= start_date),
  INDEX idx_projects_manager (manager_id),
  INDEX idx_projects_status  (status),
  FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- =============================================================================
-- PROJECT MEMBERS
-- =============================================================================
CREATE TABLE IF NOT EXISTS project_members (
  id         INT      PRIMARY KEY AUTO_INCREMENT,
  project_id INT      NOT NULL,
  user_id    INT      NOT NULL,
  joined_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_project_member (project_id, user_id),
  INDEX idx_pm_project (project_id),
  INDEX idx_pm_user    (user_id),
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE RESTRICT,
  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE RESTRICT
);

-- =============================================================================
-- VOICE RECORDS (declared before task_updates / timesheet_entries reference it)
-- =============================================================================
CREATE TABLE IF NOT EXISTS voice_records (
  id                INT          PRIMARY KEY AUTO_INCREMENT,
  user_id           INT          NOT NULL,
  audio_file_ref    VARCHAR(500),
  transcript        TEXT,
  ai_parsed_json    LONGTEXT,
  processing_status ENUM('PENDING','PROCESSING','SUCCESS','FAILED','DRAFT') DEFAULT 'PENDING',
  error_message     TEXT,
  retry_count       TINYINT      DEFAULT 0,
  created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_vr_user   (user_id),
  INDEX idx_vr_status (processing_status),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- =============================================================================
-- TASKS
-- =============================================================================
CREATE TABLE IF NOT EXISTS tasks (
  id             INT          PRIMARY KEY AUTO_INCREMENT,
  project_id     INT          NOT NULL,
  title          VARCHAR(255) NOT NULL,
  description    TEXT,
  assignee_id    INT          NOT NULL,
  category_id    INT,
  priority       ENUM('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'MEDIUM',
  status         ENUM('NOT_STARTED','IN_PROGRESS','BLOCKED','UNDER_REVIEW','COMPLETED','CANCELLED') DEFAULT 'NOT_STARTED',
  start_date     DATE         NOT NULL,
  due_date       DATE         NOT NULL,
  expected_hours DECIMAL(8,2),
  actual_hours   DECIMAL(8,2) DEFAULT 0.00,
  completion_pct TINYINT      DEFAULT 0,
  manager_grade  ENUM('A_PLUS','A','B_PLUS','B','C','NEEDS_IMPROVEMENT'),
  manager_score  DECIMAL(5,2),
  manager_remark TEXT,
  graded_at      DATETIME,
  graded_by      INT,
  created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_task_dates   CHECK (due_date >= start_date),
  CONSTRAINT chk_completion   CHECK (completion_pct BETWEEN 0 AND 100),
  INDEX idx_tasks_project  (project_id),
  INDEX idx_tasks_assignee (assignee_id),
  INDEX idx_tasks_status   (status),
  INDEX idx_tasks_due_date (due_date),
  FOREIGN KEY (project_id)  REFERENCES projects(id)    ON DELETE RESTRICT,
  FOREIGN KEY (assignee_id) REFERENCES users(id)       ON DELETE RESTRICT,
  FOREIGN KEY (category_id) REFERENCES categories(id)  ON DELETE SET NULL,
  FOREIGN KEY (graded_by)   REFERENCES users(id)       ON DELETE SET NULL
);

-- =============================================================================
-- TASK UPDATES (append-only — never overwrite)
-- =============================================================================
CREATE TABLE IF NOT EXISTS task_updates (
  id                INT      PRIMARY KEY AUTO_INCREMENT,
  task_id           INT      NOT NULL,
  user_id           INT      NOT NULL,
  update_seq        INT      NOT NULL,
  raw_text          TEXT     NOT NULL,
  ai_rephrased_text TEXT,
  completion_pct    TINYINT,
  problems_faced    TEXT,
  note              TEXT,
  voice_record_id   INT,
  created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_update_pct CHECK (completion_pct IS NULL OR completion_pct BETWEEN 0 AND 100),
  INDEX idx_tu_task (task_id),
  INDEX idx_tu_user (user_id),
  FOREIGN KEY (task_id)         REFERENCES tasks(id)         ON DELETE RESTRICT,
  FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE RESTRICT,
  FOREIGN KEY (voice_record_id) REFERENCES voice_records(id) ON DELETE SET NULL
);

-- =============================================================================
-- TIMESHEET ENTRIES
-- =============================================================================
CREATE TABLE IF NOT EXISTS timesheet_entries (
  id              INT            PRIMARY KEY AUTO_INCREMENT,
  user_id         INT            NOT NULL,
  voice_record_id INT,
  task_id         INT,
  project_id      INT,
  category_id     INT,
  entry_date      DATE           NOT NULL,
  title           VARCHAR(255)   NOT NULL,
  description     TEXT,
  duration_hours  DECIMAL(5,2)   NOT NULL,
  is_confirmed    BOOLEAN        DEFAULT FALSE,
  created_at      DATETIME       DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_duration CHECK (duration_hours > 0),
  INDEX idx_te_user       (user_id),
  INDEX idx_te_date       (entry_date),
  INDEX idx_te_task       (task_id),
  INDEX idx_te_project    (project_id),
  FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE RESTRICT,
  FOREIGN KEY (voice_record_id) REFERENCES voice_records(id) ON DELETE SET NULL,
  FOREIGN KEY (task_id)         REFERENCES tasks(id)         ON DELETE SET NULL,
  FOREIGN KEY (project_id)      REFERENCES projects(id)      ON DELETE SET NULL,
  FOREIGN KEY (category_id)     REFERENCES categories(id)    ON DELETE SET NULL
);

-- =============================================================================
-- APPRAISAL PERIODS
-- =============================================================================
CREATE TABLE IF NOT EXISTS appraisal_periods (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(150) NOT NULL,
  period_type ENUM('MONTHLY','QUARTERLY','HALF_YEARLY','YEARLY','CUSTOM') NOT NULL,
  start_date  DATE         NOT NULL,
  end_date    DATE         NOT NULL,
  created_by  INT          NOT NULL,
  status      ENUM('OPEN','CLOSED') DEFAULT 'OPEN',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_period_dates CHECK (end_date >= start_date),
  INDEX idx_ap_created_by (created_by),
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- =============================================================================
-- APPRAISALS (AI values + Manager values stored side by side, never overwritten)
-- =============================================================================
CREATE TABLE IF NOT EXISTS appraisals (
  id                       INT           PRIMARY KEY AUTO_INCREMENT,
  period_id                INT           NOT NULL,
  employee_id              INT           NOT NULL,
  manager_id               INT           NOT NULL,
  -- AI-generated values
  ai_score                 DECIMAL(5,2),
  ai_grade                 ENUM('OUTSTANDING','EXCELLENT','VERY_GOOD','GOOD','AVERAGE','NEEDS_IMPROVEMENT'),
  ai_promotion_rec         ENUM('STRONGLY_RECOMMEND','RECOMMEND','CONSIDER','NOT_RECOMMENDED'),
  ai_increment_range       VARCHAR(100),
  ai_summary               TEXT,
  ai_strengths             TEXT,
  ai_improvements          TEXT,
  ai_productivity_analysis TEXT,
  ai_reliability_analysis  TEXT,
  ai_consistency_analysis  TEXT,
  ai_problem_solving       TEXT,
  ai_generated_at          DATETIME,
  -- Manager final values (Accept / Modify / Reject)
  manager_score            DECIMAL(5,2),
  manager_grade            ENUM('OUTSTANDING','EXCELLENT','VERY_GOOD','GOOD','AVERAGE','NEEDS_IMPROVEMENT'),
  manager_decision         ENUM('ACCEPTED','MODIFIED','REJECTED'),
  manager_remark           TEXT,
  manager_reviewed_at      DATETIME,
  -- Status
  final_status             ENUM('PENDING_AI','PENDING_REVIEW','FINALIZED') DEFAULT 'PENDING_AI',
  created_at               DATETIME      DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_appraisal (period_id, employee_id),
  INDEX idx_appr_employee (employee_id),
  INDEX idx_appr_manager  (manager_id),
  INDEX idx_appr_period   (period_id),
  FOREIGN KEY (period_id)   REFERENCES appraisal_periods(id) ON DELETE RESTRICT,
  FOREIGN KEY (employee_id) REFERENCES users(id)             ON DELETE RESTRICT,
  FOREIGN KEY (manager_id)  REFERENCES users(id)             ON DELETE RESTRICT
);

-- =============================================================================
-- MANAGER REMARKS
-- =============================================================================
CREATE TABLE IF NOT EXISTS manager_remarks (
  id           INT      PRIMARY KEY AUTO_INCREMENT,
  task_id      INT,
  appraisal_id INT,
  manager_id   INT      NOT NULL,
  remark       TEXT     NOT NULL,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_mr_task       (task_id),
  INDEX idx_mr_appraisal  (appraisal_id),
  INDEX idx_mr_manager    (manager_id),
  FOREIGN KEY (task_id)      REFERENCES tasks(id)       ON DELETE SET NULL,
  FOREIGN KEY (appraisal_id) REFERENCES appraisals(id)  ON DELETE SET NULL,
  FOREIGN KEY (manager_id)   REFERENCES users(id)       ON DELETE RESTRICT
);

-- =============================================================================
-- NOTIFICATIONS
-- =============================================================================
CREATE TABLE IF NOT EXISTS notifications (
  id         INT          PRIMARY KEY AUTO_INCREMENT,
  user_id    INT          NOT NULL,
  type       ENUM('TASK_ASSIGNED','PROJECT_ASSIGNED','TASK_OVERDUE','TASK_DUE_SOON',
                  'FEEDBACK_RECEIVED','APPRAISAL_AVAILABLE','PROJECT_COMPLETED',
                  'NO_TIMESHEET_TODAY') NOT NULL,
  title      VARCHAR(255) NOT NULL,
  message    TEXT         NOT NULL,
  link       VARCHAR(500),
  is_read    BOOLEAN      DEFAULT FALSE,
  created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_notif_user    (user_id),
  INDEX idx_notif_is_read (is_read),
  INDEX idx_notif_type    (type),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- =============================================================================
-- AUDIT LOGS
-- =============================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
  id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  actor_id    INT          NOT NULL,
  actor_name  VARCHAR(150) NOT NULL,
  action      VARCHAR(100) NOT NULL,
  entity_type VARCHAR(100),
  entity_id   INT,
  detail      TEXT,
  ip_address  VARCHAR(45),
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_actor  (actor_id),
  INDEX idx_audit_action (action),
  INDEX idx_audit_time   (created_at),
  INDEX idx_audit_entity (entity_type, entity_id)
);

SET FOREIGN_KEY_CHECKS = 1;
