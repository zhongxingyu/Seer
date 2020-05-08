 package com.coverity.scan.hudson;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import hudson.model.Action;
 
 public class ScanPluginReport implements Action {
 
 	private boolean dataUpdated=false;
 	private String buildNumber;
 	private String projectName;
 	private String username;
 	private String password;
 	
 	public ScanPluginReport(String theProjectName, String theBuildNumber, String theUsername, String thePassword) {
 		buildNumber=theBuildNumber;
 		projectName=theProjectName;
 		username=theUsername;
 		password=thePassword;
 		dataUpdated=true;		
 	}
 	
     /**
      * {@inheritDoc}
      */
 	public String getIconFileName() {
 		 return ScanPluginConfiguration.ICON_FILE_NAME;
 	}
 
     /**
      * {@inheritDoc}
      */
 	public String getDisplayName() {
 		return ScanPluginConfiguration.DISPLAY_NAME;
 	}
 
     /**
      * {@inheritDoc}
      */
 	public String getUrlName() {
 		return ScanPluginConfiguration.URL;
 	}
 
     /**
      * Returns no data yet message
      */
 	public String getNoDataYet() {
 		return ScanPluginConfiguration.NO_DATA_YET;
 	}
 	
     /**
      * Returns yes data message
      */
 	public String getYesData() {
 		return ScanPluginConfiguration.YES_DATA;
 	}
 	
     /**
      * Returns <code>true</code> if Analysis data has already been obtained.
      *
      * @return Value for property 'dataUpdated'.
      */
     public boolean isDataUpdated() {
     	return dataUpdated;
     }
 	
     /**
      * Returns build number for this report
      */
 	public String getBuildNumber() {
 		return buildNumber;
 	}    
     
     /**
      * Returns the project name for this report
      */
 	public String getProjectName() {
 		return projectName;
 	}
 	
     /**
      * Returns the project name for this report
      */
 	public String getUsername() {
 		return projectName;
 	}
 	
     /**
      * Returns the project name for this report
      */
 	public String getPassword() {
 		return projectName;
 	}
 	
     /**
      * Returns the project name for this report
      */
 	public String getReport() {
 	       URL submitURL;
 	   		HttpURLConnection connection = null;  
 	   		
 	   		String urlParameters = "username="+ScanPluginConfiguration.encodeUTF8(getUsername());
 	   		urlParameters += "&password="+ScanPluginConfiguration.encodeUTF8(getPassword());
 	   		urlParameters += "&project="+ScanPluginConfiguration.encodeUTF8(getProjectName());
	   		urlParameters += "&email="+ScanPluginConfiguration.encodeUTF8(getBuildNumber());
 	    	try {
 	      	    //Create connection
 	      		submitURL = new URL(ScanPluginConfiguration.REPORT_URL);
 	            connection = (HttpURLConnection)submitURL.openConnection();
 	            connection.setRequestMethod("POST");
 	            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 				connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
 	            connection.setRequestProperty("Content-Language", "en-US");  
 	      		connection.setUseCaches (false);
 	      		connection.setDoInput(true);
 	      		connection.setDoOutput(true);
 				
 	      		//Send request
 	      		DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
 	     		wr.writeBytes (urlParameters);
 	      		wr.flush ();
 	      		wr.close ();
 
 	      		//Get Response	
 	      		InputStream is = connection.getInputStream();
 	      		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 	      		String line;
 	      		StringBuffer response = new StringBuffer(); 
 	      		while((line = rd.readLine()) != null) {
 	        	response.append(line);
 	        	response.append('\r');
 	      		}
 	      		rd.close();
 	      		return response.toString();
 	    	} catch (Exception e) {
 	    		Logger.getLogger(ScanPluginReport.class.getName()).log(Level.SEVERE, null, e);
 	      		return "Failed to obtain the report from Coverity";
 	    	} finally {
 	      		if(connection != null) connection.disconnect(); 
 	    	}
 	}	
 	
 }
