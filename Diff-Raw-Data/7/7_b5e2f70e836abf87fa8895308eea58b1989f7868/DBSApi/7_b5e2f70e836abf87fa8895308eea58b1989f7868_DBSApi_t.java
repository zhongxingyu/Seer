 /**
 $Revision: 1.29 $"
 $Id: DBSApi.java,v 1.29 2006/12/04 19:02:23 sekhri Exp $"
  *
 */
 
 package dbs.api;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.io.Writer;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import xml.DBSXMLParser;
 import xml.Element;
 import xml.XMLException;
 import db.DBManagement;
 import dbs.DBSConstants;
 import dbs.sql.DBSSql;
 import dbs.DBSException;
 import dbs.util.DBSUtil;
 import db.DBManagement;
 import java.sql.ResultSet;
 import dbs.util.DBSConfig;
 
 public class DBSApi {
 	/**
 	 * 
 	 */
 	private DBSApiLogic api;
 	
         public Vector supportedClientApiVersions() throws Exception {
 		//FIXME ASK Anzar  to use some conventions in declaing vaiables.
                 DBSConfig config = DBSConfig.getInstance();
                 String clientVers = config.getSupportedClientVersions(); 
                 String[] result = clientVers.split(","); 
                 //String[] result = clientVers.split("\\s"); 
                 //FIXME Turning array into a verstor is just something not required, fix this later 
 		Vector supported_version_list  = new Vector();
                 for (int x=0; x<result.length; x++) {
                     supported_version_list.add(result[x].trim());
                 }
 		return supported_version_list;
 
         }
 
 	public String supportedSchemaVersions() throws Exception {
                //Get Schema Version from DBSConfig
                DBSConfig config = DBSConfig.getInstance();
                String suppSchemaVer = config.getSupportedSchemaVersion();
 	       return suppSchemaVer;
 	}
 
         //This func must move to Utility
         private boolean isIn(String param, Enumeration e) {
                 while (e.hasMoreElements()) {
                         if( param.equals((String)e.nextElement()) ) {
                                 return true;
                         }
                 }
                 return false;
         }
 
 	public void checkSchemaVersion() throws Exception {
 		Connection conn =  getConnection();
 		try {
 			String sql = "select SchemaVersion from SchemaVersion";
 			System.out.println(sql);
 			ResultSet rs =  DBSSql.getSchemaVersion(conn).executeQuery();
 			String dbsSchemaVersion="";
 			if(rs.next()) {
 				dbsSchemaVersion = rs.getString("SchemaVersion");
 			} else {
 				throw new DBSException("Schema Version Failure", "1001", "Unable to get Schema Version from Database, cannot continue");
 			} 
 			String suppSchemaVer = supportedSchemaVersions();
 			if (! dbsSchemaVersion.equals(suppSchemaVer) ) {
				throw new DBSException("Unsupported Schema version", "1002", "Database Schema Mismatch, $DBS_CONFIG Version is " + suppSchemaVer + " Current schema version in DB is :" + dbsSchemaVersion); 
 			}
 		} finally {
 			if(conn != null) conn.close();
                 }
         }  
 
         public void checkVersion(String apiversion) throws Exception {
 		//FIXME I dont think this api check is does anything. Anyways it has to move in the call method insated of the constructor
                 Enumeration verEnum = supportedClientApiVersions().elements();
                 String msg  = "Incorrect API version specified '"+apiversion+"'";
                        msg += " Supported versions are: ";
 
                 for (Enumeration e = supportedClientApiVersions().elements() ; 
                                  e.hasMoreElements() ;) {
                     msg += " "+(String)e.nextElement();
                 }
 
                 if ( ! isIn(apiversion, verEnum ) ) {
                         throw new DBSException("BAD Api Version", "1003",  msg);
                         //return;
                 }
 
 
                 //Now lets check the Schema version too !!!!!!!!!!!!
         }
 
         public DBSApi() {
                 api = new DBSApiLogic();
 	}
 
 	public void call(Writer out, Hashtable table, Hashtable dbsUser) throws Exception {
                 
 		Connection conn = null;
 
 		try {
 
 			out.write(DBSConstants.XML_HEADER); 
 			String apiStr = get(table, "api", true);
         	        String apiVersion = get(table, "apiversion", true);
                 	System.out.println("apiStr: "+apiStr);
 
 	                checkVersion(get(table, "apiversion", true));
  
         	        checkSchemaVersion();
 
 
 			conn = getConnection();
 			conn.setAutoCommit(false);
 			if (apiStr.equals("listPrimaryDatasets")) {
 				api.listPrimaryDatasets(conn, out, get(table, "pattern", false));
 				
 			} else if (apiStr.equals("listProcessedDatasets")) {
 				api.listProcessedDatasets(conn, out, 
 						get(table, "primary_datatset_name_pattern", false),
 						get(table, "data_tier_name_pattern", false),
 						get(table, "processed_datatset_name_pattern", false),
 						get(table, "app_version", false),
 						get(table, "app_family_name", false),
 						get(table, "app_executable_name", false),
 						get(table, "parameterset_name", false)
 						);
 				
 			} else if (apiStr.equals("listAlgorithms")) {
 				api.listAlgorithms(conn, out,
 						get(table, "app_version", false),
 						get(table, "app_family_name", false),
 						get(table, "app_executable_name", false),
 						get(table, "parameterset_name", false)
 						);
 				
 			} else if (apiStr.equals("listRuns")) {
 				api.listRuns(conn, out, get(table, "path", true));
 				
 			} else if (apiStr.equals("listTiers")) {
 				api.listTiers(conn, out, get(table, "path", true));
 				
 			} else if (apiStr.equals("listBlocks")) {
 				api.listBlocks(conn, out, get(table, "path", true));
 				
 			} else if (apiStr.equals("listFiles")) {
 				api.listFiles(conn, out, 
 						get(table, "path", false),
 						get(table, "block_name", false),
 						get(table, "pattern_lfn", false)
 						);
 				
 			} else if (apiStr.equals("insertPrimaryDataset")) {
 				api.insertPrimaryDataset(conn, out,
 						parse( getXml(table), "primary-dataset") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertAlgorithm")) {
 				api.insertAlgorithm(conn, out,
 						parse(getXml(table), "algorithm") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertRun")) {
 				api.insertRun(conn, out,
 						parse(getXml(table), "run") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertTier")) {
 				api.insertTier(conn, out, get(table, "tier_name", true), dbsUser);
 				
 			} else if (apiStr.equals("insertLumiSection")) {
 				api.insertLumiSection(conn, out,
 						parse(getXml(table), "lumi") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertProcessedDataset")) {
 				api.insertProcessedDataset(conn, out,  parsePD(getXml(table)), dbsUser);
 				
 			} else if (apiStr.equals("insertBlock")) {
 				api.insertBlock(conn, out,
 						parse(getXml(table), "block") , 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertFiles")) {
 				insertFiles(conn, out, getXml(table), dbsUser);
 				
 			} else if (apiStr.equals("insertTierInPD")) {
 				api.insertTierInPD(conn, out,
 						get(table, "path", true), 
 						get(table, "tier_name", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertParentInPD")) {
 				api.insertParentInPD(conn, out,
 						get(table, "path", true), 
 						get(table, "parent_path", true), 
 						dbsUser);
 					
 			} else if (apiStr.equals("insertAlgoInPD")) {
 				api.insertAlgoInPD(conn, out,
 						get(table, "path", true), 
 						parse(getXml(table), "algorithm"), 
 						dbsUser);
 			
 			} else if (apiStr.equals("insertRunInPD")) {
 				api.insertRunInPD(conn, out,
 						get(table, "path", true), 
 						get(table, "run_number", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertTierInFile")) {
 				api.insertTierInFile(conn, out,
 						get(table, "lfn", true), 
 						get(table, "tier_name", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertParentInFile")) {
 				api.insertParentInFile(conn, out,
 						get(table, "lfn", true), 
 						get(table, "parent_lfn", true), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertAlgoInFile")) {
 				api.insertAlgoInFile(conn, out,
 						get(table, "lfn", true), 
 						parse(getXml(table), "algorithm"), 
 						dbsUser);
 				
 			} else if (apiStr.equals("insertLumiInFile")) {
 				api.insertLumiInFile(conn, out,
 						get(table, "lfn", true), 
 						get(table, "ls_number", true), 
 						dbsUser);
 				
 			} else {
 				writeException(out, "Invalid API", "1018", "The api " + apiStr + " provided by the client is not valid");
 				return;
 			}
 			conn.commit();
 			
 		} catch (DBSException dbsEx) {
 			if(conn != null) conn.rollback();
 			writeException(out, dbsEx.getMessage(), dbsEx.getCode(), dbsEx.getDetail());
 			//dbsEx.printStackTrace();
 			return;
 		} catch (XMLException xmlEx) {
 			if(conn != null) conn.rollback();
 			writeException(out, xmlEx.getMessage(), xmlEx.getCode(), xmlEx.getDetail());
 			//xmlEx.printStackTrace();
 			return;
 		} catch (SQLException sqlEx) {
 			if(conn != null) conn.rollback();
 			writeException(out, "Database exception", "2000", sqlEx.getMessage());
 			//sqlEx.printStackTrace();
 			return;
 		} catch (Exception ex) {
 			if(conn != null) conn.rollback();
 			writeException(out, "Unexpected execution exception", "4000", ex.getMessage());
 			//ex.printStackTrace();
 			return;
 		} finally {
 			if(conn != null) conn.close();
 		}
 		out.write(DBSConstants.XML_SUCCESS);
 		out.write(DBSConstants.XML_FOOTER);
 		out.flush();
 	}
 	
 	private String get(Hashtable table, String key, boolean excep) throws Exception {
 		String value = "";
 		if ( isNull(value = DBSUtil.get(table, key)) ) {
 			if (excep) throw new DBSException("Mandatory data missing", "1004", "Null Fields. Expected a valid " + key);
 		}
 		return value;
 	}
 	
 	
 	private String getXml(Hashtable table) throws Exception {
 		return 	get(table, "xmlinput", true);
 	}
 	
 	public void writeException(Writer out, String message, String code, String detail) throws Exception {
 		//out.write(DBSConstants.XML_EXCEPTION_HEADER); 
                 message = message.replace('\'',' ');
                 detail= detail.replace('\'',' ');
                 code =code.replace('\'',' ');
 		out.write("<exception message='" + message + "' "); 
 		out.write(" code ='" + code + "' "); 
 		out.write(" detail ='" + detail + "' />\n"); 
 		out.write(DBSConstants.XML_FOOTER);
 		out.flush();
 		//out.write(DBSConstants.XML_EXCEPTION_FOOTER); 
 	}
 
 
 	/*
 	
 	private void writeWarnning(Writer out, String message) throws Exception {
 		out.write(DBSConstants.XML_WARNNING_HEADER); 
 		out.write(message); 
 		out.write(DBSConstants.XML_WARNNING_FOOTER); 
 	}*/
 
 	private Connection getConnection() throws Exception {
 		Connection conn = DBManagement.getDBConnManInstance().getConnection();
 		if (conn != null) {
        			System.out.println("Pooling at work");
 			return conn;
 		} else {
 			System.out.println("Pooling not required for standalone client");
 			DBSConfig config = DBSConfig.getInstance();
 			return DBManagement.getConnection( config.getDbDriver(),
 					config.getDbURL(), 
 					config.getDbUserName(),
 					config.getDbUserPasswd());
 					
 		}
 		
 	}
 	
 	public Hashtable parse(String inputXml, String key) throws Exception {
 		//	checkXML(inputXml);
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
  		Vector allElement = dbsParser.getElements();
 		Hashtable table = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals(key) ) {
 				//System.out.println("Found a " + key + " : " + name);  
 				table = e.attributes;
 			} 
 		}
 		return table;
 	}
 	
 	public Hashtable parsePD(String inputXml) throws Exception {
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		Vector allElement = dbsParser.getElements();
 		Hashtable psDS = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("processed-dataset") ) {
 				//System.out.println("Found a processed-dataset: " + name);  
 				psDS = e.attributes;
 				psDS.put("data_tier", new Vector());
 				psDS.put("parent", new Vector());
 				psDS.put("algorithm", new Vector());
 				psDS.put("run", new Vector());
 			} 
 			if (name.equals("data_tier") ) 
 				((Vector)(psDS.get("data_tier"))).add(e.attributes);
 			if (name.equals("parent") ) 
 				((Vector)(psDS.get("parent"))).add(e.attributes);
 			if (name.equals("algorithm") ) 
 				((Vector)(psDS.get("algorithm"))).add(e.attributes);
 			if (name.equals("run") ) 
 				((Vector)(psDS.get("run"))).add(e.attributes);
 				//System.out.println("Algorithm hashtable "+ (e.attributes).toString());
 		}
 		return psDS;
 	}
 	
 	public void insertFiles(Connection conn, Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Vector topLevel = new Vector();
 		int index = -1;
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
     		Vector allElement = dbsParser.getElements();
 		Hashtable psDS = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("file") ) {
 				//System.out.println("Found a file: " + name);  
 				Hashtable file = e.attributes;
 				file.put("lumi_section", new Vector());
 				file.put("data_tier", new Vector());
 				file.put("parent", new Vector());
 				file.put("algorithm", new Vector());
 				topLevel.add(file);
 				++index;
 			} 
 			if (name.equals("lumi_section") ) 
 				((Vector)(((Hashtable)topLevel.get(index)).get("lumi_section"))).add(e.attributes);
 			if (name.equals("data_tier") ) 
 				((Vector)(((Hashtable)topLevel.get(index)).get("data_tier"))).add(e.attributes);
 			if (name.equals("parent") ) 
 				((Vector)(((Hashtable)topLevel.get(index)).get("parent"))).add(e.attributes);
 			if (name.equals("algorithm") ) 
 				((Vector)(((Hashtable)topLevel.get(index)).get("algorithm"))).add(e.attributes);
 			if (name.equals("processed_datatset") ) {
 				psDS = e.attributes;
 			}
 		}
 		api.insertFiles(conn, out, DBSUtil.get(psDS, "path"), DBSUtil.get(psDS, "block_name"), topLevel, dbsUser);
 	}
 
 
 	
 	//All these api call are un necessary . The client can dierecly use the call method in this class. 
 	//The DBSTest client will aslo change and it will take command line arguments which will be converted into hashtable and 
 	// can be used for call method. So the following api calls are useless. They can go away.
 
 	/*
 	public void listPrimaryDatasets(Writer out, String pattern) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listPrimaryDataset");
 		put(table, "pattern", pattern);
 		call(out, table, null);
 	}
 	
 	public void listProcessedDatasets(Writer out, String patternPrim, String patternDT, String patternProc, String patternVer, String patternFam, String patternExe, String patternPS) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listProcessedDatasets");
 		put(table, "primary_datatset_name_pattern", patternPrim);
 		put(table, "data_tier_name_pattern", patternDT);
 		put(table, "processed_datatset_name_pattern", patternProc);
 		put(table, "app_version", patternVer);
 		put(table, "app_family_name", patternFam);
 		put(table, "app_executable_name", patternExe);
 		put(table, "parameterset_name", patternPS);
 		call(out, table, null);
 	}
 
 	public void listAlgorithms(Writer out, String patternVer, String patternFam, String patternExe, String patternPS) throws Exception {	
 		Hashtable table = new Hashtable();
 		put(table, "api", "listAlgorithms");
 		put(table, "app_version", patternVer);
 		put(table, "app_family_name", patternFam);
 		put(table, "app_executable_name", patternExe);
 		put(table, "parameterset_name", patternPS);
 		call(out, table, null);
 	}
 
 	public void listRuns(Writer out, String path) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listRuns");
 		put(table, "path", path);
 		call(out, table, null);
 	}
 
 	
 	public void listTiers(Writer out, String path) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listTiers");
 		put(table, "path", path);
 		call(out, table, null);
 	}
 
 	public void listBlocks(Writer out, String path) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listBlocks");
 		put(table, "path", path);
 		call(out, table, null);
 	}
 
 	public void listFiles(Writer out, String path, String blockName, String patternLFN) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "listFiles");
 		put(table, "path", path);
 		put(table, "block_name", blockName);
 		put(table, "pattern_lfn", patternLFN);
 		call(out, table, null);
 	}
 
         public void insertPrimaryDataset(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertPrimaryDataset");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 		
         }
 	
 	public void insertAlgorithm(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertAlgorithm");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
 	public void insertRun(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertRun");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
 	public void insertBlock(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertBlock");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
         public void insertLumiSection(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertLumiSection");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
         }
 
 	public void insertTier(Writer out, String tierName, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertTier");
 		put(table, "tier_name", tierName);
 		call(out, table, dbsUser);
 	}
 
 	public void insertTierInPD(Writer out, String path, String tierName, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertTierInPD");
 		put(table, "path", path);
 		put(table, "tier_name", tierName);
 		call(out, table, dbsUser);
 	}
 	
 	public void insertParentInPD(Writer out, String path, String parentPath, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertParentInPD");
 		put(table, "path", path);
 		put(table, "parent_path", parentPath);
 		call(out, table, dbsUser);
 	}
 
 	public void insertAlgoInPD(Writer out, String path, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertAlgoInPD");
 		put(table, "path", path);
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
 	public void insertRunInPD(Writer out, String path, String runNumber, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertRunInPD");
 		put(table, "path", path);
 		put(table, "run_number", runNumber);
 		call(out, table, dbsUser);
 	}
 
 
 	public void insertTierInFile(Writer out, String lfn, String tierName, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertTierInFile");
 		put(table, "lfn", lfn);
 		put(table, "tier_name", tierName);
 		call(out, table, dbsUser);
 	}
 
 	public void insertParentInFile(Writer out, String lfn, String parentLFN, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertParentInFile");
 		put(table, "lfn", lfn);
 		put(table, "parent_lfn", parentLFN);
 		call(out, table, dbsUser);
 	}
 
 	public void insertAlgoInFile(Writer out, String lfn, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertAlgoInFile");
 		put(table, "lfn", lfn);
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
 	public void insertLumiInFile(Writer out, String lfn, String lsNumber, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertLumiInFile");
 		put(table, "lfn", lfn);
 		put(table, "ls_number", lsNumber);
 		call(out, table, dbsUser);
 	}
 
 	public void insertFiles(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertFiles");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 
 	public void insertProcessedDataset(Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Hashtable table = new Hashtable();
 		put(table, "api", "insertProcessedDataset");
 		put(table, "inputxml", inputXml);
 		call(out, table, dbsUser);
 	}
 	*/
 	private void put(Hashtable table, String key, String value) {
 		if(isNull(value)) table.put(key, "");
 		else table.put(key, value);
 	}
 	
 
 	private boolean isNull(String pattern) {
 		return DBSUtil.isNull(pattern);
 	}
 		
 	
 	/*private void checkXML(String xml) throws Exception {
 		if(isNull(xml))
 			throw new DBSException("Bad Data", "300", "Null Fields. Expected a xmlInput in XML format");
 	}
 
 */
 
 }
