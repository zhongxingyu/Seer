 package com.thinkinglogic.rest.mock;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Random;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.tools.generic.EscapeTool;
 import org.apache.velocity.tools.generic.XmlTool;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import com.jayway.jsonpath.JsonPath;
 import com.jayway.jsonpath.spi.JsonProviderFactory;
 
 /**
  * Given details of an HttpServletRequest, this class examines the classpath for details of the appropriate response.
  */
 public final class ResponseBuilder {
 
 	private static final Logger logger = Logger.getLogger(ResponseBuilder.class);
 
 	private static final String UTF8 = "UTF-8";
 	private static final Random RANDOM = new Random();
 
 	/** Enumeration of the different types of request content we can recognise. */
 	protected static enum ProbableContentType {
 		XML, JSON, UNKNOWN;
 	}
 
 	protected static final String HEADERS_FILETYPE = "headers";
 
 	protected static final String STATUS_FILETYPE = "status";
 
 	protected static final String BODY_FILETYPE = "body";
 
 	protected static final String DEFAULT_FILE_NAME = "default";
 
 	/**
 	 * The name of the properties file (without the extension) that specifies the directory/filename of the content to
 	 * return.
 	 */
 	public static final String PATH_PROPERTIES_NAME = "path";
 
 	/** The extension of the properties file that specifies the directory/filename of the content to return. */
 	public static final String PATH_PROPERTIES_EXT = "properties";
 
 	/** The name of the properties file that specifies the directory/filename of the content to return. */
 	public static final String PATH_PROPERTIES_FILE = PATH_PROPERTIES_NAME + "." + PATH_PROPERTIES_EXT;
 
 	/** The name of the properties file that specifies global defaults. */
 	public static final String DEFAULT_PATH_PROPERTIES_FILE = "default." + PATH_PROPERTIES_FILE;
 
 	/** The name of the path property that identifies a directory by the request method (GET, PUT etc) - true or false. */
 	public static final String DIR_METHOD = "dir.method";
 	/** The name of the path property that identifies a directory by a query string parameter. */
 	public static final String DIR_QUERYPARAM = "dir.queryParam";
 	/** The name of the path property that identifies a directory by a request header. */
 	public static final String DIR_HEADER = "dir.header";
 	/**
 	 * The name of the path property that identifies a directory by evaluating a json path expression against the
 	 * request body.
 	 */
 	public static final String DIR_JSONPATH = "dir.jsonpath";
 	/**
 	 * The name of the path property that identifies a directory by evaluating an XPath expression against the request
 	 * body.
 	 */
 	public static final String DIR_XPATH = "dir.xpath";
 
 	/** The name of the path property that identifies a file by the request method - set to true or false. */
 	public static final String FILE_METHOD = "file.method";
 	/** The name of the path property that identifies a file by a query string parameter. */
 	public static final String FILE_QUERYPARAM = "file.queryParam";
 	/** The name of the path property that identifies a file by a request header. */
 	public static final String FILE_HEADER = "file.header";
 	/** The name of the path property that identifies a file by evaluating a json path against the request body. */
 	public static final String FILE_JSONPATH = "file.jsonpath";
 	/** The name of the path property that identifies a file by evaluating an XPath expression against the request body. */
 	public static final String FILE_XPATH = "file.xpath";
 
 	/** The name of the path property that specifies whether to parse the response as a velocity template (true/false). */
 	public static final String VELOCITY = "velocity";
 
 	/** The name of the path property that specifies a fixed delay before responding (milliseconds). */
 	public static final String FIXED_DELAY = "fixed.delay";
 
 	/** The name of the path property that specifies the maximum random delay before responding (milliseconds). */
 	public static final String RANDOM_DELAY = "random.delay";
 
 	/** The name of the path property that should be used instead of empty (null or empty string) request properties. */
 	public static final String EMPTY_VALUE_REPLACEMENT = "empty.value.replacement";
 
 	private static final VelocityEngine VELOCITY_ENGINE = initialiseVelocity();
 	private static final Properties GLOBAL_DEFAULTS = getGlobalDefaults();
 	private static final String CLASSPATH_LOCATION = getClassesLocation();
 
 	private final Map<String, String> queryParams;
 	private final Map<String, String> requestHeaders;
 	private final String requestBody;
 	private final String requestPath;
 	private final String requestMethod;
 	private final String servletContext;
 	private ProbableContentType probableContentType;
 
 	private String derivedPath;
 	private String derivedName = DEFAULT_FILE_NAME;
 	private Properties pathProperties = null;
 	private String emptyValueReplacement;
 
 	private Document xmlDocument;
 	private XPathFactory xPathFactory;
 
 	/**
 	 * Create a new ResponseBuilder, and determine the appropriate path and filename for the response.
 	 * 
 	 * @param queryParams query parameters.
 	 * @param headers request headers.
 	 * @param body body of the request (or an empty string).
 	 * @param pathInfo the path of the request (request.getPathInfo()).
 	 * @param requestMethod GET/POST/PUT/DELETE.
 	 */
 	public ResponseBuilder(Map<String, String> queryParams, Map<String, String> headers, String body, String pathInfo,
 			String requestMethod, String servletContext) {
 		super();
 		this.requestMethod = requestMethod;
 		this.queryParams = queryParams;
 		this.requestHeaders = headers;
 		this.requestBody = notNullString(body).trim();
 		this.requestPath = pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo;
 		this.servletContext = servletContext;
 		this.setDerivedPath(requestPath);
 		determineContentType();
 		this.determinePath();
 		this.determineFile();
 	}
 
 	/**
 	 * Makes a best guess at the content type of the specified body (based on the first character of the text). If the
 	 * content is xml, attempts to parse the body.
 	 * 
 	 * @param body the body whose content type should be identified.
 	 */
 	protected void determineContentType() {
 		ProbableContentType contentType = ProbableContentType.UNKNOWN;
 		if (requestBody.startsWith("<")) {
 			try {
 				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 				domFactory.setNamespaceAware(true);
 				DocumentBuilder builder = domFactory.newDocumentBuilder();
 				xmlDocument = builder.parse(IOUtils.toInputStream(requestBody, UTF8));
 				xPathFactory = XPathFactory.newInstance();
 				contentType = ProbableContentType.XML;
 			} catch (ParserConfigurationException | SAXException | IOException e) {
 				logger.error(e);
 			}
 		} else if (requestBody.startsWith("{") || requestBody.startsWith("[")) {
 			contentType = ProbableContentType.JSON;
 		}
 		this.probableContentType = contentType;
 	}
 
 	/**
 	 * Modifies the specified response to set the status, response headers and response body.
 	 * 
 	 * @param response the response to modify.
 	 */
 	public void handleResponse(final HttpServletResponse response) {
 		handleDelay();
 		Map<String, String> responseHeaders = getResponseHeaders();
 		for (Entry<String, String> entry : responseHeaders.entrySet()) {
 			response.addHeader(entry.getKey(), entry.getValue());
 		}
 		int status = getStatus();
 		response.setStatus(status);
 		String body = getResponseBody();
 
 		if (Boolean.parseBoolean(pathProperties.getProperty(VELOCITY, "false"))) {
 			logger.info("Parsing response as a Velocity template");
 			body = parseResponse(body);
 		}
 
 		logger.info("Sending " + status + " response: headers=" + responseHeaders + ", body=\n" + body);
 		response.setContentLength(body.length());
 		try {
 			response.getWriter().write(body);
 			response.getWriter().flush();
 			response.getWriter().close();
 		} catch (IOException e) {
 			logger.error("Unable to write to, flush or close the response writer", e);
 		}
 	}
 
 	/**
 	 * Sleeps if path.properties indicates that we should do so.
 	 */
 	protected void handleDelay() {
 		try {
 			int fixedDelay = Integer.parseInt(pathProperties.getProperty(FIXED_DELAY, "0"));
 			if (fixedDelay > 0) {
 				logger.info("Sleeping for " + fixedDelay + "ms");
 				Thread.sleep(fixedDelay);
 			}
 		} catch (NumberFormatException e) {
 			logger.debug("Unable to parse " + FIXED_DELAY + " as a number: "
 					+ pathProperties.getProperty(FIXED_DELAY, "0"));
 		} catch (InterruptedException e) {
 			logger.error("IntrerruptedException while sleeping", e);
 		}
 		try {
 			int maxDelay = Integer.parseInt(pathProperties.getProperty(RANDOM_DELAY, "0"));
 			if (maxDelay > 0) {
 				final int randomDelay = RANDOM.nextInt(maxDelay);
 				logger.info("Sleeping for " + randomDelay + "ms");
 				Thread.sleep(randomDelay);
 			}
 		} catch (NumberFormatException e) {
 			logger.debug("Unable to parse " + RANDOM_DELAY + " as a number: "
 					+ pathProperties.getProperty(RANDOM_DELAY, "0"));
 		} catch (InterruptedException e) {
 			logger.error("IntrerruptedException while sleeping", e);
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return the body to set in the response.
 	 */
 	protected String getResponseBody() {
 		InputStream stream = loadFile(derivedPath, derivedName, BODY_FILETYPE);
 		if (stream == null) {
 			logger.error("Unable to retrieve body from " + derivedPath);
 			return "";
 		}
 		try {
 			return IOUtils.toString(stream, UTF8);
 		} catch (IOException | RuntimeException e) {
 			logger.error("Unable to get body from stream", e);
 		} finally {
 			IOUtils.closeQuietly(stream);
 		}
 		return "";
 	}
 
 	/**
 	 * @return the http response code (status) for the response.
 	 */
 	protected int getStatus() {
 		InputStream stream = loadFile(derivedPath, derivedName, STATUS_FILETYPE);
 		if (stream == null) {
 			logger.error("Unable to retrieve status from " + derivedPath);
 			return 500;
 		}
 		try {
			return Integer.parseInt(IOUtils.toString(stream, UTF8));
 		} catch (IOException | RuntimeException e) {
 			logger.error("Unable to get status from stream", e);
 			return 500;
 		} finally {
 			IOUtils.closeQuietly(stream);
 		}
 	}
 
 	/**
 	 * 
 	 * @return the headers to set in the response;
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected Map<String, String> getResponseHeaders() {
 		TreeMap<String, String> map = new TreeMap<String, String>();
 		// load default headers
 		InputStream stream = loadFile(derivedPath, DEFAULT_FILE_NAME, HEADERS_FILETYPE);
 		map.putAll((Map) loadPropertiesFromStream(stream));
 		// load specific headers
 		if (!DEFAULT_FILE_NAME.equals(derivedName)) {
 			stream = this.getClass().getResourceAsStream(derivedPath + derivedName + HEADERS_FILETYPE);
 			map.putAll((Map) loadPropertiesFromStream(stream));
 		}
 		return map;
 	}
 
 	/**
 	 * Loads the InputStream as a properties file.
 	 * 
 	 * @param stream the InputStream to load.
 	 * @return a Properties file based on the contents of the stream.
 	 */
 	protected Properties loadPropertiesFromStream(InputStream stream) {
 		Properties properties = new Properties();
 		if (stream != null) {
 			try {
 				properties.load(stream);
 			} catch (IOException e) {
 				logger.error(e);
 			} finally {
 				IOUtils.closeQuietly(stream);
 			}
 		}
 		return properties;
 	}
 
 	/**
 	 * Identifies the path in which we should look for return values, starting with the request path and evaluating
 	 * directories as identified in path.properties.
 	 */
 	protected void determinePath() {
 		try (InputStream stream = this.getClass().getResourceAsStream(derivedPath + PATH_PROPERTIES_FILE);) {
 			if (stream == null) {
 				logger.debug("No path.properties at " + derivedPath);
 				if (pathProperties == null) { // walk back up the path looking for path.properties
 					pathProperties = new Properties(GLOBAL_DEFAULTS);
 					pathProperties.putAll(loadPropertiesFromStream(this.loadFile(derivedPath, PATH_PROPERTIES_NAME,
 							PATH_PROPERTIES_EXT)));
 					this.emptyValueReplacement = pathProperties.getProperty(EMPTY_VALUE_REPLACEMENT, "");
 				}
 			} else {
 				logger.debug("Found path.properties at " + derivedPath);
 				pathProperties = new Properties(GLOBAL_DEFAULTS);
 				pathProperties.load(stream);
 				this.emptyValueReplacement = pathProperties.getProperty(EMPTY_VALUE_REPLACEMENT, "");
 				String key = "";
 				String property = "";
 
 				if (Boolean.parseBoolean(pathProperties.getProperty(DIR_METHOD, ""))) {
 					property = requestMethod;
 					logger.debug("Matched " + DIR_METHOD + ": " + property);
 					setDerivedPath(derivedPath + property);
 					determinePath();
 					return;
 				}
 
 				key = pathProperties.getProperty(DIR_HEADER, "");
 				property = matchDirHeader(key);
 				if (property.length() > 0) {
 					logger.debug("Matched " + DIR_HEADER + ": " + key + "=" + property);
 					setDerivedPath(derivedPath + property);
 					determinePath();
 					return;
 				}
 
 				key = pathProperties.getProperty(DIR_QUERYPARAM, "");
 				property = getProperty(queryParams, key);
 				if (property.length() > 0) {
 					logger.debug("Matched " + DIR_QUERYPARAM + ": " + key + "=" + property);
 					setDerivedPath(derivedPath + property);
 					determinePath();
 					return;
 				}
 
 				key = pathProperties.getProperty(DIR_JSONPATH, "");
 				property = matchJsonPath(key);
 				if (property.length() > 0) {
 					logger.debug("Matched " + DIR_JSONPATH + ": " + key + "=" + property);
 					setDerivedPath(derivedPath + property);
 					determinePath();
 					return;
 				}
 
 				key = pathProperties.getProperty(DIR_XPATH, "");
 				property = matchXPath(key);
 				if (property.length() > 0) {
 					logger.debug("Matched " + DIR_XPATH + ": " + key + "=" + property);
 					setDerivedPath(derivedPath + property);
 					determinePath();
 					return;
 				}
 			}
 		} catch (IOException e) {
 			logger.error(e);
 		}
 	}
 
 	/**
 	 * Returns the requested value, or emptyValueReplacement if the key is not empty but there is no value in the map.
 	 * 
 	 * @param map the map to get a property from.
 	 * @param key the key of the property to get.
 	 * @return the value stored against the key in the map, or emptyValueReplacement if the key is not empty.
 	 */
 	protected String getProperty(final Map<String, String> map, final String key) {
 		if (notNullString(key).length() > 0) {
 			return replaceEmptyValue(map.get(key));
 		}
 		return "";
 	}
 
 	/**
 	 * Looks for a request header matching the specified name, with special handling for the 'Accept' header.
 	 * 
 	 * @param headerName the name of the header to match.
 	 * @return
 	 */
 	protected String matchDirHeader(final String headerName) {
 		if ("Accept".equalsIgnoreCase(headerName)) {
 			// try to find a match for the mime type
 			String accepts = notNullString(requestHeaders.get(headerName)) + ";";
 			String[] types = accepts.substring(0, accepts.indexOf(";")).split(",");
 			for (final String type : types) {
 				// convert "/application/xml" to "xml"
 				String shortType = type.substring(("/" + type).lastIndexOf("/"));
 				// try to find type/default.body
 				String filename = derivedPath + shortType + "/" + DEFAULT_FILE_NAME + "." + BODY_FILETYPE;
 				logger.debug("Trying to match Accept header '" + type + "': Looking for " + filename);
 				URL resource = this.getClass().getResource(filename);
 				if (resource != null) {
 					return shortType;
 				}
				// try to find type/default.body
 				filename = derivedPath + shortType + "/" + PATH_PROPERTIES_FILE;
 				logger.debug("Trying to match Accept header '" + type + "': Looking for " + filename);
 				resource = this.getClass().getResource(filename);
 				if (resource != null) {
 					return shortType;
 				}
 			}
 		} else {
 			return getProperty(requestHeaders, headerName);
 		}
 		return "";
 	}
 
 	/**
 	 * Evaluates the specified json path against the request body.
 	 * 
 	 * @param path the json path to evaluate.
 	 * @return the result of evaluating the path expression against the request body.
 	 * @see <a href="http://code.google.com/p/json-path/">code.google.com/p/json-path/</a>
 	 */
 	protected String matchJsonPath(final String path) {
 		if (path.length() > 0 & probableContentType == ProbableContentType.JSON) {
 			try {
 				return replaceEmptyValue(JsonPath.read(requestBody, path));
 			} catch (Exception e) {
 				logger.error("Unable to evaluate JSONPATH: " + path, e);
 				return emptyValueReplacement;
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Evaluates the specified xpath against the request body.
 	 * 
 	 * @param path the x path to evaluate.
 	 * @return the result of evaluating the path expression against the request body.
 	 * @see <a
 	 *      href="http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html">www.ibm.com/developerworks/library/x-javaxpathapi/index.html</a>
 	 */
 	protected String matchXPath(final String path) {
 		if (path.length() > 0 & probableContentType == ProbableContentType.XML) {
 			try {
 				return replaceEmptyValue(xPathFactory.newXPath().evaluate(path, this.xmlDocument));
 			} catch (Exception e) {
 				logger.error("Unable to evaluate XPATH: " + path, e);
 				return emptyValueReplacement;
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Attempts to identify a file name for return values, based on keys in pathProperties.
 	 */
 	protected void determineFile() {
 		String key = "";
 		String property = "";
 
 		if (Boolean.parseBoolean(pathProperties.getProperty(FILE_METHOD, ""))) {
 			property = requestMethod;
 			logger.debug("Matched " + FILE_METHOD + ": " + property);
 			this.derivedName = property;
 			return;
 		}
 
 		key = pathProperties.getProperty(FILE_HEADER, "");
 		property = matchFileHeader(key);
 		if (property.length() > 0) {
 			logger.debug("Matched " + FILE_HEADER + ": " + key + "=" + property);
 			this.derivedName = property;
 			return;
 		}
 
 		key = pathProperties.getProperty(FILE_QUERYPARAM, "");
 		property = getProperty(queryParams, key);
 		if (property.length() > 0) {
 			logger.debug("Matched " + FILE_QUERYPARAM + ": " + key + "=" + property);
 			this.derivedName = property;
 			return;
 		}
 
 		key = pathProperties.getProperty(FILE_JSONPATH, "");
 		property = matchJsonPath(key);
 		if (property.length() > 0) {
 			logger.debug("Matched " + FILE_JSONPATH + ": " + key + "=" + property);
 			this.derivedName = property;
 			return;
 		}
 
 		key = pathProperties.getProperty(FILE_XPATH, "");
 		property = matchXPath(key);
 		if (property.length() > 0) {
 			logger.debug("Matched " + FILE_XPATH + ": " + key + "=" + property);
 			this.derivedName = property;
 			return;
 		}
 
 	}
 
 	/**
 	 * Looks for a request header matching the specified name, with special handling for the 'Accept' header.
 	 * 
 	 * @param headerName the name of the header to match.
 	 * @return
 	 */
 	protected String matchFileHeader(final String headerName) {
 		if ("Accept".equalsIgnoreCase(headerName)) {
 			// try to find a match for the mime type
 			String accepts = notNullString(requestHeaders.get(headerName)) + ";";
 			String[] types = accepts.substring(0, accepts.indexOf(";")).split(",");
 			for (final String type : types) {
 				// convert "/application/xml" to "xml"
 				String shortType = type.substring(("/" + type).lastIndexOf("/") + 1);
 				// try to find type.body
 				String filename = derivedPath + shortType + "." + BODY_FILETYPE;
 				logger.debug("Trying to match Accept header '" + type + "': Looking for " + filename);
 				URL resource = this.getClass().getResource(filename);
 				if (resource != null) {
 					return shortType;
 				}
 			}
 		} else {
 			return getProperty(requestHeaders, headerName);
 		}
 		return "";
 	}
 
 	/**
 	 * Attempts to load the specified file from the classpath, looking in the specified path first, then in each parent
 	 * directory in turn. If the file cannot be found in the specified path, then the default file in the path will be
 	 * returned if it exists, else we recurse in the parent directory.
 	 * 
 	 * @param myPath the path to start searching in. Must start with "/".
 	 * @param file the name of the file to load (without extension).
 	 * @param ext the extension of the file to look for.
 	 * @return an InputStream if we could find a matching file, null otherwise.
 	 */
 	protected InputStream loadFile(final String path, final String file, final String ext) {
 		final String myPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
 		String name = myPath + "/" + file + "." + ext;
 		InputStream stream = this.getClass().getResourceAsStream(name);
 		if (stream == null && !DEFAULT_FILE_NAME.equals(file)) {
 			name = myPath + "/" + DEFAULT_FILE_NAME + "." + ext;
 			stream = this.getClass().getResourceAsStream(name);
 		}
 		if (stream == null) {
 			if (myPath.lastIndexOf("/") < 0) {
 				return null;
 			}
 			String newPath = myPath.substring(0, myPath.lastIndexOf("/"));
 			return loadFile(newPath, file, ext);
 		}
 		logger.debug("Found a " + ext + " file: " + name);
 		return stream;
 	}
 
 	/**
 	 * @param derivedPath the derivedPath to set
 	 */
 	private void setDerivedPath(String derivedPath) {
 		if (!derivedPath.endsWith("/")) {
 			derivedPath = derivedPath + "/";
 		}
 		this.derivedPath = derivedPath;
 	}
 
 	/**
 	 * @param string the string to replace with emptyValueReplacement if it is empty (null or empty string)
 	 * @return the string, or emptyValueReplacement if the string is empty.
 	 */
 	protected String replaceEmptyValue(final Object string) {
 		String value = notNullString(string);
 		if (value.length() == 0) {
 			value = emptyValueReplacement;
 		}
 		return value;
 	}
 
 	/**
 	 * @param string the string to ensure is not null.
 	 * @return returns the string, or empty string if null.
 	 */
 	protected String notNullString(final Object string) {
 		if (string == null) {
 			return "";
 		}
 		return string.toString();
 	}
 
 	/**
 	 * Parses the specified string as a velocity template.
 	 * 
 	 * @param body the string to parse.
 	 * @return the string, parsed as a velocity template.
 	 */
 	protected String parseResponse(final String body) {
 		VelocityContext context = new VelocityContext();
 		context.put("queryParams", this.queryParams);
 		context.put("requestHeaders", this.requestHeaders);
 		context.put("requestMethod", this.requestMethod);
 		context.put("pathInfo", this.requestPath);
 		context.put("context", this.servletContext);
 		context.put("esc", new EscapeTool());
 		context.put("request", requestBody);
 		context.put("classpathLocation", CLASSPATH_LOCATION);
 		try {
 			if (ProbableContentType.XML.equals(this.probableContentType)) {
 				context.put("request", new XmlTool().parse(requestBody));
 			} else if (ProbableContentType.JSON.equals(this.probableContentType)) {
 				context.put("request", JsonProviderFactory.createProvider().parse(requestBody));
 			}
 		} catch (RuntimeException e) {
 			logger.error("Unable to parse requestBody as " + this.probableContentType, e);
 		}
 		final StringWriter stringWriter = new StringWriter();
 		VELOCITY_ENGINE.evaluate(context, stringWriter, "Velocity", body);
 		return stringWriter.toString();
 	}
 
 	/**
 	 * Creates and initialises a new VelocityEngine.
 	 * 
 	 * @return a new VelocityEngine.
 	 */
 	protected static VelocityEngine initialiseVelocity() {
 		VelocityEngine ve = new VelocityEngine();
 		ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute");
 		ve.setProperty("runtime.log.logsystem.log4j.logger", "root");
 		ve.setProperty(VelocityEngine.INPUT_ENCODING, UTF8);
 		ve.setProperty(VelocityEngine.OUTPUT_ENCODING, UTF8);
 		ve.init();
 		return ve;
 	}
 
 	/**
 	 * reads default.path.properties.
 	 * 
 	 * @return a Properties containing the global defaults.
 	 */
 	protected static Properties getGlobalDefaults() {
 		Properties properties = new Properties();
 		try {
 			properties.load(ResponseBuilder.class.getResourceAsStream("/" + DEFAULT_PATH_PROPERTIES_FILE));
 		} catch (IOException | RuntimeException e) {
 			logger.error("Unable to load default properties from classpath: /" + DEFAULT_PATH_PROPERTIES_FILE, e);
 		}
 		return properties;
 	}
 
 	/**
 	 * @return the location of default.path.properties (the containing folder).
 	 */
 	protected static String getClassesLocation() {
 		try {
 			URL resource = ResponseBuilder.class.getResource("/" + DEFAULT_PATH_PROPERTIES_FILE);
 			logger.info("Found " + DEFAULT_PATH_PROPERTIES_FILE + " at " + resource);
 			return resource.getPath().replace(DEFAULT_PATH_PROPERTIES_FILE, "");
 		} catch (RuntimeException e) {
 			logger.error("Unable to find location of file in classpath: /" + DEFAULT_PATH_PROPERTIES_FILE, e);
 		}
 		return "";
 	}
 }
