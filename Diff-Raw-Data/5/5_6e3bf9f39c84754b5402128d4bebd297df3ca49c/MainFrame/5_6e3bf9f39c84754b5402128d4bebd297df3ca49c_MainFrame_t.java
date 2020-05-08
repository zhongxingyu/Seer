 package cha.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.BorderLayout;
 import cha.controller.ChallengeAccepted;
 import cha.controller.Event;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame{
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 			ChallengeAccepted.getInstance();
 			new MainFrame();
 			ChallengeAccepted.getInstance().publish(Event.ShowBet, 
 					ChallengeAccepted.getInstance().getBoard().getActivePiece());
 	}
 	
 	private TileContainerPanel tileContainerPanel;
 	private TextPanel textPanel;
	private ButtonPanel buttonPanel;
 	
 	public MainFrame() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		tileContainerPanel = new TileContainerPanel();
 		textPanel = new TextPanel();
		buttonPanel = new ButtonPanel();
 		
 		textPanel.add(buttonPanel, BorderLayout.SOUTH);
 		tileContainerPanel.add(textPanel, BorderLayout.CENTER);
 		this.add(tileContainerPanel, BorderLayout.CENTER);
 		
 		this.setTitle("Challange Accepted");
 		this.setResizable(false);
 		this.setBounds(100, 100, 710, 530);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
 		this.setVisible(true);
 	}
 }
