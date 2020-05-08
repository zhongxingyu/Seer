 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package monsterMashGroupProject;
 
 import databaseManagement.PersistManager;
 import databaseManagement.Requests;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author jamesslater
  */
 public class RequestController {
     
     PersistManager persistIt = new PersistManager();
     
     Requests request;
     
    public String controller(String requestID, String choice){
         
         FriendHandler friendHand = new FriendHandler();
         
         persistIt.init();
         
         List<Requests> requestList = persistIt.searchRequets();
         
         for(int i = 0; i < requestList.size(); i++){
             if(requestList.get(i).getId().equals(requestID)){
                 this.request = requestList.get(i);
             }
         }
         
        if ((choice.equals("acceptbtn")&&(request.getType().equals("FriendRequest")))){
             try {
                 friendHand.acceptFriendRequest(request);
             } catch (IOException ex) {
                 Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         return "welcome.jsp";
     }
     
     
     /**
      * Allows the user to reject a sendRequest, sendRequest is removed from
      * sendRequest list, requester is not alerted.
      *
      * @param requester
      * @param recipient
      */
     public void rejectRequest(Requests request) {
         persistIt.init();
         
         persistIt.remove(request);
         
         persistIt.shutdown();
         
     }
     
 }
