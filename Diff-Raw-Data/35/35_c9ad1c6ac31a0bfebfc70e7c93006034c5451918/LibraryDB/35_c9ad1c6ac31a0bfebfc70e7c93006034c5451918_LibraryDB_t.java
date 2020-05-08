 package ui;
 
 import java.sql.*;
 
 // for reading from the command line
 import java.io.*;
 
 // for the login window
 import javax.swing.*;
 
 import model.Book;
 import model.BookCopy;
 import model.Borrower;
 import model.BorrowerType;
 import model.Borrowing;
 import model.Fine;
 import model.HasAuthor;
 import model.HasSubject;
 import model.HoldRequest;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import jdbc.JDBCManager;
 
 
 public class LibraryDB implements ActionListener
 {
 
 	private static JDBCManager manager;
 	private Connection con;
 
 	// user is allowed 3 login attempts
 	private int loginAttempts = 0;
 
 	// components of the login window
 	private JTextField usernameField;
 	private JPasswordField passwordField;
 	private JFrame loginFrame;
 	private UserFrame userFrame;
 
 
 	/*
 	 * constructs login window and loads JDBC driver
 	 */ 
 	public LibraryDB()
 	{
 		loginFrame = new JFrame("User Login");
 
 		JLabel usernameLabel = new JLabel("Enter username: ");
 		JLabel passwordLabel = new JLabel("Enter password: ");
 
 		usernameField = new JTextField(10);
 		passwordField = new JPasswordField(10);
 		passwordField.setEchoChar('*');
 
 		JButton loginButton = new JButton("Log In");
 
 		JPanel contentPane = new JPanel();
 		loginFrame.setContentPane(contentPane);
 		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// layout components using the GridBag layout manager
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 
 		contentPane.setLayout(gb);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		// place the username label 
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(10, 10, 5, 0);
 		gb.setConstraints(usernameLabel, c);
 		contentPane.add(usernameLabel);
 
 		// place the text field for the username 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(10, 0, 5, 10);
 		gb.setConstraints(usernameField, c);
 		contentPane.add(usernameField);
 
 		// place password label
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 10, 0);
 		gb.setConstraints(passwordLabel, c);
 		contentPane.add(passwordLabel);
 
 		// place the password field 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 10, 10);
 		gb.setConstraints(passwordField, c);
 		contentPane.add(passwordField);
 
 		// place the login button
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(5, 10, 10, 10);
 		c.anchor = GridBagConstraints.CENTER;
 		gb.setConstraints(loginButton, c);
 		contentPane.add(loginButton);
 
 		// register password field and OK button with action event handler
 		passwordField.addActionListener(this);
 		loginButton.addActionListener(this);
 
 		// anonymous inner class for closing the window
 		loginFrame.addWindowListener(new WindowAdapter() 
 		{
 			public void windowClosing(WindowEvent e) 
 			{ 
 				System.exit(0); 
 			}
 		});
 
 		// size the window to obtain a best fit for the components
 		loginFrame.pack();
 
 		// center the frame
 		Dimension d = loginFrame.getToolkit().getScreenSize();
 		Rectangle r = loginFrame.getBounds();
 		loginFrame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		// make the window visible
 		loginFrame.setVisible(true);
 
 		// place the cursor in the text field for the username
 		usernameField.requestFocus();
 
 		manager = new JDBCManager();
 		
 	}
 
 
 	/*
 	 * event handler for login window
 	 */ 
 	public void actionPerformed(ActionEvent e) 
 	{
 		if ( manager.connect(usernameField.getText(), String.valueOf(passwordField.getPassword())) )
 		{
 			// if the username and password are valid, 
 			// remove the login window and display the User Frame 
 			loginFrame.dispose();
 			userFrame = new UserFrame();
 			new DisplayDB();
			//initTableValues(); 
 			//TODO
 		}
 		else
 		{
 			loginAttempts++;
 
 			if (loginAttempts >= 3)
 			{
 				loginFrame.dispose();
 				System.exit(-1);
 			}
 			else
 			{
 				// clear the password
 				passwordField.setText("");
 			}
 		}             
 
 	}
 
 	public static JDBCManager getManager(){
 		return manager;
 	}
 	
 	public static void main(String args[])
 	{
 		new LibraryDB();
 		
 	}
 	
 	private void initTableValues(){
 		manager.insertBorrowerType(new BorrowerType("student",14));
 		manager.insertBorrowerType(new BorrowerType("faculty",84));
 		manager.insertBorrowerType(new BorrowerType("staff",42));		
 	
 		
 		try {
 			// Books
 			manager.insertBook(new Book("QA111","82746383","The Monster","Someone","pluto",1991));
 			manager.insertBook(new Book("QA222","0747581088","Harry Potter","J.K. Rowling","Bloomsbury",2005));
 			manager.insertBook(new Book("QA333", "9999", "Book name", "AuthorA", "PublisherB", 2013));
 			// Borrowers
 			manager.insertBorrower(new Borrower(0,"abcdefg","Kenny","Vancouver",604158,"a@b.com",73559098,"2014/01/01","student"));
 			manager.insertBorrower(new Borrower(0,"password","Jimmy","Surrey",604720,"jimmy@zhang.com",733232424,"2015/01/01","faculty"));
 			// BookCopies
 			manager.insertBookCopy(new BookCopy("QA222",1,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA222",2,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA222",3,BookCopy.ON_HOLD));
 			manager.insertBookCopy(new BookCopy("QA222",4,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA222",5,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA222",6,BookCopy.ON_HOLD));
 			manager.insertBookCopy(new BookCopy("QA111",1,BookCopy.IN));
 			manager.insertBookCopy(new BookCopy("QA111",2,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA111",3,BookCopy.IN));
 			manager.insertBookCopy(new BookCopy("QA333",1,BookCopy.OUT));
 			manager.insertBookCopy(new BookCopy("QA333",2,BookCopy.IN));
 			// Borrowings
 			manager.insertBorrowing(new Borrowing(0,3,"QA111",1,"2013/01/02","2013/02/26"));
 			manager.insertBorrowing(new Borrowing(0,2,"QA222",1,"2013/01/01",null)); 
 			manager.insertBorrowing(new Borrowing(0,2,"QA222",2,"2013/03/19",null));
 			manager.insertBorrowing(new Borrowing(0,2,"QA222",3,"2013/03/01","2013/03/20"));
 			manager.insertBorrowing(new Borrowing(0,2,"QA222",4,"2013/03/01",null));
 			manager.insertBorrowing(new Borrowing(0,2,"QA222",5,"2013/03/02",null));
 			manager.insertBorrowing(new Borrowing(0,2,"QA111",2,"2013/03/02",null));
 			manager.insertBorrowing(new Borrowing (0, 2, "QA333", 1, "2013/03/31", null));
 			// Hold Request
 			manager.insertHoldRequest(new HoldRequest(0,3,"QA222","2013/03/10"));
 			// Fine
 			manager.insertFine(new Fine(0, 200, "2013/03/20", null, 5));
 			manager.insertFine(new Fine(0, 300,"2013/01/16","2013/02/26",2));
 			// Has Subject
 			manager.insertHasSubject(new HasSubject("QA111","Science"));
 			manager.insertHasSubject(new HasSubject("QA111","ANIME"));
 			manager.insertHasSubject(new HasSubject("QA222","Japanese"));
 			// Has Author
 			manager.insertHasAuthor(new HasAuthor("QA111","Peter"));
 			manager.insertHasAuthor(new HasAuthor("QA222","George"));
 			
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 }
