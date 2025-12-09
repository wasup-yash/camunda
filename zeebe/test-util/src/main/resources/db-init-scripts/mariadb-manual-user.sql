-- Create user with restricted privileges for manual user testing
CREATE USER 'camunda_user'@'%' IDENTIFIED BY 'Camunda_Pass123!';

-- Grant necessary privileges for Camunda operations on the camunda database
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES, CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE, ALTER ROUTINE, TRIGGER ON camunda.* TO 'camunda_user'@'%';

-- Flush privileges to ensure they take effect
FLUSH PRIVILEGES;
