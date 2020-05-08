 package GUI;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class TooterProjectGUI {
 
	private String[] choices = {"Piano", "Drums", "TurnTable"};
 	private JFrame frame = new JFrame();
 	private JPanel panelCont = new JPanel();
 	private JPanel panelChoice = new JPanel();
 	private JPanel panelPiano = new JPanel();
 	CardLayout cl = new CardLayout();
 	
 	public TooterProjectGUI() {
 		panelCont.setLayout(cl);
 		
 		// choice panel
 		JPanel InstrumentChoice = new JPanel();
 		InstrumentChoice.setLayout(new GridLayout(2, 1, 5, 5));
         
 		        // Create the top panel
 		        JPanel topPanel = new JPanel();
 		        			        
 		        	// Add image to top panel
 		        	JLabel label = new JLabel();  
 		        	label.setIcon(new ImageIcon("tooterlogo.png"));// your image here  
 		        	topPanel.add(label);  
 		        	
 		        // Create the bottom panel
 		        JPanel botPanel = new JPanel();
 		 
 		        	
 		        	// Add a Text Field "Choose your weapon."
 		        	JLabel choiceText = new JLabel("Choose Your Weapon ");
 		        	
 		        	// Add drop down
		         	final JComboBox<String> comboBox = new JComboBox<String>();
		         	for(int i = 0; i < choices.length; i++)
		         		comboBox.addItem(choices[i]);
 			        
 			        // Add submit button
 			        JButton submitBut = new JButton("Choose");
 		         	submitBut.addActionListener(new ActionListener(){
 						@Override
 						public void actionPerformed(ActionEvent arg0) {
 							cl.show(panelCont, comboBox.getSelectedItem().toString());
 						}		         		
 		         	});
 			        
 		          	
 		         	botPanel.add(choiceText);
 		         	botPanel.add(comboBox);
 		         	botPanel.add(submitBut);
 
 		        InstrumentChoice.add(topPanel);
 		        InstrumentChoice.add(botPanel);
 		panelChoice.add(InstrumentChoice);
 		
 		// piano panel
 		Piano piano = new Piano(cl, panelCont);
 		panelPiano.add(piano);
 		
 		panelCont.add(panelChoice, "choice");
 		panelCont.add(panelPiano, "Piano");
 		cl.show(panelCont, "choice");
 		
 		frame.add(panelCont);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable(){
 			@Override
 			public void run(){
 				new TooterProjectGUI();
 			}
 	});
 
 	}
 
 }
