 package ho.core.db;
 
 import ho.HO;
 import ho.core.util.HOLogger;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 
 final class DBUpdater {
 	JDBCAdapter m_clJDBCAdapter;
 	DBManager dbZugriff;
 	
 	void setDbZugriff(DBManager dbZugriff) {
 		this.dbZugriff = dbZugriff;
 	}
 
 	void updateDB(int DBVersion) {
 		// I introduce some new db versioning system.
 		// Just add new version cases in the switch..case part
 		// and leave the old ones active, so also users which
 		// have skipped a version get their database updated.
 		// jailbird.
 		int version = 0;
 		this.m_clJDBCAdapter = dbZugriff.getAdapter();
 		
 		version = 	((UserConfigurationTable) dbZugriff.getTable(UserConfigurationTable.TABLENAME)).getDBVersion();
 
 		// We may now update depending on the version identifier!
 		if (version != DBVersion) {
 			try {
 				HOLogger.instance().log(getClass(), "Updating DB to version " + DBVersion + "...");
 
 				switch (version) { // hint: fall though (no breaks) is intended here
 					case 0 :
 					case 1 :
 					case 2 :
 					case 3 :
 					case 4 :
 						HOLogger.instance().log(getClass(), "DB version " + DBVersion + " is to old");
						try {
							JOptionPane.showMessageDialog(null, "DB is too old.\nPlease update first to HO 1.431","Error",JOptionPane.ERROR_MESSAGE);
						} catch(Exception e) {
							HOLogger.instance().log(getClass(), e);
						}
 						System.exit(0);
 					case 5 :
 						updateDBv6();
 					case 6 :
 						updateDBv7();
 					case 7 :
 						updateDBv8();
 					case 8 :
 						updateDBv9();
 					case 9 : 
 						updateDBv10();
 					case 10: 
 						updateDBv11();
 					case 11:
 						updateDBv12(DBVersion, version);
 					case 12:
 						updateDBv13(DBVersion, version);
 					case 13:
 						updateDBv14(DBVersion, version);
 					case 14:
 						updateDBv15(DBVersion, version);
 				}
 
 				HOLogger.instance().log(getClass(), "done.");
 			} catch (Exception e) {
 				HOLogger.instance().log(getClass(), "failed.");
 			}
 		} else {
 			HOLogger.instance().log(getClass(), "No DB update necessary.");
 		}
 	}
 
 	/**
 	 * Update DB structure to v6
 	 */
 	private void updateDBv6() throws Exception {
 
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER ADD COLUMN AGEDAYS INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SCOUT ADD COLUMN AGEDAYS INTEGER");
 
 		// Always set field DBVersion to the new value as last action.
 		// Do not use DBVersion but the value, as update packs might
 		// do version checking again before applying!
 		dbZugriff.saveUserParameter("DBVersion", 6);
 	}
 
 	/**
 	 * Update DB structure to v7
 	 */
 	private void updateDBv7() throws Exception {
 
 		// Adding arena/region ID
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE BASICS ADD COLUMN Region INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE STADION ADD COLUMN ArenaID INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MATCHDETAILS ADD COLUMN RegionID INTEGER");
 
 		// Drop and recreate the table
 		// (i.e. use defaults from defaults.xml)
 		AbstractTable faktorenTab = dbZugriff.getTable(FaktorenTable.TABLENAME);
 		if (faktorenTab != null) {
 			faktorenTab.dropTable();
 			faktorenTab.createTable();
 		}
 
 		// Always set field DBVersion to the new value as last action.
 		// Do not use DBVersion but the value, as update packs might
 		// do version checking again before applying!
 		dbZugriff.saveUserParameter("DBVersion", 7);
 	}
 
 	/**
 	 * Update DB structure to v8
 	 *
 	 * @throws Exception
 	 */
 	private void updateDBv8() throws Exception {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE Spieler ADD COLUMN TrainingBlock BOOLEAN");
 		m_clJDBCAdapter.executeUpdate("UPDATE Spieler SET TrainingBlock=false WHERE TrainingBlock IS null");
 		// Always set field DBVersion to the new value as last action.
 		// Do not use DBVersion but the value, as update packs might
 		// do version checking again before applying!
 		dbZugriff.saveUserParameter("DBVersion", 8);
 	}
 
 	/**
 	 * Update DB structure to v9
 	 */
 	private void updateDBv9() throws Exception {
 		// Add new columns for spectator distribution
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MatchDetails ADD COLUMN soldTerraces INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MatchDetails ADD COLUMN soldBasic INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MatchDetails ADD COLUMN soldRoof INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MatchDetails ADD COLUMN soldVIP INTEGER");
 
 		m_clJDBCAdapter.executeUpdate("UPDATE MatchDetails SET soldTerraces=-1 WHERE soldTerraces IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE MatchDetails SET soldBasic=-1 WHERE soldBasic IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE MatchDetails SET soldRoof=-1 WHERE soldRoof IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE MatchDetails SET soldVIP=-1 WHERE soldVIP IS null");
 
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE Scout ADD COLUMN Agreeability INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE Scout ADD COLUMN baseWage INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE Scout ADD COLUMN Nationality INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE Scout ADD COLUMN Leadership INTEGER");
 
 		m_clJDBCAdapter.executeUpdate("UPDATE Scout SET  Agreeability=-1 WHERE Agreeability IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE Scout SET  baseWage=-1 WHERE baseWage IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE Scout SET  Nationality=-1 WHERE Nationality IS null");
 		m_clJDBCAdapter.executeUpdate("UPDATE Scout SET  Leadership=-1 WHERE Leadership IS null");
 
 		// Always set field DBVersion to the new value as last action.
 		// Do not use DBVersion but the value, as update packs might
 		// do version checking again before applying!
 		dbZugriff.saveUserParameter("DBVersion", 9);
 	}
 	
 	/**
 	 * Update database to version 10.
 	 */
 	private void updateDBv10() throws Exception {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAM ADD COLUMN iErfahrung442 INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAM ADD COLUMN iErfahrung523 INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAM ADD COLUMN iErfahrung550 INTEGER");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAM ADD COLUMN iErfahrung253 INTEGER");
 		
 		m_clJDBCAdapter.executeUpdate("UPDATE TEAM SET iErfahrung442=8 WHERE iErfahrung442 IS NULL");
 		m_clJDBCAdapter.executeUpdate("UPDATE TEAM SET iErfahrung523=1 WHERE iErfahrung523 IS NULL");
 		m_clJDBCAdapter.executeUpdate("UPDATE TEAM SET iErfahrung550=1 WHERE iErfahrung550 IS NULL");
 		m_clJDBCAdapter.executeUpdate("UPDATE TEAM SET iErfahrung253=1 WHERE iErfahrung253 IS NULL");
 
 		// Always set field DBVersion to the new value as last action.
 		// Do not use DBVersion but the value, as update packs might
 		// do version checking again before applying!
 		dbZugriff.saveUserParameter("DBVersion", 10);
 	}
 	/** 
 	* Update database to version 11. 
 	*/ 
 	private void updateDBv11() throws Exception { 
 	 	// Problems in 1.431 release has shifted contents here to v12. 
 		dbZugriff.saveUserParameter("DBVersion", 11); 
 	 	return; 
 	} 
 	/**
 	 * Update database to version 12.
 	 */
 	private void updateDBv12(int DBVersion, int version) throws Exception {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MATCHLINEUPPLAYER ADD COLUMN RatingStarsEndOfMatch REAL");
 		m_clJDBCAdapter.executeUpdate("UPDATE MATCHLINEUPPLAYER SET RatingStarsEndOfMatch = -1 WHERE RatingStarsEndOfMatch IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE BASICS ADD COLUMN HasSupporter BOOLEAN");
 		m_clJDBCAdapter.executeUpdate("UPDATE BASICS SET HasSupporter = 'false' WHERE HasSupporter IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER ADD COLUMN Loyalty INTEGER");
 		m_clJDBCAdapter.executeUpdate("UPDATE SPIELER SET Loyalty = 0 WHERE Loyalty IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER ADD COLUMN HomeGrown BOOLEAN");
 		m_clJDBCAdapter.executeUpdate("UPDATE SPIELER SET HomeGrown = 'false' WHERE HomeGrown IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SCOUT ADD COLUMN Loyalty INTEGER");
 		m_clJDBCAdapter.executeUpdate("UPDATE SCOUT SET Loyalty = 0 WHERE Loyalty IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SCOUT ADD COLUMN MotherClub BOOLEAN");
 		m_clJDBCAdapter.executeUpdate("UPDATE SCOUT SET MotherClub = 'false' WHERE MotherClub IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MATCHLINEUPPLAYER ADD COLUMN StartPosition INTEGER");
 		m_clJDBCAdapter.executeUpdate("UPDATE MATCHLINEUPPLAYER SET StartPosition = -1 WHERE StartPosition IS NULL");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MATCHLINEUPPLAYER ADD COLUMN StartBehaviour INTEGER");
 		m_clJDBCAdapter.executeUpdate("UPDATE MATCHLINEUPPLAYER SET StartBehaviour = -1 WHERE StartBehaviour IS NULL");
 		
 		dbZugriff.getTable(MatchSubstitutionTable.TABLENAME).createTable();
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE MATCHSUBSTITUTION ADD COLUMN LineupName VARCHAR");
 		m_clJDBCAdapter.executeUpdate("UPDATE MATCHSUBSTITUTION SET LineupName = 'D' WHERE LineupName IS NULL");
 
 		
 		// Follow this pattern in the future. Only set db version if not development, or
 		// if the current db is more than one version old. The last update should be made
 		// during first run of a non development version.
 		            
 		if ((version == (DBVersion - 1) && !HO.isDevelopment()) 
 				|| (version < (DBVersion - 1))) {
 			dbZugriff.saveUserParameter("DBVersion", 12);
 		}
 	}
 	
 	private void updateDBv13(int DBVersion, int version) {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE STADION DROP VerkaufteSteh");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE STADION DROP VerkaufteSitz");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE STADION DROP VerkaufteDach");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE STADION DROP VerkaufteLogen");
 		m_clJDBCAdapter.executeUpdate("DROP INDEX ISTADION_1");
 		m_clJDBCAdapter.executeUpdate("DELETE FROM USERCONFIGURATION WHERE CONFIG_KEY='einzelnePositionenAnzeigen'");
 		m_clJDBCAdapter.executeUpdate("DELETE FROM USERCONFIGURATION WHERE CONFIG_KEY='DAUER_ALLGEMEIN'");
 		m_clJDBCAdapter.executeUpdate("DELETE FROM USERCONFIGURATION WHERE CONFIG_KEY='tempTabArenasizer'");
 		
 		// Transfers-plugin
 		ResultSet rs = m_clJDBCAdapter.executeQuery("Select * from TRANSFERS_TRANSFERS");
 		if(rs == null){
 			dbZugriff.getTable(TransferTable.TABLENAME).createTable();
 			dbZugriff.getTable(TransferTypeTable.TABLENAME).createTable();
 		} else{
 			m_clJDBCAdapter.executeUpdate("ALTER TABLE TRANSFERS_TRANSFERS RENAME TO "+TransferTable.TABLENAME);
 			m_clJDBCAdapter.executeUpdate("ALTER TABLE TRANSFERS_TYPE RENAME TO "+TransferTypeTable.TABLENAME);
 		}
 		
 		// TeamAnalyzer-plugin
 		rs = m_clJDBCAdapter.executeQuery("Select * from TEAMANALYZER_FAVORITES");
 		if(rs == null){
 			dbZugriff.getTable(TAFavoriteTable.TABLENAME).createTable();
 			dbZugriff.getTable(TAPlayerTable.TABLENAME).createTable();
 		} else{
 			m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAMANALYZER_FAVORITES RENAME TO "+TAFavoriteTable.TABLENAME);
 			m_clJDBCAdapter.executeUpdate("ALTER TABLE TEAMANALYZER_PLAYERDATA RENAME TO "+TAPlayerTable.TABLENAME);
 		}
 		
 		
 		
 		ModuleConfigTable mConfigTable = (ModuleConfigTable)dbZugriff.getTable(ModuleConfigTable.TABLENAME);
 		mConfigTable.createTable();
 		rs = m_clJDBCAdapter.executeQuery("Select * from TEAMANALYZER_SETTINGS");
 		HashMap<String,Object> tmp = new HashMap<String,Object>();
 		if(rs != null){
 			try {
 				while(rs.next())
 					tmp.put("TA_"+rs.getString("NAME"), Boolean.valueOf(rs.getBoolean("VALUE")));
 				mConfigTable.saveConfig(tmp);	
 			} catch (SQLException e) {
 				HOLogger.instance().warning(this.getClass(), e);
 			}
 		}
 		m_clJDBCAdapter.executeUpdate("DROP TABLE PLUGIN_IFA_TEAM");
 		dbZugriff.getTable(WorldDetailsTable.TABLENAME).createTable();
 		rs = m_clJDBCAdapter.executeQuery("Select * from PLUGIN_IFA_MATCHES_2");
 		if(rs == null)
 			dbZugriff.getTable(IfaMatchTable.TABLENAME).createTable();
 		else
 			m_clJDBCAdapter.executeUpdate("ALTER TABLE PLUGIN_IFA_MATCHES_2 RENAME TO "+IfaMatchTable.TABLENAME);
 
 		// Follow this pattern in the future. Only set db version if not development, or
 		// if the current db is more than one version old. The last update should be made
 		// during first run of a non development version.
 		if ((version == (DBVersion - 1) && !HO.isDevelopment()) || (version < (DBVersion - 1))) { 
 			 dbZugriff.saveUserParameter("DBVersion", 13); 
 		} 
 	}
 	
 	private void updateDBv14(int DBVersion, int version) {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER DROP COLUMN  sSpezialitaet");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER DROP COLUMN  sCharakter");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER DROP COLUMN  sAnsehen");
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE SPIELER DROP COLUMN  sAgressivitaet");
 		
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE basics ADD COLUMN ActivationDate TIMESTAMP");
 		/* Make sure there is no old IFA_MATCH entries to avoid showing matches
 		 * from previous owners.
 		 */
 		m_clJDBCAdapter.executeUpdate("DELETE FROM IFA_MATCH");
 
 		// No more DB changes to version 14 because we delete all entries from IFA_MATCH
 		dbZugriff.saveUserParameter("DBVersion", 14); 
 	}
 	
 	private void updateDBv15(int DBVersion, int version) {
 		m_clJDBCAdapter.executeUpdate("ALTER TABLE ta_player DROP PRIMARY KEY");
 		m_clJDBCAdapter.executeUpdate("CREATE INDEX ITA_PLAYER_PLAYERID_WEEK ON ta_player (playerid, week)");
 
 		// Follow this pattern in the future. Only set db version if not development, or
 		// if the current db is more than one version old. The last update should be made
 		// during first run of a non development version.
 		if ((version == (DBVersion - 1) && !HO.isDevelopment()) || (version < (DBVersion - 1))) {
 			dbZugriff.saveUserParameter("DBVersion", 15); 
 		}
 	}
 
 	
 	/**
 	 * Automatic update of User Configuration parameters
 	 *
 	 * This method is similar to the updateDB() method above
 	 * The main difference is that it is based on the
 	 * HO release version instead of the DB version
 	 *
 	 * In development mode, we execute the current update steps
 	 * again (just like in updateBD()).
 	 *
 	 * @author flattermann <flattermannHO@gmail.com>
 	 */
 	void updateConfig () {
 		if(m_clJDBCAdapter== null)
 			m_clJDBCAdapter = dbZugriff.getAdapter();
 		
 		double lastConfigUpdate = ((UserConfigurationTable) dbZugriff.getTable(UserConfigurationTable.TABLENAME)).getLastConfUpdate();
 		/**
 		 * We have to use separate 'if-then' clauses for each conf version (ascending order)
 		 * because a user might have skipped some HO releases
 		 *
 		 * DO NOT use 'if-then-else' here, as this would ignores some updates!
 		 */
 		if (lastConfigUpdate < 1.4101 || (HO.isDevelopment() && lastConfigUpdate == 1.4101)) {
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.410-1...");
 			updateConfigTo1410_1(HO.isDevelopment() && lastConfigUpdate == 1.4101);
 		}
 
 		if (lastConfigUpdate < 1.420 || (HO.isDevelopment() && lastConfigUpdate == 1.420)) {
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.420...");
 			updateConfigTo1420(HO.isDevelopment() && lastConfigUpdate == 1.420);
 		}
 
 		if (lastConfigUpdate < 1.424 || (HO.isDevelopment() && lastConfigUpdate == 1.424)) {
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.424...");
 			updateConfigTo1424(HO.isDevelopment() && lastConfigUpdate == 1.424);
 		}
 
 		if (lastConfigUpdate < 1.425 || (HO.isDevelopment() && lastConfigUpdate == 1.425)) {
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.425...");
 			updateConfigTo1425(HO.isDevelopment() && lastConfigUpdate == 1.425);
 		}
 		
 		if (lastConfigUpdate < 1.429 || (HO.isDevelopment() && lastConfigUpdate == 1.429)) {
 
 			// Lets not reset poor user's custom training setting each time they start...
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.429...");
 			updateConfigTo1429(HO.isDevelopment() && lastConfigUpdate == 1.429);
 		}
 		
 		if (lastConfigUpdate < 1.431 || (HO.isDevelopment() && lastConfigUpdate == 1.431)) {
 
 			HOLogger.instance().log(getClass(), "Updating configuration to version 1.431...");
 			updateConfigTo1431(HO.isDevelopment() && lastConfigUpdate == 1.431);
 		}
 		
 	}
 
 	private void updateConfigTo1410_1 (boolean alreadyApplied) {
 		resetTrainingParameters();
 		resetPredictionOffsets();
 
 		// Drop the feedback tables to force new feedback upload for beta testers
 		m_clJDBCAdapter.executeUpdate("DROP TABLE IF EXISTS FEEDBACK_SETTINGS");
 		m_clJDBCAdapter.executeUpdate("DROP TABLE IF EXISTS FEEDBACK_UPLOAD");
 
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.4101);
 	}
 
 	private void updateConfigTo1420 (boolean alreadyApplied) {
 		resetTrainingParameters();
 		resetPredictionOffsets();
 		dbZugriff.saveUserParameter("anzahlNachkommastellen", 2);
 
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.420);
 	}
 
 	private void updateConfigTo1424 (boolean alreadyApplied) {
 		resetTrainingParameters (); // Reset training parameters (just to be sure)
 
 		dbZugriff.saveUserParameter("updateCheck", "true");
 		dbZugriff.saveUserParameter("newsCheck", "true");
 
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.424);
 	}
 
 	private void updateConfigTo1425 (boolean alreadyApplied) {
 		// Argentina.properties is outdated and got replaced by Spanish_sudamericano.properties
 		m_clJDBCAdapter.executeUpdate("UPDATE USERCONFIGURATION SET CONFIG_VALUE='Spanish_sudamericano' where CONFIG_KEY='sprachDatei' and CONFIG_VALUE='Argentina'");
 
 		// Apply only once
 		if (!alreadyApplied) {
 			dbZugriff.saveUserParameter("updateCheck", "true");
 			dbZugriff.saveUserParameter("newsCheck", "true");
 
 			// Drop the feedback tables to force new feedback upload
 			m_clJDBCAdapter.executeUpdate("DROP TABLE IF EXISTS FEEDBACK_SETTINGS");
 			m_clJDBCAdapter.executeUpdate("DROP TABLE IF EXISTS FEEDBACK_UPLOAD");
 		}
 
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.425);
 	}
 	
 	private void updateConfigTo1429 (boolean alreadyApplied) {
 		
 		if (!alreadyApplied){
 			resetTrainingParameters ();
 		}
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.429);
 	}
 	
 	private void updateConfigTo1431 (boolean alreadyApplied) {
 		
 		if (!alreadyApplied){
 			try {
 				HOLogger.instance().debug(getClass(), "Reseting player overview rows.");
 				String sql = "DELETE FROM USERCOLUMNS WHERE COLUMN_ID BETWEEN 2000 AND 3000";
 				m_clJDBCAdapter.executeQuery(sql);
 	
 				HOLogger.instance().debug(getClass(), "Reseting lineup overview rows.");
 				sql = "DELETE FROM USERCOLUMNS WHERE COLUMN_ID BETWEEN 3000 AND 4000";
 				m_clJDBCAdapter.executeQuery(sql);
 			} catch (Exception e) {
 				HOLogger.instance().debug(getClass(), "Error updating to config 1431: " + e.getMessage());
 			}
 		}
 		// always set the LastConfUpdate as last step
 		dbZugriff.saveUserParameter("LastConfUpdate", 1.431);
 	}
 	
 	private void resetTrainingParameters () {
 		// Reset Training Speed Parameters for New Training
 		// 1.429 training speed in db is now an offset
 		HOLogger.instance().info(this.getClass(), "Resetting training parameters to default values");
 		dbZugriff.saveUserParameter("DAUER_TORWART", 0.0);
 		dbZugriff.saveUserParameter("DAUER_VERTEIDIGUNG", 0.0);
 		dbZugriff.saveUserParameter("DAUER_SPIELAUFBAU", 0.0);
 		dbZugriff.saveUserParameter("DAUER_PASSPIEL", 0.0);
 		dbZugriff.saveUserParameter("DAUER_FLUEGELSPIEL", 0.0);
 		dbZugriff.saveUserParameter("DAUER_CHANCENVERWERTUNG", 0.0);
 		dbZugriff.saveUserParameter("DAUER_STANDARDS", 0.0);
 
 		dbZugriff.saveUserParameter("AlterFaktor", 0.0);
 		dbZugriff.saveUserParameter("TrainerFaktor", 0.0);
 		dbZugriff.saveUserParameter("CoTrainerFaktor", 0.0);
 		dbZugriff.saveUserParameter("IntensitaetFaktor", 0.0);
 	}
 
 	private void resetPredictionOffsets () {
 		// Reset Rating offsets for Rating Prediction
 		// because of changes in the prediction files
 		HOLogger.instance().info(this.getClass(), "Resetting rating prediction offsets");
 		dbZugriff.saveUserParameter("leftDefenceOffset", 0.0);
 		dbZugriff.saveUserParameter("middleDefenceOffset", 0.0);
 		dbZugriff.saveUserParameter("rightDefenceOffset", 0.0);
 		dbZugriff.saveUserParameter("midfieldOffset", 0.0);
 		dbZugriff.saveUserParameter("leftAttackOffset", 0.0);
 		dbZugriff.saveUserParameter("middleAttackOffset", 0.0);
 		dbZugriff.saveUserParameter("rightAttackOffset", 0.0);
 	}
 }
