-- Create database for Camunda
CREATE DATABASE camunda_manual;
GO

-- Switch to the new database
USE camunda_manual;
GO

-- Create login and user with restricted privileges
-- Note: Password is hardcoded for testing purposes only
CREATE LOGIN camunda_user WITH PASSWORD = 'Camunda_Pass123!';
GO

CREATE USER camunda_user FOR LOGIN camunda_user;
GO

-- Grant necessary privileges for Camunda operations
GRANT CREATE TABLE TO camunda_user;
GRANT CREATE VIEW TO camunda_user;
GRANT CREATE PROCEDURE TO camunda_user;
GRANT CREATE FUNCTION TO camunda_user;
GRANT ALTER TO camunda_user;
GRANT SELECT TO camunda_user;
GRANT INSERT TO camunda_user;
GRANT UPDATE TO camunda_user;
GRANT DELETE TO camunda_user;
GRANT EXECUTE TO camunda_user;
GRANT REFERENCES TO camunda_user;
GO

-- Add user to db_ddladmin role for schema management (needed for Liquibase)
ALTER ROLE db_ddladmin ADD MEMBER camunda_user;
GO

-- Add user to db_datareader and db_datawriter roles
ALTER ROLE db_datareader ADD MEMBER camunda_user;
ALTER ROLE db_datawriter ADD MEMBER camunda_user;
GO
