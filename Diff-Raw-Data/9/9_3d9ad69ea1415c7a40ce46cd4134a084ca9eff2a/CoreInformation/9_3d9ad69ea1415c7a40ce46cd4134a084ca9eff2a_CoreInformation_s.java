 package br.org.feees.sigme.core.application;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Singleton;
 import javax.inject.Named;
 
 import br.com.engenhodesoftware.util.ResourceUtil;
 import br.com.engenhodesoftware.util.people.domain.ContactType;
 import br.com.engenhodesoftware.util.people.persistence.ContactTypeDAO;
 import br.com.engenhodesoftware.util.people.persistence.exceptions.PersistentObjectNotFoundException;
 import br.org.feees.sigme.core.domain.Institution;
 import br.org.feees.sigme.core.domain.InstitutionType;
 import br.org.feees.sigme.core.domain.Regional;
 import br.org.feees.sigme.core.domain.SigmeConfiguration;
 import br.org.feees.sigme.core.persistence.InstitutionTypeDAO;
 import br.org.feees.sigme.core.persistence.RegionalDAO;
 import br.org.feees.sigme.core.persistence.SigmeConfigurationDAO;
 
 /**
  * Singleton bean that stores in memory information that is useful for the entire application, i.e., read-only
  * information shared by all users. This bean stores information for the core package.
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  */
 @Singleton
 @Named("coreInfo")
 public class CoreInformation implements Serializable {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(CoreInformation.class.getCanonicalName());
 
 	/** The qualified name of the module's properties file. */
	private static final String PROPERTIES_FILE_PATH = "/br/com/engenhodesoftware/sigme/core/application/module.properties";
 
 	/** The default locale. */
 	public static Locale DEFAULT_LOCALE = Locale.getDefault();
 
 	/** The module's properties. */
 	private Properties properties = new Properties();
 
 	/** The DAO for SigmeConfiguration objects. */
 	@EJB
 	private SigmeConfigurationDAO sigmeConfigurationDAO;
 
 	/** The DAO for InstitutionType objects. */
 	@EJB
 	private InstitutionTypeDAO institutionTypeDAO;
 
 	/** The DAO for ContactType objects. */
 	@EJB
 	private ContactTypeDAO contactTypeDAO;
 
 	/** The DAO for Regional objects. */
 	@EJB
 	private RegionalDAO regionalDAO;
 	
 	/** The owner of the running Sigme instance. */
 	private Institution owner;
 
 	/** Indicates if the system is properly installed. */
 	private Boolean systemInstalled;
 
 	/** Indicates the decorator being used in the administration area. */
 	private String decorator = "default";
 
 	/** The list of institution types (cache of objects that don't change very often). */
 	private SortedSet<InstitutionType> institutionTypes;
 
 	/** The list of telephone types (cache of objects that don't change very often). */
 	private SortedSet<ContactType> contactTypes;
 
 	/** The list of regionals (cache of objects that don't change very often). */
 	private SortedSet<Regional> regionals;
 
 	/**
 	 * Initialization method for the core module.
 	 * 
 	 * Reads the properties file for this module and stores the information in the singleton instance of this class.
 	 */
 	@PostConstruct
 	public void init() {
 		try {
 			// Reads the module's properties.
 			InputStream is = ResourceUtil.getResourceAsStream(PROPERTIES_FILE_PATH);
 			logger.log(Level.INFO, "Loading properties for the Core module from file: {0}", PROPERTIES_FILE_PATH);
 			properties.load(is);
 
 			// Sets the default locale using information from the properties file, if available.
 			String language = properties.getProperty("core.language");
 			String country = properties.getProperty("core.country");
 			if ((language != null) && (country != null) && (!language.isEmpty()) && (!country.isEmpty())) {
 				logger.log(Level.FINE, "Setting default locale to: {0}-{1}", new Object[] { language, country });
 				DEFAULT_LOCALE = new Locale(language, country);
 			}
 		}
 		catch (IOException e) {
 			logger.log(Level.SEVERE, "Could not initialize Core module from properties file {0}. The module might not work properly!", PROPERTIES_FILE_PATH);
 		}
 	}
 
 	/** Getter for owner. */
 	public Institution getOwner() throws PersistentObjectNotFoundException {
 		// If not done before, retrieves the owner.
 		if (owner == null) loadConfiguration();
 		return owner;
 	}
 	
 	/** (Re)loads the configuration information. */
 	public void loadConfiguration() throws PersistentObjectNotFoundException {
 		SigmeConfiguration cfg = sigmeConfigurationDAO.retrieveCurrentConfiguration();
 		owner = cfg.getOwner();
 	}
 
 	/** Alias for isSystemInstalled(). */
 	public Boolean getSystemInstalled() {
 		return isSystemInstalled();
 	}
 
 	/** Getter for systemInstalled. */
 	public Boolean isSystemInstalled() {
 		// If not done before, checks if the system has been installed.
 		if (systemInstalled == null) {
 			logger.log(Level.FINER, "Checking if the system has been properly installed...");
 
 			// The system is propertly installed if the basic information on cities, states, regions, institution types, etc.
 			// have already been included in the database. As a convention, we check for institution types.
 			long count = institutionTypeDAO.retrieveCount();
 			systemInstalled = (count > 0);
 			logger.log(Level.INFO, "System properly installed: {0}", systemInstalled);
 		}
 		return systemInstalled;
 	}
 
 	/** Setter for systemInstalled. */
 	public void setSystemInstalled(Boolean systemInstalled) {
 		this.systemInstalled = systemInstalled;
 		logger.log(Level.FINE, "System installed flag has been set as: {0}", systemInstalled);
 	}
 
 	/** Getter for decorator. */
 	public String getDecorator() {
 		return decorator;
 	}
 
 	/** Getter for the default locale. */
 	public Locale getDefaultLocale() {
 		return DEFAULT_LOCALE;
 	}
 
 	/** Getter for institutionTypes. */
 	public SortedSet<InstitutionType> getInstitutionTypes() {
 		// If the institution types haven't yet been loaded, load them.
 		if (institutionTypes == null) {
 			logger.log(Level.FINER, "Application-scoped set of institution types not yet initialized. Loading...");
 			institutionTypes = new TreeSet<InstitutionType>();
 			institutionTypes.addAll(institutionTypeDAO.retrieveAll());
 			logger.log(Level.INFO, "Loaded {0} institution types.", institutionTypes.size());
 		}
 		return institutionTypes;
 	}
 
 	/** Getter for contactTypes. */
 	public SortedSet<ContactType> getContactTypes() {
 		// If the contact types haven't yet been loaded, load them.
 		if (contactTypes == null) {
 			logger.log(Level.FINER, "Application-scoped set of contact types not yet initialized. Loading...");
 			contactTypes = new TreeSet<ContactType>();
 			contactTypes.addAll(contactTypeDAO.retrieveAll());
 			logger.log(Level.INFO, "Loaded {0} contact types.", contactTypes.size());
 		}
 		return contactTypes;
 	}
 
 	/** Getter for regionals. */
 	public SortedSet<Regional> getRegionals() {
 		// If the regionals haven't yet been loaded, load them.
 		if (regionals == null) {
 			logger.log(Level.FINER, "Application-scoped set of regionals not yet initialized. Loading...");
 			regionals = new TreeSet<Regional>();
 			regionals.addAll(regionalDAO.retrieveAll());
 			logger.log(Level.INFO, "Loaded {0} regionals.", regionals.size());
 		}
 		return regionals;
 	}
 }
