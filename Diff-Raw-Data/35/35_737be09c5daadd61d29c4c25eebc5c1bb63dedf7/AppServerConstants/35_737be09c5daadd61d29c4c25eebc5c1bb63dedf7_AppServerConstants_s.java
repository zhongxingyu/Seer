 package com.scholastic.sbam.server.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.scholastic.sbam.server.database.codegen.SysConfig;
 import com.scholastic.sbam.server.database.objects.DbSysConfig;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.server.fastSearch.InstitutionCache.InstitutionCacheConfig;
 
 public class AppServerConstants {
 	public static final String REPLACEMENT_START = "[$";
 	public static final String REPLACEMENT_END   = "$]";
 	public static final String CHUNK_START = "[[";
 	public static final String CHUNK_END   = "]]";
 	public static final String CHUNK_PART  = "::";
     
 	public static final String VERSION					= "1.0b";
 	
 	private static		String	executionMode				=	"?";
 	private static		int		customerLoadLimit			=	-1;
 	private static		String	filesRoot					=	"/";
 	private static		String	emailServer					=	"";
 	private static		int		emailPort					=	25;
 	private static		String	emailUser					=	"";
 	private static		String	emailPassword				=	"";
 	private static		String	emailAddress				=	"";
 	private static		String	emailCc						=	"";
 	private static		String	emailBcc					=	"";
 	private static		String	emailFilesRoot				=	"/";
 	private static		String	siteUrl						=	"sbam.scholastic.com";
 	private static		String	techContactName				=	"";
 	private static		String	techContactEmail			=	"";
 	
 	private static		InstitutionCacheConfig instCacheConfig = new InstitutionCacheConfig();
 	
 	private static final String PROPERTIES					=	"sbam.properties";
 	private static final String PARM_EXECUTION_MODE			=	"EXECUTION_MODE";
 	private static final String PARM_CUSTOMER_LIMIT			=	"CUSTOMER_LIMIT";
 	private static final String PARM_EMAIL_SERVER			=	"EMAIL_SERVER";
 	private static final String PARM_EMAIL_PORT				=	"EMAIL_PORT";
 	private static final String PARM_EMAIL_USER				=	"EMAIL_USER";
 	private static final String PARM_EMAIL_PASSWORD			=	"EMAIL_PASSWORD";
 	private static final String PARM_EMAIL_ADDRESS			=	"EMAIL_ADDRESS";
 	private static final String PARM_EMAIL_CC				=	"EMAIL_CC";
 	private static final String PARM_EMAIL_BCC				=	"EMAIL_BCC";
 	private static final String PARM_SITE_URL				=	"SITE_URL";
 	private static final String PARM_TECH_CONTACT_NAME		=	"TECH_CONTACT_NAME";
 	private static final String PARM_TECH_CONTACT_EMAIL		=	"TECH_CONTACT_EMAIL";
 	
     private static final Log log = LogFactory.getLog(AppServerConstants.class);
 	
 	
 	/**
 	 * Initialize the constants first from the database, second from the application properties file sbam.properties,
 	 * and then from a configuration properties file in ../conf/sbam.properties, if available.
 	 * @throws Exception
 	 */
 	
 	public static void init(String path) throws Exception {
 		if (!initFromDb()) {
 			init(path, null);
 		}
 	}
 	
 	public static boolean initFromDb() {
 		boolean loaded = false;
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		try {
 			SysConfig sysConfig = DbSysConfig.getActive();
 			if (sysConfig != null) {
 				executionMode		= sysConfig.getExecutionMode();
 				customerLoadLimit	= sysConfig.getCustomerLimit();
 				emailServer			= sysConfig.getEmailServer();
 				emailPort			= sysConfig.getEmailPort();
 				emailUser			= sysConfig.getEmailUser();
 				emailPassword		= sysConfig.getEmailPassword();
 				emailAddress		= sysConfig.getEmailAddress();
 				emailCc				= sysConfig.getEmailCc();
 				emailBcc			= sysConfig.getEmailBcc();
 				siteUrl				= sysConfig.getSiteUrl();
 				techContactName		= sysConfig.getTechContactName();
 				techContactEmail	= sysConfig.getTechContactEmail();
 				
 				if (sysConfig.getInstConfigMaxList() > 0) {
 					instCacheConfig.setUseInnerStrings(sysConfig.getInstConfigInner() == 'y');
 					instCacheConfig.setUseStringPairs(sysConfig.getInstConfigPairs() == 'y');
 					instCacheConfig.setMinStringLength(sysConfig.getInstConfigMinStr());
 					instCacheConfig.setMinInnerStringLength(sysConfig.getInstConfigMinInner());
 					instCacheConfig.setMaxStringPairLength(sysConfig.getInstConfigMaxPair());
 					instCacheConfig.setMaxListLength(sysConfig.getInstConfigMaxList());
 					instCacheConfig.setMaxWordListLength(sysConfig.getInstConfigMaxWords());
 				}
 				
 				loaded = true;
 				System.out.println("System configuration properties loaded from database configuration entry " + sysConfig.getId());
 			}
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		
 		HibernateUtil.endTransaction();
 		HibernateUtil.closeSession();
 		
 		return loaded;
 	}
 
 	/**
 	 * Initialize the constants first from the application properties file sbam.properties,
 	 * and then from a configuration properties file in ../conf/sbam.properties, if available.
 	 *
 	 * This method, as opposed to init(), will echo the values to standard output as they are loaded.
 	 * @param prompt
 	 *  A non-null prompt to precede an echo of property values as they are loaded.
 	 * @throws Exception
 	 */
 	private static void init(String path, String prompt) throws Exception {
 		filesRoot = path;
 		emailFilesRoot = path + "emails/"; 
 		try {
 			loadFromFile(path, PROPERTIES, prompt); 
 		} catch (Exception e) {	}
 		
 		loadFromConf(PROPERTIES, prompt);
 	}
 
 	private static void loadFromConf(String filename, String prompt) throws Exception {
 		try {
 			String confLoc = "conf/" + filename;
 			File confProps = new File(confLoc);
 
 
 			if (!confProps.exists()) {
 				if (prompt != null)
 					System.out.print(prompt);
 				logWarn("WARNING: " + confProps.getAbsolutePath() + " file not found.");
 			}
 			
 			if (!confProps.exists()) {
 				confLoc = System.getProperty("user.home") + "/conf/" + filename;
 				confProps = new File(confLoc);
 
 				if (!confProps.exists()) {
 					if (prompt != null)
 						System.out.print(prompt);
 					logWarn("WARNING: " + confProps.getAbsolutePath() + " file not found.");
 				}
 			}
 			
 			if (!confProps.exists() && System.getProperty("sbam.properties") != null) {
 				confLoc = System.getProperty("sbam.properties") + filename;
 				confProps = new File(confLoc);
 
 				if (!confProps.exists()) {
 					if (prompt != null)
 						System.out.print(prompt);
 					logWarn("WARNING: " + confProps.getAbsolutePath() + " file not found.");
 				}
 			}
 			
 			if (confProps.exists())
 				loadFromFile("", confProps.getAbsolutePath(), prompt);
 		} catch (FileNotFoundException exc) {
 			if (prompt != null)
 				System.out.print(prompt);
 			logWarn("WARNING: error reading conf/" + PROPERTIES + " (" + exc.getMessage() + ").");
 		}
 	}
 
 	/**
 	 * Set the admin constants from a properties file.
 	 *
 	 * @param filename
 	 * 	The properties filename.
 	 * @throws Exception
 	 */
 	private static void loadFromFile(String path, String filename, String prompt) throws Exception {
 //			 create and load default properties
 //			File file = new File(path, filename);
 //			System.out.println(file.getAbsolutePath());
 //			System.out.println(file.getName());
 		Properties props = new Properties();
 		FileInputStream in = new FileInputStream(path + "/" + filename);
 		props.load(in);
 
 //			//	Where to place and find uploaded MARC files
 //			if (props.containsKey(PARM_MARC_FILES_ROOT)) {
 //				MARC_FILES_ROOT = (String) props.getProperty(PARM_MARC_FILES_ROOT);
 //				reportValue(PARM_MARC_FILES_ROOT, MARC_FILES_ROOT, prompt);
 //			}
 //
 //			//	Where to place and place and find generated report files
 //			if (props.containsKey(PARM_REPORT_FILES_ROOT)) {
 //				reportFilesRoot = (String) props.getProperty(PARM_REPORT_FILES_ROOT);
 //				reportValue(PARM_REPORT_FILES_ROOT, reportFilesRoot, prompt);
 //			}
 //
 //			//	Where to place and place and find generated report files
 //			if (props.containsKey(PARM_REPORT_LINK_ROOT)) {
 //				reportLinkRoot = (String) props.getProperty(PARM_REPORT_LINK_ROOT);
 //				if (reportLinkRoot == null || reportLinkRoot.trim().length() == 0)
 //					reportLinkRoot = "reportDownload?" + PARM_DOWNLOAD_FILE + "=";
 //				reportValue(PARM_REPORT_FILES_ROOT, reportLinkRoot, prompt);
 //			}
 
 		
 		//	Execution Mode
 
 		if (props.containsKey(PARM_EXECUTION_MODE)) {
 			executionMode = (String) props.getProperty(PARM_EXECUTION_MODE);
 			reportValue(PARM_EXECUTION_MODE, executionMode, prompt);
 		}
 
 		if (props.containsKey(PARM_CUSTOMER_LIMIT)) {
 			customerLoadLimit = Integer.parseInt((String) props.getProperty(PARM_CUSTOMER_LIMIT));
 			reportValue(PARM_CUSTOMER_LIMIT, customerLoadLimit + "", prompt);
 		}
 		
 		//	Email
 		if (props.containsKey(PARM_EMAIL_SERVER)) {
 			emailServer = (String) props.getProperty(PARM_EMAIL_SERVER);
 			reportValue(PARM_EMAIL_SERVER, emailServer, prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_PORT)) {
 			String emailPortStr = (String) props.getProperty(PARM_EMAIL_PORT);
 			if (emailPortStr != null && emailPortStr.length() > 0) {
 				try {
 					emailPort = Integer.parseInt(emailPortStr);
 				} catch (NumberFormatException e) {
 					logWarn("Invalid port " + emailPortStr + ", " + emailPort + " assumed.");
 				}
 			}
 			reportValue(PARM_EMAIL_PORT, emailPort + "", prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_USER)) {
 			emailUser = (String) props.getProperty(PARM_EMAIL_USER);
 			reportValue(PARM_EMAIL_USER, emailUser, prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_PASSWORD)) {
 			emailPassword = (String) props.getProperty(PARM_EMAIL_PASSWORD);
 			reportValue(PARM_EMAIL_PASSWORD, emailPassword, prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_ADDRESS)) {
 			emailAddress = (String) props.getProperty(PARM_EMAIL_ADDRESS);
 			reportValue(PARM_EMAIL_ADDRESS, emailAddress, prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_CC)) {
 			emailCc = (String) props.getProperty(PARM_EMAIL_CC);
 			reportValue(PARM_EMAIL_CC, emailCc, prompt);
 		}
 		if (props.containsKey(PARM_EMAIL_BCC)) {
 			emailBcc = (String) props.getProperty(PARM_EMAIL_BCC);
 			reportValue(PARM_EMAIL_BCC, emailBcc, prompt);
 		}
 		if (props.containsKey(PARM_SITE_URL)) {
 			siteUrl = (String) props.getProperty(PARM_SITE_URL);
 			if (siteUrl == null || siteUrl.length() == 0)
 				siteUrl = "sbam.scholastic.com";
 			reportValue(PARM_SITE_URL, siteUrl, prompt);
 		}
 		if (props.containsKey(PARM_TECH_CONTACT_NAME)) {
 			techContactName = (String) props.getProperty(PARM_TECH_CONTACT_NAME);
 			if (techContactName == null || techContactName.trim().length() == 0)
 				techContactName = "Tech Contact";
 			reportValue(PARM_TECH_CONTACT_NAME, techContactName, prompt);
 		} else
 			techContactName = "Tech Contact";
 		if (props.containsKey(PARM_TECH_CONTACT_EMAIL)) {
 			techContactEmail = (String) props.getProperty(PARM_TECH_CONTACT_EMAIL);
 			if (techContactEmail == null || techContactEmail.trim().length() == 0)
 				techContactEmail = emailAddress;
 			reportValue(PARM_TECH_CONTACT_EMAIL, techContactEmail, prompt);
 		} else
 			techContactEmail = emailAddress;
 
 		in.close();
 	}
 
 	/**
 	 * Echo all values to standard output.
 	 *
 	 * @param prefix
 	 * 	A non-null prefix (but it may be an empty string) that will precede all values reported.
 	 */
 	public static void echoValues(String prefix) {
 //			reportValue(PARM_MARC_FILES_ROOT, MARC_FILES_ROOT, prefix);
 //			reportValue(REPORT_EMAIL, reportEmail, prefix);
 //			reportValue(EMAIL_SERVER, emailServer, prefix);
 //			reportValue(EMAIL_ADDRESS, emailAddress, prefix);
 	}
 
 	/**
 	 * Report a loaded property value, preceded by a prefix string.
 	 *
 	 * If no prefix string is given, do not report the loaded value.
 	 *
 	 * @param name
 	 * 	The name of the property loaded.
 	 * @param value
 	 * 	The value of the proptery loaded.
 	 * @param prefix
 	 * 	A "prefix" string that precedes the reported values.
 	 */
 	private static void reportValue(String name, String value, String prefix) {
 		if (prefix == null)
 			return;
 		logInfo(prefix + name + " = " + value);
 	}
 
 	/**
 	 * Set admin constant values to custom development values, given a properties file name.
 	 *
 	 *@param filename
 	 *	The name of the properties file to be loaded.
 	 *@param prefix
 	 *	A "prefix" string that will precede values reported on the standard output.
 	 *	If this prefix is null, values will not be reported.
 	 *@throws Exception
 	 */
 	public static void setCustomValues(String path, String filename, String prefix) throws Exception {
 		loadFromFile(path, filename, prefix);
 	}
 	
 	private static void logWarn(String message) {
 		log.warn(message);
 	}
 	
 	private static void logInfo(String message) {
 		log.info(message);
 	}
     
     public static boolean isEmpty(String str) {
     	return str == null || str.length() == 0 || str.trim().length() == 0;
     }
 
 	public static String getEmailServer() {
 		return emailServer;
 	}
 
 	public static int getEmailPort() {
 		return emailPort;
 	}
 
 	public static String getEmailUser() {
 		return emailUser;
 	}
 
 	public static String getEmailPassword() {
 		return emailPassword;
 	}
 
 	public static String getEmailAddress() {
 		return emailAddress;
 	}
 
 	public static String getEmailCc() {
 		return emailCc;
 	}
 
 	public static String getEmailBcc() {
 		return emailBcc;
 	}
 
 	public static String getFilesRoot() {
 		return filesRoot;
 	}
 
 	public static String getEmailFilesRoot() {
 		return emailFilesRoot;
 	}
 
 //		public static String getReportFilesRoot() {
 //			return reportFilesRoot;
 //		}
 //
 //		public static String getReportLinkRoot() {
 //			return reportLinkRoot;
 //		}
 
 	public static String getSiteUrl() {
 		return siteUrl;
 	}
 
 	public static String getExecutionMode() {
 		return executionMode;
 	}
 
 	public static int getCustomerLoadLimit() {
 		return customerLoadLimit;
 	}
 
 	public static String getTechContactName() {
 		return techContactName;
 	}
 
 	public static String getTechContactEmail() {
 		return techContactEmail;
 	}
 
 	public static InstitutionCacheConfig getInstCacheConfig() {
 		return instCacheConfig;
 	}
 	
 	
 }
