 package rss;
 
 import utils.WebPage;
 import java.util.ArrayList;
 import java.util.List;
 
 import static utils.Constants.*;
 
 
 public class Feed
 {
     private static final String SEARCH = "<title>";
 
     private String address;
     private String lastMessage;
     private String name;
     private int titleItr;
 
 
     public Feed(String name, String address) {
         this.address = address;
         this.name = name;
         lastMessage = "";
     }
 
     public String getName() { return name; }
 
     public List<String> check(byte maxMsgsCount) throws Exception {
         WebPage entry = WebPage.loadWebPage(address, "UTF-8");
         String content = entry.getContent();
 
         String feedName;
         String message = " ";
         String firstMessage = null;
         List<String> newEntries = new ArrayList<String>();
 
         titleItr = 0;
 
        for (byte count = 1; count <= maxMsgsCount; ++count) {
             feedName = findNextTitle(content);
             message = findNextTitle(content);
 
            if(message == null || feedName == null || message.equals(lastMessage))
                 break;
 
            if (firstMessage == null)
                 firstMessage = message;
 
             newEntries.add(name + ": " + message);
         };
 
         if (firstMessage != null)
             lastMessage = firstMessage;
 
         return newEntries;
     }
 
     public ArrayList<String> getLastMessages(int count) throws Exception {
         WebPage entry = WebPage.loadWebPage(address, "UTF-8");
         String content = entry.getContent();
 
         titleItr = 0;
         findNextTitle(content); // First is feed name
 
         ArrayList<String> list = new ArrayList<String>();
         for(int i = 0; i < count; ++i) {
             String msg = findNextTitle(content);
             if(msg == null)
                 break;
             list.add(msg);
         }
         return list;
     }
 
     private String findNextTitle(String content) {
         int index = content.indexOf(SEARCH, titleItr);
         if(index == NOT_FOUND)
             return null;
 
         int begin = index + SEARCH.length();
 
         index = content.indexOf("</title>", begin);
         if(index == NOT_FOUND)
             return null;
 
         titleItr = index;
         return content.substring(begin, index);
     }
 };
