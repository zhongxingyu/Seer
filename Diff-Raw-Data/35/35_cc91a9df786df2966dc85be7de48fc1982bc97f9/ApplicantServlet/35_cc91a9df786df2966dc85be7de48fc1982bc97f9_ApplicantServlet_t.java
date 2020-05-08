 /**
  * @author Laura Irlinger
  * @author Tamino Hartmann
  * @author Oemer Sahin
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
 import user.Applicant;
 
 import com.google.gson.Gson;
 
 import database.account.AccountController;
 import database.application.Application;
 import database.application.ApplicationController;
 import database.document.AppDocument;
 import database.document.Document;
 import database.document.DocumentController;
 import database.offer.Offer;
 import database.offer.OfferController;
 
 /**
  * Das <code>Applicant</code> Servlet behandelt alle Aktionen von angemeldeten
  * Bewerbern (Applicants).
  */
 
 @WebServlet("/Applicant/*")
 public class ApplicantServlet extends HttpServlet {
 
 	/**
 	 * Standart default serial.
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * log
 	 */
 	private Log log = Helper.log;
 
 	/**
 	 * Variable zum speichern der GSON Instanz.
 	 */
 	private Gson gson;
 
 	/**
 	 * Variable zum speicher der Instanz des OfferControllers.
 	 **/
 	private OfferController offcon;
 
 	/**
 	 * Konstruktor.
 	 **/
 	public ApplicantServlet() {
 		super();
 		gson = new Gson();
 		offcon = OfferController.getInstance();
 	}
 
 	/**
 	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Check authenticity:
 		Applicant applicant = Helper.checkAuthenticity(request.getSession(),
 				Applicant.class);
 		if (applicant == null) {
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_INDEX);
 			return;
 		}
 		// Switch action on path:
 		String path = request.getPathInfo();
 		// Do loadAccount:
 		if (path.equals("/js/loadAccount")) {
 			String realName = applicant.getUserData().getName();
 			String email = applicant.getUserData().getEmail();
 			String JsonString = Helper.jsonAtor(new String[] { "realName",
 					"email" }, new String[] { realName, email });
 			response.setContentType("application/json");
 			response.getWriter().write(JsonString);
 		}
 		// Delete an application, bewerbung wiederrufen:
 		else if (path.equals("/js/deleteApplication")) {
 			// aid und benutzername sind identifier
 			int aid;
 			try {
 				aid = Integer.parseInt(request.getParameter("AID"));
 
 			} catch (NumberFormatException e) {
 				log.write("ApplicantServlet",
 						"There was an error while PARSING int-value(AID) in: "
 								+ path.toString());
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim Parsen der AID!");
 				return;
 			}
 
 			String callerUsername = applicant.getUserData().getUsername(); // benutzername
 																			// des
 																			// bewerbers
 			// alle bewerbungen dieses Bewerbers
 			Vector<Application> applisToDelete = ApplicationController
 					.getInstance().getApplicationsByApplicant(callerUsername);
 			Application appToDelete = null;
 
 			// gehe alle Bewerbungen von diesem User durch und suche nach der
 			// AID des Angebots, welches er wiederufen will
 			for (int i = 0; i < applisToDelete.size(); i++) {
 				if (applisToDelete.elementAt(i).getAid() == aid)// gefunden !
 					appToDelete = applisToDelete.elementAt(i);
 			}
 
 			if (appToDelete == null) { // nicht gefunden!
 				log.write(
 						"ApplicantServlet",
 						"There is no application for user:" + callerUsername
 								+ " with aid= " + aid + ". Errorpath: "
 								+ path.toString());
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fuer diesen Benutzernamen existiert keine Bewerbung mit der gegebenen AID!");
 				return;
 			}
 
 			log.write("ApplicantServlet", "Deleting application in progress...");
 			ApplicationController.getInstance().deleteApplication(appToDelete);
 
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			return;
 		}
 		// Load my offers:
 		else if (path.equals("/js/loadMyOffers")) {
 			// Offer vom User geholt
 			Vector<Offer> myoffers = offcon.getOffersByApplicant(applicant
 					.getUserData().getUsername());
 			response.setContentType("myapplication/json");
 			response.getWriter().write(
 					gson.toJson(myoffers, myoffers.getClass()));
 		}
 		// Load offers:
 		else if (path.equals("/js/loadOffers")) {
 			Vector<Offer> offers = offcon.getCheckedOffers(); // nur gepr�fte
 																// Angebote
 			// bereits beworbene Stellen entfernen
 			// Offer vom User geholt
 			Vector<Offer> myoffers1 = offcon.getOffersByApplicant(applicant
 					.getUserData().getUsername());
 
 			boolean entfernen;
 			for (int i = 0; i < offers.size(); i++) {
 				entfernen = false;
 				for (int j = 0; j < myoffers1.size(); j++) {
 					if (((Offer) offers.elementAt(i)).getAid() == myoffers1
 							.elementAt(j).getAid()) {
 						entfernen = true;
 					}
 					if (entfernen) {
 						offers.remove(i);
 					}
 				}
 			}
 
 			response.setContentType("application/json");
 			response.getWriter().write(gson.toJson(offers, offers.getClass()));
 		}
 
 		// Load my information about one application:
 		else if (path.equals("/js/selectApplication")) {
 			int aid = Integer.parseInt(request.getParameter("id"));

 			String username = applicant.getUserData().getUsername();
			Application appli = ApplicationController.getInstance()
					.getApplicationByOfferAndUser(aid, username);
 			String status = "fehler";
			if (appli.isChosen()) {
 				status = " - angenommen";
 			} else {
 				status = " - nicht angenommen";
 			}

 			Offer off = offcon.getOfferById(aid);
 			response.setContentType("application/json");
 			response.getWriter().write(
					Helper.jsonAtor(new String[] { "offerName", "author",
							"status" },
							new Object[] { off.getName(), off.getAuthor(),
									status }));
 			return;
 
 		}
 		// Load my information about one application(documents):
 		else if (path.equals("/js/selectDocuments")) {
 			int aid = -1;
 			try {
 				aid = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				log.write("ApplicantServlet",
 						"NumberFormatException parsing parameter!");
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim Parsen der AID!");
 				return;
 			}
 			// Create JSON version of custom data:
 			Vector<String> docDataObject = new Vector<String>();
 			for (AppDocument appDoc : DocumentController.getInstance()
 					.getAppDocument(aid, applicant.getUserData().getUsername())) {
 				int UID = appDoc.getdID();
 				Document doc = DocumentController.getInstance()
 						.getDocumentByUID(UID);
 				String dataObject = "{name:\"";
 				// Write name to dataObject:
 				dataObject += doc.getName();
 				dataObject += "\",isChecked:";
 				// Write if it has been checked:
 				dataObject += (appDoc.getPresent()) ? 1 : 0;
 				dataObject += "}";
 				// Do nice things to wrap it up:
 				docDataObject.add(dataObject);
 			}
 			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(docDataObject, docDataObject.getClass()));
 		}
 		// Delete own account:
 		else if (path.equals("/js/deleteAccount")) {
 			// Das hier ist sehr falsch (wer auch immer es war)! ->
 			// "String name = request.getParameter("name");"
 			// name wäre so IMMER NULL! (js liest also falschen parameter aus
 			// und generell soll man NIE den client trauen!)
 			String name = applicant.getUserData().getUsername();
 			if (applicant.deleteOwnAccount()) {
 				System.out.println("delete appli");
 				log.write("ApplicantServlet", name
 						+ " has deleted his account.");
 				// Simply now for debugging:
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_INDEX);
 			} else {
 				System.out.println("not delete appli");
 				response.setContentType("text/error");
 				response.getWriter().write("Error while deleting account!");
 			}
 		}
 		// change own account data
 		else if (path.equals("/js/changeAccount")) {
 			String name = request.getParameter("name");
 			String email = request.getParameter("mail");
 			String pw = request.getParameter("pw");
 			if (pw.equals(""))
 				pw = null; // falls leeres pw-> null damit die editOwnAccount
 							// funktion das pw nicht auf "" setzt!
 			if (applicant.editOwnAccount(name, email, pw, null)) {
 				log.write("ApplicantServlet", applicant.getUserData()
 						.getUsername() + " has modified his account.");
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim ändern der Daten.");
 			}
 		} else if (path.equals("/js/apply")) {
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			if (applicant.apply(aid)) {
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("error");
 			}
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
 			log.write("ApplicantServlet", "Unknown path <" + path + ">");
 		}
 	}
 }
