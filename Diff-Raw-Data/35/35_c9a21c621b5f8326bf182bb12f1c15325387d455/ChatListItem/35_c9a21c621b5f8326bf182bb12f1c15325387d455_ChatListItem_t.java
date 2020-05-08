 package directi.androidteam.training.chatclient.Chat;
 
import android.util.Log;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.R;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 13/9/12
  * Time: 2:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatListItem {
     private String username;
     private boolean sender;
     private static final int sendResource = R.layout.chatlistitem;
     private static final int receiveResource = R.layout.chatlistitem_r;
     private String time;
     private boolean status=true;
     private String id;
 
     public boolean isStatus() {
         return status;
     }
 
     public void setStatus(boolean status) {
         this.status = status;
     }
 
     public String getMessage() {
         return message;
     }
 
     private String message;
 
     public String getUsername() {
         return username;
     }
 
     public boolean isSender() {
         return this.sender;
     }
 
     public String getTime() {
         return time;
     }
 
     public String getId() {
         return id;
     }
 
 
     public ChatListItem(MessageStanza message){
         this.message=message.getBody();
         this.sender=(message.getFrom().split("@")[0]).equals(ChatStore.getInstance().getAcctUID(message.getFrom().split("@")[0]));
        Log.d("chat color","msg from : "+message.getFrom().split("@")[0]+" acctid : "+ChatStore.getInstance().getAcctUID(message.getFrom().split("@")[0]));
         this.username=message.getFrom().split("@")[0];
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         this.time = dateFormat.format(new Date(message.getTime()));
         this.id = message.getID();
         this.status = message.isStatus();
     }
 
     public int getResourceID(){
         if(isSender())
             return sendResource;
         else
             return receiveResource;
     }
 }
