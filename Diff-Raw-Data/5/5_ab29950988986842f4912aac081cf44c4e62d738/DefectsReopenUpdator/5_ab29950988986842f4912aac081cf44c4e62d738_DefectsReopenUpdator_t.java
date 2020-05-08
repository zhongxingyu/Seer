 package com.kenshoo.rallyupdator.reopen;
 
 import com.kenshoo.rallyupdator.client.RallyClient;
 import com.kenshoo.rallyupdator.defects.*;
 import org.codehaus.jackson.JsonGenerator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nir
  * Date: 1/24/13
  * Time: 11:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DefectsReopenUpdator {
     private String password;
     private String userName;
 
     public DefectsReopenUpdator(String userName, String password) {
         this.userName = userName;
         this.password = password;
     }
 
     public void updateReopen(String workspaceId) {
         RallyClient client = new RallyClient(userName,password);
         String workSpaceURI = "https://rally1.rallydev.com/slm/webservice/1.40/workspace/" + workspaceId + ".js";
         String query = "https://rally1.rallydev.com/slm/webservice/1.40/defect.js?workspace=" + workSpaceURI + "&fetch=true&order=CreationDate&pagesize=200&start=";
         DefectResponse defectResponses = null;
         int startIndex = 50;
         do {
             String queryToRun = query + startIndex;
             defectResponses = client.makeAPICall(queryToRun,DefectResponse.class);
             for (DefectResult result: defectResponses.QueryResult.Results){
                 int reOpenCounter = 0;
                 RevisionHistoryResponse revisionHistoryResponse = client.makeAPICall(result.RevisionHistory._ref, RevisionHistoryResponse.class);
                 for (Revision revision: revisionHistoryResponse.RevisionHistory.Revisions){
                    if (revision.Description.contains(" to [Reopen]")  ||  revision.Description.contains("from [Fixed] to [Open]")){
                          reOpenCounter++;
                    }                    
                 }
                 if (reOpenCounter >0){
                     System.out.println("updating " + result.FormattedID + " With Repoen " + reOpenCounter);
                     String jsonUpdte = "{\"defect\":{\"ReopenCounter\" : \"" + reOpenCounter + "\"}}";
                     DefectUpdateResponse defectUpdateResponse = client.update("https://rally1.rallydev.com/slm/webservice/1.40/defect/" + result.ObjectID + ".js", jsonUpdte, DefectUpdateResponse.class);
                 }
 
             }
             startIndex += 200;
         }
         while(defectResponses.QueryResult.Results.length > 0);
 
         int i=2;
     }
 }
