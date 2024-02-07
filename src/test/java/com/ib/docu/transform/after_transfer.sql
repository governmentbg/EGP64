-- за спортните обединения да се оправят военен и олимпйски

UPDATE
    mms_sport_obedinenie ss
SET  
    type_sport = (SELECT 1
FROM 
    mms_vid_sport vs,
    adm_vid_sport a
WHERE
   vs.id_object=s.id
 and vs.tip_object=91
AND a.vid_sport=vs.vid_sport
and a.olimp=1 having count (a.olimp)>0),
    
    voenen_sport =(SELECT 1
FROM 
    mms_vid_sport vs,
    adm_vid_sport a
WHERE
   vs.id_object=s.id
 and vs.tip_object=91
AND a.vid_sport=vs.vid_sport
and a.voenen=1 having count (a.voenen)>0)

from mms_sport_obedinenie s where s.id=ss.id


update mms_sport_obedinenie set type_sport = 2 where type_sport is null and vid = 1
update mms_sport_obedinenie set voenen_sport = 2 where voenen_sport is null and vid = 1


-----------
UPDATE
    mms_sport_formirovanie ss
SET  
    type_sport = (SELECT 1
FROM 
    mms_vid_sport vs,
    adm_vid_sport a
WHERE
   vs.id_object=s.id
 and vs.tip_object=96
AND a.vid_sport=vs.vid_sport
and a.olimp=1 having count (a.olimp)>0),
    
    voenen_sport =(SELECT 1
FROM 
    mms_vid_sport vs,
    adm_vid_sport a
WHERE
   vs.id_object=s.id
 and vs.tip_object=96
AND a.vid_sport=vs.vid_sport
and a.voenen=1 having count (a.voenen)>0)

from mms_sport_formirovanie s where s.id=ss.id

update mms_sport_formirovanie set type_sport = 2 where type_sport is null and vid = 1
update mms_sport_formirovanie set voenen_sport = 2 where voenen_sport is null and vid = 1


-----------





-- трябва да се махне достъпа до тези менюта !?!?!
delete from adm_user_roles where code_classif = 7 and code_role in (116, 119, 122, 125)
go
delete from adm_group_roles where code_classif = 7 and code_role in (116, 119, 122, 125)
go

-- разни заявления от егов трябва да се напаравят нерегистрирани
update egov_messages set msg_status = 'DS_WAIT_REGISTRATION' where doc_subject = 'Тестов - НОВ'
go


-- Обединенията не трябва да се хващат от регикс първи !!! date_last_mod=now за всички
update adm_referents set date_last_mod = current_timestamp where ref_type = 3
update adm_referents set user_last_mod = -1 where ref_type = 3 and user_last_mod is null


-- За формированията трябва да се направи да е 30 дни назад за да ги хване
update adm_referents set date_last_mod = date_last_mod - INTERVAL '30 DAY' where code in (
select distinct r.code from mms_sport_formirovanie f inner join adm_referents r on r.code = f.id_object
)



-- Трябва да се наслагат правилните шаблони на УД !!!!
select * from v_system_classif where code_classif = 104 and code in (
select doc_vid from doc_vid_settings where id in (
select object_id from file_objects where object_code = 58))

