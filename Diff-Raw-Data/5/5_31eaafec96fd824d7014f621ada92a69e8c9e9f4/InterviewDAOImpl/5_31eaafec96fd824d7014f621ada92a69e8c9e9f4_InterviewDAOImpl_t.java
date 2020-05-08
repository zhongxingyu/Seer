 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.dao;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author lastride
  */
 public class InterviewDAOImpl implements InterviewDAO {
 
     private static final Logger log =
             Logger.getLogger(InterviewDAOImpl.class.getName());
 
     @Override
     public boolean createInterview(Interview interview) {
         PreparedStatement stmtInsert = null;
         Connection conn = DAOFactory.createConnection();
         try {
             StringBuilder sbInsert = new StringBuilder();
             sbInsert.append("INSERT INTO ");
             sbInsert.append(DAOConstants.ResultsTableName);
             sbInsert.append(" (id_student, id_employee, comment)");
             sbInsert.append(" VALUES (");
             sbInsert.append("?, ?, ?)");
             stmtInsert = conn.prepareStatement(sbInsert.toString());
             stmtInsert.setInt(1, interview.getIdStudent());
             stmtInsert.setInt(2, interview.getIdEmployee());
             stmtInsert.setString(3, interview.getComment());
 
             int rows = stmtInsert.executeUpdate();
             if (rows != 1) {
                 log.info("Error in createInterview");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtInsert);
         }
         return true;
     }
 
     @Override
     public boolean deleteInterviewByIdStudent(int idStudent) {
         Connection conn = DAOFactory.createConnection();
         PreparedStatement stmtDelete = null;
         try {
             StringBuilder sbDelete = new StringBuilder();
             sbDelete.append("DELETE FROM ");
             sbDelete.append(DAOConstants.ResultsTableName);
             sbDelete.append(" WHERE id_student = ?");
             stmtDelete = conn.prepareStatement(sbDelete.toString());
             stmtDelete.setInt(1, idStudent);
             stmtDelete.executeUpdate();
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtDelete);
         }
         return true;
     }
 
     @Override
     public List<String> getInterviewsResultByStudentId(int idStudent) {
         List<String> comments = new ArrayList<String>();
         Connection conn = DAOFactory.createConnection();
         Statement stmtSelect = null;
         ResultSet res = null;
         String comment = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append(
                     "SELECT " + DAOConstants.ResultsTableName + ".comment "
                     + "FROM " + DAOConstants.ResultsTableName
                     + " WHERE " + DAOConstants.ResultsTableName
                     + ".id_student = ").append(idStudent);
             stmtSelect = conn.createStatement();
             res = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (res.next()) {
                 comment = res.getString(1);
                 comments.add(comment);
                 rowsCount++;
             }
             if (rowsCount <= 0) {
                 log.info("No comments found");
             }
             if (rowsCount > 2) {
                 log.info("Student have more than 2 comments");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return comments;
     }
 
     @Override
     public List<String> getInterviewsResultByEmployeeId(int idEmployee) {
         List<String> comments = new ArrayList<String>();
         Connection conn = DAOFactory.createConnection();
         Statement stmtSelect = null;
         ResultSet res = null;
         String comment = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append(
                     "SELECT " + DAOConstants.ResultsTableName + ".id_comment "
                     + "FROM " + DAOConstants.ResultsTableName
                     + " WHERE " + DAOConstants.ResultsTableName
                     + ".id_employee = ").append(idEmployee);
             stmtSelect = conn.createStatement();
             System.out.print(sbSelect.toString());
             res = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (res.next()) {
                 comment = res.getString(1);
                 comments.add(comment);
                 rowsCount++;
             }
             if (rowsCount <= 0) {
                 log.info("This employee did no comment");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return comments;
     }
 
     @Override
     public boolean updateInterviewsResultByIdStudent(int idStudent, int idFirstEmployee, int idSecondEmployee, String firstComment, String secondComment) {
         PreparedStatement stmtFirstUpdate = null;
         PreparedStatement stmtSecondUpdate = null;
         Connection conn = DAOFactory.createConnection();
         try {
             StringBuilder sbFirstUpdate = new StringBuilder();
             sbFirstUpdate.append("UPDATE ");
             sbFirstUpdate.append(DAOConstants.ResultsTableName);
             sbFirstUpdate.append(" SET comment = ?");
            sbFirstUpdate.append(" WHERE id_student = ? AND id_employee = ?");
             stmtFirstUpdate = conn.prepareStatement(sbFirstUpdate.toString());
             stmtFirstUpdate.setString(1, firstComment);
             stmtFirstUpdate.setInt(2, idStudent);
             stmtFirstUpdate.setInt(3, idFirstEmployee);
             int rowsFirst = stmtFirstUpdate.executeUpdate();
             if (rowsFirst != 1) {
                 log.info("Error in updateInterviewsResultByIdStudent");
             }
 
             StringBuilder sbSecondUpdate = new StringBuilder();
             sbSecondUpdate.append("UPDATE ");
             sbSecondUpdate.append(DAOConstants.ResultsTableName);
             sbSecondUpdate.append(" SET comment = ?");
            sbSecondUpdate.append(" WHERE id_student = ? AND id_employee = ?");
             stmtSecondUpdate = conn.prepareStatement(sbSecondUpdate.toString());
             stmtSecondUpdate.setString(1, secondComment);
             stmtSecondUpdate.setInt(2, idStudent);
             stmtSecondUpdate.setInt(3, idSecondEmployee);
             int rowsSecond = stmtSecondUpdate.executeUpdate();
             if (rowsSecond != 1) {
                 log.info("Error in updateInterviewsResultByIdStudent");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtFirstUpdate);
             DAOFactory.closePreparedStatement(stmtSecondUpdate);
         }
         return true;
     }
 }
