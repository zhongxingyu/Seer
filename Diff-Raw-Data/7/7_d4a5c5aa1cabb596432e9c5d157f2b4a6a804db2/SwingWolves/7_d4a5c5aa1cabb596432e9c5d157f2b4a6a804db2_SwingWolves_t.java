 package wolves;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader; 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 public class SwingWolves implements WolvesUI{
 	
 	private int players;
 	private int wolves;
 	private int SizeMemory; // font size of DayTimeDisplay 
 	
 	public SwingWolves(){
 		//I think we should use this space as a convenient alternative for commit messages
 		// If you like, but it is no longer empty, and we now have a README.
 		
 		if(GraphicsEnvironment.isHeadless()) throw new HeadlessException();
 		
 		SizeMemory = 40;
 		
 		boolean startGame = false;
 		while(!startGame){
 			Object[] buttons = {"Instructions", "About", "Lets play this game!"};
 			int UserInput = JOptionPane.showOptionDialog(null,
 						    "Welcome to this Java implementation of Quantum Werewolves",
 						    "Quantum Werewolves",
 						    JOptionPane.YES_NO_CANCEL_OPTION,
 						    JOptionPane.QUESTION_MESSAGE,
 						    null,
 						    buttons,
 						    buttons[2]);
 			switch(UserInput){
 			case JOptionPane.YES_OPTION : displayHelp();
 			break;
 			case JOptionPane.NO_OPTION : displayAbout();
 			break;
 			case JOptionPane.CLOSED_OPTION : System.exit(0);
 			break;
 			case JOptionPane.CANCEL_OPTION : startGame = true;		
 			}
 		}
 	}
 
 	private String getUserInput(String prompt) {		
 		JPanel panel = new JPanel();
 		panel.add(new JLabel(prompt));
 		final JTextField inputText = new JTextField(10);
 		panel.add(inputText);
 		
 		JOptionPane OptPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE);
 		JDialog dialog = OptPane.createDialog("Quantum Werewolves");
 		inputText.requestFocusInWindow();
 		dialog.addWindowFocusListener(new WindowFocusListener() {
 			@Override
 			public void windowGainedFocus(WindowEvent arg0) {
 				inputText.requestFocusInWindow();
 			}
 			@Override
 			public void windowLostFocus(WindowEvent arg0) {}			
 		});
 		dialog.setVisible(true);
 
 		if(OptPane.getValue() == null) System.exit(0);
 		return inputText.getText();
 	}
 	
 	private int getPlayerIDFromUser(String prompt, String[] arrplay) {
 				
 		while(true){		
 			try {
 				String Name = PlayerSelectFrame.choosePlayer(prompt, arrplay);
 				if (Name.equals("NONE")) return 0;
 				return RunFileGame.getPlayerIDFromName(Name);
 			} catch (WrongNameException e) {
 				displayError("FUCK OFF THATS NOT A NAME");
 			}
 		}
 	}
 	
 	private int getIntFromUser(String prompt) {
 		while (true) {
 			try {
 				return Integer.parseInt(getUserInput(prompt));
 			} catch (Exception e) {
 				displayError("FUCK OFF THATS NOT A NUMBER");
 			}
 		}
 	}
 	
 	@Override
 	public int getNumPlayers() {
 		int tempInt = 0;
 		while(true){
 			tempInt = getIntFromUser("PLEASE CHOOSE HOW MANY OF PEOPLE");
 			if(tempInt <= 1) {
 				displayError("Please enter an integer greater than 1.\n (but not TOO large...)");
 			} else {
 				break;
 			}
 		}
 		
 		this.players = tempInt;
 		return players;
 	}
 
 	@Override
 	public int getNumWolves() {
 		int tempInt = 0;
 		while(true){
 			tempInt = getIntFromUser("PLEASE CHOOSE HOW MANY OF WOLVES");
 			if((tempInt <= 0) || (tempInt >= this.players)) {
 				displayError("You must have at least one wolf,\n " +
 						"but fewer than the total number of players");
 			} else {
 				break;
 			}
 		}
 		
 		this.wolves = tempInt;
 		return wolves;
 	}
 
 	@Override
 	public void displayEndGame(int RoundNum, WinCodes WinCode, int[] knownRoles) {
 		String text = "";
 		
 		if(WinCode == WinCodes.NoStatesRemain){
 			text = "No Gamestates remain.";
 		} else {
 			String WinningTeam = null;
 			switch(WinCode){
 			case InnocentsWon : WinningTeam = "Innocents";
 			break;
 			case WolvesWon : WinningTeam = "Wolves";
 			break;
 			case ERROR : WinningTeam = "ERROR";
 			text = ("SOMETHING HAS GONE WRONG");
 			break;
 			}
 			text = ("Game Over. The " + WinningTeam + " have won after " + RoundNum + " rounds.\n");
 		}
 		int n;
 		for (int i = 0; i < players; i++) {
 			n = 1;
 			String role = null;
 			String dead  = "";
 			if (knownRoles[i] < 0) {
 				dead = "DEAD ";
 				n = -1;
 			}
 			switch (n*knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 				break;
 			case ROLE_SEER:
 				role = "SEER";
 				break;
 			}
 			if(n*knownRoles[i] >= 3) role = "WOLF";
 			if (knownRoles[i] != 0) {
 				text += ("PLAYER " + (i+1) + " (" + RunFileGame.getPlayerName(i+1) + ")" + " IS " + getAdjective() + " " + dead + role + "\n");
 			}
 		}
 		
 		displayString(text);
 	}
 
 	private String getAdjective() {
 		Random generator = new Random();		
 		ArrayList<String> importedWords = new ArrayList<String>();
 		try {
			InputStream in = getClass().getResourceAsStream("/AdjectiveList.txt");
			BufferedReader AdjectiveFile = new BufferedReader(new InputStreamReader(in));
			//  BufferedReader AdjectiveFile = new BufferedReader(new FileReader("AdjectiveList.txt")); 
 			String Adjective = AdjectiveFile.readLine();
 			while (Adjective != null){
 				importedWords.add(Adjective);
 				Adjective = AdjectiveFile.readLine();
 			}
 			return importedWords.get(generator.nextInt(importedWords.size()));
 		} catch (FileNotFoundException e) {
 			System.out.println("Adjective file not found.");
 			return "Adjective-y";
 		} catch (IOException e) {
 			System.out.println("Adjective file IO error occurred.");
 			return "Adjective-y";
 		}
 	}
 
 	@Override
 	public int inputLynchTarget() {
 		List<String> p = RunFileGame.getLivePlayers();
 		String[] arrplay = new String[p.size()];
 		for (int i = 0; i < arrplay.length; i++) {
 			arrplay[i] = p.get(i);
 		}
 		return getPlayerIDFromUser("PLEASE CHOOSE WHO IS LUNCHED", arrplay);
 	}
 
 	@Override
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles) {
 		String rolesText = "<html>";
 
 		int n;
 		for (int i = 0; i < players; i++) {
 			n = 1;
 			String role = null;
 			String dead  = "";
 			String name = "";
 			if (knownRoles[i] < 0) {
 				dead = "DEAD ";
 				n = -1;
 				name = " (" + RunFileGame.getPlayerName(i+1) + ")";
 			}
 			switch (n*knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 				break;
 			case ROLE_SEER:
 				role = "SEER";
 				break;
 			case ROLE_WOLF:
 				role = "WOLF";
 				break;
 			}
 			if (knownRoles[i] != 0) {
 				rolesText += ("PLAYER " + (i+1) + name + " IS " + getAdjective() + " " + dead + role + "<br>");
 			}
 		}
 		DaytimeDisplayFrame testFrame = new DaytimeDisplayFrame(probabilities, rolesText + "</html>", SizeMemory);
 		SizeMemory = testFrame.getFinalFontSize();
 	}
 	
 
 	@Override
 	public int inputSeerTarget(int inSeer) {
 		List<String> p = RunFileGame.getLivePlayers();
 		String[] arrplay = new String[p.size() + 1];
 		arrplay[0] = "NONE";
 		for (int i = 1; i < arrplay.length; i++) {
 			arrplay[i] = p.get(i - 1);
 		}
 		return getPlayerIDFromUser("\nPLEASE CHOOSE WHO IS SAW BY PLAYER " + inSeer + " (" + RunFileGame.getPlayerName(inSeer) + ")", arrplay);
 	}
 
 	@Override
 	public void displaySingleVision(int Seer, int Target, byte Vision) {
 		if(Target == 0){
 			// Do Nothing.
 		} else {
 			String role = null;
 			switch (Vision) {
 			case WolvesUI.VISION_INNO:
 				role = "INNOCENT";
 				break;
 			case WolvesUI.VISION_WOLF:
 				role = "WHEREWOLF";
 				break;
 			}
 			displayString("PLAYER " + Seer + " (" + RunFileGame.getPlayerName(Seer) + ") SEES THAT " + RunFileGame.getPlayerName(Target) + " IS " + getAdjective() + " " + role);
 		}
 
 	}
 	
 	@Override
 	public void displayAllStates(List<GameState> AllStates){
 		final String newline = "<br>";
 		String output = "<html>";
 		for(GameState gameState: AllStates){
 			int[] PlayerRoles = gameState.AllPlayers();
 			for(int i = 0; i < PlayerRoles.length; i++){
 				output += "(" + (i+1) + " " + RunFileGame.getPlayerName(i+1);
 				if(PlayerRoles[i] < 0) output += " Dead";
 				switch(PlayerRoles[i]){
 				case 1: output += " Villager";
 				break;
 				case 2: output += " Seer";
 				break;
 				case -1: output += " Villager";
 				break;
 				case -2: output += " Seer";
 				break;
 				}
 				if(PlayerRoles[i] >= 3) output += " " + (PlayerRoles[i] - 2) + "-Wolf";
 				if(PlayerRoles[i] <= -3) output += " " + (-1 * (PlayerRoles[i] + 2)) + "-Wolf";
 				output += ")";
 			}	
 			output += newline;
 		}
 		output += "</html>";
 		
 		JScrollPane ScrPane = new JScrollPane(new JLabel(output));
 		final JDialog dialog = new JDialog(null, "Quantum Werewolves", Dialog.ModalityType.APPLICATION_MODAL);	
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		dialog.setLocationRelativeTo(null);
 		
 		JPanel somePanel = new JPanel();
 		somePanel.setLayout(new BorderLayout());
 		somePanel.add(new JLabel("This is a list of all currently possible gamestates:"),BorderLayout.NORTH);
 		somePanel.add(ScrPane, BorderLayout.CENTER);
 		
 		JButton button = new JButton("Ok");
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dialog.setVisible(false);
 			}				
 		});
 		somePanel.add(button, BorderLayout.SOUTH);
 		dialog.add(somePanel);
 		dialog.pack();
 		dialog.setSize(new Dimension(600,325));
 		somePanel.setSize(new Dimension(600,300));
 		somePanel.setMaximumSize(new Dimension(600,300));
 		ScrPane.setMaximumSize(new Dimension(600,300));
 		ScrPane.setPreferredSize(new Dimension(600,300));
 		somePanel.setPreferredSize(new Dimension(600,300));
 		dialog.setLocationRelativeTo(null);
 		
 		dialog.setVisible(true);
 	}
 
 	@Override
 	public boolean getDebugMode() {
 		
 		Object[] buttons = {"Debug mode", "Play Normally"};
 		int userInput = JOptionPane.showOptionDialog(null,
 					    "Would you like to use debug mode? \n (This will display all game-states at every update.)",
 					    "Quantum Werewolves",
 					    JOptionPane.YES_NO_OPTION,
 					    JOptionPane.QUESTION_MESSAGE,
 					    null,
 					    buttons,
 					    buttons[1]);
 		switch(userInput){
 		case JOptionPane.YES_OPTION : return true;
 		case JOptionPane.NO_OPTION : return false;
 		case JOptionPane.CLOSED_OPTION : System.exit(0);
 		return false;
 		}
 		return false;
 		
 	}
 
 	@Override
 	public int InputSingleWolfTarget(int inPlayer) {
 
 		List<String> LivePlayers = RunFileGame.getLivePlayers();
 		LivePlayers.remove(RunFileGame.getPlayerName(inPlayer));
 		List<Integer> TargetIDs = RunFileGame.getWolfPastTargets(inPlayer);
 		List<String> TargetNames = new ArrayList<String>();
 		for(Integer ID : TargetIDs){
 			TargetNames.add(RunFileGame.getPlayerName(ID));
 		}
 		LivePlayers.removeAll(TargetNames);
 		String[] arrplay = new String[LivePlayers.size()];
 		for (int i = 0; i < arrplay.length; i++) {
 			arrplay[i] = LivePlayers.get(i);
 		}
 		return getPlayerIDFromUser("\nPLEASE CHOOSE WHO IS WOLFED DOWN BY PLAYER " + inPlayer + " (" + RunFileGame.getPlayerName(inPlayer) + ")", arrplay);
 	}
 
 	@Override
 	public String inputName() {
 		return getUserInput("PLEASE ENTER A NAME OF PLAYER");
 	}
 
 	@Override
 	public void displayPlayerIDs(String[] inArray) {
 		String text = "";		
 		text = ("Assigned player names: \n");
 		for(int i = 0; i < players; i++){
 			text += ((i+1) + " - " + inArray[i] +"\n");
 		}		
 		displayString(text);
 	}
 
 	@Override
 	public void displayHistory(List<PlayerAction> AllActions, List<PlayerAction> ReleventActions) {		
 		final String newline = "<br>";
 		String HistoryText = "<html>";
 		for(PlayerAction Action : ReleventActions){
 			HistoryText += Action.print() + newline;
 		}
 		HistoryText += "</html>";
 		
 		String CompleteText = "<html>";
 		for(PlayerAction Action : AllActions){
 			CompleteText += Action.print() + newline;
 		}
 		CompleteText += "</html>";
 		
 		final JDialog dialog = new JDialog(null, "Quantum Werewolves", Dialog.ModalityType.APPLICATION_MODAL);	
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		dialog.setLocationRelativeTo(null);
 		
 		JScrollPane ReleventTab = new JScrollPane(new JLabel(HistoryText));
 		JScrollPane CompleteTab = new JScrollPane(new JLabel(CompleteText));
 		JTabbedPane TabPane = new JTabbedPane();
 		TabPane.addTab("Relevent History", ReleventTab);
 		TabPane.addTab("Complete History", CompleteTab);
 		
 		JPanel somePanel = new JPanel();
 		somePanel.setLayout(new BorderLayout());
 		somePanel.add(new JLabel("These are lists of actions taken during the game, thank you for playing."),BorderLayout.NORTH);
 		somePanel.add(TabPane, BorderLayout.CENTER);
 		
 		JButton button = new JButton("Close Game");
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dialog.setVisible(false);
 			}				
 		});
 		somePanel.add(button, BorderLayout.SOUTH);
 		dialog.add(somePanel);
 		
 		dialog.pack();
 		dialog.setSize(new Dimension(600,300));
 		dialog.setLocationRelativeTo(null);
 		dialog.setVisible(true);
 				
 	}
 	
 	public void displayString(String text){		
 		Object[] buttons = {"Continue", "Quit"};
 		int userInput = JOptionPane.showOptionDialog(null,
 					    text,
 					    "Quantum Werewolves",
 					    JOptionPane.YES_NO_OPTION,
 					    JOptionPane.INFORMATION_MESSAGE,
 					    null,
 					    buttons,
 					    buttons[0]);
 		switch(userInput){
 		case JOptionPane.YES_OPTION : break;
 		case JOptionPane.NO_OPTION : int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quantum Werewolves"
 				, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		switch(check){
 		case JOptionPane.YES_OPTION : System.exit(0);
 		case JOptionPane.NO_OPTION : break;
 		}
 		break;
 		case JOptionPane.CLOSED_OPTION : System.exit(0);
 		}
 	}
 	
 	public void displayError(String message){
 		Object[] buttons = {"Continue", "Quit"};
 		int userInput = JOptionPane.showOptionDialog(null,
 					    message,
 					    "Quantum Werewolves",
 					    JOptionPane.YES_NO_OPTION,
 					    JOptionPane.ERROR_MESSAGE,
 					    null,
 					    buttons,
 					    buttons[0]);
 		switch(userInput){
 		case JOptionPane.YES_OPTION : break;
 		case JOptionPane.NO_OPTION : int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quantum Werewolves"
 				, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		switch(check){
 		case JOptionPane.YES_OPTION : System.exit(0);
 		case JOptionPane.NO_OPTION : break;
 		}
 		break;
 		case JOptionPane.CLOSED_OPTION : System.exit(0);
 		}
 	}
 	
 	private void displayHelp(){
 		final String newline = "\n";
 		
 		String HelpText = "How to play Quantum Werewolves" + newline + newline 
 				+ "There are a minimum of 2 players, although around 7 is advisable," + newline 
 				+ "and one further person is required to operate (and narrate) the game." + newline
 				+ "One of these players is a Seer, and a fraction are wolves." + newline
 				+ "The wolves form a pack, with a leader, second in command, etc." + newline
 				+ "Each night, the leading living wolf makes a kill, and the Seer has a vision." + newline
 				+ "The vision will either be a 'thumbs up' for innocent," + newline
 				+ "or a 'thumbs down' for a wolf." + newline
 				+ "Each day, the players vote and lynch someone, killing them." + newline + newline
 				+ "However, the quantum twist comes in the fact that at the start of the game" + newline
 				+ "everyone has the same chance as anyone else of being any particular character" + newline
 				+ "(wolf/Seer/villager), and it is everyone's actions which determine the" + newline
 				+ "character assigments as play proceeds." + newline + newline
 				+ "Characters are determined according to the following rules:" + newline
 				+ "- A wolf cannot attack another wolf" + newline
 				+ "- Every vision the true Seer had when alive is correct" + newline
 				+ "- When a player becomes 100% dead, their character is chosen based on" + newline
 				+ "       the probabilities at that time." + newline
 				+ "- Players are given choices based only on things they can still do." + newline
 				+ "- A Seer may choose to have a vision of anyone, even themselves, or nobody.";	
 		displayString(HelpText);
 		
 		HelpText = "Gameplay:" + newline + newline 
 				+ "Enter the number of players, wolves, and player names." + newline
 				+ "Then, tell everyone to close their eyes" + newline
 				+ "'Wake' each player in turn by name, and indicate their assigned number" + newline
 				+ "For each player choice prompt, 'wake' the player by number," + newline 
 				+ "       and tell them what choice they can make, but NOT their options." + newline
 				+ "After each player has pointed at their choice, enter it," + newline
 				+ "       and give them an appropriate thumbs up or down for visions," + newline
 				+ "       then tell them to sleep." + newline
 				+ "After the final choice, it is daytime, and all players may wake." + newline
 				+ "They must now use the probability table to vote and lynch by name, not number." + newline
 				+ "After lynching, the probabilities update, and then the night begins again." + newline
 				+ "Any dead players need not close their eyes, but may not contribute to decisions.";
 		displayString(HelpText);
 	}
 	
 	private void displayAbout(){
 		displayString("Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html \n" +
 				"this is a 'quantum' version of the classic werewoves/mafia game of asymmetric information." +
 				"\n\n Also, anything in caps was written by Jamie...");
 	}
 }
