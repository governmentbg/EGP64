package com.ib.docu.archimed;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.exceptions.RestClientException;
import com.ib.system.rest.SystemRestClient;

/**
 * Взета е идеята от {@link SystemRestClient}, но там е с версия resteasy-client(3.5.1.Final),<br>
 * а тук resteasy-client(4.5.2.Final) и инициализацията е различна. <br>
 * <br>
 *
 * @author belev
 */
public final class ArchimedClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchimedClient.class);

	private static final long WARN_EXEC_TIME = 3000L; // ще реве ако се забави повече от 3сек

	/**
	 * Създава нов клиент за подадения адрес
	 *
	 * @param targetUrl
	 * @return
	 * @throws RestClientException
	 */
	public static ArchimedClient create(String targetUrl) throws RestClientException {
		return new ArchimedClient(targetUrl);
	}

	/** чрез това реално се викат рестовете */
	private ResteasyWebTarget target;

	/**
	 * Инициализацията
	 *
	 * @param targetUrl
	 * @throws RestClientException
	 */
	private ArchimedClient(String targetUrl) throws RestClientException {
		try {
			int maxTotal = 100;
			int defaultMaxPerRoute = 10;

			LOGGER.info("Start building client with params: targetUrl={}, maxTotal={}, defaultMaxPerRoute={}", targetUrl, maxTotal, defaultMaxPerRoute);

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
			cm.setMaxTotal(maxTotal);
			cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
			ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);

			ResteasyClient client = new ResteasyClientBuilderImpl().httpEngine(engine).build();
			this.target = client.target(UriBuilder.fromPath(targetUrl));

		} catch (Exception e) {
			LOGGER.error("Exception in init method !", e);

			throw new RestClientException("Exception in init method !", e);
		}
	}

	/**
	 * DELETE извикване application/json. Ако се използва String.class връща JSON-а
	 *
	 * @param <T>
	 * @param arguments
	 * @param resultClass
	 * @param params
	 * @param headers
	 * @return
	 * @throws RestClientException
	 */
	public <T> T delete(String arguments, Class<T> resultClass, MultivaluedMap<String, Object> params, MultivaluedMap<String, Object> headers) throws RestClientException {
		LOGGER.debug("delete:arguments={},resultClass={},params={},headers={}", arguments, resultClass, params, headers);

		long begin = System.currentTimeMillis();

		ResteasyWebTarget path;
		if (params == null || params.isEmpty()) {
			path = this.target.path(arguments);
		} else {
			path = this.target.path(arguments).queryParams(params);
		}

		Builder request;
		if (headers == null || headers.isEmpty()) {
			request = path.request(APPLICATION_JSON);
		} else {
			request = path.request(APPLICATION_JSON).headers(headers);
		}

		try (Response response = request.delete()) {

			if (response.getStatus() != 204) {
				throw newRestClientException(response);
			}

			if (response.hasEntity()) {
				return response.readEntity(resultClass);
			}
			return null;

		} catch (RestClientException e) {
			LOGGER.error(e.getMessage());
			throw e; // за да не се пропакова

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			throw new RestClientException(e);
		} finally {
			long time = System.currentTimeMillis() - begin;
			if (time > WARN_EXEC_TIME) {
				LOGGER.warn("!!! TIME={}ms. -> delete:arguments={},resultClass={},params={},headers={}", time, arguments, resultClass, params, headers);
			}
		}
	}

	/**
	 * GET извикване application/json. Ако се използва String.class връща JSON-а
	 *
	 * @param <T>
	 * @param arguments
	 * @param resultClass
	 * @param params
	 * @param headers
	 * @return
	 * @throws RestClientException
	 */
	public <T> T get(String arguments, Class<T> resultClass, MultivaluedMap<String, Object> params, MultivaluedMap<String, Object> headers) throws RestClientException {
		LOGGER.debug("get:arguments={},resultClass={},params={},headers={}", arguments, resultClass, params, headers);

		long begin = System.currentTimeMillis();

		ResteasyWebTarget path;
		if (params == null || params.isEmpty()) {
			path = this.target.path(arguments);
		} else {
			path = this.target.path(arguments).queryParams(params);
		}

		Builder request;
		if (headers == null || headers.isEmpty()) {
			request = path.request(APPLICATION_JSON);
		} else {
			request = path.request(APPLICATION_JSON).headers(headers);
		}

		try (Response response = request.get()) {

			if (response.getStatus() == 404) {
				return null;
			}
			if (response.getStatus() != 200) {
				throw newRestClientException(response);
			}
			if (response.hasEntity()) {
				return response.readEntity(resultClass);
			}
			return null;

		} catch (RestClientException e) {
			LOGGER.error(e.getMessage());
			throw e; // за да не се пропакова

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			throw new RestClientException(e);
		} finally {
			long time = System.currentTimeMillis() - begin;
			if (time > WARN_EXEC_TIME) {
				LOGGER.warn("!!! TIME={}ms. -> get:arguments={},resultClass={},params={},headers={}", time, arguments, resultClass, params, headers);
			}
		}
	}

	/**
	 * GET извикване application/json което връща List, Map и подобни на тях, който се явяват Generic класове
	 *
	 * @param <T>
	 * @param arguments
	 * @param genericType
	 * @param params
	 * @param headers
	 * @return
	 * @throws RestClientException
	 */
	public <T> T get(String arguments, GenericType<T> genericType, MultivaluedMap<String, Object> params, MultivaluedMap<String, Object> headers) throws RestClientException {
		LOGGER.debug("get:arguments={},Type={},params={},headers={}", arguments, genericType, params, headers);

		long begin = System.currentTimeMillis();

		ResteasyWebTarget path;
		if (params == null || params.isEmpty()) {
			path = this.target.path(arguments);
		} else {
			path = this.target.path(arguments).queryParams(params);
		}

		Builder request;
		if (headers == null || headers.isEmpty()) {
			request = path.request(APPLICATION_JSON);
		} else {
			request = path.request(APPLICATION_JSON).headers(headers);
		}

		try (Response response = request.get()) {

			if (response.getStatus() == 404) {
				return null;
			}
			if (response.getStatus() != 200) {
				throw newRestClientException(response);
			}
			if (response.hasEntity()) {
				return response.readEntity(genericType);
			}
			return null;

		} catch (RestClientException e) {
			LOGGER.error(e.getMessage());
			throw e; // за да не се пропакова

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			throw new RestClientException(e);
		} finally {
			long time = System.currentTimeMillis() - begin;
			if (time > WARN_EXEC_TIME) {
				LOGGER.warn("!!! TIME={}ms. -> get:arguments={},Type={},params={},headers={}", time, arguments, genericType, params, headers);
			}
		}
	}

	/**
	 * POST извикване application/json. Ако се използва String.class връща JSON-а
	 *
	 * @param <T>
	 * @param method
	 * @param body
	 * @param resultClass
	 * @param params
	 * @param headers
	 * @return
	 * @throws RestClientException
	 */
	public <T> T post(String method, Object body, Class<T> resultClass, MultivaluedMap<String, Object> params, MultivaluedMap<String, Object> headers) throws RestClientException {
		LOGGER.debug("post:method={},body={},resultClass={},params={},headers={}", method, body, resultClass, params, headers);

		long begin = System.currentTimeMillis();

		ResteasyWebTarget path;
		if (params == null || params.isEmpty()) {
			path = this.target.path(method);
		} else {
			path = this.target.path(method).queryParams(params);
		}

		Builder request;
		if (headers == null || headers.isEmpty()) {
			request = path.request(APPLICATION_JSON);
		} else {
			request = path.request(APPLICATION_JSON).headers(headers);
		}

		try (Response response = request.post(Entity.entity(body, APPLICATION_JSON))) {

			if (response.getStatus() != 200) {
				throw newRestClientException(response);
			}

			if (response.hasEntity()) {
				return response.readEntity(resultClass);
			}
			return null;

		} catch (RestClientException e) {
			LOGGER.error(e.getMessage());
			throw e; // за да не се пропакова

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			throw new RestClientException(e);
		} finally {
			long time = System.currentTimeMillis() - begin;
			if (time > WARN_EXEC_TIME) {
				LOGGER.warn("!!! TIME={}ms. -> post:method={},body={},resultClass={},params={},headers={}", time, method, body, resultClass, params, headers);
			}
		}
	}

	/**
	 * POST извикване application/json което връща List, Map и подобни на тях, който се явяват Generic класове
	 *
	 * @param <T>
	 * @param method
	 * @param body
	 * @param genericType
	 * @param params
	 * @param headers
	 * @return
	 * @throws RestClientException
	 */
	public <T> T post(String method, Object body, GenericType<T> genericType, MultivaluedMap<String, Object> params, MultivaluedMap<String, Object> headers) throws RestClientException {
		LOGGER.debug("post:method={},body={},genericType={},params={},headers={}", method, body, genericType, params, headers);

		long begin = System.currentTimeMillis();

		ResteasyWebTarget path;
		if (params == null || params.isEmpty()) {
			path = this.target.path(method);
		} else {
			path = this.target.path(method).queryParams(params);
		}

		Builder request;
		if (headers == null || headers.isEmpty()) {
			request = path.request(APPLICATION_JSON);
		} else {
			request = path.request(APPLICATION_JSON).headers(headers);
		}

		try (Response response = request.post(Entity.entity(body, APPLICATION_JSON))) {

			if (response.getStatus() != 200) {
				throw newRestClientException(response);
			}

			if (response.hasEntity()) {
				return response.readEntity(genericType);
			}
			return null;

		} catch (RestClientException e) {
			LOGGER.error(e.getMessage());
			throw e; // за да не се пропакова

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			throw new RestClientException(e);
		} finally {
			long time = System.currentTimeMillis() - begin;
			if (time > WARN_EXEC_TIME) {
				LOGGER.warn("!!! TIME={}ms. -> post:method={},body={},genericType={},params={},headers={}", time, method, body, genericType, params, headers);
			}
		}
	}

	private RestClientException newRestClientException(Response response) {
		StringBuilder msg = new StringBuilder() //
			.append("Failed : HTTP errorCode=").append(response.getStatus()) //
			.append(", errorStatus=").append(response.getStatusInfo()); //

		if (response.hasEntity()) { // пробвам да добавя и съобщение или направо грешката ако е дадено нещо от услугата
			try {
				msg.append(", errorMessage=").append(response.readEntity(String.class));
			} catch (Exception e) {
				LOGGER.error("ERROR in creating RestClientException by response={}.", response, e);
			}
		}
		return new RestClientException(msg.toString());
	}
}
