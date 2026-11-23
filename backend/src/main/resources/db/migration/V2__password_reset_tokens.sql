CREATE TABLE password_reset_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMP(6) NOT NULL,
  used_at TIMESTAMP(6),
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_password_reset_user FOREIGN KEY(user_id) REFERENCES users(id),
  INDEX idx_password_reset_user_expiry(user_id, expires_at)
);
