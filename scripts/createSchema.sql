CREATE SCHEMA dina_user;

GRANT USAGE ON SCHEMA dina_user TO $spring_datasource_username;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA dina_user TO $spring_datasource_username;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA dina_user TO $spring_datasource_username;

alter default privileges in schema dina_user grant SELECT, INSERT, UPDATE, DELETE on tables to $spring_datasource_username;
alter default privileges in schema dina_user grant all on sequences to $spring_datasource_username;
