package com.ib.docu.system;

import com.ib.indexui.system.Constants;

/**
 * Константи за проекта DocuWork
 *
 * @author belev
 */
public class DocuConstants extends Constants {

	/** тил на логване в състемта */
	public static final String	LOGIN_TYPE			= "DOCU_WORK_LOGIN_TYPE";
	/** тип на логване използвайки LDAP */
	public static final String	LOGIN_TYPE_LDAP		= "DOCU_WORK_LOGIN_TYPE_LDAP";
	/** тип на логване използвайки база данни */
	public static final String	LOGIN_TYPE_DATABASE	= "DOCU_WORK_TYPE_DATABASE";
	/** дефолтен домайн на потребителите, които влизат чрез LDAP протокол */
	public static final String	DEFAULT_LDAP_DOMAIN	= "DOCU_WORK_DEFAULT_LDAP_DOMAIN";
	
	// Кодове за обекти в MMS  - подлежат на промяна 
	public static final int SPROTNO_FORMIROVANIE_OBJ = 1;
	public static final int CODE_ZNACHENIE_SPORT_OBEKT_OBJ = 101;
	
	// К О Н С Т А Н Т И ,    за невидимо обновяване на работния плот 
    public static final int CODE_FAKE_NOTIF_RELOAD_DELOVOD =  -1111;
    public static final int CODE_FAKE_NOTIF_RELOAD_DOC =  -2222;
    public static final int CODE_FAKE_NOTIF_RELOAD_TASK =  -3333;
    public static final int CODE_FAKE_NOTIF_RELOAD_ALL =  -3333;
    public static final int CODE_FAKE_NOTIF_RELOAD_FILES_SIGN =  -5555;
	
	
    // К О Н С Т А Н Т И ,   за дейности на уеб сървиса за подписване на WORD документи 
    public static final int CODE_WS_DEIN_ZA_DELOV =  1;
    public static final int CODE_WS_DEIN_ZA_SAGL =  2;
    public static final int CODE_WS_DEIN_ZA_PODPIS =  3;
    
	
	
	// К О Н С Т А Н Т И ,    за експорт 
    public static final int EXPORT_HTML = 0;
    public static final int EXPORT_WORD = 1;
    public static final int EXPORT_EXCEL = 2;

	/** Код на класификация "Тип участник" */
	public static final int CODE_CLASSIF_REF_TYPE = 101;

	/** Код на класификация "Валидност на документ" */
	public static final int CODE_CLASSIF_DOC_VALID = 102;

	/** Код на класификация "Вид алгоритъм за рег.№" */
	public static final int CODE_CLASSIF_ALG = 103;

	/** Код на класификация "Вид документ" */
	public static final int CODE_CLASSIF_DOC_VID = 104;

	/** Код на класификация "Вид задача" */
	public static final int CODE_CLASSIF_TASK_VID = 105;

	/** Код на класификация "Вид носител" */
	public static final int CODE_CLASSIF_MEDIA_TYPE = 106;

	/** Код на класификация "Вид периодичност на задача" */
	public static final int CODE_CLASSIF_TASK_PERIOD = 107;

	/** Код на класификация "Вид раздел на преписка" */
	public static final int CODE_CLASSIF_DELO_SECTION = 108;

	/** Код на класификация "Спешен" */
	public static final int CODE_CLASSIF_URGENT = 109;

	/** Код на класификация "Мнение при приключване на задача" */
	public static final int CODE_CLASSIF_TASK_OPINION = 110;

	/** Код на класификация "Начин на обработка на документ" */
	public static final int CODE_CLASSIF_DOC_PROCEED = 111;

	/** Код на класификация "Начин на предаване/получаване" */
	public static final int CODE_CLASSIF_DVIJ_METHOD = 160; // логическата се използва 

	/** Код на класификация "Начин на приключване на номенклатурно дело" */
	public static final int CODE_CLASSIF_NOM_DELO_PRIKL = 113;

	/** Код на класификация "Административна структура за справки (+напуснали)" */
	public static final int CODE_CLASSIF_ADMIN_STR_REPORTS = 114;

	/** Код на класификация "Предназначение на файл" */
	public static final int CODE_CLASSIF_FILE_PURPOSE = 115;

	/** Код на класификация "Причина за нередовност на документ" */
	public static final int CODE_CLASSIF_DOC_IRREGULAR = 116;

	/** Код на класификация "Роля в задачата" */
	public static final int CODE_CLASSIF_TASK_REF_ROLE = 117;

	/** Код на класификация "Роля на референт в документ" */
	public static final int CODE_CLASSIF_DOC_REF_ROLE = 118;

	/** Код на класификация "Срок за съхранение" */
	public static final int CODE_CLASSIF_SHEMA_PERIOD = 119;

	/** Код на класификация "Статус на обработка на документ" */
	public static final int CODE_CLASSIF_DOC_STATUS = 120;

	/** Код на класификация "Статус на задача" */
	public static final int CODE_CLASSIF_TASK_STATUS = 121;

	/** Код на класификация "Статус на изпълнение на етап" */
	public static final int CODE_CLASSIF_ETAP_STAT = 122;

	/** Код на класификация "Статус на изпълнение на услуга/процедура" */
	public static final int CODE_CLASSIF_PROC_STAT = 123;

	/** Код на класификация "Ръководни длъжности" */
	public static final int CODE_CLASSIF_BOSS_POSITIONS = 124;

	/** Код на класификация "Статус на преписка" */
	public static final int CODE_CLASSIF_DELO_STATUS = 125;

	/** Код на класификация "Тематика на документ" */
	public static final int CODE_CLASSIF_DOC_TOPIC = 126;

	/** Код на класификация "Тип e-mail" */
	public static final int CODE_CLASSIF_EMAIL_TYPE = 127;

	/** Код на класификация "Тип група на регистратура" */
	public static final int CODE_CLASSIF_REGISTRATURA_GROUP_TYPE = 128;

	/** Код на класификация "Тип документ" */
	public static final int CODE_CLASSIF_DOC_TYPE = 129;

	/** Код на класификация "Тип преписка" */
	public static final int CODE_CLASSIF_DELO_TYPE = 130;

	/** Код на класификация "Тип регистър" */
	public static final int CODE_CLASSIF_REGISTER_TYPE = 131;

	/** Код на класификация "Активни статуси на задача" */
	public static final int CODE_CLASSIF_TASK_STATUS_ACTIVE = 132;

	/** Код на класификация "Задачи за един изпълнител" */
	public static final int CODE_CLASSIF_TASK_ONE_EMPL = 133;

	/** Код на класификация "Задачи по подразбиране без срок" */
	public static final int CODE_CLASSIF_TASK_NO_DEADLINE = 134;

	/** Код на класификация "Задачи, които могат да се регистрират без документ" */
	public static final int CODE_CLASSIF_TASK_WITHOUT_DOC = 135;

	/** Код на класификация "Мнения, изискващи коментар" */
	public static final int CODE_CLASSIF_TASK_OPINION_WITH_COMMENT = 136;

	/** Код на класификация "Настройки на потребител" */
	public static final int CODE_CLASSIF_USER_SETTINGS = 137;

	/** Код на класификация "Документи, които не се регистрират през екрана за документ" */
	public static final int CODE_CLASSIF_DOC_NO_PAGE_REG = 138;

	/** Код на класификация "Дефинитивни права" */
	public static final int CODE_CLASSIF_DEF_PRAVA = 139;

	/** Код на класификация "Вид адрес" */
	public static final int CODE_CLASSIF_ADDR_TYPE = 140;

	/** Код на класификация "Вид документ в прикачен файл" */
	public static final int CODE_CLASSIF_DOC_VID_ATTACH = 141;

