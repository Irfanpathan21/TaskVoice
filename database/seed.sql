-- =============================================================================
-- TASKVOICE — SEED DATA (DEV ONLY — strip before real deployment)
-- Run AFTER schema.sql: mysql -u root -p < database/seed.sql
-- All rows below are flagged: -- SEED DATA --
-- =============================================================================

USE taskvoice;

-- =============================================================================
-- SEED DATA -- roles
-- =============================================================================
INSERT IGNORE INTO roles (id, name) VALUES
  (1, 'ADMIN'),
  (2, 'MANAGER'),
  (3, 'EMPLOYEE');

-- =============================================================================
-- SEED DATA -- departments
-- =============================================================================
INSERT IGNORE INTO departments (id, name, description) VALUES
  (1, 'Engineering',       'Software development and infrastructure'),
  (2, 'Product',           'Product management and design'),
  (3, 'Operations',        'HR, finance, and administration'),
  (4, 'Quality Assurance', 'Testing and quality control');

-- =============================================================================
-- SEED DATA -- categories (default)
-- =============================================================================
INSERT IGNORE INTO categories (id, name, description, is_default) VALUES
  (1, 'Development',    'Writing code, debugging, code review',        TRUE),
  (2, 'Design',         'UI/UX design, wireframes, prototyping',       TRUE),
  (3, 'Meetings',       'Standup, sprint planning, client calls',      TRUE),
  (4, 'Documentation',  'Writing docs, specs, reports',                TRUE),
  (5, 'Testing',        'Manual and automated testing',                TRUE),
  (6, 'Research',       'Technical research, spike tasks',             TRUE),
  (7, 'Deployment',     'CI/CD, releases, environment setup',          TRUE),
  (8, 'Administration', 'Project admin, planning, coordination',       TRUE);

