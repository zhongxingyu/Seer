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
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mitre.medcafe.util.DbConnection;
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
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and first_name like ? ";
 	
 	public static final String SEARCH_USER_PATIENTS_BY_LAST_NAME = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc , patient_repository_assoc " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and last_name like ?";
 
 	public static final String SEARCH_USER_PATIENTS_BY_ALL = "SELECT patient.id, first_name, last_name, role from patient , patient_user_assoc, patient_repository_assoc " +
 	" where patient_user_assoc.patient_id = patient.id and patient_repository_assoc.patient_id = patient.id and patient_user_assoc.username = ? and first_name like ? and first_name like ? ";
 
 	public static final String SEARCH_BY_REPOSITORY = " and patient_repository_assoc.repository = ? ";
 	
 	public static final String SEARCH_PATIENT = "SELECT id, first_name, last_name from patient where  last_name = ? and first_name = ? and rep_patient_id = ? ";
	public static final String SEARCH_PATIENTS_BY_ID = "SELECT DISTINCT id, first_name, last_name, repository from patient, patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and patient.id = ? ";
 	
	public static final String SEARCH_PATIENTS_BY_FIRST_NAME = "SELECT DISTINCT id, first_name, last_name from patient,patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and first_name like ? ";
	public static final String SEARCH_PATIENTS_BY_LAST_NAME = "SELECT DISTINCT id, first_name, last_name from patient, patient_repository_assoc where patient_repository_assoc.patient_id = patient.id and last_name like ? ";
	public static final String SEARCH_PATIENTS_BY_ALL = "SELECT DISTINCT id, first_name, last_name from patient,patient_repository_assoc  where patient_repository_assoc.patient_id = patient.id and last_name like ? and first_name like ?";
 	
 	public static final String SEARCH_PATIENTS_REP_ASSOC_BY_ID = "SELECT id, first_name, last_name, patient_repository_assoc.repository from patient, patient_repository_assoc where " +
 																" patient_repository_assoc.patient_id = patient.id and patient.id = ? and patient_repository_assoc.rep_patient_id = ? and patient_repository_assoc.repository = ? ";
 
 	public static final String SEARCH_PATIENTS_REP_ASSOC_BY_REP_ID = "SELECT id, first_name, last_name, patient_repository_assoc.repository from patient, patient_repository_assoc where " +
 																" patient_repository_assoc.patient_id = patient.id and patient_repository_assoc.rep_patient_id = ? and patient_repository_assoc.repository = ? ";
 
 	public static final String LIST_REP_ASSOC_BY_PATIENT_ID = "SELECT patient_id, rep_patient_id, repository from  patient_repository_assoc where patient_id = ? ";
 
 	public static final String SEARCH_RECENT_PATIENTS = "SELECT patient.id, patient_id,first_name,last_name from patient, recent_patients where patient.id = recent_patients.patient_id and recent_patients.username = ?";
 	public static final String SELECT_RECENT_PATIENTS = "SELECT patient_id from  recent_patients where username = ? and patient_id = ? ";
 	public static final String INSERT_RECENT_PATIENTS = "INSERT INTO recent_patients  (username, patient_id) values ( ?, ?)";
 	public static final String UPDATE_RECENT_PATIENTS = "UPDATE recent_patients SET date_accessed = ? where username = ? and patient_id = ?";
 	
 	public static final String INSERT_PATIENT = "INSERT INTO patient (first_name, last_name, rep_patient_id) values (?,?,?) ";
 	public static final String INSERT_ASSOCIATION = "INSERT INTO patient_user_assoc (patient_id, username, role) values (?,?,?) ";
 	public static final String INSERT_REPOSITORY_ASSOCIATION = "INSERT INTO patient_repository_assoc (patient_id, rep_patient_id,repository) values (?,?,?) ";
 	public static final String FIRST_NAME_TYPE = "first";
 	public static final String LAST_NAME_TYPE = "last";
 	
 	public static final String FIRST_NAME= "first_name";
 	public static final String LAST_NAME = "last_name";
 	public static final String ID = "id";
 	public static final String REP_ID = "patient_rep_id";
 	public static final String NO_PATIENT = "No Patient";
 	private static DbConnection dbConn = null;
 	
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
 	
 	public Patient(DbConnection conn)	
 	{
 		super();
 		dbConn = conn;
 	
 	}
 	
 	public static DbConnection setConnection() throws SQLException
 	{
 		if (dbConn == null)
 			dbConn= new DbConnection();
 		return dbConn;
 	}
 	
 	public static DbConnection getConnection() throws SQLException
 	{
 		return dbConn;
 	}
 	
 	public static void closeConnection() throws SQLException
 	{
 		dbConn.close();
 	}
 	public JSONObject associatePatient( String userName, String patientId, String role)
 	{
 		JSONObject ret = new JSONObject();
 
 		//INSERT_ASSOCIATION = "INSERT INTO patient_user_assoc (patient_id, user_id, role) values (?,?,?) ";
 		String insertQuery = INSERT_ASSOCIATION;
 		int rtn = 0;
 		int patient_id = Integer.parseInt(patientId);
 		String err_mess = "Could not insert association for patient  " + patient_id;
 		if (role == null)
 			role="physician";
 
 		rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , patient_id, userName, role);	
 			
 		if (rtn < 0)
 		{
 			return WebUtils.buildErrorJson( "Problem on creating an association for patient."  + patient_id  );	
 		}
 		
 		return ret;
 		
 	}
 	
 	public JSONObject associatePatientRepository( String patientId, String patient_rep_id, String repository, String userName)
 	{
 		JSONObject ret = new JSONObject();
 		
 		try {
 			if (patientId == null)
 			{
 				ret = checkExists(patient_rep_id, repository);
 				
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
 			
 			
 		} 
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return WebUtils.buildErrorJson( "Error on retrieving patient JSON data "  + firstName + " " + lastName   + " " +  e.getMessage());	
 			
 		}
 		return ret;
 		
 	}
 	
 	public JSONObject checkExists(String patientRepId, String repository)
 	{
 		JSONObject ret = new JSONObject();
 		int patient_rep_id = 0;
 		
 		if (patientRepId != null)
 		{
 			//First create a new patient
 			patient_rep_id = Integer.parseInt(patientRepId);
 		}
 		
 		String err_mess = "Could not check for existence of patient  " + firstName + " " + lastName;
 		
 		//First check that this patient has not already been added
 		String checkPatient = SEARCH_PATIENTS_REP_ASSOC_BY_REP_ID;
 		ResultSet rs = dbConn.psExecuteQuery(checkPatient, err_mess , patient_rep_id, repository);	
 		try 
 		{
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
 		return ret;
 	}
 	
 	
 	public JSONObject insertPatient(  String patientRepId)
 	{
 		JSONObject ret = new JSONObject();
 		int patient_rep_id = 0;
 		
 		if (patientRepId != null)
 		{
 			//First create a new patient
 			patient_rep_id = Integer.parseInt(patientRepId);
 		}
 		int rtn = 0;
 		try 
 		{
 				
 			String insertQuery = INSERT_PATIENT;	
 			String err_mess = "Could not insert patient  " + firstName + " " + lastName;
 			
 			rtn = dbConn.psExecuteUpdate(insertQuery, err_mess , firstName, lastName, patient_rep_id);	
 			
 			if (rtn < 0)
 			{
 				return WebUtils.buildErrorJson( "Problem on inserting patient."  + firstName + " " + lastName  );	
 			}
 			
 			ResultSet rs = dbConn.psExecuteQuery(SEARCH_PATIENT, err_mess, lastName, firstName, patient_rep_id );
 		
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
 		return ret;
 		
 	}
 	public JSONObject isPatient( String userName, String patientId)
 	{
 		JSONObject ret = new JSONObject();
 		String checkQuery = SEARCH_USER_PATIENTS_BY_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 		String err_mess = "Could not check if there is an existing association for patient  " + patient_id;
 		
 		ResultSet rs = dbConn.psExecuteQuery(checkQuery, err_mess , patient_id, userName);	
 		
 		try {
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
 		
 		
 		return ret;
 		
 	}
 	
 	public JSONObject isPatient(String patientId, String patientRepId, String repository)
 	{
 		JSONObject ret = new JSONObject();
 		String checkQuery = SEARCH_PATIENTS_REP_ASSOC_BY_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 		int patient_rep_id = Integer.parseInt(patientRepId);
 		
 		String err_mess = "Could not check if there is an existing repository association for patient  " + patient_id;
 		
 		ResultSet rs = dbConn.psExecuteQuery(checkQuery, err_mess , patient_id, patient_rep_id,repository );	
 		
 		try {
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
 		
 		
 		return ret;
 		
 	}
 	
 	public HashMap<String,String> listRepositories(String patientId)
 	{
 		HashMap<String,String> ret = new HashMap<String,String>();
 		
 		//SELECT patient_id, rep_patient_id, repository from  patient_repository_assoc where patient_id = ? ";
 
 		String listQuery = LIST_REP_ASSOC_BY_PATIENT_ID;
 
 		int patient_id = Integer.parseInt(patientId);
 		
 		String err_mess = "Could not check the list of repositories for patient " + patient_id;
 		
 		ResultSet rs = dbConn.psExecuteQuery(listQuery, err_mess , patient_id );	
 		boolean results = false;
 		try {
 			while (rs.next())
 			{
 				  patient_id = rs.getInt(1);
 				  String rep = rs.getString("repository");
 				  int repId = rs.getInt("rep_patient_id");
 		          ret.put(rep, repId +"");
 		          results = true;
 			}
 			
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return null;
 		} 
 		return ret;
 		
 	}
 	 public JSONObject searchJson(String isPatient, String searchStringFirst, String searchStringLast, String userName, String server){
 	        
 		 	boolean rtnResults = false;
 		 	JSONObject ret = new JSONObject();
 	        
 	        try
 	        {
 	        	
 	        	ResultSet rs = getPatients( isPatient, searchStringFirst, searchStringLast, userName, server);
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
 	        
 	        if (!rtnResults)
 	        {
 	        	return WebUtils.buildErrorJson( "There are no patients currently listed for First Name " + searchStringFirst + " and Last Name " + searchStringLast );
 	      	  
 	        }
 	        return ret ;
 	    }
 	
 	 private ResultSet getPatients(String isPatient, String searchStringFirst, String searchStringLast, String userName, String server) throws SQLException
 	 {
 		 
 		 setConnection();
 		 
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
 		 ResultSet rs = prep.executeQuery();
 			
 	     return rs;
 			
 	 }
 
 	 
 	 public static JSONObject getPatient(int id,  DbConnection dbConn) 
 	 {
 			
 		
 		 System.out.println("Patient: getPatients : got connection " );
 		 boolean rtnResults = false;
 		 JSONObject ret = new JSONObject();
 		 	
 		 PreparedStatement prep;
 		 try 
 		 {
 			 if (dbConn == null)
 				 dbConn= new DbConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SEARCH_PATIENTS_BY_ID);
 			
 			 prep.setInt(1,id);
 			 ResultSet rs = prep.executeQuery();
 			 while( rs.next())
 		     {
 			        //convert to JSON		        
 			        rtnResults = true;
 
 			        String fName = rs.getString("first_name");
 			        String lName = rs.getString("last_name");
 			        ret.put(Patient.ID, id);
 			        ret.put(Patient.FIRST_NAME, fName);
 			        ret.put(Patient.LAST_NAME, lName);
 		     }    
 			 
 			 if (!rtnResults)
 		      {
 		        	return WebUtils.buildErrorJson( "There are no patients currently listed for patient id " + id );
 		      	  
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
 	     return ret;
 			
 	 }
 
 	 public static JSONObject getRecentPatients(String userName)
 	 {
 		 System.out.println("Patient: getRecentPatients : got connection " );
 		 boolean rtnResults = false;
 		 JSONObject ret = new JSONObject();
 		 	
 		 PreparedStatement prep;
 		 try 
 		 {
 			 if (dbConn == null)
 				 dbConn= new DbConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SEARCH_RECENT_PATIENTS);
 			
 			 prep.setString(1,userName);
 			 ResultSet rs = prep.executeQuery();
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
 	     return ret;
 		 
 	 }
 	 
 	 public static JSONObject addRecentPatients(String userName, String patientId )
 	 {
 		 System.out.println("Patient: addRecentPatients : got connection " );
 		 
 		 JSONObject ret = new JSONObject();
 		 	
 		 int patient_id = Integer.parseInt(patientId);
 		 PreparedStatement prep;
 		 String updateSql = Patient.INSERT_RECENT_PATIENTS;
 		 try 
 		 {
 			 if (dbConn == null)
 				 dbConn= new DbConnection();
 
 			 prep = dbConn.prepareStatement(Patient.SELECT_RECENT_PATIENTS);
 			
 			 prep.setString(1,userName);
 			 prep.setInt(2,patient_id);
 			 boolean hasValue = false;
 			 ResultSet rs = prep.executeQuery();
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
 		
 	     return ret;
 		 
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
 }
