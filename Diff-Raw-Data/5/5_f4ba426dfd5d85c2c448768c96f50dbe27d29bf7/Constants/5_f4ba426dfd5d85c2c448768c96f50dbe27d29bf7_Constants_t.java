 package gov.nih.nci.evs.browser.common;
 
 /**
  * Application constants class
  * @author garciawa2
  */
 public class Constants {
 
     // Application version
     static public final int MAJOR_VER = 1;
     static public final int MINOR_VER = 0;
     static public final String CONFIG_FILE = "NCItBrowserProperties.xml";
     static public final String CODING_SCHEME_NAME = "NCI Thesaurus";
 
 	// Application constants
 	static public final String NA = "N/A";
 	static public final String TRUE = "true";
 	static public final String FALSE = "false";
 	static public final String EMPTY = "";
	
	// Application error constants
	static public final String INIT_PARAM_ERROR_PAGE = "errorPage";
	static public final String ERROR_MESSAGE = "systemMessage";
	static public final String ERROR_UNEXPECTED = "Warning: An unexpected processing error has occurred.";	
 
 	/**
 	 * Constructor
 	 */
 	private Constants() {
 		// Prevent class from being explicitly instantiated
 	}
 
 	public static String getCodingSchemeName() {
 		return CODING_SCHEME_NAME.replaceAll(" ", "%20");
 	}
 
 } // Class Constants
