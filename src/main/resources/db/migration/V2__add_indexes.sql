-- Flyway Migration V2: Performance Indexes

CREATE INDEX idx_assignments_user_id ON assignments(user_id);
CREATE INDEX idx_assignments_subject_id ON assignments(subject_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_priority ON assignments(priority);
CREATE INDEX idx_assignments_user_status ON assignments(user_id, status);
CREATE INDEX idx_assignments_user_due_date ON assignments(user_id, due_date);
CREATE INDEX idx_subjects_user_id ON subjects(user_id);
