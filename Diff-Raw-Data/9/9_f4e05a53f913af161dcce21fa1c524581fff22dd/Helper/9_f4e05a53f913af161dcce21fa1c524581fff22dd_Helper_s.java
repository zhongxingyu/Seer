 /**
  * @author Tamino Hartmann
  * @author Laura Irlinger
  */
 package servlet;
 
 import java.io.FileNotFoundException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.servlet.http.HttpSession;
 
 import config.Configurator;
 import config.IllegalTypeException;
 import config.UnknownOptionException;
 
 import logger.Log;
 import user.User;
 import userManagement.LoggedInUsers;
 
 /**
  * Hilfsklasse fuer statische Variablen und Methoden der Servlets.
  */
 public final class Helper {
 
 	/**
 	 * Leerer, privater Konstruktor. Es sollte von dieser Klasse keine Instanzen
 	 * geben.
 	 */
 	private Helper() {
 
 	}
 
 	// Notiz fuer alle Pfadnamen: ist ein Link ein direkter Link (zeigt also
 	// direkt auf eine Datei und nich auf einen Servlet), so beginnt dieser mit
 	// "D_".
 
 	/**
 	 * Systemunabhaengiger Pfad zum Projektordner.
 	 */
 	public static final String PROJECT_PATH = "/hiwi/";
 	/**
 	 * Pfad zum Index direkt.
 	 */
 	public static final String D_INDEX = "/hiwi/public/index.jsp";
 	/**
 	 * Pfad zu Datenschutzerklaerung.
 	 */
 	public static final String D_PUBLIC_DATAAGREEMENT = "/hiwi/public/dataagreement.jsp";
 	/**
 	 * Pfad zu /hiwi/public/help.jsp.
 	 */
 	public static final String D_PUBLIC_HELP = "/hiwi/public/help.jsp";
 	/**
 	 * Pfad zu /hiwi/public/register.jsp.
 	 */
 	public static final String D_PUBLIC_REGISTER = "/hiwi/public/register.jsp";
 	/**
 	 * Pfad zu /admin/accountsmanagement.jsp.
 	 */
 	public static final String D_ADMIN_ACCOUNTSMANAGEMENT = "/hiwi/admin/accountsmanagement.jsp";
 	/**
 	 * Pfad zu /admin/documentsmanagement.jsp.
 	 */
 	public static final String D_ADMIN_DOCUMENTSMANAGEMENT = "/hiwi/admin/documentsmanagement.jsp";
 	/**
 	 * Pfad zu /admin/editaccount.jsp.
 	 */
 	public static final String D_ADMIN_EDITACCOUNT = "/hiwi/admin/editaccount.jsp";
 	/**
 	 * Pfad zu /admin/userindex.jsp.
 	 */
 	public static final String D_ADMIN_USERINDEX = "/hiwi/admin/userindex.jsp";
 	/**
 	 * Pfad zu /hiwi/admin/help.jsp.
 	 */
 	public static final String D_ADMIN_HELP = "/hiwi/admin/help.jsp";
 	/**
 	 * Pfad zu /hiwi/admin/institutesmanagement.jsp.
 	 */
 	public static final String D_ADMIN_INSTITUTESMANAGMENT = "/hiwi/admin/institutesmanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/admin/defoffervalues.jsp.
 	 */
 	public static final String D_ADMIN_DEFOFFERVALUES = "/hiwi/admin/defoffervalues.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/userindex.jsp.
 	 */
 	public static final String D_CLERK_USERINDEX = "/hiwi/clerk/userindex.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/accountmanagement.jsp.
 	 */
 	public static final String D_CLERK_ACCOUNTMANAGEMENT = "/hiwi/clerk/accountmanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/offermanagement.jsp.
 	 */
 	public static final String D_CLERK_OFFERMANAGEMENT = "/hiwi/clerk/offermanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/edditapplication.jsp.
 	 */
 	public static final String D_CLERK_EDITAPPLICATION = "/hiwi/clerk/editapplication.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/applicationmanagement.jsp.
 	 */
 	public static final String D_CLERK_APPLICATIONMANAGEMENT = "/hiwi/clerk/applicationmanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/clerk/help.jsp.
 	 */
 	public static final String D_CLERK_HELP = "/hiwi/clerk/help.jsp";
 	/**
 	 * Pfad zu /hiwi/applicant/userindex.jsp.
 	 */
 	public static final String D_APPLICANT_USERINDEX = "/hiwi/applicant/userindex.jsp";
 	/**
 	 * Pfad zu /hiwi/applicant/help.jsp.
 	 */
 	public static final String D_APPLICANT_HELP = "/hiwi/applicant/help.jsp";
 	/**
 	 * Pfad zu /hiwi/applicant/status.jsp.
 	 */
 	public static final String D_APPLICANT_STATUS = "/hiwi/applicant/status.jsp";
 	/**
 	 * Pfad zu /hiwi/applicant/accountmanagement.jsp.
 	 */
 	public static final String D_APPLICANT_ACCOUNTMANAGEMENT = "/hiwi/applicant/accountmanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/accountmanagement.jsp.
 	 */
 	public static final String D_PROVIDER_ACCOUNTMANAGEMENT = "/hiwi/provider/accountmanagement.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/applicantlist.jsp.
 	 */
 	public static final String D_PROVIDER_APPLICANTLIST = "/hiwi/provider/applicantlist.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/createoffer.jsp.
 	 */
 	public static final String D_PROVIDER_CREATEOFFER = "/hiwi/provider/createoffer.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/editoffer.jsp.
 	 */
 	public static final String D_PROVIDER_EDITOFFER = "/hiwi/provider/editoffer.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/userindex.jsp.
 	 */
 	public static final String D_PROVIDER_USERINDEX = "/hiwi/provider/userindex.jsp";
 	/**
 	 * Pfad zu /hiwi/provider/help.jsp.
 	 */
 	public static final String D_PROVIDER_HELP = "/hiwi/provider/help.jsp";
 
 	/**
 	 * All servlets within the servlet package should use this instance of Log.
 	 */
 	public static final Log log = Log.getInstance();
 
 	/**
 	 * Standard email-header.
 	 */
 	public static final String EMAILHEADER = "[HIWI-BÖRSE]";
 
 	/**
 	 * Diese Hilfsmethode gibt an, ob eine Session eine gueltige User session
 	 * ist.
 	 * 
 	 * @param session
 	 *            Die session zum ueberpruefen.
 	 * @param c
 	 *            user-class
 	 * @return Das User Object wenn korrekt, sonst null.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <U> U checkAuthenticity(HttpSession session, Class<U> c) {
 		User user = LoggedInUsers.getUserBySession(session);
 		if (user == null || !(user.getClass() == c)) {
 			log.write("Helper", "User not authentic.");
 			return null;
 		}
 		return (U) user;
 	}
 
 	/**
 	 * Diese Methode erstellt JSON Objekte. Dazu werden die Bezeichner und die
 	 * Objekte eingelesen und zu einem JSON Objekt zusammengebaut.
 	 * 
 	 * @param varNames
 	 *            Die Bezeichner der Variabeln.
 	 * @param variables
 	 *            Die Variablen.
 	 * @return Das zusammengebaute JSON Objekt.
 	 */
 	public static String jsonAtor(String[] varNames, Object[] variables) {
 		if (varNames.length != variables.length || varNames.length <= 0)
 			return null;
 		String json = "{";
 		for (int i = 0; i < varNames.length; i++) {
 			if (i != 0)
 				json += ",";
			if (variables[i] instanceof String)
				json += "\"" + varNames[i] + "\":\"" + variables[i] + "\"";
			else if (variables[i] instanceof Date)
 				json += "\""
 						+ varNames[i]
 						+ "\":\""
 						+ new SimpleDateFormat("dd.MM.yyyy")
 								.format((Date) variables[i]) + "\"";
 			else
 				json += "\"" + varNames[i] + "\":" + variables[i];
 		}
 		json += "}";
 		return json;
 	}
 
 	/**
 	 * Hilfsmethode um Serverseitig einen String nach String via Base64 und MD5
 	 * zu hashen. ACHTUNG: hier wird javascript Server-seitig aufgerufen! Dies
 	 * ist noetig da die hashing Methode sowohl Client- als auch Server-seitig
 	 * identisch sein muss!
 	 * 
 	 * @param text
 	 *            Eingabetext.
 	 * @return Gehashter Text.
 	 */
 	public static String b64_md5(String text) {
 		// create a script engine manager
 		ScriptEngineManager factory = new ScriptEngineManager();
 		// create a JavaScript engine
 		ScriptEngine engine = factory.getEngineByName("JavaScript");
 		// evaluate JavaScript code from String
 		try {
 			// Load md5.js:
 			engine.eval(new java.io.FileReader(Configurator.getInstance()
 					.getPath("md5_javascript")));
 			// get string:
 			return (String) engine.eval("b64_md5('" + text + "');");
 		} catch (ScriptException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IllegalTypeException e) {
 			e.printStackTrace();
 		} catch (UnknownOptionException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Globale Funktion zum Ueberpruefen eines Strings auf Gueltigkeit.
 	 * 
 	 * @param string
 	 *            Der zu pruefende String.
 	 * @return <code>True</code> wenn gueltig, sonst <code>False</code>.
 	 */
 	public static Boolean validate(String string) {
 		if (string == null || string.trim().isEmpty())
 			return false;
 		// System.out.println("1");
 		if (!string
 				.matches("^((\\s)*[a-zA-Z0-9_\\+\\/\\*;\\^\\\\#$§%=˚´€¥\\<\\>\\-.,@\\(\\)\\[\\]ÜÄÖüöä!\\?]+(\\s)*)*$")) {
 			// System.out.println("2");
 			return false;
 		}
 		return true;
 	}
 }
