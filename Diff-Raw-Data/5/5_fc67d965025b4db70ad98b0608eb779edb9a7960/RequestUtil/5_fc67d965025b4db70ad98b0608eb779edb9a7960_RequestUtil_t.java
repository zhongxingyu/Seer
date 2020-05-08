 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.servlet;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 
 import no.feide.moria.log.MessageLogger;
 
 /**
  * This class is a toolkit for the servlets and it's main functionality is to
  * retrieve resource bundles.
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public final class RequestUtil {
 
     /**
      * Prefix for properties in config.
      */   
     private static final String PATH_PREFIX = "no.feide.moria.web.";
     
     /**
      * Property name for: Config.
      */
     public static final String PROP_CONFIG = PATH_PREFIX + "config";
     /**
      * Configuration property giving the name of the URL parameter containing
      * the Moria ticket ID. <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "login.ticket_param"</code>.
      */
     public static final String PROP_LOGIN_TICKET_PARAM = PATH_PREFIX + "login.ticket_param";
 
     /**
      * Property name for: Organization.
      */
     public static final String PROP_ORG = PATH_PREFIX + "org";
 
     /**
      * Configuration property giving the default Moria language, to be used if
      * no other preferences (user or service) are available.
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "login.default_language"</code>.
      */
     
     public static final String PROP_LOGIN_DEFAULT_LANGUAGE = PATH_PREFIX + "login.default_language";
 
     /**
      * Property name for: Language.
      */
     public static final String PROP_LANGUAGE = PATH_PREFIX + "lang";
 
     /**
      * Configuration property giving the URL to the login servlet.
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "login.url_prefix"</code>.
      */
     public static final String PROP_LOGIN_URL_PREFIX = PATH_PREFIX + "login.url_prefix";
 
     /**
      * Configuration property giving the URL to the information servlet.
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "information.url_prefix"</code>.
      */    
     public static final String PROP_INFORMATION_URL_PREFIX = PATH_PREFIX + "information.url_prefix";
 
     /**
      * Configuration property sub-string giving the abbreviations and names for
      * all available languages, on the form
      * <code>PROP_LANGUAGE + "_" + PROP_COMMON</code>.<br>
      * <br>
      * The actual values are a comma-separated list of elements on the form
      * <code>EN:English</code>, that is, a two-letter language abbreviation
      * and a ':' character followed by its display name. <br>
      * <br>
      * <em>This property is required</em><br>
      * <br>
      * Current value is <code>"common"</code>.
      */
     public static final String PROP_COMMON = "common";
 
     /**
      * Configuration property giving the name of the cookie used to remember the
      * user's previously selected organization.
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.org.name"</code>.
      */
     public static final String PROP_COOKIE_ORG = PATH_PREFIX + "cookie.org.name";
 
     /**
      * Configuration property giving the lifetime, in hours, for the cookie used
      * to remember the user's previously selected organization (named by
      * <code>PROP_COOKIE_ORG</code>).<em>This property is required.</em>
      * <br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.org.ttl"</code>.
      * @see PROP_COOKIE_ORG
      */
     public static final String PROP_COOKIE_ORG_TTL = PATH_PREFIX + "cookie.org.ttl";
 
     /**
      * Configuration property giving the name of the cookie used to set user's
      * desired language. <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.lang.name"</code>.
      */
     public static final String PROP_COOKIE_LANG = PATH_PREFIX + "cookie.lang.name";
 
     /**
      * Configuration property giving the lifetime, in hours, for the cookie used
      * to set user's desired language. <em>This property is required.</em>
      * <br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.lang.ttl"</code>.
      */
     public static final String PROP_COOKIE_LANG_TTL = PATH_PREFIX + "cookie.lang.ttl";
 
     /**
      * Configuration property giving the name of the cookie used to carry SSO
      * sessions (that is, a Moria ticket ID belonging to a SSO session).
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.sso.name"</code>.
      */
     public static final String PROP_COOKIE_SSO = PATH_PREFIX + "cookie.sso.name";
 
     /**
      * Configuration property giving the lifetime, in hours, for the cookie used
      * to carry SSO sessions (named by <code>PROP_COOKIE_SSO</code>).
      * <em>This property is required.</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.sso.ttl"</code>.
      */
     public static final String PROP_COOKIE_SSO_TTL = PATH_PREFIX + "cookie.sso.ttl";
 
     /**
      * Configuration property giving the name of the cookie used to deny SSO
      * (when using Moria on public computers, for example).
      * <em>This property is required.</em>.<br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.denysso.name"</code>.
      */
     public static final String PROP_COOKIE_DENYSSO = PATH_PREFIX + "cookie.denysso.name";
 
     /**
      * Configuration property giving the lifetime, in hours, for the cookie used
      * to deny SSO (named by <code>PROP_COOKIE_DENYSSO</code>).
      * <em>This property is required</em><br>
      * <br>
      * Current value is <code>PATH_PREFIX + "cookie.denysso.ttl"</code>.
      * @see PROP_COOKIE_DENYSSO
      */
     public static final String PROP_COOKIE_DENYSSO_TTL = PATH_PREFIX + "cookie.denysso.ttl";
 
     /**
      * Property name for: Logout URL.
      */
     public static final String PROP_LOGOUT_URL_PARAM = PATH_PREFIX + "logout.url_param";
 
     /**
      * The name of the resource bundle for the login page. <br>
      * <br>
      * Current value is <code>"login"</code>.
      */
     public static final String BUNDLE_LOGIN = "login";
 
     /**
      * Configuration property for the InformationServlet feideattribs xml file path
      */
     public static final String PROP_INFORMATION_FEIDEATTRIBS_XML = PATH_PREFIX + "information.feideattribs_xml";
      
     /**
      * Bundle for the information servlet.
      */
     public static final String BUNDLE_INFORMATIONSERVLET = "feideattribs";
     
     /**
      * Bundle for the information page about the information servlet.
      */
     public static final String BUNDLE_INFOABOUT = "infoabout";
     
     /**
      * Bundle for the faq page
      */
     public static final String BUNDLE_FAQ = "faq";
     
     /**
      * Bundle for the error page
      */
     public static final String BUNDLE_ERROR = "error";
 
     /**
      * Legal name for an organization, used by Information Servlet
      */
     public static final String EDU_ORG_LEGAL_NAME = "eduOrgLegalName";
     
     /**
      * Link to faq, shown on the login page
      */
     public static final String FAQ_LINK = PATH_PREFIX + "faqlink";
 
     /**
      * Name of property from authorization module giving the default language
      * for a given service. <em>This property is required.</em><br>
      * <br>
      * Current value is <code>"language"</code>.
      */
     // TODO: This constant should also be used in AuthenticationManager, instead
     // of hard-coded "language" string.
     public static final String CONFIG_LANG = "language";
 
     /**
      * From Authorization config: Home organization of service.
      */
     public static final String CONFIG_HOME = "home";
 
     /**
      * From Authorization config: Service name.
      */
     public static final String CONFIG_DISPLAY_NAME = "displayName";
 
     /**
      * From Authorization config: Service URL.
      */
     public static final String CONFIG_URL = "url";
 
     /**
      * Parameter in request object: Username.
      */
     public static final String PARAM_USERNAME = "username";
 
     /**
      * Parameter in request object: Password.
      */
     public static final String PARAM_PASSWORD = "password";
 
     /**
      * Organization URL parameter, used when overriding default organization in
      * the URL redirecting a user from a service to Moria. Useful for services
      * that wish to dynamically force use of a certain organization irrespective
      * of the user's previous selections or the service defaults. <br>
      * <br>
      * Current value is <code>"org"</code>.
      */
     public static final String PARAM_ORG = "org";
 
     /**
      * Language URL parameter, used when overriding current language settings.
      * <br>
      * <br>
      * Current value is <code>"language"</code>.
      */
     public static final String PARAM_LANG = "language";
 
     /**
      * Parameter in request object: Deny SSO.
      */
     public static final String PARAM_DENYSSO = "denySSO";
 
     /**
      * Base URL attribute in request object. Used to fill in the URL to the
      * authentication web page. <br>
      * <br>
      * Current value is <code>"baseURL"</code>.
      */
     public static final String ATTR_BASE_URL = "baseURL";
 
     /**
      * Attribute in request object: Security level.
      */
     // TODO: What does this actually mean?
     public static final String ATTR_SEC_LEVEL = "secLevel";
 
     /**
      * Attribute in request object: Error type.
      */
     public static final String ATTR_ERROR_TYPE = "errorType";
 
     /**
      * Attribute in request object: Available languages.
      */
     public static final String ATTR_LANGUAGES = "languages";
 
     /**
      * Attribute in request object: Available organizations.
      */
     public static final String ATTR_ORGANIZATIONS = "organizations";
 
     /**
      * Attribute in request object: Preselected organization.
      */
     public static final String ATTR_SELECTED_ORG = "selectedOrg";
 
     /**
      * Attribute in request object: Denial of SSO.
      */
     public static final String ATTR_SELECTED_DENYSSO = "selectedDenySSO";
 
     /**
      * Attribute in request object: Preselected lanugage.
      */
     public static final String ATTR_SELECTED_LANG = "selectedLang";
 
     /**
      * Attribute in request object: Name of client/service.
      */
     public static final String ATTR_CLIENT_NAME = "clientName";
 
     /**
      * Attribute in request object: Link to associate with service name.
      */
     public static final String ATTR_CLIENT_URL = "clientURL";
 
     /**
      * Attribute in request object: Language bundle.
      */
     public static final String ATTR_BUNDLE = "bundle";
 
     /**
      * Error type: No organization selected.
      */
     public static final String ERROR_NO_ORG = "noOrg";
 
     /**
      * Error type: Invalid organization selected.
      */
     public static final String ERROR_INVALID_ORG = "invalidOrg";
 
     /**
      * Error type: Authentication failed.
      */
     public static final String ERROR_AUTHENTICATION_FAILED = "authnFailed";
 
     /**
     * Error type: Authorization failed.
     */
    public static final String ERROR_AUTHORIZATION_FAILED = "authorizationFailed";

    /**
      * Error type: Unknown ticket.
      */
     public static final String ERROR_UNKNOWN_TICKET = "unknownTicket";
 
     /**
      * Error type: The directory is down.
      */
     public static final String ERROR_DIRECTORY_DOWN = "directoryDown";
 
     /**
      * Error type: Moria is unavailable.
      */
     public static final String ERROR_MORIA_DOWN = "moriaDown";
 
     /**
      * Error type: User must supply username and password.
      */
     public static final String ERROR_NO_CREDENTIALS = "noCredentials";
 
     /** Used for logging. */
     private static final MessageLogger log = new MessageLogger(LoginServlet.class);
 
 
     /**
      * Default private constructor.
      */
     private RequestUtil() {
 
     }
 
 
     /**
      * Generate a resource bundle. The language of the resource bundle is
      * selected from the following priority list:
      * <ol>
      * <li>URL parameter (<code>requestParamLang</code>)
      * <li>User's cookie (<code>langFromCookie</code>)
      * <li>Default service language (<code>serviceLang</code>)
      * <li>User's browser settings (<code>browserLang</code>)
      * <li>Moria default setting (<code>moriaLang</code>)
      * </ol>
      * @param bundleName
      *            Name of the resource bundle to retrieve. Cannot be
      *            <code>null</code>.
      * @param requestParamLang
      *            Language specified from URL parameter. Can be
      *            <code>null</code>.
      * @param langFromCookie
      *            Language specified from user's cookie. Can be
      *            <code>null</code>.
      * @param serviceLang
      *            Default language as specified by configuration for the
      *            service. Can be <code>null</code>.
      * @param browserLang
      *            Language as requested by the users browser. Can be
      *            <code>null</code>.
      * @param moriaLang
      *            Default language for Moria. Cannot be <code>null</code>.
      * @return The requested resource bundle.
      * @throws IllegalArgumentException
      *             If <code>bundleName</code> or <code>moriaLang</code> is
      *             <code>null</code> or an empty string.
      * @throws MissingResourceException
      *             If the resource bundle cannot be found.
      */
     public static ResourceBundle getBundle(final String bundleName, final String requestParamLang, final String langFromCookie, final String serviceLang, final String browserLang, final String moriaLang) {
 
         // Sanity checks.
         if (bundleName == null || bundleName.equals(""))
             throw new IllegalArgumentException("Resource bundle name must be a non-empty string.");
         if (moriaLang == null || moriaLang.equals(""))
             throw new IllegalArgumentException("Default language must be a non-empty string.");
 
         // Build array of preferred language selections.
         final Vector langSelections = new Vector();
 
         // Check URL parameter.
         if (requestParamLang != null && !requestParamLang.equals(""))
             langSelections.add(requestParamLang);
 
         // Check user cookie.
         if (langFromCookie != null)
             langSelections.add(langFromCookie);
 
         // Check service default.
         if (serviceLang != null && !serviceLang.equals(""))
             langSelections.add(serviceLang);
 
         // Check user's browser settings.
         if (browserLang != null && !browserLang.equals("")) {
             final String[] browserLangs = sortedAcceptLang(browserLang);
             for (int i = 0; i < browserLangs.length; i++) {
                 langSelections.add(browserLangs[i]);
             }
         }
 
         // Finally, add Moria default language.
         langSelections.add(moriaLang);
 
         // Locate and return resulting resource bundle.
         ResourceBundle bundle;
         for (Enumeration e = langSelections.elements(); e.hasMoreElements();) {
             bundle = locateBundle(bundleName, (String) e.nextElement());
             if (bundle != null)
                 return bundle;
         }
 
         // No bundle found?
         throw new MissingResourceException("Resource bundle not found", "ResourceBundle", bundleName);
     }
 
 
     /**
      * Locates a bundle on a given language.
      * @param bundleName
      *            name of the bundle, cannot be null or ""
      * @param lang
      *            the bundles langauge
      * @return the resourceBundle for the selected language, null if it's not
      *         found
      */
     private static ResourceBundle locateBundle(final String bundleName, final String lang) {
 
         /* Validate parameters. */
         if (bundleName == null || bundleName.equals("")) { throw new IllegalArgumentException("bundleName must be a non-empty string."); }
         if (lang == null || lang.equals("")) { throw new IllegalArgumentException("lang must be a non-empty string."); }
 
         /* Find fallback resource bundle. */
         ResourceBundle fallback;
         try {
             fallback = ResourceBundle.getBundle(bundleName, new Locale("bogus"));
         } catch (MissingResourceException e) {
             fallback = null;
         }
 
         final Locale locale = new Locale(lang);
         ResourceBundle bundle = null;
         try {
             bundle = ResourceBundle.getBundle(bundleName, locale);
         } catch (MissingResourceException e) {
             /* No bundle was found, ignore and move on. */
         }
 
         if (bundle != fallback) { return bundle; }
 
         /* Check if the fallback is actually requested. */
         if (bundle != null && bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage())) { return bundle; }
 
         /* No bundle found. */
         return null;
     }
 
 
     /**
      * Return a requested cookie value from the HTTP request.
      * @param cookieName
      *            Name of the cookie
      * @param cookies
      *            The cookies from the HTTP request
      * @return Requested value, empty string if not found
      * @throws IllegalArgumentException
      *             If <code>cookieName</code> is null or an empty string.
      */
     public static String getCookieValue(final String cookieName, final Cookie[] cookies)
     throws IllegalArgumentException {
 
         // Sanity checks.
         if (cookieName == null || cookieName.equals(""))
             throw new IllegalArgumentException("Cookie name must be a non-empty string");
         if (cookies == null)
             return null;
 
         // Return cookie value, if set.
         String value = null;
         for (int i = 0; i < cookies.length; i++) {
             if (cookies[i].getName().equals(cookieName))
                 value = cookies[i].getValue();
         }
         return value;
 
     }
 
 
     /**
      * Utility method to create a cookie.
      * @param cookieName
      *            Name of the cookie. Cannot be <code>null</code>.
      * @param cookieValue
      *            Value to set in cookie. Cannot be <code>null</code>.
      * @param validHours
      *            Number of hours before the cookie expires. Must be 0 or
      *            greater.
      * @return A Cookie with the specified name and value, with the given expiry
      *         time.
      * @throws IllegalArgumentException
      *             If <code>cookieName</code> or <code>cookieValue</code> is
      *             <code>null</code> or an empty string, or if
      *             <code>validHours</code> is less than 0.
      */
     public static Cookie createCookie(final String cookieName, final String cookieValue, final int validHours)
     throws IllegalArgumentException {
 
         // Sanity checks.
         if (cookieName == null || cookieName.equals(""))
             throw new IllegalArgumentException("Cookie name must be a non-empty string");
         if (cookieValue == null || cookieValue.equals(""))
             throw new IllegalArgumentException("Cookie value must be a non-empty string");
         if (validHours < 0)
             throw new IllegalArgumentException("Valid hours for cookie must be a positive number");
 
         // Create and return cookie.
         final Cookie cookie = new Cookie(cookieName, cookieValue);
         cookie.setMaxAge(validHours * 60 * 60); // Hours to seconds
         cookie.setVersion(0);
         return cookie;
     }
 
 
     /**
      * Parser for the Accept-Language header sent from browsers. The language
      * entries in the string can be weighted and the parser generates a list of
      * the languages sorted by the weight value.
      * @param acceptLang
      *            the accept language header, cannot be null or ""
      * @return a string array of language names, sorted by the browsers weight
      *         preferences
      */
     static String[] sortedAcceptLang(final String acceptLang) {
 
         if (acceptLang == null || acceptLang.equals("")) { throw new IllegalArgumentException("acceptLang must be a non-empty string."); }
 
         final StringTokenizer tokenizer = new StringTokenizer(acceptLang, ",");
         final HashMap weightedLangs = new HashMap();
 
         while (tokenizer.hasMoreTokens()) {
             final String token = tokenizer.nextToken();
             String lang = token;
             boolean ignore = false;
             String weight = "1.0";
             int index;
 
             /* Language and weighting are devided by ";". */
             if ((index = token.indexOf(";")) != -1) {
                 String parsedWeight;
                 lang = token.substring(0, index);
 
                 /* Weight data. */
                 parsedWeight = token.substring(index + 1, token.length());
                 parsedWeight = parsedWeight.trim();
                 if (parsedWeight.startsWith("q=")) {
                     parsedWeight = parsedWeight.substring(2, parsedWeight.length());
                     weight = parsedWeight;
                 } else {
                     /* Format error, flag to ignore token. */
                     ignore = true;
                 }
             }
 
             if (!ignore) {
                 lang = lang.trim();
 
                 /* Country and language is devided by "-" (optional). */
                 if ((index = lang.indexOf("-")) != -1) {
                     lang = lang.substring(index + 1, lang.length());
                 }
 
                 weightedLangs.put(weight, lang);
             }
         }
 
         final Vector sortedLangs = new Vector();
         final String[] sortedKeys = (String[]) weightedLangs.keySet().toArray(new String[weightedLangs.size()]);
         Arrays.sort(sortedKeys, Collections.reverseOrder());
 
         for (int i = 0; i < sortedKeys.length; i++) {
             sortedLangs.add(weightedLangs.get(sortedKeys[i]));
         }
 
         return (String[]) sortedLangs.toArray(new String[sortedLangs.size()]);
     }
 
 
     /**
      * Read institution names from the servlet configuration and generate a
      * TreeMap with the result, using the correct language. <br>
      * <br>
      * The configuration <em>must</em> contain properties on the form
      * <code>element + "_" + language</code>.
      * @param config
      *            The web module configuration.
      * @param element
      *            The sub-element of the web module configuration to process.
      * @param language
      *            The language used when generating institution names.
      * @return A <code>TreeMap</code> of institution names with full name as
      *         key and ID as value.
      * @throws IllegalArgumentException
      *             If <code>config</code> is <code>null</code>, or if
      *             <code>element</code> or <code>language</code> is
      *             <code>null</code> or an empty string.
      * @throws IllegalStateException
      *             If no elements of type <code>element</code> is found in the
      *             configuration <code>config</code>. Also thrown if the
      *             values found in <code>config</code> contains less than or
      *             more than one occurrence of the ':' separator character.
      */
     static TreeMap parseConfig(final Properties config, final String element, final String language)
     throws IllegalArgumentException, IllegalStateException {
 
         // Sanity checks.
         if (config == null)
             throw new IllegalArgumentException("Configuration cannot be null");
         if (element == null || element.equals(""))
             throw new IllegalArgumentException("Configuration element must be a non-empty string");
         if (language == null || language.equals(""))
             throw new IllegalArgumentException("Institution name language must be a non-empty string");
 
         // Get the property value from configuration, with sanity check.
         final String value = config.getProperty(element + "_" + language);
         if (value == null)
             throw new IllegalStateException("No elements of type '" + element + "' in configuration.");
 
         // Process each ","-separated language declaration.
         final StringTokenizer tokenizer = new StringTokenizer(value, ",");
         final TreeMap names = new TreeMap();
         while (tokenizer.hasMoreTokens()) {
             final String token = tokenizer.nextToken();
 
             // Do we have the correct separator in the language declaration?
             final int index = token.indexOf(":");
             if (index == -1)
                 throw new IllegalStateException("Missing ':' separator in language declaration: '" + token + "'");
 
             // Separate language declaration into two-letter abbreviation and
             // display name.
             final String shortName = token.substring(0, index);
             final String longName = token.substring(index + 1, token.length());
 
             // Do we have more than one separator in this language declaration?
             if (shortName.indexOf(":") != -1 || longName.indexOf(":") != -1)
                 throw new IllegalStateException("Config has wrong format.");
 
             // Add this declaration to the list.
             names.put(longName, shortName);
         }
 
         // Return the list of names.
         return names;
     }
 
 
     /**
      * Replaces a given token with hyperlinks. The URL and name of the hyperlink
      * is given as parameters. Every occurance of the token in the data string
      * is replaced by a hyperlink.
      * @param token
      *            the token to replace with link
      * @param data
      *            the data containing text and token(s)
      * @param name
      *            the link text
      * @param url
      *            the URL to link to
      * @return a string with hyperlinks in stead of tokens
      */
     public static String insertLink(final String token, final String data, final String name, final String url) {
 
         /* Validate parameters */
         if (token == null || token.equals("")) { throw new IllegalArgumentException("token must be a non-empty string"); }
         if (data == null || data.equals("")) { throw new IllegalArgumentException("data must be a non-empty string"); }
         if (name == null || name.equals("")) { throw new IllegalArgumentException("name must be a non-empty string"); }
         if (url == null || url.equals("")) { throw new IllegalArgumentException("url must be a non-empty string"); }
 
         final String link = "<a href=\"" + url + "\">" + name + "</a>";
 
         return data.replaceAll(token, link);
     }
 
 
     /**
      * Get the config from the context. The configuration is expected to be set
      * by the controller before requests are sent to this servlet.
      * @param context
      *            ServletContext containing the configuration.
      * @return the configuration
      */
     static Properties getConfig(final ServletContext context) {
 
         /* Validate parameters */
         if (context == null) { throw new IllegalArgumentException("context must be a non-empty string"); }
 
         final Properties config;
 
         /* Validate config */
         try {
             config = (Properties) context.getAttribute("no.feide.moria.web.config");
         } catch (ClassCastException e) {
             throw new IllegalStateException("Config is not correctly set in context. Not a java.util.Properties object.");
         }
 
         if (config == null) { throw new IllegalStateException("Config is not set in context."); }
 
         return config;
     }
 }
