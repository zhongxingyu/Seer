 package rentalcar;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 import org.jdesktop.swingx.JXDatePicker;
 
 import com.mysql.jdbc.ResultSet;
 
 import core.DBConnection;
 import core.User.MemberUser;
 
 /**
  * @author Sahil Gupta This class is page for renting the car for members.
  */
 
 public class RentCarPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	final static Dimension screenSize = Toolkit.getDefaultToolkit()
 			.getScreenSize();
 
 	protected MemberUser member;
 	private JFrame mainFrame;
 	static DBConnection connection = new DBConnection();
 
 	JLabel RentACar;
 	JLabel PickUpTime, ReturnTime, Location, Cars;
 
 	@SuppressWarnings("rawtypes")
 	JComboBox PickUpTimeCombo, ReturnTimeCombo, LocationCombo, CarsCombo,
 			CarTypesCombo;
 	JXDatePicker PickUpDateCombo, ReturnDateCombo;
 
 	// Had to hard code it because there was no way out.
 	private String[] pickUpTimeStrings = { "12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM" };
 
 	private String[] returnTimeStrings = { "12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM" };
 
 	private String[] locationStrings;
 	private String[] carTypesStrings;
 	private String[] carsModelStrings;
 
 	JButton Search;
 
 	java.sql.Date PickUpTimeDate, ReturnTimeDate;
 	String pickUpTimeString, returnTimeString;
 
 	String locationString, carTypeString, carModelString, VehicleNumber;
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public RentCarPanel(MemberUser member) {
 		this.setBackground(Color.green);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.setBounds(300, 150, 450, 400);
 		this.member = member;
 
 		RentACar = new JLabel("Rent A Car");
 		RentACar.setFont(new Font("Helvetica", Font.BOLD, 70));
 
 		PickUpTime = new JLabel("Pick Up Time:");
 		java.util.Date pickdate = new java.util.Date();
 		PickUpDateCombo = new JXDatePicker(pickdate);
 		PickUpTimeCombo = new JComboBox(pickUpTimeStrings);
 
 		ReturnTime = new JLabel("Return Time:");
 		java.util.Date returndate = new java.util.Date();
 		ReturnDateCombo = new JXDatePicker(returndate);
 		ReturnTimeCombo = new JComboBox(returnTimeStrings);
 
 		Connection conn = connection.createConnection();
 		try {
 			String statement = "SELECT distinct Car_Type FROM Car";
 			PreparedStatement prep = conn.prepareStatement(statement);
 			ResultSet rs = (ResultSet) prep.executeQuery();
 
 			int rowcount = 0;
 			if (rs.last()) {
 				rowcount = rs.getRow();
 				rs.beforeFirst();
 			}
 			carTypesStrings = new String[rowcount];
 
 			int i = 0;
 			while (rs.next()) {
 				carTypesStrings[i++] = rs.getString("Car_Type");
 			}
 			prep.close();
 
 			String statement1 = "SELECT DISTINCT Model_Name FROM Car";
 			PreparedStatement prep1 = conn.prepareStatement(statement1);
 			ResultSet rs1 = (ResultSet) prep1.executeQuery();
 
 			rowcount = 0;
 			if (rs1.last()) {
 				rowcount = rs1.getRow();
 				rs1.beforeFirst();
 			}
 			carsModelStrings = new String[rowcount];
 
 			i = 0;
 			while (rs1.next()) {
 				carsModelStrings[i++] = rs1.getString("Model_Name");
 			}
 			prep1.close();
 
 			String statement2 = "SELECT distinct Location_Name FROM Car";
 			PreparedStatement prep2 = conn.prepareStatement(statement2);
 			ResultSet rs2 = (ResultSet) prep2.executeQuery();
 
 			rowcount = 0;
 			if (rs2.last()) {
 				rowcount = rs2.getRow();
 				rs2.beforeFirst();
 			}
 			locationStrings = new String[rowcount];
 
 			i = 0;
 			while (rs2.next()) {
 				locationStrings[i++] = rs2.getString("Location_Name");
 			}
 			prep2.close();
 
 			connection.closeConnection(conn);
 		} catch (SQLException e) {
 			connection.closeConnection(conn);
 		}
 
 		this.add(RentACar);
 
 		this.add(PickUpTime);
 		this.add(PickUpDateCombo);
 		this.add(PickUpTimeCombo);
 
 		this.add(ReturnTime);
 		this.add(ReturnDateCombo);
 		this.add(ReturnTimeCombo);
 
 		Location = new JLabel("Location:");
 		this.add(Location);
 		LocationCombo = new JComboBox(locationStrings);
 		this.add(LocationCombo);
 
 		Cars = new JLabel("Cars:");
 		this.add(Cars);
 		CarTypesCombo = new JComboBox(carTypesStrings);
 		this.add(CarTypesCombo);
 
 		CarsCombo = new JComboBox(carsModelStrings);
 		this.add(CarsCombo);
 
 		Search = new JButton("Search");
 		Search.addActionListener(new SearchButtonListener());
 
 		this.add(Search);
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	private class SearchButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent event) {
 			pickUpTimeString = (String) PickUpTimeCombo.getSelectedItem();
 			returnTimeString = (String) ReturnTimeCombo.getSelectedItem();
 
 			StringBuilder sb = new StringBuilder();
 			String date = PickUpDateCombo.getDate().toString();
 			sb.append(date.substring(24, 28));
 			sb.append('-');
 			String month = new Integer(PickUpDateCombo.getDate().getMonth() + 1)
 					.toString();
 			if (month.length() == 1) {
 				sb.append('0');
 			}
 			sb.append(month);
 			sb.append('-');
 			sb.append(date.substring(8, 10));
 			sb.append(' ');
 			String[] str = pickUpTimeString.substring(0, 5).split(":");
 			if (str[0].length() == 1) {
 				sb.append('0').append(str[0]).append(':')
 						.append(pickUpTimeString.substring(2, 4));
 			} else {
 				sb.append(str[0]).append(':').append(str[1]);
 			}
 			sb.append(":00");
 			java.util.Date result;
 			SimpleDateFormat formatter = new SimpleDateFormat(
 					"yyyy-MM-dd hh:mm:ss");
 			try {
 				result = formatter.parse(sb.toString());
 				PickUpTimeDate = new java.sql.Date(result.getTime());
 			} catch (ParseException e1) {
 				e1.printStackTrace();
 			}
 
 			StringBuilder sb1 = new StringBuilder();
 			String date1 = ReturnDateCombo.getDate().toString();
 			sb1.append(date1.substring(24, 28));
 			sb1.append('-');
 			String month1 = new Integer(
 					ReturnDateCombo.getDate().getMonth() + 1).toString();
 			if (month1.length() == 1) {
 				sb1.append('0');
 			}
 			sb1.append(month1);
 			sb1.append('-');
 			sb1.append(date1.substring(8, 10));
 			sb1.append(' ');
 			if (month1.length() == 1) {
 				sb1.append('0');
 			}
 			String[] str1 = returnTimeString.substring(0, 5).split(":");
 			if (str1[0].length() == 1) {
 				sb1.append('0').append(str1[0]).append(':')
 						.append(returnTimeString.substring(2, 4));
 			} else {
 				sb1.append(str1[0]).append(':').append(str1[1]);
 			}
 			sb1.append(":00");
 			java.util.Date result1;
 			SimpleDateFormat formatter1 = new SimpleDateFormat(
 					"yyyy-MM-dd hh:mm:ss");
 			try {
 				result1 = formatter1.parse(sb1.toString());
 				ReturnTimeDate = new java.sql.Date(result1.getTime());
 			} catch (ParseException e1) {
 				e1.printStackTrace();
 			}
 			long diff = (ReturnTimeDate.getTime() - PickUpTimeDate.getTime());
 			if (PickUpTimeDate.compareTo(ReturnTimeDate) > 0) {
 				JOptionPane
 						.showMessageDialog(
 								new JFrame(),
 								"Return time is before pick up time! Please choose a another return/pickup time",
 								"Inane error", JOptionPane.ERROR_MESSAGE);
 			} else if (diff > 172800000) {
 				JOptionPane
 						.showMessageDialog(
 								new JFrame(),
 								"You cannot book a car for more than 2 days! Please choose a another return time before.",
 								"Inane error", JOptionPane.ERROR_MESSAGE);

 			} else {
 				locationString = (String) LocationCombo.getSelectedItem();
 				carTypeString = (String) CarTypesCombo.getSelectedItem();
 				carModelString = (String) CarsCombo.getSelectedItem();
 				String statement = "SELECT Model_Name, Car_Type, Location_Name, "
 						+ "Color, Hourly_Rate, Daily_Rate, Seating_Cap, Transmission_Type, Bluetooth, "
 						+ "Auxiliary_Cable, Estimated_Cost, Vehicle_Sno FROM Car NATURAL JOIN Reservation WHERE Car_Type = ? "
 						+ "AND Model_Name = ? AND Car.Location_Name = ? AND "
 						+ "? > ?";
 				Connection conn = connection.createConnection();
 				Object[][] rowDataArr = null;
 				int rowcount = 0;
 
 				try {
 					PreparedStatement prep = conn.prepareStatement(statement);
 					prep.setString(1, carTypeString);
 					prep.setString(2, carModelString);
 					prep.setString(3, locationString);
 					prep.setDate(4, PickUpTimeDate);
 					prep.setDate(5, ReturnTimeDate);
 					ResultSet rsCarAvailability = (ResultSet) prep
 							.executeQuery();
 					if (rsCarAvailability.last()) {
 						rowcount = rsCarAvailability.getRow();
 						rsCarAvailability.beforeFirst();
 					}
 					rowDataArr = new Object[rowcount][15];
 					rowcount = 0;
 					int i = 0;
 					while (rsCarAvailability.next()) {
 						rowDataArr[i][0] = rsCarAvailability
 								.getString("Model_Name");
 						rowDataArr[i][1] = rsCarAvailability
 								.getString("Car_Type");
 						rowDataArr[i][2] = rsCarAvailability
 								.getString("Location_Name");
 						rowDataArr[i][3] = rsCarAvailability.getString("Color");
 						rowDataArr[i][4] = rsCarAvailability
 								.getString("Hourly_Rate");
 						rowDataArr[i][7] = rsCarAvailability
 								.getString("Daily_Rate");
 						rowDataArr[i][8] = rsCarAvailability
 								.getString("Seating_Cap");
 						rowDataArr[i][9] = rsCarAvailability
 								.getString("Transmission_Type");
 						rowDataArr[i][10] = rsCarAvailability
 								.getString("Bluetooth");
 						rowDataArr[i][11] = rsCarAvailability
 								.getString("Auxiliary_Cable");
 						rowDataArr[i][13] = rsCarAvailability
 								.getString("Estimated_Cost");
 						rowDataArr[i][14] = new JRadioButton();
 						VehicleNumber = rsCarAvailability
 								.getString("Vehicle_Sno");
 						i++;
 					}
 					prep.close();
 					connection.closeConnection(conn);
 				} catch (SQLException e) {
 					e.printStackTrace();
 					connection.closeConnection(conn);
 				}
 
 				String statement1 = "SELECT Pick_Up_Date_Time AS Available_till, "
 						+ "FROM Car NATURAL JOIN Reservation WHERE Car_Type = ? AND "
 						+ "Model_Name = ? AND Car.Location_Name = ? AND "
 						+ "? > ?";
 				try {
 					PreparedStatement prep1 = conn.prepareStatement(statement1);
 					prep1.setString(1, carTypeString);
 					prep1.setString(2, carModelString);
 					prep1.setString(3, locationString);
 					prep1.setDate(4, PickUpTimeDate);
 					prep1.setDate(5, ReturnTimeDate);
 					ResultSet rsCarAvailableTill = (ResultSet) prep1
 							.executeQuery();
 					if (rsCarAvailableTill.last()) {
 						rowcount = rsCarAvailableTill.getRow();
 						rsCarAvailableTill.beforeFirst();
 					}
 					int j = 0;
 					while (rsCarAvailableTill.next()) {
 						rowDataArr[j][12] = rsCarAvailableTill
 								.getString("Available_till");
 						j++;
 					}
 					prep1.close();
 					connection.closeConnection(conn);
 				} catch (SQLException e) {
 					connection.closeConnection(conn);
 				}
 
 				String statement2 = "CREATE VIEW DISCOUNTED AS "
 						+ "SELECT  f.Discount AS Frequent_DRate, d.Discount as Daily_DRate "
 						+ "FROM Driving_Plan AS f, Driving_Plan AS d WHERE  "
 						+ "f.Plan_Type = \"Frequent Driving\" AND "
 						+ "d.Plan_Type = \"Daily Driving\"";
 				try {
 					PreparedStatement prep2 = conn.prepareStatement(statement2);
 					ResultSet rsDiscountedRate = (ResultSet) prep2
 							.executeQuery();
 					if (rsDiscountedRate.last()) {
 						rowcount = rsDiscountedRate.getRow();
 						rsDiscountedRate.beforeFirst();
 					}
 					int j = 0;
 					while (rsDiscountedRate.next()) {
 						rowDataArr[j][5] = rsDiscountedRate
 								.getString("Frequent_DRate");
 						rowDataArr[j][6] = rsDiscountedRate
 								.getString("Daily_DRate");
 						j++;
 					}
 					prep2.close();
 					connection.closeConnection(conn);
 				} catch (SQLException e) {
 					connection.closeConnection(conn);
 				}
 
 				if (rowcount == 0) {
 					JOptionPane
 							.showMessageDialog(
 									new JFrame(),
 									"No Car Available For these Times. Please enter a new pick up time or return time!",
 									"Inane error", JOptionPane.ERROR_MESSAGE);
 				} else {
 					ArrayList<Object> tempArr = new ArrayList();
 					tempArr.add(PickUpTimeDate);
 					tempArr.add(ReturnTimeDate);
 					tempArr.add(VehicleNumber);
 					tempArr.add(locationString);
 					CarAvailPanel panel = new CarAvailPanel(member, rowDataArr,
 							rowcount, tempArr);
 					mainFrame.setContentPane(panel);
 					mainFrame.setBounds(mainFrame.getContentPane().getBounds());
 					mainFrame.setVisible(true);
 					mainFrame.repaint();
 				}
 			}

 		}
 	}
 }
