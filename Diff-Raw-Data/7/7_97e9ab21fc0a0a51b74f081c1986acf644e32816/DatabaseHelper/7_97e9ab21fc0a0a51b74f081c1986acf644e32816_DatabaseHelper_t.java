 /**
  * A helper class designed to make accessing the crime table within the
  * database in program easier. 
  * This classes methods should be used exclusively to interact with the 
  * database to ensure thread safety. Only one connection can write to the
  * database at a time, thus once a connection is opened the entire database 
  * locks until it is closed. 
  *
  */
 package utilities;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import boloTab.Bolo;
 
 public class DatabaseHelper {	
 //-----------------------------------------------------------------------------
 	/**
 	 * Adds a new crime to the crime table in the database. 
 	 * This method adds the most basic data that every crime entry must have.
 	 * @param caseNum - the case number associated with the crime
 	 * @param location - the location of the crime
 	 * @param offCode - the offense code of the crime
 	 * @param incident - the incident associated with the crime
 	 * @param time - the time of the crime occurrence
 	 * @param date - the date of the crime occurrence 
 	 * @param remarks - remarks associated with the crime
 	 * @param status - the crime's status
 	 * @throws Exception
	 *///
 	public void addBOLO(String age,String race,String sex,String height,String weight,String build,
 			String eyes,String hair,Date incidentDate,String reference,String caseNum,String status,
 			String weapon,String preparedBy,String approvedBy,Date prepDate, String otherDescrip,
 			String narrative) throws Exception{
 		long incidentEpoch, prepEpoch;
 		//Create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 	    Connection conn = DriverManager.getConnection("jdbc:sqlite:umpd.db");
 	
 	    //Create a prepared statement to add the crime data
 	    PreparedStatement prep = conn.prepareStatement(
 	      "INSERT into bolo(age, race, sex, height, weight, build, eyes, hair," +
 	      " epochTime, reference, caseNum, status, weapon, prepedBy, approvedBy, prepdate," +
 	      " description, narrative)" + 
 	      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
 	
 	    incidentEpoch = convertDateToEpoch(incidentDate);
 	    prepEpoch = convertDateToEpoch(prepDate);
 	    
 	    //Add the data to the prepared statement
 	    prep.setString(1, age);
 	    prep.setString(2, race);
 	    prep.setString(3, sex);
 	    prep.setString(4, height);
 	    prep.setString(5, weight);
 	    prep.setString(6, build);
 	    prep.setString(7, eyes);
 	    prep.setString(8, hair);
 	    prep.setLong(9, incidentEpoch);
 	    prep.setString(10, reference);
 	    prep.setString(11, caseNum);
 	    prep.setString(12, status);
 	    prep.setString(13, weapon);
 	    prep.setString(14, preparedBy);
 	    prep.setString(15, approvedBy);
 	    prep.setLong(16, prepEpoch);
 	    prep.setString(17, otherDescrip);
 	    prep.setString(18, narrative);
 	    prep.addBatch();
 	
 	    //Create new row in the table for the data
 	    conn.setAutoCommit(false);
 	    prep.executeBatch();
 	    conn.setAutoCommit(true);
 	    
 	    //Close the connection
 	    conn.close();
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Retrives all the BOLOs from the database and places them into an 
 	 * <code>Arraylist</code> of BOLO objects, which is returned to the 
 	 * caller.
 	 * @return an arraylist of BOLO objects
 	 * @throws Exception
 	 */
 	public static ArrayList<Bolo> getBOLOsFromDB() throws Exception{
 		ArrayList<Bolo> boloList = new ArrayList<Bolo>();
 		String age, race, sex, height, weight, build, eyes, hair;
 		String reference, caseNum, status, weapon;
 		String preparedBy, approvedBy;
 		String otherDescrip, narrative, photoPath, videoPath;
 		long incidentDate=0, prepDate=0;
 		 
 		//Create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 		
 		//test to make database file access syst indep
 		Path dbFilePath = Paths.get("Database", "umpd.db");
 		String dbFileName = dbFilePath.toString();
 	    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
 	    
 	    Statement stat = conn.createStatement();
 	    ResultSet allBOLOs = stat.executeQuery("SELECT * FROM bolo;");
 
 	    Bolo bolo;
 	    while (allBOLOs.next()) {
 	    	bolo = new Bolo();
 	        
 	    	bolo.setBoloID(allBOLOs.getInt("bolo_id"));
 	    	
 	        age = allBOLOs.getString("age");
 	        if(age!=null){ bolo.setAge(age); }
 			race = allBOLOs.getString("race");
 			if(race!=null){ bolo.setRace(race); }
 			sex = allBOLOs.getString("sex");
 			if(sex!=null){ bolo.setSex(sex); }
 			height = allBOLOs.getString("height");
 			if(height!=null){ bolo.setHeight(height); }
 			weight = allBOLOs.getString("weight");
 			if(weight!=null){ bolo.setWeight(weight); }
 			build=allBOLOs.getString("build");
 			if(build!=null){ bolo.setBuild(build); }
 			eyes=allBOLOs.getString("eyes");
 			if(eyes!=null){ bolo.setEyes(eyes); }
 			hair=allBOLOs.getString("hair");
 			if(hair!=null){ bolo.setHair(hair); }
 			reference=allBOLOs.getString("reference");
 			if(reference!=null){ bolo.setReference(reference); }
 			caseNum=allBOLOs.getString("caseNum");
 			if(caseNum!=null){ bolo.setCaseNum(caseNum); }
 			status=allBOLOs.getString("status");
 			if(status!=null){ bolo.setStatus(status); }
 			weapon=allBOLOs.getString("weapon");
 			if(weapon!=null){ bolo.setWeapon(weapon); }
 			preparedBy= allBOLOs.getString("prepedBy");
 			if(preparedBy!=null){ bolo.setPreparedBy(preparedBy); }
 			approvedBy= allBOLOs.getString("approvedBy");
 			if(approvedBy!=null){ bolo.setApprovedBy(approvedBy); }
 			otherDescrip= allBOLOs.getString("description");
 			if(otherDescrip!=null){ bolo.setOtherDescrip(otherDescrip); }
 			narrative=allBOLOs.getString("narrative");
 			if(narrative!=null){ bolo.setNarrative(narrative); }
 			incidentDate = allBOLOs.getLong("incidentDate");
 			if(incidentDate!=0){ bolo.setincidentDate(incidentDate); }
 			prepDate = allBOLOs.getLong("prepdate");
 			if(prepDate!=0){ bolo.setprepDate(prepDate); }
 			
 			photoPath=allBOLOs.getString("photoPath");
 			if(photoPath!=null){ 
 				Path pp = Paths.get(photoPath);
 				bolo.setPhotoFilePath(pp);
 //DEBUG---------------------------------------------------------
 			} else { 
 				System.out.printf("\n photo path is null\n");
 			}
 //---------------------------------------------------------DEBUG				
 			videoPath=allBOLOs.getString("videoPath");
 			if(videoPath!=null){ 
 				Path vp = Paths.get(videoPath);
 				bolo.setVideoFilePath(vp);
 //DEBUG---------------------------------------------------------			
 			} else { 
 
 				System.out.printf("\n photo path is null\n");
 			}
 
 			boloList.add(bolo);
 	    }
 //---------------------------------------------------------DEBUG	    
 	    	
 	    //Close the connections
 	    allBOLOs.close();
 	    conn.close();
 	    
 	    return boloList;
 	}
 //-----------------------------------------------------------------------------
 	public void addRollCall(String name, String present, String comment, 
 			String timeArrived, String shiftDate) throws Exception {
 		
 				//Create the connection to the database
 				Class.forName("org.sqlite.JDBC");
				Path dbFilePath = Paths.get("Database", "umpd.db");
				String dbFileName = dbFilePath.toString();
			    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
 			    
 			    //insert into person table
 			    PreparedStatement personStatement = conn.prepareStatement(
 			    		"INSERT into RollCall(Name, Present, Comment, TimeArrived, ShiftDate) " +
 			    		"VALUES(?,?,?,?,?);"
 			        );
 			    
 			    //long shiftDateEpoch = convertDateToEpoch(shiftDate);
 			    
 			    personStatement.setString(1,name);
 			    personStatement.setString(2,present);
 			    personStatement.setString(3,comment);
 			    personStatement.setString(4,timeArrived);
 			    personStatement.setString(5,shiftDate);
 			    personStatement.addBatch();
 			    
 			  //Create new row in the table for the data
 			    conn.setAutoCommit(false);
 			    personStatement.executeBatch();
 			    conn.setAutoCommit(true);
 			    
 			    //Close the connection
 			    conn.close();
 	}
 //-----------------------------------------------------------------------------
 	/*
 	 * Wrote this code when thinking I needed a many to many relational database
 	 * probably unnecessary now, keeping it just in case
 	 * -BL
 	 */
 	
 	/*
 	public static void addShift(int shiftNumber, String date) throws Exception {
 		//Create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 	    Connection conn = DriverManager.getConnection("jdbc:sqlite:umpd.db");
 	    
 	    //insert into shift table
 	    PreparedStatement shiftStatement = conn.prepareStatement(
 	    		"INSERT into Person(ShiftNumber, Date) " +
 	    		"VALUES(?,?);"
 	        );
 	    shiftStatement.setInt(1,shiftNumber);
 	    shiftStatement.setString(2,date);
 	    shiftStatement.addBatch();
 	    
 	  //Create new row in the table for the data
 	    conn.setAutoCommit(false);
 	    shiftStatement.executeBatch();
 	    conn.setAutoCommit(true);
 	    
 	    //Close the connection
 	    conn.close();
 	}
 //-----------------------------------------------------------------------------
 	public static void addShiftPerson(int shiftNumber, int Cnumber) throws Exception {
 		//Create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 	    Connection conn = DriverManager.getConnection("jdbc:sqlite:umpd.db");
 	    
 	    //insert into shift table
 	    PreparedStatement shiftPersonStatement = conn.prepareStatement(
 	    		"INSERT into Person(ShiftNumber, Cnumber) " +
 	    		"VALUES(?,?);"
 	        );
 	    shiftPersonStatement.setInt(1,shiftNumber);
 	    shiftPersonStatement.setInt(2,Cnumber);
 	    shiftPersonStatement.addBatch();
 	    
 	  //Create new row in the table for the data
 	    conn.setAutoCommit(false);
 	    shiftPersonStatement.executeBatch();
 	    conn.setAutoCommit(true);
 	    
 	    //Close the connection
 	    conn.close();
 	}
 	*/
 //-----------------------------------------------------------------------------
 
 	/**
 	 * Converts a <code>Date</code> object into a <code>long</code> representing
 	 * the number of seconds elapsed since the epoch. 
 	 * @param date - <code>Date</code> object to convert
 	 * @return <code>long</code> value representing time in seconds since epoch
 	 */
 	public static long convertDateToEpoch(Date date){
 		long epoch = (date.getTime() / 1000);
 		return epoch;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Converts a <code>long</code> value representing time in seconds since 
 	 * epoch into a <code>Date</code> object representing the same time. 
 	 * @param epoch - time in seconds since epoch
 	 * @return <code>Date</code> object representing the given time
 	 */
 	public static Date convertEpochToDate(long epoch){
 		Date date = new Date(epoch*1000);
 		return date;
 	}
 //-----------------------------------------------------------------------------
 }
