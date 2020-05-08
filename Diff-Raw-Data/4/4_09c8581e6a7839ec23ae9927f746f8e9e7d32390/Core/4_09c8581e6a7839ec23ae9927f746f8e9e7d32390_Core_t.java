 package program;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.util.Map;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import userinterface.DashboardPanel;
 import userinterface.MainInterfaceWindow;
 
 
 public class Core extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private static JFrame frame;
 	private static ResourceManager rm;
 //-----------------------------------------------------------------------------	 
 	/**
 	 * Main method to start the thread that will run the main program
 	 */
 	public static void main(String[] args) {
 	    //Schedule a job for the event dispatch thread: creating and showing the GUI
 	    SwingUtilities.invokeLater(new Runnable() {
 	        public void run() {
 	        	
 	        	//Use the native systems look and feel
 	        	try{
 	        		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());            		
 	        	} catch(Exception e){
 	        		e.printStackTrace();
 	        	}
 	        	
 	        	//Set up and show the login GUI
 	        	//COMMENT NEXT LINE OUT TO GET RID OF THE LOGIN GUI FOR DEBUGGING PURPOSES
 	        	//MUST ALSO COMMENT OUT 6 LINES IN MAININTERFACEWINDOW AS INDICATED THERE
 	        	//createAndShowLoginGUI();
 	        
 	        	rm = new ResourceManager(frame);
 	        	//Set up the UI
 	        	createAndShowMainGUI();
 
 	        }
 	    });
 	}
 //-----------------------------------------------------------------------------	
 	/**
 	 * Create the GUI and show it.  For thread safety, this method should be
 	 * invoked from the event dispatch thread.
 	 */
 	private static void createAndShowMainGUI() {
 	    //Create and set up the main window	
 		frame = new JFrame("UMPD");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setPreferredSize(new Dimension(1275,1000));
 		frame.setResizable(true);
		
		//Uses platform specific method for opening frame
		frame.setLocationByPlatform(true);
 		
 		//Add the dashboard area to the window   
 		frame.add(new DashboardPanel(), BorderLayout.PAGE_START);
 
 		//Add the main panel to the window
 		frame.add(new MainInterfaceWindow(frame, rm), BorderLayout.CENTER);
 		
 		//Display the window
 	    frame.pack();
 	    frame.setVisible(true);  
 	}
 //-----------------------------------------------------------------------------	
 	/**
 	 * Create the GUI and show it.  For thread safety, this method should be
 	 * invoked from the event dispatch thread.
 	 
 	private static void createAndShowLoginGUI() {
 	    //Create and set up the frame to place the login window in
 		JFrame loginFrame = new JFrame("UMPD Login");
 		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		loginFrame.setPreferredSize(SwingHelper.LOGIN_DIALOG_DIMENSION);
 		loginFrame.setResizable(true);
 
 		//Display the login dialog
 		LoginDialog loginDialog = new LoginDialog(loginFrame);
 		loginDialog.setVisible(true);
 		
 		//If login attempt(s) was/were not successful, exit the program
 		if(!loginDialog.isSuccessful()){
 			System.exit(0);
 		}
 	}
 	*/
 //-----------------------------------------------------------------------------	
 }
