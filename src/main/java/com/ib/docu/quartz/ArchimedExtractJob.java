package com.ib.docu.quartz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.docu.archimed.ActionType;
import com.ib.docu.archimed.ActivityInfo;
import com.ib.docu.archimed.ArchimedClient;
import com.ib.docu.archimed.CompleteTaskPayload;
import com.ib.docu.archimed.CompleteTaskRequest;
import com.ib.docu.archimed.Corresp;
import com.ib.docu.archimed.CorrespondentInfo;
import com.ib.docu.archimed.Document;
import com.ib.docu.archimed.Document.CorrespondenceTypes;
import com.ib.docu.archimed.Document.DocumentStates;
import com.ib.docu.archimed.DocumentFile;
import com.ib.docu.archimed.DocumentTypeInfo;
import com.ib.docu.archimed.Login;
import com.ib.docu.archimed.LoginResponse;
import com.ib.docu.archimed.RegisterDocumentRequest;
import com.ib.docu.archimed.Task;
import com.ib.docu.archimed.TaskSearchResponse;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.ParsePdfZaqvlenie;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;
import com.ib.system.utils.SearchUtils;

/**
 * Процес, който тегли данни за заявления от системата Архимед
 *
 * @author belev
 */
@DisallowConcurrentExecution
public class ArchimedExtractJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchimedExtractJob.class);

	private static final List<Integer> IB_DOC_VID = new ArrayList<>();
	static {
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI);

		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI);

		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI);

		// TODO тези 4рите са нови и трябва да се получат кодове от Архимед
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS);
		IB_DOC_VID.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD);
	}

	/** @param args */
	public static void main(String[] args) {
		ArchimedExtractJob job = new ArchimedExtractJob();
		try {
			job.execute(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/** */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		String accessToken = null;
		ArchimedClient client = null;
		try {
			SystemData systemData;

			if (context != null) {
				ServletContext servletContext = (ServletContext) context.getScheduler().getContext().get("servletContext");
				if (servletContext == null) {
					LOGGER.error("********** ServletContext is null **********");
					return;
				}
				systemData = (SystemData) servletContext.getAttribute("systemData");

			} else { // унит тестове явно
				LOGGER.error("********** JobExecutionContext is null **********");
				systemData = new SystemData();
			}

			String setting = systemData.getSettingsValue("archimed.extract.job.on");
			if (!"1".equals(setting)) {
				LOGGER.info("SWITCHED OFF by setting 'archimed.extract.job.on'");
				return;
			}
			LOGGER.info("archimed.extract.job.on={}", setting);
			
			client = systemData.getArchimedClient();
			if (client == null) {
				LOGGER.error("********** ArchimedClient is NULL **********");
				return;
			}

//			0. логин по описания начин във файла Archimed Integration Services Core.8.4.11958.html
			accessToken = login(client, systemData);
			if (accessToken == null) {
				return;
			}

			String activityCode = systemData.getSettingsValue("archimed.activityCode");
			if (activityCode == null) {
				activityCode = "В";
				LOGGER.error("missing setting 'archimed.activityCode' -> default=В");
			}
			String answerCode = systemData.getSettingsValue("archimed.answerCode");
			if (answerCode == null) {
				answerCode = "33";
				LOGGER.error("missing setting 'archimed.answerCode' -> default=33");
			}

			// тези трябва да ги взема по код, защото надолу ще ми трябват ИД-тата
			String activityId = findActivityId(client, activityCode, accessToken); // "activity":{"id":"12","code":"В","name":"Вписвания
																					// /Индекс/"}

			String answerTypeId = findAnswerTypeId(client, answerCode, accessToken); // "type":{"id":"17969","code":"33","name":"Отговор
																						// /Индекс/"}

			if (SearchUtils.isEmpty(activityId) || SearchUtils.isEmpty(answerTypeId)) {
				LOGGER.error("!!! NOT_FOUND !!! activityId={}, answerTypeId={}. Process cannot continue!", activityId, answerTypeId);

				return; // няма как без да се знае това да се регистрира документ отговор
			}

			Task task = null;

//			1. /api/eprocess/tasks/search - с подходяща критерия
			TaskSearchResponse searchResponse = taskSearch(client, accessToken);
			if (searchResponse != null) {

//				2. /api/eprocess/tasks/search/{guid}?skip=0&take=1
				task = taskSearchExplore(client, searchResponse, accessToken);

//				3. /api/eprocess/tasks/search/{guid} - за да се освободи търсенето
				taskSearchRelease(client, searchResponse, accessToken);
			}

			if (task != null) {
				LOGGER.info("GET Task.id={}", task.getId());
//				print(task);

				if (task.getDocumentId() == null) {
					LOGGER.warn("Task.id={} ! documentId is NULL !", task.getId());
				}

//				4. /api/eprocess/documents/{id} - за конкретен документ след търсене да се вземат всички данни, като ни трябват
				Document document = loadDocument(client, task.getDocumentId(), accessToken);

				if (document != null) {
					LOGGER.info("GET Document.id={}", document.getId());
//					print(document);

//					5. /api/eprocess/correspondent-schema/correspondents/{id} - за да се вземат данни за кореспонент като ЕИК/ЕГН и др.
					Corresp corresp = loadCorrespondent(client, document.getCorrespondent(), accessToken);
					if (corresp != null) {
						LOGGER.info("GET Corresp.id={}", corresp.getId());
//						print(corresp);
					}

					Corresp secondCorresp = loadCorrespondent(client, document.getSecondCorrespondent(), accessToken);
					if (secondCorresp != null) {
						LOGGER.info("GET SecondCorresp.id={}", secondCorresp.getId());
//						print(secondCorresp);
					}

					List<Corresp> otherCorrespList = new ArrayList<>();
					if (document.getAdditionalCorrespondents() != null) {
						for (CorrespondentInfo correspInfo : document.getAdditionalCorrespondents()) {

							Corresp otherCorresp = loadCorrespondent(client, correspInfo, accessToken);
							if (otherCorresp != null) {
								LOGGER.info("GET OtherCorresp.id={}", otherCorresp.getId());
//								print(otherCorresp);

								otherCorrespList.add(otherCorresp);
							}
						}
					}

//					6. /api/eprocess/documents/{id}/files - данните за файловете
					List<DocumentFile> files = selectDocFiles(client, document.getId(), accessToken);

					for (DocumentFile file : files) {
//						7. /api/eprocess/documents/{id}/files/{fileId}/content - файловото съдържание
						byte[] content = loadFileContent(client, document.getId(), file.getId(), accessToken);
						file.setFileContent(content);
					}

					Integer docVid = findDocVidByPdfContent(files, systemData);
					if (docVid == null) { // щом не може да се открие по файла, ще се ходи към мапинга

						Map<String, Integer> docVidMap = createDocVidMap(systemData);
						docVid = docVidMap.get(document.getType().getCode()); // това ще даде вид док при нас като код от
																				// класификацията
					}

					JPA.getUtil().begin();

					EgovMessagesDAO dao = new EgovMessagesDAO(ActiveUser.DEFAULT);
					dao.saveFromArchimed(document, docVid, corresp, secondCorresp, otherCorrespList, files, systemData);

					String answerDocId = registerAnswerDoc(client, document, task, accessToken, activityId, answerTypeId);
					LOGGER.info("REGISER DocumentAnswer.id={}", answerDocId);

					JPA.getUtil().commit();
				}
			}

		} catch (Exception e) {
			JPA.getUtil().rollback();

			LOGGER.error("Грешка при теглене на документи от Архимед", e);

		} finally {
//			8. /api/session/log-out
			logout(client, accessToken);

			JPA.getUtil().closeConnection();
		}
	}

	/**
	 * Формира мапинга, по който ще може да се открие вида на документа
	 */
	Map<String, Integer> createDocVidMap(SystemData systemData) throws DbErrorException {
		Map<String, Integer> map = new HashMap<>();

		for (Integer code : IB_DOC_VID) {
			String dop = SearchUtils.trimToNULL( //
				systemData.decodeItemDopInfo(DocuConstants.CODE_CLASSIF_DOC_VID, code, SysConstants.CODE_LANG_BG, null));
			if (dop != null) {
				map.put(dop, code);
			}
		}
		return map;
	}

	/**
	 * Трябва да определи код за вид документ, като ако има грешки ще се логват, но няма да спред обработката
	 */
	Integer findDocVidByPdfContent(List<DocumentFile> files, SystemData systemData) {
		if (files == null || files.isEmpty()) {
			LOGGER.warn("findDocVidByPdfContent -> no files");
			return null;
		}
		String[] numbers = null;

		for (DocumentFile f : files) {
			String fn = f.getFileName() != null ? f.getFileName().trim().toUpperCase() : "";

			if (f.getFileContent() != null && fn.indexOf(".PDF") == fn.length() - 4) {
				try {
					numbers = new ParsePdfZaqvlenie().getCodeAndEIK(f.getFileContent(), systemData, SysConstants.CODE_LANG_BG);

					if (numbers != null && numbers[0] != null && numbers[0].length() > 0) {
						break; // за първият, който има данни ще се проверява вида на заявлението
					}
					numbers = null;
				} catch (Exception e) {
					LOGGER.error("findDocVidByPdfContent -> ERROR parsing file: " + f.getFileName(), e);
				}
			} else {
				LOGGER.info("findDocVidByPdfContent -> skip file: {}", f.getFileName());
			}
		}
		if (numbers == null) {
			LOGGER.warn("findDocVidByPdfContent -> no PDF files");
			return null; // нищо не е открито и ще се гледа от мапинга
		}

		Integer docVid = null;
		try {
			if ("037002ZVLN".equals(numbers[0])) { // заличаване и по това ще се гледа какво e пo ЕИК
				docVid = DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM; // дефолт и почти сигурно ще е такова

				if (numbers[1] != null && numbers[1].length() >= 0) {

					@SuppressWarnings("unchecked")
					List<Object[]> rows = JPA.getUtil().getEntityManager().createNativeQuery( //
						"select r.code, o.id from adm_referents r inner join mms_sport_obedinenie o on o.id_object = r.code where r.nfl_eik = ?") //
						.setParameter(1, numbers[1].trim()).getResultList();

					if (!rows.isEmpty() && rows.get(0)[1] != null) { // само ако е обединение се докаже че е обединение
						docVid = DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE;
					}
				}

			} else { // ще се търси по външния код
				List<SystemClassif> items = systemData.getItemsByCodeExt(DocuConstants.CODE_CLASSIF_DOC_VID, numbers[0], SysConstants.CODE_LANG_BG, null);
				if (items.size() == 1) {
					docVid = items.get(0).getCode();
				} else {
					LOGGER.warn("findDocVidByPdfContent -> problem find by extCode: {}", numbers[0]);
				}
			}
		} catch (Exception e) {
			LOGGER.error("findDocVidByPdfContent -> ERROR find by codes", e);
		}
		return docVid;
	}

	private String findActivityId(ArchimedClient client, String code, String accessToken) throws RestClientException {
		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		MultivaluedMap<String, Object> params = new MultivaluedMapImpl<>();
		params.putSingle("idType", "code");

		ActivityInfo response = client.get("/eprocess/activities/" + code, ActivityInfo.class, params, headers);
		if (response == null) {
			LOGGER.error("GET: /eprocess/activities/{} NULL-response", code);
			return null;
		}
		return response.getId();
	}

	private String findAnswerTypeId(ArchimedClient client, String code, String accessToken) throws RestClientException {
		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		MultivaluedMap<String, Object> params = new MultivaluedMapImpl<>();
		params.putSingle("idType", "code");

		DocumentTypeInfo response = client.get("/eprocess/document-types/" + code, DocumentTypeInfo.class, params, headers);
		if (response == null) {
			LOGGER.error("GET: /eprocess/document-types/{} NULL-response", code);
			return null;
		}
		return response.getId();
	}

	/**  */
	private Corresp loadCorrespondent(ArchimedClient client, CorrespondentInfo correspInfo, String accessToken) throws RestClientException {
		if (correspInfo == null || SearchUtils.isEmpty(correspInfo.getId())) {
			return null;
		}

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		Corresp corresp = client.get("/eprocess/correspondent-schema/correspondents/" + correspInfo.getId(), Corresp.class, null, headers);
		if (corresp == null) {
			LOGGER.error("GET: /eprocess/correspondent-schema/correspondents/{} NULL-response", correspInfo.getId());
		}
		return corresp;
	}

	/**   */
	private Document loadDocument(ArchimedClient client, String documentId, String accessToken) throws RestClientException {
		if (SearchUtils.isEmpty(documentId)) {
			return null;
		}

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		Document document = client.get("/eprocess/documents/" + documentId, Document.class, null, headers);
		if (document == null) {
			LOGGER.error("GET: /eprocess/documents/{} NULL-response", documentId);
		}

//		String s = client.get("/eprocess/documents/" + documentId, String.class, null, headers);
//		System.out.println(s);

		return document;
	}

	/** */
	private byte[] loadFileContent(ArchimedClient client, String documentId, String fileId, String accessToken) throws RestClientException {
		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		byte[] response = client.get( //
			"/eprocess/documents/" + documentId + "/files/" + fileId + "/content" //
			, byte[].class, null, headers);

		if (response == null || response.length == 0) {
			LOGGER.error("GET: /eprocess/documents/{}/files{}/content NULL-response", documentId, fileId);
		}
		return response;
	}

	/**   */
	private String login(ArchimedClient client, BaseSystemData sd) throws RestClientException, DbErrorException {
		Login login = new Login();
		login.setProductName(sd.getSettingsValue("archimed.productName"));
		login.setProfileGuid(sd.getSettingsValue("archimed.profileGuid"));
		login.setUserName(sd.getSettingsValue("archimed.username"));
		login.setPassword(sd.getSettingsValue("archimed.password"));
		print(login);

		String accessToken = null;

		LoginResponse response = client.post("/session/log-in/password", login, LoginResponse.class, null, null);
		if (response != null && response.getToken() != null) {
			accessToken = response.getToken().getAccessToken();
		}
		if (accessToken == null) {
			LOGGER.error("POST: /session/log-in/password NULL-response");
		}
		return accessToken;
	}

	/** */
	private void logout(ArchimedClient client, String accessToken) {
		if (client == null || accessToken == null) {
			return;
		}
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			client.post("/session/log-out", "", String.class, null, headers);

		} catch (Exception e) {
			LOGGER.error("POST: /session/log-out ERROR", e);
		}
	}

	private void print(Object obj) {
		if (obj == null) {
			return;
		}
		try {
			String str = new ObjectMapper().writeValueAsString(obj);
			LOGGER.info(str);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**   */
	private String registerAnswerDoc(ArchimedClient client, Document document, Task task, String accessToken, String activityId, String answerTypeId) throws RestClientException {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		RegisterDocumentRequest request = new RegisterDocumentRequest();

		request.setTaskId(task.getId());

		request.setRegistratorId(task.getPerformerId()); // изпълнителя на задачата
		request.setRegistrationDate(null);
		request.setParentId(document.getId());
		request.setDocumentTypeId(answerTypeId);
		request.setActivityId(activityId);

		request.setCorrespondenceType(CorrespondenceTypes.Outgoing);
		request.setState(DocumentStates.Registered);
//		request.setExternalUri(new DocumentExternalUri(document.getId(), sdf.format(new Date())));

		request.setExpectingResponse(Boolean.FALSE);
		request.setCreateArchive(Boolean.TRUE);

		request.setDescription(null);
		request.setDeadline(null);
		request.setAdditionalText(null);
		request.setProcessDefinitionRevisionID(null);

		if (document.getCorrespondent() != null) {
			request.setCorrespondentId(document.getCorrespondent().getId());
			request.setCorrespondentDescription(null);
		}
		if (document.getSecondCorrespondent() != null) {
			request.setSecondaryCorrespondentId(document.getSecondCorrespondent().getId());
		}

		String answerDocId = client.post("/eprocess/documents", request, String.class, null, headers);
		if (SearchUtils.isEmpty(answerDocId)) {
			LOGGER.error("POST: /eprocess/documents NULL-response");
			return null;
		}
		return answerDocId;
	}

	/**   */
	private List<DocumentFile> selectDocFiles(ArchimedClient client, String documentId, String accessToken) throws RestClientException {
		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		List<DocumentFile> files = client.get("/eprocess/documents/" + documentId + "/files" //
			, new GenericType<ArrayList<DocumentFile>>() { //
			}, null, headers);

		if (files == null) {
			LOGGER.error("GET: /eprocess/documents/{}/files NULL-response", documentId);
			files = new ArrayList<>();
		}
		return files;
	}

	/**   */
	@SuppressWarnings("unused")
	private Task taskComplete(ArchimedClient client, Document document, Task task, String accessToken, String activityId, String answerTypeId) throws RestClientException {
		// !!! не е ясно как се прави и дали ще се използва !!!

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		CompleteTaskPayload payload = new CompleteTaskPayload();
		payload.setType(ActionType.TaskAnswerByDocument);

		if (document.getCorrespondent() != null) {
			payload.setCorrespondentId(document.getCorrespondent().getId());
		}
		payload.setDocumentTypeId(answerTypeId);
		payload.setActivityId(activityId);
		payload.setCorrespondenceType(CorrespondenceTypes.Outgoing);
		payload.setState(DocumentStates.Registered);

		CompleteTaskRequest request = new CompleteTaskRequest();
		request.setActionPayload(payload);

		String response = client.post("/eprocess/tasks/" + task.getId() + "/complete", request, String.class, null, headers);
		if (response == null) {
			LOGGER.error("POST: /eprocess/tasks/{}/complete NULL-response", task.getId());
		}
		return null;
	}

	/**  */
	private TaskSearchResponse taskSearch(ArchimedClient client, String accessToken) throws RestClientException {
		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		StringBuilder body = new StringBuilder();
		body.append(" { ");
//		body.append(" \"includeCompleted\": true "); // само приключени
		body.append(" \"includeUncompleted\": true "); // само активни
		body.append(" } ");

		TaskSearchResponse response = client.post("/eprocess/tasks/search", body.toString(), TaskSearchResponse.class, null, headers);
		if (response == null) {
			LOGGER.error("POST: /eprocess/tasks/search NULL-response");
		}
		return response;
	}

	/**   */
	private Task taskSearchExplore(ArchimedClient client, TaskSearchResponse searchResponse, String accessToken) throws RestClientException {
		if (searchResponse.getCount() == null || searchResponse.getCount() == 0 || SearchUtils.isEmpty(searchResponse.getGuid())) {
			LOGGER.info("... no new tasks ...");
			return null; // няма задачи
		}

		LOGGER.info("GET First Task from {} count", searchResponse.getCount());

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		MultivaluedMap<String, Object> params = new MultivaluedMapImpl<>(); // само по една задача
		int skip = searchResponse.getCount() - 1; // за да вземем първата по време
		params.putSingle("skip", "" + skip);
		params.putSingle("take", "1");

		Task task = null;

		List<Task> response = client.get("/eprocess/tasks/search/" + searchResponse.getGuid() //
			, new GenericType<ArrayList<Task>>() { //
			}, params, headers);

		if (response == null) {
			LOGGER.error("GET: /eprocess/tasks/search/{} NULL-response", searchResponse.getGuid());
		} else {
			task = response.get(0);
		}

//		String s = client.get("/eprocess/tasks/search/" + searchResponse.getGuid() //
//			, String.class, params, headers);
//		System.out.println(s);

		return task;
	}

	/** */
	private void taskSearchRelease(ArchimedClient client, TaskSearchResponse searchResponse, String accessToken) throws RestClientException {
		if (SearchUtils.isEmpty(searchResponse.getGuid())) {
			return; // няма какво да се прави
		}

		MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
		headers.putSingle("Authorization", "Bearer " + accessToken);

		client.delete("/eprocess/tasks/search/" + searchResponse.getGuid(), String.class, null, headers);
	}
}
