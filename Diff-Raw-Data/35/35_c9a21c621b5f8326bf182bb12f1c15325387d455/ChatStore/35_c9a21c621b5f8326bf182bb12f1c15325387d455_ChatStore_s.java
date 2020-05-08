 package directi.androidteam.training.chatclient.Chat;
 
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/22/12
  * Time: 7:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatStore {
     private static ChatStore chatStore = new ChatStore();
     private HashMap<String,String> chatMap;
     private ChatStore() {
         chatMap = new HashMap<String, String>();
     }
 
     public static ChatStore getInstance() {
         return chatStore;
     }
 
     public void addEntry(String from, String accountUID) {
        chatMap.put(from,accountUID);
     }
 
     public String getAcctUID(String jid) {
        return chatMap.get(jid);
     }
 }
