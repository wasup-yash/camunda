-- Create user with restricted privileges for manual user testing
CREATE USER camunda_user WITH PASSWORD 'Camunda_Pass123!';

-- Grant all privileges on camunda database to camunda_user
GRANT ALL PRIVILEGES ON DATABASE camunda TO camunda_user;
