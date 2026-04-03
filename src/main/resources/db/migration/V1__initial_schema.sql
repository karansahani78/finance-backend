CREATE TYPE user_role AS ENUM ('VIEWER', 'ANALYST', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE transaction_type AS ENUM ('INCOME', 'EXPENSE');
CREATE TYPE transaction_category AS ENUM (
    'SALARY', 'FREELANCE', 'INVESTMENT', 'FOOD', 'TRANSPORT',
    'ENTERTAINMENT', 'HEALTHCARE', 'UTILITIES', 'EDUCATION',
    'SHOPPING', 'RENT', 'INSURANCE', 'OTHER'
);

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role NOT NULL DEFAULT 'VIEWER',
    status      user_status NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(15, 2) NOT NULL,
    type        transaction_type NOT NULL,
    category    transaction_category NOT NULL,
    txn_date    DATE NOT NULL,
    description TEXT,
    created_by  BIGINT NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_transactions_created_by ON transactions(created_by);
CREATE INDEX idx_transactions_txn_date ON transactions(txn_date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_category ON transactions(category);
CREATE INDEX idx_transactions_deleted_at ON transactions(deleted_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