	/** Код на класификация "Допълнителни регистратури за достъп до обекти" */
	public static final int CODE_CLASSIF_REGISTRATURI_OBJACCESS = 142;

	/** Код на класификация "Допълнителни регистратури за заявка за извеждане на документи" */
	public static final int CODE_CLASSIF_REGISTRATURI_REQDOC = 143;

	/** Код на класификация "Регистри" */
	public static final int CODE_CLASSIF_REGISTRI = 144;

	/** Код на класификация "Вид връзка между документи" */
	public static final int CODE_CLASSIF_DOC_REL_TYPE = 145;

	/** Код на класификация "Регистратури" */
	public static final int CODE_CLASSIF_REGISTRATURI = 146;

	/** Код на класификация "Вид делегиране на права" */
	public static final int CODE_CLASSIF_DELEGATES = 147;

	/** Код на класификация "Регистри - сортирани за регистратура" - !винаги трябва да се пуска специфика по регистратура! */
	public static final int CODE_CLASSIF_REGISTRI_SORTED = 148;

	/** Код на класификация "Групи служители" */
	public static final int CODE_CLASSIF_GROUP_EMPL = 149;

	/** Код на класификация "Групи кореспонденти" */
	public static final int CODE_CLASSIF_GROUP_CORRESP = 150;

	/** Код на класификация "Настройки на регистратура" */
	public static final int CODE_CLASSIF_REISTRATURA_SETTINGS = 151;

	/** Код на класификация "Настройки по вид документ" */
	public static final int CODE_CLASSIF_DOC_VID_SETTINGS = 152;

	/** Код на класификация "Схеми за съхранение на документи" */
	public static final int CODE_CLASSIF_DOC_SHEMA = 153;
	
	/** Код на класификация "Статус на предаване на документ" */
	public static final int CODE_CLASSIF_DOC_PREDAVANE_STATUS = 154;
	
	/** Код на класификация "Роли на референт в нотификации" */
	public static final int CODE_CLASSIF_NOTIFF_ROLI = 155;
	
	/** Код на класификация "Събития за нотификации" */
	public static final int CODE_CLASSIF_NOTIFF_EVENTS= 156;
	
	/** Код на класификация "Активни нотификации" */
	public static final int CODE_CLASSIF_NOTIFF_EVENTS_ACTIVE = 157;
	
	/** Код на класификация "Променливи за настройка на мейл акаунт" */
	public static final int CODE_CLASSIF_VARIABLES_SETTINGS_MAIL_ACCOUNT= 158; 

	/** Код на класификация "Пощенски кутии" */
	public static final int CODE_CLASSIF_MAILBOXES= 159; 

	/** Код на класификация "Компетентност" */
	public static final int CODE_CLASSIF_COMPETENCE = 161; 

	/** Код на класификация "EDELIVERY_ORGANISATIONS" - ССЕВ */
	public static final int CODE_CLASSIF_EDELIVERY_ORGANISATIONS = 162; 

	/** Код на класификация "EGOV_ORGANISATIONS" - СЕОС */
	public static final int CODE_CLASSIF_EGOV_ORGANISATIONS = 163; 

	/** Код на класификация "Помощни текстове за допълнителна информация за задача*/
	public static final int CODE_CLASSIF_TASK_HELP_TEXT = 164; 

	/** Код на класификация "Специализирани справки*/
	public static final int CODE_CLASSIF_RPT_SPEC = 165; 

	/** Код на класификация "Специализирани справки, които се използват*/
	public static final int CODE_CLASSIF_RPT_SPEC_ACTIVE = 166; 

	/** Код на класификация "Дефинирани процедури*/
	public static final int CODE_CLASSIF_PROCEDURI = 167; 

	/** Код на класификация "Статус на дефиниция на процедура*/
	public static final int CODE_CLASSIF_PROC_DEF_STAT = 168; 

	/** Код на класификация "Тип стартиращ документ на етап от процедура*/
	public static final int CODE_CLASSIF_ETAP_DOC_MODE = 169; 

	/** Код на класификация "Бизнес роля в процедура" */
	public static final int CODE_CLASSIF_PROC_BUSINESS_ROLE = 170; 

	/** Код на класификация "Вид организирано събитие" */
	public static final int CODE_CLASSIF_VID_EVENT = 171; 

	/** Код на класификация "Споделени ресурси" */
	public static final int CODE_CLASSIF_EVENT_RESOURCES = 172; 
	
	/** Код на класификация "Размер на пощенски пликове" */
	public static final int CODE_CLASSIF_POST_ENVELOPS = 173; 
	
	/** Код на класификация "Роля на участник в документ" */
	public static final int CODE_CLASSIF_DOC_MEMBER_ROLES = 174; 

	/** Код на класификация "Характер на специализиран документ" */
	public static final int CODE_CLASSIF_CHARACTER_SPEC_DOC = 175; 	
	
	/** Код на класификация "Ресурси за включване в нотификация за събитие" */
	public static final int CODE_CLASSIF_EVENT_RESOURCES_NOTIF = 176; 	

	/** Код на класификация "Начин на подписване" */
	public static final int CODE_CLASSIF_DOC_SIGN_METHOD = 177; 	
	
	/** Код на класификация "Олимпийски спортове" */
	public static final int CODE_CLASSIF_VIDOVE_SPORT_OLIMP = 494; 		
	
	/** Код на класификация "Вид спортно обединение" */
	public static final int CODE_CLASSIF_VID_SPORT_OBEDINENIE = 496; 		
	
	/** Код на класификация "Статус на вписване" */
	public static final int CODE_CLASSIF_STATUS_REGISTRATION = 499; 	
	
	/** Код на класификация "Основания за статус на заявление" */
	public static final int CODE_CLASSIF_REASON_STATUS_ZAIAVLENIE = 500; 
	
	/**  Код на класификация  "Вид спортен обект"  */
	public static final int CODE_CLASSIF_VID_SPORTEN_OBEKT = 504; 
	
	/** Код на класификация "Вид на спортно формирование" */
	public static final int CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE = 505;
	
	/** Код на класификация "Длъжност" */
	public static final int CODE_CLASSIF_DLAJNOST = 506; 
	
	/**  Код на класификация  "Тип връзка лице- спортен обект"  */
	public static final int CODE_CLASSIF_VRAZKA_LICE_SPORTEN_OBEKT = 507; 
	
	/**  Код на класификация  "Функционална категория за  спортен обект"  */
	public static final int CODE_CLASSIF_FUNC_CATEGORIA_SPORTEN_OBEKT = 508;
	
	/**  Код на класификация  "Полза частна/обществена"  */
	public static final int CODE_CLASSIF_MMS_POLZA = 509;
	
	/**  Код на класификация  "Учебни заведения"  */
	public static final int CODE_CLASSIF_UCHEB_ZAVEDENIE = 510;
	
	/**  Код на класификация  "Международни федерации"  */
	public static final int CODE_CLASSIF_MMS_MEJD_FED = 511;
	
	/**  Код на класификация  "ММС - Видове спорт"  */
	public static final int CODE_CLASSIF_VIDOVE_SPORT = 512;
	
	/** Код на класификация "Статус на заявление"  */
	public static final int CODE_CLASSIF_STATUS_ZAIAVLENIE = 513; 
	
	/** Код на класификация "Вид документ за образователен ценз	" */
	public static final int CODE_CLASSIF_VID_DOC_OBR_ZENS = 514;
	
	/** Код на класификация "ММС - Статус на обект"  */
	public static final int CODE_CLASSIF_STATUS_OBEKT = 516; 

