 /**
 $Revision$
 $Id$
  Author: Valentin Kuznetsov
  **/
 
 package dbs.api;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.sql.ResultSetMetaData;
 import java.io.Writer;
 import java.io.StringWriter;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashSet;
 import java.sql.SQLException;
 import db.DBManagement;
 import dbs.DBSConstants;
 import dbs.sql.DBSSql;
 import dbs.util.DBSUtil;
 import dbs.DBSException;
 
 public class DBSApiViewsLogic extends DBSApiLogic {
     DBSApiData data = null;
     public DBSApiViewsLogic(DBSApiData data) {
         super(data);
         this.data = data;
     }
 
     public static PreparedStatement
     getViewQuery(Connection conn, String viewName,
         String key, List cond,
         String sortKey, String sortOrder) 
     throws SQLException {
         String owner = DBSSql.owner();
         if (owner == null) {
             owner = "";
         }
         String sql = "SELECT * FROM " + owner + viewName;
         if (key != null && cond != null) {
             sql += " WHERE " + key + " IN (";
             for(int i=0;i<cond.size();i++) {
                 if (i == cond.size()-1) {
                     sql += "'"+cond.get(i)+"'";
                 } else {
                     sql += "'"+cond.get(i)+"', ";
                 }
             }
             sql += ") ";
         }
         if (sortKey != null && !sortKey.equals("")) {
             sql += " ORDER BY " + sortKey;
             if (sortOrder != null && !sortOrder.equals("")) {
                 sql += " " + sortOrder;
             } else {
                 sql += " DESC ";
             } 
         }
         PreparedStatement ps = DBManagement.getStatement(conn, sql);
         DBSUtil.writeLog("\n\n" + ps + "\n\n");
         return ps;
     }
 
     public void
     getSummary(Connection conn, Writer out, String viewName,
         int begin, int end,
         String key, List cond,
         String sortKey, String sortOrder) 
     throws Exception {
         PreparedStatement ps = null;
         ResultSet rs =  null;
         ps = getViewQuery(conn, viewName, key, cond, sortKey, sortOrder);
         pushQuery(ps);
         rs =  ps.executeQuery();
         ResultSetMetaData rsmd = rs.getMetaData();
         int numberOfColumns = rsmd.getColumnCount();
         out.write( ((String)"<summary_view>\n"));
         int counter = 0;
         while(rs.next()) {
             if (counter >= begin && counter <= end) {
                 out.write("<row>\n");
                 for(int i=1;i<=numberOfColumns;i++) {
                     String cname = rsmd.getColumnName(i);
                     String res = get(rs, cname);
                     String name = viewName+"."+cname;
                     out.write( "<"+name+">"+res+"</"+name+">\n" );
                 }
                 out.write("</row>\n");
             }
             counter += 1;
         }
         out.write("</summary_view>\n");
     }
 
     public void
     executeSummary(Connection conn, Writer out, String userQuery)
     throws Exception {
         String begin = "";
         String end = "";
         String sortKey = null;
         String sortOrder = null;
         executeSummary(conn, out, userQuery, begin, end, sortKey, sortOrder);
     }
     public void
     executeSummary(Connection conn, Writer out, 
         String userQuery, String begin, String end, 
         String sortKey, String sortOrder)
     throws Exception {
         // Step 1, call executeQuery to get SQL for userQuery
 //        ArrayList objList = executeQuery(conn, out, userQuery, begin, end, false);
         ArrayList objList = executeQuery(conn, out, userQuery, "", "", false);
         String finalQuery = (String)objList.get(1);
         List<String> bindValues = (List<String>)objList.get(2);
         List<Integer> bindIntValues = (List<Integer>)objList.get(3);
         // Step 2, execute SQL to get results
         PreparedStatement ps = 
                 DBSSql.getQuery(conn, finalQuery, bindValues, bindIntValues);
         List<String> resList = new ArrayList();
         ResultSet rs =  null;
         try {
             rs =  ps.executeQuery();
             ResultSetMetaData rsmd = rs.getMetaData();
             int numberOfColumns = rsmd.getColumnCount();
             while(rs.next()) {
                 for(int i=1;i<=numberOfColumns;i++) {
                     String cname = rsmd.getColumnName(i);
                     String res = get(rs, cname);
                     resList.add(res);
                 }
             }
         } catch(Exception e) {
             out.write( "<exception>\n"+e+"\n</exception>\n" );
             return;
         } finally { 
             if (rs != null) rs.close();
             if (ps != null) ps.close();
         }
 
         // Step 3, pass results to summary view
         String [] temp = userQuery.split(" ");
         String skey = temp[1]; // temp[0]==find
         String key = "Name";
         String view = null;
         // Mapping between QL keys and summary views, 
         // e.g. primds has PrimSummary view and its key is Name
         if (skey.equals("primds")) {
             view = "PrimSummary";
         } else if (skey.equals("run")) {
             view = "RunSummary";
             key  = "RunNumber";
         } else if (skey.equals("file")) {
             view = "FileSummary";
             key  = "LogicalFileName";
         } else if (skey.equals("procds")) {
             view = "ProcSummary";
         } else if (skey.equals("release")) {
             view = "ReleaseSummary";
             key  = "Version";
         } else if (skey.equals("site")) {
             view = "SiteSummary";
             key  = "SEName";
         } else if (skey.equals("tier")) {
             view = "TierSummary";
         } else if (skey.equals("dataset")) {
             view = "DatasetSummary";
             key  = "Path";
         } else if (skey.equals("ads")) {
             view = "AdsSummary";
         }
 
         // based on provided begin/end define bounds for result loop
         int lbound = 0;
         int rbound = 0;
         if(begin != null && !begin.equals("")) {
            lbound = new Integer(begin).intValue();
         }
         if(end != null && !end.equals("")) {
            rbound = new Integer(end).intValue();
         } else {
            rbound = resList.size();
         }
 
         try {
             getSummary(conn, out, view, lbound, rbound, key, resList, sortKey, sortOrder);
         } catch(Exception e) {
             DBSUtil.writeLog("\nexecuteSummary exception: " + e + "\n");
             out.write( "<summary_view>\n" );
             out.write( "<exception>\n" + e + "\n</exception>\n" );
             out.write( "</summary_view>\n" );
         }
         out.write( "<results>\n" );
        for(int i=lbound;i<=rbound;i++) {
             out.write( "<row>\n<"+skey+">"+resList.get(i)+"</"+skey+">\n"+"</row>\n" );
         }
         out.write( "</results>\n" );
     }
 }
