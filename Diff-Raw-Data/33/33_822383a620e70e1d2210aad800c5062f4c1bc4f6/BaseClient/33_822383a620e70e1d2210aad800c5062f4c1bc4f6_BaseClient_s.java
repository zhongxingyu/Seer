 package info.kuyur.justblog.client.stateful;
 
 import info.kuyur.justblog.client.base.ClientException;
 import info.kuyur.justblog.client.http.AllowAllCookieSpecFactory;
 import info.kuyur.justblog.client.http.ClientConfiguration;
 import info.kuyur.justblog.client.http.ConcurrentDistinctCookieStore;
 import info.kuyur.justblog.client.http.Method;
 import info.kuyur.justblog.client.util.ClientUtils;
 import info.kuyur.justblog.client.util.EncryptUtils;
 import info.kuyur.justblog.client.util.ResourceCloser;
 import info.kuyur.justblog.client.util.ResponseUtils;
 import info.kuyur.justblog.client.util.SignUtils;
 import info.kuyur.justblog.utils.Config;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Consts;
 import org.apache.http.Header;
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
 import org.apache.http.cookie.Cookie;
 import org.apache.http.cookie.CookieOrigin;
 import org.apache.http.cookie.CookieSpec;
 import org.apache.http.cookie.CookieSpecFactory;
 import org.apache.http.cookie.MalformedCookieException;
 import org.apache.http.cookie.params.CookieSpecPNames;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 public class BaseClient implements ISessionClient {
 
 	private final Log logger = LogFactory.getLog(getClass());
 
 	protected final DefaultHttpClient client;
 	private final String account;
 	private final byte[] hashedKey;
 	private final String baseAddress;
 	private final CookieOrigin cookieOrigin;
 	private final ConcurrentDistinctCookieStore cookieStore = new ConcurrentDistinctCookieStore();;
 
 	private static final String AUTH_PATH = "/rest/stateful/login";
 	private static final String SESSION_TOKEN = Config.LOGINED_SESSION_TOKEN;
 	private static final CookieSpecFactory COOKIE_SPEC_FACTORY = new AllowAllCookieSpecFactory();
 
 	/**
 	 * @param hostname should like 127.0.0.1 or a domain name
 	 * @param port
 	 * @param isSecure
 	 * @param basePath path where justblog is installed on server, it should like /justblog
 	 * @param account
 	 * @param password
 	 */
 	public BaseClient(String hostname, int port, boolean isSecure, String basePath,
 			String account, String password) {
 		validateHost(hostname, port);
 		this.account = account;
 		this.hashedKey = EncryptUtils.toSHA1(password);
 		this.baseAddress = (isSecure ? "https://" : "http://")
 			+ hostname
 			+ (isUsingNonDefaultPort(isSecure, port) ? ":" + String.valueOf(port) : "")
 			+ basePath;
 		cookieOrigin = new CookieOrigin(hostname, port, "/", isSecure);
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
 		client.getCookieSpecs().register("NoValidationCookieSpec", COOKIE_SPEC_FACTORY);
 		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "NoValidationCookieSpec");
 		client.getParams().setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, ClientConfiguration.CONNECTION_KEEP_TIMEOUT);
 		client.getParams().setParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
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
 	 * you may need to login first before accessing some api.
 	 */
 	@Override
 	public void login() {
 		HttpPost post = new HttpPost(baseAddress + AUTH_PATH);
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair(Config.ACCOUNT_FIELD, account));
 		pairs.add(new BasicNameValuePair(Config.TIMESTAMP_FIELD, SignUtils.getFormattedTimestamp()));
 		String sign = SignUtils.sign(null, pairs, null, hashedKey, Method.POST.toString(), baseAddress + AUTH_PATH);
 		pairs.add(new BasicNameValuePair(Config.SIGN_FIELD, sign));
 
 		try {
 			post.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
 			post.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
 			logger.info("Sending Request: " + post.getRequestLine());
 			HttpResponse response = client.execute(post);
 			if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
 				Header[] cookieHeaders = response.getHeaders("Set-Cookie");
 				if (cookieHeaders == null || cookieHeaders.length <= 0) {
 					throw new ClientException("Login Failed!");
 				}
 				int last = cookieHeaders.length - 1;
 				CookieSpec cookieSpec = COOKIE_SPEC_FACTORY.newInstance(post.getParams());
 				Map<String, Cookie> cookies = toCookieMap(cookieSpec.parse(cookieHeaders[last], cookieOrigin));
 				if (!cookies.containsKey(SESSION_TOKEN)) {
 					cookieStore.clear();
 					throw new ClientException("Login Failed! No " + SESSION_TOKEN + " contained in Cookies.");
 				} else {
 					cookieStore.addCookies(cookies);
 				}
 			} else {
 				throw new ClientException("Login Failed! " + ResponseUtils.getContentBodyString(response, "UTF-8"));
 			}
 		} catch (UnsupportedEncodingException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (ClientProtocolException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (IOException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} catch (MalformedCookieException e) {
 			logger.error("Error happens.", e);
 			throw new ClientException(e);
 		} finally {
 			post.releaseConnection();
 		}
 	}
 
 	private static Map<String, Cookie> toCookieMap(List<Cookie> cookies) {
 		if (cookies == null) {
 			return Collections.emptyMap();
 		}
 		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
 		for (Cookie cookie : cookies) {
 			if (cookie.getName() == null) {continue;}
 			cookieMap.put(cookie.getName(), cookie);
 		}
 		return cookieMap;
 	}
 
 	@Override
 	public void logout() {
 		cookieStore.clear();
 	}
 
 	/**
 	 * When the client is not in used anymore, close it.
 	 */
 	@Override
 	public void close() {
 		client.getConnectionManager().shutdown();
 	}
 
 	@Override
 	public List<Cookie> getCookies() {
 		return cookieStore.getCookies();
 	}
 
 	/**
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T get(Type clazz, String resourcePath, Collection<NameValuePair> queryParams) {
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpGet get = new HttpGet(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				get.setHeader(cookieHeader);
 			}
 			logger.info("Sending Request: " + get.getRequestLine());
 			HttpResponse response = client.execute(get);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.readFromJson(is, clazz);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T post(Type clazz, String resourcePath, Collection<NameValuePair> queryParams,
 			Collection<NameValuePair> bodyParams, Object bodyContent) {
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPost post = new HttpPost(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				post.setHeader(cookieHeader);
 			}
 			if (bodyParams != null) {
 				post.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
 				post.setEntity(new UrlEncodedFormEntity(bodyParams, Consts.UTF_8));
 			} else if (bodyContent != null) {
 				post.addHeader(HTTP.CONTENT_TYPE, "application/json");
 				post.setEntity(new StringEntity(ClientUtils.toJsonString(bodyContent), Consts.UTF_8));
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
				return ClientUtils.readFromJson(is, clazz);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public File getFile(String pathToSave, String resourcePath, Collection<NameValuePair> queryParams) {
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpGet get = new HttpGet(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				get.setHeader(cookieHeader);
 			}
 			logger.info("Sending Request: " + get.getRequestLine());
 			HttpResponse response = client.execute(get);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				is = response.getEntity().getContent();
 				return ClientUtils.saveToFile(is, pathToSave);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T postFile(Type clazz, String resourcePath, Collection<NameValuePair> queryParams,
 			Collection<NameValuePair> bodyParams, File file, String fileField) {
 		if (!ClientUtils.fileExisting(file)) {
 			throw new ClientException("File not existing.");
 		}
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPost post = new HttpPost(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				post.setHeader(cookieHeader);
 			}
 			FileBody bin = new FileBody(file);
 			MultipartEntity entity = new MultipartEntity();
 			entity.addPart(fileField, bin);
 			if (bodyParams != null) {
 				for (NameValuePair pair : bodyParams) {
 					entity.addPart(pair.getName(), new StringBody(pair.getValue(), Consts.UTF_8));
 				}
 			}
 			post.setHeader(entity.getContentType());
 			post.setEntity(entity);
 			logger.info("Sending Request: " + post.getRequestLine());
 			HttpResponse response = client.execute(post);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.readFromJson(is, clazz);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T postFiles(Type clazz, String resourcePath, Collection<NameValuePair> queryParams,
 			Collection<NameValuePair> bodyParams, Map<String, File> files) {
 		if (files == null || files.size() <= 0) {
 			throw new ClientException("No file selected.");
 		}
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPost post = new HttpPost(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				post.setHeader(cookieHeader);
 			}
 			MultipartEntity entity = new MultipartEntity();
 			boolean noValidFile = true;
 			for (Map.Entry<String, File> fileWithField : files.entrySet()) {
 				if (ClientUtils.fileExisting(fileWithField.getValue())) {
 					FileBody bin = new FileBody(fileWithField.getValue());
 					entity.addPart(fileWithField.getKey(), bin);
 					noValidFile = false;
 				}
 			}
 			if (noValidFile) {
 				throw new ClientException("No file selected.");
 			}
 			if (bodyParams != null) {
 				for (NameValuePair pair : bodyParams) {
 					entity.addPart(pair.getName(), new StringBody(pair.getValue(), Consts.UTF_8));
 				}
 			}
 			post.setHeader(entity.getContentType());
 			post.setEntity(entity);
 			logger.info("Sending Request: " + post.getRequestLine());
 			HttpResponse response = client.execute(post);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.readFromJson(is, clazz);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T put(Type clazz, String resourcePath, Collection<NameValuePair> queryParams, Object bodyContent) {
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpPut put = new HttpPut(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				put.setHeader(cookieHeader);
 			}
 			if (bodyContent != null) {
 				put.addHeader(HTTP.CONTENT_TYPE, "application/json");
 				put.setEntity(new StringEntity(ClientUtils.toJsonString(bodyContent), Consts.UTF_8));
 			}
 			logger.info("Sending Request: " + put.getRequestLine());
 			HttpResponse response = client.execute(put);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.readFromJson(is, clazz);
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
 	 * Notice that it won't check logined status when sending the request.<br>
 	 * Cookies won't be changed after any response received.
 	 */
 	@Override
 	public <T> T delete(Type clazz, String resourcePath, Collection<NameValuePair> queryParams) {
 		InputStream is = null;
 		String queryString = (queryParams == null) ? "" : URLEncodedUtils.format(queryParams, Consts.UTF_8);
 		String prefix = (StringUtils.isEmpty(queryString)) ? "" : "?";
 		HttpDelete delete = new HttpDelete(baseAddress + resourcePath + prefix + queryString);
 		try {
 			Header cookieHeader = ClientUtils.formatCookies(getCookies()); 
 			if (cookieHeader != null) {
 				delete.setHeader(cookieHeader);
 			}
 			logger.info("Sending Request: " + delete.getRequestLine());
 			HttpResponse response = client.execute(delete);
 			int status = response.getStatusLine().getStatusCode();
 			if (status >= 200 && status <= 299) {
 				if (clazz == null || clazz == Void.class) {
 					return null;
 				}
 				is = response.getEntity().getContent();
				return ClientUtils.readFromJson(is, clazz);
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
