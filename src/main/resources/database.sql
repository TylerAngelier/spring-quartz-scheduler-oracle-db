-- ============================================================
-- 1) If you want a *common* user across the entire multitenant CDB:
--    connect to the root container and prefix name with C## 
-- ============================================================
-- CONNECT SYS/YourSysPassword@YourCDBRoot AS SYSDBA;

CREATE USER APP_USER 
  IDENTIFIED BY "spring-quartz-scheduler-oracle-db"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- ============================================================
-- 2) Grant the object‐creation and usage privileges
-- ============================================================
-- These grants allow APP_DEVELOPER role to create and manage
-- common schema objects like tables, views, indexes, etc.

CREATE ROLE APP_DEVELOPER;

GRANT CREATE SESSION          TO APP_DEVELOPER;
GRANT CREATE TABLE            TO APP_DEVELOPER;
GRANT CREATE VIEW             TO APP_DEVELOPER;
GRANT CREATE PROCEDURE        TO APP_DEVELOPER;
GRANT CREATE TRIGGER          TO APP_DEVELOPER;
GRANT CREATE TYPE             TO APP_DEVELOPER;
GRANT CREATE INDEX            TO APP_DEVELOPER;

-- Optional: if your application uses JSON or XML types:
GRANT CREATE ANY CONTEXT      TO APP_DEVELOPER;  -- for TEXT indexes
GRANT UNLIMITED TABLESPACE    TO APP_DEVELOPER;  -- if you want no tablespace quota checks

-- ============================================================
-- 3) Grant app developer role to schema
-- ============================================================
-- These grants allow APP_USER to create and manage
-- common schema objects like tables, views, indexes, etc.

GRANT APP_DEVELOPER TO APP_USER;

-- ============================================================
-- 4) Verify grants
-- ============================================================
SELECT PRIVILEGE
  FROM   DBA_SYS_PRIVS
  WHERE  GRANTEE = 'APP_USER'
ORDER BY PRIVILEGE;

