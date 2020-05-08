 package de.tu_darmstadt.gdi1.samegame.view.gameframes;
 
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 @SuppressWarnings("serial")
 public class CustomizeFrame extends JFrame{
 	private String eingabe;
 	private ResourceBundle messages;
 	
 	String cu_width;
 	String cu_height;
 	String cu_numb;
 	String cu_minStones;
 	
 	public CustomizeFrame(Locale locale){
 		super();
 		
 		this.setName("CustomizeFrame");
 		
 		messages = ResourceBundle.getBundle("de.tu_darmstadt.gdi1.samegame.view.gameframes.CustomizeBundle", 
 				 locale, 
 				 this.getClass().getClassLoader()); 
 		
 		
 		JTextField c_width = new JTextField();
 		JTextField c_height = new JTextField();
 		JTextField c_numb = new JTextField();
 		JTextField c_minStones = new JTextField();
 		
         Object[] message = {
                 		messages.getString("Custom_Width"), c_width, 
                 		messages.getString("Custom_Height"), c_height,
                 		messages.getString("Custom_NumberStones"), c_numb,
                 		messages.getString("Custom_MinStones"), c_minStones,
                 			};
  
                 JOptionPane pane = new JOptionPane( message, 
                                                 JOptionPane.PLAIN_MESSAGE, 
                                                 JOptionPane.OK_CANCEL_OPTION);
                 
                 pane.createDialog(null, messages.getString("Custom_Title")).setVisible(true);
  
                 
                 
                 String sTest = "1";
                 
                 int iTest = Integer.valueOf( sTest ).intValue();
                 
                cu_width = c_width.getText();
                cu_height = c_height.getText();
                cu_numb = c_numb.getText();
                cu_minStones = c_minStones.getText();
 	}
 
 	public int getCustomWidth(){
 		
 		return Integer.valueOf(cu_width).intValue();
 	}
 	public int getCustomHeight(){
 		return Integer.valueOf(cu_height).intValue();
 	}
 	public int getCustomStoneNumber(){
 		return Integer.valueOf(cu_numb).intValue();
 	}
 	public int getCustomMinStones(){
 		return Integer.valueOf(cu_minStones).intValue();
 	}
 }
