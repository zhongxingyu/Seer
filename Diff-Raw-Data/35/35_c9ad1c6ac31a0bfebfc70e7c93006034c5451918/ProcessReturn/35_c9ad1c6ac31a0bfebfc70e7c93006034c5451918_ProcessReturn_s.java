 package transactions;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import model.BookCopy;
 import model.Borrowing;
 import model.Fine;
 import model.HoldRequest;
 
 import ui.LibraryDB;
 
 public class ProcessReturn extends JFrame{
 
 	// dimensions of the window
 	private int WIDTH = 400;
 	private int HEIGHT = 500;
 	private List<JTextField> textFields = new ArrayList<JTextField>();	
 
 	public ProcessReturn(){
 		super("Process Return");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //Exits the window when user clicks on "x"
 
 		initPanel();
 
 		// Center the window
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = screenSize.width/2 - WIDTH/2;
 		int y = screenSize.height/2 - HEIGHT/2;
 		this.setLocation(x,y);
 
 		setSize(WIDTH,HEIGHT);
 		setVisible(true);
 	}
 
 	private void initPanel() {
 		int scale = 10;
 		int disp = 10;
 		JPanel p = new JPanel();
 		p.setLayout(null);
 		JLabel label0 = new JLabel("Please enter item info");
 		label0.setBounds(WIDTH/4, 0, WIDTH/2, HEIGHT/scale);
 		label0.setHorizontalAlignment(SwingConstants.CENTER);
 		label0.setVerticalAlignment(SwingConstants.CENTER);
 		p.add(label0);
 
 		JLabel label1 = new JLabel("Call Number: ");
 		label1.setBounds(0, HEIGHT/scale, WIDTH/3, HEIGHT/scale);
 		label1.setHorizontalAlignment(SwingConstants.RIGHT);
 		label1.setVerticalAlignment(SwingConstants.CENTER);
 		p.add(label1);
 
 		// call number text field
 		JTextField tf1 = new JTextField();
 		tf1.setBounds(WIDTH/2, HEIGHT/scale + disp , WIDTH/3 , HEIGHT/scale - 2*disp);
 		textFields.add(tf1);
 		p.add(tf1);
 
 		JLabel label2 = new JLabel("Copy Number: ");
 		label2.setBounds(0, HEIGHT*2/scale, WIDTH/3, HEIGHT/scale);
 		label2.setHorizontalAlignment(SwingConstants.RIGHT);
 		label2.setVerticalAlignment(SwingConstants.CENTER);
 		p.add(label2);
 
 		// copy number text field
 		JTextField tf2 = new JTextField();
 		tf2.setBounds(WIDTH/2, HEIGHT*2/scale + disp , WIDTH/3 , HEIGHT/scale - 2*disp);
 		textFields.add(tf2);
 		p.add(tf2);
 
 		JButton submitButton = new JButton("Submit");
 		submitButton.setBounds(WIDTH/4, HEIGHT*4/scale, WIDTH/2, HEIGHT/scale);
 		submitButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				onClick();
 
 			}
 
 		});
 		p.add(submitButton);
 		this.add(p);
 	}
 
 
 	private void onClick() {
 		String newStatus;
 		String callNumber = textFields.get(0).getText().trim();
 		int copyNo;
 		if (!isNumeric(textFields.get(1).getText().trim())){
 			popMsg("copyNo must be a number!");
 			return;
 		}
 		copyNo = Integer.parseInt(textFields.get(1).getText().trim());
 
 		// check if book exists
 		if(!LibraryDB.getManager().hasBookCopy(callNumber, copyNo)){
 			popMsg("Book does not exist in library!");
 			return;
 		}
 
 		// check if bookcopy is available for borrowing, i.e. status = out.
 		if (!LibraryDB.getManager().isBookCopyStatus(callNumber,copyNo,BookCopy.OUT)){
 			popMsg("Sorry! This book is not out, please double check call number and copyNo.");
 			return;
 		}
 
 		// Message to be shown later
 		String msg = "Item Returned! \n";
 		
 		//Retrieve Borrowing
 		Borrowing b = LibraryDB.getManager().getBorrowing(callNumber, copyNo);
 
 		// set inDate
 		Calendar currentCal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
 		String currentDate = calendarToString(currentCal);
 		LibraryDB.getManager().updateBorrowingInDate(b.getBorid(), currentDate);
 		
 		//Check Overdue and create a new Fine if it is.
		if (isOverDue(b)) {
			Fine f = new Fine(0,0,currentDate,null,b.getBorid());
 			LibraryDB.getManager().insertFine(f);
 			msg += "Fine is assessed. \n";
 		}
 
 		// Check if on-hold
 		newStatus = getNewStatus(callNumber);
 
 		if (newStatus.matches(BookCopy.ON_HOLD)){
 			msg += "Book is placed on-hold. \n";
 		}
 		LibraryDB.getManager().updateBookCopy(callNumber, copyNo, newStatus);
 		
 		popMsg(msg);
 		this.dispose();
 	}
 	
 	private Calendar stringToCalendar(String str){
 		String parts[] = str.split("/");
 		int year = Integer.parseInt(parts[0]);
 		int month = Integer.parseInt(parts[1]) - 1;
 		int date = Integer.parseInt(parts[2]);
 		Calendar cal = Calendar.getInstance();
 		cal.set(year, month, date);
 		return cal;
 		
 	}
 	
	private boolean isOverDue(Borrowing b) {
 		int timeLimit = 0;
 		Calendar currentCal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
 		
 		try {
 			timeLimit = LibraryDB.getManager().getTimeLimit(b.getBid());
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
			return false;
 		}
 		
 		Calendar dueCal = stringToCalendar(b.getOutDate());
 		dueCal.add(Calendar.DATE, timeLimit);
		if (dueCal.before(currentCal)) return true;	
		System.out.println(b.getBid());
		return false;
 	}
 
 	private String calendarToString(Calendar calendar) {
 		int year = calendar.get(Calendar.YEAR);
 		int month = calendar.get(Calendar.MONTH) + 1;
 		int day = calendar.get(Calendar.DAY_OF_MONTH);
 		String result = year + "/"+ month + "/" + day;
 		return result;
 	}
 
 	private String getNewStatus(String callNumber) {
 		List<HoldRequest> requests = LibraryDB.getManager().getHoldRequests(callNumber);
 		if (requests.size() >= 1) return BookCopy.ON_HOLD;
 		return "in";
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
 
 	private void popMsg(String msg){
 		JOptionPane.showMessageDialog (this, msg);
 	}
 }
