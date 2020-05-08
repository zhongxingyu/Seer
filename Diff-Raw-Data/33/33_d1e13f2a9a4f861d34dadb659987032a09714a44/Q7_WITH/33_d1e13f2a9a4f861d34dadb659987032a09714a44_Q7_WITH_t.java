 package queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import database.DB;
 
 public class Q7_WITH extends Q3_WITH {
 	public Q7_WITH(String headline, int wahlkreisID) {
 		super(headline, wahlkreisID);
 	}
 
 	String tempErstStimmenNachWahlkreis() {
 		return db.tabellenName("TempErstStimmenNachWahlkreis");
 	}
 
 	String tempZweitStimmenNachWahlkreis() {
 		return db.tabellenName("TempZweitStimmenNachWahlkreis");
 	}
 	
 	String tempWahlkreisDatenTable() {
 		return db.tabellenName("TempWahlkreisDaten");
 	}
 	
 	protected String createTempZweitStimmenNachWahlkreisTable(int wahlkreisID) throws SQLException {
 	 	db.createOrReplaceTemporaryTable(tempZweitStimmenNachWahlkreis(), DB.kForeignKeyParteiID + " BIGINT, "
 	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahl + " BIGINT");
     db.executeUpdate(""
     		+ "INSERT INTO " + tempZweitStimmenNachWahlkreis() + "(" + DB.kForeignKeyParteiID + ", "
     			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahl + ") "
     		+ "SELECT s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
     		+ "FROM " + db.stimme() + " s "
     		+ "WHERE " + DB.kForeignKeyParteiID + " is not null "
     			+ "AND s." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + " "
     		+ "GROUP BY s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
     return tempZweitStimmenNachWahlkreis();
 	}
 	
 	protected String createTempErstStimmenNachWahlkreisTable(int wahlkreisID) throws SQLException {
 	 	db.createOrReplaceTemporaryTable(tempErstStimmenNachWahlkreis(), DB.kForeignKeyKandidatID + " BIGINT, "
 	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahl + " BIGINT");
     db.executeUpdate(""
     		+ "INSERT INTO " + tempErstStimmenNachWahlkreis() + "(" + DB.kForeignKeyKandidatID + ", "
     			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahl + ") "
     		+ "SELECT s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
     		+ "FROM " + db.stimme() + " s "
     		+ "WHERE " + DB.kForeignKeyKandidatID + " is not null "
    			+ "AND s." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + " "
     		+ "GROUP BY s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
     return tempErstStimmenNachWahlkreis();
 	}
 	
 	protected String createTempWahlkreisDatenTable(int wahlkreisID) throws SQLException {
 	 	db.createOrReplaceTemporaryTable(tempWahlkreisDatenTable(), DB.kForeignKeyWahlkreisID + " BIGINT, "
 	 			+ DB.kAnzahlWahlberechtigte + " BIGINT, " + DB.kUngueltigeErststimmen + " BIGINT, " + DB.kUngueltigeZweitstimmen
 	 			+ " BIGINT, " + DB.kJahr + " BIGINT");
     db.executeUpdate(""
     		+ "INSERT INTO " + tempWahlkreisDatenTable() + "(" + DB.kForeignKeyWahlkreisID + ", "
 	 			+ DB.kAnzahlWahlberechtigte + ", " + DB.kUngueltigeErststimmen + ", " + DB.kUngueltigeZweitstimmen
 	 			+ ", " + DB.kJahr + ") "
 	 			
 	 			+ "SELECT " + wahlkreisID + " AS " + DB.kForeignKeyWahlkreisID + ", "
 	 				+ "(SELECT COUNT(*) FROM " + db.wahlberechtigter() + " w "
 	 					+ "WHERE w." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + ") AS " + DB.kAnzahlWahlberechtigte + ", "
 		 			+ "(SELECT COUNT(*) FROM " + db.stimme() + " s " 
 	 					+ "WHERE " + DB.kForeignKeyKandidatID + " IS NULL AND " + DB.kJahr + "=tmp." + DB.kJahr + " "
 	 						+ "AND s." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + ") AS " + DB.kUngueltigeErststimmen + ", "
  		 			+ "(SELECT COUNT(*) FROM " + db.stimme() + " s " 
 	 					+ "WHERE " + DB.kForeignKeyParteiID + " IS NULL AND " + DB.kJahr + "=tmp." + DB.kJahr + " "
 	 						+ "AND s." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + ") AS " + DB.kUngueltigeZweitstimmen + ", "
 	 				+ "tmp." + DB.kJahr + " "
 	 			+ "FROM (VALUES(" + kCurrentElectionYear + "), (" + kPreviousElectionYear + ")) tmp(" + DB.kJahr + ")");
     return tempWahlkreisDatenTable();
 	}
 	
 	@Override
 	protected ResultSet doQuery() throws SQLException {
 		// Aggregate Wahlkreis data.	
 		//TODO: WahlkreisDaten Tabelle muss ebenfalls dynamisch erstellt werden knnen.
 		return doQuery(createTempErstStimmenNachWahlkreisTable(wahlkreisID),
 				createTempZweitStimmenNachWahlkreisTable(wahlkreisID),
 				createTempWahlkreisDatenTable(wahlkreisID));
 	}
 }
