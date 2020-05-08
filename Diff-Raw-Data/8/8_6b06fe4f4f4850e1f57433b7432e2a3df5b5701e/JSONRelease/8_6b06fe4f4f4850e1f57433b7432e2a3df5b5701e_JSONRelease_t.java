 /************************************************************
 Fileversion: JSONRelease.java
 Author: Christian Heckendorf
 Created date: 10/07/2013
 Purpose: Holds the public fields of a Release
 Feature: None
 ************************************************************/
 package edu.cs673.plm.model;
 
 import java.util.Date;
 
import com.fasterxml.jackson.annotation.JsonFormat;

 public class JSONRelease{
 	long id;
 	String version;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="EST")
 	Date startDate;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="EST")
 	Date endDate;
 
 	/************************************************************
 	Function version: JSONRelease()
 	Author: Christian Heckendorf
 	Created date: 10/07/2013
 	Purpose: Default constructor
 	************************************************************/
 	public JSONRelease(){
 	}
 
 	/************************************************************
 	Function version: JSONRelease()
 	Author: Christian Heckendorf
 	Created date: 10/07/2013
 	Purpose: Constructs a JSONRelease from a Release
 	************************************************************/
 	public JSONRelease(Release release){
 		id = release.getId();
 		version = release.getVersion();
 		startDate = release.getStartDate();
 		endDate = release.getEndDate();
 	}
 
 	/************************************************************
 	Function version: getId
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Returns the release ID
 	************************************************************/
 	public long getId(){
 		return id;
 	}
 
 	/************************************************************
 	Function version: setId
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Sets the release ID
 	************************************************************/
 	public void setId(long id){
 		this.id = id;
 	}
 
 	/************************************************************
 	Function version: getVersion
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Returns the version
 	************************************************************/
 	public String getVersion(){
 		return version;
 	}
 
 	/************************************************************
 	Function version: setVersion
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Sets the version
 	************************************************************/
 	public void setVersion(String version){
 		this.version = version;
 	}
 
 	/************************************************************
 	Function name: getStartDate()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns a startDate
 	************************************************************/
 	public Date getStartDate(){
 		return startDate;
 	}
 
 	/************************************************************
 	Function name: setStartDate()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets a startDate
 	************************************************************/
 	public void setStartDate(Date startDate){
 		this.startDate = startDate;
 	}
 
 	/************************************************************
 	Function name: getEndDate()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns a endDate
 	************************************************************/
 	public Date getEndDate(){
 		return endDate;
 	}
 
 	/************************************************************
 	Function name: setEndDate()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets a endDate
 	************************************************************/
 	public void setEndDate(Date endDate){
 		this.endDate = endDate;
 	}
 }
