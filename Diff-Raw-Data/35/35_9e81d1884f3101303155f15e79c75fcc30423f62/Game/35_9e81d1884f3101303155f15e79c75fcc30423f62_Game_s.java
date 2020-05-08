 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import java.awt.Dimension;
 
 /**
  * The default form of the robots game.
  */
 public class Game extends JPanel
 {
 	private Dimension dimension;
 	private int level;
 	private int score;
 	private int numBots;
 	private Image playerAliveImage;
 	private Image playerDeadImage;
 	private Image robotImage;
 	private Image wreckImage;
 	private Image locationImage;
 
 	/**
 	 * Used to randomly teleport the Player.
 	 */
 	protected Random generator;
 
 	/**
 	 * The width of the board.
 	 */
 	protected final int ROWS;
 
 	/**
 	 * The width of the board.
 	 */
 	protected final int COLS;
 
 
 	/**
 	 * The container of all the Locations.
 	 */
 	protected Location[][] board;
 
 	/**
 	 * The only Player on the board
 	 */
 	protected Player human;
 	/**
 	 * Getter for the board.
 	 *
 	 * @return The board.
 	 */
 	public Location[][] getBoard() { return board; }
 
 	/**
 	 * Getter for the player. There should only be 1 player (human).
 	 *
 	 * @return The player.
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
 	 * Setter for the number of robots on each new level
 	 *
 	 * @param numBots The value to set the initial number of Robots to.
 	 */
 	protected void setNumBots(int numBots)
 	{
 		this.numBots = numBots;
 	}
 
 	/**
 	 * Creates a new Game. Cals resetBoard to add the board.
 	 */
	Game()
 	{
 		super();
 		generator = new Random();
 		ROWS = 30;
 		COLS = 40;
 		resetBoard();
 		dimension = new Dimension((COLS * 20) + 10, (ROWS * 20) + 10);
 		try
 		{
 			playerAliveImage = ImageIO.read(new File("PlayerAlive.jpg"));
 			playerDeadImage = ImageIO.read(new File("PlayerDead.jpg"));
 			robotImage = ImageIO.read(new File("Robot.jpg"));
 			wreckImage = ImageIO.read(new File("Wreck.jpg"));
 			locationImage = ImageIO.read(new File("Location.jpg"));
 		}
 		catch(IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Paints the board.
	 *
	 * @deprecated This should only be called by void printBoard() (internally). 
 	 */
 	public void paint(Graphics g)
 	{
 		this.setSize(dimension);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, (ROWS * 20) + 10, (COLS * 20) + 10);
 		g.fillOval(50, 50, 20, 20);
 		Image image;
   		for(int row = 0; row < ROWS; row++)
 		{
 			for(int col = 0; col < COLS; col++)
 			{
 				if(board[row][col] instanceof Player)
 				{
 					if( ((Player)board[row][col]).isAlive())
 					{
 						image = playerAliveImage;
 					}
 					else
 					{
 						image = playerDeadImage;
 					}
 				}
 				else if(board[row][col] instanceof Robot)
 				{
 					image = robotImage;
 				}
 				else if(board[row][col] instanceof Wreck)
 				{
 					image = wreckImage;
 					
 				}
 				else
 				{
 					image = locationImage;
 				}
 				g.drawImage(image, 20 * col, 20 * row, null);
				g.setColor(Color.BLACK);
 			}	
     		}
	g.drawLine(0, 0, 0, 20 * ROWS);
	g.drawLine(0, 0, 20 * COLS, 0);
	g.drawLine(0, 20 * ROWS, 20 * COLS, 20 * ROWS);
	g.drawLine(20 * COLS, 0, 20 * COLS, 20 * ROWS);
 	}
 
 	/**
 	 * Prints the board. Calls void paint(Graphics).
 	 */
 	public void printBoard()
 	{
 		/*
 		System.out.println("\n\n\n");
 		System.out.println("--Score = " + score + (score < 10 ? '-' : ""));
 		System.out.println("----Key-----");
 		System.out.println("  1 = Robot");
 		System.out.println("  * = Wreck");
 		System.out.println("  # = Player (X = DEAD)");
 		for(int c = -1; c <= COLS; c++)
 		{
 			System.out.print('-';
 		}
 		System.out.println();
 		for(int r = 0; r < ROWS; r++)
 		{
 			System.out.print(('|');
 			for(int c = 0; c < COLS; c++)
 			{
 				System.out.print(board[r][c].value());
 			}
 			System.out.println('|');
 		}
 		for(int c = -1; c < COLS, c++)
 		{
 			System.out.print('-');
 		}
 		System.out.println();
 		*/
 		paint(this.getGraphics());
 	}
 
 	/**
 	 * Updates the board for the Player's current Location.  Runs even if the player will die in his/her current Location.
 	 */
 	protected void updateBoard()
 	{
 		Location[][] tempBoard = createBoard();
 		tempBoard[human.getRow()][human.getCol()] = human; 
 		int tempCol, tempRow, colsTo, rowsTo,  tempScore = 0;
 		for(int boardRow = 0; boardRow < ROWS; boardRow++)
 		{
 			for(int boardCol = 0; boardCol < COLS; boardCol++)
 			{
 				if(board[boardRow][boardCol] instanceof Wreck)
 				{
 					if(tempBoard[boardRow][boardCol] instanceof Robot)
 					{
 						tempScore += 1;
 						numBots -= 1;
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
 					if(tempBoard[tempRow][tempCol] instanceof Player)
 					{
 						human.die();
 
 					}
 					else if(tempBoard[tempRow][tempCol] instanceof Robot)
 					{
 						tempScore += 2;
 						numBots -= 2;
 						tempBoard[tempRow][tempCol] = new Wreck(tempRow, tempCol);
 					}
 					else if(tempBoard[tempRow][tempCol] instanceof Wreck)
 					{
 						tempScore += 1;
 						numBots -= 1;
 					}
 					else //(tempBoard[tempRow][tempCol] instanceof Location)
 					{
 						tempBoard[tempRow][tempCol] = board[boardRow][boardCol];
 					}
 				}
 			}
 		}
 		if(human.isAlive())
 		{
 			score += tempScore;	
 		}
 		board = tempBoard;
 	}
 
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
	 * Puts the robots on the board.  Should be called only for a clear board.
	 */
 	private void fillBots()
 	{
 		int row, col;
 		this.setNumBots(level + 1);
 		for(int n = 0; n < numBots; n++)
 		{
 			row = generator.nextInt(ROWS);
 			col = generator.nextInt(COLS);
 			if(!board[row][col].getClass().getName().equals( "Location"))
 			{
 				n--;
 			}
 			else
 			{
 				board[row][col] = new Robot(row, col);
 			}
 		}
 	}
 
 	/**
 	 * Increases the game level.  Called when the player beats his\her current level.  Clears the old board and adds a new number of Robots randomly positioned.  Positions the player in the middle of the board.
 	 */
 	public void increaseLevel()
 	{
 		level++;
 		human = new Player(ROWS / 2, COLS / 2, generator, ROWS, COLS);
 		board = createBoard();
 		board[ROWS / 2][COLS / 2] = human;
		this.fillBots();

 	}
 
 	/**
 	 * Resets the game. Called if the player wants to play another game.
 	 */
 	public void resetBoard()
 	{
 		score = 0;
 		level = 0;
 		this.increaseLevel();
 	}
 
 	/**
 	 * Moves the player in the specified Direction.
 	 *
 	 * @param dir The Direction to move.
 	 */
 	public void makeMove(Direction dir)
 	{
 		if(isValid(human, dir))
 		{
 			Player loc = new Player(human);
 			loc.updatePos(dir);
 			if(board[loc.getRow()][loc.getCol()].isEnemy())
 			{
 				human.die();
 			}
 			if(dir != Direction.SAME && dir != Direction.CONTINOUS)
 			{
 				board[human.getRow()][human.getCol()] = new Location(human);
 				human.updatePos(dir);
 				board[human.getRow()][human.getCol()] = human;
 			}
 			updateBoard();
 		}
 	}
 
 	/**
 	 * Sees if it is safe for the specified Location to move in the specified Direction.  If the Direction is CONTINOUS or RANDOM, true will be returned.
 	 *
 	 * @param testLocation The starting Location.
 	 * @param dir The Direction to see if is valid for the testLocation to move to.
 	 */
 	protected boolean isValid(Location testLocation, Direction dir)
 	{
 		if(dir == Direction.RANDOM || dir == Direction.CONTINOUS)
 		{
 			return true;
 		}
 		Location desiredLocation = new Location(testLocation);
 		desiredLocation = desiredLocation.updatePos(dir);
 		if(isValid(desiredLocation))
 		{
 			if(board[desiredLocation.getRow()][desiredLocation.getCol()].isEnemy())
 			{
 				return false;
 			}
 			for(Location spot : locsAround(desiredLocation))
 			{
 				if(spot instanceof Robot)
 				{
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	//Returns a list of valid locations around the specified location.
 	private Location[] locsAround(Location loc)
 	{
 		Location[] locationList = new Location[8];
 		Location testLocation;
 		int pos = 0;
 		for(Direction dir : Direction.values())
 		{
 			if(dir.ordinal() < 9 && dir != Direction.SAME)
 			{
 				testLocation = new Location(loc);
 				testLocation.updatePos(dir);
 				if(isValid(testLocation))
 				{
 					locationList[pos++] = board[testLocation.getRow()][testLocation.getCol()];
 				}
 			}
 		}
 		return locationList;
 	}
 
 	//Tells if a Location is on the board.
 	private boolean isValid(Location loc)
 	{
  		if(loc.getRow() >= 0 && loc.getRow() < ROWS && loc.getCol() >= 0 && loc.getCol() < COLS)
 		{
 			return true;
 		}
 		return false;
 	}
 }
