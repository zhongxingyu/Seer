 package rentalcar;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import core.DBConnection;
 import core.User.MemberUser;
 
 public class CarAvailPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	MemberUser member;
 	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	int DIALOGWIDTH = 500, DIALOGHEIGHT = 500;
 	static DBConnection connection = new DBConnection();
 
 	Object[][] rowData;
 
 	Object[] columnNames = { "Model_Name", "Car_Type", "Location_Name",
 			"Color", "Hourly_Rate", "Daily_Rate", "Seating_Cap",
 			"Transmission_Type", "Bluetooth", "Auxiliary_Cable",
 			"Estimated_Cost", "Annual_Fees" };
 	
 	ArrayList<Object> CarData;
 	JTable table;
 	ButtonGroup group;
 
 	JLabel pageHeading;
 	JButton reserveButton;
 	String ReservationTime;
 	JRadioButton Selected;
 
 	int numRows;
 
 	public CarAvailPanel(MemberUser member, Object[][] rowData, int numRows, ArrayList<Object> tempArr) {
 		this.CarData = tempArr;
 		this.member = member;
 		this.numRows = numRows;
 		this.rowData = rowData;
 		this.setBounds(screenSize.width / 3, screenSize.height / 3,
 				DIALOGWIDTH, DIALOGHEIGHT);
 
 		pageHeading = new JLabel("Car Availability");
 		pageHeading.setFont(new Font("Helvetica", Font.BOLD, 70));
 
 		table = new JTable(rowData, columnNames);
 
 		for (int i = 0; i < numRows; i++) {
 			group.add((JRadioButton) rowData[i][14]);
 		}
 
 		reserveButton = new JButton("Reserve");
 		reserveButton.addActionListener(new ReserveButtonListener());
 
 		this.setLayout(new BorderLayout());
 		this.add(pageHeading, BorderLayout.NORTH);
 		this.add(new JScrollPane(table), BorderLayout.CENTER);
 
 		this.setBackground(Color.green);
 		this.setBounds(400, 300, 500, 200);
 	}
 
 	private class ReserveButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent event) {
 			for (int i = 0; i < numRows; i++) {
 				Selected = (JRadioButton) rowData[i][14];
 				if (Selected.isSelected()) {
 					String statement = "INSERT INTO Reservation VALUES( ? , ? , ? , ? , ? , 0 , 0 , 0 , 0 )";
 					Connection conn = connection.createConnection();
 					try {
 						PreparedStatement prep = conn
 								.prepareStatement(statement);
 						prep.setString(1, (String) member.getUsername());
 						prep.setDate(2, (java.sql.Date) CarData.get(0));
 						prep.setDate(3, (java.sql.Date) CarData.get(1));
 						prep.setString(4, (String)  CarData.get(2));
 						prep.setString(5, (String) CarData.get(3));
 						prep.close();
 						connection.closeConnection(conn);
 					} catch (SQLException e) {
 						connection.closeConnection(conn);
 					}
 
 				}
 			}
 		}
 	}

 }
