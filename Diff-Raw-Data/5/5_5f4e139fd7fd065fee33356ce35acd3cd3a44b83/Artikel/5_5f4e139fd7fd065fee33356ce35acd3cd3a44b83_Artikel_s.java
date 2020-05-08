 package verkauf.model;
 
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 import sqlTools.SqlInfo;
 
 public class Artikel {
 	private double preis;
 	private double mwst;
 	private double einkaufspreis;
 	private double lagerstand;
 	public int lieferantenID, id;
 	private String beschreibung, einheit, ean;
 	
 	public Artikel(String ean, String beschreibung, String einheit, double preis, double mwst, double lagerstand, double ek, int lieferantenID) {
 		this.preis = preis;
 		this.einheit = einheit;
 		this.mwst = mwst;
 		this.lagerstand = lagerstand;
 		this.ean = ean;
 		this.beschreibung = beschreibung;
 		this.lieferantenID = lieferantenID;
 		this.einkaufspreis = ek;
 		
 		SqlInfo.sqlAusfuehren("INSERT INTO verkartikel (ean, beschreibung, preis, mwst, lagerstand, einheit, verklieferantID, einkaufspreis) " +
 				"VALUES('"+ this.ean +"', '"+ this.beschreibung +"', '"+ this.preis +"', '"+ this.mwst +"', '"+ this.lagerstand +"', '"+ this.einheit +"', '"+ this.lieferantenID +"', '"+ this.einkaufspreis +"'  );");
 	}
 	
 	public Artikel(int id) {
 		this.id = id;
 		// Abfragen aus der Datenbank
 		String sql = "SELECT beschreibung, preis, mwst, lagerstand, einheit, einkaufspreis, verklieferantID, ean FROM verkartikel WHERE verkartikelID = "+ this.id;
 		
 		Vector<Vector<String>> felder = SqlInfo.holeFelder(sql);
 		Vector<String> datensatz = felder.get(0);
 		if(!datensatz.isEmpty()) {
 			this.beschreibung = datensatz.get(0);
 			this.preis = Double.parseDouble(datensatz.get(1));
 			this.mwst = Double.parseDouble(datensatz.get(2));
 			this.lagerstand = Double.parseDouble(datensatz.get(3));
 			this.einheit = datensatz.get(4);
 			this.einkaufspreis = Double.parseDouble(datensatz.get(5));
 			this.lieferantenID = Integer.parseInt(datensatz.get(6));
 			this.ean = datensatz.get(7);
 		}
 	}
 	
 	public Artikel(String ean) {
 		this.ean = ean;
 		// Abfragen aus der Datenbank
 		String sql = "SELECT beschreibung, preis, mwst, lagerstand, einheit, einkaufspreis, verklieferantID, verkartikelID FROM verkartikel WHERE ean LIKE '"+ this.ean+"';";
 		
 		Vector<Vector<String>> felder = SqlInfo.holeFelder(sql);
 		Vector<String> datensatz = felder.get(0);
 		if(!datensatz.isEmpty()) {
 			this.beschreibung = datensatz.get(0);
 			this.preis = Double.parseDouble(datensatz.get(1));
 			this.mwst = Double.parseDouble(datensatz.get(2));
 			this.lagerstand = Double.parseDouble(datensatz.get(3));
 			this.einheit = datensatz.get(4);
 			this.einkaufspreis = Double.parseDouble(datensatz.get(5));
 			this.lieferantenID = Integer.parseInt(datensatz.get(6));
 			this.id = Integer.parseInt(datensatz.get(7));
 		}
 	}
 	
 	void verkaufeArtikel(double anzahl, String vnummer, Double vpreis, int patid) {
 		this.lagerstand = this.lagerstand - anzahl;
 		this.update();
 		String sql = "INSERT INTO verkfaktura (verkfakturaID, v_nummer, art_id, art_beschreibung, art_einzelpreis, art_mwst, anzahl, pat_id) " +
 				"VALUES (NULL, '"+ vnummer +"', '"+ this.id +"', '"+ this.getBeschreibung() +"', '"+ vpreis +"', '"+ this.mwst +"', '"+ anzahl +"', '"+ patid +"')";
 		SqlInfo.sqlAusfuehren(sql);
 	}
 	
 	void update() {
 		String sql = "UPDATE verkartikel SET beschreibung = '"+ this.beschreibung +"', preis = '"+ this.preis +"'," +
 				"mwst = '"+ this.mwst +"' , lagerstand = '"+ this.lagerstand +"', einkaufspreis = '"+ this.einkaufspreis +"'" +
 						", verklieferantID = '"+ this.lieferantenID +"', ean = '"+ this.ean +"'  WHERE verkartikelID = "+ this.id +" LIMIT 1;";
 		//System.out.println(sql);
 		SqlInfo.sqlAusfuehren(sql);
 	}
 	
 	public double getPreis() {
 		return preis;
 	}
 
 	public double getMwst() {
 		return mwst;
 	}
 
 	public double getLagerstand() {
 		return lagerstand;
 	}
 
 	public String getEan() {
 		return ean;
 	}
 
 	public String getBeschreibung() {
 		return beschreibung;
 	}
 
 	public void setPreis(double preis) {
 		this.preis = preis;
 		this.update();
 	}
 
 	public void setMwst(double mwst) {
 		this.mwst = mwst;
 		this.update();
 	}
 
 	public void setLagerstand(double lagerstand) {
 		this.lagerstand = lagerstand;
 		this.update();
 	}
 
 	public void setEan(String ean) {
 		this.ean = ean;
 		this.update();
 	}
 
 	public void setBeschreibung(String beschreibung) {
 		this.beschreibung = beschreibung;
 		this.update();
 	}
 	
 	public void setEinheit(String einheit) {
 		this.einheit = einheit;
 		this.update();
 	}
 	
 	public String getEinheit() {
 		return this.einheit;
 	}
 	
 	public int getLieferant() {
 		return this.lieferantenID;
 	}
 	
 	public void setLieferant(int id) {
 		this.lieferantenID = id;
 		this.update();
 	}
 	
 	public double getEinkaufspreis() {
 		return this.einkaufspreis;
 	}
 	
 	public void setEinkaufspreis(double ek) {
 		this.einkaufspreis = ek;
 		this.update();
 	}
 	
 	public static void loescheArtikel(int id) {
 		String sql = "DELETE FROM verkartikel WHERE verkartikelID = " + id;
 		SqlInfo.sqlAusfuehren(sql);
 	}
 	
 	public static boolean artikelExistiert(String ean) {
 		String sql = "Select ean From verkartikel Where ean LIKE '" + ean +"' LIMIT 1"; 
 		return SqlInfo.gibtsSchon(sql);
 	}
 	
 	public static Vector<Vector<String>> liefereArtikelDaten() {
 		String sql = "SELECT verkartikel.verkartikelID FROM verkartikel;";
		DecimalFormat df = new DecimalFormat("0,00");
 		Vector<String> artikelIDs = SqlInfo.holeFeld(sql);
 		Vector<Vector<String>> daten = new Vector<Vector<String>>();
 		while(!artikelIDs.isEmpty()) {
 			Vector<String> artikel = new Vector<String>();
 			Artikel a = new Artikel(Integer.parseInt(artikelIDs.get(0)));
 			artikelIDs.remove(0);
 			artikel.add(String.valueOf(a.getEan()));
 			artikel.add(String.valueOf(a.getBeschreibung()));
 			artikel.add(df.format(a.getPreis()));
 			artikel.add(df.format(a.getEinkaufspreis()));
 			if(a.getLieferant() != -1) {
 				artikel.add(new Lieferant(a.getLieferant()).toString());
 			} else {
 				artikel.add("");
 			}
 			artikel.add(String.valueOf(a.getLagerstand()));
 			artikel.add(String.valueOf(a.id));
 			daten.add(artikel);
 		}
 		return daten;
 	}
 	
 	public static Vector<Vector<String>> sucheArtikelDaten(String filter) {
 		String sql = "SELECT verkartikel.verkartikelID FROM verkartikel WHERE (verkartikel.ean LIKE '%"+filter+"%' OR verkartikel.beschreibung LIKE '%"+filter+"%')";
 		System.out.println(sql);
		DecimalFormat df = new DecimalFormat("0,00");
 		Vector<String> artikelIDs = SqlInfo.holeFeld(sql);
 		System.out.println(artikelIDs.size());
 		Vector<Vector<String>> daten = new Vector<Vector<String>>();
 		while(!artikelIDs.isEmpty()) {
 			Vector<String> artikel = new Vector<String>();
 			Artikel a = new Artikel(Integer.parseInt(artikelIDs.get(0)));
 			artikelIDs.remove(0);
 			artikel.add(String.valueOf(a.getEan()));
 			artikel.add(String.valueOf(a.getBeschreibung()));
 			artikel.add(df.format(a.getPreis()));
 			artikel.add(df.format(a.getEinkaufspreis()));
 			if(a.getLieferant() != -1) {
 				artikel.add(new Lieferant(a.getLieferant()).toString());
 			} else {
 				artikel.add("");
 			}
 			artikel.add(String.valueOf(a.getLagerstand()));
 			artikel.add(String.valueOf(a.id));
 			daten.add(artikel);
 		}
 		return daten;
 	}
 	public static void entferneLieferant(int lieferantID) {
 		String sql = "UPDATE verkartikel SET verklieferantID = '-1' WHERE verklieferantID = '" + lieferantID +"';";
 		SqlInfo.sqlAusfuehren(sql);
 	}
 }
