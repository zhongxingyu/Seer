 package taberystwyth.view;
 
 import java.awt.GridLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.*;
 
 import taberystwyth.controller.LocationInsertionFrameListener;
import taberystwyth.db.SQLConnection;
 
 public class LocationInsertionFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
	SQLConnection conn = SQLConnection.getInstance();
 
 	/*
 	 * Textfields
 	 */
 	private JTextField locationName = new JTextField();
 	private JLabel locationNameLabel = new JLabel("Name:");
 	private JTextField rating = new JTextField();
 	private JLabel ratingLabel = new JLabel("Rating:");
 
 	/*
 	 * Buttons
 	 */
 	private JButton clear = new JButton("Clear");
 	private JButton save = new JButton("Save");
 
 	public synchronized JTextField getLocationName() {
 		return locationName;
 	}
 
 	public synchronized JTextField getRating() {
 		return rating;
 	}
 
 	private static LocationInsertionFrame instance = new LocationInsertionFrame();
 
 	public static LocationInsertionFrame getInstance() {
 		return instance;
 	}
 
	private LocationInsertionFrame() {
 		setLayout(new GridLayout(3, 2));
 		setTitle("Insert Location");
 
 		/*
 		 * When the window is closed, make it invisible
 		 */
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent evt) {
 				LocationInsertionFrame.getInstance().setVisible(false);
 			}
 		});
 
 		add(locationNameLabel);
 		add(locationName);
 		add(ratingLabel);
 		add(rating);
 
 		add(clear);
 		add(save);
 
		clear.addActionListener(new LocationInsertionFrameListener());
		save.addActionListener(new LocationInsertionFrameListener());
 
 		pack();
 	}
 
 }
