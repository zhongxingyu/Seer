 package queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import database.DB;
 
 public class Q2_WITH extends Query {
 
 	public Q2_WITH(String headline) {
 		super(headline);
 	}
 
 	@Override
 	protected ResultSet doQuery() throws SQLException {
 		
 		String query = "WITH " + db.zweitStimmenNachBundesland() + " AS (" + stmtZweitStimmenNachBundesland() + "), "
 			+ db.zweitStimmenNachPartei() + " AS (" + stmtZweitStimmenNachPartei(db.zweitStimmenNachBundesland()) + "), "
 			+ db.maxErststimmenNachWahlkreis() + " AS (" + stmtMaxErststimmenNachWahlkreis(db.erstStimmenNachWahlkreis()) + "), "
   		+ db.direktMandateNummer() + " AS ( "
 				+ stmtDirektmandateNummer(db.maxErststimmenNachWahlkreis(), db.erstStimmenNachWahlkreis()) + "), "
 			+ db.direktMandateMaxNummer() + " AS ( "
 				+ stmtDirektmandateMaxNummer(db.direktMandateNummer()) + "), "
 			+ db.direktmandate() + " AS ("
 				+ stmtDirektmandate(db.direktMandateNummer(), db.direktMandateMaxNummer()) + "), "
 			+ db.fuenfProzentParteien() + " AS ("
 				+ stmtFuenfProzentParteien(db.zweitStimmenNachPartei()) + "), "
 			+ db.dreiDirektMandatParteien() + " AS (" + stmtDreiDirektmandateParteien(db.direktmandate()) + "), "
 			+ db.parteienImBundestag() + " AS ("
 				+ stmtParteienImBundestag(db.fuenfProzentParteien(), db.dreiDirektMandatParteien()) + "), "
 			+ db.divisoren() + " AS (" + stmtDivisoren() + "), "
 			+ db.zugriffsreihenfolgeSitzeNachPartei() + " AS (" + stmtZugriffsreihenfolgeSitzeNachPartei(
 					db.parteienImBundestag(), db.zweitStimmenNachPartei(), db.divisoren()) + "), "
 			+ db.sitzeNachPartei() + " AS (" + stmtSitzeNachParteiTable(
 					db.zweitStimmenNachPartei(), db.parteienImBundestag(), db.zugriffsreihenfolgeSitzeNachPartei()) + "), "
 					
 			+ "ListenKandidaten AS (" +
 		  		"SELECT " + DB.kID + " FROM " + db.kandidat() + " WHERE " + DB.kForeignKeyBundeslandID + " IS NOT NULL " + 
 		  		"EXCEPT " +
 		  		"SELECT " + DB.kForeignKeyKandidatID + " FROM " + db.direktmandate() + " " +
 		  	"), " +
 		  	"ListenKandidatenMitRang AS ( " + 
 		  		"SELECT lk." + DB.kID + ", k." + DB.kForeignKeyParteiID + ", b." + DB.kID + " AS BundeslandID, " +
 		  		"ROW_NUMBER() OVER (PARTITION BY b." + DB.kID + ", k." + DB.kForeignKeyParteiID + " ORDER BY k." + DB.kKandidatListenplatz + ") AS Rang " + 
 		  		"FROM ListenKandidaten lk, " + db.bundesland() + " b, " + db.kandidat() + " k " +
 		  		"WHERE lk." + DB.kID + " = k." + DB.kID + " " + 
 		  		"AND k." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " " + 
 		  	"), " +
 
 		  	"BundeslandParteiZuDirektmandate AS ( " +
   			"SELECT w." + DB.kForeignKeyBundeslandID + ", d." + DB.kForeignKeyParteiID + ", " + 
   				"Count(d." + DB.kForeignKeyKandidatID + ") as " + DB.kAnzahl + " " +
   			"FROM " + db.direktmandate() + " d, " + db.wahlkreis() + " w " +
   			"WHERE d." + DB.kKandidatDMWahlkreisID + " = w." + DB.kID + " " +
   			"GROUP BY w." + DB.kForeignKeyBundeslandID + ", d." + DB.kForeignKeyParteiID + "), " +
   	
   	"Abgeordnete AS ( " + 
   		"SELECT " + DB.kForeignKeyKandidatID + " FROM " + db.direktmandate() + " " + 
   		
   		"UNION " +
   		
   		"SELECT lkr." + DB.kID + " " +
   		"FROM ListenKandidatenMitRang lkr LEFT OUTER JOIN BundeslandParteiZuDirektmandate b ON lkr." + DB.kForeignKeyBundeslandID + " = b." + DB.kForeignKeyBundeslandID + " AND lkr." + DB.kForeignKeyParteiID + " = b." + DB.kForeignKeyParteiID + ", " + db.sitzeNachLandeslisten() + " s " + 
   		"WHERE s." + DB.kForeignKeyParteiID + " = lkr." + DB.kForeignKeyParteiID + " " + 
   		"AND s." + DB.kForeignKeyBundeslandID + " = lkr." + DB.kForeignKeyBundeslandID + " " + 
   		"AND lkr.Rang <= s." + DB.kAnzahlSitze + " - COALESCE(b." + DB.kAnzahl + ", 0)" +
   	") " +
 		  	
 			  "SELECT k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + " " +
 			  "FROM Abgeordnete a, " + db.kandidat() + " k LEFT OUTER JOIN " + db.partei() +
 			  " p ON k." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " +
 			  "WHERE a." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " +
 			  "ORDER BY k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + "";
 			
 	  return db.executeSQL(query);
 	}
 	
 	@Override
 	protected String generateBody(ResultSet resultSet) throws SQLException {
 		
 		String[] headers = new String[] {"", "Abgeordneter", "Partei"};
 		List<List<String>> rows = new ArrayList<List<String>>();
 		
 		int i = 0;
 		while (resultSet.next()) {
 			i++;
 			List<String> row = new ArrayList<String>();
 			row.add(i + "");
 			row.add(resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname));
 			row.add(resultSet.getString(DB.kParteiKuerzel));
 			rows.add(row);
 		}
 		
 		Table table = new Table(headers, rows);
 		
 		return table.getHtml();
 	}
 
 }
