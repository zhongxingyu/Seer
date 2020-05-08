 package info.kuyur.justblog.client.stateless;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.TimeUnit;
 
 import info.kuyur.justblog.client.base.ClientException;
 import info.kuyur.justblog.client.http.ClientConfiguration;
 import info.kuyur.justblog.client.http.Method;
 import info.kuyur.justblog.client.util.ClientUtils;
 import info.kuyur.justblog.client.util.EncryptUtils;
 import info.kuyur.justblog.client.util.ResourceCloser;
 import info.kuyur.justblog.client.util.ResponseUtils;
 import info.kuyur.justblog.client.util.SignUtils;
 import info.kuyur.justblog.utils.Config;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Consts;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 public class HmacClient {
 
 	private final Log logger = LogFactory.getLog(getClass());
 
 	protected final DefaultHttpClient client;
 	private final String account;
 	private final byte[] hashedKey;
 	private final String baseAddress;
 
 	public HmacClient(String hostname, int port, boolean isSecure, String basePath,
 			String account, String password) {
 		validateHost(hostname, port);
 		this.account = account;
 		this.hashedKey = EncryptUtils.toSHA1(password);
 		this.baseAddress = (isSecure ? "https://" : "http://")
 			+ hostname
 			+ (isUsingNonDefaultPort(isSecure, port) ? ":" + String.valueOf(port) : "")
 			+ basePath;
 		HttpParams params = new BasicHttpParams();
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setUserAgent(params, ClientConfiguration.USER_AGENT);
 		HttpProtocolParams.setContentCharset(params, ClientConfiguration.CONTENT_CHARSET);
 		HttpProtocolParams.setUseExpectContinue(params, true);
 		HttpConnectionParams.setConnectionTimeout(params, ClientConfiguration.CONNECTION_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(params, ClientConfiguration.SOKET_TIMEOUT);
 		HttpConnectionParams.setTcpNoDelay(params, true);
 		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
 		cm.setMaxTotal(ClientConfiguration.MAX_CONNECTIONS);
 		cm.setDefaultMaxPerRoute(ClientConfiguration.MAX_CONNECTIONS);
 		cm.closeIdleConnections(30,	TimeUnit.SECONDS);
 		client = new DefaultHttpClient(cm, params);
 		client.getParams().setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, ClientConfiguration.CONNECTION_KEEP_TIMEOUT);
 	}
 
 	private static void validateHost(String hostname, int port) {
 		if (StringUtils.isEmpty(hostname)) {
 			throw new IllegalArgumentException("Hostname may not be null or empty.");
 		}
 		if (port < 0 || port > 65535) {
 			throw new IllegalArgumentException("Port should be between 0 and 65535.");
 		}
 	}
 
 	private static boolean isUsingNonDefaultPort(boolean isSecure, int port) {
 		if (port <= 0)
 			return false;
 		if (!isSecure && port == 80)
 			return false;
 		if (isSecure && port == 443)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Get resource from remote server by REST API.
 	 * @param <T>
 	 * @param clazz type of returned value
 	 * @param resourcePath REST API path
 	 * @param queryParams query parameters containing no signature info
 	 * @return resource as a Java Object
 	 */
 	public <T> T get(Type clazz, String resourcePath, Collection<NameValuePair> queryParams) {
 		if (queryParams == null) {
 			queryParams = new ArrayList<NameValuePair>();
 		}
 		queryParams.add(new BasicNameValuePair(Config.ACCOUNT_FIELD, account));
 		queryParams.add(new BasicNameValuePair(Config.TIMESTAMP_FIELD, SignUtils.getFormattedTimestamp()));
 		String sign = SignUtils.sign(queryParams, null, null, hashedKey, Method.GET.toString(), baseAddress + resourcePath);
 		queryParams.add(new BasicNameValuePair(Config.SIGN_FIELD, sign));
 
 		String queryString = URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		HttpGet get = new HttpGet(baseAddress + resourcePath + "?" + queryString);
 		InputStream is = null;
 		try {
 			logger.info("Sending Request: " + get.getRequestLine());
 			HttpResponse response = client.execute(get);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.<T>readFromJson(is, clazz);
 			} else {
 				StringBuilder buf = new StringBuilder();
 				buf.append("Received error response:  Code=");
 				buf.append(String.valueOf(status));
 				buf.append(", Status=");
 				buf.append(response.getStatusLine().getReasonPhrase());
 				buf.append(Config.LINE_SEPARATOR);
 				buf.append(ResponseUtils.getContentBodyString(response, "UTF-8"));
 				String message = buf.toString();
 				logger.info(message);
 				throw new ClientException(message);
 			}
 		} catch (ClientProtocolException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (IOException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} finally {
 			ResourceCloser.close(is);
 			get.releaseConnection();
 		}
 	}
 
 	/**
 	 * Post a request to remote server by REST API.<br>
 	 * @param <T>
 	 * @param clazz type of returned value
 	 * @param resourcePath REST API path
 	 * @param queryParams query parameters containing no signature info
 	 * @param bodyParams form parameters which has high priority than bodyContent
 	 * @param bodyContent a Java Object which will be deserialized into JSON
 	 * @return resource as a Java Object
 	 */
 	public <T> T post(Type clazz, String resourcePath, Collection<NameValuePair> queryParams,
 			Collection<NameValuePair> bodyParams, Object bodyContent) {
 		if (queryParams == null) {
 			queryParams = new ArrayList<NameValuePair>();
 		}
 		queryParams.add(new BasicNameValuePair(Config.ACCOUNT_FIELD, account));
 		queryParams.add(new BasicNameValuePair(Config.TIMESTAMP_FIELD, SignUtils.getFormattedTimestamp()));
 		String bodyString = (bodyContent == null) ? "" : ClientUtils.toJsonString(bodyContent);
 		String sign = SignUtils.sign(queryParams, bodyParams, bodyString,
 				hashedKey, Method.POST.toString(), baseAddress + resourcePath);
 		queryParams.add(new BasicNameValuePair(Config.SIGN_FIELD, sign));
 
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPost post = new HttpPost(baseAddress + resourcePath + prefix + queryString);
 		InputStream is = null;
 		try {
 			if (bodyParams != null) {
 				post.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
 				post.setEntity(new UrlEncodedFormEntity(bodyParams, Consts.UTF_8));
 			} else if (bodyContent != null) {
 				post.addHeader(HTTP.CONTENT_TYPE, "application/json");
 				post.setEntity(new StringEntity(bodyString, Consts.UTF_8));
 			} else {
 				post.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
 			}
 			logger.info("Sending Request: " + post.getRequestLine());
 			HttpResponse response = client.execute(post);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.<T>readFromJson(is, clazz);
 			} else {
 				StringBuilder buf = new StringBuilder();
 				buf.append("Received error response:  Code=");
 				buf.append(String.valueOf(status));
 				buf.append(", Status=");
 				buf.append(response.getStatusLine().getReasonPhrase());
 				buf.append(Config.LINE_SEPARATOR);
 				buf.append(ResponseUtils.getContentBodyString(response, "UTF-8"));
 				String message = buf.toString();
 				logger.info(message);
 				throw new ClientException(message);
 			}
 		} catch (ClientProtocolException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (IOException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} finally {
 			ResourceCloser.close(is);
 			post.releaseConnection();
 		}
 	}
 
 	/**
 	 * Send a put request to remote server by REST API.<br>
 	 * @param <T>
 	 * @param clazz type of returned value
 	 * @param resourcePath REST API path
 	 * @param queryParams query parameters containing no signature info
 	 * @param bodyContent a Java Object which will be deserialized into JSON
 	 * @return resource as a Java Object
 	 */
 	public <T> T put(Type clazz, String resourcePath, Collection<NameValuePair> queryParams, Object bodyContent) {
 		if (queryParams == null) {
 			queryParams = new ArrayList<NameValuePair>();
 		}
 		queryParams.add(new BasicNameValuePair(Config.ACCOUNT_FIELD, account));
 		queryParams.add(new BasicNameValuePair(Config.TIMESTAMP_FIELD, SignUtils.getFormattedTimestamp()));
 		String bodyString = (bodyContent == null) ? "" : ClientUtils.toJsonString(bodyContent);
 		String sign = SignUtils.sign(queryParams, null, bodyString,
 				hashedKey, Method.PUT.toString(), baseAddress + resourcePath);
 		queryParams.add(new BasicNameValuePair(Config.SIGN_FIELD, sign));
 
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPut put = new HttpPut(baseAddress + resourcePath + prefix + queryString);
 		InputStream is = null;
 		try {
 			put.addHeader(HTTP.CONTENT_TYPE, "application/json");
 			put.setEntity(new StringEntity(bodyString, Consts.UTF_8));
 
 			logger.info("Sending Request: " + put.getRequestLine());
 			HttpResponse response = client.execute(put);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.<T>readFromJson(is, clazz);
 			} else {
 				StringBuilder buf = new StringBuilder();
 				buf.append("Received error response:  Code=");
 				buf.append(String.valueOf(status));
 				buf.append(", Status=");
 				buf.append(response.getStatusLine().getReasonPhrase());
 				buf.append(Config.LINE_SEPARATOR);
 				buf.append(ResponseUtils.getContentBodyString(response, "UTF-8"));
 				String message = buf.toString();
 				logger.info(message);
 				throw new ClientException(message);
 			}
 		} catch (ClientProtocolException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (IOException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} finally {
 			ResourceCloser.close(is);
 			put.releaseConnection();
 		}
 	}
 
 	/**
 	 * Send a delete request to remote server by REST API.<br>
 	 * @param <T>
 	 * @param clazz type of returned value
 	 * @param resourcePath REST API path
 	 * @param queryParams query parameters containing no signature info
 	 * @return resource as a Java Object
 	 */
 	public <T> T delete(Type clazz, String resourcePath, Collection<NameValuePair> queryParams) {
 		if (queryParams == null) {
 			queryParams = new ArrayList<NameValuePair>();
 		}
 		queryParams.add(new BasicNameValuePair(Config.ACCOUNT_FIELD, account));
 		queryParams.add(new BasicNameValuePair(Config.TIMESTAMP_FIELD, SignUtils.getFormattedTimestamp()));
 		String sign = SignUtils.sign(queryParams, null, null, hashedKey, Method.DELETE.toString(), baseAddress + resourcePath);
 		queryParams.add(new BasicNameValuePair(Config.SIGN_FIELD, sign));
 
 		String queryString = URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		HttpDelete delete = new HttpDelete(baseAddress + resourcePath + "?" + queryString);
 		InputStream is = null;
 		try {
 			logger.info("Sending Request: " + delete.getRequestLine());
 			HttpResponse response = client.execute(delete);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.<T>readFromJson(is, clazz);
 			} else {
 				StringBuilder buf = new StringBuilder();
 				buf.append("Received error response:  Code=");
 				buf.append(String.valueOf(status));
 				buf.append(", Status=");
 				buf.append(response.getStatusLine().getReasonPhrase());
 				buf.append(Config.LINE_SEPARATOR);
 				buf.append(ResponseUtils.getContentBodyString(response, "UTF-8"));
 				String message = buf.toString();
 				logger.info(message);
 				throw new ClientException(message);
 			}
 		} catch (ClientProtocolException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (IOException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} finally {
 			ResourceCloser.close(is);
 			delete.releaseConnection();
 		}
 	}
 }
