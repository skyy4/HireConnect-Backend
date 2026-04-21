-- HireConnect Database Initialization Script
-- Creates all required databases for each microservice

CREATE DATABASE IF NOT EXISTS hireconnect_auth;
CREATE DATABASE IF NOT EXISTS hireconnect_profile;
CREATE DATABASE IF NOT EXISTS hireconnect_jobs;
CREATE DATABASE IF NOT EXISTS hireconnect_applications;
CREATE DATABASE IF NOT EXISTS hireconnect_interviews;
CREATE DATABASE IF NOT EXISTS hireconnect_notifications;
CREATE DATABASE IF NOT EXISTS hireconnect_subscriptions;

-- Grant permissions (when using non-root user)
-- GRANT ALL PRIVILEGES ON hireconnect_auth.*          TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_profile.*       TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_jobs.*          TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_applications.*  TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_interviews.*    TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_notifications.* TO 'hireconnect'@'%';
-- GRANT ALL PRIVILEGES ON hireconnect_subscriptions.* TO 'hireconnect'@'%';
-- FLUSH PRIVILEGES;