	/** Код на класификация "ММС - Военни спортове" */
	public static final int CODE_CLASSIF_VIDOVE_SPORT_VOEN = 517; 
	
	/** Код на класификация "ММС Вид документ - Вписване" */
	public static final int CODE_CLASSIF_VID_DOC_VPISVANE = 518;
	
	/** Код на класификация "ММС - Статус на вписване без Вписан" */
	public static final int CODE_CLASSIF_STATUS_VPISVANE_NO_VPISAN = 520;
	
	/** Код на класификация "ММС - Статус на заявление без В разглеждане" */
	public static final int CODE_CLASSIF_STATUS_ZAIAVLENIE_NO_V_RAZGLEJDANE = 521;
	
	/** Код на класификация "ММС - Пол на референт 522" */
	public static final int CODE_CLASSIF_REFERENT_POL = 522;

	
	/** Код на класификация "Държави" */
	public static final int CODE_CLASSIF_COUNTRIES = 22; 

	/** Код на класификация "Меню" */
	public static final int CODE_CLASSIF_MENU = 7;

	/** Код на логически списък "Роля на участник в задача – Статус, в който може да постави задачата" */
	public static final int CODE_LIST_TASK_REF_ROLE_TASK_STATUS = 2;

	/** Код на логически списък "Вид задача – Мнение при приключване на задача" */
	public static final int CODE_LIST_TASK_TYPE_TASK_OPINION = 3;

	/** Код на логически списък "Вид документ – Роля на участник в документ" */
	public static final int CODE_LIST_DOC_VID_MEMBER_ROLE = 4;
	
	/** Код на логически списък "Статус на заявление - основание за Спортно обединение" */
	public static final int CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTNO_OBEDINENIE = 49;
	
	/** Код на логически списък "Статус на заявление - основание за Спортно формирование" */
	public static final int CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTNO_FORMIROVANIE = 50;
	
	/** Код на логически списък "Статус на заявление - основание за Спортен обект" */
	public static final int CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTEN_OBEKT = 51;
	
	/** Код на логически списък "Статус на заявление - основание за Треньорски кадър" */
	public static final int CODE_LIST_REASON_STATUS_ZAIAVLENIE_COACH = 52;
	
	/** Код на логически списък "Статус на вписване - основание за Спортно обединение" */
	public static final int CODE_LIST_REASON_STATUS_VPISVANE_SPORTNO_OBEDINENIE = 53;
	
	/** Код на логически списък "Статус на вписване - основание за Спортно формирование" */
	public static final int CODE_LIST_REASON_STATUS_VPISVANE_SPORTNO_FORMIROVANIE = 54;
	
	/** Код на логически списък "Статус на вписване - основание за Спортен обект" */
	public static final int CODE_LIST_REASON_STATUS_VPISVANE_SPORTEN_OBEKT = 55;
	
	/** Код на логически списък "Статус на вписване - основание за Треньорски кадър" */
	public static final int CODE_LIST_REASON_STATUS_VPISVANE_COACH = 56;	
	
	
	// Константи за нотификации

	/** Код на значение "НОТИФИКАЦИЯ - непрочетена" */
	public static final int CODE_NOTIF_STATUS_NEPROCHETENA = 2;

	/** Код на значение "НОТИФИКАЦИЯ - прочетена" */
	public static final int CODE_NOTIF_STATUS_PROCHETENA = 1;

	// Значения от системни класификации

	// Системна класификация 2 - Информационни обекти (за журналиране)
	/** Документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC					= 51;
	/** Участник в процеса */
	public static final int	CODE_ZNACHENIE_JOURNAL_REFERENT				= 52;
	/** Преписка */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO					= 53;
	/** Регистратура */
	public static final int	CODE_ZNACHENIE_JOURNAL_REISTRATURA			= 54;
	/** Регистър */
	public static final int	CODE_ZNACHENIE_JOURNAL_REGISTER				= 55;
	/** Настройка за регистратура */
	public static final int	CODE_ZNACHENIE_JOURNAL_REISTRATURA_SETT		= 56;
	/** Група служители/кореспонденти */
	public static final int	CODE_ZNACHENIE_JOURNAL_REISTRATURA_GROUP	= 57;
	/** Характеристика на вид документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_VID_SETT			= 58;
	/** Задача */
	public static final int	CODE_ZNACHENIE_JOURNAL_TASK					= 59;
	/** Делегиране (заместване, упълномощаване) */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELEGATION			= 60;
	/** Вложен документ в преписка */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO_DOC				= 61;
	/** Схема за съхранение на документи */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_SHEMA			= 62;
	/** Кореспондент в група на регистратура */
	public static final int	CODE_ZNACHENIE_JOURNAL_REG_GROUP_CORRESP	= 63;
	/** Вложена преписка в преписка */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO_DELO			= 64;
	/** Връзка между документи */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_DOC				= 65;	
	/** Движение на документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_DVIJ				= 66;	
	/** Движение на дело/преписка */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO_DVIJ			= 67;
	/** Унищожени документи */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_DESTRUCT			= 68;	
	/** Архивирани номенклатурни дела */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO_ARCHIVE			= 69;	
	/** Шаблон за нотификации */
	public static final int	CODE_ZNACHENIE_JOURNAL_NOTIF_PATTERN		= 70;	
	/** Пощенска кутия */
	public static final int	CODE_ZNACHENIE_JOURNAL_MAILBOX				= 71;	
	/** СЕОС/ССЕВ съобщение */
	public static final int	CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE			= 72;	
	/** Съхранение на том на преписка */
	public static final int	CODE_ZNACHENIE_JOURNAL_DELO_STORAGE			= 73;	
	/** Дефиниция на периодична задача */
	public static final int	CODE_ZNACHENIE_JOURNAL_TASK_SCHEDULE		= 74; // ??	
	/** Приложение (на външен носител) към документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_PRIL				= 75;	
	/** Потребителкси сертификат */
	public static final int	CODE_ZNACHENIE_JOURNAL_USER_CERT			= 76;	
	/** Дефиниция на процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_DEF				= 77;	
	/** Дефиниция на етап от процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_DEF_ETAP		= 78;	
	/** Дефиниция на задача от етап на процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_DEF_TASK		= 79;	
	/** Изпълнение на процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_EXE				= 80;	
	/** Изпълнение на етап от процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_EXE_ETAP		= 81;	
	/** Задача от Изпълнение на етап от процедура */
	public static final int	CODE_ZNACHENIE_JOURNAL_PROC_EXE_TASK		= 82;	
	/** Задача от Организирано събитие */
	public static final int	CODE_ZNACHENIE_JOURNAL_EVENT				= 83;	
	/** Участник в документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_MEMBER			= 84;	
	/** Изричен достъп */
	public static final int	CODE_ZNACHENIE_JOURNAL_IZR_DOST				= 85;	
	/** Настройка за уеб услуга */
	public static final int	CODE_ZNACHENIE_JOURNAL_DOC_WS_OPT			= 86;	
	/** Изричен достъп дело*/
	public static final int	CODE_ZNACHENIE_JOURNAL_IZR_DOST_DELO		= 87;
	/** Празничен ден */
	public static final int	CODE_ZNACHENIE_JOURNAL_PRAZNIK				= 88;
	/** Нотификация */
	public static final int	CODE_ZNACHENIE_JOURNAL_NOTIF				= 89;	
	/** Опис */
	public static final int	CODE_ZNACHENIE_JOURNAL_OPISDOCDELO			= 90;	
	/** Спортно обединение */
	public static final int	CODE_ZNACHENIE_JOURNAL_SPORT_OBED			= 91;	
	/** Вписване */
	public static final int	CODE_ZNACHENIE_JOURNAL_REGISTRATION			= 92;	
	/** Вписване документ */
	public static final int	CODE_ZNACHENIE_JOURNAL_REGISTRATION_DOC		= 93;	
	/** Треньорски кадри */
	public static final int	CODE_ZNACHENIE_JOURNAL_COACHES				= 95;	
	/** Спортни формирования */
	public static final int	CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS	= 96;
	/** Спортни обекти */
	public static final int	 CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS		= 97;
	/** Международни федерации */
	public static final int	 CODE_ZNACHENIE_JOURNAL_MEJD_FED			= 98;
	/** Вид спорт */
	public static final int	 CODE_ZNACHENIE_JOURNAL_VID_SPORTS			= 99;
	/** Адм. вид спорт */
	public static final int CODE_ZNACHENIE_JOURNAL_ADM_VID_SPORT		= 100;
	/** Спортен обект лице */
	public static final int	 CODE_ZNACHENIE_JOURNAL_SPORT_OBJECT_LICE	= 101;
	/** Допълнително поле */
	public static final int	 CODE_ZNACHENIE_JOURNAL_DOP_POLE			= 102;

