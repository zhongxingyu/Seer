 import java.awt.*;
 import java.awt.event.*;
 import java.lang.Integer;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.TimerTask;
 import javax.swing.*;
 import javax.swing.border.Border;
 
 /**
  * 
  * @author Hayden, Laura, Jerome, Steven
  * some code inspired from Core Java Fundamentals Book 1 Chapter 8, Event Handling p329/ Cay Hortsmann
  * code for blinkFuntion command inspired from:
  * http://www.daniweb.com/software-development/java/threads/268414/changing-a-jbuttons-colour-with-a-timer
  * @version 0.1
  */
 
 
 /**
  * SudokuFrame creates the GUI for Board (implemented by SudokuBoard)
  * @param currentValue is the value used to 
  * @param board represents the current board
  * @param buttonRow is the sudoku grid in ArrayList form
  * @param buttonPanel is JPanel containing the Sudoku grid or 'play' area JButtons
  * @param commandPanel is a JPanel containing game features JButtons
  * @param scorePanel is a JPanel with JButtons that control the game's number inputs
  * @param buttonInputs is the number inputs JButtons in ArrayList form
  * @param newGameButton triggers a pop-up with the option to initialise a new game
  * @param resetGameButton triggers a pop-up with the option to reset the current game to its initial state
  * @param revealButton triggers a pop-up to 
  * @param solutionButton
  * @param undoButton
  * @param draftButton
  * @param eraseButton
  */
 
 public class SudokuFrame extends JFrame
 {
 	public SudokuFrame(Board sBoard)
 	{
 		this.board = sBoard;
 		
 		setTitle("SUDOKU FUN!");
 		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		
 		currentValue = BLANK;
 		
 		buttonRow = new ArrayList<JButton>();
 		buttonInputs = new LinkedList<JButton>();
 		
 		buttonPanel = new JPanel();
 		buttonPanel.setBackground(DEFAULTBG);
 		
 		setUpGrid();
 		
 		commandPanel = new JPanel();
 		commandPanel.setLayout(new FlowLayout());
 		commandPanel.setBackground(DEFAULTBG);
 		
 		scorePanel = new JPanel();
 		scorePanel.setLayout(new FlowLayout());
 		scorePanel.setVisible(true);
 		scorePanel.setBackground(DEFAULTBG);
 
 		
 		newGameButton = makeCommandButton("NEW GAME", commandPanel, new newGameFunction());
 		resetGameButton = makeCommandButton("Restart game", commandPanel, new resetFunction());
 		revealButton = makeCommandButton("Reveal ALL", commandPanel, new revealFunction());
 		solutionButton = makeCommandButton("Check my solution!", commandPanel, new solutionFunction());
 		undoButton = makeCommandButton("Undo my last", commandPanel, new undoFunction());
 		draftButton = makeCommandButton("DRAFT", scorePanel, new draftFunction());
 		eraseButton = makeCommandButton("Rubber", scorePanel, new eraseFunction());
 
 		prepareForDifficulty();
 		
 		setUpButtonInputs();
 		
 		JPanel fillerEast = new JPanel();
 		fillerEast.setBackground(DEFAULTBG);
 		JPanel fillerWest = new JPanel();
 		fillerWest.setBackground(DEFAULTBG);
 		
 		add(buttonPanel,BorderLayout.CENTER);
 		add(commandPanel,BorderLayout.NORTH);
 		add(scorePanel,BorderLayout.SOUTH);
 		add(fillerEast,BorderLayout.EAST);
 		add(fillerWest, BorderLayout.WEST);
 		
 	}
 	
 	/**
 	 * Prepares a game layout according to difficulty
 	 */
 	private void prepareForDifficulty() {
 
 		if(board.isDifficultyEasy()){
 			solutionButton.setVisible(true);
 		}
 		if(board.isDifficultyMedium()){
 			solutionButton.setVisible(true);
 		}else{
 			solutionButton.setVisible(true);
 		}
 	}
 	
 	/**
 	 * Sets up the number inputs and adds to an ArrayList and to a JPanel
 	 * @param keyButton
 	 * @param label
 	 */
 	private void setUpButtonInputs(){
 		for (int i = 0; i < LABELS.length(); i++)
 	      {
 	         final String label = LABELS.substring(i, i + 1);
 	         JButton keyButton = new JButton(label);
 	         keyButton.addActionListener(new NumberSelect());
 	         keyButton.setBackground(DEFAULT_COMMAND);
 	         keyButton.setFont(USERINPUT);
 	         buttonInputs.add(keyButton);
 	         scorePanel.add(keyButton);
 	      }
 	}
 	
 	/**
 	 * Sets up the grid at the beginning of a new game (not a reset)
 	 * @param cellVal display value to be passed on to button
 	 * @param i,j represent row and column values of cell location respectively
 	 * @param given represents whether or not a value is initially given to the user
 	 */
 	private void setUpGrid(){
 		buttonPanel.removeAll();
 		buttonPanel.setLayout(new GridLayout(9,9));
 		buttonRow.removeAll(buttonRow);
 		for(int i = 1; i<=9; i++)
 		{
 			for(int j = 1; j<=9; j++){
 				String cellVal;
 				boolean given = false;
 				if (board.isInitiallySet(i,j))
 				{
 					cellVal = Integer.toString(board.getCellValue(i,j));
 					given = true;
 				}else{
 					cellVal = BLANK;
 				}
 				buttonRow.add(makeGridButton(cellVal, buttonPanel, given));
 			}
 		}
 	}
 	
 	/**
 	 * Scans the board's current state and updates all values
 	 * @param b is a temporary JButton value used as whole button grid is scanned and checked
 	 */
 	
 	private void labelGrid(){
 		JButton b;
 		for(int i = 1; i<=9; i++){
 			for(int j = 1; j<=9; j++){
 				b = buttonRow.get(findIndex(i,j));
 				if(!board.isInitiallySet(i, j)){
 					if (board.hasInput(i,j)){
 						b.setText(Integer.toString(board.getCellValue(i,j)));
 					}else{
 						b.setText(BLANK);
 						if(board.hasDrafts(i,j)){
 							for(int draft = 1; draft <= 9; draft++){
 								if(board.isVisibleCellDraft(i, j, draft)){
 									toggleDraftValues(Integer.toString(draft), b);
 								}
 							}
 						}
 						
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new JButton used in the Sudoku 'play' area
 	 * @param button is the new button
 	 * @param name is the button text
 	 * @param panel is the JPanel button will belong to
 	 * @param given says whether a button value is initially given or not
 	 * @return new JButton
 	 */
 	public JButton makeGridButton( String name, JPanel panel, boolean given)
 	{
 		JButton button = new JButton(name);
 		panel.add(button);
 		button.setBackground(DEFAULT_GRID);
 		button.setFont(FIXEDSQUARES);
 		button.setBorder(blackline);
 		if(!given){
 			NumberInsert insert = new NumberInsert(button);
 			button.addActionListener(insert);
 			button.setForeground(userTempColor);
 			button.setLayout(new GridLayout(3,3));
 			for (int i = 0; i < LABELS.length(); i++)
 			{
 			    String label = LABELS.substring(i, i + 1);
 			    JLabel l = new JLabel(label);
 			    l.setVisible(false);
 			    l.setFont(DRAFTSMALL);
 				l.setForeground(draftColor);
 				button.add(l);
 			}
 		}
 		return button;
 	}
 	/**
 	 * Creates a new command or feature button
 	 * @param name
 	 * @param panel
 	 * @param action is the listener to be attached
 	 * @return JButton
 	 */
 	public JButton makeCommandButton(String name, JPanel panel, ActionListener action)
 	{
 		JButton button = new JButton(name);
 		panel.add(button);
 		button.addActionListener(action);
 		button.setBackground(DEFAULT_COMMAND);
 		return button;
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku 'play' area buttons
 	 */
 	
 	private class NumberInsert implements ActionListener
 	{
 		/**
 		 * Constructor
 		 * @param b is the button
 		 */
 		public NumberInsert(JButton button)
 		{
 			b = button;
 		}
 		
 		/**
 		 * action performed if button is clicked
 		 */
 		public void actionPerformed(ActionEvent event){
 			//board.takeSnapShot(rowVal(b), colVal(b));
 			int value = Integer.parseInt(getCurrentValue());
 			if(isButtonToggled(eraseButton, DEFAULT_COMMAND)){
 				//toggleDraftFalse(b);
 				board.removeCellValue(rowVal(b), colVal(b));
 				//board.setIfInitiallySet(rowVal(b), colVal(b), false);
 			}else if(!getCurrentValue().equalsIgnoreCase(BLANK)){
 				if(!isButtonToggled(draftButton, DEFAULT_COMMAND)){
					if(!(board.isDifficultyEasy() && !checkSquare(b,1))){
 						board.setCellValue(rowVal(b), colVal(b), value);
						//board.setIfInitiallySet(rowVal(b), colVal(b), true);
 						//toggleDraftFalse(b);
 					}
 				} else{
 					toggleDraftValues(getCurrentValue(), b);
 				}
 			}
 			labelGrid();
 			}
 		private JButton b;
 	}
 	
 	/**
 	 * Toggles all draft values on a button to false
 	 */
 	
 	public void toggleDraftFalse(JButton button){
 		for(Component label: button.getComponents()){
 			label.setVisible(false);
 			board.setCellDraftVisibility(rowVal(button), colVal(button), Integer.parseInt(((JLabel) label).getText()), false);
 		}
 	}
 	
 	/**
 	 * Toggles a particular draft value
 	 * @param value is the value to be toggled
 	 * @param b is the button concerned in this action 
 	 */
 	
 	private void toggleDraftValues(String value, JButton b){
 		Component label = b.getComponent(Integer.parseInt(value)-1);
 		if(label.isVisible()){
 			label.setVisible(false);
 			board.setCellDraftVisibility(rowVal(b), colVal(b), Integer.parseInt(value), false);
 		} else{
 			label.setVisible(true);
 			board.setCellDraftVisibility(rowVal(b), colVal(b), Integer.parseInt(b.getText()), true);
 		}
 		if(!b.getText().equalsIgnoreCase(BLANK)){
 			Component label2 = b.getComponent(Integer.parseInt(b.getText())-1);
 			b.setText(BLANK);
 			label2.setVisible(true);
 			board.setCellDraftVisibility(rowVal(b), colVal(b), Integer.parseInt(b.getText()), true);
 		}
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku eraseButton
 	 * 
 	 */
 	
 	private class eraseFunction implements ActionListener
 	{
 		
 		public void actionPerformed(ActionEvent event)
 		{
 			//resetCommands();
 			toggleButton(eraseButton, COMMAND_TOGGLED, DEFAULT_COMMAND);
 			resetCurrentValue();
 			highlightValue(BLANK);
 		}
 	}
 	
 	/**
 	 * Checks whether a button is toggled
 	 * @param button
 	 * @param defaultColour
 	 * @return boolean
 	 */
 	
 	public boolean isButtonToggled(JButton button, Color defaultColour){
 		if(!button.getBackground().equals(defaultColour))
 			return true;
 		else return false;
 	}
 	
 	/**
 	 * Toggles a button 
 	 * @param button
 	 * @param colour
 	 * @param defaultColour
 	 */
 	
 	public void toggleButton(JButton button, Color colour, Color defaultColour){
 		if(!button.getBackground().equals(colour)){
 			button.setBackground(colour);
 		}
 		else button.setBackground(defaultColour);
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku number inputs on scorePanel
 	 * Toggles number inputs and changes currentValue to be input
 	 */
 	
 	private class NumberSelect implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			if(currentValue.equalsIgnoreCase(event.getActionCommand())){
 				highlightValue(resetCurrentValue());
 			}else{
 				currentValue = event.getActionCommand();
 				highlightValue(currentValue);
 			}
 			if(isButtonToggled(eraseButton, DEFAULT_COMMAND))
 				toggleButton(eraseButton, COMMAND_TOGGLED, DEFAULT_COMMAND);
 		}
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku newGameButton
 	 * Creates a pop-up with choice of new game difficulty and sets the new game up
 	 */
 	
 	private class newGameFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			if(pane.newGameInGame()){
 				board.clear();
 				board.generate(pane.chooseLevelInGame());
 				//prepareForDifficulty();
 				setUpGrid();
 			}
 		}
 	}
 	
 	/**
 	 * Undoes last action attached to undoButton
 	 */
 	
 	private class undoFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			board.undoLast();
 			labelGrid();
 		}
 	}
 	
 	/**
 	 * ActionListener attached to draftButton - toggles on/off
 	 */
 	
 	private class draftFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			resetCommands();
 			if(isButtonToggled(draftButton, DEFAULT_COMMAND)){
 				toggleButton(draftButton, COMMAND_TOGGLED, DEFAULT_COMMAND);
 			} else toggleButton(draftButton, COMMAND_TOGGLED, DEFAULT_COMMAND);
 		
 		}
 	}
 	
 	/**
 	 * Untoggles all commands
 	 */
 	
 	public void resetCommands(){
 		for(Component commands: commandPanel.getComponents()){
 			commands.setBackground(DEFAULT_COMMAND);
 		}
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku solutionButton
 	 * 
 	 */
 	
 	private class solutionFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			resetCommands();
 			toggleButton(solutionButton, COMMAND_TOGGLED, DEFAULT_COMMAND);
 			blinkOut(solutionButton, DEFAULT_COMMAND,2);
 			highlightValue(BLANK);
 			for(JButton b: buttonRow){
 				if(!board.isInitiallySet(rowVal(b), colVal(b))){
 					if(board.isDifficultyHard() && board.isFilledBoard()){
 						checkSquare(b,3);
 					} else if(!board.isEmptyCell(rowVal(b), colVal(b)))
 						checkSquare(b,3);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * ActionListener attached to Sudoku revealButton
 	 * 
 	 */
 	
 	private class revealFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			if(pane.revealMessage()){
 				revealAll();
 			}
 		}
 	}
 
 	/**
 	 * Checks whether or not a square is filled correctly and makes it flash appropriately
 	 * @param b is button to be checked
 	 * @param seconds time delay for blink
 	 */
 	
 	public boolean checkSquare(JButton b, int seconds)	{
 		
 		boolean correct = board.isCorrectInputForCell(rowVal(b), colVal(b), Integer.decode(getCurrentValue()));
 		if(correct)		
 			b.setBackground(CORRECT);
 		else
 			b.setBackground(WRONG);
 		blinkOut(b,DEFAULT_GRID, seconds);
 		return correct;
 	}
 	
 	/**
 	 * Sets up timer for blinkerFunction
 	 * @param button
 	 * @param colour
 	 * @param seconds
 	 */
 	
 	private void blinkOut(JButton button , Color colour, int seconds){
         Timer timer = new Timer(seconds*1000, new blinkFunction(button, colour));
         timer.start(); 
     }
 	
 	/**
 	 * AciotnListener 
 	 */
 	
 	private class blinkFunction implements ActionListener{
 
 		public blinkFunction(JButton button , Color colour){
 			b = button;
 			c = colour;
 		}
 		public void actionPerformed(ActionEvent e) {
 			b.setBackground(c);
 		}
 		private JButton b;
         private Color c;
 	}
 	
 	/**
 	 * Helper method for resetFunction - removes all input values
 	 */
 	
 	public void removeAll(){
 		for(JButton b: buttonRow){
 			if (!board.isInitiallySet(rowVal(b),colVal(b))){
 				//toggleDraftFalse(b);
 				board.removeCellValue(rowVal(b), colVal(b));
 				//board.setIfInitiallySet(rowVal(b), colVal(b), false);
 				//b.setText(BLANK);
 			}
 		}
 	}
 	
 	/**
 	 * Action listener linked to resetButton
 	 * Resets current puzzle to original set values
 	 */
 	
 	private class resetFunction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			if(pane.restartGame()){
 				removeAll();
 				labelGrid();
 			}
 		}
 	}
 	
 	/**
 	 * Reveals the grid and disables any further gameplay
 	 */
 	
 	public void revealAll(){
 		for(int i = 1; i<=9; i++)
 		{
 			JButton finalButton;
 			for(int j = 1; j<=9; j++){
 				String cellVal;
 				cellVal = Integer.toString(board.getCellValue(i,j));
 				if (!board.hasInput(i,j)){
 					finalButton = buttonRow.get(findIndex(i,j));
 					finalButton.setText(cellVal);
 					finalButton.setEnabled(false);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Resets currentValue
 	 */
 	
 	public String resetCurrentValue(){
 		this.currentValue = BLANK;
 		return currentValue;
 	}
 	
 	/**
 	 * @return currentValue
 	 */
 	
 	public String getCurrentValue(){
 		return this.currentValue;
 	}
 	
 	/**
 	 * Highlights a chosen number input
 	 * @param current is the value to be highlighted - BLANK to be passed in to un-highlight
 	 */
 	
 	public void highlightValue(String current){
 		for(JButton jb: buttonInputs){
 			if(jb.getText().equalsIgnoreCase(current))
 				jb.setBackground(INPUT_TOGGLED);
 			else jb.setBackground(DEFAULT_INPUT);
 		}
 	}
 	
 	/**
 	 * Converts a button's row and column values to an index
 	 * @param row
 	 * @param column
 	 * @return index fir buttonRow ArrayList
 	 */
 	
 	private int findIndex(int row, int col){
 		int index;
 			index = col - 1;
 		if(row > 1){
 			index = index + (row-1) * 9;
 		}
 		return index;
 	}
 	
 	/**
 	 * Converts a button's index to a row value
 	 * @param JButton b
 	 * @return row value for JButton
 	 */
 	
 	private int rowVal(JButton b){
 		int index = buttonRow.indexOf(b);
 		int row;
 		 row = 1 + ((int)Math.floor((index)/9));
 		return row;
 	}
 	
 	/**
 	 * Converts a button's index to a column value
 	 * @param JButton b
 	 * @return column value for JButton
 	 */
 	
 	private int colVal(JButton b){
 		int index = buttonRow.indexOf(b);
 		int col = 1 + index % 9;
 		return col;
 	}
 	
 	public Color DEFAULTBG = new Color(200,200,250);
 	public Border greyline = BorderFactory.createLineBorder(Color.gray);
 	public Border blackline = BorderFactory.createLineBorder(Color.gray, 5);
 	public Timer timer;
 	public TimerTask task;
 	public Color CORRECT = Color.GREEN;
 	public Color WRONG = Color.RED;
 	public Color INPUT_TOGGLED = Color.pink;
 	public Color COMMAND_TOGGLED = Color.yellow;
 	public Color DEFAULT_INPUT = new Color(238,238,238);
 	public Color userTempColor = Color.BLUE;
 	public Color draftColor = Color.LIGHT_GRAY;
 	public Font USERINPUT = new Font ("Courrier", Font.CENTER_BASELINE, 25);
 	public Font DRAFTSMALL = new Font("Courrier",Font.CENTER_BASELINE ,15);
 	public Font FIXEDSQUARES = new Font("Arial", Font.BOLD, 20);
 	private Color DEFAULT_GRID = new Color(238,238,238);
 	private Color DEFAULT_COMMAND = new Color(238,238,238);
 	private JButton newGameButton;
 	private JButton resetGameButton;
 	private JButton undoButton;
 	private JButton draftButton;
 	private JButton eraseButton;
 	private JButton solutionButton;
 	private JButton revealButton;
 	private String currentValue;
 	private JPanel buttonPanel;
 	private JPanel commandPanel;
 	private JPanel scorePanel;
 	public LinkedList<JButton> buttonInputs;
 	private ArrayList<JButton> buttonRow;
 	private OptionPanes pane = new OptionPanes();
 
 	public Board board;
 	public static final String LABELS = "123456789";
 	public static final String BLANK = "";
 	public static final int DEFAULT_WIDTH = 900;
 	public static final int DEFAULT_HEIGHT = 700;
 }
 
 
 
 
 
