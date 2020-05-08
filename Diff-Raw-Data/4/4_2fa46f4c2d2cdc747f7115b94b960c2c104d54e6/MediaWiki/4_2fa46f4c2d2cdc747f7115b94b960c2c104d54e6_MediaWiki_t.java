 package org.mediawiki;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InvalidObjectException;
 import java.io.ObjectInputStream;
 import java.io.ObjectInputValidation;
 import java.io.OutputStreamWriter;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.SoftReference;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /*-
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class MediaWiki implements Serializable, ObjectInputValidation {
 	// TODO Add processContinuation to MultipleRevisionIterator
 	// TODO Add parse-pagetext
 	// TODO Add block/unblock
 	// TODO Add undelete
 	// TODO Add watch-add/watch-del/watch-list/watch-newedits
 	// TODO Add Special:Recentchanges, Special:Contributions
 	// TODO Add patrol
 	// TODO Add setSecure
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The version of the <tt>MediaWiki</tt> class, in terms of features. The
 	 * Java Virtual Machine Specification states that a plain <tt>String</tt>
 	 * literal that is in a field declared <code>public</code>,
 	 * <code>static</code> and <code>final</code> can be embedded directly in
 	 * other class files that refer to the value, therefore the literal is
 	 * wrapped in a <code>new String</code> to avoid this.
 	 */
 	public static final String VERSION = new String("0.02");
 
 	// - - - VARIABLES - - -
 
 	private final String host;
 
 	private final String scriptPath;
 
 	/**
 	 * Parses information returned by the MediaWiki API in XML format.
 	 */
 	private transient DocumentBuilder documentBuilder;
 
 	/**
 	 * Contains cookies set by the wiki. An implementation of <tt>Map</tt>
 	 * placed here must also be <tt>Serializable</tt>.
 	 */
 	private final Map<String, String> cookies = new TreeMap<String, String>();
 
 	/**
 	 * Whether this <tt>MediaWiki</tt> attempts to retrieve compressed content
 	 * from the wiki it represents.
 	 */
 	private boolean useGzip;
 
 	/**
 	 * The maximum database replication lag to allow for during expensive
 	 * informational queries and editing on the wiki represented by this
 	 * <tt>MediaWiki</tt>, in seconds.
 	 * <p>
 	 * If this field is <code>null</code>, the feature is disabled.
 	 */
 	private Integer maxLag;
 
 	/**
 	 * Lock used to ensure that only one thread can write to the preference
 	 * variables of this <tt>MediaWiki</tt>. To prevent deadlock in this class,
 	 * if an operation needs to lock both <code>preferenceLock</code> and
 	 * <code>networkLock</code>, <code>preferenceLock</code> must be acquired
 	 * last.
 	 */
 	private transient ReadWriteLock preferenceLock;
 
 	/**
 	 * Lock used to ensure that only one thread can access the network to
 	 * connect to the wiki represented by this <tt>MediaWiki</tt>. To prevent
 	 * deadlock in this class, if an operation needs to lock both
 	 * <code>preferenceLock</code> and <code>networkLock</code>,
 	 * <code>networkLock</code> must be acquired first.
 	 */
 	private transient Lock networkLock;
 
 	// - - - CONSTRUCTORS, INITIALIZATION AND SERIALIZATION CODE - - -
 
 	protected void init() {
 		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilderFactory.setCoalescing(true);
 		documentBuilderFactory.setIgnoringComments(true);
 		try {
 			documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		} catch (final ParserConfigurationException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 		preferenceLock = new ReentrantReadWriteLock();
 		networkLock = new ReentrantLock();
 	}
 
 	/**
 	 * Creates an instance of <tt>MediaWiki</tt> that performs actions on the
 	 * MediaWiki wiki at the given <code>host</code>, whose script path is the
 	 * root of the wiki (<tt>http://<em>host</em>/api.php</tt>).
 	 * 
 	 * @param host
 	 *            The hostname, IPv4 dotted decimal or IPv6 address of the wiki.
 	 * @throws NullPointerException
 	 *             if the <code>host</code> is <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             if the <code>host</code> is an empty string
 	 */
 	public MediaWiki(final String host) throws NullPointerException, IllegalArgumentException {
 		this(host, "");
 	}
 
 	/**
 	 * Creates an instance of <tt>MediaWiki</tt> that performs actions on the
 	 * MediaWiki wiki at the given <code>host</code>, whose script path is the
 	 * given <code>scriptPath</code> (
 	 * <tt>http://<em>host</em><em>scriptPath</em>/api.php</tt>).
 	 * 
 	 * @param host
 	 *            The hostname, IPv4 dotted decimal or IPv6 address of the wiki.
 	 * @param scriptPath
 	 *            The script path on the wiki which contains <tt>api.php</tt>.
 	 *            This may be empty, in which case the root of the wiki is used,
 	 *            or contain any number of leading and/or trailing slashes,
 	 *            which are removed.
 	 * @throws NullPointerException
 	 *             if the <code>host</code> or <code>scriptPath</code> is
 	 *             <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             if the <code>host</code> is an empty string
 	 */
 	public MediaWiki(final String host, final String scriptPath) throws NullPointerException, IllegalArgumentException {
 		if (host.length() == 0)
 			throw new IllegalArgumentException("host is empty");
 		if (scriptPath == null)
 			throw new NullPointerException();
 		this.host = host;
 
 		int beginIndex = 0, endIndex = scriptPath.length();
 		while ((beginIndex < endIndex) && (scriptPath.charAt(beginIndex) == '/')) {
 			beginIndex++;
 		}
 		while ((endIndex > beginIndex) && (scriptPath.charAt(endIndex - 1) == '/')) {
 			endIndex--;
 		}
 		this.scriptPath = new String(scriptPath.substring(beginIndex, endIndex));
 
 		init();
 	}
 
 	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.registerValidation(this, 0);
 		in.defaultReadObject();
 
 		init();
 	}
 
 	/**
 	 * This method validates deserialized objects and should generally not be
 	 * called by applications.
 	 */
 	public void validateObject() throws InvalidObjectException {
 		if (host == null)
 			throw new InvalidObjectException("host == null");
 		if (scriptPath == null)
 			throw new InvalidObjectException("scriptPath == null");
 		if (host.length() == 0)
 			throw new InvalidObjectException("host is empty");
 		if (scriptPath.length() > 0 && ((scriptPath.charAt(0) == '/') || (scriptPath.charAt(scriptPath.length() - 1) == '/')))
 			throw new InvalidObjectException("scriptPath starts or ends with /");
 	}
 
 	// - - - CONNECTION PREFERENCES - - -
 
 	/**
 	 * Returns whether this <tt>MediaWiki</tt> attempts to retrieve compressed
 	 * content from the wiki it represents.
 	 * 
 	 * @return whether this <tt>MediaWiki</tt> attempts to retrieve compressed
 	 *         content from the wiki it represents
 	 */
 	public boolean isUsingCompression() {
 		preferenceLock.readLock().lock();
 		try {
 			return useGzip;
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Sets whether this <tt>MediaWiki</tt> is to attempt to retrieve compressed
 	 * content from the wiki it represents.
 	 * 
 	 * @param newValue
 	 *            <code>true</code> if this <tt>MediaWiki</tt> is to attempt to
 	 *            retrieve compressed content from the wiki it represents;
 	 *            <code>false</code> if compression should be avoided. The value
 	 *            of <code>false</code> is useful if using <tt>MediaWiki</tt> to
 	 *            edit a wiki on a local network, where using compression would
 	 *            overload the wiki server's resources more than sending
 	 *            uncompressed content.
 	 * @return this <tt>MediaWiki</tt>
 	 */
 	public MediaWiki setUsingCompression(final boolean newValue) {
 		preferenceLock.writeLock().lock();
 		try {
 			useGzip = newValue;
 		} finally {
 			preferenceLock.writeLock().unlock();
 		}
 		return this;
 	}
 
 	/**
 	 * Returns the maximum database replication lag allowed for all requests, in
 	 * seconds. The return value is <code>null</code> if this feature is not
 	 * enabled.
 	 * <p>
 	 * If this feature is enabled, all actions may throw
 	 * <tt>MediaWiki.ActionDelayException</tt>.
 	 * 
 	 * @return the maximum database replication lag allowed for all requests, in
 	 *         seconds
 	 */
 	public Integer getMaxLag() {
 		preferenceLock.readLock().lock();
 		try {
 			return maxLag;
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Sets the maximum database replication lag allowed for all requests, in
 	 * seconds.
 	 * 
 	 * @param newValue
 	 *            The new maximum database replication lag allowed for all
 	 *            requests, in seconds. This parameter is <code>null</code> to
 	 *            disable the feature.
 	 *            <p>
 	 *            If this feature is enabled, all actions may throw
 	 *            <tt>MediaWiki.ActionDelayException</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 */
 	public MediaWiki setMaxLag(Integer newValue) {
 		preferenceLock.writeLock().lock();
 		try {
 			maxLag = newValue;
 		} finally {
 			preferenceLock.writeLock().unlock();
 		}
 		return this;
 	}
 
 	// - - - INFORMATION ON MEDIAWIKI OBJECT - - -
 
 	/**
 	 * Returns the name of the host to which this <tt>MediaWiki</tt> is
 	 * connected.
 	 * 
 	 * @return the name of the host to which this <tt>MediaWiki</tt> is
 	 *         connected
 	 */
 	public String getHostName() {
 		return host;
 	}
 
 	/**
 	 * Returns the script path on the wiki to which this <tt>MediaWiki</tt> is
 	 * connected. The return value does not have leading or trailing slashes.
 	 * For example, to access <a
 	 * href="http://en.wikipedia.org/w/api.php">en.wikipedia.org/w/api.php</a>,
 	 * the script path is <code>"w"</code>.
 	 * 
 	 * @return the script path on the wiki to which this <tt>MediaWiki</tt> is
 	 *         connected
 	 */
 	public String getScriptPath() {
 		return scriptPath;
 	}
 
 	// - - - USER LOGIN AND LOGOUT - - -
 
 	/**
 	 * Attempts to log into the wiki that this <tt>MediaWiki</tt> represents
 	 * using the specified credentials.
 	 * 
 	 * @param user
 	 *            The username to use to log in.
 	 * @param password
 	 *            The password to use to log in.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws NullPointerException
 	 *             if either <code>user</code> or <code>password</code> is
 	 *             <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             if <code>user</code> is the empty string or
 	 *             <code>password</code> is of length 0
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned (subtypes thrown:
 	 *             <tt>MediaWiki.LoginFailureException</tt>,
 	 *             <tt>MediaWiki.LoginDelayException</tt>,
 	 *             <tt>MediaWiki.BlockException</tt>,
 	 *             <tt>MediaWiki.UnknownError</tt>)
 	 */
 	public MediaWiki logIn(final String user, final char[] password) throws NullPointerException, IllegalArgumentException, IOException, MediaWiki.MediaWikiException {
 		return logIn(user, password, null);
 	}
 
 	/**
 	 * Attempts to log into the wiki that this <tt>MediaWiki</tt> represents
 	 * using the specified credentials.
 	 * 
 	 * @param user
 	 *            The username to use to log in.
 	 * @param password
 	 *            The password to use to log in.
 	 * @param domain
 	 *            The domain to use to log in, or <code>null</code> if not using
 	 *            LDAP authentication.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws NullPointerException
 	 *             if either <code>user</code> or <code>password</code> is
 	 *             <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             if <code>user</code> is the empty string or
 	 *             <code>password</code> is of length 0
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned (subtypes thrown:
 	 *             <tt>MediaWiki.LoginFailureException</tt>,
 	 *             <tt>MediaWiki.LoginDelayException</tt>,
 	 *             <tt>MediaWiki.BlockException</tt>,
 	 *             <tt>MediaWiki.UnknownError</tt>)
 	 */
 	public MediaWiki logIn(final String user, final char[] password, final String domain) throws NullPointerException, IllegalArgumentException, IOException, MediaWiki.MediaWikiException {
 		if (user.length() == 0)
 			throw new IllegalArgumentException("user is empty");
 		if (password.length == 0)
 			throw new IllegalArgumentException("password is empty");
 
 		final Map<String, String> postParams = paramValuesToMap("lgname", user, "lgpassword", new String(password));
 		if (domain != null) {
 			if (domain.length() == 0)
 				throw new IllegalArgumentException("domain is empty");
 			postParams.put("lgdomain", domain);
 		}
 
 		final Map<String, String> getParams = paramValuesToMap("action", "login", "format", "xml");
 		final String url = createApiGetUrl(getParams);
 
 		int retry = 0;
 		networkLock.lock();
 		try {
 			do {
 				final InputStream in = post(url, postParams);
 				Document xml = parse(in);
 				// no checkError: specific errors checked below
 
 				final NodeList loginTags = xml.getElementsByTagName("login");
 				if (loginTags.getLength() >= 1) {
 					final Element loginTag = (Element) loginTags.item(0);
 					final String result = loginTag.getAttribute("result");
 					// Errors not checked for: NoName, EmptyPass, mustbeposted
 					if (result.equals("NeedToken")) {
 						retry++;
 						postParams.put("lgtoken", loginTag.getAttribute("token"));
 					} else if (result.equals("Success"))
 						return this;
 					else if (result.equals("Illegal"))
 						throw new MediaWiki.LoginFailureException("Disallowed username: " + user);
 					else if (result.equals("NotExists"))
 						throw new MediaWiki.LoginFailureException("Inexistent user: " + user);
 					else if (result.equals("WrongPass") || result.equals("WrongPluginPass"))
 						throw new MediaWiki.LoginFailureException("Incorrect password for user: " + user);
 					else if (result.equals("CreateBlocked"))
 						throw new MediaWiki.BlockException("Automatic user creation failed due to an IP block for user: " + user);
 					else if (result.equals("Throttled"))
 						throw new MediaWiki.LoginDelayException(Integer.parseInt(loginTag.getAttribute("wait")));
 					else if (result.equals("Blocked"))
 						throw new MediaWiki.BlockException(user);
 					else
 						throw new MediaWiki.UnknownError("login: " + result);
 				}
 			} while (retry <= 1);
 		} finally {
 			networkLock.unlock();
 		}
 		throw new MediaWiki.UnknownError("login");
 	}
 
 	/**
 	 * Logs out from the wiki that this <tt>MediaWiki</tt> represents.
 	 * 
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki
 	 */
 	public MediaWiki logOut() throws IOException {
 		final Map<String, String> getParams = paramValuesToMap("action", "logout", "format", "xml");
 		final String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			get(url);
 		} finally {
 			networkLock.unlock();
 		}
 
 		return this;
 	}
 
 	/**
 	 * Returns information about the currently logged-in user on the wiki that
 	 * this <tt>MediaWiki</tt> represents.
 	 * 
 	 * @return information about the currently logged-in user on the wiki that
 	 *         this <tt>MediaWiki</tt> represents
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if the API does not return a result in the expected format
 	 *             (subtypes thrown: <tt>MediaWiki.UnknownError</tt>)
 	 */
 	public MediaWiki.CurrentUser getCurrentUser() throws IOException, MediaWiki.MediaWikiException {
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "userinfo", "uiprop", "hasmsg|groups|rights|blockinfo|editcount");
 		final String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors declared for this action
 
 			final NodeList userinfoTags = xml.getElementsByTagName("userinfo");
 			if (userinfoTags.getLength() > 0) {
 				final Element userinfoTag = (Element) userinfoTags.item(0);
 
 				final boolean isAnonymous = userinfoTag.hasAttribute("anon");
 				final String userName = userinfoTag.getAttribute("name");
 				final long userID = Long.parseLong(userinfoTag.getAttribute("id"));
 				final boolean hasNewMessages = userinfoTag.hasAttribute("messages");
 				final String blockedBy = userinfoTag.hasAttribute("blockedby") ? userinfoTag.getAttribute("blockedby") : null;
 				final String blockReason = userinfoTag.hasAttribute("blockreason") ? userinfoTag.getAttribute("blockreason") : null;
 				final long editCount = Long.parseLong(userinfoTag.getAttribute("editcount"));
 
 				final Collection<String> rights = new TreeSet<String>();
 				final Element rightsTag = (Element) userinfoTag.getElementsByTagName("rights").item(0);
 				final NodeList rTags = rightsTag.getElementsByTagName("r");
 				for (int i = 0; i < rTags.getLength(); i++) {
 					rights.add(rTags.item(i).getTextContent());
 				}
 
 				final Collection<String> groups = new TreeSet<String>();
 				final Element groupsTag = (Element) userinfoTag.getElementsByTagName("groups").item(0);
 				final NodeList gTags = groupsTag.getElementsByTagName("g");
 				for (int i = 0; i < gTags.getLength(); i++) {
 					groups.add(gTags.item(i).getTextContent());
 				}
 
 				return new MediaWiki.CurrentUser(isAnonymous, userName, userID, hasNewMessages, groups, rights, editCount, blockedBy, blockReason);
 			}
 			throw new MediaWiki.ResponseFormatException("expected <userinfo> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - META: SITE INFO - - -
 
 	/**
 	 * A <tt>SoftReference</tt> towards an unmodifiable view of the list of
 	 * namespaces available on the wiki that this <tt>MediaWiki</tt> represents.
 	 */
 	private transient SoftReference<MediaWiki.Namespaces> namespaceCache;
 
 	/**
 	 * Gets a list of namespaces on the wiki that this <tt>MediaWiki</tt>
 	 * represents. The return value may be cached from an earlier invocation of
 	 * the method on the same <tt>MediaWiki</tt>.
 	 * 
 	 * @return a list of namespaces on the wiki that this <tt>MediaWiki</tt>
 	 *         represents
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 */
 	public MediaWiki.Namespaces getNamespaces() throws IOException {
 		preferenceLock.readLock().lock();
 		try {
 			MediaWiki.Namespaces result;
 			if ((namespaceCache != null) && ((result = namespaceCache.get()) != null))
 				return result;
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 		// Not cached. Get, cache and return the result now.
 
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "siteinfo", "siprop", "namespaces|namespacealiases");
 		final String url = createApiGetUrl(getParams);
 
 		final Map<Long, String> canonicalNames = new HashMap<Long, String>();
 		final Map<Long, Collection<String>> aliases = new HashMap<Long, Collection<String>>();
 		final Map<Long, Boolean> caseSensitives = new HashMap<Long, Boolean>();
 		final Map<Long, Boolean> areContent = new HashMap<Long, Boolean>();
 		final Map<Long, Boolean> allowSubpages = new HashMap<Long, Boolean>();
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors declared for this action
 
 			// Process <namespaces>.
 			final NodeList namespacesTags = xml.getElementsByTagName("namespaces");
 
 			if (namespacesTags.getLength() >= 1) {
 				final Element namespacesTag = (Element) namespacesTags.item(0);
 
 				final NodeList nsTags = namespacesTag.getElementsByTagName("ns");
 				for (int i = 0; i < nsTags.getLength(); i++) {
 					final Element nsTag = (Element) nsTags.item(i);
 					final long id = Long.parseLong(nsTag.getAttribute("id"));
 					final Long lID = Long.valueOf(id);
 					canonicalNames.put(lID, nsTag.getAttribute("canonical"));
 					if ((nsTag.getChildNodes().getLength() > 0) && !nsTag.getTextContent().equals(nsTag.getAttribute("canonical"))) {
 						// Add the content of <ns> to aliases.
 						final Collection<String> aliasesForNamespace = new TreeSet<String>();
 						aliasesForNamespace.add(nsTag.getTextContent());
 						aliases.put(lID, aliasesForNamespace);
 					}
 					caseSensitives.put(lID, nsTag.getAttribute("case").equals("case-sensitive"));
 					areContent.put(lID, nsTag.hasAttribute("content"));
 					allowSubpages.put(lID, nsTag.hasAttribute("subpages"));
 				}
 			}
 
 			// Process <namespacealiases>.
 			final NodeList namespaceAliasesTags = xml.getElementsByTagName("namespacealiases");
 
 			if (namespaceAliasesTags.getLength() >= 1) {
 				final Element namespaceAliasesTag = (Element) namespaceAliasesTags.item(0);
 
 				final NodeList nsTags = namespaceAliasesTag.getElementsByTagName("ns");
 				for (int i = 0; i < nsTags.getLength(); i++) {
 					final Element nsTag = (Element) nsTags.item(i);
 					final long id = Long.parseLong(nsTag.getAttribute("id"));
 					final Long lID = Long.valueOf(id);
 					if (nsTag.getChildNodes().getLength() > 0) {
 						// Add the content of <ns> to aliases.
 						Collection<String> aliasesForNamespace = aliases.get(lID);
 						if (aliasesForNamespace == null) {
 							aliasesForNamespace = new TreeSet<String>();
 							aliases.put(lID, aliasesForNamespace);
 						}
 						aliasesForNamespace.add(nsTag.getTextContent());
 					}
 				}
 			}
 
 			final Collection<MediaWiki.Namespace> namespaces = new ArrayList<Namespace>(canonicalNames.size());
 			for (final Long id : canonicalNames.keySet()) {
 				namespaces.add(new MediaWiki.Namespace(id, canonicalNames.get(id), aliases.get(id), caseSensitives.get(id), areContent.get(id), allowSubpages.get(id)));
 			}
 			final MediaWiki.Namespaces result = new MediaWiki.Namespaces(namespaces);
 			preferenceLock.writeLock().lock();
 			try {
 				namespaceCache = new SoftReference<MediaWiki.Namespaces>(result);
 			} finally {
 				preferenceLock.writeLock().unlock();
 			}
 			return result;
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * A <tt>SoftReference</tt> towards an unmodifiable view of the list of
 	 * interwiki prefixes available on the wiki that this <tt>MediaWiki</tt>
 	 * represents.
 	 */
 	private transient SoftReference<MediaWiki.InterwikiPrefixes> interwikiPrefixCache;
 
 	/**
 	 * Gets a list of interwiki prefixes on the wiki that this
 	 * <tt>MediaWiki</tt> represents. The return value may be cached from an
 	 * earlier invocation of the method on the same <tt>MediaWiki</tt>.
 	 * 
 	 * @return a list of interwiki prefixes on the wiki that this
 	 *         <tt>MediaWiki</tt> represents
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 */
 	public MediaWiki.InterwikiPrefixes getInterwikiPrefixes() throws IOException {
 		preferenceLock.readLock().lock();
 		try {
 			MediaWiki.InterwikiPrefixes result;
 			if ((interwikiPrefixCache != null) && ((result = interwikiPrefixCache.get()) != null))
 				return result;
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 		// Not cached. Get, cache and return the result now.
 
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "siteinfo", "siprop", "interwikimap");
 		final String url = createApiGetUrl(getParams);
 
 		final Map<String, Boolean> areLocal = new HashMap<String, Boolean>();
 		final Map<String, String> urlPatterns = new HashMap<String, String>();
 		final Map<String, String> languages = new HashMap<String, String>();
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors declared for this action
 
 			final NodeList interwikiMapTags = xml.getElementsByTagName("interwikimap");
 
 			if (interwikiMapTags.getLength() >= 1) {
 				final Element interwikiMapTag = (Element) interwikiMapTags.item(0);
 
 				final NodeList iwTags = interwikiMapTag.getElementsByTagName("iw");
 				for (int i = 0; i < iwTags.getLength(); i++) {
 					final Element iwTag = (Element) iwTags.item(i);
 					final String name = iwTag.getAttribute("prefix");
 					urlPatterns.put(name, iwTag.getAttribute("url"));
 					if (iwTag.hasAttribute("language")) {
 						languages.put(name, iwTag.getAttribute("language"));
 					}
 					areLocal.put(name, iwTag.hasAttribute("local"));
 				}
 			}
 
 			final Map<String, MediaWiki.InterwikiPrefix> interwikiPrefixes = new TreeMap<String, InterwikiPrefix>();
 			for (final String name : urlPatterns.keySet()) {
 				interwikiPrefixes.put(name, new MediaWiki.InterwikiPrefix(name, languages.get(name), urlPatterns.get(name), areLocal.get(name)));
 			}
 			final MediaWiki.InterwikiPrefixes result = new MediaWiki.InterwikiPrefixes(interwikiPrefixes);
 			preferenceLock.writeLock().lock();
 			try {
 				interwikiPrefixCache = new SoftReference<MediaWiki.InterwikiPrefixes>(result);
 			} finally {
 				preferenceLock.writeLock().unlock();
 			}
 			return result;
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Gets various statistics about the wiki that this <tt>MediaWiki</tt>
 	 * represents.
 	 * 
 	 * @return an object containing various statistics on the wiki that this
 	 *         <tt>MediaWiki</tt> represents
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if the API does not return a result in the expected format
 	 *             (subtypes thrown: <tt>MediaWiki.UnknownError</tt>)
 	 */
 	public MediaWiki.Statistics getStatistics() throws IOException, MediaWiki.MediaWikiException {
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "siteinfo", "siprop", "statistics");
 		final String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors declared for this action
 
 			final NodeList statisticsTags = xml.getElementsByTagName("statistics");
 
 			if (statisticsTags.getLength() >= 1) {
 				final Element statisticsTag = (Element) statisticsTags.item(0);
 
 				final long pages = Long.parseLong(statisticsTag.getAttribute("pages"));
 				final long articles = Long.parseLong(statisticsTag.getAttribute("articles"));
 				final long edits = Long.parseLong(statisticsTag.getAttribute("edits"));
 				final long images = Long.parseLong(statisticsTag.getAttribute("images"));
 				final long users = Long.parseLong(statisticsTag.getAttribute("users"));
 				final long activeUsers = Long.parseLong(statisticsTag.getAttribute("activeusers"));
 				final long admins = Long.parseLong(statisticsTag.getAttribute("admins"));
 				final long jobs = Long.parseLong(statisticsTag.getAttribute("jobs"));
 				final long views = statisticsTag.hasAttribute("views") ? Long.parseLong(statisticsTag.getAttribute("views")) : -1;
 				return new MediaWiki.Statistics(pages, articles, edits, images, users, activeUsers, admins, jobs, views);
 			}
 			throw new MediaWiki.ResponseFormatException("expected <statistics> tag not found");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Returns the database replication lag on the wiki that this
 	 * <tt>MediaWiki</tt> represents, in seconds. According to the API
 	 * documentation, a lag of over 5 seconds indicates that scripts should stop
 	 * making large amounts of edits to let the database replicas catch up.
 	 * <p>
 	 * The return value is <code>Long.MAX_VALUE</code> if, for some reason,
 	 * there is no database server named in the reply from the MediaWiki API.
 	 * This may indicate that all database replicas are down.
 	 * 
 	 * @return the database replication lag on the wiki that this
 	 *         <tt>MediaWiki</tt> represents, in seconds
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @see #setMaxLag(Integer)
 	 */
 	public long getDatabaseReplicationLag() throws IOException {
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "siteinfo", "siprop", "dbrepllag");
 		final String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors defined for this action
 
 			final NodeList dbReplLagTags = xml.getElementsByTagName("dbrepllag");
 			if (dbReplLagTags.getLength() >= 1) {
 				final Element dbReplLagTag = (Element) dbReplLagTags.item(0);
 				final NodeList dbTags = dbReplLagTag.getElementsByTagName("db");
 
 				if (dbTags.getLength() == 0)
 					return Long.MAX_VALUE;
 
 				long maxLag = 0;
 
 				for (int i = 0; i < dbTags.getLength(); i++) {
 					final Element dbTag = (Element) dbTags.item(i);
 					final long lag = Long.parseLong(dbTag.getAttribute("lag"));
 					maxLag = Math.max(maxLag, lag);
 				}
 
 				return maxLag;
 			} else
 				return Long.MAX_VALUE;
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * A <tt>SoftReference</tt> towards an unmodifiable view of the list of
 	 * special pages available on the wiki that this <tt>MediaWiki</tt>
 	 * represents.
 	 */
 	private transient SoftReference<Map<String, String>> specialPageAliasCache;
 
 	/**
 	 * Gets a list of special page aliases on the wiki that this
 	 * <tt>MediaWiki</tt> represents. The return value may be cached from an
 	 * earlier invocation of the method on the same <tt>MediaWiki</tt>.
 	 * <p>
 	 * The return value maps an alias name to the canonical name of its special
 	 * page, for all special page names and aliases.
 	 * 
 	 * @return a list of special page aliases on the wiki that this
 	 *         <tt>MediaWiki</tt> represents
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 */
 	public Map<String, String> getSpecialPageAliases() throws IOException {
 		preferenceLock.readLock().lock();
 		try {
 			Map<String, String> result;
 			if ((specialPageAliasCache != null) && ((result = specialPageAliasCache.get()) != null))
 				return result;
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 		// Not cached. Get, cache and return the result now.
 
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "meta", "siteinfo", "siprop", "specialpagealiases");
 		final String url = createApiGetUrl(getParams);
 
 		final Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			// no checkError: no errors defined for this action
 
 			final NodeList specialPageTags = xml.getElementsByTagName("specialpage");
 
 			for (int i = 0; i < specialPageTags.getLength(); i++) {
 				final Element specialPageTag = (Element) specialPageTags.item(i);
 
 				final String realName = specialPageTag.getAttribute("realname");
 
 				final NodeList aliasTags = specialPageTag.getElementsByTagName("alias");
 				for (int j = 0; j < aliasTags.getLength(); j++) {
 					final Element aliasTag = (Element) aliasTags.item(j);
 
 					final String alias = aliasTag.getTextContent();
 					result.put(alias, realName);
 				}
 
 				result.put(realName, realName);
 			}
 
 			preferenceLock.writeLock().lock();
 			try {
 				specialPageAliasCache = new SoftReference<Map<String, String>>(result);
 			} finally {
 				preferenceLock.writeLock().unlock();
 			}
 			return result;
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - PAGE INFORMATION (PROP=INFO) - - -
 
 	/**
 	 * Retrieves information about pages specified by their full names. The
 	 * return value is an iterator which will return information about the pages
 	 * in the <code>titles</code> parameter in order when its <code>next</code>
 	 * method is called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing page;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param titles
 	 *            The full name(s) of the page(s) to return information about.
 	 * @return an iterator which will return information about the pages in the
 	 *         <code>titles</code> parameter in order when its <code>next</code>
 	 *         method is called
 	 */
 	public Iterator<MediaWiki.Page> getPageInformation(final String... titles) {
 		return new MediaWiki.PageIterator(titles);
 	}
 
 	private static final SimpleDateFormat iso8601TimestampParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
 
 	static {
 		iso8601TimestampParser.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 
 	private class PageIterator extends AbstractReadOnlyIterator<MediaWiki.Page> {
 		/**
 		 * The titles to get information about.
 		 */
 		private final String[] elements;
 
 		private int i;
 
 		private final Map<String, String> getParams;
 
 		PageIterator(final String[] elements) {
 			this.elements = elements;
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "inprop", "protection");
 
 			i = -1;
 		}
 
 		public synchronized boolean hasNext() {
 			return i + 1 < (elements.length);
 		}
 
 		public synchronized MediaWiki.Page next() throws MediaWiki.IterationException {
 			i++;
 
 			getParams.put("titles", titleToAPIForm(elements[i]));
 
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					final String title = pageTag.getAttribute("title");
 					final boolean missing = pageTag.hasAttribute("missing");
 					final long namespaceID = Long.parseLong(pageTag.getAttribute("ns"));
 
 					final long pageID = pageTag.hasAttribute("pageid") ? Long.parseLong(pageTag.getAttribute("pageid")) : -1;
 					final Date lastEdit = pageTag.hasAttribute("touched") ? iso8601TimestampParser.parse(pageTag.getAttribute("touched")) : null;
 					final long lastRevisionID = pageTag.hasAttribute("lastrevid") ? Long.parseLong(pageTag.getAttribute("lastrevid")) : -1;
 					final long views = pageTag.hasAttribute("counter") && pageTag.getAttribute("counter").length() > 0 ? Long.parseLong(pageTag.getAttribute("counter")) : -1;
 					final long length = pageTag.hasAttribute("length") ? Long.parseLong(pageTag.getAttribute("length")) : 0;
 					final boolean isRedirect = pageTag.hasAttribute("redirect");
 					final boolean isNew = pageTag.hasAttribute("new");
 
 					final Map<String, MediaWiki.Protection> protections = new TreeMap<String, Protection>();
 
 					final NodeList prTags = pageTag.getElementsByTagName("pr");
 
 					for (int i = 0; i < prTags.getLength(); i++) {
 						final Element prTag = (Element) prTags.item(i);
 
 						final String type = prTag.getAttribute("type");
 						final String level = prTag.getAttribute("level");
 						final Date expiry = prTag.getAttribute("expiry").equals("infinity") ? null : iso8601TimestampParser.parse(prTag.getAttribute("expiry"));
 						final boolean isCascading = prTag.hasAttribute("cascade");
 						final String cascadeSource = prTag.hasAttribute("source") ? prTag.getAttribute("source") : null;
 
 						protections.put(type, new MediaWiki.Protection(level, expiry, isCascading, cascadeSource));
 					}
 
 					return new MediaWiki.Page(missing, pageID, title, lastEdit, namespaceID, lastRevisionID, views, length, isRedirect, isNew, protections);
 				}
 
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 			} catch (final IOException ioe) {
 				throw new MediaWiki.IterationException(ioe);
 			} catch (final ParseException pe) {
 				throw new MediaWiki.IterationException(pe);
 			} catch (MediaWiki.IterationException ie) {
 				throw ie;
 			} catch (MediaWiki.MediaWikiException mwe) {
 				throw new MediaWiki.IterationException(mwe);
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - REVISION INFORMATION (PROP=REVISIONS) - - -
 
 	/**
 	 * Retrieves information about the last revision of pages specified by their
 	 * full names. The return value is an iterator which will return information
 	 * about the last revision of the pages listed in the <code>titles</code>
 	 * parameter in order when its <code>next</code> method is called. The
 	 * iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing page;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param titles
 	 *            The full name(s) of the pages(s) to return information about
 	 *            the last revision of.
 	 * @return an iterator which will return information about the last revision
 	 *         of the pages listed in the <code>titles</code> parameter in order
 	 *         when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getLastRevision(final String... titles) {
 		return getLastRevision(false, titles);
 	}
 
 	/**
 	 * Retrieves information about the specified revision(s). The return value
 	 * is an iterator which will return information about each revision of the
 	 * <code>ids</code> parameter in order when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing revision;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param ids
 	 *            The ID(s) of the revision(s) to return information about.
 	 * @return an iterator which will return information about each revision of
 	 *         the <code>ids</code> parameter in order when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final long... ids) {
 		return getRevisions(false, ids);
 	}
 
 	/**
 	 * Retrieves information about the last revision of pages specified by their
 	 * full names. The return value is an iterator which will return information
 	 * about the last revision of the pages listed in the <code>titles</code>
 	 * parameter in order when its <code>next</code> method is called. The
 	 * iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing page;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param getContentImmediately
 	 *            <code>true</code> to get the content of the last revision of
 	 *            each page in the same request, saving a request. Regardless of
 	 *            the value of this parameter, the returned
 	 *            <tt>MediaWiki.Revision</tt> object's <code>getContent</code>
 	 *            method will return the content.
 	 * @param titles
 	 *            The full name(s) of the pages(s) to return information about
 	 *            the last revision of.
 	 * @return an iterator which will return information about the last revision
 	 *         of the pages listed in the <code>titles</code> parameter in order
 	 *         when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getLastRevision(final boolean getContentImmediately, final String... titles) {
 		return new MediaWiki.SingleRevisionIterator("titles", titles, getContentImmediately);
 	}
 
 	/**
 	 * Retrieves information about the specified revision(s). The return value
 	 * is an iterator which will return information about each revision of the
 	 * <code>ids</code> parameter in order when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing revision;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param getContentImmediately
 	 *            <code>true</code> to get the content of the last revision of
 	 *            each page in the same request, saving a request. Regardless of
 	 *            the value of this parameter, the returned
 	 *            <tt>MediaWiki.Revision</tt> object's <code>getContent</code>
 	 *            method will return the content.
 	 * @param ids
 	 *            The ID(s) of the revision(s) to return information about.
 	 * @return an iterator which will return information about each revision of
 	 *         the <code>ids</code> parameter in order when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final boolean getContentImmediately, final long... ids) {
 		return new MediaWiki.SingleRevisionIterator("revids", ids, getContentImmediately);
 	}
 
 	private class SingleRevisionIterator extends AbstractReadOnlyIterator<MediaWiki.Revision> {
 		/**
 		 * The type of the elements given. This may be "titles" or "revids",
 		 * corresponding to the parameter names acceptable for prop=revisions.
 		 */
 		private final String elementType;
 
 		/**
 		 * The elements (titles or revision IDs) to get information about. The
 		 * type of this field is either String[] or long[].
 		 */
 		private final Object elements;
 
 		private final boolean getContentImmediately;
 
 		private int i;
 
 		private final Map<String, String> getParams;
 
 		SingleRevisionIterator(final String elementType, final Object elements, final boolean getContentImmediately) {
 			if ((elementType == null) || (elementType.length() == 0))
 				throw new IllegalArgumentException("elementType is null or empty");
 			if (!((elements instanceof String[]) || (elements instanceof long[])))
 				throw new IllegalArgumentException("elements does not have an acceptable type");
 			this.elementType = elementType;
 			this.elements = elements;
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "revisions", "rvprop", "ids|flags|timestamp|user|comment|size");
 
 			if (getContentImmediately) {
 				getParams.put("rvprop", getParams.get("rvprop") + "|content");
 			}
 
 			this.getContentImmediately = getContentImmediately;
 
 			i = -1;
 		}
 
 		public synchronized boolean hasNext() {
 			return i + 1 < (elements instanceof String[] ? ((String[]) elements).length : ((long[]) elements).length);
 		}
 
 		public synchronized MediaWiki.Revision next() throws MediaWiki.IterationException {
 			i++;
 
 			getParams.put(elementType, elements instanceof String[] ? titleToAPIForm(((String[]) elements)[i]) : Long.toString(((long[]) elements)[i]));
 
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList badrevidsTags = xml.getElementsByTagName("badrevids");
 				if (badrevidsTags.getLength() > 0)
 					return null;
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (pageTag.hasAttribute("missing"))
 						return null;
 
 					final NodeList revTags = pageTag.getElementsByTagName("rev");
 
 					if (revTags.getLength() > 0) {
 						final Element revTag = (Element) revTags.item(0);
 
 						final long revisionID = Long.parseLong(revTag.getAttribute("revid"));
 						final long parentID = revTag.hasAttribute("parentid") ? Long.parseLong(revTag.getAttribute("parentid")) : -1;
 
 						final Date timestamp = iso8601TimestampParser.parse(revTag.getAttribute("timestamp"));
 
 						final boolean userHidden = revTag.hasAttribute("userhidden");
 						final String userName = userHidden ? null : revTag.getAttribute("user");
 
 						final boolean commentHidden = revTag.hasAttribute("commenthidden");
 						final String comment = commentHidden ? null : revTag.getAttribute("comment");
 
 						final boolean isMinor = revTag.hasAttribute("minor");
 						final boolean isAnonymous = revTag.hasAttribute("anon");
 
 						final long length = revTag.hasAttribute("size") ? Long.parseLong(revTag.getAttribute("size")) : 0;
 
 						MediaWiki.Revision result = new MediaWiki.Revision(revisionID, parentID, timestamp, userName, userHidden, length, comment, commentHidden, isMinor, isAnonymous);
 
 						if (getContentImmediately) {
 							if (revTag.hasAttribute("contenthidden"))
 								result.contentHidden = true;
 							else
 								result.content = revTag.getTextContent();
 							result.contentStored = true;
 						}
 
 						return result;
 					}
 				}
 
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 			} catch (final IOException ioe) {
 				throw new MediaWiki.IterationException(ioe);
 			} catch (final ParseException pe) {
 				throw new MediaWiki.IterationException(pe);
 			} catch (MediaWiki.IterationException ie) {
 				throw ie;
 			} catch (MediaWiki.MediaWikiException mwe) {
 				throw new MediaWiki.IterationException(mwe);
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name. The return value is an iterator which will return information about
 	 * each revision of the page in order from oldest to newest when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to return information about all
 	 *            revisions of.
 	 * @return an iterator which will return information about each revision of
 	 *         the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final String title) {
 		return getRevisions(title, (Date) null, (Date) null);
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name, between the two specified dates inclusively. The return value is an
 	 * iterator which will return information about each matching revision of
 	 * the page in order from oldest to newest when its <code>next</code> method
 	 * is called. The iterator's methods behave as follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if no revisions fall in the specified date range.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to return information about matching
 	 *            revisions of.
 	 * @param earliest
 	 *            The timestamp at which to start enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only the <code>end</code> bound.
 	 * @param latest
 	 *            The timestamp at which to stop enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only the <code>start</code> bound.
 	 * @return an iterator which will return information about each matching
 	 *         revision of the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final String title, final Date earliest, final Date latest) {
 		return getRevisions(false, title, earliest, latest);
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name, between the two specified revision IDs inclusively. The return
 	 * value is an iterator which will return information about each matching
 	 * revision of the page in order from oldest to newest when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if no revisions fall in the specified revision ID
 	 * range.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to return information about matching
 	 *            revisions of.
 	 * @param earliestID
 	 *            The revision ID at which to start enumerating revisions. If
 	 *            one of the page's revisions has this ID, it is included as
 	 *            well. May be <code>null</code> to use only the
 	 *            <code>end</code> bound.
 	 * @param latestID
 	 *            The revision ID at which to stop enumerating revisions. If one
 	 *            of the page's revisions has this ID, it is included as well.
 	 *            May be <code>null</code> to use only the <code>start</code>
 	 *            bound.
 	 * @return an iterator which will return information about each matching
 	 *         revision of the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final String title, final Long earliestID, final Long latestID) {
 		return getRevisions(false, title, earliestID, latestID);
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name. The return value is an iterator which will return information about
 	 * each revision of the page in order from oldest to newest when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing.
 	 * </ul>
 	 * 
 	 * @param getContentImmediately
 	 *            <code>true</code> to get the content of matching revisions in
 	 *            the same request, saving a request. Regardless of the value of
 	 *            this parameter, the returned <tt>MediaWiki.Revision</tt>
 	 *            object's <code>getContent</code> method will return the
 	 *            content.
 	 * @param title
 	 *            The full name of the page to return information about all
 	 *            revisions of.
 	 * @return an iterator which will return information about each revision of
 	 *         the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final boolean getContentImmediately, final String title) {
 		return getRevisions(getContentImmediately, title, (Date) null, (Date) null);
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name, between the two specified dates inclusively. The return value is an
 	 * iterator which will return information about each matching revision of
 	 * the page in order from oldest to newest when its <code>next</code> method
 	 * is called. The iterator's methods behave as follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if no revisions fall in the specified date range.
 	 * </ul>
 	 * 
 	 * @param getContentImmediately
 	 *            <code>true</code> to get the content of matching revisions in
 	 *            the same request, saving a request. Regardless of the value of
 	 *            this parameter, the returned <tt>MediaWiki.Revision</tt>
 	 *            object's <code>getContent</code> method will return the
 	 *            content.
 	 * @param title
 	 *            The full name of the page to return information about matching
 	 *            revisions of.
 	 * @param earliest
 	 *            The timestamp at which to start enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only the <code>end</code> bound.
 	 * @param latest
 	 *            The timestamp at which to stop enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only the <code>start</code> bound.
 	 * @return an iterator which will return information about each matching
 	 *         revision of the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final boolean getContentImmediately, final String title, final Date earliest, final Date latest) {
 		return new MediaWiki.MultipleRevisionIterator(title, "rvstart", earliest, "rvend", latest, getContentImmediately);
 	}
 
 	/**
 	 * Retrieves information about all revisions of a page specified by its full
 	 * name, between the two specified revision IDs inclusively. The return
 	 * value is an iterator which will return information about each matching
 	 * revision of the page in order from oldest to newest when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if no revisions fall in the specified revision ID
 	 * range.
 	 * </ul>
 	 * 
 	 * @param getContentImmediately
 	 *            <code>true</code> to get the content of matching revisions in
 	 *            the same request, saving a request. Regardless of the value of
 	 *            this parameter, the returned <tt>MediaWiki.Revision</tt>
 	 *            object's <code>getContent</code> method will return the
 	 *            content.
 	 * @param title
 	 *            The full name of the page to return information about matching
 	 *            revisions of.
 	 * @param earliestID
 	 *            The revision ID at which to start enumerating revisions. If
 	 *            one of the page's revisions has this ID, it is included as
 	 *            well. May be <code>null</code> to use only the
 	 *            <code>end</code> bound.
 	 * @param latestID
 	 *            The revision ID at which to stop enumerating revisions. If one
 	 *            of the page's revisions has this ID, it is included as well.
 	 *            May be <code>null</code> to use only the <code>start</code>
 	 *            bound.
 	 * @return an iterator which will return information about each matching
 	 *         revision of the page in order from oldest to newest when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Revision> getRevisions(final boolean getContentImmediately, final String title, final Long earliestID, final Long latestID) {
 		return new MediaWiki.MultipleRevisionIterator(title, "rvstartid", earliestID, "rvendid", latestID, getContentImmediately);
 	}
 
 	private class MultipleRevisionIterator extends AbstractContinuableQueryIterator<MediaWiki.Revision> {
 		private final boolean getContentImmediately;
 
 		private final Map<String, String> getParams;
 
 		MultipleRevisionIterator(final String element, final String startType, final Object start, final String endType, final Object end, final boolean getContentImmediately) {
			super(startType, start != null ? (start instanceof Date ? dateToTimestamp((Date) start) : start.toString()) : null);
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "revisions", "titles", titleToAPIForm(element), "rvprop", "ids|flags|timestamp|user|comment|size", "rvdir", "newer", "rvlimit", "max");
 			if (end != null) {
 				getParams.put(endType, end.toString());
 			}
 			Integer maxLag = getMaxLag();
 			if (maxLag != null)
 				getParams.put("maxlag", maxLag.toString());
 
 			if (getContentImmediately) {
 				getParams.put("rvprop", getParams.get("rvprop") + "|content");
 			}
 
 			this.getContentImmediately = getContentImmediately;
 		}
 
 		public MediaWiki.Revision convert(final Element element) throws ParseException {
 			final long revisionID = Long.parseLong(element.getAttribute("revid"));
 			final long parentID = element.hasAttribute("parentid") ? Long.parseLong(element.getAttribute("parentid")) : -1;
 
 			final Date timestamp = iso8601TimestampParser.parse(element.getAttribute("timestamp"));
 
 			final boolean userHidden = element.hasAttribute("userhidden");
 			final String userName = userHidden ? null : element.getAttribute("user");
 
 			final boolean commentHidden = element.hasAttribute("commenthidden");
 			final String comment = commentHidden ? null : element.getAttribute("comment");
 
 			final boolean isMinor = element.hasAttribute("minor");
 			final boolean isAnonymous = element.hasAttribute("anon");
 
 			final long length = element.hasAttribute("size") ? Long.parseLong(element.getAttribute("size")) : 0;
 
 			MediaWiki.Revision result = new MediaWiki.Revision(revisionID, parentID, timestamp, userName, userHidden, length, comment, commentHidden, isMinor, isAnonymous);
 
 			if (getContentImmediately) {
 				if (element.hasAttribute("contenthidden"))
 					result.contentHidden = true;
 				else
 					result.content = element.getTextContent();
 				result.contentStored = true;
 			}
 
 			return result;
 		}
 
 		protected synchronized void cacheUpcoming() throws IOException, MediaWiki.MediaWikiException {
 			// Get the next page of revisions from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			// The start parameter is for only this get.
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList badrevidsTags = xml.getElementsByTagName("badrevids");
 				if (badrevidsTags.getLength() == 0) {
 					final NodeList pageTags = xml.getElementsByTagName("page");
 
 					if (pageTags.getLength() > 0) {
 						final Element pageTag = (Element) pageTags.item(0);
 
 						if (!pageTag.hasAttribute("missing")) {
 							setUpcoming(pageTag.getElementsByTagName("rev"));
 						}
 					}
 				}
 
 				processContinuation(xml, "revisions");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - CATEGORY MEMBERSHIP (PROP=CATEGORIES) - - -
 
 	/**
 	 * Retrieves information about the membership of a page, specified by its
 	 * title, in categories. The return value is an iterator which will return
 	 * information about each category that the page is a member of when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if the page is not a member of any category.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to be examined.
 	 * @return an iterator which will return information about each category
 	 *         that the page is a member of when its <code>next</code> method is
 	 *         called
 	 */
 	public Iterator<MediaWiki.CategoryMembership> getCategories(final String title) {
 		return new MediaWiki.CategoryIterator(title);
 	}
 
 	private class CategoryIterator extends AbstractContinuableQueryIterator<MediaWiki.CategoryMembership> {
 		private final Map<String, String> getParams;
 
 		CategoryIterator(final String element) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "categories", "titles", titleToAPIForm(element), "clprop", "sortkey", "cllimit", "max");
 		}
 
 		public MediaWiki.CategoryMembership convert(Element element) throws Exception {
 			final String category = element.getAttribute("title");
 			final String sortKey = element.getAttribute("sortkeyprefix");
 
 			return new MediaWiki.CategoryMembership(category, sortKey);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of categories from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						setUpcoming(pageTag.getElementsByTagName("cl"));
 					}
 				}
 
 				processContinuation(xml, "categories");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - CATEGORY MEMBERS (LIST=CATEGORYMEMBERS) - - -
 
 	/**
 	 * Retrieves information about the members of a category, specified by its
 	 * title. The return value is an iterator which will return information
 	 * about each matching page that is a member of the category when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the category is missing, or if the category has no members.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the category to be examined.
 	 * @param chronologicalOrder
 	 *            Whether the pages are listed in chronological order (
 	 *            <code>true</code>) of being added to the category, or in
 	 *            reverse chronological order (<code>false</code>).
 	 * @param earliest
 	 *            The timestamp at which to start enumerating additions to the
 	 *            category. Milliseconds are not used. Any pages added on the
 	 *            second specified by it are included. May be <code>null</code>
 	 *            to use only the <code>end</code> bound.
 	 * @param latest
 	 *            The timestamp at which to stop enumerating additions to the
 	 *            category. Milliseconds are not used. Any pages added on the
 	 *            second specified by it are included. May be <code>null</code>
 	 *            to use only the <code>start</code> bound.
 	 * @return an iterator which will return information about each matching
 	 *         page that is a member of the given category when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.CategoryMember> getCategoryMembers(final String title, final boolean chronologicalOrder, final Date earliest, final Date latest, final long... namespaceIDs) {
 		return new MediaWiki.CategoryMemberIterator(title, chronologicalOrder, earliest, latest, namespaceIDs);
 	}
 
 	/**
 	 * Retrieves information about the members of a category, specified by its
 	 * title. The return value is an iterator which will return information
 	 * about each matching page that is a member of the category when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the category is missing, or if the category has no members.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the category to be examined.
 	 * @param ascendingOrder
 	 *            Whether the pages are listed in ascending order (
 	 *            <code>true</code>), or in descending order (<code>false</code>
 	 *            ) according to the category's sort key.
 	 * @param first
 	 *            The sort key at which to start enumerating pages in the
 	 *            category. A page having this exact sort key is included in the
 	 *            result. May be <code>null</code> to use only the
 	 *            <code>end</code> bound.
 	 * @param last
 	 *            The sort key at which to stop enumerating pages in the
 	 *            category. A page having this exact sort key is excluded from
 	 *            the result. May be <code>null</code> to use only the
 	 *            <code>start</code> bound.
 	 * @param namespaces
 	 *            Whether to require a category member to be in one of the
 	 *            specified namespaces to be retrieved, or not to care about
 	 *            that (<code>null</code>).
 	 * @return an iterator which will return information about each matching
 	 *         page that is a member of the given category when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.CategoryMember> getCategoryMembers(final String title, final boolean ascendingOrder, final String first, final String last, final long... namespaceIDs) {
 		return new MediaWiki.CategoryMemberIterator(title, ascendingOrder, first, last, namespaceIDs);
 	}
 
 	private class CategoryMemberIterator extends AbstractContinuableQueryIterator<MediaWiki.CategoryMember> {
 		private final Map<String, String> getParams;
 
 		CategoryMemberIterator(final String element, final boolean ascendingOrder, final Object first, final Object last, final long... namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "categorymembers", "cmtitle", titleToAPIForm(element), "cmprop", "ids|title|sortkeyprefix|timestamp", "cmlimit", "max");
 
 			if ((first == null && last == null) || (first instanceof String) || (last instanceof String))
 				getParams.put("cmsort", "sortkey");
 			else if (first instanceof Date || last instanceof Date)
 				getParams.put("cmsort", "timestamp");
 			getParams.put("cmdir", ascendingOrder ? "asc" : "desc");
 			// If the order is descending, we need to reverse them.
 			String firstValue = first != null ? (first instanceof Date ? dateToISO8601((Date) first) : (String) first) : null;
 			String lastValue = last != null ? (last instanceof Date ? dateToISO8601((Date) last) : (String) last) : null;
 			if (ascendingOrder) {
 				getParams.put(first instanceof Date ? "cmstart" : "cmstartsortkeyprefix", firstValue);
 				getParams.put(last instanceof Date ? "cmend" : "cmendsortkeyprefix", lastValue);
 			} else {
 				getParams.put(first instanceof Date ? "cmend" : "cmendsortkeyprefix", firstValue);
 				getParams.put(last instanceof Date ? "cmstart" : "cmstartsortkeyprefix", lastValue);
 			}
 
 			getParams.put("cmnamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.CategoryMember convert(Element element) throws Exception {
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final String title = element.getAttribute("title");
 			final long pageID = Long.parseLong(element.getAttribute("pageid"));
 			final String sortKey = element.getAttribute("sortkeyprefix");
 			final Date addTime = timestampToDate(element.getAttribute("timestamp"));
 
 			return new MediaWiki.CategoryMember(namespaceID, pageID, addTime, title, sortKey);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of category members from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList categorymembersTags = xml.getElementsByTagName("categorymembers");
 
 				if (categorymembersTags.getLength() > 0) {
 					final Element categorymembersTag = (Element) categorymembersTags.item(0);
 
 					setUpcoming(categorymembersTag.getElementsByTagName("cm"));
 				}
 
 				processContinuation(xml, "categorymembers");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - IMAGE INFORMATION (PROP=IMAGEINFO) - - -
 
 	/**
 	 * Retrieves information about the revisions of an image, specified by its
 	 * title. The return value is an iterator which will return information
 	 * about each revision made to the image, in order from newest to oldest,
 	 * when its <code>next</code> method is called. The iterator's methods
 	 * behave as follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the image is missing or has had no upload.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the image to be examined. That is, the
 	 *            namespace prefix is included.
 	 * @return an iterator which will return information about each revision
 	 *         made to the image, in order from newest to oldest, when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.ImageRevision> getImageRevisions(final String title) {
 		return getImageRevisions(title, null, null);
 	}
 
 	/**
 	 * Retrieves information about the revisions of an image during a specified
 	 * time range, specified by its title. The return value is an iterator which
 	 * will return information about each matching revision made to the image,
 	 * in order from newest to oldest, when its <code>next</code> method is
 	 * called. The iterator's methods behave as follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the image is missing or has had no upload during the specified time
 	 * range.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the image to be examined. That is, the
 	 *            namespace prefix is included.
 	 * @param earliest
 	 *            The timestamp at which to start enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only <code>end</code> (or enumerate all image revisions if
 	 *            <code>end</code> is also <code>null</code>).
 	 * @param latest
 	 *            The timestamp at which to stop enumerating revisions.
 	 *            Milliseconds are not used. Any revisions made on the second
 	 *            specified by it are included. May be <code>null</code> to use
 	 *            only <code>start</code> (or enumerate all image revisions if
 	 *            <code>start</code> is also <code>null</code>).
 	 * @return an iterator which will return information about each matching
 	 *         revision made to the image, in order from newest to oldest, when
 	 *         its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.ImageRevision> getImageRevisions(final String title, final Date earliest, final Date latest) {
 		return new MediaWiki.ImageRevisionIterator(title, earliest != null ? dateToTimestamp(earliest) : null, latest != null ? dateToTimestamp(latest) : null);
 	}
 
 	private class ImageRevisionIterator extends AbstractContinuableQueryIterator<MediaWiki.ImageRevision> {
 		/**
 		 * The full name of the image whose revisions are being iterated over,
 		 * gathered from the <tt>&lt;page&gt;</tt> tag.
 		 */
 		private String imageFullName;
 
 		private final Map<String, String> getParams;
 
 		ImageRevisionIterator(final String element, final String earliest, final String latest) {
 			/*
 			 * Implementation note: The MediaWiki API considers 'start' to be
 			 * after 'end' chronologically because it returns image revisions
 			 * from the newest to the oldest, so they are reversed below.
 			 */
 			super("iistart", latest);
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "imageinfo", "titles", titleToAPIForm(element), "iiprop", "timestamp|user|comment|url|size|sha1|mime", "iilimit", "max");
 			getParams.put("iiend", earliest);
 		}
 
 		public MediaWiki.ImageRevision convert(Element element) throws Exception {
 			final Date timestamp = iso8601TimestampParser.parse(element.getAttribute("timestamp"));
 			final String userName = element.getAttribute("user");
 			final long length = Long.parseLong(element.getAttribute("size"));
 			final long width = Long.parseLong(element.getAttribute("width"));
 			final long height = Long.parseLong(element.getAttribute("height"));
 			final String url = element.getAttribute("url");
 			final String comment = element.getAttribute("comment");
 			final String sha1hash = element.getAttribute("sha1");
 			final String mimeType = element.getAttribute("mime");
 
 			return new MediaWiki.ImageRevision(imageFullName, timestamp, userName, length, width, height, url, comment, sha1hash, mimeType);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of image revisions from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList badrevidsTags = xml.getElementsByTagName("badrevids");
 				if (badrevidsTags.getLength() == 0) {
 					final NodeList pageTags = xml.getElementsByTagName("page");
 
 					if (pageTags.getLength() > 0) {
 						final Element pageTag = (Element) pageTags.item(0);
 
 						imageFullName = pageTag.getAttribute("title");
 
 						if (!pageTag.hasAttribute("missing")) {
 							setUpcoming(pageTag.getElementsByTagName("ii"));
 						}
 					}
 				}
 
 				processContinuation(xml, "imageinfo");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - LANGUAGE LINKS (PROP=LANGLINKS) - - -
 
 	/**
 	 * Retrieves a list of the interlanguage links of a page, specified by its
 	 * title. The return value is an iterator which will return information
 	 * about each interlanguage link that the page contains when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if the page has no interlanguage links.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to be examined.
 	 * @return an iterator which will return information about each
 	 *         interlanguage link that the page contains when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.InterlanguageLink> getInterlanguageLinks(final String title) {
 		return new MediaWiki.InterlanguageLinkIterator(title);
 	}
 
 	private class InterlanguageLinkIterator extends AbstractContinuableQueryIterator<MediaWiki.InterlanguageLink> {
 		private final Map<String, String> getParams;
 
 		InterlanguageLinkIterator(final String element) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "langlinks", "titles", titleToAPIForm(element), "lllimit", "max");
 		}
 
 		public MediaWiki.InterlanguageLink convert(Element element) throws Exception {
 			final String language = element.getAttribute("lang");
 			final String foreignTitle = element.getTextContent();
 
 			return new MediaWiki.InterlanguageLink(language, foreignTitle);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of links from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						setUpcoming(pageTag.getElementsByTagName("ll"));
 					}
 				}
 
 				processContinuation(xml, "langlinks");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - PAGE LINKS (PROP=LINKS) - - -
 
 	/**
 	 * Retrieves a list of the links on a page, specified by its title. The
 	 * return value is an iterator which will return information about each link
 	 * that the page contains when its <code>next</code> method is called. The
 	 * iterator's methods behave as follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if the page has no links.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to be examined.
 	 * @param namespaceIDs
 	 *            The namespaces to return links towards. May be
 	 *            <code>null</code> or an empty array or argument list to
 	 *            include all namespaces. For example, if this is <code>0</code>
 	 *            , then only links towards articles in the <i>main
 	 *            namespace</i> on the designated page are returned.
 	 * @return an iterator which will return information about each link that
 	 *         the page contains when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Link> getLinks(final String title, final long... namespaceIDs) {
 		return new MediaWiki.LinkIterator(title, namespaceIDs);
 	}
 
 	private class LinkIterator extends AbstractContinuableQueryIterator<MediaWiki.Link> {
 		private final Map<String, String> getParams;
 
 		LinkIterator(final String element, final long[] namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "links", "titles", titleToAPIForm(element), "pllimit", "max");
 			getParams.put("plnamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.Link convert(Element element) throws Exception {
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final String title = element.getAttribute("title");
 
 			return new MediaWiki.Link(namespaceID, title);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of links from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						setUpcoming(pageTag.getElementsByTagName("pl"));
 					}
 				}
 
 				processContinuation(xml, "links");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - TRANSCLUSIONS IN A PAGE (PROP=TEMPLATES) - - -
 
 	/**
 	 * Retrieves a list of the pages transcluded on a page, specified by its
 	 * title. The return value is an iterator which will return information
 	 * about each transcluded page that the page contains when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if the page does not have anything transcluded on
 	 * it.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to be examined. Transclusions of
 	 *            <em>other pages</em> on this page are returned.
 	 * @param namespaceIDs
 	 *            The namespaces to return transclusions from. May be
 	 *            <code>null</code> or an empty array or argument list to
 	 *            include all namespaces. For example, if this is <code>0</code>
 	 *            , then only transclusions of pages in the <i>main
 	 *            namespace</i> on the designated page are returned.
 	 * @return an iterator which will return information about each transcluded
 	 *         page that the page contains when its <code>next</code> method is
 	 *         called
 	 */
 	public Iterator<MediaWiki.Link> getTransclusions(final String title, final long... namespaceIDs) {
 		return new MediaWiki.TransclusionIterator(title, namespaceIDs);
 	}
 
 	private class TransclusionIterator extends AbstractContinuableQueryIterator<MediaWiki.Link> {
 		private final Map<String, String> getParams;
 
 		TransclusionIterator(final String element, final long[] namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "templates", "titles", titleToAPIForm(element), "tllimit", "max");
 			getParams.put("tlnamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.Link convert(Element element) throws Exception {
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final String title = element.getAttribute("title");
 
 			return new MediaWiki.Link(namespaceID, title);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of links from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						setUpcoming(pageTag.getElementsByTagName("tl"));
 					}
 				}
 
 				processContinuation(xml, "templates");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - PAGES TRANSCLUDING ANOTHER (LIST=EMBEDDEDIN) - - -
 
 	/**
 	 * Retrieves information about all pages transcluding the specified page.
 	 * The return value is an iterator which will return information about each
 	 * of the pages transcluding the specified page on the wiki that this
 	 * <tt>MediaWiki</tt> represents when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param fullName
 	 *            The full name of the page to get transclusions of.
 	 * @param redirect
 	 *            Whether to require a page to be a redirect to be retrieved (
 	 *            <code>Boolean.TRUE</code>), or <em>not</em> to be a redirect (
 	 *            <code>Boolean.FALSE</code>), or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            transcluding the page designated by <code>fullName</code>.
 	 * @param namespaceIDs
 	 *            Whether to require a page to be in one of the specified
 	 *            namespaces to be retrieved, or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            transcluding the page designated by <code>fullName</code>.
 	 * @return an iterator which will return information about each of the pages
 	 *         transcluding the page designated by the given
 	 *         <code>fullName</code> on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.PageDesignation> getPagesTranscluding(final String fullName, final Boolean redirect, final long... namespaceIDs) {
 		return new MediaWiki.TranscludingPagesIterator(fullName, redirect, namespaceIDs);
 	}
 
 	private class TranscludingPagesIterator extends AbstractContinuableQueryIterator<MediaWiki.PageDesignation> {
 		private final Map<String, String> getParams;
 
 		TranscludingPagesIterator(final String fullName, final Boolean redirect, final long[] namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "embeddedin", "eilimit", "max", "eititle", titleToAPIForm(fullName));
 
 			if (redirect != null)
 				getParams.put("eifilterredir", redirect ? "redirects" : "nonredirects");
 			getParams.put("einamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.PageDesignation convert(Element element) throws Exception {
 			final String fullName = element.getAttribute("title");
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final long pageID = Long.parseLong(element.getAttribute("pageid"));
 
 			return new MediaWiki.PageDesignation(pageID, fullName, namespaceID);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of pages from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList embeddedinTags = xml.getElementsByTagName("embeddedin");
 
 				if (embeddedinTags.getLength() > 0) {
 					final Element embeddedinTag = (Element) embeddedinTags.item(0);
 
 					setUpcoming(embeddedinTag.getElementsByTagName("ei"));
 				}
 
 				processContinuation(xml, "embeddedin");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - PAGES USING AN IMAGE (LIST=IMAGEUSAGE) - - -
 
 	/**
 	 * Retrieves information about all pages using the specified image. The
 	 * return value is an iterator which will return information about each of
 	 * the pages using the specified image on the wiki that this
 	 * <tt>MediaWiki</tt> represents when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param fullName
 	 *            The full name of the image to get uses of.
 	 * @param redirect
 	 *            Whether to require a page to be a redirect to be retrieved (
 	 *            <code>Boolean.TRUE</code>), or <em>not</em> to be a redirect (
 	 *            <code>Boolean.FALSE</code>), or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            using the image designated by <code>fullName</code>.
 	 * @param namespaceIDs
 	 *            Whether to require a page to be in one of the specified
 	 *            namespaces to be retrieved, or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            using the image designated by <code>fullName</code>.
 	 * @return an iterator which will return information about each of the pages
 	 *         using the image designated by the given <code>fullName</code> on
 	 *         the wiki that this <tt>MediaWiki</tt> represents when its
 	 *         <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.PageDesignation> getPagesUsingImage(final String fullName, final Boolean redirect, final long... namespaceIDs) {
 		return new MediaWiki.ImageUsageIterator(fullName, redirect, namespaceIDs);
 	}
 
 	private class ImageUsageIterator extends AbstractContinuableQueryIterator<MediaWiki.PageDesignation> {
 		private final Map<String, String> getParams;
 
 		ImageUsageIterator(final String fullName, final Boolean redirect, final long[] namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "imageusage", "iulimit", "max", "iutitle", titleToAPIForm(fullName));
 
 			if (redirect != null)
 				getParams.put("iufilterredir", redirect ? "redirects" : "nonredirects");
 			getParams.put("iunamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.PageDesignation convert(Element element) throws Exception {
 			final String fullName = element.getAttribute("title");
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final long pageID = Long.parseLong(element.getAttribute("pageid"));
 
 			return new MediaWiki.PageDesignation(pageID, fullName, namespaceID);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of pages from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList imageusageTags = xml.getElementsByTagName("imageusage");
 
 				if (imageusageTags.getLength() > 0) {
 					final Element imageusageTag = (Element) imageusageTags.item(0);
 
 					setUpcoming(imageusageTag.getElementsByTagName("iu"));
 				}
 
 				processContinuation(xml, "imageusage");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - PAGES LINKING TO ANOTHER (LIST=BACKLINKS) - - -
 
 	/**
 	 * Retrieves information about all pages linking to the specified page. The
 	 * return value is an iterator which will return information about each of
 	 * the pages linking to the specified page on the wiki that this
 	 * <tt>MediaWiki</tt> represents when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * <p>
 	 * The returned iterator does not return transclusions of the given page,
 	 * not does it return uses of a page in the <tt>File</tt> namespace as an
 	 * image. For these, see <code>getPagesTranscluding</code> and
 	 * <code>getPagesUsingImage</code>.
 	 * 
 	 * @param fullName
 	 *            The full name of the page to get links to.
 	 * @param redirect
 	 *            Whether to require a page to be a redirect to be retrieved (
 	 *            <code>Boolean.TRUE</code>), or <em>not</em> to be a redirect (
 	 *            <code>Boolean.FALSE</code>), or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            linking to the page designated by <code>fullName</code>.
 	 * @param namespaceIDs
 	 *            Whether to require a page to be in one of the specified
 	 *            namespaces to be retrieved, or not to care about that (
 	 *            <code>null</code>). This constraint is applied to the pages
 	 *            linking to the page designated by <code>fullName</code>.
 	 * @return an iterator which will return information about each of the pages
 	 *         linking to the page designated by the given <code>fullName</code>
 	 *         on the wiki that this <tt>MediaWiki</tt> represents when its
 	 *         <code>next</code> method is called
 	 * @see MediaWiki#getPagesTranscluding(String, Boolean, long...)
 	 * @see MediaWiki#getPagesUsingImage(String, Boolean, long...)
 	 */
 	public Iterator<MediaWiki.PageDesignation> getPagesLinkingTo(final String fullName, final Boolean redirect, final long... namespaceIDs) {
 		return new MediaWiki.BacklinkIterator(fullName, redirect, namespaceIDs);
 	}
 
 	private class BacklinkIterator extends AbstractContinuableQueryIterator<MediaWiki.PageDesignation> {
 		private final Map<String, String> getParams;
 
 		BacklinkIterator(final String fullName, final Boolean redirect, final long[] namespaceIDs) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "backlinks", "bllimit", "max", "bltitle", titleToAPIForm(fullName));
 
 			if (redirect != null)
 				getParams.put("blfilterredir", redirect ? "redirects" : "nonredirects");
 			getParams.put("blnamespace", namespacesParameter(namespaceIDs));
 		}
 
 		public MediaWiki.PageDesignation convert(Element blTag) throws Exception {
 			final String fullName = blTag.getAttribute("title");
 			final long namespaceID = Long.parseLong(blTag.getAttribute("ns"));
 			final long pageID = Long.parseLong(blTag.getAttribute("pageid"));
 
 			return new MediaWiki.PageDesignation(pageID, fullName, namespaceID);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of pages from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList backlinksTags = xml.getElementsByTagName("backlinks");
 
 				if (backlinksTags.getLength() > 0) {
 					final Element backlinksTag = (Element) backlinksTags.item(0);
 
 					setUpcoming(backlinksTag.getElementsByTagName("bl"));
 				}
 
 				processContinuation(xml, "backlinks");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - EXTERNAL LINKS ON A PAGE (PROP=EXTLINKS) - - -
 
 	/**
 	 * Retrieves a list of the external Web pages referenced on a page,
 	 * specified by its title. The return value is an iterator which will return
 	 * information about each external link that the page contains when its
 	 * <code>next</code> method is called. The iterator's methods behave as
 	 * follows:
 	 * <ul>
 	 * <li>the <code>hasNext</code> method may throw
 	 * <tt>MediaWiki.IterationException</tt>, an unchecked exception, if it
 	 * encounters an error;
 	 * <li>the <code>hasNext</code> method always returns <code>false</code> if
 	 * the page is missing, or if the page does not reference any external Web
 	 * pages.
 	 * </ul>
 	 * 
 	 * @param title
 	 *            The full name of the page to be examined.
 	 * @return an iterator which will return information about each external
 	 *         link that the page contains when its <code>next</code> method is
 	 *         called
 	 */
 	public Iterator<String> getExternalLinks(final String title) {
 		return new MediaWiki.ExternalLinkIterator(title);
 	}
 
 	private class ExternalLinkIterator extends AbstractContinuableQueryIterator<String> {
 		private final Map<String, String> getParams;
 
 		ExternalLinkIterator(final String element) {
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "extlinks", "titles", titleToAPIForm(element), "ellimit", "max");
 		}
 
 		public String convert(Element element) throws Exception {
 			return element.getTextContent();
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of links from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						setUpcoming(pageTag.getElementsByTagName("el"));
 					}
 				}
 
 				processContinuation(xml, "extlinks");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - CATEGORY INFORMATION (PROP=CATEGORYINFO) - - -
 
 	/**
 	 * Retrieves information about one or more categories, specified by their
 	 * titles. The return value is an iterator which will return information
 	 * about each of the specified categories when its <code>next</code> method
 	 * is called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>return <code>null</code>, if it encounters a missing page, a page
 	 * that is not a category or a category that is empty;
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param titles
 	 *            The full name(s) of the page(s) to be examined.
 	 * @return an iterator which will return information about each of the
 	 *         specified categories when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Category> getCategoryInformation(final String... titles) {
 		return new MediaWiki.CategoryInfoIterator(titles);
 	}
 
 	private class CategoryInfoIterator extends AbstractReadOnlyIterator<MediaWiki.Category> {
 		/**
 		 * The titles to get category information about.
 		 */
 		private final String[] elements;
 
 		private int i;
 
 		private final Map<String, String> getParams;
 
 		CategoryInfoIterator(final String[] elements) {
 			this.elements = elements;
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "categoryinfo");
 
 			i = -1;
 		}
 
 		public synchronized boolean hasNext() {
 			return i + 1 < (elements.length);
 		}
 
 		public synchronized MediaWiki.Category next() throws MediaWiki.IterationException {
 			i++;
 
 			getParams.put("titles", titleToAPIForm(elements[i]));
 
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (!pageTag.hasAttribute("missing")) {
 						final NodeList categoryinfoTags = pageTag.getElementsByTagName("categoryinfo");
 
 						if (categoryinfoTags.getLength() > 0) {
 							final String fullName = pageTag.getAttribute("title");
 
 							final Element categoryinfoTag = (Element) categoryinfoTags.item(0);
 
 							final long entries = Long.parseLong(categoryinfoTag.getAttribute("size"));
 							final long pages = Long.parseLong(categoryinfoTag.getAttribute("pages"));
 							final long files = Long.parseLong(categoryinfoTag.getAttribute("files"));
 							final long subcategories = Long.parseLong(categoryinfoTag.getAttribute("subcats"));
 
 							return new MediaWiki.Category(fullName, entries, pages, files, subcategories);
 						}
 					}
 				}
 
 				return null;
 			} catch (final IOException ioe) {
 				throw new MediaWiki.IterationException(ioe);
 			} catch (MediaWiki.IterationException ie) {
 				throw ie;
 			} catch (MediaWiki.MediaWikiException mwe) {
 				throw new MediaWiki.IterationException(mwe);
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - LISTS OF ALL OF SOMETHING - - -
 
 	/**
 	 * Retrieves information about all categories matching all of the filters
 	 * specified. The return value is an iterator which will return information
 	 * about each of the matching categories on the wiki that this
 	 * <tt>MediaWiki</tt> represents when its <code>next</code> method is
 	 * called. The iterator's <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * <p>
 	 * In MediaWiki versions before 1.18, not all of the filters are supported,
 	 * so the iterator may retrieve all categories regardless of the values
 	 * specified for all the parameters.
 	 * 
 	 * @param first
 	 *            The base name of the first category to retrieve. This
 	 *            parameter is <code>null</code> to avoid using this constraint.
 	 * @param last
 	 *            The base name of the last category to retrieve. This parameter
 	 *            is <code>null</code> to avoid using this constraint.
 	 * @param prefix
 	 *            The first few characters of the base name of all categories to
 	 *            be returned. This parameter is <code>null</code> to avoid
 	 *            using this constraint.
 	 * @param ascendingOrder
 	 *            <code>true</code> if the order to enumerate the categories in
 	 *            is lexicographical; <code>false</code> if the order is reverse
 	 *            lexicographical. This also affects the order of
 	 *            <code>start</code> and <code>end</code>.
 	 * @param minimumEntries
 	 *            The minimum number of entries that a category must contain in
 	 *            order to be returned. This parameter is <code>null</code> to
 	 *            avoid using this constraint.
 	 * @param maximumEntries
 	 *            The maximum number of entries that a category must contain in
 	 *            order to be returned. This parameter is <code>null</code> to
 	 *            avoid using this constraint.
 	 * @return an iterator which will return information about each of the
 	 *         matching categories on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.Category> getAllCategories(final String first, final String last, final String prefix, final boolean ascendingOrder, final Long minimumEntries, final Long maximumEntries) {
 		return new MediaWiki.AllCategoriesIterator(first, last, prefix, ascendingOrder, minimumEntries, maximumEntries);
 	}
 
 	private class AllCategoriesIterator extends AbstractContinuableQueryIterator<MediaWiki.Category> {
 		private final Map<String, String> getParams;
 
 		AllCategoriesIterator(final String first, final String last, final String prefix, final boolean ascendingOrder, final Long minimumEntries, final Long maximumEntries) {
 			super("acfrom", first /* can also be null */);
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "allcategories", "aclimit", "max", "acprop", "size", "acdir", ascendingOrder ? "ascending" : "descending");
 
 			if (last != null && last.length() > 0)
 				getParams.put("acto", last);
 			if (prefix != null && prefix.length() > 0)
 				getParams.put("acprefix", prefix);
 
 			if (minimumEntries != null)
 				getParams.put("acmin", minimumEntries.toString());
 			if (maximumEntries != null)
 				getParams.put("acmax", maximumEntries.toString());
 		}
 
 		public MediaWiki.Category convert(Element element) throws Exception {
 			final long entries = Long.parseLong(element.getAttribute("size"));
 			final long pages = Long.parseLong(element.getAttribute("pages"));
 			final long files = Long.parseLong(element.getAttribute("files"));
 			final long subcategories = Long.parseLong(element.getAttribute("subcats"));
 
 			final String name = element.getTextContent();
 
 			return new MediaWiki.Category(name, entries, pages, files, subcategories);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of categories from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList allcategoriesTags = xml.getElementsByTagName("allcategories");
 
 				if (allcategoriesTags.getLength() > 0) {
 					final Element allcategoriesTag = (Element) allcategoriesTags.item(0);
 
 					setUpcoming(allcategoriesTag.getElementsByTagName("c"));
 				}
 
 				processContinuation(xml, "allcategories");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Retrieves information about all images matching all of the filters
 	 * specified. The return value is an iterator which will return information
 	 * about each of the matching image on the wiki that this <tt>MediaWiki</tt>
 	 * represents when its <code>next</code> method is called. The iterator's
 	 * <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param first
 	 *            The base name of the first image to retrieve. This parameter
 	 *            is <code>null</code> to avoid using this constraint.
 	 * @param prefix
 	 *            The first few characters of the base name of all images to be
 	 *            returned. This parameter is <code>null</code> to avoid using
 	 *            this constraint.
 	 * @param ascendingOrder
 	 *            <code>true</code> if the order to enumerate the images in is
 	 *            lexicographical; <code>false</code> if the order is reverse
 	 *            lexicographical. This also affects the order of
 	 *            <code>start</code> and <code>end</code>.
 	 * @param minimumLength
 	 *            The minimum length, in bytes, that an image must have in order
 	 *            to be returned. This parameter is <code>null</code> to avoid
 	 *            using this constraint.
 	 * @param maximumLength
 	 *            The maximum length, in bytes, that an image must have in order
 	 *            to be returned. This parameter is <code>null</code> to avoid
 	 *            using this constraint.
 	 * @param sha1
 	 *            The SHA-1 hash that an image must have in order to be
 	 *            returned. This parameter is a hexadecimal string, or
 	 *            <code>null</code> to avoid using this constraint.
 	 * @return an iterator which will return information about each of the
 	 *         matching images on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.ImageRevision> getAllImages(final String first, final String prefix, final boolean ascendingOrder, final Long minimumLength, final Long maximumLength, final String sha1) {
 		return new MediaWiki.AllImagesIterator(first, prefix, ascendingOrder, minimumLength, maximumLength, sha1);
 	}
 
 	private class AllImagesIterator extends AbstractContinuableQueryIterator<MediaWiki.ImageRevision> {
 		private final Map<String, String> getParams;
 
 		AllImagesIterator(final String first, final String prefix, final boolean ascendingOrder, final Long minimumLength, final Long maximumLength, final String sha1) {
 			super("aifrom", first /* can also be null */);
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "allimages", "ailimit", "max", "aiprop", "timestamp|user|comment|url|size|sha1|mime", "aidir", ascendingOrder ? "ascending" : "descending");
 
 			if (prefix != null && prefix.length() > 0)
 				getParams.put("aiprefix", prefix);
 
 			if (minimumLength != null)
 				getParams.put("aiminsize", minimumLength.toString());
 			if (maximumLength != null)
 				getParams.put("aimaxsize", maximumLength.toString());
 			getParams.put("aisha1", sha1);
 		}
 
 		public MediaWiki.ImageRevision convert(Element imgTag) throws Exception {
 			final String baseName = imgTag.getAttribute("name");
 			final Date timestamp = iso8601TimestampParser.parse(imgTag.getAttribute("timestamp"));
 			final String userName = imgTag.getAttribute("user");
 			final long length = Long.parseLong(imgTag.getAttribute("size"));
 			final long width = Long.parseLong(imgTag.getAttribute("width"));
 			final long height = Long.parseLong(imgTag.getAttribute("height"));
 			final String url = imgTag.getAttribute("url");
 			final String comment = imgTag.getAttribute("comment");
 			final String sha1hash = imgTag.getAttribute("sha1");
 			final String mimeType = imgTag.getAttribute("mime");
 
 			return new MediaWiki.ImageRevision(getNamespaces().getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(baseName), timestamp, userName, length, width, height, url, comment, sha1hash, mimeType);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of images from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList allimagesTags = xml.getElementsByTagName("allimages");
 
 				if (allimagesTags.getLength() > 0) {
 					final Element allimagesTag = (Element) allimagesTags.item(0);
 
 					setUpcoming(allimagesTag.getElementsByTagName("img"));
 				}
 
 				processContinuation(xml, "allimages");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Retrieves information about all pages matching all of the filters
 	 * specified. The return value is an iterator which will return information
 	 * about each of the matching pages on the wiki that this <tt>MediaWiki</tt>
 	 * represents when its <code>next</code> method is called. The iterator's
 	 * <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param first
 	 *            The base name of the first page to retrieve. This parameter is
 	 *            <code>null</code> to avoid using this constraint.
 	 * @param prefix
 	 *            The first few characters of the base name of all pages to be
 	 *            returned, like in <tt>Special:PrefixIndex</tt> with the
 	 *            difference that more filters can be applied using the other
 	 *            parameters. This parameter is <code>null</code> to avoid using
 	 *            this constraint.
 	 * @param namespaceID
 	 *            The ID of the namespace to enumerate pages from. This is
 	 *            typically one of the namespace identifiers in
 	 *            <tt>MediaWiki.StandardNamespace</tt>.
 	 * @param ascendingOrder
 	 *            <code>true</code> if the order to enumerate the pages in is
 	 *            lexicographical; <code>false</code> if the order is reverse
 	 *            lexicographical. This also affects the order of
 	 *            <code>start</code> and <code>end</code>.
 	 * @param minimumLength
 	 *            The minimum length, in bytes, that a page must have in order
 	 *            to be returned. This parameter is <code>null</code> to avoid
 	 *            using this constraint.
 	 * @param maximumLength
 	 *            The maximum length, in bytes, that a page must have in order
 	 *            to be returned. This parameter is <code>null</code> to avoid
 	 *            using this constraint.
 	 * @param redirect
 	 *            Whether to require a page to be a redirect to be retrieved (
 	 *            <code>Boolean.TRUE</code>), or <em>not</em> to be a redirect (
 	 *            <code>Boolean.FALSE</code>), or not to care about that (
 	 *            <code>null</code>).
 	 * @param languageLinks
 	 *            Whether to require a page to have language links to be
 	 *            retrieved (<code>Boolean.TRUE</code>), or <em>not</em> to be
 	 *            have language links (<code>Boolean.FALSE</code>), or not to
 	 *            care about that (<code>null</code>).
 	 * @param protectionAction
 	 *            An action that must be protected on the pages to be retrieved.
 	 *            This parameter is <code>null</code> to avoid using this
 	 *            constraint, and when not <code>null</code>, is usually one of
 	 *            the actions in <tt>MediaWiki.ProtectionAction</tt>.
 	 * @param protectionType
 	 *            When <code>protectionAction</code> is not <code>null</code>,
 	 *            the action must additionally be protected from users not in
 	 *            the group specified in this parameter (for example, if
 	 *            <code>protectionAction.equals("move") && protectionType.equals("sysop")</code>
 	 *            , then matching pages are move-protected except for sysops).
 	 * @return an iterator which will return information about each of the
 	 *         matching pages on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.PageDesignation> getAllPages(final String first, final String prefix, final long namespaceID, final boolean ascendingOrder, final Long minimumLength, final Long maximumLength, final Boolean redirect, final Boolean languageLinks, final String protectionAction, final String protectionType) {
 		return new MediaWiki.AllPagesIterator(first, prefix, namespaceID, ascendingOrder, minimumLength, maximumLength, redirect, languageLinks, protectionAction, protectionType);
 	}
 
 	private class AllPagesIterator extends AbstractContinuableQueryIterator<MediaWiki.PageDesignation> {
 		private final Map<String, String> getParams;
 
 		AllPagesIterator(final String first, final String prefix, final long namespaceID, final boolean ascendingOrder, final Long minimumLength, final Long maximumLength, final Boolean redirect, final Boolean languageLinks, final String protectionAction, final String protectionType) {
 			super("apfrom", first /* can also be null */);
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "allpages", "aplimit", "max", "apnamespace", Long.toString(namespaceID), "apdir", ascendingOrder ? "ascending" : "descending");
 
 			if (prefix != null && prefix.length() > 0)
 				getParams.put("apprefix", prefix);
 
 			if (minimumLength != null)
 				getParams.put("apminsize", minimumLength.toString());
 			if (maximumLength != null)
 				getParams.put("apmaxsize", maximumLength.toString());
 			if (redirect != null)
 				getParams.put("apfilterredir", redirect ? "redirects" : "nonredirects");
 			if (languageLinks != null)
 				getParams.put("apfilterlanglinks", languageLinks ? "withlanglinks" : "withoutlanglinks");
 			if (protectionAction != null) {
 				getParams.put("apprtype", protectionAction);
 				getParams.put("apprlevel", protectionType);
 			}
 		}
 
 		public MediaWiki.PageDesignation convert(Element element) throws Exception {
 			final String fullName = element.getAttribute("title");
 			final long namespaceID = Long.parseLong(element.getAttribute("ns"));
 			final long pageID = Long.parseLong(element.getAttribute("pageid"));
 
 			return new MediaWiki.PageDesignation(pageID, fullName, namespaceID);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of pages from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList allpagesTags = xml.getElementsByTagName("allpages");
 
 				if (allpagesTags.getLength() > 0) {
 					final Element allpagesTag = (Element) allpagesTags.item(0);
 
 					setUpcoming(allpagesTag.getElementsByTagName("p"));
 				}
 
 				processContinuation(xml, "allpages");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Retrieves information about the specified user(s), specified by name. The
 	 * return value is an iterator which will return information about each of
 	 * the users when its <code>next</code> method is called. The iterator's
 	 * <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * 
 	 * @param users
 	 *            The names of the users to retrieve information about.
 	 * @return an iterator which will return information about each of the
 	 *         matching users on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.User> getUserInformation(final String... users) {
 		return new MediaWiki.SingleUserIterator(users);
 	}
 
 	private class SingleUserIterator extends AbstractReadOnlyIterator<MediaWiki.User> {
 		/**
 		 * The names of the users to get information about.
 		 */
 		private final String[] elements;
 
 		private int i;
 
 		private final Map<String, String> getParams;
 
 		SingleUserIterator(final String[] elements) {
 			this.elements = elements;
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "users", "usprop", "blockinfo|editcount|groups|rights|registration");
 
 			i = -1;
 		}
 
 		public synchronized boolean hasNext() {
 			return i + 1 < elements.length;
 		}
 
 		public synchronized MediaWiki.User next() throws MediaWiki.IterationException {
 			i++;
 
 			getParams.put("ususers", titleToAPIForm(elements[i]));
 
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList userTags = xml.getElementsByTagName("user");
 
 				if (userTags.getLength() > 0) {
 					final Element userTag = (Element) userTags.item(0);
 
 					if (userTag.hasAttribute("missing"))
 						return new MediaWiki.User(true /*- missing */, null, Collections.<String> emptySet(), Collections.<String> emptySet(), 0, null, null, null);
 
 					final String name = userTag.getAttribute("name");
 
 					final TreeSet<String> groups = new TreeSet<String>();
 
 					NodeList gTags = userTag.getElementsByTagName("g");
 
 					for (int j = 0; j < gTags.getLength(); j++) {
 						Element gTag = (Element) gTags.item(j);
 
 						groups.add(gTag.getTextContent());
 					}
 
 					final TreeSet<String> rights = new TreeSet<String>();
 
 					NodeList rTags = userTag.getElementsByTagName("r");
 
 					for (int j = 0; j < rTags.getLength(); j++) {
 						Element rTag = (Element) rTags.item(j);
 
 						rights.add(rTag.getTextContent());
 					}
 
 					final long editCount = Long.parseLong(userTag.getAttribute("editcount"));
 
 					final String blockingUser = userTag.hasAttribute("blockedby") ? userTag.getAttribute("blockedby") : null;
 					final String blockReason = userTag.hasAttribute("blockreason") ? userTag.getAttribute("blockreason") : null;
 					Date registration = null;
 					try {
 						registration = userTag.hasAttribute("registration") ? timestampToDate(userTag.getAttribute("registration")) : null;
 					} catch (ParseException e) {
 						// information unavailable; don't care
 					}
 
 					return new MediaWiki.User(false /*- (not) missing */, name, groups, rights, editCount, blockingUser, blockReason, registration);
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <user> tag not found");
 			} catch (final IOException ioe) {
 				throw new MediaWiki.IterationException(ioe);
 			} catch (MediaWiki.IterationException ie) {
 				throw ie;
 			} catch (MediaWiki.MediaWikiException mwe) {
 				throw new MediaWiki.IterationException(mwe);
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Retrieves information about all users matching all of the filters
 	 * specified. The return value is an iterator which will return information
 	 * about each of the matching users on the wiki that this <tt>MediaWiki</tt>
 	 * represents when its <code>next</code> method is called. The iterator's
 	 * <code>next</code> method may:
 	 * <ul>
 	 * <li>throw <tt>MediaWiki.IterationException</tt>, an unchecked exception,
 	 * if it encounters an error.
 	 * </ul>
 	 * <p>
 	 * In MediaWiki versions prior to 1.18, the filters may not be supported,
 	 * and the iteration may stop after the first batch of 500 (or 5000 for
 	 * accounts that have the bot flag). Even in MediaWiki 1.18, the iteration
 	 * may stop spuriously.
 	 * 
 	 * @param first
 	 *            The name of the first user to retrieve. This parameter is
 	 *            <code>null</code> to avoid using this constraint.
 	 * @param prefix
 	 *            The first few characters of the name of all users to be
 	 *            returned. This parameter is <code>null</code> to avoid using
 	 *            this constraint.
 	 * @param group
 	 *            The group that a user must be in to be retrieved. This
 	 *            parameter is <code>null</code> to avoid using this constraint.
 	 * @return an iterator which will return information about each of the
 	 *         matching users on the wiki that this <tt>MediaWiki</tt>
 	 *         represents when its <code>next</code> method is called
 	 */
 	public Iterator<MediaWiki.User> getAllUsers(final String first, final String prefix, final String group) {
 		return new MediaWiki.AllUsersIterator(first, prefix, group);
 	}
 
 	private class AllUsersIterator extends AbstractContinuableQueryIterator<MediaWiki.User> {
 		private final Map<String, String> getParams;
 
 		AllUsersIterator(final String first, final String prefix, final String group) {
 			super("aufrom", first /* can also be null */);
 
 			getParams = paramValuesToMap("action", "query", "format", "xml", "list", "allusers", "aulimit", "max", "auprop", "blockinfo|editcount|groups|rights|registration");
 
 			if (prefix != null && prefix.length() > 0)
 				getParams.put("auprefix", prefix);
 		}
 
 		public MediaWiki.User convert(Element uTag) throws Exception {
 			final String name = uTag.getAttribute("name");
 
 			final TreeSet<String> groups = new TreeSet<String>();
 
 			NodeList gTags = uTag.getElementsByTagName("g");
 
 			for (int j = 0; j < gTags.getLength(); j++) {
 				Element gTag = (Element) gTags.item(j);
 
 				groups.add(gTag.getTextContent());
 			}
 
 			final TreeSet<String> rights = new TreeSet<String>();
 
 			NodeList rTags = uTag.getElementsByTagName("r");
 
 			for (int j = 0; j < rTags.getLength(); j++) {
 				Element rTag = (Element) rTags.item(j);
 
 				rights.add(rTag.getTextContent());
 			}
 
 			final long editCount = Long.parseLong(uTag.getAttribute("editcount"));
 
 			final String blockingUser = uTag.hasAttribute("blockedby") ? uTag.getAttribute("blockedby") : null;
 			final String blockReason = uTag.hasAttribute("blockreason") ? uTag.getAttribute("blockreason") : null;
 			Date registration = null;
 			try {
 				registration = uTag.hasAttribute("registration") ? timestampToDate(uTag.getAttribute("registration")) : null;
 			} catch (ParseException e) {
 				// information unavailable; don't care
 			}
 
 			return new MediaWiki.User(false /*- (not) missing */, name, groups, rights, editCount, blockingUser, blockReason, registration);
 		}
 
 		protected synchronized void cacheUpcoming() throws Exception {
 			// Get the next page of users from the API.
 			final Map<String, String> pageGetParams = new TreeMap<String, String>(getParams);
 			pageGetParams.put(getContinuationName(), getContinuation());
 
 			final String url = createApiGetUrl(pageGetParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList allusersTags = xml.getElementsByTagName("allusers");
 
 				if (allusersTags.getLength() > 0) {
 					final Element allusersTag = (Element) allusersTags.item(0);
 
 					setUpcoming(allusersTag.getElementsByTagName("u"));
 				}
 
 				processContinuation(xml, "allusers");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 	}
 
 	// - - - PURGE - - -
 
 	/**
 	 * Purges one or more pages on the wiki represented by this
 	 * <tt>MediaWiki</tt>.
 	 * <p>
 	 * Silently ignores failures to purge individual pages, but not the lack of
 	 * a reply to the purge request.
 	 * 
 	 * @param pages
 	 *            The full name(s) of the page(s) to purge.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki purge(String... fullPageNames) throws IOException, MediaWiki.MediaWikiException {
 		if (fullPageNames.length == 0)
 			return this;
 
 		Map<String, String> getParams = paramValuesToMap("action", "purge", "format", "xml");
 
 		StringBuilder titles = new StringBuilder();
 		for (String title : fullPageNames) {
 			if (titles.length() > 0)
 				titles.append('|');
 			titles.append(title);
 		}
 
 		Map<String, String> postParams = paramValuesToMap("titles", titles.toString());
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList purgeTags = xml.getElementsByTagName("purge");
 
 			if (purgeTags.getLength() > 0) {
 				return this;
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <purge> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - EXPAND TEMPLATES IN WIKITEXT (ACTION=EXPANDTEMPLATES) - - -
 
 	/**
 	 * Expands templates in wikitext on the wiki represented by this
 	 * <tt>MediaWiki</tt>.
 	 * <p>
 	 * Template invocations are generally between <tt>{{</tt> and <tt>}}</tt>.
 	 * <p>
 	 * This version of the <code>expandTemplatesInWikitext</code> method may not
 	 * resolve <i>magic words</i> such as <tt>{{PAGENAME}}</tt> and
 	 * <tt>{{FULLPAGENAME}}</tt> correctly. According to the MediaWiki API
 	 * documentation, it acts as if the page were named <code>"API"</code>.
 	 * 
 	 * @param wikitext
 	 *            Wikitext to be expanded, possibly containing template
 	 *            invocations.
 	 * @return the wikitext with templates expanded
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 * @see #expandTemplatesInWikitext(String, String)
 	 */
 	public String expandTemplatesInWikitext(String wikitext) throws IOException, MediaWiki.MediaWikiException {
 		return expandTemplatesInWikitext(wikitext, null);
 	}
 
 	/**
 	 * Expands templates in wikitext on the wiki represented by this
 	 * <tt>MediaWiki</tt> as if the wikitext were on the given page.
 	 * <p>
 	 * Template invocations are generally between <tt>{{</tt> and <tt>}}</tt>.
 	 * <p>
 	 * This version of the <code>expandTemplatesInWikitext</code> method
 	 * resolves <i>magic words</i> such as <tt>{{PAGENAME}}</tt> and
 	 * <tt>{{FULLPAGENAME}}</tt> correctly.
 	 * 
 	 * @param wikitext
 	 *            Wikitext to be expanded, possibly containing template
 	 *            invocations.
 	 * @param fullPageName
 	 *            Act as if the wikitext were on a page with this full name. May
 	 *            be <code>null</code>.
 	 * @return the wikitext with templates expanded
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public String expandTemplatesInWikitext(String wikitext, String fullPageName) throws IOException, MediaWiki.MediaWikiException {
 		if (wikitext.length() <= 4) // {{}}
 			return wikitext; // Cannot contain any templates
 
 		Map<String, String> getParams = paramValuesToMap("action", "expandtemplates", "format", "xml", "text", wikitext, "title", fullPageName);
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList expandtemplatesTags = xml.getElementsByTagName("expandtemplates");
 
 			if (expandtemplatesTags.getLength() > 0) {
 				Element expandtemplatesTag = (Element) expandtemplatesTags.item(0);
 
 				return expandtemplatesTag.getTextContent();
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <expandtemplates> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - PARSE WIKITEXT (ACTION=PARSE) - - -
 
 	/**
 	 * Parses wikitext to HTML on the wiki represented by this
 	 * <tt>MediaWiki</tt>.
 	 * <p>
 	 * This method resolves <i>magic words</i> such as <tt>{{PAGENAME}}</tt> and
 	 * <tt>{{FULLPAGENAME}}</tt> correctly.
 	 * 
 	 * @param wikitext
 	 *            Wikitext to be parsed.
 	 * @param fullPageName
 	 *            Act as if the wikitext were on a page with this full name. May
 	 *            be <code>null</code>, in which case, according to the
 	 *            MediaWiki API documentation, it acts as if the page were named
 	 *            <code>"API"</code>.
 	 * @param preSaveTransform
 	 *            If this parameter is <code>true</code>, requests a pre-save
 	 *            transform, which expands <tt>{{subst:}}</tt> template
 	 *            invocations, signatures and timestamps (<tt>~~~</tt>,
 	 *            <tt>~~~~</tt>, <tt>~~~~~</tt>).
 	 * @param languageCode
 	 *            Code of the language used to parse the page. Usually not
 	 *            needed, except on pages that use the value of
 	 *            <tt>{{int:Lang}}</tt>. May be <code>null</code>.
 	 * @return an object representing the result of parsing the given
 	 *         <code>wikitext</code>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki.ParseResult parseWikitext(String wikitext, String fullPageName, boolean preSaveTransform, String languageCode) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "parse", "format", "xml", "text", wikitext, "title", fullPageName, "uselang", languageCode);
 		if (preSaveTransform)
 			getParams.put("pst", "true");
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList parseTags = xml.getElementsByTagName("parse");
 
 			if (parseTags.getLength() > 0) {
 				Element parseTag = (Element) parseTags.item(0);
 
 				String text = null;
 				List<MediaWiki.InterlanguageLink> langLinks = new ArrayList<MediaWiki.InterlanguageLink>();
 				List<MediaWiki.CategoryMembership> categoryMemberships = new ArrayList<MediaWiki.CategoryMembership>();
 				List<MediaWiki.Link> internalLinks = new ArrayList<MediaWiki.Link>();
 				List<MediaWiki.Link> transclusionsInWikitext = new ArrayList<MediaWiki.Link>();
 				List<MediaWiki.Link> images = new ArrayList<MediaWiki.Link>();
 				List<String> externalLinks = new ArrayList<String>();
 
 				NodeList textTags = parseTag.getElementsByTagName("text");
 				if (textTags.getLength() > 0) {
 					Element textTag = (Element) textTags.item(0);
 
 					text = textTag.getTextContent();
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <text> tag not present");
 
 				NodeList langlinksTags = parseTag.getElementsByTagName("langlinks");
 				if (langlinksTags.getLength() > 0) {
 					Element langlinksTag = (Element) langlinksTags.item(0);
 
 					NodeList llTags = langlinksTag.getElementsByTagName("ll");
 
 					for (int i = 0; i < llTags.getLength(); i++) {
 						Element llTag = (Element) llTags.item(i);
 
 						langLinks.add(new MediaWiki.InterlanguageLink(llTag.getAttribute("lang"), llTag.getTextContent()));
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <langlinks> tag not present");
 
 				NodeList categoriesTags = parseTag.getElementsByTagName("categories");
 				if (categoriesTags.getLength() > 0) {
 					Element categoriesTag = (Element) categoriesTags.item(0);
 
 					NodeList clTags = categoriesTag.getElementsByTagName("cl");
 
 					for (int i = 0; i < clTags.getLength(); i++) {
 						Element clTag = (Element) clTags.item(i);
 
 						categoryMemberships.add(new MediaWiki.CategoryMembership(clTag.getTextContent(), clTag.getAttribute("sortkey")));
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <categories> tag not present");
 
 				NodeList linksTags = parseTag.getElementsByTagName("links");
 				if (linksTags.getLength() > 0) {
 					Element linksTag = (Element) linksTags.item(0);
 
 					NodeList plTags = linksTag.getElementsByTagName("pl");
 
 					for (int i = 0; i < plTags.getLength(); i++) {
 						Element plTag = (Element) plTags.item(i);
 
 						internalLinks.add(new Link(Long.parseLong(plTag.getAttribute("ns")), plTag.getTextContent()));
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <links> tag not present");
 
 				NodeList templatesTags = parseTag.getElementsByTagName("templates");
 				if (templatesTags.getLength() > 0) {
 					Element templatesTag = (Element) templatesTags.item(0);
 
 					NodeList tlTags = templatesTag.getElementsByTagName("tl");
 
 					for (int i = 0; i < tlTags.getLength(); i++) {
 						Element tlTag = (Element) tlTags.item(i);
 
 						transclusionsInWikitext.add(new Link(Long.parseLong(tlTag.getAttribute("ns")), tlTag.getTextContent()));
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <templates> tag not present");
 
 				NodeList imagesTags = parseTag.getElementsByTagName("images");
 				if (imagesTags.getLength() > 0) {
 					Element imagesTag = (Element) imagesTags.item(0);
 
 					NodeList imgTags = imagesTag.getElementsByTagName("img");
 
 					for (int i = 0; i < imgTags.getLength(); i++) {
 						Element imgTag = (Element) imgTags.item(i);
 
 						images.add(new MediaWiki.Link(MediaWiki.StandardNamespace.FILE, getNamespaces().getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(imgTag.getTextContent())));
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <images> tag not present");
 
 				NodeList externallinksTags = parseTag.getElementsByTagName("externallinks");
 				if (externallinksTags.getLength() > 0) {
 					Element externallinksTag = (Element) externallinksTags.item(0);
 
 					NodeList elTags = externallinksTag.getElementsByTagName("el");
 
 					for (int i = 0; i < elTags.getLength(); i++) {
 						Element elTag = (Element) elTags.item(i);
 
 						externalLinks.add(elTag.getTextContent());
 					}
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <externallinks> tag not present");
 
 				// TODO Sections in wikitext
 
 				return new MediaWiki.ParseResult(text, langLinks, categoryMemberships, internalLinks, transclusionsInWikitext, images, externalLinks);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <parse> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - EDIT - - -
 
 	/**
 	 * Starts an edit to the given page, specified by its full name. The
 	 * returned token contains informaton used to detect conflicts when the edit
 	 * is ended.
 	 * 
 	 * @param fullName
 	 *            The full name, including the namespace, of the page to start
 	 *            editing.
 	 * @return a <tt>MediaWiki.EditToken</tt> containing informaton used to
 	 *         detect conflicts when the edit is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.EditToken startEdit(final String fullName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "intoken", "edit", "titles", titleToAPIForm(fullName));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList pageTags = xml.getElementsByTagName("page");
 
 			if (pageTags.getLength() > 0) {
 				Element pageTag = (Element) pageTags.item(0);
 
 				if (!pageTag.hasAttribute("edittoken"))
 					throw new MediaWiki.PermissionException("edit");
 
 				final Date lastRevision = pageTag.hasAttribute("touched") ? iso8601TimestampParser.parse(pageTag.getAttribute("touched")) : null;
 				final Date start = iso8601TimestampParser.parse(pageTag.getAttribute("starttimestamp"));
 				final String token = pageTag.getAttribute("edittoken");
 
 				return new MediaWiki.EditToken(fullName, lastRevision, start, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 		} catch (ParseException pe) {
 			throw new MediaWiki.MediaWikiException(pe);
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code> by
 	 * replacing the entire page it specifies with the given text.
 	 * <p>
 	 * This method requires the page to exist on the wiki represented by this
 	 * <tt>MediaWiki</tt> prior to the call. If it doesn't,
 	 * <tt>MediaWiki.MissingPageException</tt> is thrown. If this behavior is
 	 * not desired, please use <code>createOrReplacePage</code>.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param newText
 	 *            The text to be contained by the page after the edit.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki replacePage(final MediaWiki.EditToken editToken, String newText, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		return editPage(editToken, null, true, newText, editSummary, bot, minor);
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code> by creating
 	 * the page it specifies and adding the given text to it.
 	 * <p>
 	 * This method requires the page not to exist on the wiki represented by
 	 * this <tt>MediaWiki</tt> prior to the call. If it does,
 	 * <tt>MediaWiki.ExistingPageException</tt> is thrown. If this behavior is
 	 * not desired, please use <code>createOrReplacePage</code>.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param newText
 	 *            The text to be contained by the page after the edit.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki createPage(final MediaWiki.EditToken editToken, String newText, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		return editPage(editToken, null, false, newText, editSummary, bot, minor);
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code> by either
 	 * creating or replacing the page it specifies with the given text.
 	 * <p>
 	 * This method creates the page if it doesn't exist, or replaces its
 	 * contents if it does.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param newText
 	 *            The text to be contained by the page after the edit.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki createOrReplacePage(final MediaWiki.EditToken editToken, String newText, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		return editPage(editToken, null, null, newText, editSummary, bot, minor);
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code> by
 	 * replacing one of the sections of the page it specifies with the given
 	 * text.
 	 * <p>
 	 * This method requires the page to exist on the wiki represented by this
 	 * <tt>MediaWiki</tt> prior to the call. If it doesn't,
 	 * <tt>MediaWiki.MissingPageException</tt> is thrown.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param sectionIndex
 	 *            The index of the section to replace. <code>0</code> for the
 	 *            introduction.
 	 * @param newText
 	 *            The text to be contained by the section after the edit.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki replacePageSection(final MediaWiki.EditToken editToken, int sectionIndex, String newText, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		return editPage(editToken, Integer.toString(sectionIndex), true, newText, editSummary, bot, minor);
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code> by creating
 	 * a section at the bottom of the page it specifies.
 	 * <p>
 	 * This method creates the page if it doesn't exist, or adds to its contents
 	 * if it does.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param heading
 	 *            The heading of the section to be added. This is also used to
 	 *            create an automatic edit summary to describe the creation of
 	 *            the section.
 	 * @param text
 	 *            The text to be contained by the section.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki createPageSection(final MediaWiki.EditToken editToken, String heading, String text, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		return editPage(editToken, "new", null, text, heading, bot, minor);
 	}
 
 	/**
 	 * Ends an edit embodied by the specified <code>editToken</code>.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to edit and
 	 *            containing information to detect conflicts.
 	 * @param section
 	 *            <code>"new"</code> to create a new section; the string
 	 *            corresponding to a number to edit that section index;
 	 *            <code>null</code> to replace the entire page.
 	 * @param requireExist
 	 *            <code>true</code> to require that the page already exist;
 	 *            <code>false</code> to require that the page not exist;
 	 *            <code>null</code> to require nothing.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit, or, if
 	 *            <code>section</code> is <code>"new"</code>, the heading of the
 	 *            section to be added.
 	 * @param text
 	 *            The text to be contained by the section.
 	 * @param bot
 	 *            Whether to mark the edit as made by a bot ( <code>true</code>)
 	 *            or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit as minor (<code>true</code>) or not (
 	 *            <code>false</code>) or to use the value in
 	 *            <tt>Special:Preferences</tt> for the currently-logged in user
 	 *            of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	protected MediaWiki editPage(final MediaWiki.EditToken editToken, String section, Boolean requireExist, String text, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "edit", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", editToken.getFullPageName(), "text", text, "token", editToken.getTokenText(), "starttimestamp", iso8601TimestampParser.format(editToken.getStartTime()), "summary", editSummary, "section", section);
 		if (bot)
 			postParams.put("bot", "true");
 		if (minor != null)
 			postParams.put(minor ? "minor" : "notminor", "true");
 		if (requireExist != null)
 			postParams.put(requireExist ? "nocreate" : "createonly", "true");
 		if (editToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(editToken.getLastRevisionTime()));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList editTags = xml.getElementsByTagName("edit");
 
 			if (editTags.getLength() > 0) {
 				Element editTag = (Element) editTags.item(0);
 
 				if (editTag.hasAttribute("result")) {
 					if (editTag.getAttribute("result").equals("Success"))
 						return this;
 					else
 						throw new MediaWiki.ActionFailureException(editTag.getAttribute("result"));
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <edit result=\"\"> attribute not present");
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <edit> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - UNDO - - -
 
 	/**
 	 * Undoes a revision on the page embodied by the specified
 	 * <code>editToken</code>.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to undo a revision
 	 *            of and containing information to detect conflicts. This is
 	 *            gotten using <code>startEdit</code>.
 	 * @param revisionID
 	 *            The ID of the revision to undo.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit that undoes the
 	 *            revision.
 	 * @param bot
 	 *            Whether to mark the edit that undoes the revision as made by a
 	 *            bot ( <code>true</code>) or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit that undoes the revision as minor (
 	 *            <code>true</code>) or not ( <code>false</code>) or to use the
 	 *            value in <tt>Special:Preferences</tt> for the currently-logged
 	 *            in user of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki undoRevision(final MediaWiki.EditToken editToken, long revisionID, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "edit", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", editToken.getFullPageName(), "token", editToken.getTokenText(), "starttimestamp", iso8601TimestampParser.format(editToken.getStartTime()), "undo", Long.toString(revisionID), "summary", editSummary);
 		if (bot)
 			postParams.put("bot", "true");
 		if (minor != null)
 			postParams.put(minor ? "minor" : "notminor", "true");
 		if (editToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(editToken.getLastRevisionTime()));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList editTags = xml.getElementsByTagName("edit");
 
 			if (editTags.getLength() > 0) {
 				Element editTag = (Element) editTags.item(0);
 
 				if (editTag.hasAttribute("result")) {
 					if (editTag.getAttribute("result").equals("Success"))
 						return this;
 					else
 						throw new MediaWiki.ActionFailureException(editTag.getAttribute("result"));
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <edit result=\"\"> attribute not present");
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <edit> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - PREPEND AND APPEND - - -
 
 	/**
 	 * Prepends (i.e. writes text before the start) or appends (i.e. writes text
 	 * after the end) some text to the page embodied by the specified
 	 * <code>editToken</code>.
 	 * 
 	 * @param editToken
 	 *            The <tt>EditToken</tt> describing the page to add the text to
 	 *            and containing information to detect conflicts. This is gotten
 	 *            using <code>startEdit</code>.
 	 * @param text
 	 *            The text to add.
 	 * @param atEnd
 	 *            If <code>true</code>, text will be added after the end of the
 	 *            page. If <code>false</code>, text will be added before the
 	 *            start of the page.
 	 * @param editSummary
 	 *            The summary to be used to describe the edit that adds the
 	 *            text..
 	 * @param bot
 	 *            Whether to mark the edit that adds the text as made by a bot (
 	 *            <code>true</code>) or not (<code>false</code>).
 	 * @param minor
 	 *            Whether to mark the edit that adds the text as minor (
 	 *            <code>true</code>) or not ( <code>false</code>) or to use the
 	 *            value in <tt>Special:Preferences</tt> for the currently-logged
 	 *            in user of this <tt>MediaWiki</tt>.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki addText(final MediaWiki.EditToken editToken, String text, boolean atEnd, String editSummary, boolean bot, Boolean minor) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "edit", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", editToken.getFullPageName(), "token", editToken.getTokenText(), "starttimestamp", iso8601TimestampParser.format(editToken.getStartTime()), atEnd ? "appendtext" : "prependtext", text, "summary", editSummary);
 		if (bot)
 			postParams.put("bot", "true");
 		if (minor != null)
 			postParams.put(minor ? "minor" : "notminor", "true");
 		if (editToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(editToken.getLastRevisionTime()));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList editTags = xml.getElementsByTagName("edit");
 
 			if (editTags.getLength() > 0) {
 				Element editTag = (Element) editTags.item(0);
 
 				if (editTag.hasAttribute("result")) {
 					if (editTag.getAttribute("result").equals("Success"))
 						return this;
 					else
 						throw new MediaWiki.ActionFailureException(editTag.getAttribute("result"));
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <edit result=\"\"> attribute not present");
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <edit> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - MOVE - - -
 
 	/**
 	 * Starts a move of the given page, specified by its full name. The returned
 	 * token contains informaton used to detect conflicts when the move is
 	 * ended.
 	 * 
 	 * @param fullName
 	 *            The full name, including the namespace, of the page to start
 	 *            moving. This is the name of the page as it exists before the
 	 *            move.
 	 * @return a <tt>MediaWiki.EditToken</tt> containing informaton used to
 	 *         detect conflicts when the move is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.EditToken startMove(final String fullName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "intoken", "move", "titles", titleToAPIForm(fullName));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList pageTags = xml.getElementsByTagName("page");
 
 			if (pageTags.getLength() > 0) {
 				Element pageTag = (Element) pageTags.item(0);
 
 				if (!pageTag.hasAttribute("movetoken"))
 					throw new MediaWiki.PermissionException("move");
 
 				final Date lastRevision = pageTag.hasAttribute("touched") ? iso8601TimestampParser.parse(pageTag.getAttribute("touched")) : null;
 				final Date start = iso8601TimestampParser.parse(pageTag.getAttribute("starttimestamp"));
 				final String token = pageTag.getAttribute("movetoken");
 
 				return new MediaWiki.EditToken(fullName, lastRevision, start, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 		} catch (ParseException pe) {
 			throw new MediaWiki.MediaWikiException(pe);
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends a move embodied by the specified <code>moveToken</code>.
 	 * 
 	 * @param moveToken
 	 *            The <tt>EditToken</tt> describing the page to move and
 	 *            containing information to detect conflicts.
 	 * @param editSummary
 	 *            The reason to be used to describe the move.
 	 * @param suppressRedirect
 	 *            <code>true</code> to move the page without leaving a redirect
 	 *            to the new page's name under its old name. This requires the
 	 *            <tt>suppressredirect</tt> right.
 	 * @param moveTalk
 	 *            <code>true</code> to move the page's talk page as well;
 	 *            <code>false</code> to avoid doing so.
 	 * @param moveSubpages
 	 *            <code>true</code> to move the page's subpages as well;
 	 *            <code>false</code> to avoid doing so.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki endMove(final MediaWiki.EditToken moveToken, String newFullName, String reason, boolean suppressRedirect, boolean moveTalk, boolean moveSubpages) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "move", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("from", moveToken.getFullPageName(), "to", newFullName, "token", moveToken.getTokenText(), "starttimestamp", iso8601TimestampParser.format(moveToken.getStartTime()), "reason", reason);
 		if (suppressRedirect)
 			postParams.put("noredirect", "true");
 		if (moveTalk)
 			postParams.put("movetalk", "true");
 		if (moveSubpages)
 			postParams.put("movesubpages", "true");
 		if (moveToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(moveToken.getLastRevisionTime()));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 			checkError(xml, "talkmove-error-code", "talkmove-error-info");
 
 			NodeList moveTags = xml.getElementsByTagName("move");
 
 			if (moveTags.getLength() > 0)
 				return this;
 			else
 				throw new MediaWiki.ResponseFormatException("expected <move> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - ROLLBACK - - -
 
 	/**
 	 * Starts a rollback of the given page, specified by its full name. The
 	 * returned token contains informaton used to abort the rollback if another
 	 * user edits the page. The name of the last user who made an edit to the
 	 * page is also available.
 	 * 
 	 * @param fullName
 	 *            The full name, including the namespace, of the page to start
 	 *            rolling back.
 	 * @return a <tt>MediaWiki.RollbackToken</tt> containing informaton used to
 	 *         detect conflicts when the move is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.RollbackToken startRollback(final String fullName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "revisions", "rvtoken", "rollback", "titles", titleToAPIForm(fullName), "rvlimit", "1");
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList pageTags = xml.getElementsByTagName("page");
 
 			if (pageTags.getLength() > 0) {
 				Element pageTag = (Element) pageTags.item(0);
 
 				if (pageTag.hasAttribute("missing"))
 					throw new MediaWiki.MissingPageException();
 
 				NodeList revTags = xml.getElementsByTagName("rev");
 
 				String userName, token;
 				if (revTags.getLength() > 0) {
 					Element revTag = (Element) revTags.item(0);
 
 					if (!revTag.hasAttribute("rollbacktoken"))
 						throw new MediaWiki.PermissionException("rollback");
 
 					userName = revTag.getAttribute("user");
 					token = revTag.getAttribute("rollbacktoken");
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <rev> tag not found");
 
 				return new MediaWiki.RollbackToken(fullName, userName, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends a rollback embodied by the specified <code>rollbackToken</code>.
 	 * 
 	 * @param rollbackToken
 	 *            The <tt>RollbackToken</tt> describing the page to roll back
 	 *            and containing information to abort the rollback if another
 	 *            user edited the page.
 	 * @param comment
 	 *            The reason to be used to describe the rollback. This parameter
 	 *            may be <code>null</code> to use a default rollback comment.
 	 * @param bot
 	 *            <code>true</code> to mark the rolled-back edits and the edit
 	 *            rolling back the edits as made by a bot; <code>false</code> to
 	 *            avoid doing so. Marking the edits as made by a bot hides them
 	 *            from <tt>Special:RecentChanges</tt> by default, which helps in
 	 *            case of severe vandalism.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki endRollback(final MediaWiki.RollbackToken rollbackToken, final String comment, final boolean bot) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "rollback", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", rollbackToken.getFullPageName(), "user", rollbackToken.getUserName(), "token", rollbackToken.getTokenText(), "summary", comment);
 		if (bot)
 			postParams.put("markbot", "true");
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList rollbackTags = xml.getElementsByTagName("rollback");
 
 			if (rollbackTags.getLength() > 0)
 				return this;
 			else
 				throw new MediaWiki.ResponseFormatException("expected <rollback> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - UPLOAD - - -
 
 	/**
 	 * Starts an upload to the given image page, specified by its base name. The
 	 * returned token contains informaton used to detect conflicts when the
 	 * upload is ended.
 	 * 
 	 * @param baseName
 	 *            The base name (excluding the namespace) of the page to start
 	 *            uploading to.
 	 * @return a <tt>MediaWiki.EditToken</tt> containing informaton used to
 	 *         detect conflicts when the upload is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.EditToken startUpload(final String baseName) throws IOException, MediaWiki.MediaWikiException {
 		/*
 		 * Implementation note: On MediaWiki 1.18 and earlier, the upload token
 		 * is the edit token.
 		 */
 		return startEdit(getNamespaces().getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(baseName));
 	}
 
 	/**
 	 * Ends an upload embodied by the specified <code>editToken</code> by making
 	 * the content of the specified <tt>InputStream</tt> the latest revision of
 	 * the image.
 	 * 
 	 * @param uploadToken
 	 *            The <tt>EditToken</tt> describing the image page to upload to
 	 *            and containing information to detect conflicts.
 	 * @param content
 	 *            The bytes to be contained by the image after the edit.
 	 * @param comment
 	 *            The summary to be used to describe the upload. If the file is
 	 *            created by this upload, this comment is also used as the text
 	 *            of the page associated with the image, unless
 	 *            <code>pageText</code> is not <code>null</code>.
 	 * @param pageText
 	 *            The text to be contained by the page associated with the
 	 *            image, if the file is created by this upload.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned (if the page is
 	 *             protected from uploading,
 	 *             <tt>MediaWiki.PermissionException</tt>)
 	 */
 	public MediaWiki endUpload(final MediaWiki.EditToken uploadToken, InputStream content, String comment, String pageText) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "upload", "format", "xml");
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			// Repeating post() here because we are sending in
 			// multipart/form-data.
 			final HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
 			initConnection(http);
 			initPost(http);
 			// Choose a multipart boundary.
 			String boundary;
 			{
 				// It shall have between 14 and 17 characters.
 				int count = 14 + (int) (Math.random() * 4);
 				// It shall contain only alphanumeric characters, chosen to
 				// minimize the probability that the image contains them.
 				char[] cc = new char[count];
 				for (int i = 0; i < count; i++) {
 					int random = (int) (Math.random() * 62);
 					if (random < 26)
 						cc[i] = (char) ('A' + random);
 					else if (random < 52)
 						cc[i] = (char) ('a' + (random - 26));
 					else
 						cc[i] = (char) ('0' + (random - 52));
 				}
 				boundary = new String(cc);
 			}
 			http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
 
 			initCookies(http);
 			final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(http.getOutputStream(), "UTF-8"));
 
 			out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"filename\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n");
 			out.write(getNamespaces().removeNamespacePrefix(uploadToken.getFullPageName()));
 
 			out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"token\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n");
 			out.write(uploadToken.getTokenText());
 
 			out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"ignorewarnings\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n");
 			out.write("true");
 
 			if (comment != null) {
 				out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"comment\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n");
 				out.write(comment);
 			}
 
 			if (pageText != null) {
 				out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"text\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n");
 				out.write(pageText);
 			}
 
 			out.write("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"x\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: binary\r\n\r\n");
 			// Switch from buffered characters to unbuffered bytes.
 			out.flush();
 
 			byte[] buf = new byte[4096];
 			int read;
 
 			while ((read = content.read(buf)) > 0) {
 				http.getOutputStream().write(buf, 0, read);
 			}
 
 			out.write("\r\n--" + boundary + "--\r\n"); // End
 
 			out.flush();
 			out.close();
 			http.connect();
 
 			if (http.getResponseCode() != 200)
 				throw new MediaWiki.HttpStatusException(http.getResponseCode());
 
 			updateCookies(http);
 
 			final String encoding = http.getHeaderField("Content-Encoding");
 			InputStream in = (encoding != null) && encoding.equals("gzip") ? new GZIPInputStream(http.getInputStream()) : http.getInputStream();
 			// End of post() repeat.
 
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList uploadTags = xml.getElementsByTagName("upload");
 
 			if (uploadTags.getLength() > 0) {
 				Element uploadTag = (Element) uploadTags.item(0);
 
 				if (uploadTag.hasAttribute("result")) {
 					if (uploadTag.getAttribute("result").equals("Success"))
 						return this;
 					else
 						throw new MediaWiki.ActionFailureException(uploadTag.getAttribute("result"));
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <upload result=\"\"> attribute not present");
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <upload> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - DELETE - - -
 
 	/**
 	 * Starts a deletion of the given page, specified by its full name. The
 	 * returned token contains informaton used to detect conflicts when the
 	 * deletion is ended.
 	 * 
 	 * @param fullName
 	 *            The full name, including the namespace, of the page to start
 	 *            deleting. This is the name of the page as it exists before the
 	 *            move.
 	 * @return a <tt>MediaWiki.EditToken</tt> containing informaton used to
 	 *         detect conflicts when the deletion is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.EditToken startDelete(final String fullName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "intoken", "delete", "titles", titleToAPIForm(fullName));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList pageTags = xml.getElementsByTagName("page");
 
 			if (pageTags.getLength() > 0) {
 				Element pageTag = (Element) pageTags.item(0);
 
 				if (!pageTag.hasAttribute("deletetoken"))
 					throw new MediaWiki.PermissionException("delete");
 
 				final Date lastRevision = pageTag.hasAttribute("touched") ? iso8601TimestampParser.parse(pageTag.getAttribute("touched")) : null;
 				final Date start = iso8601TimestampParser.parse(pageTag.getAttribute("starttimestamp"));
 				final String token = pageTag.getAttribute("deletetoken");
 
 				return new MediaWiki.EditToken(fullName, lastRevision, start, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 		} catch (ParseException pe) {
 			throw new MediaWiki.MediaWikiException(pe);
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends a deletion embodied by the specified <code>deletionToken</code>.
 	 * This deletes the page itself and all of its revisions, making them
 	 * accessible only to those who have the rights required to view
 	 * <tt>Special:Undelete</tt> on the wiki that this <tt>MediaWiki</tt>
 	 * represents.
 	 * 
 	 * @param deletionToken
 	 *            The <tt>EditToken</tt> describing the page to delete and
 	 *            containing information to detect conflicts.
 	 * @param reason
 	 *            The reason to be used to describe the deletion.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki endDelete(final MediaWiki.EditToken deletionToken, String reason) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "delete", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", deletionToken.getFullPageName(), "starttimestamp", iso8601TimestampParser.format(deletionToken.getStartTime()), "token", deletionToken.getTokenText(), "reason", reason);
 		if (deletionToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(deletionToken.getLastRevisionTime()));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList deleteTags = xml.getElementsByTagName("delete");
 
 			if (deleteTags.getLength() > 0)
 				return this;
 			else
 				throw new MediaWiki.ResponseFormatException("expected <delete> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - PROTECT - - -
 
 	/**
 	 * Starts a modification to the protection of the given page, specified by
 	 * its full name. The returned token contains informaton used to detect
 	 * conflicts when the (un)protection is ended.
 	 * 
 	 * @param fullName
 	 *            The full name, including the namespace, of the page to start
 	 *            (un)protecting.
 	 * @return a <tt>MediaWiki.EditToken</tt> containing informaton used to
 	 *         detect conflicts when the (un)protection is ended
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.EditToken startProtect(final String fullName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "intoken", "protect", "titles", titleToAPIForm(fullName));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList pageTags = xml.getElementsByTagName("page");
 
 			if (pageTags.getLength() > 0) {
 				Element pageTag = (Element) pageTags.item(0);
 
 				if (!pageTag.hasAttribute("protecttoken"))
 					throw new MediaWiki.PermissionException("protect");
 
 				final Date lastRevision = pageTag.hasAttribute("touched") ? iso8601TimestampParser.parse(pageTag.getAttribute("touched")) : null;
 				final Date start = iso8601TimestampParser.parse(pageTag.getAttribute("starttimestamp"));
 				final String token = pageTag.getAttribute("protecttoken");
 
 				return new MediaWiki.EditToken(fullName, lastRevision, start, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 		} catch (ParseException pe) {
 			throw new MediaWiki.MediaWikiException(pe);
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends a protection embodied by the specified <code>protectionToken</code>.
 	 * This protects the page itself and, if cascading protection is enabled,
 	 * any page that it transcludes.
 	 * 
 	 * @param protectionToken
 	 *            The <tt>EditToken</tt> describing the page to protect and
 	 *            containing information to detect conflicts.
 	 * @param protections
 	 *            A map associating names of actions to (un)protect to the data
 	 *            associated with the (un)protection. If an action to be
 	 *            protected, it is associated with a
 	 *            <tt>MediaWiki.Protection</tt> object describing the protection
 	 *            type and expiry (the expiry being <code>null</code> to signify
 	 *            never). If an action is to be unprotected, it is
 	 *            <em>explicitly</em> associated with a
 	 *            <tt>MediaWiki.Protection</tt> of <code>null</code>. Actions
 	 *            that are to be left alone are not in this map altogether.
 	 * @param reason
 	 *            The reason to be used to describe the protection.
 	 * @param cascade
 	 *            <code>true</code> if pages that are or become transcluded in
 	 *            the page given in the <code>protectionToken</code> are to be
 	 *            protected as well; <code>false</code> if only the page given
 	 *            in the <code>protectionToken</code> is to be protected.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki endProtect(final MediaWiki.EditToken protectionToken, Map<String, MediaWiki.Protection> protections, String reason, boolean cascade) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "protect", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("title", protectionToken.getFullPageName(), "starttimestamp", iso8601TimestampParser.format(protectionToken.getStartTime()), "token", protectionToken.getTokenText(), "reason", reason);
 		if (cascade)
 			postParams.put("cascade", "true");
 		if (protectionToken.getLastRevisionTime() != null)
 			postParams.put("basetimestamp", iso8601TimestampParser.format(protectionToken.getLastRevisionTime()));
 		{
 			StringBuilder paramProtections = new StringBuilder();
 			StringBuilder paramExpiry = new StringBuilder();
 			for (Map.Entry<String, MediaWiki.Protection> protection : protections.entrySet()) {
 				if (protection.getValue() != null) {
 					// Protecting
 					if (paramProtections.length() > 0)
 						paramProtections.append('|');
 					paramProtections.append(protection.getKey() + "=" + protection.getValue().getLevel());
 					if (paramExpiry.length() > 0)
 						paramExpiry.append('|');
 					paramExpiry.append(protection.getValue().getExpiry() != null ? protection.getValue().getExpiry() : "never");
 				} else {
 					// Unprotecting
 					if (paramProtections.length() > 0)
 						paramProtections.append('|');
 					paramProtections.append(protection.getKey() + "=all");
 					if (paramExpiry.length() > 0)
 						paramExpiry.append('|');
 					paramExpiry.append("never");
 				}
 			}
 			postParams.put("protections", paramProtections.toString());
 			postParams.put("expiry", paramExpiry.toString());
 		}
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList protectTags = xml.getElementsByTagName("protect");
 
 			if (protectTags.getLength() > 0)
 				return this;
 			else
 				throw new MediaWiki.ResponseFormatException("expected <protect> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - USER RIGHTS MODIFICATION - - -
 
 	/**
 	 * Starts a modification of the group memberships of the given user,
 	 * specified by its name.
 	 * 
 	 * @param userName
 	 *            The name of the user to modify the group membership of.
 	 * @return a <tt>MediaWiki.UserGroupsToken</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or while reading the XML reply from the API
 	 * @throws MediaWiki.MediaWikiException
 	 *             if a MediaWiki API error is returned
 	 */
 	public MediaWiki.UserGroupsToken startUserGroupModification(final String userName) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "list", "users", "ustoken", "userrights", "ususers", titleToAPIForm(userName));
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList userTags = xml.getElementsByTagName("user");
 
 			if (userTags.getLength() > 0) {
 				Element userTag = (Element) userTags.item(0);
 
 				if (!userTag.hasAttribute("userrightstoken"))
 					throw new MediaWiki.PermissionException("userrights");
 
 				final String token = userTag.getAttribute("userrightstoken");
 
 				return new MediaWiki.UserGroupsToken(userName, token);
 			} else
 				throw new MediaWiki.ResponseFormatException("expected <user> tag not found");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Ends a modification to user group memberships embodied by the specified
 	 * <code>userGroupsToken</code>.
 	 * 
 	 * @param userGroupsToken
 	 *            The <tt>UserGroupsToken</tt> describing the user to modify the
 	 *            group membership of.
 	 * @param add
 	 *            The group(s) to make the user a member of.
 	 * @param remove
 	 *            The group(s) to remove the user from.
 	 * @param reason
 	 *            The reason to be used to describe the change.
 	 * @return this <tt>MediaWiki</tt>
 	 * @throws IOException
 	 * @throws MediaWiki.MediaWikiException
 	 */
 	public MediaWiki endUserGroupModification(final MediaWiki.UserGroupsToken userRightsToken, Collection<String> add, Collection<String> remove, String reason) throws IOException, MediaWiki.MediaWikiException {
 		Map<String, String> getParams = paramValuesToMap("action", "userrights", "format", "xml");
 		Map<String, String> postParams = paramValuesToMap("user", userRightsToken.getUserName(), "token", userRightsToken.getTokenText(), "reason", reason);
 		{
 			StringBuilder value = new StringBuilder();
 			for (String group : add) {
 				if (value.length() > 0)
 					value.append('|');
 				value.append(group);
 			}
 			if (value.length() > 0)
 				postParams.put("add", value.toString());
 			value.setLength(0);
 			for (String group : remove) {
 				if (value.length() > 0)
 					value.append('|');
 				value.append(group);
 			}
 			if (value.length() > 0)
 				postParams.put("remove", value.toString());
 		}
 
 		String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			InputStream in = post(url, postParams);
 			Document xml = parse(in);
 			checkError(xml);
 
 			NodeList userrightsTags = xml.getElementsByTagName("userrights");
 
 			if (userrightsTags.getLength() > 0)
 				return this;
 			else
 				throw new MediaWiki.ResponseFormatException("expected <userrights> tag not present");
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	// - - - DATA CLASSES - - -
 
 	public static class CurrentUser {
 		private final boolean isAnonymous;
 
 		private final long userID;
 
 		private final String userName;
 
 		private final boolean hasNewMessages;
 
 		private final Set<String> groups;
 
 		private final Set<String> rights;
 
 		private final long editCount;
 
 		private final String blockingUser;
 
 		private final String blockReason;
 
 		CurrentUser(final boolean isAnonymous, final String userName, final long userID, final boolean hasNewMessages, final Collection<String> groups, final Collection<String> rights, final long editCount, final String blockingUser, final String blockReason) {
 			this.userID = userID;
 			this.userName = userName;
 			this.isAnonymous = isAnonymous;
 			this.hasNewMessages = hasNewMessages;
 			this.groups = Collections.unmodifiableSet(groups instanceof TreeSet<?> ? (TreeSet<String>) groups : new TreeSet<String>(groups));
 			this.rights = Collections.unmodifiableSet(rights instanceof TreeSet<?> ? (TreeSet<String>) rights : new TreeSet<String>(rights));
 			this.editCount = editCount;
 			this.blockingUser = blockingUser;
 			this.blockReason = blockReason;
 		}
 
 		/**
 		 * Return whether the currently logged-in user is an anonymous user,
 		 * i.e. an IP address.
 		 * 
 		 * @return whether the currently logged-in user is an anonymous user
 		 */
 		public boolean isAnonymous() {
 			return isAnonymous;
 		}
 
 		/**
 		 * Returns the ID of the currently logged-in user. The return value is
 		 * undefined if <code>isAnonymous()</code> returns <code>true</code>.
 		 * 
 		 * @return the ID of the currently logged-in user
 		 */
 		public long getUserID() {
 			return userID;
 		}
 
 		/**
 		 * Returns the username (not real name) of the currently logged-in user.
 		 * The return value is an IP address if <code>isAnonymous()</code>
 		 * returns <code>true</code>.
 		 * 
 		 * @return the username of the currently logged-in user
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns whether the currently logged-in user has new messages.
 		 * 
 		 * @return whether the currently logged-in user has new messages
 		 */
 		public boolean hasNewMessages() {
 			return hasNewMessages;
 		}
 
 		/**
 		 * Returns an immutable view of the list of groups that the currently
 		 * logged-in user is a part of.
 		 * 
 		 * @return an immutable view of the list of groups that the currently
 		 *         logged-in user is a part of
 		 */
 		public Collection<String> getGroups() {
 			return groups;
 		}
 
 		/**
 		 * Returns an immutable view of the list of rights that the currently
 		 * logged-in user has.
 		 * 
 		 * @return an immutable view of the list of rights that the currently
 		 *         logged-in user has
 		 */
 		public Collection<String> getRights() {
 			return rights;
 		}
 
 		/**
 		 * Returns whether the currently logged-in user is in all of the
 		 * specified groups. The return value is <code>true</code> if no groups
 		 * are specified.
 		 * 
 		 * @param groups
 		 *            The groups to check membership in.
 		 * @return whether the currently logged-in user is in all of the
 		 *         specified groups
 		 */
 		public boolean isInGroups(final String... groups) {
 			for (final String group : groups) {
 				if (!this.groups.contains(group))
 					return false;
 			}
 			return true;
 		}
 
 		/**
 		 * Returns whether the currently logged-in user has all of the specified
 		 * rights. The return value is <code>true</code> if no rights are
 		 * specified.
 		 * 
 		 * @param rights
 		 *            The rights to check possession of.
 		 * @return whether the currently logged-in user has all of the specified
 		 *         rights
 		 */
 		public boolean hasRights(final String... rights) {
 			for (final String right : rights) {
 				if (!this.rights.contains(right))
 					return false;
 			}
 			return true;
 		}
 
 		/**
 		 * Returns the number of edits made by the currently logged-in user on
 		 * the wiki represented by the enclosing <tt>MediaWiki</tt>.
 		 * 
 		 * @return the number of edits made by the currently logged-in user on
 		 *         the wiki represented by the enclosing <tt>MediaWiki</tt>
 		 */
 		public long getEditCount() {
 			return editCount;
 		}
 
 		/**
 		 * Returns the name of the user who blocked the currently logged-in
 		 * user. The return value is <code>null</code> if the currently
 		 * logged-in user is not blocked.
 		 * 
 		 * @return the name of the user who blocked the currently logged-in user
 		 */
 		public String getBlockingUser() {
 			return blockingUser;
 		}
 
 		/**
 		 * Returns the reason why the currently logged-in user is blocked. The
 		 * return value is <code>null</code> if the currently logged-in user is
 		 * not blocked.
 		 * 
 		 * @return the reason why the currently logged-in user is blocked
 		 */
 		public String getBlockReason() {
 			return blockReason;
 		}
 
 		@Override
 		public String toString() {
 			// CurrentUser["Name" *new messages* (user ID X) (anonymous),
 			// groups, rights, Y edits, blocked by Sysop (Reason)]
 			return String.format("CurrentUser[\"%s\"%s (user ID %d)%s, groups: %s, %rights: %s, %d edits%s]", userName, hasNewMessages ? " *new messages*" : "", userID, isAnonymous ? " (anonymous)" : "", groups, rights, editCount, blockingUser != null ? ", blocked by " + blockingUser + " (" + blockReason + ")" : "");
 		}
 	}
 
 	public static class Namespaces {
 		private final Collection<MediaWiki.Namespace> list;
 
 		Namespaces(final Collection<MediaWiki.Namespace> list) {
 			this.list = Collections.unmodifiableCollection(list);
 		}
 
 		/**
 		 * Returns a <tt>Namespace</tt> object matching the given
 		 * <code>prefix</code> drawn from this <tt>Namespaces</tt>'s list. The
 		 * return value is <code>null</code> if there is no such namespace.
 		 * 
 		 * @param prefix
 		 *            The prefix to retrieve a <tt>Namespace</tt> for. This
 		 *            value may not end with a colon <code>':'</code>, nor may
 		 *            it contain a page name. For example,
 		 *            <code>"User talk"</code>.
 		 * @return a <tt>Namespace</tt> object matching the given
 		 *         <code>prefix</code>
 		 */
 		public MediaWiki.Namespace getNamespace(final String prefix) {
 			for (final MediaWiki.Namespace namespace : list)
 				if (namespace.matches(prefix))
 					return namespace;
 			return null;
 		}
 
 		/**
 		 * Returns a <tt>Namespace</tt> object having the given <code>id</code>
 		 * drawn from this <tt>Namespaces</tt>'s list. The return value is
 		 * <code>null</code> if there is no such namespace.
 		 * 
 		 * @param id
 		 *            The id of the <tt>Namespace</tt> to retrieve.
 		 * @return a <tt>Namespace</tt> object matching the given
 		 *         <code>prefix</code>
 		 */
 		public MediaWiki.Namespace getNamespace(final long id) {
 			for (final MediaWiki.Namespace namespace : list)
 				if (namespace.getID() == id)
 					return namespace;
 			return null;
 		}
 
 		/**
 		 * Returns a read-only <tt>Collection</tt> enumerating the entries in
 		 * this <tt>MediaWiki.Namespaces</tt>.
 		 * 
 		 * @return a read-only <tt>Collection</tt> enumerating the entries in
 		 *         this <tt>MediaWiki.Namespaces</tt>
 		 */
 		public Collection<MediaWiki.Namespace> getList() {
 			return list;
 		}
 
 		/**
 		 * Returns the <tt>Namespace</tt> object designating the namespage
 		 * containing the page of the given name drawn from this
 		 * <tt>Namespaces</tt>'s list. The return value is the
 		 * <tt>Namespace</tt> object having the ID of 0, i.e. the main
 		 * namespace, if there is no such namespace.
 		 * 
 		 * @param fullPageName
 		 *            The page to retrieve the <tt>Namespace</tt> of. For
 		 *            example, "Paperclip" (in the main namespace),
 		 *            "Project:Administrator requests" (in the Project
 		 *            namespace), "Inexistent namespace:Example" (in the main
 		 *            namespace, despite there being a colon in the full page
 		 *            name).
 		 * @return the <tt>Namespace</tt> object designating the namespage
 		 *         containing the page of the given name
 		 */
 		public MediaWiki.Namespace getNamespaceForPage(final String fullPageName) {
 			final int colonIndex = fullPageName.indexOf(':');
 			if (colonIndex == -1)
 				return getNamespace(0L);
 			MediaWiki.Namespace result;
 			return (result = getNamespace(fullPageName.substring(0, colonIndex))) != null ? result : getNamespace(0L);
 		}
 
 		/**
 		 * Returns the name of the given page after stripping it of its
 		 * namespace prefix. The return value is the page name itself if what is
 		 * before the colon is not a valid namespace for the wiki that the
 		 * enclosing <tt>MediaWiki</tt> represents.
 		 * 
 		 * @param fullPageName
 		 *            The page name to strip of its namespace prefix. For
 		 *            example, "Paperclip" (in the main namespace) becomes
 		 *            "Paperclip", "Project:Administrator requests" (in the
 		 *            Project namespace) becomes "Administrator requests",
 		 *            "Inexistent namespace:Example" (in the main namespace,
 		 *            despite there being a colon in the full page name) becomes
 		 *            "Inexistent namespace:Example".
 		 * @return the name of the given page after stripping it of its
 		 *         namespace prefix
 		 */
 		public String removeNamespacePrefix(final String fullPageName) {
 			final int colonIndex = fullPageName.indexOf(':');
 			if (colonIndex == -1)
 				return fullPageName;
 			return getNamespace(fullPageName.substring(0, colonIndex)) != null ? fullPageName.substring(colonIndex + 1) : fullPageName;
 		}
 
 		/**
 		 * Returns the name of the given page's talk page.
 		 * 
 		 * @param fullPageName
 		 *            The page name to get the talk page of.
 		 * @return the name of the given page's talk page
 		 * @throws IllegalArgumentException
 		 *             if the given page's namespace does not support having, or
 		 *             does not have, a talk namespace; if the given page's
 		 *             namespace is already a talk namespace
 		 */
 		public String getTalkPage(final String fullPageName) throws IllegalArgumentException {
 			final MediaWiki.Namespace namespace = getNamespaceForPage(fullPageName);
 			final String basePageName = removeNamespacePrefix(fullPageName);
 			if (namespace.getID() < 0 /*- Special:, Media: */)
 				throw new IllegalArgumentException("Namespace '" + namespace.getCanonicalName() + "' does not support having an associated talk namespace");
 			else if (namespace.getID() % 2 == 1 /*- Talk namespace */)
 				throw new IllegalArgumentException("Namespace '" + namespace.getCanonicalName() + "' is a talk namespace");
 			else {
 				// Talk namespaces have the same ID as the ones they are talking
 				// about, plus 1.
 				final MediaWiki.Namespace talkNamespace = getNamespace(namespace.getID() + 1L);
 				if (talkNamespace == null)
 					throw new IllegalArgumentException("Namespace '" + namespace.getCanonicalName() + "' does not have an associated talk namespace");
 				return talkNamespace.getFullPageName(basePageName);
 			}
 		}
 
 		@Override
 		public String toString() {
 			return list.toString();
 		}
 	}
 
 	public static class Namespace {
 		private final long id;
 
 		private final String canonicalName;
 
 		private final Set<String> aliases;
 
 		private final boolean caseSensitive;
 
 		private final boolean isContent;
 
 		private final boolean allowsSubpages;
 
 		Namespace(final long id, final String canonicalName, final Collection<String> aliases, final boolean caseSensitive, final boolean isContent, final boolean allowsSubpages) {
 			this.id = id;
 			this.canonicalName = canonicalName;
 			this.aliases = Collections.unmodifiableSet(aliases instanceof TreeSet<?> ? (TreeSet<String>) aliases : (aliases != null ? new TreeSet<String>(aliases) : new TreeSet<String>()));
 			this.caseSensitive = caseSensitive;
 			this.isContent = isContent;
 			this.allowsSubpages = allowsSubpages;
 		}
 
 		/**
 		 * Returns the ID of this <tt>Namespace</tt>.
 		 * 
 		 * @return the ID of this <tt>Namespace</tt>
 		 */
 		public long getID() {
 			return id;
 		}
 
 		/**
 		 * Returns the canonical name of this <tt>Namespace</tt>.
 		 * 
 		 * @return the canonical name of this <tt>Namespace</tt>
 		 */
 		public String getCanonicalName() {
 			return canonicalName;
 		}
 
 		/**
 		 * Returns an unmodifiable view of the aliases that can be used to refer
 		 * to this <tt>Namespace</tt>.
 		 * 
 		 * @return an unmodifiable view of the aliases that can be used to refer
 		 *         to this <tt>Namespace</tt>
 		 */
 		public Collection<String> getAliases() {
 			return aliases;
 		}
 
 		/**
 		 * Returns whether the first letter of the name of this
 		 * <tt>Namespace</tt> must be written with the correct case, as
 		 * specified in its canonical name. If <code>false</code>, this
 		 * <tt>Namespace</tt> allows using any case for its first letter.
 		 * 
 		 * @return whether the first letter of the name of this
 		 *         <tt>Namespace</tt> must be written with the correct case, as
 		 *         specified in its canonical name
 		 */
 		public boolean isCaseSensitive() {
 			return caseSensitive;
 		}
 
 		/**
 		 * Returns whether this <tt>Namespace</tt> is a content namespace, i.e.
 		 * whether it gets added to page count statistics etc.
 		 * 
 		 * @return whether this <tt>Namespace</tt> is a content namespace
 		 */
 		public boolean isContent() {
 			return isContent;
 		}
 
 		/**
 		 * Returns whether this <tt>Namespace</tt> allows pages to be under
 		 * other pages, separated by slashes.
 		 * 
 		 * @return whether this <tt>Namespace</tt> allows pages to be under
 		 *         other pages
 		 */
 		public boolean allowsSubpages() {
 			return allowsSubpages;
 		}
 
 		/**
 		 * Returns whether the given <code>prefix</code> matches this
 		 * <tt>Namespace</tt>'s prefix.
 		 * 
 		 * @param prefix
 		 *            The prefix to check. This value may not end with a colon
 		 *            <code>':'</code>, nor may it contain a page name.
 		 * @return whether the given <code>prefix</code> matches this
 		 *         <tt>Namespace</tt>'s prefix
 		 */
 		public boolean matches(final String prefix) {
 			if (matches(canonicalName, prefix, caseSensitive))
 				return true;
 			for (final String alias : aliases)
 				if (matches(alias, prefix, caseSensitive))
 					return true;
 			return false;
 		}
 
 		/**
 		 * Returns the full name of a page having the given base name in this
 		 * <tt>Namespace</tt>.
 		 * 
 		 * @param basePageName
 		 *            Base name of the page, without a namespace prefix or
 		 *            colon.
 		 * @return the full name of a page having the given base name in this
 		 *         <tt>Namespace</tt>
 		 */
 		public String getFullPageName(final String basePageName) {
 			return canonicalName.length() != 0 ? canonicalName + ":" + basePageName : basePageName;
 		}
 
 		private static boolean matches(final String namespace, final String prefix, final boolean caseSensitive) {
 			if (caseSensitive)
 				// May throw NullPointerException
 				return prefix.equals(namespace);
 			else {
 				// May throw NullPointerException
 				if (namespace.length() == 0)
 					return prefix.length() == 0;
 				else if (namespace.length() == 1)
 					return (prefix.length() == 1) && (Character.toLowerCase(namespace.charAt(0)) == Character.toLowerCase(prefix.charAt(0)));
 				else
 					return (prefix.length() == namespace.length()) && (Character.toLowerCase(namespace.charAt(0)) == Character.toLowerCase(prefix.charAt(0))) && namespace.substring(1).replace(' ', '_').equals(prefix.substring(1).replace(' ', '_'));
 			}
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Namespace[\"%s\" or %s (ID %d), case sensitive: %s, content: %s, subpages allowed: %s]", canonicalName, aliases, id, caseSensitive, isContent, allowsSubpages);
 		}
 	}
 
 	public static class Statistics {
 		private final long pages, articles, edits, images, users, activeUsers, admins, jobs, views;
 
 		Statistics(final long pages, final long articles, final long edits, final long images, final long users, final long activeUsers, final long admins, final long jobs, final long views) {
 			this.pages = pages;
 			this.articles = articles;
 			this.edits = edits;
 			this.images = images;
 			this.users = users;
 			this.activeUsers = activeUsers;
 			this.admins = admins;
 			this.jobs = jobs;
 			this.views = views;
 		}
 
 		/**
 		 * Returns the number of pages on all namespaces of the wiki for which
 		 * this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of pages on all namespaces of the wiki
 		 */
 		public long getPageCount() {
 			return pages;
 		}
 
 		/**
 		 * Returns the number of pages on all content namespaces of the wiki for
 		 * which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of pages on all content namespaces of the wiki
 		 * @see Namespace#isContent()
 		 */
 		public long getContentPageCount() {
 			return articles;
 		}
 
 		/**
 		 * Returns the number of edits that have been made in total on the wiki
 		 * for which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of edits that have been made in total on the wiki
 		 */
 		public long getEditCount() {
 			return edits;
 		}
 
 		/**
 		 * Returns the number of user accounts that have been made in total on
 		 * the wiki for which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of user accounts that have been made in total on
 		 *         the wiki
 		 */
 		public long getUserCount() {
 			return users;
 		}
 
 		/**
 		 * Returns the number of user accounts that have been active in the last
 		 * 30 days on the wiki for which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of user accounts that have been active in the last
 		 *         30 days on the wiki
 		 */
 		public long getActiveUserCount() {
 			return activeUsers;
 		}
 
 		/**
 		 * Returns the number of images that have been uploaded on the wiki for
 		 * which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of images that have been uploaded on the wiki
 		 */
 		public long getImageCount() {
 			return images;
 		}
 
 		/**
 		 * Returns the number of user accounts that are administrators on the
 		 * wiki for which this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of user accounts that are administrators on the
 		 *         wiki
 		 */
 		public long getAdministratorCount() {
 			return admins;
 		}
 
 		/**
 		 * Returns the number of jobs that are pending on the wiki for which
 		 * this <tt>Statistics</tt> was created.
 		 * 
 		 * @return the number of jobs that are pending on the wiki
 		 */
 		public long getJobQueueLength() {
 			return jobs;
 		}
 
 		/**
 		 * Returns the number of views for all pages on the wiki for which this
 		 * <tt>Statistics</tt> was created.
 		 * <p>
 		 * Not all wikis show this information. The return value is -1 if the
 		 * number of views is unknown.
 		 * 
 		 * @return the number of views for all pages on the wiki
 		 */
 		public long getViewCount() {
 			return views;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Statistics[pages: %d, articles: %d, edits: %d, images: %d, users: %d (%d active, %d administrators), job queue: %d%s]", pages, articles, edits, images, users, activeUsers, admins, jobs, views != -1 ? ", " + views + " total views" : ", unknown total views");
 		}
 	}
 
 	public static class InterwikiPrefixes {
 		private final Map<String, MediaWiki.InterwikiPrefix> map;
 
 		InterwikiPrefixes(final Map<String, MediaWiki.InterwikiPrefix> map) {
 			this.map = Collections.unmodifiableMap(map);
 		}
 
 		/**
 		 * Returns an <tt>InterwikiPrefix</tt> object matching the given
 		 * <code>prefix</code> drawn from this <tt>InterwikiPrefixes</tt>'s
 		 * list. The return value is <code>null</code> if there is no such
 		 * object.
 		 * 
 		 * @param prefix
 		 *            The prefix to retrieve an <tt>InterwikiPrefix</tt> for.
 		 *            This value may not end with a colon <code>':'</code>, nor
 		 *            may it contain a page name. For example,
 		 *            <code>"wikipedia"</code>.
 		 * @return a <tt>Namespace</tt> object matching the given
 		 *         <code>prefix</code>
 		 */
 		public MediaWiki.InterwikiPrefix getInterwikiPrefix(final String prefix) {
 			return map.get(prefix);
 		}
 
 		/**
 		 * Returns the <tt>InterwikiPrefix</tt> object designating the other
 		 * wiki containing the page of the given name drawn from this
 		 * <tt>InterwikiPrefixes</tt>'s list. The return value is
 		 * <code>null</code> if there is no such other wiki, i.e. if the link
 		 * points inside the wiki that the enclosing <tt>MediaWiki</tt>
 		 * represents.
 		 * 
 		 * @param fullPageName
 		 *            The page to retrieve the <tt>InterwikiPrefix</tt> of. For
 		 *            example, "Paperclip" (<code>null</code>: on the active
 		 *            wiki), "Project:Administrator requests" (<code>null</code>
 		 *            : on the active wiki, in a namespace),
 		 *            "wikipedia:Internet" (an <tt>InterwikiPrefix</tt>
 		 *            describing Wikipedia: on an external wiki),
 		 *            "mw:Manual:Contents" (an <tt>InterwikiPrefix</tt>
 		 *            describing Mediawiki.org: on an external wiki in a
 		 *            namespace).
 		 * @return the <tt>InterwikiPrefix</tt> object designating the other
 		 *         wiki containing the page of the given name
 		 */
 		public MediaWiki.InterwikiPrefix getInterwikiPrefixForPage(final String fullPageName) {
 			final int colonIndex = fullPageName.indexOf(':');
 			if (colonIndex == -1)
 				return null;
 			return getInterwikiPrefix(fullPageName.substring(0, colonIndex));
 		}
 
 		/**
 		 * Returns the name of the given page after stripping it of its
 		 * interwiki prefix. The return value is the page name itself if what is
 		 * before the colon is not a valid interwiki prefix for the wiki that
 		 * the enclosing <tt>MediaWiki</tt> represents.
 		 * 
 		 * @param fullPageName
 		 *            The page name to strip of its interwiki prefix. For
 		 *            example, "Paperclip" (in the main namespace) becomes
 		 *            "Paperclip", "Project:Administrator requests" (in the
 		 *            Project namespace) becomes
 		 *            "Project:Administrator requests", "wikipedia:Internet" (on
 		 *            another wiki) becomes "Internet".
 		 * @return the name of the given page after stripping it of its
 		 *         namespace prefix
 		 */
 		public String removeInterwikiPrefix(final String fullPageName) {
 			final int colonIndex = fullPageName.indexOf(':');
 			if (colonIndex == -1)
 				return fullPageName;
 			return getInterwikiPrefix(fullPageName.substring(0, colonIndex)) != null ? fullPageName.substring(colonIndex + 1) : fullPageName;
 		}
 
 		@Override
 		public String toString() {
 			return map.toString();
 		}
 	}
 
 	public static class InterwikiPrefix {
 		private final String name;
 
 		private final String language;
 
 		private final String urlPattern;
 
 		private final boolean isLocal;
 
 		InterwikiPrefix(final String name, final String language, final String urlPattern, final boolean isLocal) {
 			this.name = name;
 			this.language = language;
 			this.urlPattern = urlPattern;
 			this.isLocal = isLocal;
 		}
 
 		/**
 		 * Returns the name of this <tt>InterwikiPrefix</tt>. This is the prefix
 		 * itself, not the name of the target wiki.
 		 * 
 		 * @return the name of this <tt>InterwikiPrefix</tt>
 		 */
 		public String getName() {
 			return name;
 		}
 
 		/**
 		 * Returns the language of the wiki targetted by this
 		 * <tt>InterwikiPrefix</tt>.
 		 * 
 		 * @return the language of the wiki targetted by this
 		 *         <tt>InterwikiPrefix</tt>
 		 */
 		public String getLanguage() {
 			return language;
 		}
 
 		/**
 		 * Returns the URL pattern of this <tt>InterwikiPrefix</tt>, containing
 		 * a <code>"$1"</code> placeholder where the article name goes.
 		 */
 		public String getURLPattern() {
 			return urlPattern;
 		}
 
 		/**
 		 * Returns whether this <tt>InterwikiPrefix</tt> refers to a wiki on the
 		 * same project (loosely defined as being controlled by the same
 		 * organisation) as the wiki that the enclosing <tt>MediaWiki</tt>
 		 * represents.
 		 * 
 		 * @return whether this <tt>InterwikiPrefix</tt> refers to a wiki on the
 		 *         same project as the wiki that the enclosing
 		 *         <tt>MediaWiki</tt> represents
 		 */
 		public boolean isLocal() {
 			return isLocal;
 		}
 
 		/**
 		 * Returns the full name of a page having the given base name in a wiki
 		 * and starting with this <tt>InterwikiPrefix</tt>.
 		 * 
 		 * @param basePageName
 		 *            Base name of the page, which may contain a namespace
 		 *            prefix.
 		 * @return the full name of a page having the given base name in a wiki
 		 *         and starting with this <tt>InterwikiPrefix</tt>
 		 */
 		public String getFullPageName(final String basePageName) {
 			return name + ":" + basePageName;
 		}
 
 		/**
 		 * Returns the full URL to the interwiki resource having this
 		 * <tt>InterwikiPrefix</tt> as its prefix and the given page name. In
 		 * the case that the page name contains an interwiki prefix of its own,
 		 * it is not resolved. To resolve it, a
 		 * <code>new MediaWiki(new URL(return value).getHost(),
 		 * <i>the new wiki's script path</i>)</code> is needed.
 		 * 
 		 * @param pageName
 		 *            Name of the page, which may contain a namespace prefix or
 		 *            another interwiki prefix.
 		 * @return the full URL to the interwiki resource having this
 		 *         <tt>InterwikiPrefix</tt> as its prefix and the given page
 		 *         name
 		 */
 		public String resolveURL(final String pageName) {
 			try {
 				// If $1 is at the end...
 				if (urlPattern.endsWith("$1"))
 					// Put the entire page name at the end.
 					return urlPattern.substring(0, urlPattern.length() - 2) + URLEncoder.encode(titleToDisplayForm(pageName), "UTF-8");
 				else {
 					/*
 					 * Not at the end. Put the first colon-separated token where
 					 * $1 is, and put the rest of the page at the end. (For
 					 * Wikia's c:wikiname:Page thing.)
 					 */
 					final int colonIndex = pageName.indexOf(':');
 					if (colonIndex == -1)
 						return urlPattern.replaceAll("\\$1", Matcher.quoteReplacement(URLEncoder.encode(pageName, "UTF-8")));
 					else
 						return urlPattern.replaceAll("\\$1", Matcher.quoteReplacement(pageName.substring(0, colonIndex))) + URLEncoder.encode(titleToDisplayForm(pageName.substring(colonIndex + 1)), "UTF-8");
 				}
 			} catch (final UnsupportedEncodingException shouldNeverHappen) {
 				throw new InternalError("UTF-8 is not supported by this Java VM");
 			}
 		}
 
 		@Override
 		public String toString() {
 			// InterwikiPrefix["Name" -> <URL>, local: true, language: English
 			return String.format("InterwikiPrefix[\"%s\" -> <%s>, local: %s%s]", name, urlPattern, isLocal, language != null ? ", language prefix: " + language : "");
 		}
 	}
 
 	public abstract class PageNameComponents {
 		/**
 		 * Page ID. If unknown, this field contains <code>null</code>.
 		 */
 		private final Long pageID;
 
 		/**
 		 * Namespace ID.
 		 */
 		private final long namespaceID;
 
 		/**
 		 * Namespace.
 		 */
 		private final MediaWiki.Namespace namespace;
 
 		/**
 		 * Base name of the page.
 		 */
 		private final String baseName;
 
 		/**
 		 * Full name of the page.
 		 */
 		private final String fullName;
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name. The base page name, namespace and namespace ID are filled
 		 * in after getting the list of namespaces from the wiki represented by
 		 * the enclosing <tt>MediaWiki</tt>. The page ID is considered to be
 		 * unknown.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName) throws IOException {
 			this(fullPageName, (Long) null);
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and a base page name. The namespace and namespace ID are
 		 * filled in after getting the list of namespaces from the wiki
 		 * represented by the enclosing <tt>MediaWiki</tt>. The page ID is
 		 * considered to be unknown.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param basePageName
 		 *            Base page name, excluding namespace prefix.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final String basePageName) throws IOException {
 			this(fullPageName, basePageName, (Long) null);
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and namespace ID. The base page name and namespace are
 		 * filled in after getting the list of namespaces from the wiki
 		 * represented by the enclosing <tt>MediaWiki</tt>. The page ID is
 		 * considered to be unknown.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param namespaceID
 		 *            ID of the namespace containing the page.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final long namespaceID) throws IOException {
 			this(fullPageName, namespaceID, (Long) null);
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and a base page name. The namespace is filled in after
 		 * getting the list of namespaces from the wiki represented by the
 		 * enclosing <tt>MediaWiki</tt>. The page ID is considered to be
 		 * unknown.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param basePageName
 		 *            Base page name, excluding namespace prefix.
 		 * @param namespaceID
 		 *            ID of the namespace containing the page.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final String basePageName, final long namespaceID) throws IOException {
 			this(fullPageName, basePageName, namespaceID, (Long) null);
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name. The base page name, namespace and namespace ID are filled
 		 * in after getting the list of namespaces from the wiki represented by
 		 * the enclosing <tt>MediaWiki</tt>.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param pageID
 		 *            Page ID, or <code>null</code> if not known.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final Long pageID) throws IOException {
 			fullName = fullPageName;
 			MediaWiki.Namespaces namespaces = getNamespaces();
 			baseName = namespaces.removeNamespacePrefix(fullPageName);
 			namespace = namespaces.getNamespaceForPage(fullPageName);
 			namespaceID = namespace.getID();
 			this.pageID = pageID;
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and a base page name. The namespace and namespace ID are
 		 * filled in after getting the list of namespaces from the wiki
 		 * represented by the enclosing <tt>MediaWiki</tt>.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param basePageName
 		 *            Base page name, excluding namespace prefix.
 		 * @param pageID
 		 *            Page ID, or <code>null</code> if not known.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final String basePageName, final Long pageID) throws IOException {
 			fullName = fullPageName;
 			MediaWiki.Namespaces namespaces = getNamespaces();
 			baseName = basePageName;
 			namespace = namespaces.getNamespaceForPage(fullPageName);
 			namespaceID = namespace.getID();
 			this.pageID = pageID;
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and namespace ID. The base page name and namespace are
 		 * filled in after getting the list of namespaces from the wiki
 		 * represented by the enclosing <tt>MediaWiki</tt>.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param namespaceID
 		 *            ID of the namespace containing the page.
 		 * @param pageID
 		 *            Page ID, or <code>null</code> if not known.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final long namespaceID, final Long pageID) throws IOException {
 			fullName = fullPageName;
 			MediaWiki.Namespaces namespaces = getNamespaces();
 			baseName = namespaces.removeNamespacePrefix(fullPageName);
 			namespace = namespaces.getNamespace(namespaceID);
 			this.namespaceID = namespaceID;
 			this.pageID = pageID;
 		}
 
 		/**
 		 * Initialises an instance of <tt>PageNameComponents</tt> from a full
 		 * page name and a base page name. The namespace is filled in after
 		 * getting the list of namespaces from the wiki represented by the
 		 * enclosing <tt>MediaWiki</tt>.
 		 * 
 		 * @param fullPageName
 		 *            Full page name, including namespace prefix.
 		 * @param basePageName
 		 *            Base page name, excluding namespace prefix.
 		 * @param namespaceID
 		 *            ID of the namespace containing the page.
 		 * @param pageID
 		 *            Page ID, or <code>null</code> if not known.
 		 * @throws IOException
 		 *             if gathering the list of namespaces from the wiki
 		 *             represented by the enclosing <tt>MediaWiki</tt> raises
 		 *             <tt>IOException</tt>
 		 */
 		public PageNameComponents(final String fullPageName, final String basePageName, final long namespaceID, final Long pageID) throws IOException {
 			fullName = fullPageName;
 			MediaWiki.Namespaces namespaces = getNamespaces();
 			baseName = basePageName;
 			namespace = namespaces.getNamespace(namespaceID);
 			this.namespaceID = namespaceID;
 			this.pageID = pageID;
 		}
 
 		/**
 		 * Returns the full page name for which this <tt>PageNameComponents</tt>
 		 * was created. The full page name consists of the namespace's prefix
 		 * followed by a colon, if it is not the <i>main namespace</i>, followed
 		 * by the base page name.
 		 * 
 		 * @return the full page name for which this <tt>PageNameComponents</tt>
 		 *         was created
 		 * @see #getBasePageName()
 		 * @see MediaWiki.StandardNamespace#MAIN
 		 */
 		public String getFullPageName() {
 			return fullName;
 		}
 
 		/**
 		 * Returns the base page name for which this <tt>PageNameComponents</tt>
 		 * was created.
 		 * 
 		 * @return the base page name for which this <tt>PageNameComponents</tt>
 		 *         was created
 		 */
 		public String getBasePageName() {
 			return baseName;
 		}
 
 		/**
 		 * Returns the namespace containing the page for which this
 		 * <tt>PageNameComponents</tt> was created.
 		 * 
 		 * @return the namespace containing the page for which this
 		 *         <tt>PageNameComponents</tt> was created
 		 */
 		public MediaWiki.Namespace getNamespace() {
 			return namespace;
 		}
 
 		/**
 		 * Returns the ID of the namespace containing the page for which this
 		 * <tt>PageNameComponents</tt> was created.
 		 * <p>
 		 * This is a convenience method for <code>getNamespace().getID()</code>.
 		 * 
 		 * @return the ID of the namespace containing the page for which this
 		 *         <tt>PageNameComponents</tt> was created
 		 */
 		public long getNamespaceID() {
 			return namespaceID;
 		}
 
 		/**
 		 * Returns the ID of the page for which this <tt>PageNameComponents</tt>
 		 * was created.
 		 * <p>
 		 * The ID may be unknown because it was not provided by the API reply
 		 * that created this <tt>PageNameComponents</tt>. In that case, the
 		 * return value is <code>null</code>, and one may get the page ID using
 		 * <code>getPageInformation</code> on the enclosing <tt>MediaWiki</tt>
 		 * instance.
 		 * 
 		 * @return the ID of the page for which this <tt>PageNameComponents</tt>
 		 *         was created, or <code>null</code> if it is unknown
 		 */
 		public Long getPageID() {
 			return pageID;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("PageNameComponents[\"%s\"%s]", fullName, pageID != null ? " (ID " + pageID + ")" : "");
 		}
 	}
 
 	public class Page extends PageNameComponents {
 		private final boolean missing;
 
 		private final Date lastEdit;
 
 		private final long lastRevisionID;
 
 		private final long views;
 
 		private final long length;
 
 		private final boolean isRedirect;
 
 		private final boolean isNew;
 
 		private final Map<String, MediaWiki.Protection> protections;
 
 		Page(final boolean missing, final long pageID, final String fullName, final Date lastEdit, final long namespaceID, final long lastRevisionID, final long views, final long length, final boolean isRedirect, final boolean isNew, final Map<String, MediaWiki.Protection> protections) throws IOException {
 			super(fullName, namespaceID, missing ? null : pageID);
 			this.missing = missing;
 			this.lastEdit = lastEdit;
 			this.lastRevisionID = lastRevisionID;
 			this.views = views;
 			this.length = length;
 			this.isRedirect = isRedirect;
 			this.isNew = isNew;
 			this.protections = Collections.unmodifiableMap(protections);
 		}
 
 		/**
 		 * Returns <code>true</code> if the page designated by this
 		 * <tt>Page</tt> is missing, or <code>false</code> if it exists. Most
 		 * other attributes will also be missing if this is the case; however,
 		 * information about page creation protection (if applicable), and the
 		 * namespace and title of the page will still be available.
 		 * 
 		 * @return <code>true</code> if the page designated by this
 		 *         <tt>Page</tt> is missing; <code>false</code> if it exists
 		 */
 		public boolean isMissing() {
 			return missing;
 		}
 
 		/**
 		 * Returns the date and time at which the page designated by this
 		 * <tt>Page</tt> was last edited. The return value is <code>null</code>
 		 * if the page is missing.
 		 * 
 		 * @return the date and time at which the page designated by this
 		 *         <tt>Page</tt> was last edited
 		 * @see #isMissing()
 		 */
 		public Date getLastEdit() {
 			return lastEdit;
 		}
 
 		/**
 		 * Returns the ID of the last revision made to the page designated by
 		 * this <tt>Page</tt>. The return value is -1 if the page is missing.
 		 * 
 		 * @return the ID of the last revision made to the page designated by
 		 *         this <tt>Page</tt>
 		 * @see #isMissing()
 		 */
 		public long getLastRevisionID() {
 			return lastRevisionID;
 		}
 
 		/**
 		 * Returns the number of times the page designated by this <tt>Page</tt>
 		 * has been viewed. The return value is -1 if the page is missing, or if
 		 * the wiki represented by the enclosing <tt>MediaWiki</tt> does not
 		 * give this information.
 		 * 
 		 * @return the number of times the page designated by this <tt>Page</tt>
 		 *         has been viewed
 		 */
 		public long getViewCount() {
 			return views;
 		}
 
 		/**
 		 * Returns the length the page designated by this <tt>Page</tt> as of
 		 * its last revision. The return value is 0 if the page is missing.
 		 * 
 		 * @return the length the page designated by this <tt>Page</tt> as of
 		 *         its last revision
 		 * @see #isMissing()
 		 */
 		public long getLength() {
 			return length;
 		}
 
 		/**
 		 * Returns <code>true</code> if the page designated by this
 		 * <tt>Page</tt> exists and is a redirect to another page, and
 		 * <code>false</code> otherwise.
 		 * <p>
 		 * Redirect pages start with <code>"#REDIRECT"</code> followed by a
 		 * wikilink to the targetted page.
 		 * 
 		 * @return <code>true</code> if the page designated by this
 		 *         <tt>Page</tt> exists and is a redirect to another page, and
 		 *         <code>false</code> otherwise
 		 */
 		public boolean isRedirect() {
 			return isRedirect;
 		}
 
 		/**
 		 * Returns <code>true</code> if the page designated by this
 		 * <tt>Page</tt> exists and has one revision, and <code>false</code>
 		 * otherwise.
 		 * 
 		 * @return <code>true</code> if the page designated by this
 		 *         <tt>Page</tt> exists and has one revision, and
 		 *         <code>false</code> otherwise
 		 */
 		public boolean isNew() {
 			return isNew;
 		}
 
 		/**
 		 * Returns a map associating action names (such as those in
 		 * <tt>MediaWiki.ProtectionAction</tt>) to <tt>MediaWiki.Protection</tt>
 		 * objects describing the terms of their protection. The return value is
 		 * never <code>null</code>, but the map may be empty.
 		 * 
 		 * @return a map associating action names (such as those in
 		 *         <tt>MediaWiki.ProtectionAction</tt>) to
 		 *         <tt>MediaWiki.Protection</tt> objects describing the terms of
 		 *         their protection
 		 */
 		public Map<String, MediaWiki.Protection> getProtection() {
 			return protections;
 		}
 
 		@Override
 		public String toString() {
 			// Page["Main Page" (missing) (ID 1), redirect, new, 410 bytes
 			// long, last revision: 5207, edited on DATE, views: 61358,
 			// protections: {edit=...}]
 			return String.format("Page[\"%s\"%s%s%s%s%s%s%s%s, protections: %s]", getFullPageName(), missing ? " (missing)" : "", getPageID() != null ? " (ID " + getPageID() + ")" : "", isRedirect ? ", redirect" : "", isNew ? ", new" : "", ", " + length + " bytes long", lastRevisionID != -1 ? ", last revision: " + lastRevisionID : "", lastEdit != null ? ", edited on " + lastEdit.toString() : "", views != -1 ? ", views: " + views : "", protections);
 		}
 	}
 
 	public class PageDesignation extends PageNameComponents {
 		PageDesignation(final long pageID, final String fullName, final long namespaceID) throws IOException {
 			super(fullName, namespaceID, pageID);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("PageDesignation[\"%s\" (ID %d)]", getFullPageName(), getPageID());
 		}
 	}
 
 	public class Protection implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private final String level;
 
 		private final Date expiry;
 
 		private final boolean isCascading;
 
 		private final String cascadeSource;
 
 		public Protection(final String level, final Date expiry, final boolean isCascading, final String cascadeSource) {
 			this.level = level;
 			this.expiry = expiry;
 			this.isCascading = isCascading;
 			this.cascadeSource = cascadeSource;
 		}
 
 		/**
 		 * Returns the level required for users to be able to perform the
 		 * protected action for which this <tt>Protection</tt> was created. The
 		 * return value is equivalent to, but not necessarily the same object
 		 * as, either <code>MediaWiki.ProtectionLevel.AUTOCONFIRMED_USERS</code>
 		 * or <code>MediaWiki.ProtectionLevel.SYSOPS</code>.
 		 * 
 		 * @return the level required for users to be able to perform the
 		 *         protected action for which this <tt>Protection</tt> was
 		 *         created
 		 */
 		public String getLevel() {
 			return level;
 		}
 
 		/**
 		 * Returns the date and time at which the protection for which this
 		 * <tt>Protection</tt> was created is set to expire. After this, the
 		 * protected action may be performed by everyone. The return value is
 		 * <code>null</code> if the protection is set never to expire.
 		 * 
 		 * @return the date and time at which the protection for which this
 		 *         <tt>Protection</tt> was created is set to expire
 		 */
 		public Date getExpiry() {
 			return expiry;
 		}
 
 		/**
 		 * Returns whether the protection for which this <tt>Protection</tt> was
 		 * created is cascading, i.e. whether pages transcluded into the
 		 * protected page are also to be protected. For pages that are protected
 		 * <em>due to</em> cascade protection on another page, this method
 		 * returns <code>false</code> and <code>getCascadeSource</code> returns
 		 * the source of the protection.
 		 * 
 		 * @return whether the protection for which this <tt>Protection</tt> was
 		 *         created is cascading
 		 */
 		public boolean isCascading() {
 			return isCascading;
 		}
 
 		/**
 		 * Returns the full name of the page that caused the action for which
 		 * this <tt>Protection</tt> was created to be protected. The return
 		 * value is <code>null</code> if there is no such page.
 		 * 
 		 * @return whether the protection for which this <tt>Protection</tt> was
 		 *         created is cascading
 		 */
 		public String getCascadeSource() {
 			return cascadeSource;
 		}
 
 		@Override
 		public String toString() {
 			// Protection[sysop expiring DATE, cascading, from "Other page"]
 			return String.format("Protection[%s expiring %s%s%s]", level, expiry != null ? expiry.toString() : "never", isCascading ? ", cascading" : "", cascadeSource != null ? ", from \"" + cascadeSource + "\"" : "");
 		}
 	}
 
 	public class Revision {
 		private final long revisionID;
 
 		private final long parentID;
 
 		private final Date timestamp;
 
 		private final String userName;
 
 		private final boolean userHidden;
 
 		private final long length;
 
 		private final String comment;
 
 		private final boolean commentHidden;
 
 		private final boolean isMinor;
 
 		private final boolean isAnonymous;
 
 		private String content;
 
 		private boolean contentHidden;
 
 		private boolean contentStored;
 
 		Revision(final long revisionID, final long parentID, final Date timestamp, final String userName, final boolean userHidden, final long length, final String comment, final boolean commentHidden, final boolean isMinor, final boolean isAnonymous) {
 			this.revisionID = revisionID;
 			this.parentID = parentID;
 			this.timestamp = timestamp;
 			this.userName = userName;
 			this.userHidden = userHidden;
 			this.length = length;
 			this.comment = comment;
 			this.commentHidden = commentHidden;
 			this.isMinor = isMinor;
 			this.isAnonymous = isAnonymous;
 		}
 
 		/**
 		 * Returns the ID of the revision for which this <tt>Revision</tt> was
 		 * created. The return value is guaranteed to be greater than or equal
 		 * to 0, and to be known even if the revision is deleted.
 		 * 
 		 * @return the ID of the revision for which this <tt>Revision</tt> was
 		 *         created
 		 */
 		public long getRevisionID() {
 			return revisionID;
 		}
 
 		/**
 		 * Returns the ID of the revision before the one for which this
 		 * <tt>Revision</tt> was created, from the same page. The return value
 		 * is greater than or equal to 0 if known, or -1 if unknown. It can be
 		 * known even if the revision is deleted. The return value is 0 if this
 		 * revision created its page.
 		 * 
 		 * @return the ID of the revision before the one for which this
 		 *         <tt>Revision</tt> was created, from the same page
 		 */
 		public long getParentID() {
 			return parentID;
 		}
 
 		/**
 		 * Returns the timestamp at which the revision for which this
 		 * <tt>Revision</tt> was created was made. The return value is
 		 * guaranteed to be non-<code>null</code>, even if the revision is
 		 * deleted.
 		 * 
 		 * @return the timestamp at which the revision for which this
 		 *         <tt>Revision</tt> was created was made
 		 */
 		public Date getTimestamp() {
 			return timestamp;
 		}
 
 		/**
 		 * Returns the name of the user who made the revision for which this
 		 * <tt>Revision</tt> was created. The return value is <code>null</code>
 		 * if the revision is deleted with the username hidden.
 		 * 
 		 * @return the name of the user who made the revision for which this
 		 *         <tt>Revision</tt> was created
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns whether the name of the user who made the revision for which
 		 * this <tt>Revision</tt> was created is hidden by revision deletion.
 		 * 
 		 * @return whether the name of the user who made the revision for which
 		 *         this <tt>Revision</tt> was created is hidden by revision
 		 *         deletion
 		 */
 		public boolean isUserNameHidden() {
 			return userHidden;
 		}
 
 		/**
 		 * Returns the length of the page as of the revision for which this
 		 * <tt>Revision</tt> was created.
 		 * 
 		 * @return the length of the page as of the revision for which this
 		 *         <tt>Revision</tt> was created
 		 */
 		public long getLength() {
 			return length;
 		}
 
 		/**
 		 * Returns the comment for the revision for which this <tt>Revision</tt>
 		 * was created. The return value is <code>null</code> if the revision is
 		 * deleted with the comment hidden.
 		 * 
 		 * @return the comment for the revision for which this <tt>Revision</tt>
 		 *         was created
 		 */
 		public String getComment() {
 			return comment;
 		}
 
 		/**
 		 * Returns whether the comment for the revision for which this
 		 * <tt>Revision</tt> was created is hidden by revision deletion.
 		 * 
 		 * @return whether the comment for the revision for which this
 		 *         <tt>Revision</tt> was created is hidden by revision deletion
 		 */
 		public boolean isCommentHidden() {
 			return commentHidden;
 		}
 
 		/**
 		 * Returns <code>true</code> if any piece of information about the
 		 * revision for which this <tt>Revision</tt> was created is deleted.
 		 * 
 		 * @return <code>true</code> if any piece of information about the
 		 *         revision for which this <tt>Revision</tt> was created is
 		 *         deleted
 		 */
 		public boolean isDeleted() {
 			return userHidden || commentHidden || (contentStored && contentHidden);
 		}
 
 		/**
 		 * Returns whether the revision for which this <tt>Revision</tt> was
 		 * created was marked by the user who made it as <i>minor</i>.
 		 * 
 		 * @return whether the revision for which this <tt>Revision</tt> was
 		 *         created was marked by the user who made it as <i>minor</i>
 		 */
 		public boolean isMinor() {
 			return isMinor;
 		}
 
 		/**
 		 * Returns whether the revision for which this <tt>Revision</tt> was
 		 * created was made by an anonymous user. The return value is undefined
 		 * if the user's name is hidden by revision deletion.
 		 * 
 		 * @return whether the revision for which this <tt>Revision</tt> was
 		 *         created was made by an anonymous user
 		 */
 		public boolean isAnonymous() {
 			return isAnonymous;
 		}
 
 		/**
 		 * Returns the content of this revision. If no exception is thrown, the
 		 * return value is cached in this <tt>Revision</tt> so it can be
 		 * returned later. The return value is undefined if the content is
 		 * hidden by revision deletion.
 		 * 
 		 * @return the content of this revision
 		 * @throws IOException
 		 *             if <tt>IOException</tt> is thrown while connecting to the
 		 *             wiki or while reading the XML reply from the API
 		 * @throws MediaWiki.MediaWikiException
 		 *             if the API does not return a result in the expected
 		 *             format (subtypes thrown:
 		 *             <tt>MediaWiki.MediaWikiException</tt>,
 		 *             <tt>MediaWiki.UnknownError</tt>)
 		 */
 		public synchronized String getContent() throws IOException, MediaWiki.MediaWikiException {
 			if (contentStored)
 				return content;
 			else {
 				storeContent();
 				return getContent();
 			}
 		}
 
 		/**
 		 * Returns whether the content of this revision is hidden by revision
 		 * deletion.
 		 * 
 		 * @return whether the content of this revision is hidden by revision
 		 *         deletion
 		 * @throws IOException
 		 *             if <tt>IOException</tt> is thrown while connecting to the
 		 *             wiki or while reading the XML reply from the API
 		 * @throws MediaWiki.MediaWikiException
 		 *             if the API does not return a result in the expected
 		 *             format (subtypes thrown:
 		 *             <tt>MediaWiki.MediaWikiException</tt>,
 		 *             <tt>MediaWiki.UnknownError</tt>)
 		 */
 		public synchronized boolean isContentHidden() throws IOException, MediaWiki.MediaWikiException {
 			if (contentStored)
 				return contentHidden;
 			else {
 				storeContent();
 				return isContentHidden();
 			}
 		}
 
 		protected synchronized void storeContent() throws IOException, MediaWiki.MediaWikiException {
 			final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "revisions", "rvprop", "content", "revids", Long.toString(revisionID));
 
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList badrevidsTags = xml.getElementsByTagName("badrevids");
 				if (badrevidsTags.getLength() > 0)
 					throw new MediaWiki.MediaWikiException("Revision ID " + revisionID + " is now inexistent");
 
 				final NodeList pageTags = xml.getElementsByTagName("page");
 
 				if (pageTags.getLength() > 0) {
 					final Element pageTag = (Element) pageTags.item(0);
 
 					if (pageTag.hasAttribute("missing"))
 						throw new MediaWiki.MediaWikiException("Revision ID " + revisionID + " now belongs to no page");
 
 					final NodeList revTags = pageTag.getElementsByTagName("rev");
 
 					if (revTags.getLength() > 0) {
 						final Element revTag = (Element) revTags.item(0);
 
 						if (revTag.hasAttribute("texthidden")) {
 							contentHidden = true;
 						} else {
 							content = revTag.getTextContent();
 						}
 						contentStored = true;
 					} else
 						throw new MediaWiki.ResponseFormatException("expected <rev> tag not found");
 				} else
 					throw new MediaWiki.ResponseFormatException("expected <page> tag not found");
 			} finally {
 				networkLock.unlock();
 			}
 		}
 
 		@Override
 		public String toString() {
 			// Revision[1337 <- 1336 @ DATE (SIZE) by <user hidden>
 			// or USER, minor, anonymous <comment hidden> or (COMMENT)]
 			return String.format("Revision[%d <- %d (%d bytes) @ %s by %s%s%s %s]", revisionID, parentID, length, timestamp, userHidden ? "<user hidden>" : userName, isMinor ? ", minor" : "", isAnonymous ? ", anonymous" : "", commentHidden ? "<comment hidden>" : "(" + comment + ")");
 		}
 	}
 
 	public class CategoryMembership {
 		private final String category;
 
 		private final String sortKey;
 
 		CategoryMembership(final String category, final String sortKey) {
 			this.category = category;
 			this.sortKey = sortKey;
 		}
 
 		/**
 		 * Returns the full name of the category to which a page belongs.
 		 * 
 		 * @return the full name of the category to which a page belongs
 		 */
 		public String getCategoryName() {
 			return category;
 		}
 
 		/**
 		 * Returns the base name of the category to which a page belongs; that
 		 * is, the category's name without its namespace prefix.
 		 * 
 		 * @return the base name of the category to which a page belongs
 		 * @throws IOException
 		 *             if an error occurs while getting the list of namespaces
 		 *             on the wiki represented by the enclosing
 		 *             <tt>MediaWiki</tt>
 		 */
 		public String getCategoryBaseName() throws IOException {
 			return getNamespaces().removeNamespacePrefix(category);
 		}
 
 		/**
 		 * Returns the name of the page as it is used to sort it inside the
 		 * category's members.
 		 * 
 		 * @return the name of the page as it is used to sort it inside the
 		 *         category's members
 		 */
 		public String getSortKey() {
 			return sortKey;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("CategoryMembership[\"%s\" as \"%s\"]", category, sortKey);
 		}
 	}
 
 	public class CategoryMember extends PageNameComponents {
 		private final String sortKey;
 
 		private final Date addTime;
 
 		CategoryMember(final long namespaceID, final long pageID, final Date addTime, final String fullPageName, final String sortKey) throws IOException {
 			super(fullPageName, namespaceID, pageID);
 			this.addTime = addTime;
 			this.sortKey = sortKey;
 		}
 
 		/**
 		 * Returns the name of the page as it is used to sort it inside the
 		 * category's members.
 		 * 
 		 * @return the name of the page as it is used to sort it inside the
 		 *         category's members
 		 */
 		public String getSortKey() {
 			return sortKey;
 		}
 
 		/**
 		 * Returns the time when this page was added to the category of which it
 		 * is a member.
 		 * 
 		 * @return the time when this page was added to the category of which it
 		 *         is a member
 		 */
 		public Date getAdditionTime() {
 			return addTime;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("CategoryMember[\"%s\" (ID %d) as \"%s\", added %s]", getFullPageName(), getPageID(), sortKey, addTime);
 		}
 	}
 
 	public class ImageRevision extends PageNameComponents {
 		private final Date timestamp;
 
 		private final String userName;
 
 		private final long length;
 
 		private final long width;
 
 		private final long height;
 
 		private final String url;
 
 		private final String comment;
 
 		private final String sha1hash;
 
 		private final String mimeType;
 
 		ImageRevision(final String fullName, final Date timestamp, final String userName, final long length, final long width, final long height, final String url, final String comment, final String sha1hash, final String mimeType) throws IOException {
 			super(fullName);
 			this.timestamp = timestamp;
 			this.userName = userName;
 			this.length = length;
 			this.width = width;
 			this.height = height;
 			this.url = url;
 			this.comment = comment;
 			this.sha1hash = sha1hash;
 			this.mimeType = mimeType;
 		}
 
 		/**
 		 * Returns the timestamp at which the image revision for which this
 		 * <tt>ImageRevision</tt> was created was made. The return value is
 		 * guaranteed to be non-<code>null</code>, even if the revision is
 		 * deleted.
 		 * 
 		 * @return the timestamp at which the revision for which this
 		 *         <tt>Revision</tt> was created was made
 		 */
 		public Date getTimestamp() {
 			return timestamp;
 		}
 
 		/**
 		 * Returns the name of the user who made the image revision for which
 		 * this <tt>Revision</tt> was created. The return value is
 		 * <code>null</code> if the revision is deleted with the username
 		 * hidden.
 		 * 
 		 * @return the name of the user who made the revision for which this
 		 *         <tt>Revision</tt> was created
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns the length, in bytes, of the image as of the revision for
 		 * which this <tt>ImageRevision</tt> was created.
 		 * 
 		 * @return the length, in bytes, of the image as of the revision for
 		 *         which this <tt>Revision</tt> was created
 		 */
 		public long getLength() {
 			return length;
 		}
 
 		/**
 		 * Returns the width, in pixels, of the image as of the revision for
 		 * which this <tt>ImageRevision</tt> was created.
 		 * 
 		 * @return the width, in pixels, of the image as of the revision for
 		 *         which this <tt>ImageRevision</tt> was created
 		 */
 		public long getWidth() {
 			return width;
 		}
 
 		/**
 		 * Returns the height, in pixels, of the image as of the revision for
 		 * which this <tt>ImageRevision</tt> was created.
 		 * 
 		 * @return the height, in pixels, of the image as of the revision for
 		 *         which this <tt>ImageRevision</tt> was created
 		 */
 		public long getHeight() {
 			return height;
 		}
 
 		/**
 		 * Returns the URL at which the contents of the image as of the revision
 		 * for which this <tt>ImageRevision</tt> was created can be retrieved.
 		 * 
 		 * @return the URL at which the contents of the image as of the revision
 		 *         for which this <tt>ImageRevision</tt> was created can be
 		 *         retrieved
 		 */
 		public String getURL() {
 			return url;
 		}
 
 		/**
 		 * Returns the comment for the revision for which this <tt>Revision</tt>
 		 * was created. The return value is <code>null</code> if the revision is
 		 * deleted with the comment hidden.
 		 * 
 		 * @return the comment for the revision for which this <tt>Revision</tt>
 		 *         was created
 		 */
 		public String getComment() {
 			return comment;
 		}
 
 		/**
 		 * Returns the SHA-1 hash of the contents of the image as of the
 		 * revision for which this <tt>ImageRevision</tt> was created. The
 		 * return value is a hexadecimal string.
 		 * 
 		 * @return the SHA-1 hash of the contents of the image as of the
 		 *         revision for which this <tt>ImageRevision</tt> was created
 		 */
 		public String getSHA1Hash() {
 			return sha1hash;
 		}
 
 		/**
 		 * Returns the MIME type of the contents of the image as of the revision
 		 * for which this <tt>ImageRevision</tt> was created. The return value
 		 * is a hexadecimal string.
 		 * 
 		 * @return the SHA-1 hash of the contents of the image as of the
 		 *         revision for which this <tt>ImageRevision</tt> was created
 		 */
 		public String getMIMEType() {
 			return mimeType;
 		}
 
 		/**
 		 * Returns an <tt>InputStream</tt> that reads the content of the image
 		 * as of this revision.
 		 * 
 		 * @return an <tt>InputStream</tt> that reads the content of the image
 		 *         as of this revision
 		 * @throws IOException
 		 *             if <tt>IOException</tt> is thrown while connecting to the
 		 *             wiki
 		 */
 		public InputStream getContent() throws IOException {
 			return get(url);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("ImageRevision[%s (%d bytes), %dx%d, by %s (%s) <%s>, MIME type: %s, SHA-1 hash: %s]", timestamp, length, width, height, userName, comment, url, mimeType, sha1hash);
 		}
 	}
 
 	public class InterlanguageLink {
 		private final String language;
 
 		private final String foreignTitle;
 
 		InterlanguageLink(final String language, final String foreignTitle) {
 			this.language = language;
 			this.foreignTitle = foreignTitle;
 		}
 
 		/**
 		 * Returns the language identifier specified in the interlanguage link
 		 * for which this <tt>InterlanguageLink</tt> was created.
 		 * 
 		 * @return the language identifier specified in the interlanguage link
 		 *         for which this <tt>InterlanguageLink</tt> was created
 		 */
 		public String getLanguage() {
 			return language;
 		}
 
 		/**
 		 * Returns the <tt>InterwikiPrefix</tt> corresponding to the language of
 		 * the interlanguage link for which this <tt>InterlanguageLink</tt> was
 		 * created, drawn from the enclosing <tt>MediaWiki</tt>'s interwiki
 		 * prefix map.
 		 * 
 		 * @return the <tt>InterwikiPrefix</tt> corresponding to the language of
 		 *         the interlanguage link for which this
 		 *         <tt>InterlanguageLink</tt> was created
 		 * @throws IOException
 		 *             if an error occurs while getting the interwiki prefixes
 		 *             on the wiki represented by the enclosing
 		 *             <tt>MediaWiki</tt>
 		 */
 		public MediaWiki.InterwikiPrefix getLanguagePrefix() throws IOException {
 			return getInterwikiPrefixes().getInterwikiPrefix(language);
 		}
 
 		/**
 		 * Returns the full name of the article on the other wiki.
 		 * 
 		 * @return the full name of the article on the other wiki
 		 */
 		public String getForeignTitle() {
 			return foreignTitle;
 		}
 
 		/**
 		 * Returns the URL at which the article on the other wiki can be seen in
 		 * a browser.
 		 * 
 		 * @return the URL at which the article on the other wiki can be seen in
 		 *         a browser
 		 * @throws IOException
 		 *             if an error occurs while getting the interwiki prefixes
 		 *             on the wiki represented by the enclosing
 		 *             <tt>MediaWiki</tt>
 		 */
 		public String getURL() throws IOException {
 			return getLanguagePrefix().resolveURL(foreignTitle);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("InterlanguageLink[%s:\"%s\"]", language, foreignTitle);
 		}
 	}
 
 	public class Link extends PageNameComponents {
 		Link(final long namespace, final String fullPageName) throws IOException {
 			super(fullPageName, namespace);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Link[\"%s\"]", getFullPageName());
 		}
 	}
 
 	public class Category extends PageNameComponents {
 		private final long entries;
 
 		private final long pages;
 
 		private final long files;
 
 		private final long subcategories;
 
 		Category(final String fullName, final long entries, final long pages, final long files, final long subcategories) throws IOException {
 			super(fullName);
 			this.entries = entries;
 			this.pages = pages;
 			this.files = files;
 			this.subcategories = subcategories;
 		}
 
 		/**
 		 * Returns the number of entries in the category for which this
 		 * <tt>Category</tt> was created. This includes pages, files and
 		 * subcategories.
 		 * 
 		 * @return the number of entries in the category for which this
 		 *         <tt>Category</tt> was created
 		 */
 		public long getEntryCount() {
 			return entries;
 		}
 
 		/**
 		 * Returns the number of pages in the category for which this
 		 * <tt>Category</tt> was created.
 		 * 
 		 * @return the number of pages in the category for which this
 		 *         <tt>Category</tt> was created
 		 */
 		public long getPageCount() {
 			return pages;
 		}
 
 		/**
 		 * Returns the number of files in the category for which this
 		 * <tt>Category</tt> was created.
 		 * 
 		 * @return the number of files in the category for which this
 		 *         <tt>Category</tt> was created
 		 */
 		public long getFileCount() {
 			return files;
 		}
 
 		/**
 		 * Returns the number of categories placed in the category for which
 		 * this <tt>Category</tt> was created. Those are its sub-categories.
 		 * 
 		 * @return the number of categories placed in the category for which
 		 *         this <tt>Category</tt> was created
 		 */
 		public long getSubcategoryCount() {
 			return subcategories;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("Category[\"%s\", entries: %d, pages: %d, files: %d, subcategories: %d]", getFullPageName(), entries, pages, files, subcategories);
 		}
 	}
 
 	public static class ParseResult {
 		private final String text;
 
 		private final List<MediaWiki.InterlanguageLink> interlanguageLinks;
 
 		private final List<MediaWiki.CategoryMembership> categoryMemberships;
 
 		private final List<MediaWiki.Link> pageLinks;
 
 		private final List<MediaWiki.Link> transclusions;
 
 		private final List<MediaWiki.Link> images;
 
 		private final List<String> externalLinks;
 
 		ParseResult(final String text, final List<MediaWiki.InterlanguageLink> interlanguageLinks, final List<MediaWiki.CategoryMembership> categoryMemberships, final List<MediaWiki.Link> pageLinks, final List<MediaWiki.Link> transclusions, final List<MediaWiki.Link> images, final List<String> externalLinks) {
 			this.text = text;
 			this.interlanguageLinks = interlanguageLinks != null ? Collections.unmodifiableList(interlanguageLinks) : null;
 			this.categoryMemberships = categoryMemberships != null ? Collections.unmodifiableList(categoryMemberships) : null;
 			this.pageLinks = pageLinks != null ? Collections.unmodifiableList(pageLinks) : null;
 			this.transclusions = transclusions != null ? Collections.unmodifiableList(transclusions) : null;
 			this.images = images != null ? Collections.unmodifiableList(images) : null;
 			this.externalLinks = externalLinks != null ? Collections.unmodifiableList(externalLinks) : null;
 		}
 
 		/**
 		 * Returns the HTML parsed version of the wikitext for which this
 		 * <tt>ParseResult</tt> was created.
 		 * 
 		 * @return the HTML parsed version of the wikitext for which this
 		 *         <tt>ParseResult</tt> was created
 		 */
 		public String getText() {
 			return text;
 		}
 
 		/**
 		 * Returns the list of interlanguage links contained in the wikitext for
 		 * which this <tt>ParseResult</tt> was created. The returned list is not
 		 * modifiable.
 		 * 
 		 * @return the list of interlanguage links contained in the wikitext for
 		 *         which this <tt>ParseResult</tt> was created
 		 */
 		public List<MediaWiki.InterlanguageLink> getInterlanguageLinks() {
 			return interlanguageLinks;
 		}
 
 		/**
 		 * Returns the list of categorisations contained in the wikitext for
 		 * which this <tt>ParseResult</tt> was created. The returned list is not
 		 * modifiable.
 		 * 
 		 * @return the list of categorisations contained in the wikitext for
 		 *         which this <tt>ParseResult</tt> was created
 		 */
 		public List<MediaWiki.CategoryMembership> getCategoryMemberships() {
 			return categoryMemberships;
 		}
 
 		/**
 		 * Returns the list of links pointing to pages on the same wiki
 		 * contained in the wikitext for which this <tt>ParseResult</tt> was
 		 * created. The returned list is not modifiable.
 		 * 
 		 * @return the list of links pointing to pages on the same wiki
 		 *         contained in the wikitext for which this <tt>ParseResult</tt>
 		 *         was created
 		 */
 		public List<MediaWiki.Link> getPageLinks() {
 			return pageLinks;
 		}
 
 		/**
 		 * Returns the list of transclusions contained in the wikitext for which
 		 * this <tt>ParseResult</tt> was created. The returned list is not
 		 * modifiable.
 		 * 
 		 * @return the list of transclusions contained in the wikitext for which
 		 *         this <tt>ParseResult</tt> was created
 		 */
 		public List<MediaWiki.Link> getTransclusions() {
 			return transclusions;
 		}
 
 		/**
 		 * Returns the list of images used in the wikitext for which this
 		 * <tt>ParseResult</tt> was created. The returned list is not
 		 * modifiable.
 		 * 
 		 * @return the list of images used in the wikitext for which this
 		 *         <tt>ParseResult</tt> was created
 		 */
 		public List<MediaWiki.Link> getImages() {
 			return images;
 		}
 
 		/**
 		 * Returns the list of Web links contained in the wikitext for which
 		 * this <tt>ParseResult</tt> was created. The returned list is not
 		 * modifiable.
 		 * 
 		 * @return the list of Web links contained in the wikitext for which
 		 *         this <tt>ParseResult</tt> was created
 		 */
 		public List<String> getExternalLinks() {
 			return externalLinks;
 		}
 	}
 
 	public static class User {
 		private final String userName;
 
 		private final Set<String> groups;
 
 		private final Set<String> rights;
 
 		private final long editCount;
 
 		private final String blockingUser;
 
 		private final String blockReason;
 
 		private final Date registration;
 
 		private final boolean isMissing;
 
 		User(final boolean isMissing, final String userName, final Collection<String> groups, final Collection<String> rights, final long editCount, final String blockingUser, final String blockReason, final Date registration) {
 			this.isMissing = isMissing;
 			this.userName = userName;
 			this.groups = Collections.unmodifiableSet(groups instanceof TreeSet<?> ? (TreeSet<String>) groups : new TreeSet<String>(groups));
 			this.rights = Collections.unmodifiableSet(rights instanceof TreeSet<?> ? (TreeSet<String>) rights : new TreeSet<String>(rights));
 			this.editCount = editCount;
 			this.blockingUser = blockingUser;
 			this.blockReason = blockReason;
 			this.registration = registration;
 		}
 
 		/**
 		 * Returns <code>true</code> if this <tt>User</tt> represents a missing
 		 * user on the wiki represented by the enclosing <tt>MediaWiki</tt>, or
 		 * <code>false</code> if he or she exists. If the return value is
 		 * <code>true</code>, all other methods except <code>getUserName</code>
 		 * will return undefined values.
 		 * 
 		 * @return whether this <tt>User</tt> represents a missing user on the
 		 *         wiki represented by the enclosing <tt>MediaWiki</tt>
 		 */
 		public boolean isMissing() {
 			return isMissing;
 		}
 
 		/**
 		 * Returns the username (not real name) of the user for whom this
 		 * <tt>User</tt> was created. The return value is an IP address if
 		 * <code>isAnonymous()</code> returns <code>true</code>.
 		 * 
 		 * @return the username of the user for whom this <tt>User</tt> was
 		 *         created
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns an immutable view of the list of groups that the user for
 		 * whom this <tt>User</tt> was created user is a part of.
 		 * 
 		 * @return an immutable view of the list of groups that the user for
 		 *         whom this <tt>User</tt> was created is a part of
 		 */
 		public Collection<String> getGroups() {
 			return groups;
 		}
 
 		/**
 		 * Returns an immutable view of the list of rights that the user for
 		 * whom this <tt>User</tt> was created has.
 		 * 
 		 * @return an immutable view of the list of rights that the user for
 		 *         whom this <tt>User</tt> was created has
 		 */
 		public Collection<String> getRights() {
 			return rights;
 		}
 
 		/**
 		 * Returns whether the the user for whom this <tt>User</tt> was created
 		 * is in all of the specified groups. The return value is
 		 * <code>true</code> if no groups are specified.
 		 * 
 		 * @param groups
 		 *            The groups to check membership in.
 		 * @return whether the user for whom this <tt>User</tt> was created is
 		 *         in all of the specified groups
 		 */
 		public boolean isInGroups(final String... groups) {
 			for (final String group : groups) {
 				if (!this.groups.contains(group))
 					return false;
 			}
 			return true;
 		}
 
 		/**
 		 * Returns whether the user for whom this <tt>User</tt> was created has
 		 * all of the specified rights. The return value is <code>true</code> if
 		 * no rights are specified.
 		 * 
 		 * @param rights
 		 *            The rights to check possession of.
 		 * @return whether the user for whom this <tt>User</tt> was created has
 		 *         all of the specified rights
 		 */
 		public boolean hasRights(final String... rights) {
 			for (final String right : rights) {
 				if (!this.rights.contains(right))
 					return false;
 			}
 			return true;
 		}
 
 		/**
 		 * Returns the number of edits made by the user for whom this
 		 * <tt>User</tt> was created on the wiki represented by the enclosing
 		 * <tt>MediaWiki</tt>.
 		 * 
 		 * @return the number of edits made by the user for whom this
 		 *         <tt>User</tt> was created on the wiki represented by the
 		 *         enclosing <tt>MediaWiki</tt>
 		 */
 		public long getEditCount() {
 			return editCount;
 		}
 
 		/**
 		 * Returns the name of the user who blocked the user for whom this
 		 * <tt>User</tt> was created. The return value is <code>null</code> if
 		 * the user for whom this <tt>User</tt> was created is not blocked.
 		 * 
 		 * @return the name of the user who blocked the user for whom this
 		 *         <tt>User</tt> was created
 		 */
 		public String getBlockingUser() {
 			return blockingUser;
 		}
 
 		/**
 		 * Returns the reason why the user for whom this <tt>User</tt> was
 		 * created is blocked. The return value is <code>null</code> if the user
 		 * for whom this <tt>User</tt> was created is not blocked.
 		 * 
 		 * @return the reason why the user for whom this <tt>User</tt> was
 		 *         created is blocked
 		 */
 		public String getBlockReason() {
 			return blockReason;
 		}
 
 		/**
 		 * Returns the date and time at which the user for whom this
 		 * <tt>User</tt> was created registered on the enclosing
 		 * <tt>MediaWiki</tt>. The return value is <code>null</code> if this
 		 * information is unavailable.
 		 * 
 		 * @return the date and time at which the user for whom this
 		 *         <tt>User</tt> was created registered on the enclosing
 		 *         <tt>MediaWiki</tt>
 		 */
 		public Date getRegistrationDate() {
 			return registration;
 		}
 
 		@Override
 		public String toString() {
 			// CurrentUser["Name", groups, rights, Y edits, registered DATE,
 			// blocked by Sysop (Reason)]
 			return String.format("User[\"%s\", groups: %s, %rights: %s, %d edits%s%s]", userName, groups, rights, editCount, registration != null ? ", registered " + registration : "", blockingUser != null ? ", blocked by " + blockingUser + " (" + blockReason + ")" : "");
 		}
 	}
 
 	/**
 	 * An edit token returned by the various <tt>start___</tt> methods, which is
 	 * used to perform the action and detect conflicts.
 	 */
 	public class EditToken implements Serializable, ObjectInputValidation {
 		private static final long serialVersionUID = 1L;
 
 		private final String fullName;
 
 		private final Date lastRevision;
 
 		private final Date start;
 
 		private final String token;
 
 		EditToken(final String fullName, final Date lastRevision, final Date start, final String token) {
 			if (fullName == null || fullName.length() == 0)
 				throw new IllegalArgumentException("fullName may not be null or empty");
 			if (token == null)
 				throw new NullPointerException("token");
 			if (start == null)
 				throw new NullPointerException("start");
 			this.fullName = fullName;
 			this.lastRevision = lastRevision;
 			this.start = start;
 			this.token = token;
 		}
 
 		/**
 		 * Returns the full name of the page to be edited.
 		 * 
 		 * @return the full name of the page to be edited
 		 */
 		public String getFullPageName() {
 			return fullName;
 		}
 
 		/**
 		 * Returns the time at which the edit started, which is used to detect
 		 * page recreation conflicts.
 		 * 
 		 * @return the time at which the edit started
 		 */
 		public Date getStartTime() {
 			return start;
 		}
 
 		/**
 		 * Returns the time at which the page was last edited, which is used to
 		 * detect edit conflicts.
 		 * 
 		 * @return the time at which the page was last edited
 		 */
 		public Date getLastRevisionTime() {
 			return lastRevision;
 		}
 
 		/**
 		 * Returns the text of the token.
 		 * 
 		 * @return the text of the token
 		 */
 		public String getTokenText() {
 			return token;
 		}
 
 		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
 			in.registerValidation(this, 0);
 			in.defaultReadObject();
 		}
 
 		/**
 		 * This method validates deserialized objects and should generally not
 		 * be called by applications.
 		 */
 		public void validateObject() throws InvalidObjectException {
 			if (token == null)
 				throw new InvalidObjectException("token == null");
 			if (start == null)
 				throw new InvalidObjectException("start == null");
 			if (fullName == null || fullName.length() == 0)
 				throw new IllegalArgumentException("fullName may not be null or empty");
 		}
 	}
 
 	/**
 	 * An edit token returned by the <code>startRollback</code> method, which is
 	 * used to perform the action and detect new edits.
 	 */
 	public class RollbackToken implements Serializable, ObjectInputValidation {
 		private static final long serialVersionUID = 1L;
 
 		private final String fullName;
 
 		private final String userName;
 
 		private final String token;
 
 		RollbackToken(final String fullName, final String userName, final String token) {
 			if (fullName == null || fullName.length() == 0)
 				throw new IllegalArgumentException("fullName may not be null or empty");
 			if (userName == null || userName.length() == 0)
 				throw new IllegalArgumentException("userName may not be null or empty");
 			if (token == null)
 				throw new NullPointerException("token");
 			this.fullName = fullName;
 			this.userName = userName;
 			this.token = token;
 		}
 
 		/**
 		 * Returns the full name of the page to be rolled back.
 		 * 
 		 * @return the full name of the page to be rolled back
 		 */
 		public String getFullPageName() {
 			return fullName;
 		}
 
 		/**
 		 * Returns the name of the user who made the last revision to the page
 		 * to be rolled back, and whose edits would therefore be rolled back if
 		 * this <tt>RollbackToken</tt> were used to complete the rollback.
 		 * 
 		 * @return the name of the user who made the last revision to the page
 		 *         to be rolled back
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns the text of the token.
 		 * 
 		 * @return the text of the token
 		 */
 		public String getTokenText() {
 			return token;
 		}
 
 		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
 			in.registerValidation(this, 0);
 			in.defaultReadObject();
 		}
 
 		/**
 		 * This method validates deserialized objects and should generally not
 		 * be called by applications.
 		 */
 		public void validateObject() throws InvalidObjectException {
 			if (token == null)
 				throw new InvalidObjectException("token == null");
 			if (fullName == null || fullName.length() == 0)
 				throw new InvalidObjectException("fullName may not be null or empty");
 			if (userName == null || userName.length() == 0)
 				throw new InvalidObjectException("userName may not be null or empty");
 		}
 	}
 
 	/**
 	 * A user group modification token returned by the
 	 * <code>startUserGroupModification</code> method, which is used to perform
 	 * the action and detect new edits.
 	 */
 	public class UserGroupsToken implements Serializable, ObjectInputValidation {
 		private static final long serialVersionUID = 1L;
 
 		private final String userName;
 
 		private final String token;
 
 		UserGroupsToken(final String userName, final String token) {
 			if (userName == null || userName.length() == 0)
 				throw new IllegalArgumentException("userName may not be null or empty");
 			if (token == null)
 				throw new NullPointerException("token");
 			this.userName = userName;
 			this.token = token;
 		}
 
 		/**
 		 * Returns the name of the user whose group memberships are to be
 		 * modified.
 		 * 
 		 * @return the name of the user whose group memberships are to be
 		 *         modified
 		 */
 		public String getUserName() {
 			return userName;
 		}
 
 		/**
 		 * Returns the text of the token.
 		 * 
 		 * @return the text of the token
 		 */
 		public String getTokenText() {
 			return token;
 		}
 
 		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
 			in.registerValidation(this, 0);
 			in.defaultReadObject();
 		}
 
 		/**
 		 * This method validates deserialized objects and should generally not
 		 * be called by applications.
 		 */
 		public void validateObject() throws InvalidObjectException {
 			if (token == null)
 				throw new InvalidObjectException("token == null");
 			if (userName == null || userName.length() == 0)
 				throw new InvalidObjectException("userName may not be null or empty");
 		}
 	}
 
 	// - - - ENUMS AND ENUM-LIKE CLASSES - - -
 
 	/**
 	 * Useful constants for protection actions. These describe actions that are
 	 * prohibited to users who are not in certain groups.
 	 */
 	public static class ProtectionAction {
 		/**
 		 * Protection against creation.
 		 */
 		public static final String CREATE = "create";
 
 		/**
 		 * Protection against edition.
 		 */
 		public static final String EDIT = "edit";
 
 		/**
 		 * Protection against movement.
 		 */
 		public static final String MOVE = "move";
 
 		/**
 		 * Protection against uploads (in the <tt>File</tt> namespace).
 		 */
 		public static final String UPLOAD = "upload";
 	}
 
 	/**
 	 * Useful constants for protection levels. These describe the groups that
 	 * users must belong to in order to perform protected actions.
 	 */
 	public static class ProtectionLevel {
 		/**
 		 * Actions require that users be autoconfirmed.
 		 * <p>
 		 * The definition of autoconfirmed varies from wiki to wiki, but
 		 * generally requires some number of edits or days since the creation of
 		 * a user account.
 		 */
 		public static final String AUTOCONFIRMED_USERS = "autoconfirmed";
 
 		/**
 		 * Actions require that users be sysops.
 		 */
 		public static final String SYSOPS = "sysop";
 	}
 
 	/**
 	 * Useful constants for standard namespaces. These designate namespace IDs
 	 * that are standard in many MediaWiki installations.
 	 */
 	public static class StandardNamespace {
 		/**
 		 * The namespace identifier for the Media namespace.
 		 */
 		public static final long MEDIA = -2;
 
 		/**
 		 * The namespace identifier for the Special namespace.
 		 */
 		public static final long SPECIAL = -1;
 
 		/**
 		 * The namespace identifier for the main namespace.
 		 */
 		public static final long MAIN = 0;
 
 		/**
 		 * The namespace identifier for the Talk namespace, which contains talk
 		 * pages for the main namespace.
 		 */
 		public static final long TALK = 1;
 
 		/**
 		 * The namespace identifier for the User namespace.
 		 */
 		public static final long USER = 2;
 
 		/**
 		 * The namespace identifier for the User talk namespace.
 		 */
 		public static final long USER_TALK = 3;
 
 		/**
 		 * The namespace identifier for the Project namespace.
 		 */
 		public static final long PROJECT = 4;
 
 		/**
 		 * The namespace identifier for the Project talk namespace.
 		 */
 		public static final long PROJECT_TALK = 5;
 
 		/**
 		 * The namespace identifier for the File namespace.
 		 */
 		public static final long FILE = 6;
 
 		/**
 		 * The namespace identifier for the File talk namespace.
 		 */
 		public static final long FILE_TALK = 7;
 
 		/**
 		 * The namespace identifier for the MediaWiki namespace, which contains
 		 * system messages.
 		 */
 		public static final long MEDIAWIKI = 8;
 
 		/**
 		 * The namespace identifier for the MediaWiki talk namespace.
 		 */
 		public static final long MEDIAWIKI_TALK = 9;
 
 		/**
 		 * The namespace identifier for the Template namespace.
 		 */
 		public static final long TEMPLATE = 10;
 
 		/**
 		 * The namespace identifier for the Template talk namespace.
 		 */
 		public static final long TEMPLATE_TALK = 11;
 
 		/**
 		 * The namespace identifier for the Help namespace.
 		 */
 		public static final long HELP = 12;
 
 		/**
 		 * The namespace identifier for the Help talk namespace.
 		 */
 		public static final long HELP_TALK = 13;
 
 		/**
 		 * The namespace identifier for the Category namespace.
 		 */
 		public static final long CATEGORY = 14;
 
 		/**
 		 * The namespace identifier for the Category talk namespace.
 		 */
 		public static final long CATEGORY_TALK = 15;
 	}
 
 	// - - - HELPER METHODS FOR DATA FORMAT CONVERSION - - -
 
 	private static final SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
 
 	static {
 		timestampFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 
 	/**
 	 * Returns the specified <code>date</code> formatted for use as a MediaWiki
 	 * timestamp value. The return value is a string containing at least 14
 	 * digits, with at least 4 for the year, 2 for the month, 2 for the day of
 	 * the month, 2 for the hour in 24-hour format, 2 for the minute and 2 for
 	 * the second (<tt><em>yyyyMMddHHmmss</em></tt>).
 	 * 
 	 * @param date
 	 *            The date to format.
 	 * @return the specified <code>date</code> formatted for use as a MediaWiki
 	 *         timestamp value
 	 */
 	public static String dateToTimestamp(final Date date) {
 		return timestampFormatter.format(date);
 	}
 
 	/**
 	 * Returns the specified <code>date</code> formatted for use as a MediaWiki
 	 * timestamp value. The return value is a string containing at least 14
 	 * digits, with at least 4 for the year, 2 for the month, 2 for the day of
 	 * the month, 2 for the hour in 24-hour format, 2 for the minute and 2 for
 	 * the second (<tt><em>yyyyMMddHHmmss</em></tt>).
 	 * 
 	 * @param date
 	 *            The date to format.
 	 * @return the specified <code>date</code> formatted for use as a MediaWiki
 	 *         timestamp value
 	 */
 	public static String dateToISO8601(final Date date) {
 		return iso8601TimestampParser.format(date);
 	}
 
 	/**
 	 * Returns the specified <code>timestamp</code> parsed as a <tt>Date</tt>
 	 * object.
 	 * 
 	 * @param timestamp
 	 *            The timestamp value to parse. This value is either of the
 	 *            following:
 	 *            <ul>
 	 *            <li>a MediaWiki timestamp value, i.e. a string containing at
 	 *            least 14 digits, with at least 4 for the year, 2 for the
 	 *            month, 2 for the day of the month, 2 for the hour in 24-hour
 	 *            format, 2 for the minute and 2 for the second (
 	 *            <tt><em>yyyyMMddHHmmss</em></tt>);
 	 *            <li>an ISO 8601 date and time stamp in the GMT timezone, i.e.
 	 *            a string conforming to the format
 	 *            <tt><em>yyyy</em>-<em>MM</em>-<em>dd</em>T<em>HH</em>:<em>mm</em>:<em>ss</em>Z</tt>.
 	 *            </ul>
 	 * @return the specified <code>timestamp</code> parsed as a <tt>Date</tt>
 	 *         object
 	 * @throws ParseException
 	 *             if the specified <code>timestamp</code> is not a valid
 	 *             timestamp
 	 */
 	public static Date timestampToDate(final String timestamp) throws ParseException {
 		if (timestamp.length() > 0 && timestamp.charAt(timestamp.length() - 1) == 'Z')
 			return iso8601TimestampParser.parse(timestamp);
 		else
 			return timestampFormatter.parse(timestamp);
 	}
 
 	/**
 	 * Normalises the given <code>title</code> according to the rules in place
 	 * on the wiki represented by this <tt>MediaWiki</tt>.
 	 * 
 	 * @param title
 	 *            Title to normalise.
 	 * @return the normalised title
 	 * @throws IOException
 	 * @throws MediaWikiException
 	 */
 	public String normalizeTitle(final String title) throws IOException, MediaWikiException {
 		final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "titles", title);
 		final String url = createApiGetUrl(getParams);
 
 		networkLock.lock();
 		try {
 			final InputStream in = get(url);
 			Document xml = parse(in);
 			checkError(xml);
 
 			final NodeList normalizedTags = xml.getElementsByTagName("normalized");
 
 			final Map<String, String> normalizations = new TreeMap<String, String>();
 
 			for (int i = 0; i < normalizedTags.getLength(); i++) {
 				Element normalizedTag = (Element) normalizedTags.item(i);
 
 				NodeList nTags = normalizedTag.getElementsByTagName("n");
 
 				for (int j = 0; j < nTags.getLength(); j++) {
 					Element nTag = (Element) nTags.item(i);
 
 					normalizations.put(nTag.getAttribute("from"), nTag.getAttribute("to"));
 				}
 			}
 
 			return normalizations.containsKey(title) ? normalizations.get(title) : title;
 		} finally {
 			networkLock.unlock();
 		}
 	}
 
 	/**
 	 * Normalises the given <code>titles</code> according to the rules in place
 	 * on the wiki represented by this <tt>MediaWiki</tt>.
 	 * 
 	 * @param titles
 	 *            Titles to normalise. The members of this list are modified
 	 *            in-place.
 	 * @return <code>titles</code>
 	 * @throws IOException
 	 * @throws MediaWikiException
 	 */
 	public List<String> normalizeTitles(final List<String> titles) throws IOException, MediaWikiException {
 		if (titles.isEmpty())
 			return titles;
 
 		int index = 0, max = Math.min(titles.size(), 500);
 
 		while (index < titles.size()) {
 			int end = Math.min(index + max, titles.size());
 
 			StringBuilder titleString = new StringBuilder((end - index) * 2);
 			for (int i = index; i < end; i++) {
 				if (titleString.length() > 0)
 					titleString.append('|');
 				titleString.append(titles.get(i));
 			}
 
 			final Map<String, String> getParams = paramValuesToMap("action", "query", "format", "xml", "prop", "info", "titles", titleString.toString());
 			final String url = createApiGetUrl(getParams);
 
 			networkLock.lock();
 			try {
 				final InputStream in = get(url);
 				Document xml = parse(in);
 				checkError(xml);
 
 				final NodeList normalizedTags = xml.getElementsByTagName("normalized");
 
 				if (normalizedTags.getLength() == 0)
 					throw new MediaWiki.ResponseFormatException("expected <normalized> tag not found");
 				if (normalizedTags.getLength() >= 2)
 					throw new MediaWiki.ResponseFormatException("more than one <normalized> tag found");
 
 				final Map<String, String> normalizations = new TreeMap<String, String>();
 
 				Element normalizedTag = (Element) normalizedTags.item(0);
 
 				NodeList nTags = normalizedTag.getElementsByTagName("n");
 
 				for (int j = 0; j < nTags.getLength(); j++) {
 					Element nTag = (Element) nTags.item(j);
 
 					normalizations.put(nTag.getAttribute("from"), nTag.getAttribute("to"));
 				}
 
 				for (int i = index; i < end; i++) {
 					if (normalizations.containsKey(titles.get(i)))
 						titles.set(i, normalizations.get(titles.get(i)));
 				}
 
 				final NodeList pagesTags = xml.getElementsByTagName("pages");
 
 				if (pagesTags.getLength() == 0)
 					throw new MediaWiki.ResponseFormatException("expected <pages> tag not found");
 				if (pagesTags.getLength() >= 2)
 					throw new MediaWiki.ResponseFormatException("more than one <pages> tag found");
 
 				Element pagesTag = (Element) pagesTags.item(0);
 
 				NodeList pageTags = pagesTag.getElementsByTagName("page");
 
 				max = pageTags.getLength();
 
 				index += max;
 			} finally {
 				networkLock.unlock();
 			}
 		}
 
 		return titles;
 	}
 
 	/**
 	 * Converts the given title, which may include a namespace, to the form most
 	 * suitable for the API. Currently, this means that the title's underscores
 	 * are replaced with spaces.
 	 * <p>
 	 * Use of this method reduces the occurrence of the API returning
 	 * normalisation replies, which saves bandwidth.
 	 * 
 	 * @param title
 	 *            The title to convert.
 	 * @return the given title, which may include a namespace, to the form most
 	 *         suitable for the API
 	 */
 	public static String titleToAPIForm(final String title) {
 		return title.replace('_', ' ');
 	}
 
 	/**
 	 * Converts the given title, which may include a namespace, to the form most
 	 * suitable for display, e.g. in URLs. Currently, this means that the
 	 * title's spaces are replaced with underscores.
 	 * 
 	 * @param title
 	 *            The title to convert.
 	 * @return the given title, which may include a namespace, to the form most
 	 *         suitable for the display
 	 */
 	public static String titleToDisplayForm(final String title) {
 		return title.replace(' ', '_');
 	}
 
 	/**
 	 * Converts an array of namespace IDs into the form expected by the API for
 	 * <code>functionShortName + "namespace"</code> parameters, which is the
 	 * namespace IDs in ASCII digit form without thousands separators, separated
 	 * by <code>'|'</code>.
 	 * 
 	 * @param namespaceIDs
 	 *            The IDs of the namespaces to convert. This may be
 	 *            <code>null</code> or empty, in which case <code>null</code>
 	 *            will be returned.
 	 * @return a text string suitable for the API and representing the specified
 	 *         namespace IDs, or <code>null</code> if no namespaces are
 	 *         specified
 	 */
 	protected static String namespacesParameter(final long[] namespaceIDs) {
 		if (namespaceIDs == null || namespaceIDs.length == 0)
 			return null;
 		StringBuilder result = new StringBuilder(namespaceIDs.length * 4);
 		result.append(namespaceIDs[0]);
 		for (int i = 1; i < namespaceIDs.length; i++)
 			result.append('|').append(namespaceIDs[i]);
 		return result.toString();
 	}
 
 	// - - - HELPER CLASSES - - -
 
 	protected abstract class AbstractReadOnlyIterator<T> implements Iterator<T> {
 		public final void remove() {
 			throw new UnsupportedOperationException("read-only iterator");
 		}
 	}
 
 	protected abstract class AbstractBufferingIterator<T> extends AbstractReadOnlyIterator<T> {
 		/**
 		 * The index of the last node returned among <code>upcoming</code>. This
 		 * field has the value <code>-1</code> if iteration has not yet started
 		 * or <code>upcoming</code> was just refilled.
 		 */
 		private int i = -1;
 
 		/**
 		 * The list of nodes that are being buffered by this
 		 * <tt>AbstractBufferingIterator</tt>. Query continuation, if
 		 * applicable, loads more nodes into this field.
 		 */
 		private List<? extends Element> upcoming;
 
 		/**
 		 * Returns the index of the last node returned among
 		 * <code>getUpcoming()</code>. The return value is <code>-1</code> if
 		 * iteration has not yet started or <code>upcoming</code> was just
 		 * refilled.
 		 * 
 		 * @return the index of the last node to return among
 		 *         <code>getUpcoming()</code>
 		 */
 		protected int getIndex() {
 			return i;
 		}
 
 		/**
 		 * Sets the index of the last node returned among
 		 * <code>getUpcoming()</code>.
 		 * 
 		 * @param newValue
 		 *            The index of the last node returned among
 		 *            <code>getUpcoming()</code>. This value is <code>-1</code>
 		 *            if <code>upcoming</code> was just refilled.
 		 */
 		protected void setIndex(int newValue) {
 			i = newValue;
 		}
 
 		/**
 		 * Returns the list of nodes that this
 		 * <tt>AbstractBufferingIterator</tt> is iterating over for the time
 		 * being.
 		 * 
 		 * @return the list of nodes that this
 		 *         <tt>AbstractBufferingIterator</tt> is iterating over for the
 		 *         time being
 		 */
 		protected List<? extends Element> getUpcoming() {
 			return upcoming;
 		}
 
 		/**
 		 * Sets the list of nodes that this <tt>AbstractBufferingIterator</tt>
 		 * will be iterating over.
 		 * <p>
 		 * This method also resets the index to <code>-1</code>.
 		 * 
 		 * @param newValue
 		 *            The list of nodes that this
 		 *            <tt>AbstractBufferingIterator</tt> will be iterating over.
 		 */
 		protected void setUpcoming(List<? extends Element> newValue) {
 			this.upcoming = newValue;
 			setIndex(-1);
 		}
 
 		/**
 		 * Creates a Java <tt>List</tt> out of the DOM <tt>NodeList</tt>
 		 * supplied and fills the <code>upcoming</code> queue with it.
 		 * <p>
 		 * This method also resets the index to <code>-1</code>.
 		 * 
 		 * @param nodes
 		 *            The list of nodes that this
 		 *            <tt>AbstractBufferingIterator</tt> will be iterating over.
 		 * @throws ClassCastException
 		 *             if any of the nodes supplied is not an <tt>Element</tt>
 		 *             node
 		 */
 		protected void setUpcoming(NodeList nodes) {
 			if (nodes == null)
 				setUpcoming((List<Element>) null);
 			else {
 				List<Element> newUpcoming = new ArrayList<Element>(nodes.getLength());
 				for (int i = 0; i < nodes.getLength(); i++) {
 					newUpcoming.add((Element) nodes.item(i));
 				}
 				setUpcoming(newUpcoming);
 			}
 		}
 	}
 
 	protected abstract class AbstractContinuableQueryIterator<T> extends AbstractBufferingIterator<T> {
 		/**
 		 * This field contains the name of the continuation value; for example,
 		 * <code>"rvstartid"</code>, <code>"eloffset"</code>.
 		 */
 		private String continuationName;
 
 		/**
 		 * This field contains the start or continue value for the next API
 		 * reply's worth of things being iterated over.
 		 */
 		private String continuation;
 
 		/**
 		 * This field is <code>true</code> if the iteration is done.
 		 */
 		private boolean done;
 
 		/**
 		 * Constructs a new instance of
 		 * <tt>AbstractContinuableQueryIterator</tt> without any continuation.
 		 * In other words, the new iterator will start iterating at the
 		 * beginning.
 		 */
 		AbstractContinuableQueryIterator() {}
 
 		/**
 		 * Constructs a new instance of
 		 * <tt>AbstractContinuableQueryIterator</tt> with the given
 		 * continuation. This can be useful when a start parameter is provided
 		 * to an iterator implementation's constructor.
 		 * 
 		 * @param continuation
 		 *            The continuation to start with.
 		 */
 		AbstractContinuableQueryIterator(final String continuationName, final String continuation) {
			this.continuationName = continuationName;
 			this.continuation = continuation;
 		}
 
 		/**
 		 * Returns whether the iteration is done. If the return value is
 		 * <code>true</code>, then <code>hasNext</code> should return
 		 * <code>false</code> forever from that point on.
 		 * 
 		 * @return whether the iteration is done
 		 */
 		public boolean isDone() {
 			return done;
 		}
 
 		/**
 		 * Returns the next continuation name to be used by
 		 * <code>cacheUpcoming</code>. Depending on the concrete implementation
 		 * of <tt>AbstractContinuableQueryIterator</tt>, this is a
 		 * <tt>start</tt>, <tt>from</tt> or <tt>continue</tt> value.
 		 * 
 		 * @return the next continuation name to be used by
 		 *         <code>cacheUpcoming</code>
 		 */
 		protected String getContinuationName() {
 			return continuationName;
 		}
 
 		/**
 		 * Returns the next continuation value to be used by
 		 * <code>cacheUpcoming</code>. Depending on the concrete implementation
 		 * of <tt>AbstractContinuableQueryIterator</tt>, this is a
 		 * <tt>start</tt>, <tt>from</tt> or <tt>continue</tt> value.
 		 * 
 		 * @return the next continuation value to be used by
 		 *         <code>cacheUpcoming</code>
 		 */
 		protected String getContinuation() {
 			return continuation;
 		}
 
 		/**
 		 * Returns whether this <tt>AbstractContinuableQueryIterator</tt> has at
 		 * least one more element to return. If the pointer into the buffer is
 		 * past the end, the query is continued by calling
 		 * <code>cacheUpcoming</code> before deciding the return value.
 		 */
 		public synchronized boolean hasNext() throws MediaWiki.IterationException {
 			if (getUpcoming() != null && getIndex() + 1 < getUpcoming().size())
 				return true;
 			if (!isDone()) {
 				try {
 					cacheUpcoming();
 				} catch (MediaWiki.IterationException ie) {
 					throw ie;
 				} catch (Exception e) {
 					throw new MediaWiki.IterationException(e);
 				}
 				if (getUpcoming() == null) {
 					done = true;
 					return false;
 				} else
 					return getIndex() + 1 < getUpcoming().size();
 			} else
 				return false;
 		}
 
 		/**
 		 * Returns the next element of this
 		 * <tt>AbstractContinuableQueryIterator</tt>. The XML <tt>Element</tt>
 		 * at the buffer's current pointer is converted to an element of type
 		 * <tt>T</tt> using the <code>convert</code> method.
 		 * <p>
 		 * If the pointer into the buffer is past the end, the query is
 		 * continued by calling <code>cacheUpcoming</code> before deciding the
 		 * return value.
 		 * 
 		 * @throws NoSuchElementException
 		 *             if <code>isDone</code> returns <code>true</code>
 		 */
 		public synchronized T next() throws MediaWiki.IterationException {
 			if ((getUpcoming() != null && getIndex() + 1 < getUpcoming().size()) || !isDone()) {
 				if (getUpcoming() == null || getIndex() + 1 >= getUpcoming().size())
 					try {
 						cacheUpcoming();
 					} catch (MediaWiki.IterationException ie) {
 						throw ie;
 					} catch (Exception e) {
 						throw new MediaWiki.IterationException(e);
 					}
 				if (getUpcoming() == null) {
 					done = true;
 					throw new NoSuchElementException();
 				}
 				int index = getIndex() + 1;
 				setIndex(index);
 				Element element = getUpcoming().get(index);
 				if (getIndex() + 1 >= getUpcoming().size())
 					setUpcoming((List<Element>) null);
 				try {
 					return convert(element);
 				} catch (MediaWiki.IterationException ie) {
 					throw ie;
 				} catch (Exception e) {
 					throw new MediaWiki.IterationException(e);
 				}
 			} else
 				throw new NoSuchElementException();
 		}
 
 		/**
 		 * Processes the <tt>&lt;query-continue&gt;</tt> tag, updating the
 		 * continuation name and value as appropriate and updating the done
 		 * flag.
 		 * <p>
 		 * The attribute is parsed from the
 		 * 
 		 * @param reply
 		 *            The continuable reply from the API in XML format.
 		 * @param tagName
 		 *            The name of the tag in <tt>&lt;query-continue&gt;</tt>
 		 *            containing the new continuation value, for example
 		 *            <code>"allpages"</code>.
 		 */
 		protected void processContinuation(Document reply, String tagName) throws MediaWiki.ResponseFormatException {
 			final NodeList queryContinueTags = reply.getElementsByTagName("query-continue");
 
 			if (queryContinueTags.getLength() > 0) {
 				final Element queryContinueTag = (Element) queryContinueTags.item(0);
 
 				final NodeList continuationTags = queryContinueTag.getElementsByTagName(tagName);
 
 				if (continuationTags.getLength() > 0) {
 					final Element continuationTag = (Element) continuationTags.item(0);
 
 					NamedNodeMap attributes = continuationTag.getAttributes();
 
 					if (attributes.getLength() == 0)
 						throw new MediaWiki.ResponseFormatException("no attribute in query-continue/" + tagName);
 
 					continuationName = attributes.item(0).getNodeName();
 					continuation = attributes.item(0).getNodeValue();
 				} else {
 					done = true;
 				}
 			} else {
 				done = true;
 			}
 		}
 
 		/**
 		 * Converts an XML <tt>Element</tt> containing information to be
 		 * returned into an object of type <tt>T</tt>.
 		 * 
 		 * @param element
 		 *            An XML <tt>Element</tt> containing information to be
 		 *            converted.
 		 * @return the converted element
 		 * @throws Exception
 		 *             if parsing the <tt>Element</tt>'s data throws
 		 *             <tt>Exception</tt>
 		 */
 		protected abstract T convert(Element element) throws Exception;
 
 		/**
 		 * Reads the continuation name and value, stores the next buffer of
 		 * elements using <code>setUpcoming</code>, and stores the new
 		 * continuation name and value.
 		 * <p>
 		 * This method can assume that, when it is called, all of the following
 		 * conditions are met:
 		 * <ul>
 		 * <li>The buffer pointer is past its end or the buffer is
 		 * <code>null</code>, so more elements are needed;
 		 * <li>The iteration is not done; that is, <code>isDone()</code> returns
 		 * <code>false</code>.
 		 * <p>
 		 * This method can assume that, if it throws a checked exception, the
 		 * buffer will not be filled, and the iteration will not be considered
 		 * done. It can then be retried later safely.
 		 * 
 		 * @throws Exception
 		 *             if receiving the next buffer's worth of <tt>Element</tt>s
 		 *             throws <tt>Exception</tt>
 		 */
 		protected abstract void cacheUpcoming() throws Exception;
 	}
 
 	// - - - HELPER METHODS FOR CONNECTIONS - - -
 
 	/**
 	 * Returns an instance of <tt>InputStream</tt> that reads the wiki's reply
 	 * to a GET request.
 	 * 
 	 * @param url
 	 *            The URL to get.
 	 * @return an instance of <tt>InputStream</tt> that reads the wiki's reply
 	 *         to the GET request
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or reading HTTP headers
 	 */
 	protected InputStream get(final String url) throws IOException {
 		final HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
 		initConnection(http);
 		initGet(http);
 		initCookies(http);
 		http.connect();
 
 		if (http.getResponseCode() != 200)
 			throw new MediaWiki.HttpStatusException(http.getResponseCode());
 
 		updateCookies(http);
 
 		final String encoding = http.getHeaderField("Content-Encoding");
 		return (encoding != null) && encoding.equals("gzip") ? new GZIPInputStream(http.getInputStream()) : http.getInputStream();
 	}
 
 	/**
 	 * Parses an XML <tt>Document</tt> from content read from the given
 	 * <tt>InputStream</tt>.
 	 * 
 	 * @param in
 	 *            The <tt>InputStream<tt> to read from.
 	 * @return an XML <tt>Document</tt> from content read from the given
 	 *         <tt>InputStream</tt>
 	 * @throws IOException
 	 *             if <tt>IOException</tt> or <tt>SAXException</tt> is thrown
 	 *             while parsing the content as XML
 	 */
 	protected Document parse(final InputStream in) throws IOException {
 		try {
 			return documentBuilder.parse(in);
 		} catch (SAXException e) {
 			throw new IOException(e);
 		}
 	}
 
 	/**
 	 * Returns an instance of <tt>InputStream</tt> that reads the wiki's reply
 	 * to a POST request.
 	 * 
 	 * @param url
 	 *            The URL to POST to. This URL may have some GET parameters.
 	 * @param params
 	 *            The parameters and their values to use for the POST data. Keys
 	 *            and values in this map are not URL-encoded.
 	 * @return an instance of <tt>InputStream</tt> that reads the wiki's reply
 	 *         to the POST request
 	 * @throws IOException
 	 *             if <tt>IOException</tt> is thrown while connecting to the
 	 *             wiki or reading HTTP headers
 	 */
 	protected InputStream post(final String url, final Map<String, String> params) throws IOException {
 		final HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
 		initConnection(http);
 		initPost(http);
 		http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		initCookies(http);
 		// URL-encoded data can be written quickly with ISO-8859-1.
 		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(http.getOutputStream(), "ISO-8859-1"));
 		out.write(createApiPostData(params));
 		out.flush();
 		out.close();
 		http.connect();
 
 		if (http.getResponseCode() != 200)
 			throw new MediaWiki.HttpStatusException(http.getResponseCode());
 
 		updateCookies(http);
 
 		final String encoding = http.getHeaderField("Content-Encoding");
 		return (encoding != null) && encoding.equals("gzip") ? new GZIPInputStream(http.getInputStream()) : http.getInputStream();
 	}
 
 	/**
 	 * Initializes an HTTP connection as desired by the <tt>MediaWiki</tt>
 	 * implementation. Subclasses can override this method to specify a proxy to
 	 * use, a different read timeout or user agent string, or other settings.
 	 * 
 	 * @param http
 	 *            The HTTP connection to initialize.
 	 */
 	protected void initConnection(final HttpURLConnection http) {
 		http.setAllowUserInteraction(false);
 		http.setConnectTimeout(15000);
 		http.setReadTimeout(60000);
 		http.setRequestProperty("User-Agent", "MediaWiki.java/" + VERSION);
 		if (isUsingCompression()) {
 			http.setRequestProperty("Accept-Encoding", "gzip; q=1.0");
 		}
 	}
 
 	/**
 	 * Initializes an HTTP connection for a GET request as desired by the
 	 * <tt>MediaWiki</tt> implementation. Subclasses can assume that
 	 * <code>initConnection</code> was called before this method.
 	 * 
 	 * @param http
 	 *            The HTTP connection to initialize.
 	 */
 	protected void initGet(final HttpURLConnection http) {}
 
 	/**
 	 * Initializes an HTTP connection for a POST request as desired by the
 	 * <tt>MediaWiki</tt> implementation. Subclasses can assume that
 	 * <code>initConnection</code> was called before this method.
 	 * 
 	 * @param http
 	 *            The HTTP connection to initialize.
 	 */
 	protected void initPost(final HttpURLConnection http) {
 		http.setDoOutput(true);
 		try {
 			http.setRequestMethod("POST");
 		} catch (ProtocolException shouldNeverHappen) {
 			throw new InternalError("POST is not accepted by the HTTP protocol implementation");
 		}
 	}
 
 	/**
 	 * Adds cookies that have been preserved by this <tt>MediaWiki</tt> to the
 	 * <tt>Cookie</tt> header of the specified HTTP connection. Subclasses can
 	 * assume that <code>initConnection</code> was called before this method.
 	 * 
 	 * @param http
 	 *            The HTTP connection to initialize.
 	 */
 	protected void initCookies(final HttpURLConnection http) {
 		final StringBuilder cookieHeader = new StringBuilder();
 		preferenceLock.readLock().lock();
 		try {
 			for (final Map.Entry<String, String> cookie : cookies.entrySet()) {
 				cookieHeader.append(cookie.getKey());
 				cookieHeader.append("=");
 				cookieHeader.append(cookie.getValue());
 				cookieHeader.append("; ");
 			}
 		} finally {
 			preferenceLock.readLock().unlock();
 		}
 		if (cookieHeader.length() >= 2) {
 			cookieHeader.delete(cookieHeader.length() - 2, cookieHeader.length());
 		}
 		http.setRequestProperty("Cookie", cookieHeader.toString());
 	}
 
 	private static final Pattern rfc1123DateRegex = Pattern.compile("^(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun), ([0-9]{2}-(?:Jan|Feb|Ma[ry]|Apr|Ju[nl]|Aug|Sep|Oct|Nov|Dec)-[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}) GMT$");
 
 	private static final SimpleDateFormat rfc1123DateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);
 
 	static {
 		rfc1123DateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 
 	/**
 	 * Preserves cookies that have been received by this <tt>MediaWiki</tt> in a
 	 * <tt>Set-Cookie</tt> header.
 	 * 
 	 * @param http
 	 *            The HTTP connection to preserve cookies from.
 	 */
 	protected void updateCookies(final HttpURLConnection http) {
 		String headerName;
 		preferenceLock.writeLock().lock();
 		try {
 			for (int i = 1; (headerName = http.getHeaderFieldKey(i)) != null; i++)
 				if (headerName.equalsIgnoreCase("Set-Cookie")) {
 					final String cookie = http.getHeaderField(i);
 
 					final String cookieCrumb = cookie.substring(0, cookie.indexOf(';'));
 					final String name = cookieCrumb.substring(0, cookieCrumb.indexOf('=')).trim();
 					final String value = cookieCrumb.substring(cookieCrumb.indexOf('=') + 1, cookieCrumb.length()).trim();
 					cookies.put(name, value);
 
 					/* If the cookie's Expires date in the past, remove it. */
 					final String[] cookieAttributes = cookie.substring(cookie.indexOf(';') + 1).split(";");
 					for (final String cookieAttribute : cookieAttributes) {
 						if (cookieAttribute.indexOf('=') == -1) {
 							continue;
 						}
 						final String attrName = cookieAttribute.substring(0, cookieAttribute.indexOf('=')).trim();
 						final String attrValue = cookieAttribute.substring(cookieAttribute.indexOf('=') + 1, cookieAttribute.length()).trim();
 						if (attrName.equalsIgnoreCase("Expires")) {
 							final Matcher m = rfc1123DateRegex.matcher(attrValue);
 							if (m.find()) {
 								try {
 									final Date expires = rfc1123DateParser.parse(m.group(1));
 									if (expires.compareTo(new Date()) < 0) {
 										cookies.remove(name);
 									}
 								} catch (final ParseException e) {
 									// don't care; just keep the cookie
 								}
 							}
 						}
 					}
 				}
 		} finally {
 			preferenceLock.writeLock().unlock();
 		}
 	}
 
 	protected String createApiGetUrl(final Map<String, String> params) {
 		final StringBuilder result = new StringBuilder("http://").append(host).append('/').append(scriptPath);
 		if (scriptPath.length() != 0) {
 			result.append('/');
 		}
 		result.append("api.php");
 		boolean first = true;
 		if (params != null) {
 			for (final Map.Entry<String, String> param : params.entrySet()) {
 				if (param.getValue() != null)
 					try {
 						result.append(first ? '?' : '&').append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(param.getValue(), "UTF-8"));
 						first = false;
 					} catch (final UnsupportedEncodingException shouldNeverHappen) {
 						throw new InternalError("UTF-8 is not supported by this Java VM");
 					}
 			}
 		}
 		Integer maxLag = getMaxLag();
 		if (maxLag != null) {
 			result.append(first ? '?' : '&').append("maxlag=").append(maxLag);
 			first = false;
 		}
 		return result.toString();
 	}
 
 	protected String createApiPostData(final Map<String, String> params) {
 		// TODO Make a version that allows forcing a param to be sent last
 		final StringBuilder result = new StringBuilder();
 		boolean first = true;
 		if (params != null) {
 			for (final Map.Entry<String, String> param : params.entrySet()) {
 				if (param.getValue() != null)
 					try {
 						if (!first) {
 							result.append('&');
 						}
 						result.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(param.getValue(), "UTF-8"));
 						first = false;
 					} catch (final UnsupportedEncodingException shouldNeverHappen) {
 						throw new InternalError("UTF-8 is not supported by this Java VM");
 					}
 			}
 		}
 		return result.toString();
 	}
 
 	/**
 	 * Dramatically shortens param-value map creation for
 	 * <code>createApiGetData</code> and <code>createApiPostData</code> by
 	 * making it possible to put everything on one line.
 	 * 
 	 * @param paramValues
 	 *            Parameters and values. Even-numbered indices must be parameter
 	 *            names; odd-numbered indices must be values. The length of this
 	 *            parameter list must be even.
 	 * @return a modifiable <tt>Map</tt> object that initially contains the
 	 *         supplied parameters as keys and values
 	 */
 	protected Map<String, String> paramValuesToMap(String... paramValues) {
 		TreeMap<String, String> result = new TreeMap<String, String>();
 		for (int i = 0; i < paramValues.length; i += 2) {
 			result.put(paramValues[i], paramValues[i + 1]);
 		}
 		return result;
 	}
 
 	// - - - ERROR CHECKING - - -
 
 	/**
 	 * Checks for an error being present in an API reply and throws an
 	 * appropriate exception for the error code.
 	 * 
 	 * @param reply
 	 *            The reply from the API in XML document form.
 	 * @throws MediaWiki.MediaWikiException
 	 *             as appropriate
 	 */
 	protected void checkError(Document reply) throws MediaWiki.MediaWikiException, MediaWiki.ResponseFormatException {
 		checkError(reply, "code", "info");
 	}
 
 	/**
 	 * Checks for an error being present in an API reply and throws an
 	 * appropriate exception for the error code.
 	 * 
 	 * @param reply
 	 *            The reply from the API in XML document form.
 	 * @param codeAttribute
 	 *            The name of the attribute of the <tt>&lt;error&gt;</tt> tag to
 	 *            examine for error codes. This can be, for example,
 	 *            <code>"code"</code> or <code>"talkmove-error-code"</code>.
 	 * @param codeAttribute
 	 *            The name of the attribute of the <tt>&lt;error&gt;</tt> tag to
 	 *            examine for error information. This can be, for example,
 	 *            <code>"info"</code> or <code>"talkmove-error-info"</code>.
 	 * @throws MediaWiki.MediaWikiException
 	 *             as appropriate
 	 */
 	protected void checkError(Document reply, final String codeAttribute, final String infoAttribute) throws MediaWiki.MediaWikiException, MediaWiki.ResponseFormatException {
 		NodeList apiTags = reply.getElementsByTagName("api");
 
 		if (apiTags.getLength() > 0) {
 			Element apiTag = (Element) apiTags.item(0);
 
 			NodeList errorTags = apiTag.getElementsByTagName("error");
 
 			if (errorTags.getLength() > 0) {
 				/*
 				 * Oh, MediaWiki! Well, there's an error, so we won't leave this
 				 * method without throwing some form of MediaWikiException now.
 				 * However, for some error codes, we should throw a more
 				 * appropriate subclass.
 				 */
 				Element errorTag = (Element) errorTags.item(0);
 
 				String errorCode = errorTag.getAttribute(codeAttribute), errorInfo = errorTag.getAttribute(infoAttribute);
 
 				// Most likely errors
 				if (errorCode.equals("protectedpage") || errorCode.equals("cascadeprotected") || errorCode.equals("protectedtitle"))
 					throw new MediaWiki.ProtectionException(errorCode + ": " + errorInfo);
 				if (errorCode.equals("permissiondenied") || errorCode.equals("confirmemail") || errorCode.equals("protectednamespace-interface") || errorCode.equals("protectednamespace") || errorCode.equals("customcssjsprotected") || errorCode.equals("cantcreate") || errorCode.equals("cantcreate-anon") || errorCode.equals("noimageredirect") || errorCode.equals("noimageredirect-anon") || errorCode.equals("noedit") || errorCode.equals("noedit-anon") || errorCode.equals("cantmove")
 						|| errorCode.equals("cantmove-anon") || errorCode.equals("cantmovefile"))
 					throw new MediaWiki.PermissionException(errorCode + ": " + errorInfo);
 				if (errorCode.equals("alreadyrolled") || errorCode.equals("onlyauthor"))
 					throw new MediaWiki.ActionFailureException(errorCode + ": " + errorInfo);
 				if (errorCode.equals("articleexists"))
 					throw new MediaWiki.ExistingPageException(errorInfo);
 				if (errorCode.equals("missingtitle"))
 					throw new MediaWiki.MissingPageException(errorInfo);
 				if (errorCode.equals("ratelimited") || errorCode.equals("maxlag"))
 					throw new MediaWiki.ActionDelayException(errorCode + ": " + errorInfo);
 
 				if (errorCode.equals("spamdetected") || errorCode.equals("filtered") || errorCode.equals("contenttoobig") || errorCode.equals("emptypage") || errorCode.equals("emptynewsection") || errorCode.equals("selfmove") || errorCode.equals("nonfilenamespace") || errorCode.equals("filetypemismatch"))
 					throw new MediaWiki.ContentException(errorCode + ": " + errorInfo);
 
 				if (errorCode.equals("blocked") || errorCode.equals("autoblocked"))
 					throw new MediaWiki.BlockException(errorCode + ": " + errorInfo);
 
 				// Least likely errors
 				if (errorCode.equals("pagedeleted") || errorCode.equals("editconflict"))
 					throw new MediaWiki.ConflictException(errorCode + ": " + errorInfo);
 				if (errorCode.equals("readonly") || errorCode.equals("immobilenamespace"))
 					throw new MediaWiki.RestrictionException(errorCode + ": " + errorInfo);
 
 				if (errorCode.equals("unsupportednamespace"))
 					throw new UnsupportedOperationException(errorInfo);
 				if (errorCode.equals("unknownerror"))
 					throw new MediaWiki.UnknownError(errorInfo);
 
 				throw new MediaWiki.MediaWikiException(errorCode + ": " + errorInfo);
 			}
 		} else
 			throw new MediaWiki.ResponseFormatException("No <api> tag in reply from wiki");
 	}
 
 	// - - - CUSTOM EXCEPTIONS - - -
 
 	/**
 	 * Type of exception thrown when an HTTP request that is expected to return
 	 * the status code 200 (OK) returns anything in the 400 or 500 ranges.
 	 */
 	public static class HttpStatusException extends IOException {
 		private static final long serialVersionUID = 1L;
 
 		private final int statusCode;
 
 		public HttpStatusException(final int statusCode) {
 			this.statusCode = statusCode;
 		}
 
 		@Override
 		public String getMessage() {
 			return Integer.toString(statusCode);
 		}
 
 		@Override
 		public String getLocalizedMessage() {
 			return getMessage();
 		}
 
 		public int getStatusCode() {
 			return statusCode;
 		}
 	}
 
 	public static class MediaWikiException extends Exception {
 		private static final long serialVersionUID = 1L;
 
 		public MediaWikiException() {
 			super();
 		}
 
 		public MediaWikiException(final String message, final Throwable cause) {
 			super(message, cause);
 		}
 
 		public MediaWikiException(final String message) {
 			super(message);
 		}
 
 		public MediaWikiException(final Throwable cause) {
 			super(cause);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when a username or password used to log into a
 	 * MediaWiki wiki is incorrect.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * logging in with the same credentials.
 	 */
 	public static class LoginFailureException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public LoginFailureException() {}
 
 		public LoginFailureException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when a login on a MediaWiki wiki is refused due
 	 * to excessive login attempts for a user.
 	 * <p>
 	 * Generally, after receiving this exception, client code should retry
 	 * logging in with the same credentials after the number of seconds returned
 	 * by <code>getWaitTime()</code>.
 	 */
 	public static class LoginDelayException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		private final int waitTime;
 
 		public LoginDelayException(final int waitTime) {
 			this.waitTime = waitTime;
 		}
 
 		@Override
 		public String getMessage() {
 			return Integer.toString(waitTime);
 		}
 
 		@Override
 		public String getLocalizedMessage() {
 			return getMessage();
 		}
 
 		public int getWaitTime() {
 			return waitTime;
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an action on a MediaWiki wiki is refused
 	 * due to rate limiting or a wiki-wide temporary problem, such as
 	 * maintenance switching the wiki to read-only mode.
 	 * <p>
 	 * Generally, after receiving this exception, client code should retry the
 	 * action with the same content later.
 	 */
 	public static class ActionDelayException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public ActionDelayException() {}
 
 		public ActionDelayException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an action on a MediaWiki wiki has failed.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * the action.
 	 */
 	public static class ActionFailureException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public ActionFailureException() {}
 
 		public ActionFailureException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an action on a MediaWiki wiki is refused
 	 * due to it conflicting with another user's action on the same page.
 	 * <p>
 	 * Generally, after receiving this exception, client code should retry the
 	 * action immediately, honoring the new content of the page.
 	 */
 	public static class ConflictException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public ConflictException() {}
 
 		public ConflictException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an action involving writes to a MediaWiki
 	 * wiki is refused due to a problem with the content being written. For
 	 * example, this type of exception is thrown when spam is detected, or when
 	 * content exceeds the wiki's page size limit.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * the write with the same content.
 	 */
 	public static class ContentException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public ContentException() {}
 
 		public ContentException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when anything that is attempted on a MediaWiki
 	 * wiki returns that the user or his/her IP address is blocked.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not
 	 * attempts any more writes to the wiki.
 	 */
 	public static class BlockException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public BlockException() {}
 
 		public BlockException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an unknown error occurs while attempting
 	 * anything on a MediaWiki wiki.
 	 * <p>
 	 * Generally, after receiving this exception, client code should retry the
 	 * same action immediately, or retry it after a delay, or abort the
 	 * operation and perform others, depending on the action. Receiving this
 	 * exception multiple times when performing an action may also indicate a
 	 * server-side bug.
 	 */
 	public static class UnknownError extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public UnknownError() {}
 
 		public UnknownError(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an expected part of an API response is not
 	 * present, or not formatted correctly.
 	 * <p>
 	 * Generally, after receiving this exception, client code should retry the
 	 * same action after a small delay, or abort it if it receives the exception
 	 * again.
 	 */
 	public static class ResponseFormatException extends IOException {
 		private static final long serialVersionUID = 1L;
 
 		public ResponseFormatException() {
 			super();
 		}
 
 		public ResponseFormatException(final String message, final Throwable cause) {
 			super(message, cause);
 		}
 
 		public ResponseFormatException(final String message) {
 			super(message);
 		}
 
 		public ResponseFormatException(final Throwable cause) {
 			super(cause);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when attempting to perform an action on a page
 	 * that is denied by protection applied on the page. As opposed to
 	 * <tt>PermissionException</tt>, this type of exception is thrown when the
 	 * same user <em>could</em> perform the same action on another similar page,
 	 * e.g. in the same namespace.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * performing the protected action on the page until the page is
 	 * unprotected.
 	 * 
 	 * @see MediaWiki.PermissionException
 	 */
 	public static class ProtectionException extends MediaWiki.PermissionException {
 		private static final long serialVersionUID = 1L;
 
 		public ProtectionException() {}
 
 		public ProtectionException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when attempting to perform an action on a page
 	 * that is denied by user rights. As opposed to <tt>ProtectionException</tt>
 	 * , this type of exception is thrown when the same user <em>cannot</em>
 	 * perform the same action on another similar page, e.g. in the same
 	 * namespace.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * performing the denied action on the page or any similar pages until the
 	 * user has sufficient rights.
 	 * <p>
 	 * For code that only cares about whether an action is permitted <em>for the
 	 * user</em> (e.g. to try logging in again), catching this exception also
 	 * catches <tt>MediaWiki.ProtectionException</tt>.
 	 * 
 	 * @see MediaWiki.ProtectionException
 	 */
 	public static class PermissionException extends MediaWiki.RestrictionException {
 		private static final long serialVersionUID = 1L;
 
 		public PermissionException() {}
 
 		public PermissionException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when attempting to perform an action on a page
 	 * that is denied by the nature of the action on the wiki. This happens when
 	 * the entire wiki is read-only, or the entire file namespace cannot be the
 	 * subject of any move.
 	 * <p>
 	 * Generally, after receiving this exception, client code should not retry
 	 * performing the restricted action on the page or any similar pages.
 	 * <p>
 	 * For code that only cares about whether an action is permitted or denied,
 	 * catching this exception also catches
 	 * <tt>MediaWiki.PermissionException</tt> and
 	 * <tt>MediaWiki.ProtectionException</tt>.
 	 */
 	public static class RestrictionException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public RestrictionException() {}
 
 		public RestrictionException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an edit on a MediaWiki wiki requires a page
 	 * not to exist, and it does.
 	 * <p>
 	 * Generally, after receiving this exception, client code should abort the
 	 * creation of the page.
 	 */
 	public static class ExistingPageException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public ExistingPageException() {}
 
 		public ExistingPageException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of exception thrown when an edit on a MediaWiki wiki requires a page
 	 * to exist, and it doesn't.
 	 * <p>
 	 * Generally, after receiving this exception, client code should abort the
 	 * replacement of the page.
 	 */
 	public static class MissingPageException extends MediaWiki.MediaWikiException {
 		private static final long serialVersionUID = 1L;
 
 		public MissingPageException() {}
 
 		public MissingPageException(final String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Type of unchecked exception thrown when an iterator returned by a
 	 * <tt>MediaWiki</tt> method encounters an exception.
 	 */
 	public static class IterationException extends RuntimeException {
 		private static final long serialVersionUID = 1L;
 
 		public IterationException() {}
 
 		public IterationException(final String message, final Throwable cause) {
 			super(message, cause);
 		}
 
 		public IterationException(final String message) {
 			super(message);
 		}
 
 		public IterationException(final Throwable cause) {
 			super(cause);
 		}
 	}
 }
