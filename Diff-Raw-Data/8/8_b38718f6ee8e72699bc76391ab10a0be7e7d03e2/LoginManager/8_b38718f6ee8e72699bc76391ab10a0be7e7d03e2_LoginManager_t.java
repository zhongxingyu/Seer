 package grisu.frontend.control.login;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.ServiceInterfaceException;
 import grisu.frontend.control.UncaughtExceptionHandler;
 import grisu.jcommons.configuration.CommonGridProperties;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.GridEnvironment;
 import grisu.jcommons.dependencies.BouncyCastleTool;
 import grisu.jcommons.dependencies.ClasspathHacker;
 import grisu.jcommons.exceptions.CredentialException;
 import grisu.jcommons.utils.DefaultGridSecurityProvider;
 import grisu.jcommons.utils.EnvironmentVariableHelpers;
 import grisu.jcommons.utils.JythonHelpers;
 import grisu.jcommons.view.cli.CliHelpers;
 import grisu.model.GrisuRegistryManager;
 import grisu.settings.ClientPropertiesManager;
 import grisu.settings.Environment;
 import grisu.utils.GrisuPluginFilenameFilter;
 import grith.jgrith.control.LoginParams;
 import grith.jgrith.credential.Credential;
 import grith.jgrith.credential.Credential.PROPERTY;
 import grith.jgrith.credential.CredentialFactory;
 import grith.jgrith.credential.CredentialLoader;
 import grith.jgrith.utils.CertificateFiles;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 
 import com.google.common.collect.ImmutableBiMap;
 import com.google.common.collect.ImmutableMap;
 
 public class LoginManager {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(LoginManager.class
 					.getName());
 
 	private static String CLIENT_NAME = setClientName(null);
 
 	private static String CLIENT_VERSION = setClientVersion(null);
 
 	public static String USER_SESSION = setUserSessionId(null);
 
 	public static volatile boolean environmentInitialized = false;
 	public static final ImmutableBiMap<String, String> SERVICEALIASES = new ImmutableBiMap.Builder<String, String>()
 			.put("local", "Local")
 			.put("testbed",
 					"https://compute.test.nesi.org.nz/soap/GrisuService")
 					.put("bestgrid",
 							"https://compute.services.bestgrid.org/soap/GrisuService")
 							.put("nesi", "https://compute.nesi.org.nz/soap/GrisuService")
 							.put("dev",
 									"https://compute-dev.services.bestgrid.org/soap/GrisuService")
 									.put("bestgrid-test",
 											"https://compute-test.services.bestgrid.org/soap/GrisuService")
 											.put("local_ws_jetty", "http://localhost:8080/soap/GrisuService")
 											.put("local_ws",
 													"http://localhost:8080/grisu-ws/soap/GrisuService").build();
 
 	public static final ImmutableMap<String, String> MYPROXY_SERVERS = new ImmutableMap.Builder<String, String>()
 			.put("Local",
 					GridEnvironment.getDefaultMyProxyServer() + ":"
 							+ GridEnvironment.getDefaultMyProxyPort())
 							.put("https://compute.test.nesi.org.nz/soap/GrisuService",
 									"myproxy.test.nesi.org.nz:7512")
 									.put("https://compute.nesi.org.nz/soap/GrisuService",
 											"myproxy.arcs.org.au:7512")
 											.put("https://compute.services.bestgrid.org/soap/GrisuService",
 													"myproxy.arcs.org.au:7512")
 													.put("https://compute-dev.services.bestgrid.org/soap/GrisuService",
 															"myproxy.arcs.org.au:7512")
 															.put("https://compute-test.services.bestgrid.org/soap/GrisuService",
 																	"myproxy.arcs.org.au:7512")
 																	.put("http://localhost:8080/grisu-ws/soap/GrisuService",
 																			GridEnvironment.getDefaultMyProxyServer() + ":"
 																					+ GridEnvironment.getDefaultMyProxyPort())
 																					.put("http://localhost:8080/soap/GrisuService",
 																							GridEnvironment.getDefaultMyProxyServer() + ":"
 																									+ GridEnvironment.getDefaultMyProxyPort()).build();
 
 	public static String httpProxyHost = null;
 
 	public static int httpProxyPort = 80;
 
 	public static String httpProxyUsername = null;
 
 	public static char[] httpProxyPassphrase = null;
 
 	public static int REQUIRED_BACKEND_API_VERSION = 15;
 
 	public static final int DEFAULT_PROXY_LIFETIME_IN_HOURS = 240;
 
 	public static String myProxyHost = null;
 	public static int myProxyPort = -1;
 
 	public static void addPluginsToClasspath() throws IOException {
 
 		ClasspathHacker.initFolder(Environment.getGrisuPluginDirectory(),
 				new GrisuPluginFilenameFilter());
 
 	}
 
 	public static void clearHttpProxyPassword() {
 		Arrays.fill(httpProxyPassphrase, 'x');
 	}
 
 	public static String getClientName() {
 		return CLIENT_NAME;
 	}
 
 	public static String getClientVersion() {
 		return CLIENT_VERSION;
 	}
 
 	public static synchronized void initEnvironment() {
 
 		if (!environmentInitialized) {
 
 			// make sure tmp dir exists
 			String tmpdir = System.getProperty("java.io.tmpdir");
 			if (tmpdir.startsWith("~")) {
 				tmpdir = tmpdir.replaceFirst("~",
 						System.getProperty("user.home"));
 				System.setProperty("java.io.tmpdir", tmpdir);
 			}
 			File tmp = new File(tmpdir);
 			if (!tmp.exists()) {
 				myLogger.debug("Creating tmpdir: {}", tmpdir);
 				tmp.mkdirs();
 				if (!tmp.exists()) {
 					myLogger.error("Could not create tmp dir {}.", tmpdir);
 				}
 			}
 
 			java.util.logging.LogManager.getLogManager().reset();
 			// LoggerFactory.getLogger("root").setLevel(Level.OFF);
 
 			JythonHelpers.setJythonCachedir();
 
 			final String debug = CommonGridProperties
 					.getDefault()
 					.getGridProperty(
 							CommonGridProperties.Property.DEBUG_UNCAUGHT_EXCEPTIONS);
 
 			if ("true".equalsIgnoreCase(debug)) {
 				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
 			}
 
 			java.security.Security
 			.addProvider(new DefaultGridSecurityProvider());
 
 			java.security.Security
 			.setProperty("ssl.TrustManagerFactory.algorithm",
 					"TrustAllCertificates");
 
 			try {
 				BouncyCastleTool.initBouncyCastle();
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
 			try {
 				CertificateFiles.copyCACerts(false);
 			} catch (Exception e) {
 				myLogger.error("Problem copying root certificates.", e);
 			}
 
 			environmentInitialized = true;
 		}
 
 	}
 
 	public static synchronized void initGrisuClient(String clientname) {
 
 		Thread.currentThread().setName("main");
 
 		LoginManager.setClientName(clientname);
 
 		LoginManager.setClientVersion(grisu.jcommons.utils.Version
 				.get(clientname));
 
 		EnvironmentVariableHelpers.loadEnvironmentVariablesToSystemProperties();
 
 		LoginManager.initEnvironment();
 
 	}
 
 	public static ServiceInterface login() throws LoginException {
 		return login("Local");
 	}
 
 	public static ServiceInterface login(Credential cred,
 			LoginParams loginParams, boolean displayCliProgress)
 					throws LoginException {
 
 		String defaultUrl = ClientPropertiesManager
 				.getDefaultServiceInterfaceUrl();
 
 		try {
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress(
 						"Setting up environment...", true);
 			}
 			initEnvironment();
 
 			if (loginParams == null) {
 
 				if (StringUtils.isBlank(defaultUrl)) {
 					defaultUrl = "Local";
 				}
 				loginParams = new LoginParams(defaultUrl, null, null);
 
 			}
 
 			loginParams.setAliasMap(SERVICEALIASES);
 			loginParams.setMyProxyMap(MYPROXY_SERVERS);
 
 			try {
 				addPluginsToClasspath();
 			} catch (final IOException e2) {
 				// TODO Auto-generated catch block
 				myLogger.warn(e2.getLocalizedMessage(), e2);
 				throw new RuntimeException(e2);
 			}
 
 			// do the cacert thingy
 			try {
 				final URL cacertURL = LoginManager.class
 						.getResource("/ipsca.pem");
 				final HttpSecureProtocol protocolSocketFactory = new HttpSecureProtocol();
 
 				TrustMaterial trustMaterial = null;
 				trustMaterial = new TrustMaterial(cacertURL);
 
 				// We can use setTrustMaterial() instead of addTrustMaterial()
 				// if we want to remove
 				// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 				protocolSocketFactory.addTrustMaterial(trustMaterial);
 
 				// Maybe we want to turn off CN validation (not recommended!):
 				protocolSocketFactory.setCheckHostname(false);
 
 				final Protocol protocol = new Protocol("https",
 						(ProtocolSocketFactory) protocolSocketFactory, 443);
 				Protocol.registerProtocol("https", protocol);
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress("Uploading credential...", true);
 			}
 
 			// making sure that the right myproxy server is used for upload
 			if (StringUtils.isNotBlank(loginParams.getMyProxyServer())) {
 				cred.setProperty(PROPERTY.MyProxyHost,
 						loginParams.getMyProxyServer());
 				cred.setProperty(PROPERTY.MyProxyPort,
 						Integer.parseInt(loginParams.getMyProxyPort()));
 			}
 
 			try {
 				cred.uploadMyProxy();
 			} catch (Exception e) {
 				throw new LoginException("Could not upload myproxy credential.", e);
 			}
 
 			ServiceInterface si;
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress("Logging in to backend...",
 						true);
 			}
 			try {
 				si = ServiceInterfaceFactory.createInterface(
 						loginParams.getLoginUrl(), cred.getMyProxyUsername(),
 						cred.getMyProxyPassword(), cred.getMyProxyServer(),
 						new Integer(cred.getMyProxyPort()).toString(),
 						loginParams.getHttpProxy(), loginParams.getHttpProxyPort(),
 						loginParams.getHttpProxyUsername(),
 						loginParams.getHttpProxyPassphrase());
 			} catch (ServiceInterfaceException e) {
 				throw new LoginException("Could not login to backend.", e);
 			} catch (Throwable t) {
 				t.printStackTrace();
 				throw new LoginException("Error while loggin in: "
 						+ t.getLocalizedMessage());
 			}
 
 			loginParams.clearPasswords();
 
 			GrisuRegistryManager.registerServiceInterface(si, cred);
 			GrisuRegistryManager.getDefault(si).set(Constants.BACKEND,
 					loginParams.getLoginUrl());
 
 			return si;
 		} finally {
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress(false);
 
 			}
 		}
 
 
 	}
 
 	public static ServiceInterface login(Credential cred,
 			String backend,
 			boolean displayCliProgress)
 					throws LoginException {
 		LoginParams params = new LoginParams(backend, null, null);
 		return login(cred, params, displayCliProgress);
 	}
 
 	public static ServiceInterface login(String backend)
 			throws LoginException {
 		Credential cred = null;
 		try {
 			cred = CredentialFactory.loadFromLocalProxy();
 		} catch (Exception e) {
 			throw new LoginException("Could not load default credential.", e);
 		}
 		return login(cred, backend, false);
 
 	}
 
 	public static ServiceInterface login(String backend, String credConfigFile,
 			String nameOfCredentialToUse, boolean displayCliProgress)
 					throws LoginException {
 
 		if ( StringUtils.isBlank(credConfigFile) ) {
 			throw new LoginException("Credential config file not specified.");
 		}
 
 		File configFile = new File(credConfigFile);
 		if ( !configFile.exists() || ! configFile.canRead() ) {
 			throw new LoginException("Can't read credential config file: "+credConfigFile);
 		}
 
 		Map<String, Credential> creds = CredentialLoader.loadCredentials(credConfigFile);
 
 		if ( (creds == null) || (creds.size() == 0) ) {
 			throw new LoginException("Can't load any credentials using config file: "+credConfigFile);
 		}
 
 		Credential cred = null;
 		if ( StringUtils.isNotBlank(nameOfCredentialToUse) ) {
 			cred = creds.get(nameOfCredentialToUse);
 
 			if (cred == null) {
 				throw new LoginException("Can't load credential with alias "
 						+ nameOfCredentialToUse + " using config file: "
 						+ credConfigFile);
 			}
 		} else {
 			cred = creds.values().iterator().next();
 		}
 
 		return login(cred, backend, displayCliProgress);
 
 	}
 
 	public static ServiceInterface loginCommandline() throws LoginException {
 		return loginCommandline(LoginManager.DEFAULT_PROXY_LIFETIME_IN_HOURS);
 	}
 
 	public static ServiceInterface loginCommandline(int proxy_lifetime_in_hours)
 			throws LoginException {
 		return loginCommandline("Local", proxy_lifetime_in_hours);
 	}
 
 	public static ServiceInterface loginCommandline(String backend)
 			throws LoginException {
 		return loginCommandline(backend,
 				LoginManager.DEFAULT_PROXY_LIFETIME_IN_HOURS);
 	}
 
 	public static ServiceInterface loginCommandline(String backend,
 			boolean saveCredToDisk, int proxy_lifetime_in_hours) throws LoginException {
 		return loginCommandline(backend, saveCredToDisk, proxy_lifetime_in_hours, -1);
 	}
 
 	public static ServiceInterface loginCommandline(String backend,
 			boolean saveCredToDisk, int proxyLifetimeInHours, int minProxyLifetimeInSeconds)
 					throws LoginException {
 
 		Credential c = null;
 
 		boolean validLocalProxy = false;
 
 		try {
 			c = Credential.load();
 			int lifetime = c.getRemainingLifetime();
 			if (lifetime >= minProxyLifetimeInSeconds) {
 				validLocalProxy = true;
 			} else {
 				validLocalProxy = false;
 			}
 		} catch (CredentialException ce) {
 			validLocalProxy = false;
 		}
 
 		if (myProxyPort <= 0) {
 			myProxyPort = GridEnvironment.getDefaultMyProxyPort();
 		}
 
 
 		if (validLocalProxy) {
 			CliHelpers.setIndeterminateProgress(
 					"Local credential found, logging in...", true);
 			try {
 
 				if (StringUtils.isNotBlank(myProxyHost)) {
 					c.uploadMyProxy(myProxyHost, myProxyPort, false);
 				} else {
 					c.uploadMyProxy();
 				}
 			} finally {
 				CliHelpers.setIndeterminateProgress(false);
 			}
 
 		} else {
 
 			LoginParams p = new LoginParams(backend, null, null);
			p.setMyProxyMap(MYPROXY_SERVERS);
			p.setAliasMap(SERVICEALIASES);
 			if (StringUtils.isNotBlank(myProxyHost)) {
 				p.setMyProxyServer(myProxyHost);
 				p.setMyProxyPort(Integer.toString(myProxyPort));
 			}

 			c = CredentialFactory
 					.createFromCommandline(p,
 							proxyLifetimeInHours);
 			if (StringUtils.isNotBlank(myProxyHost)) {
 				c.uploadMyProxy(myProxyHost, myProxyPort, false);
 			} else {
				c.uploadMyProxy(p.getMyProxyServer(),
						Integer.parseInt(p.getMyProxyPort()), false);
 			}
 			if (saveCredToDisk) {
 				c.saveCredential();
 			}
 		}
 
 		return login(c, backend, true);
 
 	}
 
 	public static ServiceInterface loginCommandline(String backend, int proxy_lifetime_in_hours)
 			throws LoginException {
 		return loginCommandline(backend, true, proxy_lifetime_in_hours);
 	}
 
 	public static ServiceInterface loginCommandlineLocalProxy(String backend)
 			throws LoginException {
 		Credential c = Credential.load();
 
 		if ((c == null) || !c.isValid()) {
 			throw new CredentialException("Your session has expired. Please login and try again.");
 		}
 
 		return login(c, backend, true);
 	}
 
 	public static ServiceInterface loginCommandlineMyProxy(String backend,
 			String username, int proxy_lifetime_in_hours, boolean saveCredToDisk)
 					throws LoginException {
 
 		LoginParams p = new LoginParams(backend, username, null);
 
 		if (StringUtils.isNotBlank(myProxyHost)) {
 			p.setMyProxyServer(myProxyHost);
 			if (myProxyPort <= 0) {
 				p.setMyProxyPort(Integer
 						.toString(GridEnvironment
 								.getDefaultMyProxyPort()));
 			} else {
 				p.setMyProxyPort(Integer.toString(myProxyPort));
 			}
 		}
 
 		Credential c;
 		try {
 			c = CredentialFactory.createFromMyProxyCommandline(p,
 					proxy_lifetime_in_hours * 3600);
 			if (saveCredToDisk) {
 				c.saveCredential();
 			}
 		} catch (Exception e) {
 			throw new LoginException("Can't get credential from MyProxy.", e);
 		}
 
 		return login(c, backend, true);
 	}
 
 	public static ServiceInterface loginCommandlineShibboleth(String backend,
 			String username, String idp, boolean saveCredToDisk)
 					throws LoginException {
 
 		Credential c;
 		try {
 			c = CredentialFactory.createFromSlcsCommandline(username, idp,
 					DEFAULT_PROXY_LIFETIME_IN_HOURS * 3600);
 			if (saveCredToDisk) {
 				c.saveCredential();
 			}
 		} catch (Exception e) {
 			throw new LoginException("Can't get credential from MyProxy.", e);
 		}
 
 		return login(c, backend, true);
 
 	}
 
 	public static ServiceInterface loginCommandlineX509cert(String backend,
 			int proxy_lifetime_in_hours,
 			boolean saveCredToDisk)
 					throws LoginException {
 		Credential c = CredentialFactory
 				.createFromLocalCertCommandline(proxy_lifetime_in_hours);
 		if (saveCredToDisk) {
 			c.saveCredential();
 		}
 		return login(c, backend, true);
 	}
 
 	public static void main(String[] args) throws LoginException {
 
 		// Credential c = CredentialFactory.createFromCommandline();
 
 		ServiceInterface si = loginCommandline("dev", 12);
 		System.out.println("");
 		System.out.println(si.getDN());
 	}
 
 	public static ServiceInterface myProxyLogin(String username,
 			char[] password, String backend, boolean displayCliProgress)
 					throws LoginException {
 
 		Credential c = CredentialFactory.createFromMyProxy(username, password,
 				DEFAULT_PROXY_LIFETIME_IN_HOURS * 3600);
 		return login(c, backend, displayCliProgress);
 	}
 
 
 	public static String setClientName(String name) {
 
 		if (StringUtils.isBlank(name)) {
 			name = "Unknown";
 		}
 		CLIENT_NAME = name;
 		MDC.put("client", name);
 
 		return name;
 
 	}
 
 	public static String setClientVersion(String version ) {
 		if (StringUtils.isBlank(version)) {
 			version = "n/a";
 		}
 		CLIENT_VERSION = version;
 		MDC.put("client_version", version);
 
 		return version;
 	}
 
 	public static String setUserSessionId(String id) {
 		if (StringUtils.isBlank(id)) {
 			id = System.getProperty("user.name") + "_" + new Date().getTime();
 		}
 		USER_SESSION = id;
 		MDC.put("session", id);
 
 		return id;
 	}
 
 }
