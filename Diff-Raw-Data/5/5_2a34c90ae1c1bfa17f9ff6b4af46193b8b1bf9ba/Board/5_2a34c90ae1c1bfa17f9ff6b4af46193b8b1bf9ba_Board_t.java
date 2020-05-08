 package net.foxycorndog.tetris.board;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.foxycorndog.jfoxylib.Frame;
 import net.foxycorndog.jfoxylib.components.Button;
 import net.foxycorndog.jfoxylib.events.ButtonEvent;
 import net.foxycorndog.jfoxylib.events.ButtonListener;
 import net.foxycorndog.jfoxylib.events.KeyEvent;
 import net.foxycorndog.jfoxylib.events.KeyListener;
 import net.foxycorndog.jfoxylib.font.Font;
 import net.foxycorndog.jfoxylib.input.Keyboard;
 import net.foxycorndog.jfoxylib.network.Client;
 import net.foxycorndog.jfoxylib.network.Network;
 import net.foxycorndog.jfoxylib.network.Packet;
 import net.foxycorndog.jfoxylib.network.Server;
 import net.foxycorndog.jfoxylib.opengl.GL;
 import net.foxycorndog.tetris.Tetris;
 import net.foxycorndog.tetris.event.BoardEvent;
 import net.foxycorndog.tetris.event.BoardListener;
 import net.foxycorndog.tetris.multiplayer.GamePacket;
 
 /**
  * Class that holds the information for the Pieces in the Tetris game, as well
  * as demonstrating the interactions of the Pieces.
  * 
  * @author Jeremiah Blackburn
  * @author Braden Steffaniak
  * @since May 6, 2013 at 3:31:08 PM
  * @since v0.1
  * @version May 6, 2013 at 3:31:08 PM
  * @version v0.1
  */
 public class Board extends AbstractBoard
 {
 	private boolean						lost;
 	private	boolean						gameStarted;
 	
 	private	int							ticks;
 	private	int							lastSpeedTick;
 	
 	private	float						speedChangeAmount;
 	private	float						speedChangeFactor;
 	
 	private long						pressStartTime;
 
 	private Piece						currentPiece;
 
 	private	KeyListener					keyListener;
 
 	private	Network						network;
 	private	Client						client;
 	private	Server						server;
 	
 	private Tetris						tetris;
 
 	private ArrayList<BoardListener>	events;
 
 	/**
 	 * Instantiate the image for the Board as well as other instantiations.
 	 * 
 	 * @param width
 	 *            The number of horizontal grid spaces the Board will contain.
 	 * @param height
 	 *            The number of vertical grid spaces the Board will contain.
 	 * @param gridSpaceSize
 	 *            The size (in pixels) that each space on the Board will take
 	 *            up. eg: passing 10 would create 10x10 grid spaces across the
 	 *            board.
 	 */
 	public Board(int width, int height, int gridSpaceSize, final Tetris tetris)
 	{
 		super(width, height, gridSpaceSize);
 
 		this.tetris = tetris;
 
 		events = new ArrayList<BoardListener>();
 		
 		pressStartTime = Long.MAX_VALUE;
 
 		keyListener = new KeyListener()
 		{
 			public void keyPressed(KeyEvent event)
 			{
 				if (event.getKeyCode() == Keyboard.KEY_LEFT)
 				{
 					movePiece(currentPiece, -1, 0);
 					
 					pressStartTime = System.currentTimeMillis();
 				}
 				if (event.getKeyCode() == Keyboard.KEY_RIGHT)
 				{
 					movePiece(currentPiece, 1, 0);
 					
 					pressStartTime = System.currentTimeMillis();
 				}
 				if (event.getKeyCode() == Keyboard.KEY_UP)
 				{
 					currentPiece.rotateClockwise();
 				}
 				if (event.getKeyCode() == Keyboard.KEY_DOWN)
 				{
 					setTicksPerSecond(getTicksPerSecond() * 4);
 				}
 			}
 
 			public void keyReleased(KeyEvent event)
 			{
 				if (event.getKeyCode() == Keyboard.KEY_DOWN)
 				{
 					setTicksPerSecond(getTicksPerSecond() / 4);
 				}
 				if (event.getKeyCode() == Keyboard.KEY_LEFT)
 				{
 					if (!Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
 					{
 						pressStartTime = Long.MAX_VALUE;
 					}
 				}
 				if (event.getKeyCode() == Keyboard.KEY_RIGHT)
 				{
 					if (!Keyboard.isKeyDown(Keyboard.KEY_LEFT))
 					{
 						pressStartTime = Long.MAX_VALUE;
 					}
 				}
 			}
 
 			public void keyTyped(KeyEvent event)
 			{
 
 			}
 
 			public void keyDown(KeyEvent event)
 			{
 
 			}
 		};
 
 		Keyboard.addKeyListener(keyListener);
 
 		//setTicksPerSecond(8f);
 		
 		speedChangeFactor = 1.8f;
 		speedChangeAmount = 0.5f;
 		
 		lastSpeedTick     = 10;
 		
 		addListener(new BoardListener()
 		{
 			public void onPieceMove(BoardEvent event)
 			{
 				
 			}
 			
 			public void onLineCompleted(BoardEvent event)
 			{
 				if (network != null)
 				{
 					GamePacket packet = new GamePacket(event.getLines(), GamePacket.LINES_COMPLETED);
 					
 					network.sendPacket(packet);
 				}
 			}
 			
 			public void onGameLost(BoardEvent event)
 			{
 				GamePacket packet = new GamePacket(null, GamePacket.GAME_LOST);
 				
				if (network != null)
				{
					network.sendPacket(packet);
				}
 			}
 		});
 	}
 
 	/**
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#tick()
 	 * 
 	 * Moves a piece down
 	 * one space after half a second until the piece hits the bottom of the
 	 * board or another piece on the board.
 	 */
 	public void tick()
 	{
 		if (server != null)
 		{
 			if (server.isConnected() && !gameStarted)
 			{
 				newGame();
 				gameStarted = true;
 			}
 			else
 			{
 				
 			}
 		}
 		
 		if (!lost && gameStarted)
 		{
 			if (ticks >= lastSpeedTick * speedChangeFactor)
 			{
 				lastSpeedTick = ticks;
 				
 				setTicksPerSecond(getTicksPerSecond() + speedChangeAmount);
 			}
 			
 			if (System.currentTimeMillis() - pressStartTime >= 200)
 			{
 				if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
 				{
 					movePiece(currentPiece, -1, 0);
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
 				{
 					movePiece(currentPiece, 1, 0);
 				}
 			}
 			
 			boolean moved = movePiece(currentPiece, 0, -1);
 
 			if (!moved)
 			{
 				currentPiece.kill();
 
 				clearRows();
 
 				currentPiece = tetris.getSidebar().getNextPiece().getNextPiece();
 				tetris.getSidebar().getNextPiece().generateNextPiece();
 				
 				addPieceToCenter();
 
 				if (currentPiece.yallHitTheBottomBaby())
 				{
 					Keyboard.removeKeyListener(keyListener);
 					
 					lost = true;
 
 					for (BoardListener listener : events)
 					{
 						listener.onGameLost(null);
 					}
 					
 					quitGame();
 					Tetris.SOUND_LIBRARY.playSound("lose.wav");
 				}
 				else
 				{
 					Tetris.SOUND_LIBRARY.playSound("pop.wav");
 				}
 			}
 			
 			ticks++;
 		}
 	}
 	
 	/**
 	 * Quit the game and stop the music.
 	 */
 	public void quitGame()
 	{
 		Tetris.SOUND_LIBRARY.stopSound("music.wav");
 		
 		if (network != null)
 		{
 			network.close();
 		}
 	}
 	
 	/**
 	 * Move the specified Piece the specified amount.
 	 * 
 	 * @param piece The Piece to move.
 	 * @param dx The amount of squares to move it horizontally.
 	 * @param dy The amount of squares to move it vertically.
 	 * @return Whether it moved successfully or not.
 	 */
 	private boolean movePiece(Piece piece, int dx, int dy)
 	{
 		boolean moved = piece.move(dx, dy);
 		
 		if (moved)
 		{
 			BoardEvent event = new BoardEvent(piece.getX(), piece.getY(), piece, 0);
 			
 			for (BoardListener listener : events)
 			{
 				listener.onPieceMove(event);
 			}
 		}
 		
 		return moved;
 	}
 
 	/**
 	 * Sets lost to l.
 	 */
 	public void setLost(boolean l)
 	{
 		lost = l;
 	}
 
 	/**
 	 * @return lost. Lost is either true or false. If lost is true, the piece is
 	 *         still able to move.
 	 */
 	public boolean hasLost()
 	{
 		return lost;
 	}
 
 	/**
 	 * Coordinates use the Cartesian system.
 	 * 
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#isValid(int, int)
 	 * 
 	 * checks to see it the coordinate (x,y) is valid for the piece to
 	 * move to.
 	 */
 	public boolean isValid(int x, int y)
 	{
 		return (x >= 0 && x < getWidth()) && (y >= 0 && y < getHeight());
 	}
 
 	/**
 	 * Coordinates use the Cartesian system.
 	 * 
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#isValid(int, int) checks
 	 *      to see it the coordinate (x,y) is valid for the piece to move to.
 	 */
 	public boolean isValid(Location loc)
 	{
 		return isValid(loc.getX(), loc.getY());
 	}
 
 	/**
 	 * Looks at one row of the board at a time and checks each location in that
 	 * row to see if it has a square. If all of the locations in the row have a
 	 * square the squares are deleted.
 	 */
 	public void clearRows()
 	{
 		// if (true)return;
 		int counter = 0;
 		
 		int r       = 0;
 		
 		int lines   = 0;
 
 		while (r < getHeight())
 		{
 			counter = 0;
 
 			for (int c = 0; c < getWidth(); c++)
 			{
 				if (getPieces(new Location(c, r)).length > 0)
 				{
 					counter++;
 				}
 
 				if (counter == getWidth())
 				{
 					for (int dRow = 0; dRow < getWidth(); dRow++)
 					{
 						Location l = new Location(dRow, r);
 						
 						Piece pieces[] = getPieces(l);
 						
 						pieces[0].deleteSquare(l);
 					}
 					
 					moveSquares(0, r, getWidth(), getHeight(), 0, -1);
 					
 					lines++;
 				}
 			}
 			
 			if (counter < getWidth())
 			{
 				r++;
 			}
 		}
 		
 		if (lines > 0)
 		{
 			BoardEvent event = new BoardEvent(0, r, null, lines);
 	
 			for (BoardListener listener : events)
 			{
 				listener.onLineCompleted(event);
 			}
 			
 			Tetris.SOUND_LIBRARY.playSound("lineremoved.wav");
 		}
 	}
 
 	/**
 	 * Adds a BoardListener to the ArrayList events.
 	 * 
 	 * @param b
 	 */
 	public void addListener(BoardListener b)
 	{
 		events.add(b);
 	}
 
 	/**
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#newGame()
 	 */
 	public void newGame()
 	{
 		gameStarted = true;
 		
 //		int ind = (int)(Math.random() * getWidth());
 //		
 //		ArrayList<Location> shape = new ArrayList<Location>();
 //		
 ////		ind = 0;
 //		
 //		int i  = 0;
 //		while (i < getWidth())
 //		{
 //			if (i != ind)
 //			{
 //				shape.add(new Location(i, 0));
 //			}
 //			
 //			i++;
 //		}
 //		
 //		currentPiece = new Piece(shape, new Color(100, 100, 100));
 		currentPiece = Piece.getRandomPiece();
 //		currentPiece = new Piece(2);
 		
 		addPieceToCenter();
 		
 		Tetris.SOUND_LIBRARY.playSound("pop.wav");
 		Tetris.SOUND_LIBRARY.loopSound("music.wav");
 		
 //		setTicksPerSecond(4);
 		setTicksPerSecond(4);
 	}
 
 	public void addPieceToCenter()
 	{
 		addPiece(currentPiece, getWidth() / 2 - Math.round(currentPiece.getWidth() / 2), getHeight() - currentPiece.getHeight());
 	}
 	
 	/**
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#addPiece(net.foxycorndog.tetris.board.Piece,
 	 *      int, int)
 	 */
 	public void addPiece(Piece piece, int x, int y)
 	{
 		piece.setBoard(this);
 		piece.setLocation(x, y);
 
 		getPieces().add(piece);
 	}
 	
 	/**
 	 * Moves all of the squares within the given rectangle specifications
 	 * the specified amount.
 	 * 
 	 * @param x The horizontal start of the bounds of the rectangle of the
 	 * 		squares to move.
 	 * @param y The vertical start of the bounds of the rectangle of the
 	 * 		squares to move.
 	 * @param width The width of the bounds of the rectangle of the
 	 * 		squares to move.
 	 * @param height The height of the bounds of the rectangles of the
 	 * 		squares to move.
 	 * @param dx The horizontal amount to move the squares.
 	 * @param dy The vertical amount to move the squares.
 	 */
 	private void moveSquares(int x, int y, int width, int height, int dx, int dy)
 	{
 		x = x < 0 ? 0 : x;
 		y = y < 0 ? 0 : y;
 		x = x >= getWidth()  ? getWidth()  - 1 : x;
 		y = y >= getHeight() ? getHeight() - 1 : y;
 		
 		width  = width  > getWidth()  ? getWidth()  : width;
 		height = height > getHeight() ? getHeight() : height;
 		
 		ArrayList<Piece>    ps   = new ArrayList<Piece>();
 		ArrayList<Location> locs = new ArrayList<Location>();
 		
 		for (int y2 = y; y2 < height; y2++)
 		{
 			for (int x2 = x; x2 < width; x2++)
 			{
 				Location l = new Location(x2, y2);
 				
 				Piece pieces[] = getPieces(l);
 				
 				if (pieces.length > 0)
 				{
 					locs.add(l);
 					ps.add(pieces[0]);
 					
 //					pieces[0].moveSquare(l, new Location(dx, dy));
 				}
 			}
 		}
 		
 		for (int i = 0; i < ps.size(); i++)
 		{
 			Location loc = locs.get(i);
 			
 			ps.get(i).moveSquare(loc, new Location(dx, dy));
 		}
 	}
 	
 	/**
 	 * Adds the specified number of straight lines to the bottom of the
 	 * Board. The straight lines are full of squares, except for one
 	 * spot.
 	 */
 	public void addStraightLines(int numLines)
 	{
 		moveSquares(0, 0, getWidth(), getHeight(), 0, numLines);
 		
 		for (int n = 0; n < numLines; n++)
 		{
 			int ind = (int)(Math.random() * getWidth());
 			
 			ArrayList<Location> shape = new ArrayList<Location>();
 			
 	//		ind = 0;
 			
 			int i  = 0;
 			while (i < getWidth())
 			{
 				if (i != ind)
 				{
 					shape.add(new Location(i, 0));
 				}
 				
 				i++;
 			}
 			
 			Piece newPiece = new Piece(shape, new Color(100, 100, 100));
 			
 			addPiece(newPiece, 0, n);
 		}
 	}
 
 	/**
 	 * Connect the Board game to the Client.
 	 * 
 	 * @param ip The IP of the Server to connect to.
 	 * @param port The port of the Server to connect to.
 	 */
 	public void connectClient(String ip, int port)
 	{
 		client = new Client(ip, port)
 		{
 			public void onReceivedPacket(Packet packet)
 			{
 				tetris.addPacketToQueue(packet);
 			}
 		};
 		
 		setClient(client);
 		
 		new Thread()
 		{
 			public void run()
 			{
 				client.connect();
 				
 				newGame();
 			}
 		}.start();
 	}
 
 	/**
 	 * Set the Client that the Board should use.
 	 * 
 	 * @param client The Client to use.
 	 */
 	public void setClient(Client client)
 	{
 		network = client;
 		
 		if (client.isConnected())
 		{
 			newGame();
 		}
 	}
 	
 	/**
 	 * Create a server for Clients to connect to.
 	 * 
 	 * @param port The port to create the Server on.
 	 */
 	public void createServer(int port)
 	{
 		gameStarted = false;
 		
 		server = new Server(port)
 		{
 			public void onReceivedPacket(Packet packet)
 			{
 				tetris.addPacketToQueue(packet);
 			}
 		};
 		
 		network = server;
 		
 		new Thread()
 		{
 			public void run()
 			{
 				server.create();
 				
 //				server.sendPacket(new Packet(null, 33));
 			}
 		}.start();
 	}
 	
 	/**
 	 * Render the back Button.
 	 * 
 	 * @see net.foxycorndog.tetris.board.AbstractBoard#render()
 	 */
 	public void render()
 	{
 		if (server != null)
 		{
 			if (server.isConnected())
 			{
 				
 			}
 			else
 			{
 				GL.setColor(0, 0, 0, 1);
 				Tetris.getFont().render("Waiting for\nconnection.", 0, 0, 2, 1, Font.CENTER, Font.CENTER, null);
 
 				GL.setColor(1, 1, 1, 1);
 				Tetris.getFont().render("Waiting for\nconnection.", 2, 2, 2, 1, Font.CENTER, Font.CENTER, null);
 				
 				GL.setColor(0.5f, 0.5f, 0.5f, 1);
 			}
 		}
 		
 		super.render();
 		
 		GL.pushMatrix();
 		{
 			GL.scale(0.75f, 0.75f, 1);
 		}
 		GL.popMatrix();
 	}
 }