	/** Код на значение "Деловодител" класификация "Бизнес роля" 4 */
	public static final int CODE_ZNACHENIE_BUSINESS_ROLE_DELOVODITEL = 1;
	/** Код на значение "Администратор на процедури" класификация "Бизнес роля" 4 */
	public static final int CODE_ZNACHENIE_BUSINESS_ROLE_PROC_ADMIN  = 2;
	/** Код на значение "Администратор на събития" класификация "Бизнес роля" 4 */
	public static final int CODE_ZNACHENIE_BUSINESS_ROLE_EVENT_ADMIN  = 3;
	
	/** Код на значение  "Административна структура"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_ADM_STRUCT = 32;
	
	/** Код на значение  "Кореспонденти"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_CORESP = 33;

	/** Код на значение  "деловодни документи"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DELO_DOC = 67;
	/** Код на значение  "за получаване по e-mail"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_EMAIL = 68;
	/** Код на значение  "за получаване през СЕОС"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_CEOC = 69;
	/** Код на значение  "за получаване през e-връчване" класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_EL_VR = 70;
	/** Код на значение  "входящи за насочване" класификация"Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_NAS = 71;
	/** Код на значение  "за регистриране като официални"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_REG_OF = 72;
	/** Код на значение  "за приемане от други регистратури"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_DIF_REG = 73;
	/** Код на значение  "за изпращане по компетентност"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_DOC_COMPETENCE = 100;
	/** Код на значение  "документи"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_TASK_DOC = 74;
	/** Код на значение  "За резолюция"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_FOR_REZOL	= 75;
	/** Код на значение  "За съгласуване"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_FOR_SAGL	= 76;
	/** Код на значение  "За подпис"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_FOR_PODPIS= 77;
	/** Код на значение  "Нерегистрирани работни"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_NONREG= 78;	
	/** Код на значение  "Неизпълнени задачи "  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_TASK =79;
	/** Код на значение  "На които съм изпълнител"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_EXEC	= 80;
	/** Код на значение  "На които съм възложител"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_ASSIGN	= 81;
	/** Код на значение  "На които съм контролиращ"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_CONTROL= 82;
	/** Код на значение  "с изтичащ срок"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_GOING_LATE= 83;
	/** Код на значение  "с изтичащ срок"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_LATE= 84;
	/** Код на значение  "за запознаване"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_DASHBOARD_ZA_ZAPOZNAVANE= 102;
	
	/** Код на значение "звено" класификация "Тип участник" 101 */
	public static final int	CODE_ZNACHENIE_REF_TYPE_ZVENO	= 1;
	/** Код на значение "служител" класификация "Тип участник" 101 */
	public static final int	CODE_ZNACHENIE_REF_TYPE_EMPL	= 2;
	/** Код на значение "организация кореспондент" класификация "Тип участник" 101 */
	public static final int	CODE_ZNACHENIE_REF_TYPE_NFL		= 3;
	/** Код на значение "лице кореспондент" класификация "Тип участник" 101 */
	public static final int	CODE_ZNACHENIE_REF_TYPE_FZL		= 4;
	/** Код на значение "мигриран" класификация "Тип участник" 101 */
	public static final int	CODE_ZNACHENIE_REF_TYPE_MIG		= 5;

	/** Код на значение "действащ" класификация "Валидност на документ" */
	public static final int	CODE_CLASSIF_DOC_VALID_ACTUAL	= 1;
	/** Код на значение "архивиран" класификация "Валидност на документ" */
	public static final int	CODE_CLASSIF_DOC_VALID_ARCH		= 2;
	/** Код на значение "унищожен" класификация "Валидност на документ" */
	public static final int	CODE_CLASSIF_DOC_VALID_DESTRUCT	= 3;

	/** Код на значение "с индекс и стъпка" класификация "Вид алгоритъм за рег.№" 103 */
	public static final int	CODE_ZNACHENIE_ALG_INDEX_STEP	= 1;
	/** Код на значение "индекс по вид документ" класификация "Вид алгоритъм за рег.№" 103 */
	public static final int	CODE_ZNACHENIE_ALG_VID_DOC		= 2;
	/** Код на значение "произволен рег.номер" класификация "Вид алгоритъм за рег.№" 103 */
	public static final int	CODE_ZNACHENIE_ALG_FREE			= 3;
	
	/** Код на значение "Заявление за вписване на спортен обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT = 34;	
	/** Код на значение "Заявление за вписване на спортен клуб" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM = 35;
	/** Код на значение "Заявление за вписване на спортна федерация" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE = 36;
	/** Код на значение "Заявление за вписване на треньорски кадри" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI = 37;

	/** Код на значение "Заявление за вписване на ОСК" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK = 64;
	/** Код на значение "Заявление за вписване на НОСТД" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD = 66;
	/** Код на значение "Заявление за вписване на НОУС" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS = 63;
	/** Код на значение "Заявление за вписване на ТД" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD = 65;

	/** Код на значение "Заявление за промяна на обстоятелствата на спортен обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT = 51;	
	/** Код на значение "Заявление за промяна на обстоятелствата на СК и ТД" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM = 52;
	/** Код на значение "Заявление за промяна на обстоятелствата на СФ, ОСК, НОСТД, НОУС" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE = 53;
	/** Код на значение "Заявление за промяна на обстоятелства на треньорски кадри" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI = 54;
	
	/** Код на значение "Заявление за заличаване на спортен обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT = 38;	
	/** Код на значение "Заявление за заличаване на СК, ТД" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM = 39;
	/** Код на значение "Заявление за заличаване на СФ, ОСК, НОСТД, НОУС" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE = 40;
	/** Код на значение "Заявление за заличаване на треньорски кадри" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI = 41;
	
	/** Код на значение "Спортен лиценз или Удостоверение за вписване на НОУС/ОСЦ" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ = 42;
	/** Код на значение "Удостоверение за регистрация на спортно формирование" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV = 43;
	/** Код на значение "Удостоверение за регистрация на спортен обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT = 44;
	/** Код на значение "Удостоверение за регистрация на треньорски кадри" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR = 45;
	/** Код на значение "Уведомление за отказ на лиценз" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UVED_OTKAZ_LICENZ = 46;
	/** Код на значение "Уведомление за заличаване на лиценз" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UVED_ZALICH_LICENZ = 47;
	/** Код на значение "Уведомление за отказ от регистрация на формирование/обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UVED_OTKAZ_REG_FORMIROV_OBEKT = 48;
	/** Код на значение "Уведомление за заличаване на регистрация на спортно формирование" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UVED_ZALICH_REG_SPORT_FORMIROV = 49;
	/** Код на значение "Уведомление за заличаване на регистрация на спортен обект" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_UVED_ZALICH_REG_SPORT_OBEKT = 50;

	/** Код на значение "Заповед за вписване" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_VPISVANE = 56;
	/** Код на значение "Заповед за отказано вписване" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTKAZANO_VPISVANE = 57;
	/** Код на значение "Заповед за прекратено производство" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATENO_PROIZVODSTVO = 58;
	/** Код на значение "Заповед за оставяне без последствие" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_OSTAVIANE_BEZ_POSLEDSTVIA = 59;
	/** Код на значение "Заповед за заличаване" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_ZALICHAVANE = 60;
	/** Код на значение "Заповед за прекратяване на лиценз/удостоверение/вписване" класификация "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATIAVANE = 61;
	/** Код на значение "Заповед за отнемане на лиценз/удостоверение/вписване "Вид документ" 104 */
	public static final int CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTNEMANE = 62;	


