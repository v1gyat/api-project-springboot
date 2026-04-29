-- 1. Create the Users table first (because Tasks depend on it)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'MANAGER', 'USER'))
);

-- 2. Create the Tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id BIGINT,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- Foreign Key Constraints with clean names
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tasks_creator FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_tasks_updater FOREIGN KEY (updated_by) REFERENCES users (id),
    
    -- Enums/Check constraints
    CONSTRAINT tasks_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT tasks_status_check CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE'))
);

-- 3. Create the Comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    -- Foreign Key Constraints
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
);