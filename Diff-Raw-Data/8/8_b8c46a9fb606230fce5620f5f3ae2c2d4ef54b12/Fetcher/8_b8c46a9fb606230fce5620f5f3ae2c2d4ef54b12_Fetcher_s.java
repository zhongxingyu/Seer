 package microfeed;
 
 import java.sql.*;
 import java.util.*;
 import sql.*;
 import util.StrVal;
 
 /**
  * Class Fetcher This class contains the main functionality for the microfeed
  * application.
  *
  * @author Alex Hughes <alexhughes117@gmail.com>
  */
 public class Fetcher {
 
     private Connector con;
     private ArrayList<Feed> feeds;
     private ArrayList<String> authors;
 
     public Fetcher(Connector aConnector) {
         con = aConnector;
     }
 
     public ArrayList<Feed> fetchFeeds() throws SQLException {
         feeds = new ArrayList<Feed>();
         Feed feed;
         ResultSet feedR = con.sendQuery(""
                 + "SELECT * "
                 + "FROM microfeed ");
 
         while (feedR.next()) {
             feed = new Feed(feedR.getInt("microID"), feedR.getString("Author"),
                     feedR.getString("Title"), feedR.getString("Alias"), feedR.getString("Content"),
                     feedR.getTimestamp("DatePosted"), feedR.getInt("Status"));
 
             feeds.add(feed);
         }
         return feeds;
     }
 
     public ArrayList<String> fetchAuthors() throws SQLException {
         authors = new ArrayList<String>();
 
         ResultSet authorR = con.sendQuery(""
                 + "SELECT DISTINCT Author "
                 + "FROM microfeed ");
 
         while (authorR.next()) {
             authors.add(authorR.getString("Author"));
         }
         return authors;
     }
 
     public int createFeed(Feed aFeed) throws SQLException {
         int microID = -1;
 
         PreparedStatement ps = con.prepareStatement(""
                 + "INSERT INTO `microfeed` (`Author`, `Title`, `Alias`, `Content`, `Status`) VALUES "
                 + "(?,?,?,?,?)");
 
         ps.setString(1, aFeed.getAuthor());
         ps.setString(2, aFeed.getTitle());
        ps.setString(3, StrVal.createAlias(aFeed.getTitle()));
         ps.setString(4, aFeed.getContent());
         ps.setInt(5, aFeed.getStatus());
 
         ps.executeUpdate();
         ResultSet keyR = ps.getGeneratedKeys();
 
         while (keyR.next()) {
             microID = keyR.getInt(1);
         }
 
         return microID;
     }
     
     public void updateFeed(Feed aFeed) throws SQLException {
         
         String query = ""
                 + "UPDATE `microfeed` SET "
                 + "`Author` = ?, "
                 + "`Title` = ?, "
                 + "`Alias` = ? "
                 + "`Content` = ? "
                 + "WHERE microID = ? ";
         
         PreparedStatement ps = con.prepareStatement(query);
         
         ps.setString(1, aFeed.getAuthor());
         ps.setString(2, aFeed.getTitle());
         ps.setString(3, StrVal.createAlias(aFeed.getTitle()));
         ps.setString(3, aFeed.getContent());
         ps.setInt(4, aFeed.getMicroID());
         
         ps.executeUpdate();
     }
 
     public Feed fetchFeed(int anID) throws SQLException {
         Feed feed = null;
 
         PreparedStatement ps = con.prepareStatement(""
                 + "SELECT * "
                 + "FROM microfeed "
                 + "WHERE microID = ?");
 
         ps.setInt(1, anID);
 
         ResultSet feedR = ps.executeQuery();
 
         while (feedR.next()) {
             feed = new Feed(feedR.getInt("microID"), feedR.getString("Author"),
                     feedR.getString("Title"), feedR.getString("Alias"),feedR.getString("Content"),
                     feedR.getTimestamp("DatePosted"), feedR.getInt("Status"));
         }
 
         return feed;
     }
 
     /**
      * This function fetches the latest draft from the database. If available it
      * returns it, otherwise it returns null.
      *
      * @return
      * @throws SQLException
      */
     public Feed fetchDraft() throws SQLException {
         Feed draft = null;
 
         ResultSet draftR = con.sendQuery(""
                 + "SELECT Author, Title, Content "
                 + "FROM microfeed "
                 + "WHERE Status = 0 "
                 + "ORDER BY DatePosted DESC "
                 + "LIMIT 1 ");
 
         while (draftR.next()) {
             draft = new Feed(draftR.getString("Author"), draftR.getString("Title"), draftR.getString("Content"), 0);
         }
 
         return draft;
     }
 
     /**
      * This function cleans all draft posts from database. It is used after a
      * post is made, to clean all previous draft versions.
      *
      * @throws SQLException
      */
     public void cleanDrafts() throws SQLException {
         con.sendUpdate(""
                 + "DELETE "
                 + "FROM microfeed "
                 + "WHERE Status = 0 ");
     }
 
     public ArrayList<Feed> getFeeds() {
         return feeds;
     }
 
     public ArrayList<String> getAuthors() {
         return authors;
     }
 }
