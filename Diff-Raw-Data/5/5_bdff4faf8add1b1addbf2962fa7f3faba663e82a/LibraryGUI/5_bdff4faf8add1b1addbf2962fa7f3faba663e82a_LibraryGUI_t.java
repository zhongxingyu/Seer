 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.*;
 
 public class LibraryGUI {
 	// The main frame for the GUI
 	static JFrame frame;
 
 	// The menubar for the GUI
 	private JMenuBar menuBar;
 
 	// All the panels within the frame
 	private JPanel userPane;
 	public static TablePane tablePane;
 	private ActivitiesPane activitiesPane;
 	public static int height = 550;
 	public int width = 1050;
 
 	// Constructor for GUI
 	public LibraryGUI() {
 
 	}
 
 	// Method to display the GUI
 	public void showGUI() {
 
 		frame = new JFrame("Library");
 		frame.setBackground(Color.white);
 
 		frame.setPreferredSize(new Dimension(width, height));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Panel for viewing the tables
 		tablePane = new TablePane();
 		tablePane.setBorder(BorderFactory.createLineBorder(Color.black));
 		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
 		
 		// Panel for displaying types of users
 		userPane = new JPanel();
 		userPane.setLayout(new BoxLayout(userPane, BoxLayout.Y_AXIS));
 		userPane.setBorder(BorderFactory.createLineBorder(Color.black));
 		Font font = new Font("Arial", Font.BOLD, 16);
 		JTextArea users = new JTextArea("Users");
 		users.setFont(font);
 		users.setEditable(false);
 		users.setMaximumSize(new Dimension(700, 30));
 		userPane.add(users);
 
 		activitiesPane = new ActivitiesPane();
 		activitiesPane.setBorder(BorderFactory.createLineBorder(Color.black));
 
 		initializeMenu();
 		initializeUserPane();
 
 		// Adds all the panels to the frame
 		frame.getContentPane().add(userPane, BorderLayout.WEST);
 		frame.getContentPane().add(tablePane, BorderLayout.CENTER);
 
 		frame.getContentPane().add(activitiesPane, BorderLayout.EAST);
 
 		// Shows the frame
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	// Method for initializing the menu
 	private void initializeMenu() {
 
 		JMenu Lib;
 		JMenuItem quit;
 
 		JMenu Tables;
 		JMenuItem Borrower;
 		JMenuItem Book;
 		JMenuItem HasAuthor;
 		JMenuItem HasSubject;
 		JMenuItem BookCopy;
 		JMenuItem HoldRequest;
 		JMenuItem Borrowing;
 		JMenuItem Fine;
 
 		Lib = new JMenu("Library");
 
 		// Exits the application
 		quit = new JMenuItem("Quit");
 		quit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.setVisible(false);
 				frame.dispose();
 			}
 		});
 
 		Lib.add(quit);
 
 		Tables = new JMenu("Tables");
 
 		// Display borrower table
 		Borrower = new JMenuItem("Borrower");
 		Borrower.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM Borrower");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM Borrower");
 					List<Integer> borrowers = new ArrayList<Integer>();
 					while (count.next()) {
 						borrowers.add(count.getInt("bid"));
 					}
 					
 					Object data[][] = new Object[borrowers.size()][numCols];
 					count.close();
 
 					int bid;
 					String password;
 					String name;
 					String address;
 					int phone;
 					String emailAddress;
 					int sinOrStNo;
 					String type;
 					String expiryDate;
 					
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						bid = rs.getInt("bid");
 						password = rs.getString("password");
 						name = rs.getString("name");
 						address = rs.getString("address");
 						phone = rs.getInt("phone");
 						emailAddress = rs.getString("emailAddress");
 						sinOrStNo = rs.getInt("sinOrStNo");
 						type = rs.getString("type");
 						expiryDate = rs.getString("expiryDate");
 					
 						
 						Object tuple[] = { bid, password, name, address, phone,
 								emailAddress, sinOrStNo, type, expiryDate };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("Borrower");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display book table
 		Book = new JMenuItem("Book");
 		Book.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM Book");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2.executeQuery("SELECT * FROM Book");
 					List<String> books = new ArrayList<String>();
 					while (count.next()) {
 						books.add(count.getString("callNumber"));
 					}
 					
 					Object data[][] = new Object[books.size()][numCols];
 					count.close();
 
 					String callNumber;
 					String isbn;
 					String title;
 					String mainAuthor;
 					String publisher;
 					int year;
 					
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						callNumber = rs.getString("callNumber");
 						isbn = rs.getString("isbn");
 						title = rs.getString("title");
 						mainAuthor = rs.getString("mainAuthor");
 						publisher = rs.getString("publisher");
 						year = rs.getInt("year");
 
 						Object tuple[] = { callNumber, isbn, title, mainAuthor,
 								publisher, year };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("Book");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 					
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display HasAuthor table
 		HasAuthor = new JMenuItem("HasAuthor");
 		HasAuthor.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM HasAuthor");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 
 					}
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM HasAuthor");
 					List<String> authors = new ArrayList<String>();
 					while (count.next()) {
 						authors.add(count.getString("callNumber"));
 					}
 					
 					Object data[][] = new Object[authors.size()][numCols];
 					count.close();
 
 					String callNumber;
 					String name;
 
 					int j = 0;
 					
 					// Fill table
 					while (rs.next()) {
 						callNumber = rs.getString("callNumber");
 						name = rs.getString("name");
 
 						Object tuple[] = { callNumber, name };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("HasAuthor");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display HasSubject table
 		HasSubject = new JMenuItem("HasSubject");
 		HasSubject.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt
 							.executeQuery("SELECT * FROM HasSubject");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM HasSubject");
 					List<String> subjects = new ArrayList<String>();
 					while (count.next()) {
 						subjects.add(count.getString("callNumber"));
 					}
 					
 					Object data[][] = new Object[subjects.size()][numCols];
 					count.close();
 
 					String callNumber;
 					String subject;
 
 					int j = 0;
 					
 					// Fill table
 					while (rs.next()) {
 						callNumber = rs.getString("callNumber");
 						subject = rs.getString("subject");
 
 						Object tuple[] = { callNumber, subject };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("HasSubject");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display BookCopy table
 		BookCopy = new JMenuItem("BookCopy");
 		BookCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM BookCopy");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM BookCopy");
 					List<String> copies = new ArrayList<String>();
 					while (count.next()) {
 						copies.add(count.getString("callNumber"));
 					}
 					
 					Object data[][] = new Object[copies.size()][numCols];
 					count.close();
 
 					String callNumber;
 					String copyNo;
 					String status;
 					
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						callNumber = rs.getString("callNumber");
 						copyNo = rs.getString("copyNo");
 						status = rs.getString("status");
 						
 						Object tuple[] = { callNumber, copyNo, status };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("BookCopy");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display HoldRequest table
 		HoldRequest = new JMenuItem("HoldRequest");
 		HoldRequest.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt
 							.executeQuery("SELECT * FROM HoldRequest");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM HoldRequest");
 					List<Integer> holds = new ArrayList<Integer>();
 					while (count.next()) {
 						holds.add(count.getInt("hid"));
 					}
 					
 					Object data[][] = new Object[holds.size()][numCols];
 					count.close();
 
 					int hid;
 					int bid;
 					String callNumber;
 					Date issuedDate;
 
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						hid = rs.getInt("hid");
 						bid = rs.getInt("bid");
 						callNumber = rs.getString("callNumber");
 						issuedDate = rs.getDate("issuedDate");
 						
 						Object tuple[] = { hid, bid, callNumber, issuedDate };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("HoldRequest");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display Borrowing table
 		Borrowing = new JMenuItem("Borrowing");
 		Borrowing.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM Borrowing");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2
 							.executeQuery("SELECT * FROM Borrowing");
 					List<Integer> borrowings = new ArrayList<Integer>();
 					while (count.next()) {
 						borrowings.add(count.getInt("borid"));
 					}
 					
 					Object data[][] = new Object[borrowings.size()][numCols];
 					count.close();
 
 					int borid;
 					int bid;
 					String callNumber;
 					String copyNo;
 					Date outDate;
 					Date inDate;
 					
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						borid = rs.getInt("borid");
 						bid = rs.getInt("bid");
 						callNumber = rs.getString("callNumber");
 						copyNo = rs.getString("copyNo");
 						outDate = rs.getDate("outDate");
 						inDate = rs.getDate("inDate");
 						
 						Object tuple[] = { borid, bid, callNumber, copyNo,
 								outDate, inDate };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("Borrowing");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 					
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		// Display Fine table
 		Fine = new JMenuItem("Fine");
 		Fine.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Statement stmt = Library.con.createStatement();
 					ResultSet rs = stmt.executeQuery("SELECT * FROM Fine");
 					ResultSetMetaData rsmd = rs.getMetaData();
 
 					int numCols = rsmd.getColumnCount();
 
 					String columnNames[] = new String[numCols];
 					for (int i = 0; i < numCols; i++) {
 						columnNames[i] = rsmd.getColumnName(i + 1);
 					}
 					
 					Statement stmt2 = Library.con.createStatement();
 					ResultSet count = stmt2.executeQuery("SELECT * FROM Fine");
 					List<Integer> fines = new ArrayList<Integer>();
 					while (count.next()) {
 						fines.add(count.getInt("fid"));
 					}
 					
 					Object data[][] = new Object[fines.size()][numCols];
 					count.close();
 
 					int fid;
 					String amount;
 					Date issuedDate;
 					Date paidDate;
 					int borid;
 
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						fid = rs.getInt("fid");
 						amount = rs.getString("amount");
 						issuedDate = rs.getDate("issuedDate");
 						paidDate = rs.getDate("paidDate");
 						borid = rs.getInt("borid");
 
 						Object tuple[] = { fid, amount, issuedDate, paidDate,
 								borid };
 						data[j] = tuple;
 
 						j++;
 					}
 
 					JTable table = new JTable(data, columnNames);
 					JTextArea tableTitle = new JTextArea("Fine");
 					table.setEnabled(false);
 					table.setPreferredSize(new Dimension(600, height));
 					JScrollPane scrollPane = new JScrollPane(table);
 					scrollPane.setPreferredSize(new Dimension(600, height));
 					table.setAutoCreateRowSorter(true);
 
 					// Display table
 					table.setFillsViewportHeight(true);
 					tablePane.removeAll();
 					tablePane.updateUI();
 					tableTitle.setEditable(false);
 					tablePane.add(tableTitle);
 					tablePane.add(scrollPane);
 
 				} catch (SQLException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		Tables.add(Borrower);
 		Tables.add(Book);
 		Tables.add(HasAuthor);
 		Tables.add(HasSubject);
 		Tables.add(BookCopy);
 		Tables.add(HoldRequest);
 		Tables.add(Borrowing);
 		Tables.add(Fine);
 
 		menuBar = new JMenuBar();
 		menuBar.add(Lib);
 		menuBar.add(Tables);
 		frame.setJMenuBar(menuBar);
 	}
 
 	// Method for initializing the activities for each user
 	private void initializeUserPane() {
 
 		JButton clerkButton = new JButton("Clerk");
 		JButton borrowerButton = new JButton("Borrower");
 		JButton librarianButton = new JButton("Librarian");
 
 		userPane.add(clerkButton);
 		userPane.add(borrowerButton);
 		userPane.add(librarianButton);
 
 		clerkButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				activitiesPane.removeAll();
 				activitiesPane.updateUI();
 				activitiesPane.display("Clerk");
 			}
 		});
 		
 		borrowerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				activitiesPane.removeAll();
 				activitiesPane.updateUI();
 				activitiesPane.display("Borrower");
 			}
 		});
 		
 		librarianButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				activitiesPane.removeAll();
 				activitiesPane.updateUI();
 				activitiesPane.display("Librarian");
 			}
 		});
 	}
 
 	// For displaying the appropriate tables after each transaction
 	public static void showTable(ResultSet rs, String buttonClicked) {
 
 		int numCols;
 		ResultSetMetaData rsmd;
 		JTextArea tableTitle = null;
 		JTable table = null;
 
 		try {
 			rsmd = rs.getMetaData();
 			numCols = rsmd.getColumnCount();
 
 			String columnNames[] = new String[numCols];
 			for (int i = 0; i < numCols; i++) {
 				columnNames[i] = rsmd.getColumnName(i + 1);
 			}
 
 			if (buttonClicked == "addBorrowerButton") {
 
 				// For creating the size of the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet count = stmt.executeQuery("SELECT * FROM Borrower");
 				List<Integer> borrowers = new ArrayList<Integer>();
 				while (count.next()) {
 					borrowers.add(count.getInt("bid"));
 				}
 				
 				Object data[][] = new Object[borrowers.size()][numCols];
 				count.close();
 
 				int bid;
 				String password;
 				String name;
 				String address;
 				int phone;
 				String emailAddress;
 				int sinOrStNo;
 				String type;
 				String expiryDate;
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					bid = rs.getInt("bid");
 					password = rs.getString("password");
 					name = rs.getString("name");
 					address = rs.getString("address");
 					phone = rs.getInt("phone");
 					emailAddress = rs.getString("emailAddress");
 					sinOrStNo = rs.getInt("sinOrStNo");
 					type = rs.getString("type");
 					expiryDate = rs.getString("expiryDate");
 					Object tuple[] = { bid, password, name, address, phone,
 							emailAddress, sinOrStNo, type, expiryDate };
 					data[j] = tuple;
 					j++;
 
 				}
 				tableTitle = new JTextArea("Borrower table");
 				table = new JTable(data, columnNames);
 			}
 
 			if (buttonClicked == "addBookButton") {
 
 				// For creating the size of the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet count = stmt.executeQuery("SELECT * FROM Book");
 				List<String> books = new ArrayList<String>();
 				while (count.next()) {
 					books.add(count.getString("callNumber"));
 				}
 				Object data[][] = new Object[books.size()][numCols];
 				count.close();
 
 				String callNumber;
 				String isbn;
 				String title;
 				String mainAuthor;
 				String publisher;
 				int year;
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					callNumber = rs.getString("callNumber");
 					isbn = rs.getString("isbn");
 					title = rs.getString("title");
 					mainAuthor = rs.getString("mainAuthor");
 
 					publisher = rs.getString("publisher");
 					year = rs.getInt("year");
 
 					Object tuple[] = { callNumber, isbn, title, mainAuthor,
 							publisher, year };
 					data[j] = tuple;
 					j++;
 
 				}
 				tableTitle = new JTextArea("Book table");
 				table = new JTable(data, columnNames);
 			}
 
 			if (buttonClicked == "addBookCopyButton"
 					|| buttonClicked == "processReturnButton") {
 
 				// For creating the size of the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet count = stmt.executeQuery("SELECT * FROM BookCopy");
 				List<String> bookCopies = new ArrayList<String>();
 				while (count.next()) {
 					bookCopies.add(count.getString("callNumber"));
 				}
 				Object data[][] = new Object[bookCopies.size()][numCols];
 				count.close();
 
 				String callNumber;
 				String copyNo;
 				String status;
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					callNumber = rs.getString("callNumber");
 					copyNo = rs.getString("copyNo");
 					status = rs.getString("status");
 					Object tuple[] = { callNumber, copyNo, status };
 					data[j] = tuple;
 					j++;
 
 				}
 				tableTitle = new JTextArea("BookCopy table");
 				table = new JTable(data, columnNames);
 			}
 
 			if (buttonClicked == "checkoutButton") {
 
 				// For creating the size of the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet count = stmt.executeQuery("SELECT * FROM borrowing");
 				List<String> borrowings = new ArrayList<String>();
 				while (count.next()) {
 					borrowings.add(count.getString("callNumber"));
 				}
 				Object data[][] = new Object[borrowings.size()][numCols];
 				count.close();
 
 				int borid;
 				int bid;
 				String callNumber;
 				String copyNo;
 				Date outDate;
 				Date inDate;
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					borid = rs.getInt("borid");
 					bid = rs.getInt("bid");
 					callNumber = rs.getString("callNumber");
 					copyNo = rs.getString("copyNo");
 					outDate = rs.getDate("outDate");
 					inDate = rs.getDate("inDate");
 					Object tuple[] = { borid, bid, callNumber, copyNo, outDate,
 							inDate };
 					data[j] = tuple;
 					j++;
 				}
 				tableTitle = new JTextArea("Borrowing table");
 				table = new JTable(data, columnNames);
 			}
 			
 			
 			if (buttonClicked == "holdRequestButton"){
 					
 					Statement stmt = Library.con.createStatement();
 					ResultSet count = stmt
 							.executeQuery("SELECT * FROM HoldRequest");
 					List<Integer> holds = new ArrayList<Integer>();
 					while (count.next()) {
 						holds.add(count.getInt("hid"));
 					}
 					
 					Object data[][] = new Object[holds.size()][numCols];
 					count.close();
 
 					int hid;
 					int bid;
 					String callNumber;
 					Date issuedDate;
 
 					int j = 0;
 
 					// Fill table
 					while (rs.next()) {
 						hid = rs.getInt("hid");
 						bid = rs.getInt("bid");
 						callNumber = rs.getString("callNumber");
 						issuedDate = rs.getDate("issuedDate");
 						
 						Object tuple[] = { hid, bid, callNumber, issuedDate };
 						data[j] = tuple;
 						
 						j++;
 					}
 
 					tableTitle = new JTextArea("HoldRequest table");
 					table = new JTable(data, columnNames);	
 			}
 
 			if (buttonClicked == "checkOverdueItems"){
 				
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
 				
 				PreparedStatement ps = Library.con
 						.prepareStatement("select borid, bid, callNumber, copyNo from borrowing where callNumber in (select callNumber from bookCopy where bookCopy.callNumber = borrowing.callNumber and bookCopy.copyNo = borrowing.copyNo and status like 'out') and inDate < ?");
 				ps.setDate(1, curDate);
 				ps.executeQuery();
 
 				ResultSet count = ps.getResultSet();
 				List<Integer> overdues = new ArrayList<Integer>();
 				while (count.next()) {
 					overdues.add(count.getInt("borid"));
 				}
 				
 				Object data[][] = new Object[overdues.size()][numCols];
 				count.close();
 
 				int borid;
 				int bid;
 				String callNumber;
 				String copyNo;
 				Date inDate;
 
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					borid = rs.getInt("borid");
 					bid = rs.getInt("bid");
 					callNumber = rs.getString("callNumber");
 					copyNo = rs.getString("copyNo");
 					inDate = rs.getDate("inDate");
 					
 					Object tuple[] = {borid, bid, callNumber, copyNo, inDate};
 					data[j] = tuple;
 					
 					j++;
 				}
 
 				tableTitle = new JTextArea("Overdue items");
 				table = new JTable(data, columnNames);	
 		}
 			if (buttonClicked == "payFineButton") {
 
 				// For creating the size of the table
 				Statement stmt = Library.con.createStatement();
 				ResultSet count = stmt.executeQuery("SELECT * FROM Fine");
 				List<Integer> fines = new ArrayList<Integer>();
 				while (count.next()) {
 					fines.add(count.getInt("fid"));
 				}
 				
 				Object data[][] = new Object[fines.size()][numCols];
 				count.close();
 
 				int fid;
 				String amount;
 				Date issuedDate;
 				Date paidDate;
 				int borid;
 
 				int j = 0;
 
 				// Fill table
 				while (rs.next()) {
 					fid = rs.getInt("fid");
 					amount = rs.getString("amount");
 					issuedDate = rs.getDate("issuedDate");
 					paidDate = rs.getDate("paidDate");
 					borid = rs.getInt("borid");
 
 					Object tuple[] = { fid, amount, issuedDate, paidDate,
 							borid };
 					data[j] = tuple;
 
 					j++;
 				}
 				
 				tableTitle = new JTextArea("Fines");
 				table = new JTable(data, columnNames);
 			}
 
 			table.setEnabled(false);
 			table.setPreferredSize(new Dimension(600, height));
 			JScrollPane scrollPane = new JScrollPane(table);
 			scrollPane.setPreferredSize(new Dimension(600, height));
 			table.setAutoCreateRowSorter(true);
 
 			// Display table
 			table.setFillsViewportHeight(true);
 			tablePane.removeAll();
 			tablePane.updateUI();
 			tableTitle.setEditable(false);
 			tablePane.add(tableTitle);
 			tablePane.add(scrollPane);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void showReportsTable(ResultSet rs, String reportsQuery, String subject) {
 		int numCols;
 		ResultSetMetaData rsmd;
 		JTextArea tableTitle = null;
 		JTable table = null;
 
 		try {
 			rsmd = rs.getMetaData();
 			numCols = rsmd.getColumnCount() + 1;	
 			
 			String columnNames[] = new String[numCols];
 			for (int i = 0; i < numCols - 1; i++) {
 				columnNames[i] = rsmd.getColumnName(i + 1);
 			}
 			columnNames[numCols-1] = "OVERDUE";
 
 			// For creating the size of the table
 			PreparedStatement ps1 = Library.con
 					.prepareStatement(reportsQuery);
 
 
 			System.out.println("QUERY: " + reportsQuery);
 			if (subject.isEmpty() == false) {
 				ps1.setString(1,  subject);
 			}
 			ps1.executeQuery();
 			ResultSet count = ps1.getResultSet();
 			List<String> books = new ArrayList<String>();
 			while (count.next()) {
 				books.add(count.getString("callNumber"));
 			}
 			
 			// Get current date
 			java.util.Date currentDate = new java.util.Date();
 			
 			Object data[][] = new Object[books.size()][numCols];
 			count.close(); 
 			String callNumber;
 			int copyNo;
 			Date inDate;
 			Date outDate;
 			String overdue = "NO";
 			int j = 0;
 
 			// Fill table
 			while (rs.next()) {
 				callNumber = rs.getString("callNumber");
 				copyNo = rs.getInt("copyNo");
 				inDate = rs.getDate("inDate");
 				outDate = rs.getDate("outDate");
 
 				try {
 					if (currentDate.after(inDate)) {
 						overdue = "YES";
 					}	
 				} catch (NullPointerException e) {
 					overdue = "N/A";
 				}
 
 				Object tuple[] = { callNumber, copyNo, inDate, outDate, overdue };
 				
 				data[j] = tuple;
 				j++;
 
 			}
 			
 			rs.close();
 			tableTitle = new JTextArea("Checked out Books Report");
 			table = new JTable(data, columnNames);
 			if(data.length == 0) {
 				
 				new ErrorMessage("No books found.");
 			}
 			
 			table.setEnabled(false);
			table.setPreferredSize(new Dimension(600, height));
 			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(600, height));
 			table.setAutoCreateRowSorter(true);
 
 			// Display table
 			table.setFillsViewportHeight(true);
 			tablePane.removeAll();
 			tablePane.updateUI();
 			tableTitle.setEditable(false);
 			tablePane.add(tableTitle);
 			tablePane.add(scrollPane);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public static void showPopularBooksReportsTable(ResultSet rs, String reportsQuery, Date[] years, int noBooks) {
 		int numCols;
 		ResultSetMetaData rsmd;
 		JTextArea tableTitle = null;
 		JTable table = null;
 
 		try {
 			rsmd = rs.getMetaData();
 			numCols = rsmd.getColumnCount();	
 			
 			String columnNames[] = new String[numCols];
 			for (int i = 0; i < numCols; i++) {
 				columnNames[i] = rsmd.getColumnName(i + 1);
 			}
 
 			// For creating the size of the table
 			PreparedStatement ps1 = Library.con
 					.prepareStatement(reportsQuery);
 
 
 			System.out.println("QUERY: " + reportsQuery);
 
 			ps1.setDate(1, years[0]);
 			ps1.setDate(2, years[1]);
 			ps1.setInt(3, noBooks);
 			ps1.executeQuery();
 			ResultSet count = ps1.getResultSet();
 			List<String> books = new ArrayList<String>();
 			while (count.next()) {
 				books.add(count.getString("callNumber"));
 			}
 			
 			Object data[][] = new Object[books.size()][numCols];
 			count.close(); 
 			String callNumber;
 			String title;
 			String mainAuthor;
 			int timesBorrowed = 0;
 			int j = 0;
 
 			// Fill table
 			while (rs.next()) {
 				callNumber = rs.getString("callNumber");
 				title = rs.getString("title");
 				mainAuthor = rs.getString("mainAuthor");
 				timesBorrowed= rs.getInt("timesBorrowed");
 
 				Object tuple[] = { callNumber, title, mainAuthor, timesBorrowed };
 				
 				data[j] = tuple;
 				j++;
 
 			}
 			
 			rs.close();
 			tableTitle = new JTextArea("Checked out Books Report");
 			table = new JTable(data, columnNames);
 			if(data.length == 0) {
 				
 				new ErrorMessage("No books found.");
 			}
 			
 			table.setEnabled(false);
 			table.setPreferredSize(new Dimension(600, 400));
 			JScrollPane scrollPane = new JScrollPane(table);
 			scrollPane.setPreferredSize(new Dimension(600, 400));
 			table.setAutoCreateRowSorter(true);
 
 			// Display table
 			table.setFillsViewportHeight(true);
 			tablePane.removeAll();
 			tablePane.updateUI();
 			tableTitle.setEditable(false);
 			tablePane.add(tableTitle);
 			tablePane.add(scrollPane);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	public static void showSearchResultsTable(ResultSet rs, String searchQuery, List<String> inputs) {
 		int numCols;
 		ResultSetMetaData rsmd;
 		JTextArea tableTitle = null;
 		JTable table = null;
 
 		try {
 			rsmd = rs.getMetaData();
 			numCols = rsmd.getColumnCount() + 2;
 
 			String columnNames[] = new String[numCols];
 			for (int i = 0; i < numCols-2; i++) {
 				columnNames[i] = rsmd.getColumnName(i + 1);
 			}
 			columnNames[numCols-2] = "IN";
 			columnNames[numCols-1] = "OUT";
 			// For creating the size of the table
 			PreparedStatement ps1 = Library.con
 					.prepareStatement(searchQuery);
 			for (int i = 0; i < inputs.size(); i++) {
 				ps1.setString(i+1, inputs.get(i));
 			}
 
 			System.out.println("QUERY: " + searchQuery);
 			ps1.executeQuery();
 			ResultSet count = ps1.getResultSet();
 			List<String> books = new ArrayList<String>();
 			while (count.next()) {
 				books.add(count.getString("callNumber"));
 			}
 			Object data[][] = new Object[books.size()][numCols];
 			count.close();
 
 			String callNumber;
 			String isbn;
 			String title;
 			String mainAuthor;
 			String publisher;
 			int copiesAvailable = 0;
 			int copiesOut = 0;
 			int year;
 			int j = 0;
 
 			// Fill table
 			while (rs.next()) {
 				callNumber = rs.getString("callNumber");
 				isbn = rs.getString("isbn");
 				title = rs.getString("title");
 				mainAuthor = rs.getString("mainAuthor");
 				publisher = rs.getString("publisher");
 				year = rs.getInt("year");
 				
 				// Check the number of available copies for a book
 				PreparedStatement ps2 = Library.con
 						.prepareStatement("SELECT count(*) from BookCopy WHERE callNumber = ? and status LIKE 'in'");
 				ps2.setString(1, callNumber);
 				ps2.executeQuery();
 				ResultSet rs2 = ps2.getResultSet();
 				if(rs2.next()) {
 					copiesAvailable = rs2.getInt(1);
 				}
 				
 				PreparedStatement ps3 = Library.con
 						.prepareStatement("SELECT count(*) from BookCopy WHERE callNumber = ? and status LIKE 'out'");
 				ps3.setString(1, callNumber);
 				ps3.executeQuery();
 				
 				ResultSet rs3 = ps3.getResultSet();
 				if(rs3.next()) {
 					copiesOut = rs3.getInt(1);
 				}
 
 				Object tuple[] = { callNumber, title, mainAuthor,
 						publisher, year, isbn, copiesAvailable, copiesOut };
 				
 				data[j] = tuple;
 				j++;
 
 			}
 			
 			rs.close();
 			tableTitle = new JTextArea("Search Results");
 			table = new JTable(data, columnNames);
 			if(data.length == 0) {
 				
 				new ErrorMessage("No books found.");
 			}
 			table.setEnabled(false);
 			table.setPreferredSize(new Dimension(600, height));
 			JScrollPane scrollPane = new JScrollPane(table);
 			scrollPane.setPreferredSize(new Dimension(600, height));
 			table.setAutoCreateRowSorter(true);
 
 			// Display table
 			table.setFillsViewportHeight(true);
 			tablePane.removeAll();
 			tablePane.updateUI();
 			tableTitle.setEditable(false);
 			tablePane.add(tableTitle);
 			tablePane.add(scrollPane);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	public static void showAccountTables(ResultSet rs1, ResultSet rs2,
 			ResultSet rs3, int bid) {
 		// Tables won't set size properly!!!
 
 		int numCols1;
 		int numCols2;
 		int numCols3;
 		ResultSetMetaData rsmd1;
 		ResultSetMetaData rsmd2;
 		ResultSetMetaData rsmd3;
 
 		JTextArea tableTitle1 = new JTextArea("Checked out books");
 		JTextArea tableTitle2 = new JTextArea("Outstanding fines");
 		JTextArea tableTitle3 = new JTextArea("Books on hold");
 
 		try {
 
 			rsmd1 = rs1.getMetaData();
 			rsmd2 = rs2.getMetaData();
 			rsmd3 = rs3.getMetaData();
 
 			numCols1 = rsmd1.getColumnCount();
 			numCols2 = rsmd2.getColumnCount();
 			numCols3 = rsmd3.getColumnCount();
 
 			// Get column names for checked out table
 			String columnNames1[] = new String[numCols1];
 			for (int i = 0; i < numCols1; i++) {
 				columnNames1[i] = rsmd1.getColumnName(i + 1);
 			}
 
 			// Get column names for fines table
 			String columnNames2[] = new String[numCols2];
 			for (int i = 0; i < numCols2; i++) {
 				columnNames2[i] = rsmd2.getColumnName(i + 1);
 			}
 
 			// Get column names for holds table
 			String columnNames3[] = new String[numCols3];
 			for (int i = 0; i < numCols3; i++) {
 				columnNames3[i] = rsmd3.getColumnName(i + 1);
 			}
 
 			// Get table sizes
 			PreparedStatement ps1 = Library.con
 					.prepareStatement("select borrowing.borid, bookcopy.callNumber, bookcopy.copyNo, borrowing.outDate, borrowing.inDate from Borrowing, BookCopy where Borrowing.callNumber=BookCopy.callNumber and Borrowing.copyNo=BookCopy.CopyNo and BookCopy.Status = 'out' and Borrowing.bid = ?");
 			ps1.setInt(1, bid);
 			ps1.executeQuery();
 
 			PreparedStatement ps2 = Library.con
 					.prepareStatement("Select fid, amount, issuedDate from Fine WHERE paidDate is NULL and borid in (select borrowing.borid from Borrowing, BookCopy where Borrowing.callNumber = BookCopy.callNumber and Borrowing.copyNo = BookCopy.copyNo and Borrowing.bid = ?)");
 			ps2.setInt(1, bid);
 			ps2.executeQuery();
 
 			PreparedStatement ps3 = Library.con
 					.prepareStatement("select holdrequest.hid, holdrequest.issuedDate, Book.callNumber, Book.isbn, Book.title from Book INNER JOIN HoldRequest on Book.callNumber = HoldRequest.callNumber where HoldRequest.bid = ?");
 			ps3.setInt(1, bid);
 			ps3.executeQuery();
 
 			List<String> checkedOut = new ArrayList<String>();
 			ResultSet count1 = ps1.getResultSet();
 			while (count1.next()) {
 				checkedOut.add(count1.getString("borid"));
 			}
 			Object data1[][] = new Object[checkedOut.size()][numCols1];
 			count1.close();
 
 			List<String> fines = new ArrayList<String>();
 			ResultSet count2 = ps2.getResultSet();
 			while (count2.next()) {
 				fines.add(count2.getString("amount"));
 			}
 			Object data2[][] = new Object[fines.size()][numCols2];
 			count2.close();
 
 			List<String> holds = new ArrayList<String>();
 			ResultSet count3 = ps3.getResultSet();
 			while (count3.next()) {
 				holds.add(count3.getString("callNumber"));
 			}
 			Object data3[][] = new Object[holds.size()][numCols3];
 			count3.close();
 
 			int borid;
 			String callNumber;
 			String copyNo;
 			Date outDate;
 			Date inDate;
 
 			int j = 0;
 
 			// Fill checked out table
 			while (rs1.next()) {
 				borid = rs1.getInt("borid");
 				callNumber = rs1.getString("callNumber");
 				copyNo = rs1.getString("copyNo");
 				outDate = rs1.getDate("outDate");
 				inDate = rs1.getDate("inDate");
 				Object tuple[] = { borid, callNumber, copyNo, outDate, inDate };
 				data1[j] = tuple;
 				j++;
 
 			}
 
 			int fid;
 			String amount;
 			Date issuedDate;
 
 			j = 0;
 
 			// Fill fines table
 			while (rs2.next()) {
 				fid = rs2.getInt("fid");
 				amount = rs2.getString("amount");
 				issuedDate = rs2.getDate("issuedDate");
 				Object tuple[] = { fid, amount, issuedDate };
 				data2[j] = tuple;
 				j++;
 
 			}
 
 			int hid;
 			Date issuedDate2;
 			String isbn;
 			String callNumber2;
 			String title;
 
 			j = 0;
 
 			// Fill holds table
 			while (rs3.next()) {
 				hid = rs3.getInt("hid");
 				issuedDate2 = rs3.getDate("issuedDate");
 				isbn = rs3.getString("isbn");
 				callNumber2 = rs3.getString("callNumber");
 				title = rs3.getString("title");
 				
 				Object tuple[] = { hid, issuedDate2, isbn, callNumber2, title };
 				data3[j] = tuple;
 				j++;
 
 			}
 			rs1.close();
 			rs2.close();
 			rs3.close();
 
 			// View tables
 			JTable checkedOutTable = new JTable(data1, columnNames1);
 			checkedOutTable.setEnabled(false);
 			checkedOutTable.setPreferredSize(new Dimension(600, 200));
 			JScrollPane scrollPane1 = new JScrollPane(checkedOutTable);
 			scrollPane1.setPreferredSize(new Dimension(600, 200));
 			checkedOutTable.setAutoCreateRowSorter(true);
 
 			JTable fineTable = new JTable(data2, columnNames2);
 			fineTable.setEnabled(false);
 			fineTable.setPreferredSize(new Dimension(600, 200));
 			JScrollPane scrollPane2 = new JScrollPane(fineTable);
 			scrollPane2.setPreferredSize(new Dimension(600, 200));
 			fineTable.setAutoCreateRowSorter(true);
 
 			JTable holdsTable = new JTable(data3, columnNames3);
 			holdsTable.setEnabled(false);
 			holdsTable.setPreferredSize(new Dimension(600, 200));
 			JScrollPane scrollPane3 = new JScrollPane(holdsTable);
 			scrollPane3.setPreferredSize(new Dimension(600, 200));
 			holdsTable.setAutoCreateRowSorter(true);
 
 			fineTable.setFillsViewportHeight(true);
 
 			tablePane.removeAll();
 			tablePane.updateUI();
 			tableTitle1.setEditable(false);
 			tablePane.add(tableTitle1);
 			tablePane.add(scrollPane1);
 
 			tableTitle2.setEditable(false);
 			tablePane.add(tableTitle2);
 			tablePane.add(scrollPane2);
 
 			tableTitle3.setEditable(false);
 			tablePane.add(tableTitle3);
 			tablePane.add(scrollPane3);
 
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
