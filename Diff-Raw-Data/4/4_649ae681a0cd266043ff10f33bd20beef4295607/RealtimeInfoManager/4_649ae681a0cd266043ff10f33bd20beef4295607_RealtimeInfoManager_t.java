 package timing;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import timing.ComputeKeyWord.WeightedWord;
 
 import db.DBInterface;
 
 public class RealtimeInfoManager {
     private static DBInterface db = new DBInterface();
     private static ComputeKeyWord ckw = new ComputeKeyWord();
 
     public static void update(String target) {
         try {
             Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("select text, segment from weibo where target='" + target + "';");
             ArrayList<String> texts = new  ArrayList<String>();
             ArrayList<String> segs = new ArrayList<String>();
             while(rs.next()) {
                 String text = rs.getString(1);
                 String seg = rs.getString(2);
                 texts.add(text);
                 segs.add(seg);
             }
             st.close();
 
             String insql="INSERT INTO keyword (target, keyword) VALUES (?, ?) ON DUPLICATE KEY UPDATE keyword = ?";
             PreparedStatement ps = null;
             try {
                 ps = db.getConnection().prepareStatement(insql);
             } catch (SQLException e) {
                 e.printStackTrace();
             }
             List<WeightedWord> keyWords = ckw.compute(texts, segs);
             StringBuilder keywordBuilder = new StringBuilder();
             for (WeightedWord ww : keyWords) {
                 keywordBuilder.append(ww.word + "::=" + ww.score + "::;");
             }
             ps.setString(1, target);
             ps.setString(2, keywordBuilder.toString());
             ps.setString(3, keywordBuilder.toString());
             ps.executeUpdate();
             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         try {
             Statement st = db.getConnection().createStatement();
            ResultSet rs = st.executeQuery("select distinct(target) from weibo");
             while(rs.next()) {
                System.out.println("UPDATING real-time information for " + rs.getString(1));
                 update(rs.getString(1));
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 }
