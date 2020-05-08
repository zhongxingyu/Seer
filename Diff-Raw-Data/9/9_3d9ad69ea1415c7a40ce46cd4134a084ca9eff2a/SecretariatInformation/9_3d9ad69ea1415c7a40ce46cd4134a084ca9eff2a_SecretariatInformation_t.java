 package br.org.feees.sigme.secretariat.application;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Singleton;
 
 import br.com.engenhodesoftware.util.ResourceUtil;
 import br.org.feees.sigme.secretariat.domain.AllMailingAddressee;
 import br.org.feees.sigme.secretariat.domain.MailingAddresseeScope;
 import br.org.feees.sigme.secretariat.domain.MailingList;
 import br.org.feees.sigme.secretariat.persistence.MailingListDAO;
 import br.org.feees.sigme.secretariat.view.I18n;
 
 /**
  * Singleton bean that stores in memory information that is useful for the entire application, i.e., read-only
  * information shared by all users. This bean stores information for the secretariat package.
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  */
 @Singleton
 public class SecretariatInformation implements Serializable {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(SecretariatInformation.class.getCanonicalName());
 
 	// FIXME: the SMTP configuration should be part of the system installation, not a properties file in the source code!
 	// Moreover, only hostname and port were included, the sender email address is missing.
 
 	/** The qualified name of the module's properties file. */
	private static final String PROPERTIES_FILE_PATH = "/br/org/feees/sigme/secretariat/application/module.properties";
 
 	/** The module's properties. */
 	private Properties properties = new Properties();
 
 	/** The SMTP configuration. */
 	private SmtpConfig smtpConfig;
 
 	/** Output: the list of mailing list addressee scopes. */
 	private SortedSet<MailingAddresseeScope> addresseeScopes;
 
 	/** TODO: document this field. */
 	@EJB
 	private MailingListDAO mailingListDAO;
 
 	/**
 	 * TODO: document this method.
 	 */
 	@PostConstruct
 	public void init() {
 		try {
 			// Reads the module's properties.
 			InputStream is = ResourceUtil.getResourceAsStream(PROPERTIES_FILE_PATH);
 			logger.log(Level.INFO, "Loading properties for the Secretariat module from file: {0}", PROPERTIES_FILE_PATH);
 			properties.load(is);
 			is.close();
 
 			// Reads the SMTP configuration, making it avaliable to the Mailer via getSmtpConfig().
 			String hostName = properties.getProperty("secretariat.smtp.hostname");
 			String sPort = properties.getProperty("secretariat.smtp.port");
 			if ((hostName == null) || (hostName.isEmpty()))
 				throw new IllegalStateException("Secretariat's module configuration does not contain mandatory property secretariat.smtp.hostname. The module might now work properly!");
 			int port;
 			try {
 				port = ((sPort == null) || (sPort.isEmpty())) ? 25 : Integer.parseInt(sPort);
 			}
 			catch (NumberFormatException e) {
 				logger.log(Level.WARNING, "Expected integer in property secretariat.smtp.port. Found: {0}. Falling back to default port.", sPort);
 				port = 25;
 			}
 			logger.log(Level.FINE, "Read SMTP configuration. Will use server {0}:{1}", new Object[] { hostName, port });
 			smtpConfig = new SmtpConfig(hostName, port);
 		}
 		catch (IllegalStateException | IOException e) {
 			logger.log(Level.SEVERE, "Could not initialize Secretariat module from properties file {0}. The module might not work properly!", PROPERTIES_FILE_PATH);
 		}
 	}
 
 	/** Getter for smtpConfig. */
 	public SmtpConfig getSmtpConfig() {
 		return smtpConfig;
 	}
 	
 	/** Getter for addresseeScopes. */
 	public SortedSet<MailingAddresseeScope> getAddresseeScopes() {
 		// If the addressee scopes haven't yet been loaded, load them.
 		if (addresseeScopes == null) {
 			logger.log(Level.FINER, "Application-scoped set of mailing addressee scopes not yet initialized. Loading...");
 			addresseeScopes = new TreeSet<MailingAddresseeScope>();
 			addresseeScopes.addAll(Arrays.asList(MailingAddresseeScope.values()));
 			logger.log(Level.INFO, "Loaded {0} mailing list addressee scopes.", addresseeScopes.size());
 		}
 		return addresseeScopes;
 	}
 
 	/**
 	 * TODO: document this method.
 	 */
 	public void installModule() {
 		// Creates the mailing list for all registered spiritists.
 		logger.log(Level.FINE, "Creating the \"ALL\" mailing list.");
 		MailingList mailingList = new MailingList();
 		AllMailingAddressee addressee = new AllMailingAddressee();
 		mailingList.addAddressee(addressee);
 
 		// Assigns its name and description.
 		mailingList.setName(I18n.getString("MailingList.all.name"));
 		mailingList.setDescription(I18n.getString("MailingList.all.description"));
 
 		// Persists it.
 		mailingListDAO.save(mailingList);
 		logger.log(Level.INFO, "Mailing list for all registered spiritists created successfully.");
 	}
 
 	/**
 	 * TODO: document this type.
 	 * 
 	 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 	 * @version 1.0
 	 */
 	public static class SmtpConfig {
 		/** TODO: document this field. */
 		private String hostName;
 
 		/** TODO: document this field. */
 		private int port;
 
 		/** Constructor. */
 		public SmtpConfig(String hostName, int port) {
 			this.hostName = hostName;
 			this.port = port;
 		}
 
 		/** Getter for hostName. */
 		public String getHostName() {
 			return hostName;
 		}
 
 		/** Getter for port. */
 		public int getPort() {
 			return port;
 		}
 	}
 }
