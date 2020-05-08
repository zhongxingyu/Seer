 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.HMM_Model;
 
 /**
  *
  * @author anu
  */
 import java.sql.*;
 import javax.swing.*;
 import java.util.*;
 import javax.swing.tree.TreeNode;
 import java.lang.String;
 
 public class DBConnect {
 
     private Connection con;
     private Statement st;
     private ResultSet rs;
     private boolean isConnected = false;
 
     public DBConnect(String ip, String port, String password, String username, String db, JLabel jLabel_ConnectToDBStatus, String connectionName, JComboBox jComboBox_RecentDBList) {
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             String connectorStr = "jdbc:mysql://" + ip + ":" + port + "/" + db;
             con = DriverManager.getConnection(connectorStr, username, password);
             isConnected = true;
             jLabel_ConnectToDBStatus.setText("Connected");
         } catch (ClassNotFoundException | SQLException ex) {
             isConnected = false;
             JOptionPane.showMessageDialog(null, "Could not connect to the database.\nPlease ensure that the database credentials are correct.");
             jLabel_ConnectToDBStatus.setText("Not Connected");
         }
     }
 
     public boolean isConnected() {
 
         return isConnected;
     }
 
     public Statement createStatement() {
         try {
             st = con.createStatement();
 
         } catch (SQLException ex) {
         }
         return st;
     }
 
     public ResultSet getData(String query, Statement st) {
 
         try {
 
             rs = st.executeQuery(query);
 
 
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, ex);
         }
         return rs;
     }
 
     public void closeDBConnect() {
         if (con != null) {
             try {
 
                 con.close();
 
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(null, ex);
             }
 
         }
     }
     
    public void updateConnectionIcon(JLabel jLabel_ConnectedIcon, JLabel jLabel_ConnectToDBStatus) {
         // Jamie: update connection icon
         if (isConnected) {
              jLabel_ConnectedIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/HMM_Model/DB-connected.png")));
              jLabel_ConnectToDBStatus.setText("Connected");
         }
         else {
              jLabel_ConnectedIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/HMM_Model/DB-idle.png"))); 
              jLabel_ConnectToDBStatus.setText("Connected");
         }
         
     }
     
     
 }
