 package nz.org.nesi.goji.control;
 
 import grisu.jcommons.model.info.GFile;
 import grith.jgrith.plainProxy.LocalProxy;
 
import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 import nz.org.nesi.goji.Goji;
 import nz.org.nesi.goji.exceptions.CommandException;
 import nz.org.nesi.goji.exceptions.CredentialException;
 import nz.org.nesi.goji.model.Credential;
 import nz.org.nesi.goji.model.Endpoint;
 import nz.org.nesi.goji.model.commands.AbstractCommand;
 import nz.org.nesi.goji.model.commands.Activate;
 import nz.org.nesi.goji.model.commands.EndpointList;
 import nz.org.nesi.goji.model.commands.LsCommand;
 import nz.org.nesi.goji.model.commands.PARAM;
 
 import org.apache.commons.lang.StringUtils;
 import org.globusonline.transfer.BaseTransferAPIClient;
 import org.globusonline.transfer.JSONTransferAPIClient;
 import org.python.google.common.collect.Sets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 public class GlobusOnlineSession {
 
 	private final Logger myLogger = LoggerFactory
 			.getLogger(GlobusOnlineSession.class);
 
 	private final BaseTransferAPIClient client;
 	private final String go_username;
 	private final String go_url;
 
 	private Set<Endpoint> endpoints;
 
 	private Credential credential = null;
 
 	public GlobusOnlineSession(String go_username) throws CredentialException {
 		this(go_username, LocalProxy.PROXY_FILE, null);
 	}
 
 	/**
 	 * Creates a GlobusUserSession object.
 	 * 
 	 * Internally, this creates an instance of {@link JSONTransferAPIClient} and
 	 * also ensures that the provided credential is uploaded to MyProxy.
 	 * 
 	 * @param go_username
 	 *            the GlobusOnline username
 	 * @param cred
 	 *            the credential to use for this GO session
 	 * @param go_url
 	 *            the GO REST API url
 	 * @throws CredentialException
 	 *             if MyProxy delegation of credential fails
 	 */
 	public GlobusOnlineSession(String go_username, Credential cred,
 			String go_url) throws CredentialException {
 		this.go_username = go_username;
 		if (StringUtils.isBlank(go_url)) {
 			this.go_url = Goji.DEFAULT_BASE_URL;
 		} else {
 			this.go_url = go_url;
 		}
 
 		this.credential = cred;
 		try {
 			client = new JSONTransferAPIClient(go_username,
					System.getProperty("user.home") + File.separator
							+ ".globus" + File.separator + "certificates"
							+ File.separator + "gd_bundle.crt",
 					cred.getLocalPath(), cred.getLocalPath(),
 					Goji.DEFAULT_BASE_URL);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		cred.uploadMyProxy();
 
 	}
 
 	public GlobusOnlineSession(String go_username, String  pathToCredential, String go_url) throws CredentialException {
 		this(go_username, new Credential(pathToCredential), go_url);
 	}
 
 	/**
 	 * Activates all of the users endpoints, using the session credential.
 	 * 
 	 * If some endpoints need a different credential, they need to be activated
 	 * manually.
 	 * 
 	 * @throws CommandException
 	 *             if not all endpoints could be activated.
 	 */
 	public void activateAllUserEndpoints() throws CommandException {
 
 		for (Endpoint e : getAllUserEndpoints()) {
 
 			myLogger.debug("Activating endpoint: " + e.getName());
 			// we can use the session credential here, might not be what needs
 			// to be done tough...
 			activateEndpoint(e.getName(), getCredential());
 		}
 	}
 
 	/**
 	 * Activates the endpoint with the provided credential
 	 * 
 	 * @param ep
 	 *            the endpoint name
 	 * @param cred
 	 *            the credential
 	 * @throws CommandException
 	 *             if the endpoint can't be activated for some reason
 	 */
 	public void activateEndpoint(String ep, Credential cred)
 			throws CommandException {
 		Activate a = newCommand(Activate.class);
 		a.setEndpoint(ep);
 		a.setCredential(cred);
 		a.execute();
 		// endpoint list needs to be refreshed now
 		endpoints = null;
 	}
 
 	public AbstractCommand execute(Class commandClass,
 			Map<PARAM, String> config)
 					throws CommandException {
 		AbstractCommand c = newCommand(commandClass, config);
 		c.execute();
 		return c;
 	}
 
 	public Set<Endpoint> getAllEndpoints() throws CommandException {
 		if ( endpoints == null ) {
 			EndpointList el = newCommand(EndpointList.class);
 			el.execute();
 			endpoints = Sets.newTreeSet(el.getEndpoints().values());
 		}
 		return endpoints;
 	}
 
 	public Set<Endpoint> getAllUserEndpoints() throws CommandException {
 
 		Set<Endpoint> result = Sets.newTreeSet();
 
 		for (Endpoint e : getAllEndpoints()) {
 
 			if (this.go_username.equals(e.getUsername())) {
 				result.add(e);
 			}
 		}
 		return result;
 	}
 
 	public Credential getCredential() {
 		return this.credential;
 	}
 
 	public String getGlobusOnlineUsername() {
 		return this.go_username;
 	}
 
 	public SortedSet<GFile> list(String ep, String path)
 			throws CommandException {
 
 		LsCommand ls = newCommand(LsCommand.class);
 		ls.setEndpoint(ep);
 		ls.setPath(path);
 
 		ls.execute();
 
 		return ls.getFiles();
 
 	}
 
 	public <T extends AbstractCommand> T newCommand(Class<T> commandClass) {
 
 		try {
 			Constructor c = commandClass
 					.getConstructor(BaseTransferAPIClient.class);
 
 			AbstractCommand com = (AbstractCommand) c.newInstance(this.client);
 
 			return commandClass.cast(com);
 
 		} catch (Exception e) {
 			myLogger.error(
 					"Can't create command " + commandClass.getSimpleName(), e);
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public <T extends AbstractCommand> T newCommand(Class<T> commandClass,
 			Map<PARAM, String> config) throws CommandException {
 
 		AbstractCommand c = newCommand(commandClass);
 		c.init(config);
 
 		return commandClass.cast(c);
 
 	}
 
 }