	/** Код на значение "за изпълнение" (дефолтно) класификация "Вид задача" 105 */
	public static final int	CODE_ZNACHENIE_TASK_TYPE_DEFAULT	= 2;
	/** Код на значение "за съгласуване" класификация "Вид задача" 105 */
	public static final int	CODE_ZNACHENIE_TASK_TYPE_SAGL		= 5;
	/** Код на значение "за подпис" класификация "Вид задача" 105 */
	public static final int	CODE_ZNACHENIE_TASK_TYPE_PODPIS		= 6;
	/** Код на значение "за резолюция" класификация "Вид задача" 105 */
	public static final int	CODE_ZNACHENIE_TASK_TYPE_REZOL		= 1;

	/** Код на значение "дневна" класификация "Вид периодичност на задача" 107 */
	public static final int CODE_ZNACHENIE_TASK_PERIOD_DAY 		= 1;
	/** Код на значение "седмична" класификация "Вид периодичност на задача" 107 */
	public static final int CODE_ZNACHENIE_TASK_PERIOD_WEEK 	= 2;
	/** Код на значение "месечна" класификация "Вид периодичност на задача" 107 */
	public static final int CODE_ZNACHENIE_TASK_PERIOD_MONTH 	= 3;
	/** Код на значение "годишна" класификация "Вид периодичност на задача" 107 */
	public static final int CODE_ZNACHENIE_TASK_PERIOD_YEAR 	= 4;

	/** Код на значение "официален" класификация "Вид раздел на преписка" 108 */
	public static final int	CODE_ZNACHENIE_DELO_SECTION_OFFICIAL	= 1;
	/** Код на значение "вътрешен" класификация "Вид раздел на преписка" 108 */
	public static final int	CODE_ZNACHENIE_DELO_SECTION_INTERNAL	= 2;
	/** Код на значение "контролен" класификация "Вид раздел на преписка" 108 */
	public static final int	CODE_ZNACHENIE_DELO_SECTION_CONTROL		= 3;	
	
	/** Код на значение "нормално" класификация "Спешен" 109 */
	public static final int	CODE_ZNACHENIE_URGENT_NORMAL = 1;
	/** Код на значение "бързо" класификация "Спешен" 109 */
	public static final int	CODE_ZNACHENIE_URGENT_FAST = 2;
	/** Код на значение "много бързо" класификация "Спешен" 109 */
	public static final int	CODE_ZNACHENIE_URGENT_VERY_FAST = 3;
	
	/** Код на значение "съгласен" класификация "Мнение при приключване на задача" 110 */
	public static final int CODE_ZNACHENIE_TASK_OPINION_SAGL = 1;
	/** Код на значение "прекратявам процедурата" класификация "Мнение при приключване на задача" 110 */
	public static final int CODE_ZNACHENIE_TASK_OPINION_STOP_PROC = 3;	
	/** Код на значение "подписвам без забележки" класификация "Мнение при приключване на задача" 110 */
	public static final int CODE_ZNACHENIE_TASK_OPINION_PODPIS = 4;
	/** Код на значение "подписвам с особено мнение" класификация "Мнение при приключване на задача" 110 */
	public static final int CODE_ZNACHENIE_TASK_OPINION_PODPIS_MNENIE = 5;	

	/** Код на значение "основен документ" класификация "Предназначение на файл" 115 */
	public static final int	CODE_ZNACHENIE_FILE_PURPOSE_MAIN_DOC = 1;
	/** Код на значение "приложение" класификация "Предназначение на файл" 115 */
	public static final int	CODE_ZNACHENIE_FILE_PURPOSE_APPLICATION	= 2;
	/** Код на значение "помощен файл" класификация "Предназначение на файл" 115 */
	public static final int	CODE_ZNACHENIE_FILE_PURPOSE_HELP_FILE = 3;

	/** Код на значение "отговорен изпълнител" класификация "Роля в задачата" 117 */
	public static final int	CODE_ZNACHENIE_TASK_REF_ROLE_EXEC_OTG	= 1;
	/** Код на значение "изпълнител" класификация "Роля в задачата" 117 */
	public static final int	CODE_ZNACHENIE_TASK_REF_ROLE_EXEC		= 2;
	/** Код на значение "възложител" класификация "Роля в задачата" 117 */
	public static final int	CODE_ZNACHENIE_TASK_REF_ROLE_ASSIGN		= 3;
	/** Код на значение "контролиращ" класификация "Роля в задачата" 117 */
	public static final int	CODE_ZNACHENIE_TASK_REF_ROLE_CONTROL	= 4;

	/** Код на значение "автор" класификация "Роля на референт в документ" 118 */
	public static final int	CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR	= 1;
	/** Код на значение "съгласувал" класификация "Роля на референт в документ" 118 */
	public static final int	CODE_ZNACHENIE_DOC_REF_ROLE_AGREED	= 2;
	/** Код на значение "подписал" класификация "Роля на референт в документ" 118 */
	public static final int	CODE_ZNACHENIE_DOC_REF_ROLE_SIGNED	= 3;

	/** Код на значение "с определен срок на запазване" класификация "Срок за съхранение" 119 */
	public static final int	CODE_ZNACHENIE_SHEMA_PERIOD_DEFINED		= 2;

	/** Код на значение "неизпълнена" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_NEIZP	= 1;
	/** Код на значение "чака указания" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_INSTRUCTIONS	= 3;
	/** Код на значение "неприето изпълнение" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_NEPRIETA	= 5;
	/** Код на значение "изпълнена" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_IZP		= 6;
	/** Код на значение "изпълнена след срока" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_IZP_SROK	= 7;
	/** Код на значение "снета" класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_SNETA	= 8;
	/** Код на значение "приключена автоматично " класификация "Статус на задача" 121 */
	public static final int	CODE_ZNACHENIE_TASK_STATUS_AUTO_PRIKL	= 10;

	/** Код на значение "неприключена" класификация "Статус на преписка" 125 */
	public static final int	CODE_ZNACHENIE_DELO_STATUS_ACTIVE		= 1;
	/** Код на значение "приключена" класификация "Статус на преписка" 125 */
	public static final int	CODE_ZNACHENIE_DELO_STATUS_COMPLETED	= 2;
	/** Код на значение "продължена" класификация "Статус на преписка" 125 */
	public static final int	CODE_ZNACHENIE_DELO_STATUS_CONTINUED	= 3;
	/** Код на значение "предадена в УА" класификация "Статус на преписка" 125 */
	public static final int	CODE_ZNACHENIE_DELO_STATUS_ARCHIVED_UA		= 4;
	/** Код на значение "предадена в ДА" класификация "Статус на преписка" 125 */
	public static final int	CODE_ZNACHENIE_DELO_STATUS_ARCHIVED_DA		= 5;

