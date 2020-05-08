 /**
  * @author Tamino Hartmann
  * @author Laura Irlinger
  */
 package servlet;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import logger.Log;
 import user.Admin;
 import userManagement.LoggedInUsers;
 
 import com.google.gson.Gson;
 
 import database.account.Account;
 import database.account.AccountController;
 import database.document.Document;
 import database.document.DocumentController;
 import database.institute.Institute;
 import database.institute.InstituteController;
 
 import static servlet.Helper.validate;
 
 /**
  * Das <code>Admin</code> Servlet behandelt alle Aktionen von angemeldeten
  * Administratoren.
  */
 
 @WebServlet("/Admin/*")
 public class AdminServlet extends HttpServlet {
 	/**
 	 * Standard serialVersionUID.
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Variable zum speicher der Log Instanz.
 	 */
 	private Log log;
 	/**
 	 * Variable zum speicher der Instanz des AccountController.
 	 */
 	private AccountController accountController;
 	/**
 	 * Variable zum speicher der Instanz des DocumentController.
 	 */
 	private DocumentController docController;
 	/**
 	 * Variable zum speicher der Instanz des InstituteController.
 	 */
 	private InstituteController instController;
 	/**
 	 * Variable zum speichern der GSON Instanz.
 	 */
 	private Gson gson;
 
 	/**
 	 * Konstruktor des AdminServlet. Hier werden die wichtigen Referenzen
 	 * gesetzt und wenn noetig erstellt. Auch wird ein log Eintrag geschrieben
 	 * um die Initialisierung zu ersichtlich zu machen.
 	 **/
 	public AdminServlet() {
 		super();
 		log = Helper.log;
 		gson = new Gson();
 		accountController = AccountController.getInstance();
 		docController = DocumentController.getInstance();
 		instController = InstituteController.getInstance();
 		log.write("AdminServlet", "Instance created.");
 	}
 
 	/**
 	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Check authenticity:
 		Admin admin = Helper.checkAuthenticity(request.getSession(),
 				Admin.class);
 		if (admin == null) {
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_INDEX);
 			return;
 		}
 		// Switch action on path:
 		String path = request.getPathInfo();
 		// Only activate this if you need to debug the path:
 		// log.write("AdminServlet", "Received request <" + path+">.");
 		if (path.equals("/js/loadAccounts")) {
 			Vector<Account> accounts = accountController.getAllAccounts();
 			response.setContentType("application/json");
 			response.getWriter().write(
 					gson.toJson(accounts, accounts.getClass()));
 		}
 		// Delete an account:
 		else if (path.equals("/js/deleteAccount")) {
 			// Get username parameter:
 			String username = request.getParameter("name");
 			// Check if legal:
 			if (!validate(username)) {
 				log.write("AdminServlet", "Username invalid!");
 				response.setContentType("text/error");
 				response.getWriter().write("Username invalid!");
 				return;
 			}
 			// Do & check if all okay:
 			if (!admin.deleteAccount(username)) {
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Dieser Benutzer existiert nicht oder ist aktuell angemeldet. Kann nicht gelöscht werden!");
 			}
 
 		}
 		// Get the information of an account:
 		else if (path.equals("/js/getAccountData")) {
 			String username = request.getParameter("name");
 			if (!validate(username)) {
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_ADMIN_ACCOUNTSMANAGEMENT);
 				return;
 			}
 			log.write("AdminServlet", "Getting data of account with username <"
 					+ username + ">");
 			Account account = accountController.getAccountByUsername(username);
 			response.setContentType("application/json");
 			response.getWriter().write(gson.toJson(account, Account.class));
 		} else if (path.equals("/js/addAccount")) {
 			// /hiwi/Admin/js/addAccount?realName=&email=&userName=&userPassword=&accountType=&institute=
 			String realName = request.getParameter("realName");
 			String email = request.getParameter("email");
 			String userName = request.getParameter("userName");
 			String password = request.getParameter("userPassword");
 			int accountType = -1;
 			int institute = -1;
 			try {
 				institute = Integer.parseInt(request.getParameter("institute"));
 				accountType = Integer.parseInt(request
 						.getParameter("accountType"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für Institut und AccountType.");
 				return;
 			}
 			if (!validate(realName) || !validate(email) || !validate(userName)
 					|| !validate(password) || accountType < 0
 					|| accountType > 3 || institute == -1) {
 				log.write("AdminServlet", "Error in parameters!");
 				response.setContentType("text/error");
 				response.getWriter().write("Werte illegal!");
 				return;
 			}
 			// If already exists:
 			if (accountController.getAccountByUsername(userName) != null) {
 				log.write("AdminServlet",
 						"Error creating account – username alreay exists!");
 				response.setContentType("text/plain");
 				response.getWriter().write("false");
 				return;
 			}
 			// Okay, all okay, continue:
 			Account account = new Account(userName, password, accountType,
 					email, realName, institute, null);
 			if (!admin.createAccount(account)) {
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Account konnte nicht erstellt werden! Existiert das Institut in der Datenbank?");
 				return;
 			}
 			response.setContentType("text/plain");
 			response.getWriter().write("true");
 			return;
 		} else if (path.equals("/js/getSystemInformation")) {
 			response.setContentType("application/json");
 			Runtime r = Runtime.getRuntime();
 			String[] names = new String[] { "loggedInUsers", "allUsers",
 					"totalRAM", "maxRAM" };
 			Object[] objects = new Object[] { LoggedInUsers.getUsers().size(),
 					accountController.accountCount(),
 					"~" + r.totalMemory() / (1024 * 1024) + " MB",
 					"~" + r.maxMemory() / (1024 * 1024) + " MB" };
 			response.getWriter().write(Helper.jsonAtor(names, objects));
 		} else if (path.equals("/js/editAccount")) {
 			String realName = request.getParameter("realName");
 			String email = request.getParameter("email");
 			String userName = request.getParameter("userName");
 			String password = request.getParameter("userPassword");
 			// System.out.println(password);
 			int institute = -1;
 			int accountType = -1;
 			try {
 				institute = Integer.parseInt(request.getParameter("institute"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für Institut.");
 				return;
 			}
 			if (!validate(realName) || !validate(userName) || !validate(email)
 					|| institute == -1) {
 				log.write("AdminServlet", "Error in parameters!");
 				response.setContentType("text/error");
 				response.getWriter().write("Werte illegal!");
 				return;
 			}
 			// This can happen and is legal if the password isn't to be changed:
 			if (!validate(password))
 				password = accountController.getAccountByUsername(userName)
 						.getPasswordhash();
 			accountType = accountController.getAccountByUsername(userName)
 					.getAccounttype();
 			if (!admin.editAccount(new Account(userName, password, accountType,
 					email, realName, institute, null))) {
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Fehler beim Update in der Datenbank!");
 				return;
 			}
 			response.setContentType("text/plain");
 			response.getWriter().write("true");
 			return;
 		} else if (path.equals("/js/loadDocuments")) {
 			Vector<Document> documents = docController.getAllDocuments();
 			response.setContentType("application/json");
 			response.getWriter().write(
 					gson.toJson(documents, documents.getClass()));
 		} else if (path.equals("/js/addDocument")) {
 			String title = request.getParameter("title");
 			String description = request.getParameter("description");
 			int uid = -1;
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 			} catch (NumberFormatException e) {
 				// Note: As of here, all errors that can happen regularly are
 				// encoded with a number in the text/error response so that the
 				// client can display the correct error message at the correct
 				// location in the webpage.
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("0");
 				return;
 			}
 			if (!validate(title) || !validate(description) || uid < 0) {
 				log.write("AdminServlet", "Error in parameters!");
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Fehler bei Eingabe! Fehlende Eingaben.");
 				return;
 			}
 			// all okay... continue:
 			if (!admin.addDoc(new Document(uid, title, description))) {
 				response.setContentType("text/error");
 				response.getWriter().write("1");
 				return;
 			}
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
 			return;
 		} else if (path.equals("/js/deleteDocument")) {
 			int uid = -1;
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehlerhafte UID!");
 				return;
 			}
 			Document doc = docController.getDocumentByUID(uid);
 			admin.deleteDoc(doc);
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
 			return;
 		} else if (path.equals("/js/getDocument")) {
 			int uid = -1;
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehlerhafte UID!");
 				return;
 			}
 			Document doc = docController.getDocumentByUID(uid);
 			response.setContentType("application/json");
 			response.getWriter().write(gson.toJson(doc, Document.class));
 			return;
 		} else if (path.equals("/js/editDocument")) {
 			String title = request.getParameter("title");
 			String description = request.getParameter("description");
 			int uid = -1;
 			try {
 				uid = Integer.parseInt(request.getParameter("UID"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für die UID.");
 				return;
 			}
 			if (!validate(title) || !validate(description) || uid < 0) {
 				log.write("AdminServlet", "Error in parameters!");
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Fehler bei Eingabe! Fehlende Eingaben.");
 				return;
 			}
 			// all okay... continue:
 			if (!admin.editDoc(new Document(uid, title, description))) {
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Fehler beim edititieren des Dokuments!");
 				return;
 			}
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
 			return;
 		}
 		// Load institutes as JSON data string:
 		else if (path.equals("/js/loadInstitutes")) {
 			Vector<Institute> inst = instController.getAllInstitutes();
 			if (inst == null) {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim laden der Institute!");
 				return;
 			}
 			// for (Institute in : inst)
 			// System.out.println(in.getIID() + ":" + in.getName());
 			response.setContentType("application/json");
 			response.getWriter().write(gson.toJson(inst, inst.getClass()));
 			return;
 		}
 		// Add an institute to the DB:
 		else if (path.equals("/js/addInstitute")) {
 			String name = request.getParameter("name");
 			int IID = -1;
 			try {
 				IID = Integer.parseInt(request.getParameter("IID"));
 			} catch (NumberFormatException e) {
 				// Note: As of here, all errors that can happen regularly are
 				// encoded with a number in the text/error response so that the
 				// client can display the correct error message at the correct
 				// location in the webpage.
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("0");
 				return;
 			}
 			if (!validate(name) || IID < 0) {
 				log.write("AdminServlet", "Error in parameters!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fehler bei Eingabe des Institutes! Fehlende Eingaben.");
 				return;
 			}
 			// all okay... continue:
 			if (!admin.addInstitute(new Institute(IID, name))) {
 				response.setContentType("text/error");
 				response.getWriter().write("1");
 				return;
 			}
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_ADMIN_INSTITUTESMANAGMENT);
 			return;
 		}
 		// Delete an institute from the DB:
 		else if (path.equals("/js/deleteInstitute")) {
 			int IID = -1;
 			try {
 				IID = Integer.parseInt(request.getParameter("IID"));
 			} catch (NumberFormatException e) {
 				log.write("AdminServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehlerhafte IID!");
 				return;
 			}
 			Institute institute = InstituteController.getInstance()
 					.getInstituteByIID(IID);
 			if (!admin.deleteInstitute(institute)) {
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Fehler beim löschen! Stimmt die IID?");
 				return;
 			}
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_ADMIN_INSTITUTESMANAGMENT);
 			return;
 		}
 		// Unknown:
 		else {
 			log.write("AdminServlet", "Unknown path <" + path + ">");
 		}
 	}
 }
