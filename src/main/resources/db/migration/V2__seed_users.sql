INSERT INTO users (name, email, password, role, status)
VALUES
    ('Super Admin', 'admin@finance.io', '$2a$12$KIXaEkHGEeJdxp8KoX9v5.E3KjJnMhqFqEqKb0fJsH2Nq9L7mXrCK', 'ADMIN', 'ACTIVE'),
    ('Alice Analyst', 'analyst@finance.io', '$2a$12$KIXaEkHGEeJdxp8KoX9v5.E3KjJnMhqFqEqKb0fJsH2Nq9L7mXrCK', 'ANALYST', 'ACTIVE'),
    ('Bob Viewer', 'viewer@finance.io', '$2a$12$KIXaEkHGEeJdxp8KoX9v5.E3KjJnMhqFqEqKb0fJsH2Nq9L7mXrCK', 'VIEWER', 'ACTIVE');
