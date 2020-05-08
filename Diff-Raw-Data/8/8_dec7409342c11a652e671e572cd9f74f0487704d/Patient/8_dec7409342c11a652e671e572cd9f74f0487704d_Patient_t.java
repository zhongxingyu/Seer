 /*
  *  Copyright 2010 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.mitre.medcafe.model;
 
 import java.io.*;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.util.*;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mitre.medcafe.util.DbConnection;
 import org.mitre.medcafe.util.DatabaseUtility;
 import org.mitre.medcafe.util.WebUtils;
 
 /**
  *  Representation of the text data
  *  @author: Gail Hamilton
  */
 public class Patient
 {
 	 public final static String KEY = Patient.class.getName();
 	    public final static Logger log = Logger.getLogger( KEY );
 	    static{log.setLevel(Level.FINER);}
 
 	private String id = "";
 	private String firstName = "";
 	private String lastName = "";
 	private ArrayList<String> repositories =null;
 
 	public static final String SEARCH_USER_PATIENTS_BY_ID = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc, patient_repository_assoc " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and  patient_user_assoc.patient_id = ? and patient_user_assoc.username = ? ";
 
 	public static final String SEARCH_USER_PATIENTS_BY_FIRST_NAME = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc, patient_repository_assoc  " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and lower(first_name) like lower(?) ";
 
 	public static final String SEARCH_USER_PATIENTS_BY_LAST_NAME = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc , patient_repository_assoc " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and lower(last_name) like lower(?)";
 
 	public static final String SEARCH_USER_PATIENTS_BY_ALL = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc, patient_repository_assoc " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and lower(first_name) like lower(?) and lower(first_name) like lower(?) ";
 
 	public static final String SEARCH_BY_REPOSITORY = " and patient_repository_assoc.repository = ? ";
 
 	public static final String SEARCH_PATIENT = "SELECT id, first_name, last_name from patient where  last_name = ? and first_name = ? and rep_patient_id = ? ";
	public static final String SEARCH_PATIENTS_BY_ID = "SELECT DISTINCT id, first_name, last_name, photo, patient.repository from patient, patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and patient.id = ? and patient.repository=patient_repository_assoc.repository";
 
 	public static final String SEARCH_PATIENTS_BY_FIRST_NAME = "SELECT DISTINCT id, first_name, last_name from patient,patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and lower(first_name) like lower(?) ";
 	public static final String SEARCH_PATIENTS_BY_LAST_NAME = "SELECT DISTINCT id, first_name, last_name from patient, patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and lower(last_name) like lower(?) ";
 	public static final String SEARCH_PATIENTS_BY_ALL = "SELECT DISTINCT id, first_name, last_name from patient,patient_repository_assoc  where patient_repository_assoc.patient_id = patient.id and lower(last_name) like lower(?) and lower(first_name) like lower(?)";
 
 	public static final String SEARCH_PATIENTS_REP_ASSOC_BY_ID = "SELECT id, first_name, last_name, patient_repository_assoc.repository from patient, patient_repository_assoc where " +
 																" patient_repository_assoc.patient_id = patient.id and patient.id = ? and patient_repository_assoc.rep_patient_id = ? and patient_repository_assoc.repository = ? ";
 
 	public static final String SEARCH_PATIENTS_REP_ASSOC_BY_REP_ID = "SELECT id, first_name, last_name, patient_repository_assoc.repository from patient, patient_repository_assoc where " +
 																" patient_repository_assoc.patient_id = patient.id and patient_repository_assoc.rep_patient_id = ? and patient_repository_assoc.repository = ? ";
 
 	public static final String LIST_REP_ASSOC_BY_PATIENT_ID = "SELECT patient_id, rep_patient_id, repository from  patient_repository_assoc where patient_id = ? ";
 
 	public static final String SELECT_PATIENT_ID_FOR_REPOSITORY = "SELECT patient_id from patient_repository_assoc where repository=? and rep_patient_id=?";
 	public static final String SEARCH_RECENT_PATIENTS = "SELECT patient.id, patient_id,first_name,last_name from patient, recent_patients where patient.id = recent_patients.patient_id and recent_patients.username = ?";
 	public static final String SELECT_RECENT_PATIENTS = "SELECT patient_id from  recent_patients where username = ? and patient_id = ? ";
 	public static final String INSERT_RECENT_PATIENTS = "INSERT INTO recent_patients  (username, patient_id) values ( ?, ?)";
 	public static final String UPDATE_RECENT_PATIENTS = "UPDATE recent_patients SET date_accessed = ? where username = ? and patient_id = ?";
 
 	public static final String INSERT_PATIENT = "INSERT INTO patient (first_name, last_name, rep_patient_id) values (?,?,?) ";
 	public static final String SELECT_PATIENT_HISTORY = " SELECT patient_id, history, category, history_date, history_notes, priority.priority, color from medical_history, history_category, priority where medical_history.category_id = history_category.id and priority.id = medical_history.priority and  patient_id = ? and category = ? ";
 	public static final String SELECT_PATIENT_HISTORY_EXT = " and history_date > ? and history_date < ? ";
 	public static final String SELECT_PATIENT_HISTORY_ORDER_BY = " order by priority.priority ASC, history_date DESC";
 	public static final String INSERT_ASSOCIATION = "INSERT INTO patient_user_assoc (patient_id, username, role) values (?,?,?) ";
 	public static final String INSERT_REPOSITORY_ASSOCIATION = "INSERT INTO patient_repository_assoc (patient_id, rep_patient_id,repository) values (?,?,?) ";
 	public static final String FIRST_NAME_TYPE = "first";
 	public static final String LAST_NAME_TYPE = "last";
 
 	public static final String FIRST_NAME= "first_name";
 	public static final String LAST_NAME = "last_name";
	public static final String PHOTO = "photo";
 	public static final String ID = "id";
 	public static final String REP_ID = "patient_rep_id";
 	public static final String NO_PATIENT = "No Patient";
 
 
 	public Patient(String firstName, String lastName)
 	{
 		this();
 		this.firstName = firstName;
 		this.lastName = lastName;
 
 
 	}
 
 	public Patient()
 	{
 		super();
 		repositories = new ArrayList<String>();
 	}
 
 
 	public static DbConnection setConnection() throws SQLException
 	{
 
 		return new DbConnection();
 	}
 
 
 	public static void closeConnections(DbConnection dbConn, PreparedStatement prep, ResultSet rs) 
 	{
 		DatabaseUtility.close(prep);
 		DatabaseUtility.close(rs);
 		
 		if (dbConn != null)
 			dbConn.close();
 		
 	}
 		public static void closeConnections(DbConnection dbConn) 
 	{
 		
 		closeConnections(dbConn, null, null);
 		
 	}
 	public static void closeConnections(DbConnection dbConn, PreparedStatement prep)
 	{
 		closeConnections(dbConn, prep, null);
 	}
 	public static void closeConnections(DbConnection dbConn, ResultSet rs)
 	{
 		closeConnections(dbConn, null, rs);
 	}
 	public JSONObject associatePatient( String userName, String patientId, String role)
 	{
 		JSONObject ret = new JSONObject();
 		DbConnection dbConn = null;
 		int rtn = 0;
 		try {
 		dbConn = setConnection();
 		//INSERT_ASSOCIATION = "INSERT INTO patient_user_assoc (patient_id, user_id, role) values (?,?,?) ";
 		String insertQuery = INSERT_ASSOCIATION;
 		int patient_id = Integer.parseInt(patientId);
 		String err_mess = "Could not insert association for patient  " + patient_id;
 		if (role == null)
 			role="physician";
 
 		rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , patient_id, userName, role);
 		}
 		catch (SQLException e)
 		{
 
 			return WebUtils.buildErrorJson("Problem accessing database for patient " + patientId);
 		}
 		finally
 		{
 			closeConnections(dbConn);
 			dbConn = null;
 		}
 		if (rtn < 0)
 		{
 
 			return WebUtils.buildErrorJson( "Problem on creating an association for patient."  + patientId  );
 		}
 
 		return ret;
 
 	}
 
 	public JSONObject associatePatientRepository( String patientId, String patient_rep_id, String repository, String userName)
 	{
 		JSONObject ret = new JSONObject();
 		DbConnection dbConn = null;
 		try {
 			dbConn = setConnection();
 			if (patientId == null)
 			{
 				ret = checkExists(patient_rep_id, repository);
 
 				System.out.print("Patient: associatePatientRepository patient exists already JSON data  " + ret.toString() );
 
 				if (ret.get("exists").equals("true"))
 					return ret;
 
 				//First create a new patient
 				ret = insertPatient(patient_rep_id);
 				System.out.print("Patient: associatePatientRepository got JSON Object " + ret.toString());
 				patientId = ret.getString(Patient.ID);
 
 			}
 			else
 			{
 
 			}
 
 			System.out.print("Patient: associatePatientRepository patient exists so about to insert association " );
 
 			int patient_id = Integer.parseInt(patientId);
 			int patientRepId = Integer.parseInt(patient_rep_id);
 
 			ret = associatePatient(userName, patientId, "physician");
 
 			//INSERT_ASSOCIATION = "INSERT INTO patient_user_assoc (patient_id, user_id, role) values (?,?,?) ";
 			String insertQuery = INSERT_REPOSITORY_ASSOCIATION;
 			int rtn = 0;
 			String err_mess = "Could not insert repository association for patient  " + patient_id;
 
 			//public static final String INSERT_REPOSITORY_ASSOCIATION = "INSERT INTO patient_repository_assoc (patient_id, rep_patient_id,repository) values (?,?,?) ";
 
 			rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , patient_id, patientRepId, repository );
 
 			if (rtn < 0)
 			{
 				return WebUtils.buildErrorJson( "Problem on creating an association for patient."  + patient_id  );
 			}
 			ret = checkExists(patient_rep_id, repository);
 			System.out.print("Patient: associatePatientRepository patient added successfully " + ret.toString() );
 
 
 		}
 		catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return WebUtils.buildErrorJson( "Error on retrieving patient JSON data "  + firstName + " " + lastName   + " " +  e.getMessage());
 
 		}
 		catch (SQLException e)
 		{
 
 			e.printStackTrace();
 			return WebUtils.buildErrorJson("Error on saving patient association information " + firstName + " " + lastName+ " " + e.getMessage());
 		}
 		finally {
 			closeConnections(dbConn);
 			dbConn = null;
 		}
 		return ret;
 
 	}
 
 	public JSONObject checkExists(String patientRepId, String repository)
 	{
 		JSONObject ret = new JSONObject();
 		int patient_rep_id = 0;
 		DbConnection dbConn = null;
 		ResultSet rs = null;
 		try
 		{
       	dbConn = setConnection();
 			if (patientRepId != null)
 			{
 				//First create a new patient
 				patient_rep_id = Integer.parseInt(patientRepId);
 			}
 
 			String err_mess = "Could not check for existence of patient  " + firstName + " " + lastName;
 
 			//First check that this patient has not already been added	
 			String checkPatient = SEARCH_PATIENTS_REP_ASSOC_BY_REP_ID;
 			rs = dbConn.psExecuteQuery(checkPatient, err_mess , patient_rep_id, repository);
 
 			//This patient already exists
 			if (rs.next())
 			{
 				int patientId = rs.getInt(1);
 				ret = this.toJSON();
 				ret.put(Patient.ID, patientId );
 				ret.put("exists", "true");
 				ret.put(Patient.REP_ID, patientRepId);
 				return ret;
 			}
 			else
 			{
 				ret.put("exists", "false");
 				ret.put(Patient.REP_ID, patientRepId);
 			}
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return WebUtils.buildErrorJson( "Error on retrieving patient data "  + firstName + " " + lastName   + " " +  e.getMessage());
 
 		} catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Error on generating JSON data "  + firstName + " " + lastName   + " " +  e.getMessage());
 
 		}
 		finally
 		{
 			closeConnections(dbConn, rs);
 			rs = null;
 			dbConn = null; 
 		}
 
 		return ret;
 	}
 
 
 	public JSONObject insertPatient(  String patientRepId)
 	{
 		JSONObject ret = new JSONObject();
 		int patient_rep_id = 0;
 		DbConnection dbConn = null;
 		ResultSet rs = null;
 		try
 		{
 			dbConn = setConnection();
 			if (patientRepId != null)
 			{
 				//First create a new patient
 				patient_rep_id = Integer.parseInt(patientRepId);
 			}
 			int rtn = 0;
 
 
 			String insertQuery = INSERT_PATIENT;
 			String err_mess = "Could not insert patient  " + firstName + " " + lastName;
 
 			rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , firstName, lastName, patient_rep_id);
 
 			if (rtn < 0)
 			{
 			   closeConnections(dbConn);
 			   dbConn = null;
 				return WebUtils.buildErrorJson( "Problem on inserting patient."  + firstName + " " + lastName  );
 			}
 
 			rs = dbConn.psExecuteQuery(SEARCH_PATIENT, err_mess, lastName, firstName, patient_rep_id );
 
 			//Now get the newly inserted patient
 
 			if (rs.next())
 			{
 				int patient_id = rs.getInt("id");
 				if (patient_id != -1)
 				{
 					id = patient_id +"";
 					ret = this.toJSON();
 				}
 			}
 			else
 			{
 				closeConnections(dbConn, rs);
 				rs = null;
 				dbConn = null;
 				return WebUtils.buildErrorJson( "Could not find patient with name  :"  + firstName + " " + lastName  );
 
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return WebUtils.buildErrorJson( "Error on retrieving patient data "  + firstName + " " + lastName   + " " +  e.getMessage());
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return WebUtils.buildErrorJson( "Error on retrieving patient JSON data "  + firstName + " " + lastName   + " " +  e.getMessage());
 
 		}
 		finally
 		{
 			closeConnections(dbConn, rs);
 			rs = null;
 			dbConn = null;
 		}
 
 		return ret;
 
 	}
 	public JSONObject isPatient( String userName, String patientId)
 	{
 		DbConnection dbConn = null;
 		ResultSet rs = null;
 		JSONObject ret = new JSONObject();
 		String checkQuery = SEARCH_USER_PATIENTS_BY_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 
 		String err_mess = "Could not check if there is an existing association for patient  " + patient_id;
 		try {
 			dbConn = setConnection();
 			rs = dbConn.psExecuteQuery(checkQuery, err_mess , patient_id, userName);
 
 
 			if (rs.next())
 			{
 				  String role = rs.getString("role");
 				  int id = rs.getInt(1);
 		          String fName = rs.getString(Patient.FIRST_NAME);
 		          String lName = rs.getString(Patient.LAST_NAME);
 		          ret.put(Patient.ID, id);
 			      ret.put(Patient.FIRST_NAME, fName);
 			      ret.put(Patient.LAST_NAME, lName);
 			}
 			else
 			{
 				 ret.put(ID, NO_PATIENT);
 			}
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 
 			return WebUtils.buildErrorJson( "Problem on checking if patient currently is in list. "  + patient_id  + " Error " + e.getMessage() );
 		} catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on JSON creation on checking if patient currently is in list. "  + patient_id  + " Error " + e.getMessage() );
 		}
 		finally {
 	   	closeConnections(dbConn, rs);
 	   	dbConn = null;
 	   	rs = null;
 	   }
 		return ret;
 
 	}
 
 	public JSONObject isPatient(String patientId, String patientRepId, String repository)
 	{
 		DbConnection dbConn = null;
 		ResultSet rs = null;
 		JSONObject ret = new JSONObject();
 		String checkQuery = SEARCH_PATIENTS_REP_ASSOC_BY_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 		int patient_rep_id = Integer.parseInt(patientRepId);
 
 		String err_mess = "Could not check if there is an existing repository association for patient  " + patient_id;
 		try {
 			dbConn = setConnection();
 			rs = dbConn.psExecuteQuery(checkQuery, err_mess , patient_id, patient_rep_id,repository );
 
 
 			if (rs.next())
 			{
 				  patient_id = rs.getInt(1);
 		          String fName = rs.getString(Patient.FIRST_NAME);
 		          String lName = rs.getString(Patient.LAST_NAME);
 		          ret.put(Patient.ID, patient_id);
 		          ret.put(Patient.REP_ID, patient_rep_id);
 		          ret.put("repository", repository);
 		          ret.put(Patient.FIRST_NAME, fName);
 		          ret.put(Patient.LAST_NAME, lName);
 			}
 			else
 			{
 				 ret.put(Patient.ID, NO_PATIENT);
 			}
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 
 			return WebUtils.buildErrorJson( "Problem on checking if patient currently is in list. "  + patient_id  + " Error " + e.getMessage() );
 		} catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on JSON creation on checking if patient currently is in list. "  + patient_id  + " Error " + e.getMessage() );
 		}
 		finally{
 			closeConnections(dbConn, rs);
 			dbConn = null;
 			rs = null;
 		}
 		return ret;
 
 	}
 
 	public JSONObject listRepositories(String patientId)
 	{
 		DbConnection dbConn = null;
 		ResultSet rs = null;
 		JSONObject ret = new JSONObject();
 		//SELECT patient_id, rep_patient_id, repository from  patient_repository_assoc where patient_id = ? ";
 
 		String listQuery = LIST_REP_ASSOC_BY_PATIENT_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 
 		String err_mess = "Could not check the list of repositories for patient " + patient_id;
 		boolean results = false;
 		try {
 			dbConn = setConnection();		
 			rs = dbConn.psExecuteQuery(listQuery, err_mess , patient_id );
 
 		/*
 		{ "repositories" : [
 				{"repository" : "OurVista" , "id" : "3"},{"repository" : "local" , "id" : "1"}
 				]
 		}
 		*/
 
 
 			JSONArray reps = new JSONArray();
 			while (rs.next())
 			{
 				JSONObject innerObj = new JSONObject();
 
 				  patient_id = rs.getInt(1);
 				  String rep = rs.getString("repository");
 				  int repId = rs.getInt("rep_patient_id");
 
 				  innerObj.put("repository", rep);
 				  innerObj.put("id", repId);
 				  ret.append("repositories", innerObj);
 
 		          results = true;
 			}
 
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		} catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		}
 		finally {
 			closeConnections(dbConn, rs);
 			dbConn = null;
 			rs = null;
 		}
 
 		if (!results)
         {
 
         	return WebUtils.buildErrorJson( "There are no patients currently listed in repositories " );
 
         }
 
 		return ret;
 
 	}
 
 	public JSONObject searchJson(String isPatient, String searchStringFirst, String searchStringLast, String userName, String server){
 			DbConnection dbConn = null;
 			ResultSet rs = null;
 			PreparedStatement prep = null;
 		 	boolean rtnResults = false;
 		 	JSONObject ret = new JSONObject();
 	        try
 	        {
 	           	dbConn = setConnection();
 	        		StatementResult stmtRes = getPatients( isPatient, searchStringFirst, searchStringLast, userName, server, dbConn);
 	        		rs = stmtRes.getRS();
 	        		prep = stmtRes.getPrep();
 		        if( rs == null )
 		        {
 		            return WebUtils.buildErrorJson( "Could not establish a connection to the database  at this time.");
 		        }
 		        while( rs.next())
 		        {
 			        //convert to JSON
                     rtnResults = true;
                     JSONObject o = new JSONObject();
                     int id = rs.getInt(1);
                     String fName = rs.getString(Patient.FIRST_NAME);
                     String lName = rs.getString(Patient.LAST_NAME);
                     o.put(Patient.ID, id);
                     o.put(Patient.FIRST_NAME, fName);
                     o.put(Patient.LAST_NAME, lName);
                     ret.append("patients", o);
 		        }
 
 		    }
 	        catch(org.json.JSONException e)
 	        {	
 
 	            log.throwing(KEY, "toJson()", e);
 	            try {
 					return new JSONObject("{\"error\": \""+e.getMessage()+"\"}");
 				} catch (JSONException e1) {
 
 					// TODO Auto-generated catch block
 					return WebUtils.buildErrorJson( "Problem on generating JSON error." + e.getMessage());
 				}
 	        }
 	        catch (SQLException e) {
 
 				// TODO Auto-generated catch block
 	        	 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 			}
 			finally{
 				closeConnections(dbConn, prep, rs);
 				rs = null;
 				prep = null;
 				dbConn = null;
 			}
 
 	        if (!rtnResults)
 	        {
 
 	        	return WebUtils.buildErrorJson( "There are no patients currently listed for First Name " + searchStringFirst + " and Last Name " + searchStringLast );
 	        }
 
 	        return ret ;
 	    }
 
 	private StatementResult getPatients(String isPatient, String searchStringFirst, String searchStringLast, String userName, String server, DbConnection dbConn) throws SQLException
 	{
 		 
 
 		 System.out.println("Patient: getPatients : got connection " );
 
 		 PreparedStatement prep= null;
 
 		 boolean hasServer = false;
 		 if (!server.equals("noServer"))
 		 {
 			 hasServer = true;
 		 }
 
 		 if (isPatient.equals("true"))
 		 {
 			 if (searchStringFirst.length() == 0)
 			 {
 				 String sql = Patient.SEARCH_USER_PATIENTS_BY_LAST_NAME;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep = dbConn.prepareStatement(sql);
 
 				 prep.setString(1, userName);
 				 prep.setString(2, "%"+searchStringLast+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(3,server);
 				 }
 			 }
 			 else if (searchStringLast.length() == 0 )
 			 {
 				 String sql = Patient.SEARCH_USER_PATIENTS_BY_FIRST_NAME;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep = dbConn.prepareStatement(sql);
 
 				 prep.setString(1, userName);
 				 prep.setString(2, "%"+searchStringFirst+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(3,server);
 				 }
 			 }
 			 else
 			 {
 				 String sql = Patient.SEARCH_USER_PATIENTS_BY_ALL;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 prep = dbConn.prepareStatement(sql);
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep.setString(1, userName);
 				 prep.setString(2, "%"+searchStringLast+"%");
 				 prep.setString(3, "%"+searchStringFirst+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(4,server);
 				 }
 			 }
 		 }
 		 else
 		 {
 			 if (searchStringFirst.length() == 0)
 			 {
 				 //prep = dbConn.prepareStatement(Patient.SEARCH_PATIENTS_BY_LAST_NAME);
 				 String sql = Patient.SEARCH_PATIENTS_BY_LAST_NAME;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep = dbConn.prepareStatement(sql);
 
 				 prep.setString(1, "%"+searchStringLast+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(2,server);
 				 }
 			 }
 			 else if (searchStringLast.length() == 0 )
 			 {
 				 //prep = dbConn.prepareStatement(Patient.SEARCH_PATIENTS_BY_FIRST_NAME);
 				 String sql = Patient.SEARCH_PATIENTS_BY_FIRST_NAME;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep = dbConn.prepareStatement(sql);
 
 				 prep.setString(1, "%"+searchStringFirst+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(2,server);
 				 }
 			 }
 			 else
 			 {
 				 //prep = dbConn.prepareStatement(Patient.SEARCH_PATIENTS_BY_ALL);
 				 String sql = Patient.SEARCH_PATIENTS_BY_ALL;
 				 if (hasServer)
 				 {
 					 sql = sql + Patient.SEARCH_BY_REPOSITORY;
 				 }
 				 System.out.println("Patient: getPatients : SQL " + sql);
 				 prep = dbConn.prepareStatement(sql);
 
 				 prep.setString(1, "%"+searchStringLast+"%");
 				 prep.setString(2, "%"+searchStringFirst+"%");
 				 if (hasServer)
 				 {
 					 prep.setString(3,server);
 				 }
 			 }
 
 		 }
 		 log.finer("Patient.getPatients() query: " + prep.toString());
 		 ResultSet rs = prep.executeQuery();
 		// DatabaseUtility.close(prep);
 	     return new StatementResult(prep,rs);
 
 	}
 
 
 	 public static JSONObject getPatient(int id)
 	 {
 
 
 		 System.out.println("Patient: getPatients : got connection " );
 		 boolean rtnResults = false;
 		 JSONObject ret = new JSONObject();
 		
 		DbConnection dbConn = null;
 		 PreparedStatement prep= null;
 		 ResultSet rs = null;
 		 try
 		 {
 		
 				 dbConn= setConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SEARCH_PATIENTS_BY_ID);
 
 			 prep.setInt(1,id);
 			 rs = prep.executeQuery();
 			 while( rs.next())
 		     {
 			        //convert to JSON
 			        rtnResults = true;
 
 			        String fName = rs.getString("first_name");
 			        String lName = rs.getString("last_name");
			        String photo = rs.getString("photo");
 			        ret.put(Patient.ID, id);
 			        ret.put(Patient.FIRST_NAME, fName);
 			        ret.put(Patient.LAST_NAME, lName);
			        ret.put(Patient.PHOTO, photo);
 		     }
 
 			 if (!rtnResults)
 		      {
 					closeConnections(dbConn, prep, rs);
 					dbConn = null;
 					prep = null;
 					rs = null;
 		          log.warning( "There are no patients currently listed for patient id " + id );
 		          return WebUtils.buildErrorJson( "There are no patients currently listed for patient id " + id );
 		      }
 		 }
 		 catch (SQLException e)
 		 {
 
 		     // TODO Auto-generated catch block
 		     log.warning("Problem on selecting data from database ." + e.getMessage());
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		 }
 		 catch (JSONException e)
 		 {
 
 			// TODO Auto-generated catch block
 			log.warning("Problem on generating JSON error." + e.getMessage());
 			return WebUtils.buildErrorJson( "Problem on generating JSON error." + e.getMessage());
 
 		 }
 		 finally{
 		 	closeConnections(dbConn, prep, rs);
 		 	dbConn = null;
 		 	prep = null;
 		 	rs = null;
 		 }
 
 	     return ret;
 
 	 }
 
 	 public static JSONObject getRecentPatients(String userName)
 	 {
 		 DbConnection dbConn = null;
 		 System.out.println("Patient: getRecentPatients : got connection " );
 		 boolean rtnResults = false;
 		 JSONObject ret = new JSONObject();
 
 		 PreparedStatement prep = null;
 		 ResultSet rs = null;
 		 try
 		 {
 			
 				 dbConn= setConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SEARCH_RECENT_PATIENTS);
 
 			 prep.setString(1,userName);
 			 rs = prep.executeQuery();
 			 while( rs.next())
 		     {
 			        //convert to JSON
 			        rtnResults = true;
 
 			        JSONObject o = new JSONObject();
 			        String fName = rs.getString("first_name");
 			        String lName = rs.getString("last_name");
 			        int patient_id = rs.getInt("patient_id");
 			        o.put(Patient.ID, patient_id);
 			        o.put(Patient.FIRST_NAME, fName);
 			        o.put(Patient.LAST_NAME, lName);
 			        ret.append("patients", o);
 		     }
 
 			 if (!rtnResults)
 		      {
 		      	closeConnections(dbConn, prep, rs);
 		      	dbConn = null;
 		      	prep = null;
 		      	rs = null;
 
 		        	return WebUtils.buildErrorJson( "There are no recent patients currently listed for user " + userName );
 
 		      }
 		 }
 		 catch (SQLException e)
 		 {
 
 				// TODO Auto-generated catch block
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		 }
 		 catch (JSONException e)
 		 {
 
 			// TODO Auto-generated catch block
 			return WebUtils.buildErrorJson( "Problem on generating JSON error." + e.getMessage());
 
 		 }
 		 finally {
 
 		 	closeConnections(dbConn, prep, rs);
 		 	dbConn = null;
 		 	prep = null;
 		 	rs = null;
 		 }
 	     return ret;
 
 	 }
 
 public static int getLocalId(String repository, String rep_patient_id)
 	 {
 		 System.out.println("Patient: getLocalId : got connection " );
 		 boolean rtnResults = false;
 		DbConnection dbConn = null;
 		 PreparedStatement prep = null;
 		 ResultSet rs = null;
 		 int ret = -1;
 		 try
 		 {
 
 				 dbConn= setConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SELECT_PATIENT_ID_FOR_REPOSITORY);
 			 int rep_id = Integer.parseInt(rep_patient_id);
 			 prep.setString(1,repository);
 			 prep.setInt(2,rep_id);
 			 rs = prep.executeQuery();
 			 
 			 while( rs.next())
 		     {
 			        //convert to JSON
 			        rtnResults = true;
 			        ret = rs.getInt("patient_id");
 			     
 		     }
 
 			
 		 }
 		 catch (SQLException e)
 		 {
 				// TODO Auto-generated catch block
 			 log.log(Level.SEVERE, "Problem on selecting data from database ." + e.getMessage());
 
 		 }
 		 finally {
 		  	closeConnections(dbConn, prep, rs);
 		  	dbConn = null;
 		  	prep = null;
 		  	rs = null;
 		  }
 	     return ret;
 
 	 }
 
 
 	 public static JSONObject addRecentPatients(String userName, String patientId )
 	 {
 	 	DbConnection dbConn = null;
 		 System.out.println("Patient: addRecentPatients : got connection " );
 
 		 JSONObject ret = new JSONObject();
 
 		 int patient_id = Integer.parseInt(patientId);
 		 PreparedStatement prep = null;
 		 ResultSet rs = null;
 		 String updateSql = Patient.INSERT_RECENT_PATIENTS;
 		 try
 		 {
 		 	 dbConn = setConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SELECT_RECENT_PATIENTS);
 
 			 prep.setString(1,userName);
 			 prep.setInt(2,patient_id);
 			 boolean hasValue = false;
 			 rs = prep.executeQuery();
 			 if( rs.next())
 		     {
 				 updateSql = Patient.UPDATE_RECENT_PATIENTS;
 				 hasValue = true;
 		     }
 
 			 prep = dbConn.prepareStatement(updateSql);
 			 //If no current value then insert
 			 if (!hasValue)
 			 {
 				 prep.setString(1,userName);
 				 prep.setInt(2,patient_id);
 			 }
 			 else
 			 {
 				 java.util.Date today_date = new java.util.Date();
 				 prep.setDate(1, new java.sql.Date (today_date.getTime()));
 				 prep.setString(2,userName);
 				 prep.setInt(3,patient_id);
 			 }
 			 prep.executeUpdate();
 
 		 }
 		 catch (SQLException e)
 		 {
 				// TODO Auto-generated catch block
 
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		 }
 		 finally {
 
 		  	closeConnections(dbConn, prep, rs);
 		  	dbConn = null;
 		  	prep = null;
 		  	rs = null;
 		  }
 	     return ret;
 
 	 }
 
 
 	 public static JSONObject getHistory(String patientId, String category,  Date startDate, Date endDate)
 	 {
 		 JSONObject ret = new JSONObject();
 		 PreparedStatement prep = null;
 		 int patient_id = Integer.parseInt(patientId);
        DbConnection dbConn = null;
        ResultSet rs = null;
 		 try
 		 {
 		
 				 dbConn= setConnection();
 			 String sql = Patient.SELECT_PATIENT_HISTORY;
 
 			 if (startDate != null)
 			 {
 				sql = sql +  Patient.SELECT_PATIENT_HISTORY_EXT + SELECT_PATIENT_HISTORY_ORDER_BY;
 				prep = dbConn.prepareStatement(sql);
 
 				prep.setInt(1, patient_id);
 				prep.setString(2, category);
 
 				prep.setDate(3, new java.sql.Date (startDate.getTime()));
 
 				if (endDate == null)
 				{
 					endDate = new java.util.Date();
 					prep.setDate(3, new java.sql.Date (endDate.getTime()));
 
 				}
 			 }
 			 else if (endDate != null)
 			 {
 				 sql = sql +  Patient.SELECT_PATIENT_HISTORY_EXT + SELECT_PATIENT_HISTORY_ORDER_BY;
 
 				 prep = dbConn.prepareStatement(sql);
 				 prep.setInt(1, patient_id);
 				 prep.setString(2, category);
 
 				 Calendar cal = new GregorianCalendar();
 				 cal.set(1920, 1, 1);
 				 prep.setDate(3, new java.sql.Date (cal.getTime().getTime()));
 				 prep.setDate(4, new java.sql.Date (endDate.getTime()));
 
 			 }
 			 else
 			 {
 				 sql = sql + SELECT_PATIENT_HISTORY_ORDER_BY;
 				 prep = dbConn.prepareStatement(sql);
 				 prep.setInt(1, patient_id);
 				 prep.setString(2, category);
 
 			 }
 			 System.out.println("Patient: getPatientHistory : query " + prep.toString());
 
 			 rs = prep.executeQuery();
 			 boolean rtnResults = false;
 
 			 while (rs.next())
 			 {
 				 //SELECT patient_id, history, category_id, history_date, history_notes
 
 				  rtnResults = true;
 
 				  JSONObject o = new JSONObject();
 				  JSONObject o_new = new JSONObject();
 
 			      String history = rs.getString("history");
 			      String history_note = rs.getString("history_notes");
 			      String priority = rs.getString("priority");
 			      String color = rs.getString("color");
 			      category = rs.getString("category");
 			      Date history_date = rs.getDate("history_date");
 
 			      o.put("patient_id", patient_id);
 			      o.put("title", history);
 			      o.put("category", category);
 			      o.put("priority", priority);
 
 			      if (color != null)
 			    	  o.put("color", color);
 
 			      if (history_note != null)
 			    	  o.put("note", history_note);
 
 			      if (history_date != null)
 			    	  o.put("date", history_date);
 
 			      //o_new.put("history", o);
 			      ret.append("patient_history", o);
 		     }
 
 			 if (!rtnResults)
 		      {
 		      	closeConnections(dbConn, prep, rs);
 		      	dbConn = null;
 		      	prep = null;
 		      	rs = null;
 		        	return WebUtils.buildErrorJson( "There is no patient history listed for patient " + patient_id );
 
 		      }
 		 }
 		 catch (SQLException e)
 		 {
 
 				// TODO Auto-generated catch block
 			 return WebUtils.buildErrorJson( "Problem on selecting data from database ." + e.getMessage());
 
 		 } catch (JSONException e) {
 
 			// TODO Auto-generated catch block
 			 return WebUtils.buildErrorJson( "Problem on building JSON Object ." + e.getMessage());
 		}
 		finally {
 
        	closeConnections(dbConn);
        	dbConn = null;
 		   prep = null;
 		   rs = null;
        }
 		 return ret;
 	 }
 
 	 public void parseFullName(String fullName) 
 	 {
 		 if (fullName.contains(","))
 		 {
 			 String[] nameParts = fullName.split(",");
 			 lastName = nameParts[0].trim();
 			 firstName = nameParts[1].trim();
 		 }
 	 }
 	 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public ArrayList<String> getRepositories() {
 		return repositories;
 	}
 
 	public void setRepositories(ArrayList<String> repositories) {
 		this.repositories = repositories;
 	}
 
 	public JSONObject toJSON(int newid, String first_name, String last_name) throws JSONException
 	{
 		JSONObject ret = new JSONObject();
 
 		ret.put(Patient.ID, newid);
         ret.put(Patient.FIRST_NAME, first_name);
         ret.put(Patient.LAST_NAME, last_name);
         return ret;
 
 	}
 
 	public JSONObject toJSON() throws JSONException
 	{
 		JSONObject ret = new JSONObject();
 
 		ret.put(Patient.ID, id);
         ret.put(Patient.FIRST_NAME, firstName);
         ret.put(Patient.LAST_NAME, lastName);
         return ret;
 
 	}
 	
 
 	private class StatementResult
 	{
 		private PreparedStatement prep;
 		private ResultSet rs;
 		
 		public StatementResult(PreparedStatement prep, ResultSet rs)
 		{
 			super();
 			this.prep = prep;
 			this.rs = rs;
 		}
 		public PreparedStatement getPrep()
 		{
 			return prep;
 		}
 		public ResultSet getRS()
 		{
 			return rs;
 		}
 		
 	}
 }
