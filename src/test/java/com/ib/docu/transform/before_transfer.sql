-- Нулират се броячите
delete from SID
GO

-- 0. ММС таблиците

delete from mms_vpisvane_doc
go
delete from mms_vpisvane
go
delete from mms_dop_pol
go
delete from mms_adm_etal_dop
go
delete from mms_vid_sport
go
delete from mms_chlenstvo
go
delete from mms_coaches_diploms
go
delete from mms_coaches
go
delete from mms_sport_formirovanie
go
delete from mms_sport_obed_mf
go
delete from mms_sport_obedinenie
go
delete from mms_sport_obekt_lice
go
delete from mms_sport_obekt
go


DROP SEQUENCE seq_mms_adm_etal_dop
go
DROP SEQUENCE seq_mms_chlenstvo
go
DROP SEQUENCE seq_mms_coaches
go
DROP SEQUENCE seq_mms_coaches_diploms
go
DROP SEQUENCE seq_mms_dop_pol
go
DROP SEQUENCE seq_mms_sport_formirovanie
go
DROP SEQUENCE seq_mms_sport_obed_mf
go
DROP SEQUENCE seq_mms_sport_obedninenie
go
DROP SEQUENCE seq_mms_sport_obekt
go
DROP SEQUENCE seq_mms_sport_obekt_lice
go
DROP SEQUENCE seq_mms_vid_sport
go
DROP SEQUENCE seq_mms_vpisvane
go
DROP SEQUENCE seq_mms_vpisvane_doc
go


CREATE SEQUENCE seq_mms_adm_etal_dop INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_chlenstvo INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_coaches INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_coaches_diploms INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_dop_pol INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_sport_formirovanie INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_sport_obed_mf INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_sport_obedninenie INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_sport_obekt INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_sport_obekt_lice INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_vid_sport INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_vpisvane INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go
CREATE SEQUENCE seq_mms_vpisvane_doc INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
go

-- 1.3. Изтривам съдържанието на класификации, които трябва да се дефинират при клиента
delete from SYSCLASSIF_MULTILANG where TEKST_KEY in (select TEKST_KEY from SYSTEM_CLASSIF where CODE_CLASSIF in (126,166,171,172,174,176))
GO
delete from SYSTEM_CLASSIF where CODE_CLASSIF in (126,166,171,172,174,176)
GO

-- 1.4. Зачистване на история и нулиране на датите от-до
delete from SYSTEM_CLASSIF where DATE_DO is not null
GO
update SYSTEM_CLASSIF set DATE_OT = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_REG = -1, DATE_REG = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_LAST_MOD = null, DATE_LAST_MOD = null
GO
update SYSTEM_CLASSIF_OPIS set USER_REG = -1, DATE_REG = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_LAST_MOD = null, DATE_LAST_MOD = null
GO
update SYSLOGIC_LIST set USER_REG = -1, DATE_REG = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_LAST_MOD = null, DATE_LAST_MOD = null
GO
update SYSLOGIC_LIST_OPIS set USER_REG = -1, DATE_REG = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_LAST_MOD = null, DATE_LAST_MOD = null
GO

-- 2. РЕФЕРЕНТИ+
-- Изтрива се всичко
delete from ADM_REF_ADDRS where code_ref in (select code from ADM_REFERENTS where ref_type in (3,4))
GO
delete from ADM_REFERENTS where ref_type in (3,4)
GO
delete from adm_ref_delegations
GO
delete from USER_NOTIFICATIONS
GO
delete from LOCK_OBJECTS
GO
delete from SYSTEM_JOURNAL
GO
-- Пресъздават се и SEQUENCE-ите
DROP SEQUENCE SEQ_USER_MESSAGES
GO
DROP SEQUENCE SEQ_SYSTEM_JOURNAL
GO
DROP SEQUENCE seq_adm_ref_delegations
GO
CREATE SEQUENCE SEQ_USER_MESSAGES INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_SYSTEM_JOURNAL INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE seq_adm_ref_delegations INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO

