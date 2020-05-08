 package UI;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import Objects.BookCopy;
 import Objects.Borrower;
 import Objects.HoldRequest;
 import Transactions.Transactions;
 
 public class CheckOutItemsDialog extends JFrame implements ActionListener{
 
 	JTextField borrowerID = new JTextField();
 	JTextField bookCallNumber = new JTextField();
 	JTextField copyNumber = new JTextField();
 	DefaultListModel listModel = new DefaultListModel();
 	ArrayList<String> callNumberList = new ArrayList<String>();
 	ArrayList<Integer> copyNumberList = new ArrayList<Integer>();
 	
 	static String CHECKOUT = "Check Out Books";
 	static String returnToClerkDialogString = "Return to Clerk Dialog";
 	
     private Connection con;
     
     public static final int VALIDATIONERROR = 2;
     
 	public CheckOutItemsDialog(String name)
 	{
 		super(name);
 	}
 	
 	private void addComponentsToPane(final Container pane)
 	{
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(6, 3));
 		
 		panel.add(new Label("Check Out Books"));
 		panel.add(new Label(""));
 		panel.add(new Label(""));
 		
 		panel.add(new Label("Borrower ID"));
 		panel.add(borrowerID);
 		panel.add(new Label(""));
 		
 		panel.add(new Label("Enter one call number"));
 		panel.add(bookCallNumber);
 		panel.add(new Label(""));
 		
 		panel.add(new Label("Enter Copy Number and press Add"));
 		JButton addCallNumberButton = new JButton("Add");
 		addCallNumberButton.setActionCommand("Add");
 		addCallNumberButton.addActionListener(this);
 		panel.add(copyNumber);
 		panel.add(addCallNumberButton);
 		
 		JList list = new JList(listModel);
 		JScrollPane scrollPane = new JScrollPane(list);
 		panel.add(scrollPane);
 		panel.add(new Label(""));
 		
 		
 		panel.add(new Label(""));
 		JButton returnToUserDialog = new JButton(returnToClerkDialogString);
 		returnToUserDialog.setActionCommand(returnToClerkDialogString);
 		returnToUserDialog.addActionListener(this);
 		
 		JButton checkOutButton = new JButton(CHECKOUT);
 		checkOutButton.setActionCommand(CHECKOUT);
 		checkOutButton.addActionListener(this);
 		
 		panel.add(returnToUserDialog);
 		panel.add(checkOutButton);
 		
 		pane.add(panel);
 	}
     public static void createAndShowGUI() {
         //Create and set up the window.
         CheckOutItemsDialog frame = new CheckOutItemsDialog("Check Out Books");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         frame.addComponentsToPane(frame.getContentPane());
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (returnToClerkDialogString.equals(arg0.getActionCommand()))
 		{
 			this.dispose();
 		}else if(arg0.getActionCommand().equals("Add"))
 		{
 			listModel.addElement(bookCallNumber.getText());
 			addCallAndCopyNumber();
 
 		}else if(arg0.getActionCommand().equals(CHECKOUT))
 		{
 			
 			Borrower b = getBorrower();
 			String borrowerType = b.getType();
 			if (borrowerType != null)
 			{
 				checkOutBooks(borrowerType, b.getBid());
 			}
 			
 		}
 
 	}
 	
 	
 	private Borrower getBorrower()
 	{
 		int bid = Integer.parseInt(borrowerID.getText().trim());
 		Transactions t = new Transactions();
 		Borrower b = t.showBorrowerById(bid);
 		return b;
 	}
 	private void checkOutBooks(String borrowerType, int bid)
 	{
 		String items = "";
 		String failedItems = "";
 		Transactions t = new Transactions();
 		List<HoldRequest> holds = t.showHoldRequestById(bid);
 
 		for (int i = 0; i < callNumberList.size(); i++)
 		{
 			
 			String callNumber = callNumberList.get(i);
 			int copyNumber = copyNumberList.get(i);
 			
 			BookCopy bc = t.showCopyOfGivenBook(callNumber, copyNumber);
 			if (bc.status == null)
 			{
 				failedItems = failedItems + callNumber + " ";
 				System.out.println(callNumber + " was not found.");
 			}else
 			{
 				HoldRequest h = thisUsersHold(holds, bc);
 				if (bc.status.equals(Constants.IN))
 				{
 					items = items + callNumber + " ";
 					t.updateBookCopyStatus(callNumber, copyNumber, Constants.OUT);
 					
 					t.insertBorrowing(callNumber, copyNumber, Integer.parseInt(borrowerID.getText().trim()), getCurrentDateInStringFormat(), null);
				}else if (bc.status.equals(Constants.ON_HOLD) && h != null && !(h.issuedDate.equals("null")))
 				{
 					
 					items = items + callNumber + " ";
 					t.updateBookCopyStatus(callNumber, copyNumber, Constants.OUT);
 					
 					t.insertBorrowing(callNumber, copyNumber, Integer.parseInt(borrowerID.getText().trim()), getCurrentDateInStringFormat(), null);
 					t.deleteHoldREquest(h.hid);
 				}else
 				{
 					failedItems = failedItems + callNumber + " ";
 				}
 			}
 			
 		}
 		if (failedItems.length() == 0)
 		{
 			GiveMeTitleAndMessageDialog.createAndShowGUI("Check out success", "The following books " + items + "are due on " + this.getReturnDate(borrowerType));
 		}else
 		{
 			GiveMeTitleAndMessageDialog.createAndShowGUI("Check out result", "The following books " + items + "are due on " + this.getReturnDate(borrowerType) + ".  The following" +
 					" items were not checked out successfully " + failedItems);
 		}
 		clearAllFields();
 	}
 	
 	private HoldRequest thisUsersHold(List<HoldRequest> holds, BookCopy bc)
 	{
 		for (HoldRequest hr : holds)
 		{
			String hrCallNumber = hr.callNumber;
			String bcCallNumber =  bc.callNumber;
			String hrIssuedDate = hr.issuedDate;
			if (hrCallNumber.equals(bcCallNumber) && !hrIssuedDate.equals("null"))
 			{
 				return hr;
 			}
 		}
 		return null;
 	}
 	
 	private void clearAllFields()
 	{
 		this.borrowerID.setText("");
 		this.bookCallNumber.setText("");
 		this.copyNumber.setText("");
 		copyNumberList.clear();
 		callNumberList.clear();
 		listModel.clear();
 	}
 	private void addCallAndCopyNumber()
 	{
 		this.callNumberList.add(bookCallNumber.getText());
 		this.copyNumberList.add(Integer.parseInt(copyNumber.getText().trim()));
 		bookCallNumber.setText("");
 		copyNumber.setText("");
 	}
 	
 	private String getCurrentDateInStringFormat()
 	{
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		Date date = new Date();
 		return dateFormat.format(date);
 	}
 	
 	private String getReturnDate(String borrowerType)
 	{
 		Calendar c = Calendar.getInstance();
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		
 		try {
 			c.setTime(dateFormat.parse(getCurrentDateInStringFormat()));
 			if (borrowerType.equals(Constants.STAFF))
 			{
 				c.add(Calendar.DATE, 42);
 			}else if (borrowerType.equals(Constants.STUDENT))
 			{
 				c.add(Calendar.DATE, 14);
 			}else if (borrowerType.equals(Constants.FACULTY))
 			{
 				c.add(Calendar.DATE, 84);
 			}else
 			{
 				c.add(Calendar.DATE, 14);
 			}
 			
 			String duedate = dateFormat.format(c.getTime());
 			return duedate;
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "Error occurred calculating due date";
 		}
 	}
 }