	/** Код на значение "група служители" класификация "Тип група на регистратура" 128 */
	public static final int	CODE_ZNACHENIE_REG_GROUP_TYPE_EMPL		= 1;
	/** Код на значение "група кореспонденти" класификация "Тип група на регистратура" 128 */
	public static final int	CODE_ZNACHENIE_REG_GROUP_TYPE_CORRESP	= 2;
	/** Код на значение "група СЕОС" класификация "Тип група на регистратура" 128 */
	public static final int	CODE_ZNACHENIE_REG_GROUP_TYPE_SEOS		= 3;

	/** Код на значение "входящ" класификация "Тип документ" 129 */
	public static final int	CODE_ZNACHENIE_DOC_TYPE_IN	= 1;
	/** Код на значение "собствен" класификация "Тип документ" 129 */
	public static final int	CODE_ZNACHENIE_DOC_TYPE_OWN	= 2;
	/** Код на значение "работен" класификация "Тип документ" 129 */
	public static final int	CODE_ZNACHENIE_DOC_TYPE_WRK	= 3;

	/** Код на значение "номенклатурно дело" класификация "Тип преписка" 130 */
	public static final int	CODE_ZNACHENIE_DELO_TYPE_NOM	= 1;
	/** Код на значение "преписка" класификация "Тип преписка" 130 */
	public static final int	CODE_ZNACHENIE_DELO_TYPE_DOSS	= 2;


	/** Код на значение "автор" класификация "регистър за документи" 131 */
	public static final int	CODE_ZNACHENIE_REGISTER_DNEV	= 1;
	/** Код на значение "съгласувал" класификация "кореспондентска група" 131 */
	public static final int	CODE_ZNACHENIE_REGISTER_COR_GR	= 2;
	/** Код на значение "подписал" класификация "ОДР" 131 */
	public static final int	CODE_ZNACHENIE_REGISTER_ODR		= 3;
	
	
	/** Код на значение "чака указания" класификация "Активни статуси на задача" 132 */
	public static final int	CODE_ZNACHENIE_WAITING_INSTR	= 3;
	
	/** Код на значение "Дублиране на съобщения по e-mail" класификация "Настройки на потребител" 137 */
	public static final int CODE_ZNACHENIE_USER_SETT_DUBL_MAIL = 1;
	
	/** Код на значение "Пълен достъп за разглеждане" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_FULL_VIEW						= 1;
	/** Код на значение "Право на електронен подпис" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DIGITAL_SIGN					= 2;
	/** Код на значение "Право да изпраща по e-mail" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_SEND_MAIL						= 3;
	/** Код на значение "Право да вижда лични данни" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_SEE_PERSONAL_DATA				= 4;
	/** Код на значение "Право да делегира чужди права" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DELEGATE_FOREIGN				= 5;
	/** Код на значение "Право да упълномощава от свое име" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_AUTHORIZE						= 6;
	/** Код на значение "Право да приключва задачи на регистратурата" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_COMPLETE_REGISTRATURA_TASKS	= 7;
	/** Код на значение "Право да дава достъп до преписка през задача" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_GIVE_ACCESS_DELO_BY_TASK		= 8;
	/** Код на значение "Пълен достъп за актуализация на задачи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_TASK_FULL_EDIT					= 9;
	/** Код на значение "Пълен достъп за актуализация на преписки/дела и документи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DOC_DELO_FULL_EDIT				= 10;
	/** Код на значение "Право на групови дейности със задачи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_GROUP_TASK				= 11;
	/** Код на значение "Право на групови дейности с документи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_GROUP_DOC				= 12;
	/** Код на значение "Право на групови дейности с преписки" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_GROUP_DELO				= 13;
	/** Код на значение "Право да поддържа учрежденски архив" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_UA				= 14;
	/** Код на значение "Право да изтрива задачи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DELETE_TASK				= 15;
	/** Код на значение "Право да изтрива документи" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DELETE_DOC				= 16;
	/** Код на значение "Право да изтрива преписки" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DELETE_DELO				= 17;
	/** Код на значение "Право да регистрира задача" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_REG_TASK				= 18;
	/** Код на значение "Право да администрира Групи служители и Групи кореспонденти на всички регистратури" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_REG_GROUP_ADM				= 20;
	
	/** Код на значение "Право да изтрива обекти на регистрите" класификация "Дефинитивни права" 139 */
	public static final int	CODE_ZNACHENIE_DEF_PRAVA_DEL_MMS_REG						= 22;

	
	/** Код на значение "адрес за кореспонденция" класификация "Вид адрес" 140 */
	public static final int CODE_ZNACHENIE_ADDR_TYPE_CORRESP = 1;
	public static final int CODE_ZNACHENIE_ADDR_TYPE_POSTOQNEN = 2;

	/** Код на значение "има връзка с" класификация "Вид връзка между документи" 145 */
	public static final int CODE_ZNACHENIE_DOC_REL_TYPE_VRAZKA = 10;
	/** Код на значение "е резултат от задача по" класификация "Вид връзка между документи" 145 */
	public static final int CODE_ZNACHENIE_DOC_REL_TYPE_BY_TASK = 13;

	/** Код на значение "заместване" класификация"Вид делегиране на права" 147 */
	public static final int	CODE_ZNACHENIE_DELEGATES_ZAMESTVANE			= 1;
	/** Код на значение "упълномощаване" класификация"Вид делегиране на права" 147 */
	public static final int	CODE_ZNACHENIE_DELEGATES_UPYLNOMOSHTAVANE	= 2;

	/**
	 * Код на значение "Включен по подразбиране чек-бокс за генериране на рег. номер на документ" класификация "Настройки на
	 * регистратура" 151
	 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_1	= 1;
	/**
	 * Код на значение "Подписал в документ може да бъде само от собствена регистратура" класификация "Настройки на регистратура"
	 * 151
	 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_3	= 3;
	/** Код на значение "Допуска се въвеждане на съгласувал и подписал в работен документ" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_4	= 4;
	/** Код на значение "Поддържа се списък с участници в документа" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_7	= 7;
	/** Код на значение "В собствен документ да се поддържа за кого се отнася" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_8	= 8;
	/** Код на значение "Преписката се движи с документите" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_9	= 9;
	/**
	 * Код на значение "При приключване на преписка се приключват задачите към нейните документи и вложените преписки"
	 * класификация "Настройки на регистратура" 151
	 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_10	= 10;
	/**
	 * Код на значение "Документи и преписки по подразбиране са с ограничен достъп" класификация "Настройки на регистратура" 151
	 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_12	= 12;
	/** Код на значение "Тип на преписка по подразбиране" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_14	= 14;
	/** Код на значение "Да се допуска време в срок на задача" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_15	= 15;
	/** Код на значение "Изпълнител на задача може да бъде от друга регистратура" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_16	= 16;
	/** Код на значение "Начин на подписване на документ по подразбиране" класификация "Настройки на регистратура" 151 */
	public static final int	CODE_ZNACHENIE_REISTRATURA_SETTINGS_17	= 17;

