 package simulator; 
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.NavigationFilter;
 import javax.swing.text.Position;
 
 import java.util.Scanner;
 import java.util.List;
 
 public class TextUI extends JFrame implements KeyListener 
 {
 	private static final long serialVersionUID = -8860972241763753423L;
 	
 	private final static String START_TEXT = 
 			"To start a name game type\t\t: newgame\n"
             +"To load a saved game type\t\t: loadgame\n"
             +"To view highscore type\t\t\t: highscores\n"
             +"To view credits type\t\t\t: credits\n";
 	// This looks weird because the backslashes need escaping!
 	private final static String REACTOR_ASCII =  
     " _______  _______  _______  _______ _________ _______  _______\n"
     +"(  ____ )(  ____ \\(  ___  )(  ____ \\\\__   __/(  ___  )(  ____ )\n"
     +"| (    )|| (    \\/| (   ) || (    \\/   ) (   | (   ) || (    )|\n"
     +"| (____)|| (__    | (___) || |         | |   | |   | || (____)|\n"
     +"|     __)|  __)   |  ___  || |         | |   | |   | ||     __)\n"
     +"| (\\ (   | (      | (   ) || |         | |   | |   | || (\\ (\n" 
     +"| ) \\ \\__| (____/\\| )   ( || (____/\\   | |   | (___) || ) \\ \\__\n"
     +"|/   \\__/(_______/|/     \\|(_______/   )_(   (_______)|/   \\__/\n";
 	
 	// UI variables
 	private JTextArea systemText = new JTextArea(10,20);
     private JTextArea outputText = new JTextArea(10,20);
     private JTextField inputBox = new JTextField(30);
     private final static Font default_font = new Font("Monospaced",Font.PLAIN, 12);
     private final static String prompt = "> ";
     
     private PlantPresenter presenter;
     private State state = State.Uninitialised;
     private AreYouSureCaller caller = AreYouSureCaller.NoAction;
     
     public TextUI(PlantPresenter presenter)
     {
     	super("REACTOR");
     	this.presenter = presenter;
         initWindow();
         startUp();     
     }
 
     private void initWindow() {
     	setSize(1000,600);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JPanel leftPanel = new JPanel();           
         GridBagLayout leftPanelGridBagLayout = new GridBagLayout();
         leftPanel.setLayout(leftPanelGridBagLayout);
         initInputArea(leftPanel);
         initOutputArea(leftPanel);
         
         JPanel rightPanel = new JPanel();
         initSystemArea(rightPanel);
         GridLayout rightPanelGridLayout = new GridLayout(1,1);
         rightPanel.setLayout(rightPanelGridLayout);
         
         GridLayout grid = new GridLayout(1,2, 0, 0);
         setLayout(grid);
         add(leftPanel);
         add(rightPanel);
         setVisible(true);
     }
     
     private void initInputArea(JPanel parentPanel) {
     	GridBagLayout layout = (GridBagLayout) parentPanel.getLayout();        
         inputBox.setBackground(Color.BLACK);
         inputBox.setForeground(Color.WHITE);
         inputBox.setFont(default_font);
         inputBox.setCaretColor(inputBox.getForeground());
         
         GridBagConstraints inputBoxConstraints = new GridBagConstraints();
         inputBoxConstraints.gridx = 0;
         inputBoxConstraints.gridy = 2;
         inputBoxConstraints.gridwidth = 1;
         inputBoxConstraints.gridheight = 1;
         inputBoxConstraints.weightx = 10;
         inputBoxConstraints.weighty = 1;
         inputBoxConstraints.fill = GridBagConstraints.BOTH;
         inputBoxConstraints.anchor = GridBagConstraints.SOUTH;
         layout.setConstraints(inputBox, inputBoxConstraints);
         
         // Anti Prompt deletion navigation filter!
         NavigationFilter filter = new NavigationFilter() {
             public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
             	if (dot <= prompt.length()) {
             		fb.setDot(prompt.length(), bias);
             	} else {
             		fb.setDot(dot, bias);
             	}
             }
 
             public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
            	if (dot <= prompt.length()) {
            		fb.setDot(prompt.length(), bias);
            	} else {
            		fb.setDot(dot, bias);
            	}
             }
         };
         inputBox.setNavigationFilter(filter);
         
         
         inputBox.addKeyListener(this);
         parentPanel.add(inputBox);
     }
     
     private void initOutputArea(JPanel parentPanel) {
     	GridBagLayout layout = (GridBagLayout) parentPanel.getLayout();
     	
     	//outputText.setBorder(BorderFactory.createMatteBorder(0,0,2,1,Color.GRAY));
         outputText.setBackground(Color.BLACK);
         outputText.setForeground(Color.WHITE); 
         outputText.setFont(default_font);
         outputText.setLineWrap(true);  
         outputText.setWrapStyleWord(true);
         outputText.setEditable(false);
         
         JScrollPane Scroll = new JScrollPane(outputText);
         Scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         
         // Auto scroll down!
         DefaultCaret caret = (DefaultCaret)outputText.getCaret();
         caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
          
         GridBagConstraints OutputTextConstraints = new GridBagConstraints();
         OutputTextConstraints.gridx = 0;
         OutputTextConstraints.gridy = 0;
         OutputTextConstraints.gridwidth = 1;
         OutputTextConstraints.gridheight = 1;
         OutputTextConstraints.weightx = 10;
         OutputTextConstraints.weighty = 100;
         OutputTextConstraints.fill = GridBagConstraints.BOTH;
         OutputTextConstraints.anchor = GridBagConstraints.CENTER;
         layout.setConstraints(Scroll, OutputTextConstraints);
         parentPanel.add(Scroll);
     }
     
     private void initSystemArea(JPanel parentPanel) {
 
         systemText.setBackground(Color.BLACK);
         systemText.setForeground(Color.WHITE);
         systemText.setFont(default_font);
         systemText.setEditable(false);
         parentPanel.add(systemText);
     }
     
     private void startUp()
     {
         outputText.setText(START_TEXT + REACTOR_ASCII);
         inputBox.setText(prompt);
     }
     
     public void keyReleased(KeyEvent k)
     {
     	int keyCode = k.getKeyCode();
     	switch (keyCode) {
     		case KeyEvent.VK_BACK_SPACE:
     			persistPrompt();
     			break;
     	}
     }
     
     public void keyTyped(KeyEvent k)
     { 
     }
     
     public void keyPressed(KeyEvent k) 
     {    	
         int keyCode = k.getKeyCode();
         switch (keyCode) {
 			case KeyEvent.VK_ENTER:
 				actUponInput();
 				break;
 			case KeyEvent.VK_BACK_SPACE:
     			persistPrompt();
     			break;
         }
     }
     
     private void persistPrompt()
 	{
     	if (inputBox.getCaret().getDot() <= prompt.length()) inputBox.getCaret().setDot(prompt.length());
 		if (!inputBox.getText().startsWith(prompt)) {
 			inputBox.setText(prompt + inputBox.getText().substring(1));
 		}
 		
 	}
 
 	private void actUponInput() {
     	String command = inputBox.getText().substring(prompt.length());
     	print(prompt + command);
     	parse(command);
 		inputBox.setText(prompt);
 		
 		if(state == State.Normal)
 			updateSystemText();
     }
     
     private void print(String output) {
     	outputText.append(output + "\n");
     }
     
     private void updateSystemText() {
     	String reactorInfo = new String();
     	reactorInfo += "Operator Name: " + presenter.getOperatorName() + "\t| SCORE: " + presenter.getScore() + "\n\n";
     	reactorInfo += "PLANT READINGS: \n\n";
     	
     	reactorInfo += "REACTOR HEALTH: " + presenter.getReactorHealth() + "\n";  
     	reactorInfo += "Temperature: "    + presenter.getReactorTemperature() + "  \t| Max: "                + presenter.getReactorMaxTemperature()     + "\n";
     	reactorInfo += "Pressure: "       + presenter.getReactorPressure()    + " \t\t| Max: "               + presenter.getReactorMaxPressure()        + "\n";
     	reactorInfo += "Water Volume: "   + presenter.getReactorWaterVolume() + " \t| Minimum Safe Volume: " + presenter.getReactorMinSafeWaterVolume() + "\n\n";
     	
     	reactorInfo += "CONDENSER HEALTH: " + presenter.getCondenserHealth()      + "\n"; //Improve
     	reactorInfo += "Temperature: "      + presenter.getCondenserTemperature() + "  \t| Max: "                + presenter.getReactorMaxTemperature()     + "\n";
     	reactorInfo += "Pressure: "         + presenter.getCondenserPressure()    + " \t\t| Max: "               + presenter.getReactorMaxPressure()        + "\n";
     	reactorInfo += "Water Volume: "     + presenter.getCondenserWaterVolume() + " \t| Minimum Safe Volume: " + presenter.getReactorMinSafeWaterVolume() + "\n\n";
     	
     	List<Valve> valves = presenter.getValves();
     	for (Valve v : valves) {
     		reactorInfo += "VALVE ID: " + v.getID() + " | ";
     		reactorInfo += "POSITION: " + (v.isOpen() ? "OPEN\n" : "CLOSED\n");
     	}
     	reactorInfo += "\n";
     	
     	List<Pump> pump = presenter.getPumps();
     	for (Pump p : pump) {
     		reactorInfo += "PUMP ID: " + p.getID() + "  | ";
     		reactorInfo += "STATUS: " + ((p.isOperational()) ? "FUNCTIONAL | " : "BROKEN | ");
     		reactorInfo += "POWER STATE: " + (p.isOn() ? "ON\n" : "OFF\n");
     	}
     	reactorInfo += "\n";
     	
     	reactorInfo += "CONTROL RODS PERCENT INTO CORE: " + presenter.getControlRodsPercentage() + "%\n";
     	
     	systemText.setText(reactorInfo);
     }
     
     //--------------- Parsing ----------------
     
 	private void parse(String input) {
 		if (state == State.Normal)
 			parseNormal(input.toLowerCase());
 		else if (state == State.Uninitialised)
 			parseUninitialised(input.toLowerCase());
 		else if (state == State.NewGame)
 			parseNewGame(input);
 		else if (state == State.AreYouSure)
 			parseAreYouSure(input.toLowerCase());
 	}
 	
 	private void parseNormal(String input) {
 		Scanner scanner = new Scanner(input);
 		if (!scanner.hasNext()) {
 			print("");
 		}
 		else {
 			String command = scanner.next();
 			if (command.equals("newgame") && !scanner.hasNext()) {
 				caller = AreYouSureCaller.Newgame;
 				doAreYouSure();
 			}
 	    	else if (command.equals("loadgame") && !scanner.hasNext()) {
 				caller = AreYouSureCaller.Loadgame;
 				doAreYouSure();
 	    	}
 	    	else if (command.equals("savegame") && !scanner.hasNext()) {
 				caller = AreYouSureCaller.Savegame;
 				doAreYouSure();
 	    	}
 	    	else if (command.equals("highscores") && !scanner.hasNext()) {
 	    		printHighScores();
 	    	}
 	    	else if (command.equals("credits") && !scanner.hasNext()) {
 	    		printCredits();
 	    	}
 	    	else if (command.equals("step")) {
 	    		if (!scanner.hasNext()) {
 	    			doStep(1);
 	    		} else if (scanner.hasNextInt()) {
 	    			int n = scanner.nextInt();
 	    			doStep(n);
 	    		} else {
 	    			print("Invalid usage of step command - step or step n");
 	    		}
 	    	}
 	    	else if (command.equals("set") && scanner.hasNext()) {
 	    		String component = scanner.next();
 	    		if (component.equals("valve") && scanner.hasNextInt()) {
 	    			int valveID = scanner.nextInt();
 	    			if (scanner.hasNext()) {
 	    				String valveCommand = scanner.next();
 		    			if (valveCommand.equals("open") || valveCommand.equals("close")) {
 		    				boolean success;
 		    				if (valveCommand.equals("open")) {
 		    					success = presenter.setValve(valveID, true);
 		    				}
 		    				else { //close
 		    					success = presenter.setValve(valveID, false);
 		    				}
 		    				if (success)
 		    					print("Valve was successfully set");
 		    				else
 		    					print("Valve was not successfully set. Try another (smaller) valve ID.");
 		    			}
 	    				else {
 	    					printNotValidValve();
 	    				}
 	    			}
 		    		else {
 		    			printNotValidValve();
 		    		}
 	    		}
 	    		else if (component.equals("valve") && !scanner.hasNextInt()) {
 	    			printNotValidValve();
 	    		}
 	    		else if (component.equals("pump") && scanner.hasNextInt()) {
 	    			int pumpID = scanner.nextInt();
 	    			if (scanner.hasNext()) {
 	    				String pumpCommand = scanner.next();
 		    			if (pumpCommand.equals("on") || pumpCommand.equals("off")) {
 		    				boolean success;
 		    				if (pumpCommand.equals("on")) {
 		    					success = presenter.setPump(pumpID, true);
 		    				}
 		    				else { //close
 		    					success = presenter.setPump(pumpID, false);
 		    				}
 		    				if (success)
 		    					print("Pump was successfully set");
 		    				else
 		    					print("Pump was not successfully set. Try another (smaller) pump ID.");
 		    			}
 	    				else {
 	    					printNotValidPump();
 	    				}
 	    			}
 		    		else {
 		    			printNotValidPump();
 		    		}
 	    		}
 	    		else if (component.equals("pump") && !scanner.hasNextInt()) {
 	    			printNotValidPump();
 	    		}
 	    		else if (component.equals("controlrods") && scanner.hasNextInt()) {
 	    			int percentageLowered = scanner.nextInt();
 	    			if (percentageLowered >= 0 && percentageLowered <= 100 && !scanner.hasNext()) {
 	    				presenter.setControlRods(percentageLowered);
 	    				print("Control rods were successfully set.");
 	    			}
 	    			else {
 	    				print("Control rods have to be set to a value between 0 and 100 (i.e. \"set control rods 50\")");
 	    			}
 	    		}
 	    		else if (component.equals("controlrods") && !scanner.hasNextInt()) {
 	    			print("Please select a value for the control rods: set controlrods n (n is a number between 0 and 100)");
 	    		}
 	    		else {
 	    			print("Incorrect usage of set command - set valve, set controlrods, set pump");
 	    		}
 	    	}
 	    	else if (command.equals("set") && !scanner.hasNext()) {
 	    		print("Incorrect usage of set command - set valve, set controlrods, set pump");
 	    	}
 	    	else if (command.equals("repair") && scanner.hasNext()) {
 	    		String component = scanner.next();
 	    		if (component.equals("turbine") && !scanner.hasNext()) {
 	    			doRepairTurbine();
 	    		}
 	    		else if (component.equals("pump") && scanner.hasNextInt()) {
 	    			int pumpID = scanner.nextInt();
 	    			doRepairPump(pumpID);
 	    		}
 	    		else {
 	    			print("Not a valid command.");
 	    		}
 	    	}
 	    	else if ( (command.equals("exit") || command.equals("quit")) && !scanner.hasNext()) {
 	    		caller = AreYouSureCaller.Exit;
 	    		doAreYouSure();
 	    	}
 			else {
 				print("Not a valid command.");
 			}
 		}
 		scanner.close();
 	}
 
 	private void parseUninitialised(String input) {
     	Scanner scanner = new Scanner(input);
 		if (!scanner.hasNext()) {
 			//Nothing
 		}
 		else {
 	    	String next = scanner.next();
 	    	if (next.equals("newgame") && !scanner.hasNext()) {
 	    		doNewGame();
 	    	}
 	    	else if (next.equals("loadgame") && !scanner.hasNext()) {
 	    		doLoadGame();
 	    	}
 	    	else if (next.equals("highscores") && !scanner.hasNext()) {
 	    		print("Ask for high scores after the game is initialised.");
 	    	}
 	    	else if (next.equals("credits") && !scanner.hasNext()) {
 	    		printCredits();
 	    	}
 	    	else {
 	    		print("The game is uninitialised. Please use one of the following commands: newgame, loadgame, highscores, credits.");
 	    	}
 		}
 		scanner.close();
     }
 	
 	private void parseNewGame(String input) {
 		if (input.length() > 30) {
 			print("Your name is too long - please use a name shorter than 30 characters.");
 		}
 		else {
 			presenter.newGame(input);
 			state = State.Normal;
 			print("New game started.");
 		}
 	}
 	
 	private void parseAreYouSure(String input) {
 		Scanner scanner = new Scanner(input);
 		if (scanner.hasNext()) {
 			String next = scanner.next();
 			if( (next.equals("yes") || next.equals("y")) && !scanner.hasNext()) {
 				state = State.Normal;
 				if      (caller == AreYouSureCaller.Newgame)	doNewGame();
 				else if (caller == AreYouSureCaller.Savegame)	doSaveGame();
 				else if (caller == AreYouSureCaller.Loadgame)	doLoadGame();
 				else if (caller == AreYouSureCaller.Exit)		doExit();
 			}
 			else {
 				state = State.Normal;
 				print("Action not confirmed.");
 			}
 		}
 		caller = AreYouSureCaller.NoAction;
 		scanner.close();
 	}
 	
 	//-------------- Methods used inside parsing -------------------
 	
 	private void doStep(int numSteps)
 	{
 		if (numSteps >= 0 && numSteps <= 10) {
 			presenter.step(numSteps);
 			print("Game advanced " + numSteps + " steps.");
 		}
 		else {
 			print("Too many steps. Try a number between 0 and 10.");
 		}
 	}
 	
 	private void doNewGame() {
 		state = State.NewGame;
 		print("Please enter your name.");
 	}
 	
 	private void doLoadGame() {
 		if (presenter.loadGame()) {
 			state = State.Normal;
 			print("Game loaded from file.");
 		}
 		else {
 			print("Loading a game was not successful: check if a savegame file exists or start a new game.");
 		}
 	}
 	
 	private void doSaveGame() {
 		if (presenter.saveGame() && state == State.Normal) {
 			print("Game saved to file.");
 		}
 		else {
 			print("Saving a game was not successful.");
 		}
 	}
 	
 	private void doRepairTurbine() {
 		if (presenter.repairTurbine()) {
 			print("Repair on the turbine has began.");
 		}
 		else {
 			print("Turbine is either functional or already being repaired.");
 		}
 	}
 	
 	private void doRepairPump(int pumpID) {
 		if (presenter.repairPump(pumpID)) {
 			print("Repair on pump " + pumpID + " has began.");
 		}
 		else {
 			print("Pump " + pumpID + " is either functional or already being repaired.");
 		}
 	}
 	
 	private void doExit() {
 		System.exit(0);
 	}
 	
 	private void doAreYouSure() {
 		state = State.AreYouSure;
 		print("Are you sure (Y/n)");
 	}
 	
 	private void printHighScores() {
 		List<HighScore> highScores = presenter.getHighScores();
 		if (!highScores.isEmpty()) {
     		for (HighScore highScore : highScores) {
     			print(highScore.getName() + ": " + highScore.getHighScore());
     		}
 		}
 		else {
 			print("No high scores yet.");
 		}
 	}
 	
 	private void printCredits() {
 		print("Created by:");
 		print("(Put superhero names here)");
 	}
 	
 	private void printNotValidValve() {
 		print("Not a valid command. Format for setting a valve: set valve id newState");
 		print("where \"id\" is the ID of the valve and \"newState\" is either \"open\" or \"close\"");
 		print("i.e. \"set valve 2 open\" or \"set valve 4 close\"");
 	}
 	
 	private void printNotValidPump() {
 		print("Not a valid command. Format for setting a pump: set pump id newState");
 		print("where \"id\" is the ID of the pump and \"newState\" is either \"on\" or \"off\"");
 		print("i.e. \"set pump 2 on\" or \"set pump 1 off\"");
 	}
 	
 	private enum State {
 		Normal, NewGame, AreYouSure, Uninitialised;
 	}
 	
 	private enum AreYouSureCaller {
 		Newgame, Savegame, Loadgame, Exit, NoAction;
 	}
 }
