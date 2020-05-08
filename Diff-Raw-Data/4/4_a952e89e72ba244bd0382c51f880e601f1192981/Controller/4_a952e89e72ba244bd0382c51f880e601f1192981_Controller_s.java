 import java.util.*;
 
 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.event.*;
 
 public class Controller 
 {
 	protected ArrayList<Account> accounts = new ArrayList<Account>();
 	protected ArrayList<Meter> meters = new ArrayList<Meter>();
 	
 	private static final Controller instance = new Controller();
 	 
     private Controller() {}
  
     public static Controller getInstance() {
         return instance;
     }
     
     protected Meter_IO dataMeter;
     protected Account_IO dataAccount;
     
 	public static void main(String[] args)
 	{
 		Controller.getInstance().dataAccount = new Account_IO();
 		Controller.getInstance().dataMeter = new Meter_IO();
 		Controller.UserInterface ui = Controller.getInstance().new UserInterface();
 	}
 	
 	public static void createAccount()
 	{
 		/*TODO write createAccount() method */
 	}	
 	public static void deleteAccount()
 	{
 		/*TODO write deleteAccount() method */
 	}
 	//Save to file
 
 
 	class UserInterface
 	{
 
 		public UserInterface()
 		{	
 			final JFrame mainFrame = new JFrame();
			mainFrame.setSize(1000, 1000);
			
 			JMenuBar menuBar = new JMenuBar();
 			menuBar.add(new JMenuItem("File"));
 			mainFrame.setJMenuBar(menuBar);
 			
 			String acc[] = new String[3];
 			acc[0] = "Mark Duncan";
 			acc[1] = "Avi Levy";
 			acc[2] = "Donald Trump";
 			
 			JList accounts = new JList(acc);
 			JScrollPane accountScrollPane = new JScrollPane(accounts);
 			JPanel input = new JPanel();
 			JButton addReading = new JButton("Enter Meter Reading");
 			JButton addAccount = new JButton("New Residential Account");
 			
 			addReading.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e){	
 					
 					
 					JTextField meterIDField = new JTextField(5);
 				    JTextField dateField = new JTextField(12);
 				    JTextField readingField = new JTextField(10);
 				    Object[] options = {"SAVE", "CANCEL"};
 
 				      JPanel myPanel = new JPanel();
 				      myPanel.add(new JLabel("Meter ID"));
 				      myPanel.add(meterIDField);
 				      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
 				      myPanel.add(new JLabel("Date"));
 				      myPanel.add(dateField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Reading"));
 				      myPanel.add(readingField);
 
 				      int result = JOptionPane.showOptionDialog(null, myPanel, 
 				               "Enter information for a meter reading", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 				      if (result == 0) {
 				    	 /* STUB - fill in saving code */
 				    	Controller.this.getInstance().meters.add(new Meter(123,"foo"));
 				         System.out.println("saved!");
 				      }
 				}
 			});
 			
 			addAccount.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e){	
 					JTextField firstNameField = new JTextField(10);
 				    JTextField lastNameField = new JTextField(10);
 				    JTextField line1Field = new JTextField(10);
 				    JTextField line2Field = new JTextField(10);
 				    JTextField cityField = new JTextField(10);
 				    JTextField stateField = new JTextField(10);
 				    JTextField zipField = new JTextField(10);
 				    
 				    Object[] options = {"SAVE", "CANCEL"};
 
 				      JPanel myPanel = new JPanel();
 				      myPanel.add(new JLabel("First Name"));
 				      myPanel.add(firstNameField);
 				      myPanel.add(Box.createHorizontalStrut(15)); // a spacer
 				      myPanel.add(new JLabel("Last Name"));
 				      myPanel.add(lastNameField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Address Line 1"));
 				      myPanel.add(line1Field);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Address Line 2"));
 				      myPanel.add(line2Field);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("City"));
 				      myPanel.add(cityField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("State"));
 				      myPanel.add(stateField);
 				      myPanel.add(Box.createHorizontalStrut(15));
 				      myPanel.add(new JLabel("Zip"));
 				      myPanel.add(zipField);
 
 				      int result = JOptionPane.showOptionDialog(null, myPanel, 
 				               "Enter information for the new residential account", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 				      if (result == 0) {
 				    	 /* STUB - fill in saving code */
 				    	 
 					    	 Controller.this.getInstance().accounts.add(new ResidentialAccount(
 					    			 "foo", "eiohg", 123, 1.3, true, new Date(), new Address("l1","l2","city","zip","state")
 					    			 ));
 				         System.out.println("saved!");
 				      }
 				}
 			});
 			
 			input.add(addReading);
 			input.add(addAccount);
 			mainFrame.add(accounts, BorderLayout.WEST);
 			mainFrame.add(input, BorderLayout.EAST);
 			
 			mainFrame.setVisible(true);
 		}
 
 	}
 }
