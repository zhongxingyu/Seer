 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.*;
 
 /**
  * The GameInterface class creates a GUI to represent a sudoku board and 
  * allows the user to interact with the board.
  * @author Aaron Balsara, Nicholas Figueira, David Loyzaga
  *
  */
 public class GameInterface implements FocusListener {
 	/**
 	 * The board representing the current sudoku game.
 	 */
 	private Board currentGame;
 	/**
 	 * An array of text fields used to represent the sudoku board.
 	 */
 	private JTextField[][] sudokuBoard;
 	/**
 	 * The game player class which is used to interface with the board.
 	 */
 	private GamePlayer gamePlayer;
 	/**
 	 * A label to provide feedback to the player.
 	 */
 	private final JLabel statusIndicator;
 	/**
 	 * The difficulty of the current game.
 	 */
 	private int difficulty;
 	/**
 	 * The text field that most recently had the focus.
 	 */
 	private JTextField lastSelected;
 	/**
 	 * The clock used to measure the length of sudoku games.
 	 */
 	private Timer clock;
 	/**
 	 * The button pressed to give a hint.
 	 */
 	private JButton hintButton;
 
 	private final String hintTip = "Gives a random number in the sudoku";
 	private final String ResetTip = "Resets the sudoku to the orginal numbers";
 	private final String SolveTip = "Solves the sudoku puzzle";
 	
 	private final Color YELLOW = new Color (254, 255, 210);
 	private final Color GREEN = new Color (200, 255, 200);
 
 	/**
 	 * The constructor for GameInterface requires a GamePlayer to be made.
 	 * @param gamePlayer	The gamePlayer that will receive input from this
 	 * class.
 	 */
 	public GameInterface (GamePlayer gamePlayer) {
 
 		sudokuBoard = new JTextField[9][9];
 		this.gamePlayer = gamePlayer;
 		difficulty = -1;
 		statusIndicator = new JLabel(" ");
 
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 		.addKeyEventDispatcher(new KeyEventDispatcher() {
 			@Override
 			public boolean dispatchKeyEvent(KeyEvent e) {
 				if (e.getID() == KeyEvent.KEY_TYPED) {
 					if (e.getKeyChar() == 'h') 
 						hint ();
				}
 				return false;
 			}
 		});
 		selectDifficulty ();
 	}
 
 	/**
 	 * Makes the Sudoku board using 9 3x3 grids within each 3x3 grid
 	 * @param difficulty The selected difficulty of the game
 	 */
 	private void makeSudokuBoard (String difficulty) {
 
 		JFrame frame = new JFrame();
 		if (difficulty == null) {
 			frame.setTitle("Sudoku solver");
 		} else {
 			frame.setTitle("Difficulty: " + difficulty);	
 		}
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		
 		JPanel sudokuPanel = make3x3Grid();
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				JPanel smallPanel = make3x3Grid();
 				if (difficulty != null) {
 					makeSudokuCell (smallPanel, i, j);	
 				} else {
 					currentGame = new Board (Board.EMPTYBOARD);
 					makeSudokuCell (smallPanel, i, j);
 				}
 				sudokuPanel.add(smallPanel);
 			}
 		}
 		updateBoard();
 		sudokuPanel.setBorder(BorderFactory.createMatteBorder(4,4,4,4, Color.BLACK));
 
 		for (int i = 0; i < 81; i++) {
 			int row = i % 9;
 			int col = i / 9;
 			JTextField field = sudokuBoard[row][col];
 			field.addFocusListener(this);
 		}
 
 		frame.add(sudokuPanel, BorderLayout.CENTER);
 
 		if (difficulty != null) {
 			frame.add(makeSideButtons(frame), BorderLayout.EAST);	
 		} else {
 			frame.add(makeSolverSideButtons(frame), BorderLayout.EAST);
 		}
 
 		frame.pack();
 		frame.setSize(765, 660);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
	/**
	 * Used for solver mode, creates a sudoku board with no cells filled in.
	 */
 	private void makeEmptyBoard () {
 		makeSudokuBoard(null);
 	}
 
 	/**
 	 * Creates a cell in the Sudoku Board
 	 * @param panel The 3x3 grid
 	 * @param startRow The row in which the 3x3 grid is in
 	 * @param startColumn The column in which the 3x3 grid is in
 	 * @param numbers A 2D Array that holds all the numbers on the sudoku board
 	 */
 	private void makeSudokuCell (JPanel panel, int startRow, int startColumn) {
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				final int row = startRow * 3 + i;
 				final int column = startColumn * 3 + j;
 				final String value = String.valueOf(currentGame.cellValue(row, column));
 				JTextField field = new JTextField();
 
 				Font font = new Font("Arial", Font.PLAIN, 30);
 				field.setFont(font);
 				field.setHorizontalAlignment(JLabel.CENTER);
 				field.setBorder(BorderFactory.createLineBorder(Color.black));
 				if (!value.equals("0")) {
 					field.setEditable(false);
 					field.setBackground(new Color (220, 220, 220));
 					field.setFocusable(false);
 				}
 
 				field.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent event) {
 						JTextField me = (JTextField) event.getSource();
 						editTextField(me);
 						
 						int row = getFieldCoordinates(me);
 						int col = row % 10;
 						row = row / 10;
 						boolean done = false;
 						for (int i = row; i < 9 && !done; i++) {
 							for (int j = col; j < 9 && !done; j++) {
 								if (sudokuBoard[i][j].getText().equals("")) {
 									sudokuBoard[i][j].requestFocus();
 									done = true;
 								}
 							}
 						}
 					}
 				});
 				sudokuBoard[row][column] = field;
 				panel.add(field);
 			}
 		}
 	}
 
 	/**
 	 * The actions to be performed in the event an object loses focus.
 	 */
 	public void focusLost(FocusEvent e) {
 		JTextField eventTrigger = (JTextField) e.getComponent();
 		editTextField(eventTrigger);
 	}
 
 	/**
 	 * Updates the board based on the changes in this text field. 
 	 * @param me	The text field whos changes need to be added to the board.
 	 */
 	private void editTextField (JTextField me) {
 		if (me.getBackground().equals(GREEN)) {
 			me.setBackground(Color.WHITE);	
 		}
 		
 		int row = getFieldCoordinates(me);
 		int col = row % 10;
 		row = row / 10;
 		String text = me.getText();
 
 		if (text.equals("")) {
 			gamePlayer.clearCell(row, col);
 			return;
 		}
 		if (text.length() > 1) {
 			updateBoard();
 			text = me.getText();
 			if (text.equals("")) {
 				gamePlayer.clearCell(row, col);
 				return;
 			}
 		}
 
 		char input = text.charAt(0);
 		if (!Character.isDigit(input) || input == '0') {
 			text = "";
 			me.setText(text);
 			return;
 		}
 
 		int num = Character.getNumericValue(input);
 		if (currentGame.getSet(row, col)) {
 			if (currentGame.cellValue(row, col) == num) {
 				return;
 			} else {
 				gamePlayer.clearCell(row, col);
 				if (!gamePlayer.assign(row, col, num)) {
 					gamePlayer.clearCell(row, col);
 					updateBoard();
 					return;
 				}
 			}
 		} else {
 			if (!gamePlayer.assign(row, col, num)) {
 				gamePlayer.clearCell(row, col);
 				updateBoard();
 				return;
 			}
 		}
 		updateBoard();
 	}
 	
 	/**
 	 * The actions to be performed in the event an object gains focus.
 	 */
 	@Override
 	public void focusGained(FocusEvent e) {
 		if (e.getComponent() instanceof JTextField) {
 			JTextField eventTrigger = (JTextField) e.getComponent();
 			if (eventTrigger.getBackground().equals(Color.WHITE)) {
 				eventTrigger.setBackground(GREEN);	
 			}
 			
 			lastSelected = eventTrigger;
 		}
 	}
 
 	/**
 	 * Creates a small 3x3 grid and places it on a panel.
 	 * @return JPanel	A JPanel containing the 3x3 grid.
 	 */
 	private JPanel make3x3Grid () {
 		final GridLayout gridLayout = new GridLayout(3, 3);
 		JPanel panel = new JPanel(gridLayout);
 		panel.setBorder(BorderFactory.createLineBorder(Color.black));
 		return panel;
 	}
 
 	/**
 	 * Makes the buttons for the sudoku game mode and places them on a panel
 	 * @return JPanel	The JPanel containing the buttons.
 	 */
 	private JPanel makeSideButtons (final JFrame frame) {
 		JPanel sideButtons = new JPanel();
 		sideButtons.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		sideButtons.setPreferredSize(new Dimension(120, 600));
 		
 		JButton menuButton = initButton("Menu", "Returns to the menu");
 		menuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				updateStatus(" ");
 				selectDifficulty();
 			}
 		});
 
 		JButton newGameButton = initButton("New Game", "Starts a new game");
 		newGameButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				updateStatus(" ");
 				gamePlayer.newGame(difficulty);
 				if (difficulty == BoardGenerator.EASY){
 					makeSudokuBoard ("Easy");	
 				} else if (difficulty == BoardGenerator.MEDIUM) {
 					makeSudokuBoard("Medium");
 				} else {
 					makeSudokuBoard("Hard");
 				}
 			}
 		});
 		hintButton = initButton("Hint", hintTip);
 		hintButton.setText("Hint: " + gamePlayer.hintsLeft());
 		hintButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				hint ();
 			}
 		});
 		final JButton resetButton = initButton("Reset", ResetTip);
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.resetGame();
 				updateStatus("Board reset");
 				updateBoard();
 				hintButton.setText("Hint: " + gamePlayer.hintsLeft());
 			}
 		});
 		JButton solveButton = initButton("Solve", SolveTip);
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.solveBoard();
 				updateStatus("Board solved");
 				updateBoard();
 				stopTimer();
 				resetButton.setEnabled(false);
 			}
 		});
 		
 		final JLabel timerLabel = new JLabel("Timer");
 		timerLabel.setHorizontalAlignment(JLabel.CENTER);
 		timerLabel.setPreferredSize(new Dimension(110, 20));
 		
 		clock = new Timer();
 		TimerTask task = new TimerTask() {
 			int seconds = 0;
 			int mins = 0;
 			int hours = 0;
 			public void run() {
 				seconds++;
 				if (seconds >= 60) {
 					seconds = 0;
 					mins++;
 				}
 				if (mins >= 60) {
 					mins = 0;
 					hours++;
 				}
 				String timerText = "Time: ";
 				if (hours > 0) {
 					timerText = timerText.concat(hours + ":");
 				}
 				if (mins < 10 && hours > 0) {
 					timerText = timerText.concat("0");
 				}
 				timerText = timerText.concat(mins + ":");
 				if (seconds < 10) {
 					timerText = timerText.concat("0");
 				}
 				timerText = timerText.concat(String.valueOf(seconds));
 				
 				timerLabel.setText(timerText);
 	        }
 		};
 		clock.schedule(task, 0, 1000);
 
 		JButton helpButton = initButton("Help", "Help Button");
 		helpButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				helpAction ();
 			}
 		});
 
 		JLabel blankLabel2 = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 2;
 		sideButtons.add(menuButton, c);
 		c.weighty = 0.25;
 		c.gridy = 3;
 		sideButtons.add(statusIndicator, c);
 		c.gridy = 4;
 		sideButtons.add(newGameButton, c);
 		c.gridy = 6;
 		sideButtons.add(hintButton, c);
 		c.gridy = 10;
 		sideButtons.add(resetButton, c);
 		c.gridy = 14;
 		sideButtons.add(solveButton, c);
 		c.gridy = 18;
 		sideButtons.add(timerLabel, c);
 		c.gridy = 19;
 		sideButtons.add(helpButton, c);
 		c.weighty = 1;
 		c.gridy = 22;
 		sideButtons.add(blankLabel2, c);
 		return sideButtons;
 	}
 	
 	public void hint () {
 		if (lastSelected == null) {
 			return;
 		}
 		if (!lastSelected.getText().equals("")) {
 			return;
 		}
 		int row = getFieldCoordinates(lastSelected);
 		int col = row % 10;
 		row = row / 10;
 		if (gamePlayer.hint(row, col)) {
 			sudokuBoard[row][col].setBackground(YELLOW);
 			sudokuBoard[row][col].setEditable(false);
 		}
 		updateBoard();
 		if (gamePlayer.hintsLeft() == 0) {
 			hintButton.setEnabled(false);
 		}
 		hintButton.setText("Hint: " + gamePlayer.hintsLeft());
 	}
 	
 	/**
 	 * Stops the game timer. Activated when the sudoku game is complete.
 	 */
 	public void stopTimer () {
 		if (clock != null) {
 			clock.cancel();	
 		}
 	}
 
 	/**
 	 * Creates the side buttons for Solver mode and places them on a panel.
 	 * @param frame	The frame that will be using the buttons.
 	 * @return		The JPanel with the components on it.
 	 */
 	private JPanel makeSolverSideButtons (final JFrame frame) {
 		JPanel sideButtons = new JPanel();
 		sideButtons.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		sideButtons.setPreferredSize(new Dimension(120, 600));
 
 		JButton newGameButton = initButton("Menu", "Returns to the menu");
 		newGameButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				updateStatus(" ");
 				selectDifficulty();
 			}
 		});
 		final JButton resetButton = initButton("Reset", ResetTip);
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.solverMode();
 				updateStatus("Board reset");
 				updateBoard();
 				sudokuBoard[0][0].requestFocus();
 			}
 		});
 		JButton solveButton = initButton("Solve", SolveTip);
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.solveBoard();
 				updateStatus("Board solved");
 				updateBoard();
 			}
 		});
 		JLabel blankLabel1 = new JLabel(" ");
 		JLabel blankLabel2 = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 2;
 		sideButtons.add(blankLabel1, c);
 		c.weighty = 0.25;
 		c.gridy = 3;
 		sideButtons.add(statusIndicator, c);
 		c.gridy = 4;
 		sideButtons.add(newGameButton, c);
 		c.gridy = 10;
 		sideButtons.add(resetButton, c);
 		c.gridy = 14;
 		sideButtons.add(solveButton, c);
 		c.weighty = 1;
 		c.gridy = 22;
 		sideButtons.add(blankLabel2, c);
 		return sideButtons;
 	}
 
 	/**
 	 * Shows a help frame to help the user play sudoku.
 	 */
 	private void helpAction () {
 		JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		URL helpURL = GameInterface.class.getResource("Help.html");
 		JEditorPane editorPane = new JEditorPane();
 		editorPane.setEditable(false);
 		try {
 			editorPane.setPage(helpURL);
 		} catch (IOException e) {
 			System.err.println("Attempted to read a bad URL: " + helpURL);
 		}
 		JScrollPane editorScrollPane = new JScrollPane(editorPane);
 		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.add(editorScrollPane);
 		frame.setTitle("Sudoku Help");
 		frame.pack();
 		frame.setSize(400, 400);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Pops up the initial window that asks the user to select a
 	 * difficulty. It then closes and runs the game with the
 	 * selected difficulty.
 	 */
 	public void selectDifficulty () {
 		final JFrame frame = new JFrame();
 		frame.setFocusable(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		
 		final JButton easy = initButton("Easy", "Easy mode");
 		easy.setMnemonic(KeyEvent.VK_E);
 		easyAction(easy, frame);
 		JButton medium = initButton("Medium", "Medium mode");
 		medium.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.MEDIUM);
 				difficulty = BoardGenerator.MEDIUM;
 				makeSudokuBoard ("Medium");
 			}
 		});
 		JButton hard = initButton("Hard", "Hard mode");
 		hard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.HARD);
 				difficulty = BoardGenerator.HARD;
 				makeSudokuBoard ("Hard");
 			}
 		});
 		JButton solveButton = initButton("Solver", "Sudoku solver");
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				difficulty = 0;
 				makeEmptyBoard ();
 				gamePlayer.solverMode();
 			}
 		});
 
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 		.addKeyEventDispatcher(new KeyEventDispatcher() {
 			@Override
 			public boolean dispatchKeyEvent(KeyEvent e) {
 				if (e.getID() == KeyEvent.KEY_TYPED) {
 					if (e.getKeyChar() == 'e') {
 						easyAction(easy, frame);
 					}
 				}	
 				return false;
 			}
 		});
 		
 		JPanel difficulties = new JPanel (new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		JLabel blankLabel1 = new JLabel(" ");
 		JLabel blankLabel2 = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 2;
 		difficulties.add(blankLabel1, c);
 		c.weighty = 0.25;
 		c.gridy = 6;
 		difficulties.add(easy, c);
 		c.gridy = 10;
 		difficulties.add(medium, c);
 		c.gridy = 14;
 		difficulties.add(hard, c);
 		c.gridy = 15;
 		difficulties.add(solveButton, c);
 		c.weighty = 1;
 		c.gridy = 18;
 		difficulties.add(blankLabel2, c);
 
 		frame.add(difficulties);
 		frame.pack();
 		frame.setSize(150, 250);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	public void easyAction (JButton easy, final JFrame frame) {
 		easy.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.EASY);
 				difficulty = BoardGenerator.EASY;
 				makeSudokuBoard ("Easy");
 			}
 		});
 	}
 	
 	/**
 	 * Creates a JButton with a given name and roll over tool tip
 	 * @param buttonName The String the JButton is called
 	 * @param toolTip The String of the tool tip
 	 * @param width The width of the JButton
 	 * @return The JButton
 	 */
 	private JButton initButton (String buttonName, String toolTip) {
 		JButton button = new JButton (buttonName);
 		button.setToolTipText(toolTip);
 		button.setHorizontalAlignment(JButton.CENTER);
 		button.setPreferredSize(new Dimension(110, 20));
 		return button;
 	}
 
 	/**
 	 * Updates the GUI to display the board's new values.
 	 */
 	public void updateBoard () {
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				JTextField field = sudokuBoard[i][j];
 				int num = currentGame.cellValue(i, j);
 				if (num == 0) {
 					if (!field.getBackground().equals(YELLOW)) {
 						field.setText("");	
 					}
 				} else {
 					field.setText(String.valueOf(num));
 				}
 			}
 		}
 		if (currentGame.isComplete()) {
 			stopTimer();
 		}
 	}
 
 	/**
 	 * Updates the value of the label on the GUI. Used to give feedback.
 	 * @param newStatus	The message to be displayed by the label.
 	 */
 	public void updateStatus (String newStatus) {
 		statusIndicator.setText(newStatus);
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				statusIndicator.setText(" ");
 			}
 		}, 2000); // Timer for 2 secs. Is a little buggy when you spam click a number.
 	}
 	
 	private int getFieldCoordinates (JTextField field) {
 		int row = -1;
 		int col = -1;
 		boolean found = false;
 		for (int i = 0; i < 9 && !found; i++) {
 			for (int j = 0; j < 9 && !found; j++) {
 				if (sudokuBoard[i][j].equals(field)) {
 					row = i;
 					col = j;
 					found = true;
 				}
 			}
 		}
 		if (found) {
 			return 10 * row + col;
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Sets the current board to be displayed by the interface to the given one.
 	 * @param board	The board to be displayed.
 	 */
 	public void setBoard (Board board) {
 		currentGame = board;
 	}
 }
