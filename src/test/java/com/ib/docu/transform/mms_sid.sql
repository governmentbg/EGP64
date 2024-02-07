select * from sid

--------------------------------------------------------------------------------
-- Спортна федерация /НОУС
select * from mms_sport_obedinenie where vid in (1,3)
select * from sid where object like 'obedinenie'
//delete from sid where object = 'obedinenie'
insert into sid (object, next_val) values ('obedinenie', 214)

-- НОСТД
select * from mms_sport_obedinenie where vid = 2
select * from sid where object like 'obed_nostd'
delete from sid where object = 'obed_nostd'
insert into sid (object, next_val) values ('obed_nostd', 2)

-- Обединен спортен клуб
select * from mms_sport_obedinenie where vid = 4
select * from sid where object like 'obed_osk'
delete from sid where object = 'obed_osk'
insert into sid (object, next_val) values ('obed_osk', 308)

--------------------------------------------------------------------------------
--------------------------------------------------------------------------------

-- Спортен клуб
select * from mms_sport_formirovanie where vid = 1
select * from sid where object like 'formir.%'

-- Туристическо дружество
select * from mms_sport_formirovanie where vid = 2
select * from sid where object like 'turist_druj'
delete from sid where object like 'turist_druj'
insert into sid (object, next_val) values ('turist_druj', 136)

--------------------------------------------------------------------------------
--------------------------------------------------------------------------------


-- Регистър треньорски кадри - оправяне на брояча
select * from mms_coaches where reg_nomer = '5995'

