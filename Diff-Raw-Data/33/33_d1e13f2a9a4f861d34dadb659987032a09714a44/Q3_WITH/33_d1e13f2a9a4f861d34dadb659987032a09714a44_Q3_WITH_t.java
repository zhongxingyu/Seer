 package queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import database.DB;
 
 public class Q3_WITH extends Query {
 	
 	protected int wahlkreisID;
 	
 	private String wahlkreisName;
 	
 	private List<List<String>> q1rows;
 	private List<List<String>> q2rows;
 	private List<List<String>> q3rows;
 	
 	public Q3_WITH(String headline, int wahlkreisID) {
 		super(headline);
 		this.wahlkreisID = wahlkreisID;
 	}
 	
 	@Override
 	protected ResultSet doQuery() throws SQLException {
 		return doQuery(db.erstStimmenNachWahlkreis(), db.zweitStimmenNachWahlkreis(), db.wahlkreisDaten());
 	}
 	
 	protected ResultSet doQuery(String erststimmenNachWahlkreisTable, 
 			String zweitStimmenNachWahlkreisTable, String wahlkreisDatenTable) throws SQLException {
 		
 		ResultSet rsWahlkreisName = db.executeSQL("" + 
 				"SELECT " + DB.kWahlkreisName + " FROM " + db.wahlkreis() + " " +
 				"WHERE " + DB.kID + " = " + wahlkreisID
 		); 
 		rsWahlkreisName.next();
 		wahlkreisName = rsWahlkreisName.getString(DB.kWahlkreisName);
     
 		// Q3.1 - Wahlbeteiligung
     
 		ResultSet rs1 = db.executeSQL(""
 			+ "SELECT (1.0 * sum(w2." + DB.kWahlergebnis2Anzahl + ") / "
 				+ "max(wd." + DB.kAnzahlWahlberechtigte + ")) as Wahlbeteiligung "
       + "FROM " + wahlkreisDatenTable + " wd, " + zweitStimmenNachWahlkreisTable + " w2 "
       + "WHERE wd." + DB.kForeignKeyWahlkreisID + " = w2." + DB.kForeignKeyWahlkreisID + " "
       	+ "AND wd." + DB.kJahr + " = w2.jahr AND wd." + DB.kJahr + " = " + kCurrentElectionYear + " "
       	+ "AND wd." + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " "
       + "GROUP BY w2." + DB.kForeignKeyWahlkreisID);
     q1rows = new ArrayList<List<String>>();
     rs1.next();
     String wahlbeteiligung;
     if (rs1.isClosed())
     	wahlbeteiligung = "0 %";
     else
     	wahlbeteiligung = String.format("%.2f %%", rs1.getFloat("Wahlbeteiligung") * 100);
     q1rows.add(new ArrayList<String>(Arrays.asList(new String[] {wahlbeteiligung})));
 		
 		// Q3.2 - gewhlter Direktkandidat
 		
     ResultSet rs2 = db.executeSQL(
     		"WITH ErstStimmenEinWahlkreis AS (" +
     			"SELECT * " +
     			"FROM " + erststimmenNachWahlkreisTable + " " +
     			"WHERE " + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID +
     		"), " +
     		db.maxErststimmenNachWahlkreis() + " AS (" + 
     			stmtMaxErststimmenNachWahlkreis("ErstStimmenEinWahlkreis") + "), " +
     		db.direktMandateNummer() + " AS ( " +
  				stmtDirektmandateNummer(db.maxErststimmenNachWahlkreis(), "ErstStimmenEinWahlkreis") + "), " +
   			db.direktMandateMaxNummer() + " AS ( " +
   				stmtDirektmandateMaxNummer(db.direktMandateNummer()) + "), " +
   			db.direktmandate() + " AS (" +
   				stmtDirektmandate(db.direktMandateNummer(), db.direktMandateMaxNummer()) + ") " +    		
     		"SELECT k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + " " + 
     		"FROM " + db.direktmandate() + " dm, " + db.kandidat() + " k, " + db.partei() + " p " + 
     		"WHERE dm." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
     		"AND dm." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " + 
     		"AND dm." + DB.kKandidatDMWahlkreisID + " = " + wahlkreisID + " "
     );
     q2rows = new ArrayList<List<String>>();
     rs2.next();
     if (rs2.isClosed()) {
       q2rows.add(new ArrayList<String>(Arrays.asList(new String[] {"Keine Daten", ""})));    	
     } else {
 	    q2rows.add(new ArrayList<String>(Arrays.asList(new String[] {
 	    		rs2.getString(DB.kKandidatVorname) + " " + rs2.getString(DB.kKandidatNachname),
 	    		rs2.getString(DB.kParteiKuerzel)
 	    })));
     }
 
     
 		// Q3.3 - prozentuale und absolute Anzahl an Stimmen fr jede Partei
 		
     ResultSet rs3 = db.executeSQL("" + 
     		"WITH ZweitStimmenWahlkreis2009 AS ( " + 
     			"SELECT " + DB.kForeignKeyParteiID + ", " + DB.kWahlergebnis2Anzahl + " " +
     			"FROM " + zweitStimmenNachWahlkreisTable + " " +
     			"WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " +
     				"AND " + DB.kJahr + " = " + kCurrentElectionYear +
     		"), " +
     		"ZweitStimmenWahlkreis2005 AS ( " + 
   			"SELECT " + DB.kForeignKeyParteiID + ", " + DB.kWahlergebnis2Anzahl + " " +
   			"FROM " + zweitStimmenNachWahlkreisTable + " " +
   			"WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " +
   				"AND " + DB.kJahr + " = " + kPreviousElectionYear +
   			"), " +
     		"SummeZweitStimmenWahlkreis2009 AS ( " + 
   			"SELECT SUM(" + DB.kWahlergebnis2Anzahl + ") AS Summe " + 
   			"FROM " + zweitStimmenNachWahlkreisTable + " " +
   			"WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " + 
   				"AND " + DB.kJahr + " = " + kCurrentElectionYear + " " +
   			"GROUP BY " + DB.kForeignKeyWahlkreisID + " " + 
 	  		"), " +
 	  		"SummeZweitStimmenWahlkreis2005 AS ( " + 
 				"SELECT SUM(" + DB.kWahlergebnis2Anzahl + ") AS Summe " + 
 				"FROM " + zweitStimmenNachWahlkreisTable + " " +
 				"WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " + 
 					"AND " + DB.kJahr + " = " + kPreviousElectionYear + " " +
 				"GROUP BY " + DB.kForeignKeyWahlkreisID + " " + 
 				") " +
 				
     		"SELECT " + 
     			"p." + DB.kParteiKuerzel + ", " +
     			"COALESCE(w2009." + DB.kWahlergebnis2Anzahl + ", 0) AS Absolut2009, " +
     			"CAST(COALESCE(w2009." + DB.kWahlergebnis2Anzahl + ", 0) AS FLOAT) / (SELECT Summe FROM SummeZweitStimmenWahlkreis2009) AS Prozentual2009, " +
     			"COALESCE(w2005." + DB.kWahlergebnis2Anzahl + ", 0) AS Absolut2005, " +
     			"CAST(COALESCE(w2005." + DB.kWahlergebnis2Anzahl + ", 0) AS FLOAT) / (SELECT Summe FROM SummeZweitStimmenWahlkreis2005) AS Prozentual2005, " +
     			"(COALESCE(w2009." + DB.kWahlergebnis2Anzahl + ", 0) - COALESCE(w2005." + DB.kWahlergebnis2Anzahl + ", 0)) as Aenderung " +
     		"FROM ZweitStimmenWahlkreis2009 w2009 FULL OUTER JOIN ZweitStimmenWahlkreis2005 w2005 "
     			+ "ON w2009." + DB.kForeignKeyParteiID  + " = w2005." + DB.kForeignKeyParteiID
     			+ " RIGHT OUTER JOIN " + db.partei() + " p ON p." + DB.kID + " = w2009." + DB.kForeignKeyParteiID + " " +
     		"ORDER BY Absolut2009 DESC, Absolut2005 DESC"
     );
     
 		q3rows = new ArrayList<List<String>>();
 		while(rs3.next()) {
 			List<String> row = new ArrayList<String>();
 			row.add(rs3.getString(DB.kParteiKuerzel));
 			row.add(String.format("%d", rs3.getInt("Absolut2009")));
 			row.add(String.format("%12.2f %%", 100 * rs3.getFloat("Prozentual2009")));
 			row.add(String.format("%d", rs3.getInt("Absolut2005")));
 			row.add(String.format("%12.2f %%", 100 * rs3.getFloat("Prozentual2005")));
 			row.add(String.format("%+d", rs3.getInt("Aenderung")));
 			q3rows.add(row);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	protected String generateBody(ResultSet resultSet) throws SQLException {
 		
 		String html = "<h2>Wahlkreis " + wahlkreisName + " (" + wahlkreisID + ")</h2>";
 		
 		html += "<h3>Gewhlter Direktkandidat</h3>";
     String[] q2headers = new String[] {"Name", "Partei"};
 		Table q2table = new Table(q2headers, q2rows);
 		html += q2table.getHtml();
 		
 		html += "<h3>Wahlbeteiligung</h3>";
 		String[] q1headers = new String[] {"" + kCurrentElectionYear};
 		Table q1table = new Table(q1headers, q1rows);
 		html += q1table.getHtml();
     
 		html += "<h3>Stimmen fr jede Partei</h3>";
     String[] q3headers = new String[] {"Partei", "Stimmen Absolut 2009", "Stimmen Prozentual 2009", "Stimmen Absolut 2005", "Stimmen Prozentual 2005", "nderung 2009 - 2005"};
     Table q3table = new Table(q3headers, q3rows);
     html += q3table.getHtml();
     
     
 		return html;
 	}
 
 	
 
 }
