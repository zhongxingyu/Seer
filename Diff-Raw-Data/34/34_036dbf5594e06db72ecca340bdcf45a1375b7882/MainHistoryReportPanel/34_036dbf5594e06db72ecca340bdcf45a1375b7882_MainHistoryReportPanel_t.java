 package rentalcar;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import com.mysql.jdbc.ResultSet;
 
 import core.DBConnection;
 import core.User.EmployeeUser;
 
 public class MainHistoryReportPanel extends JPanel {
     private static final long serialVersionUID = 1L;
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     DBConnection connection = new DBConnection();
 
     EmployeeUser employee;
 
     Object[] tableElement;
     Object[] columnNames = { "Car" , "Date-Time", "Employee", "Problem"};
     Object[][] rowData;
 
     JTable table;
     JLabel pageHeading;
 
     public MainHistoryReportPanel(EmployeeUser employee) {
         this.employee = employee;
 
        pageHeading = new JLabel("Maintenance History Report");
         pageHeading.setFont(new Font("Helvetica", Font.BOLD, 70));
 
         Connection conn = connection.createConnection();
         try {
             String statement = "SELECT Model_Name,Date_Time,Username,Problems " +
                 "FROM Car NATURAL JOIN Maintenance_Request NATURAL JOIN Problems NATURAL JOIN NUM_PROBLEMS " +
                 "ORDER BY Num_Prob DESC";
 
             PreparedStatement prep = conn.prepareStatement(statement);
             ResultSet rs = (ResultSet) prep.executeQuery();
             int rowcount = 0;
             if (rs.last()) {
                 rowcount = rs.getRow();
                 rs.beforeFirst();
             }
            rowData = new Object[rowcount][4];
             for(int i = 0; rs.next(); i++){
                 rowData[i][0] = rs.getString("Model_Name");
                 rowData[i][1] = rs.getString("Date_Time");
                 rowData[i][2] = rs.getString("Username");
                rowData[i][3] = rs.getString("Problems");
             }
             prep.close();
             connection.closeConnection(conn);
         } catch (SQLException e) {
             connection.closeConnection(conn);
         }
 
         table = new JTable(rowData, columnNames);
 
         this.setLayout(new BorderLayout());
         this.add(pageHeading, BorderLayout.NORTH);
         this.add(new JScrollPane(table), BorderLayout.CENTER);
 
         this.setBackground(Color.green);
        this.setBounds(0, 0, screenSize.width, screenSize.height);
     }
 }
