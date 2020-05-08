 /**
  * @author Anatoli Brill
  * @author Manuel Guentzel
  */
 
 package user;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.servlet.http.HttpSession;
 
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 import mail.Mailer;
 import servlet.Helper;
 import database.HilfsDatenClerk;
 import database.account.Account;
 import database.application.Application;
 import database.document.AppDocument;
 import database.document.Document;
 import database.document.OfferDocument;
 import database.offer.Offer;
 import file.ExcelExport;
 
 /**
  * Verwaltet alle Aufgaben und Daten eines Verwalters.
  */
 public class Clerk extends User {
 
 	/**
 	 * Instanz des Mailers.
 	 */
 	private Mailer mail;
 
 	/**
 	 * Konstruktor. Erstellte Objekte werden automatisch in der LoggedInUsers
 	 * klasse aufgenommen.
 	 * 
 	 * @param username
 	 *            Benutzername im System
 	 * @param email
 	 *            E-Mail Adresse
 	 * @param name
 	 *            Realer Name
 	 * @param representative
 	 *            Name des Repraesentanten.
 	 * @param session
 	 *            Session des Benutzers
 	 */
 	public Clerk(String username, String email, String name,
 			String representative, HttpSession session) {
 		super(username, email, name, representative, session);
 		mail = Mailer.getInstance();
 		userManagement.LoggedInUsers.addUser(this);
 	}
 
 	/**
 	 * Editiert den eigenen Account. Der Benutzername ist dabei nicht aenderbar
 	 * und identifiziert den zu aendernden Account in der Datenbank
 	 * 
 	 * @param acc
 	 *            geaenderter Account
 	 * @return Wahrheitswert ob erfolgreich.
 	 */
 	public boolean editAccount(Account acc) {
 		if (!acccon.updateAccount(acc)) {
 			log.write("Clerk", "Error modifying account!");
 			return false;
 		}
 		log.write("Clerk", "<" + getUserData().getUsername()
 				+ "> modified account of <" + acc.getUsername() + ">.");
 		return true;
 	}
 
 	/**
 	 * Methode zum Loeschen seines Accounts
 	 * 
 	 * @return Beim erfolgreichen Entfernen wird ein TRUE zurueckgegeben. Falls
 	 *         irgendwo ein Fehler aufgetreten ist wird ein FALSE
 	 *         zurueckgegeben.
 	 */
 	public boolean deleteOwnAccount() {
 		invalidate();
 		return acccon.deleteClerkAccount(this);
 	}
 
 	/**
 	 * Gibt ein Angebot anhand der AID zurueck.
 	 * 
 	 * @param AID
 	 *            Die AID des Angebots.
 	 * @return Das Angebot.
 	 */
 	public Offer getOfferByAID(int AID) {
 		Offer offer = offcon.getOfferById(AID);
 		// Sichergehen, dass der Sachbearbeiter dieses auch einsehen darf:
 		if ((offer.getInstitute() == 0)
 				|| (offer.getInstitute() == acccon.getAccountByUsername(
 						getUserData().getUsername()).getInstitute()))
 			return offer;
 		else
 			return null;
 	}
 
 	/**
 	 * Aktualisiert ein Angebot in der Datenbank und schickt entsprechend Emails
 	 * an die Betroffenen.
 	 * 
 	 * @param off
 	 *            Das zu aendernde Angebot.
 	 * @return Flag fuer Fehler.
 	 */
 	public boolean updateOffer(Offer off) {
 		Account author = acccon.getAccountByUsername(off.getAuthor());
 		String address = author.getEmail();
 		if (off.isChecked() && !off.isFinished())
 			mail.sendMail(
 					address,
 					"Freischaltung des Angebots \"" + off.getName() + "\"",
 					"Hiermit teilen wir ihnen mit, dass ihr Angebot \""
 							+ off.getName()
 							+ "\" für Bewerber freigeschaltet wurde.");
 		if (!off.isChecked() && off.isFinished())
 			mail.sendMail(address, "Ablehnen des Angebots \"" + off.getName()
 					+ "\"", "Hiermit teilen wir ihnen mit, dass ihr Angebot \""
 					+ off.getName()
 					+ "\" durch einen Verwalter abgelehnt wurde.");
 		return offcon.updateOffer(off);
 	}
 
 	/**
 	 * Liefert alle Dokumente zu einem Angebot zurueck.
 	 * 
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @return Die Dokumente.
 	 */
 	public Vector<Document> getDocumentsFromOffer(int aid) {
 		Vector<Offer> offersid = offcon.getAllOffers();
 		Vector<OfferDocument> offerdocuments = new Vector<OfferDocument>();
 		for (Offer offer : offersid) {
 			if (aid == offer.getAid())
 				offerdocuments = doccon.getDocumentsByOffer(aid);
 		}
 		Vector<Document> documents = new Vector<Document>();
 		for (OfferDocument offDoc : offerdocuments) {
 			documents.add(doccon.getDocumentByUID(offDoc.getDocumentid()));
 		}
 		return documents;
 	}
 
 	/**
 	 * Export fuer die Excel-File
 	 * 
 	 * @return Fileobjekt des ExcelFiles
 	 * @throws IOException
 	 * @throws WriteException
 	 * @throws RowsExceededException
 	 */
 	public File doExport() throws IOException, RowsExceededException,
 			WriteException {
 		return ExcelExport.export(this.getUserData());
 
 	}
 
 	/**
 	 * Prueft ob ein Bewerber alle Dokumente abgegeben hat.
 	 * 
 	 * @param username
 	 *            benutzername des Bewerbers
 	 * @param offerID
 	 *            ID des Angebotes der Bewerbung
 	 * @return True falls alles abgegeben wurde, sonst False.
 	 */
 	public boolean checkAllDocFromApplicant(String username, int offerID) {
 		Account acc = acccon.getAccountByUsername(username);
 		Offer off = offcon.getOfferById(offerID);
 		Vector<AppDocument> vec = doccon.getDocumentsByUserAndOffer(acc, off);
 		for (AppDocument appDoc : vec) {
 			if (!(appDoc.getPresent())) {
 				return false;
 			}
 		}
 		String clerkname = getUserData().getUsername();
 		Application appli = appcon.getApplicationByOfferAndUser(offerID,
 				username);
 		appli.setFinished(true);
 		appli.setClerk(clerkname);
		return appcon.updateApplication(appli);
 	}
 
 	/**
 	 * Diese Methode holt alle ungeprueften Angebote aus der Datenbank und gibt
 	 * sie als Vector<Offer> zurueck. Dabei werden auch die Angebote jener
 	 * Benutzer gegeben, welche dieser Benutzer vertritt.
 	 * 
 	 * @return Denn Vector<Offer> mit den entsprechenden Angeboten.
 	 */
 	public Vector<Offer> getAllUncheckedOffers() {
 		Vector<Offer> offers = new Vector<Offer>();
 		String userName = getUserData().getUsername();
 		Vector<Account> reps = acccon.getRepresentativeAccounts(userName);
 		// Check if field is used:
 		if (reps != null) {
 			for (Account acc : reps)
 				offers.addAll(offcon.getUncheckedOffersByClerk(acc));
 		}
 		// Load own offers:
 		log.write("Clerk", "Getting unchecked offers applyable to <" + userName
 				+ ">.");
 		Account ownAccount = acccon.getAccountByUsername(userName);
 		offers.addAll(offcon.getUncheckedOffersByClerk(ownAccount));
 		return offers;
 	}
 
 	/**
 	 * Gibt alle Dokumente zurueck, welche in einem Angebot noch nicht verwendet
 	 * werden.
 	 * 
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @return Die unverwendeten Dokumente.
 	 */
 	public Vector<Document> getUnusedDocForOffer(int aid) {
 		return doccon.getDocumentsToAddToOffer(aid);
 	}
 
 	/**
 	 * Gibt alle zu vergebende Dokumente auf eine Bewerbung zurueck.
 	 * 
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @param username
 	 *            Der Benutzername des Bewerbers.
 	 * @return Die Dokumente verpackt in einen wunderschoenen Vektor.
 	 */
 	public Vector<Document> getDocsForApplication(int aid, String username) {
 		return doccon.getDocumentsToAddToApp(aid, username);
 	}
 
 	/**
 	 * Gibt Bewerbername und Angebotsname aller angenommenen Bewerbungen des
 	 * uebergebenen Instituts in Form eines Vectors des Datentyps
 	 * HilfsDatenClerk zurueck.
 	 * 
 	 * @param clerkAccount
 	 *            Der Account des Clerks.
 	 * @return Die Hilfsdaten.
 	 */
 	public Vector<HilfsDatenClerk> getVoodoo(Account clerkAccount) {
 		return appcon
 				.getChosenAndNotFinishedApplicationDataByInstitute(clerkAccount
 						.getInstitute());
 	}
 
 	/**
 	 * Liefert einen Vector<Object> zurueck, welcher abwechselnd (oO) Document
 	 * und AppDocument enthaelt passend zu einem Benutzer und Angebot.
 	 * 
 	 * @param aid
 	 *            AID des Angebots.
 	 * @param user
 	 *            Username des Benutzers.
 	 * @return Vector
 	 */
 	public Vector<Object> doVoodoo2nd(int aid, String user) {
 		Account acc = acccon.getAccountByUsername(user);
 		Offer off = offcon.getOfferById(aid);
 		Vector<AppDocument> docs = doccon.getDocumentsByUserAndOffer(acc, off);
 		Vector<Document> docs2 = new Vector<Document>(docs.size());
 		// dieser Vector enth�lt bei geraden Indizes ein Element vom Typ
 		// Document, bei dem
 		// jeweiligen darauffolgenden, ungeraden Index das Pendant zu
 		// diesem, in Form eines AppDocument
 		Vector<Object> customDocs = new Vector<Object>(docs.size() * 2);
 		for (int i = 0; i < docs.size(); i++) {
 			docs2.add(doccon.getDocumentByUID(docs.elementAt(i).getdID()));
 			// Verflixt und zugenäht aber auch, hört auf sys.outs unkommentiert
 			// in den commits zu pushen!!!
 			// System.out.println(docs.elementAt(i).getUsername());
 			// System.out.println(docs2.elementAt(i).getName());
 			customDocs.add(docs2.elementAt(i));
 			customDocs.add(docs.elementAt(i));
 		}
 		return customDocs;
 	}
 
 	/**
 	 * Aktualisiert ein AppDoc in der DB.
 	 * 
 	 * @param username
 	 *            Der Benutzername des Bewerbers des AppDocs.
 	 * @param offerid
 	 *            Die AngebotsID (aka AID).
 	 * @param docid
 	 *            Die ID des Dokuments.
 	 * @return Flag fuer fehler.
 	 */
 	public boolean updateAppDoc(String username, int offerid, int docid) {
 		AppDocument appdoc = doccon.getDocumentByUsernameAIDandUID(username,
 				offerid, docid);
 		if (appdoc.getPresent()) {
 			appdoc.setPresent(false);
 		} else {
 			appdoc.setPresent(true);
 		}
 		return doccon.updateAppDocument(appdoc);
 	}
 
 	/**
 	 * Liest die Informationen eines Bewerbers zu einen Angebot aus der
 	 * Datenbank.
 	 * 
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @param user
 	 *            Der Bewerber.
 	 * @return JSON-Objekt der Informationen.
 	 */
 	public String getApplicantInfo(int aid, String user) {
 		String realName = acccon.getAccountByUsername(user).getName();
 		String offerName = offcon.getOfferById(aid).getName();
 		return Helper.jsonAtor(new String[] { "realName", "offerName" },
 				new Object[] { realName, offerName });
 	}
 
 	/**
 	 * Loescht ein AppDoc aus der Datenbank.
 	 * 
 	 * @param username
 	 *            Der Benutzername des zum Dokument gehoerenden Bewerbers.
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @param uid
 	 *            Die UID des Dokuments.
 	 * @return Flag fuer Fehler.
 	 */
 	public boolean deleteAppDoc(String username, int aid, int uid) {
 		AppDocument appdoc = doccon.getDocumentByUsernameAIDandUID(username,
 				aid, uid);
 		return doccon.deleteAppDocument(appdoc);
 	}
 
 	/**
 	 * Gibt einige Informationen ueber den eigenen Account als JSON-Objekt
 	 * zurueck.
 	 * 
 	 * @return JSON-Objekt der Informationen.
 	 */
 	public String getJSONAccountInfo() {
 		String realName = getUserData().getName();
 		String email = getUserData().getEmail();
 		String rep = getUserData().getRepresentant();
 		return Helper.jsonAtor(new String[] { "realName", "email", "rep" },
 				new String[] { realName, email, rep });
 	}
 
 	/**
 	 * Gibt die Stellvertreter eines Sachbearbeiters zurueck.
 	 * 
 	 * @return Vektor mit Benutzernamen der Stellvertreter.
 	 */
 	public Vector<String> loadRepresentatives() {
 		String username = getUserData().getUsername();
 		return acccon.getPotentialRepresentatives(username);
 	}
 
 	/**
 	 * Loescht ein AngebotsDokument aus der Datenbank.
 	 * 
 	 * @param uid
 	 *            Die UID des Dokuments.
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @return Flag fuer Fehler.
 	 */
 	public boolean deleteOfferDoc(int uid, int aid) {
 		return doccon.deleteOfferDocument(new OfferDocument(aid, uid));
 	}
 
 	/**
 	 * Erstellt ein AngebotsDokument in der Datenbank.
 	 * 
 	 * @param uid
 	 *            Die UID des Dokuments.
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @return Flag fuer Fehler.
 	 */
 	public boolean addOfferDoc(int uid, int aid) {
 		return doccon.createOfferDocument(new OfferDocument(aid, uid));
 	}
 
 	/**
 	 * Fuegt ein AppDoc in die Datenbank hinzu,
 	 * 
 	 * @param username
 	 *            Der Benutzername des Bewerbers.
 	 * @param uid
 	 *            Die UID des Dokuments.
 	 * @param aid
 	 *            Die AID des Angebots.
 	 * @return Flag fuer Fehler.
 	 */
 	public boolean addAppDoc(String username, int uid, int aid) {
 		return doccon.createAppDocument(new AppDocument(username, aid, uid,
 				false));
 	}
 
 	/**
 	 * Liest Informationen bezueglich Angboten aus der Datenbank als
 	 * JSON-Objekt.
 	 * 
 	 * @return Das JSON-Objekt mit den Informationen.
 	 */
 	public String getOfferInfo() {
 		Vector<Offer> off = new Vector<Offer>();
 		Vector<Integer> institutes = instcon
 				.getAllRepresentingInstitutes(getUserData().getUsername());
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
 		return Helper.jsonAtor(new String[] { "offers", "apps" }, new Object[] {
 				unchecked, apps });
 	}
 
 	/**
 	 * Holt die Emailaddresse eines Benutzers aus der Datenbank.
 	 * 
 	 * @param user
 	 *            Der Benutzer dessen Email gefragt ist.
 	 * @return Die Emailaddresse.
 	 */
 	public String getEmail(String user) {
 		try {
 			return acccon.getAccountByUsername(user).getEmail();
 		} catch (NullPointerException e) {
 			log.write("ClerkServlet", "Error getting e-mail adress of user <"
 					+ user + ">");
 			return null;
 		}
 	}
 }
