 package edu.neumont.learningChess.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import edu.neumont.learningChess.api.Location;
 import edu.neumont.learningChess.api.PieceType;
 import edu.neumont.learningChess.dev.DevTools;
 import edu.neumont.learningChess.model.ChessBoard;
 import edu.neumont.learningChess.model.ChessPiece;
 import edu.neumont.learningChess.model.Move;
 import edu.neumont.learningChess.model.Pawn;
 
 @SuppressWarnings("serial")
 public class BoardDisplay extends JFrame implements KeyListener, MouseListener, MouseMotionListener, IDisplay, Pawn.IPromotionListener {
 
 	public static final int N_ROWS = ChessBoard.NUMBER_OF_ROWS;
 	public static final int N_COLS = ChessBoard.NUMBER_OF_COLUMNS;
 
 	JLayeredPane layeredPane;
 	JPanel chessBoard;
 	BoardDisplayPiece chessPiece;
 	int xAdjustment;
 	int yAdjustment;
 	private static boolean SHOW_ALERT = true;
 
 	public static void setSHOW_ALERT(boolean showAlert) {
 		SHOW_ALERT = showAlert;
 	}
 
 	private ArrayList<IDisplay.IMoveHandler> moveHandlers = new ArrayList<IDisplay.IMoveHandler>();
 
 	public BoardDisplay(boolean allowListeners) {
 		this.setTitle("Chess");
 		Dimension boardSize = new Dimension(600, 600);
 
 		layeredPane = new JLayeredPane();
 		getContentPane().add(layeredPane);
 		layeredPane.setPreferredSize(boardSize);
 
 		if (allowListeners) {
 			layeredPane.addMouseListener(this);
 			layeredPane.addMouseMotionListener(this);
 			this.addKeyListener(this);
 		}
 
 		chessBoard = new JPanel();
 		layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
 		chessBoard.setLayout(new GridLayout(N_ROWS, N_COLS));
 		chessBoard.setPreferredSize(boardSize);
 		chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);
 
 		Color squareColor = Color.blue;
 
 		for (int row = 0; row < N_ROWS; row++) {
 
 			squareColor = getOtherColor(squareColor);
 
 			for (int col = 0; col < N_COLS; col++) {
 				JPanel square = new JPanel(new BorderLayout());
 				square.setBackground(squareColor);
 				chessBoard.add(square);
 
 				squareColor = getOtherColor(squareColor);
 			}
 		}
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setResizable(true);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	public void addMoveHandler(IDisplay.IMoveHandler handler) {
 		moveHandlers.add(handler);
 	}
 
 	public void removeMoveHandler(IDisplay.IMoveHandler handler) {
 		moveHandlers.remove(handler);
 	}
 
 	private boolean notifyHandlers(Move move) {
 		boolean canHandle = true;
 		for (Iterator<IDisplay.IMoveHandler> i = moveHandlers.iterator(); canHandle && i.hasNext();) {
 			IDisplay.IMoveHandler handler = i.next();
 			canHandle = handler.handleMove(move);
 		}
 		return canHandle;
 	}
 
 	public void placePiece(IDisplay.Piece piece, Location location) {
 		piece.setVisible(false);
 		JPanel panel = (JPanel) chessBoard.getComponent(getIndexFromLocation(location));
 		piece.setPieceLocation(location);
 		panel.add((Component) piece);
 		piece.setVisible(true);
 	}
 
 	public IDisplay.Piece removePiece(Location location) {
 		JPanel panel = (JPanel) chessBoard.getComponent(getIndexFromLocation(location));
 		IDisplay.Piece piece = (IDisplay.Piece) panel.getComponent(0);
 		piece.setVisible(false);
 		panel.remove(0);
 		piece.setPieceLocation(null);
 		piece.setVisible(true);
 		return piece;
 	}
 
 	private int getIndexFromLocation(Location location) {
 		return ((N_ROWS - 1 - location.getRow()) * N_COLS) + location.getColumn();
 	}
 
 	private Color getOtherColor(Color squareColor) {
 		return (squareColor == Color.white) ? Color.blue : Color.white;
 	}
 
 	public void notifyCheck(boolean isWhite) {
 		this.setTitle((isWhite ? "White's move" : "Black's move") + " check");
 		// JOptionPane.showMessageDialog(this.getParent(),
 		// ((isWhite)?"White":"Black") + " is in check");
 	}
 
 	public void notifyCheckmate(boolean isWhite) {
 		this.setTitle("Checkmate!  " + ((isWhite) ? "White" : "Black") + " wins");
		// DevTools.saveMoveHistory(); //TODO: for development only. remove
		// before deployment
 		if (SHOW_ALERT) {
 			JOptionPane.showMessageDialog(this.getParent(), "Checkmate!  " + ((isWhite) ? "White" : "Black") + " wins");
 		}
 	}
 
 	public void notifyStalemate() {
 		this.setTitle("Stalemate");
		// DevTools.saveMoveHistory(); //TODO: for development only. remove
		// before deployment
 		if (SHOW_ALERT) {
 			JOptionPane.showMessageDialog(this.getParent(), "Stalemate");
 		}
 	}
 
 	public void promptForMove(boolean isWhite) {
 		this.setTitle(isWhite ? "White's move" : "Black's move");
 	}
 
 	public void mousePressed(MouseEvent e) {
 		if (e.getButton() == MouseEvent.BUTTON1) {
 			chessPiece = null;
 			Component component = chessBoard.findComponentAt(e.getX(), e.getY());
 
 			if (component instanceof BoardDisplayPiece) {
 
 				Container parent = component.getParent();
 				Point parentLocation = parent.getLocation();
 				xAdjustment = parentLocation.x - e.getX();
 				yAdjustment = parentLocation.y - e.getY();
 				chessPiece = (BoardDisplayPiece) component;
 				chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
 				chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
 				parent.remove(0);
 				layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
 			}
 		}
 	}
 
 	public void mouseDragged(MouseEvent me) {
 		if (chessPiece != null) {
 			chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if (chessPiece != null) {
 			if (releaseIsInBounds(e)) {
 
 				chessPiece.setVisible(false);
 				Component component = chessBoard.findComponentAt(e.getX(), e.getY());
 				Container parent = (component instanceof BoardDisplayPiece) ? component.getParent() : (Container) component;
 
 				int row = (parent == null) ? -1 : N_ROWS - 1 - (e.getY() / parent.getHeight());
 				int col = (parent == null) ? -1 : e.getX() / parent.getWidth();
 				Location targetLocation = new Location(row, col);
 
 				Move move = new Move(chessPiece.getPieceLocation(), targetLocation);
 
 				chessPiece.getParent().remove(chessPiece);
 				replacePiece();
 
 				boolean canHandle = notifyHandlers(move);
 				if (!canHandle) {
 					// replacePiece();
 				}
 			} else { // release was out of bounds
 				replacePiece();
 			}
 
 			chessPiece.setVisible(true);
 			chessPiece = null;
 		}
 	}
 
 	private void replacePiece() {
 		JPanel panel = (JPanel) chessBoard.getComponent(getIndexFromLocation(chessPiece.getPieceLocation()));
 		panel.add(chessPiece);
 	}
 
 	private boolean releaseIsInBounds(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		return (x >= 0) && (x < getWidth()) && (y >= 0) && (y < getHeight());
 	}
 
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	public void mouseMoved(MouseEvent e) {
 	}
 
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public ChessPiece getPromotionPiece(Location location) {
 		JComboBox pieceTypeComboBox = new JComboBox(new Object[] { PieceType.QUEEN, PieceType.BISHOP, PieceType.ROOK, PieceType.KNIGHT });
 		pieceTypeComboBox.setSelectedIndex(0);
 		JPanel pieceSelectionPanel = new JPanel();
 		pieceSelectionPanel.setLayout(new GridLayout(2, 1, 0, 15));
 		pieceSelectionPanel.add(new JLabel("Promote pawn to :"));
 		pieceSelectionPanel.add(pieceTypeComboBox);
 		JOptionPane.showMessageDialog(null, pieceSelectionPanel, "Select Prmotion piece at location " + location, JOptionPane.INFORMATION_MESSAGE);
 		PieceType type = PieceType.valueOf(pieceTypeComboBox.getSelectedItem().toString());
 		return ChessPiece.getChessPieceFromPieceType(type, this, null);
 	}
 
 	// for debugging - saves current gamestate to a file
 	@Override
 	public void keyTyped(KeyEvent e) {
 		if (e.getKeyChar() == 's' && e.isAltDown()) {
 			DevTools.saveCurrentGameState();
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
 	public void disableClosing() {
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 	}
 }
