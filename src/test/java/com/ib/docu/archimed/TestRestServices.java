package com.ib.docu.archimed;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ib.docu.system.SystemData;

/** @author belev */
public class TestRestServices {

	static String	accessToken		= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy5hcmNoaW1lZC5iZy9pZGVudGl0eS9jbGFpbXMvc2Vzc2lvbmlkIjoiMWY2ZTkzYjItMjQ3Yy00NjM0LTg4NzMtNjFiMTY1MTRiNmVlIiwibmJmIjoxNjU5MDAzNzYyLCJleHAiOjE2NTkwMDczNjJ9.n6NUu_JAjFzRXlUpX1b5YlgmpeXp07NHTPhP-qusmp4";
	static String	docResultGuid	= "610ffe91-c58d-46a1-bdb4-f04f2d381a3c";

	private static SystemData sd;

	/** @throws Exception */
	@BeforeClass
	public static void setUp() throws Exception {
		sd = new SystemData();
	}

	/** */
	@Test
	public void test_01_SessionGetProfiles() {
		try {
			String s = sd.getArchimedClient().get("/profiles", String.class, null, null);

			System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
	@Test
	public void test_02_SessionGetProfile() {
		try {
			String s = sd.getArchimedClient().get("/profiles/41ab22bb-6691-4f63-ace7-754d4ebb7889", String.class, null, null);

			System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	/** */
//	@Test
//	public void test_04_SessionLogIn_password() {
//		try {
//			StringBuilder body = new StringBuilder();
//			body.append(" { ");
//			body.append("     \"profileGuid\": \"" + ArchimedClient.PROFILE_GUID + "\", ");
//			body.append("     \"productName\": \"" + ArchimedClient.PRODUCT_NAME + "\", ");
//			body.append("     \"userName\": \"" + ArchimedClient.USERNAME + "\", ");
//			body.append("     \"password\": \"" + ArchimedClient.PASSWORD + "\" ");
//			body.append(" } ");
//
//			String s = sd.getArchimedClient().post("/session/log-in/password", body.toString(), String.class, null, null);
//
//			System.out.println(s);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	/**  */
//	@Test
//	public void test_05_SessionGetSessionInfo() {
//		try {
//			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
//			headers.putSingle("Authorization", "Bearer " + accessToken);
//
//			String s = sd.getArchimedClient().get("/session", String.class, null, headers);
//
//			System.out.println(s);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	/**  */
//	@Test
//	public void test_06_EProcessGetSessionInfo() {
//		try {
//			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
//			headers.putSingle("Authorization", "Bearer " + accessToken);
//
//			String s = sd.getArchimedClient().get("/eprocess/info", String.class, null, headers);
//
//			System.out.println(s);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/** */
//	@Test
	public void test_07_EProcessFindDocuments_directed() {
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			StringBuilder body = new StringBuilder();
			body.append(" { ");
			body.append("     \"criteria\": { ");
			body.append("         \"documentTypeId\": \"17969\" ");
			body.append("     }, ");
			body.append("     \"orderItems\": [{ ");
			body.append("         \"property\": \"Date\", ");
			body.append("         \"isAscending\": true ");
			body.append("     }] ");
			body.append(" } ");

			String s = sd.getArchimedClient().post("/eprocess/documents/search", body.toString(), String.class, null, headers);

			System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_08_EProcessGetDocuments_directed() {
		try {
			MultivaluedMap<String, Object> params = new MultivaluedMapImpl<>();
			params.putSingle("skip", "0");
			params.putSingle("take", "3");

			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			String s = sd.getArchimedClient().get("/eprocess/documents/search/" + docResultGuid, String.class, params, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_09_EProcessReleaseDocuments_directed() {
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			String s = sd.getArchimedClient().delete("/eprocess/documents/search/" + docResultGuid, String.class, null, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_10_EProcessDocumentFind() {
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			Integer docId = 939996;
			String s = sd.getArchimedClient().get("/eprocess/documents/" + docId, String.class, null, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_11_EProcessDocumentTypes() {
		try {
			MultivaluedMap<String, Object> params = new MultivaluedMapImpl<>();
			List<Object> list = new ArrayList<>();
			list.add("17953");
			list.add("17954");
			params.put("ids", list);

			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			String s = sd.getArchimedClient().get("/eprocess/document-types", String.class, params, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_12_EProcessCorrespondentFind() {
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			Integer correspId = 1551;
			String s = sd.getArchimedClient().get("/eprocess/correspondent-schema/correspondents/" + correspId, String.class, null, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** */
//	@Test
	public void test_13_EProcessDocumentFiles() {
		try {
			MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<>();
			headers.putSingle("Authorization", "Bearer " + accessToken);

			Integer docId = 939988;
			String s = sd.getArchimedClient().get("/api/eprocess/documents/" + docId + "/files", String.class, null, headers);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
