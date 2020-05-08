 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 
 public class GUINumbersMenu implements ActionListener {
     
 	private JButton myButton;
 	JPopupMenu  menu;
     private GUIMenuListener listen;
     
 	
     public GUINumbersMenu(JButton clicker, Space s, int noOfItems, ActionListener listn) {
 		myButton = clicker;
 		
 		// create a popup menu
 		menu = new JPopupMenu("");
         
         listen = new GUIMenuListener(s);
         
         JMenuItem blank = new JMenuItem("");
         menu.add(blank);
         blank.addActionListener(listen);
         
         for (int i = 0; i < 9; i++){
             JMenuItem item = new JMenuItem(""+(i+1));
             menu.add(item);
             item.addActionListener(listen);
             item.addActionListener(listn);
         }
 
         clicker.addActionListener(this);
         
         
     }
     
     
 	public void actionPerformed(ActionEvent e) {
 		
 		menu.show(myButton,myButton.getWidth()/2,myButton.getHeight()/2);
 	}
     
     
 }
