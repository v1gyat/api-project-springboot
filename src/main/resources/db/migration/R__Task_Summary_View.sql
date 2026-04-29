CREATE OR REPLACE VIEW task_summary_view AS
SELECT 
    t.id AS task_id,
    t.title AS task_title,
    t.description AS task_description,
    t.priority AS task_priority,
    t.status AS task_status,
    u.name AS assigned_user_name,
    u.email AS assigned_user_email,
    t.created_at AS task_created_at
FROM tasks t
LEFT JOIN users u ON t.user_id = u.id;