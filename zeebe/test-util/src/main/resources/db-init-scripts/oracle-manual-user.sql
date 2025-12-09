-- Connect to pluggable database
ALTER SESSION SET CONTAINER = FREEPDB1;

-- Create user with restricted privileges for manual user testing (user acts as schema in Oracle)
CREATE USER camunda_user IDENTIFIED BY "Camunda_Pass123!"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant necessary system privileges for Camunda operations
GRANT CREATE SESSION TO camunda_user;
GRANT CREATE TABLE TO camunda_user;
GRANT CREATE VIEW TO camunda_user;
GRANT CREATE SEQUENCE TO camunda_user;
GRANT CREATE PROCEDURE TO camunda_user;
GRANT CREATE TRIGGER TO camunda_user;
GRANT CREATE TYPE TO camunda_user;
GRANT CREATE SYNONYM TO camunda_user;
