 package org.aap.monitoring;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class SQLManager {
 
     private static Logger LOG = LoggerFactory.getLogger(SQLManager.class);
     Connection con = null;
     SolrManager solrManager;
     String url = "jdbc:mysql://66.175.223.5:3306/AAP";
     String user = "root";
     String password = "aapmysql00t";
 
     public SQLManager(SolrManager solrManager) throws SQLException {
         this.solrManager = solrManager;
     }
 
 
     //yy-mm-dd
     public void triggerIndexer(String dateString) throws SQLException {
         int currSize = -1;
         int start = 0;
         int resultSize = 200;
         int count=0, failedCount=0;
         while(currSize != 0){
             currSize = 0;
             ResultSet rs = null;
             Statement st = null;
             if(con == null){
                 getConn();
             }
             try {
                 String query = "SELECT * from ARTICLE_TBL" ;
                 if(!StringUtils.isBlank(dateString)){
                     query += " where publishedDate > " + dateString ;
                 }
                 query += " order by publishedDate limit " + start + ", " + resultSize  + ";";
                 st = con.createStatement();
                 rs = st.executeQuery(query);
                 while (rs.next()) {
                     currSize++;
                     try {
                         this.solrManager.insertDocument(rs);
                         count++;
                     } catch (Exception e) {
                         LOG.error("Failed to index document");
                         failedCount++;
                     }
                     count++;
                 }
                 start = start + currSize;
             } catch (SQLException ex) {
                 LOG.error(ex.getMessage(), ex);
             } finally {
                 try {
                     if (rs != null) {
                         rs.close();
                     }
                     if (st != null) {
                         st.close();
                     }
                 } catch (SQLException ex) {
                     LOG.info(ex.getMessage(), ex);
                 }
             }
         }
         LOG.info("Indexer trigger: indexed successfully: " + count + ", failed: " + failedCount +   " documents for trigger: " + dateString);
     }
 
     /** Gives list of synonyms including the query itself**/
     public List<String> getSynonyms(final String query) throws SQLException {
         Map<String, String> synonymsMap = Maps.newHashMap();
         ResultSet rs = null;
         Statement st = null;
         if(con == null){
             getConn();
         }
         String sqlQuery = "select * from AAP_LIST;";
         st = con.createStatement();
         rs = st.executeQuery(query);
         while (rs.next()) {
             try {
                 String name = rs.getString("name");
                 String synonymString = rs.getString("synonyms");
                 if (!synonymString.equals("NULL")) {
                     String[] synonymsArray = synonymString.split(",");
                     List<String> synonyms = Lists.newArrayList(synonymsArray);
                     if (query.equals(name)) {
                         synonyms.add(name);
                         return synonyms;
                     } else {
                         int count = synonyms.size();
                         Iterables.filter(synonyms, new Predicate<String>() {
                             public boolean apply(String arg0) {
                                 return !arg0.equals(query);
                             }
                         });
                         if (count != synonyms.size()) {
                             synonyms.add(name);
                             return synonyms;
                         }
                     }
                 }
             } catch (Exception e) {
                 LOG.error("Failed to find synonyms");
             }
         }
         return Lists.newArrayList(query);
     }
 
     public void getConn() throws SQLException {
         con = DriverManager.getConnection(url, user, password);
     }
    public void closeConn() throws SQLException{
         if (con != null) {
             con.close();
         }
     }
 }