	/** Код на значение "на ръка" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_NA_RAKA = 1;
	/** Код на значение "по поща" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_POSHTA = 2;
	/** Код на значение "по факс" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_FAX = 3;
	/** Код на значение "по електронна поща" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_EMAIL = 4;
	/** Код на значение "чрез куриерска фирма" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_KURIER = 5;
	/** Код на значение "чрез СЕОС" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_SEOS = 6;
	/** Код на значение "чрез ССЕВ" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_SSEV = 7;
	/** Код на значение "чрез уеб интерфейс за ЕАУ" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_WEB_EAU = 8;
	/** Код на значение "чрез системата" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_DRUGA_REGISTRATURA = 9;
	/** Код на значение "ел.поща (неинтегрирана)" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_MAIL_CUSTOM = 11;
	/** Код на значение "друг" класификация "Начин на предаване/получаване" 112 */
	public static final int	CODE_ZNACHENIE_PREDAVANE_DRUG = 12;
	
	// Класификация 252 - Роли на референт в нотификации
	/** Код на значение "Деловодител" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_DELOVODITEL	= 1;	
	/** Код на значение "Автор на документ" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_AVTOR	= 2;	
	/** Код на значение "Служител, получил достъп" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_SLUJIT_DOST	= 3;	
	/** Код на значение "Възложител на задача" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_VAZLOJITEL	= 4;	
	/** Код на значение "Контролиращ на задача" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_CONTR	= 5;	
	/** Код на значение "Изпълнител на задача" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_IZP	= 6;	
	/** Код на значение "Водещ служител на преписка" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_VODEST	= 7;	
	/** Код на значение "Всички с достъп" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_ALL_DOST	= 8;	
	/** Код на значение "Заместник на служител" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_ZAM	= 9;	
	/** Код на значение "Упълномощен" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_UPALNOMOSTEN	= 10;	
	/** Код на значение "включен участник в събитие" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_EVENT_IN		= 11;	
	/** Код на значение "отпаднал участник в събитие" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_EVENT_OUT	= 12;	
	/** Код на значение "участник в променено събитие" */
	public static final int	CODE_ZNACHENIE_NOTIF_ROLIA_EVENT_CHANGE	= 13;	


	// Класификация 156 - Събития за нотификации
	/** Код на значение "Заявка за деловодна регистрация" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELOVODNA_REQUEST		= 9;
	/** Код на значение "Регистрация на работен документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_REGIST				= 11;
	/** Код на значение "Получаване на документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_RECEIVE			= 12;
	/** Код на значение "Пререгистрация на изпратен документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_RE_REGIST			= 14;
	/** Код на значение "Отказ на регистрация на документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_REGIST_REGECT		= 15;
	/** Код на значение "Даване на достъп до документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_ACCESS				= 16;
	/** Код на значение "Промяна на съдържание на документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_FILE_CHANGE			= 17;
	/** Код на значение "Насочване на документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_NASOCH		= 18;
	/** Код на значение "Смяна на изпълнител" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_EXEC_CHANGE	= 20;
	/** Код на значение "Документ за съгласуване" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_SAGL			= 23;
	/** Код на значение "Документ за подпис" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_PODPIS		= 24;
	/** Код на значение "Ново насочване на документ" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_NASOCH_NEW	= 25;
	/** Код на значение "Промяна на насочване" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_TASK_NASOCH_CHANGE	= 26;
	/** Код на значение "Възлагане на задача-проста" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_ASIGN				= 27;
	/** Код на значение "Смяна на изпълнител на задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_EXEC_CHANGE		= 29;
	/** Код на значение "Промяна на задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_DATA_CHANGE		= 31;
	/** Код на значение "Приключване на задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_COMPLETE			= 33;
	/** Код на значение "Връщане на задача за изпълнение" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_RETURN			= 34;
	/** Код на значение "Възлагане на преписка" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELO_REF_NEW			= 37;
	/** Код на значение "Смяна на водещ на преписка" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELO_REF_CHANGE		= 38;
	/** Код на значение "Приключване на преписка" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELO_END				= 39;
	/** Код на значение "Ново заместване/упълномощаване" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_REF_DELEGATE			= 40;

	/** Код на значение "Определяне на отговорен за изпълнение на процедура" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_CONTROL_PROC			= 41;
	/** Код на значение "Определяне на контролиращ на етап от процедура" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_CONTROL_ETAP			= 42;
	/** Код на значение "Вземане на решение по изпълнението на процедура" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DECISION_PROC			= 43;
	/** Код на значение "Определяне на изпълнител на задача по процедура" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_PROC				= 44;

	/** Код на значение "Даване на достъп до преписка" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELO_ACCESS			= 45;

	/** Код на значение "Просрочено изпълнение на задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_OVERDUE_IZP		= 46;
	/** Код на значение "Просрочване на възложена задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_OVERDUE_ASSIGN	= 47;
	/** Код на значение "Просрочване на контролирана задача" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_OVERDUE_CONTROL	= 48;

	/** Код на значение "Заключен потребител" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_USER_LOCKED			= 49;
	/** Код на значение "Опит за неоторизиран достъп до страница" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_UNAUTHORIZED_PAGE		= 50;
	/** Код на значение "Опит за неоторизиран достъп до обект" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_UNAUTHORIZED_OBJECT	= 51;

	/** Код на значение "Участие в събитие" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_PARTICIPATION			= 52;

	/** Код на значение "Очакване на указания" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_TASK_INSTRUCTIONS			= 53;

	/** Код на значение "Отпадане на заместване/упълномощаване" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_DELEGATE_REMOVE			= 54;

	/** Код на значение "Прекратяване на процедура" */
	public static final int	CODE_ZNACHENIE_NOTIFF_EVENTS_PROC_STOP			= 55;

	
	/** Код на значение "наша компетентност" класификация "Компетентност" 161 */
	public static final int	CODE_ZNACHENIE_COMPETENCE_OUR 		= 1;
	/** Код на значение "за изпращане по компетентност" класификация "Компетентност" 161 */
	public static final int	CODE_ZNACHENIE_COMPETENCE_FOR_SEND 	= 2;
	/** Код на значение "изпратен по компетентност" класификация "Компетентност" 161 */
	public static final int	CODE_ZNACHENIE_COMPETENCE_SENT 		= 3;

	/** Код на значение "чака изпълнение" класификация "Статус на изпълнение на услуга/процедура" 123 */
	public static final int	CODE_ZNACHENIE_PROC_STAT_WAIT 		= 1;
	/** Код на значение "изпълнява се" класификация "Статус на изпълнение на услуга/процедура" 123 */
	public static final int	CODE_ZNACHENIE_PROC_STAT_EXE 		= 2;
	/** Код на значение "изпълнена" класификация "Статус на изпълнение на услуга/процедура" 123 */
	public static final int	CODE_ZNACHENIE_PROC_STAT_IZP 		= 3;
	/** Код на значение "изпълнена след срока" класификация "Статус на изпълнение на услуга/процедура" 123 */
	public static final int	CODE_ZNACHENIE_PROC_STAT_IZP_SROK 	= 4;
	/** Код на значение "прекратена" класификация "Статус на изпълнение на услуга/процедура" 123 */
	public static final int	CODE_ZNACHENIE_PROC_STAT_STOP 		= 5;
	
	/** Код на значение "чака изпълнение" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_WAIT 		= 1;
	/** Код на значение "изпълнява се" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_EXE 		= 2;
	/** Код на значение "чака решение" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_DECISION 	= 3;
	/** Код на значение "изпълнен" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_IZP 		= 4;
	/** Код на значение "изпълнен след срока" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_IZP_SROK 	= 5;
	/** Код на значение "отменен" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_CANCEL 	= 6;
	/** Код на значение "прекратен" класификация "Статус на изпълнение на етап" 122 */
	public static final int	CODE_ZNACHENIE_ETAP_STAT_STOP 		= 7;

