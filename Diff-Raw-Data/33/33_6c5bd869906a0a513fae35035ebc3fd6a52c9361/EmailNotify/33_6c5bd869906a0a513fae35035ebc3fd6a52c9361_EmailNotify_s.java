 /**
  * Created by IntelliJ IDEA.
  * uk.ac.homenetworks.notify.User: rob
  * Date: 07/07/2011
  * Time: 16:27
  */
 
 public class EmailNotify implements Notify{
     public boolean sendNotification(String notificationId, String userDetails, String body) {
         NotificationResponse nr = RemoteNotify.sendNotification("email", userDetails, body);
 	if(nr != null){
 	    if(nr.code == 200){
 		HWDBResponse.respond(notificationId, true, "App engine sent message");
 		return true;
 	    }
	}
 	HWDBResponse.respond(notificationId, false, nr.message);
 	return false;
     }
 }
