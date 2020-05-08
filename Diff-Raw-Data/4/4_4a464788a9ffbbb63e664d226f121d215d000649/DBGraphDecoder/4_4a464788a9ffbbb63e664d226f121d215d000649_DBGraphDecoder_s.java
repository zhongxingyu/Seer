 package org.agora.server.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 
 import org.agora.graph.JAgoraEdge;
 import org.agora.graph.JAgoraGraph;
 import org.agora.graph.JAgoraNode;
 import org.agora.graph.JAgoraNodeID;
 import org.agora.graph.VoteInformation;
 import org.bson.BSONDecoder;
 import org.bson.BSONObject;
 import org.bson.BasicBSONDecoder;
 
 public class DBGraphDecoder {
   
   protected JAgoraGraph graph;
   
   public DBGraphDecoder() { initialise(); }
   
   public void initialise() { graph = new JAgoraGraph(); }
   
   public JAgoraGraph getGraph() { return graph; }
   
   
   public boolean loadGraphByThreadID(Statement s, int threadID)
       throws SQLException {
     // Get arguments with votes
     // TODO: get usernames
     ResultSet rs = s.executeQuery("SELECT a.arg_ID AS arg_ID, a.source_ID AS source_ID, "
             + "content, a.date AS date, acceptability, thread_ID, "
             + "a.user_ID AS user_ID, u.username AS username, "
             + "SUM(CASE WHEN v.type = 1 THEN 1 ELSE 0 END) AS positive_votes, "
             + "SUM(CASE WHEN v.type = 0 THEN 1 ELSE 0 END) AS negative_votes "
             + "FROM `arguments` a LEFT OUTER JOIN `votes` v "
             + "ON a.arg_ID = v.arg_ID AND a.source_ID = v.source_ID "
             + "INNER JOIN `users` u ON a.user_ID = u.user_ID "
             + "WHERE a.thread_ID = '" + threadID + "' " + "GROUP BY a.arg_ID, a.source_ID;");
     boolean success = loadNodesFromResultSet(rs);
     rs.close(); // Is this important?
     if (!success)
       return false;
 
     // Get attacks with votes
     rs = s.executeQuery("SELECT a.arg_ID_attacker AS arg_ID_attacker, a.source_ID_attacker AS source_ID_attacker, "
             + "a.arg_ID_defender AS arg_ID_defender, a.source_ID_defender AS source_ID_defender, "
             + "arg_att.thread_ID AS att_thread_ID, arg_def.thread_ID AS def_thread_ID, "
             + "SUM(CASE WHEN v.type = 1 THEN 1 ELSE 0 END) AS positive_votes, "
             + "SUM(CASE WHEN v.type = 0 THEN 1 ELSE 0 END) AS negative_votes "
             + "FROM `attacks` a LEFT OUTER JOIN `votes` v "
             + "ON a.arg_ID_attacker = v.arg_ID_attacker AND a.source_ID_attacker = v.source_ID_attacker AND "
             + "   a.arg_ID_defender = v.arg_ID_defender AND a.source_ID_defender = v.source_ID_defender "
             + "INNER JOIN `arguments` arg_att "
             + "ON a.arg_ID_attacker = arg_att.arg_ID AND a.source_ID_attacker = arg_att.source_ID "
             + "INNER JOIN `arguments` arg_def "
             + "ON a.arg_ID_defender = arg_def.arg_ID AND a.source_ID_defender = arg_def.source_ID "
             + "WHERE v.arg_ID IS NULL AND (arg_att.thread_ID = '" + threadID + "' OR arg_def.thread_ID = '" + threadID + "') "
             + "GROUP BY a.arg_ID_attacker, a.source_ID_attacker, a.arg_ID_defender, a.source_ID_defender;");
     success = loadAttacksFromResultSet(rs);
     rs.close();
     if (!success)
       return false;
 
     return true;
   }
   
   /**
    * Constructs a single node using the current row of the given ResultSet.
    * @param rs
    * @return What you'd expect :D
    * @throws SQLException
    */
   protected JAgoraNode loadNodeFromResultSet(ResultSet rs) throws SQLException {
     JAgoraNode node = new JAgoraNode();
     
     // ID
     JAgoraNodeID id = new JAgoraNodeID();
     id.setSource(rs.getString("source_ID"));
     id.setLocalID(rs.getInt("arg_ID"));
     node.construct(id);
     
     // Content
     // TODO: inefficiency ->
     //       we are decoding into BSONObject and then re-encoding into byte[]
     //       to send over the network.
     BSONDecoder bdec = new BasicBSONDecoder();
     byte[] contentBytes = rs.getBytes("content");
     BSONObject contentBSON = bdec.readObject(contentBytes);
     node.setContent(contentBSON);
     
     // Other stuff
     node.setDate(new Date(rs.getTimestamp("date").getTime()));
     node.setAcceptability(rs.getBigDecimal("acceptability"));
     node.setThreadID(rs.getInt("thread_ID"));
     node.setPosterID(rs.getInt("user_ID"));
     node.setPosterName(rs.getString("username"));
     
     // Vote information
     node.setVotes(loadVoteInformationFromResultSet(rs));
     
     return node;
   }
   
   /**
    * Constructs nodes and populates the graph from the given query.
    * The query MUST include the username of the user associated with
    * the userID. It must also include all the aggregated vote information in
    * columns called 'positive_votes' and 'negative_votes'.
    * @param rs The query!
    * @return Whether the load was successful.
    * @throws SQLException
    */
   protected boolean loadNodesFromResultSet(ResultSet rs) throws SQLException {
     if (graph == null)
       return false;
     
     while (rs.next())
       graph.addNode(loadNodeFromResultSet(rs));
     
     return true;
   }
   
   
   
   
   /**
    * Loads a single attack from the current row of the ResultSet.
    * @param rs The result set pointing at the current attack.
    * @return An edge
    * @throws SQLException
    */
   protected JAgoraEdge loadAttackFromResultSet(ResultSet rs) throws SQLException {
     JAgoraNodeID attackerID = new JAgoraNodeID(
         rs.getString("source_ID_attacker"),
         rs.getInt("arg_ID_attacker"));
     JAgoraNodeID defenderID = new JAgoraNodeID(
        rs.getString("source_ID_attacker"),
        rs.getInt("arg_ID_attacker"));
     
     JAgoraNode attacker = graph.getNodeByID(attackerID);
     if (attacker == null) { // Create placeholder node.
       attacker = new JAgoraNode(attackerID);
       attacker.setThreadID(rs.getInt("att_thread_ID"));
     }
     JAgoraNode defender = graph.getNodeByID(defenderID);
     if (defender == null) {
       defender = new JAgoraNode(defenderID);
       defender.setThreadID(rs.getInt("def_thread_ID"));
     }
     
     JAgoraEdge edge = new JAgoraEdge(attacker, defender);
     
     edge.setVotes(loadVoteInformationFromResultSet(rs));
     return edge;
   }
   
   /**
    * Loads attacks from the ResultSet. It must also include all the aggregated
    * vote information in columns called 'positive_votes' and 'negative_votes'.
    * @param rs
    * @return
    * @throws SQLException
    */
   protected boolean loadAttacksFromResultSet(ResultSet rs) throws SQLException {
     // TODO: add user who posted the attack to information
     if (graph == null)
       return false;
     
     while (rs.next()) 
       graph.addEdge(loadAttackFromResultSet(rs));
     
     return true;
   }
   
   protected VoteInformation loadVoteInformationFromResultSet(ResultSet rs) throws SQLException {
     return new VoteInformation(rs.getInt("positive_votes"), rs.getInt("negative_votes"));
   }
 }
