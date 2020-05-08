 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.HashMap;
 import java.util.TreeMap;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 /**
  * A frame that shows the player the high scores that have been made.  The high scores can also be reset via this frame.  Reads the list of high scores out of a text file named HighScores.txt.  Only the top 5 scores for each game mode are stored. Scores are stored in an xml file.
  */
 public class HighScoresFrame extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 1L;
 	private JLabel scoresLabel;
 	private JFrame resetDialougeFrame;
 	private HashMap<String, TreeMap<Integer, String>> scoresList;
 	private Gui currentGui;
 	
 	/**
 	 * For restarting the game on frame close.
 	 */
 	private boolean restartGame;
 
 	/**
 	 * Constructs HighScoresFrame.
 	 *
 	 * @param currentGui The current type of Gui the player is using.  For hiding the Gui when the AboutFrame is clicked.
 	 */
 	public HighScoresFrame(Gui currentGui)
 	{
 		super("High Scores");
 		this.currentGui = currentGui;
 		scoresLabel = new JLabel("High scores for " + currentGui.getGameType() + " mode.");
 
 		JButton okButton = new JButton("Ok");
 			okButton.setActionCommand("ok");
 			okButton.addActionListener(this);
 		JButton resetButton = new JButton("Reset");
 			resetButton.setActionCommand("resetDialouge");
 			resetButton.addActionListener(this);
 
 		this.readScores();
 		this.add(scoresLabel, BorderLayout.NORTH);
 		this.add(okButton, BorderLayout.WEST);
 		this.add(resetButton, BorderLayout.EAST);
 		this.setSize(200, 200);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.setLocationRelativeTo(currentGui);
 	}
 
 	/**
 	 * The action performed when a button is pressed.
 	 *
 	 * @param event The event triggered by the close button.
 	 */
 	public void actionPerformed(ActionEvent event)
 	{
 		String command = event.getActionCommand();
 		if(command.equals("ok"))
 		{
 			this.setVisible(false, restartGame);
 		}
 		else if(command.equals("resetDialouge"))
 		{
 			String message = "<html><pre>Are you sure you want to<br> reset the high scores<br>  for " + currentGui.getGameType().toLowerCase() + " mode?</pre></html>";
 			JOptionPane.showConfirmDialog(this, message, "Reset?", JOptionPane.YES_NO_OPTION);
 		}
 		else if(command.equals("resetYes"))
 		{
 			resetScores();
 			resetDialougeFrame.setVisible(false);
 			this.pack();
 			this.setVisible(true);
 		}
 		else if(command.equals("resetNo"))
 		{
 			resetDialougeFrame.setVisible(false);
 			this.setVisible(true);
 		}
 	}
 
 	/**
 	 * Updates the scoresLabelText.  Should only be called when the current high scores change.
 	 */
 	private void updateScoresLabel()
 	{
 		TreeMap<Integer, String> currentList = this.scoresList.get(currentGui.getGameType());
 		if(currentList != null)
 		{
 			String scoresListText = "<html>";
 			Stack<String> stack= new Stack<String>();
 			for(Integer key : currentList.keySet())
 			{
 				stack.push(currentList.get(key) + '\t' + key + "<br>");
 			}
 			while(!stack.empty())
 			{
 				scoresListText += stack.pop();
 			}
 			scoresListText += "</html>";
 			scoresLabel.setText(scoresListText);
 		}
 	}
 
 	/**
 	 * Called when constructed.
 	 */
 	private void readScores()
 	{
 		scoresList = new HashMap<String, TreeMap<Integer, String>>();
 		String type;
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader("HighScores.txt"));
 			while(reader.ready())
 			{
 				do
 				{
 					type = reader.readLine();
 				} while(reader.ready() && type.length() < 1);
 				for(int i = 0; i < 5 && reader.ready(); i++)
 				{
 					this.addScore(type, reader.readLine());
 				}
 			}
 			reader.close();
 		}
 		catch(FileNotFoundException ex)
 		{
 			new File("HighScores.txt");
 			resetScores();
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * called when the user confirms to reset the scores.
 	 */
 	private void resetScores()
 	{
 		TreeMap<Integer, String> tempMap = new TreeMap<Integer, String>();
 		for(int i = 5; i > 0; i--)
 		{
 			tempMap.put((Integer)(i * 5), "Jacob Patterson");
 		}
 		this.scoresList.put(currentGui.getGameType(), tempMap);
 		this.saveScores();
 		this.updateScoresLabel();
 	}
 
 	/**
 	 * Adds the specified score to the list of high scores. Removes all high scores lower than the top 4 (To make 5 total).
 	 *
 	 * @param gameModeType The Game-mode to add the high score to.
 	 * @param score The score to add to the list of high scores.
 	 */
 	public void addScore(String gameModeType, String score)
 	{
 		TreeMap<Integer, String> map = this.scoresList.get(gameModeType);
 		if(map == null)
 		{
 			map = new TreeMap<Integer, String>();
 		}
 		String[] scoreArray = score.split(" ");
 		Integer points = new Integer(scoreArray[scoreArray.length - 1]);
 		String	name = this.getName(scoreArray).trim();
 			name = (name.equals("") ? "Anonymous" : name);
 		while(map.size() >= 5)
 		{
 			map.remove(map.firstKey());
 		}
 		map.put(points, name);
 		this.scoresList.put(gameModeType, map);
 	}
 
 	/**
 	 * For formatting the name from the file as to include spaces and trailing numbers (Example: fat bob 123)
 	 * 
 	 * @param separated
 	 * @return
 	 */
 	private String getName(String[] separated)
 	{
 		String name = "";
 		int pos = 0;
 		while(pos != separated.length - 1)
 		{
 			name += separated[pos++] + ' ';
 		}
 		return name;
 	}
 
 	/**
 	 * Saves the high scores.
 	 */
 	public void saveScores()
 	{
 		try
 		{
 			TreeMap<Integer, String> gameTypeScores;
 			File file = new File("HighScores.txt");
 			file.delete();
 			BufferedWriter writer = new BufferedWriter(new FileWriter("HighScores.txt"));
 			for(String gameMode : this.scoresList.keySet())
 			{
 				writer.write(gameMode);
 				writer.newLine();
 				gameTypeScores = this.scoresList.get(gameMode);
 				for(Integer key : gameTypeScores.keySet())
 				{
 					writer.write(gameTypeScores.get(key) + " " + key);
 					writer.newLine();
 				}
 			}
 			writer.close();
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sees if the given score is high enough to be a high score.
 	 *
 	 * @param score The score to see if is a high score.
 	 * @return True if is a high score, otherwise, false.
 	 */
 	public boolean isHighScore(int score)
 	{
 		TreeMap<Integer, String> map = this.scoresList.get(currentGui.getGameType());
 		if(map == null)
 		{
 			this.resetScores();
 			map = this.scoresList.get(currentGui.getGameType());
 		}
 		if(score > map.firstKey() || map.size() < 5)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Shows or hides this component depending on the value of parameter visible.
 	 *
 	 * @param visible If true, shows this component; otherwise, hides this component
 	 * @param restartGame Tells whether or not to restart the game when the HighScoresPrame is closed (if visible is True).
 	 */
 	public void setVisible(boolean visible, boolean restartGame)
 	{
 		if(!visible)
 		{
 			if(restartGame)
 			{
 				final int choice = JOptionPane.showConfirmDialog(this, "Do you want to restart the game?", "Restart?", JOptionPane.YES_NO_OPTION);
 				if(choice == JOptionPane.YES_OPTION)
 				{
 					currentGui.getGame().resetBoard();
 				}
 				else if(choice == JOptionPane.NO_OPTION)
 				{
 					System.exit(0);
 				}
 			}
			else
			{
				currentGui.setVisible(true);
				super.setVisible(false);
			}
 		}
 		else
 		{
 			this.restartGame = restartGame;
 			this.updateScoresLabel();
 			this.pack();
 		}
 		currentGui.setEnabled(!visible);
 		super.setVisible(visible);
 	}
 }	
