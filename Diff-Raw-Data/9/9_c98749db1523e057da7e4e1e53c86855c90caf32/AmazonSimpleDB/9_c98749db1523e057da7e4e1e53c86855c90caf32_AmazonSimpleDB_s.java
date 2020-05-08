 package me.justshare.storage;
 
 import com.xerox.amazonws.sdb.*;
 import me.justshare.domain.SharedItem;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: filippo@diotalevi.com
  * Date: Nov 16, 2010
  */
 public class AmazonSimpleDB {
 
     private static final String DOMAIN = AmazonKeys.DOMAIN;

     private SimpleDB sds = new SimpleDB(AmazonKeys.awsAccessKey, AmazonKeys.awsSecretKey, true);
 
     public void createSpace(String name, String password) throws SDBException {
         Domain dom = sds.getDomain(DOMAIN);
 
         Item item = dom.getItem("space/"+name);
         List<ItemAttribute> list = new ArrayList<ItemAttribute>();
 
         list.add(new ItemAttribute("name", name, false));
         list.add(new ItemAttribute("type", "space", false));
         list.add(new ItemAttribute("password", password, false));
 
         item.putAttributes(list);
     }
 
     public List<String> listSpaces() throws SDBException {
 
         Domain dom = sds.getDomain(DOMAIN);
 
         QueryWithAttributesResult list = dom.selectItems("select output_list from `"+DOMAIN+"` where type = 'space'", "", true);
         List<String> output = new ArrayList<String>();
 
         for (String s : list.getItems().keySet()) {
             if (s.startsWith("space/"))
                 output.add(s.substring(6));
         }
         return output;
     }
 
     public boolean isPasswordCorrect(String space, String password) throws SDBException {
 
         Domain dom = sds.getDomain(DOMAIN);
 
         String query = "select output_list from `" + DOMAIN + "` where type = 'space' and name = '" + space + "'" +
                 " and password = '" + password + "'";
 //        System.out.println(query);
         QueryWithAttributesResult list = dom.selectItems(query, "", true);
         return !list.getItems().isEmpty();
     }
 
 
     public void addSharedItem(String space, String fileKey, String contentType, String description) throws SDBException {
 
         Domain dom = sds.getDomain(DOMAIN);
 
         String key = "" + fileKey + contentType + description ;
         Item item = dom.getItem("item/" + key.hashCode());
         List<ItemAttribute> list = new ArrayList<ItemAttribute>();
 
         list.add(new ItemAttribute("type", "item", false));
         list.add(new ItemAttribute("space", space, false));
         list.add(new ItemAttribute("timestamp", ""+System.currentTimeMillis(), false));
         if (fileKey != null)        
             list.add(new ItemAttribute("fileKey", fileKey, false));
         if (contentType != null)
             list.add(new ItemAttribute("contentType", contentType, false));
         if (description != null)
             list.add(new ItemAttribute("description", description, false));
 
         item.putAttributes(list);
     }
 
     public List<SharedItem> getSharedItems(String space, int page) throws SDBException {
         List<SharedItem> output = new ArrayList<SharedItem>();
 
         Domain dom = sds.getDomain(DOMAIN);
 
        int limit = 10 + page * 10;
 
         String query = "select output_list from `" + DOMAIN + "` where type = 'item' and space = '" + space + "'" +
                 " and timestamp is not null order by timestamp desc limit " + limit;
 
 //        System.out.println(query);
         
         QueryWithAttributesResult list = dom.selectItems(query, "", true);
 
         Map<String, List<ItemAttribute>> map = list.getItems();
 
         for (String itemKey : map.keySet()) {
             String desc = null;
             String ct = null;
             String fileKey = null;
 
             Item item = dom.getItem(itemKey);
 
             for (ItemAttribute itemAttribute : item.getAttributes()) {
                 if (itemAttribute.getName().equals("fileKey"))
                     fileKey = itemAttribute.getValue();
                 else if (itemAttribute.getName().equals("contentType"))
                     ct = itemAttribute.getValue();
                 else if (itemAttribute.getName().equals("description"))
                     desc = itemAttribute.getValue();
 
             }
             output.add(new SharedItem(fileKey, ct, desc));
         }
 
        return output;
     }
 
 
     public boolean existSpace(String name) throws SDBException {
 
         Domain dom = sds.getDomain(DOMAIN);
 
         QueryWithAttributesResult list = dom.selectItems("select output_list from `"+DOMAIN+"` where type = 'space' and name = '"+name+"'", "", true);
         return !list.getItems().isEmpty();
     }
 
 
     public static void main(String[] args) {
 
         try {
             AmazonSimpleDB db = new AmazonSimpleDB();
 
             db.createSpace("testspace", "12345");
             db.createSpace("testspace2", "12345");
 
             db.addSharedItem("testspace", "filekey", null, "a description");
 
             for (String s : db.listSpaces()) {
                 System.out.println(s);
             }
             System.out.println(db.existSpace("testspace"));
             System.out.println(db.existSpace("testspaceNoNOnO"));
 
             for (SharedItem sharedItem : db.getSharedItems("testspace", 2)) {
                 System.out.println(sharedItem.getFileKey());
             }
 
         } catch (SDBException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
 
 
 }
