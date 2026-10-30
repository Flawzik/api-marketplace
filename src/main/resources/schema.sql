-- Схема базы данных «Маркетплейс API» (PostgreSQL).
-- Выполняется автоматически при запуске программы.

CREATE TABLE IF NOT EXISTS api_clients (
    id           BIGSERIAL    PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- столбец добавлен позже: команда безопасна и для старых баз, и для новых
ALTER TABLE api_clients ADD COLUMN IF NOT EXISTS client_type VARCHAR(20) NOT NULL DEFAULT 'COMPANY';

CREATE TABLE IF NOT EXISTS api_connection_requests (
    id                      BIGSERIAL    PRIMARY KEY,
    client_id               BIGINT       NOT NULL REFERENCES api_clients (id),
    title                   VARCHAR(100) NOT NULL,
    description             TEXT         NOT NULL,
    api_type                VARCHAR(20)  NOT NULL,
    priority                VARCHAR(10)  NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    expected_daily_requests INTEGER      NOT NULL CHECK (expected_daily_requests > 0),
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_catalog (
    id                    BIGSERIAL    PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    description           TEXT         NOT NULL,
    version               VARCHAR(20)  NOT NULL,
    api_type              VARCHAR(20)  NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    rate_limit_per_minute INTEGER      NOT NULL CHECK (rate_limit_per_minute > 0),
    monthly_price         INTEGER      NOT NULL CHECK (monthly_price >= 0),
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (name, version)
);

CREATE TABLE IF NOT EXISTS api_keys (
    id         BIGSERIAL   PRIMARY KEY,
    key_value  VARCHAR(64) NOT NULL UNIQUE,
    client_id  BIGINT      NOT NULL REFERENCES api_clients (id),
    request_id BIGINT      NOT NULL REFERENCES api_connection_requests (id),
    status     VARCHAR(20) NOT NULL,
    issued_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP   NOT NULL
);
