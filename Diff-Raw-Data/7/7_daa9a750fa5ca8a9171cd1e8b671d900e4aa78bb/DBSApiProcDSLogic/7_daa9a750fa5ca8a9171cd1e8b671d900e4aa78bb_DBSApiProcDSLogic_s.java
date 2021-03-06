 /**
 $Revision: 1.13 $"
 $Id: DBSApiProcDSLogic.java,v 1.13 2007/01/18 18:07:10 afaq Exp $"
  *
  */
 
 package dbs.api;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.io.Writer;
 import java.util.Hashtable;
 import java.util.Vector;
 import dbs.sql.DBSSql;
 import dbs.util.DBSUtil;
 import dbs.DBSException;
 
 /**
 * A class that has the core business logic of all the Processed dataset APIs.  The signature for the API is internal to DBS and is not exposed to the clients. There is another class <code>dbs.api.DBSApi</code> that has an interface for the clients. All these low level APIs are invoked from <code>dbs.api.DBSApi</code>. This class inherits from DBSApiLogic class.
 * @author sekhri
 */
 public class DBSApiProcDSLogic extends DBSApiLogic {
 		
 	/**
 	* Constructs a DBSApiLogic object that can be used to invoke several APIs.
 	*/
 
 	DBSApiPersonLogic personApi = null;
 	DBSApiData data = null;
 	public DBSApiProcDSLogic(DBSApiData data) {
 		super(data);
 		this.data = data;
 		personApi = new DBSApiPersonLogic(data);
 	}
 
 	/**
 	 * Lists all the processed datasets from the database in a xml format. This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listProcessedDatasets</code> method. A sample XML that is written to the output stream is like <br>
 	 * <code><"processed-dataset id='1' primary_datatset_name='TestPrimary' processed_datatset_name='TestProcessed' creation_date='2006-11-29 16:39:48.0' last_modification_date='2006-11-29 16:39:48.0' physics_group_name='BPositive' physics_group_convener='ANZARDN' created_by='ANZARDN' last_modified_by='ANZARDN'"> <br> 
 	 * <"data_tier name='SIM'"/> <"data_tier name='HIT'"/> <"algorithm app_version='TestVersio' app_family_name='AppFamily011' app_executable_name='TestExe011' ps_name='MyFirstParam01'"/> <"algorithm app_version='TestVersion0111' app_family_name='AppFamily01' app_executable_name='TestExe01' ps_name='MyFirstParam01'"/> <"/processed-dataset"> </code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param patternPrim a parameter passed in from the client that can contain wild card characters for primary dataset name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternDT a parameter passed in from the client that can contain wild card characters for data tier name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternProc a parameter passed in from the client that can contain wild card characters for processed dataset name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternVer a parameter passed in from the client that can contain wild card characters for application version. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternFam a parameter passed in from the client that can contain wild card characters for application family. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternExe a parameter passed in from the client that can contain wild card characters for application executable name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @param patternPS a parameter passed in from the client that can contain wild card characters for parameter set name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied patterns are invalid or the database connection is unavailable.
 	 */
 	public void listProcessedDatasets(Connection conn, Writer out, String patternPrim, String patternDT, String patternProc, String patternVer, String patternFam, String patternExe, String patternPS) throws Exception {
 		String prevDS = "";
 		/*String prevTier = "";
 		String prevExe = "";
 		String prevFam = "";
 		String prevVer = "";
 		String prevPS = "";*/
 		// When data is returned from the database, a bunch of tiers and application are returned in random order, so we need
 		// to store all of them in a vector so that while writing xml, previously written data tier does not get written again.
 		Vector dtVec = null; 
 		Vector algoVec = null; 
 		
 		//The xml genrated is nested and this flag is needed to know if first time a tag needs to be written
 		boolean first = true; 
 
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listProcessedDatasets(conn, 
 					getPattern(patternPrim, "primary_datatset_name_pattern"), 
 					getPattern(patternDT, "data_tier_name_pattern"), 
 					getPattern(patternProc, "processed_datatset_name_pattern"), 
 					getPattern(patternVer, "app_version"), 
 					getPattern(patternFam, "app_family_name"), 
 					getPattern(patternExe, "app_executable_name"), 
 					getPattern(patternPS, "ps_hash"));
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				//String path = "/" + get(rs, "primary_name") + "/" + get(rs, "data_tier") + "/" + get(rs, "processed_name");
 				String procDSID = get(rs, "ID");
 				String tier = get(rs, "DATA_TIER");
 				String fam = get(rs, "APP_FAMILY_NAME");
 				String exe = get(rs, "APP_EXECUTABLE_NAME");
 				String ver = get(rs, "APP_VERSION");
 				String pset = get(rs, "PS_HASH");
 	
 				if( !prevDS.equals(procDSID) && ! first) {
 					out.write(((String) "</processed-dataset>\n")); 
 				}
 				if( !prevDS.equals(procDSID) || first) {
 					out.write(((String) "<processed-dataset id='" + get(rs, "ID") + 
 							//"' path='" +  get(rs, "PATH") +
 							"' primary_datatset_name='" +  get(rs, "PRIMARY_DATATSET_NAME") +
 							"' processed_datatset_name='" +  get(rs, "PROCESSED_DATATSET_NAME") +
 							"' creation_date='" + getTime(rs, "CREATION_DATE") +
 							"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 							"' physics_group_name='" + get(rs, "PHYSICS_GROUP_NAME") +
 							"' physics_group_convener='" + get(rs, "PHYSICS_GROUP_CONVENER") +
 							"' created_by='" + get(rs, "CREATED_BY") +
 							"' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 							"'>\n"));
 					first = false;
 					prevDS = procDSID;
 					dtVec = new Vector();// Or dtVec.removeAllElements();
 					algoVec = new Vector();// Or algoVec.removeAllElements();
 				}
 				//if( (!prevTier.equals(tier) || first) && !dtVec.contains(tier) ) {
 				if( !dtVec.contains(tier) && !isNull(tier) ) {
 					out.write(((String) "\t<data_tier name='" + tier + "'/>\n"));
 					dtVec.add(tier);
 					//prevTier = tier;
 				}
 				//if( !prevExe.equals(exe) || !prevFam.equals(fam) || !prevVer.equals(ver) || !prevPS.equals(pset) || first) {
 				String uniqueAlgo = ver + exe + fam + pset;
				if(!algoVec.contains(uniqueAlgo)) {
 					out.write(((String) "\t<algorithm app_version='" + ver +
 	   							"' app_family_name='" + fam + 
 								"' app_executable_name='" + exe + 
 								//"' ps_name='" + pset + 
 								"' ps_hash='" + pset +
 								//"' ps_version='" + get(rs, "PS_VERSION") +
 								//"' ps_type='" + get(rs, "PS_TYPE") +
 								//"' ps_annotation='" + get(rs, "PS_ANNOTATION") +
 								//"' ps_content='" + get(rs, "PS_CONTENT") +
 								"'/>\n"));
 					algoVec.add(uniqueAlgo);
 					/*prevExe = exe;
 					prevFam = fam;
 					prevVer = ver;
 					prevPS = pset;*/
 				}
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
                 if (!first) out.write(((String) "</processed-dataset>\n")); 
 	}
 
 	/**
 	 * Lists all the parents of the given processed datatset.  This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listPDParents</code> method. It writes a lsit of processed dataset paths on the output stream.  A sample XML that is written to the output stream is like <br>
 	 * <code> <"processed-dataset-parent id='43' path='/primary/HIT/processed' creation_date='2006-12-11 14:02:18.0' last_modification_date='2006-12-11 14:02:18.0' physics_group_name='AnyName' physics_group_convener='ANZARDN' created_by='ANZARDN' last_modified_by='ANZARDN'"/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param path a dataset path in the format of /primary/tier/processed. This path is used to find the existing processed dataset id.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied path is invalid, the database connection is unavailable or processed dataset is not found.
 	 */
 	 public void listDatasetParents(Connection conn, Writer out, String path) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listDatasetParents(conn, getProcessedDSID(conn, path));
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<processed-dataset-parent id='" + get(rs, "ID") + 
 						"' path='" +  get(rs, "PATH") +
 						"' creation_date='" + getTime(rs, "CREATION_DATE") +
 						"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 						"' physics_group_name='" + get(rs, "PHYSICS_GROUP_NAME") +
 						"' physics_group_convener='" + get(rs, "PHYSICS_GROUP_CONVENER") +
 						"' created_by='" + get(rs, "CREATED_BY") +
 						"' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 						"'/>\n"));
 				}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	 }
 
 
 	/**
 	 * Lists all the runs within a processed dataset from the database in a xml format. This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listRuns</code> method. First it fetches the processed dataset ID from the database by calling a private <code>getProcessedDSID<code> method using the path provided in the parameter. If the processed dataset id does not esists then it throws an exception. A sample XML that is written to the output stream is like <br>
 	 * <code> <"run id='1' run_number='9999' number_of_events='54' number_of_lumi_sections='12' total_luminosity='2' store_number='32' start_of_run='nov' end_of_run='dec' creation_date='2006-12-06 16:12:12.0' last_modification_date='2006-12-06 16:12:12.0' created_by='ANZARDN' last_modified_by='ANZARDN'"/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param path a dataset path in the format of /primary/tier/processed. If this path is not provided or the dataset id could not be found then an exception is thrown.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied path is invalid, the database connection is unavailable or processed dataset is not found.
 	 */
 	public void listRuns(Connection conn, Writer out, String path) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = DBSSql.listRuns(conn, getProcessedDSID(conn, path));
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<run id='" + get(rs, "ID") +
 					"' run_number='" + get(rs, "RUN_NUMBER") +
 					"' number_of_events='" + get(rs, "NUMBER_OF_EVENTS") +
 					"' number_of_lumi_sections='" + get(rs, "NUMBER_OF_LUMI_SECTIONS") +
 					"' total_luminosity='" + get(rs, "TOTAL_LUMINOSITY") +
 					"' store_number='" + get(rs, "STRORE_NUMBER") +
 					"' start_of_run='" + get(rs, "START_OF_RUN") +
 					"' end_of_run='" + get(rs, "END_OF_RUN") +
 					"' creation_date='" + getTime(rs, "CREATION_DATE") +
 					"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 					"' created_by='" + get(rs, "CREATED_BY") +
 					"' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 					"'/>\n"));
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 	}
 
 	/**
 	 * Lists all the data tiers within a processed dataset from the database in a xml format. This method makes one sql query, execute it, fetch the results and packs and write it in xml format to the output stream. The query that it executes get generated by <code>dbs.DBSSql.listTiers</code> method. First it fetches the processed dataset ID from the database by calling a private <code>getProcessedDSID<code> method using the path provided in the parameter. If the processed dataset id does not esists then it throws an exception. A sample XML that is written to the output stream is like <br>
 	 * <code> <"data_tier id='12' name='HIT' creation_date='2006-12-06 16:21:46.0' last_modification_date='2006-12-06 16:21:46.0' created_by='ANZARDN' last_modified_by='ANZARDN'"/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param path a dataset path in the format of /primary/tier/processed. If this path is not provided or the dataset id could not be found then an exception is thrown.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied path is invalid, the database connection is unavailable or processed dataset is not found.
 	 */
 	public void listTiers(Connection conn, Writer out, String path) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps =  DBSSql.listTiers(conn, getProcessedDSID(conn, path));
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<data_tier id='" + get(rs, "ID") +
 					"' name='" + get(rs, "NAME") +
 					"' creation_date='" + getTime(rs, "CREATION_DATE") +
 					"' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
 					"' created_by='" + get(rs, "CREATED_BY") +
 					"' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
 					"'/>\n"));
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 	}
 
 
        /**
 	 * Insert a processed dataset whose parameters are provided in the passed dataset <code>java.util.Hashtable</code>. This hashtable is generated externally and filled in with the processed dataset parameters by parsing the xml input provided by the client. This method inserts entr into more than one table associated with ProcessedDataset table. The the main query that it executes to insert in ProcessedDataset table, get generated by <code>dbs.DBSSql.insertProcessedDataset</code> method.<br> 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it insert a new processed dataset whose sql query is generated by calling <code>dbs.sql.insertProcessedDatatset<code>
 	 * Then it fetches all the algorithm list of the processed dataset that just got inserted and inserts a new row in ProcAlgoMap table by calling a generic private insertMap method. <br>
 	 * Then it fetches all the tier list of the  processed dataset that just got inserted and inserts a new row in ProcDSTier table by calling a generic private insertMap method. Before that it first inserts the data tier if it does not exists by calling a generic insertName method.<br>
 	 * Then it fetches all the parent list of the  processed dataset that just got inserted and inserts a new row in ProcDSParent table by calling a generic private insertMap method. <br>
 	 * Then it fetches all the run list of the  processed dataset that just got inserted and inserts a new row in ProcDSRun table by calling a generic private insertMap method. <br>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param dataset a  <code>java.util.Hastable</code>  that contain all the necessary key value pairs required for inserting a new processed dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>primary_datatset_name, processed_datatset_name, physics_group_name, physics_group_convener,status, data_tier, parent, algorithm, run, created_by, creation_date </code> <br>
 	 * Further the keys <code>data_tier, parent, algorithm, run </code> are itself vector of Hashtable. <br>
 	 * The key that <code>parent </code> hashtable may or may not contain is <code>path</code> <br>
 	 * The key that <code>data_tier </code> hashtable may or may not contain is <code>name</code> <br>
 	 * The keys that <code>run </code> hashtable may or may not contain is  <code>run_number</code> <br>
 	 * The keys that <code>algorithm </code> hashtable may or may not contain are <br> 
 	 * <code>app_version, app_family_name, app_executable_name, ps_name</code> <br>
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a duplicate entry is being added.
 	 */
 	public void insertProcessedDataset(Connection conn, Writer out, Hashtable dataset, Hashtable dbsUser) throws Exception {
 		//Get the User ID from USERDN
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		String cbUserID = personApi.getUserID(conn, get(dataset, "created_by", false), dbsUser );
 		String creationDate = getTime(dataset, "creation_date", false);
 
 		String procDSName = get(dataset, "processed_datatset_name", true);
 		String phyGroupName = get(dataset, "physics_group_name", false);
 		String phyGroupCon = get(dataset, "physics_group_convener", false);
 		String status = get(dataset, "status", false);
 		Vector tierVector = DBSUtil.getVector(dataset,"data_tier");
 		Vector parentVector = DBSUtil.getVector(dataset,"parent");
 		Vector algoVector = DBSUtil.getVector(dataset,"algorithm");
 		Vector runVector = DBSUtil.getVector(dataset,"run");
 	
 
 		//Set defaults Values
 		if (isNull(status)) status = "VALID";
 		if (isNull(phyGroupName)) phyGroupName = "ALLGROUP";
 		if (isNull(phyGroupCon)) phyGroupCon = "ANZARDN";//FIXME Some default convenor name should be used
 		
 		//Insert a Processed Dataset status if it does not exists
 		//insertName(conn, out, "Status", "Status", status , lmbUserID);
 		//FIXME the creation date and created by user id used for other tabes are same as processed dataset table
 		//Insert a Physics Group if it does not exists
 		insertPhysicsGroup(conn, out,  phyGroupName, phyGroupCon, cbUserID, lmbUserID, creationDate);
 		
 		String procDSID = "";
 		//Insert a Processed Datatset before by fetching the primDSID, status
 		if( (procDSID = getID(conn, "ProcessedDataset", "Name", procDSName, false)) == null ) {
 			PreparedStatement ps = null;
 			try {
 				ps = DBSSql.insertProcessedDatatset(conn, 
 					procDSName,
 					getID(conn, "PrimaryDataset", "Name", 
 					get(dataset, "primary_datatset_name", true), 
 					true),
 					get(dataset, "open_for_writing", false),
 					getID(conn, "PhysicsGroup", "PhysicsGroupName", phyGroupName, true), 
 					getID(conn, "ProcDSStatus", "Status", status, true), 
 					cbUserID,
 					lmbUserID,
 					creationDate);
 				ps.execute();
         	        } finally {
 				if (ps != null) ps.close();
 	                }
 
 		} else {
 			writeWarning(out, "Already Exists", "1020", "ProcessedDataset " + procDSName + " Already Exists");
 		}
 
 
 		//Fetch the Processed Datatset ID that was just inseted or fetched , to be used for subsequent insert of other tables.
 		//FIXME this might use processed datatset with primary datatset combination instead of just proDSName
 		//if(isNull(procDSID)) procDSID = getID(conn, "ProcessedDataset", "Name", procDSName, true);
 		if(algoVector.size() > 0 || tierVector.size() > 0 || parentVector.size() > 0) 
 			if(isNull(procDSID)) procDSID = getID(conn, "ProcessedDataset", "Name", procDSName, true);
 		
 		//Insert ProcAlgoMap table by fetching application ID. 
 		for (int j = 0; j < algoVector.size(); ++j) {
 			Hashtable hashTable = (Hashtable)algoVector.get(j);
 			insertMap(conn, out, "ProcAlgo", "Dataset", "Algorithm", 
 					procDSID, 
 					(new DBSApiAlgoLogic(this.data)).getAlgorithmID(conn, get(hashTable, "app_version"), 
 							get(hashTable, "app_family_name"), 
 							get(hashTable, "app_executable_name"),
 							get(hashTable, "ps_hash"), 
 							true), 
 					cbUserID, lmbUserID, creationDate);
 		}
 
 		//Insert ProcDSTier table by fetching data tier ID
 		for (int j = 0; j < tierVector.size(); ++j) {
 			Hashtable hashTable = (Hashtable)tierVector.get(j);
 			String tierName = get(hashTable, "name", true);
 			//Insert DataTier if it does not exists
 			insertTier(conn, out, tierName, cbUserID, lmbUserID, creationDate);
 			//insertName(conn, out, "DataTier", "Name", tierName , lmbUserID);
 			insertMap(conn, out, "ProcDSTier", "Dataset", "DataTier", 
 					procDSID, 
 					getID(conn, "DataTier", "Name", tierName , true), 
 					cbUserID, lmbUserID, creationDate);
 		}
 
 		//Insert ProcDSParent table by fetching parent File ID
 		for (int j = 0; j < parentVector.size(); ++j) {
 			insertMap(conn, out, "ProcDSParent", "ThisDataset", "ItsParent", 
 					procDSID, 
 					getProcessedDSID(conn,  get((Hashtable)parentVector.get(j), "path")), 
 					cbUserID, lmbUserID, creationDate);
 		}
 
 		//Insert ProcDSRun table by fetching Run ID
 		for (int j = 0; j < runVector.size(); ++j) {
 			insertMap(conn, out, "ProcDSRuns", "Dataset", "Run", 
 					procDSID, 
 					getID(conn, "Runs", "RunNumber", get((Hashtable)runVector.get(j), "run_number") , true), 
 					cbUserID, lmbUserID, creationDate);
 		}
 
 	}
 
  
  	/**
 	 * Insert a data tier in processed dataset. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table ProcDSTier by calling a generic private <code>insertMap</code> method. It first fetches the processed dataset id by calling getProcessedDSID.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single processed dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>path, created_by, creation_date </code> <br>
 	 * If this path is not provided or the processed dataset id could not be found then an exception is thrown.
 	 * @param tierName a data tier name which is assumed to be already present in the database.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void insertTierInPD(Connection conn, Writer out, Hashtable table, String tierName, Hashtable dbsUser) throws Exception {
 		insertMap(conn, out, "ProcDSTier", "Dataset", "DataTier", 
 				getProcessedDSID(conn, get(table, "path")), 
 				getID(conn, "DataTier", "Name", tierName , true), 
 				personApi.getUserID(conn, get(table, "created_by", false), dbsUser ),
 				personApi.getUserID(conn, dbsUser),
 				getTime(table, "creation_date", false));
 	}
 
 
 
 	/**
 	 * Insert a dataset parent in processed dataset. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table ProcDSParent by calling a generic private <code>insertMap</code> method. It first fetches the processed dataset id by calling a private getProcessedDSID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single processed dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>path, created_by, creation_date </code> <br>
 	 * If this path is not provided or the processed dataset id could not be found then an exception is thrown.
 	 * @param parentPath a dataset path in the format of /primary/tier/processed that represent the parent of this dataset represented by path.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void insertParentInPD(Connection conn, Writer out, Hashtable table, String parentPath, Hashtable dbsUser) throws Exception {
 		insertMap(conn, out, "ProcDSParent", "ThisDataset", "ItsParent", 
 					getProcessedDSID(conn, get(table, "path")), 
 					getProcessedDSID(conn, parentPath), 
 					personApi.getUserID(conn, get(table, "created_by", false), dbsUser ),
 					personApi.getUserID(conn, dbsUser),
 					getTime(table, "creation_date", false));
 	}
 
 	/**
 	 * Insert a algorithm in processed dataset. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table ProcAlgoMap by calling a generic private <code>insertMap</code> method. It first fetches the processed dataset id by calling a private getProcessedDSID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single processed dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>path, created_by, creation_date </code> <br>
 	 * If this path is not provided or the processed dataset id could not be found then an exception is thrown.
 	 * @param algo a <code>java.util.Hashtable</code> that conatin the parameter that defines an algorithm. The keys that <code>algo </code> hashtable may or may not contain are <br> 
 	 * <code>app_version, app_family_name, app_executable_name, ps_name</code> <br>
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void insertAlgoInPD(Connection conn, Writer out, Hashtable table, Hashtable algo, Hashtable dbsUser) throws Exception {
 		insertMap(conn, out, "ProcAlgo", "Dataset", "Algorithm", 
 					getProcessedDSID(conn, get(table, "path")), 
 					(new DBSApiAlgoLogic(this.data)).getAlgorithmID(conn, get(algo, "app_version"), 
 							get(algo, "app_family_name"), 
 							get(algo, "app_executable_name"),
 							get(algo, "ps_hash"), 
 							true), 
 					personApi.getUserID(conn, get(table, "created_by", false), dbsUser ),
 					personApi.getUserID(conn, dbsUser),
 					getTime(table, "creation_date", false));
 	}
 
 
 	/**
 	 * Insert a run parent in processed dataset. 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it inserts entry into just one table ProcDSRun by calling a generic private <code>insertMap</code> method. It first fetches the processed dataset id by calling a private getProcessedDSID method.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param table a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single processed dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>path, created_by, creation_date </code> <br>
 	 * If this path is not provided or the processed dataset id could not be found then an exception is thrown.
 	 * @param runNumber a run number that uniquely identifies a run.
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a procsssed dataset is not found.
 	 */
 	public void insertRunInPD(Connection conn, Writer out, Hashtable table, String runNumber, Hashtable dbsUser) throws Exception {
 		insertMap(conn, out, "ProcDSRuns", "Dataset", "Run", 
 				getProcessedDSID(conn, get(table, "path")), 
 				getID(conn, "Runs", "RunNumber", runNumber , true), 	
 				personApi.getUserID(conn, get(table, "created_by", false), dbsUser ),
 				personApi.getUserID(conn, dbsUser),
 				getTime(table, "creation_date", false));
 	}
 
 	
 	/**
 	 * Gets a processed data set ID from the datbase by using the dataset path. This method calls another private method getProcessedDSID after spliting the dataset path.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param path a dataset path in the format of /primary/tier/processed. If this path is not provided or the dataset id could not be found then an exception is thrown.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid or  the database connection is unavailable, or the processed dataset is not found.
 	 */
 	public String getProcessedDSID(Connection conn, String path) throws Exception {
 		String id = "";
 		if(!isNull( id = get(this.data.globalPDPath, path) )) {
 			return id;
 		}
 		String[] data = parseDSPath(path);
 		id = getProcessedDSID(conn, data[1], data[2], data[3]);
 		this.data.globalPDPath.put(path, id);
 		return  id;
 	}
 
 	/**
 	 * Gets a processed dataset id from the datbase by using the primary dataset name , data tier name and processed dataset name. This actually generates the sql by calling the <code>dbs.sql.DBSSql.getProcessedDSID</code> method. 
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param prim the name of the primary dataset whose processed dataset id needs to be fetched..
 	 * @param dt the name of the data tier whose processed dataset id needs to be fetched..
 	 * @param proc the name of the processed dataset whose id needs to be fetched..
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid or  the database connection is unavailable, or the processed dataset is not found.
 	 */
 	private String getProcessedDSID(Connection conn, String prim, String dt, String proc) throws Exception {
 		checkWord(prim, "primary_dataset_name");
 		checkWord(dt, "data_tier");
 		checkWord(proc, "processed_dataset_name");
 		//ResultSet rs =  DBManagement.executeQuery(conn, DBSSql.getProcessedDSID(prim, dt, proc));
 		String id = "";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = DBSSql.getProcessedDSID(conn, prim, dt, proc);
 			rs =  ps.executeQuery();
 			if(!rs.next()) {
 				throw new DBSException("Unavailable data", "1008", "No such processed dataset /" + prim + "/" + dt + "/" +proc );
 			}
 			id = get(rs, "ID");
 		} finally {
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 		return  id;
 	}
 
 
 }
