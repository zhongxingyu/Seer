 package grisu.frontend.control.login;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.ServiceInterfaceException;
 import grisu.frontend.control.UncaughtExceptionHandler;
 import grisu.jcommons.configuration.CommonGridProperties;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.dependencies.BouncyCastleTool;
 import grisu.jcommons.dependencies.ClasspathHacker;
 import grisu.jcommons.exceptions.CredentialException;
 import grisu.jcommons.utils.DefaultGridSecurityProvider;
 import grisu.jcommons.utils.JythonHelpers;
 import grisu.jcommons.view.cli.CliHelpers;
 import grisu.model.GrisuRegistryManager;
 import grisu.settings.ClientPropertiesManager;
 import grisu.settings.Environment;
 import grisu.utils.GrisuPluginFilenameFilter;
 import grith.jgrith.control.LoginParams;
 import grith.jgrith.credential.Credential;
 import grith.jgrith.credential.CredentialFactory;
 import grith.jgrith.utils.CertificateFiles;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 
 import com.google.common.collect.ImmutableBiMap;
 
 public class LoginManager {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(LoginManager.class
 					.getName());
 
 	private static String CLIENT_NAME = setClientName(null);
 
 	private static String CLIENT_VERSION = setClientVersion(null);
 
 	public static String USER_SESSION = setUserSessionId(null);
 
 	public static volatile boolean environmentInitialized = false;
 	static final public ImmutableBiMap<String, String> SERVICEALIASES = new ImmutableBiMap.Builder<String, String>()
 			.put("local", "Local")
 			.put("bestgrid",
 					"https://compute.services.bestgrid.org/soap/GrisuService")
 					.put("dev",
 							"https://compute-dev.services.bestgrid.org/soap/GrisuService")
 							.put("bestgrid-test",
 									"https://compute-test.services.bestgrid.org/soap/GrisuService")
									.put("local_ws_jetty", "http://localhost:8080/soap/GrisuService")
									.put("local_ws",
 											"http://localhost:8080/grisu-ws/soap/GrisuService").build();
 	public static String httpProxyHost = null;
 
 	public static int httpProxyPort = 80;
 
 	public static String httpProxyUsername = null;
 
 	public static char[] httpProxyPassphrase = null;
 
 	public static int REQUIRED_BACKEND_API_VERSION = 15;
 
 	public static final int DEFAULT_PROXY_LIFETIME_IN_HOURS = 240;
 
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
 
 			environmentInitialized = true;
 		}
 
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
 
 			try {
 				addPluginsToClasspath();
 			} catch (final IOException e2) {
 				// TODO Auto-generated catch block
 				myLogger.warn(e2.getLocalizedMessage(), e2);
 				throw new RuntimeException(e2);
 			}
 
 			try {
 				CertificateFiles.copyCACerts(true);
 			} catch (final Exception e1) {
 				// e1.printStackTrace();
 				myLogger.warn(e1.getLocalizedMessage(), e1);
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
 				throw new LoginException("Coult not login to backend.", e);
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
 
 
 		if (validLocalProxy) {
 			CliHelpers.setIndeterminateProgress(
 					"Local credential found, logging in...", true);
 			try {
 				c.uploadMyProxy();
 			} finally {
 				CliHelpers.setIndeterminateProgress(false);
 			}
 
 		} else {
 			c = CredentialFactory.createFromCommandline(proxyLifetimeInHours);
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
 
 	public static ServiceInterface loginCommandlineMyProxy(String backend,
 			String username, int proxy_lifetime_in_hours, boolean saveCredToDisk)
 					throws LoginException {
 
 		LoginParams p = new LoginParams(backend, username, null);
 
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
