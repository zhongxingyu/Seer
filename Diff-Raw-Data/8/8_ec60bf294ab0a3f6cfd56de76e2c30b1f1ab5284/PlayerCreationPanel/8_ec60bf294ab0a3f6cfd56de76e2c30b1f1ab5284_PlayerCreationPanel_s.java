 package ui.panel;
 
 import game.PlayerType;
 import game.Session;
 import game.state.GameSetupState;
 import game.state.State;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import ui.SimpleFocusListener;
 import core.ImageLoader;
 import core.IdGenerator;
 import core.StateSelector;
 
 /**
  * This is a JPanel used during MenuState
  * 
  * This panel displays all the buttons and text
  * boxes in order for each player to choose a race,
  * choose color, and set a name.
  * 
  * @author grant
  * @author trevor
  */
 @SuppressWarnings("serial")
 public class PlayerCreationPanel extends JPanel 
 {	
 	private int playerAt;
 	private int numPlayers;
 	private Color currentColor;
 	private PlayerType currentPlayerType;
 	
 	private JTextField nameTextField;
 	
 	private JButton doneButton;
 	
 	private ColorPickerPanel colorPickerPanel;
 	private PlayerTypePanel playerPanel;
 	
 	private Session session;
 	private ArrayList<String> playerIds;
 
 	/**
 	 * This constructor displays all the buttons, text boxes,
 	 * and internal panels in order for the user to interface
 	 * with it and design their player.
 	 */
 	public PlayerCreationPanel() 
 	{   
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		StateSelector stateSelector = StateSelector.getInstance();
 		State state = stateSelector.getState();
 		
 		numPlayers = ((GameSetupState)state).getNumPlayers();
		System.out.println(numPlayers);
 		
 		nameTextField = new JTextField("name", 30);
 		nameTextField.addFocusListener(new SimpleFocusListener("name"));
 		add(nameTextField);
 		
 		playerPanel = new PlayerTypePanel();
 		add(playerPanel);
 
 		colorPickerPanel = new ColorPickerPanel();
 		add(colorPickerPanel);
 		
 		Border emptyBorder = BorderFactory.createEmptyBorder();
 		
 		doneButton = new JButton();
 		doneButton.setBorder(emptyBorder);
 		doneButton.setFocusable(false);
 		doneButton.addActionListener(new DoneListener());
 		doneButton.setIcon(new ImageIcon(imageLoader.load("assets/images/done.png")));
 		add(doneButton);
 
 		resetInput();
 
 		session = ((GameSetupState)state).getSession();
 		
 		playerAt = 1;
 		playerIds = session.getPlayerIds();
 	}
 	
 	/**
 	 * After each user picks their settings for their player,
 	 * this method resets the settings for each data value to 
 	 * defaults.
 	 */
 	public void resetInput()
 	{
 		currentPlayerType = PlayerType.HUMAN;
 		currentColor = colorPickerPanel.getAvailableColor();
 		colorPickerPanel.setColor(currentColor);
 		nameTextField.setText("name");
 	}
 	
 	public void setCurrentColor(Color color)
 	{
 		currentColor = color;
 	}
 	
 	public void setPlayerType(PlayerType type)
 	{
 		currentPlayerType = type;
 	}
 	
 	/**
 	 * This class represents the action listener for 
 	 * the JButton titled "done."
 	 * 
 	 * Clicking on Done should save the user's choices and 
 	 * move on to the next player if one exists.
 	 * 
 	 * @author trevor
 	 *
 	 */
 	private class DoneListener implements ActionListener 
 	{	
 		/**
 		 * The is listener takes the information from the user's 
 		 * button clicking and entering their name and passes 
 		 * it on to the GameSetup State which later on creates
 		 * the important Session Object.
 		 * 
 		 * @param e The actionEvent triggered when user clicks the 'Done' button
 		 */
 		public void actionPerformed(ActionEvent e) 
 		{
 			StateSelector stateSelector = StateSelector.getInstance();
 			GameSetupState state = (GameSetupState)stateSelector.getState();
 
 			String name = nameTextField.getText();
 			if (name.equals("name") || name.isEmpty())
 			{
 				name = IdGenerator.getId();
 				name = name.substring(name.indexOf("-") + 1);
 				name = name.substring(0, 1).toUpperCase() + name.substring(1);
 			}
 			
 			String id = playerIds.get(playerAt - 1);
 			
 			session.setPlayerColor(id, currentColor);
 			session.setPlayerType(id, currentPlayerType);
 			session.setPlayerName(id, name);
 			
 			colorPickerPanel.blockColor(currentColor);
 			
 			resetInput();
 
 			playerAt++;
 			if (playerAt > numPlayers)
 			{
 				state.completeSession();
 			}
 		}
 	}
 }