-- 14. ПРОЦЕДУРИ
delete from praznici
go
delete from proc_exe_task
go
delete from proc_exe_etap
go
delete from proc_exe
go
DROP SEQUENCE seq_praznici
go
DROP SEQUENCE seq_proc_exe_task
go
DROP SEQUENCE seq_proc_exe_etap
go
DROP SEQUENCE seq_proc_exe
go
create sequence seq_praznici increment by 1 minvalue 0 maxvalue 2147483647 start with 1 cache 1 no cycle
go
create sequence seq_proc_exe_task increment by 1 minvalue 0 maxvalue 2147483647 start with 1 cache 1 no cycle
go
create sequence seq_proc_exe_etap increment by 1 minvalue 0 maxvalue 2147483647 start with 1 cache 1 no cycle
go
create sequence seq_proc_exe increment by 1 minvalue 0 maxvalue 2147483647 start with 1 cache 1 no cycle
go

-- 4. ЗАДАЧИ
-- Изтрива се всичко
delete from TASK_SCHEDULE
GO
delete from TASK_STATES
GO
delete from TASK_REFERENTS
GO
delete from TASK
GO

-- Пресъздават се и SEQUENCE-ите
DROP SEQUENCE SEQ_TASK_SCHEDULE
GO
DROP SEQUENCE SEQ_TASK_STATES
GO
DROP SEQUENCE SEQ_TASK_REFERENTS
GO
DROP SEQUENCE SEQ_TASK
GO
CREATE SEQUENCE SEQ_TASK_SCHEDULE INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_TASK_STATES INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_TASK_REFERENTS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_TASK INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO


-- 5. ПРЕПИСКИ
-- Изтрива се всичко
delete from DELO_ACCESS
GO
delete from DELO_ACCESS_ALL
GO
delete from DELO_DELO
GO
delete from DELO_DOC
GO
delete from DELO_DVIJ
GO
delete from DELO_ARCHIVE
GO
delete from DELO_STORAGE
GO
delete from DELO
GO
-- Пресъздават се и SEQUENCE-ите
DROP SEQUENCE SEQ_DELO_ACCESS
GO
DROP SEQUENCE SEQ_DELO_DELO
GO
DROP SEQUENCE SEQ_DELO_DOC
GO
DROP SEQUENCE SEQ_DELO_DVIJ
GO
DROP SEQUENCE SEQ_DELO_ARCHIVE
GO
DROP SEQUENCE SEQ_DELO_STORAGE
GO
DROP SEQUENCE SEQ_DELO
GO
CREATE SEQUENCE SEQ_DELO_ACCESS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO_DELO INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO_DOC INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO_DVIJ INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO_ARCHIVE INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO_STORAGE INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DELO INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO


-- 6. ДОКУМЕНТИ
-- Изтрива се всичко
delete from DOC_ACCESS
GO
delete from DOC_ACCESS_ALL
GO
delete from DOC_DOC
GO
delete from DOC_DVIJ
GO
delete from DOC_DESTRUCT
GO
delete from DOC_TOPIC
GO
delete from DOC_PRIL
GO
delete from DOC_REFERENTS
GO
delete from DOC_DOPDATA
GO
delete from DOC_MEMBERS
GO
delete from DOC
GO
update doc_vid_settings set proc_def_in = null, proc_def_own = null, proc_def_work = null
GO
-- Пресъздават се и SEQUENCE-ите
DROP SEQUENCE SEQ_DOC_ACCESS
GO
DROP SEQUENCE SEQ_DOC_DOC
GO
DROP SEQUENCE SEQ_DOC_DVIJ
GO
DROP SEQUENCE SEQ_DOC_DESTRUCT
GO
DROP SEQUENCE SEQ_DOC_PRIL
GO
DROP SEQUENCE SEQ_DOC_REFERENTS
GO
DROP SEQUENCE SEQ_DOC_DOPDATA
GO
DROP SEQUENCE SEQ_DOC_MEMBERS
GO
DROP SEQUENCE SEQ_DOC
GO
CREATE SEQUENCE SEQ_DOC_ACCESS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_DOC INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_DVIJ INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_DESTRUCT INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_PRIL INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_REFERENTS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_DOPDATA INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC_MEMBERS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_DOC INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO

