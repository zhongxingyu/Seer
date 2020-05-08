 package transactions;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import model.Book;
 import model.BookCopy;
 import model.Borrower;
 import ui.LibraryDB;
 
 public class AddBook extends JFrame{
 	
 	private int WIDTH = 300;
 	private int HEIGHT = 400;
 	private List<JTextField> textFields = new ArrayList<JTextField>();
 	
 	public AddBook() {
 		super("Add Book");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		initPanel();
 		
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = screenSize.width/2 - WIDTH/2;
 		int y = screenSize.height/2 - HEIGHT/2;
 		this.setLocation(x,y);
 		
 		setSize(WIDTH,HEIGHT);
 		setVisible(true);
 	}
 	
 	private void createLabel(JPanel p,int i){
 		JLabel label = new JLabel(indexToMsg(i) + ": ");
 		label.setHorizontalAlignment(SwingConstants.RIGHT);
 		label.setVerticalAlignment(SwingConstants.CENTER);
 		label.setBounds(0, i*HEIGHT/11, 3*WIDTH/7, HEIGHT/11);
 		if (i==0)label.setVisible(false);
 		p.add(label);
 	}
 	
 	private void createTextField(JPanel p, int i){
 		JTextField tf = new JTextField();
 		int tf_y = i*HEIGHT/11;
 		int tf_h = HEIGHT/15;
 		tf.setBounds(WIDTH/2, tf_y + (HEIGHT/11 - tf_h)/2 , WIDTH/3 , tf_h);
 		textFields.add(tf);
 		if (i==0)tf.setVisible(false);
 		p.add(tf);
 	}
 	private void initPanel() {
 		JPanel p = new JPanel();
 		p.setLayout(null);
 		for (int i=0;i<7;i++){
 			createLabel(p,i);
 			createTextField(p,i);
 		}
 
 		// Add confirm button
 		JButton confirmButton = new JButton("Confirm");
 		confirmButton.setBounds(WIDTH/4, 9*HEIGHT/11, WIDTH/2, HEIGHT/13);
 		confirmButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				String year = textFields.get(6).getText().trim();
 				
 				if (!isNumeric(year)) {
 					popMsg("Year needs to be a number!");
 					return;
 				}
 				
 				//Execute when button is pressed
 				Book b = new Book(
 						textFields.get(1).getText().trim(),
 						textFields.get(2).getText().trim(),
 						textFields.get(3).getText().trim(),
 						textFields.get(4).getText().trim(),
 						textFields.get(5).getText().trim(),
 						Integer.parseInt(year));
 				
 				BookCopy bcUpdate = new BookCopy(
 						textFields.get(1).getText().trim(),
						0, "in");
 				System.out.println("Book has been created");
 				String callNumber = b.getCallNumber();
 				Integer countBook = LibraryDB.getManager().countBook(callNumber);
 				if (countBook > 0) { // returns true if book exists
 						try {
 							Integer countBookCopy = LibraryDB.getManager().countBookCopy(callNumber);
 							System.out.println(countBookCopy);
 							BookCopy bc = new BookCopy(b.getCallNumber(), countBookCopy + 1, "in"); // insert book copy instead
 							LibraryDB.getManager().insertBookCopy(bc);
 							System.out.println("This is happening 1");
 							exitWindow();
 							System.out.println("Submit book copy");
 						} catch (SQLException e1) { 
 							determineError(e1);
 						}
 					}
 					else {
 						try {
 							System.out.println("We are trying!");
 							LibraryDB.getManager().insertBook(b); // just insert the book if callnumber doesnt exist
 							exitWindow();
 							System.out.println("Submit book");
 							} catch (SQLException e1) { 
 								determineError(e1);
 							}
 					}
 				}
 
 
 			
 		});
 		p.add(confirmButton);
 		this.add(p);
 		
 		
 	}
 	
 	private void determineError(SQLException e){
 		if (e.getMessage().contains("ORA-01400"))
 			popMsg("Error! \nOne of the values are not given. \nPlease try again.");
 		if (e.getMessage().contains("ORA-00001"))
 			popMsg("Error! \ncall number already exists! \nPlease try again.");
 	//	if (e.getMessage().contains("ORA-02291"))
 		//	popMsg("Error! \ntype must be one of: \nstudent , faculty , or staff");
 
 	}
 
 	private void exitWindow(){
 		this.dispose();
 	}
 
 	private void popMsg(String msg){
 		JOptionPane.showMessageDialog (this, msg);
 	}
 
 	private String indexToMsg(int i) {
 		switch (i){
 		case 1:	return "callnumber"; 
 		case 2:	return "isbn";
 		case 3: return "title";
 		case 4: return "mainAuthor";
 		case 5: return "publisher";
 		case 6: return "year";
 		}
 		return "";
 
 	}
 	
 	private boolean isNumeric(String str)  
 	{  
 	  try  
 	  {  
 	    int i = Integer.parseInt(str);  
 	  }  
 	  catch(NumberFormatException nfe)  
 	  {  
 	    return false;  
 	  }  
 	  return true;  
 	}
 }
