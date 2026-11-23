CREATE TABLE roles (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(32) NOT NULL UNIQUE
);
INSERT INTO roles(name) VALUES ('CLIENT'), ('FREELANCER'), ('ADMIN');

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  INDEX idx_users_status(status)
);
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY(user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY(user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY(role_id) REFERENCES roles(id)
);
CREATE TABLE freelancer_profiles (
  user_id BIGINT NOT NULL PRIMARY KEY,
  headline VARCHAR(160), description TEXT,
  hourly_rate DECIMAL(12,2), experience_years INT,
  availability VARCHAR(24) NOT NULL DEFAULT 'AVAILABLE', location VARCHAR(120),
  profile_image_key VARCHAR(500), resume_key VARCHAR(500),
  CONSTRAINT fk_freelancer_profile_user FOREIGN KEY(user_id) REFERENCES users(id),
  CONSTRAINT chk_freelancer_rate CHECK(hourly_rate IS NULL OR hourly_rate >= 0),
  CONSTRAINT chk_freelancer_experience CHECK(experience_years IS NULL OR experience_years >= 0)
);
CREATE TABLE client_profiles (
  user_id BIGINT NOT NULL PRIMARY KEY,
  company_name VARCHAR(160), company_description TEXT, website VARCHAR(255), location VARCHAR(120),
  CONSTRAINT fk_client_profile_user FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE skills (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE
);
CREATE TABLE freelancer_skills (
  freelancer_id BIGINT NOT NULL, skill_id BIGINT NOT NULL,
  experience_years INT,
  PRIMARY KEY(freelancer_id, skill_id),
  CONSTRAINT fk_freelancer_skills_user FOREIGN KEY(freelancer_id) REFERENCES users(id),
  CONSTRAINT fk_freelancer_skills_skill FOREIGN KEY(skill_id) REFERENCES skills(id)
);
CREATE TABLE portfolio_items (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  freelancer_id BIGINT NOT NULL, title VARCHAR(160) NOT NULL, description TEXT, url VARCHAR(2048),
  CONSTRAINT fk_portfolio_freelancer FOREIGN KEY(freelancer_id) REFERENCES users(id),
  INDEX idx_portfolio_freelancer(freelancer_id)
);
CREATE TABLE projects (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  client_id BIGINT NOT NULL, title VARCHAR(180) NOT NULL, description TEXT NOT NULL,
  project_type VARCHAR(20) NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
  budget_min DECIMAL(12,2) NOT NULL, budget_max DECIMAL(12,2) NOT NULL,
  experience_level VARCHAR(24), category VARCHAR(80), deadline DATE,
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_project_client FOREIGN KEY(client_id) REFERENCES users(id),
  CONSTRAINT chk_project_budget CHECK(budget_min >= 0 AND budget_max >= budget_min),
  INDEX idx_projects_status_created(status, created_at), INDEX idx_projects_client(client_id)
);
CREATE TABLE project_skills (
  project_id BIGINT NOT NULL, skill_id BIGINT NOT NULL,
  PRIMARY KEY(project_id, skill_id),
  CONSTRAINT fk_project_skills_project FOREIGN KEY(project_id) REFERENCES projects(id),
  CONSTRAINT fk_project_skills_skill FOREIGN KEY(skill_id) REFERENCES skills(id)
);
CREATE TABLE project_attachments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, project_id BIGINT NOT NULL, uploaded_by BIGINT NOT NULL,
  storage_key VARCHAR(500) NOT NULL, original_name VARCHAR(255) NOT NULL, created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_attachment_project FOREIGN KEY(project_id) REFERENCES projects(id),
  CONSTRAINT fk_attachment_user FOREIGN KEY(uploaded_by) REFERENCES users(id)
);
CREATE TABLE proposals (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, project_id BIGINT NOT NULL, freelancer_id BIGINT NOT NULL,
  cover_letter TEXT NOT NULL, proposed_budget DECIMAL(12,2) NOT NULL, completion_days INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_proposal_project FOREIGN KEY(project_id) REFERENCES projects(id),
  CONSTRAINT fk_proposal_freelancer FOREIGN KEY(freelancer_id) REFERENCES users(id),
  CONSTRAINT chk_proposal_budget CHECK(proposed_budget >= 0), CONSTRAINT chk_proposal_days CHECK(completion_days > 0),
  INDEX idx_proposals_project_status(project_id, status), INDEX idx_proposals_freelancer_status(freelancer_id,status)
);
CREATE TABLE proposal_portfolio_links (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, proposal_id BIGINT NOT NULL, url VARCHAR(2048) NOT NULL,
  CONSTRAINT fk_proposal_link FOREIGN KEY(proposal_id) REFERENCES proposals(id)
);
CREATE TABLE assignments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, project_id BIGINT NOT NULL UNIQUE, proposal_id BIGINT NOT NULL UNIQUE,
  client_id BIGINT NOT NULL, freelancer_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  started_at TIMESTAMP(6) NOT NULL, completed_at TIMESTAMP(6),
  CONSTRAINT fk_assignment_project FOREIGN KEY(project_id) REFERENCES projects(id),
  CONSTRAINT fk_assignment_proposal FOREIGN KEY(proposal_id) REFERENCES proposals(id),
  CONSTRAINT fk_assignment_client FOREIGN KEY(client_id) REFERENCES users(id),
  CONSTRAINT fk_assignment_freelancer FOREIGN KEY(freelancer_id) REFERENCES users(id),
  INDEX idx_assignments_client_status(client_id,status), INDEX idx_assignments_freelancer_status(freelancer_id,status)
);
CREATE TABLE milestones (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, assignment_id BIGINT NOT NULL, title VARCHAR(160) NOT NULL,
  description TEXT, due_date DATE, amount DECIMAL(12,2) NOT NULL, sequence_number INT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT fk_milestone_assignment FOREIGN KEY(assignment_id) REFERENCES assignments(id),
  CONSTRAINT uk_milestone_assignment_sequence UNIQUE(assignment_id,sequence_number),
  CONSTRAINT chk_milestone_amount CHECK(amount >= 0),
  INDEX idx_milestone_assignment_status_due(assignment_id,status,due_date)
);
CREATE TABLE milestone_submissions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, milestone_id BIGINT NOT NULL, submitted_by BIGINT NOT NULL,
  description TEXT, comments TEXT, submitted_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_submission_milestone FOREIGN KEY(milestone_id) REFERENCES milestones(id),
  CONSTRAINT fk_submission_user FOREIGN KEY(submitted_by) REFERENCES users(id), INDEX idx_submission_milestone(milestone_id,submitted_at)
);
CREATE TABLE submission_files (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, submission_id BIGINT NOT NULL,
  storage_key VARCHAR(500) NOT NULL, original_name VARCHAR(255) NOT NULL,
  CONSTRAINT fk_submission_file FOREIGN KEY(submission_id) REFERENCES milestone_submissions(id)
);
CREATE TABLE milestone_history (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, milestone_id BIGINT NOT NULL, changed_by BIGINT NOT NULL,
  from_status VARCHAR(24), to_status VARCHAR(24) NOT NULL, comment TEXT, changed_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_history_milestone FOREIGN KEY(milestone_id) REFERENCES milestones(id),
  CONSTRAINT fk_history_user FOREIGN KEY(changed_by) REFERENCES users(id), INDEX idx_milestone_history(milestone_id,changed_at)
);
CREATE TABLE payments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, assignment_id BIGINT NOT NULL, milestone_id BIGINT,
  amount DECIMAL(12,2) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  transaction_reference VARCHAR(120), paid_at TIMESTAMP(6),
  CONSTRAINT fk_payment_assignment FOREIGN KEY(assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_payment_milestone FOREIGN KEY(milestone_id) REFERENCES milestones(id),
  CONSTRAINT chk_payment_amount CHECK(amount >= 0), INDEX idx_payment_assignment_status(assignment_id,status)
);
CREATE TABLE conversations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, project_id BIGINT NOT NULL, created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_conversation_project FOREIGN KEY(project_id) REFERENCES projects(id)
);
CREATE TABLE conversation_participants (
  conversation_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY(conversation_id,user_id),
  CONSTRAINT fk_participant_conversation FOREIGN KEY(conversation_id) REFERENCES conversations(id),
  CONSTRAINT fk_participant_user FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE messages (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, conversation_id BIGINT NOT NULL, sender_id BIGINT NOT NULL,
  body TEXT NOT NULL, sent_at TIMESTAMP(6) NOT NULL, read_at TIMESTAMP(6),
  CONSTRAINT fk_message_conversation FOREIGN KEY(conversation_id) REFERENCES conversations(id),
  CONSTRAINT fk_message_sender FOREIGN KEY(sender_id) REFERENCES users(id), INDEX idx_message_conversation_sent(conversation_id,sent_at)
);
CREATE TABLE notifications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, type VARCHAR(48) NOT NULL,
  resource_type VARCHAR(48), resource_id BIGINT, message TEXT NOT NULL, read_at TIMESTAMP(6), created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_notification_user FOREIGN KEY(user_id) REFERENCES users(id), INDEX idx_notifications_user_read_created(user_id,read_at,created_at)
);
CREATE TABLE reviews (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, assignment_id BIGINT NOT NULL, reviewer_id BIGINT NOT NULL,
  reviewee_id BIGINT NOT NULL, rating INT NOT NULL, feedback TEXT, created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_review_assignment FOREIGN KEY(assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_review_reviewer FOREIGN KEY(reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_review_reviewee FOREIGN KEY(reviewee_id) REFERENCES users(id),
  CONSTRAINT uk_review_assignment_reviewer UNIQUE(assignment_id,reviewer_id),
  CONSTRAINT chk_review_rating CHECK(rating BETWEEN 1 AND 5)
);
CREATE TABLE reports (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, reporter_id BIGINT NOT NULL, target_user_id BIGINT,
  project_id BIGINT, status VARCHAR(24) NOT NULL DEFAULT 'OPEN', reason TEXT NOT NULL, created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_report_reporter FOREIGN KEY(reporter_id) REFERENCES users(id),
  CONSTRAINT fk_report_target FOREIGN KEY(target_user_id) REFERENCES users(id),
  CONSTRAINT fk_report_project FOREIGN KEY(project_id) REFERENCES projects(id), INDEX idx_reports_status_created(status,created_at)
);
CREATE TABLE audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, actor_id BIGINT, action VARCHAR(80) NOT NULL,
  resource_type VARCHAR(48) NOT NULL, resource_id BIGINT, created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_audit_actor FOREIGN KEY(actor_id) REFERENCES users(id), INDEX idx_audit_created(created_at)
);