-- 8. ФАЙЛОВЕ
-- Изтрива се всичко
delete from FILE_OBJECTS
GO
delete from FILES
GO
-- Пресъздават се и SEQUENCE-ите
DROP SEQUENCE SEQ_FILE_OBJECTS
GO
DROP SEQUENCE SEQ_FILES
GO
CREATE SEQUENCE SEQ_FILE_OBJECTS INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO
CREATE SEQUENCE SEQ_FILES INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH 1 CACHE 1 NO CYCLE
GO

-- 12. КВАРЦ ТАБЛИЦИ
-- !!! Ако трябва да се трият се пуска това:
delete from JOB_HISTORY
GO
delete from QRTZ_BLOB_TRIGGERS
GO
delete from QRTZ_CALENDARS
GO
delete from QRTZ_CRON_TRIGGERS
GO
delete from QRTZ_FIRED_TRIGGERS
GO
delete from QRTZ_LOCKS
GO
delete from QRTZ_PAUSED_TRIGGER_GRPS
GO
delete from QRTZ_SCHEDULER_STATE
GO
delete from QRTZ_SIMPLE_TRIGGERS
GO
delete from QRTZ_SIMPROP_TRIGGERS
GO
delete from QRTZ_TRIGGERS
GO
delete from QRTZ_JOB_DETAILS
GO

-- 13. ОРГАНИЦИРАНИ СЪБИТИЯ
delete from event_referents
go
delete from event_resources
go
delete from events
go
DROP SEQUENCE seq_events
GO
create sequence seq_events increment by 1 minvalue 0 maxvalue 2147483647 start with 1 cache 1 no cycle
go


-- 16. SEQ НА КОИТО ТРЯБВА ДА СЕ СМЕНИ NOCACHE, ЗАЩОТО ГОРЕ НЕ СЕ ПРЕСЪЗДАВАТ
alter sequence seq_mail_patterns cache 1
GO
alter sequence seq_mail_patterns_variables cache 1
GO
alter sequence seq_sysclassif_multilang cache 1
GO
alter sequence seq_syslogic_list cache 1
GO
alter sequence seq_syslogic_list_opis cache 1
GO
alter sequence seq_system_classif cache 1
GO
alter sequence seq_system_classif_opis cache 1
GO
alter sequence seq_system_options cache 1
GO


-- пассворд невер експире и други
update adm_users set pass_last_change = TO_DATE('2222-01-01','YYYY-MM-DD'), status_explain = null, status = 2, email = null, names = 'Системен служител' where user_id = -1
go


-- !!! СИСТЕМНИТЕ НАСТРОЙКИ !!!
update system_options set USER_REG = -1, DATE_REG = TO_DATE('01.01.1901','DD.MM.YYYY'), USER_LAST_MOD = null, DATE_LAST_MOD = null
go

-- ЗАПИС НА ВЕРСИЯТА
delete from version_table -- TODO може да се дропне
go
insert into version_table(current_version, upgrade_time) 
values('1.01', current_timestamp)
go

-- Изтриване на всички тестове от логнати в мобилното
delete from mobile_logins -- TODO може да се дропне
go

delete from egov_messages_files
go
delete from egov_messages_coresp
go
delete from egov_messages
go

WITH tbl AS
  (SELECT table_schema,
          TABLE_NAME
   FROM information_schema.tables
   WHERE TABLE_NAME not like 'pbc_%' and TABLE_NAME not like 'v_%' and TABLE_NAME not like 'ekatte_%' 
     AND table_schema in ('mms_migr'))
SELECT table_schema,
       TABLE_NAME,
       (xpath('/row/c/text()', query_to_xml(format('select count(*) as c from %I.%I', table_schema, TABLE_NAME), FALSE, TRUE, '')))[1]::text::int AS rows_n
FROM tbl
where TABLE_NAME not like 'reg_%' and TABLE_NAME not like 'egov_%' and TABLE_NAME not like 'model_%'
ORDER BY rows_n DESC;
