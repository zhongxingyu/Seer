 package nl.minicom.evenexus.persistence.versioning;
 
 import org.hibernate.Session;
 
 
 public class StructureUpgrader extends RevisionCollection {
 
 	public StructureUpgrader() {
 		super("database");
 		
 		super.registerRevision(new Revision(3) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS transactions (");
 				builder.append("transactionID BIGINT unsigned NOT NULL,");
 				builder.append("characterID BIGINT unsigned NOT NULL,");
 				builder.append("transactionDateTime TIMESTAMP unsigned NOT NULL,");
 				builder.append("quantity BIGINT unsigned NOT NULL,");
 				builder.append("typeName VARCHAR(255) NOT NULL,");
 				builder.append("typeID BIGINT unsigned NOT NULL,");
 				builder.append("price DECIMAL(20,2) NOT NULL,");
 				builder.append("taxes DECIMAL(20,2) NOT NULL,");
 				builder.append("clientID BIGINT unsigned NOT NULL,");
 				builder.append("clientName VARCHAR(255) NOT NULL,");
 				builder.append("stationID BIGINT unsigned NOT NULL,");
 				builder.append("stationName VARCHAR(255) NOT NULL,");
 				builder.append("isPersonal INT(1) unsigned NOT NULL,");
 				builder.append("PRIMARY KEY (`transactionID`))");				
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(4) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP INDEX IF EXISTS `transactions_typeName`");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(5) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX ");
 				builder.append("IF NOT EXISTS transactions_IX_typeName ");
 				builder.append("ON `transactions` (`typeName`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(6) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS apilogger (");
 				builder.append("type VARCHAR(255) NOT NULL,");
 				builder.append("charID BIGINT NOT NULL,");
 				builder.append("lastRun TIMESTAMP NOT NULL,");
 				builder.append("PRIMARY KEY (`type`, `charID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(7) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS journal (");
 				builder.append("refID BIGINT unsigned NOT NULL,");
 				builder.append("journalTypeID INT unsigned NOT NULL,");
 				builder.append("date TIMESTAMP unsigned NOT NULL,");
 				builder.append("ownerName1 VARCHAR(255) NULL,");
 				builder.append("ownerID1 BIGINT unsigned NULL,");
 				builder.append("ownerName2 VARCHAR(255) NULL,");
 				builder.append("ownerID2 BIGINT unsigned NULL,");
 				builder.append("argName1 VARCHAR(255) NULL,");
 				builder.append("argID1 BIGINT unsigned NOT NULL,");
 				builder.append("amount DECIMAL(20,2) NOT NULL,");
 				builder.append("balance DECIMAL(20,2) NOT NULL,");
 				builder.append("reason VARCHAR(255) NULL,");
 				builder.append("taxReceiverID BIGINT DEFAULT NULL,");
 				builder.append("taxAmount DECIMAL(20,2) DEFAULT NULL,");
 				builder.append("PRIMARY KEY (`refID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(8) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP INDEX IF EXISTS `journal_date`");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(9) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP INDEX IF EXISTS `journal_journalTypeID`");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(10) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS journal_IX_journalTypeID ");
 				builder.append("ON `journal` (`journalTypeID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(11) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS journal_IX_date ");
 				builder.append("ON `journal` (`date`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(12) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS profit (");
 				builder.append("transactionID BIGINT unsigned NOT NULL,");
 				builder.append("typeID BIGINT unsigned NOT NULL,");
 				builder.append("typeName VARCHAR(255) NOT NULL,");
 				builder.append("date TIMESTAMP unsigned NOT NULL,");
 				builder.append("quantity BIGINT unsigned NOT NULL,");
 				builder.append("value DECIMAL(20,2) NOT NULL,");
 				builder.append("taxes DECIMAL(20,2) NOT NULL,");
 				builder.append("PRIMARY KEY (`transactionID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(13) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP INDEX IF EXISTS `profit_date`");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(14) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP INDEX IF EXISTS `profit_typeID`");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(15) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS profit_IX_typeID ");
 				builder.append("ON `profit` (`typeID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(16) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS profit_IX_date ");
 				builder.append("ON `profit` (`date`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(17) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS apisettings (");
 				builder.append("name VARCHAR(64) NOT NULL,");
 				builder.append("userID BIGINT unsigned NOT NULL,");
 				builder.append("apiKey VARCHAR(64) NOT NULL,");
 				builder.append("charID BIGINT unsigned NOT NULL,");
 				builder.append("PRIMARY KEY (`charID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(18) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS skills (");
 				builder.append("characterID BIGINT unsigned NOT NULL,");
 				builder.append("typeID BIGINT unsigned NOT NULL,");
 				builder.append("level INT unsigned NOT NULL,");
 				builder.append("PRIMARY KEY (`characterID`, `typeID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(19) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS standings (");
 				builder.append("characterID BIGINT unsigned NOT NULL,");
 				builder.append("fromID BIGINT unsigned NOT NULL,");
 				builder.append("fromName VARCHAR(255) NULL,");
 				builder.append("standing DECIMAL(4, 2) NOT NULL,");
 				builder.append("PRIMARY KEY (`characterID`, `fromID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(20) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS marketorders (");
 				builder.append("orderID BIGINT unsigned NOT NULL,");
 				builder.append("charID BIGINT unsigned NOT NULL,");
 				builder.append("stationID BIGINT unsigned NOT NULL,");
 				builder.append("volEntered BIGINT unsigned NOT NULL,");
 				builder.append("volRemaining BIGINT unsigned NOT NULL,");
 				builder.append("minVolume BIGINT unsigned NOT NULL,");
 				builder.append("orderState INT unsigned NOT NULL,");
 				builder.append("typeID BIGINT unsigned NOT NULL,");
 				builder.append("range INT NOT NULL,");
 				builder.append("accountKey INT unsigned NOT NULL,");
 				builder.append("duration INT unsigned NOT NULL,");
 				builder.append("escrow DECIMAL(20,2) NOT NULL,");
 				builder.append("price DECIMAL(20,2) NOT NULL,");
 				builder.append("bid TINYINT NOT NULL,");
 				builder.append("issued TIMESTAMP NOT NULL,");
 				builder.append("PRIMARY KEY (`orderID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(21) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS importlogger (");
 				builder.append("importer BIGINT NOT NULL,");
 				builder.append("characterID VARCHAR(255) NOT NULL,");
 				builder.append("lastrun TIMESTAMP NULL,");
 				builder.append("PRIMARY KEY (`importer`, `characterID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 				
 		super.registerRevision(new Revision(22) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS importers (");
 				builder.append("id BIGINT NOT NULL,");
 				builder.append("name VARCHAR(255) NOT NULL,");
 				builder.append("cooldown BIGINT NOT NULL,");
 				builder.append("path VARCHAR(255) NOT NULL,");
 				builder.append("PRIMARY KEY (`id`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(23) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("MERGE INTO `importers` (`id`, `name`, `cooldown`, `path`) VALUES ");
 				builder.append("(1, 'Character list', 0, '/account/Characters.xml.aspx'), ");
 				builder.append("(2, 'Character account balance', 0, '/char/AccountBalance.xml.aspx'), ");
 				builder.append("(3, 'Character skills', 21660000, '/char/CharacterSheet.xml.aspx'), ");
 				builder.append("(4, 'Character standings', 21660000, '/char/Standings.xml.aspx'), ");
 				builder.append("(5, 'Character wallet transactions', 3660000, '/char/WalletTransactions.xml.aspx'),");
 				builder.append("(6, 'Character journal entries', 3660000, '/char/WalletJournal.xml.aspx'),");
 				builder.append("(7, 'Character market orders', 3660000, '/char/MarketOrders.xml.aspx')");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(25) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("DROP TABLE IF EXISTS apilogger");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(26) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS reftypes (");
 				builder.append("refTypeID INT(10) unsigned NOT NULL,");
 				builder.append("description VARCHAR(255) NOT NULL,");
 				builder.append("PRIMARY KEY (`refTypeID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		/*
 		 *  Revision 27 - inserted content into the reftypes table.
 		 *  TODO: This should be done using the updating service...  
 		 */
 		
 		super.registerRevision(new Revision(28) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS invtypes (");
 				builder.append("typeID BIGINT NOT NULL,");
 				builder.append("groupID BIGINT DEFAULT NULL,");
 				builder.append("typeName VARCHAR(255) DEFAULT NULL,");
 				builder.append("description TEXT DEFAULT NULL,");
 				builder.append("graphicID BIGINT DEFAULT NULL,");
 				builder.append("radius DOUBLE DEFAULT NULL,");
 				builder.append("mass DOUBLE DEFAULT NULL,");
 				builder.append("volume DOUBLE DEFAULT NULL,");
 				builder.append("capacity DOUBLE DEFAULT NULL,");
 				builder.append("portionSize BIGINT DEFAULT NULL,");
 				builder.append("raceID INT unsigned DEFAULT NULL,");
 				builder.append("basePrice DOUBLE DEFAULT NULL,");
 				builder.append("published TINYINT(1) DEFAULT NULL,");
 				builder.append("marketGroupID BIGINT DEFAULT NULL,");
 				builder.append("chanceOfDuplicating DOUBLE DEFAULT NULL,");
 				builder.append("PRIMARY KEY (`typeID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(29) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS invtypes_IX_groupID ");
 				builder.append("ON `invtypes` (`groupID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(30) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS invtypes_IX_graphicID ");
 				builder.append("ON `invtypes` (`graphicID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(31) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS invtypes_IX_raceID ");
 				builder.append("ON `invtypes` (`raceID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(32) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS invtypes_IX_marketGroupID ");
 				builder.append("ON `invtypes` (`marketGroupID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		/*
 		 *  Revisions 33 to 126 - inserted content into the invtypes table.
 		 *  TODO: This should be done using the updating service...  
 		 */
 		
 		super.registerRevision(new Revision(127) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS mapregions (");
 				builder.append("regionID BIGINT NOT NULL,");
 				builder.append("regionName VARCHAR(255) DEFAULT NULL,");
 				builder.append("x DOUBLE DEFAULT NULL,");
 				builder.append("y DOUBLE DEFAULT NULL,");
 				builder.append("z DOUBLE DEFAULT NULL,");
 				builder.append("xMin DOUBLE DEFAULT NULL,");
 				builder.append("xMax DOUBLE DEFAULT NULL,");
 				builder.append("yMin DOUBLE DEFAULT NULL,");
 				builder.append("yMax DOUBLE DEFAULT NULL,");
 				builder.append("zMin DOUBLE DEFAULT NULL,");
 				builder.append("zMax DOUBLE DEFAULT NULL,");
 				builder.append("factionID BIGINT DEFAULT NULL,");
 				builder.append("radius DOUBLE DEFAULT NULL,");
 				builder.append("PRIMARY KEY (`regionID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(128) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS mapregions_IX_factionID ");
 				builder.append("ON `mapregions` (`factionID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(130) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS stastations (");
 				builder.append("stationID BIGINT NOT NULL,");
 				builder.append("security INT DEFAULT NULL,");
 				builder.append("dockingCostPerVolume DOUBLE DEFAULT NULL,");
 				builder.append("maxShipVolumeDockable DOUBLE DEFAULT NULL,");
 				builder.append("officeRentalCost BIGINT DEFAULT NULL,");
 				builder.append("operationID INT unsigned DEFAULT NULL,");
 				builder.append("stationTypeID BIGINT DEFAULT NULL,");
 				builder.append("corporationID BIGINT DEFAULT NULL,");
 				builder.append("solarSystemID BIGINT DEFAULT NULL,");
 				builder.append("constellationID BIGINT DEFAULT NULL,");
 				builder.append("regionID BIGINT DEFAULT NULL,");
 				builder.append("stationName VARCHAR(255) DEFAULT NULL,");
 				builder.append("x DOUBLE DEFAULT NULL,");
 				builder.append("y DOUBLE DEFAULT NULL,");
 				builder.append("z DOUBLE DEFAULT NULL,");
 				builder.append("reprocessingEfficiency DOUBLE DEFAULT NULL,");
 				builder.append("reprocessingStationsTake DOUBLE DEFAULT NULL,");
 				builder.append("reprocessingHangarFlag INT DEFAULT NULL,");
 				builder.append("PRIMARY KEY (`stationID`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(131) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_constellation ");
 				builder.append("ON `stastations` (`constellationID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(132) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_corporation ");
 				builder.append("ON `stastations` (`corporationID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(133) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_operation ");
 				builder.append("ON `stastations` (`operationID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(134) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_region ");
 				builder.append("ON `stastations` (`regionID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(135) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_system ");
 				builder.append("ON `stastations` (`solarSystemID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(136) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_type ");
 				builder.append("ON `stastations` (`stationTypeID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(137) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE INDEX IF NOT EXISTS staStations_IX_solarSystemID ");
 				builder.append("ON `stastations` (`solarSystemID`,`constellationID`,`regionID`)");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		/*
 		 *  Revisions 138 to 158 - inserted content into the stastations table.
 		 *  TODO: This should be done using the updating service...  
 		 */
 		
 		super.registerRevision(new Revision(159) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE MARKETORDERS ALTER COLUMN ");
 				builder.append("CHARID BIGINT(19) UNSIGNED NOT NULL");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(160) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("TRUNCATE TABLE PROFIT").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(161) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE profit DROP COLUMN transactionID");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(162) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE profit ADD COLUMN ");
 				builder.append("buyTransactionID BIGINT unsigned NOT NULL");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 
 		super.registerRevision(new Revision(163) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE profit ADD COLUMN ");
 				builder.append("sellTransactionID BIGINT unsigned NOT NULL");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(164) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE transactions ADD COLUMN ");
 				builder.append("remaining BIGINT unsigned DEFAULT 0 NOT NULL BEFORE typename");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(165) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("UPDATE transactions SET remaining = quantity");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(166) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE importlogger ALTER COLUMN ");
 				builder.append("characterid BIGINT unsigned NOT NULL");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(167) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN security").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(168) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN dockingcostpervolume").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(169) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN maxshipvolumedockable").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(170) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN officerentalcost").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(171) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN operationid").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(172) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN x").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(173) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN y").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(174) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN z").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(175) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN reprocessingefficiency").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(176) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN reprocessingstationstake").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(177) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE stastations DROP COLUMN reprocessinghangarflag").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(178) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN x").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(179) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN y").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(180) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN z").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(181) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN xmin").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(182) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN ymin").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(183) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN zmin").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(184) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN xmax").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(185) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN ymax").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(186) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN zmax").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(187) {
 			@Override
 			public void execute(Session session) {
 				session.createSQLQuery("ALTER TABLE mapregions DROP COLUMN radius").executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(188) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("MERGE INTO `importers` (`id`, `name`, `cooldown`, `path`) VALUES ");
 				builder.append("(8, 'Journal reference types', 3600000, '/eve/RefTypes.xml.aspx')");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		super.registerRevision(new Revision(189) {
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("CREATE TABLE IF NOT EXISTS bugreports (");
 				builder.append("issueNumber BIGINT NOT NULL, ");
 				builder.append("PRIMARY KEY (`issueNumber`))");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 		/*
 		 *  Revisions 190 to 191 - added buy and sell price to profit table
 		 */
 		
 		super.registerRevision(new Revision(190) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE profit ADD COLUMN ");
 				builder.append("buyprice DECIMAL(20,2) ");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 
		super.registerRevision(new Revision(190) {
 			@Override
 			public void execute(Session session) {
 				StringBuilder builder = new StringBuilder();
 				builder.append("ALTER TABLE profit ADD COLUMN ");
 				builder.append("sellprice DECIMAL(20,2) ");
 				session.createSQLQuery(builder.toString()).executeUpdate();
 			}
 		});
 		
 	}
 	
 	@Override
 	public void registerRevision(IRevision revision) {
 		throw new UnsupportedOperationException("Not allowed to add new revisions this way!");
 	}
 
 }
