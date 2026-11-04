CREATE DATABASE IF NOT EXISTS codetrack;

USE codetrack;

CREATE TABLE IF NOT EXISTS problems (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    platform VARCHAR(100),
    topic VARCHAR(100),
    difficulty VARCHAR(50),
    problem_url VARCHAR(500),
    solved BOOLEAN DEFAULT FALSE,
    solved_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS revisions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    revision_number INT NOT NULL,
    revision_date DATE NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    notes TEXT,

    PRIMARY KEY (id),

    INDEX idx_revisions_problem_id (problem_id),
    INDEX idx_revisions_date (revision_date),

    CONSTRAINT fk_revision_problem
        FOREIGN KEY (problem_id)
        REFERENCES problems(id)
        ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);