	/** Код на значение "разработва се" класификация "Статус на дефиниция на процедура" 168 */
	public static final int	CODE_ZNACHENIE_PROC_DEF_STAT_DEV 		= 1;
	/** Код на значение "отпаднала" класификация "Статус на дефиниция на процедура" 168 */
	public static final int	CODE_ZNACHENIE_PROC_DEF_STAT_CANCEL 	= 2;
	/** Код на значение "активна" класификация "Статус на дефиниция на процедура" 168 */
	public static final int	CODE_ZNACHENIE_PROC_DEF_STAT_ACTIVE 	= 3;
	
	/** Код на значение "стартиращ предходния етап" класификация "Тип стартиращ документ на етап от процедура" 169 */
	public static final int	CODE_ZNACHENIE_ETAP_DOC_MODE_PREV_IN 		= 1;
	/** Код на значение "резултат от предходния етап" класификация "Тип стартиращ документ на етап от процедура" 169 */
	public static final int	CODE_ZNACHENIE_ETAP_DOC_MODE_PREV_OUT 		= 2;

	/** Код на значение "Ръководител на автора на документ" класификация "Бизнес роля в процедура" 170 */
	public static final int	CODE_ZNACHENIE_PROC_BUSINESS_ROLE_AUTHOR_BOSS 		= 3;
	/** Код на значение "Ръководител на изпълнителя на задачата от предходния етап" класификация "Бизнес роля в процедура" 170 */
	public static final int	CODE_ZNACHENIE_PROC_BUSINESS_ROLE_EXEC_BOSS		 	= 4;
	/** Код на значение "Aвтор на документ" класификация "Бизнес роля в процедура" 170 */
	public static final int	CODE_ZNACHENIE_PROC_BUSINESS_ROLE_AUTHOR 			= 5;
	/** Код на значение "Изпълнител на задачата от предходния етап" класификация "Бизнес роля в процедура" 170 */
	public static final int	CODE_ZNACHENIE_PROC_BUSINESS_ROLE_EXEC		 		= 6;

	/** Код на значение "договор" класификация "Характер на специализиран документ" 175 */
	public static final int	CODE_ZNACHENIE_HAR_DOC_DOGOVOR		= 1;
	/** Код на значение "заповед за изпълнение на проект" класификация "Характер на специализиран документ" 175 */
	public static final int	CODE_ZNACHENIE_HAR_DOC_ZAP_PROEKT	= 2;

	// Класификация 154 - Статус на предаване
	/** Код на значение "Чака антивирусна проверка" */
	public static final int	DS_МALWARE_CHECK = 1;	
	/** Код на значение "Чака регистрация" */
	public static final int	DS_WAIT_REGISTRATION = 3;	
	/** Код на значение "Регистриран" */
	public static final int	DS_REGISTERED = 8;	
	/** Код на значение "Спряна работата" */
	public static final int	DS_STOPPED	= 4; 
	/** Код на значение "Приключена работата" */
	public static final int	DS_CLOSED	= 12;
	/** Код на значение "Не е намрен" */
	public static final int	DS_NOT_FOUND = 11;	
	/** Код на значение "Вече е получен" */
	public static final int	DS_ALREADY_RECEIVED = 13; 	
	/** Код на значение "Отказан" */
	public static final int	DS_REJECTED	= 7; 
	/** Код на значение "Не е изпратено" */
	public static final int	DS_WAIT_SENDING	= 2;
	/** Код на значение "Изпратен" */
	public static final int	DS_SENT	= 5;
	/** Код на значение "Върнато съобщение за грешка" */
	public static final int	DS_RETURNED_ERROR = 6;	
	/** Код на значение "Не е връчено" */
	public static final int	DS_NOT_OPENED = 10;	
	/** Код на значение "Връчено" */
	public static final int	DS_OPENED = 9;		
	/** Код на значение "Чака формиране на пакет" */
	public static final int	DS_WAIT_PROCESSING = 14;
	
	
	/** Код на значение "Тип физическо лице в ССЕВ" */
	public static final String	SSEV_TYPE_PERSON = "Person";
	/** Код на значение "Тип юридическо лице в ССЕВ" */
	public static final String	SSEV_TYPE_LEGALPERSON = "LegalPerson";
	/** Код на значение "Тип администрация в ССЕВ" */
	public static final String	SSEV_TYPE_ADMINISTRATION = "Institution";
	/** Код на значение "Тип отговор" */
	public static final String	SSEV_TYPE_REPLY = "Reply";
	
	// Класификация 496 - Вид спортно обединение
	/** Код на значение "Спортна федерация" */
	public static final int	CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF	= 1;	
	/** Код на значение "НОСТД" */
	public static final int	CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOSTD	= 2;		
	/** Код на значение "НОУС" */
	public static final int	CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOUS	= 3;		
	/** Код на значение "Обединен спортен клуб" */
	public static final int	CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK	= 4;		
	
	// Класификация 499 - "Статус на вписване" 	
	/** Код на значение 1 "Вписан" */
	public static final int	 CODE_ZNACHENIE_STATUS_REG_VPISAN 	= 1;	
	/** Код на значение 2 "Отнето вписване" */
	public static final int	 CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE	= 2;
	/** Код на значение 3 "Прекратено вписване" */
	public static final int	 CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE	= 3;
	/** Код на значение 4 "Заличено вписване" */
	public static final int	 CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE	= 4;
	
	// Класификация 505 - Вид на спортно формирование
	/** Код на значение "спортен клуб" */
	public static final int	CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK	= 1;
	/** Код на значение "туристическо дружество" */
	public static final int	CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_TD	= 2;

	
	// Класификация 507 - Тип връзка лице-спортен обект
	/** Код на значение Собственик  */
	public static final int	 CODE_ZNACHENIE_TYPE_VR_SOBSTV	= 1;;  
	/** Код на значение Наемател.  */
	public static final int	 CODE_ZNACHENIE_TYPE_VR_NAEM	= 2;  
	
	// Класификация 512 - Вид спорт
	/** Код на значение "многоспортова" */
	public static final int	CODE_ZNACHENIE_MNOGOSPORTOV	= 172;
	
	// Класификация 513 - "Статус на заявление"	
	/** Код значение 1  "В разглеждане" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE = 1;
	/** Код значение  2   "Вписан" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN = 2;
	/** Код значение  3   "Отказано вписване" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE = 3;
	/** Код значение  4   "Оставено без последствие" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OSTAVENO_BEZ_POSLEDSTVIE = 4;
	/** Код значение  5   "Оттеглено" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTTEGLENO = 5;
	/** Код значение  6   "Прекратено производство" */
	public static final int	 CODE_ZNACHENIE_STATUS_ZAIAVLENIE_PREKRATENO_PROIZVODSTVO = 6;
	
	// Класификация 516 - "Статус на обект"
	/** Код на значение 1 "Вписан" */
	public static final int	 CODE_ZNACHENIE_STATUS_OBEKT_VPISAN 	= 1;	
	/** Код на значение 2 "В разглеждане" */
	public static final int	 CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE	= 2;
	/** Код на значение 3 "Отказан" */
	public static final int	 CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN	= 3;
	/** Код на значение 4 "Заличен" */
	public static final int	 CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN	= 4;
	
	
	/** Код на значение  "Актуализация на спортни обединения"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_SP_OBED_UPD = 117;
	/** Код на значение  "Актуализация на спортни формирования"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_SP_FORMIR_UPD = 120;
	/** Код на значение  "Актуализация на спортни обекти"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_SP_OBEKT_UPD = 123;
	/** Код на значение  "Актуализация на треньорски кадри"  класификация "Меню" 7*/
	public static final int	CODE_ZNACHENIE_MENU_COACHES_UPD = 126;

	/** */
	private DocuConstants() {
		super();
	}
}