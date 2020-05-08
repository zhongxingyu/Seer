 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.utils;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.AbstractParser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.persistency.db.DatabaseConnection;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.springframework.util.StringUtils;
 
 /**
  * This class provides a variety of utility methods.
  */
 public class Utilities {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(Utilities.class.getName());
 	private static Pattern INVALID_TOPIC_NAME_PATTERN = null;
 	private static Pattern VALID_USER_LOGIN_PATTERN = null;
 
 	static {
 		try {
 			INVALID_TOPIC_NAME_PATTERN = Pattern.compile("([\\n\\r\\\\<>\\[\\]?#]+)");
 			VALID_USER_LOGIN_PATTERN = Pattern.compile("([A-Za-z0-9_]+)");
 		} catch (Exception e) {
 			logger.severe("Unable to compile pattern", e);
 		}
 	}
 
 	/**
 	 * Utility method for setting a cookie.  This method will overwrite an existing
 	 * cookie of the same name if such a cookie already exists.
 	 *
 	 * @param response The servlet response object.
 	 * @param cookieName The name of the cookie to be set.
 	 * @param cookieValue The value of the cookie to be set.
 	 * @param cookieAge The length of time before the cookie expires, specified in seconds.
 	 * @throws Exception Thrown if any error occurs while setting cookie values.
 	 */
 	public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, int cookieAge) throws Exception {
 		Cookie cookie = null;
 		// after confirming credentials
 		cookie = new Cookie(cookieName, cookieValue);
 		cookie.setMaxAge(cookieAge);
 		response.addCookie(cookie);
 	}
 
 	/**
 	 * Convert a string value from one encoding to another.
 	 *
 	 * @param text The string that is to be converted.
 	 * @param fromEncoding The encoding that the string is currently encoded in.
 	 * @param toEncoding The encoding that the string is to be encoded to.
 	 * @return The encoded string.
 	 */
 	public static String convertEncoding(String text, String fromEncoding, String toEncoding) {
 		if (!StringUtils.hasText(text)) return text;
 		if (!StringUtils.hasText(fromEncoding)) {
 			logger.warning("No character encoding specified to convert from, using UTF-8");
 			fromEncoding = "UTF-8";
 		}
 		if (!StringUtils.hasText(toEncoding)) {
 			logger.warning("No character encoding specified to convert to, using UTF-8");
 			toEncoding = "UTF-8";
 		}
 		try {
 			text = new String(text.getBytes(fromEncoding), toEncoding);
 		} catch (Exception e) {
 			// bad encoding
 			logger.warning("Unable to convert value " + text + " from " + fromEncoding + " to " + toEncoding, e);
 		}
 		return text;
 	}
 
 	/**
 	 * Retrieve the current logged-in user from the servlet request.  If there is
 	 * no user return <code>null</code>.
 	 *
 	 * @param reqeust The servlet request object.
 	 * @return The current logged-in user, or <code>null</code> if there is no
 	 *  user currently logged in.
 	 */
 	public static WikiUser currentUser(HttpServletRequest request) {
 		// first look for user in the session
 		WikiUser user = (WikiUser)request.getSession().getAttribute(JAMWikiServlet.PARAMETER_USER);
 		if (user != null) return user;
 		// look for user cookie
 		String userInfo = Utilities.retrieveCookieValue(request, JAMWikiServlet.USER_COOKIE);
 		if (!StringUtils.hasText(userInfo)) return null;
 		StringTokenizer tokens = new StringTokenizer(userInfo, JAMWikiServlet.USER_COOKIE_DELIMITER);
 		if (tokens.countTokens() != 2) return null;
 		String login = tokens.nextToken();
 		String encryptedPassword = tokens.nextToken();
 		try {
 			user = WikiBase.getHandler().lookupWikiUser(login, encryptedPassword, true);
 		} catch (Exception e) {
 			// FIXME - safe to ignore?
 		}
 		if (user != null) request.getSession().setAttribute(JAMWikiServlet.PARAMETER_USER, user);
 		return user;
 	}
 
 	/**
 	 * Decode a value that has been retrieved from a servlet request.  This
 	 * method will replace any underscores with spaces.
 	 *
 	 * @param The encoded value that is to be decoded.
 	 * @return A decoded value.
 	 */
 	public static String decodeFromRequest(String url) {
 		// convert underscores to spaces
 		url = StringUtils.replace(url, "_", " ");
 		return url;
 	}
 
 	/**
 	 * Decode a value that has been retrieved directly from a URL or file
 	 * name.  This method will URL decode the value and then replace any
 	 * underscores with spaces.  Note that this method SHOULD NOT be called
 	 * for values retrieved using request.getParameter(), but only values
 	 * taken directly from a URL.
 	 *
 	 * @param The encoded value that is to be decoded.
 	 * @return A decoded value.
 	 */
 	public static String decodeFromURL(String url) {
 		try {
 			url = URLDecoder.decode(url, "UTF-8");
 		} catch (Exception e) {
 			logger.warning("Failure while decoding url " + url + " with charset UTF-8");
 		}
 		return Utilities.decodeFromRequest(url);
 	}
 
 	/**
 	 * Convert a topic name or other value into a value suitable for use as a
 	 * file name.  This method replaces spaces with underscores, and then URL
 	 * encodes the value.
 	 *
 	 * @param name The value that is to be encoded for use as a file name.
 	 * @return The encoded value.
 	 */
 	public static String encodeForFilename(String name) {
 		// replace spaces with underscores
 		name = StringUtils.replace(name, " ", "_");
 		// URL encode the rest of the name
 		try {
 			name = URLEncoder.encode(name, "UTF-8");
 		} catch (Exception e) {
 			logger.warning("Failure while encoding " + name + " with charset UTF-8");
 		}
 		return name;
 	}
 
 	/**
 	 * Encode a topic name for use in a URL.  This method will replace spaces
 	 * with underscores and URL encode the value, but it will not URL encode
 	 * colons.
 	 *
 	 * @param url The topic name to be encoded for use in a URL.
 	 * @return The encoded topic name value.
 	 */
 	public static String encodeForURL(String url) {
 		url = Utilities.encodeForFilename(url);
 		// un-encode colons
 		url = StringUtils.replace(url, "%3A", ":");
 		// un-encode forward slashes
 		url = StringUtils.replace(url, "%2F", "/");
 		return url;
 	}
 
 	/**
 	 * Replace any occurrences of <, >, ", ', or & with their HTML equivalents.
 	 *
 	 * @param input The text from which XML characters are to be escaped.
 	 * @return An escaped version of the given text.
 	 */
 	public static String escapeHTML(String input) {
 		if (!StringUtils.hasText(input)) return input;
 		// for obvious reasons ampersand must be replaced first
 		input = StringUtils.replace(input, "&", "&amp;");
 		input = StringUtils.replace(input, ">", "&gt;");
 		input = StringUtils.replace(input, "<", "&lt;");
 		input = StringUtils.replace(input, "\"", "&quot;");
 		input = StringUtils.replace(input, "'", "&#39;");
 		return input;
 	}
 
 	/**
 	 * Given an article name, return the appropriate comments topic article name.
 	 * For example, if the article name is "Topic" then the return value is
 	 * "Comments:Topic".
 	 *
 	 * @param name The article name from which a comments article name is to
 	 *  be constructed.
 	 * @return The comments article name for the article name.
 	 */
 	public static String extractCommentsLink(String name) {
 		if (name == null) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
 		if (!StringUtils.hasText(wikiLink.getNamespace())) {
 			return WikiBase.NAMESPACE_COMMENTS + WikiBase.NAMESPACE_SEPARATOR + name;
 		}
 		String namespace = wikiLink.getNamespace();
 		if (namespace.equals(WikiBase.NAMESPACE_SPECIAL)) {
 			return null;
 		} else if (Utilities.isCommentsPage(name)) {
 			return name;
 		} else if (namespace.equals(WikiBase.NAMESPACE_CATEGORY)) {
 			return WikiBase.NAMESPACE_CATEGORY_COMMENTS + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_IMAGE)) {
 			return WikiBase.NAMESPACE_IMAGE_COMMENTS + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_USER)) {
 			return WikiBase.NAMESPACE_USER_COMMENTS + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_JAMWIKI)) {
 			return WikiBase.NAMESPACE_JAMWIKI_COMMENTS + name.substring(namespace.length());
 		} else {
			return null;
 		}
 	}
 
 	/**
 	 * Given an article name, extract an appropriate topic article name.  For
 	 * example, if the article name is "Comments:Topic" then the return value
 	 * is "Topic".
 	 *
 	 * @param name The article name from which a topic article name is to be
 	 *  constructed.
 	 * @return The topic article name for the article name.
 	 */
 	public static String extractTopicLink(String name) {
 		if (name == null) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
 		if (!StringUtils.hasText(wikiLink.getNamespace())) {
 			return name;
 		}
 		String namespace = wikiLink.getNamespace();
 		if (!Utilities.isCommentsPage(name)) {
 			return name;
 		} else if (namespace.equals(WikiBase.NAMESPACE_COMMENTS)) {
 			return name.substring(namespace.length() + WikiBase.NAMESPACE_SEPARATOR.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_CATEGORY_COMMENTS)) {
 			return WikiBase.NAMESPACE_CATEGORY + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_IMAGE_COMMENTS)) {
 			return WikiBase.NAMESPACE_IMAGE + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_USER_COMMENTS)) {
 			return WikiBase.NAMESPACE_USER + name.substring(namespace.length());
 		} else if (namespace.equals(WikiBase.NAMESPACE_JAMWIKI_COMMENTS)) {
 			return WikiBase.NAMESPACE_JAMWIKI + name.substring(namespace.length());
 		} else {
			return null;
 		}
 	}
 
 	/**
 	 * Returns any trailing . , ; : characters from the given string.
 	 *
 	 * @param text The text from which trailing punctuation should be returned.
 	 * @return Any trailing punctuation from the given text, or an empty string
 	 *  otherwise.
 	 */
 	public static String extractTrailingPunctuation(String text) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = text.length() - 1; i >= 0; i--) {
 			char c = text.charAt(i);
 			if (c == '.' || c == ';' || c == ',' || c == ':' || c == ')' || c == '(' || c == ']' || c == '[') {
 				buffer.append(c);
 			} else {
 				break;
 			}
 		}
 		if (buffer.length() == 0) return "";
 		buffer = buffer.reverse();
 		return buffer.toString();
 	}
 
 	/**
 	 * Given a file name for a file that is located somewhere in the application
 	 * classpath, return a File object representing the file.
 	 *
 	 * @param filename The name of the file (relative to the classpath) that is
 	 *  to be retrieved.
 	 * @return A file object representing the requested filename
 	 * @throws Exception Thrown if the classloader can not be found or if
 	 *  the file can not be found in the classpath.
 	 */
 	public static File getClassLoaderFile(String filename) throws Exception {
 		// note that this method is used when initializing logging, so it must
 		// not attempt to log anything.
 		Method method = null;
 		ClassLoader loader = null;
 		URL url = null;
 		File file = null;
 		try {
 			// first try to use the standard class loader path
 			method = Thread.class.getMethod("getContextClassLoader", null);
 			loader = (ClassLoader)method.invoke(Thread.currentThread(), null);
 		} catch (Exception e) {
 			// that didn't work... try something else
 		}
 		if (loader == null) {
 			// attempt the the class loader that loaded this class
 			loader = Utilities.class.getClassLoader();
 		}
 		if (loader == null) {
 			throw new Exception("Unable to find class loader");
 		}
 		url = loader.getResource(filename);
 		if (url != null) {
 			file = FileUtils.toFile(url);
 		} else {
 			url = ClassLoader.getSystemResource(filename);
 			if (url == null) {
 				throw new Exception("Unable to find " + filename);
 			}
 			file = FileUtils.toFile(url);
 		}
 		if (file == null | !file.exists()) {
 			throw new Exception("Found invalid class loader root " + file);
 		}
 		return file;
 	}
 
 	/**
 	 * Attempt to get the class loader root directory.  This method works
 	 * by searching for a file that MUST exist in the class loader root
 	 * and then returning its parent directory.
 	 *
 	 * @return Returns a file indicating the directory of the class loader.
 	 * @throws Exception Thrown if the class loader can not be found.
 	 */
 	public static File getClassLoaderRoot() throws Exception {
 		File file = Utilities.getClassLoaderFile("ApplicationResources.properties");
 		if (!file.exists()) {
 			throw new Exception("Unable to find class loader root");
 		}
 		return file.getParentFile();
 	}
 
 	/**
 	 * Given a message key and locale return a locale-specific message.
 	 *
 	 * @param key The message key that corresponds to the formatted message
 	 *  being retrieved.
 	 * @param locale The locale for the message that is to be retrieved.
 	 * @return A formatted message string that is specific to the locale.
 	 */
 	public static String getMessage(String key, Locale locale) {
 		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
 		return messages.getString(key);
 	}
 
 	/**
 	 * Given a message key, locale, and formatting parameters, return a
 	 * locale-specific message.
 	 *
 	 * @param key The message key that corresponds to the formatted message
 	 *  being retrieved.
 	 * @param locale The locale for the message that is to be retrieved.
 	 * @param params An array of formatting parameters to use in the message
 	 *  being returned.
 	 * @return A formatted message string that is specific to the locale.
 	 */
 	public static String getMessage(String key, Locale locale, Object[] params) {
 		MessageFormat formatter = new MessageFormat("");
 		formatter.setLocale(locale);
 		String message = Utilities.getMessage(key, locale);
 		formatter.applyPattern(message);
 		return formatter.format(params);
 	}
 
 	/**
 	 * Finds the current WikiUser object in the request and determines
 	 * if that user is an admin.
 	 *
 	 * @param request The current servlet request object.
 	 * @return <code>true</code> if the current request contains a valid user
 	 *  object and if that user is an admin, <code>false</code> otherwise.
 	 */
 	public static boolean isAdmin(HttpServletRequest request) {
 		WikiUser user = currentUser(request);
 		return (user != null && user.getAdmin());
 	}
 
 	/**
 	 * Given a topic name, determine if that name corresponds to a comments
 	 * page.
 	 *
 	 * @param topicName The topic name (non-null) to examine to determine if it
 	 *  is a comments page or not.
 	 * @return <code>true</code> if the page is a comments page, <code>false</code>
 	 *  otherwise.
 	 */
 	public static boolean isCommentsPage(String topicName) {
 		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
 		if (!StringUtils.hasText(wikiLink.getNamespace())) {
 			return false;
 		}
 		String namespace = wikiLink.getNamespace();
 		if (namespace.equals(WikiBase.NAMESPACE_COMMENTS)) {
 			return true;
 		} else if (namespace.equals(WikiBase.NAMESPACE_CATEGORY_COMMENTS)) {
 			return true;
 		} else if (namespace.equals(WikiBase.NAMESPACE_IMAGE_COMMENTS)) {
 			return true;
 		} else if (namespace.equals(WikiBase.NAMESPACE_USER_COMMENTS)) {
 			return true;
 		} else if (namespace.equals(WikiBase.NAMESPACE_JAMWIKI_COMMENTS)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Determine if the system properties file exists and has been initialized.
 	 * This method is primarily used to determine whether or not to display
 	 * the system setup page or not.
 	 *
 	 * @return <code>true</code> if the properties file has NOT been initialized,
 	 *  <code>false</code> otherwise.
 	 */
 	public static boolean isFirstUse() {
 		return !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
 	}
 
 	/**
 	 * Determine if the system code has been upgraded from the configured system
 	 * version.  Thus if the system is upgraded, this method returns <code>true</code>
 	 *
 	 * @return <code>true</code> if the system has been upgraded, <code>false</code>
 	 *  otherwise.
 	 */
 	public static boolean isUpgrade() {
 		if (Utilities.isFirstUse()) return false;
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		WikiVersion currentVersion = new WikiVersion(WikiVersion.CURRENT_WIKI_VERSION);
 		return (oldVersion.before(currentVersion));
 	}
 
 	/**
 	 * Determine if the given string is an IP address.  This method uses pattern
 	 * matching to see if the given string could be a valid IP address.
 	 *
 	 * @param ipAddress A string that is to be examined to verify whether or not
 	 *  it could be a valid IP address.
 	 * @return <code>true</code> if the string is a value that is a valid IP address,
 	 *  <code>false</code> otherwise.
 	 */
 	public static boolean isIpAddress(String ipAddress) {
 		// note that a regular expression would be the easiest way to handle
 		// this, but regular expressions don't handle things like "number between
 		// 0 and 255" very well, so use a heavier approach
 		// if no text, obviously not valid
 		if (!StringUtils.hasText(ipAddress)) return false;
 		// must contain three periods
 		if (StringUtils.countOccurrencesOf(ipAddress, ".") != 3) return false;
 		// ip addresses must be between seven and 15 characters long
 		if (ipAddress.length() < 7 || ipAddress.length() > 15) return false;
 		// verify that the string is "0-255.0-255.0-255.0-255".
 		StringTokenizer tokens = new StringTokenizer(ipAddress, ".");
 		String token = null;
 		int number = -1;
 		while (tokens.hasMoreTokens()) {
 			token = tokens.nextToken();
 			try {
 				number = Integer.parseInt(token);
 				if (number < 0 || number > 255) return false;
 			} catch (Exception e) {
 				// not a number
 				return false;
 			}
 		}
 		// all tests passed, it's an IP address
 		return true;
 	}
 
 	/**
 	 * Using the system parser, parse system content.
 	 *
 	 * @param parserInput A ParserInput object that contains parser configuration
 	 *  information.
 	 * @param content The raw topic content that is to be parsed.
 	 * @param topicName The name of the topic being parsed.
 	 * @return Returns a ParserOutput object with parsed topic content and other
 	 *  parser output fields set.
 	 * @throws Exception Thrown if there are any parsing errors.
 	 */
 	public static ParserOutput parse(ParserInput parserInput, String content, String topicName) throws Exception {
 		if (content == null) {
 			return null;
 		}
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseHTML(content, topicName);
 	}
 
 	/**
 	 * Using the system parser, parse system content that should not be persisted
 	 * such as signatures.
 	 *
 	 * @param parserInput A ParserInput object that contains parser configuration
 	 *  information.
 	 * @param content The raw topic content that is to be parsed.
 	 * @return Returns a ParserOutput object with minimally parsed topic content
 	 *  and other parser output fields set.
 	 * @throws Exception Thrown if there are any parsing errors.
 	 */
 	public static ParserOutput parsePreSave(ParserInput parserInput, String content) throws Exception {
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parsePreSave(content);
 	}
 
 	/**
 	 * Utility method to retrieve an instance of the current system parser.
 	 *
 	 * @param parserInput A ParserInput object that contains parser configuration
 	 *  information.
 	 * @return An instance of the system parser.
 	 * @throws Exception Thrown if a parser instance can not be instantiated.
 	 */
 	private static AbstractParser parserInstance(ParserInput parserInput) throws Exception {
 		String parserClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
 		logger.fine("Using parser: " + parserClass);
 		Class clazz = Class.forName(parserClass, true, Thread.currentThread().getContextClassLoader());
 		Class[] parameterTypes = new Class[1];
 		parameterTypes[0] = Class.forName("org.jamwiki.parser.ParserInput", true, Thread.currentThread().getContextClassLoader());
 		Constructor constructor = clazz.getConstructor(parameterTypes);
 		Object[] initArgs = new Object[1];
 		initArgs[0] = parserInput;
 		return (AbstractParser)constructor.newInstance(initArgs);
 	}
 
 	/**
 	 * Given a topic name, return the parser-specific syntax to indicate a page
 	 * redirect.
 	 *
 	 * @param topicName The name of the topic that is being redirected to.
 	 * @return A string containing the syntax indicating a redirect.
 	 * @throws Exception Thrown if a parser instance cannot be instantiated or
 	 *  if any other parser error occurs.
 	 */
 	public static String parserRedirectContent(String topicName) throws Exception {
 		AbstractParser parser = parserInstance(null);
 		return parser.buildRedirectContent(topicName);
 	}
 
 	/**
 	 * Retrieve a default ParserOutput object for a given topic name.  Note that
 	 * the content has almost no parsing performed on it other than to generate
 	 * parser output metadata.
 	 *
 	 * @param content The raw topic content.
 	 * @return Returns a minimal ParserOutput object initialized primarily with
 	 *  parser metadata such as links.
 	 * @throws Exception Thrown if a parser error occurs.
 	 */
 	public static ParserOutput parserOutput(String content) throws Exception {
 		ParserInput parserInput = new ParserInput();
 		parserInput.setMode(ParserInput.MODE_SEARCH);
 		return Utilities.parsePreSave(parserInput, content);
 	}
 
 	/**
 	 * When editing a section of a topic, this method provides a way of slicing
 	 * out a given section of the raw topic content.
 	 *
 	 * @param request The servlet request object.
 	 * @param virtualWiki The virtual wiki for the topic being parsed.
 	 * @param topicName The name of the topic being parsed.
 	 * @param targetSection The section to be sliced and returned.
 	 * @return Returns a ParserOutput object containing the raw topic content
 	 *  for the target section.
 	 * @throws Exception Thrown if a parser error occurs.
 	 */
 	public static ParserOutput parseSlice(HttpServletRequest request, String virtualWiki, String topicName, int targetSection) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null || topic.getTopicContent() == null) {
 			return null;
 		}
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setTopicName(topicName);
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setMode(ParserInput.MODE_SLICE);
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseSlice(topic.getTopicContent(), topicName, targetSection);
 	}
 
 	/**
 	 * When editing a section of a topic, this method provides a way of splicing
 	 * an edited section back into the raw topic content.
 	 *
 	 * @param request The servlet request object.
 	 * @param virtualWiki The virtual wiki for the topic being parsed.
 	 * @param topicName The name of the topic being parsed.
 	 * @param targetSection The section to be sliced and returned.
 	 * @param replacementText The edited content that is to be spliced back into
 	 *  the raw topic.
 	 * @return Returns a ParserOutput object containing the raw topic content
 	 *  including the new replacement text.
 	 * @throws Exception Thrown if a parser error occurs.
 	 */
 	public static ParserOutput parseSplice(HttpServletRequest request, String virtualWiki, String topicName, int targetSection, String replacementText) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null || topic.getTopicContent() == null) {
 			return null;
 		}
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setTopicName(topicName);
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setMode(ParserInput.MODE_SPLICE);
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseSplice(topic.getTopicContent(), topicName, targetSection, replacementText);
 	}
 
 	/**
 	 * Utility method for parsing a multipart servlet request.  This method returns
 	 * an iterator of FileItem objects that corresponds to the request.
 	 *
 	 * @param request The servlet request containing the multipart request.
 	 * @return Returns an iterator of FileItem objects the corresponds to the request.
 	 * @throws Exception Thrown if any problems occur while processing the request.
 	 */
 	public static Iterator processMultipartRequest(HttpServletRequest request) throws Exception {
 		// Create a factory for disk-based file items
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setRepository(new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH)));
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		upload.setSizeMax(Environment.getLongValue(Environment.PROP_FILE_MAX_FILE_SIZE));
 		return upload.parseRequest(request).iterator();
 	}
 
 	/**
 	 * Utility method for reading a file from a classpath directory and returning
 	 * its contents as a String.
 	 *
 	 * @param filename The name of the file to be read, either as an absolute file
 	 *  path or relative to the classpath.
 	 * @return A string representation of the file contents.
 	 * @throws Exception Thrown if the file cannot be found or if an I/O exception
 	 *  occurs.
 	 */
 	public static String readFile(String filename) throws Exception {
 		File file = new File(filename);
 		if (file.exists()) {
 			// file passed in as full path
 			return FileUtils.readFileToString(file, "UTF-8");
 		}
 		// look for file in resource directories
 		Class[] parameterTypes = null;
 		Method method = Thread.class.getMethod("getContextClassLoader", parameterTypes);
 		Object[] args = null;
 		ClassLoader loader = (ClassLoader)method.invoke(Thread.currentThread(), args);
 		URL url = loader.getResource(filename);
 		file = FileUtils.toFile(url);
 		if (file == null || !file.exists()) {
 			throw new FileNotFoundException("File " + filename + " is not available for reading");
 		}
 		return FileUtils.readFileToString(file, "UTF-8");
 	}
 
 	/**
 	 * Utility method used to delete a cookie by setting its expiration time to the
 	 * current time.
 	 *
 	 * @param response The servlet response object.
 	 * @param cookieName The name of the cookie that is to be deleted.
 	 */
 	public static final void removeCookie(HttpServletResponse response, String cookieName) {
 		Cookie cookie = new Cookie(cookieName, null);
 		cookie.setMaxAge(0);
 		// FIXME - need path to be the server base
 		//cookie.setPath("/");
 		response.addCookie(cookie);
 	}
 
 	/**
 	 * Utility method used to retrieve the value of a specific cookie from the request.
 	 *
 	 * @param request The servlet request object.
 	 * @param cookieName The name of the cookie whose value is being retrieved from
 	 *  the request.
 	 * @return Returns the content of the cookie value, or <code>null</code> if the
 	 *  cookie cannot be found in the request.
 	 */
 	public static String retrieveCookieValue(HttpServletRequest request, String cookieName) {
 		Cookie[] cookies = request.getCookies();
 		if (cookies == null) return null;
 		Cookie cookie = null;
 		for (int i=0; i < cookies.length; i++) {
 			if (cookies[i].getName().equals(cookieName)) {
 				cookie = cookies[i];
 				break;
 			}
 		}
 		if (cookie == null) return null;
 		return cookie.getValue();
 	}
 
 	/**
 	 * Utility method for retrieving values from the URI.  This method
 	 * will attempt to properly convert the URI encoding, and then offers a way
 	 * to return directories after the initial context directory.  For example,
 	 * if the URI is "/context/first/second/third" and this method is called
 	 * with a skipCount of 1, the return value is "second/third".
 	 *
 	 * @param request The servlet request object.
 	 * @param skipCount The number of directories to skip.
 	 * @return A UTF-8 encoded portion of the URL that skips the web application
 	 *  context and skipCount directories, or <code>null</code> if the number of
 	 *  directories is less than skipCount.
 	 */
 	public static String retrieveDirectoriesFromURI(HttpServletRequest request, int skipCount) {
 		String uri = request.getRequestURI().trim();
 		// FIXME - needs testing on other platforms
 		uri = Utilities.convertEncoding(uri, "ISO-8859-1", "UTF-8");
 		String contextPath = request.getContextPath().trim();
 		if (!StringUtils.hasText(uri) || contextPath == null) {
 			return null;
 		}
 		uri = uri.substring(contextPath.length() + 1);
 		int i = 0;
 		while (i < skipCount) {
 			int slashIndex = uri.indexOf('/');
 			if (slashIndex == -1) {
 				return null;
 			}
 			uri = uri.substring(slashIndex + 1);
 			i++;
 		}
 		return uri;
 	}
 
 	/**
 	 * Verify that a directory exists and is writable.
 	 *
 	 * @param name The full name (including the path) for the directory being tested.
 	 * @return A WikiMessage object containing any error encountered, otherwise
 	 *  <code>null</code>.
 	 */
 	public static WikiMessage validateDirectory(String name) {
 		File directory = new File(name);
 		if (!directory.exists() || !directory.isDirectory()) {
 			return new WikiMessage("error.directoryinvalid", name);
 		}
 		String filename = "jamwiki-test-" + System.currentTimeMillis() + ".txt";
 		File file = new File(name, filename);
 		String text = "Testing";
 		String read = null;
 		try {
 			// attempt to write a temp file to the directory
 			FileUtils.writeStringToFile(file, text, "UTF-8");
 		} catch (Exception e) {
 			return new WikiMessage("error.directorywrite", name, e.getMessage());
 		}
 		try {
 			// verify that the file was correctly written
 			read = FileUtils.readFileToString(file, "UTF-8");
 			if (read == null || !text.equals(read)) throw new IOException();
 		} catch (Exception e) {
 			return new WikiMessage("error.directoryread", name, e.getMessage());
 		}
 		try {
 			// attempt to delete the file
 			FileUtils.forceDelete(file);
 		} catch (Exception e) {
 			return new WikiMessage("error.directorydelete", name, e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Validate that vital system properties, such as database connection settings,
 	 * have been specified properly.
 	 *
 	 * @param props The property object to validate against.
 	 * @return A Vector of WikiMessage objects containing any errors encountered,
 	 *  or an empty Vector if no errors are encountered.
 	 */
 	public static Vector validateSystemSettings(Properties props) {
 		Vector errors = new Vector();
 		// test directory permissions & existence
 		WikiMessage baseDirError = Utilities.validateDirectory(props.getProperty(Environment.PROP_BASE_FILE_DIR));
 		if (baseDirError != null) {
 			errors.add(baseDirError);
 		}
 		WikiMessage fullDirError = Utilities.validateDirectory(props.getProperty(Environment.PROP_FILE_DIR_FULL_PATH));
 		if (fullDirError != null) {
 			errors.add(fullDirError);
 		}
 		String classesDir = null;
 		try {
 			classesDir = Utilities.getClassLoaderRoot().getPath();
 			WikiMessage classesDirError = Utilities.validateDirectory(classesDir);
 			if (classesDirError != null) {
 				errors.add(classesDirError);
 			}
 		} catch (Exception e) {
 			errors.add(new WikiMessage("error.directorywrite", classesDir, e.getMessage()));
 		}
 		// test database
 		String databaseType = props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE);
 		if (databaseType != null && databaseType.equals("DATABASE")) {
 			String driver = props.getProperty(Environment.PROP_DB_DRIVER);
 			String url = props.getProperty(Environment.PROP_DB_URL);
 			String userName = props.getProperty(Environment.PROP_DB_USERNAME);
 			String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
 			if (!DatabaseConnection.testDatabase(driver, url, userName, password)) {
 				errors.add(new WikiMessage("error.databaseconnection"));
 			}
 		}
 		// verify valid parser class
 		boolean validParser = true;
 		String parserClass = props.getProperty(Environment.PROP_PARSER_CLASS);
 		String abstractParserClass = "org.jamwiki.parser.AbstractParser";
 		if (parserClass == null || parserClass.equals(abstractParserClass)) validParser = false;
 		try {
 			Class parent = Class.forName(parserClass, true, Thread.currentThread().getContextClassLoader());
 			Class child = Class.forName(abstractParserClass, true, Thread.currentThread().getContextClassLoader());
 			if (!child.isAssignableFrom(parent)) validParser = false;
 		} catch (Exception e) {
 			validParser = false;
 		}
 		if (!validParser) errors.add(new WikiMessage("error.parserclass", parserClass));
 		return errors;
 	}
 
 	/**
 	 * Utility method for determining if a topic name is valid for use on the Wiki,
 	 * meaning that it is not empty and does not contain any invalid characters.
 	 *
 	 * @param name The topic name to validate.
 	 * @return <code>true</code> if the name is a valid topic name, <code>false</code>
 	 *  otherwise.
 	 */
 	public static boolean validateTopicName(String name) {
 		if (!StringUtils.hasText(name)) return false;
 		WikiLink wikiLink = LinkUtil.parseWikiLink(name);
 		String namespace = wikiLink.getNamespace();
 		if (namespace != null && namespace.toLowerCase().trim().equals(WikiBase.NAMESPACE_SPECIAL.toLowerCase())) return false;
 		Matcher m = INVALID_TOPIC_NAME_PATTERN.matcher(name);
 		if (m.find()) return false;
 		return true;
 	}
 
 	/**
 	 * Utility method for determining if a user login is valid for use on the Wiki,
 	 * meaning that it is not empty and does not contain any invalid characters.
 	 *
 	 * @param name The user login to validate.
 	 * @return <code>true</code> if the name is a valid user login, <code>false</code>
 	 *  otherwise.
 	 */
 	public static boolean validateUserName(String name) {
 		if (!Utilities.validateTopicName(name)) return false;
 		Matcher m = VALID_USER_LOGIN_PATTERN.matcher(name);
 		if (!m.matches()) return false;
 		return true;
 	}
 }
