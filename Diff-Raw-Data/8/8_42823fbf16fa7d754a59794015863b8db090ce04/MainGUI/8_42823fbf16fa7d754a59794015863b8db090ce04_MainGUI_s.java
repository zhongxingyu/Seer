 /**
  * @author: Justin Peterson
  * @email: Jmp3833@rit.edu
  * MainGUI.java is the main window of the HTML Editor's user interface. 
  * This window will interact with two components, the menu bars, and the 
  * text tab window. 
  */
 
 
 package Views;
 import java.awt.*;
 
 import Views.MenuBar;
 import Views.TextWindow;
 
 import javax.swing.*;
 
 public class MainGUI extends JFrame {
 	private static final long serialVersionUID = 1L;
 	
 	//launches the main user interface window of the system.
 	public MainGUI() {
 		
 		
 		//Separates the layout into components representing cardinal directions. 
 		getContentPane().setLayout(new BorderLayout(5,10));
 		setSize(600,800);
 		setTitle("Simple HTML Editor");
 		
 		//Create new components
 		TextTabWindow mainWindow = new TextTabWindow();
 		
 		//Adds components to the layout
 		add(new MenuBar(mainWindow).barCreator(this),BorderLayout.NORTH);
 		add(mainWindow,BorderLayout.CENTER);
 		
 	    //set constraints of the main window. 
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setVisible(true);
 			
 		
 	}
 	
 
 }
