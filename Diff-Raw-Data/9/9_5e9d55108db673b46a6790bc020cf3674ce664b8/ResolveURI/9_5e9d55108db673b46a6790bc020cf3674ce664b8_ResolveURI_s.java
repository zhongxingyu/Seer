 package edu.mayo.cts2.uriresolver.controller;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import edu.mayo.cts2.uriresolver.dao.UriJDBCTemplate;
 import edu.mayo.cts2.uriresolver.dao.UriResults;
 import org.apache.log4j.Logger;
 
 @Controller
 public class ResolveURI {
 	private ApplicationContext context;
 	private UriJDBCTemplate uriJDBCTemplate;
 	private static Logger logger = Logger.getLogger(ResolveURI.class);
 	private static final String TYPE="type";
 	private static final String IDENTIFIER="identifier";
 	private static final int ACCESS_DENIED = 1045;
 	private static final int DATABASE_MISSING = 1049;
 	private static final String [] TABLENAMES = {"urimap", "versionmap", "identifiermap"};
 		
 	// -- ID
 	@RequestMapping("/id/{type}")
 	@ResponseBody 
 	public UriResults uriMapById(@PathVariable(TYPE) String type, @RequestParam(value = "id") String id){
 		if(this.connectDB()){
 			String identifier;
 			identifier = uriJDBCTemplate.getIdentifierByID(type, id);
 			return uriMapByIdentifier(type, identifier);
 		} 
 		
 		return null;
 	}
 
 	@RequestMapping(value={"/id/{type}/{identifier}"})
 	@ResponseBody
 	public UriResults uriMapByIdentifier(@PathVariable(TYPE) String type, @PathVariable(IDENTIFIER) String identifier){
 		if(this.connectDB()){
 			return uriJDBCTemplate.getURIMapByIdentifier(type, identifier);	
 		} 
 		
 		return null;
 	}
 
 	// -- IDS
 	@RequestMapping("/ids/{type}/{identifier}")
 	@ResponseBody
 	public UriResults allUriMapIdentities(@PathVariable(TYPE) String type, @PathVariable(IDENTIFIER) String identifier){
 		if(this.connectDB()){
 			return uriJDBCTemplate.getURIMapIdentifiers(type, identifier);	
 		} 
 		
 		return null;
 	}
 
 	// --- VERSION
 	@RequestMapping("/version/{type}/{identifier}")
 	@ResponseBody
 	public UriResults uriMapByVersionID(@PathVariable(TYPE) String type, 
 			@PathVariable(IDENTIFIER) String identifier, @RequestParam(value = "versionID") String versionID){
 		if(this.connectDB()){
 			String versionIdentifier = uriJDBCTemplate.getVersionIdentifierByVersionID(type, identifier, versionID);			
 			return uriMapByIdentifier("CODE_SYSTEM_VERSION", versionIdentifier);
 		} 
 		
 		return null;
 	}
 
 	@RequestMapping("/version/{type}/{identifier}/{versionIdentifier}")
 	@ResponseBody
 	public UriResults uriMapByVersionIdentifier(@PathVariable(TYPE) String type, 
 			@PathVariable(IDENTIFIER) String identifier, @PathVariable("versionIdentifier") String versionIdentifier){
 		if(this.connectDB()){
 			return uriJDBCTemplate.getURIMapByVersionIdentifier(type, identifier, versionIdentifier);
 		} 
 		
 		return null;
 	}
 
 	// -- VERSIONS
 	@RequestMapping("/versions/{type}/{identifier}")
 	@ResponseBody
 	public UriResults allUriMapVersionIdentifiers(@PathVariable(TYPE) String type, @PathVariable(IDENTIFIER) String identifier){
 		if(this.connectDB()){
 			return uriJDBCTemplate.getURIMapVersionIdentifiers(type, identifier);		
 		} 
 		
 		return null;
 	}
    
 	private boolean connectDB() {
 		if(uriJDBCTemplate != null && uriJDBCTemplate.isConnected()){
 			return true;
 		}
 		
 		DataSource ds;
 		boolean inMemoryDBrequired = false;
 		context = new ClassPathXmlApplicationContext("Beans.xml");
 		
 		uriJDBCTemplate = (UriJDBCTemplate) context.getBean("uriJDBCTemplate");
 		ds = (DataSource) context.getBean("dataSource");
 		int connectionCode = uriJDBCTemplate.checkDataSource(ds);
 		
 		if(connectionCode != 0){
 			inMemoryDBrequired = true;
 			this.logConnectionError(connectionCode);
 		}
 		else if(!this.checkTablesExist(ds)){
 			inMemoryDBrequired = true;
 		}
 		
 		// TODO: test data exists?  and correct columns ??
 		
 		if(inMemoryDBrequired){
 			ds = this.createInMemoryDatabase();
 		}
 		
 		uriJDBCTemplate.setDataSource(ds);
 		return true;
 	}
 
 	private DataSource createInMemoryDatabase() {
 		// createDatabase in memory
 		logger.info("Creating an in memory database");
 		DataSource ds = (DataSource) context.getBean("h2DataSource");
 		int connectionCode = uriJDBCTemplate.checkDataSource(ds);
 		
 		this.importMySQLDataToH2Database(ds);
 		
 		if(connectionCode != 0){
 			logger.error("Unable to create in memory database.  Cannot continue.");
			throw new RuntimeException();
 		}
 		
 		return ds;
 	}
 
 	private boolean checkTablesExist(DataSource ds) {
 		Connection con = null;
 		boolean tablesExist = true;
 		try {
 			con = ds.getConnection();
 			DatabaseMetaData metaData = con.getMetaData();
 			for(int i=0; i < TABLENAMES.length; i++){
 				if(!this.tableExists(metaData, TABLENAMES[i])){
					tablesExist = false;;
 					logger.error("Database is missing table: " + TABLENAMES[i]);
 				}
 			}	
 			con.close();
 		} catch (SQLException e) {
 			logger.error("Unknown error while checking tables exist: " + e.getMessage());
 			return false;
 		} finally {
 			try {
 				if(con != null){
 					con.close();
 				}
 			} catch (SQLException e) {
 				logger.error("Error while closing data source connection object: " + e.getMessage());
 				return false;
 			}
 		}
 		
 		return tablesExist;
 	}
 
 	private void logConnectionError(int connectionCode) {
 		if(connectionCode == ACCESS_DENIED){ 
 			logger.error("Access denied to DB server");
 		}
 		else if(connectionCode == DATABASE_MISSING){
 			logger.error("Database does not exist");
 		}
 		else{
 			logger.error("Unknown error while connecting to database");
 		}
 	}
 
 	private boolean tableExists(DatabaseMetaData metaData, String tablename) {
 		//ResultSet tables = metaData.getTables(null, "public", "%" ,new String[] {"TABLE"} );
 		ResultSet tables = null;
 		try {
 			tables = metaData.getTables(null, null, tablename, null);
 			if(tables.next()){
 				tables.close();
 				return true;
 			}
 		} catch (SQLException e) {
 			return false;
 		} finally {
 			if(tables != null){
 				try {
 					tables.close();
 				} catch (SQLException e1) {
 					logger.error("Error while colsing result set: " + e1.getMessage());
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean importMySQLDataToH2Database(DataSource ds) {
 		JdbcTemplate jdbcTemplateObject;
 		jdbcTemplateObject = new JdbcTemplate(ds);
 
 		StringBuffer sqlBuffer = new StringBuffer();
 		BufferedReader bufferedReader =  null;
 		Reader reader = null;
 		
 		try{
 			InputStream in = ResolveURI.class.getResourceAsStream("/uriresolver.sql");
 			reader = new InputStreamReader(in, "UTF-8");
             bufferedReader = new BufferedReader(reader);
             while (bufferedReader.ready()) {
             	String line = bufferedReader.readLine().trim(); 
  				if (this.isSQLCode(line)) {
  					sqlBuffer.append(this.convertToH2(line));
  				}
             }
             bufferedReader.close();
             reader.close(); 
 		} catch (IOException e) {
 			logger.error("Error while importing data to in memory database: " + e.getMessage());
 			return false;
 		} finally {
 			try {
 				if(bufferedReader != null){
 					bufferedReader.close();
 				} 
 				if(reader != null){
 					reader.close();
 				}
 			} catch (IOException ex) {
 				logger.error("Error while closing access to in memory database: " + ex.getMessage());
 				return true;
 			}
 		}
 			
 		jdbcTemplateObject.execute(sqlBuffer.toString());
 		return true;		
 	}
 
 
 	private String convertToH2(String mySQLLine) {
 		String [] stringsToRemove = {"`", " COLLATE utf8_bin", " CHARACTER SET utf8", " COLLATE utf8_unicode_ci", " ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin"};
 		String h2Line = mySQLLine.replaceAll("\\\\'", "''");
 		for(int i=0; i < stringsToRemove.length; i++){ 						
 			h2Line = h2Line.replaceAll(stringsToRemove[i], "");
 		}
 		return h2Line + "\n";
 	}
 
 
 	private boolean isSQLCode(String line) {
 		String [] commentIdentifiers = {"--", "//", "#", "/*", "LOCK", "UNLOCK"};
 		for(int i=0; i < commentIdentifiers.length; i++){
 			if(line.startsWith(commentIdentifiers[i])){
 				return false;
 			}
 		}
 		return true;
 	}
 }
