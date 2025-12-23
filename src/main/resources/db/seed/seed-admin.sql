INSERT INTO users (fullname, username, token_version, password, role, created_at, deleted, deleted_at)
SELECT
    'Admin'            AS fullname,
    'admin'            AS username,
    0                  AS token_version,
    '$2b$10$J6zti0zME1hMrCbmXdgh8OOvahyGQ.m0yWHHLmkUwbTWwJCQxRe7q'  AS password,
    'ADMIN'            AS role,
    NOW()              AS created_at,
    false              AS deleted,
    NULL               AS deleted_at
    WHERE NOT EXISTS (SELECT 1 FROM users);
