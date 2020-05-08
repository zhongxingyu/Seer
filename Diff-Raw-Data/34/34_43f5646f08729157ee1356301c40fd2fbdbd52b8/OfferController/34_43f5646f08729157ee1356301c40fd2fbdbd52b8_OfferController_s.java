 /**
  * @author Patryk Boczon
  * @author Oemer Sahin
  * @author Manuel Guentzel
  * @author Tamino Hartmann
  * @author Laura Irlinger
  */
 
 package database.offer;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Random;
 import java.util.Vector;
 
 import logger.Log;
 import mail.Mailer;
 import database.DatabaseController;
 import database.account.Account;
 import database.application.Application;
 import database.application.ApplicationController;
 
 /**
  * Verwaltet alle Datenbankzugriffe auf Angebotsbezogene Daten.
  */
 public class OfferController {
 
 	/**
 	 * Private Instanz des Loggers.
 	 */
 	private Log log;
 
 	/**
 	 * Diese Methode prueft ob ein OfferController-Objekt existiert. Falls nicht
 	 * wird eine neue OfferController-Instanz angelegt, zurueckgegeben und in
 	 * dem Klassenattribut "offcontr" abgespeichert. Dies dient zur
 	 * Gewaehrleistung, dass nur eine Instanz von OfferController existiert.
 	 * 
 	 * @return Es wird eine Instanz zurueckgegeben.
 	 */
 	public static OfferController getInstance() {
 		if (offcontr == null)
 			offcontr = new OfferController();
 		return offcontr;
 	}
 
 	/**
 	 * Privater Konstruktor, da die Klasse selbst ein Singleton ist.
 	 */
 	private OfferController() {
 		db = DatabaseController.getInstance();
 		log = Log.getInstance();
 		mail = Mailer.getInstance();
 		log.write("OfferController", "Instance created.");
 	}
 
 	/**
 	 * Klassenattribut "offcontr" beinhaltet eine OfferController-Instanz, falls
 	 * keine vorhanden war und mit der Methode getInstance angelegt wird. Dies
 	 * dient zur Gewaehrleistung, dass nur eine Instanz von OfferController
 	 * existiert.
 	 */
 	private static OfferController offcontr;
 
 	/**
 	 * Attribut db ist eine DatabaseController Instanz und wird fuer den
 	 * Datenbankzugang benoetigt.
 	 */
 	private DatabaseController db;
 	/**
 	 * Instanz der Mailer-Klasse.
 	 */
 	private Mailer mail;
 
 	/**
 	 * Name der Tabelle
 	 */
 	final static String tableName = "Angebote";// tabellenname
 
 	/**
 	 * Diese Methode erstellt ein neues Jobangebot in der Datenbank mit Daten
 	 * aus dem uebergebenen Offer-Objekt.
 	 * 
 	 * @param offer
 	 *            Parameter "offer" ist ein Offer-Objekt mit allen noetigen
 	 *            Attributen.
 	 * @return Gibt an, ob die Operation erfolgreich war.
 	 */
 	public boolean createOffer(Offer offer) {
 
 		// Object[] values = { offer.getAid(), offer.getAuthor(),
 		// offer.getName(),
 		// offer.getNote(), offer.isChecked(), offer.getSlots(),
 		// offer.getHoursperweek(), offer.getDescription(),
 		// offer.getStartdate(), offer.getEnddate(), offer.getWage(),
 		// offer.getInstitute(), offer.getModificationdate() };
 
 		Object[] values = { offer.getAid(), offer.getAuthor(), offer.getName(),
 				offer.getNote(), offer.isChecked(), offer.getSlots(),
 				offer.getHoursperweek(), offer.getDescription(),
 				offer.getStartdate(), offer.getEnddate(), offer.getWage(),
 				offer.getInstitute(), offer.getModificationdate(),
 				offer.isFinished() };
 		return db.insert(tableName, values);
 	}
 
 	/**
 	 * Diese Methode loescht ein Jobangebot aus der Datenbank, welches mit den
 	 * Daten des uebergebenem Offer-Objekts uebereinstimmt. Alle Bewerber auf
 	 * dieses Angebot werden informiert.
 	 * 
 	 * @param offer
 	 *            Parameter "offer" ist ein Offer-Objekt mit allen noetigen
 	 *            Attributen. Uebergebene Instanz wird komplett vom System
 	 *            entfernt.
 	 * @return Gibt an, ob die Operation erfolgreich war.
 	 */
 	public boolean deleteOffer(Offer offer) {
 
 		// Hier ist die Mail benachrichtigung:
 		ResultSet rs = db.select(new String[] { "acc.email" }, new String[] {
 				"Accounts as acc", "Bewerbungen as b", "Angebote as a" },
 				"a.AID=b.AID AND b.benutzername=acc.benutzername AND a.AID="
 						+ offer.getAid() + " AND a.abgeschlossen=false");
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't delete offer");
 			return false;
 		}
 		try {
 			while (rs.next()) {
 				String addi = rs.getString(1);
 				mail.sendMail(
 						addi,
 						"Warnung: Angebot \"" + offer.getName()
 								+ "\" wurde gelöscht!",
 						"Dies ist eine automatische Mitteilung, dass das Angebot \""
 								+ offer.getName()
 								+ "\" aus der Hiwi-Börse entfernt wurde. \nIhre Bewerbung wurde daraufhin automatisch gelöscht.");
 			}
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error during mailnotification while deleting Offer "
 							+ offer.getAid());
 		}
 		boolean retBool = true;
 		// Ende benachrichtigung und anfang löschen
 		retBool &= db.delete("Bewerbungsunterlagen", "AID=" + offer.getAid());
 		retBool &= db.delete("Standardunterlagen", "AID=" + offer.getAid());
 		retBool &= db.delete("Bewerbungen", "AID=" + offer.getAid());
 		retBool &= db.delete(tableName, "AID=" + offer.getAid());
 		return retBool;
 	}
 
 	/**
 	 * Diese Methode aendert die Attribute eines Jobangebots beziehungsweise
 	 * aktualisiert diese in der Datenbank.
 	 * 
 	 * @param offer
 	 *            Parameter "offer" ist ein Offer-Objekt mit allen
 	 *            dazugehoerigen Attributen.
 	 * @return Gibt an, ob die Operation erfolgreich war.
 	 */
 	public boolean updateOffer(Offer offer) {
 
 		String[] columns = { "Ersteller", "Name", "Notiz", "Geprueft",
 				"Plaetze", "Stundenprowoche", "Beschreibung", "Beginn", "Ende",
 				"Stundenlohn", "Institut", "aenderungsdatum", "abgeschlossen" };
 
 		java.util.Date aenderungsdatum_1 = new java.util.Date();
 		java.sql.Date aenderungsdatum = new java.sql.Date(
 				aenderungsdatum_1.getTime());
 		offer.setModificationdate(aenderungsdatum);
 
 		Object[] values = { offer.getAuthor(), offer.getName(),
 				offer.getNote(), offer.isChecked(), offer.getSlots(),
 				offer.getHoursperweek(), offer.getDescription(),
 				offer.getStartdate(), offer.getEnddate(), offer.getWage(),
 				offer.getInstitute(), offer.getModificationdate(),
 				offer.isFinished() };
 
 		String where = "AID = " + offer.getAid();
 
 		return (db.update(tableName, columns, values, where));
 
 	}
 	
 	/**
 	 * Diese Methode aendert die Attribute eines Jobangebots beziehungsweise
 	 * aktualisiert diese in der Datenbank und benachrichtigt die Bewerber darueber.
 	 * 
 	 * @param offer
 	 *            Parameter "offer" ist ein Offer-Objekt mit allen
 	 *            dazugehoerigen Attributen.
 	 * @return Gibt an, ob die Operation erfolgreich war.
 	 */
 	public boolean updateOfferWithNotification(Offer offer) {
 		// Hier ist die Mail benachrichtigung:
 		ResultSet rs = db.select(new String[] { "acc.email" }, new String[] {
 				"Accounts as acc", "Bewerbungen as b", "Angebote as a" },
 				"a.AID=b.AID AND b.benutzername=acc.benutzername AND a.AID="
 						+ offer.getAid());
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't update offer");
 			return false;
 		}
 		try {
 			while (rs.next()) {
 				String addi = rs.getString(1);
 				mail.sendMail(
 						addi,
 						"Warnung: Angebot \"" + offer.getName()
 								+ "\" wurde aktualisiert!",
 						"Dies ist eine automatische Mitteilung, dass das Angebot \""
 								+ offer.getName()
 								+ "\" geändert wurde. \nBitte überprüfen sie, ob das Angebot immernoch ihren Vorstellungen entspricht.");
 			}
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error during mailnotification while updating Offer "
 							+ offer.getAid());
 		}
 		// Ende benachrichtigung und anfang updaten.
 
 		String[] columns = { "Ersteller", "Name", "Notiz", "Geprueft",
 				"Plaetze", "Stundenprowoche", "Beschreibung", "Beginn", "Ende",
 				"Stundenlohn", "Institut", "aenderungsdatum", "abgeschlossen" };
 
 		java.util.Date aenderungsdatum_1 = new java.util.Date();
 		java.sql.Date aenderungsdatum = new java.sql.Date(
 				aenderungsdatum_1.getTime());
 		offer.setModificationdate(aenderungsdatum);
 
 		Object[] values = { offer.getAuthor(), offer.getName(),
 				offer.getNote(), offer.isChecked(), offer.getSlots(),
 				offer.getHoursperweek(), offer.getDescription(),
 				offer.getStartdate(), offer.getEnddate(), offer.getWage(),
 				offer.getInstitute(), offer.getModificationdate(),
 				offer.isFinished() };
 
 		String where = "AID = " + offer.getAid();
 
 		return (db.update(tableName, columns, values, where));
 
 	}
 
 
 	/**
 	 * Diese Methode sammelt alle Jobangebote aus der Datenbank und speichert
 	 * diese in einem Vector.
 	 * 
 	 * @return Es wird ein Vector mit allen vorhanden Jobangeboten aus der
 	 *         Datenbank zurueckgegeben.
 	 */
 	public Vector<Offer> getAllOffers() {
 
 		Vector<Offer> offervec = new Vector<Offer>(50, 10);
 
 		String[] select = { "*" };
 		String[] from = { tableName };
 
 		ResultSet rs = db.select(select, from, null);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offervec;
 		}
 		try {
 			while (rs.next()) {
 				Offer currentoff;
 				currentoff = convertToOffer(rs);
 				offervec.add(currentoff);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return offervec;
 	}
 
 	/**
 	 * Diese Methode sammelt alle 2 Jahre alten Jobangebote aus der Datenbank
 	 * und speichert diese in einem Vector.
 	 * 
 	 * @param years
 	 *            Gibt an wie viele Jahre das Angebot zurueckliegen muss um
 	 *            zurueckgegeben zu werden.
 	 * 
 	 * @return Alle Jobangebote in der Datenbank, die bereits 2 Jahre im System
 	 *         waren werden in Form eines Vectors zurueckgegeben.
 	 */
 	public Vector<Offer> getOldOffers(int years) {
 		Vector<Offer> offervec = new Vector<Offer>();
 		String[] select = { "*" };
 		String[] from = { tableName };
 		// War ebenfalls als fehler vermerkt. Mehrfach getestet geht!.
 		String where = "(Year(Now())-YEAR(aenderungsdatum)) >= " + years;
 		ResultSet rs = db.select(select, from, where);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offervec;
 		}
 		try {
 			while (rs.next()) {
 				// TODO: Stimmt die logik hier so mit isChecked?
 				Offer currentoff = convertToOffer(rs);
 				if (currentoff.isChecked())
 					offervec.add(currentoff);
 			}
 		} catch (SQLException e) {
 			log.write("OfferController", "Error retrieving all old offers!");
 			return null;
 		}
 		return offervec;
 	}
 
 	/**
 	 * Diese Methode sammelt alle ueberprueften Jobangebote aus der Datenbank
 	 * und speichert diese in einem Vector.
 	 * 
 	 * @return Alle Jobangebote in der Datenbank, die dem Wert von "checked"
 	 *         entsprechen werden in Form eines Vectors zurueckgegeben.
 	 */
 	// TODO: Methode wirft NullPointer!
 	public Vector<Offer> getCheckedOffers() {
 		Vector<Offer> offervec = new Vector<Offer>();
 		String[] select = { "*" };
 		String[] from = { tableName };
 		// War ebenfalls als fehler vermerkt. Mehrfach getestet geht!.
 		String where = "Geprueft = 1 and abgeschlossen = 0";
 		ResultSet rs = db.select(select, from, where);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offervec;
 		}
 		try {
 			while (rs.next()) {
 				offervec.add(convertToOffer(rs));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			log.write("OfferController", "Error retrieving all checked offers!");
 			return null;
 		}
 		return offervec;
 	}
 
 	/**
 	 * Diese Methode sammelt alle Jobangebote eines Providers aus der Datenbank
 	 * und speichert diese in einem Vector.
 	 * 
 	 * @param string
 	 *            Parameter gibt den benoetigten Provider an
 	 * 
 	 * @return Es wird ein Vector mit allen Jobangeboten eines Providers aus der
 	 *         Datenbank zurueckgegeben.
 	 */
 	public Vector<Offer> getOffersByProvider(String string) {
 		Vector<Offer> offervec = new Vector<Offer>(50, 10);
 		String[] select = { "*" };
 		String[] from = { tableName };
 		String where = "abgeschlossen=0 and Ersteller = '" + string + "'";
 		ResultSet rs = db.select(select, from, where);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offervec;
 		}
 		try {
 			while (rs.next()) {
 				Offer currentoff = convertToOffer(rs);
 				offervec.add(currentoff);
 			}
 		} catch (SQLException e) {
 			log.write("OfferController", "Error reading free offers!");
 			return null;
 		}
 		return offervec;
 	}
 
 	/**
 	 * Diese Methode sammelt alle nicht abgeschlossenen Jobangebote, fuer die
 	 * sich ein Bewerber beworben hat, aus der Datenbank und speichert diese in
 	 * einem Vector.
 	 * 
 	 * @param username
 	 *            Bewerber von die Bewerbungen gesucht werden
 	 * @return Es wird ein Vector mit allen nicht abgeschlossenen Jobangeboten
 	 *         zurueckgegeben, auf die sich ein Bewerber beworben hat.
 	 */
 	public Vector<Offer> getOffersByApplicant(String username) {
 
 //		Vector<Application> applications = ApplicationController.getInstance()
 //				.getApplicationsByApplicant(username);
 		Vector<Offer> offervec = new Vector<Offer>(5,2);
 		String[] select = { "Angebote.*" };
 		String[] from = { tableName, "Bewerbungen" };
 		String where = "Angebote.AID = Bewerbungen.AID AND Bewerbungen.benutzername = '"+username+"' AND Bewerbungen.status = FALSE";
 		
 		ResultSet rs = db.select(select, from, where);
 		if (rs == null) {
 			log.write("OfferController",
 					"No connection: couldn't get offers");
 			return offervec;
 		}
 		try {
 			while (rs.next()) {
 				Offer currentoff = convertToOffer(rs);
				if (currentoff.isChecked())
 					offervec.add(currentoff);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
 //		for (int i = 0; i < applications.size(); i++) {
 //			// Ich weis nicht wieso das bei dem Test schief ging ? Habs getestet
 //			// und es geht.
 //			where = "Bewerbungen.status = 0 AND Angebote.AID = "
 //					+ applications.elementAt(i).getAid();
 //			ResultSet rs = db.select(select, from, where);
 //			if (rs == null) {
 //				log.write("OfferController",
 //						"No connection: couldn't get offers");
 //				return offervec;
 //			}
 //			try {
 //				while (rs.next()) {
 //					Offer currentoff = convertToOffer(rs);
 //					if (currentoff.isChecked())
 //						offervec.add(currentoff);
 //				}
 //			} catch (SQLException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 
 		return offervec;
 	}
 
 	/**
 	 * Liest ein Angebot anhand der AID aus der Datenbank aus.
 	 * 
 	 * @param ID
 	 *            ID des Angebots
 	 * @return Gibt das gesuchte Angebot zurueck
 	 */
 	public Offer getOfferById(int ID) {
 		String[] select = { "*" };
 		String[] from = { tableName };
 		String where = "AID = " + ID;
 
 		ResultSet rs = db.select(select, from, where);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offer");
 			return null;
 		}
 
 		try {
 			if (rs.next()) {
 				return convertToOffer(rs);
 			} else
 				return null;
 		} catch (SQLException e) {
 			logger.Log.getInstance().write("OfferController",
 					"Error while reading Offer from Database");
 		}
 		return null;
 	}
 
 	/**
 	 * Liest alle zugehoerigen Angebote eines Clerk-Accounts aus der DB. Ist der
 	 * Clerk dem Institut 0 (default) zugeordnet, werden ALLE ungeprueften
 	 * Angebote unabhaengig des Instituts ausgelesen.
 	 * 
 	 * @param account
 	 *            Der account wessen angebote gelesen werden sollen. Ist
 	 *            accounttyp != 3 (also kein Clerk) wird null zurueckgeben!
 	 * @return Alle ungeprueften Angebote des entsprechenden Instituts.
 	 */
 	// DONE: Institut = 0 universal already implemented!
 	public Vector<Offer> getUncheckedOffersByClerk(Account account) {
 		// If not clerk:
 		if (account.getAccounttype() != Account.VERWALTER) {
 			log.write("OfferController",
 					"Illegal access tried in getUncheckedOffersByClerk()!");
 			return null;
 		}
 
 		Vector<Offer> offers = new Vector<Offer>();
 		// Implement that institut 0 is universal:
 		ResultSet rs;
 
 		if (account.getInstitute() == 0) {
 
 			rs = db.select(new String[] { "*" }, new String[] { tableName },
 					"abgeschlossen=0 and Geprueft=0");
 			if (rs == null) {
 				log.write("OfferController",
 						"No connection: couldn't get offers");
 				return offers;
 			}
 			// TODO: fehlt hier nicht was? was ist, wenn rs != null ?!
 		} else {
 			// Institut in (accountInstitut, 0) secures that Offers of Institut
 			// 0 are universally seeable.
 
 			rs = db.select(new String[] { "*" }, new String[] { tableName },
 					"abgeschlossen = 0 and Geprueft=0 AND Institut IN ("
 							+ account.getInstitute() + ",0)");
 			if (rs == null) {
 				log.write("OfferController",
 						"No connection: couldn't get offers");
 				return offers;
 			}
 
 		}
 		try {
 			while (rs.next()) {
 				offers.add(convertToOffer(rs));
 			}
 			return offers;
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error reading ResultSet in getUncheckedOffersByClerk()!");
 			return null;
 		}
 	}
 
 	/**
 	 * Liest alle zugehoerigen Angebote eines Institutes aus der DB.
 	 * 
 	 * @param institute
 	 *            Nummer des Instituts
 	 * @return Alle ungeprueften Angebote des entsprechenden Instituts.
 	 */
 	public Vector<Offer> getUncheckedOffersByInstitute(int institute) {
 
 		Vector<Offer> offers = new Vector<Offer>();
 		ResultSet rs;
 
 		rs = db.select(new String[] { "*" }, new String[] { tableName },
 				"Geprueft=0 AND Institut =" + institute);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offers;
 		}
 
 		try {
 			while (rs.next()) {
 				offers.add(convertToOffer(rs));
 			}
 			return offers;
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error reading ResultSet in getUncheckedOffersByClerk()!");
 			return null;
 		}
 	}
 
 	/**
 	 * Eine neue Id wird generiert
 	 * 
 	 * @param tablename
 	 *            Name der Tabelle
 	 * @return generated ID
 	 */
 	public int getNewOffID(String tablename) {
 		int newID = 0;
 		boolean check = false;
 		int count = 0;// um endlosschleife zu vermeiden
 
 		java.util.Date startdatum_1 = new java.util.Date();
 		java.sql.Date startdatum = new java.sql.Date(startdatum_1.getTime());
 
 		// Damit der Log logisch erscheint:
 		log.write("OfferController",
 				"Searching for free AID, INSERT errors can happen! This is wanted.");
 
 		while (!check) {
 			if (count == 100) {
 				log.write("OfferController",
 						"Error while generating random AID after " + count
 								+ " attempts. Process was aborted...");
 				return -1;
 			}
 
 			newID = generateRandomNr(1, 10);
 			Object[] data = { newID, "", "", "", false, 0, 0, "", startdatum,
 					startdatum, 0, 0, startdatum, false };
 			check = db.insert(tablename, data);
 			count++;
 		}
 		db.delete(tablename, "AID= " + newID);
 		return newID;
 	}
 
 	/**
 	 * Gibt die Gesamtzahl der Stellen eines Angebots zur�ck.
 	 * 
 	 * @param aid
 	 *            Aid des Angebots
 	 * @return Gesamtzahl der Stellen des Angebots
 	 */
 	public int getTotalSlotsOfOffer(int aid) {
 
 		int total = 0;
 		Offer off = getOfferById(aid);
 		int free = off.getSlots();
 		int taken = 0;
 		Vector<Application> apps = ApplicationController.getInstance()
 				.getApplicationsByOffer(aid);
 		for (int i = 0; i < apps.size(); i++) {
 			if (apps.elementAt(i).isChosen()) {
 				taken++;
 			}
 		}
 
 		total = free + taken;
 
 		return total;
 	}
 
 	/**
 	 * Genrierung einer Zufallszahl.
 	 * 
 	 * @param aStart
 	 *            Start der Zahl.
 	 * @param aEnd
 	 *            Ende der Zahl.
 	 * @return generierte Zahl.
 	 */
 	private int generateRandomNr(int aStart, int aEnd) {
 		Random random = new Random();
 		// get the range, casting to long to avoid overflow problems
 		long range = (long) aEnd - (long) aStart + 1;
 		// compute a fraction of the range, 0 <= frac < range
 		long fraction = (long) (range * random.nextDouble());
 		int randomNumber = (int) (fraction + aStart);
 		return randomNumber;
 	}
 
 	/**
 	 * Liest alle Angebote von einem Institut aus.
 	 * 
 	 * @param iid
 	 *            Die ID des Institutes.
 	 * 
 	 * @return Alle Angebote die zu einem Institut gehoeren.
 	 */
 	public Vector<Offer> getOffersByInstitute(int iid) {
 		Vector<Offer> offers = new Vector<Offer>();
 		ResultSet rs;
 		rs = db.select(new String[] { "*" }, new String[] { tableName },
 				"Institut =" + iid);
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offers;
 		}
 		try {
 			while (rs.next()) {
 				offers.add(convertToOffer(rs));
 			}
 			return offers;
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error reading ResultSet in getOffersByInstitute()!");
 			return null;
 		}
 	}
 
 	/**
 	 * Hilfsmethode welche aus einem ResultSet ein offer castet.
 	 * 
 	 * @param rs
 	 *            Das ResultSet.
 	 * @return Das Offer.
 	 * @throws SQLException
 	 */
 	private Offer convertToOffer(ResultSet rs) throws SQLException {
 		if (rs == null)
 			throw new SQLException("ResultSet is NULL!");
 		return new Offer(rs.getInt("AID"), rs.getString("Ersteller"),
 				rs.getString("Name"), rs.getString("Notiz"),
 				rs.getBoolean("Geprueft"), rs.getInt("Plaetze"),
 				rs.getDouble("Stundenprowoche"), rs.getString("Beschreibung"),
 				rs.getDate("Beginn"), rs.getDate("Ende"),
 				rs.getDouble("Stundenlohn"), rs.getInt("Institut"),
 				rs.getDate("aenderungsdatum"), rs.getBoolean("abgeschlossen"));
 	}
 
 	/**
 	 * Gibt alle Angebote zurueck auf die sich ein Benutzer noch bewerben
 	 * koennte.
 	 * 
 	 * @param username
 	 *            Benutzername
 	 * @return Vektor mir Angeboten
 	 */
 	public Vector<Offer> getPossibleOffers(String username) {
 		Vector<Offer> offers = new Vector<Offer>();
 		ResultSet rs = db
 				.select(new String[] { "*" },
 						new String[] { "Angebote" },
 						"abgeschlossen = false and geprueft = true and AID not in (Select a.aid from Bewerbungen as b,Angebote as a where b.aid = a.aid and b.benutzername = '"
 								+ username + "')");
 		if (rs == null) {
 			log.write("OfferController", "No connection: couldn't get offers");
 			return offers;
 		}
 		try {
 			while (rs.next()) {
 				offers.add(convertToOffer(rs));
 			}
 			return offers;
 		} catch (SQLException e) {
 			log.write("OfferController",
 					"Error reading ResultSet in getPossibleOffers()!");
 			return null;
 		}
 	}
 }
