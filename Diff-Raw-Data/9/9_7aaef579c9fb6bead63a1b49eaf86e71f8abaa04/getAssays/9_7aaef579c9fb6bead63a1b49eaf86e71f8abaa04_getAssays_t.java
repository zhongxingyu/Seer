 package ctd.services;
 
 import ctd.services.exceptions.Exception400BadRequest;
 import ctd.services.exceptions.Exception403Forbidden;
 import ctd.services.exceptions.Exception500InternalServerError;
 import ctd.services.internal.GscfService;
 import ctd.services.internal.responseComparator;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Taco Steemers
  * @author Tjeerd van Dijk
  */
 public class getAssays {
     private String strStudyToken;
     private String strSessionToken;
 
     /***
      * This function calls GSCF in order to get a list of assayTokens that are
      * available for a certain study.
      * 
      * @return the list of assayTokens, HTML formatted as OPTION's
      *
      * @throws Exception400BadRequest thrown if the sessionToken or studyToken not is set
      * @throws Exception403Forbidden thrown if the sessionToken isn't valide
      * @throws Exception500InternalServerError thrown if something goes wrong
      */
 
     public String getAssays() throws Exception400BadRequest, Exception403Forbidden, Exception500InternalServerError {
         String strReturn = "";
 
         // Check if the minimal parameters are set
         if(getSessionToken()==null){
             Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, "getAssays(): strSessionToken==null");
             throw new Exception400BadRequest();
         }
         if(getStudyToken()==null){
             Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, "getAssays(): strStudyToken==null");
             throw new Exception400BadRequest();
         }
 
         // Create a GSCF service
         GscfService objGSCFService = new GscfService();
 
         // Check if the provided sessionToken is valid
         if(!objGSCFService.isUser(getSessionToken())) {
             Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, "getAssays(): strSessionToken invalid: "+getSessionToken());
             throw new Exception403Forbidden();
         }
 
         // Get all the CTD assays that belong to a certain study
         HashMap<String, String> restParams = new HashMap<String, String>();
         restParams.put("studyToken", getStudyToken());
         LinkedList lstGetAssays = objGSCFService.callGSCF2(getSessionToken(),"getAssays",restParams);
 
         // Sort the assays by name
         Collections.sort(lstGetAssays, new responseComparator("name"));
 
         // Transform all assays into HTML OPTIONs
         strReturn = "<option value='none'>Select an assay...</option>";
         for(int i = 0; i < lstGetAssays.size(); i++){
             HashMap<String, String> map = (HashMap<String, String>) lstGetAssays.get(i);
            strReturn += "<option value='"+map.get("assayToken")+"'>"+map.get("name")+"</option>";
         }
         
         return strReturn;
     }
     /**
      * @return the strSessionToken
      */
     public String getSessionToken() {
         return strSessionToken;
     }
 
     /**
      * @param strSessionToken the strSessionToken to set
      */
     public void setSessionToken(String strSessionToken) {
         this.strSessionToken = strSessionToken;
     }
 
     /**
      * @return the strAssayToken
      */
     public String getStudyToken() {
         return strStudyToken;
     }
 
     /**
      * @param studyToken the strStudyToken to set
      */
     public void setStudyToken(String studyToken) {
         this.strStudyToken = studyToken;
     }
 }
