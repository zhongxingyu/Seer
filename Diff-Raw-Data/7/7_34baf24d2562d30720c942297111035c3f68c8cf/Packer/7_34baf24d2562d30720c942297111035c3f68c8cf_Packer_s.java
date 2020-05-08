 /**
  * Created with IntelliJ IDEA.
  * User: romanfilippov
  * Date: 09.03.13
  * Time: 23:02
  */
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.sql.*;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import java.util.logging.Logger;
 
 
 class Packer{
 
     private final String hostName = "";
     private final String className = "com.mysql.jdbc.Driver";
     private final String dbName = "";
     private final String userName = "";
     private final String userPassword = "";
     private static Logger log = Logger.getLogger(Packer.class.getName());
 
     Connection con = null;
 
     public Packer() {
 
         try {
 
             log.info("Establishing a connection...");
             String url = "jdbc:mysql://" + hostName + "/" + dbName;
             Class.forName(className);
             con = DriverManager.getConnection(url, userName, userPassword);
             log.info("Connection established");
 
         } catch (ClassNotFoundException e) {
             log.warning("Class not found" + e.getMessage());
         } catch (SQLException ex) {
             log.warning(ex.getMessage());
         }
     }
 
     int[] getUsersIds() throws SQLException {
 
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("Select user_ptr_id from Server_account");
 
         int rowcount = 0;
         if (rs.last()) {
             rowcount = rs.getRow();
             rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
         } else {
             return null;
         }
 
         int[] usersIds = new int[rowcount];
 
         int i = 0;
         while (rs.next()) {
             usersIds[i] = rs.getInt(1);
             i++;
         }
 
         rs.close();
         st.close();
 
         return usersIds;
     }
 
     RssItem[] getDataForUserId(int uid) throws SQLException {
 
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("select * from Server_rssitem where rssfeed_id = (select rssfeed_id from Server_rssfeed_collection where collection_id = (select id from Server_collection where user_id = " + uid +"));");
 
         int rowcount = 0;
         if (rs.last()) {
             rowcount = rs.getRow();
             rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
         } else {
             return null;
         }
 
         RssItem[] items = new RssItem[rowcount];
 
         int i = 0;
         while (rs.next()) {
             items[i] = new RssItem(rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getDate(7));
             i++;
         }
 
         rs.close();
         st.close();
 
         return items;
     }
 
     DocumentItem formDocument (int uid, RssItem[] userFeeds) {
 
         String doc = new String("<html><head><title>Your RSS feed from novaJoy</title></head><body>");
 
         for (int i = 0; i < userFeeds.length; i++) {
             doc += userFeeds[i].toHtml();
         }
 
         doc += "</body></html>";
 
         return new DocumentItem(uid,doc);
     }
 
     DocumentItem[] getPackagedData() throws SQLException {
 
         int[] userIds = getUsersIds();
         DocumentItem[] usersDocuments = new DocumentItem[userIds.length];
 
         for (int i = 0; i < userIds.length; i++) {
 
             RssItem[] userFeed = getDataForUserId(userIds[i]);
             usersDocuments[i] = formDocument(userIds[i], userFeed);
         }
         return usersDocuments;
     }
 
     public void performRoutineTasks() {
 
         try {
 
             DocumentItem[] packagedDocuments = getPackagedData();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("insert into ");
 
         } catch (SQLException e) {
             log.warning(e.getMessage());
         }
     }
 
     boolean performDataBaseUpdate(DocumentItem[] documents) {
 
 
     }
 
     /**
      * Returns transformed String which consists result of XSLT transformation
      *
      * @return  {@code String} representing transformed document
      *          (which may be {@code null}).
      */
     public String performXSLT(String source, String stylesheet) {
 
         try {
             StringReader reader = new StringReader(source);
             StringWriter writer = new StringWriter();
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer(
                     new javax.xml.transform.stream.StreamSource(stylesheet));
 
             transformer.transform(
                     new javax.xml.transform.stream.StreamSource(reader),
                     new javax.xml.transform.stream.StreamResult(writer));
 
             String s = writer.toString();
             return s;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 }
 
