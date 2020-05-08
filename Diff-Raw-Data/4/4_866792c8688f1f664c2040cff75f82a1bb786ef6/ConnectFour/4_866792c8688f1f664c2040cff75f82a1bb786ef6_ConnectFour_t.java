 package edu.berkeley.gamesman.testing;
 
 import java.awt.Container;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.swing.JFrame;
 
 import edu.berkeley.gamesman.Gamesman;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.database.FileDatabase;
 import edu.berkeley.gamesman.game.Connect4;
 import edu.berkeley.gamesman.game.connect4.C4Board;
 import edu.berkeley.gamesman.util.Pair;
 
 public class ConnectFour implements MouseListener {
 	char[][] board = new char[HEIGHT][WIDTH];
 	private int[] columnHeight = new int[7];
 	private char turn = 'X';
 	private boolean compO = true;
 	private boolean compX = false;
 	private boolean win = false;
 	private Thread paintThread;
 	private DisplayFour df;
 	private Connect4 cgame;
 	private FileDatabase fd;
 	static int WIDTH = 4;
 	static int HEIGHT = 4;
 
 	public ConnectFour(DisplayFour disfour, FileDatabase db) {
 		int c, r;
 		fd = db;
 		df = disfour;
 		cgame = new Connect4(db.getConfiguration());
 		cgame.prepare();
 		for (c = 0; c < WIDTH; c++) {
 			for (r = 0; r < HEIGHT; r++) {
 				df.slots[r][c].addMouseListener(this);
 			}
 		}
 		paintThread = new Thread(df);
 		for (c = 0; c < WIDTH; c++) {
 			for (r = 0; r < HEIGHT; r++) {
 				board[r][c] = ' ';
 			}
 			columnHeight[c] = 0;
 		}
 		startCompMove();
 	}
 
 	boolean compTurn() {
 		return (turn == 'O' && compO) || (turn == 'X' && compX);
 	}
 
 	void makeMove(int move) {
 		if (columnHeight[move] >= 6 || win())
 			return;
 		board[columnHeight[move]][move] = turn;
 		if (turn == 'O')
 			turn = 'X';
 		else
 			turn = 'O';
 		columnHeight[move]++;
 		df.setBoard(copy(board));
 		paintThread.start();
 		paintThread = new Thread(df);
 		if (!win())
 			startCompMove();
 	}
 
 	private void startCompMove() {
 		if (compTurn() && !win()) {
 			Collection<Pair<String, C4Board>> moves = cgame
 					.validMoves(new C4Board(board));
 			Iterator<Pair<String, C4Board>> nextStates = moves.iterator();
 			C4Board s;
 			C4Board best = null;
 			PrimitiveValue bestOutcome = PrimitiveValue.Undecided;
 			PrimitiveValue thisOutcome;
 
 			while (nextStates.hasNext()) {
 				Pair<String, C4Board> move = nextStates.next();
 				s = move.cdr;
 				thisOutcome = fd.getRecord(cgame.stateToHash(s)).get();
 				System.out.println("Next possible move " + move.car
 						+ " for state " + cgame.stateToHash(s) + " has value "
 						+ thisOutcome);
 				if (best == null || thisOutcome.isPreferableTo(bestOutcome)) {
 					bestOutcome = thisOutcome;
 					best = s;
 				}
 			}
 			moveBySet(best.getCharBoard());
 			System.out.println("Done with startCompMove");
 		}
 	}
 
 	private void moveBySet(char[][] pos) {
 		int row, col;
 		if (pos == null)
 			System.out.println("pos is null");
 		System.out.println(Arrays.deepToString(pos));
 		for (col = 0; col < WIDTH; col++) {
 			for (row = 0; row < HEIGHT; row++) {
 				if (pos[row][col] != board[row][col])
 					break;
 			}
 			if (row < HEIGHT)
 				break;
 		}
 		makeMove(col);
 	}
 
 	char getTurn() {
 		return turn;
 	}
 
 	private boolean win() {
 		int col, row, i;
 		boolean up, right, upright, downright;
 		if (win)
 			return true;
 		for (col = 0; col < WIDTH; col++) {
 			for (row = 0; row < HEIGHT; row++) {
 				if (board[row][col] == ' ')
 					break;
 				up = row <= HEIGHT - 4;
 				right = col <= WIDTH - 4;
 				upright = up && right;
 				downright = row >= 3 && right;
 				for (i = 0; i < 4; i++) {
 					up = up && board[row + i][col] == board[row][col];
 					right = right && board[row][col + i] == board[row][col];
 					upright = upright
 							&& board[row + i][col + i] == board[row][col];
 					downright = downright
 							&& board[row - i][col + i] == board[row][col];
 				}
 				if (up || right || upright || downright) {
 					if (board[row][col] == 'O')
 						System.out.println("Black wins");
 					else
 						System.out.println("Red wins");
 					win = true;
 					paintThread.start();
 					paintThread = new Thread(df);
 					return true;
 				}
 			}
 		}
 		for (col = 0; col < WIDTH; col++) {
 			if (columnHeight[col] < HEIGHT)
 				return false;
 		}
 		return true;
 	}
 
 	private char[][] copy(char[][] b) {
 		int c, r;
 		char[][] rBoard = new char[b.length][];
 		for (r = 0; r < b.length; r++) {
 			rBoard[r] = new char[b[r].length];
 			for (c = 0; c < b[r].length; c++) {
 				rBoard[r][c] = b[r][c];
 			}
 		}
 		return rBoard;
 	}
 
 	public void mouseClicked(MouseEvent me) {
 	}
 
 	public void mousePressed(MouseEvent me) {
 	}
 
 	public void mouseReleased(MouseEvent me) {
 		Slot o = (Slot) me.getSource();
 		if (compTurn())
 			return;
 		makeMove(o.getCol());
 	}
 
 	public void mouseEntered(MouseEvent me) {
 	}
 
 	public void mouseExited(MouseEvent me) {
 	}
 
 	public static void main(String[] args) throws InstantiationException,
 			IllegalAccessException {
 		Gamesman.main(new String[] { "jobs/fc4.gprop" });
 		FileDatabase fd = new FileDatabase();
		fd.initialize("file:///tmp/fastdatabase.db", null);
 		System.out.println(fd.getRecord(BigInteger.ZERO));
 		DisplayFour df = new DisplayFour();
 		/* ConnectFour cf= */new ConnectFour(df, fd);
 		JFrame jf = new JFrame();
 		Container c = jf.getContentPane();
 		c.add(df);
 		jf.setSize(350, 300);
 		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		jf.setVisible(true);
 	}
 }
