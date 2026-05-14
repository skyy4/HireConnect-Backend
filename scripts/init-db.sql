-- HireConnect Database Initialization Script
-- Creates all required databases for each microservice
-- Updated to include databases for Messaging (notification-service)
-- and Wallet (subscription-service) which are co-located in their parent DBs.

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

-- ─────────────────────────────────────────────────────────────────────────────
-- NOTE: All new tables below are auto-created by Hibernate (ddl-auto: update)
-- They are listed here for documentation / manual schema management purposes.
-- ─────────────────────────────────────────────────────────────────────────────

-- hireconnect_jobs (job-service)
--   tables: jobs, job_skills, bookmarks, job_views

-- hireconnect_profile (profile-service)
--   tables: candidate_profiles, candidate_skills, recruiter_profiles,
--           parsed_resumes, parsed_resume_skills, parsed_resume_education,
--           parsed_resume_experience, parsed_resume_certifications,
--           team_members

-- hireconnect_notifications (notification-service)
--   tables: notifications, messages

-- hireconnect_subscriptions (subscription-service)
--   tables: subscriptions, invoices, wallets, wallet_transactions
