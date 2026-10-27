CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    capacity INT NOT NULL CONSTRAINT chk_venues_capacity CHECK (capacity > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_venues_code UNIQUE(code)
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_date TIMESTAMPTZ NOT NULL,
    minimum_age INT NOT NULL DEFAULT 0,
    venue_id BIGINT NOT NULL,
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
    CONSTRAINT uk_events_event_code UNIQUE(event_code)
);

CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_artists_stage_name UNIQUE(stage_name)
);

CREATE TABLE event_artists (
    event_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, artist_id),
    CONSTRAINT fk_event_artists_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_artists_artist FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_users_username UNIQUE(username),
    CONSTRAINT uk_users_email UNIQUE(email)

);

CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    city VARCHAR(100),
    birth_date DATE,
    user_id BIGINT NOT NULL CONSTRAINT uk_user_profiles_user_id UNIQUE,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    purchase_date TIMESTAMPTZ NOT NULL,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tickets_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT uk_tickets_ticket_code UNIQUE(ticket_code),
    CONSTRAINT chk_tickets_price CHECK (price >= 0)
);

CREATE INDEX idx_events_venue_id ON events(venue_id);
CREATE INDEX idx_events_status_date ON events(status, event_date);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);