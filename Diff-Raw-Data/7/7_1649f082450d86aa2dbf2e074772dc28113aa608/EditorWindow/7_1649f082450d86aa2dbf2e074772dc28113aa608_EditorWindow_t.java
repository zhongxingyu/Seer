 package gui;
 
 import helpers.DateHelper;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import model.Airport;
 import model.Connection;
 
 @SuppressWarnings("serial")
 public class EditorWindow extends JFrame implements View, MouseListener {
 
 	Vector<Airport> locations;
 	
 	// Beans
 	JPanel connectionsContainer;
 	JList<Airport> locationJList;
 	
 	public EditorWindow(Vector<Airport> locations) {
 		this.locations = locations;
 	}
 	
 	public void draw() {
 		super.getContentPane().removeAll(); // making this function being able to repaint the mainwindow
 		super.setTitle("Bearbeiten");
 		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		super.setResizable(false);
 		
 		super.setLayout(new GridLayout(1, 2, 10, 0));
 		
 		((JComponent)getContentPane()).setBorder(BorderFactory.createMatteBorder( 4, 4, 4, 4, Color.LIGHT_GRAY ) );
 		
 		// Build the UI Elems
 		this.locationJList = new JList<Airport>(this.locations);
 		this.connectionsContainer = new JPanel();
 		
 		// Add ActionListening
 		this.locationJList.addMouseListener(this);
 		
 		// Add elems to frame
 		super.getContentPane().add(this.locationJList);
 		super.getContentPane().add(this.connectionsContainer);
 		
 		// Do the rest for displaying the window
 		super.pack();
 		super.setLocationRelativeTo(null); // center the frame
 		
 		// Show the window
 		super.setVisible(true);
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		// Determine which element was clicked
 		int elem = this.locations.indexOf(this.locationJList.getSelectedValue());
		Airport ap = this.locations.get(elem); // the object of type Airport that has been chosen from the list
 
 		// Render Form
 		this.connectionsContainer.removeAll();
 		this.connectionsContainer.setLayout(new GridLayout(ap.getConnections().size(), 2));
 		
 		for(Map.Entry<Airport, Connection> entry : ap.getConnections().entrySet()){
 			//System.out.print(entry.getKey());
 			//System.out.print(" -> ");
 			//System.out.println(entry.getValue().getName());
 			this.connectionsContainer.add(new JLabel(entry.getKey().getName())); // THIS WILL BECOME A SELECTBOX LATER!!!!
 			this.connectionsContainer.add(new JTextField(DateHelper.INSTANCE.durationToString(entry.getValue().getDuration())));
 		}
 		this.pack();
 		this.repaint();
 	}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 
 }
