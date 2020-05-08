 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import java.util.Date;
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import com.couchbase.client.CouchbaseClient;
 import net.spy.memcached.internal.GetFuture;
 import net.spy.memcached.internal.OperationFuture;
 
 
 
 /**
  * Copy the SQL data into JSON format, then send it to Couchbase.
  */
 public class MoveSQLDataToCouch {
   private static final String DB_URL = "jdbc:mysql://localhost:3306/cgat?autoReconnect=true";
   private static final String DB_USER = "cgat";
   private static final String DB_PASS = "csc560";
 
    private static List<URI> uris;
 
    public static void main(String[] args) throws Exception {
       Connection conn = null;
       CouchbaseClient client = null;
       uris = new LinkedList<URI>();
       uris.add(URI.create("http://127.0.0.1:8091/pools"));
 
     
       try {
          // Instantiate the DB Driver
          Class.forName("com.mysql.jdbc.Driver");
          conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
 
          client = new CouchbaseClient(uris, "default", "");
       } catch (Exception ex) {         
          System.err.println("Cannot connect to DB: " + ex);
       }
       
       /*
       System.out.println("Starting to move the stuff...\n");
       moveUsers(conn, client);
       System.out.println("-Finished moving users.");
       moveGroups(conn, client);
       System.out.println("-Finished moving groups.");
       moveContigs(conn, client);
       System.out.println("-Finished moving contigs.");
       */
       moveAnnotations(conn, client);
       System.out.println("-Finished moving annotations.");
      
       System.out.println("\nDone!");
       conn.close();
       client.shutdown();
    }
 
 
    private static void moveUsers(Connection conn, CouchbaseClient client) throws Exception {
       String usersQuery = "SELECT UserId, FirstName, LastName, UserName, Email, Pass, Salt, LastLoginDate, RegistrationDate, Level, Role, Exp FROM Users;";
       String historyQuery = "SELECT AnnotationId, FinishedDate, ExpGained, PartialSubmission FROM Annotations WHERE UserId = ?;";
       String taskQuery = "SELECT ContigId, Description, EndDate FROM Tasks WHERE UserId = ?;";
       String groupQuery = "SELECT GroupId FROM GroupMembership WHERE UserID = ?;";
       PreparedStatement userQ = conn.prepareStatement(usersQuery);
       PreparedStatement historyQ = conn.prepareStatement(historyQuery);
       PreparedStatement taskQ = conn.prepareStatement(taskQuery);
       PreparedStatement groupQ = conn.prepareStatement(groupQuery);
 
       ResultSet rs = userQ.executeQuery();
       while(rs.next()) {
          int userId = rs.getInt("UserId");
          String first = rs.getString("FirstName");
          String last = rs.getString("LastName");
          String userName = rs.getString("UserName");
          String email = rs.getString("Email");
          String pass = rs.getString("Pass");
          String salt = rs.getString("Salt");
          Date lastLogin = rs.getDate("LastLoginDate");
          Date registered = rs.getDate("RegistrationDate");
          int level = rs.getInt("Level");
          String role = rs.getString("Role");
          int exp = rs.getInt("Exp");
          ArrayList<Integer> historyIds = new ArrayList<Integer>();
          ArrayList<Date> historyDates = new ArrayList<Date>();
          ArrayList<Integer> historyExp = new ArrayList<Integer>();
          ArrayList<Boolean> historyPartialFlag = new ArrayList<Boolean>();
          ArrayList<Integer> taskContig = new ArrayList<Integer>();
          ArrayList<String> taskDesc = new ArrayList<String>();
          ArrayList<Date> taskEnd = new ArrayList<Date>();
          ArrayList<Integer> groups = new ArrayList<Integer>();
 
          historyQ.setInt(1, userId);
          ResultSet hrs = historyQ.executeQuery();
          while(hrs.next()) {
             int annotationId = hrs.getInt("AnnotationId");
             Date finished = hrs.getDate("FinishedDate");
             int expGained = hrs.getInt("ExpGained");
             boolean partial = hrs.getInt("PartialSubmission") == 1;
             historyIds.add(annotationId);
             historyDates.add(finished);
             historyExp.add(expGained);
             historyPartialFlag.add(partial);
          }
 
          taskQ.setInt(1, userId);
          ResultSet trs = taskQ.executeQuery();
          while(trs.next()) {
             int contigId = trs.getInt("ContigId");
             String desc = trs.getString("Description");
             Date end = trs.getDate("EndDate");
             taskContig.add(contigId);
             taskDesc.add(desc);
             taskEnd.add(end);
          }
 
          groupQ.setInt(1, userId);
          ResultSet grs = groupQ.executeQuery();
          while(grs.next()) {
             int groupId = grs.getInt("GroupId");
             groups.add(groupId);
          }
 
          //toJSON
          String JSON = userToJSON(userId, first, last, userName, email, pass, salt, 
                                   lastLogin, registered,
                                   level, role, exp, historyIds, historyDates, historyExp, 
                                   historyPartialFlag, taskContig, taskDesc, taskEnd, groups);
          //SEND TO COUCHBASE
          client.set("Users-"+userId, 0, JSON);
       }
 
       rs.close();
       userQ.close();
       historyQ.close();
       taskQ.close();
       groupQ.close();
    }
 
    private static void moveGroups(Connection conn, CouchbaseClient client) throws Exception {
       String groupsQuery = "SELECT GroupId, Name, GroupDescription, CreateDate FROM Groups;";
       String membershipQuery = "SELECT G.UserId, U.UserName FROM GroupMembership G, Users U WHERE G.UserId = U.UserId AND GroupId = ?;";
       PreparedStatement groupsQ = conn.prepareStatement(groupsQuery);
       PreparedStatement membersQ = conn.prepareStatement(membershipQuery);
 
       ResultSet rs = groupsQ.executeQuery();
       while(rs.next()) {
          int groupId = rs.getInt("GroupId");
          String name = rs.getString("Name");
          String desc = rs.getString("GroupDescription");
          Date createDate = rs.getDate("CreateDate");
          ArrayList<Integer> memberIds = new ArrayList<Integer>();
          ArrayList<String> memberNames = new ArrayList<String>();
 
          membersQ.setInt(1, groupId);
          ResultSet mrs = membersQ.executeQuery();
          while(mrs.next()) {
             int userId = mrs.getInt("G.UserId");
             String uname = mrs.getString("U.UserName");
             memberIds.add(userId);
             memberNames.add(uname);
          }
          //toJSON
          String JSON = groupToJSON(groupId, name, desc, createDate, memberIds, memberNames);
          //SEND TO COUCHBASE  
          client.set("Groups-"+groupId, 0, JSON);  
       }
 
       rs.close();
       groupsQ.close();
       membersQ.close();
    }
 
 
    private static void moveContigs(Connection conn, CouchbaseClient client) throws Exception {
       String contigsQuery = "SELECT ContigId, Name, Difficulty, Sequence, UploaderId, UserName, Source, Species, Status, CreateDate FROM Contigs, Users WHERE Contigs.UploaderId = Users.UserId;";
       String expertAnnoQuery = "SELECT AnnotationId FROM Annotations WHERE ExpertSubmission = 1 AND ContigId = ?;";
       String geneAnnoQuery = " SELECT A.AnnotationId, G.Name FROM Annotations A, GeneNames G WHERE A.GeneId = G.GeneId AND A.ContigId = ?;";
       PreparedStatement contigQ = conn.prepareStatement(contigsQuery);
       PreparedStatement expertAnnoQ = conn.prepareStatement(expertAnnoQuery);
       PreparedStatement geneAnnoQ = conn.prepareStatement(geneAnnoQuery);
 
       ResultSet rs = contigQ.executeQuery();
       while(rs.next()) {
          int contigId = rs.getInt("ContigId");
          String name = rs.getString("Name");
          int diff = rs.getInt("Difficulty");
          String seq = rs.getString("Sequence");
          int uploader = rs.getInt("UploaderId");
          String uploader_name = rs.getString("UserName");
          String source = rs.getString("Source");
          String species = rs.getString("Species");
          String status = rs.getString("Status");
          Date create = rs.getDate("CreateDate");
          ArrayList<Integer> expertAnnos = new ArrayList<Integer>();
          HashMap<String, ArrayList<Integer>> isoforms = 
                   new HashMap<String, ArrayList<Integer>>();
 
          expertAnnoQ.setInt(1,contigId);
          ResultSet expertrs = expertAnnoQ.executeQuery();
          while(expertrs.next()) {
             int annoId = expertrs.getInt("AnnotationId");
             expertAnnos.add(annoId);
          }
 
          geneAnnoQ.setInt(1, contigId);
          ResultSet isors = geneAnnoQ.executeQuery();
          while(isors.next()) {
             int annoId = isors.getInt("A.AnnotationId");
             String isoName = isors.getString("G.Name");
             //ENSURE the key exists
             if(!isoforms.containsKey(isoName)) {
                isoforms.put(isoName, new ArrayList<Integer>());
             }
             //ADD the annoId to the correct list
             isoforms.get(isoName).add(annoId);
          }
 
          //toJSON
          String JSON = contigToJSON(contigId, name, diff, seq, uploader, source, species,
                                     status, create, uploader_name, expertAnnos, isoforms);
          //SEND TO COUCHBASE
          client.set("Contigs-"+contigId, 0, JSON);
       }
 
       rs.close();
       contigQ.close();
       expertAnnoQ.close();
       geneAnnoQ.close();
    }
 
    private static void moveAnnotations(Connection conn, CouchbaseClient client) throws Exception {
       int pageSize = 1000;
 
       String annotationQuery = "SELECT A.AnnotationId, G.Name, A.StartPos, A.EndPos, A.ReverseComplement," +
                                " A.PartialSubmission, A.ExpertSubmission, A.ContigId, A.UserId,  A.CreateDate," +
                                " A.LastModifiedDate, A.FinishedDate, A.Incorrect, A.ExpertIncorrect, A.ExpGained" +
                                " FROM Annotations A, GeneNames G" +
                                " WHERE A.GeneId = G.GeneId" +
                                " ORDER BY A.AnnotationId" +
                                " LIMIT ?, " + pageSize;
       String exonQuery = "SELECT AnnotationId, StartPos, EndPos FROM Exons WHERE AnnotationId BETWEEN ? AND ?";
       PreparedStatement annotationQ = conn.prepareStatement(annotationQuery);
       PreparedStatement exonQ = conn.prepareStatement(exonQuery);
 
       ResultSet rs = null;
 
       // We know we have 1 million annotations.
       for (int page = 0; page < (1000000 / pageSize); page++) {
          annotationQ.setInt(1, pageSize * page);
 
          List<ArrayList<Integer>> exonsBatch = new ArrayList<ArrayList<Integer>>();
          for (int i = 0; i < pageSize; i++) {
             exonsBatch.add(new ArrayList());
          }
 
          // It costs too much to do the exon queries one at a time.
          // Do a batch at a time.
          exonQ.setInt(1, page * pageSize + 1);
          exonQ.setInt(2, page * (pageSize + 1) + 1);
          ResultSet exonrs = exonQ.executeQuery();
          while(exonrs.next()) {
             int annotationId = exonrs.getInt("AnnotationId");
             int exonStartPos = exonrs.getInt("StartPos");
             int exonEndPos = exonrs.getInt("EndPos");
             exonsBatch.get(annotationId).add(exonStartPos);
             exonsBatch.get(annotationId).add(exonEndPos);
          }
 
          rs = annotationQ.executeQuery();
          while(rs.next()) {
             int annoId = rs.getInt("A.AnnotationId");
             String geneName = rs.getString("G.Name");
             int startPos = rs.getInt("A.StartPos");
             int endPos = rs.getInt("A.EndPos");
             boolean reverse = rs.getInt("A.ReverseComplement") == 1;
             boolean partial = rs.getInt("A.PartialSubmission") == 1;
             boolean expert = rs.getInt("A.ExpertSubmission") == 1;
             int contigId =  rs.getInt("A.ContigId");
             int userId = rs.getInt("A.UserId");
             Date createDate =  rs.getDate("A.CreateDate");
             Date lastModifiedDate = rs.getDate("A.LastModifiedDate");
             Date finishedDate = rs.getDate("A.FinishedDate");
             boolean incorrect = rs.getInt("A.Incorrect") == 1;
             boolean expertIncorrect = rs.getInt("A.ExpertIncorrect") == 1;
             ArrayList<Integer> exonStartEndPairs = exonsBatch.get(annoId);
             //toJSON
             String JSON = annotationToJSON(annoId, geneName, startPos, endPos, reverse,
                                                 partial, expert, contigId, userId, createDate,
                                                 lastModifiedDate, finishedDate, incorrect, 
                                                 expertIncorrect, exonStartEndPairs);
 
             //SEND to couchbase
             client.set("Annotations-"+annoId, 0, JSON);
          }
 
          //TEST
          System.out.println("Batch #" + page + " Complete");
       }
 
       rs.close();
       annotationQ.close();
       exonQ.close();
    }
 
    private static void moveCollabAnnotations(Connection conn, CouchbaseClient client) throws Exception {
       String annotationQuery = "SELECT A.CollabAnnotationId, G.Name, A.StartPos, A.EndPos, A.ReverseComplement, A.ContigId, A.CreateDate, A.LastModifiedDate FROM CollabAnnotations A, GeneNames G WHERE A.GeneId = G.GeneId;";
       String exonQuery = "SELECT StartPos, EndPos FROM CollabExons WHERE CollabAnnotationId = ?;";
       PreparedStatement annotationQ = conn.prepareStatement(annotationQuery);
       PreparedStatement exonQ = conn.prepareStatement(exonQuery);
       
       ResultSet rs = annotationQ.executeQuery();
       while(rs.next()) {
          int collabId = rs.getInt("A.CollabAnnotationId");
          String geneName = rs.getString("G.Name");
          int startPos = rs.getInt("A.StartPos");
          int endPos = rs.getInt("A.EndPos");
          boolean reverse = rs.getInt("A.ReverseComplement") == 1;
          int contigId =  rs.getInt("A.ContigId");
          Date createDate =  rs.getDate("A.CreateDate");
          Date lastModifiedDate = rs.getDate("A.LastModifiedDate");
          ArrayList<Integer> exonStartEndPairs = new ArrayList<Integer>();
 
          exonQ.setInt(1,collabId);
          ResultSet exonrs = exonQ.executeQuery();
          while(exonrs.next()) {
             int exonStartPos = exonrs.getInt("StartPos");
             int exonEndPos = exonrs.getInt("EndPos");
             exonStartEndPairs.add(exonStartPos);
             exonStartEndPairs.add(exonEndPos);
          }
          //toJSON
          String JSON = collabAnnotationToJSON(collabId, geneName, startPos, 
                                                     endPos, reverse, contigId,
                                                     createDate, lastModifiedDate,
                                                     exonStartEndPairs);
 
          //SEND to couchbase
       }
 
       rs.close();
       annotationQ.close();
       exonQ.close();
    }
 
    private static String userToJSON(int userId, String first, String last, 
                                     String userName, String email,
                                     String pass, String salt, Date lastLogin, Date registered,
                                     int level, String role, int exp, 
                                     ArrayList<Integer> historyIds, ArrayList<Date> historyDates,
                                     ArrayList<Integer> historyExp, 
                                     ArrayList<Boolean> historyPartialFlag,
                                     ArrayList<Integer> taskContig, ArrayList<String> taskDesc,
                                     ArrayList<Date> taskEnd, ArrayList<Integer> groups) {
       ArrayList<Integer> historyCompleteIds  = new ArrayList<Integer>();
       ArrayList<Date> historyCompleteDates = new ArrayList<Date>();
       ArrayList<Integer> historyCompleteExp = new ArrayList<Integer>();
       ArrayList<Integer> incompleteIds = new ArrayList<Integer>();
 
       //Split up incomplete/complete annotations
       for (int i = 0; i < historyIds.size(); i++ ) {
           if(historyPartialFlag.get(i)) {
              incompleteIds.add(historyIds.get(i));
           }
           else {
              historyCompleteIds.add(historyIds.get(i));
              historyCompleteDates.add(historyDates.get(i));
              historyCompleteExp.add(historyExp.get(i));
           }
       }     
 
 
       return "{\n" + "   \"user_id\": \"" + userId + "\",\n" +
                    "   \"groups\": [\n" + listToJSON(groups) + "\n   ],\n" +
                    "   \"history\": [\n" + 
                          historyToJSON(historyCompleteIds,
                                        historyCompleteDates, historyCompleteExp) + "\n" +
                    "    ],\n" +
                    "    \"incomplete_annotations\": [\n" + listToJSON(incompleteIds) + "\n    ],\n" +
                    "    \"meta\": {\n" +
                    "         \"email\": \"" + email + "\",\n" +
                    "         \"first_name\": \"" + first + "\",\n" +
                    "         \"joined\": \"" + registered + "\",\n" +
                    "         \"last_login\": \"" + lastLogin + "\",\n" +
                    "         \"last_name\": \"" + last + "\",\n" +
                    "         \"level\": \"" + level + "\",\n" +
                    "         \"pass_hash\": \"" + pass + "\",\n" +
                    "         \"role\": \"" + role + "\",\n" +
                    "         \"salt\": \"" + salt + "\",\n" +
                    "         \"user_name\": \"" + userName + "\",\n" +
                    "         \"exp\": \"" + exp + "\"\n" +
                    "    },\n" +
                    "    \"tasks\": [\n" + tasksToJSON(taskContig, taskDesc, taskEnd) + "\n" +
                    "    ]\n}";
    }
 
    private static String tasksToJSON(ArrayList<Integer> ids, ArrayList<String> descs,
                                        ArrayList<Date> end) {
 
       String ret = "";
       
       for (int i = 0; i < ids.size(); i++) {
          ret += "       {\n" +
                 "           \"desc\": \"" + descs.get(i) + "\",\n" + 
                 "           \"contig_id\": \"" + ids.get(i) + "\",\n" +
                 "           \"end_date\": \"" + end.get(i) + "\"\n" + 
                 "       }";
          if (i != ids.size() - 1) {
             ret+= ",\n";
          }
       }
 
       return ret;
    }
   
    private static String historyToJSON(ArrayList<Integer> ids, ArrayList<Date> dates,
                                        ArrayList<Integer> exp) {
       String ret = "";
       
       for (int i = 0; i < ids.size(); i++) {
          ret += "       {\n" +
                 "           \"meta\": {\n" + 
                 "              \"experience_gained\": \"" + exp.get(i) + "\",\n" +
                 "              \"date\": \"" + dates.get(i) + "\"\n" +
                 "           },\n" +
                 "          \"anno_id\": \"" + ids.get(i) + "\"\n" + 
                 "       }";
          if (i != ids.size() - 1) {
             ret+= ",\n";
          }
       }
 
       return ret;
    }
 
    private static String listToJSON(ArrayList<?> list) {
       String ret = "";
 
       for (int i = 0; i < list.size(); i++) {
          ret += "       \"" + list.get(i) + "\"";
          if (i != list.size() - 1) {
             ret+= ",\n";
          }
       }
       return ret;
    }
 
    private static String groupToJSON(int groupId, String name, String desc, 
                                      Date createDate, ArrayList<Integer> memberIds,
                                      ArrayList<String> memberNames) {
       return "{\n" + "   \"group_id\": \"" + groupId + "\",\n" +
                    "   \"created\": \"" + createDate + "\",\n" +
                    "   \"description\": \"" + desc + "\",\n" +
                    "   \"name\": \"" + name + "\",\n" +
                    "   \"users\": [\n" +
                          membersToJSON(memberIds, memberNames) + "\n   ]\n}";
    }
 
    private static String membersToJSON(ArrayList<Integer> memberIds,
                                      ArrayList<String> memberNames) {
       String ret = "";
       
       for (int i = 0; i < memberIds.size(); i++) {
          ret += "      {\n" +
                 "         \"user_id\": \"" + memberIds.get(i) + "\",\n" +
                 "         \"name\": \"" + memberNames.get(i) + "\"\n" + "      }";
          if (i != memberIds.size() - 1) {
             ret+= ",\n";
          }
       }
 
       return ret;
    }
 
    private static String contigToJSON(int contigId, String name, int diff, String seq, 
                                       int uploader, String source, String species, 
                                       String status, Date create, String uploader_name,
                                       ArrayList<Integer> expertAnnos,
                                       HashMap<String, ArrayList<Integer>> isoforms) {
        return "{\n" + "   \"contig_id\": \"" + contigId + "\",\n" +
                    "   \"expert_annotations\": [\n" + listToJSON(expertAnnos) + "\n   ],\n" +
                    "   \"isoform_names\": {\n" + isoformsToJSON(isoforms) + "\n" +
                    "   },\n" +
                    "   \"meta\": {\n" +
                    "        \"difficulty\": \"" + diff + "\",\n" +
                    "        \"name\": \"" + name + "\",\n" +
                    "        \"source\": \"" + source + "\",\n" +
                    "        \"species\": \"" + species + "\",\n" +
                    "        \"status\": \"" + status + "\",\n" +
                    "        \"upload_date\": \"" + create + "\",\n" +
                    "        \"uploader\": \"" + uploader + "\",\n" +
                    "        \"uploader_name\": \"" + uploader_name + "\"\n" +
                    "   },\n" +
                    "   \"sequence\": \"" + seq + "\"\n}"; 
    }
    
    private static String isoformsToJSON(HashMap<String, ArrayList<Integer>> isoforms) {
       String ret = "";
 
       for (String key: isoforms.keySet()) {
          ArrayList<Integer> ids = isoforms.get(key);
          ret += "       \"" + key + "\": [\n" +
                         specialListToJSON(ids) + "\n" +
                 "        ],\n";
                 
       }
       if(ret.length() >= 2) {
          return ret.substring(0, ret.length()-2);
       }
       return ret;
    }
 
    private static String specialListToJSON(ArrayList<?> list) {
       String ret = "";
 
       for (int i = 0; i < list.size(); i++) {
          ret += "          \"" + list.get(i) + "\"";
          if (i != list.size() - 1) {
             ret+= ",\n";
          }
       }
       return ret;
    }
 
    private static String annotationToJSON(int annoId, String geneName, int startPos, 
                                           int endPos, boolean reverse , boolean partial,
                                           boolean expert, int contigId, int userId, 
                                           Date createDate, Date lastModifiedDate, 
                                           Date finishedDate, boolean incorrect, 
                                           boolean expertIncorrect, 
                                          ArrayList<Integer> exonStartEndPairs) {
       return "{\n" + "   \"annotation_id\": \"" + annoId + "\",\n" +
                    "   \"contig_id\": \"" + contigId + "\",\n" +
                    "   \"end\": \"" + endPos + "\",\n" +
                    "   \"exons\": [\n" + exonsToJSON(exonStartEndPairs) + "\n" +
                    "   ],\n" +
                    "   \"expert\": \"" + expert + "\",\n" +
                    "   \"isoform_name\": \"" + geneName + "\",\n" +
                    "   \"meta\": {\n" +
                    "        \"created\": \"" + createDate + "\",\n" +
                    "        \"expert_incorrect\": \"" + expertIncorrect + "\",\n" +
                    "        \"finished\": \"" + finishedDate + "\",\n" +
                    "        \"incorrect\": \"" + incorrect + "\",\n" +
                    "        \"last_modified\": \"" + lastModifiedDate + "\"\n" +
                    "   },\n" +
                    "   \"partial\": \"" + partial + "\",\n" +
                    "   \"reverse_complement\": \"" + reverse + "\",\n" +
                    "   \"start\": \"" + startPos + "\",\n" +               
                    "   \"user_id\": \"" + userId + "\"\n}"; 
    }
 
    private static String exonsToJSON(ArrayList<Integer> exons) {
       String ret = "";
       
       for (int i = 0; i < exons.size(); i+=2) {
          ret += "      {\n" +
                 "         \"start\": \"" + exons.get(i) + "\",\n" +
                 "         \"end\": \"" + exons.get(i+1) + "\"\n" + "      }";
          if (i != exons.size() - 2) {
             ret+= ",\n";
          }
       }
 
       return ret;
    }
 
    private static String collabAnnotationToJSON(int collabId, String geneName, int startPos,
                               int endPos, boolean reverse, int contigI, Date createDate,
                               Date lastModifiedDat, ArrayList<Integer> exonStartEndPairs) {
       return "{}";
    }
 }
