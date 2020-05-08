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
  * @author Tjeerd van Dijk
  * @author Taco Steemers
  */
 public class getStudies {
     private String strAssayToken;
     private String strSessionToken;
 
     /***
      * This function generates a sting containing all the OPTIONs that are shown
      * in step 2 of the upload process.
      * 
      * @return The string containing the OPTIONs
      * @throws Exception400BadRequest returned if the sessiontoken is not set
      * @throws Exception403Forbidden returned if the user isn't logged in
      * @throws Exception500InternalServerError returned if there is some error
      */
 
     public String getStudies() throws Exception400BadRequest, Exception403Forbidden, Exception500InternalServerError {
         String strReturn = "";
 
         // Check if the minimal parameters are set
         if(getSessionToken()==null){
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getStudies(): strSessionToken==null");
             throw new Exception400BadRequest();
         }
 
         // init a GSCF service
         GscfService objGSCFService = new GscfService();
 
         // Check if the provided sessionToken is valid
         if(!objGSCFService.isUser(getSessionToken())) {
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getStudies(): strSessionToken invalid: "+getSessionToken());
             throw new Exception403Forbidden();
         }
 
         // Get all assays that are available to the user from GSCF
         LinkedList lstGetAssays = objGSCFService.callGSCF2(getSessionToken(),"getAssays",null);
 
         // Collect all the studyTokens in order to call CGSF for more information
         String strStudyCall = "";
         for(int i = 0; i < lstGetAssays.size(); i++){
             HashMap<String, String> mapTokens = (HashMap<String, String>) lstGetAssays.get(i);
 
             if(!strStudyCall.equals("")) strStudyCall += "&studyToken=";
             strStudyCall += mapTokens.get("parentStudyToken");
         }
 
         // Call GSCF in order to get information of the studies
         HashMap<String, String> objParam = new HashMap();
         objParam.put("studyToken",strStudyCall);
         LinkedList lstGetStudies = objGSCFService.callGSCF2(getSessionToken(),"getStudies",objParam);
 
         // Sort all studies according to title
         Collections.sort(lstGetStudies, new responseComparator("title"));
 
         // Generate the OPTIONs containing the studies a user has access to
         strReturn = "<option value='none'>Select a study...</option>";
         for(int i = 0; i < lstGetStudies.size(); i++){
             HashMap<String, String> map = (HashMap<String, String>) lstGetStudies.get(i);
             String strCode = " ("+map.get("code")+")";
             if(map.get("code")==null) {
                 strCode = "";
             }
            strReturn += "<option value='"+map.get("studyToken")+"'>"+map.get("title")+strCode+"</option>";
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
     public String getAssayToken() {
         return strAssayToken;
     }
 
     /**
      * @param strAssayToken the strAssayToken to set
      */
     public void setAssayToken(String assayToken) {
         this.strAssayToken = assayToken;
     }
 }
