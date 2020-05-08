 package org.vpac.grisu.frontend.control.login;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import jline.ConsoleReader;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.apache.log4j.Logger;
 import org.globus.gsi.GlobusCredential;
 import org.ietf.jgss.GSSCredential;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.frontend.control.UncaughtExceptionHandler;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.utils.GrisuPluginFilenameFilter;
 import org.vpac.security.light.CredentialHelpers;
 import org.vpac.security.light.Init;
 import org.vpac.security.light.control.CertificateFiles;
 import org.vpac.security.light.plainProxy.LocalProxy;
 
 import au.org.arcs.auth.shibboleth.CredentialManager;
 import au.org.arcs.auth.shibboleth.DummyCredentialManager;
 import au.org.arcs.auth.shibboleth.DummyIdpObject;
 import au.org.arcs.auth.shibboleth.IdpObject;
 import au.org.arcs.auth.shibboleth.Shibboleth;
 import au.org.arcs.auth.slcs.SLCS;
 import au.org.arcs.jcommons.configuration.CommonArcsProperties;
 import au.org.arcs.jcommons.constants.ArcsEnvironment;
 import au.org.arcs.jcommons.constants.Enums.LoginType;
 import au.org.arcs.jcommons.constants.Enums.UI;
 import au.org.arcs.jcommons.dependencies.ClasspathHacker;
 import au.org.arcs.jcommons.dependencies.Dependency;
 import au.org.arcs.jcommons.dependencies.DependencyManager;
 import au.org.arcs.jcommons.utils.ArcsSecurityProvider;
 import au.org.arcs.jcommons.utils.HttpProxyManager;
 import au.org.arcs.jcommons.utils.JythonHelpers;
 
 import com.google.common.collect.ImmutableBiMap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 public class LoginManager {
 
 	private static ConsoleReader consoleReader;
 
 	public static boolean environmentInitialized = false;
 
 	static final Logger myLogger = Logger.getLogger(LoginManager.class
 			.getName());
 
 	static final public ImmutableBiMap<String, String> SERVICEALIASES = new ImmutableBiMap.Builder<String, String>()
 			.put("LOCAL", "Local")
 			.put("ARCS",
 					"https://grisu-vpac.arcs.org.au/grisu-ws/soap/GrisuService")
 			.put("ARCS_DEV",
 					"https://ngportal.vpac.org/grisu-ws/soap/GrisuService")
 			.put("BeSTGRID",
 					"https://globus.ceres.auckland.ac.nz:8443/grisu-ws/soap/GrisuService")
 			.put("BeSTGRID_TEST",
 					"https://grisu.ceres.auckland.ac.nz/grisu-ws/soap/GrisuService")
 			.put("LOCAL_WS", "http://localhost:8080/soap/GrisuService")
 			.put("LOCAL_WS_TOMCAT",
 					"http://localhost:8080/grisu-ws/soap/GrisuService").build();
 
 	public static String httpProxyHost = null;
 
 	public static int httpProxyPort = 80;
 
 	public static String httpProxyUsername = null;
 
 	public static char[] httpProxyPassphrase = null;
 
 	public static void addPluginsToClasspath() throws IOException {
 
 		ClasspathHacker.initFolder(Environment.getGrisuPluginDirectory(),
 				new GrisuPluginFilenameFilter());
 
 	}
 
 	public static void clearHttpProxyPassword() {
 		Arrays.fill(httpProxyPassphrase, 'x');
 	}
 
 	private static ConsoleReader getConsoleReader() {
 		if (consoleReader == null) {
 			try {
 				consoleReader = new ConsoleReader();
 			} catch (IOException e) {
 				throw new RuntimeException();
 			}
 		}
 		return consoleReader;
 	}
 
 	public static void initEnvironment() {
 
 		if (!environmentInitialized) {
 
 			JythonHelpers.setJythonCachedir();
 
 			String debug = CommonArcsProperties.getDefault().getArcsProperty(
 					CommonArcsProperties.Property.DEBUG_UNCAUGHT_EXCEPTIONS);
 
 			if ("true".equalsIgnoreCase(debug)) {
 				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
 			}
 
 			java.security.Security.addProvider(new ArcsSecurityProvider());
 
 			java.security.Security
 					.setProperty("ssl.TrustManagerFactory.algorithm",
 							"TrustAllCertificates");
 
 			String disableLoadBouncyCastle = System
 					.getProperty("disableLoadBouncyCastle");
 
 			if (!"true".equalsIgnoreCase(disableLoadBouncyCastle)) {
 
 				Map<Dependency, String> dependencies = new HashMap<Dependency, String>();
 
 				dependencies.put(Dependency.BOUNCYCASTLE, "jdk15-145");
 
 				DependencyManager.addDependencies(dependencies,
 						ArcsEnvironment.getArcsCommonJavaLibDirectory(), true);
 			}
 
 			environmentInitialized = true;
 		}
 
		try {
			Init.initBouncyCastle();
		} catch (Exception e) {
			e.printStackTrace();
		}
 
 	}
 
 	/**
 	 * Simplest way to login.
 	 * 
 	 * Logs into a local backend using an already existing local proxy.
 	 * 
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 */
 	public static ServiceInterface login() throws LoginException {
 		return login((GlobusCredential) null, (char[]) null, (String) null,
 				(String) null, (LoginParams) null, false);
 	}
 
 	/**
 	 * One-for-all method to login to a local Grisu backend.
 	 * 
 	 * Specify nothing in order to use a local proxy. If you specify the
 	 * password in addition to that the local x509 cert will be used to create a
 	 * proxy from your local cert which in turn will be used to login to the
 	 * Grisu backend.
 	 * 
 	 * 
 	 * In order to use shibboleth login, you need to specify the password, the
 	 * idp-username and the name of the idp.
 	 * 
 	 * @param password
 	 *            the password or null
 	 * @param username
 	 *            the shib-username or null
 	 * @param idp
 	 *            the name of the idp or null
 	 * @param saveCredentialAsLocalProxy
 	 *            whether to save the credential as a local proxy after
 	 *            successful login or not
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 * @throws IOException
 	 *             if necessary plugins couldn't be downloaded/stored in the
 	 *             .grisu/plugins folder
 	 */
 	public static ServiceInterface login(GlobusCredential cred,
 			char[] password, String username, String idp,
 			boolean saveCredentialAsLocalProxy) throws LoginException {
 
 		LoginParams params = new LoginParams("Local", null, null);
 		return login(cred, password, username, idp, params,
 				saveCredentialAsLocalProxy);
 
 	}
 
 	public static ServiceInterface login(GlobusCredential cred,
 			char[] password, String username, String idp,
 			LoginParams loginParams) throws LoginException {
 		return login(cred, password, username, idp, loginParams, false);
 	}
 
 	/**
 	 * One-for-all method to login to a Grisu backend.
 	 * 
 	 * Specify nothing except the loginParams (without the myproxy username &
 	 * password) in order to use a local proxy. If you specify the password in
 	 * addition to that the local x509 cert will be used to create a local proxy
 	 * which in turn will be used to login to the Grisu backend.
 	 * 
 	 * If you specify the myproxy username & password in the login params those
 	 * will be used for a simple myproxy login to the backend.
 	 * 
 	 * In order to use shibboleth login, you need to specify the password, the
 	 * idp-username and the name of the idp.
 	 * 
 	 * @param password
 	 *            the password or null
 	 * @param username
 	 *            the shib-username or null
 	 * @param idp
 	 *            the name of the idp or null
 	 * @param loginParams
 	 *            the login parameters
 	 * @param saveCredentialAsLocalProxy
 	 *            whether to save the credential as a local proxy after
 	 *            successful login or not
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 * @throws IOException
 	 *             if necessary plugins couldn't be downloaded/stored in the
 	 *             .grisu/plugins folder
 	 */
 	public static ServiceInterface login(GlobusCredential cred,
 			char[] password, String username, String idp,
 			LoginParams loginParams, boolean saveCredentialAsLocalProxy)
 			throws LoginException {
 
 		initEnvironment();
 
 		if (loginParams == null) {
 
 			String defaultUrl = ClientPropertiesManager
 					.getDefaultServiceInterfaceUrl();
 			if (StringUtils.isNotBlank(defaultUrl)) {
 				loginParams = new LoginParams(defaultUrl, null, null);
 			} else {
 				loginParams = new LoginParams("Local", null, null);
 			}
 
 		}
 
 		if (StringUtils.isNotBlank(httpProxyHost)) {
 			loginParams.setHttpProxy(httpProxyHost);
 			loginParams.setHttpProxyPort(httpProxyPort);
 			loginParams.setHttpProxyUsername(httpProxyUsername);
 			loginParams.setHttpProxyPassphrase(httpProxyPassphrase);
 		}
 
 		try {
 			addPluginsToClasspath();
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			myLogger.warn(e2);
 			throw new RuntimeException(e2);
 		}
 
 		try {
 			CertificateFiles.copyCACerts(true);
 		} catch (Exception e1) {
 			// e1.printStackTrace();
 			myLogger.warn(e1);
 		}
 
 		// do the cacert thingy
 		try {
 			URL cacertURL = LoginManager.class.getResource("/ipsca.pem");
 			HttpSecureProtocol protocolSocketFactory = new HttpSecureProtocol();
 
 			TrustMaterial trustMaterial = null;
 			trustMaterial = new TrustMaterial(cacertURL);
 
 			// We can use setTrustMaterial() instead of addTrustMaterial()
 			// if we want to remove
 			// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 			protocolSocketFactory.addTrustMaterial(trustMaterial);
 
 			// Maybe we want to turn off CN validation (not recommended!):
 			protocolSocketFactory.setCheckHostname(false);
 
 			Protocol protocol = new Protocol("https",
 					(ProtocolSocketFactory) protocolSocketFactory, 443);
 			Protocol.registerProtocol("https", protocol);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		Map<Dependency, String> dependencies = new HashMap<Dependency, String>();
 		String serviceInterfaceUrl = loginParams.getServiceInterfaceUrl();
 
 		if ("Local".equals(serviceInterfaceUrl)
 				|| "Dummy".equals(serviceInterfaceUrl)) {
 
 			dependencies = new HashMap<Dependency, String>();
 
 			dependencies.put(Dependency.GRISU_LOCAL_BACKEND,
 					ServiceInterface.INTERFACE_VERSION);
 
 			DependencyManager.addDependencies(dependencies,
 					Environment.getGrisuPluginDirectory());
 
 		} else if (serviceInterfaceUrl.startsWith("http")) {
 
 			// assume xfire -- that needs to get smarter later on
 
 			dependencies = new HashMap<Dependency, String>();
 
 			dependencies.put(Dependency.GRISU_XFIRE_CLIENT_LIBS,
 					ServiceInterface.INTERFACE_VERSION);
 			// also try to use client side mds
 			dependencies.put(Dependency.CLIENT_SIDE_MDS,
 					ServiceInterface.INTERFACE_VERSION);
 
 			DependencyManager.addDependencies(dependencies,
 					Environment.getGrisuPluginDirectory());
 
 		}
 
 		ServiceInterface si = null;
 
 		if (StringUtils.isBlank(username)) {
 
 			if (StringUtils.isBlank(loginParams.getMyProxyUsername())) {
 
 				// set random myproxy username, not dn, because otherwise
 				// parallel sessions
 				// wouldn't be possible
 				loginParams.setMyProxyUsername(UUID.randomUUID().toString());
 
 				if (cred != null) {
 					try {
 						si = LoginHelpers.globusCredentialLogin(loginParams,
 								cred);
 					} catch (Exception e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				} else if ((password == null) || (password.length == 0)) {
 					// means certificate auth
 					try {
 						// means try to load local proxy
 						si = LoginHelpers.defaultLocalProxyLogin(loginParams);
 						ClientPropertiesManager
 								.saveLastLoginType(LoginType.LOCAL_PROXY);
 
 					} catch (Exception e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				} else {
 					// means to create local proxy
 					try {
 						// TODO should put that one somewhere else
 						if (saveCredentialAsLocalProxy) {
 							try {
 								LocalProxy.gridProxyInit(password, 240);
 							} catch (Exception e) {
 								throw new ServiceInterfaceException(
 										"Could not create local proxy.", e);
 							}
 						}
 						si = LoginHelpers
 								.localProxyLogin(password, loginParams);
 						ClientPropertiesManager
 								.saveLastLoginType(LoginType.X509_CERTIFICATE);
 					} catch (ServiceInterfaceException e) {
 						throw new LoginException("Could not login: "
 								+ e.getLocalizedMessage(), e);
 					}
 				}
 
 			} else {
 				// means myproxy login
 				try {
 					si = LoginHelpers.myProxyLogin(loginParams);
 					ClientPropertiesManager
 							.saveLastLoginType(LoginType.MYPROXY);
 					CommonArcsProperties.getDefault().setLastMyProxyUsername(
 							loginParams.getMyProxyUsername());
 				} catch (ServiceInterfaceException e) {
 					throw new LoginException("Could not login: "
 							+ e.getLocalizedMessage(), e);
 				}
 			}
 		} else {
 			try {
 				// means shib login
 				dependencies = new HashMap<Dependency, String>();
 
 				dependencies.put(Dependency.ARCSGSI, "1.2-SNAPSHOT");
 
 				DependencyManager.addDependencies(dependencies,
 						Environment.getGrisuPluginDirectory());
 
 				GSSCredential slcsproxy = slcsMyProxyInit(username, password,
 						idp, loginParams);
 
 				if (saveCredentialAsLocalProxy) {
 					CredentialHelpers.writeToDisk(slcsproxy);
 				}
 
 				si = LoginHelpers.gssCredentialLogin(loginParams, slcsproxy);
 				ClientPropertiesManager.saveLastLoginType(LoginType.SHIBBOLETH);
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new LoginException("Could not do slcs login: "
 						+ e.getLocalizedMessage(), e);
 			}
 
 		}
 
 		ClientPropertiesManager.setDefaultServiceInterfaceUrl(loginParams
 				.getServiceInterfaceUrl());
 
 		return si;
 	}
 
 	/**
 	 * One-for-all method to login to a Grisu backend.
 	 * 
 	 * Specify nothing except the loginParams (without the myproxy username &
 	 * password) in order to use a local proxy. If you specify the password in
 	 * addition to that the local x509 cert will be used to create a local proxy
 	 * which in turn will be used to login to the Grisu backend.
 	 * 
 	 * If you specify the myproxy username & password in the login params those
 	 * will be used for a simple myproxy login to the backend.
 	 * 
 	 * In order to use shibboleth login, you need to specify the password, the
 	 * idp-username and the name of the idp.
 	 * 
 	 * @param password
 	 *            the password or null
 	 * @param username
 	 *            the shib-username or null
 	 * @param idp
 	 *            the name of the idp or null
 	 * @param url
 	 *            the serviceInterfaceUrl to connect to
 	 * @param saveCredentialAsLocalProxy
 	 *            whether to save the credential as a local proxy after
 	 *            successful login or not
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 * @throws IOException
 	 *             if necessary plugins couldn't be downloaded/stored in the
 	 *             .grisu/plugins folder
 	 */
 	public static ServiceInterface login(GlobusCredential cred,
 			char[] password, String username, String idp, String url,
 			boolean saveCredentialAsLocalProxy) throws LoginException {
 
 		LoginParams params = new LoginParams(url, null, null);
 		return login(cred, password, username, idp, params,
 				saveCredentialAsLocalProxy);
 
 	}
 
 	/**
 	 * 2nd simplest way to login.
 	 * 
 	 * Logs into a backend using an already existing local proxy.
 	 * 
 	 * @param url
 	 *            the url of the serviceinterface
 	 * 
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 */
 	public static ServiceInterface login(String url) throws LoginException {
 		return login((GlobusCredential) null, (char[]) null, (String) null,
 				(String) null, url, false);
 	}
 
 	public static ServiceInterface login(UI ui, Set<LoginType> types, String url)
 			throws LoginException {
 
 		switch (ui) {
 		case COMMANDLINE:
 			return loginCommandline(types, url);
 		case SWING:
 			return loginSwing(types, url);
 		}
 
 		throw new IllegalArgumentException("Login type " + ui.toString()
 				+ " not supported.");
 	}
 
 	public static ServiceInterface loginCommandline() throws LoginException {
 		return loginCommandline(SERVICEALIASES.get("LOCAL"));
 	}
 
 	public static ServiceInterface loginCommandline(LoginType type, String url)
 			throws LoginException {
 
 		switch (type) {
 		case SHIBBOLETH:
 			return loginCommandlineShibboleth(url);
 		case MYPROXY:
 			return loginCommandlineMyProxy(url);
 		case LOCAL_PROXY:
 			return LoginManager.login(url);
 		case X509_CERTIFICATE:
 			return loginCommandlineX509cert(url);
 		}
 		throw new IllegalArgumentException("Login type not supported.");
 
 	}
 
 	public static ServiceInterface loginCommandline(Set<LoginType> types,
 			String url) throws LoginException {
 
 		if ((types == null) || (types.size() == 0)) {
 
 			throw new IllegalArgumentException("No login type specified.");
 		}
 
 		if (types.size() == 1) {
 			return loginCommandline(types.iterator().next(), url);
 		}
 
 		ImmutableList<LoginType> temp = ImmutableList.copyOf(types);
 
 		StringBuffer message = new StringBuffer(
 				"Please select your preferred login method:\n\n");
 
 		for (int i = 0; i < temp.size(); i++) {
 			message.append("[" + (i + 1) + "]\t" + temp.get(i).getPrettyName()
 					+ "\n");
 		}
 		message.append("\n[0]\tExit\n\n");
 
 		System.out.println(message.toString());
 
 		int choice = -1;
 		while ((choice < 0) || (choice >= temp.size())) {
 			String input;
 			try {
 				input = getConsoleReader().readLine("Login method: ");
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 
 			try {
 				choice = Integer.parseInt(input);
 			} catch (Exception e) {
 				continue;
 			}
 		}
 
 		if (choice == 0) {
 			System.exit(0);
 		}
 
 		return loginCommandline(temp.get(choice - 1), url);
 
 	}
 
 	public static ServiceInterface loginCommandline(String url)
 			throws LoginException {
 
 		initEnvironment();
 
 		if (LocalProxy.validGridProxyExists()) {
 			return LoginManager.login(url);
 		} else {
 			ImmutableSet<LoginType> temp = ImmutableSet.of(
 					LoginType.SHIBBOLETH, LoginType.MYPROXY,
 					LoginType.X509_CERTIFICATE);
 			return loginCommandline(temp, url);
 		}
 
 	}
 
 	public static ServiceInterface loginCommandlineMyProxy(String url) {
 
 		while (true) {
 			try {
 
 				StringBuffer prompt = new StringBuffer(
 						"Please enter your myproxy username");
 				String lastMyProxyUsername = CommonArcsProperties.getDefault()
 						.getLastMyProxyUsername();
 
 				if (StringUtils.isNotBlank(lastMyProxyUsername)) {
 					prompt.append(" [" + lastMyProxyUsername + "]: ");
 				} else {
 					prompt.append(": ");
 				}
 
 				String username = null;
 				while (StringUtils.isBlank(username)) {
 					try {
 						username = getConsoleReader().readLine(
 								prompt.toString());
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 
 					if (StringUtils.isNotBlank(lastMyProxyUsername)
 							&& StringUtils.isBlank(username)) {
 						username = lastMyProxyUsername;
 					}
 				}
 
 				CommonArcsProperties.getDefault().setLastMyProxyUsername(
 						username);
 
 				String password = null;
 				while (StringUtils.isBlank(password)) {
 					try {
 						password = getConsoleReader().readLine(
 								"Please enter your myproxy password",
 								new Character('*'));
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 				}
 
 				return LoginManager.myProxyLogin(url, username,
 						password.toCharArray());
 
 			} catch (LoginException e) {
 				System.out.println("Login failed: " + e.getLocalizedMessage());
 			}
 		}
 
 	}
 
 	public static ServiceInterface loginCommandlineShibboleth(String url) {
 
 		while (true) {
 			try {
 
 				initEnvironment();
 
 				System.out.println("Loading list of institutions...");
 
 				StringBuffer prompt = new StringBuffer(
 						"Please select your institution");
 				String lastIdp = CommonArcsProperties.getDefault()
 						.getLastShibIdp();
 
 				IdpObject idpObj = new DummyIdpObject();
 				CredentialManager cm = new DummyCredentialManager();
 
 				Shibboleth shib = new Shibboleth(idpObj, cm);
 				shib.openurl(SLCS.DEFAULT_SLCS_URL);
 
 				ImmutableList<String> idps = ImmutableList.copyOf(idpObj
 						.getIdps());
 				int defaultChoice = -1;
 
 				for (int i = 0; i < idps.size(); i++) {
 					System.out.println("[" + (i + 1) + "]\t" + idps.get(i));
 
 					if (StringUtils.isNotBlank(lastIdp)
 							&& idps.get(i).equals(lastIdp)) {
 						defaultChoice = i + 1;
 					}
 				}
 				System.out.println("\n[0]\tExit");
 
 				if (defaultChoice < 0) {
 					prompt.append(": ");
 				} else {
 					prompt.append(" [" + defaultChoice + "] ");
 				}
 
 				String idpchoice = null;
 				int choice = -1;
 				while (choice < 0) {
 					try {
 						idpchoice = getConsoleReader().readLine(
 								prompt.toString());
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 
 					if ((defaultChoice >= 0) && StringUtils.isBlank(idpchoice)) {
 						idpchoice = new Integer(defaultChoice).toString();
 					}
 
 					try {
 						choice = Integer.parseInt(idpchoice);
 					} catch (Exception e) {
 						continue;
 					}
 				}
 
 				if (choice == 0) {
 					System.exit(0);
 				}
 
 				idpchoice = idps.get(choice - 1);
 
 				CommonArcsProperties.getDefault().setLastShibIdp(idpchoice);
 
 				prompt = new StringBuffer(
 						"Please enter your institution username");
 				String lastShibUsername = CommonArcsProperties.getDefault()
 						.getLastShibUsername();
 
 				if (StringUtils.isNotBlank(lastShibUsername)) {
 					prompt.append(" [" + lastShibUsername + "]: ");
 				} else {
 					prompt.append(": ");
 				}
 
 				String username = null;
 				while (StringUtils.isBlank(username)) {
 					try {
 						username = getConsoleReader().readLine(
 								prompt.toString());
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 
 					if (StringUtils.isNotBlank(lastShibUsername)
 							&& StringUtils.isBlank(username)) {
 						username = lastShibUsername;
 					}
 				}
 
 				CommonArcsProperties.getDefault().setLastShibUsername(username);
 
 				String password = null;
 				while (StringUtils.isBlank(password)) {
 					try {
 						password = getConsoleReader().readLine(
 								"Please enter your institution password: ",
 								new Character('*'));
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 				}
 
 				return LoginManager.shiblogin(username, password.toCharArray(),
 						idpchoice, url, true);
 			} catch (LoginException e) {
 				System.out.println("Login failed: " + e.getLocalizedMessage());
 			}
 		}
 
 	}
 
 	public static ServiceInterface loginCommandlineX509cert(String url) {
 
 		while (true) {
 			try {
 
 				String password = null;
 				while (StringUtils.isBlank(password)) {
 					try {
 						password = getConsoleReader()
 								.readLine(
 										"Please enter your x509 certificate passphrase: ",
 										new Character('*'));
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 				}
 
 				return LoginManager.login(null, password.toCharArray(), null,
 						null, true);
 			} catch (LoginException e) {
 				System.out.println("Login exception: "
 						+ e.getLocalizedMessage());
 			}
 		}
 
 	}
 
 	public static ServiceInterface loginSwing(Set<LoginType> types, String url) {
 		throw new RuntimeException("Not supported yet.");
 	}
 
 	public static void main(String[] args) throws LoginException {
 
 		ServiceInterface si = LoginManager.shiblogin("markus",
 				args[0].toCharArray(), "VPAC", true);
 
 	}
 
 	public static ServiceInterface myProxyLogin(String url, String username,
 			char[] password) throws LoginException {
 		LoginParams loginParams = new LoginParams(url, username, password);
 		return login(null, null, null, null, loginParams, false);
 	}
 
 	public static void setHttpProxy(String host, int port, String username,
 			char[] password) {
 		httpProxyHost = host;
 		httpProxyPort = port;
 		httpProxyUsername = username;
 		httpProxyPassphrase = password;
 		HttpProxyManager.setHttpProxy(httpProxyHost, httpProxyPort,
 				httpProxyUsername, httpProxyPassphrase);
 	}
 
 	/**
 	 * Standard shib login to local backend.
 	 * 
 	 * Logs into a backend using an already existing local proxy.
 	 * 
 	 * @param username
 	 *            the idp username
 	 * @param password
 	 *            the idp password
 	 * @param idp
 	 *            the idp name
 	 * @param url
 	 *            the url of the serviceinterface
 	 * @param saveCredendentialsToLocalProxy
 	 *            whether to save the credentials to a local proxy afterwards
 	 * 
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 */
 	public static ServiceInterface shiblogin(String username, char[] password,
 			String idp, boolean saveCredendentialsToLocalProxy)
 			throws LoginException {
 		return login((GlobusCredential) null, password, username, idp, "Local",
 				saveCredendentialsToLocalProxy);
 	}
 
 	/**
 	 * Standard shib login.
 	 * 
 	 * Logs into a backend using an already existing local proxy.
 	 * 
 	 * @param username
 	 *            the idp username
 	 * @param password
 	 *            the idp password
 	 * @param idp
 	 *            the idp name
 	 * @param url
 	 *            the url of the serviceinterface
 	 * @param saveCredendentialsToLocalProxy
 	 *            whether to save the credentials to a local proxy afterwards
 	 * 
 	 * @return the serviceinterface
 	 * @throws LoginException
 	 *             if the login doesn't succeed
 	 */
 	public static ServiceInterface shiblogin(String username, char[] password,
 			String idp, String url, boolean saveCredendentialsToLocalProxy)
 			throws LoginException {
 		return login((GlobusCredential) null, password, username, idp, url,
 				saveCredendentialsToLocalProxy);
 	}
 
 	public static GSSCredential slcsMyProxyInit(String username,
 			char[] password, String idp, LoginParams params) throws Exception {
 
 		return SlcsLoginWrapper
 				.slcsMyProxyInit(username, password, idp, params);
 
 	}
 
 }
