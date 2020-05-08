 /**
 $Revision: 1.19 $"
 $Id: DBSApiAnaDSLogic.java,v 1.19 2007/02/01 19:56:50 sekhri Exp $"
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
 * A class that has the core business logic of all the Analysis datasets APIs.  The signature for the API is internal to DBS and is not exposed to the clients. There is another class <code>dbs.api.DBSApi</code> that has an interface for the clients. All these low level APIs are invoked from <code>dbs.api.DBSApi</code>. This class inherits from DBSApiLogic class.
 * @author sekhri
 */
 public class DBSApiAnaDSLogic extends DBSApiLogic {
 		
 	/**
 	* Constructs a DBSApiLogic object that can be used to invoke several APIs.
 	*/
 
 	DBSApiPersonLogic personApi = null;
 	DBSApiData data = null;
 	public DBSApiAnaDSLogic(DBSApiData data) {
 		super(data);
 		this.data = data;
 		personApi = new DBSApiPersonLogic(data);
 	}
 
 	/**
 	 * Lists all the definitions .  
 	 * <code> <""/></code>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param patternName a parameter passed in from the client that can contain wild card characters for analysis dataset defination name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied lfn is invalid, the database connection is unavailable or the file is not found.
 	 */
 	 public void listAnalysisDatasetDefinition(Connection conn, Writer out, String patternName) throws Exception {
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listAnalysisDatasetDefinition(conn, getPattern(patternName, "pattern_analysis_dataset_definition_name"));
 			rs =  ps.executeQuery();
 			while(rs.next()) {
 				out.write(((String) "<analysis_dataset_definition id='" +  get(rs, "ID") +
					"' analysis_dataset_definition_name='" + get(rs, "ANALYSIS_DATASET_DEFINITION_NAME") +
 					"' lumi_sections='" + get(rs, "LUMI_SECTIONS") +
 					"' lumi_section_ranges='" + get(rs, "LUMI_SECTION_RANGES") +
 					"' runs='" + get(rs, "RUNS") +
 					"' runs_ranges='" + get(rs, "RUNS_RANGES") +
 					"' algorithms='" + get(rs, "ALGORITHMS") +
 					"' lfns='" + get(rs, "LFNS") +
 					"' path='" + get(rs, "PATH") +
 					"' tiers='" + get(rs, "TIERS") +
 					"' analysis_dataset_names='" + get(rs, "ANALYSIS_DATASET_NAMES") +
 					"' user_cut='" + get(rs, "USER_CUT") +
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
          * Lists analysis dataset along with its definition, ALL, or for a particular PATH  
          * <code> <""/></code>
          * @param conn a database connection <code>java.sql.Connection</code> object created externally.
          * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
          * @param patternName a parameter passed in from the client that can contain wild card characters for analysis dataset name. This pattern is used to restrict the SQL query results by sustitution it in the WHERE clause.
          * @param path a parameter passed in from the client that contains Path of a Processed Dataset name. It is used to restrict the SQL query results by sustitution it in the WHERE clause.
          * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied lfn is invalid, the database connection is unavailable or the file is not found.
          */
          public void listAnalysisDataset(Connection conn, Writer out, String patternName, String path) throws Exception {
                 PreparedStatement ps = null;
                 ResultSet rs =  null;
 
                 String procDSID = null;
                 if(!isNull(path)) {
                         procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, path, true);
                 }
 		//System.out.println("Pattern ---------> " + patternName);
                 try {
                         ps = DBSSql.listAnalysisDataset(conn, getPattern(patternName, "analysis_dataset_name_pattern"), procDSID);
                         rs =  ps.executeQuery();
                         while(rs.next()) {
 
                                 out.write(((String) "<analysis_dataset id='" +  get(rs, "ID") +
                                         "' analysis_dataset_name='" + get(rs, "ANALYSIS_DATASET_NAME") +
                                         "' annotation='" + get(rs, "ANNOTATION") +
                                         //"' processed_dataset='" + get(rs, "PROCDSID") +
                                         //"' Definition='" + get(rs, "DEFID") +
                                         "' type='" + get(rs, "TYPE") +
                                         "' status='" + get(rs, "STATUS") +
                                         "' creation_date='" + getTime(rs, "CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "LAST_MODIFIED_BY") +
                                         "'>\n"));
 				//Add the details of definition also for this dataset
                                 out.write(((String) "<analysis_dataset_definition id='" +  get(rs, "ADDID") +
                                        "' analysis_dataset_definition_name='" + get(rs, "ANALYSIS_DATASET_DEFINITION_NAME") +
                                         "' lumi_sections='" + get(rs, "LUMI_SECTIONS") +
                                         "' lumi_section_ranges='" + get(rs, "LUMI_SECTION_RANGES") +
                                         "' runs='" + get(rs, "RUNS") +
                                         "' runs_ranges='" + get(rs, "RUNS_RANGES") +
                                         "' algorithms='" + get(rs, "ALGORITHMS") +
                                         "' lfns='" + get(rs, "LFNS") +
                                         "' path='" + get(rs, "PATH") +
                                         "' tiers='" + get(rs, "TIERS") +
                                         "' analysis_dataset_names='" + get(rs, "ANALYSIS_DATASET_NAMES") +
                                         "' user_cut='" + get(rs, "USER_CUT") +
                                         "' creation_date='" + getTime(rs, "ADD_CREATION_DATE") +
                                         "' last_modification_date='" + get(rs, "ADD_LAST_MODIFICATION_DATE") +
                                         "' created_by='" + get(rs, "ADD_CREATED_BY") +
                                         "' last_modified_by='" + get(rs, "ADD_LAST_MODIFIED_BY") +
                                         "'/>\n"));
                                  out.write("</analysis_dataset>\n");
 
                         }
                 } finally {
                         if (rs != null) rs.close();
                         if (ps != null) ps.close();
                 }
          }
 
 
 
 
  	/**
 	 * Insert a analysis dataset whose parameters are provided in the passed dataset <code>java.util.Hashtable</code>. This hashtable is generated externally and filled in with the analysis dataset parameters by parsing the xml input provided by the client. This method inserts entry into more than one table associated with AnalysisDataset table. The the main query that it executes to insert in AnalysisDataset table, get generated by <code>dbs.DBSSql.insertAnalysisDataset</code> method.<br> 
 	 * First it fetches the userID by using the parameters specified in the dbsUser <code>java.util.Hashtable</code> and if the user does not exists then it insert the new user in the Person table. All this user operation is done by a private method getUserID. <br>
 	 * Then it insert a new type field in AnalysisDSType table by calling a generic private  insertName method. <br>
 	 * Then it insert a new status field in AnalysisDSStatus table by calling a generic private  insertName method. <br>
 	 * Then it insert a new analysis dataset in AnalysisDataset table. <br>
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param out an output stream <code>java.io.Writer</code> object where this method writes the results into.
 	 * @param dataset a  <code>java.util.Hastable</code>  that contain all the necessary key value pairs required for inserting a new analysis dataset. The keys along with its values that it may or may not contain are <br>
 	 * <code>name, type, status, annotation, physics_group_name, created_by, creation_date </code> <br>
 	 * @param dbsUser a <code>java.util.Hashtable</code> that contains all the necessary key value pairs for a single user. The most import key in this table is the user_dn. This hashtable is used to insert the bookkeeping information with each row in the database. This is to know which user did the insert at the first place.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters in the hashtable are invalid, the database connection is unavailable or a duplicate entry is being added.
 	 */
          public void createAnalysisDatasetFromPD(Connection conn, Writer out, Hashtable dataset, Hashtable dbsUser) throws Exception { 
 		String name = get(dataset, "name", true);
 		String type = get(dataset, "type", true);
 		String status = get(dataset, "status", true);
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		String cbUserID = personApi.getUserID(conn, get(dataset, "created_by", false), dbsUser );
 		String creationDate = getTime(dataset, "creation_date", false);
 
 		String procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, get(dataset, "path"), true);
 
 		//FIXME Parentage of Analysis Datasets (not well understood yet)
 		//Vector parentVector = DBSUtil.getVector(dataset,"parent");
 
 		//FIXME   Make sure that Type and Status fileds are well understood
 		//        will they pre-exist, or user can define as they create AnalysisDS ??     
 
 		insertName(conn, out, "AnalysisDSType", "Type", type , cbUserID, lmbUserID, creationDate);
 		insertName(conn, out, "AnalysisDSStatus", "Status", status, cbUserID, lmbUserID, creationDate);
 
 	        ResultSet rsLumi = null;
                 PreparedStatement psLumi = null; 
 		try { 
 			psLumi = DBSSql.listLumiSections(conn, procDSID); 
 			PreparedStatement ps = null;
 			try {
 				ps = DBSSql.insertAnalysisDataset(conn,
 		   				getStr(dataset, "annotation", true),
 		   				name,
 		   				psLumi.toString(),
 		   				procDSID,
 		   				getID(conn, "AnalysisDSType", "Type", type, true),
 						getID(conn, "AnalysisDSStatus", "Status", status, true),
 						getID(conn, "PhysicsGroup", "PhysicsGroupName", 
 							get(dataset, "physics_group_name", true), 
 							true), 
 						cbUserID, lmbUserID, creationDate); 
                                                 
 				ps.execute();
 			 } finally {
 				if (ps != null) ps.close();   
 			 }
 
 			 //System.out.println("ANZAR: procDSID="+procDSID);
 			 //ID of just added AnalysisDS 
 			 String analysisDSID = getID(conn, "AnalysisDataset", "Name", name , true);
                  
 			  //For eacxh run belonging to this ProcDS, get LumiSections and
 			  //make an entry into AnalysisDatasetLumi
 
 			  rsLumi =  psLumi.executeQuery(); 
 			  while(rsLumi.next())    
 				insertMap(conn, out, "AnalysisDatasetLumi", "AnalysisDataset", "Lumi",  
 	 					analysisDSID, 
 						get(rsLumi, "ID"), 
 						cbUserID, lmbUserID, creationDate);
 			  
 		} finally {
                           if (psLumi != null) psLumi.close();
                           if (rsLumi != null) rsLumi.close();
 		}
          }
 	 
 
 		
         public void createAnalysisDatasetDefinition(Connection conn, Writer out, Hashtable table, Hashtable dbsUser) throws Exception { 
 		String adsDefName = get(table, "analysisds_def_name", true);
 		String path = get(table, "path", false);
 		String userCut = get(table, "user_cut");
 		String desc = getStr(table, "description", true);
 		Vector lumiVector = DBSUtil.getVector(table, "lumi_section");
 		Vector runVector = DBSUtil.getVector(table, "run");
 		Vector tierVector = DBSUtil.getVector(table, "data_tier");
 		Vector algoVector = DBSUtil.getVector(table, "algorithm");
 		Vector fileVector = DBSUtil.getVector(table, "file");
 		Vector adsVector = DBSUtil.getVector(table, "analysis_dataset");
 
 		String lumiNumberList = "";
 		String lumiRangeList = "";
 		String runNumberList = "";
 		String runRangeList = "";
 		String tierList = "";
 		String algoList = "";
 		String fileList = "";
 		String adsList = "";
 
 		for (int j = 0; j < lumiVector.size(); ++j) {
 			Hashtable hashTable = (Hashtable)lumiVector.get(j);
 			String lumiNumber = get(hashTable, "lumi_section_number");
 			String lumiRange = get(hashTable, "lumi_section_range");
 			if(!isNull(lumiNumber)) {
 				if(!isNull(lumiNumberList)) lumiNumberList += ",";
 				lumiNumberList += lumiNumber;
 			}
 			if(!isNull(lumiRange)) {
 				if(!isNull(lumiRangeList)) lumiRangeList += ";";
 			       	//lumiRangeList += parseRange(lumiRange);
 			       	lumiRangeList += lumiRange;
 			}
 	 	}
 		for (int j = 0; j < runVector.size(); ++j) {
 			Hashtable hashTable = (Hashtable)runVector.get(j);
 			String runNumber = get(hashTable, "run_number");
 			String runRange = get(hashTable, "run_range");
 			if(!isNull(runNumber)) {
 				if(!isNull(runNumberList)) runNumberList += ",";
 			       	runNumberList += runNumber;
 			}
 			if(!isNull(runRange)) {
 				if(!isNull(runRangeList)) runRangeList += ";";
 				//runRangeList += parseRange(runRange);
 				runRangeList += runRange;
 			}
 	 	}
 		for (int j = 0; j < tierVector.size(); ++j) {
 			if(!isNull(tierList)) tierList += ",";
 			tierList += get((Hashtable)tierVector.get(j), "tier_name", true);
 	 	}
 		for (int j = 0; j < fileVector.size(); ++j) {
 			if(!isNull(fileList)) fileList += ",";
 			fileList += get((Hashtable)fileVector.get(j), "lfn", true);
 	 	}
 		for (int j = 0; j < adsVector.size(); ++j) {
 			if(!isNull(adsList)) adsList += ",";
 			adsList += get((Hashtable)adsVector.get(j), "analysis_dataset_name", true);
 	 	}
 		for (int j = 0; j < algoVector.size(); ++j) {
 			Hashtable hashTable = (Hashtable)algoVector.get(j);
 			if(!isNull(algoList)) algoList += ",";
 			algoList +=  get(hashTable, "app_version", true) + 
 				";" + get(hashTable, "app_family_name", true) + 
 				";" + get(hashTable, "app_executable_name", true) + 
 				";" + get(hashTable, "ps_hash", true);
 	 	}
 		createAnalysisDatasetDefinition(conn, out, adsDefName, path, lumiNumberList, lumiRangeList, 
 				runNumberList, runRangeList, tierList, fileList, adsList, algoList, userCut, desc,
 				personApi.getUserID(conn, get(table, "created_by", false), dbsUser ),
 				personApi.getUserID(conn, dbsUser),
 				getTime(table, "creation_date", false) );
 
 	 }
 	 
          private void createAnalysisDatasetDefinition(Connection conn, Writer out, 
 			 String adsDefName, String path,
 			 String lumiNumberList, String lumiRangeList, 
 			 String runNumberList, String runRangeList, 
 			 String tierList, String fileList, 
 			 String adsList, String algoList, 
 			 String userCut, String desc,
 			 String cbUserID, String lmbUserID, 
 			 String creationDate) throws Exception { 
 		 if( getID(conn, "AnalysisDSDef", "Name", adsDefName, false) == null ) {
 			PreparedStatement ps = null;
 			try {
 				ps = DBSSql.insertAnalysisDatasetDefinition(conn, 
 						adsDefName, 
 						path, 
 						lumiNumberList, 
 						lumiRangeList, 
 						runNumberList, 
 						runRangeList, 
 						tierList, 
 						fileList, 
 						adsList, 
 						algoList, 
 						userCut,
 						desc,
 						cbUserID, 
 						lmbUserID, 
 						creationDate);
 				ps.execute();
 			} finally {
 				if (ps != null) ps.close();
 			}
 
 		} else {
 			//Instead of warnning it is throwing an exception because the clients will know that there already exists 
 			//a definition and he/she needs to create a new one. Warnning can be ignored but exception cannot.
 			//writeWarning(out, "Already Exists", "1020", "Analysis Dataset Defintaion " + adsDefName + " Already Exists");
 			throw new DBSException("Already Exists", "1020", "Analysis Dataset Defintaion " + adsDefName + " Already Exists");
 		}
 
 	 }
 	 
          //public void createAnalysisDataset(Connection conn, Writer out, String analysisDatasetDefinitionName, Hashtable dbsUser) throws Exception {
          public void createAnalysisDataset(Connection conn, Writer out, Hashtable table, Hashtable dbsUser) throws Exception {
 
 		//Get the Definition of the analysis dataset first and get the path and its id to be used for inserting analysis dataset
 		String analysisDatasetDefinitionName = get(table, "analysisds_def_name", true);
 		String lmbUserID = personApi.getUserID(conn, dbsUser);
 		String cbUserID = personApi.getUserID(conn, get(table, "created_by", false), dbsUser );
 		String creationDate = getTime(table, "creation_date", false);
 		String logicalOp = "AND";
 
 		String anaDSDefID = ""; 
 		String procDSID = ""; 
 		//String lumiNumberList = ""; 
 		//String runNumberList = ""; 
 		String tierList = ""; 
 		Vector tierIDList = new Vector(); 
 		Vector fileList = new Vector(); 
 		Vector adsList = new Vector(); 
 		String algoList = ""; 
 		Vector algoIDList = new Vector(); 
 		String userCut = "";
 		Vector lumiRangeList = new Vector(); 
 		Vector runRangeList = new Vector(); 
 
 		PreparedStatement ps = null;
 		ResultSet rs =  null;
 		try {
 			ps = DBSSql.listAnalysisDatasetDefinition(conn, analysisDatasetDefinitionName);
 			rs =  ps.executeQuery();
 			if(rs.next()) {
 				anaDSDefID = get(rs, "ID");
 				procDSID = (new DBSApiProcDSLogic(this.data)).getProcessedDSID(conn, get(rs, "PATH"), true);
 				//lumiNumberList = get(rs, "LUMI_SECTIONS");
 				lumiRangeList = parseRangeList(get(rs, "LUMI_SECTION_RANGES"));
 				//runNumberList = get(rs, "RUNS");
 				runRangeList = parseRangeList(get(rs, "RUNS_RANGES"));
 				tierList = get(rs, "TIERS");
 				algoList = get(rs, "ALGORITHMS");
 				userCut = get(rs, "USER_CUT");
 				//FIXME insert it in definition first and change the query to fetch it
 				//logicalOp = get(rs, "LOGICAL_OP");
 				fileList = listToVector(get(rs, "LFNS"));
 				adsList = listToVector(get(rs, "ANALYSIS_DATASET_NAMES"));
 			
 			} else {
 				throw new DBSException("Unavailable data", "1008", "No such analysis dataset definition " + analysisDatasetDefinitionName );
 			}
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 
 		//Get all the tier IDs
 		if(!isNull(tierList)) {
 			String[] data = tierList.split(",");
 			String tmpList = "";
 			for (int i = 0; i != data.length ; ++i) {
 				if(!isNull(tmpList)) tmpList += ",";
 				tmpList += getID(conn, "DataTier", "Name", data[i], true);
 			}
 			tierIDList = listToVector(tmpList);
 		}
 
 		//Get all the algo IDs
 		if(!isNull(algoList)) {
 			String[] algo = algoList.split(",");
 			String tmpList = "";
 			for (int i = 0; i != algo.length ; ++i) {
 				String data[] = algo[i].split(";");
 				if (data.length != 4) throw new DBSException("Invalid format", "1066", "Algorithm not stored in proper format in AnalysisDSDef Table. Proper format is version1;family1;exe1;pshash1,vrsion2;family2;exe2;pshash Given " + algo[i]);
 				if(!isNull(tmpList)) tmpList += ",";
 				tmpList += (new DBSApiAlgoLogic(this.data)).getAlgorithmID(conn, data[0], data[1], data[2],data[3], true);
 			}
 			algoIDList = listToVector(tmpList);
 		}
 
 		
 
 		//Insert a row in AnalysisDataset Table
 		String analysisDatasetName = get(table, "name", true); 
 		String status = get(table, "status", false);
 		String type = get(table, "type", false);
 
 		//FIXME Dafults should no be set by the server
 		if (isNull(status)) status = "NEW";
 		if (isNull(type)) type = "TEST";
 
 		String aDSID = "";
 		if( isNull((aDSID = getID(conn, "AnalysisDataset", "Name", analysisDatasetName, false)))) {
 			ps = null;
 			try {
 				ps = DBSSql.insertAnalysisDataset(conn, 
 						analysisDatasetName,
 						get(table, "annotation", false),
 						procDSID,
 						anaDSDefID,
 						getID(conn, "AnalysisDSType", "Type", type, true),
 						getID(conn, "AnalysisDSStatus", "Status", status, true),
 						"",//FIXME Parent is debatable
 						getID(conn, "PhysicsGroup", "PhysicsGroupName", 
 							 get(table, "physics_group_name", true),
  							 true),
 						cbUserID, lmbUserID, creationDate);
 				ps.execute();
 			} finally { 
 				if (ps != null) ps.close();
 			}
 		} else {
 			//Write waring message that analysis dataset exists already
 			writeWarning(out, "Already Exists", "1020", "AnalysisDataset " + analysisDatasetName + " Already Exists");
 		}
 		
 		//Fetch the analysis dataset id if not null, that just got inserted
 		if(isNull(aDSID)) aDSID = getID(conn, "AnalysisDataset", "Name", analysisDatasetName, false);
 
 		//Insert the contents of the analysis dataset in the AnalysisDSFileLumi map table
 		ps = null;
 		rs = null;
 		//Defualt value for logical op
 		if(isNull(logicalOp)) logicalOp = "OR";
 		try {
 			ps = DBSSql.listAnalysisDSFileLumi(conn, 
 					//aDSID,
 					procDSID,
 					tierIDList,
 					algoIDList,
 					fileList,
 					lumiRangeList,
 					runRangeList,
 					adsList,
 					userCut,
 					logicalOp,
 					cbUserID, lmbUserID, creationDate);
 			rs =  ps.executeQuery();
 			
 			//For every lumiid,fileid pair insert a row in AnalysisDSFuleLumi table 
 			while(rs.next()) {
 				System.out.println("ADSID, Lumi ID , File ID = " + aDSID + "," + get(rs, "LUMIID") + "," + get(rs, "FILEID"));
 				insertMap(conn, out, "AnalysisDSFileLumi", "AnalysisDataset", "Lumi", "Fileid",
 						aDSID,
 						get(rs, "LUMIID"),
 						get(rs, "FILEID"),
 						cbUserID, lmbUserID, creationDate);
 
 			} 
 
 		} finally { 
 			if (rs != null) rs.close();
 			if (ps != null) ps.close();
 		}
 
 		
 	
 	 }
  
  	private Vector listToVector(String list) throws Exception {
 		Vector v = new Vector();
 		if(isNull(list)) return v;
 		String[] data = list.split(",");
 		if(data.length == 0) {
 			return v;
 		}
 		for (int i = 0; i != data.length ; ++i) {
 			v.add(data[i]);
 		}
 		return v;
 	}
 
 	 private String parseRange(String range) throws Exception {
 		String[] data = range.split(",");
 		if(data.length != 2) {
 			throw new DBSException("Invalid format", "1037", " Expected a range in format number,number given " + range);
 		}
 		return data[0] + " AND " + data[1];
 	}
 
 	private Vector parseRangeList(String range) throws Exception {
 		Vector v = new Vector();
 		if(isNull(range)) return v;
 		String[] data = range.split(";");
 		if(data.length == 0) {
 			return v;
 		}
 		for (int i = 0; i != data.length ; ++i) {
 			String[] finalData = data[i].split(",");
 			if(finalData.length != 2) throw new DBSException("Invalid format", "1037", " Expected a range in format number,number given " + data[i]);
 			v.add(finalData);
 		}
 		return v;
 	}
 
 }
