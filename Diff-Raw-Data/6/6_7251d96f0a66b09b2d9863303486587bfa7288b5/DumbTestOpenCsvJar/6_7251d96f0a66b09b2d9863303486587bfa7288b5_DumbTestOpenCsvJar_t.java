 package org.jhuapl.edu.sages.etl.opencsvpods;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jhuapl.edu.sages.etl.SagesEtlException;
 import org.jhuapl.edu.sages.etl.strategy.ETLPostgresqlStrategy;
 import org.jhuapl.edu.sages.etl.strategy.SagesOpenCsvJar;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * This is the main class that runs the ETL
  * 
  * @author POKUAM1
  * @created Oct 4, 2011
  */
 public class DumbTestOpenCsvJar extends SagesOpenCsvJar {
 
 	private static Logger log = Logger.getLogger(DumbTestOpenCsvJar.class);
 	private static Logger prettyPrintLog = Logger.getLogger("HeadingPrinter");
 	private static Logger triageLog = Logger.getLogger("TriageLogger");
 
 	/**
 	 * @throws SagesEtlException
 	 */
 	public DumbTestOpenCsvJar() throws SagesEtlException {
 		super();
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws SagesEtlException, IOException {
 
 		
 		/**
 		 * 1. LOAD PROPERTIES 
 		 * 2. ESTABLISH DB CONNECTION 
 		 * 3. TOGGLE PROPS:
 		 * 	   TRANSACTION, LOGGING, STATS, ETC... 
 		 * 4. SET ETLSTRATEGY (*dpattern) 
 		 * 5. LOAD CSV FILES 
 		 * 6. ETL per CSV FILE 
 		 * 	(on success: copy out, delete from in ) COMMIT 
 		 * 	(on failure: copy fail, delete from in) ROLLBACK
 		 */
 
 		/** LOAD PROPERTIES *************************************************************/
 		DumbTestOpenCsvJar socj_dumb = new DumbTestOpenCsvJar();
 
 		// log.debug("Debug");
 		// log.info("Info");
 		// log.warn("Warn");
 		// log.error("Error");
 		// log.fatal("Fatal");
 
 		/** ESTABLISH DB CONNECTION *****************************************************/
 		Connection c = null;
 		c = socj_dumb.getConnection();
 
 		/** SET ETL Strategy *************************************************************/
 		socj_dumb.setEtlStrategy(new ETLPostgresqlStrategy(socj_dumb));
 
 		ArrayList<String[]> master_entries_rawdata = new ArrayList<String[]>();
 
 		try {
 			socj_dumb.extractHeaderColumns(socj_dumb);
 		} catch (FileNotFoundException e) {
 			log.fatal("problem occurred trying to process csv files:" + e.getMessage());
 		} catch (IOException e) {
 			log.fatal("problem occurred trying to process csv files:" + e.getMessage());
 		} catch (Exception e){
 			log.fatal("problem occurred trying to process csv files:" + e.getMessage());
 		} finally {
 			//TODO probaly should exit at this point
 		}
 
 		String DEBUGheader_src = StringUtils.join(socj_dumb.header_src);
 		log.debug("header_src:\n" + DEBUGheader_src + "\n");
 
 		/**
 		 * LOAD CSV FILES
 		 * *************************************************************** -
 		 * load into memory all csv files in the inputdir - each iteration is
 		 * new transaction so we can isolate erroneous files
 		 */
 			// build CLEANSE
 			// alter CLEANSE
 			// build STAGING
 			// alter STAGING
 			// build MAPPINGS
 			// insert into CLEANSE
 			// select insert into STAGING
 			// select insert into FINAL PRODUCTION
 	
 			// --CLEANUP--
 			// copy file to OUT|FAILED
 			// delete file from IN
 			// COMMIT
 		log.info("-- STEP 1 -- FILES NOW BEING PROCESSED SEQUENTIALLY");
 		for (File file : socj_dumb.csvFiles) {
 			int step = 1;
 			
 			Savepoint baseLine = null;
 			try {
 				baseLine = c.setSavepoint("baseLine");
 				socj_dumb.savepoints.put("baseLine", baseLine);
 			} catch (SQLException e) {
 				log.debug("Making the savepoint 'baseLine' failed.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, baseLine, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 			
 			prettyPrintLog.info("--STEP "+ (step++) +"-- TRUNCATING CLEANSE & STAGING TABLES");
 			try {
 				socj_dumb.truncateCleanseAndStagingTables(socj_dumb, c, null, baseLine);
 			} catch (SQLException e){
 				log.fatal("the rollbacks failed.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, baseLine, c, null, socj_dumb.getFaileddir_csvfiles(), e);
 				closeDbConnection(c);
 				System.exit(-1);
 			}
 			prettyPrintLog.info("--STEP "+ (step++) +"-- PROCESSING A FILE");
 
 			socj_dumb.setCurrentFile(file);
 			socj_dumb.setSuccess(true);
 
 			log.debug("FILE SEEN: " + file.getName() + "\n");
 
 			/** loading file into memory for ETL processing." **/
 			log.debug("loading " + file.getName() + " into memory for ETL processing.");
 			master_entries_rawdata.clear();
 			CSVReader reader_rawdata2 = new CSVReader(new FileReader(file));
 			socj_dumb.currentEntries = (ArrayList<String[]>) reader_rawdata2.readAll();
 			socj_dumb.currentEntries.remove(0);
 			/** remove the header row, already got it above */
 			master_entries_rawdata.addAll(socj_dumb.currentEntries);
 
 			ArrayList<String[]> entries_rawdata = master_entries_rawdata;
 			reader_rawdata2.close();
 
 			/***********************************
 			 * SAVEPOINT #1
 			 *********************************** 
 			 * before CLEANSE & STAGING built
 			 ***********************************/
 			log.debug("build CLEANSE");
 
 			Savepoint save1 = null;
 			try {
 				save1 = c.setSavepoint("save1");
 			} catch (SQLException e1) {
 				log.debug("making savepoint 'save1' failed.");
 				e1.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save1, c, file, socj_dumb.getFaileddir_csvfiles(), e1);
 			}
 
 			Savepoint createCleanseSavepoint = null;
 			try {
 				createCleanseSavepoint = socj_dumb.buildCleanseTable(c, socj_dumb, save1);
 				socj_dumb.savepoints.put("createCleanseSavepoint", createCleanseSavepoint);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				log.debug("the rollbacks failed.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save1, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("alter CLEANSE");
 			try {
 				socj_dumb.alterCleanseTableAddFlagColumn(c, save1, createCleanseSavepoint);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save1, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("build STAGING");
 			Savepoint createStagingSavepoint = null;
 
 			try {
 				createStagingSavepoint = socj_dumb.buildStagingTable(c,socj_dumb, save1);
 				socj_dumb.savepoints.put("createStagingSavepoint", createStagingSavepoint);
 			} catch (SQLException e) {
 				log.debug("rollback failed.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save1, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("alter STAGING");
 			try {
 				socj_dumb.alterStagingTableAddFlagColumn(c, save1,
 						createStagingSavepoint);
 			} catch (SQLException e) {
 				log.debug("rollback failed.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save1, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("build MAPPINGS");
 			/**
 			 * This is defined in the file:
 			 * "src-to-dst-column-mappings.properties" and it must be configured
 			 * per installation
 			 */
 			socj_dumb.generateSourceDestMappings(socj_dumb);
 			log.debug("MAPPING KEYS: " + StringUtils.join(socj_dumb.MAPPING_MAP.keySet(), ","));
 			log.debug("MAPPING VALUES: " + StringUtils.join(socj_dumb.MAPPING_MAP.values(), ","));
 
 			/*****************************************
 			 * SAVEPOINT #2
 			 ***************************************** 
 			 * before INSERT csv records into CLEANSE
 			 *****************************************/
 			Savepoint save2 = null;
 			try {
 				save2 = c.setSavepoint("save2");
 				socj_dumb.savepoints.put("save2", save2);
 			} catch (SQLException e1) {
 				log.debug("making savepoint 'save2' failed.");
 				e1.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e1);
 			}
 
 			log.debug("insert into CLEANSE");
 			PreparedStatement ps_INSERT_CLEANSE = null;
 
 			try {
 				ps_INSERT_CLEANSE = c.prepareStatement(socj_dumb.buildInsertIntoCleansingTableSql(c, socj_dumb));
 			} catch (SQLException e) {
 				log.debug("error creating prepared statement");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			try {
 				socj_dumb.setAndExecuteInsertIntoCleansingTablePreparedStatement(c, socj_dumb, entries_rawdata, save2,ps_INSERT_CLEANSE);
 			} catch (SQLException e) {
 				log.debug("error setting params and executing the insert into cleanse prep stmt.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			prettyPrintLog.info("--STEP "+ (step++) +"-- RUNNING CUSTOM SQL AGAINST CLEANSING");
 			
 			/**
 			 * 
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
 			 *   INJECT THE CUSTOM SQL AGAINST THE CLEANSE TABLE HERE
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			 * 
 			 * */
 			try {
 				runCustomSql(c, socj_dumb.props_customsql_cleanse, socj_dumb.src_table_name);
 			} catch (Exception e) {
 				triageLog.debug("ETL_LOGGER: failure with custom sql against cleansing.");
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("(copying from CLEANSE to STAGING) select insert into STAGING");
 
 			try {
 				// IF ERROR OCCURS INSIDE THIS GUY, COME OUT AND DO ERROR
 				// CLEANUP. FILE IS BAD. ROLLBACK. PROCEED.
 				socj_dumb.copyFromCleanseToStaging(c, socj_dumb, save2);
 			} catch (SQLException e) {
 				log.debug("error occured copying from CLEANSE to STAGING.");
 				e.printStackTrace();
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			} catch (SagesEtlException e) {
 				log.debug("error occured copying from CLEANSE to STAGING.");
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("FINISHED MESSY STUFF. ALMOST DONE...\n\n");
 			prettyPrintLog.info("--STEP "+ (step++) +"-- RUNNING CUSTOM SQL AGAINST STAGING");
 
 			/** TODO
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
 			 *   INJECT THE CUSTOM SQL AGAINST THE STAGING TABLE HERE
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			 **/
 
 			try {
 				runCustomSql(c, socj_dumb.props_customsql_staging, socj_dumb.dst_table_name);
 			} catch (Exception e) {
 				triageLog.debug("ETL_LOGGER: failure with custom sql against staging.");
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			log.debug("select insert into FINAL PRODUCTION");
 
 			prettyPrintLog.info("--STEP "+ (step++) +"-- FINAL LOAD INTO THE PRODUCTION TABLE STARTING.");
 			/**
 			 * TODO
 			 * 
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			 *  INJECT THE CUSTOM TRANSFER SQL AGAINST THE FINAL PRODUCTION TABLE HERE
 			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			 * 
 			 *  Takes the syntax: 
 			 *   INSERT INTO $staging_table ($columnlist) SELECT $prodcolumnlist FROM $staging_table
 			 **/
 
 			socj_dumb.prod_table_name = socj_dumb.props_etlconfig.getProperty("productionTableName");
 			String insertSelect_Production = "INSERT INTO " + socj_dumb.prod_table_name 
 					+ " (_columnlist_) SELECT _columnlist_ FROM "
 					+ socj_dumb.dst_table_name;
 
 			// TODO: don't want to build this each time. enhance
 			String columnlist = StringUtils.join(socj_dumb.DEST_COLTYPE_MAP.keySet(), ",");
 			log.debug("ETL_LOGGER(columnList): " + columnlist);
 
 			insertSelect_Production = insertSelect_Production.replaceAll("_columnlist_", columnlist);
 			log.debug("ETL_LOGGER(insertSelect_Production): " + insertSelect_Production);
 
 			try {
 				PreparedStatement ps_finalToProd = c.prepareStatement(insertSelect_Production);
 				ps_finalToProd.execute();
 			} catch (Exception e) {
 				log.error("ETL_LOGGER: OOOOOOPS SOMETHING HAPPENED BAD IN THE END. SHUCKS");
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			try {
 				Properties customProdLoaderSqlprops = socj_dumb.props_customsql_final_to_prod;
 				int numSql3 = customProdLoaderSqlprops.size();
 				for (int i = 1; i <= numSql3; i++) {
 					String sql = customProdLoaderSqlprops.getProperty(String.valueOf(i));
 					sql = sql.replace("$table", socj_dumb.dst_table_name);
 					log.debug("CUSTOM SQL: " + sql);
 					PreparedStatement ps = c.prepareStatement(sql);
 					ps.execute();
 				}
 			} catch (Exception e) {
 				triageLog.debug("ETL_LOGGER: failure with custom sql final to prod: " + e.getMessage());
 				socj_dumb.errorCleanup(socj_dumb, save2, c, file, socj_dumb.getFaileddir_csvfiles(), e);
 			}
 
 			prettyPrintLog.info("--STEP "+ (step++) + "---CLEANUP--");
 			File fileRefForStatusLogging = null; 
 			try {
 				if (socj_dumb.isSuccess()) {
 					log.debug("copy file to 'OUT' DIRECTORY");
 					/** Destination dir */
 					File destinationDir = new File(socj_dumb.outputdir_csvfiles);
 					// ON FAILURE: destinationDir = faileddir_csvfiles -- this
 					// was done in the errorCleanup method
 					log.debug("delete file from 'IN' DIRECTORY");
 					fileRefForStatusLogging = etlMoveFile(file, destinationDir);
 				} else {
 					log.debug("fyi, was an unsuccessful run. errorcleanup already occurred.");
 				}
 			} catch (IOException io) {
 				triageLog.fatal("ALERT PROBLEM DELETING FILE: " + io.getMessage());
 				logFileOutcome(socj_dumb, c, fileRefForStatusLogging, "FAILURE: " + io.getMessage());
 				closeDbConnection(c);
 				System.exit(-1);
 			} finally {
 				try {
					// is null if success=false
					if (fileRefForStatusLogging != null) logFileOutcome(socj_dumb, c, fileRefForStatusLogging, "SUCCESS");

 					c.commit();
 					Savepoint finalCommit = c.setSavepoint("finalCommit");
 					socj_dumb.savepoints.put("finalCommit", finalCommit);
 					prettyPrintLog.info("--STEP "+ (step++) +"-- COMMITED");
 				} catch (SQLException e) {
 					triageLog.fatal("UNEXPECTEDLY COULD NOT COMMIT CHANGES TO THE DATABASE. THIS IS BAD.");
 					try {
 						logFileOutcome(socj_dumb, c, fileRefForStatusLogging, "FAILURE: " + e.getMessage());
 					} catch (SagesEtlException e2){
 						//???? this bombed when trying to log file processing stats... 
 						triageLog.error(e2.getMessage());
 					}
 					closeDbConnection(c);
 					System.exit(-1);
 				}
 			}
 		}
 
 		closeDbConnection(c);
 	}
 }
