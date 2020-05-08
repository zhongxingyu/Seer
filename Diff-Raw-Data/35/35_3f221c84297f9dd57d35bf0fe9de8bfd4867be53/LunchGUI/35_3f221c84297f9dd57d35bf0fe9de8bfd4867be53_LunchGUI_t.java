 package chapter2;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 public class LunchGUI implements Runnable, ActionListener {
 	
 	public static final double WITAX = .055;
 	public static final int DAYS  = 5;
 	
 	private static String[] dayNames = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
 	
 	private JFrame frame;
	@SuppressWarnings("unused")
 	private JTextField[] dayTextFields;
 	private JLabel subTotalResultLabel;
 	private JLabel taxResultLabel;
 	private JLabel totalResultLabel;
 	
 	public static void main(String[] args) {
 		LunchGUI lg = new LunchGUI();
 		SwingUtilities.invokeLater(lg);
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void run() {
 		this.frame = new JFrame("What's for Lunch?");
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setLayout(new BoxLayout(this.frame.getContentPane(),BoxLayout.Y_AXIS));
 		
 		JPanel daysPanel = new JPanel(new GridLayout(5,2));
 		JTextField[] newTextFields;
 		newTextFields = new JTextField[5];
 		for(int i = 0; i < 5; i++) {
 			JTextField newTextField = new JTextField("0.00");
 			newTextField.setColumns(6);
 			newTextField.addActionListener(this);
 			newTextFields[i] = newTextField;
 			JLabel newDayLabel = new JLabel(dayNames[i]);
 			JPanel dayPanel = new JPanel(new FlowLayout());
 			dayPanel.add(new JLabel("$"));
 			dayPanel.add(newTextField);
 			daysPanel.add(newDayLabel);
 			daysPanel.add(dayPanel);
 		}
 		this.dayTextFields = newTextFields;
 		daysPanel.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createLineBorder(Color.BLACK),
 				BorderFactory.createLineBorder(this.frame.getContentPane().getBackground(), 5)));
 		this.frame.getContentPane().add(daysPanel);
 		
 		JPanel resultsPanel = new JPanel(new GridLayout(3,2));
 		resultsPanel.add(new JLabel("Sub-Total:"));
 		this.subTotalResultLabel = new JLabel("$0.00");
 		resultsPanel.add(this.subTotalResultLabel);
 		resultsPanel.add(new JLabel("Tax:"));
 		this.taxResultLabel = new JLabel("$0.00");
 		resultsPanel.add(this.taxResultLabel);
 		resultsPanel.add(new JLabel("Total:"));
 		this.totalResultLabel = new JLabel("$0.00");
 		resultsPanel.add(this.totalResultLabel);
 		resultsPanel.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createLineBorder(Color.BLACK),
 				BorderFactory.createLineBorder(this.frame.getContentPane().getBackground(), 5)));
 		this.frame.getContentPane().add(resultsPanel);
 		
 		this.frame.pack();
         this.frame.setVisible(true);
 		
 	}
 
 }
