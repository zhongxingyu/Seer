 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ObservationQuestion {
     static int seqNum;
 
     static {
         seqNum = 0;
     }
 
     int qid;
     String text;
     int otid;
 
     private ObservationQuestion(int qid, String text, int otid) {
         this.qid = qid;
         this.text = text;
         this.otid = otid;
     }
 
     private static void setSeqNum(MyConnection connection) throws SQLException {
         if(seqNum == 0) {
             String query = "SELECT COALESCE(MAX(qid), 0) as qid from ObservationQuestion";
             ResultSet resultSet = connection.stmt.executeQuery(query);
             while (resultSet.next())
                 seqNum = resultSet.getInt("qid") + 1;
         }
     }
 
     static ObservationQuestion getById(int qid, MyConnection conn) throws MyException {
         try {
             String query = "select * from ObservationQuestion where qid = " + qid;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next())
                 return new ObservationQuestion(rs.getInt("otid"), rs.getString("text"), rs.getInt("otid"));
 
         } catch (SQLException e) {
             throw new MyException("Could not get observation question with qid = " + qid);
         }
         return null;
     }
 
     static List<ObservationQuestion> getByObservationType(int otid, MyConnection conn) throws MyException {
         try {
             String query = "select * from ObservationQuestion where otid = " + otid;
             ResultSet rs = conn.stmt.executeQuery(query);
             ArrayList<ObservationQuestion> questions = new ArrayList<ObservationQuestion>();
             while (rs.next())
                 questions.add(new ObservationQuestion(rs.getInt("qid"), rs.getString("text"), rs.getInt("otid")));
             return questions;
 
         } catch (SQLException e) {
             throw new MyException("Could not get questions for observation type " + otid);
         }
     }
 
     static int insert(String text, int otid, MyConnection conn) throws MyException {
         try {
             setSeqNum(conn);
             String query = "INSERT INTO ObservationQuestion values(?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, seqNum);
             pstmt.setString(2, text);
             pstmt.setInt(3, otid);
             int ret = pstmt.executeUpdate();
 
             if(ret != 0)
                 return seqNum++;
 
         } catch (SQLException e) {
             throw new MyException("Insertion of question with text " + text + " failed");
         }
         return 0;
     }
 
     static int insertByTypeName(String name, String text, MyConnection conn) throws MyException {
         int otid = ObservationType.getIdFromName(name, conn);
         if(otid != 0){
            insert(text, otid, conn);
         }
        return otid;
     }
 }
