-- Create user with restricted privileges
CREATE USER camunda_user WITH PASSWORD 'camunda_pass';

-- Create database for Camunda
CREATE DATABASE camunda_manual OWNER camunda_user;

-- Grant connection privilege
GRANT ALL PRIVILEGES ON DATABASE camunda_manual TO camunda_user;
