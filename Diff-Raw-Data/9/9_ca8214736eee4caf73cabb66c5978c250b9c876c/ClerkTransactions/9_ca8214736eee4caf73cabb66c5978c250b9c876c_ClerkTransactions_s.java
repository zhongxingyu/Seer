 import java.awt.FlowLayout;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class ClerkTransactions {
 
 	/*************************************************************************************
 	 * CLERK TRANSACTIONS: - add borrowers, checkout items, process returns,
 	 * check overdue items
 	 *************************************************************************************/
 	public void addBorrower() {
 
 		// User inputs: password, name, address, phone, email, sin/st, type,
 		// date
 		JTextField passwordField = new JTextField(15);
 		JTextField nameField = new JTextField(15);
 		JTextField addressField = new JTextField(15);
 		JTextField phoneField = new JTextField(15);
 		JTextField emailAddressField = new JTextField(15);
 		JTextField sinOrStNoField = new JTextField(10);
 
 		String[] typeString = { "Student", "Faculty", "Staff" };
 		JComboBox typeCombo = new JComboBox(typeString);
 		DateFormat dateFormat = new SimpleDateFormat("yyyy");
 		java.util.Date date2 = new java.util.Date();
 
 		String currentYear = dateFormat.format(date2).toString();
 		String[] yearString = {
 				"Year",
 				currentYear,
 				Integer.toString(((Integer.valueOf(currentYear)).intValue() + 1)),
 				Integer.toString(((Integer.valueOf(currentYear)).intValue() + 2)),
 				Integer.toString(((Integer.valueOf(currentYear)).intValue() + 3)),
 				Integer.toString(((Integer.valueOf(currentYear)).intValue() + 4)) };
 		String[] monthString = { "Month", "01", "02", "03", "04", "05", "06",
 				"07", "08", "09", "10", "11", "12" };
 		String[] dayString = { "Day", "01", "02", "03", "04", "05", "06", "07",
 				"08", "09", "10", "11", "12", "13", "14", "15", "16", "17",
 				"18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
 				"28", "29", "30", "31" };
 		JComboBox yearCombo = new JComboBox(yearString);
 		JComboBox monthCombo = new JComboBox(monthString);
 		JComboBox dayCombo = new JComboBox(dayString);
 		JPanel expiryPanel = new JPanel();
 		FlowLayout expiryLayout = new FlowLayout();
 
 		expiryPanel.setLayout(expiryLayout);
 		expiryPanel.add(yearCombo);
 		expiryPanel.add(monthCombo);
 		expiryPanel.add(dayCombo);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Password:"), passwordField, 
 				new JLabel("Name:"), nameField,
 				new JLabel("Address:"), addressField, 
 				new JLabel("Phone:"),phoneField, 
 				new JLabel("Email:"), emailAddressField,
 				new JLabel("Sin or St No.:"), sinOrStNoField,
 				new JLabel("Type:"), typeCombo, 
 				new JLabel("Expiration date"), expiryPanel, 
 				};
 
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter borrower info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 
 			String password = passwordField.getText();
 			String name = nameField.getText();
 			String address = addressField.getText();
 			int phone = Integer.parseInt(phoneField.getText());
 			String emailAddress = emailAddressField.getText();
 			int sinOrStNo = Integer.parseInt(sinOrStNoField.getText());
 			String type = typeCombo.getSelectedItem().toString().toLowerCase();
 			Date expiryDate = null;
 
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			String date = yearCombo.getSelectedItem().toString() + "-"
 					+ monthCombo.getSelectedItem().toString() + "-"
 					+ dayCombo.getSelectedItem().toString();
 
 			// TODO: add error popup
 			try {
 				expiryDate = new Date(df.parse(date).getTime());
 			} catch (ParseException ex) {
 				new ErrorMessage("Error adding new borrower.");
 			}
 
 			PreparedStatement ps;
 
 			try {
 				// Insert new borrower into Borrower table
 				ps = Library.con
 						.prepareStatement("INSERT INTO borrower (password, name, address, phone, emailAddress, sinOrStNo, type, expiryDate) VALUES (?,?,?,?,?,?,?,?)");
 
 				ps.setString(1, password);
 				ps.setString(2, name);
 				ps.setString(3, address);
 				ps.setInt(4, phone);
 				ps.setString(5, emailAddress);
 				ps.setInt(6, sinOrStNo);
 				ps.setString(7, type);
 
 				ps.setDate(8, expiryDate);
 				ps.executeUpdate();
 				Library.con.commit();
 
 				// Show Borrower table
 				Statement stmt = Library.con.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Borrower");
 				LibraryGUI.showTable(rs, "addBorrowerButton");
 
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
				e1.printStackTrace();
 			}
 		}
 	}
 
 	public void checkout() {
 
 		// User inputs: bid, list of call numbers
 		JTextField bidField = new JTextField(10);
 		JTextField callNumbersField = new JTextField(50);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Borrower ID:"), bidField,
 				new JLabel("Call number:"), callNumbersField, 
 				};
 
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter borrowing info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 			int bid = Integer.parseInt(bidField.getText());
 			String[] callNumbers = callNumbersField.getText().split(",");
 
 			try {
 
 				java.util.Date currentDate = new java.util.Date();
 				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 				Date outDate = null;
 				try {
 					outDate = new Date(dateFormat.parse(
 							dateFormat.format(currentDate)).getTime());
 				} catch (ParseException e2) {
 					// TODO Auto-generated catch block
 					e2.printStackTrace();
 				}
 
 				// Get type of borrower
 				PreparedStatement ps4 = Library.con
 						.prepareStatement("select type from borrower where bid=?");
 				ps4.setInt(1, bid);
 				ps4.executeQuery();
 				ResultSet rs3 = ps4.getResultSet();
 				if (rs3.next()) {
 					String type = rs3.getString("type");
 
 					PreparedStatement ps6 = Library.con
 							.prepareStatement("select expiryDate from borrower where bid=?");
 					ps6.setInt(1, bid);
 					ps6.executeQuery();
 					ResultSet rs6 = ps6.getResultSet();
 					rs6.next();
 
 					java.util.Date expiryDate = null;
 					expiryDate = rs6.getDate("expiryDate");
 					
 					if (expiryDate.after(outDate)) {
 						// Set appropriate inDate for borrower type
 						Date inDate = null;
 						Calendar cal = Calendar.getInstance();
 						cal.setTime(outDate);
 						
 						if (type.equals("student")) {
 							cal.add(Calendar.DAY_OF_YEAR, 7);
 							inDate = new Date(cal.getTimeInMillis());
 						}
 
 						if (type.equals("faculty")) {
 							cal.add(Calendar.DAY_OF_YEAR, 84);
 							inDate = new Date(cal.getTimeInMillis());
 						}
 
 						if (type.equals("staff")) {
 							cal.add(Calendar.DAY_OF_YEAR, 42);
 							inDate = new Date(cal.getTimeInMillis());
 						}
 
 						for (int i = 0; i < callNumbers.length; i++) {
 
 							PreparedStatement ps5 = Library.con
 									.prepareStatement("select * from book where callNumber = ?");
 							ps5.setString(1, callNumbers[i]);
 							ps5.executeQuery();
 
 							// If callNumber exists in the database, proceed
 							if (ps5.getResultSet().next() == true) {
 								// Get all copies with the matching callNumber
 								// and
 								// status
 								// being 'in'
 								PreparedStatement ps2 = Library.con
 										.prepareStatement("select * from bookcopy where callNumber = ? and status like 'in'");
 								ps2.setString(1, callNumbers[i]);
 
 								ps2.executeUpdate();
 								ResultSet rs = ps2.getResultSet();
 
 								if (rs.next() == true) {
 
 									// Insert into Borrowing table if there
 									// exists a
 									// copy of
 									// the book with status "in"
 									String copyNo = rs.getString("copyNo");
 									PreparedStatement ps = Library.con
 											.prepareStatement("INSERT INTO Borrowing (bid, callNumber, copyNo, outDate, inDate) VALUES (?,?,?,?,?)");
 									ps.setInt(1, bid);
 									ps.setString(2, callNumbers[i]);
 									ps.setString(3, copyNo);
 									ps.setDate(4, outDate);
 									ps.setDate(5, inDate);
 
 									ps.executeUpdate();
 									Library.con.commit();
 
 									// Set status of checked out copy to "out"
 									PreparedStatement ps3 = Library.con
 											.prepareStatement("update bookcopy set status = ? where callNumber = ? and copyNo=?");
 									ps3.setString(1, "out");
 									ps3.setString(2, callNumbers[i]);
 									ps3.setString(3, copyNo);
 
 									ps3.executeUpdate();
 									Library.con.commit();
 
 									ps.close();
 									ps2.close();
 									ps3.close();
 									rs.close();
 									rs3.close();
 
 								} else {
 									// Error message for if there are no copies
 									// in
 									new ErrorMessage("No copies are in for "
 											+ callNumbers[i]);
 								}
 							} else {
 								// Error message for if callNumber could not be
 								// found in
 								// the database
 								new ErrorMessage(callNumbers[i]
 										+ " does not exist!");
 							}
 							ps5.close();
 						}
 						ps4.close();
 						Statement stmt = Library.con.createStatement();
 						ResultSet rs2 = stmt
 								.executeQuery("SELECT * FROM Borrowing");
 						LibraryGUI.showTable(rs2, "checkoutButton");
 
 					} else {
 						new ErrorMessage("Account is expired.");
 					}
 					ps6.close();
 				} else {
 					new ErrorMessage("User does not exist.");
 				}
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	public void returnBooks() {
 		// User inputs: callNumber, copyNo of book to be returned
 		JTextField copyNoField = new JTextField(10);
 		JTextField callNumbersField = new JTextField(20);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Call number:"), callNumbersField, 
 				new JLabel("Copy Number:"), copyNoField 
 				};
 
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter borrowing info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 			String copyNo = copyNoField.getText();
 			String callNumbers = callNumbersField.getText();
 
 			try {
 				// get system current date
 				java.util.Date currentDate = new java.util.Date();
 				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 				// Check if the Book was actually checked out and if it was then
 				// get due date for the book
 				PreparedStatement ps2 = Library.con
 						.prepareStatement("select Borrowing.inDate, Borrowing.bid, borrowing.borid from Borrowing INNER JOIN BookCopy on Borrowing.callNumber = BookCopy.callNumber and Borrowing.copyNo = BookCopy.copyNo where Borrowing.callNumber = ? and Borrowing.copyNo = ? and BookCopy.status like 'out'");
 				ps2.setString(1, callNumbers);
 				ps2.setString(2, copyNo);
 				java.util.Date inDate = null;
 				// Change system dates format
 				try {
 					currentDate = new Date(dateFormat.parse(
 							dateFormat.format(currentDate)).getTime());
 				} catch (ParseException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				// execute above sql query
 				ps2.executeUpdate();
 				ResultSet rs = ps2.getResultSet();
 				// If something was checked out
 				if (rs.next() == true) {
 					// get date of the book
 					inDate = rs.getDate("inDate");
 					PreparedStatement ps = null;
 
 					// Compare the system date with inDate of the book
 					// If inDate is before current date then issue a fine
 					if (inDate.before(currentDate)) {
 
 						// Fine amount = 100
 						ps = Library.con
 								.prepareStatement("INSERT into FINE (amount, issuedDate, borid) values (?, ?, ?)");
 						ps.setInt(1, 100);
 						// change current date to sql date for Issue date of
 						// fine
 						Date currentDateFine = null;
 						try {
 							currentDateFine = new Date(dateFormat.parse(
 									dateFormat.format(currentDate)).getTime());
 						} catch (ParseException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 						// get borid from the above result
 						ps.setDate(2, currentDateFine);
 						ps.setInt(3, rs.getInt("borid"));
 						ps.executeUpdate();
 						Library.con.commit();
 					}
 					// Check if there is a hold request for the book
 					// If so then change the status to on-hold else change to in
 					// Also need to send an email but dont know how to do that
 					// yet
 					PreparedStatement ps3 = Library.con
 							.prepareStatement("select bid, issuedDate from HoldRequest where callNumber = ? ORDER BY issuedDate");
 					ps3.setString(1, callNumbers);
 					ps3.executeQuery();
 					ResultSet rs2 = ps3.getResultSet();
 					PreparedStatement ps4 = null;
 					// if there is a hold request then create the sql query
 					// accordingly
 					if (rs2.next() == true) {
 						// Do sth for sending emails
 						ps4 = Library.con
 								.prepareStatement("update BookCopy Set status = 'on-hold' where callNumber = ? and copyNo = ?");
 						ps4.setString(1, callNumbers);
 						ps4.setString(2, copyNo);
 						ps4.executeUpdate();
 						Library.con.commit();
 					}
 					// If no hold request then change the status to in
 					else {
 						ps4 = Library.con
 								.prepareStatement("update BookCopy Set status = 'in' where callNumber = ? and copyNo = ?");
 						ps4.setString(1, callNumbers);
 						ps4.setString(2, copyNo);
 						ps4.executeUpdate();
 						Library.con.commit();
 					}
 					Library.con.commit();
 					ps2.close();
 					ps3.close();
 					ps4.close();
 				} else {
 					// Error message for if there are no copies in
 					new ErrorMessage("This copy was not checked out");
 				}
 				// Display the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet rs2 = stmt.executeQuery("SELECT * FROM bookcopy");
 				LibraryGUI.showTable(rs2, "processReturnButton");
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	public void checkOverdueItems() {
 
 			try {
 				
 				// Get current date
 				java.util.Date currentDate = new java.util.Date();
 				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 				Date curDate = null;
 				try {
 					curDate = new Date(dateFormat.parse(
 							dateFormat.format(currentDate)).getTime());
 				} catch (ParseException e2) {
 					// TODO Auto-generated catch block
 					e2.printStackTrace();
 				}
 				
 				// Get list of overdue items (curDate > inDate and status being out)
 				PreparedStatement ps = Library.con
 						.prepareStatement("select borid, bid, callNumber, copyNo, inDate from borrowing where callNumber in (select callNumber from bookCopy where bookCopy.callNumber = borrowing.callNumber and bookCopy.copyNo = borrowing.copyNo and status like 'out') and inDate < ?");
 				ps.setDate(1, curDate);
 				ps.executeQuery();
 
 				LibraryGUI.showTable(ps.getResultSet(), "checkOverdueItems");
 
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 	}
 }
