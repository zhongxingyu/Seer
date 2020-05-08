 /**
  * filename: BookFrame.java
  * description: 
  * 
  * @author Jake Gregg
  * @version 1.0
  */
 
 package books;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class BookFrame extends JFrame{
 	
 	JPanel searchPanel; // panels for holding search and button options
 	JPanel buttonPanel;
 	JButton search; // buttons implemented
 	JButton clearForm;
 	JButton addBook;
 	JButton addAuthor;
 	JButton searchByAuthor;
 	JButton searchByTitle;
 	JButton removeBook;
 	JButton searchByKeyword;
 	JButton modifyBook;
 	JButton modifyAuthor;
 	JTextField bookTitle; // text fields for book search
 	JTextField bookPubDay;
 	JTextField bookPubMonth;
 	JTextField bookPubYear;
 	JTextField bookID;
 	JTextField authorFirstName; // text fields for author search
 	JTextField authorMiddleName;
 	JTextField authorLastName;
 	JTextField authorBDay;
 	JTextField authorBMonth;
 	JTextField authorBYear;
 	JTextField authorID;
 	
 	public BookFrame() {
 		
 		// construct a new frame, set general settings
 		super("Book & Author Search");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		setBounds(200, 200, 800, 300);
 		setResizable(false);
 		
 		// searchPanel holds the book and search options, which are
 		// on the bookPanel and authorPanel respectively
 		searchPanel = new JPanel(new GridLayout(1, 2));
 //		searchPanel.setLayout(null);
 		JPanel bookPanel = new JPanel();
 		bookPanel.setLayout(null);
 		JPanel authorPanel = new JPanel();
 		authorPanel.setLayout(null);
 		
 		// Labels for labeling the textboxes
 		JLabel bookLabel1 = new JLabel("Title");
 		JLabel bookLabel2 = new JLabel("Publish Day");
 		JLabel bookLabel3 = new JLabel("Publish Month");
 		JLabel bookLabel4 = new JLabel("Publish Year");
 		JLabel bookLabel5 = new JLabel("Book ID");
 		JLabel bookLabel6 = new JLabel("Book");
 		JLabel authorLabel1 = new JLabel("First Name");
 		JLabel authorLabel2 = new JLabel("Middle Name");
 		JLabel authorLabel3 = new JLabel("Last Name");
 		JLabel authorLabel4 = new JLabel("Birth Day");
 		JLabel authorLabel5 = new JLabel("Birth Month");
 		JLabel authorLabel6 = new JLabel("Birth Year");
 		JLabel authorLabel7 = new JLabel("Author ID");
 		JLabel authorLabel8 = new JLabel("Author");
 		
 		Font bold1 = new Font(bookLabel6.getFont().getName(),
 				Font.BOLD, bookLabel6.getFont().getSize());
 		Font bold2 = new Font(authorLabel8.getFont().getName(),
 				Font.BOLD, authorLabel8.getFont().getSize());
 		
 		// text fields for getting the information from the user
 		bookTitle = new JTextField(); // books 
 		bookPubDay = new JTextField();
 		bookPubMonth = new JTextField();
 		bookPubYear = new JTextField();
 		bookID = new JTextField();
 		authorFirstName = new JTextField(); // authors
 		authorMiddleName = new JTextField();
 		authorLastName = new JTextField();
 		authorBDay = new JTextField();
 		authorBMonth = new JTextField();
 		authorBYear = new JTextField();
 		authorID = new JTextField();
 		
 		// add the labels and textfields to the bookPanel
 		bookLabel6.setFont(bold1); // book header
 		bookPanel.add(bookLabel6); 
 		bookLabel6.setBounds(140, 10, 200, 25);
 		
 		bookPanel.add(bookLabel1); // title
 		bookLabel1.setBounds(10, 35, 200, 25); 
 		bookPanel.add(bookTitle);
 		bookTitle.setBounds(8, 60, 300, 25);
 		
 		bookPanel.add(bookLabel2); // pub day, month, year
 		bookLabel2.setBounds(10, 85, 200, 25);
 		bookPanel.add(bookLabel3);
 		bookLabel3.setBounds(100, 85, 200, 25);
 		bookPanel.add(bookLabel4);
 		bookLabel4.setBounds(210, 85, 200, 25);
 		bookPanel.add(bookPubDay);
 		bookPubDay.setBounds(8, 110, 75, 25);
 		bookPanel.add(bookPubMonth);
 		bookPubMonth.setBounds(108, 110, 75, 25);
 		bookPanel.add(bookPubYear);
 		bookPubYear.setBounds(208, 110, 75, 25);
 		
 		bookPanel.add(bookLabel5); // book ID
 		bookLabel5.setBounds(10, 135, 200, 25);
 		bookPanel.add(bookID);
 		bookID.setBounds(8, 160, 200, 25);
 		
 		
 		// add the labels annd textfields to the authorPanel
 		authorLabel8.setFont(bold2); // author header
 		authorPanel.add(authorLabel8);
 		authorLabel8.setBounds(140, 10, 200, 25);
 		
 		authorPanel.add(authorLabel1); // first, middle, last name
 		authorLabel1.setBounds(10, 35, 200, 25);
 		authorPanel.add(authorLabel2);
 		authorLabel2.setBounds(150, 35, 200, 25);
 		authorPanel.add(authorLabel3);
 		authorLabel3.setBounds(260, 35, 200, 25);
 		authorPanel.add(authorFirstName); 
 		authorFirstName.setBounds(8, 60, 125, 25);
 		authorPanel.add(authorMiddleName);
 		authorMiddleName.setBounds(150, 60, 75, 25);
 		authorPanel.add(authorLastName);
 		authorLastName.setBounds(260, 60, 125, 25);
 		
 		authorPanel.add(authorLabel4); // birth day, month, year
 		authorLabel4.setBounds(10, 85, 200, 25);
 		authorPanel.add(authorLabel5);
 		authorLabel5.setBounds(100, 85, 200, 25);
 		authorPanel.add(authorLabel6);
 		authorLabel6.setBounds(200, 85, 200, 25);
 		authorPanel.add(authorBDay);
 		authorBDay.setBounds(8, 110, 75, 25);
 		authorPanel.add(authorBMonth);
 		authorBMonth.setBounds(108, 110, 75, 25);
 		authorPanel.add(authorBYear);
 		authorBYear.setBounds(208, 110, 75, 25);
 		
 		authorPanel.add(authorLabel7); // author ID
 		authorLabel7.setBounds(10, 135, 200, 25);
 		authorPanel.add(authorID);
 		authorID.setBounds(8, 160, 200, 25);
 		
 		// add the two panels to the searchPanel
 		searchPanel.add(bookPanel);
 		searchPanel.add(authorPanel);
 		
 		// initialize all buttons
 		buttonPanel = new JPanel(new GridLayout(3, 3));
 		clearForm = new JButton("Clear Form");
 		addBook = new JButton("Add Book");
 		addAuthor = new JButton("Add Author");
 		searchByAuthor = new JButton("Search by Author");
 		searchByTitle = new JButton("Search by Title");
 		removeBook = new JButton("Remove Book");
 		searchByKeyword = new JButton("Search by Keyword");
 		modifyBook = new JButton("Modify Book...");
 		modifyAuthor = new JButton("Modify Author...");
 		
 		// add buttons to panel
 		buttonPanel.add(addBook);
 		buttonPanel.add(addAuthor);
 		buttonPanel.add(removeBook);
 		buttonPanel.add(searchByAuthor);
 		buttonPanel.add(searchByTitle);
 		buttonPanel.add(searchByKeyword);
 		buttonPanel.add(modifyBook);
 		buttonPanel.add(modifyAuthor);
 		buttonPanel.add(clearForm);
 		
 		// add the panel to the frame
 		this.add(searchPanel, BorderLayout.CENTER);
 		this.add(buttonPanel, BorderLayout.SOUTH);
 		
 		// connect all the buttons to the ActionListener
 		Click button = new Click();
 		clearForm.addActionListener(button);
 		addBook.addActionListener(button);
 		addAuthor.addActionListener(button);
 		searchByAuthor.addActionListener(button);
 		searchByTitle.addActionListener(button);
 		removeBook.addActionListener(button);
 		searchByKeyword.addActionListener(button);
 		modifyBook.addActionListener(button);
 		modifyAuthor.addActionListener(button);
 		
 		// make the JFrame visible
 		setVisible(true);
 	}
 	
 	/**
 	 * 
 	 * @author Jake Gregg
 	 *
 	 */
 	public class Click implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == addBook) {
 				
 			}
 			else if (e.getSource() == addAuthor) {
 				
 			}
 			else if (e.getSource() == searchByAuthor) {
 				
 			}
 			else if (e.getSource() == searchByTitle) {
 				
 			}
 			else if (e.getSource() == removeBook) {
 				
 			}
 			else if (e.getSource() == searchByKeyword) {
 				
 			}
 			else if (e.getSource() == modifyBook) {
 				
 			}
 			else if (e.getSource() == modifyAuthor) {
 				
 			}
 			// this clears all the form data
 			else if (e.getSource() == clearForm) {
 				authorFirstName.setText("");
 				authorMiddleName.setText("");
 				authorLastName.setText("");
 				authorBDay.setText("");
 				authorBMonth.setText("");
 				authorBYear.setText("");
 				authorID.setText("");
 				bookTitle.setText("");
 				bookPubDay.setText("");
 				bookPubMonth.setText("");
 				bookPubYear.setText("");
 				bookID.setText("");
 			}
 		}
 		
 	}
 }
