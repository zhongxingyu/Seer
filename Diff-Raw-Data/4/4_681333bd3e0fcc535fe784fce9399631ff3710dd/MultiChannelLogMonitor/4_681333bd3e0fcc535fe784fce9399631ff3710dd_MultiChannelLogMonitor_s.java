 package ch.zhaw.multichannel.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
 import java.util.Date;
 
import javax.swing.BoundedRangeModel;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 
 
 /**
  * The MultiChannelLogMonitor class is used to show log-messages include error-messages.
  *
  * @author  Benjamin Buetikofer, Roland Hofer
  * @version 1.0
  */
 
 public class MultiChannelLogMonitor extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JFrame frame;
 	private JLabel label;
 	private JTextArea area;
 	private JScrollPane scrollbar;
 	private static MultiChannelLogMonitor _instance = null;
 
 	
 	
     public static MultiChannelLogMonitor getInstance() {
         if (_instance == null) {
             _instance = new MultiChannelLogMonitor();
         }
         return _instance;
     }
 
     /**
 	 * Constructor MultiChannelLogMonitor
 	 * starts the frame of MultiChannelLogMonitor
 	 * @param 
 	 */
     
 	private MultiChannelLogMonitor() {
 		logMonitor();
 	}
 	/**
 	 * Initialises the contents of the frame
 	 * 
 	 */
 		private void logMonitor() {
 		frame = new JFrame("MultiChannel-LogMonitor");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocation(850, 450);
 		frame.setSize(500, 250);
 		frame.getContentPane().setLayout(new BorderLayout());
 		label = new JLabel("Multi-Channel Log-Einträge");
 		area = new JTextArea();
 		area.setLineWrap(true);
 		area.setWrapStyleWord(true);
 		area.setBackground(Color.WHITE);
 		area.setFont(new Font("SansSerif", Font.PLAIN, 12));
 		area.setEditable(false);
 		scrollbar = new JScrollPane();
 		scrollbar.getViewport().setView(area);
 		scrollbar.getViewport().add(area, null);
 		frame.getContentPane().add(label, BorderLayout.NORTH);
 		frame.getContentPane().add(scrollbar, BorderLayout.CENTER);
 
 		this.frame.setVisible(true);
 	}
 	
 	public void logInformation(String text,int status){
 		this.area.append((new Date())+" "+text+"\n");
 		//TODO Output zu Konsole
 	}
 	
 	public void logException(Exception exception){
 		this.area.append((new Date())+" "+exception.getMessage()+"\n");
 		
 	}
 
 }
