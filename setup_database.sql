-- Run this as SYSDBA or a user with admin privileges
CREATE
USER dictionary_user IDENTIFIED BY password;
GRANT CONNECT, RESOURCE TO dictionary_user;
GRANT
CREATE
SESSION TO dictionary_user;
GRANT
CREATE TABLE TO dictionary_user;
GRANT
UNLIMITED
TABLESPACE TO dictionary_user;

