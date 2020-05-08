 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 /**
  * A class to display the user interface for the board to allow play
  * @author Sam
  */
 public class GameInterface {
 	
 	//Containers and things that control them
 	static JFrame frame;
 	static Container overall;
 	static Container pane;
 	static Container menu;
 	static Container centralMenu;
 	
 	//The Sudoku boxes themselves and images that go with them
 	static JButton[][] box;
 	static BufferedImage[] imageValue;
 	static BufferedImage[] imageSelectedValue;
 	static ImageIcon[] iconValue;
 	static ImageIcon[] iconSelectedValue;
 	
 	//texture for the background (not implemented)
 	static BufferedImage backgroundTexture;
 	static Graphics2D backgroundTextureGraphic;
 	
 	//Buttons and timer for bottom of game screen
 	static JButton btnMenu;
 	static JButton btnHint;
 	static JLabel elapseTimer;
 	static JLabel timerLabel;
 	static BufferedImage menuImage;
 	static BufferedImage hintImage;
 	static ImageIcon menuIcon;
 	static ImageIcon hintIcon;
 	
 	//Buttons for the main menu
 	static JButton[] btnNewGame;
 	static JButton btnMenuQuit;
 	static ImageIcon[] menuText;
 	
 	//Letter images and constants for the end game
 	static BufferedImage[] letterImage;
 	static ImageIcon[] letter;
 	static final int C = 0;
 	static final int O = 1;
 	static final int N = 2;
 	static final int G = 3;
 	static final int R = 4;
 	static final int A = 5;
 	static final int T = 6;
 	static final int U = 7;
 	static final int L = 8;
 	static final int I = 9;
 	static final int S = 10;
 	static final int Y = 11;
 	static final int W = 12;
 	static final int EXCLAMATION = 13;
 	
 	//General final settings for the game
 	static final int boxWidth = 50;
 	static final int boxHeight = 50;
 	static final int frameWidth = 520;
 	static final int frameHeight = 580;
 	
 	//Settings to decide the viewMode
 	static final boolean MENU = true;
 	static final boolean GAME = false;
 	static boolean viewMode = MENU;
 		
 	//Color presets
 	static final Color PRESET_COLOR = new Color(140, 140, 140);
 	static final Color WRONG_COLOR = new Color(200, 140, 140);
 	static final Color WRONG_PRESET_COLOR = new Color(150, 120, 120);
 	static final Color USER_COLOR = new Color(255, 255, 255);
 	static final Color HINT_COLOR = new Color(230, 230, 230);
 	
 	static final Color PRESET_TEXT_COLOR = new Color(100, 100, 100);
 	static final Color WRONG_TEXT_COLOR = new Color(255, 0, 0);
 	static final Color USER_TEXT_COLOR = Color.BLACK;
 	static final Color HINT_TEXT_COLOR = new Color(150, 150, 250);
 	
 	static final Color BACKGROUND_COLOR = new Color(200, 200, 220);
 	
 	/**
 	 * Constructor of GameInterface
 	 */
 	public GameInterface(){
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		
 		loadImages();
 		
 		//creates a sudoku frame
 		frame = new JFrame("Sudoku");
 		frame.setSize(frameWidth, frameHeight); //sets the frame size
 		pane = frame.getContentPane(); //sets a pane within the frame
 		frame.setSize(frameWidth, frameHeight);
 		frame.setBackground(Color.WHITE);
 		overall = frame.getContentPane();
 		overall.setBackground(Color.WHITE);
 		
 		//Create the two main containers (and the subContainer for the menu) which hold the two possible pages
 		pane = new Container();
 		menu = new Container();
 		centralMenu = new Container();
 		overall.add(pane);
 		overall.add(menu);
 		menu.add(centralMenu);
 		
 		//set information relating to the menu container
 		menu.setSize(frame.getSize());
 		menu.setLocation(0, 0);
 		menu.setLayout(null);
 		
 		centralMenu.setBounds(10, 10, 496, 496);
 		GridLayout centralMenuLayout = new GridLayout(3, 3);
 		centralMenuLayout.setHgap(12);
 		centralMenuLayout.setVgap(12);
 		centralMenu.setLayout(centralMenuLayout);
 		
 		//buttons and images that appear on the main menu
 		btnNewGame = new JButton[3];
 		btnNewGame[0] = new JButton(menuText[6]);
 		btnNewGame[1] = new JButton(menuText[7]);
 		btnNewGame[2] = new JButton(menuText[8]);
 		
 		btnMenuQuit = new JButton(menuText[5]);
 		
 		btnNewGame[0].setBorder(BorderFactory.createEmptyBorder());
 		btnNewGame[1].setBorder(BorderFactory.createEmptyBorder());
 		btnNewGame[2].setBorder(BorderFactory.createEmptyBorder());
 		btnMenuQuit.setBorder(BorderFactory.createEmptyBorder());
 		
 		btnNewGame[0].setRolloverIcon(menuText[10]);
 		btnNewGame[1].setRolloverIcon(menuText[11]);
 		btnNewGame[2].setRolloverIcon(menuText[12]);
 		btnMenuQuit.setRolloverIcon(menuText[9]);
 		
 		centralMenu.add(new JLabel(menuText[0]));
 		centralMenu.add(new JLabel(menuText[1]));
 		centralMenu.add(new JLabel(menuText[2]));
 		centralMenu.add(new JLabel(menuText[3]));
 		centralMenu.add(new JLabel(menuText[4]));
 		centralMenu.add(btnMenuQuit);
 		centralMenu.add(btnNewGame[0]);
 		centralMenu.add(btnNewGame[1]);
 		centralMenu.add(btnNewGame[2]);		
 		
 		btnMenuQuit.addActionListener(new btnQuitListener());		
 		btnNewGame[0].addActionListener(new btnNewGameListener(0));		
 		btnNewGame[1].addActionListener(new btnNewGameListener(1));		
 		btnNewGame[2].addActionListener(new btnNewGameListener(2));
 		
 		menu.setVisible(true);
 		
 		
 		pane.setSize(frame.getSize());
 		pane.setLayout(null);
 		
 		//Setup the actual Sudoku board
 		box = new JButton[9][9];
 		setStartingBoxInfo();
 		
 		//timer label
 		timerLabel = new JLabel("Elapsed Time");
 		pane.add(timerLabel);
 		timerLabel.setBounds(10, frameHeight - 55, timerLabel.getPreferredSize().width, timerLabel.getPreferredSize().height);
 		
 		//timer label with time that will update
 		elapseTimer = new JLabel("00:00");
 		pane.add(elapseTimer);
 		elapseTimer.setBounds(115, frameHeight - 55, elapseTimer.getPreferredSize().width + 100, elapseTimer.getPreferredSize().height);
 		
 		//hint button		
 		btnHint = new JButton(hintIcon);
 		pane.add(btnHint);
 		btnHint.setBounds(350, frameHeight - 60, 155, 25);
 		btnHint.setBorder(BorderFactory.createEmptyBorder());
 		btnHint.addActionListener(new btnHintListener());
 		
 		//menu button
 		btnMenu = new JButton(menuIcon);
 		pane.add(btnMenu);
 		btnMenu.setBounds(180, frameHeight - 60, 155, 25);
 		btnMenu.setBorder(BorderFactory.createEmptyBorder());
 		btnMenu.addActionListener(new btnMenuListener());
 				
 		//Make the frame visible
 		pane.setVisible(false);
 		frame.requestFocus();
 		frame.setVisible(true); //set frame as visible
 
 		updateTimer(); //updates the time elapsed
 		
 	}
 	
 	/**
 	 * Function to update the timer with how much time has elapsed
 	 */
 	public void updateTimer() {
 		int hour, minute, sec;
 		while (!hasWon()) { //checks if game is still going
 			long timeInSeconds = Puzzle.calculateTimeElapse(startTime); //finds the time elapsed
 			hour = (int) (timeInSeconds / 3600);
 			timeInSeconds = timeInSeconds - (hour * 3600);
 			minute = (int) (timeInSeconds / 60);
 			timeInSeconds = timeInSeconds - (minute * 60);
 			sec = (int) (timeInSeconds); //calculates hours, minutes based on total seconds
 			elapseTimer.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute)  + 
 					":" + String.format("%02d", sec)); //updates the timer
 		}
 	}
 	
 	/**
 	 * Returns whether or not there is a selected square
 	 * @return	True if a square is selected, false otherwise
 	 */
 	public boolean squareSelected(){
 		return (source != null);
 	}
 
 	/**
 	 * Sets all the starting boxes positions and settings
 	 */
 	private static void setStartingBoxInfo(){
 		int x = 0;
 		int y = 0;
 		//For every box
 		while (y < 9){
 			while (x < 9){
 				box[x][y] = new JButton();
 				pane.add(box[x][y]);
 				//Set box position and size
 				box[x][y].setBounds(x*boxWidth+((Math.round(x/3)+1) * 10) + x*3, y*boxHeight+((Math.round(y/3)+1) * 10) + y*3, boxWidth, boxHeight);
 				//Set box border
 				box[x][y].setBorder(BorderFactory.createEmptyBorder());
 
 				//Set box Actions
 				box[x][y].addActionListener(new btnSquareListener(x, y));
 				box[x][y].addKeyListener(new keyPressedListener(y, x));
 				x++;
 			}
 			y++;
 			x = 0;
 		}
 
 	}
 	
 	/**
 	 * Function to load all of the images for buttons and the board
 	 */
 	private static void loadImages() {
 		imageValue = new BufferedImage[10];
 		imageSelectedValue = new BufferedImage[10];
 		iconValue = new ImageIcon[10];
 		iconSelectedValue = new ImageIcon[10];
 		//tries to load image files for each square type
 		try {
 			//Loads square values
 			imageValue[0] = ImageIO.read(new File("src/ProjectPics/Invisible/blank.png"));
 			imageValue[1] = ImageIO.read(new File("src/ProjectPics/Invisible/1.png"));
 			imageValue[2] = ImageIO.read(new File("src/ProjectPics/Invisible/2.png"));
 			imageValue[3] = ImageIO.read(new File("src/ProjectPics/Invisible/3.png"));
 			imageValue[4] = ImageIO.read(new File("src/ProjectPics/Invisible/4.png"));
 			imageValue[5] = ImageIO.read(new File("src/ProjectPics/Invisible/5.png"));
 			imageValue[6] = ImageIO.read(new File("src/ProjectPics/Invisible/6.png"));
 			imageValue[7] = ImageIO.read(new File("src/ProjectPics/Invisible/7.png"));
 			imageValue[8] = ImageIO.read(new File("src/ProjectPics/Invisible/8.png"));
 			imageValue[9] = ImageIO.read(new File("src/ProjectPics/Invisible/9.png"));
 			imageSelectedValue[0] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/blank.png"));
 			imageSelectedValue[1] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/1.png"));
 			imageSelectedValue[2] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/2.png"));
 			imageSelectedValue[3] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/3.png"));
 			imageSelectedValue[4] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/4.png"));
 			imageSelectedValue[5] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/5.png"));
 			imageSelectedValue[6] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/6.png"));
 			imageSelectedValue[7] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/7.png"));
 			imageSelectedValue[8] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/8.png"));
 			imageSelectedValue[9] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/9.png"));
 			for (int i = 0; i < 10; i++){
 				iconValue[i] = new ImageIcon(imageValue[i]);
 				iconSelectedValue[i] = new ImageIcon(imageSelectedValue[i]);
 			}
 			
 			//Load background texture (unimplemented)
 			backgroundTexture = ImageIO.read(new File("src/ProjectPics/texture.jpg"));
 			backgroundTextureGraphic = backgroundTexture.createGraphics();
 			
 			//Load letters for the win message
 			letterImage = new BufferedImage[14];
 			letter = new ImageIcon[14];
 			letterImage[C] = ImageIO.read(new File("src/ProjectPics/WinCharacters/C.png"));
 			letterImage[O] = ImageIO.read(new File("src/ProjectPics/WinCharacters/O.png"));
 			letterImage[N] = ImageIO.read(new File("src/ProjectPics/WinCharacters/N.png"));
 			letterImage[G] = ImageIO.read(new File("src/ProjectPics/WinCharacters/G.png"));
 			letterImage[R] = ImageIO.read(new File("src/ProjectPics/WinCharacters/R.png"));
 			letterImage[A] = ImageIO.read(new File("src/ProjectPics/WinCharacters/A.png"));
 			letterImage[T] = ImageIO.read(new File("src/ProjectPics/WinCharacters/T.png"));
 			letterImage[U] = ImageIO.read(new File("src/ProjectPics/WinCharacters/U.png"));
 			letterImage[L] = ImageIO.read(new File("src/ProjectPics/WinCharacters/L.png"));
 			letterImage[I] = ImageIO.read(new File("src/ProjectPics/WinCharacters/I.png"));
 			letterImage[S] = ImageIO.read(new File("src/ProjectPics/WinCharacters/S.png"));
 			letterImage[Y] = ImageIO.read(new File("src/ProjectPics/WinCharacters/Y.png"));
 			letterImage[W] = ImageIO.read(new File("src/ProjectPics/WinCharacters/W.png"));
 			letterImage[EXCLAMATION] = ImageIO.read(new File("src/ProjectPics/WinCharacters/Exclamation.png"));
 			for (int x = 0; x < 14; x++){
 				letter[x] = new ImageIcon(letterImage[x]);
 			}
 			
 			//Load button images for the buttons next to the game board
 			menuImage = ImageIO.read(new File("src/ProjectPics/MainMenu/Menu.png"));
 			menuIcon = new ImageIcon(menuImage);
 			hintImage = ImageIO.read(new File("src/ProjectPics/MainMenu/Hint.png"));
 			hintIcon = new ImageIcon(hintImage);
 			
 			//Load the images for the grid on the main menu
 			menuText = new ImageIcon[13];
 			menuText[0] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Title.png")));
 			menuText[1] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/blank.png")));
 			menuText[2] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/About.png")));
 			menuText[3] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Rules.png")));
 			menuText[4] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/blank.png")));
 			menuText[5] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Quit.png")));
 			menuText[6] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Easy.png")));
 			menuText[7] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Medium.png")));
 			menuText[8] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/Hard.png")));
 			menuText[9] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/QuitU.png")));
 			menuText[10] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/EasyU.png")));
 			menuText[11] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/MediumU.png")));
 			menuText[12] = new ImageIcon(ImageIO.read(new File("src/ProjectPics/MainMenu/HardU.png")));
 
 		} catch (Exception e) {
 			System.out.println("SOME OR ALL IMAGE FILES NOT FOUND!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Creates a newGame and sets the board layout.
 	 */
 	private static void newGame(int difficulty){
 		boardLayout = null;
 		//collect the board from Puzzle.java
 		boardLayout = Puzzle.createPuzzle(difficulty);
 		int x, y;
 		Integer value;
 		
 		//sets each box with it's applicable number and background if needed
 		for (x = 0; x < 9; x++){
 			for (y = 0; y < 9; y++){
 				value = boardLayout[y][x].getCurrentValue();
 				box[x][y].setBackground(Color.white);
 				box[x][y].setIcon(getSquareIcon(value, false));
 				if (value != 0){
 					box[x][y].setBackground(PRESET_COLOR);
 					box[x][y].setForeground(PRESET_TEXT_COLOR);
 				} else {
 					box[x][y].setText("");
 				}
 				box[x][y].setEnabled(true);
 
 			}
 		}
 		
 		startTime = Calendar.getInstance();
 	}
 	
 	/**
 	 * Switches between the game board and the main menu when called
 	 */
 	private static void switchView(){
 		viewMode = !viewMode;
 		
 		menu.setVisible(!menu.isVisible());
 		pane.setVisible(!pane.isVisible());
 		
 		startTime = Calendar.getInstance();
 	}
 	
 	/**
 	 * Resets all imagry on the board, runs legal and win checks then re-paints the pane
 	 */
 	private static void resetSourceBox(){
 		System.out.println("STUFF");
 		int row = 0;
 		int col = 0;
 		int type = -1;
 		JButton currentBox = null;
 		Square currentSquare = null;
 		//for each square
 		while (row < 9){
 			while (col < 9){
 				currentSquare = boardLayout[row][col];
 				//if there is a set value for the square, set it to that
 				if (currentSquare.getCurrentValue() >= 0){
 					currentBox = box[col][row];
 					//if not legal, set the look
 					if (LegalCheck.isNotLegal(boardLayout, currentSquare, currentSquare.getCurrentValue())){
 						currentSquare.setType(Square.ERROR_CELL);
 					} else {
 						//if legal, and predefined cell, set it to that type
 						if (!(currentSquare.getType() == Square.PREDEFINE_CELL))
 							currentSquare.setType(currentSquare.getPreviousType());
 					}
 					type = currentSquare.getType(); //sets the look for various square types
 					currentBox.setIcon(getSquareIcon(currentSquare.getCurrentValue(), currentBox.hasFocus()));
 					type = currentSquare.getType();
 					
 					//Choose the background colour according to the squares and their state
 					if (type == Square.USER_INPUT_CELL){
 						currentBox.setBackground(USER_COLOR);
 					} else if (type == Square.ERROR_CELL){
 						if (currentSquare.getPreviousType() == Square.PREDEFINE_CELL){
 							currentBox.setBackground(WRONG_PRESET_COLOR);
 						} else {
 							currentBox.setBackground(WRONG_COLOR);
 						}
 					} else if (type == Square.EMPTY_CELL){
 						currentBox.setBackground(USER_COLOR);
 					} else if (type == Square.HINT_CELL){
 						currentBox.setBackground(HINT_COLOR);
 					} else if (type == Square.PREDEFINE_CELL) {
 						currentBox.setBackground(PRESET_COLOR);
 					}
 					if (currentSquare.getCurrentValue() == 0)
 						currentBox.setText(""); //if empty cell, display it as such
 				} else {
 					Integer x = 0;
 					String boxString = "";
 					while (x < 9){ //displays the draft cell
 						if (currentSquare.isMarkedDraft(x)){
 							boxString = (boxString + " " + x.toString());
 						}
 						x++;
 					}
 	
 				}
 				col++;
 			}
 			row++;
 			col = 0;
 		}
 		//checks if game has been won
 		if (hasWon()){
 			System.out.println("CONGRATULATIONS YOU WON!!!");
 			youWin();
 		}
 		//update the display
 		pane.repaint();
 
 	}
 			
 	/**
 	 * Checks if the player has won the game. This checks
 	 * if the boards if completely filled first, then for each filled
 	 * squares, checks if there are duplicates in the regions.
 	 * @return True if the board is completely filled and all values
 	 * are valid. False otherwise.
 	 */
 	public static boolean hasWon() {
 		if (viewMode == GAME) {
 			boolean boardFilled = checkBoardFilled();
 			
 			if (boardFilled) {
 				//checks if each value of the board is legal, if so, has won
 				for (int i = 0; i < Puzzle.ROW_NUMBER; i++) {
 					for (int j = 0; j < Puzzle.COLUMN_NUMBER; j++) {
 						if (LegalCheck.isNotLegal(boardLayout, boardLayout[i][0], boardLayout[i][0].getCurrentValue()))
 							return false;
 					}
 				}
 			} else
 				return false;
 			return true;
 		} else
 			return false;
 	}
 	
 	/**
 	 * Checks if the board is completely filled out.
 	 * @return True if the board is completely filled out.
 	 * False otherwise.
 	 */
 	private static boolean checkBoardFilled() {
 		for (int i = 0; i < Puzzle.ROW_NUMBER; i++) {
 			for (int j = 0; j < Puzzle.COLUMN_NUMBER; j++) {
 				if (boardLayout[i][j].getCurrentValue() == 0)
 					return false;
 			} //checks if the board doesn't have empty values
 		}
 		return true;
 	}
 	
 	/**
 	 * De-selects all squares
 	 */
 	private static void deselectAll(){
 		inputX = -1;
 		inputY = -1;
 		source = null;
 		
 		int x, y;
 		for (x = 0; x < 9; x++){
 			for (y = 0; y < 9; y++){
 				box[x][y].setIcon(getSquareIcon(boardLayout[y][x].getCurrentValue(), false));
 			}
 		}
 	}
 	
 	/**
 	 * gets the Icon for the selected number and selection
 	 * @param number	the number that is in the square (0 is empty)
 	 * @param selected	if the square needs to have a selection square around it
 	 * @return			returns the ImageIcon related to the given number and selection state
 	 */
 	private static ImageIcon getSquareIcon(int number, boolean selected){
 		if (selected){
 			return iconSelectedValue[number];
 		} else {
 			return iconValue[number];
 		}
 	}
 	
 	/**
 	 * Clears and disables the board before placing the message
 	 * "Congrats You Win!" on the centre
 	 */
 	private static void youWin(){
 		int x, y;
 		
 		//Reset and lock all squares
 		for (x = 0; x < 9; x++){
 			for (y = 0; y < 9; y++){
 				if (y != 3 && y!= 4){
 					box[x][y].setBackground(PRESET_COLOR);
 				} else {
 					box[x][y].setBackground(USER_COLOR);
 				}
 				box[x][y].setIcon(getSquareIcon(0, false));
 				box[x][y].setEnabled(false);
 
 			}
 		}
 		
 		box[0][3].setIcon(letter[C]);
 		box[1][3].setIcon(letter[O]);
 		box[2][3].setIcon(letter[N]);
 		box[3][3].setIcon(letter[G]);
 		box[4][3].setIcon(letter[R]);
 		box[5][3].setIcon(letter[A]);
 		box[6][3].setIcon(letter[T]);
 		box[7][3].setIcon(letter[S]);
 		
 		box[1][4].setIcon(letter[Y]);
 		box[2][4].setIcon(letter[O]);
 		box[3][4].setIcon(letter[U]);
 		box[5][4].setIcon(letter[W]);
 		box[6][4].setIcon(letter[I]);
 		box[7][4].setIcon(letter[N]);
 		box[8][4].setIcon(letter[EXCLAMATION]);
 	}
 		
 	private static JButton source;
 	private static int inputX;
 	private static int inputY;
 	private static Square[][] boardLayout;
 	private static Calendar startTime = Calendar.getInstance();
 	
 	//============================================================================================================================================================
 	//AAAAAAAA  CCCCCCCC  TTTTTTTTTT  IIIIII  OOOOOOOO  NN      NN        LL      IIIIII  SSSSSSSS  TTTTTTTTTT  EEEEEEEE  NN      NN  EEEEEEEE  RRRRRR    SSSSSSSS
 	//AA    AA  CC            TT        II    OO    OO  NNNN    NN        LL        II    SS            TT      EE        NNNN    NN  EE        RR    RR  SS
 	//AA    AA  CC            TT        II    OO    OO  NN  NN  NN        LL        II    SSSS          TT      EEEEEE    NN  NN  NN  EEEEEE    RRRRRR    SSSS
 	//AAAAAAAA  CC            TT        II    OO    OO  NN  NN  NN        LL        II        SSSS      TT      EE        NN  NN  NN  EE        RRRR          SSSS
 	//AA    AA  CC            TT        II    OO    OO  NN    NNNN        LL        II          SS      TT      EE        NN    NNNN  EE        RR  RR          SS
 	//AA    AA  CCCCCCCC      TT      IIIIII  OOOOOOOO  NN      NN        LLLLLL  IIIIII  SSSSSSSS      TT      EEEEEEEE  NN      NN  EEEEEEEE  RR    RR  SSSSSSSS
 	//============================================================================================================================================================
 	
 	/**
 	 * Action Listener to quit
 	 * @author Sam
 	 */
 	public static class btnQuitListener implements ActionListener{
 		/**
 		 * Function to quit when button is pressed
 		 */
 		public void actionPerformed(ActionEvent e){
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * Action Listener to provide a hint when a user desires such
 	 */
 	public static class btnHintListener implements ActionListener{
 		/**
 		 * Function to provide a hint when button is pressed
 		 */
 		public void actionPerformed(ActionEvent e){
 			if (hasWon()) //if game is won, do nothing
 				return;
 			//creates new hint system and uses it to find hint from current board
 			HintSystem h = new HintSystem();
 			Move newMove = h.Hint(boardLayout);
 			//checks to see if move is valid
 			if (newMove != null && newMove.getValue() != 0) {
 				//sets the board value to hint
 				boardLayout[newMove.getY()][newMove.getX()].setCurrentValue(newMove.getValue());
 				//set what has changed
 				inputX = newMove.getX();
 				inputY = newMove.getY();
 				source = box[inputX][inputY];
 				//sets the color of the hint cell
 				Color hintColor = new Color(102, 255, 178);
 				source.setForeground(hintColor);
 				//updates the display
 				resetSourceBox();
 			}
 		}
 	}
 	
 	/**
 	 * Action Listener for each of the 81 squares 
 	 * @author Sam
 	 */
 	public static class btnSquareListener implements ActionListener{
 		int squareX, squareY;
 		
 		/**
 		 * Constructor to create a new square listener
 		 * @param x the x coor
 		 * @param y the y coor
 		 */
 		public btnSquareListener(int x, int y){
 			squareX = x;
 			squareY = y;
 		}
 		
 		/**
 		 * Function to update input when square has been selected
 		 */
 		public void actionPerformed(ActionEvent e){
 			//source.setForeground(defaultBGColor);
 			deselectAll();
 			if (boardLayout[squareY][squareX].getType() != Square.PREDEFINE_CELL && boardLayout[squareY][squareX].getPreviousType() != Square.PREDEFINE_CELL){
 				System.out.println("square selected " + squareX + " " + squareY);
 				System.out.println(" square type is " + boardLayout[squareY][squareX].getType() + " and has value " + boardLayout[squareY][squareX].getCurrentValue());
 				source = (JButton) e.getSource(); //sets the square selection as the source
 				inputX = squareX;
 				inputY = squareY;
 	//			source.setBackground(Color.green);
 				source.setIcon(getSquareIcon(boardLayout[inputY][inputX].getCurrentValue(), true));
 				//source.setText(inputX + " " + inputY);
 				//source.setForeground(selectedBGColor);
 			}
 		}
 	}
 	
 	/**
 	 * Action Listener for checking when someone presses the New Game buttons
 	 * it then changes the screens and loads a new board.
 	 * @author Sam
 	 *
 	 */
 	public static class btnNewGameListener implements ActionListener{
 		int difficulty;
 		
 		public btnNewGameListener(int diff){
 			difficulty = diff;
 		}
 		
 		public void actionPerformed(ActionEvent e){
 			newGame(difficulty);
 			switchView();
 		}
 	}
 	
 	/**
 	 * Action Listener for when someone presses the menu button in the main board
 	 * @author Sam
 	 *
 	 */
 	public static class btnMenuListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			switchView();
 		}
 	}
 		
 	/**
 	 * Key Press listeners for typing
 	 * @author Sam
 	 */
 	public static class keyPressedListener implements KeyListener{
 		private int row;
 		private int column;
 		
 		/**
 		 * Creates a new action listener for the key
 		 * @param row x coor
 		 * @param col y coor
 		 */
 		public keyPressedListener(int row, int col){
 			this.row = row;
 			this.column = col;
 		}
 		
 		public void keyPressed(KeyEvent e) {}
 
 		public void keyReleased(KeyEvent e) {}
 
 		/**
 		 * Function to update the display when an input is received
 		 */
 		public void keyTyped(KeyEvent e) {
 			System.out.println("Key Typed " + e.getKeyChar());
 			char key = e.getKeyChar();
 			//if the square selected isn't a predefined cell
 			if ((boardLayout[row][column].getType() != Square.PREDEFINE_CELL && boardLayout[row][column].getPreviousType() != Square.PREDEFINE_CELL) && Character.isDigit(key)){
 				int number = 0;
 				// work out what number is pressed
 				if (key == '!' || key == '1'){
 					number = 1;
 				} else if (key == '@' || key == '2'){
 					number = 2;
 				} else if (key == '#' || key == '3'){
 					number = 3;
 				} else if (key == '$' || key == '4'){
 					number = 4;
 				} else if (key == '%' || key == '5'){
 					number = 5;
 				} else if (key == '^' || key == '6'){
 					number = 6;
 				} else if (key == '&' || key == '7'){
 					number = 7;
 				} else if (key == '*' || key == '8'){
 					number = 8;
 				} else if (key == '(' || key == '9'){
 					number = 9;
 				}
 				//sets the value of the square to input
 				boardLayout[row][column].setCurrentValue(number);
 				//deletes the current value depending on input
 			} else if ((boardLayout[row][column].getType() != Square.PREDEFINE_CELL && boardLayout[row][column].getPreviousType() != Square.PREDEFINE_CELL) && (e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == 0 || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE)) {
 				boardLayout[row][column].setType(Square.EMPTY_CELL);
 				boardLayout[row][column].setCurrentValue(0);
 			}
 			resetSourceBox();
 		}
 	}
 		
 	//NOTE: if you're looking for private variables, they're above the Action Listeners
 }
