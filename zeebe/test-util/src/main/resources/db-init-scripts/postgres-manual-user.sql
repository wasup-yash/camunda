-- Create database for Camunda
CREATE DATABASE camunda_manual;

-- Connect to the new database
\c camunda_manual

-- Create user with restricted privileges
CREATE USER camunda_user WITH PASSWORD 'camunda_pass';

-- Grant necessary privileges for Camunda operations
GRANT CONNECT ON DATABASE camunda_manual TO camunda_user;
GRANT CREATE ON DATABASE camunda_manual TO camunda_user;
GRANT USAGE ON SCHEMA public TO camunda_user;
GRANT CREATE ON SCHEMA public TO camunda_user;

-- Grant all privileges on tables, sequences, and functions in public schema
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO camunda_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO camunda_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO camunda_user;

-- Grant privileges on existing objects (if any)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO camunda_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO camunda_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO camunda_user;
