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
 import core.User.AdminUser;
 
 public class AdminRevenuePanel extends JPanel {
     private static final long serialVersionUID = 1L;
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     DBConnection connection = new DBConnection();
 
     AdminUser admin;
 
     Object[] tableElement;
     Object[] columnNames = { "Vehicle Sno", "Type", "Car Model",
             "Reservation revenue", "Late Fees Reveue" };
     Object[][] rowData;
 
     JTable table;
     JLabel pageHeading;
 
     public AdminRevenuePanel(AdminUser admin) {
         this.admin = admin;
 
         pageHeading = new JLabel("Revenue Generated");
         pageHeading.setFont(new Font("Helvetica", Font.BOLD, 70));
 
         Connection conn = connection.createConnection();
         try {
             String statement = "SELECT  Vehicle_Sno, Car_Type, Model_Name, SUM(Estimated_Cost)" +
             " AS Reservation_Revenue, SUM(Late_Fees) FROM Car NATURAL JOIN Reservation " +
             "WHERE 0 <= DATEDIFF( CURRENT_DATE, Return_Date_Time ) /30 <=3 AND " +
            "Return_Date_Time < CURRENT_DATE GROUP BY Vehicle_Sno";
 
             PreparedStatement prep = conn.prepareStatement(statement);
             ResultSet rs = (ResultSet) prep.executeQuery();
             int rowcount = 0;
             if (rs.last()) {
                 rowcount = rs.getRow();
                 rs.beforeFirst();
             }
             rowData = new Object[rowcount][5];
             for(int i = 0; rs.next(); i++){
                 rowData[i][0] = rs.getString("Vehicle_Sno");
                 rowData[i][1] = rs.getString("Car_Type");
                 rowData[i][2] = rs.getString("Model_Name");
                 rowData[i][3] = rs.getString("Reservation_Revenue");
                rowData[i][4] = rs.getString(5);
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
