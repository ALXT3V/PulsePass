CREATE TABLE user_credentials (
    user_id BIGINT NOT NULL PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMPTZ,
    CONSTRAINT fk_user_credentials_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_roles_role CHECK (role IN ('USER', 'STAFF', 'ADMIN'))
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);