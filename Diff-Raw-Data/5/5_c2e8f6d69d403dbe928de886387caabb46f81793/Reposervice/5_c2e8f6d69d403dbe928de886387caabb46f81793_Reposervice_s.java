 package org.bitrepository.webservice;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.bitrepository.BasicClient;
 import org.bitrepository.BasicClientFactory;
 import org.bitrepository.GetFileIDsResults;
 
 /**
  * The class exposes the REST webservices provided by the Bitrepository-webclient using Jersey. 
  */
 
 @Path("/reposervice")
 public class Reposervice {
     
     private BasicClient client;
     
     
     public Reposervice() {
         client = BasicClientFactory.getInstance();
     }
     
     /**
      * putFile exposes the possibility of uploading a file to the bitrepository collection that the webservice 
      * is configured to use. The three parameters are all mandatory.
      * @param fileID Filename of the file to be put in the bitrepository. 
      * @param fileSize Size of the file en bytes
      * @param url Place where the bitrepository pillars can fetch the file from 
      * @return A string indicating if the request was successfully started or has been rejected. 
      */
     @GET
     @Path("/putfile/")
     @Produces("text/plain")
     public String putFile(
             @QueryParam("fileID") String fileID,
             @QueryParam("fileSize") long fileSize,
             @QueryParam("url") String URL,
             @QueryParam("putChecksum") String putChecksum,
             @QueryParam("putChecksumType") String putChecksumType,
             @QueryParam("putSalt") String putSalt,
             @QueryParam("approveChecksumType") String approveChecksumType,
             @QueryParam("approveSalt") String approveSalt) {
         try {
         WebserviceInputChecker.checkFileIDParameter(fileID);
         WebserviceInputChecker.checkURLParameter(URL);
         WebserviceInputChecker.checkChecksumParameter(putChecksum);
         WebserviceInputChecker.checkSaltParameter(putSalt);
         WebserviceInputChecker.checkSaltParameter(approveSalt);
         String approveChecksumTypeStr = null;
         if(approveChecksumType != null && !approveChecksumType.equals("disabled")) {
             approveChecksumTypeStr = approveChecksumType;
         }
         return client.putFile(fileID, fileSize, URL, putChecksum, putChecksumType, putSalt, 
         		approveChecksumTypeStr, approveSalt);    
         } catch (WebserviceInputCheckException e) {
             return e.getMessage();
         }
     }
     
     /**
      * getFile exposes the possibility of downloading a file from the bitrepository collection that the webservice 
      * is configured to use. The two parameters are all mandatory.
      * @param fileID Filename of the file to be downloaded in the bitrepository. 
      * @param url Place where the bitrepository pillars can upload the file to. 
      * @return A string indicating if the request was successfully started or has been rejected. 
      */
     @GET
     @Path("/getfile/")
     @Produces("text/plain")
     public String getFile(
             @QueryParam("fileID") String fileID,
             @QueryParam("url") String URL) {
         try {
             WebserviceInputChecker.checkFileIDParameter(fileID);
             WebserviceInputChecker.checkURLParameter(URL);
             return client.getFile(fileID, URL);
         } catch (WebserviceInputCheckException e) {
             return e.getMessage();
         }            
     }
     
     /**
      * Lists files that has completed upload from the collection. 
      */
     @GET
     @Path("/getfile/getCompletedFiles/")
     @Produces("text/html")
     public String getCompletedFiles() {
         return client.getCompletedFiles();
     }
     
     /**
      * getLog gets the log of events that has happened since the webclient were started. The log contains a textual description 
      * of all events that has occurred, both successes and failures.  
      * @return The log in a textual format. 
      */
     @GET
     @Path("/getLog")
     @Produces("text/plain")
     public String getLog() {
         return client.getLog();
     }
     
     /**
      * getHtmlLog gets the log of events that has happened since the webclient were started. The log contains a textual description 
      * of all events that has occurred, both successes and failures.  
      * @return The log is formatted with HTML. 
      */
     @GET
     @Path("/getHtmlLog")
     @Produces("text/html")
     public String getHtmlLog() {
         return client.getHtmlLog();
     }
     
     /**
      * getShortHtmlLog gets the latests 25 log entries in reverse order. The log contains a textual description 
      * of all events that has occurred, both successes and failures.  
      * @return The log is formatted with HTML. 
      */
     @GET
     @Path("/getShortHtmlLog")
     @Produces("text/html")
     public String getShortHtmlLog() {
     	return client.getShortHtmlLog();
     }
     
     /**
      * getSettingsSummary provides a summary of some important settings of the Bitrepository collection, herein:
      * - The message bus which that is communicated with
      * - The Pillars in the collection
      * - The Bitrepository collection ID
      * @return The current settings formatted as HTML 
      */
     @GET
     @Path("/getSettingsSummary")
     @Produces("text/plain")
     public String getSummarySettings() {
         return client.getSettingsSummary();
     }
     
     /**
      * getChecksumsHtml exposes the possibility of requesting checksums for the files present in the Bitrepository.
      * The two first parameters are mandatory.
      * @param fileIDs List of filenames to get checksums for. FileIDs should be seperated by a '\n'
      * @param checksumType The type of checksum algorithm that the requested checksum should be in.
      * 			The type needs to be one supported by all pillars in the collection. 
      * @param salt A string to alter the preconditions of calculating a checksum. Will result in the returned checksum
      * 			being of type hmac:<checksumType>. The salt parameter is optional. 
      * @return A HTML page containing a table of the requested fileIDs and their checksums, or an error message.
      */
     @GET
     @Path("getChecksumsHtml")
     @Produces("text/html")
     public String getChecksumsHtml(
     		@QueryParam("fileID") String fileID,
     		@QueryParam("checksumType") String checksumType,
     		@QueryParam("salt") String salt) {
         try {
             WebserviceInputChecker.checkFileIDParameter(fileID);
             WebserviceInputChecker.checkChecksumTypeParameter(checksumType);
         } catch (WebserviceInputCheckException e) {
             return "<html><body><b>" + e.getMessage() + "</b></body></html>";
         }
         
     	Map<String, Map<String, String>> result = client.getChecksums(fileID, checksumType, salt);
     	if(result == null) {
     		return "<html><body><b>Failed!</b></body></html>";
     	}
     	StringBuilder sb = new StringBuilder();
     	sb.append("<html><body><table>");
     	Set<String> returnedFileIDs = result.keySet();
     	ArrayList <String> pillarIDList = new ArrayList<String>();
     	
     	sb.append("<tr> <td><b>File Id:</b></td><td>&nbsp;</td>");
     	for(String fileId : returnedFileIDs) {
     		Set<String> pillarIDs = result.get(fileId).keySet();
     		for(String pillarID : pillarIDs) {
     			pillarIDList.add(pillarID);
     			sb.append("<td><b>Checksums from " + pillarID + ":</b></td>");
     		}
     		break;
     	}
     	sb.append("</tr>");
     	for(String fileId : returnedFileIDs) {
     		sb.append("<tr> <td> " + fileId + "</td><td>&nbsp;</td>"); 
     		for(String pillarID : pillarIDList) {
     			if(result.get(fileId).containsKey(pillarID)) {
     				sb.append("<td> " + result.get(fileId).get(pillarID) + " </td>");	
     			} else {
     				sb.append("<td> unknown </td>");
     			}
     		}
     		sb.append("</tr>");
     	}
     	sb.append("</table></body></html>");
     	
     	return sb.toString();
     }
     
     /**
      * getChecksums exposes the possibility of requesting checksums for the files present in the Bitrepository.
      * The two first parameters are mandatory.
      * @param fileIDs List of filenames to get checksums for. FileIDs should be seperated by a '\n'
      * @param checksumType The type of checksum algorithm that the requested checksum should be in.
      * 			The type needs to be one supported by all pillars in the collection. 
      * @param salt A string to alter the preconditions of calculating a checksum. Will result in the returned checksum
      * 			being of type hmac:<checksumType>. The salt parameter is optional. 
      * @return A tab separated table containing the requested fileIDs and their checksums, or an error message.
      */
     @GET
     @Path("getChecksums")
     @Produces("text/plain")
     public String getChecksums(
     		@QueryParam("fileID") String fileID,
     		@QueryParam("checksumType") String checksumType,
     		@QueryParam("salt") String salt) {
         try {
             WebserviceInputChecker.checkFileIDParameter(fileID);
             WebserviceInputChecker.checkChecksumTypeParameter(checksumType);
         } catch (WebserviceInputCheckException e) {
             return e.getMessage();
         }
 
     	Map<String, Map<String, String>> result = client.getChecksums(fileID, checksumType, salt);
     	if(result == null) {
     		return "Failed!";
     	}
     	StringBuilder sb = new StringBuilder();
     	Set<String> returnedFileIDs = result.keySet();
     	ArrayList <String> pillarIDList = new ArrayList<String>();
     	
     	sb.append("FileID \t");
     	for(String fileId : returnedFileIDs) {
     		Set<String> pillarIDs = result.get(fileId).keySet();
     		for(String pillarID : pillarIDs) {
     			pillarIDList.add(pillarID);
     			sb.append(pillarID + "\t");
     		}
     		break;
     	}
     	sb.append("\n");
     	for(String fileId : returnedFileIDs) {
     		sb.append(fileId + "\t"); 
     		for(String pillarID : pillarIDList) {
     			if(result.get(fileId).containsKey(pillarID)) {
     				sb.append(result.get(fileId).get(pillarID) + "\t");	
     			} else {
     				sb.append("unknown \t");
     			}
     		}
     		sb.append("\n");
     	}
     	return sb.toString();
     }
 
     /**
      * getFileIDsHtml exposes the possibility of requesting listing of files present in the Bitrepository.
      * Of the two parameters at least one may not be empty. 
      * @param fileIDs List of filenames to be listed. FileIDs should be seperated by a '\n'
      * @param allFileIDs Boolean indicating to get a list of all files in the Bitrepository collection. 
      * 			Setting this will override any files set in by the fileIDs parameter. 
      * @return A HTML page containing a table containing the requested fileIDs and which pillars have answered.
      * 			The entry of each fileID is color coded to indicate whether all pillars have answered on that particular file.  
      * 			In case of an error, an error message is returned instead.
      */
     @GET
     @Path("getFileIDsHtml")
     @Produces("text/html")
     public String getFileIDsHtml(
     		@QueryParam("fileIDs") String fileIDs,
     		@QueryParam("allFileIDs") boolean allFileIDs) {
     	GetFileIDsResults results = client.getFileIDs(fileIDs, allFileIDs);
     	if(results.getResults() == null) {
     		return "<p>Get file ID's provided no results.</p>";
     	}
     	StringBuilder sb = new StringBuilder();
     	sb.append("<html><head><style> #good{background-color:#31B404;} #bad{background-color:#B40404;} " +
     			"td{padding: 5px;}</style></head><body>"); 
     	
     	sb.append("<table> <tr valign=\"top\"> \n <td>");
     	sb.append("<table> <tr> <th> <b>File Id:</b> </th> <th>&nbsp;&nbsp;" +
     			"</th><th><b>Number of answers </b></th></tr>");
     	Set<String> IDs = results.getResults().keySet();
     	String status;
     	for(String ID : IDs) {
     		if(results.getResults().get(ID).size() == results.getPillarList().size()) {
     			status = "good";
     		} else {
     			status = "bad";
     		}
     		sb.append("<tr><td id=" + status + ">" + ID + "</td><td></td><td>" + 
     				results.getResults().get(ID).size() +"</td></tr>");
     	}
     	sb.append("</table> </td> <td>&nbsp;&nbsp;</td><td><table><tr> <th> <b> Pillar list</b> </th> </tr>");
     	for(String pillar : results.getPillarList()) {
     		sb.append("<tr><td>" + pillar + "</td></tr>");
     	}
     	sb.append("</table></td> </tr> </table></body></html>");
     	
     	return sb.toString();
     }
     
     @GET
     @Path("deleteFile")
     @Produces("text/html")
     public String deleteFile(
             @QueryParam("fileID") String fileID, @QueryParam("pillarID") String pillarID,
             @QueryParam("deleteChecksum") String deleteChecksum, 
             @QueryParam("deleteChecksumType") String deleteChecksumType,
             @QueryParam("deleteChecksumSalt") String deleteChecksumSalt,
             @QueryParam("approveChecksumType") String approveChecksumType,
             @QueryParam("approveChecksumSalt") String approveChecksumSalt) {
         try {
             WebserviceInputChecker.checkFileIDParameter(fileID);
             WebserviceInputChecker.checkPillarIDParameter(pillarID);
         } catch (WebserviceInputCheckException e) {
             return e.getMessage();
         }
 
         return client.deleteFile(fileID, pillarID, deleteChecksum, deleteChecksumType, deleteChecksumSalt, 
                 approveChecksumType, approveChecksumSalt);
     }
 
     @GET
     @Path("replaceFile")
     @Produces("text/html")
     public String replaceFile(
             @QueryParam("fileID") String fileID, @QueryParam("pillarID") String pillarID,
             @QueryParam("oldFileChecksum") String oldFileChecksum, 
             @QueryParam("oldFileChecksumType") String oldFileChecksumType,
             @QueryParam("oldFileChecksumSalt") String oldFileChecksumSalt,
             @QueryParam("oldFileRequestChecksumType") String oldFileRequestChecksumType,
             @QueryParam("oldFileRequestChecksumSalt") String oldFileRequestChecksumSalt,
             @QueryParam("url") String url, @QueryParam("fileSize") String fileSize,  
             @QueryParam("newFileChecksum") String newFileChecksum, 
             @QueryParam("newFileChecksumType") String newFileChecksumType,
             @QueryParam("newFileChecksumSalt") String newFileChecksumSalt,
             @QueryParam("newFileRequestChecksumType") String newFileRequestChecksumType,
             @QueryParam("newFileRequestChecksumSalt") String newFileRequestChecksumSalt) {
         try {
             WebserviceInputChecker.checkFileIDParameter(fileID);
             WebserviceInputChecker.checkPillarIDParameter(pillarID);
             WebserviceInputChecker.checkURLParameter(url);
             WebserviceInputChecker.checkChecksumTypeParameter(oldFileChecksumType);
             WebserviceInputChecker.checkChecksumParameter(oldFileChecksum);
             WebserviceInputChecker.checkSaltParameter(oldFileChecksumSalt);
            WebserviceInputChecker.checkChecksumTypeParameter(oldFileRequestChecksumType);
             WebserviceInputChecker.checkSaltParameter(oldFileRequestChecksumSalt);
             WebserviceInputChecker.checkChecksumTypeParameter(newFileChecksumType);
             WebserviceInputChecker.checkChecksumParameter(newFileChecksum);
             WebserviceInputChecker.checkSaltParameter(newFileChecksumSalt);
             WebserviceInputChecker.checkSaltParameter(newFileRequestChecksumSalt);
            WebserviceInputChecker.checkChecksumTypeParameter(newFileRequestChecksumType);
         } catch (WebserviceInputCheckException e) {
             return e.getMessage();
         }
         return client.replaceFile(fileID, pillarID, oldFileChecksum, oldFileChecksumType, oldFileChecksumSalt, 
         		oldFileRequestChecksumType, oldFileRequestChecksumSalt, url, Long.parseLong(fileSize), newFileChecksum, 
         		newFileChecksumType, newFileChecksumSalt, newFileRequestChecksumType, newFileRequestChecksumSalt);
     }
 
 }
