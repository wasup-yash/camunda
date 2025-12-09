-- Create database for Camunda
CREATE DATABASE camunda_manual;

-- Create user with restricted privileges
CREATE USER 'camunda_user'@'%' IDENTIFIED BY 'camunda_pass';

-- Grant necessary privileges for Camunda operations
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES, CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE, ALTER ROUTINE, TRIGGER ON camunda_manual.* TO 'camunda_user'@'%';

-- Flush privileges to ensure they take effect
FLUSH PRIVILEGES;
