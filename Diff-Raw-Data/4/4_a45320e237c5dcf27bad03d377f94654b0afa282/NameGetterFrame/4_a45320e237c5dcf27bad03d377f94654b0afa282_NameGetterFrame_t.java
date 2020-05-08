 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JFrame;
 import javax.swing.JTextField;

/**
 * Gets the player's name for a new high score.
 */
 public class NameGetterFrame extends JFrame implements ActionListener
 {
 	private final int score;
 	private HighScoresFrame highScoresFrame;
 	private JTextField inputField;
 	private Gui currentGUI;
 
 	public NameGetterFrame(HighScoresFrame highScoresFrame, final int score, Gui currentGUI)
 	{
 		super("Congragulations!");
 		this.highScoresFrame = highScoresFrame;
 		this.score = score;
 		this.currentGUI = currentGUI;
 		JLabel highScoreLabel = new JLabel("You got a new high score of " + score + "!");
 		this.inputField = new JTextField("Anonymous");
 		JButton okButton = new JButton("Ok");
 			okButton.setActionCommand("ok");
 			okButton.addActionListener(this);
 
 		this.add(highScoreLabel, BorderLayout.NORTH);
 		this.add(inputField, BorderLayout.CENTER);
 		this.add(okButton, BorderLayout.SOUTH);
 		this.pack();
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.setVisible(true);
 	}
 
 	/**
 	 * The action performed when a button is pressed.
 	 *
 	 * @param event The event triggered by the close button.
 	 */
 	public void actionPerformed(ActionEvent event)
 	{
 		if(event.getActionCommand().equals("ok"))
 		{
 			this.highScoresFrame.addScore(this.currentGUI.getGameType(), this.inputField.getText() + ' ' + score);
 			this.highScoresFrame.saveScores();
 			this.highScoresFrame.setVisible(true, true);
 		}
 		this.dispose();
 	}
 	
 	/**
 	 * Shows or hides this component depending on the value of parameter visible. Hides the GUI on startup.
 	 *
 	 * @param visible If true, shows this component; otherwise, hides this component
 	 */
 	public void setVisible(boolean visible)
 	{
 		if(visible)
 		{
 			this.currentGUI.setVisible(!visible);
 		}
 		super.setVisible(visible);
 	}
 }
