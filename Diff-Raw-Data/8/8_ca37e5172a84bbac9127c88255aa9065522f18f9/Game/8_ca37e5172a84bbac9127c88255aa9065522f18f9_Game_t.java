 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.Label;
 import java.awt.Panel;
 import java.io.IOException;
 import java.util.Random;
 import javax.imageio.ImageIO;
 import Pieces.*;
 
 /**
  * The default form of the robots game.
  */
 public class Game extends Panel
 {
 	private Dimension dimension;
 	private int level;
 	private int score;
 	private int numBots;
 	private int safeTeleports;
 	private final int jpegSize = 20;
 	private Image playerAliveImage;
 	private Image playerDeadImage;
 	private Image robotImage;
 	private Image wreckImage;
 	private Label levelLabel;
 	private Label scoreLabel;
 	private Label safeTeleportsLabel;
 
 	/**
 	 * Used to randomly teleport the Player.
 	 */
 	private Random generator;
 
 	/**
 	 * The width of the board.
 	 */
 	private final int ROWS;
 
 	/**
 	 * The width of the board.
 	 */
 	private final int COLS;
 
 
 	/**
 	 * The container of all the Locations.
 	 */
 	private Location[][] board;
 
 	/**
 	 * Used for updating the board with updateBoard(int, int). Is cleared on each player move.
 	 */
 	private Location[][] tempBoard;
 
 	/**
 	 * The only Player on the board
 	 */
 	private Player human;
 
 	/**
 	 * Returns the player. There should only be 1 player (human).
 	 */
 	public Player getHuman() { return human; }
 
 	/**
 	 * Getter for the level of the game.
 	 *
 	 * @return The level of the game.
 	 */
 	public int getLevel() { return level; }
 
 	/**
 	 * Getter for the score of the game.
 	 *
 	 * @return The score of the game.
 	 */
 	public int getScore() { return score; }
 
 	/**
 	 * Getter for the number of robots on the board.  A variable is returned to increase game speed. This makes the game not have an "int numBots()" to count the number of robots on the board every time it is called.
 	 *
 	 * @return The number of robots on the board.
 	 */
 	public int numBots() { return numBots; }
 
 	/**
 	 * Creates a new Game. Calls resetBoard to add the board.
 	 */
 	public Game()
 	{
 		super();
 		
 		levelLabel         = new Label("level");
 		scoreLabel         = new Label("score");
 		safeTeleportsLabel = new Label("safe");
 		generator = new Random();
 		ROWS = 30;
 		COLS = 40;
 		dimension = new Dimension(COLS * 21 + 1, ROWS * 21 + 1);
 		this.resetBoard();
 		this.initializeImages();
 		this.setBackground(Color.WHITE);
 	}
 
 	/**
 	 * Puts the robots on the board.  Same as Game's  void setNumBots(int), but the number of robots is capped at SIDE * SIDE / 2.  This prevents the Player from having no possible safe teleports because of the amount of SafeTeleports.
 	 *
 	 * @param numBots Starts at 2, increases by 1 on each level increase.
 	 */
 	private void setNumBots(int numBots)
 	{
 		if(numBots > ROWS * COLS / 2)
 		{
 			this.numBots = ROWS * COLS / 2;
 		}
 		else
 		{
 			this.numBots = numBots;
 		}
 	}
 
 	/**
 	 * Initializes the images of the painted Locations.
 	 */
 	private void initializeImages()
 	{
 		try
 		{
 			try
 			{
				playerAliveImage = ImageIO.read(this.getClass().getResource("images/PlayerAlive.jpg"));
				playerDeadImage  = ImageIO.read(this.getClass().getResource("images/PlayerDead.jpg"));
				robotImage       = ImageIO.read(this.getClass().getResource("images/Robot.jpg"));
				wreckImage       = ImageIO.read(this.getClass().getResource("images/Wreck.jpg"));
 			}
 			catch(IllegalArgumentException ex)
 			{
 				playerAliveImage = ImageIO.read(new java.io.File("images/PlayerAlive.jpg"));
 				playerDeadImage  = ImageIO.read(new java.io.File("images/PlayerDead.jpg"));
 				robotImage       = ImageIO.read(new java.io.File("images/Robot.jpg"));
 				wreckImage       = ImageIO.read(new java.io.File("images/Wreck.jpg"));
 			}
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Draws the image using DoubleBuffered graphics.
 	 * @param g Should be the Paint panels Graphics
 	 */
 	public void update(Graphics g)
 	{
 		BufferedImage lastDrawnImage = (BufferedImage)this.createImage(this.getWidth(), this.getHeight());
 		
 		//Draws the shape onto the BufferedImage
 		this.paint(lastDrawnImage.getGraphics());
 
 		//Draws the BufferedImage onto the PaintPanel
 		g.drawImage(lastDrawnImage, 0, 0, this);
 	}
 
 	/**
 	 * Paints the board to the specified graphics.
 	 */
 	public void paint(Graphics g)
 	{
 		this.setSize(dimension);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, (COLS * jpegSize) + 10, (ROWS * jpegSize) + 10);
 		g.setColor(Color.LIGHT_GRAY);
 
 		//Draws the board.
   		for(int row = 0; row < ROWS; row++)
 		{
 			for(int col = 0; col < COLS; col++)
 			{
 				g.drawLine(jpegSize * col, 0, (jpegSize * col) + 0, (jpegSize * row) + jpegSize);
 				g.drawLine(0, jpegSize * row, (jpegSize * col) + jpegSize, (jpegSize * row) + 0);
 				g.drawImage( getImage(row, col), jpegSize * col, jpegSize * row, null);
 			}	
     	}
 
 		//Draws the edges of the board.
 		g.drawLine(0, 0, 0, jpegSize * ROWS);
 		g.drawLine(0, 0, jpegSize * COLS, 0);
 		g.drawLine(0, jpegSize * ROWS, jpegSize * COLS, jpegSize * ROWS);
 		g.drawLine(jpegSize * COLS, 0, jpegSize * COLS, jpegSize * ROWS);
 		
 		//Sets the score label.
 		// TODO: This should only be done if the score changes, causing the labels text to change in its own method.
 		scoreLabel.setText("Score: " + score);
 
 
 		//Paints the labels.
 		g.setColor(Color.BLACK);
 		g.drawString(scoreLabel.getText(),         1,                           jpegSize * ROWS + ROWS / 2);
 		g.drawString(safeTeleportsLabel.getText(), jpegSize * ((COLS / 2) - 2), jpegSize * ROWS + ROWS / 2);
 		g.drawString(levelLabel.getText(),         jpegSize *  (COLS - 3),      jpegSize * ROWS + ROWS / 2);
 
 		//Tell the player to restart if he dies.
 		//TODO: show new high score, if applicable?
 		if(!human.isAlive())
 		{
 			g.setFont(new Font("serif", Font.BOLD, 32));
 			g.setColor(Color.GREEN);
 			g.drawString("SORRY, YOU LOSE.",				   jpegSize * COLS / 2 - 140, jpegSize * ROWS / 2 + jpegSize - 32);
 			g.drawString("PRESS ANY KEY TO START A NEW GAME.", jpegSize * COLS / 2 - 335, jpegSize * ROWS / 2 + jpegSize + 32);
 		}
 	}
 
 	/**
 	 * Gets the image for location in the grid.
 	 *
 	 * @param row The row of the location on the board to get the image for
 	 * @param col The column of the location on the board to get the image for.
 	 * @return The image of the board's row and column.
 	 */
 	private Image getImage(int row, int col)
 	{
 		if(board[row][col] instanceof Player)
 		{
 			if( ((Player)board[row][col]).isAlive())
 			{
 				return playerAliveImage;
 			}
 			else
 			{
 				return playerDeadImage;
 			}
 		}
 		else if(board[row][col] instanceof Robot)
 		{
 			return robotImage;
 		}
 		else if(board[row][col] instanceof Wreck)
 		{
 			return wreckImage;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * Updates the board for the Player's current Location.  Runs even if the player will die in his/her current Location.
 	 */
 	private void updateBoard()
 	{
 		int tempScore = 0, numReturned;
 		tempBoard = createBoard();
 		tempBoard[human.getRow()][human.getCol()] = human; 
 		for(int boardRow = 0; boardRow < ROWS; boardRow++)
 		{
 			for(int boardCol = 0; boardCol < COLS; boardCol++)
 			{
 				numReturned = updateBoard(boardRow, boardCol);
 				numBots -= numReturned;
 				tempScore += numReturned;
 			}
 		}
 		safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);
 		if(human.isAlive())
 		{
 			score += tempScore;	
 		}
 		board = tempBoard;
 	}
 
 	/**
 	 * Used for updating the board. Called for each location. Saves the new piece to a location on the tempBoard.  If the selected piece lands on another piece on the tempBoard, A Wreck should created in the spot with the spot's location.
 	 *
 	 * @param boardRow The row of the piece being moved.
 	 * @param boardCol The column of the piece being moved.
 	 * @return Returns 1 if the piece to move dies.  Returns 2 if lands on another piece.  Otherwise, returns 0.
 	 */
 	private int updateBoard(int boardRow, int boardCol)
 	{
 		int rowsTo, colsTo, tempRow, tempCol;
 		if(board[boardRow][boardCol] instanceof Wreck)
 		{
 			if(tempBoard[boardRow][boardCol] instanceof Robot)
 			{
 				tempBoard[boardRow][boardCol] = board[boardRow][boardCol];
 				return 1;
 			}
 			tempBoard[boardRow][boardCol] = board[boardRow][boardCol];
 		}
 		else if(board[boardRow][boardCol] instanceof Robot)
 		{
 			rowsTo = human.getRow() - boardRow;
 			colsTo = human.getCol() - boardCol;
 			if(rowsTo < 0)
 			{
 				tempRow = boardRow - 1;
 			}
 			else if(rowsTo > 0)
 			{
 				tempRow = boardRow + 1;
 			}
 			else
 			{
 				tempRow = boardRow;
 			}
 			if(colsTo < 0)
 			{
 				tempCol = boardCol - 1;
 			}
 			else if(colsTo > 0)
 			{
 				tempCol = boardCol + 1;
 			}		
 			else
 			{
 				tempCol = boardCol;
 			}
 			if(tempBoard[tempRow][tempCol] instanceof Robot)
 			{
 				tempBoard[tempRow][tempCol] = new Wreck(tempRow, tempCol);
 				return 2;
 			}
 			else if(tempBoard[tempRow][tempCol] instanceof Wreck)
 			{
 				return 1;
 			}
 			else if(tempBoard[tempRow][tempCol] instanceof Player)
 			{
 				human.die();
 			}
 			else //(tempBoard[tempRow][tempCol] instanceof Location)
 			{
 				tempBoard[tempRow][tempCol] = board[boardRow][boardCol];
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 *  Creates a new array of Locations the size of the board.  Each Location refers to its spot int the array.
 	 * 
 	 * @return A board full of locations reflecting their spots on the board.
 	 */
 	private Location[][] createBoard()
 	{
 		Location[][] temp = new Location[ROWS][COLS];
 		for(int row = 0; row < ROWS; row++)
 		{
 			for(int col = 0; col < COLS; col++)
 			{
 				temp[row][col] = new Location(row, col);
 			}
 		}
 		return temp;
 	}
 
 	/**
 	 *  Puts the robots on the board.  Should be called only for a clear board.
 	 */
 	private void fillBots()
 	{
 		int row, col;
 		this.setNumBots(level + 1);
 		for(int n = 0; n < numBots; n++)
 		{
 			row = generator.nextInt(ROWS);
 			col = generator.nextInt(COLS);
 			if(board[row][col] instanceof Robot)
 			{
 				n--;
 			}
 			else
 			{
 				board[row][col] = addEnemy(row, col, n);
 			}
 		}
 	}
 
 	/**
 	 * Decides what enemy to add.
 	 *
 	 * @param row The row to add the enemy to.
 	 * @param col The column to add the enemy to.
 	 * @param n The nth enemy being added.
 	 * @return The enemy to add to the board.  The Location should return true for isEnemy().
 	 */
 	private Location addEnemy(int row, int col, int n)
 	{
 		return new Robot(row, col);
 	}
 
 	/**
 	 * Increases the game level.  Called when the player beats his\her current level.  Clears the old board and adds a new number of Robots randomly positioned.  Positions the player in the middle of the board.
 	 */
 	public void increaseLevel()
 	{
 		level++;
 		safeTeleports++;
 		do
 		{
 			board = createBoard();
 			this.fillBots();
 		}
 		while(board[ROWS / 2][COLS / 2] instanceof Robot);
 		human = new Player(ROWS / 2, COLS / 2, ROWS, COLS);
 		board[ROWS / 2][COLS / 2] = human;
 		levelLabel.setText("Level: " + level);
 		safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);
 
 		this.repaint();
 	}
 
 	/**
 	 * Resets the game. Called if the player wants to play another game.  Updates the score and level labels.
 	 */
 	public void resetBoard()
 	{
 		score = 0;
 		level = 0;
 		safeTeleports = -1;
 		
 		scoreLabel.setText("Score: 0");
 		this.increaseLevel();
 	}
 
 	/**
 	 * Moves the player in the specified Direction.  Calls {@link #updateBoard() updateBoard}.
 	 *
 	 * @param dir The Direction to move.
 	 */
 	public void makeMove(Direction dir)
 	{
 		try{Thread.sleep(100);}catch(InterruptedException ex){ex.printStackTrace();}
 		int row = 0, col = 0;
 		if(dir == Direction.RANDOM || dir == Direction.SAFE)
 		{
 			boolean safe;
 			do
 			{
 				safe = true;
 				row = generator.nextInt(ROWS);
 				col = generator.nextInt(COLS);
 
 				//makes sure the selected location is valid
 				if(dir == Direction.SAFE && safeTeleports > 0)
 				{
 					if(board[row][col].isEnemy())
 					{
 						safe = false;
 						continue;
 					}
 					for(int r = row - 1; r <= row + 1 && safe; r++)
 					{
 						for(int c = col - 1; c <= col + 1 && safe; c++)
 						{
 							if(this.isValid(r, c) && board[r][c] instanceof Robot)
 							{
 								safe = false;
 							}
 						}
 					}
 				}
 			}
 			while(!safe && dir == Direction.SAFE && safeTeleports > 0);
 			if(dir == Direction.SAFE && safeTeleports > 0)
 			{
 				safeTeleports--;
 				safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);
 			}
 		}
 		else
 		{
 			Player loc = new Player(human);
 			loc.updatePos(dir);
 	
 			if(board[loc.getRow()][loc.getCol()].isEnemy())
 			{
 				human.die();
 			}
 		}
 		//This step is not needed if the human is not <u>PHYSICALLY</u> moving.
 		if(dir != Direction.SAME && dir != Direction.CONTINUOUS)
 		{
 			//Move the human.
 			board[human.getRow()][human.getCol()] = new Location(human);
 			if(dir == Direction.SAFE || dir == Direction.RANDOM)
 			{
 				human.updatePos(row, col);
 			}
 			else
 			{
 				human.updatePos(dir);
 			}
 			board[human.getRow()][human.getCol()] = human;
 		}
 		this.updateBoard();
 	}
 
 	/**
 	 * Tells if the specified row and column is on the board.
 	 *
 	 * @param row The row to test if is on the board.
 	 * @param col The column to test if is on the board.
 	 * @return True if the specified location is on the board. Otherwise, false.
 	 */
 	private boolean isValid(int row, int col)
 	{
  		return row >= 0 && row < ROWS && col >= 0 && col < COLS;
 	}
 }
