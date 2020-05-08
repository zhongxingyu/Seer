 package Zugriffsschicht;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import Com.ComStatistik;
 
 import jdbc.JdbcAccess;
 
 public class OrgaEinheit {
 	private JdbcAccess db;
 	private Zugriffschicht dbZugriff;
 	private int idOrgaEinheit;
 	private int idUeberOrgaEinheit;
 	private String OrgaEinheitBez;
 	private String Leitername;
 	private int idLeiterBerechtigung;
 	private boolean zustand;
 	private int idMitarbeiterBerechtigung;
 	private String OrgaEinheitTyp;
 
 	//Konstruktor ber ein ResultSet. Abfrage muss dann vorher schon stattgefunden haben.
 	public OrgaEinheit(ResultSet resultSet, JdbcAccess db,
 			Zugriffschicht dbZugriff) throws SQLException {
 		werteSetzen(resultSet);
 		this.db = db;
 		this.dbZugriff = dbZugriff;
 	}
 
 	//Konstruktor, der eine neue OrgaEinheit erstellt. Wirft eine Exception wenn etwas
 	//nicht funktioniert hat.
 	public OrgaEinheit(int idUeberOrgaEinheit, String OrgaEinheitBez,
 			String Leitername, boolean zustand, String OrgaEinheitTyp,
 			JdbcAccess db, Zugriffschicht dbZugriff) throws SQLException {
 		this.db = db;
 		this.dbZugriff = dbZugriff;
 		String stringZustand;
 		if (zustand)
 			stringZustand = "1";
 		else
 			stringZustand = "0";
 		if (Leitername == null) {
 			db.executeUpdateStatement("INSERT INTO OrgaEinheiten ("
 					+ "idUeberOrgaEinheit, OrgaEinheitBez, Zustand, OrgaEinheitTyp) "
 					+ "VALUES (" + idUeberOrgaEinheit + ", '" + OrgaEinheitBez
 					+ "', " + stringZustand + ", '" + OrgaEinheitTyp + "')");
 			ResultSet resultSet = db
 					.executeQueryStatement("SELECT * FROM OrgaEinheiten NATURAL JOIN OrgaEinheitTyp WHERE "
 							+ "idUeberOrgaEinheit = "
 							+ idUeberOrgaEinheit
 							+ " AND "
 							+ "OrgaEinheitBez = '"
 							+ OrgaEinheitBez
 							+ "' AND "
 							+ "Zustand = "
 							+ stringZustand
 							+ " AND "
 							+ "OrgaEinheitTyp = '" + OrgaEinheitTyp + "'");
 			resultSet.next();
 			werteSetzen(resultSet);
 			resultSet.close();
 		}
 
 		else {
 			db.executeUpdateStatement("INSERT INTO OrgaEinheiten ("
 					+ "idUeberOrgaEinheit, OrgaEinheitBez, Leitername,  Zustand, OrgaEinheitTyp) "
 					+ "VALUES (" + idUeberOrgaEinheit + ", '" + OrgaEinheitBez
 					+ "', '" + Leitername + "', " + stringZustand + ", '"
 					+ OrgaEinheitTyp + "')");
 			ResultSet resultSet = db
 					.executeQueryStatement("SELECT * FROM OrgaEinheiten NATURAL JOIN OrgaEinheitTyp WHERE "
 							+ "idUeberOrgaEinheit = "
 							+ idUeberOrgaEinheit
 							+ " AND "
 							+ "OrgaEinheitBez = '"
 							+ OrgaEinheitBez
 							+ "' AND "
 							+ "Leitername = '"
 							+ Leitername
 							+ "' AND "
 							+ "Zustand = "
 							+ stringZustand
 							+ " AND "
 							+ "OrgaEinheitTyp = '" + OrgaEinheitTyp + "'");
 			resultSet.next();
 			werteSetzen(resultSet);
 			resultSet.close();
 		}
 	}
 
 	//Liest die Werte aus dem ResultSet und setzt die Werte entsprechend.
 	private void werteSetzen(ResultSet resultSet) throws SQLException {
 		this.idOrgaEinheit = resultSet.getInt("idOrgaEinheit");
 		this.idUeberOrgaEinheit = resultSet.getInt("idUeberOrgaEinheit");
 		this.OrgaEinheitBez = resultSet.getString("OrgaEinheitBez");
 		this.Leitername = resultSet.getString("Leitername");
 		this.idLeiterBerechtigung = resultSet.getInt("Leiterberechtigung");
 		this.zustand = resultSet.getBoolean("Zustand");
 		this.idMitarbeiterBerechtigung = resultSet
 				.getInt("Mitarbeiterberechtigung");
 		this.OrgaEinheitTyp = resultSet.getString("OrgaEinheitTyp");
 	}
 
 	public int getIdOrgaEinheit() {
 		return idOrgaEinheit;
 	}
 
 	public int getIdUeberOrgaEinheit() {
 		return idUeberOrgaEinheit;
 	}
 
 	public String getOrgaEinheitBez() {
 		return OrgaEinheitBez;
 	}
 
 	public String getLeitername() {
 		return Leitername;
 	}
 
 	public int getIdLeiterBerechtigung() {
 		return idLeiterBerechtigung;
 	}
 
 	public boolean isZustand() {
 		return zustand;
 	}
 
 	public int getIdMitarbeiterBerechtigung() {
 		return idMitarbeiterBerechtigung;
 	}
 
 	public String getOrgaEinheitTyp() {
 		return OrgaEinheitTyp;
 	}
 
 	//Alle Setter Methoden ndern zuerst den Wert in der Datenbank und nur wenn das geklappt hat
 	//auch den Wert in diesem Objekt. False wenn ein Fehler aufgetreten ist.
 	public boolean setOrgaEinheitBez(String orgaEinheitBez) {
 		try {
 			db.executeUpdateStatement("UPDATE OrgaEinheiten SET orgaEinheitBez = '"
 					+ orgaEinheitBez
 					+ "' WHERE idOrgaEinheit = "
 					+ idOrgaEinheit);
 			OrgaEinheitBez = orgaEinheitBez;
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setIdUeberOrgaEinheit(int idueberOrgaEinheit) {
 		try {
 			db.executeUpdateStatement("UPDATE OrgaEinheiten SET idUeberOrgaEinheit = "
 					+ idueberOrgaEinheit
 					+ " WHERE idOrgaEinheit = "
 					+ idOrgaEinheit);
 			idUeberOrgaEinheit = idueberOrgaEinheit;
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setLeitername(String leitername) {
 		try {
 			db.executeUpdateStatement("UPDATE OrgaEinheiten SET Leitername = '"
 					+ leitername + "' WHERE idOrgaEinheit = " + idOrgaEinheit);
 			Leitername = leitername;
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setIdLeiterBerechtigung(int idLeiterBerechtigung) {
 		try {
 			db.executeUpdateStatement("UPDATE OrgaEinheiten SET idLeiterBerechtigung = "
 					+ idLeiterBerechtigung
 					+ " WHERE idOrgaEinheit = "
 					+ idOrgaEinheit);
 			this.idLeiterBerechtigung = idLeiterBerechtigung;
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setZustand(boolean neuerZustand) {
 		try {
 			String stringZustand;
 			if (neuerZustand)
 				stringZustand = "1";
 			else
 				stringZustand = "0";
 			db.executeUpdateStatement("UPDATE OrgaEinheiten SET Zustand = "
 					+ stringZustand + " WHERE idOrgaEinheit = " + idOrgaEinheit);
 			zustand = neuerZustand;
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public void setIdMitarbeiterBerechtigung(int idMitarbeiterBerechtigung)
 			throws SQLException {
 		db.executeUpdateStatement("UPDATE OrgaEinheiten SET idMitarbeiterBerechtigung = "
 				+ idMitarbeiterBerechtigung
 				+ " WHERE idOrgaEinheit = "
 				+ idOrgaEinheit);
 		this.idMitarbeiterBerechtigung = idMitarbeiterBerechtigung;
 	}
 
 	//Gibt alle Striche aufaddiert der bergebenen strichart in der bergebenen kalendarwoche und jahr zurck.
 	//Holt die Werte aus der Tabelle Arbeitsschritte.
 	public int getAlleStricheInWoche(int kalendarwoche, int jahr,
 			int idStrichart) {
 		try {
 			ResultSet result = db
 					.executeQueryStatement("SELECT SUM(Strichzahl) FROM Arbeitsschritte "
 							+ "GROUP BY idOrgaEinheit, idStrichart, Kalendarwoche, Jahr HAVING "
 							+ "idOrgaEinheit = "
 							+ idOrgaEinheit
 							+ " AND "
 							+ "idStrichart = "
 							+ idStrichart
 							+ " AND "
 							+ "Kalendarwoche = "
 							+ kalendarwoche
 							+ " AND "
 							+ "Jahr = " + jahr);
 			if (result.next())
 				return result.getInt(1);
 			else
 				return 0;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 	}
 
 	//Gibt eine Liste mit allen OrgaEinheiten, die diese als UeberOrgaEinheit gespeichert haben.
 	public List<OrgaEinheit> getUnterOrgaEinheiten() {
 		ResultSet resultSet;
 		List<OrgaEinheit> rueckgabe = new ArrayList<OrgaEinheit>();
 		try {
 			resultSet = db
 					.executeQueryStatement("SELECT * FROM OrgaEinheiten NATURAL JOIN OrgaEinheitTyp WHERE idUeberOrgaEinheit = "
 							+ this.idOrgaEinheit);
 			while (resultSet.next()) {
 				rueckgabe.add(new OrgaEinheit(resultSet, db, dbZugriff));
 			}
 			resultSet.close();
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Statistiken, die in der bergebenen kalendarwoche und
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach OrgaEinheit sortiert.
 	public List<ComStatistik> getOrgaEinheitStatistikAusDatenbank(
 			int kalendarwoche, int jahr, int hierarchieStufe,
 			List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		List<Strichart> stricharten = dbZugriff.getAlleStricharten(false);
 		if (OrgaEinheitTyp.equals("Gruppe")) {
 			try {
 				for (Strichart strichart : stricharten) {
 					ResultSet result = db
 							.executeQueryStatement("SELECT * FROM Statistiken WHERE "
 									+ "idOrgaEinheit = "
 									+ idOrgaEinheit
 									+ " AND Kalenderwoche = "
 									+ kalendarwoche
 									+ " AND Jahr = "
 									+ jahr
 									+ " AND idStrichart = "
 									+ strichart.getIdStrichart());
 					if (result.next()) {
 						Statistik stat = new Statistik(result, db);
 						rueckgabe.add(new ComStatistik(idOrgaEinheit,
 								OrgaEinheitBez, kalendarwoche, jahr, stat
 										.getStrichartBez(),
 								stat.getStrichart(), stat.getStrichanzahl(),
 								hierarchieStufe, OrgaEinheitTyp, null));
 					} else {
 						rueckgabe.add(new ComStatistik(idOrgaEinheit,
 								OrgaEinheitBez, kalendarwoche, jahr, strichart
 										.getStrichbez(), strichart
 										.getIdStrichart(), 0, hierarchieStufe,
 								OrgaEinheitTyp, null));
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			for (int i = 0; i < unterOrga.size(); i++) {
 				List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 				rueckgabe.addAll(unterOrga.get(i)
 						.getOrgaEinheitStatistikAusDatenbank(kalendarwoche, jahr,
 								hierarchieStufe + 1, hilfsListe));
 			}
 			List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 			for (int i = 0; i < unterOrga.size(); i++) {
 				idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 			}
 			List<ComStatistik> eigeneStatistiken = new ArrayList<ComStatistik>();
 			for (Strichart strichart : stricharten) {
 				int stricheUnterEinheiten = 0;
 				for (ComStatistik stat : rueckgabe) {
					if (hierarchieStufe==stat.getHierarchiestufe()-1 && stat.getIdStrichBez() == strichart.getIdStrichart()) {
 						stricheUnterEinheiten = stricheUnterEinheiten
 								+ stat.getStrichzahl();
 					}
 				}
 				eigeneStatistiken.add(new ComStatistik(idOrgaEinheit,
 						OrgaEinheitBez, kalendarwoche, jahr, strichart
 								.getStrichbez(), strichart.getIdStrichart(),
 						stricheUnterEinheiten, hierarchieStufe, OrgaEinheitTyp,
 						idUnterOrgaEinheiten));
 			}
 			// eigeneStatistiken in richtiger Reihenfolge oben ausgeben:
 			for (int x = eigeneStatistiken.size() - 1; x >= 0; x--) {
 				rueckgabe.add(0, eigeneStatistiken.get(x));
 			}
 
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Statistiken, die in dem bergebenen
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach OrgaEinheit sortiert.
 	public List<ComStatistik> getJahresOrgaEinheitStatistikAusDatenbank(int jahr,
 			int hierarchieStufe, List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		List<Strichart> stricharten = dbZugriff.getAlleStricharten(false);
 		if (OrgaEinheitTyp.equals("Gruppe")) {
 			try {
 				for (Strichart strichart : stricharten) {
 					ResultSet result = db
 							.executeQueryStatement("SELECT * FROM Statistiken WHERE "
 									+ "idOrgaEinheit = "
 									+ idOrgaEinheit
 									+ " AND Jahr = "
 									+ jahr
 									+ " AND idStrichart = "
 									+ strichart.getIdStrichart());
 					if (result.next()) {
 						Statistik stat = new Statistik(result, db);
 						rueckgabe.add(new ComStatistik(idOrgaEinheit,
 								OrgaEinheitBez, 0, jahr,
 								stat.getStrichartBez(), stat.getStrichart(),
 								stat.getStrichanzahl(), hierarchieStufe,
 								OrgaEinheitTyp, null));
 					} else {
 						rueckgabe.add(new ComStatistik(idOrgaEinheit,
 								OrgaEinheitBez, 0, jahr, strichart
 										.getStrichbez(), strichart
 										.getIdStrichart(), 0, hierarchieStufe,
 								OrgaEinheitTyp, null));
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			for (int i = 0; i < unterOrga.size(); i++) {
 				List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 				rueckgabe.addAll(unterOrga.get(i)
 						.getJahresOrgaEinheitStatistikAusDatenbank(jahr,
 								hierarchieStufe + 1, hilfsListe));
 			}
 			List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 			for (int i = 0; i < unterOrga.size(); i++) {
 				idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 			}
 			List<ComStatistik> eigeneStatistiken = new ArrayList<ComStatistik>();
 			for (Strichart strichart : stricharten) {
 				int stricheUnterEinheiten = 0;
 				for (ComStatistik stat : rueckgabe) {
					if (hierarchieStufe==stat.getHierarchiestufe()-1 && stat.getIdStrichBez() == strichart.getIdStrichart()) {
 						stricheUnterEinheiten = stricheUnterEinheiten
 								+ stat.getStrichzahl();
 					}
 				}
 				eigeneStatistiken.add(new ComStatistik(idOrgaEinheit,
 						OrgaEinheitBez, 0, jahr, strichart.getStrichbez(),
 						strichart.getIdStrichart(), stricheUnterEinheiten,
 						hierarchieStufe, OrgaEinheitTyp, idUnterOrgaEinheiten));
 			}
 			// eigeneStatistiken in richtiger Reihenfolge oben ausgeben:
 			for (int x = eigeneStatistiken.size() - 1; x >= 0; x--) {
 				rueckgabe.add(0, eigeneStatistiken.get(x));
 			}
 
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Arbeitsschritte, die in der bergebenen Kalendarwoche und
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach OrgaEinheit sortiert.
 	public List<ComStatistik> getTemporaereOrgaEinheitStatistik(int kalendarwoche,
 			int jahr, int hierarchieStufe, List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		List<Strichart> stricharten = dbZugriff.getAlleStricharten(false);
 		if (OrgaEinheitTyp.equals("Gruppe")) {
 			for (Strichart strichart : stricharten) {
 				int anzahlStriche = getAlleStricheInWoche(kalendarwoche, jahr,
 						strichart.getIdStrichart());
 				rueckgabe.add(new ComStatistik(idOrgaEinheit, OrgaEinheitBez,
 						kalendarwoche, jahr, strichart.getStrichbez(),
 						strichart.getIdStrichart(), anzahlStriche,
 						hierarchieStufe, OrgaEinheitTyp, null));
 			}
 		} else {
 			for (int i = 0; i < unterOrga.size(); i++) {
 				List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 				rueckgabe.addAll(unterOrga.get(i)
 						.getTemporaereOrgaEinheitStatistik(kalendarwoche, jahr,
 								hierarchieStufe + 1, hilfsListe));
 			}
 			List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 			for (int i = 0; i < unterOrga.size(); i++) {
 				idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 			}
 			List<ComStatistik> eigeneStatistiken = new ArrayList<ComStatistik>();
 			for (Strichart strichart : stricharten) {
 				int stricheUnterEinheiten = 0;
 				for (ComStatistik stat : rueckgabe) {
					if (hierarchieStufe==stat.getHierarchiestufe()-1 && stat.getIdStrichBez() == strichart.getIdStrichart()) {
 						stricheUnterEinheiten = stricheUnterEinheiten
 								+ stat.getStrichzahl();
 					}
 				}
 				eigeneStatistiken.add(new ComStatistik(idOrgaEinheit,
 						OrgaEinheitBez, kalendarwoche, jahr, strichart
 								.getStrichbez(), strichart.getIdStrichart(),
 						stricheUnterEinheiten, hierarchieStufe, OrgaEinheitTyp,
 						idUnterOrgaEinheiten));
 			}
 			// eigeneStatistiken in richtiger Reihenfolge oben ausgeben:
 			for (int x = eigeneStatistiken.size() - 1; x >= 0; x--) {
 				rueckgabe.add(0, eigeneStatistiken.get(x));
 			}
 
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Statistiken, die in der bergebenen kalendarwoche und
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach Strichart sortiert.
 	public List<ComStatistik> getStrichartStatistikAusDatenbank(int kalendarwoche,
 			int jahr, int idStrichart, String strichBezeichnung,
 			int hierarchieStufe, List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		int stricheUnterEinheiten = 0;
 		for (int i = 0; i < unterOrga.size(); i++) {
 			List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 			rueckgabe.addAll(unterOrga.get(i).getStrichartStatistikAusDatenbank(
 					kalendarwoche, jahr, idStrichart, strichBezeichnung,
 					hierarchieStufe + 1, hilfsListe));
 			for (int x = 0; x < hilfsListe.size(); x++) {
 				ComStatistik statistik = hilfsListe.get(x);
 				if (statistik.getHierarchiestufe() == hierarchieStufe + 1)
 					stricheUnterEinheiten = stricheUnterEinheiten
 							+ statistik.getStrichzahl();
 			}
 		}
 		try {
 			ResultSet result = db
 					.executeQueryStatement("SELECT * FROM Statistiken WHERE "
 							+ "idOrgaEinheit = '" + idOrgaEinheit + "' AND "
 							+ "Kalenderwoche = '" + kalendarwoche + "' AND "
 							+ "Jahr = '" + jahr + "' AND " + "idStrichart = '"
 							+ idStrichart + "'");
 			int strichzahl;
 			if (result.next()) {
 				strichzahl = result.getInt("Strichzahl");
 			} else {
 				strichzahl = 0;
 			}
 			strichzahl = strichzahl + stricheUnterEinheiten;
 			List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 			for (int i = 0; i < unterOrga.size(); i++) {
 				idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 			}
 			rueckgabe.add(0, new ComStatistik(idOrgaEinheit, OrgaEinheitBez,
 					kalendarwoche, jahr, strichBezeichnung, idStrichart,
 					strichzahl, hierarchieStufe, OrgaEinheitTyp,
 					idUnterOrgaEinheiten));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Statistiken, die in dem bergebenen
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach Strichart sortiert.
 	public List<ComStatistik> getJahresStrichartStatistikAusDatenbank(int jahr,
 			int idStrichart, String strichBezeichnung, int hierarchieStufe,
 			List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		int stricheUnterEinheiten = 0;
 		for (int i = 0; i < unterOrga.size(); i++) {
 			List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 			rueckgabe.addAll(unterOrga.get(i).getJahresStrichartStatistikAusDatenbank(
 					jahr, idStrichart, strichBezeichnung, hierarchieStufe + 1,
 					hilfsListe));
 			for (int x = 0; x < hilfsListe.size(); x++) {
 				ComStatistik statistik = hilfsListe.get(x);
 				if (statistik.getHierarchiestufe() == hierarchieStufe + 1)
 					stricheUnterEinheiten = stricheUnterEinheiten
 							+ statistik.getStrichzahl();
 			}
 		}
 		try {
 			ResultSet result = db
 					.executeQueryStatement("SELECT SUM(Strichzahl) AS Strichzahl FROM Statistiken GROUP BY "
 							+ "idOrgaEinheit, Jahr, idStrichart HAVING "
 							+ "idOrgaEinheit = '"
 							+ idOrgaEinheit
 							+ "' AND "
 							+ "Jahr = '"
 							+ jahr
 							+ "' AND "
 							+ "idStrichart = '"
 							+ idStrichart + "'");
 			int strichzahl;
 			if (result.next()) {
 				strichzahl = result.getInt("Strichzahl");
 			} else {
 				strichzahl = 0;
 			}
 			strichzahl = strichzahl + stricheUnterEinheiten;
 			List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 			for (int i = 0; i < unterOrga.size(); i++) {
 				idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 			}
 			rueckgabe.add(0, new ComStatistik(idOrgaEinheit, OrgaEinheitBez, 0,
 					jahr, strichBezeichnung, idStrichart, strichzahl,
 					hierarchieStufe, OrgaEinheitTyp, idUnterOrgaEinheiten));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rueckgabe;
 	}
 
 	//liest alle Statistiken aus der Tabelle Arbeitsschritte, die in der bergebenen Kalendarwoche und
 	//Jahr gespeichert sind. Auch die Statistiken von den unterOrgaEinheiten werden mit bergeben.
 	//Ist nach Strichart sortiert.
 	public List<ComStatistik> getTemporaereStrichartStatistik(int kalendarwoche,
 			int jahr, int idStrichart, String strichBezeichnung,
 			int hierarchieStufe, List<ComStatistik> rueckgabe) {
 		List<OrgaEinheit> unterOrga = getUnterOrgaEinheiten();
 		int stricheUnterEinheiten = 0;
 		for (int i = 0; i < unterOrga.size(); i++) {
 			List<ComStatistik> hilfsListe = new ArrayList<ComStatistik>();
 			rueckgabe.addAll(unterOrga.get(i).getTemporaereStrichartStatistik(
 					kalendarwoche, jahr, idStrichart, strichBezeichnung,
 					hierarchieStufe + 1, hilfsListe));
 			for (int x = 0; x < hilfsListe.size(); x++) {
 				ComStatistik statistik = hilfsListe.get(x);
 				if (statistik.getHierarchiestufe() == hierarchieStufe + 1)
 					stricheUnterEinheiten = stricheUnterEinheiten
 							+ statistik.getStrichzahl();
 			}
 		}
 		int strichzahl = getAlleStricheInWoche(kalendarwoche, jahr, idStrichart);
 		strichzahl = strichzahl + stricheUnterEinheiten;
 		List<Integer> idUnterOrgaEinheiten = new ArrayList<Integer>();
 		for (int i = 0; i < unterOrga.size(); i++) {
 			idUnterOrgaEinheiten.add(unterOrga.get(i).getIdOrgaEinheit());
 		}
 		rueckgabe.add(0, new ComStatistik(idOrgaEinheit, OrgaEinheitBez,
 				kalendarwoche, jahr, strichBezeichnung, idStrichart,
 				strichzahl, hierarchieStufe, OrgaEinheitTyp,
 				idUnterOrgaEinheiten));
 		return rueckgabe;
 	}
 
 }
