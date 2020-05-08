 package edu.wpi.cs.jburge.SEURAT.rationaleData;
 
 import java.util.*;
 import java.io.*; //needed to be serializable
 import java.net.URL;
 
 import javax.xml.transform.*;
 import org.w3c.dom.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.parsers.*;
 
 import org.xml.sax.SAXException;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import SEURAT.preferences.PreferenceConstants;
 
 import edu.wpi.cs.jburge.SEURAT.SEURATPlugin;
 import edu.wpi.cs.jburge.SEURAT.views.CandidateTreeParent;
 import edu.wpi.cs.jburge.SEURAT.views.TreeParent;
 import SEURAT.events.*;
 
 /**
  * This is the MUCH too large class that handles most of the database
  * operations. If you need to get things from the DB in bulk, this is the one to call.
  * The class is set up as a singleton design pattern.
  * @author burgeje
  *
  */
 public final class RationaleDB implements Serializable {
 	/**
 	 * Added by Eclipse
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * This is the handle to our ONE singleton instance
 	 */
 	private static RationaleDB s;
 
 	/**
 	 * If you're loading up the DB from an XML file, this is where the
 	 * name is stored.
 	 */
 	private static String ratFile;
 
 	/**
 	 * Used when reading in from XML
 	 */
 	private static Hashtable idRefs;
 	private static Hashtable xmlRefs[];
 	private static final int NUM_XML_CLASSES = 3;
 	private static final int PATTERN_XML_INDEX = 0;
 	private static final int PATTERNDECISION_XML_INDEX = 1;
 	private static final int PATTERNPROBLEMCATEGORY_XML_INDEX = 2;
 	/**
 	 * Contains the last rationale ID. I'm not sure what this is used
 	 * for - I'd guess maybe the XML?
 	 */
 	private int saveLast;
 
 	// for rationale XML input. I'm not clear why these are actually necessary!
 	/**
 	 * Holds arguments when reading in from XML
 	 */
 	private Vector<Argument> arguments;
 	/**
 	 * Holds assumptions when reading in from XML
 	 */
 	private Vector<Assumption> assumptions;
 	/**
 	 * Holds claims when reading in from XML
 	 */
 	private Vector<Claim> claims;
 	/**
 	 * Holds requirements when reading in from XML
 	 */
 	private Vector<Requirement> requirements;
 	/**
 	 * Holds decisions when reading in from XML
 	 */
 	private Vector<Decision> decisions;
 	/**
 	 * Holds tradeoffs when reading in from XML
 	 */
 	private Vector<Tradeoff> tradeoffs;
 	/**
 	 * Holds co_occurrences when reading in from XML
 	 */
 	private Vector<Tradeoff> co_occurences;
 	/**
 	 * Holds questions when reading in from XML
 	 */
 	private Vector<Question> questions;
 	/**
 	 * Holds alternatives when reading in from XML
 	 */
 	private Vector<Alternative> alternatives;
 	/**
 	 * Holds the root of the argument ontology when reading in
 	 * from XML
 	 * (This is used in the old readXMLData method, not the new
 	 * argument ontology import functionality.)
 	 */
 	private OntEntry argumentOntology;
 
 	/**
 	 * The location of the XML import file for DB creation.
 	 */
 	//private static String ratDBCreateFile = SEURATPlugin.getDefault().getStateLocation()
 	//.addTrailingSeparator().toOSString() + "ratDBCreate.xml";
 	private static String ratDBCreateFile = SEURATPlugin.getDefault().getBundle().getLocation() +
 	"ratDBCreate.xml";
 	/**
 	 * The default database name
 	 */
 	private static String dbName = "testrat";
 
 	/**
 	 * Points towards our plugin preferences.
 	 */
 	IPreferenceStore store = SEURATPlugin.getDefault().getPreferenceStore();
 
 	/**
 	 * The connection to our database
 	 */
 	private static Connection conn;
 
 	private PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>
 	updateNotifier = new 
 	PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>(
 			new RationaleUpdateEvent(), false
 	);
 
 	public PublishSubscribeManager<RationaleElement,Object,RationaleUpdateEvent>
 	Notifier()
 	{
 		return updateNotifier;
 	}
 	/**
 	 * Rationale DB Constructor. This MUST remain PRIVATE in order for this
 	 * class to stay a d. This class sets up the JDBC database
 	 * connection using the information from the preferences.
 	 * @param x - unused parameter.
 	 */
 	private RationaleDB(int x) {
 		idRefs = new Hashtable();
 		xmlRefs = new Hashtable[NUM_XML_CLASSES];
 		xmlRefs[PATTERN_XML_INDEX] = new Hashtable<String, Pattern>();
 		xmlRefs[PATTERNDECISION_XML_INDEX] = new Hashtable<String, PatternDecision>();
 		//This hashtable's second element is String[3], with String[1] as problemcategory
 		//and String[2] as type (Architecture, Design, Idiom), and String[0] as id in the database.
 		xmlRefs[PATTERNPROBLEMCATEGORY_XML_INDEX] = new Hashtable<String, String[]>();
 		requirements = new Vector<Requirement>();
 		arguments = new Vector<Argument>();
 		assumptions = new Vector<Assumption>();
 		alternatives = new Vector<Alternative>();
 		claims = new Vector<Claim>();
 		decisions = new Vector<Decision>();
 		questions = new Vector<Question>();
 		tradeoffs = new Vector<Tradeoff>();
 		co_occurences = new Vector<Tradeoff>();
 		argumentOntology = new OntEntry("Argument-Ontology", null);
 		saveLast = RationaleElement.getLastID();
 
 		if( store.getString(PreferenceConstants.P_DATABASETYPE).equals(
 				PreferenceConstants.DatabaseType.DERBY
 		))
 		{
 			derbyConnect();
 		}
 		else if( store.getString(PreferenceConstants.P_DATABASETYPE).equals(
 				PreferenceConstants.DatabaseType.MYSQL
 		))
 		{
 			sqlConnect();
 		}
 
 		migrate();
 
 		prepareStatements();
 	};
 
 	/**
 	 * Rationale DB connect method for a MySQL database.  This method is called
 	 * from the RationaleDB constructor if the SEURAT preferences specify MySQL.
 	 */
 	private void sqlConnect()
 	{	//Set up our database connection
 		System.out.println("Setting up our database connection");
 		try {
 			//get the file name from the preferences
 			dbName = store.getString(PreferenceConstants.P_DATABASE);
 		}
 		catch (Exception ex) {
 			Shell shell = new Shell();
 			MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 			mbox.setMessage("Error getting preferences");
 			mbox.open();
 		}
 
 
 		//lets set up our database
 		try {
 			// The newInstance() call is a work around for some
 			// broken Java implementations
 			com.mysql.jdbc.Driver.class.newInstance();
 			//Class.forName("com.mysql.jdbc.Driver").newInstance();
 		} catch (Exception ex) {
 			// handle the error
 			int i = 0;
 			i++;
 		}
 
 		String passwordSpec;
 		String password = store.getString(PreferenceConstants.P_DATABASEPASSWORD);
 		if (password.length() == 0)
 		{
 			passwordSpec = "";
 		}
 		else
 		{
 			passwordSpec = "&password=" + password;
 			// passwordSpec = " ?password=" + password;
 		}
 
 		String connStr = "jdbc:mysql://";
 
 		// Set up connection string based on server location, address, and port preferences
 		if (store.getString(PreferenceConstants.P_MYSQLLOCATION).equals(PreferenceConstants.
 				MySQLLocationType.LOCAL)) {
 			connStr += "localhost/";
 		}
 		else {
 			String address = store.getString(PreferenceConstants.P_MYSQLADDRESS);
 			String port = store.getString(PreferenceConstants.P_MYSQLPORT);
 
 			if (port.length() == 0) {
 				connStr += address+"/";
 			}
 			else {
 				connStr += address+":"+port+"/";
 			}
 		}
 		connStr += dbName
 		+ "?user="
 		+ store
 		.getString(PreferenceConstants.P_DATABASEUSER)
 		+ passwordSpec;
 
 		try {
 
 			System.out.println(connStr);
 			conn = DriverManager.getConnection(connStr);
 		} catch (SQLException ex) {
 			try {
 				conn = DriverManager.getConnection(connStr + "&createDatabaseIfNotExist=true");
 				createTables();
 			}
 			catch( SQLException eInner ) {
 				reportError(ex, "constructor", "couldn't connect to database "
 						+ dbName);
 				Shell shell = new Shell();
 				MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 				mbox.setMessage("Couldn't connect to database " + dbName);
 				mbox.open();
 			}
 		}
 	}
 
 	/**
 	 * Rationale DB connect method for an Apache Derby local database.  This method
 	 * is called from the RationaleDB constructor if the SEURAT preferences specify Derby.
 	 */
 	private void derbyConnect()
 	{
 		String l_DerbyPath = "";
 		String l_DerbyPathType = "";
 
 		try {
 			//get the file name from the preferences
 			dbName = store.getString(PreferenceConstants.P_DERBYNAME);
 			l_DerbyPath = store.getString(PreferenceConstants.P_DERBYPATH);
 			l_DerbyPathType = store.getString(PreferenceConstants.P_DERBYPATHTYPE);
 		}
 		catch (Exception ex) {
 			Shell shell = new Shell();
 			MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 			mbox.setMessage("Error getting preferences");
 			mbox.open();
 		}
 
 		final String l_ConnectString = "jdbc:derby:";
 		Properties props = new Properties();
 		props.put("user", "seurat_dummy");
 		props.put("password", "seurat_dummy");
 
 		String l_dbPath = "";
 		if( l_DerbyPathType.equals(PreferenceConstants.DerbyPathType.ABSOLUTE_PATH)) {
 			l_dbPath += l_DerbyPath;
 		}
 		else {
 			l_dbPath += ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
 		}
 		l_dbPath += "/" + dbName;
 		props.put("databaseName", l_dbPath);
 
 		try {
 			//final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
 			org.apache.derby.jdbc.EmbeddedDriver.class.newInstance();
 			//Class.forName(DRIVER).newInstance();
 		} catch (Exception ex) {
 			// handle the error
 			int i = 0;
 			i++;
 		}
 
 
 		// Attempt To Connect To Pre Existing Database
 		try {
 			conn = DriverManager.getConnection(l_ConnectString, props);
 		}
 		catch( Exception eError ) {
 			try
 			{
 				props.put("create", "true");
 				// If the database can't be opened, it needs to be created.
 				conn = DriverManager.getConnection(l_ConnectString, props);
 
 				createTables();
 			}
 			catch( SQLException eInner )
 			{
 				reportError(eInner, "constructor", "couldn't connect to database " + dbName);
 				Shell shell = new Shell();
 				MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 				mbox.setMessage("Couldn't connect to database " + dbName);
 				mbox.open();
 			}
 		}
 		System.out.println(l_dbPath);
 
 	}
 
 	/**
 	 * Rationale DB method to create the initial tables for a new rationale database.
 	 * This method is called by the connect methods if a new database needs to be created,
 	 * and functions by retrieving and executing queries using the RationaleDBCreate class. 
 	 * @throws SQLException
 	 */
 	private void createTables() throws SQLException
 	{		
 		String []l_queries = RationaleDBCreate.getQueries();
 
 		Statement l_stmt = conn.createStatement();
 
 		for( String l_query : l_queries )
 		{
 			try
 			{
 				l_stmt.execute(l_query);
 			}
 			catch( SQLException eError )
 			{
 				int i = 0;
 				i++;
 				throw eError;
 			}
 		}
 
 
 
 		// Now import the pattern library
 		boolean importXMLSuccess = false;
 		if (new File(ratDBCreateFile.substring(ratDBCreateFile.indexOf('/'))).exists()){
 			String xmlFile = ratDBCreateFile.substring(ratDBCreateFile.indexOf('/'));
 			importXMLSuccess = RationaleDBUtil.importFromXML(xmlFile);
 		}
 		if (!importXMLSuccess){		
 			System.err.println("Unable fo find " + ratDBCreateFile);
 			System.err.println("Loading from hard-coded defaults");
 			RationaleDBCreate.resetCurrentID();
 			// Import hard-coded data, first the ontology, then the pattern library...
 			String [] l_ontQueries = RationaleDBCreate.getOntologyQueries();
 			for (String l_ontQuery : l_ontQueries) {
 				try
 				{
 					l_stmt.execute(l_ontQuery);
 				}
 				catch( SQLException eError )
 				{
 					int i = 0;
 					i++;
 					System.out.println("DEBUG OntEntry: " + eError.getMessage());
 					eError.printStackTrace();
 					throw eError;
 				}
 			}
 			
 			RationaleDBCreate.resetCurrentID();
 			PreparedStatement ps = conn.prepareStatement("INSERT INTO patterns (name, type, description,problem, context, solution, implementation,example,url,id) values (?,?,?,?,?,?,?,?,?,?)");
 			importPatterns(ps);
 			ps.close();
 
 			RationaleDBCreate.resetCurrentID();
 			String[] l_patternDecisionQueries = RationaleDBCreate.getPatternDecisionQueries();
 			for (String l_patternDecisionQuery: l_patternDecisionQueries){
 				try {
 					l_stmt.execute(l_patternDecisionQuery);
 				} catch (SQLException e) {
 					int i = 0;
 					i++;
 					System.err.println("Debug PatternDecision: " + e.getMessage());
 					throw e;
 				}				
 			}
 
 			RationaleDBCreate.resetCurrentID();
 			String[] l_patternOntEntryQueries = RationaleDBCreate.getPatternOntEntryQueries();
 			for (String l_patternOntEntryQuery: l_patternOntEntryQueries){
 				try {
 					l_stmt.execute(l_patternOntEntryQuery);
 				} catch (SQLException e) {
 					int i = 0;
 					i++;
 					System.err.println("Debug PatternOntEntry: " + e.getMessage());
 					throw e;
 				}				
 			}
 
 			RationaleDBCreate.resetCurrentID();
 			String[] l_patternDecisionRelationshipQueries = RationaleDBCreate.getPatternDecisionRelationShipQueries();
 			for (String l_patternDecisionRelationshipQuery: l_patternDecisionRelationshipQueries){
 				try {
 					l_stmt.execute(l_patternDecisionRelationshipQuery);
 				} catch (SQLException e) {
 					int i = 0;
 					i++;
 					throw e;
 				}				
 			}
 
 			RationaleDBCreate.resetCurrentID();
 			String[] l_patternProblemCategoriesQueries = RationaleDBCreate.getPatternProblemCategoriesQueries();
 			for (String l_patternProblemCategoryQuery: l_patternProblemCategoriesQueries){
 				try {
 					l_stmt.execute(l_patternProblemCategoryQuery);
 				} catch (SQLException e) {
 					int i = 0;
 					i++;
 					throw e;
 				}				
 			}
 
 			RationaleDBCreate.resetCurrentID();
 			String[] l_patternProblemCategoryQueries = RationaleDBCreate.getPatternProblemCategoryQueries();
 			for (String l_pattern_problemCategoryQuery: l_patternProblemCategoryQueries){
 				try {
 					l_stmt.execute(l_pattern_problemCategoryQuery);
 				} catch (SQLException e) {
 					int i = 0;
 					i++;
 					throw e;
 				}				
 			}
 		}
 		l_stmt.close();	
 
 	}
 
 	/**
 	 * Prepared Statement Which Is Used Primarily By The Select
 	 * Ontology Dialog To Generate A Tree Of Ontology
 	 * Elements To Select.
 	 * 
 	 * @see SelectOntEntry
 	 */
 	private static PreparedStatement m_ontEntriesQuery = null;
 
 	/**
 	 * Prepared Statement For The Ontology Entry's
 	 * FromDatabase pseudo constructor.
 	 * 
 	 * @see OntEntry#fromDatabase(String)
 	 */
 	private static PreparedStatement m_ontEntryFromDB = null;
 
 	private static PreparedStatement m_patternFromDB = null;
 
 	private static PreparedStatement m_altPatternFromDB = null;
 
 	/**
 	 * Accessor Method For The Ontology Entries FromDatabase Pseudo
 	 * Constructor. This is how the Ontology Entry Class Retrieves
 	 * The Prepared Statement
 	 * 
 	 * @see OntEntry#fromDatabase(String)
 	 * 
 	 * @return A prepared statement for retrieving an ontology
 	 * 		entry from the database by name.	 
 	 */
 	public PreparedStatement getStatement_OntologyEntryFromDB() {
 		return m_ontEntryFromDB;
 	}
 
 	/**
 	 * Accessor method. Retrieves the prepared statement for patterns
 	 * @return
 	 */
 	public PreparedStatement getStatement_PatternFromDB() {
 		return m_patternFromDB;
 	}
 
 	/**
 	 * Accessor method. Retrieves the prepared statement for pattern alternatives
 	 * @return
 	 */
 	public PreparedStatement getStatement_AltPatternFromDB() {
 		return m_altPatternFromDB;
 	}
 
 	/**
 	 * Generate prepared statements for use in database queries. The
 	 * Prepared Statements Make accessing The Database Much Faster. 
 	 */
 	private void prepareStatements() {
 		try {
 			if( m_ontEntriesQuery != null )
 				m_ontEntriesQuery.close();
 
 			m_ontEntriesQuery = conn.prepareStatement("SELECT childName from " 
 					+ RationaleDBUtil.escapeTableName("ONT_HIERARCHY") + " "
 					+ " where parentName = ?");
 			m_ontEntriesQuery.setEscapeProcessing(true);
 		} catch( SQLException eError ) {
 			//int doNothing;
 			//doNothing = 0;
 		}
 		try {
 			if( m_ontEntryFromDB != null )
 				m_ontEntryFromDB.close();
 
 			m_ontEntryFromDB = conn.prepareStatement("SELECT id, importance, description "
 					+ " from " + RationaleDBUtil.escapeTableName("OntEntries")
 					+ " where name = ?");
 			m_ontEntryFromDB.setEscapeProcessing(true);
 		} catch( SQLException eError ) {
 			//int doNothing;
 			//doNothing = 0;
 		}
 
 		//for pattern library
 		try {
 			if( m_patternFromDB != null )
 				m_patternFromDB.close();
 
 			m_patternFromDB = conn.prepareStatement("SELECT * "
 					+ " from " + RationaleDBUtil.escapeTableName("patterns")
 					+ " where name = ?");
 			m_patternFromDB.setEscapeProcessing(true);
 
 			if( m_altPatternFromDB != null )
 				m_altPatternFromDB.close();
 
 			m_altPatternFromDB = conn.prepareStatement("SELECT * "
 					+ " from " + RationaleDBUtil.escapeTableName("alternativepatterns")
 					+ " where name = ?");
 			m_altPatternFromDB.setEscapeProcessing(true);
 
 		} catch( SQLException eError ) {
 			//int doNothing;
 			//doNothing = 0;
 		}
 	}
 
 	/**
 	 * This Code Seamlessly Updates A Database With New Data. It will
 	 * check the existing database schema and determine whether the
 	 * desired database properties exist. If the properties do not
 	 * exist then the database schema will be modified to contain
 	 * the new attributes with default values.
 	 * 
 	 * Move This To RationaleDBCreate
 	 * @see RationaleDBCreate
 	 */
 	private void migrate() {
 		Statement l_stmt = null;
 
 		try {
 			l_stmt = conn.createStatement();
 
 			l_stmt.executeQuery("SELECT * FROM " + RationaleDBUtil.escapeTableName("ONT_HIERARCHY"));
 		}
 		catch( Exception eError ) {
 			Statement l_create = null;
 			try {
 				l_create = conn.createStatement();
 
 				l_create.execute(RationaleDBCreate.CREATEVIEW_ONTRELATIONSHIP_HIERARCHY());
 			}
 			catch( Exception eInner ) 
 			{
 				int a = 0;
 				a++;
 			}
 			finally {
 				try {
 					l_create.close();
 				}
 				catch( Exception eInner ) {
 				}
 			}
 		}
 		finally {
 			try {
 				l_stmt.close();
 			}
 			catch( Exception eInner ) {
 			}
 		}
 
 	}
 
 	/**
 	 * This does not appear to be used!
 	 *
 	 */
 	public void resetDatabase()
 	{
 		RationaleElement.setLastID(saveLast);
 	}
 
 	/**
 	 * Get the connection to the database so we can query it.
 	 * @return the database connection
 	 */
 	public Connection getConnection() {
 		return conn;
 	}
 
 	/**
 	 * This is how we get a handle to our database. This is from the Singleton
 	 * Design Pattern
 	 * @return the database class
 	 */
 	public static RationaleDB getHandle() {
 		if (s == null) {
 			s = new RationaleDB(0);
 		}
 		return s;
 	}
 
 	/**
 	 * Set our file name. The user may decide to use a different DB.
 	 * @param fileN
 	 */
 	public static void setFile(String fileN) {
 		ratFile = fileN;
 	}
 
 	/**
 	 * Reset our connection to the database. This is done when the user
 	 * decides to connect to a different database
 	 *
 	 */
 	public static void resetConnection() {
 		s = null;
 		s = new RationaleDB(0);
 	}
 
 	/**
 	 * Create a new database using the SEURAT_Base database as a start. This
 	 * is needed so we get our empty rationale element tables and a fully populated
 	 * Argument Ontology
 	 * @param newDBName - the name for our new database.
 	 */
 	public static void createNewDB(String newDBName)
 	{
 		IPreferenceStore store = SEURATPlugin.getDefault().getPreferenceStore();
 
 		//set our preferences to give the new database name
 		if( store.getString(PreferenceConstants.P_DATABASETYPE).equals
 				(PreferenceConstants.DatabaseType.DERBY) )
 		{
 			store.setValue(PreferenceConstants.P_DERBYNAME, newDBName);
 		}
 		else
 			if( store.getString(PreferenceConstants.P_DATABASETYPE).equals
 					(PreferenceConstants.DatabaseType.MYSQL) )
 			{
 				store.setValue(PreferenceConstants.P_DATABASE, newDBName);
 			}
 	}
 
 	/**
 	 * Given the name and type of a rationale element, get it from the database.
 	 * @param name - the name of the element
 	 * @param type - the type of the element
 	 * @return our rationale element from the database
 	 */
 	public static RationaleElement getRationaleElement(String name,
 			RationaleElementType type) {
 		RationaleElement ourEle = RationaleElementFactory
 		.getRationaleElement(type);
 		if (ourEle != null) {
 			ourEle.fromDatabase(name);
 		}
 		return ourEle;
 	}
 
 	/**
 	 * Given the ID and type of a rationale element, get it from the database
 	 * @param id - the ID (key) of the element
 	 * @param type - the type of the element
 	 * @return our rationale element from the database
 	 */
 	public static RationaleElement getRationaleElement(int id,
 			RationaleElementType type) {
 		RationaleElement ourElement;
 		ourElement = RationaleElementFactory.getRationaleElement(type);
 
 		if (ourElement != null)
 			ourElement.fromDatabase(id);
 		return ourElement;
 	}
 
 	/**
 	 * Given the name and type of a rationale element (such as an argument), return
 	 * find all requirements that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
 	public Vector<TreeParent> getRequirements(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "requirements", parentType);
 	}
 
 	/**
 	 * Given the name and type of a rationale element , return
 	 * find all contingencies that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
 	public Vector<TreeParent> getContingencies(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "contingencies", parentType);
 	}
 	/**
 	 * Given the name and type of a rationale element , return
 	 * find all designers that are children of that element in the database.
 	 * @param parentName - name of the parent element in the tree
 	 * @param parentType - type of the parent element
 	 * @return a vector of the children elements
 	 */
 	public Vector<TreeParent> getDesigners(String parentName,
 			RationaleElementType parentType) {
 		return getTreeElements(parentName, "designerprofiles", parentType);
 	}
 
 	/**
 	 * Get all the associations from the database.
 	 * @return a vector of associations
 	 */
 	public Vector<Association> getAssociations() {
 		Vector<Association> assocs = new Vector<Association>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("associations");
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				Association as = new Association(rs.getInt("alternative"), rs.getString("artifact"),
 						rs.getString("artresource"), rs.getString("artname"), rs.getString("assocmessage"));
 				assocs.add(as);
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getAssociations", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return assocs;
 	}
 
 	/**
 	 * Get all the associations from the database that are at a given resource.
 	 * @return a vector of associations
 	 */
 	public Vector<Association> getAssociations(String resource) {
 		Vector<Association> assocs = new Vector<Association>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("associations")
 				+ " where artresource = '" + resource + "'";
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				Association as = new Association(rs.getInt("alternative"), rs.getString("artifact"),
 						rs.getString("artresource"), rs.getString("artname"), rs.getString("assocmessage"));
 				assocs.add(as);
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getAssociations", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return assocs;
 	}
 
 	/**
 	 * Get all the requirements from the database that have a particular
 	 * status value (Addressed, Satisfied, or Violated)
 	 * @param stat the status value we are looking for
 	 * @return a vector of requirements
 	 */
 	public Vector<Requirement> getRequirements(ReqStatus stat) {
 		Vector<Requirement> reqst = new Vector<Requirement>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from "
 				+ RationaleDBUtil.escapeTableName("requirements") + " "
 				+ "where status = '" + stat.toString() + "'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				Requirement req = new Requirement();
 				req.fromDatabase(name);
 				reqst.add(req);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getRequirements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return reqst;
 	}
 
 	/**
 	 * Get all the requirements from the database that are either enabled
 	 * or disabled, depending on what we are looking for
 	 * @param enabled the enabled value we are looking for (true/false)
 	 * @return a vector of requirements
 	 */
 	public Vector<Requirement> getEnabledRequirements(boolean enabled) {
 		Vector<Requirement> reqst = new Vector<Requirement>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		String enabledStr;
 		if (enabled)
 			enabledStr = "True";
 		else
 			enabledStr = "False";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from "
 				+ RationaleDBUtil.escapeTableName("requirements") + " "
 				+ "where enabled = '" + enabledStr + "'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				Requirement req = new Requirement();
 				req.fromDatabase(name);
 				reqst.add(req);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getEnabledRequirements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return reqst;
 	}
 
 	/**
 	 * Return a vector of all non-functional requirements
 	 * @return
 	 * @author wang2
 	 */
 	public Vector<Requirement> getNFRs(){
 		Vector<Requirement> nfrs = new Vector<Requirement>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from "
 				+ RationaleDBUtil.escapeTableName("requirements") + " "
 				+ "where type = 'NFR'";
 			//System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				//System.out.println(name);
 				Requirement req = new Requirement();
 				req.fromDatabase(name);
 				nfrs.add(req);
 			}
 			//System.out.println(nfrs.size());
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNFRs", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 
 		return nfrs;
 	}
 
 	/**
 	 * Get all the decisions that correspond to a particular parent and
 	 * parent type.
 	 * @param parentName - the name of our parent element
 	 * @param parentType - the parent type
 	 * @return the list of decisions
 	 */
 	public Vector<TreeParent> getDecisions(String parentName,
 			RationaleElementType parentType) {
 		//		return getElements(parentName, "decisions", parentType);
 		return getTreeElements(parentName, "decisions", parentType);
 	}
 
 	/**
 	 * Return a list of questions corresponding to a particular parent
 	 * @param parentName - the name of our parent element
 	 * @param parentType - the parent type
 	 * @return the list of questions
 	 */
 	public Vector<TreeParent> getQuestions(String parentName,
 			RationaleElementType parentType) {
 		//   	return getElements(parentName, "questions", parentType);
 		return getTreeElements(parentName, "questions", parentType);
 	}
 
 	/**
 	 * Return a list of alternatives that are depent on the passed in alternative
 	 * @param alt - our alternative
 	 * @return the alternatives dependent on our alternative
 	 */
 	public Vector<Alternative> getDependentAlternatives(Alternative alt) {
 		Vector<Alternative> dependent = new Vector<Alternative>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> altV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from " 
 				+ RationaleDBUtil.escapeTableName("arguments") + " " 
 				+ "where alternative = "
 				+ new Integer(alt.getID()).toString()
 				+ " and argType = 'Alternative'"
 				+ " and (type = 'Pre-supposes' or type = 'Opposed')";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				if ((RationaleElementType) RationaleElementType.fromString(rs
 						.getString("ptype")) == RationaleElementType.ALTERNATIVE) {
 					int altID = rs.getInt("parent");
 					altV.add(new Integer(altID));
 
 				}
 			}
 
 			if (altV.size() > 0) {
 				Iterator<Integer> altI = altV.iterator();
 				while (altI.hasNext()) {
 					Alternative relAlt = new Alternative();
 					relAlt.fromDatabase((altI.next()).intValue());
 					dependent.add(relAlt);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDependentAlternatives", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Return a list of alternatives that are dependent on the given pattern alternative.
 	 * @param alt
 	 * @param atype
 	 * @return
 	 */
 	public Vector<Alternative> getDependentAlternatives(AlternativePattern alt, ArgType atype) {
 		Vector<Alternative> dependent = new Vector<Alternative>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> altV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where alternative = "
 				+ new Integer(alt.getID()).toString()
 				+ " and (type = '"
 				+ atype.toString() + "')";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				if ((RationaleElementType) RationaleElementType.fromString(rs
 						.getString("ptype")) == RationaleElementType.ALTERNATIVE) {
 					int altID = rs.getInt("parent");
 					altV.add(new Integer(altID));
 
 				}
 			}
 
 			if (altV.size() > 0) {
 				Iterator altI = altV.iterator();
 				while (altI.hasNext()) {
 					Alternative relAlt = new Alternative();
 					relAlt.fromDatabase(((Integer) altI.next()).intValue());
 					dependent.add(relAlt);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDependentAlternatives2", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Get all  arguments that argue about a specific requirement
 	 * @param reqID - the requirement ID
 	 * @return the dependent alternatives
 	 */
 	public static Vector<Argument> getDependentArguments(int reqID) {
 		Vector<Argument> dependent = new Vector<Argument>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> argV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where argtype = 'Requirement'"
 				+ " and requirement = "
 				+ new Integer(reqID).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				int altID = rs.getInt("id");
 				argV.add(new Integer(altID));
 			}
 
 			if (argV.size() > 0) {
 				Iterator<Integer> argI = argV.iterator();
 				while (argI.hasNext()) {
 					Argument relArg = new Argument();
 					relArg.fromDatabase(((Integer) argI.next()).intValue());
 					dependent.add(relArg);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting arguments that argue the requirement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Get all  arguments that argue about a specific requirement
 	 * @param reqID - the requirement ID
 	 * @return the dependent alternatives
 	 */
 	public static Vector<Argument> getDependentAssumptionArguments(int reqID) {
 		Vector<Argument> dependent = new Vector<Argument>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> argV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where argtype = 'Assumption"
 				+ " and assumption = "
 				+ new Integer(reqID).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				int altID = rs.getInt("id");
 				argV.add(new Integer(altID));
 			}
 
 			if (argV.size() > 0) {
 				Iterator<Integer> argI = argV.iterator();
 				while (argI.hasNext()) {
 					Argument relArg = new Argument();
 					relArg.fromDatabase(((Integer) argI.next()).intValue());
 					dependent.add(relArg);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting arguments that argue the requirement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	/**
 	 * Get all dependent alternatives that are dependent on a parent where the
 	 * dependency is of a particular argument type
 	 * @param alt - the alternative
 	 * @param atype - the argument type
 	 * @return the dependent alternatives
 	 */
 	public Vector<Alternative> getDependentAlternatives(Alternative alt, ArgType atype) {
 		Vector<Alternative> dependent = new Vector<Alternative>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			Vector<Integer> altV = new Vector<Integer>();
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from " 
 				+ RationaleDBUtil.escapeTableName("arguments") 
 				+ " where alternative = "
 				+ new Integer(alt.getID()).toString()
 				+ " and (type = '"
 				+ atype.toString() + "')";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				if ((RationaleElementType) RationaleElementType.fromString(rs
 						.getString("ptype")) == RationaleElementType.ALTERNATIVE) {
 					int altID = rs.getInt("parent");
 					altV.add(new Integer(altID));
 
 				}
 			}
 
 			if (altV.size() > 0) {
 				Iterator<Integer> altI = altV.iterator();
 				while (altI.hasNext()) {
 					Alternative relAlt = new Alternative();
 					relAlt.fromDatabase(((Integer) altI.next()).intValue());
 					dependent.add(relAlt);
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDependentAlternatives2", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * Check to see if a requirement has any dependent alternatives
 	 * @param req - the requirement
 	 * @return true if there are dependent alternatives
 	 */
 	public boolean dependentAlternatives(Requirement req) {
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT parent, ptype from "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " where requirement = "
 				+ new Integer(req.getID()).toString();
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				return true;
 			} else {
 				return false;
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "ERror in get dependent alternatives3", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return false;
 	}
 
 	/**
 	 * Find all the alternatives that have a specific parent
 	 * @param parentName - the name of the parent
 	 * @param parentType - the type of the parent
 	 * @return a vector of alternatives
 	 */
 	public Vector<TreeParent> getAlternatives(String parentName,
 			RationaleElementType parentType) {
 		String findQuery = "";
 		//    	return getElements(parentName, "alternatives", parentType);
 		Vector<TreeParent> altTree = getTreeElements(parentName, "alternatives", parentType);
 		if (altTree != null) {
 			Statement stmt = null;
 			ResultSet rs = null;
 			try {
 				stmt = conn.createStatement();
 				//we need to find out if we are selected
 				Enumeration<TreeParent> alts = altTree.elements();
 				while (alts.hasMoreElements()) {
 					TreeParent alt = (TreeParent) alts.nextElement();
 					alt.setActive(false);
 
 					//now, get our status
 					findQuery = "SELECT status from " 
 						+ RationaleDBUtil.escapeTableName("alternatives") 
 						+ " where "
 						+ "name = '" + RationaleDBUtil.escape(alt.getName()) + "'";
 					rs = stmt.executeQuery(findQuery);
 					if (rs.next()) {
 						String stat = rs.getString("status");
 						AlternativeStatus altst = AlternativeStatus
 						.fromString(stat);
 						if (altst == AlternativeStatus.ADOPTED) {
 							alt.setActive(true);
 						}
 						rs.close();
 					}
 
 				}
 			} catch (SQLException ex) {
 				reportError(ex, "Error in getAlternatives", findQuery);
 			} finally {
 				releaseResources(stmt, rs);
 			}
 		}
 		return altTree;
 
 	}
 
 	/**
 	 * Find all the arguments that have a particular parent
 	 * @param parentName - the name of the parent
 	 * @param parentType - the type of the parent
 	 * @return the arguments
 	 */
 	public Vector<TreeParent> getArguments(String parentName,
 			RationaleElementType parentType) {
 		//    	return getElements(parentName, "arguments", parentType);
 		return getTreeElements(parentName, "arguments", parentType);
 	}
 
 	/**
 	 * Get all the decisions from the database (hmm... this could have
 	 * scalability issues in the future!)
 	 * @return a vector of all the decisions.
 	 */
 	public Vector<Decision> getAllDecisions() {
 		Vector<Decision> all = new Vector<Decision>();
 		Vector<String> decNames = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name from " + RationaleDBUtil.escapeTableName("decisions");
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = rs.getString("name");
 				//				System.out.println("dec name = " + name);
 				decNames.add(name);
 				//				rs.close();
 			}
 
 			Iterator<String> decI = decNames.iterator();
 			while (decI.hasNext()) {
 				Decision dec = new Decision();
 				dec.fromDatabase((String) decI.next());
 				all.add(dec);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getAllDecisions", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 
 		return all;
 	}
 
 	/**
 	 * Given the type of RationaleElement (RationaleElementType), return
 	 * the name of the database table that contains elements of that type.
 	 * @param element - the element type
 	 * @return the table name
 	 */
 	public static String getTableName(RationaleElementType element) {
 		if (element == RationaleElementType.REQUIREMENT) {
 			return ("requirements");
 		} else if (element == RationaleElementType.DECISION) {
 			return ("decisions");
 		} else if (element == RationaleElementType.QUESTION) {
 			return ("questions");
 		} else if (element == RationaleElementType.ARGUMENT) {
 			return ("arguments");
 		} else if (element == RationaleElementType.ALTERNATIVE) {
 			return ("alternatives");
 		} else if (element == RationaleElementType.ASSUMPTION) {
 			return ("assumptions");
 		} else if (element == RationaleElementType.CLAIM) {
 			return ("claims");
 		} else if (element == RationaleElementType.EXPERTISE) {
 			return ("expertise");
 		} else if (element == RationaleElementType.ONTENTRY) {
 			return ("ontentries");
 		} else if (element == RationaleElementType.COOCCURRENCE) {
 			return ("tradeoffs");
 		} else if (element == RationaleElementType.TRADEOFF) {
 			return ("tradeoffs");
 		} else if (element == RationaleElementType.CONTINGENCY) {
 			return ("contingencies");
 		} else if (element == RationaleElementType.DESIGNER) {
 			return ("designerprofiles");
 		} else if (element == RationaleElementType.PATTERN) {
 			return ("patterns");
 		}else if (element == RationaleElementType.ALTERNATIVEPATTERN) {
 			return ("alternativepatterns");
 		}else if (element == RationaleElementType.PATTERNDECISION) {
 			return ("patterndecisions");
 		}else {
 			//   		System.out.println("Need to add new type " + parent.toString() );
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * Given the name of the database table, return the RationaleElementType
 	 * @param elementType - the name of the database table
 	 * @return the element type
 	 */
 	public RationaleElementType getElementType(String elementType) {
 		if (elementType.compareTo("requirements") == 0) {
 			return (RationaleElementType.REQUIREMENT);
 		} else if (elementType.compareTo("decisions") == 0) {
 			return (RationaleElementType.DECISION);
 		} else if (elementType.compareTo("questions") == 0) {
 			return (RationaleElementType.QUESTION);
 		} else if (elementType.compareTo("arguments") == 0) {
 			return (RationaleElementType.ARGUMENT);
 		} else if (elementType.compareTo("alternatives") == 0) {
 			return (RationaleElementType.ALTERNATIVE);
 		} else if (elementType.compareTo("assumptions") == 0) {
 			return (RationaleElementType.ASSUMPTION);
 		} else if (elementType.compareTo("claims") == 0) {
 			return (RationaleElementType.CLAIM);
 		} else if (elementType.compareTo("expertise") == 0) {
 			return (RationaleElementType.EXPERTISE);
 		} else if (elementType.compareTo("ontentries") == 0) {
 			return (RationaleElementType.ONTENTRY);
 		} else if (elementType.compareTo("tradeoffs") == 0) {
 			return RationaleElementType.COOCCURRENCE; //or tradeoff?
 		} else if (elementType.compareTo("contingencies") == 0) {
 			return RationaleElementType.CONTINGENCY; //or contingency?
 		} else if (elementType.compareTo("designerprofiles") == 0) {
 			return RationaleElementType.DESIGNER;
 		} else if (elementType.compareTo("patterns") == 0) {
 			return RationaleElementType.PATTERN;
 		}
 		/*		else if (elementType.compareTo("requirements" == 0))
 		 {
 		 return ("tradeoffs");
 		 } */
 		else {
 			System.out.println("Need to add new type " + elementType);
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * Return the name of the claim mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getClaim(String parentName) {
 		String claims = getElement(parentName, "arguments", "claim",
 				RationaleElementType.CLAIM);
 		return claims;
 	}
 	/**
 	 * Return the name of the assumption mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getAssumption(String parentName) {
 		String claims = getElement(parentName, "arguments", "assumption",
 				RationaleElementType.ASSUMPTION);
 		return claims;
 	}
 	/**
 	 * Return the name of the requirement mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getRequirement(String parentName) {
 		String claims = getElement(parentName, "arguments", "requirement",
 				RationaleElementType.REQUIREMENT);
 		return claims;
 	}
 	/**
 	 * Return the name of the alternative mapping to the parent argument
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getAlternative(String parentName) {
 		String claims = getElement(parentName, "arguments", "alternative",
 				RationaleElementType.ALTERNATIVE);
 		return claims;
 	}
 
 	/**
 	 * Return the name of the alternative pattern mapping to the given parent
 	 * @param parentName
 	 * @return the alternative pattern
 	 */
 	public String getAlternativePattern(String parentName) {
 		String alternativePattern = getElement(parentName, "arguments", "alternativepattern",
 				RationaleElementType.ALTERNATIVEPATTERN);
 		return alternativePattern;
 	}
 	/**
 	 * Return the name of the ontology entry mapping to the parent tradeoff
 	 * @param tradeName - the parents name
 	 * @param whichOnt the element name
 	 * @return our elements name
 	 */
 	public String getTradeOntology(String tradeName, String whichOnt) {
 		String onts = getElement(tradeName, "tradeoffs", whichOnt,
 				RationaleElementType.ONTENTRY);
 		return onts;
 	}
 	/**
 	 * Return the name of the ontology entry mapping to the parent claim
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getOntEntry(String parentName) {
 		String onts = getElement(parentName, "claims", "ontology",
 				RationaleElementType.ONTENTRY);
 		return onts;
 	}
 
 
 
 	/**
 	 * Given the name of the parent, the table type of the parent, the name
 	 * of the element field in the parent table, and the element type, get the
 	 * name of the element. Ok, this is confusing but the parent table has the
 	 * ID of the item associated with it, not its name. As an example, you
 	 * want the ontology entry associated with a claim called "Cheap to code" so
 	 * you would give the parent name as "Cheap to code", the parent table as "claims", the
 	 * name of the field that will identy the ontology (rather misleadingly
 	 * called "elementName" here) as "ontology", and the element type as ONTENTRY.
 	 *
 	 * @param parentName - the name of the parent element
 	 * @param parentTable - the type of table for the parent
 	 * @param elementName - the name of the field in the parent's table that contains our element
 	 * @param elementType - the type of element we are trying to get
 	 * @return the name of our child element
 	 */
 	private String getElement(String parentName, String parentTable,
 			String elementName, RationaleElementType elementType) {
 
 		String ourElement = "not set!";
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT " + elementName + " FROM " 
 			+ RationaleDBUtil.escapeTableName(parentTable)
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt(elementName);
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				//***			System.out.println("No matching " + elementType + " for name " + parentName);
 				return ("");
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(RationaleDB.getTableName(elementType)) 
 				+ " WHERE id = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				ourElement = RationaleDBUtil.decode(rs.getString("name"));
 			}
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "error in getElement", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElement;
 	}
 
 	/**
 	 * Return the name of the ont entry mapping to the parent claim
 	 * @param parentName - the parents name
 	 * @return our elements name
 	 */
 	public String getClaimOntEntry(String parentName) {
 
 		String ourElement = "not set!";
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			String direction = "";
 			//first, get the ID of our element
 			findQuery = "SELECT ontology, direction " + " FROM "
 			+ RationaleDBUtil.escapeTableName("claims") + " " 
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 			//				System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("ontology");
 				direction = rs.getString("direction") + " ";
 				//				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching ontolgy entry " + " for name "
 						+ parentName);
 				return ("");
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("OntEntries") + " " 
 				+ " WHERE id = "
 				+ new Integer(pid).toString();
 			//			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				ourElement = direction
 				+ RationaleDBUtil.decode(rs.getString("name"));
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getClaimOntEntry", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElement;
 	}
 
 	/**
 	 * Find the names of the ontology entries that are children of the parent
 	 * @param parentName - the name of the parent element
 	 * @return the name of the ontology entries
 	 */
 	public Vector<String> getOntology(String parentName) {
 
 		Vector<String> onts = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " + 
 			RationaleDBUtil.escapeTableName("ontentries") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching ontolgy entry " + " for name "
 						+ parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("OntRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration<String> ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("ontentries") + " "
 					+ "WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					onts.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return onts;
 	}
 
 	/**
 	 * Delete a pattern from the database. Before deleting, must first delete all
 	 * references to this pattern.
 	 * @param patternName
 	 */
 	public void removePattern(String patternName){
 		Statement stmt = null;
 		ResultSet rs = null;
 		String query = "";
 		int patternID = -1;
 
 		//First, get the ID of the pattern.
 		query = "SELECT id from PATTERNS where name = '" + patternName + "'";
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(query);
 			if (rs.next()){
 				patternID = rs.getInt(1);
 			}
 			if (patternID == -1){
 				System.err.println("Cannot find the ID of the pattern");
 				return;
 			}
 
 			Vector<Integer> assocDecision = new Vector<Integer>(); //vector of all associated pattern-decisions
 
 			query = "SELECT id from PATTERNDECISIONS where parent = " + patternID;
 			rs = stmt.executeQuery(query);
 			while (rs.next()){
 				assocDecision.add(rs.getInt(1));
 			}
 
 			//Remove subpatterns first, then decisions, then ontologies.
 			Iterator<Integer> decIterator = assocDecision.iterator();
 			while (decIterator.hasNext()){
 				query = "DELETE FROM PATTERN_DECISION where decisionID = " + Integer.parseInt(decIterator.next().toString());
 				stmt.execute(query);
 			}
 			query = "DELETE FROM PATTERNDECISIONS where parent = " + patternID;
 			stmt.execute(query);
 			query = "DELETE FROM PATTERN_ONTENTRIES where patternID = " + patternID;
 			stmt.execute(query);
 			query = "DELETE FROM PATTERNS where id = " + patternID;
 			stmt.execute(query);
 
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Return a vector containing all parents created from the database entry.
 	 * @return a vector of patterns created.
 	 */
 	public Vector<Pattern> getPatterns(){
 		Vector<Pattern> patterns = new Vector<Pattern>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name FROM patterns";
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				Pattern newPattern = new Pattern();
 				newPattern.fromDatabase(rs.getString("name"));
 				patterns.add(newPattern);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "error in getPatterns", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}	
 		return patterns;		
 	}
 
 	/**
 	 * Create a vector of new OntEntries that are associated with the given pattern(name).
 	 * @param parentName
 	 * @param isPositive
 	 * @return
 	 */
 	public Vector<OntEntry> getPatternOntologies(String parentName, boolean isPositive){
 
 		Pattern patternSelected = new Pattern();
 		patternSelected.fromDatabase(parentName);
 
 		Vector<OntEntry> onts = new Vector<OntEntry>();
 
 		if(isPositive){
 			onts = patternSelected.getPosiOnts();
 		}else{
 			onts = patternSelected.getNegaOnts();
 		}
 		return onts;
 	}
 
 	/**
 	 * Create a vector of new Candidate Patterns that are associated with the given pattern(name)
 	 * @param parentName
 	 * @return
 	 */
 	public Vector<Pattern> getCandidatePatterns(String parentName){
 		Vector<Pattern> candidatePatterns = new Vector<Pattern>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 
 		try {
 			PatternDecision parentDecision = new PatternDecision();
 			parentDecision.fromDatabase(parentName);
 			findQuery = "SELECT patternID FROM pattern_decision WHERE decisionID = "
 				+ parentDecision.getID()
 				+ " and parentType = 'Decision'";
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(findQuery);
 			while(rs.next()){
 				Pattern candidate = new Pattern();
 				candidate.fromDatabase(rs.getInt("patternID"));
 				candidatePatterns.add(candidate);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 
 		return candidatePatterns;		
 	}
 
 
 	/**
 	 * Create a vector of decisions for a given pattern(name) and its grandparent(name).
 	 * @param parentName
 	 * @param grandParentName
 	 * @return
 	 */
 	public Vector<PatternDecision> getPatternDecisions(String parentName, String grandParentName){
 
 		Vector<PatternDecision> children = new Vector<PatternDecision>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			findQuery = "SELECT id from patterns where name = '" + parentName + "'";
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(findQuery);
 			int patternID = -1;
 			if(rs.next()){
 				patternID = rs.getInt("id");
 			}		
 			findQuery = "SELECT * FROM patterndecisions where parent = " + patternID;
 			rs = stmt.executeQuery(findQuery);
 
 			while(rs.next()){
 				PatternDecision patternDecision = new PatternDecision();
 				patternDecision.fromDatabase(RationaleDBUtil.decode(rs.getString("name")));
 
 				children.addElement(patternDecision);
 			}
 		}catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 
 
 		return children;
 	}
 
 	/**
 	 * Gets a list of all the constraints belonging to a particular parent entry
 	 * @param parentName - the name of the parent
 	 * @param ptype - the parents type
 	 * @return a vector of constraints
 	 */
 	public Vector<String> getConstraints(String parentName, RationaleElementType ptype) {
 
 		Vector<String> consts = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			if (ptype == RationaleElementType.CONSTRAINT) {
 
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM "
 				+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					//						System.out.println(findQuery);
 					System.out.println("No matching constraint entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT child FROM " 
 					+ RationaleDBUtil.escapeTableName("ConstraintRelationships") + " "
 					+ " WHERE parent = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("child"))
 					.toString());
 				}
 				rs.close();
 			} else if (ptype == RationaleElementType.ALTCONSTREL) {
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM " 
 				+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					System.out.println(findQuery);
 					System.out.println("No matching parent entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT constr FROM " 
 					+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 					+ " WHERE id = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("constr"))
 					.toString());
 				}
 				rs.close();
 			} else if (ptype == RationaleElementType.DECISION) {
 				int pid = 0;
 				//first, get the ID of our element
 				findQuery = "SELECT id " + " FROM " +
 				RationaleDBUtil.escapeTableName("Decisions") + " "
 				+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 				+ "'";
 
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					System.out.println(findQuery);
 					System.out.println("No matching parent entry "
 							+ " for name " + parentName);
 				}
 
 				findQuery = "SELECT constr FROM " 
 					+ RationaleDBUtil.escapeTableName("ConDecRelationships") + " "
 					+ " WHERE decision = " + new Integer(pid).toString();
 
 				rs = stmt.executeQuery(findQuery);
 
 				while (rs.next()) {
 					children.addElement(new Integer(rs.getInt("constr"))
 					.toString());
 				}
 				rs.close();
 			}
 			//Now, finally, get our children
 			Enumeration<String> ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " " 
 					+ "WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					consts.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getConstraints", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return consts;
 	}
 
 	/**
 	 * Return a list of relationships between alternatives and constraints
 	 * @param parentName - the name of the parent alternative
 	 * @return the list of relationships (constraint names???)
 	 */
 	public Vector<String> getAltConstRels(String parentName) {
 
 		Vector<String> relationships = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 			+ RationaleDBUtil.escapeTableName("Alternatives") + " "
 			+ " WHERE name = '"
 			+ RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//						System.out.println(findQuery);
 				System.out.println("No matching constraint entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("AltConstRel") + " "
 				+ " WHERE alternative = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				relationships.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			rs.close();
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getConstraints", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return relationships;
 	}
 
 	/**
 	 * Gets a list of child design components given the name of a parent component
 	 * @param parentName - the parent component name
 	 * @return a list of child names
 	 */
 	public Vector<String> getProducts(String parentName) {
 
 		Vector<String> products = new Vector<String>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 			+ RationaleDBUtil.escapeTableName("DesignComponents") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName) + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				System.out.println("No matching component entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("DesignComponentRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration<String> ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("DesignComponents") + " " 
 					+ " WHERE id = " + (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					products.addElement(RationaleDBUtil.decode(rs.getString("name")));
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getOntology", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return products;
 	}
 
 	/**
 	 * Return a list of child ontology entries given the parent
 	 * @param parentName - the name of the parent
 	 * @return a list of child elements - the actual elements, not just names
 	 */
 	public Vector<OntEntry> getOntologyElements(String parentName) {
 
 		Vector<OntEntry> onts = new Vector<OntEntry>();
 		ResultSet rs = null;
 		try {
 			m_ontEntriesQuery.setString(1, parentName);
 			rs = m_ontEntriesQuery.executeQuery();
 
 			while (rs.next()) {			
 				OntEntry entry = new OntEntry();
 				entry.fromDatabase(rs.getString("childName"));
 				onts.addElement(entry);
 			}
 			rs.close();
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOntologyElements", "getOntologyElements");
 		} finally {
 			releaseResources(null, rs);
 		}
 		return onts;
 	}
 
 	/**
 	 * Return a list of child element entries given the parent for treeview display
 	 * @param parentName - the name of the parent
 	 * @param type - specify the type of element
 	 * @return a list of child elements - the actual elements, not just names
 	 */
 	public Vector<String> getElements_TreeView(String parentName, RationaleElementType type,boolean firstTime) {
 
 		Vector<String> elements = new Vector<String>();
 		ResultSet rs = null;
 		try {
 			RationaleDB db = RationaleDB.getHandle();
 			Connection conn = db.getConnection();
 			Statement stmt = null;
 			String selectStr="";
 			if(firstTime)
 			{
 				if (type==RationaleElementType.REQUIREMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (requirements as a) left join (requirements as b) on (a.parent=b.id)) as t where t.parentName is null";
 				else if (type==RationaleElementType.DECISION)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (decisions as a) left join (decisions as b) on (a.parent=b.id)) as t where t.parentName is null";
 				else if (type==RationaleElementType.ALTERNATIVE)
 					selectStr="Select * from (SELECT a.id as childID,a.ptype,a.name as childName,a.parent as parentID,b.name as parentName FROM (alternatives as a) left join (alternatives as b) on (a.parent=b.id)) as t where t.ptype="+"'Decision'";
 				else if (type==RationaleElementType.ARGUMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (arguments as a) left join (arguments as b) on (a.parent=b.id)) as t where t.parentName is null";
 			}
 			else
 			{
 				if (type==RationaleElementType.REQUIREMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (requirements as a) left join (requirements as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.DECISION)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (decisions as a) left join (decisions as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.ALTERNATIVE)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (alternatives as a) left join (alternatives as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 				else if (type==RationaleElementType.ARGUMENT)
 					selectStr="Select * from (SELECT a.id as childID,a.name as childName,a.parent as parentID,b.name as parentName FROM (arguments as a) left join (arguments as b) on (a.parent=b.id)) as t where t.parentName="+"'"+parentName+"'";
 			}
 			//			System.out.println(selectStr);
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(selectStr);
 			while (rs.next()) {			
 				String name = "";
 				name = rs.getString("childName");
 				elements.addElement(name);
 			}
 			rs.close();
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getElements_TreeView method", "getOntologyElements");
 		} finally {
 			releaseResources(null, rs);
 		}
 		return elements;
 	}
 
 
 	/**
 	 * Given a parent constraint, return a list of sub-constraints
 	 * @param parentName - the name of the parent
 	 * @return the child constraints
 	 */
 	public Vector<Constraint> getConstraintElements(String parentName) {
 
 		Vector<Constraint> constrs = new Vector<Constraint>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " 
 			+ RationaleDBUtil.escapeTableName("CONSTRAINTS") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				System.out.println(findQuery);
 				System.out.println("No matching constraint entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("ConstraintRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration<String> ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escape("CONSTRAINTS") + " "
 					+ " WHERE id = "
 					+ (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					Constraint entry = new Constraint();
 					entry
 					.fromDatabase(RationaleDBUtil.decode(rs
 							.getString("name")));
 					constrs.addElement(entry);
 
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getConstraintElements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return constrs;
 	}
 
 	/**
 	 * Get a list of sub-design product elements given the parent component
 	 * @param parentName - the name of the parent element
 	 * @return the list of sub-elements
 	 */
 	public Vector<DesignProductEntry> getDesignProductElements(String parentName) {
 		String findQuery = "";
 		Vector<DesignProductEntry> components = new Vector<DesignProductEntry>();
 		Vector<String> children = new Vector<String>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			int pid = 0;
 			//first, get the ID of our element
 			findQuery = "SELECT id " + " FROM " +
 			RationaleDBUtil.escapeTableName("DesignComponents") + " "
 			+ " WHERE name = '" + RationaleDBUtil.escape(parentName)
 			+ "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				pid = rs.getInt("id");
 				rs.close();
 			} else {
 				System.out.println(findQuery);
 				System.out.println("No matching design component entry "
 						+ " for name " + parentName);
 			}
 
 			findQuery = "SELECT child FROM " 
 				+ RationaleDBUtil.escapeTableName("DesignComponentRelationships") + " "
 				+ " WHERE parent = " + new Integer(pid).toString();
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				children.addElement(new Integer(rs.getInt("child")).toString());
 			}
 			rs.close();
 			//Now, finally, get our children
 			Enumeration<String> ids = children.elements();
 			while (ids.hasMoreElements()) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("DesignComponents") + " "
 					+ " WHERE id = " + (String) ids.nextElement();
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					DesignProductEntry entry = new DesignProductEntry();
 					entry
 					.fromDatabase(RationaleDBUtil.decode(rs
 							.getString("name")));
 					components.addElement(entry);
 
 					rs.close();
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getDesignProductElements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return components;
 	}
 	/**
 	 * Given the name of an argument, get its type from the database
 	 * @param argName - argument name
 	 * @return argument type
 	 */
 	public String getArgumentType(String argName) {
 		String argType = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		//just in case
 		argName = RationaleDBUtil.escape(argName);
 		try {
 			stmt = conn.createStatement();
 			//first, get the ID of our element
 			findQuery = "SELECT argtype from " +
 			RationaleDBUtil.escapeTableName("arguments") + " "
 			+ " where name ='" + argName + "'";
 
 			rs = stmt.executeQuery(findQuery);
 			if (rs.next()) {
 				argType = rs.getString("argtype");
 				rs.close();
 			} else {
 				//				System.out.println(findQuery);
 				argType = "";
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getArgumentType", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return argType;
 	}
 
 
 	/**
 	 * Get a list of names of all elements in the database that have a
 	 * particular rationale element type
 	 * @param type - the type of element we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getNameList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String tableName = RationaleDB.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			String additional = "";
 			if (type == RationaleElementType.TRADEOFF) {
 				additional = "WHERE type = 'Tradeoff'";
 			}
 			else if (type == RationaleElementType.COOCCURRENCE) {
 				additional = "WHERE type = 'Co-Occurrence'";
 			}
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ additional + " ORDER BY name ASC";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 
 	}
 
 	/**
 	 * This method provides better running time than getPatternByCategory.
 	 * Used for generate candidate patterns.
 	 * @param categoryID
 	 * @return
 	 */
 	public ArrayList<Pattern> getPatternByCategoryID(int categoryID){
 		ArrayList<Pattern> matchingPatterns = new ArrayList<Pattern>();
 
 		try{
 			String query = "";
 			Statement stmt = null;
 			ResultSet rs = null;
 			stmt = conn.createStatement();
 			query = "select patternID from pattern_problemcategory where problemcategoryID = " + categoryID;
 			rs = stmt.executeQuery(query);
 			while (rs.next()){
 				Pattern pattern = new Pattern();
 				pattern.fromDatabase(new Integer(rs.getInt("patternID")));
 				matchingPatterns.add(pattern);
 			}
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 		return matchingPatterns;
 	}
 
 	/**
 	 * List all patterns associated with one category
 	 * @param category
 	 * @return An array list of patterns
 	 */
 	public ArrayList<Pattern> getPatternByCategory(String category){
 		ArrayList<Pattern> matchedPatterns = new ArrayList<Pattern>();
 
 		try {
 			String query = "";
 			Statement stmt = null;
 			ResultSet rs = null;
 			stmt = conn.createStatement();
 			query = "SELECT id FROM patternproblemcategories WHERE problemcategory = '" + category + "'";
 			rs = stmt.executeQuery(query);
 
 			if(rs.next()){
 
 				query = "SELECT patternID FROM pattern_problemcategory WHERE problemcategoryID = " + rs.getInt("id");
 				rs = stmt.executeQuery(query);
 
 				while(rs.next()){
 					Pattern pattern = new Pattern();
 					pattern.fromDatabase(new Integer(rs.getInt("patternID")));
 					matchedPatterns.add(pattern);
 				}
 			}			
 
 		} catch (SQLException e) {
 
 			e.printStackTrace();
 		}		
 		return matchedPatterns;
 	}
 
 	/**
 	 * Returns the id of the problem category given a pattern ID
 	 * @param patternID
 	 * @return -1 if it is not associated, otherwise it returns the ID of the problem category.
 	 */
 	public int getCategoryByPattern(int patternID){
 		int toReturn = -1;
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			String query = "SELECT problemcategoryID from pattern_problemcategory WHERE patternID = " + patternID + "";
 			rs = stmt.executeQuery(query);
 			if (rs.next()){
 				toReturn = rs.getInt("problemcategoryID");
 			}
 		}catch (SQLException e){
 			e.printStackTrace();
 		}
 		finally{
 			releaseResources(stmt, rs);
 		}
 		return toReturn;
 	}
 
 	/**
 	 * Given a pattern type, return a map from name to id of problem categories.
 	 * @param patternType
 	 * @return a has map between category id and category name.
 	 */
 	public HashMap<String, Integer> getCategories(String patternType){
 
 		//Wang uses upper case for archiecture name, so we should 'convert' it first...
 		/*char upperCaseBeginning = patternType.toUpperCase().charAt(0);
 		String correctedType = upperCaseBeginning + patternType.substring(1);
 		patternType = correctedType;*/
 		//This should be fixed...
 
 		HashMap<String, Integer> map = new HashMap<String, Integer>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			String query = "SELECT id, problemcategory FROM patternproblemcategories  WHERE patterntype = '" + patternType + "'";
 			rs = stmt.executeQuery(query);
 			while (rs.next()){
 				Integer id = rs.getInt("id");
 				String problemcategory = rs.getString("problemcategory");
 				map.put(problemcategory, id);
 			}
 		} catch (SQLException e) {e.printStackTrace();};
 		return map;
 	}
 
 	/**
 	 * Given a pattern type, return all patterns matches the type.
 	 * The type is "archiecture", "idiom", or "design" 
 	 * @param patternType
 	 * @return A vector of TreeParent, having element type pattern
 	 */
 	private Vector<TreeParent> getPatterns(String patternType){
 		Vector<TreeParent> patternList = new Vector<TreeParent>();
 		String query = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		Pattern pattern = new Pattern();
 
 		try {
 			stmt = conn.createStatement();
 			query = "SELECT name FROM patterns WHERE type = '"
 				+ patternType + "'";
 			rs = stmt.executeQuery(query);
 			//			while(rs.next()){
 			//				pattern.fromDatabase((rs.getString("name")));
 			//				System.out.println(pattern.getName());
 			//				patternList.add(pattern);
 			//			}q1]
 
 			while (rs.next()) {
 				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 						.getString("name")), RationaleElementType.PATTERN);
 				patternList.addElement(element);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return patternList;
 	}
 
 	/**
 	 * Given a pattern type, return all patterns matches the type
 	 * @param type
 	 * @return A vector of Patterns
 	 */
 	public ArrayList<Pattern> getPatternsByType(String type){
 		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
 		String query = "";
 		Statement stmt = null;
 		ResultSet rs = null;		
 
 		try {
 			stmt = conn.createStatement();
 			query = "SELECT name FROM patterns WHERE type = '"
 				+ type + "'";
 			rs = stmt.executeQuery(query);
 			while(rs.next()){
 				Pattern pattern = new Pattern();
 				pattern.fromDatabase((rs.getString("name")));
 				System.out.println(pattern.getName());
 				patterns.add(pattern);
 			}
 
 			//			while (rs.next()) {
 			//				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 			//						.getString("name")), RationaleElementType.PATTERN);
 			//				patternList.addElement(element);
 			//			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 		return patterns;
 	}
 
 	/**
 	 * Get a list of names of all elements in the database that have a
 	 * particular rationale element type and where the name contains a search string
 	 * @param type - the type of element we are looking for
 	 * @param sstring - the substring we want to find in the name
 	 * @return - the list of names
 	 */
 	public Vector<String> getNameList(RationaleElementType type, String sstring) {
 		Vector<String> ourElements = new Vector<String>();
 		String tableName = RationaleDB.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			String additional = "";
 			if (type == RationaleElementType.TRADEOFF) {
 				additional = "WHERE type = 'Tradeoff'";
 			}
 			else if (type == RationaleElementType.COOCCURRENCE) {
 				additional = "WHERE type = 'Co-Occurrence'";
 			}
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ additional 
 				+ "WHERE upper(name) LIKE" + " upper('%" + sstring + "%')"
 				+ " ORDER BY name ASC";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList with Search String", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 
 	}
 	/**
 	 * Find out if a  particular element exists and has the passed in name
 	 * @param name - the name of the element
 	 * @param type - the type of element we are looking for
 	 * @return - the list of names
 	 */
 	public static boolean elementExists(String name, RationaleElementType type) {
 
 		boolean exists;
 		exists = false;
 		//int id = -1;
 
 		//Vector<String> ourElements = new Vector<String>();
 		String tableName = RationaleDB.getTableName(type);
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT id FROM " 
 				+ RationaleDBUtil.escapeTableName(tableName) + " " 
 				+ "WHERE name = '" +
 				name + "' ";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				//int nextID = rs.getInt("id");
 				exists = true;
 			}
 			rs.close();
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in elementExists", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return exists;
 
 	}
 	/**
 	 * Get a list of all "potential new parents" for a particular type of candidate rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getCandidateList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			if (type == RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "SELECT name FROM candidates where type='Decision'";
 			}
 			else if (type == RationaleElementType.ARGUMENT)
 			{
 				findQuery = "SELECT name FROM candidates where type='Requirement' or type='Alternative'";	
 			}
 			else if (type == RationaleElementType.ASSUMPTION)
 			{
 				findQuery = "SELECT name FROM candidates where type='Argument'";
 			}
 			else if (type == RationaleElementType.QUESTION)
 			{
 				findQuery = "SELECT name FROM candidates where type='Decision' or type='Alternative'";	
 			}
 			else
 			{
 				return null;
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 	}
 
 	/**
 	 * Get a list of all "potential new parents" for a particular type of rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @return - the list of names
 	 */
 	public Vector<String> getRationaleElementList(RationaleElementType type) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			if (type == RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "SELECT name FROM decisions";
 			}
 			/*			else if (type == RationaleElementType.ARGUMENT)
 			{
 				findQuery = "(SELECT name FROM requirements) union (select name FROM alternatives)";	
 			} */
 			else if (type == RationaleElementType.REQUIREMENT)
 			{
 				findQuery = "SELECT name FROM requirements";	
 			}
 			else if (type == RationaleElementType.DECISION)
 			{
 				findQuery = "select name FROM alternatives";
 			}
 			else if (type == RationaleElementType.QUESTION)
 			{
 				findQuery = "SELECT decisions.name,alternatives.name FROM decisions ,alternatives";	
 			}
 			else
 			{
 				return null;
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;	
 	}
 
 	/**
 	 * Get a list of all "potential new parents" for Argument type of rationale element
 	 * @param type - the type of element whose new parent we are looking for
 	 * @param ptype - parent's the type of element
 	 * @return - the list of names
 	 */
 	public Vector<String> getRationaleElementList(RationaleElementType type, RationaleElementType ptype ) {
 		Vector<String> ourElements = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 
 			if (type == RationaleElementType.ARGUMENT && ptype== RationaleElementType.REQUIREMENT)
 			{
 				findQuery = "SELECT name FROM requirements";	
 			}
 			if (type == RationaleElementType.ARGUMENT && ptype== RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "select name FROM alternatives";	
 			}
 			if (type == RationaleElementType.DECISION && ptype== RationaleElementType.DECISION)
 			{
 				findQuery = "SELECT name FROM decisions";	
 			}
 			if (type == RationaleElementType.DECISION && ptype== RationaleElementType.ALTERNATIVE)
 			{
 				findQuery = "select name FROM alternatives";	
 			}
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				ourElements
 				.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getNameList", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return ourElements;
 	}
 
 
 	/**
 	 * Returns a list of TreeParent items (to put in the tree from the RationaleExplorer)
 	 * from the database given the parent name, the element type, and the parent type.
 	 * @param parentName - parent name
 	 * @param elementType - type of element we are looking for (table name)
 	 * @param parentType - parents element type
 	 * @return a list of tree elements
 	 */
 	private Vector<TreeParent> getTreeElements(String parentName, String elementType,
 			RationaleElementType parentType) {
 
 		Vector<TreeParent> treeElementList = new Vector<TreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			if (parentType == null) {
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName(elementType)
 					+ " ORDER BY name ASC";
 			} else {
 				//first, lets get the ID for the parent
 				int pid = 0;
 				findQuery = "SELECT id FROM " 
 					+ RationaleDBUtil.escapeTableName(getTableName(parentType))
 					+ "  WHERE name= '" + RationaleDBUtil.escape(parentName)
 					+ "'";
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					error = true;
 					System.out.println(findQuery);
 					System.out.println("No matching " + elementType
 							+ " for name " + parentName);
 					return (new Vector<TreeParent>());
 				}
 				/*already have parentType==null in the front. This can't happen.
 				if (parentType == null) {
 					findQuery = "SELECT name FROM " 
 						+ RationaleDBUtil.escapeTableName(elementType) + " "
 						+ " WHERE parent = " + new Integer(pid).toString()
 						+ " ORDER BY name ASC";
 				} else {
 				 */
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName(elementType) + " "
 					+ " WHERE parent = " + new Integer(pid).toString()
 					+ " AND ptype = '" + parentType.toString()
 					+ "' ORDER BY name ASC";
 				//}
 
 			}
 			if (error) {
 				//				 System.out.println(findQuery);
 			}
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 						.getString("name")), getElementType(elementType));
 				treeElementList.addElement(element);
 			}
 			//now, need to enumerate over the list of items and add
 			//their status information *if* this is a type that has status.
 
 			RationaleElementType type = getElementType(elementType);
 
 			if ((type == RationaleElementType.REQUIREMENT)
 					|| (type == RationaleElementType.DECISION)
 					|| (type == RationaleElementType.ALTERNATIVE)) {
 
 				Enumeration<TreeParent> stats = treeElementList.elements();
 				while (stats.hasMoreElements()) {
 					TreeParent element = (TreeParent) stats.nextElement();
 					RationaleErrorLevel ourStatus = getActiveStatus(element
 							.getName(), type);
 					element.setStatus(ourStatus);
 				}
 			}
 
 		} catch (SQLException ex) {
 			if (ex.getErrorCode() == 1146) {
 				reportError(ex, "getTreeElements - could not find table",
 						findQuery);
 			} else {
 				reportError(ex, "ERror in getTreeELements", findQuery);
 			}
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return treeElementList;
 	}
 
 	/**
 	 * Returns a list of CandidateTreeParent items (to put in the tree from the RationaleExplorer) 
 	 * from the database given the element type
 	 * @param elementType - type of element we are looking for
 	 * @return a list of tree elements
 	 */
 	public Vector<CandidateTreeParent> getCandidateTreeElements(RationaleElementType etype) {
 
 		Vector<CandidateTreeParent> treeElementList = new Vector<CandidateTreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		//boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "Select name FROM candidates where type = '" + 
 			etype.toString() + "'";
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				CandidateTreeParent treeE = new CandidateTreeParent(name, etype);
 				treeElementList.add(treeE);
 			}
 
 
 		} catch (SQLException ex) {
 			if (ex.getErrorCode() == 1146) {
 				reportError(ex, "getTreeElements - could not find table",
 						findQuery);
 			} else {
 				reportError(ex, "ERror in getTreeELements", findQuery);
 			}
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return treeElementList;
 	}
 
 
 	/**
 	 * Given an alternative, find all the related constraints.
 	 * @param parentName - the name of the parent alternative
 	 * @param parentType - it is not clear why this is needed!
 	 * Probably a cut-and-paste artifact...
 	 * @return a list of constraints in tree element form
 	 */
 	public Vector<TreeParent> getAltConstRels(String parentName,
 			RationaleElementType parentType) {
 
 		Vector<TreeParent> relationshipList = new Vector<TreeParent>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			if (parentType == null) {
 				findQuery = "SELECT name FROM "
 					+ RationaleDBUtil.escapeTableName("AltConstRel") + " " 
 					+ " ORDER BY name ASC";
 			} else {
 				//first, lets get the ID for the parent
 				int pid = 0;
 				findQuery = "SELECT id FROM " 
 					+ RationaleDBUtil.escapeTableName(getTableName(parentType)) + " "
 					+ "  WHERE name= '" + RationaleDBUtil.escape(parentName)
 					+ "'";
 				//
 				rs = stmt.executeQuery(findQuery);
 				if (rs.next()) {
 					pid = rs.getInt("id");
 					rs.close();
 				} else {
 					error = true;
 					System.out.println(findQuery);
 					System.out.println("No matching " + "constraint rel "
 							+ " for name " + parentName);
 					return (new Vector<TreeParent>());
 				}
 
 				findQuery = "SELECT name FROM " 
 					+ RationaleDBUtil.escapeTableName("altconstrel")
 					+ " WHERE alternative = " + new Integer(pid).toString()
 					+ " ORDER BY name ASC";
 
 			}
 			if (error) {
 				//				 System.out.println(findQuery);
 			}
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				TreeParent element = new TreeParent(RationaleDBUtil.decode(rs
 						.getString("name")), RationaleElementType.ALTCONSTREL);
 				relationshipList.addElement(element);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "ERror in getTreeELements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return relationshipList;
 
 	}
 
 	/**
 	 * Find out what error level an element has (error, warning, information). If
 	 * there is more than one error, return the most severe level
 	 * @param name - the name of the rationale element
 	 * @param elementType - the type of the rationale element
 	 * @return the error level
 	 */
 	public RationaleErrorLevel getActiveStatus(String name,
 			RationaleElementType elementType) {
 		String findQuery = "";
 		RationaleErrorLevel worst;
 		Statement stmt = null;
 		ResultSet rs = null;
 		//Assume least severe error level to start
 		worst = RationaleErrorLevel.INFORMATION;
 		//just in case
 		name = RationaleDBUtil.escape(name);
 		try {
 			stmt = conn.createStatement();
 			;
 			findQuery = "SELECT S.type from "
 				+ RationaleDBUtil.escapeTableName("status") + " S, "
 				+ RationaleDBUtil.escapeTableName(getTableName(elementType))
 				+ " P" + " where P.name = '"
 				+ name + "' and " + "P.id = S.parent " + "and S.ptype = '"
 				+ elementType.toString() + "' and override = 'No'";
 
 			//***	 System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				//				System.out.println("found element");
 				//				System.out.println("type = " + rs.getString("type"));
 				RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 						.getString("type"));
 				if (worst == RationaleErrorLevel.INFORMATION) {
 					worst = newStat;
 				} else if (worst == RationaleErrorLevel.WARNING) {
 					if (newStat == RationaleErrorLevel.ERROR) {
 						worst = newStat;
 					}
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "SQLException, get status: " + ex.getMessage(),
 					findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return worst;
 
 	}
 	/*
 	 public RationaleErrorLevel getStatus(String name,
 	 RationaleElementType elementType) {
 	 String findQuery = "";
 	 RationaleErrorLevel worst;
 	 Statement stmt = null;
 	 ResultSet rs = null;
 	 worst = RationaleErrorLevel.INFORMATION;
 	 //just in case
 	  name = RationaleDB.escape(name);
 	  try {
 	  stmt = conn.createStatement();
 	  ;
 	  findQuery = "SELECT S.type from status S, "
 	  + getTableName(elementType) + " P" + " where P.name = '"
 	  + name + "' and " + "P.id = S.parent " + "and S.ptype = '"
 	  + elementType.toString() + "'";
 	  //***	 System.out.println(findQuery);
 	   rs = stmt.executeQuery(findQuery);
 
 	   while (rs.next()) {
 	   //				System.out.println("found element");
 	    //				System.out.println("type = " + rs.getString("type"));
 	     RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 	     .getString("type"));
 	     if (worst == RationaleErrorLevel.INFORMATION) {
 	     worst = newStat;
 	     } else if (worst == RationaleErrorLevel.WARNING) {
 	     if (newStat == RationaleErrorLevel.ERROR) {
 	     worst = newStat;
 	     }
 	     }
 	     }
 
 	     } catch (SQLException ex) {
 	     reportError(ex, "SQLException, get status: " + ex.getMessage(),
 	     findQuery);
 	     } finally {
 	     releaseResources(stmt, rs);
 	     }
 	     return worst;
 
 	     }
 	 */
 
 
 	/**
 	 * Given an element ID and type, find out if it is enabled (active) or not
 	 * @param pid - the element ID
 	 * @param elementType - the type
 	 * @return true if the element is enabled
 	 */
 	public boolean getActive(int pid, RationaleElementType elementType) {
 		RationaleElement ourElement = getRationaleElement(pid, elementType);
 		return ourElement.getEnabled();
 	}
 
 	/**
 	 * Find the rationale error level given an element ID and type. Seems a bit redundant
 	 * with ActiveStatus which uses the name and type...
 	 * @param pid - the element ID
 	 * @param elementType - the element type
 	 * @return - the level of error
 	 */
 	public RationaleErrorLevel getStatusLevel(int pid,
 			RationaleElementType elementType) {
 		RationaleErrorLevel worst;
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		worst = RationaleErrorLevel.NONE;
 		try {
 			stmt = conn.createStatement();
 			;
 			findQuery = "SELECT S.type from " 
 				+ RationaleDBUtil.escapeTableName("status") + " S, "
 				+ RationaleDBUtil.escapeTableName(getTableName(elementType))
 				+ " P where " + "S.parent = "
 				+ new Integer(pid).toString() + " and S.ptype = '"
 				+ elementType.toString() + "' and S.override = 'No'";
 			//***		 System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				//***				System.out.println("found element");
 				//***				System.out.println("type = " + rs.getString("type"));
 				RationaleErrorLevel newStat = RationaleErrorLevel.fromString(rs
 						.getString("type"));
 				if ((worst == RationaleErrorLevel.NONE)
 						|| (worst == RationaleErrorLevel.INFORMATION)) {
 					worst = newStat;
 				} else if (worst == RationaleErrorLevel.WARNING) {
 					if (newStat == RationaleErrorLevel.ERROR) {
 						worst = newStat;
 					}
 				}
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getStatusLevel", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return worst;
 
 	}
 
 
 	/**
 	 * Get a list of all elements that are tradeoffs or cooccurrences
 	 * @return a vector of tradeoff elements
 	 */
 	public Vector<Tradeoff> getTradeoffData() {
 		Vector<String> tradeoffNames = new Vector<String>();
 		Vector<Tradeoff> tradeoffList = new Vector<Tradeoff>();
 
 		tradeoffNames = getTradeoffs(RationaleElementType.TRADEOFF);
 		tradeoffNames.addAll(getTradeoffs(RationaleElementType.COOCCURRENCE));
 
 		Iterator<String> tradeI = tradeoffNames.iterator();
 		while (tradeI.hasNext()) {
 			Tradeoff ourTrade = new Tradeoff();
 			ourTrade.fromDatabase((String) tradeI.next());
 			tradeoffList.add(ourTrade);
 		}
 		return tradeoffList;
 	}
 
 	/**
 	 * Get the names of all the tradeoffs of a specific type (tradeoff or
 	 * cooccurrence relationship)
 	 * @param type - the type
 	 * @return a list of names
 	 */
 	public Vector<String> getTradeoffs(RationaleElementType type) {
 
 		Vector<String> nameList = new Vector<String>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT name FROM " 
 				+ RationaleDBUtil.escapeTableName("tradeoffs") + " "
 				+ " WHERE type = '" + type.toString() + "' ORDER BY name ASC";
 
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				nameList.addElement(RationaleDBUtil.decode(rs.getString("name")));
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getTradeoffs", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return nameList;
 	}
 
 	/**
 	 * Get all the status items from the database so they can be displayed on
 	 * the Task List
 	 * @return the current status elements
 	 */
 	public Vector<RationaleStatus> getStatus() {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status")
 				+ " ORDER BY description ASC";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(rs.getString("status")));
 				if (rs.getString("override").compareTo("Yes") == 0) {
 					rstat.setOverride(true);
 				}
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "error in getStatus", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Override the current status of an element in the database. This is done
 	 * if the user feels that an error or warning is not actually a problem and
 	 * they do not want to see it appear on the list of errors and warnings. Of course,
 	 * this can easily be reversed later.
 	 * @param descrip - the error/warning description
 	 * @param parent - the element the error/warning is about
 	 * @param ptype - the type of element the error/warning is about
 	 */
 	public void overrideStatus(String descrip, int parent, String ptype) {
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 
 			//who is our parent?
 			findQuery = "SELECT status  FROM " 
 				+ RationaleDBUtil.escapeTableName("status")
 				+ " WHERE parent = "
 				+ new Integer(parent).toString() + " and ptype = '" + ptype
 				+ "'" + " and description = '"
 				+ RationaleDBUtil.escape(descrip) + "'";
 			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			if (rs.next()) {
 				String stat = rs.getString("status");
 				String updateQuery;
 				updateQuery = "UPDATE " 
 					+ RationaleDBUtil.escapeTableName("Status") + " " 
 					+ "SET override = 'Yes' "
 					+ "WHERE status = '" + stat + "' AND parent = "
 					+ new Integer(parent).toString() + " AND ptype = '"
 					+ ptype + "'";
 				System.out.println(updateQuery);
 				stmt.execute(updateQuery);
 			}
 		} catch (SQLException ex) {
 			reportError(ex, "Error in OverrideStatus", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 	}
 
 	/**
 	 * Given an ID and type, get the rationale element's status
 	 * @param ID - the id
 	 * @param type - the type of element
 	 * @return the status
 	 */
 	public Vector<RationaleStatus> getStatus(int ID, RationaleElementType type) {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status") + " " 
 				+ " WHERE parent = "
 				+ new Integer(ID).toString() + " and ptype = '"
 				+ type.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(RationaleDBUtil.decode(rs
 								.getString("status"))));
 				if (rs.getString("override").compareTo("Yes") == 0) {
 					rstat.setOverride(true);
 				}
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getStatus2", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Get all the rationale status elements from the database that have been
 	 * overriden by the user.
 	 * @return the status overrides
 	 */
 	public Vector<RationaleStatus> getOverrides() {
 
 		Vector<RationaleStatus> statusList = new Vector<RationaleStatus>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("status") + " "
 				+ " WHERE override = 'Yes'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 
 				RationaleStatus rstat = new RationaleStatus(RationaleErrorLevel
 						.fromString(rs.getString("type")), RationaleDBUtil
 						.decode(rs.getString("description")),
 						RationaleElementType.fromString(rs.getString("ptype")),
 						rs.getDate("date"), rs.getInt("parent"),
 						RationaleStatusType.fromString(RationaleDBUtil.decode(rs
 								.getString("status"))));
 				rstat.setOverride(true);
 
 				statusList.add(rstat);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOverrides", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return statusList;
 	}
 
 	/**
 	 * Get patterns of type architecture
 	 * @param parentName
 	 * @param parentType
 	 * @return
 	 */
 	public Vector getArchitecturePatterns(String parentName,
 			RationaleElementType parentType) {
 
 
 		return getPatterns("Architecture");
 	}
 
 	/**
 	 * Get patterns of type design
 	 * @param parentName
 	 * @param parentType
 	 * @return
 	 */
 	public Vector getDesignPatterns(String parentName,
 			RationaleElementType parentType) {
 		return getPatterns("Design");
 	}
 
 	/**
 	 * Get patterns of type idiom
 	 * @return
 	 */
 	public Vector getIdioms() {
 		return getPatterns("Idiom");
 	}
 
 	/**
 	 * Add a new status elements to the database. This is done because
 	 * SEURAT detected problems with the rationale.
 	 * @param newStatus - the status element
 	 */
 	public void addStatus(Vector<RationaleStatus> newStatus) {
 		Iterator<RationaleStatus> statI = newStatus.iterator();
 		while (statI.hasNext()) {
 			RationaleStatus stat = (RationaleStatus) statI.next();
 			if (stat.getParent() <= 0) {
 				System.out.println("error in status value");
 			} else {
				System.out.println("Added status " + stat.getDescription());
 				stat.toDatabase(stat.getParent());
 			}
 
 		}
 	}
 
 	/**
 	 * Remove status elements from the database (presumably because the
 	 * problem with the rationale has been fixed).
 	 * @param oldStatus - the status element to remove
 	 */
 	public void removeStatus(Vector<RationaleStatus> oldStatus) {
 		String updateStr = "";
 		Iterator<RationaleStatus> statI = oldStatus.iterator();
 		while (statI.hasNext()) {
 			RationaleStatus stat = (RationaleStatus) statI.next();
 			Statement stmt = null;
 			try {
 				stmt = conn.createStatement();
 				updateStr = "DELETE from " 
 					+ RationaleDBUtil.escapeTableName("STATUS") + " "
 					+ " WHERE parent = "
 					+ new Integer(stat.getParent()).toString() + " and "
 					+ "ptype = '" + stat.getRationaleType().toString()
 					+ "' and status = '" + stat.getStatusType().toString()
 					+ "' and description = '"
 					+ RationaleDBUtil.escape(stat.getDescription()) + "'";
 				//			System.out.println(updateStr);
 				stmt.execute(updateStr);
 
 			} catch (SQLException ex) {
 				reportError(ex, "Error in removeStatus", updateStr);
 			} finally {
 				releaseResources(stmt, null);
 			}
 		} //end while
 	}
 
 	/**
 	 * Associate an alternative with a resource. In Eclipse, a resource can
 	 * be a file, attribute, or method. This information is actually maintained
 	 * using markers (bookmarks and special markers to put the rat icon on the file) but
 	 * we also store it in the database so we can share it between machines.  The database
 	 * schema also includes the line number, but this is useless in helping us restore
 	 * the markers between sessions, so the method no longer uses it.
 	 * @param alternative - the ID of the alternative being associated
 	 * @param artifact - the Eclipse format artifact identifier
 	 * @param resource - the file name for the resource - this is what gets displayed in the bookmark
 	 * @param name - the name of the method or attribute we are associating with
 	 * @param message - the name of the alternative that is displayed
 	 */
 	public void associateAlternative(String alternative, String artifact,
 			String resource, String name, String message) {
 		String getAltID = "";
 		String insertStr = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		//just in case
 		alternative = RationaleDBUtil.escape(alternative);
 		name = RationaleDBUtil.escape(name);
 		try {
 			stmt = conn.createStatement();
 			getAltID = "SELECT id from " 
 				+ RationaleDBUtil.escapeTableName("Alternatives") + " "
 				+ " where name = '"
 				+ alternative + "'";
 			rs = stmt.executeQuery(getAltID);
 			if (rs.next()) {
 				int altID = rs.getInt("id");
 				// TODO get alternative object here so that it can be used in an update event
 				insertStr = "Insert into "
 					+ RationaleDBUtil.escapeTableName("Associations") + " "
 					+ "(alternative, artifact, artresource, artname, assocmessage) "
 					+ "Values (" + Integer.toString(altID) + ", '"
 					+ artifact + "', '" + resource + "', '" + name + "', '"
 					+ RationaleDBUtil.escape(message) + "')";
 				//				System.out.println(insertStr);
 				stmt.execute(insertStr);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in associate alternative", getAltID + " or "
 					+ insertStr);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 	}
 
 	/**
 	 * Given the Eclipse format resource, delete all associations to that resource
 	 * from the associations table. This will remove all the associations to a file.
 	 * @param resource - the artifact to remove
 	 */
 	public void removeAssociation(String resource) {
 
 		Statement stmt = null;
 		String removeAssoc = "";
 		try {
 			stmt = conn.createStatement();
 			// TODO get alternative object here so that it can be used in an update event
 			removeAssoc = "Delete from Associations where artresource = '"
 				+ resource + "'";
 			stmt.execute(removeAssoc);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in removeAssociation", removeAssoc);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 	/**
 	 * Removes associations of a specific alternative to a specific file at
 	 * a specific line number. This is very brittle - if the line number comes from a bookmark
 	 * and the file has been edited it will not match the database!
 	 * @param resource - the file name where the association is
 	 * @param message - the name of the alternative it was associated with
 	 * @param lineNumber - the specific line number
 	 */
 	public void removeAssociation(String resource, String message,
 			int lineNumber) {
 		//TODO May not be necessary anymore? candidate for cleanup -molerjc
 		String removeAssoc = "";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			removeAssoc = "Delete from " 
 				+ RationaleDBUtil.escapeTableName("Associations") + " " 
 				+ " where artresource = '"
 				+ resource + "' and assocmessage = '"
 				+ RationaleDBUtil.escape(message) + "'";
 			System.out.println(removeAssoc);
 			stmt.execute(removeAssoc);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in removeAssociation", removeAssoc);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 
 	/**
 	 * Delete the candidate pattern associated with a pattern decision in Pattern Library
 	 * @param String - the name of the pattern, String - the name of the parent decision
 	 */
 	public void removeCandidatePattern(String patternName, String decisionName){
 		Statement stmt = null;
 		String removeCanPattern = "";
 		try {
 			stmt = conn.createStatement();
 			Pattern p = new Pattern();
 			p.fromDatabase(patternName);
 			PatternDecision pd = new PatternDecision();
 			pd.fromDatabase(decisionName);
 			removeCanPattern = "Delete from pattern_decision where parentType = 'Decision' and patternID = " + p.getID() + " and decisionID = " + pd.getID();
 			stmt.execute(removeCanPattern);
 		} catch (SQLException e) {
 			reportError(e, "Error in removeCandidatePattern", removeCanPattern);
 		} finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 
 	/**
 	 * Remove pattern ontology entry
 	 * @param ontName
 	 * @param patternName
 	 * @param direction
 	 */
 	public void removePatternOnt(String ontName, String patternName, String direction){
 		Statement stmt = null;
 		String removePOnt = "";
 		try {
 			stmt = conn.createStatement();
 			Pattern p = new Pattern();
 			p.fromDatabase(patternName);
 			OntEntry oe = new OntEntry();
 			oe.fromDatabase(ontName);
 			removePOnt = "Delete from pattern_ontentries where ontID = " + oe.getID() + " and patternID = " + p.getID() + " and direction = '" + direction + "'";
 			stmt.execute(removePOnt);
 		} catch (SQLException e) {
 			reportError(e, "Error in removePatternOnt", removePOnt);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 	/**
 	 * Save candidate patterns 
 	 * @param decisionName
 	 * @param patternNames
 	 */
 	public void saveCandidatePatterns(String decisionName, Vector<String> patternNames){
 		String insertCanPattern = "";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			PatternDecision pd = new PatternDecision();
 			pd.fromDatabase(decisionName);
 			for(int k=0; k<patternNames.size(); k++){
 				Pattern p = new Pattern();
 				p.fromDatabase(patternNames.get(k));				
 				insertCanPattern = "INSERT INTO pattern_decision (patternID, decisionID, parentType) VALUES (" + p.getID() + "," + pd.getID() + ", 'Decision')";
 				//System.out.println(insertCanPattern);
 				stmt.execute(insertCanPattern);
 			}
 		} catch (SQLException e) {
 			reportError(e, "Error in saveCandidatePatterns", insertCanPattern);
 		} finally {
 			releaseResources(stmt, null);
 		}
 	}
 
 	/**
 	 * Remove all status records for a specific rationale element.
 	 * @param id - the ID of the element
 	 * @param ptype - the type of the element
 	 */
 	public void clearStatus(int id, RationaleElementType ptype) {
 		String deleteCmd = "";
 		//make sure we are of a type that has status!
 		if (!((ptype == RationaleElementType.REQUIREMENT)
 				|| (ptype == RationaleElementType.DECISION) || (ptype == RationaleElementType.ALTERNATIVE))) {
 			return;
 		}
 		Connection conn = getConnection();
 
 		Statement stmt = null;
 
 		//		System.out.println("Clearing out status");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM "
 				+ RationaleDBUtil.escapeTableName("status") + " "
 				+ " where parent = "
 				+ new Integer(id).toString() + " and ptype = '"
 				+ ptype.toString() + "'";
 			//			 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in clearStatus", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 
 	/**
 	 * Clear out the rationale history record for a specific element
 	 * @param id - the element ID
 	 * @param ptype - the element type
 	 */
 	public void clearHistory(int id, RationaleElementType ptype) {
 
 		//make sure we are of a type that has status!
 		if (!((ptype == RationaleElementType.REQUIREMENT)
 				|| (ptype == RationaleElementType.DECISION)
 				|| (ptype == RationaleElementType.ALTERNATIVE) || (ptype == RationaleElementType.QUESTION))) {
 			return;
 		}
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Clearing out history");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM " 
 				+ RationaleDBUtil.escapeTableName("history") + " "
 				+ " where parent = "
 				+ new Integer(id).toString() + " and ptype = '"
 				+ ptype.toString() + "'";
 			///			 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in clear history", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 
 		}
 
 	}
 
 	/**
 	 * Remove an element from the database. This includes clearing out
 	 * the history but doesn't clear out the status.
 	 * @param ele - the rationale element to remove.
 	 */
 	public void deleteRationaleElement(RationaleElement ele) {
 		//first, clear out our status
 		int id = ele.getID();
 
 		/* don't clear out status -- we need this to be handled by explorer
 		 clearStatus(id, ele.getElementType());
 		 */
 		//clear our history
 		clearHistory(id, ele.getElementType());
 
 		//now, delete ourself from the database
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Deleting rationale from the database");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM "
 				+ RationaleDBUtil.escapeTableName(RationaleDB.getTableName(ele.getElementType()))
 				+ " where id = "
 				+ new Integer(id).toString();
 			//		 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in deleteRationaleElement", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 
 	/** 
 	 * Remove a rationale candidate from the database. This can't use the generic
 	 * deleteRationaleElement because the type doesn't signify the table name. 
 	 * @param ele - the rationale element to remove.
 	 */
 	public void deleteCandidateRationaleElement(CandidateRationale ele) {
 
 		int id = ele.getID();
 
 		//now, delete ourself from the database
 		Connection conn = getConnection();
 		String deleteCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Deleting rationale from the database");
 
 		try {
 			stmt = conn.createStatement();
 			deleteCmd = "DELETE FROM Candidates where id = "
 				+ new Integer(id).toString();
 			//		 System.out.println(deleteCmd);
 			stmt.execute(deleteCmd);
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in deleteRationaleElement", deleteCmd);
 		}
 
 		finally {
 			releaseResources(stmt, null);
 		}
 
 	}
 
 	/**
 	 * Find out how many arguments use a particular element (claim, assumption,
 	 * or requirement)
 	 * @param ele - the element of interest
 	 * @return the number of arguments that refer to it
 	 */
 	public int countArgReferences(RationaleElement ele) {
 		int numReferences = 0;
 
 		//get our ID
 		int id = ele.getID();
 		RationaleElementType type = ele.getElementType();
 
 		//get the field name
 		String argField = new String();
 		if (type == RationaleElementType.CLAIM) {
 			argField = "claim";
 		} else if (type == RationaleElementType.ASSUMPTION) {
 			argField = "assumption";
 		} else if (type == RationaleElementType.REQUIREMENT) {
 			argField = "requirement";
 		}
 		//now, find out how often we occur
 		Connection conn = getConnection();
 		ResultSet rs = null;
 
 		String findCmd = "";
 		Statement stmt = null;
 
 		//		System.out.println("Counting arguments");
 
 		try {
 			stmt = conn.createStatement();
 			findCmd = "Select id FROM "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " where " + argField + " = "
 				+ new Integer(id).toString();
 			//***			 System.out.println(findCmd);
 			rs = stmt.executeQuery(findCmd);
 
 			while (rs.next()) {
 				numReferences++;
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in countArgReferences", findCmd);
 		}
 
 		finally {
 			releaseResources(stmt, rs);
 		}
 
 		return numReferences;
 	}
 
 	/**
 	 * Get all the claims that have an importance that overrides the default
 	 * inherited from the Argument Ontology
 	 * @return the overridden claims
 	 */
 	public Vector<Claim> getOverridenClaims() {
 
 		Vector<Claim> claimList = new Vector<Claim>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " 
 				+ RationaleDBUtil.escapeTableName("claims") + " "
 				+ " WHERE importance <> '"
 				+ Importance.DEFAULT.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				String cname = RationaleDBUtil.decode(rs.getString("name"));
 				//experiment
 				Claim newClaim = new Claim();
 				newClaim.fromDatabase(cname);
 				claimList.add(newClaim);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getOverridenClaims", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return claimList;
 	}
 
 	/**
 	 * Get all the arguments that have an importance that overrides the default
 	 * inherited from a claim
 	 * @return the overriden arguments
 	 */
 	public Vector<Argument> getOverridenArguments() {
 
 		Vector<Argument> argList = new Vector<Argument>();
 		String findQuery = "";
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM "
 				+ RationaleDBUtil.escapeTableName("arguments") + " "
 				+ " WHERE importance <> '"
 				+ Importance.DEFAULT.toString() + "'";
 			//***		System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 
 			while (rs.next()) {
 				String cname = RationaleDBUtil.decode(rs.getString("name"));
 				//experiment
 				Argument newArg = new Argument();
 				newArg.fromDatabase(cname);
 				if ((newArg.getCategory() == ArgCategory.CLAIM)
 						|| ((newArg.getCategory() == ArgCategory.REQUIREMENT) && (newArg
 								.getImportance() != Importance.ESSENTIAL))) {
 					argList.add(newArg);
 				}
 
 			}
 
 		} catch (SQLException ex) {
 			// handle any errors
 			reportError(ex, "Error in getOverridenArguments", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 		}
 		return argList;
 	}
 
 	/**
 	 * Release our database resources. This is done over and over and over again so
 	 * we have this nifty utility method to cut down on duplicated code.
 	 * @param stmt - the statement to close
 	 * @param rs - the resource to close
 	 */
 	public static void releaseResources(Statement stmt, ResultSet rs) {
 		// it is a good idea to release
 		// resources in a finally{} block
 		// in reverse-order of their creation
 		// if they are no-longer needed
 
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs = null;
 		}
 
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			stmt = null;
 		}
 	}
 
 	/**
 	 * Release our database resources. This is done over and over and over again so
 	 * we have this nifty utility method to cut down on duplicated code. We
 	 * need the two resource version because sometimes we have to nest queries
 	 * @param stmt - the statement to close
 	 * @param rs - the resource to close
 	 * @param rs2 - the other resource to close
 	 */
 	public static void releaseResources(Statement stmt, ResultSet rs, ResultSet rs2) {
 		// it is a good idea to release
 		// resources in a finally{} block
 		// in reverse-order of their creation
 		// if they are no-longer needed
 
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs = null;
 		}
 
 		if (rs2 != null) {
 			try {
 				rs2.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			rs2 = null;
 		}
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (SQLException sqlEx) { // ignore
 			}
 
 			stmt = null;
 		}
 	}
 
 	/**
 	 * Report an error that occured when trying to do something with the database.
 	 * @param ex - the SQL exception that has occured
 	 * @param location - the part of SEURAT that ran into trouble (typically a method name)
 	 * @param message - the message describing what went wrong
 	 */
 	public static void reportError(SQLException ex, String location,
 			String message) {
 		System.out.println(location);
 		System.out.println(message);
 		System.out.println("SQLException: " + ex.getMessage());
 		System.out.println("SQLState: " + ex.getSQLState());
 		System.out.println("VendorError: " + ex.getErrorCode());
 	}
 
 	/**
 	 * Gets the name of the database
 	 * @return the database name
 	 */
 	public static String getDbName() {
 		return dbName;
 	}
 
 	/**
 	 * Gets the name of the default ontology file
 	 * @return the filename
 	 */
 	public static String getOntName() {
 		return ratDBCreateFile;
 	}
 
 	/**
 	 * Load up a new database from an XML file.
 	 *
 	 */
 	public static void loadRationaleDB() {
 		FileInputStream istream = null;
 
 		try {
 			istream = new FileInputStream(ratFile);
 		} catch (FileNotFoundException e) {
 
 			e.printStackTrace();
 		}
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document ratDoc;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			ratDoc = builder.parse(new File(ratFile));
 			readXMLData(ratDoc);
 			RationaleDB db = RationaleDB.getHandle();
 			//Element rationale = ratDoc.createElement("DR:rationale");
 			//ratDoc.appendChild(rationale);
 
 
 			db.addXMLRequirements();
 			db.addXMLDecisions();
 			//db.addXMLOntology(ratDoc);
 
 
 
 		} catch (SAXException sce) {
 			System.err.println( sce.toString());
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 		} catch (ParserConfigurationException pce) {
 			System.err.println (pce.toString());
 		}
 
 		try {
 			istream.close();
 		} catch (IOException ioe) {
 			System.err.println (ioe.toString());
 			return;
 		}
 		return;
 
 	} //end of reading in new data
 
 	/**
 	 * Read in XML data and put it in the database. How does this relate to the
 	 * previous method???
 	 * @param ratDoc - an XML document
 	 */
 	private static void readXMLData(Document ratDoc)
 	{
 		s = RationaleDB.getHandle();
 		idRefs.clear(); //shouldn't actually need to do
 
 		Element ratTop = ratDoc.getDocumentElement();
 		//this should be our parent Rationale element
 
 		Element ratNext = (Element) ratTop.getFirstChild();
 		while (ratNext != null)
 		{
 			String nextName;
 			nextName = ratNext.getNodeName();
 			//			System.out.println(nextName);
 			//here we check the type, then process
 			if (nextName.compareTo("DR:argOntology") == 0)
 			{
 				//				System.out.println("found the ontology");
 				//need to get the root ontology entry
 				Element topOnt = (Element) ratNext.getFirstChild();
 				//process argument ontology
 				OntEntry topE = new OntEntry();
 				topE.fromXML(topOnt, null); //null parent
 				s.argumentOntology = topE;
 			}
 			else if (nextName.compareTo("DR:requirement") == 0)
 			{
 				Requirement nextReq = new Requirement();
 				//				s.addRequirement(nextReq);
 				nextReq.fromXML(ratNext);
 				s.addRequirement(nextReq);
 			}
 			else if (nextName.compareTo("reqref") == 0)
 			{
 				//this is unusual but can happen!
 				//nothing to do here. it is already in the database and it's a top
 				//level item
 			}
 			else if (nextName.compareTo("DR:decisionproblem") == 0)
 			{
 				//process decision
 				Decision nextDec = new Decision();
 				nextDec.fromXML(ratNext);
 				s.addDecision(nextDec);
 			}
 			else if (nextName.compareTo("decref") == 0)
 			{
 				//again, nothing to do here, it is in the DB already
 			}
 			else if (nextName.compareTo("DR:backgroundKn") == 0)
 			{
 				//process background knowledge
 			}
 			ratNext = (Element) ratNext.getNextSibling();
 		}
 
 	}
 
 	/**
 	 * Exports the argument ontology to XML by calling the appropriate
 	 * utility function and passing it the name of the file we are exporting to.
 	 * 
 	 * @return a boolean indicating success or failure which can be used to provide
 	 * appropriate feedback to the user
 	 */
 	public boolean exportOntology(String path) {
 		return RationaleDBUtil.exportArgumentOntology(path);
 	}
 
 	/**
 	 * Exports the whole db to an XML file.
 	 * (Currently, we're only exporting pattern library and the ontology)
 	 * This is done by calling the utlity function in RationaleDBUtil.
 	 * @param path
 	 * @return true if it's successful. false otherwise.
 	 */
 	public boolean exportXML(String path){
 		return RationaleDBUtil.exportToXML(path);
 	}
 
 	/**
 	 * Add a requirement to our requirement vector
 	 * @param newReq - the requirement
 	 */
 	public void addRequirement(Requirement newReq)
 	{
 		requirements.addElement(newReq);
 	}
 
 	/**
 	 * Add an argument to our argument vector
 	 * @param newArg - the argument
 	 */
 	public void addArgument(Argument newArg)
 	{
 		arguments.addElement(newArg);
 	}
 
 	/**
 	 * Add an assumption to our assumption vector
 	 * @param newAssump - the new assumption
 	 */
 	public void addAssumption(Assumption newAssump)
 	{
 		assumptions.addElement(newAssump);
 	}
 
 	/**
 	 * Add a claim to our claim vector
 	 * @param newClaim - the new claim
 	 */
 	public void addClaim(Claim newClaim)
 	{
 		claims.addElement(newClaim);
 	}
 
 	/**
 	 * Add a new question to our question vector
 	 * @param newQuestion - the new question
 	 */
 	public void addQuestion(Question newQuestion)
 	{
 		questions.addElement(newQuestion);
 	}
 
 	/**
 	 * Add a new decision to our decision vector
 	 * @param newDecision - the new decision
 	 */
 	public void addDecision(Decision newDecision)
 	{
 		decisions.addElement(newDecision);
 	}
 
 	/**
 	 * Add a new alternative to our alternative vector
 	 * @param alt - the new alternative
 	 */
 	public void addAlternative(Alternative alt)
 	{
 		alternatives.addElement(alt);
 	}
 
 	/**
 	 * Add a new tradeoff to our tradeoff vector
 	 * @param newTradeoff - the new tradeoff
 	 */
 	public void addTradeoff(Tradeoff newTradeoff)
 	{
 		tradeoffs.addElement(newTradeoff);
 	}
 
 	/**
 	 * Add a new cooccurence to our co-occurence vector
 	 * @param newCooccurences - our new element
 	 */
 	public void addCo_Occurence(Tradeoff newCooccurences)
 	{
 		co_occurences.addElement(newCooccurences);
 	}
 
 
 	/**
 	 * Given an XML reference ID, get the element name
 	 * @param i - the id
 	 * @return the name
 	 */
 	public String getRef(int i)
 	{
 		Integer inti = new Integer(i);
 		return (String) idRefs.get(inti);
 	}
 
 	/**
 	 * Used to get the XML reference string of a pattern
 	 * @param p
 	 * @return
 	 */
 	public String getRef(Pattern p){
 		Iterator keyIterator = xmlRefs[PATTERN_XML_INDEX].keySet().iterator();
 		while (keyIterator.hasNext()){
 			String current = (String) keyIterator.next();
 			if (p.equals(xmlRefs[PATTERN_XML_INDEX].get(current))) return current;
 		}
 		return null;
 	}
 
 	/**
 	 * Used to get the XML reference string of a pattern deicison
 	 * @param d
 	 * @return
 	 */
 	public String getRef(PatternDecision d){
 		Iterator keyIterator = xmlRefs[PATTERNDECISION_XML_INDEX].keySet().iterator();
 		while (keyIterator.hasNext()){
 			String current = (String) keyIterator.next();
 			if (d.equals(xmlRefs[PATTERNDECISION_XML_INDEX].get(current))) return current;
 		}
 		return null;
 	}
 
 	/**
 	 * Used to get the XML reference string of a problem category
 	 * @param id
 	 * @param problem
 	 * @param type
 	 * @return
 	 */
 	public String getRef(String id, String problem, String type){
 		Iterator keyIterator = xmlRefs[PATTERNPROBLEMCATEGORY_XML_INDEX].keySet().iterator();
 		while (keyIterator.hasNext()){
 			String current = (String) keyIterator.next();
 			String[] curValue = (String[]) xmlRefs[PATTERNPROBLEMCATEGORY_XML_INDEX].get(current);
 			if (id.equals(curValue[0])){
 				return current;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * this is the version of getRef that returns a rationale element
 	 * this is used when reading in rationale from the XML, in this
 	 * case, storing ids is not sufficient
 	 */
 	public RationaleElement getRef(String sr)
 	{
 		RationaleElement t;
 		t = (RationaleElement) idRefs.get(sr);
 		if (t == null)
 		{
 			System.out.println("bad value from getRef");
 		}
 		return (RationaleElement) idRefs.get(sr);
 	}
 
 	/**
 	 * Add a reference
 	 * @param i - the ref ID
 	 * @return the string version of the reference
 	 */
 	public String addRef(int i)
 	{
 		Integer tempi;
 		tempi = new Integer(i);
 		String newRef = "r" + tempi.toString();
 		idRefs.put(tempi, newRef);
 		return newRef;
 	}
 
 	public String addPatternRef(Pattern p){
 		Integer tempi = new Integer(p.getID());
 		String ref = "p" + tempi.toString();
 		xmlRefs[PATTERN_XML_INDEX].put(ref, p);
 		return ref;
 	}
 
 	public String addPatternDecisionRef(PatternDecision d){
 		Integer tempi = new Integer(d.getID());
 		String ref = "pd" + tempi.toString();
 		xmlRefs[PATTERNDECISION_XML_INDEX].put(ref, d);
 		return ref;
 	}
 
 	public String addPatternCategoryRef(String id, String category, String type){
 		String ref = "c" + id;
 		String[] value = {id, category, type};
 		xmlRefs[PATTERNPROBLEMCATEGORY_XML_INDEX].put(ref, value);
 		return ref;
 	}
 
 	/**
 	 * This is the version of addRef that puts a RationaleElement into the hash
 	 * table. This is used when reading in the XML
 	 */
 	public void addRef(String sr, RationaleElement re)
 	{
 		//ref is already in the correct form, no change needed
 		idRefs.put(sr, re);
 	}
 
 	/**
 	 * Given a pattern object that was created from XML, add it to the database.
 	 * Note that at this point, the database may violate referential integrity constraints.
 	 * @param pattern
 	 */
 	public void addPatternFromXML(Pattern pattern){
 
 		try{
 			//First, insert the pattern to the database. Easy...
 			PreparedStatement ps = conn.prepareStatement("INSERT INTO PATTERNS values (" +
 			"?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 			ps.setInt(1, pattern.getID());
 			ps.setString(2, pattern.getName());
 			ps.setString(3, pattern.getType().toString());
 			ps.setBytes(4, pattern.getDescription().getBytes());
 			ps.setBytes(5, pattern.getProblem().getBytes());
 			ps.setBytes(6, pattern.getContext().getBytes());
 			ps.setBytes(7, pattern.getSolution().getBytes());
 			ps.setBytes(8, pattern.getImplementation().getBytes());
 			ps.setBytes(9, pattern.getExample().getBytes());
 			ps.setString(10, pattern.getUrl());
 			ps.executeUpdate();
 			ps.close();
 
 			//Next, create entry to connect with patternproblemcategory
 			Statement stmt = conn.createStatement();
 			String expr = "INSERT INTO PATTERN_PROBLEMCATEGORY (patternID, problemcategoryID)" + 
 			" values (" + pattern.getID() + ", " + pattern.getProblemCategory() + ")";
 			stmt.execute(expr);
 
 			//Then, create entry to connect with positive ontology
 			Iterator<Integer> pos = pattern.iteratorPosOntID();
 			while (pos.hasNext()){
 				int ontID = pos.next();
 				expr = "INSERT INTO PATTERN_ONTENTRIES values (" + 
 				pattern.getID() + ", " + ontID + ", 'IS')";
 				stmt.execute(expr);
 			}
 			//Neg ontology
 			Iterator<Integer> neg = pattern.iteratorNegOntID();
 			while (neg.hasNext()){
 				int ontID = neg.next();
 				expr = "INSERT INTO PATTERN_ONTENTRIES values (" + 
 				pattern.getID() + ", " + ontID + ", 'NOT')";
 				stmt.execute(expr);
 			}
 			stmt.close();
 
 			//connection with sub-decision must be done in utility after decision has been created...
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Given a pattern decision imported from xml, add it to the database.
 	 * Note that the parent value must be updated later.
 	 * @param pd
 	 */
 	public void addPatternDecisionFromXML(PatternDecision pd){
 		try{
 			Statement stmt = conn.createStatement();
 			String expr = "INSERT INTO PATTERNDECISIONS (id, name, description, type, status, phase, ptype, subdecreq)"+
 			" values (" + pd.getID() + ", '" + pd.getName() + "', '" + pd.getDescription() + "', '" + 
 			pd.getType().toString() + "', '" + pd.getStatus().toString() + "', '" + pd.getPhase().toString() +
 			"', 'Pattern', 'No')";
 			stmt.execute(expr);
 
 			//Now, add the associated candidate patterns
 			Iterator<Integer> candidatePID = pd.iteratorSubPatterns();
 			while (candidatePID.hasNext()){
 				int cpid = candidatePID.next();
 				expr = "INSERT INTO PATTERN_DECISION values (" + cpid + ", " + pd.getID() + ", 'Decision')";
 				stmt.execute(expr);
 			}
 			stmt.close();
 		} catch (SQLIntegrityConstraintViolationException e){
 			System.out.println("WARNING: Duplicated Entry. Skipping: " + pd.getName());
 		} catch (SQLException e){
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Assume pattern and patterndecisions have been imported. Utility can call this method to associate
 	 * between pattern and child pattern-decisions.
 	 * @param patternID
 	 * @param pdID
 	 */
 	public void assocPatternAndDecisionFromXML(int patternID, int pdID){
 		try{
 			Statement stmt = conn.createStatement();
 			String expr = "UPDATE PATTERNDECISIONS set parent = " + patternID + " WHERE id = " + pdID;
 			stmt.execute(expr);
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method is used for XML import of pattern problem category.
 	 * Given the database id, category, and type of the problem category, add
 	 * it to the database.
 	 * @param id The parsed id of the reference id in XML
 	 * @param category
 	 * @param type
 	 */
 	public void addProblemCategory(int id, String category, String type){
 		try{
 			Statement stmt = conn.createStatement();
 			String expr = "INSERT INTO PATTERNPROBLEMCATEGORIES values (" +
 			new Integer(id).toString() + ", '" + category + "', '" + type + "')";
 			stmt.execute(expr);
 			stmt.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method returns all database info about problem category.
 	 * Used for XML export
 	 * String[0] represents the id of each problem category
 	 * String[1] represents the problem category names
 	 * String[2] represents the type of each problem category
 	 * @return Vector containing String[3]
 	 */
 	public Vector<String[]> getProblemCategoryData(){
 		Vector<String[]> toReturn = new Vector<String[]>();
 		try{
 			Statement stmt = conn.createStatement();
 			ResultSet rs = null;
 
 			String query = "SELECT *" +
 			"FROM PATTERNPROBLEMCATEGORIES";
 
 			rs = stmt.executeQuery(query);
 
 			while (rs.next()){
 				String[] content = new String[3];
 				content[0] = new Integer(rs.getInt(1)).toString();
 				content[1] = rs.getString(2);
 				content[2] = rs.getString(3);
 				toReturn.add(content);
 			}
 			stmt.close();
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 		return toReturn;
 	}
 
 	/**
 	 * This method returns all database info about patterns.
 	 * Used for XML export.
 	 * @return
 	 */
 	public Vector<Pattern> getPatternData(){
 		Vector<Pattern> toReturn = new Vector<Pattern>();
 		try{
 			Statement stmt = conn.createStatement();
 			ResultSet rs = null;
 			String query = "SELECT id FROM patterns";
 			rs = stmt.executeQuery(query);
 
 			while (rs.next()){
 				Integer id = rs.getInt(1);
 				Pattern pattern = new Pattern();
 				pattern.fromDatabase(id);
 				toReturn.add(pattern);
 			}
 			stmt.close();
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 
 		return toReturn;
 	}
 
 	/**
 	 * What appears to be happening with the XML is that the rationale elements
 	 * are read from the file and stored into vectors. Then, this method is called
 	 * to put the elements actually INTO the database.
 	 * @deprecated not using it right now
 	 */
 	//	private void addXMLRequirements(Document ratDoc)
 	private void addXMLRequirements()
 	{
 		Enumeration<Requirement> reqs = requirements.elements();
 		while (reqs.hasMoreElements())
 		{
 			Requirement req = (Requirement) reqs.nextElement();
 			req.toDatabase(0, RationaleElementType.NONE);
 		}
 	}
 
 
 	/**
 	 * This method is called when adding XML decisions
 	 * @deprecated Not using it right now
 	 */
 	private void addXMLDecisions()
 	{
 
 		Enumeration<Decision> decs = decisions.elements();
 		while (decs.hasMoreElements())
 		{
 			Decision dec = (Decision) decs.nextElement();
 			dec.toDatabase(0, RationaleElementType.NONE);
 			//dec.toDatabase(0, RationaleElementType.NONE);
 		}
 		System.out.println("done adding decisions");
 	}
 
 
 	/**
 	 * This method contains the hard-coded insertion of patterns.
 	 * Used when XML is not there.
 	 * @param ps
 	 */
 	private void importPatterns(PreparedStatement ps){
 		try{
 			ps.setString(1, "Three-layer");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The system is organized into three primary layers: Presentation, Domain, and Data Source.")).getBytes());
 			ps.setBytes(4, (new String("In a system in which abstract domains must be implemented in terms of more concrete (less abstract) domains, we need a simple organizational pattern. Additionally, in many systems we need portability of the application to other platforms, or we want to provide an abstract platform or execution environment for which applications may be easily adapted.")).getBytes());
 			ps.setBytes(5, (new String("Development of a large business application, where many users share common data and operations on them. In addition, there might be legacy systems which have to be integrated in the new application.")).getBytes());
 			ps.setBytes(6, (new String("'Base your layered architecture on three layers: Presentation, Domain, and Data Source.  Presentation layer is about how to handle the interaction between the user and the software. This can be as simple as a command-line or text-based menu system, but these days it more likely to be a rich-client graphics UI or an HTML-based browser UI. Data source layer is about communicating with other systems that carry out tasks on behalf of the application. These can be transaction monitors, other applications, messaging systems, and so forth. Domain logic, also referred to as business logic. This is the work that this application needs to do for the domain youe working with. It involves calculations based on inputs and stored data, validation of any data that comes in from the presentation, and figuring out exactly what data source logic to dispatch, depending on commands received from the presentation.")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("The three-layer architecture offers significant advantages even for relatively small applications. For instance, the single-user PC application First Account from the Norwegiancompany Economica encapsulates most of the accounting and invoicing functionality in adynamic link library (DLL), which in turn works against a local, flat-file database. This separationenabled the developers with knowledge of accounting and object-oriented design to dedicatethemselves to the central functionality, and user interface designers with little or no knowledge ofprogramming to fully control their part of the application.")).getBytes());
 			ps.setString(9, "http://msdn.microsoft.com/en-us/library/ms978689.aspx");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Layers");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Layers architectural pattern helps to structure applications that can be decomposed into groups of subtasks in which each group of subtasks is at a particular level of abstraction.")).getBytes());
 			ps.setBytes(4, (new String("In a system in which abstract domains must be implemented in terms of more concrete (less abstract) domains, we need a simple organizational pattern. Additionally, in many systems we need portability of the application to other platforms, or we want to provide an abstract platform or execution environment for which applications may be easily adapted.")).getBytes());
 			ps.setBytes(5, (new String("You are designing a Layered Application. You want to expose some of the core functionality of your application as services that other applications can consume, and you want your application to consume services exposed by other applications.")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Layers/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Pipes and Filters");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Pipes and Filters architectural pattern provides a structure for systems that process a stream of data. Each processing step is encapsulated in a filter component. Data is passed through pipes between adjacent filters. Recombining filters allows you to build families of related systems.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://msdn.microsoft.com/en-us/library/ms978599.aspx");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Blackboard");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Blackboard architectural pattern is useful for problems for which no deterministic solution strategies are known. In Blackboard several specialized subsystem assemble their knowledge to build a possibly partial or approximate solution.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20Blackboard/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Model-View-Controller");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The MVC architectural pattern divides an interactive application into three components. The model contains the core functionality and data. Views display information to the user. Controllers handle user input. Views and Controllers together comprise the user interface. A change-propagation mechanism ensures consistency between the user interface and the model.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://msdn.microsoft.com/en-us/library/ms978748.aspx");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Broker");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Broker architectural pattern can be used to structure distributed software systems with decoupled components that interact by remote service invocations. A broker component is responsible for coordinating communication, such as forwarding requests, as well as for transmitting results and exceptions.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://msdn.microsoft.com/en-us/library/ms978706.aspx");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Presentation-Abstraction-Control");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Presentation-Abstraction-Control architectural pattern (PAC) defines a structure for interactive software systems in the form of a hierarchy of cooperating agents. Every agent is responsible for a specific aspect of the applications functionality and consists of three components: presentation, abstraction, and control. This subdivision separates the human-computer interaction aspects of the agent from its functional core and its communication with other agents.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Presentation%20Abstra/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Microkernel");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Microkernel architectural pattern applies to software systems that must be able to adapt to changing system requirements. It separates a minimal functional core from extended functionality and customer-specific parts. The microkernel also serves as a socket for plugging in these extensions and coordinating their collaboration.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20MicroKernel/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Reflection");
 			ps.setString(2, "Architecture");
 			ps.setBytes(3, (new String("The Reflection architectural pattern provides a mechanism for changing structure and behavior of software systems dynamically. It supports the modification of fundamental aspects, such as type structures and function call mechanisms. In this pattern, an application is split into two parts. A meta level provides information about selected system properties and makes the software self-aware. A base level includes the application logic. Its implementation builds on the meta level. Changes to information kept in the meta level affect subsequent base-level behavior.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Reflection/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Whole-Part");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Whole-Part design pattern helps with the aggregation of components that together form a semantic unit. An aggregate component, the Whole, encapsulates its constituent components, the Parts, organizes their collaboration, and provides a common interface to its functionality. Direct access to the Parts is not possible.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20Whole%20Part/index.html");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Master-Slave");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Master-Slave design pattern supports fault tolerance, parallel computation and computational accuracy. A master component distributes work to identical slave components and computes a final result from the results these slaves return.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20Master%20Slave/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Proxy");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Provide a surrogate or placeholder for another object to control access to it.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20Broker/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Command Processor");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Command Processor design pattern separates the request for a service from its execution. A command processor component manages requests as separate objects, schedules their execution, and provides additional services such as the storing of request objects for later undo.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://www.vico.org/pages/PatronsDisseny/Pattern%20Command%20Processor/index.html");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "View Handler");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The View Handler design pattern helps to manage all views that a software system provides. A view handler component allows clients to open, manipulate and dispose of views. It also coordinates dependencies between view and organizes their update.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20View%20Handler/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Forward-Receiver");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Forwarder-Receiver design pattern provides transparent interprocess communication for software systems with a peer-to-peer interaction model. It introduces forwarders and receivers to decouple peers from the underlying communication mechanisms.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Forward-Receiver/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Client-Dispatcher-Server");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Client-Dispatcher-Server design pattern introduces an intermediate layer between clients and servers, the dispatcher component. It provides location transparency by means of a name service, and hides the details of the establishment of the communication connection between clients and servers.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20ClientDispatcherServer/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Publisher-Subscriber");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("The Publisher-Subscriber design pattern helps to keep the state of cooperating components synchronized. To achieve this it enables one-way propagation of changes: one publisher notifies any number of subscribers about changes to its state.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Publisher%20Subscriber/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Strategy");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Define a family of algorithms, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Strategy/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Factory");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Define an interface for creating an object, but let subclasses decide which class to instantiate. Factory Method lets a class defer instantiation to subclasses.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Factory%20Method/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Decorator");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Decorator/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Composite");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Compose objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Composite/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Template Method");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Define the skeleton of an algorithm in an operation, deferring some steps to subclasses. Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithms structure.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Template%20Method/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Command");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Encapsulate a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Command/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Chain of Responsibility");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Avoid coupling the sender of a request to its receiver by giving more than one object a chance to handle the request. Chain the receiving objects and pass the request along the chain until an object handles it.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Chain%20of%20Responsability/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Facade");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Provide a unified interface to a set of interfaces in a subsystem. Fa?ade defines a higher-level interface that makes the subsystem easier to user.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Facade/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Transaction Script");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Organizes business logic by procedures where each procedure handles a single request from the presentation.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Domain Model");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("An object model of the domain that incorporates both behavior and data.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Table Module");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("A single instance that handles the business logic for all rows in a database table or view.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Gateway");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("An object that encapsulates access to an external system or resource.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Row Data Gateway");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("An object that acts as a Gateway to a single record in a data source. There is one instance per row.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Active Record");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("An object that wraps a row in a database table or view, encapsulates the database access, and adds domain logic on that data.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("The essence of an Active Record is a Domain Model in which the classes match very closely the record structure of an underlying database. Each Active Record is responsible for saving and loading to the database and also for any domain logic that acts on the data. This may be all the domain logic in the application, or you may find that some domain logic is held in Transaction Scripts with common and data-oriented code in the Active Record.")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Table Data Gateway");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("An object that acts as a Gateway to a database table. One instance handles all the rows in the table.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("")).getBytes());
 			ps.setBytes(7, (new String("A Row Data Gateway acts as an object that exactly mimics a single record, such as one database row. In it each column in the database becomes one field. The Row Data Gateway will usually do any type conversion from the data source types to the in-memory types, but this conversion is pretty simple. This pattern holds the data about a row so that a client can then access the Row")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Application Controller");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("A centralized point for handling screen navigation and the flow of an application.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Transform View");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("A view that processes domain data element by element and transforms it into HTML.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Template View");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Renders information into HTML by embedding markers in an HTML page.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Two Step View");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Turns domain data into HTML in two steps: first by forming some kind of logical page, then rendering the logical page into HTML.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "EAA Book Online");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Bridge");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("Decouple an abstraction from its implementation so that the two can vary independently.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Bridge/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Data Mappter");
 			ps.setString(2, "Design");
 			ps.setBytes(3, (new String("A layer of Mappers that moves data between objects and a database while keeping them independent of each other and the mapper itself.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("Objects and relational databases have different mechanisms for structuring data. Many parts of an object, such as collections and inheritance, arent present in relational databases. When you build an object model with a lot of business logic its valuable to use these mechanisms to better organize the data and the behavior that goes with it.  Doing so leads to variant schemas; that is, the object schema and the relational schema dont match up.")).getBytes());
 			ps.setBytes(6, (new String("The Data Mapper is a layer of software that separates the in-memory objects from the database. Its responsibility is to transfer data between the two and also to isolate them from each other. With Data Mapper the in-memory objects neednt know even that theres a database present; they need no SQL interface code, and certainly no knowledge of the database schema. (The database schema is always ignorant of the objects that use it.) Since its a form of Mapper, Data Mapper itself is even unknown to the domain layer.")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Bridge/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Counted Pointer");
 			ps.setString(2, "Idiom");
 			ps.setBytes(3, (new String("This idiom makes memory management of dynamically-allocated shared objects in C++ easier. It introduces a reference counter to a body class that is updated by handle objects. Clients access body class objects only through handles via the overloaded operator ->()..")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "Book Reference");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Singleton");
 			ps.setString(2, "Idiom");
 			ps.setBytes(3, (new String("Ensure a class only has one instance, and provide a global point of access to it.")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "http://vico.org/pages/PatronsDisseny/Pattern%20Singleton/");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 
 			ps.setString(1, "Indented Control Flow");
 			ps.setString(2, "Idiom");
 			ps.setBytes(3, (new String("")).getBytes());
 			ps.setBytes(4, (new String("")).getBytes());
 			ps.setBytes(5, (new String("")).getBytes());
 			ps.setBytes(6, (new String("'")).getBytes());
 			ps.setBytes(7, (new String("")).getBytes());
 			ps.setBytes(8, (new String("")).getBytes());
 			ps.setString(9, "Book Reference");
 			ps.setInt(10, RationaleDBCreate.pushCurrentID());
 			ps.executeUpdate();
 		}catch(Exception ex){
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getToplevelMappings() {
 		Vector<XFeatureMapping> xfeaturenodes = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where parent < 1";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				int mapID = rs.getInt("id");
 				XFeatureMapping xNode = new XFeatureMapping();
 				xNode.fromDatabase(mapID);
 				xfeaturenodes.add(xNode);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting top level xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return xfeaturenodes;
 	}
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getDependentMappings(int parent) {
 		Vector<XFeatureMapping> dependent = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where parent = " + parent;
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				int mapID = rs.getInt("id");
 				XFeatureMapping relArg = new XFeatureMapping();
 				relArg.fromDatabase(mapID);
 				dependent.add(relArg);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting dependent xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 	/**
 	 * Get all top-level XFeature mapping nodes
 	 * @return the top level elements
 	 */
 	public static Vector<XFeatureMapping> getDependentMappings(String xnodeName) {
 		Vector<XFeatureMapping> dependent = new Vector<XFeatureMapping>();
 		Statement stmt = null;
 		ResultSet rs = null;
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT * from " 
 				+ RationaleDBUtil.escapeTableName("xfeaturemapping") 
 				+ " where nodename = '" + xnodeName
 				+ "' and rattype = 'Alternative'";
 			//***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next()) {
 				int mapID = rs.getInt("id");
 				XFeatureMapping relArg = new XFeatureMapping();
 				relArg.fromDatabase(mapID);
 				dependent.add(relArg);
 			}
 
 		} catch (SQLException ex) {
 			reportError(ex, "Error in getting dependent xfeature elements", findQuery);
 		} finally {
 			releaseResources(stmt, rs);
 
 		}
 		return dependent;
 	}
 
 	/**
 	 * It seems the XML Import/Export has broken the ID auto_increment. I need this for every
 	 * insertion of all entities.
 	 * This method returns the next available ID after the max of ID's given dbName.
 	 * @param dbName The database name to find the max ID.
 	 */
 	public static int findAvailableID(String dbName){
 		Statement stmt = null;
 		ResultSet r = null;
 		String query = "SELECT max(id) FROM " + dbName;
 		try{
 			stmt = conn.createStatement();
 			r = stmt.executeQuery(query);
 			if (r.next()){
 				return r.getInt(1) + 1;
 			}
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 		return 1;
 	}
 
 }
