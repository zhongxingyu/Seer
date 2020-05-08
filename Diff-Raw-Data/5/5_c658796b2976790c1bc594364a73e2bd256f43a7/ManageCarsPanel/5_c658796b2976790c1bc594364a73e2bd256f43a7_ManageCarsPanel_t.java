 package rentalcar;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.mysql.jdbc.ResultSet;
 
 import core.DBConnection;
 import core.User.EmployeeUser;
 
 public class ManageCarsPanel extends JPanel {
     private static final long serialVersionUID = 1L;
     final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     EmployeeUser employee;
     DBConnection connection = new DBConnection();
 
     JLabel ManageCars, AddACar, ChangeCarLocation, BriefDescription;
 
     JLabel VehicleSno, CarModel, CarType, Location, Color,
     HourlyRate, DailyRate, SeatingCapacity, TransmissionType,
     BluetoothConnectivity, AuxilliaryCable;
     JTextField VehicleSnoTextField, CarModelTextField, ColorTextField,
     HourlyRateTextField, DailyRateTextField, SeatingCapacityTextField;
 
    JComboBox CarTypeCombo, LocationCombo, TransmissionTypeCombo, BluetoothConnectivityCombo, AuxilliaryCableCombo;
 
     JButton add;
 
     private String[] carTypeStrings, locationStrings, transmissionTypeStrings, bluetoothConnectivityStrings, auxilliaryCableStrings;
 
     JLabel CurrentLocation, Car, CarType2, Color2,
     SeatingCapacity2, TransmissionType2, NewLocation;
     JTextField CarType2TextField, Color2TextField,SeatingCapacity2TextField,
     TransmissionType2TextField;
    JComboBox CurrentLocationCombo, CarCombo, NewLocationCombo;
 
     private String[] carStrings;
 
     JButton submitChanges;
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public ManageCarsPanel(EmployeeUser employee) {
         this.setEmployee(employee);
         this.setBounds(50, 50, 1300, screenSize.height-100);
 
         this.setBackground(java.awt.Color.green);
         this.setLayout(new BorderLayout());
 
         ManageCars = new JLabel("Manage Cars");
         ManageCars.setFont(new Font("Helvetica", Font.BOLD, 70));
 
         this.add(ManageCars, BorderLayout.NORTH);
 
         JPanel TwoPanel = new JPanel();
         TwoPanel.setLayout(new GridLayout(1,2));
 
         JPanel AddCar = new JPanel();
         AddCar.setLayout(new BoxLayout(AddCar, BoxLayout.PAGE_AXIS));
 
         AddACar = new JLabel("Add A Car");
         AddACar.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         VehicleSno = new JLabel("Vehicle Sno:");
         CarModel = new JLabel("Car Model:");
         CarType = new JLabel("Car Type:");
         Location = new JLabel("Location:");
         Color = new JLabel("Color:");
         HourlyRate = new JLabel("Hourly Rate:");
         DailyRate = new JLabel("Daily Rate:");
         SeatingCapacity = new JLabel("Seating Capacity:");
         TransmissionType = new JLabel("Transmission Type:");
         BluetoothConnectivity = new JLabel("Bluetooth Connectivity:");
         AuxilliaryCable = new JLabel("Auxilliary Cable:");
 
         VehicleSnoTextField = new JTextField(20);
         CarModelTextField = new JTextField(30);
         ColorTextField = new JTextField(10);	
         HourlyRateTextField = new JTextField(10);
         DailyRateTextField = new JTextField(10);
         SeatingCapacityTextField = new JTextField(10);
         
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
 			carTypeStrings = new String[rowcount];
 
 			int i = 0;
 			while (rs.next()) {
 				carTypeStrings[i++] = rs.getString("Car_Type");
 			}
 			prep.close();
 
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
 			
 			String statement3 = "SELECT distinct Transmission_Type FROM Car";
 			PreparedStatement prep3 = conn.prepareStatement(statement3);
 			ResultSet rs3 = (ResultSet) prep3.executeQuery();
 
 			rowcount = 0;
 			if (rs3.last()) {
 				rowcount = rs3.getRow();
 				rs3.beforeFirst();
 			}
 			transmissionTypeStrings = new String[rowcount];
 
 			i = 0;
 			while (rs3.next()) {
 				transmissionTypeStrings[i++] = rs3.getString("Transmission_Type");
 			}
 			prep3.close();
 			
 			String statement4 = "SELECT distinct Bluetooth FROM Car";
 			PreparedStatement prep4 = conn.prepareStatement(statement4);
 			ResultSet rs4 = (ResultSet) prep4.executeQuery();
 
 			rowcount = 0;
 			if (rs4.last()) {
 				rowcount = rs4.getRow();
 				rs4.beforeFirst();
 			}
 			bluetoothConnectivityStrings = new String[rowcount];
 
 			i = 0;
 			while (rs4.next()) {
 				bluetoothConnectivityStrings[i++] = rs4.getString("Bluetooth");
 			}
 			prep4.close();
 			
 			String statement5 = "SELECT distinct Auxiliary_Cable FROM Car";
 			PreparedStatement prep5 = conn.prepareStatement(statement5);
 			ResultSet rs5 = (ResultSet) prep5.executeQuery();
 
 			rowcount = 0;
 			if (rs5.last()) {
 				rowcount = rs5.getRow();
 				rs5.beforeFirst();
 			}
 			auxilliaryCableStrings = new String[rowcount];
 
 			i = 0;
 			while (rs5.next()) {
 				auxilliaryCableStrings[i++] = rs5.getString("Auxiliary_Cable");
 			}
 			prep5.close();
 			
 			String statement6 = "SELECT distinct Model_Name FROM Car";
 			PreparedStatement prep6 = conn.prepareStatement(statement6);
 			ResultSet rs6 = (ResultSet) prep6.executeQuery();
 			
 			rowcount = 0;
 			if (rs6.last()) {
 				rowcount = rs6.getRow();
 				rs6.beforeFirst();
 			}
 			carStrings = new String[rowcount];
 
 			i = 0;
 			while (rs6.next()) {
 				carStrings[i++] = rs6.getString("Model_Name");
 			}
 			prep6.close();
 			
 			connection.closeConnection(conn);
 		} catch (SQLException e) {
 			connection.closeConnection(conn);
 		}
 		
         CarTypeCombo = new JComboBox(carTypeStrings);
         LocationCombo = new JComboBox(locationStrings);
         
         TransmissionTypeCombo = new JComboBox(transmissionTypeStrings);
         BluetoothConnectivityCombo = new JComboBox(bluetoothConnectivityStrings);
         AuxilliaryCableCombo = new JComboBox(auxilliaryCableStrings);
 
         add = new JButton("Add");
         add.addActionListener(new AddButtonListener());
         
         JPanel p0 = new JPanel();
         p0.add(VehicleSno);
         p0.add(VehicleSnoTextField);
         JPanel p1 = new JPanel();
         p1.add(CarModel);
         p1.add(CarModelTextField);
         JPanel p2 = new JPanel();
         p2.add(CarType);
         p2.add(CarTypeCombo);
         JPanel p3 = new JPanel();
         p3.add(Location);
         p3.add(LocationCombo);
         JPanel p4 = new JPanel();
         p4.add(Color);
         p4.add(ColorTextField);
         JPanel p5 = new JPanel();
         p5.add(HourlyRate);
         p5.add(HourlyRateTextField);
         JPanel p6 = new JPanel();
         p6.add(DailyRate);
         p6.add(DailyRateTextField);
         JPanel p7 = new JPanel();
         p7.add(SeatingCapacity);
         p7.add(SeatingCapacityTextField);
         JPanel p8 = new JPanel();
         p8.add(TransmissionType);
         p8.add(TransmissionTypeCombo);
         JPanel p9 = new JPanel();
         p9.add(BluetoothConnectivity);
         p9.add(BluetoothConnectivityCombo);
         JPanel p10 = new JPanel();
         p10.add(AuxilliaryCable);
         p10.add(AuxilliaryCableCombo);
 
         AddCar.add(AddACar);
         AddCar.add(p0);
         AddCar.add(p1);
         AddCar.add(p2);
         AddCar.add(p3);
         AddCar.add(p4);
         AddCar.add(p5);
         AddCar.add(p6);
         AddCar.add(p7);
         AddCar.add(p8);
         AddCar.add(p9);
         AddCar.add(p10);
         AddCar.add(add);
 
         JPanel ChangeCar = new JPanel();
         ChangeCar.setLayout(new BoxLayout(ChangeCar, BoxLayout.PAGE_AXIS));
 
         ChangeCarLocation = new JLabel("Change Car Location");
         ChangeCarLocation.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         CurrentLocation = new JLabel("Choose Current Location:");
         CurrentLocationCombo = new JComboBox(locationStrings);		
         Car = new JLabel("Choose Car:");
         CarCombo = new JComboBox(carStrings);
         BriefDescription = new JLabel("Brief Description");
         CarType2 = new JLabel("Car Type:");
         CarType2TextField = new JTextField(20);
         Color2 = new JLabel("Color:");
         Color2TextField = new JTextField(20);
         SeatingCapacity2 = new JLabel("Seating Capacity:");
         SeatingCapacity2TextField = new JTextField(20);
         TransmissionType2 = new JLabel("Transmission Type:");
         TransmissionType2TextField = new JTextField(20);
         NewLocation = new JLabel("Choose new location:");
         NewLocationCombo = new JComboBox(locationStrings);
 
         submitChanges = new JButton("Submit Changes");
         submitChanges.addActionListener(new SubmitButtonListener());
 
         JPanel p20 = new JPanel();
         p20.add(CurrentLocation);
         p20.add(CurrentLocationCombo);
         JPanel p21 = new JPanel();
         p21.add(Car);
         p21.add(CarCombo);
         JPanel p22 = new JPanel();
         p22.add(CarType2);
         p22.add(CarType2TextField);
         JPanel p23 = new JPanel();
         p23.add(Color2);
         p23.add(Color2TextField);
         JPanel p24 = new JPanel();
         p24.add(SeatingCapacity2);
         p24.add(SeatingCapacity2TextField);
         JPanel p25 = new JPanel();
         p25.add(TransmissionType2);
         p25.add(TransmissionType2TextField);
         JPanel p26 = new JPanel();
         p26.add(NewLocation);
         p26.add(NewLocationCombo);
         
         ChangeCar.add(ChangeCarLocation);
         ChangeCar.add(p20);
         ChangeCar.add(p21);
         ChangeCar.add(BriefDescription);
         ChangeCar.add(p22);
         ChangeCar.add(p23);
         ChangeCar.add(p24);
         ChangeCar.add(p25);
         ChangeCar.add(p26);
         ChangeCar.add(submitChanges);
 
 
         TwoPanel.add(AddCar);
         TwoPanel.add(ChangeCar);
         this.add(TwoPanel, BorderLayout.CENTER);
     }
 
     public EmployeeUser getEmployee() {
 		return employee;
 	}
 
 	public void setEmployee(EmployeeUser employee) {
 		this.employee = employee;
 	}
 
 	private class AddButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
 
         }
     }
 
     private class SubmitButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
 
         }
     }
 }
