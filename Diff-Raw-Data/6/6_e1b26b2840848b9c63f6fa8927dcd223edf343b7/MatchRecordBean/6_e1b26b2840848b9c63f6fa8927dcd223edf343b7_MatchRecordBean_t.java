 package com.frcscout.scout;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.frcscout.sql.DBConnection;
 import com.frcscout.sql.MySQLConnection;
 import com.frcscout.util.StringUtil;
 
 public class MatchRecordBean {
 
     private Integer id;
     private String user;
     private Integer teamId;
     private Integer matchId;
     private Integer eventId;
     private String color;
     private Integer autonTop;
     private Integer autonMiddle;
     private Integer autonBottom;
     private Integer teleopTop;
     private Integer teleopMiddle;
     private Integer teleopBottom;
     private Integer teleopPyramid;
     private Integer pyramidLevel;
     private String playStyle;
     private Integer confidence;
     private Integer ability;
     private Boolean fouls;
     private Boolean technicalFouls;
     private String comments;
     private String path; //TODO
 
     private Connection conn;
     private DBConnection dbconn;
     
     public MatchRecordBean() {
         dbconn = new MySQLConnection();    
         id = null;
         user = null;
         teamId = null;
         matchId = null;
         color = null;
         autonTop = null;
         autonMiddle = null;
         autonBottom = null;
         teleopTop = null;
         teleopMiddle = null;
         teleopBottom = null;
         teleopPyramid = null;
         pyramidLevel = null;
         playStyle = null;
         confidence = null;
         ability = null;
         fouls = null;
         technicalFouls = null;
         comments = null;
         path = null;
     }
 
     public void deleteMatchRecord() {
         if (id != null) {
             PreparedStatement st = null;
             String q = "DELETE FROM match_record_2013 WHERE id = ?";
 
             try {
                 conn = dbconn.getConnection();
                 st = conn.prepareStatement(q);
                 st.setInt(1, this.id.intValue());
                 st.executeUpdate();
             } catch (SQLException e) {
                 e.printStackTrace();
             } finally {
                 try {
                     conn.close();
                     st.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to close connection");
                 }
             }
         } else {
             System.out.println("Unable to delete event because id is null");
         }
     }
     
     public void insertMatchRecord() {
         PreparedStatement st = null;
         ResultSet rs = null;
         String q = "INSERT INTO match_record_2013 SET " +
             "user = ?, team_id = ?, event_id = ?, match_number = ?, color = ?, " +
             "auton_top = ?, auton_middle = ?, auton_bottom = ?, " +
             "teleop_top = ?, teleop_middle = ?, teleop_bottom = ?, " +
             "teleop_pyramid = ?, pyramid_level = ?, play_style = ?, " +
             "confidence = ?, ability = ?, fouls = ?, " +
             "technical_fouls = ?, comments = ?, path = ?;";
 
         try {
             conn = dbconn.getConnection();
             conn.setAutoCommit(false);
             insertMatch(conn, this.eventId, this.matchId);
             st = conn.prepareStatement(q, PreparedStatement.RETURN_GENERATED_KEYS);
             st.setString(1, this.user);
             st.setInt(2, this.teamId);
             st.setInt(3, this.eventId);
             st.setInt(4, this.matchId);
             st.setString(5, this.color);
             st.setInt(6, this.autonTop);
             st.setInt(7, this.autonMiddle);
             st.setInt(8, this.autonBottom);
             st.setInt(9, this.teleopTop);
             st.setInt(10, this.teleopMiddle);
             st.setInt(11, this.teleopBottom);
             st.setInt(12, this.teleopPyramid);
             st.setInt(13, this.pyramidLevel);
             st.setString(14, this.playStyle);
             st.setInt(15, this.confidence);
             st.setInt(16, this.ability);
             st.setBoolean(17, this.fouls);
             st.setBoolean(18, this.technicalFouls);
             st.setString(19, this.comments);
             st.setString(20, this.path);    // TODO path
 
             st.executeUpdate();
             conn.commit();
             rs = st.getGeneratedKeys();
             if (rs.next()) {
                 setId(rs.getInt(1));
             }
         } catch (SQLException e) {
             try {
                 conn.rollback();
             } catch (SQLException a) {
                 System.out.println("Unable to roll back transaction");
             }
             e.printStackTrace();
         } finally {
             try {
                 conn.close();
                 st.close();
                 rs.close();
             } catch (SQLException e) {
                 System.out.println("Unable to close connection");
             }
         }
     }
     
     private void insertMatch(Connection conn, int event, int match) throws SQLException{
         String q = "INSERT IGNORE INTO `match` SET match_number = ?, event_id = ?";
         PreparedStatement st = null;
         try {
             st = conn.prepareStatement(q);
             st.setInt(1, match);
             st.setInt(2, event);
             st.executeUpdate();
         } catch (SQLException e) {
             throw e;
         } finally {
             try {
                 st.close();
             } catch (SQLException a) {
                 System.out.println("Unable to close connection");
             }
         }
     }
 
     public void updateMatchRecord() {
         if (id != null) {
             PreparedStatement st = null;
             String q = "UPDATE match_record_2013 SET " +
                 "user = ?, team_id = ?, match_number = ?, color = ?, " +
                 "auton_top = ?, auton_middle = ?, auton_bottom = ?, " +
                 "teleop_top = ?, teleop_middle = ?, teleop_bottom = ?, " +
                 "teleop_pyramid = ?, pyramid_level = ?, play_style = ?, " +
                 "confidence = ?, ability = ?, fouls = ?, " +
                 "technical_fouls = ?, comments = ?, path = ?, event_id = ? WHERE id = ?;";
     
             try {
                 conn = dbconn.getConnection();
                 conn.setAutoCommit(false);
                 insertMatch(conn, this.eventId, this.matchId);
                 st = conn.prepareStatement(q);
                 st.setString(1, this.user);
                 st.setInt(2, this.teamId);
                 st.setInt(3, this.matchId);
                 st.setString(4, this.color);
                 st.setInt(5, this.autonTop);
                 st.setInt(6, this.autonMiddle);
                 st.setInt(7, this.autonBottom);
                 st.setInt(8, this.teleopTop);
                 st.setInt(9, this.teleopMiddle);
                 st.setInt(10, this.teleopBottom);
                 st.setInt(11, this.teleopPyramid);
                 st.setInt(12, this.pyramidLevel);
                 st.setString(13, this.playStyle);
                 st.setInt(14, this.confidence);
                 st.setInt(15, this.ability);
                 st.setBoolean(16, this.fouls);
                 st.setBoolean(17, this.technicalFouls);
                 st.setString(18, this.comments);
                 st.setString(19, this.path); //TODO path
                 st.setInt(20, this.eventId);
                 st.setInt(21, this.id.intValue());
                 
                 st.executeUpdate();
                 conn.commit();
             } catch (SQLException e) {
                 e.printStackTrace();
                 try {
                     conn.rollback();
                 } catch (SQLException a) {
                     System.out.println("Unable to roll back transaction");
                 }
               
                 
             } finally {
                 try {
                     conn.close();
                     st.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to close connection");
                 }
             }
         }
         else {
             System.out.println("Unable to update match record because id is null");
         }
     }
 
     public void loadMatchRecord(String id) {
         if (!StringUtil.isBlank(id)) {
             PreparedStatement st = null;
             ResultSet rs = null;
             try {
                 conn = dbconn.getConnection();
                 st = conn.prepareStatement("SELECT * FROM match_record_2013 WHERE id = " + id + " limit 1");
                 rs = st.executeQuery();
                 if (rs.next()) {
                     setId(rs.getInt("id"));
                     setUser(rs.getString("user"));
                     setTeamId(rs.getInt("team_id"));
                     setMatchId(rs.getInt("match_number"));
                     setEventId(rs.getInt("event_id"));
                     setColor(rs.getString("color"));
                     setAutonTop(rs.getInt("auton_top"));
                     setAutonMiddle(rs.getInt("auton_middle"));
                     setAutonBottom(rs.getInt("auton_bottom"));
                     setTeleopTop(rs.getInt("teleop_top"));
                     setTeleopMiddle(rs.getInt("teleop_middle"));
                     setTeleopBottom(rs.getInt("teleop_bottom"));
                     setTeleopPyramid(rs.getInt("teleop_pyramid"));
                     setPyramidLevel(rs.getInt("pyramid_level"));
                     setPlayStyle(rs.getString("play_style"));
                     setConfidence(rs.getInt("confidence"));
                     setAbility(rs.getInt("ability"));
                     setFouls(rs.getBoolean("fouls"));
                     setTechnicalFouls(rs.getBoolean("technical_fouls"));
                     setComments(rs.getString("comments"));
                     setPath(rs.getString("path"));    // TODO path
                 } else {
                     System.out.println("match record id " + this.id.toString() + " not found");
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             } finally {
                 try{
                     conn.close();
                     st.close();
                     rs.close();
                 }catch (SQLException e) {
                     System.out.println("Error closing query");
                 }
             }
         }
     }
     
     @SuppressWarnings("unchecked")
     public String loadMatchRecords() { //for admins
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
             st = conn.prepareStatement("SELECT * FROM match_record_2013");
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 
                 o.put("id", rs.getInt("id"));
                 o.put("user", rs.getString("user"));
                 o.put("team_id", rs.getInt("team_id"));
                 o.put("match_id", rs.getInt("match_number"));
                 o.put("event_id", rs.getInt("event_id"));
                 o.put("color", rs.getString("color"));
                 o.put("auton_top", rs.getInt("auton_top"));
                 o.put("auton_middle", rs.getInt("auton_middle"));
                 o.put("auton_bottom", rs.getInt("auton_bottom"));
                 o.put("teleop_top", rs.getInt("teleop_top"));
                 o.put("teleop_middle", rs.getInt("teleop_middle"));
                 o.put("teleop_bottom", rs.getInt("teleop_bottom"));
                 o.put("teleop_pyramid", rs.getInt("teleop_pyramid"));
                 o.put("pyramid_level", rs.getInt("pyramid_level"));
                 o.put("play_style", rs.getString("play_style"));
                 o.put("confidence", rs.getInt("confidence"));
                 o.put("ability", rs.getInt("ability"));
                 o.put("fouls", rs.getBoolean("fouls"));
                 o.put("technical_fouls", rs.getBoolean("technical_fouls"));
                 o.put("comments", rs.getString("comments"));
                 o.put("path", rs.getString("path"));    // TODO path
                 
                 json.add(o);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
        return json.toString();
     }
     
     @SuppressWarnings("unchecked")
     public String loadMatchRecords(String user) { //for scouts
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
             st = conn.prepareStatement("SELECT * FROM match_record_2013 where user = ?");
             st.setString(1, user);
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 
                 o.put("id", rs.getInt("id"));
                 o.put("user", rs.getString("user"));
                 o.put("team_id", rs.getInt("team_id"));
                 o.put("match_id", rs.getInt("match_number"));
                 o.put("event_id", rs.getInt("event_id"));
                 o.put("color", rs.getString("color"));
                 o.put("auton_top", rs.getInt("auton_top"));
                 o.put("auton_middle", rs.getInt("auton_middle"));
                 o.put("auton_bottom", rs.getInt("auton_bottom"));
                 o.put("teleop_top", rs.getInt("teleop_top"));
                 o.put("teleop_middle", rs.getInt("teleop_middle"));
                 o.put("teleop_bottom", rs.getInt("teleop_bottom"));
                 o.put("teleop_pyramid", rs.getInt("teleop_pyramid"));
                 o.put("pyramid_level", rs.getInt("pyramid_level"));
                 o.put("play_style", rs.getString("play_style"));
                 o.put("confidence", rs.getInt("confidence"));
                 o.put("ability", rs.getInt("ability"));
                 o.put("fouls", rs.getBoolean("fouls"));
                 o.put("technical_fouls", rs.getBoolean("technical_fouls"));
                 o.put("comments", rs.getString("comments"));
                 o.put("path", rs.getString("path"));    // TODO path
                 
                 json.add(o);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
        return json.toString();
     }
     
     public int getId() {
         return this.id.intValue();
     }
 
     public void setId(int id) {
         this.id = Integer.valueOf(id);
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         if (!StringUtil.isBlank(user)) {
             this.user = user;
         } else {
             this.user = null;
         }
     }
 
     public int getTeamId() {
         return this.teamId.intValue();
     }
 
     public void setTeamId(int teamId) {
         this.teamId = Integer.valueOf(teamId);
     }
 
     public int getMatchId() {
         return this.matchId.intValue();
     }
 
     public void setMatchId(int matchId) {
         this.matchId = Integer.valueOf(matchId);
     }
 
     public String getColor() {
         return this.color;
     }
 
     public void setColor(String color) {
         if (!StringUtil.isBlank(color)) {
             this.color = color;
         } else {
             this.color = null;
         }
     }
 
     public int getAutonTop() {
         return this.autonTop.intValue();
     }
 
     public void setAutonTop(int autonTop) {
         this.autonTop = Integer.valueOf(autonTop);
     }
 
     public int getAutonMiddle() {
         return this.autonMiddle.intValue();
     }
 
     public void setAutonMiddle(int autonMiddle) {
         this.autonMiddle = Integer.valueOf(autonMiddle);
     }
 
     public int getAutonBottom() {
         return this.autonBottom.intValue();
     }
 
     public void setAutonBottom(int autonBottom) {
         this.autonBottom = Integer.valueOf(autonBottom);
     }
 
     public int getTeleopTop() {
         return this.teleopTop.intValue();
     }
 
     public void setTeleopTop(int teleopTop) {
         this.teleopTop = Integer.valueOf(teleopTop);
     }
 
     public int getTeleopMiddle() {
         return this.teleopMiddle.intValue();
     }
 
     public void setTeleopMiddle(int teleopMiddle) {
         this.teleopMiddle = Integer.valueOf(teleopMiddle);
     }
 
     public int getTeleopBottom() {
         return this.teleopBottom.intValue();
     }
 
     public void setTeleopBottom(int teleopBottom) {
         this.teleopBottom = Integer.valueOf(teleopBottom);
     }
 
     public int getTeleopPyramid() {
         return this.teleopPyramid.intValue();
     }
 
     public void setTeleopPyramid(int teleopPyramid) {
         this.teleopPyramid = Integer.valueOf(teleopPyramid);
     }
 
     public int getPyramidLevel() {
         return this.pyramidLevel.intValue();
     }
 
     public void setPyramidLevel(int pyramidLevel) {
         this.pyramidLevel = Integer.valueOf(pyramidLevel);
     }
 
     public String getPlayStyle() {
         return this.playStyle;
     }
 
     public void setPlayStyle(String playStyle) {
         if (!StringUtil.isBlank(playStyle)) {
             this.playStyle = playStyle;
         } else {
             this.playStyle = null;
         }
     }
 
     public int getConfidence() {
         return this.confidence.intValue();
     }
 
     public void setConfidence(int confidence) {
         this.confidence = Integer.valueOf(confidence);
     }
 
     public int getAbility() {
         return this.ability.intValue();
     }
 
     public void setAbility(int ability) {
         this.ability = Integer.valueOf(ability);
     }
 
     public boolean getFouls() {
         return this.fouls.booleanValue();
     }
 
     public void setFouls(boolean fouls) {
         this.fouls = Boolean.valueOf(fouls);
     }
 
     public boolean getTechnicalFouls() {
         return this.technicalFouls.booleanValue();
     }
 
     public void setTechnicalFouls(boolean technicalFouls) {
         this.technicalFouls = Boolean.valueOf(technicalFouls);
     }
 
     public String getComments() {
        if (StringUtil.isBlank(comments)) {
            return "";
        } else {
            return this.comments;
        }
     }
 
     public void setComments(String comments) {
         if (!StringUtil.isBlank(comments)) {
             this.comments = comments;
         } else {
             this.comments = null;
         }
     }
     
     public int getEventId() {
         return this.eventId.intValue();
     }
     
     public void setEventId(int id) {
         this.eventId = id;
     }
 
     public String getPath() {
         return this.path;
     }
 
     public void setPath(String path) {
         if (!StringUtil.isBlank(path)) {
             this.path = path;
         } else {
             this.path = null;
         }
     }
     
 }
