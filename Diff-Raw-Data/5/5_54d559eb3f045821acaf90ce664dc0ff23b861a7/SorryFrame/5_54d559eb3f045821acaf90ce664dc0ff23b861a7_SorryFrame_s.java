 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.SampleModel;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  * Extension of JFrame used to contain necessary menus and the Sorry board game.
  * 
  * @author sturgedl. Created Mar 25, 2013.
  */
 public class SorryFrame extends JFrame implements ActionListener {
 	private static final int BOARD_WIDTH = 1000;
 	private static final int BOARD_HEIGHT = 1000;
 	private static final int BOARD_ROWS = 16;
 	private static final int BOARD_COLS = 16;
 	private static final double CELL_WIDTH = ((double) BOARD_WIDTH / BOARD_COLS);
 	private static final double CELL_HEIGHT = ((double) BOARD_HEIGHT / BOARD_ROWS);
 	private static final int FRAME_X_PAD = 10;
 	private static final int FRAME_Y_PAD = 30;
 
 	private volatile int clickCount = 0;
 	private volatile ArrayList<Coordinate> clicks = new ArrayList<Coordinate>();
 
 	private static final long serialVersionUID = 1L;
 	private BoardList board;
 	private Engine engine;
 	private Card currentCard;
 
 	/*
 	 * Indices 0-3 are red, Indices 4-7 are blue, Indices 8-11 are Yellow,
 	 * Indices 12-15 are green
 	 */
 	/**
 	 * Basic constructor. Does what it does.
 	 * 
 	 * @param board
 	 * @param engine
 	 */
 	public SorryFrame(BoardList board, Engine engine) {
 		super("Sorry!");
 		this.board = board;
 		this.engine = engine;
 		this.setSize(1000, 1000);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JComponent displayBoard = new DisplayableBoard(this.board);
 		this.add(displayBoard);
 		// displayBoard.setSize(width, height)
 		this.setVisible(true);
 		this.repaint();
 		this.addMouseListener(new BoardMouseListener(this));
 		this.insertTestPlayers();
 		this.initiateTurn();
 	}
 
 	private void insertTestPlayers() {
 		this.engine.insertPlayer(new Player(Piece.COLOR.red, "Hugh Hefner"));
 		this.engine
 				.insertPlayer(new Player(Piece.COLOR.blue, "Amanda Streich"));
		this.engine.insertPlayer(new Player(Piece.COLOR.green, "Britany Nola"));
 		this.engine
				.insertPlayer(new Player(Piece.COLOR.yellow, "Pamela Horton"));
 	}
 
 	/**
 	 * Given an (x, y) tuple of doubles, will return an appropriate board grid
 	 * coordinate of integers.
 	 * 
 	 * @param x
 	 * @param y
 	 * 
 	 * @return Coordinate, position on board corresponding to x and y
 	 */
 	public static Coordinate convertClickToCoordinate(double x, double y) {
 		if (x > BOARD_WIDTH || x < 0)
 			throw new CoordinateOffOfBoardException("Bad location: x = " + x
 					+ " y = " + y);
 
 		if (y > BOARD_HEIGHT || y < 0)
 			throw new CoordinateOffOfBoardException("Bad location: x = " + x
 					+ " y = " + y);
 
 		int xCoord = (int) Math.floor((x / CELL_WIDTH));
 		int yCoord = (int) Math.floor((y / CELL_HEIGHT));
 
 		return new Coordinate(xCoord, yCoord);
 	}
 
 	private void awaitUserInteraction() {
 		while (this.clickCount < 2)
 			// wait for it
 			;
 		System.out.println("got enough clicks");
 	}
 
 	/**
 	 * 
 	 * Asks engine for a card, displays that card. Instructs engine to swap
 	 * active player. Begins listening to mouse input.
 	 * 
 	 */
 	private void initiateTurn() {
 		this.currentCard = this.engine.getNextCard();
 		System.out.println(this.currentCard.toString());
 		this.engine.rotatePlayers();
 		this.notifyPlayer();
 		this.resetClickDetection();
 		this.awaitUserInteraction();
 		this.performTurn();
 	}
 
 	/**
 	 * 
 	 * Waits for mouse input, converts them to coords. Relays coords to engine,
 	 * checking if the move was legal and if the turn should end. Checks for
 	 * turn forfeit, reverts the board if so. If turn is done, finalizes turn in
 	 * engine. Initiates next player's turn.
 	 * 
 	 */
 	private void performTurn() {
 		int result = this.engine.pawnMove(this.clicks.get(0),
 				this.clicks.get(1));
 		if (result < 0) {
 			// movement failed, alert player... blah blah blah
 		} else if (result == Engine.SAME_NODE_SELECTED) {
 			this.resetClickDetection();
 			this.notifyPlayer();
 			this.awaitUserInteraction();
 			this.performTurn();
 		} else if (result == Engine.INVALID_MOVE) {
 			this.resetClickDetection();
 			this.notifyPlayer();
 			this.awaitUserInteraction();
 			this.performTurn();
 		} else if (result == Engine.NODE_NOT_FOUND) {
 			this.resetClickDetection();
 			this.notifyPlayer();
 			this.awaitUserInteraction();
 			this.performTurn();
 
 		} else {
 			if (this.currentCard.cardNum == result) {
 				// turn is over, rotate
 				this.engine.finalizeTurn();
 				this.repaint();
 				this.initiateTurn();
 			} else {
 				// player had a 7, let them go again
 				this.resetClickDetection();
 				this.awaitUserInteraction();
 				this.notifyPlayer();
 				this.performTurn();
 			}
 		}
 
 	}
 
 	private void resetClickDetection() {
 		this.clicks.clear();
 		this.clickCount = 0;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void registerMouseClick(Coordinate coord) {
 		this.clickCount++;
 		this.clicks.add(coord);
 		System.out.println("The node number: " + Engine.getNodePosition(coord));
 	}
 
 	private void notifyPlayer() {
 		JOptionPane.showMessageDialog(this, this.engine.activePlayer.getName()
 				+ " it is your turn");
 	}
 
 	/**
 	 * Container class for mouse-click coordinates. Really just to provide
 	 * convenience, because Java is really horrible at dealing with multiple
 	 * return values. If this was a nice language like Python or Scheme or
 	 * really almost anything else then I could just return a tuple but because
 	 * it's Java and whatnot I have to write an entire freaking class just to
 	 * conveniently return 2 integers. So yeah. It contains 2 integers.
 	 * 
 	 * @author sturgedl. Created Mar 24, 2013.
 	 */
 	protected static class Coordinate {
 		private static final int HASH_BROWNS = 17;
 		private static final int SALT = 113;
 		private int x;
 		private int y;
 
 		/**
 		 * Makes a coordinate. Don't be stupid.
 		 * 
 		 * @param x
 		 * @param y
 		 */
 		public Coordinate(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@SuppressWarnings("javadoc")
 		public int getX() {
 			return this.x;
 		}
 
 		@SuppressWarnings("javadoc")
 		public int getY() {
 			return this.y;
 		}
 
 		@SuppressWarnings("javadoc")
 		public void setX(int x) {
 			this.x = x;
 		}
 
 		@SuppressWarnings("javadoc")
 		public void setY(int y) {
 			this.y = y;
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (o instanceof Coordinate)
 				return this.equals((Coordinate) o);
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			return this.x * HASH_BROWNS + this.y * SALT;
 		}
 
 		/**
 		 * Checks if 2 coordinates are equal, based on their x and y values.
 		 * 
 		 * @param c
 		 * @return true if equal... duh
 		 */
 		public boolean equals(Coordinate c) {
 			return this.x == c.x && this.y == c.y;
 		}
 
 	}
 
 	/**
 	 * Class for the Mouse listener used on the game board.
 	 * 
 	 * @author sturgedl. Created Mar 25, 2013.
 	 */
 	protected class BoardMouseListener implements MouseListener {
 		private SorryFrame myFrame;
 
 		/**
 		 * Basic MouseListener constructor, takes a frame to interact with. Uses
 		 * the frame to register mouse clicks upon.
 		 * 
 		 * @param frame
 		 */
 		public BoardMouseListener(SorryFrame frame) {
 			this.myFrame = frame;
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent click) {
 			System.out.println("Mouse click registerd! x: " + click.getX()
 					+ " y: " + click.getY());
 			try {
 				this.myFrame.registerMouseClick(SorryFrame
 						.convertClickToCoordinate(click.getX() - FRAME_X_PAD,
 								click.getY() - FRAME_Y_PAD));
 			} catch (CoordinateOffOfBoardException e) {
 				System.out.println("Got an exception.");
 			}
 
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 	}
 
 }