-- =============================================================================
-- SEED DATA -- users
-- Password for all seed users: TaskVoice@123!
-- bcrypt hash of "TaskVoice@123!" (cost 12):
-- $2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG
-- =============================================================================
INSERT IGNORE INTO users (id, employee_no, name, email, password_hash, role_id, department_id, joining_date, status, force_pw_change) VALUES
  (1,  'EMP-001', 'System Admin',     'admin@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 1, 3, '2024-01-01', 'ACTIVE', TRUE),
  (2,  'EMP-002', 'Priya Sharma',     'priya@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 2, 1, '2024-02-01', 'ACTIVE', FALSE),
  (3,  'EMP-003', 'Rahul Mehta',      'rahul@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 2, 2, '2024-02-15', 'ACTIVE', FALSE),
  (4,  'EMP-004', 'Ananya Singh',     'ananya@taskvoice.com',   '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 3, 1, '2024-03-01', 'ACTIVE', FALSE),
  (5,  'EMP-005', 'Arjun Nair',       'arjun@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 3, 1, '2024-03-15', 'ACTIVE', FALSE),
  (6,  'EMP-006', 'Kavya Reddy',      'kavya@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 3, 2, '2024-04-01', 'ACTIVE', FALSE),
  (7,  'EMP-007', 'Vikram Patel',     'vikram@taskvoice.com',   '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 3, 4, '2024-04-15', 'ACTIVE', FALSE),
  (8,  'EMP-008', 'Meera Joshi',      'meera@taskvoice.com',    '$2a$12$95t.qkzKH97xYuqgGQtuBei6hybaAhlHlve4SnLj15wPA1hgz.NWG', 3, 1, '2024-05-01', 'ACTIVE', FALSE);

-- =============================================================================
-- SEED DATA -- manager_assignments
-- =============================================================================
INSERT IGNORE INTO manager_assignments (employee_id, manager_id) VALUES
  (4, 2),  -- Ananya reports to Priya
  (5, 2),  -- Arjun reports to Priya
  (6, 3),  -- Kavya reports to Rahul
  (7, 3),  -- Vikram reports to Rahul
  (8, 2);  -- Meera reports to Priya

-- =============================================================================
-- SEED DATA -- projects
-- =============================================================================
INSERT IGNORE INTO projects (id, title, description, manager_id, status, start_date, end_date) VALUES
  (1, 'Customer Portal Redesign', 'Complete redesign of the customer-facing portal with modern UX', 2, 'ACTIVE',    '2026-07-01', '2026-09-30'),
  (2, 'API Gateway Integration',  'Integrate third-party APIs with the internal gateway',          2, 'ACTIVE',    '2026-07-15', '2026-08-31'),
  (3, 'Mobile App v2',            'Second major version of the mobile application',                3, 'PLANNING',  '2026-09-01', '2026-12-31'),
  (4, 'QA Automation Framework',  'Build an end-to-end test automation framework',                 3, 'ACTIVE',    '2026-06-01', '2026-08-31');

-- =============================================================================
-- SEED DATA -- project_members
-- =============================================================================
INSERT IGNORE INTO project_members (project_id, user_id) VALUES
  (1, 4), (1, 5), (1, 8),
  (2, 4), (2, 5),
  (3, 6),
  (4, 7), (4, 6);

-- =============================================================================
-- SEED DATA -- tasks
-- =============================================================================
INSERT IGNORE INTO tasks (id, project_id, title, description, assignee_id, category_id, priority, status, start_date, due_date, expected_hours, actual_hours, completion_pct) VALUES
  (1,  1, 'Design new homepage mockup',    'Create Figma mockup for the redesigned homepage',            4, 2, 'HIGH',     'IN_PROGRESS',  '2026-07-01', '2026-07-20', 16.00,  8.00,  50),
  (2,  1, 'Implement responsive header',   'Code the responsive header component in HTML/CSS/JS',        5, 1, 'HIGH',     'NOT_STARTED',  '2026-07-15', '2026-07-30', 12.00,  0.00,  0),
  (3,  1, 'User authentication flow',      'Implement OAuth2 login for the customer portal',             4, 1, 'CRITICAL', 'IN_PROGRESS',  '2026-07-10', '2026-08-05', 24.00,  10.00, 40),
  (4,  2, 'API schema documentation',      'Document all endpoints using OpenAPI 3.0 spec',              5, 4, 'MEDIUM',   'COMPLETED',    '2026-07-15', '2026-07-25', 8.00,   9.50,  100),
  (5,  2, 'Gateway rate limiting',         'Implement rate limiting on the API gateway',                 4, 1, 'HIGH',     'UNDER_REVIEW', '2026-07-20', '2026-08-10', 20.00,  18.00, 90),
  (6,  4, 'Selenium test suite for login', 'Automate login, logout, and session tests',                  7, 5, 'MEDIUM',   'IN_PROGRESS',  '2026-06-15', '2026-07-31', 16.00,  12.00, 75),
  (7,  4, 'Test report dashboard',         'Build a dashboard to visualise test run results',             6, 1, 'LOW',      'NOT_STARTED',  '2026-08-01', '2026-08-20', 10.00,  0.00,  0);

-- Grade task 4 (completed)
UPDATE tasks SET
  manager_grade  = 'A',
  manager_score  = 88.00,
  manager_remark = 'Excellent documentation quality. Delivered one day ahead of schedule.',
  graded_at      = '2026-07-26 10:30:00',
  graded_by      = 2
WHERE id = 4;

-- =============================================================================
-- SEED DATA -- appraisal_periods
-- =============================================================================
INSERT IGNORE INTO appraisal_periods (id, title, period_type, start_date, end_date, created_by, status) VALUES
  (1, 'Q2 2026 — Apr–Jun', 'QUARTERLY', '2026-04-01', '2026-06-30', 2, 'CLOSED'),
  (2, 'Q3 2026 — Jul–Sep', 'QUARTERLY', '2026-07-01', '2026-09-30', 2, 'OPEN');

-- =============================================================================
-- SEED DATA -- audit_logs (sample entries)
-- =============================================================================
INSERT IGNORE INTO audit_logs (actor_id, actor_name, action, entity_type, entity_id, detail, ip_address, created_at) VALUES
  (1, 'System Admin', 'USER_CREATED', 'USER', 2, 'Created manager account: priya@taskvoice.com', '127.0.0.1', '2024-02-01 09:00:00'),
  (1, 'System Admin', 'USER_CREATED', 'USER', 3, 'Created manager account: rahul@taskvoice.com',  '127.0.0.1', '2024-02-15 09:00:00'),
  (2, 'Priya Sharma',  'PROJECT_CREATED', 'PROJECT', 1, 'Created project: Customer Portal Redesign', '127.0.0.1', '2026-07-01 10:00:00'),
  (2, 'Priya Sharma',  'PROJECT_CREATED', 'PROJECT', 2, 'Created project: API Gateway Integration',  '127.0.0.1', '2026-07-15 10:00:00'),
  (2, 'Priya Sharma',  'MANAGER_GRADE_CHANGED', 'TASK', 4, 'Graded task #4: A (88)', '127.0.0.1', '2026-07-26 10:30:00');
