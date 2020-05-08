 /**
  * @author Laura Irlinger
  * @author Tamino Hartmann
  * @author Patryk Boczon
  * @author Oemer Sahin
  */
 package servlet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 import logger.Log;
 import mail.Mailer;
 import user.Clerk;
 
 import com.google.gson.Gson;
 
 import database.DatabaseController;
 import database.HilfsDatenClerk;
 import database.account.Account;
 import database.account.AccountController;
 import database.application.Application;
 import database.application.ApplicationController;
 import database.document.AppDocument;
 import database.document.Document;
 import database.document.DocumentController;
 import database.document.OfferDocument;
 import database.institute.InstituteController;
 import database.offer.Offer;
 import database.offer.OfferController;
 
 /**
  * Das <code>Clerk</code> Servlet behandelt alle Aktionen von angemeldeten
  * (Sach-)Bearbeitern (Clerk).
  */
 
 @WebServlet("/Clerk/*")
 public class ClerkServlet extends HttpServlet {
 	/**
 	 * Standard serialVersionUID.
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Variable zum speicher der Log Instanz.
 	 */
 	private Log log;
 
 	/**
 	 * Variable zum speichern einer Instanz vom DocumentController;
 	 */
 	private DocumentController doccon;
 
 	/**
 	 * Variable zum speichern einer Instanz vom ApplicationController;
 	 */
 	private ApplicationController appcon;
 
 	/**
 	 * Variable zum speichern einer Instanz vom OfferController;
 	 */
 	private OfferController offcon;
 
 	/**
 	 * Variable zum speichern einer Instanz vom InstituteController
 	 */
 	private InstituteController instcon;
 	/**
 	 * Variable zum speichern einer Instanz vom InstituteController
 	 */
 	private AccountController acccon;
 	/**
 	 * Variable zum speichern einer Instanz vom Mailer
 	 */
 	private Mailer mail;
 
 	/**
 	 * Variable zum speichern der GSON Instanz.
 	 */
 	private Gson gson;
 
 	/**
 	 * Konstruktor.
 	 */
 	public ClerkServlet() {
 		super();
 		log = Helper.log;
 		gson = new Gson();
 		acccon = AccountController.getInstance();
 		doccon = DocumentController.getInstance();
 		appcon = ApplicationController.getInstance();
 		offcon = OfferController.getInstance();
 		instcon = InstituteController.getInstance();
 		mail = Mailer.getInstance();
 	}
 
 	/**
 	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Check authenticity:
 		Clerk clerk = Helper.checkAuthenticity(request.getSession(),
 				Clerk.class);
 		if (clerk == null) {
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_INDEX);
 			return;
 		}
 		String path = request.getPathInfo();
 		path = (path == null) ? "" : path;
 		// log.write("ClerkServlet", "Received request: " + path);
 		// Load the offers of the clerk:
 		if (path.equals("/js/showMyOffers")) {
 			// Load all correct offers:
 			Vector<Offer> myoffers = clerk.getAllUncheckedOffers();
 			response.setContentType("offers/json");
 			response.getWriter().write(
 					gson.toJson(myoffers, myoffers.getClass()));
 		} else if (path.equals("/js/editOneOffer")) {
 			int aid = -1;
 			try {
 				aid = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler! Ungültige AID:");
 				return;
 			}
 			// AID should be != -1 here, so continue:
 			Offer offertoedit = OfferController.getInstance().getOfferById(aid);
 			response.setContentType("application/json");
 			response.getWriter().write(
 					gson.toJson(offertoedit, offertoedit.getClass()));
 			return;
 		} else if (path.equals("/js/saveOffer")) {
 			boolean changed = Boolean.parseBoolean(request
 					.getParameter("changed"));
 			boolean accepted = Boolean.parseBoolean(request
 					.getParameter("annehmen"));
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			double hoursperweek = Double.parseDouble(request
 					.getParameter("hoursperweek"));
 			double wage = 0.0;
 			try {
 				wage = Double.parseDouble(request.getParameter("wage"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fehler bei Eingabe! Nur double Werte erlaubt fuer wage.");
 				return;
 			}
 			Offer offertosave = OfferController.getInstance().getOfferById(aid);
 			// set modificationdate to current date
 			java.util.Date aenderungsdatum = new java.util.Date();
 			java.sql.Date aenderungsdatum_toUp = new java.sql.Date(
 					aenderungsdatum.getTime());
 			// sets modificationdate and updates it
 			offertosave.setModificationdate(aenderungsdatum_toUp);
 			offertosave.setWage(wage);
 			offertosave.setHoursperweek(hoursperweek);
 			if (changed && accepted) {
 				offertosave.setChecked(true);
 				offertosave.setFinished(false);
 			} else if (changed && !accepted) {
 				offertosave.setChecked(false);
 				offertosave.setFinished(true);
 			} else {
 				offertosave.setChecked(false);
 				offertosave.setFinished(false);
 			}
 			Account author = acccon.getAccountByUsername(offertosave
 					.getAuthor());
 			String address = author.getEmail();
 			if (offertosave.isChecked() && !offertosave.isFinished())
 				mail.sendMail(address, "Freischaltung des Angebots \""
 						+ offertosave.getName() + "\"",
 						"Hiermit teilen wir ihnen mit, dass ihr Angebot \""
 								+ offertosave.getName()
 								+ "\" für Bewerber freigeschaltet wurde.");
 			if (!offertosave.isChecked() && offertosave.isFinished())
 				mail.sendMail(address,
 						"Ablehnen des Angebots \"" + offertosave.getName()
 								+ "\"",
 						"Hiermit teilen wir ihnen mit, dass ihr Angebot \""
 								+ offertosave.getName()
 								+ "\" durch einen Verwalter abgelehnt wurde.");
 			OfferController.getInstance().updateOffer(offertosave);
 			// wir wollten doch einen String als date?
 			// Antwort von Tamino: ist es auch... aber irgendwie müssen wir das
 			// Datum auch holen um es abspeichern zu können, bzw. irgendwo geht
 			// da was schief.
 			// OfferController.getInstance().getOfferById(aid).setModificationdate(getDateTime());
 			response.setContentType("offers/json");
 			response.getWriter().write(
 					gson.toJson(offertosave, offertosave.getClass()));
 			return;
 		} else if (path.equals("/js/documentsFromOffer")) {
 
 			String aid = request.getParameter("aid");
 			int aid1 = Integer.parseInt(aid);
 			Vector<Offer> offersid = OfferController.getInstance()
 					.getAllOffers();
 			// String offername;
 			Vector<OfferDocument> offerdocuments = new Vector<OfferDocument>();
 			for (int i = 0; i < offersid.size(); i++) {
 				if (aid1 == offersid.elementAt(i).getAid()) {
 					offerdocuments = DocumentController.getInstance()
 							.getDocumentsByOffer(Integer.parseInt(aid));
 				}
 			}
 			Vector<Document> documents = new Vector<Document>();
 			for (int i = 0; i < offerdocuments.size(); i++) {
 				documents.add(DocumentController.getInstance()
 						.getDocumentByUID(
 								offerdocuments.elementAt(i).getDocumentid()));
 			}
 			response.setContentType("documentsoffer/json");
 			response.getWriter().write(
 					gson.toJson(documents, documents.getClass()));
 
 		}
 
 		// Creates a Vector of Documents which can be added to an offer
 		else if (path.endsWith("/js/documentsToAddToOffer")) {
 
 			int aid = Integer.parseInt(request.getParameter("aid"));
 
 			Vector<Document> docsToAdd = DocumentController.getInstance()
 					.getDocumentsToAddToOffer(aid);
 
 			response.setContentType("documentstoaddoffer/json");
 			response.getWriter().write(
 					gson.toJson(docsToAdd, docsToAdd.getClass()));
 		}
 
 		// Creates a Vector of Documents which can be added to an application
 		else if (path.endsWith("/js/documentsToAddToApplication")) {
 
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			String username = request.getParameter("username");
 
 			Vector<Document> docsToAdd = DocumentController.getInstance()
 					.getDocumentsToAddToApp(aid, username);
 
 			response.setContentType("docstoaddtoapp/json");
 			response.getWriter().write(
 					gson.toJson(docsToAdd, docsToAdd.getClass()));
 		}
 
 		// Creates a Vector for the table in applicationmanagement.jsp
 		else if (path.equals("/js/showApplication")) {
 			String username = clerk.getUserData().getUsername();
 			Account clerka = AccountController.getInstance()
 					.getAccountByUsername(username);
 			Vector<HilfsDatenClerk> daten = DatabaseController.getInstance()
 					.getChosenApplicationDataByInstitute(clerka.getInstitute());
 			if (daten == null || daten.isEmpty()) {
 				response.setContentType("text/error");
 				response.getWriter().write("Keine Einträge in der DB!");
 				return;
 			}
 			// System.out.println("Ergebnis: "+daten.size());
 			response.setContentType("showapplication/json");
 			response.getWriter().write(gson.toJson(daten, daten.getClass()));
 
 		}
 		// Creates an Vector for the table in editapplication.jsp
 		else if (path.equals("/js/applicationDocuments")) {
 			String user = request.getParameter("User");
 			// System.out.println("User:"+user);
 			String aid = request.getParameter("AID");
 			// System.out.println("Aid:"+aid);
 			int aid1 = Integer.parseInt(aid);
 			Account acc = AccountController.getInstance().getAccountByUsername(
 					user); // Account vom ausgew�hlten User
 			// Offer des ausgew�hlten User
 			Offer off = OfferController.getInstance().getOfferById(aid1);
 			Vector<AppDocument> docs = DocumentController.getInstance()
 					.getDocumentsByUserAndOffer(acc, off);
 			Vector<Document> docs2 = new Vector<Document>(docs.size());
 
 			// dieser Vector enth�lt bei geraden Indizes ein Element vom Typ
 			// Document, bei dem
 			// jeweiligen darauffolgenden, ungeraden Index das Pendant zu
 			// diesem, in Form eines AppDocument
 			Vector<Object> customDocs = new Vector<Object>(docs.size() * 2);
 
 			for (int i = 0; i < docs.size(); i++) {
 				docs2.add(DocumentController.getInstance().getDocumentByUID(
 						docs.elementAt(i).getdID()));
 
 				customDocs.add(docs2.elementAt(i));
 				customDocs.add(docs.elementAt(i));
 			}
 
 			// System.out.println("Ergebnis: "+docs2);
 			response.setContentType("showthedocuments/json");
 			response.getWriter().write(
 					gson.toJson(customDocs, customDocs.getClass()));
 
 		}
 		// Updates the status of an AppDocument
 		else if (path.equals("/js/setDocCheck")) {
 			String username = request.getParameter("username");
 			// TODO: try-catch!
 			int offerid = Integer.parseInt(request.getParameter("offerid"));
 			int docid = Integer.parseInt(request.getParameter("docid"));
 			AppDocument appdoc = DocumentController.getInstance()
 					.getDocumentByUsernameAIDandUID(username, offerid, docid);
 			if (appdoc.getPresent()) {
 				appdoc.setPresent(false);
 			} else {
				appdoc.setPresent(true);
				// System.out.println("set to true");
 			}
 			DocumentController.getInstance().updateAppDocument(appdoc);
			log.write("ClerkServelt", "<" + clerk.getUserData().getUsername()
					+ "> changed AppDoc.");
 			return;
 		}
 		// Creates an String for the table in editapplication.jsp
 		else if (path.equals("/js/getApplicantInfo")) {
 			String user = request.getParameter("User");
 			String aid = request.getParameter("AID");
 			// TODO: try-catch
 			int aid1 = Integer.parseInt(aid);
 			String realName = AccountController.getInstance()
 					.getAccountByUsername(user).getName();
 			String offerName = OfferController.getInstance().getOfferById(aid1)
 					.getName();
 			response.setContentType("application/json");
 			response.getWriter().write(
 					Helper.jsonAtor(new String[] { "realName", "offerName" },
 							new Object[] { realName, offerName }));
 		}
 		// Funktion zum hinzufuegen eines Dokuments (aehnlich wie beim Admin).
 		// else if (path.equals("/js/createDocument")) {
 		// String title = request.getParameter("title");
 		// String description = request.getParameter("description");
 		// // int uid = -1;
 		// // try {
 		// // uid = Integer.parseInt(request.getParameter("uid"));
 		// // } catch (NumberFormatException e) {
 		// // log.write("ClerkServlet",
 		// // "NumberFormatException while parsing URL!");
 		// // response.setContentType("text/error");
 		// // response.getWriter()
 		// //
 		// .write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für die UID.");
 		// // return;
 		// // }
 		// if (title == null || title.isEmpty() || description == null
 		// || description.isEmpty() /* || uid < 0 */) {
 		// log.write("ClerkServlet", "Error in parameters!");
 		// response.setContentType("text/error");
 		// response.getWriter().write(
 		// "Fehler bei Eingabe! Fehlende Eingaben.");
 		// return;
 		// }
 		// // all okay... continue:
 		// if (!DocumentController.getInstance().generateDocument(title,
 		// description)) {
 		// response.setContentType("text/error");
 		// response.getWriter()
 		// .write("Fehler beim erstellen des Dokuments! Ist die UID eineindeutig?");
 		// return;
 		// }
 		// response.setContentType("text/url");
 		// response.getWriter().write(Helper.D_CLERK_EDITAPPLICATION);
 		// return;
 		// }
 		else if (path.equals("/js/deleteAppDocument")) {
 			int uid = Integer.parseInt(request.getParameter("uid"));
 			String username = request.getParameter("user");
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			AppDocument appdoc = DocumentController.getInstance()
 					.getDocumentByUsernameAIDandUID(username, aid, uid);
 
 			DocumentController.getInstance().deleteAppDocument(appdoc);
 			// response.setContentType("text/url");
 			// response.getWriter().write(Helper.D_CLERK_EDITAPPLICATION);
 			return;
 
 		}
 		// Funktion zum entfernen eines Dokuments (aehnlich wie beim Admin).
 		else if (path.equals("/js/deleteDocument")) {
 			int uid = -1;
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehlerhafte uid!");
 				return;
 			}
 			Document doc = doccon.getDocumentByUID(uid);
 			clerk.delDoc(doc);
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_CLERK_EDITAPPLICATION);
 			return;
 		}
 		// Delete own account:
 		else if (path.equals("/js/deleteAccount")) {
 			String name = request.getParameter("name");
 
 			if (clerk.deleteOwnAccount()) {
 				log.write("ApplicantServlet", name
 						+ " has deleted his account.");
 				// Simply now for debugging:
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_INDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("Error while deleting account!");
 			}
 		}
 		// change own account data
 		else if (path.equals("/js/changeAccount")) {
 			String name = request.getParameter("name");
 			String email = request.getParameter("mail");
 			String pw = request.getParameter("pw");
 			String rep = request.getParameter("rep");
 			// System.out.println("clerk pw: " + pw);
 			if (pw.equals(""))
 				pw = null; // falls leeres pw-> null damit die editOwnAccount
 							// funktion das pw nicht auf "" setzt!
 			if (rep == null)
 				rep = "";
 			// System.out.println("clerk edit own account: " + name + "-" +
 			// email+ "-" + pw + "-" + rep);
 			if (clerk.editOwnAccount(name, email, pw, rep)) {
 				log.write("ClerkServlet", clerk.getUserData().getUsername()
 						+ " has modified his account.");
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_CLERK_USERINDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim ändern der Daten.");
 			}
 		}
 		// Do loadAccount:
 		else if (path.equals("/js/loadAccount")) {
 			String realName = clerk.getUserData().getName();
 			String email = clerk.getUserData().getEmail();
 			String rep = clerk.getUserData().getRepresentant();
 			String JsonString = Helper.jsonAtor(new String[] { "realName",
 					"email", "rep" }, new String[] { realName, email, rep });
 			response.setContentType("application/json");
 			response.getWriter().write(JsonString);
 		}
 		// loads potential representatives for this account
 		else if (path.equals("/js/loadRepresentatives")) {
 			String username = clerk.getUserData().getUsername();
 			Vector<String> representatives = AccountController.getInstance()
 					.getPotentialRepresentatives(username);
 
 			response.setContentType("application/json");
 			response.getWriter().write(
 					gson.toJson(representatives, representatives.getClass()));
 		}
 		// TODO
 		// Ich bekomme noch keine Daten vom Server (username,AID). --> Unchecked
 		else if (path.equals("/js/doApplicationCompletion")) {
 			int AID = 0;
 			String username;
 			try {
 				AID = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"NumberFormatException while parsing URL!");
 				response.setContentType("text/error");
 				response.getWriter().write(
 						"Error while parsing String into int");
 				return;
 			}
 			username = request.getParameter("username");
 			// Prueft ob alle Dokumente abgegeben wurden.
 			// Die einzige Bedingung die wir and den Vertragsabschluss-Button
 			// gestellt haben
 			// war das er nur dann erfolgreich ist wen alles vorhanden ist und
 			// nicht das er
 			// die fehlenden Dokumente mitschickt(Name des Dokuments) oder doch?
 			if (clerk.checkAllDocFromApplicant(username, AID)) {
 				response.setContentType("text/url");
 				// Soll jetzt ab hier den Bewerber als "angenommen" markiert
 				// werden oder wird das dan endgueltig vom
 				// Anbieter bestimmt? (Tabelle: Bewerbungen Zeile: ausgewaehlt)
 			} else {
 				response.setContentType("error/url");
 				response.getWriter().write(Helper.D_CLERK_EDITAPPLICATION);
 			}
 		}
 		// Funktion zum entfernen eines OfferDocuments des gewaehlten Offers
 		else if (path.equals("/js/deleteOfferDocument")) {
 
 			int uid = Integer.parseInt(request.getParameter("uid"));
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			DocumentController.getInstance().deleteOfferDocument(
 					new OfferDocument(aid, uid));
 			return;
 		}
 
 		// Funktion zum hinzufuegen eines OfferDocuments des gewaehlten Offers
 		else if (path.equals("/js/addOfferDocument")) {
 			int uid;
 			int aid;
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 				aid = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"Error add offer document! UID or AID invalid!");
 				return;
 			}
 			DocumentController.getInstance().createOfferDocument(
 					new OfferDocument(aid, uid));
 			return;
 		}
 
 		// Funktion zum hinzufuegen eines AppDocuments der gewaehlten
 		// Application
 		else if (path.equals("/js/addAppDocument")) {
 			int uid;
 			int aid;
 			String username = request.getParameter("username");
 			try {
 				uid = Integer.parseInt(request.getParameter("uid"));
 				aid = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"Error add offer document! UID or AID invalid!");
 				return;
 			}
 			DocumentController.getInstance().createAppDocument(
 					new AppDocument(username, aid, uid, false));
 			return;
 		}
 
 		// TODO!
 		// Ich bekomme noch keine Daten vom Server (username,AID). --> Unchecked
 		else if (path.equals("/js/doApplicationCompletion")) {
 			int AID = 0;
 			String username;
 			try {
 				AID = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ClerkServlet",
 						"NumberFormatException while parsing URL!");
 			}
 			username = request.getParameter("username");
 			// Prueft ob alle Dokumente abgegeben wurden.
 			// Die einzige Bedingung die wir and den Vertragsabschluss-Button
 			// gestellt haben
 			// war das er nur dann erfolgreich ist wen alles vorhanden ist und
 			// nicht das er
 			// die fehlenden Dokumente mitschickt(Name des Dokuments) oder doch?
 			if (clerk.checkAllDocFromApplicant(username, AID)) {
 				response.setContentType("test/url");
 				// Soll jetzt ab hier den Bewerber als "angenommen" markiert
 				// werden oder wird das dan endgueltig vom
 				// Anbieter bestimmt? (Tabelle: Bewerbungen Zeile: ausgewahlt)
 			} else {
 				response.setContentType("error/url");
 				response.getWriter().write(Helper.D_CLERK_EDITAPPLICATION);
 			}
 
 		} else if (path.equals("/js/loadInfo")) {
 			Vector<Offer> off = new Vector<Offer>();
 			Vector<Integer> institutes = instcon
 					.getAllRepresentingInstitutes(clerk.getUserData()
 							.getUsername());
 			institutes.add(0);
 			for (Integer i : institutes)
 				off.addAll(offcon.getUncheckedOffersByInstitute(i));
 			int unchecked = off.size();
 			int apps = 0;
 			for (Offer o : offcon.getCheckedOffers()) {
 				if (!o.isFinished()) {
 					Vector<Application> aps = appcon.getApplicationsByOffer(o
 							.getAid());
 					for (Application a : aps) {
 						if (a.isChosen() && !a.isFinished())
 							apps++;
 					}
 				}
 			}
 			String gsonny = Helper.jsonAtor(new String[] { "offers", "apps" },
 					new Object[] { unchecked, apps });
 			response.setContentType("application/json");
 			response.getWriter().write(gsonny);
 			return;
 		}
 		// Get email addresses if required:
 		else if (path.equals("/js/getEmail")) {
 			String user = request.getParameter("user");
 			if (!Helper.validate(user)) {
 				response.setContentType("text/error");
 				response.getWriter().write("Invalid user parameter!");
 				return;
 			}
 			String email = null;
 			try {
 				email = AccountController.getInstance()
 						.getAccountByUsername(user).getEmail();
 			} catch (NullPointerException e) {
 				log.write("ClerkServlet",
 						"Error getting e-mail adress of user in: " + path);
 				return;
 			}
 			response.setContentType("text/email");
 			response.getWriter().write(email);
 		} else {
 			log.write("ClerkServlet", "Unknown path <" + path + ">");
 		}
 	}
 
 	/**
 	 * GET-Aufruffe werden hier nur zum Download der Excel-Datei verwendet.
 	 * Ansonsten leitet es an public/index.jsp weiter.
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Check authenticity:
 		Clerk clerk = Helper.checkAuthenticity(request.getSession(),
 				Clerk.class);
 		if (clerk == null) {
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_INDEX);
 			return;
 		}
 		String path = request.getPathInfo();
 		if (path.equals("/js/doExcelExport")) {
 			File file = null;
 			try {
 				file = clerk.doExport();
 			} catch (RowsExceededException e) {
 				response.setContentType("text/error");
 				response.getWriter().write("RowsExceededExcetion!");
 			} catch (WriteException e) {
 				response.setContentType("text/error");
 				response.getWriter().write("Error while writing File");
 			}
 			FileInputStream fileToDownload = new FileInputStream(file);
 			ServletOutputStream output = response.getOutputStream();
 			response.setContentType("application/msexcel");
 			response.setHeader("Content-Disposition",
 					"attachment; filename=excelExport.xls");
 			response.setContentLength(fileToDownload.available());
 			int c;
 			while ((c = fileToDownload.read()) != -1) {
 				output.write(c);
 			}
 			output.flush();
 			output.close();
 			fileToDownload.close();
 		}
 	}
 }
