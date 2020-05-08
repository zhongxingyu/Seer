 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 public class BorrowerTransactions {
 
 	/*************************************************************************************
 	*   BORROWER TRANSACTIONS:
	*   	- book search, check account, place holds, pay fine
 	*************************************************************************************/
 	public void searchForBooks() {
 		// User inputs: title, author
 		JTextField titleField = new JTextField(30);
 		JTextField authorField = new JTextField(30);
 		JTextField subjectField = new JTextField(30);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Title:"), titleField,
 				new JLabel("Author:"), authorField,
 				new JLabel ("Subject:"), subjectField
 				};
 
 		Object[] options = { "Search", "Cancel" };
 		int buttonSelected = JOptionPane.showOptionDialog(null, inputs, "Book Search",
 				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 				null, options, options[0]);
 		if (buttonSelected == 0){
 		String title = titleField.getText();
 		String author = authorField.getText();
 		String subject = subjectField.getText();
 		
 		String query = "";
 		java.util.List<String> setInputs = new java.util.ArrayList<String>();
 
 		if (title.isEmpty() == false) { 
 			query += " SELECT callNumber, title, mainAuthor, publisher, year, isbn " +
 						"FROM Book " +
 						"WHERE title = ? ";
 			setInputs.add(title);
 		}
 		if (author.isEmpty() == false) {
 			if(query.isEmpty() == false) {
 				query += "UNION";
 			}
 			query += " SELECT Book.callNumber, Book.title, Book.mainAuthor, Book.publisher, Book.year, Book.isbn " +
 						"FROM Book, HasAuthor " +
						"WHERE Book.callNumber = HasAuthor.callNumber and HasAuthor.name = ? ";
 			query += "UNION";
 			query += " SELECT Book.callNumber, Book.title, Book.mainAuthor, Book.publisher, Book.year, Book.isbn " +
 					"FROM Book " +
 					"WHERE Book.mainAuthor = ? ";
 			setInputs.add(author);
 			setInputs.add(author);
 		}
 		if (subject.isEmpty() == false) {
 			if(query.isEmpty() == false) {
 				query += "UNION";
 			}
 			query += "SELECT Book.callNumber, Book.title, Book.mainAuthor, Book.publisher, Book.year, Book.isbn " +
 						"FROM Book, HasSubject " +
 						"WHERE Book.callNumber=HasSubject.callNumber and HasSubject.subject = ? ";
 			setInputs.add(subject);
 		}
 		
 		// Show all books if all search fields are empty
 		if (title.isEmpty() == true && author.isEmpty() == true && subject.isEmpty() == true) {
 			query = "SELECT * from Book";
 		}
 
 		try {
 			PreparedStatement ps = Library.con
 					.prepareStatement(query);
 			for (int i = 0; i < setInputs.size(); i++) {
 				ps.setString(i+1, setInputs.get(i));
 			}
 			ps.executeQuery();
 
 			ResultSet rs = ps.getResultSet();
 			
 			// Show tables
 			LibraryGUI.showSearchResultsTable(rs, query, setInputs);
 			ps.close();
 				
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		}		
 
 	}
 
 	public void checkAccount() {
 
 		// User inputs: bid, password
 		JTextField bidField = new JTextField(15);
 		JPasswordField passwordField = new JPasswordField(15);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Borrower ID:"), bidField,
 				new JLabel("Password:"), passwordField,
 
 		};
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter borrower info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 			int bid = Integer.parseInt(bidField.getText());
 			char[] password = passwordField.getPassword();
 
 
 			try {
 				PreparedStatement ps = Library.con
 						.prepareStatement("select * from borrower where bid = ?");
 				ps.setInt(1, bid);
 				ps.executeQuery();
 
 				ResultSet rs = ps.getResultSet();
 				rs.next();
 
 				// Check if password is correct
 				char[] correctPassword = rs.getString("password").toCharArray();
 				if (!Arrays.equals(password, correctPassword)) {
 					new ErrorMessage("Incorrect password!");
 				}
 
 				else {
 
 					// Select items the borrower has currently borrowed and not
 					// yet returned
 					PreparedStatement ps2 = Library.con
 							.prepareStatement("select borrowing.borid, bookcopy.callNumber, bookcopy.copyNo, borrowing.outDate, borrowing.inDate " +
 												"from Borrowing, BookCopy " +
 												"where Borrowing.callNumber=BookCopy.callNumber " +
 													"and Borrowing.copyNo=BookCopy.CopyNo " +
 													"and BookCopy.Status = 'out' " +
 													"and Borrowing.bid = ?");
 					ps2.setInt(1, bid);
 					ps2.executeQuery();
 
 					// Select outstanding fines
 					PreparedStatement ps3 = Library.con
 							.prepareStatement("Select fid, amount, issuedDate, borid " +
 												"from Fine " +
 												"WHERE amount > 0 and borid in " +
 													"(select borrowing.borid " +
 														"from Borrowing, BookCopy " +
 														"where Borrowing.callNumber = BookCopy.callNumber " +
 														"and Borrowing.copyNo = BookCopy.copyNo " +
 														"and Borrowing.bid = ?)");
 					ps3.setInt(1, bid);
 					ps3.executeQuery();
 
 					// Select hold requests
 					PreparedStatement ps4 = Library.con
 							.prepareStatement("select holdrequest.hid, holdrequest.issuedDate, Book.callNumber, Book.isbn, Book.title " +
 												"from Book " +
 												"INNER JOIN HoldRequest on Book.callNumber = HoldRequest.callNumber " +
 												"where HoldRequest.bid = ?");
 					ps4.setInt(1, bid);
 					ps4.executeQuery();
 
 					// Show tables
 					LibraryGUI.showAccountTables(ps2.getResultSet(),
 							ps3.getResultSet(), ps4.getResultSet(), bid);
 				}
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void placeHold() {
 		// User inputs: bid, password
 				JTextField bidField = new JTextField(15);
 				JPasswordField passwordField = new JPasswordField(15);
 				JTextField callNumberField = new JTextField(15);
 
 				JComponent[] inputs = new JComponent[] { 
 						new JLabel("Borrower ID:"), bidField,
 						new JLabel("Password:"), passwordField,
 						new JLabel("Call Number:"), callNumberField,
 
 				};
 				int result = JOptionPane.showConfirmDialog(null, inputs,
 						"Enter information", JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.WARNING_MESSAGE);
 
 				if (result == JOptionPane.OK_OPTION) {
 					int bid = Integer.parseInt(bidField.getText());
 					char[] password = passwordField.getPassword();
 					String callNumber = callNumberField.getText();
 
 					try {
 						PreparedStatement ps = Library.con
 								.prepareStatement("select * from borrower where bid = ?");
 						ps.setInt(1, bid);
 						ps.executeQuery();
 
 						ResultSet rs = ps.getResultSet();
 						rs.next();
 
 						char[] correctPassword = rs.getString("password").toCharArray();
 						// Check if password is correct
 						if (Arrays.equals(password, correctPassword)) {
 							new ErrorMessage("Incorrect password!");
 						}
 
 						else {
 
 							// Check if there is a copy of the book currently in
 							PreparedStatement ps2 = Library.con
 									.prepareStatement("select callNumber from bookCopy where status like 'in' and callNumber=?");
 							ps2.setString(1, callNumber);
 							ps2.executeQuery();
 							
 							PreparedStatement ps4 = Library.con
 									.prepareStatement("select callNumber from bookCopy where status like 'out' and callNumber=?");
 							ps4.setString(1, callNumber);
 							ps4.executeQuery();
 							
 							// If there is a copy of the book that is in or no copies that are out, error message
 							if (ps2.getResultSet().next() != false){
 								new ErrorMessage("You cannot place this book on hold because there are copies that are in");
 							}
 							
 							else if(ps4.getResultSet().next() == false){
 								 new ErrorMessage("There are no copies avaiable to be put on hold");
 							}
 							
 							// If there are no copies of the book in, place a hold for the book
 							else{
 								
 								java.util.Date currentDate = new java.util.Date();
 								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 								Date issuedDate = null;
 								try {
 									issuedDate = new Date(dateFormat.parse(
 											dateFormat.format(currentDate)).getTime());
 								} catch (ParseException e2) {
 									// TODO Auto-generated catch block
 									e2.printStackTrace();
 								}
 								
 							PreparedStatement ps3 = Library.con
 									.prepareStatement("insert into HoldRequest (bid, callNumber, issuedDate) VALUES (?,?,?)");
 							ps3.setInt(1, bid);
 							ps3.setString(2, callNumber);
 							ps3.setDate(3, issuedDate);
 							ps3.executeUpdate();
 							Library.con.commit();
 
 							// Show table
 							Statement stmt = Library.con.createStatement();
 							ResultSet rs2 = stmt.executeQuery("SELECT * FROM HoldRequest");
 							LibraryGUI.showTable(rs2, "holdRequestButton");
 							
 							ps.close();
 							ps2.close();
 							ps3.close();
 							rs.close();
 							rs2.close();
 
 							
 							}
 						}
 
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 	}
 	
 	public void payFine() {
 		
 		// User inputs: bid, fid, amount to pay
 				JTextField bidField = new JTextField(15);
 				JTextField fineField = new JTextField(15);
 				JTextField amountField = new JTextField(15);
 
 				JComponent[] inputs = new JComponent[] { 
 						new JLabel("Borrowing ID:"), bidField,
 						new JLabel("Fine ID:"), fineField,
 						new JLabel("Amount:"), amountField,
 
 				};
 				int result = JOptionPane.showConfirmDialog(null, inputs,
 						"Enter fine info", JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.WARNING_MESSAGE);
 
 				if (result == JOptionPane.OK_OPTION) {
 					int bid = Integer.parseInt(bidField.getText());
 					int fid = Integer.parseInt(fineField.getText());
 					float amount = Float.parseFloat(amountField.getText());
 					
 					java.util.Date currentDate = new java.util.Date();
 					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 					Date paidDate = null;
 					try {
 						paidDate = new Date(dateFormat.parse(dateFormat.format(currentDate)).getTime());
 					} catch (ParseException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					try {
 						PreparedStatement ps = Library.con
 								.prepareStatement("SELECT * FROM fine WHERE borid = ? and fid = ?");
 						ps.setInt(1, bid);
 						ps.setInt(2, fid);
 						ps.executeQuery();
 
 						ResultSet rs = ps.getResultSet();
 						if(rs.next() == true) {
 							// get fine amount for borrower
 							float due = rs.getFloat("amount");
 							float remainingFine = due - amount;
 							
 							if (remainingFine < 0.0f) {
 								remainingFine = 0.00f;
 							}
 
 							PreparedStatement ps2 = Library.con
 									.prepareStatement("UPDATE Fine Set amount = ?, paidDate = ? WHERE fid = ?");
 							ps2.setFloat(1, remainingFine);
 							ps2.setDate(2, paidDate);
 							ps2.setInt(3, fid);
 
 							ps2.executeUpdate();
 							Library.con.commit();
 							
 							String msg = String.format("<html>Remaining balance for fine ID %d is $%.2f.", fid, remainingFine);
 							msg += "<br>";
 							msg += "Thank you.</html>";
 							new ErrorMessage(msg);
 							
 							ps2.close();
 						}
 
 
 						// Show tables
 						Statement stmt = Library.con.createStatement();
 						ResultSet rs2 = stmt.executeQuery("SELECT * FROM Fine");
 						LibraryGUI.showTable(rs2, "payFineButton");
 						
 						ps.close();
 						
 
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 	}
 }
