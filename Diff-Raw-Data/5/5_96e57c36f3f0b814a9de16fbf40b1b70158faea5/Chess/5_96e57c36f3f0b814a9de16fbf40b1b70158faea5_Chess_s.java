 package Logic;
 
 import Accessories.Log;
 import Pieces.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.*;
 import static javax.swing.JOptionPane.*;
 
 /**
  * Provides the classes necessary to create the chessboard and moving the chess
  * pieces around on it. It`s more or less the heart of the chess game.<p> - It
  * includes the special moves <br /> - Updating of the log of which pieces that
  * are taken <br /> - Update of the who`s turn it is <br /> - Mouse listeners
  * methods <br /> - Different methods for locating pieces <br /> - Methods for
  * coloring the chessboard and clearing the chessboard <br /> - Method for
  * changing the labels of the pieces <br /> - Method for sending information to
  * the chess table and getting information from the chess table <br /> - Method
  * for refreshing the chessboard <br /> - Method for changing turn between
  * players <br /> - Method for the special chess moves <br /> - The class
  * includes a lot of instance variables, which means it also includes a lot of
  * get and set-methods
  *
  * @author andreaskalstad
  */
 public class Chess extends JInternalFrame implements MouseListener, MouseMotionListener {
 
     private int colorSquareW;
     private int colorSquareB;
     private boolean castling = false;
     private boolean passanten;
     private int enPassantB;
     private int enPassantW;
     private Point enPassantPW;
     private Point enPassantPB;
     private java.util.List _listeners = new ArrayList();
     private Point startPos;
     private int xAdjustment;
     private int yAdjustment;
     private int turn = 2;
     private int team;
     private Log blackLog = new Log();
     private Log whiteLog = new Log();
     private Coordinates kord = new Coordinates();
     private ChessTable chessTable = new ChessTable();
     private JLayeredPane layeredPane;
     private JLabel chessBoard;
     private PieceLabel chessPiece;
     private PieceLabel piece;
     private boolean meme = false;
     private Icon hjelpIkon;
     private Icon lolW = new ImageIcon(getClass().getResource("/Logic/Pictures/nyancat2.gif"));
     private Icon lolB = new ImageIcon(getClass().getResource("/Logic/Pictures/nyancat3.gif"));
     private PawnB pawnB = new Pieces.PawnB(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnB.png")));
     private PawnW pawnW = new Pieces.PawnW(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnW.png")));
     private RookB rookB = new Pieces.RookB(new ImageIcon(getClass().getResource("/Logic/Pictures/RookB.png")));
     private RookW rookW = new Pieces.RookW(new ImageIcon(getClass().getResource("/Logic/Pictures/RookW.png")));
     private RookB rookBright = rookB;
     private RookB rookBleft = rookB;
     private RookW rookWleft = rookW;
     private RookW rookWright = rookW;
     private KnightB knightB = new Pieces.KnightB(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightB.png")));
     private KnightW knightW = new Pieces.KnightW(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightW.png")));
     private BishopB bishopB = new Pieces.BishopB(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopB.png")));
     private BishopW bishopW = new Pieces.BishopW(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopW.png")));
     private QueenB queenB = new Pieces.QueenB(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenB.png")));
     private QueenW queenW = new Pieces.QueenW(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenW.png")));
     private KingB kingB = new Pieces.KingB(new ImageIcon(getClass().getResource("/Logic/Pictures/KingB.png")));
     private KingW kingW = new Pieces.KingW(new ImageIcon(getClass().getResource("/Logic/Pictures/KingW.png")));
     private int playerTeam;
 
     /**
      *
      * @param team The player controlled team, 0 for white, 1 for black, 2 for
      * both.
      */
     public Chess(int team) {
 
         this.playerTeam = team;
 
         Dimension boardSize = new Dimension(600, 600);
 
         setVisible(true);
         setLocation(100, 100);
         setTitle("");
         setResizable(false);
         setClosable(false);
         setIconifiable(false);
         setMaximizable(false);
         setBorder(null);
         setRootPaneCheckingEnabled(false);
         javax.swing.plaf.InternalFrameUI ifu = getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI) ifu).setNorthPane(null);
 
         layeredPane = new JLayeredPane();
         getContentPane().add(layeredPane);
         layeredPane.setPreferredSize(boardSize);
         layeredPane.addMouseListener(this);
         layeredPane.addMouseMotionListener(this);
 
         chessBoard = new JLabel(new ImageIcon(getClass().getResource("/Logic/Pictures/Chessboard.png")));
         layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
         chessBoard.setLayout(new GridLayout(8, 8));
         chessBoard.setPreferredSize(boardSize);
         chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);
 
         for (int i = 0; i < 64; i++) {
             JPanel square = new JPanel(new BorderLayout());
             square.setOpaque(false);
             chessBoard.add(square);
         }
 
         //Adding pieces to the board
         for (int i = 0; i < 8; i++) {
             PieceLabel test = new PieceLabel(pawnB.getIcon(), pawnB);
             JPanel panel = (JPanel) chessBoard.getComponent(8 + i);
             panel.add(test);
         }
         for (int i = 0; i < 8; i++) {
             PieceLabel piece = new PieceLabel(pawnW.getIcon(), pawnW);
             JPanel panel = (JPanel) chessBoard.getComponent(48 + i);
             panel.add(piece);
         }
         PieceLabel test = new PieceLabel(rookBleft.getIcon(), rookBleft);
         JPanel panel = (JPanel) chessBoard.getComponent(0);
         panel.add(test);
         test = new PieceLabel(rookBright.getIcon(), rookBright);
         panel = (JPanel) chessBoard.getComponent(7);
         panel.add(test);
         test = new PieceLabel(rookWright.getIcon(), rookWright);
         panel = (JPanel) chessBoard.getComponent(63);
         panel.add(test);
         test = new PieceLabel(rookWleft.getIcon(), rookWleft);
         panel = (JPanel) chessBoard.getComponent(56);
         panel.add(test);
         test = new PieceLabel(knightB.getIcon(), knightB);
         panel = (JPanel) chessBoard.getComponent(1);
         panel.add(test);
         test = new PieceLabel(knightB.getIcon(), knightB);
         panel = (JPanel) chessBoard.getComponent(6);
         panel.add(test);
         test = new PieceLabel(knightW.getIcon(), knightW);
         panel = (JPanel) chessBoard.getComponent(62);
         panel.add(test);
         test = new PieceLabel(knightW.getIcon(), knightW);
         panel = (JPanel) chessBoard.getComponent(57);
         panel.add(test);
         test = new PieceLabel(bishopB.getIcon(), bishopB);
         panel = (JPanel) chessBoard.getComponent(5);
         panel.add(test);
         test = new PieceLabel(bishopB.getIcon(), bishopB);
         panel = (JPanel) chessBoard.getComponent(2);
         panel.add(test);
         test = new PieceLabel(bishopW.getIcon(), bishopW);
         panel = (JPanel) chessBoard.getComponent(61);
         panel.add(test);
         test = new PieceLabel(bishopW.getIcon(), bishopW);
         panel = (JPanel) chessBoard.getComponent(58);
         panel.add(test);
         test = new PieceLabel(queenB.getIcon(), queenB);
         panel = (JPanel) chessBoard.getComponent(3);
         panel.add(test);
         test = new PieceLabel(queenW.getIcon(), queenW);
         panel = (JPanel) chessBoard.getComponent(59);
         panel.add(test);
         test = new PieceLabel(kingB.getIcon(), kingB);
         panel = (JPanel) chessBoard.getComponent(4);
         panel.add(test);
         test = new PieceLabel(kingW.getIcon(), kingW);
         panel = (JPanel) chessBoard.getComponent(60);
         panel.add(test);
         toTable();
     }
 
     /**
      * Checks if theres a piece on the clicked position on the chessboard.
      *
      * @param e MouseEvent
      */
     @Override
     public void mousePressed(MouseEvent e) {
         if(e.getButton() == 3){
             chessPiece = null;
         }
         if (e.getButton() == 1) {
             toTable();
             try {
                 chessPiece = null;
                 Component c = chessBoard.findComponentAt(e.getX(), e.getY());
 
                 if (c instanceof JPanel) {
                     return;
                 }
 
                 Point parentLocation = c.getParent().getLocation();
                 xAdjustment = parentLocation.x - e.getX();
                 yAdjustment = parentLocation.y - e.getY();
                 chessPiece = (PieceLabel) c;
                 chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
                 chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
                 layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
                 hjelpIkon = chessPiece.getIcon(); //Hjelpevariabel for midlertidig ikon funksjon
                 startPos = chessPiece.getLocation();
                 if ((c instanceof PieceLabel && playerTeam == turn % 2) || c instanceof PieceLabel && playerTeam == 2) {
                     PieceLabel d = (PieceLabel) c;
                     if (d.getPiece().getTeam() == 1 && turn % 2 == 0) {
                         colorSpecialSquares(chessTable.colorSpecialMoves(kord.getIndex(startPos), chessPiece.getPiece(), rookWleft.move(), rookWright.move(), kingW.move(), pawnB.getPassant(), kord.getIndex(enPassantPB)));
                         colorSquares(chessTable.colorMoves(kord.getIndex(startPos), chessPiece.getPiece()));
                     }
                     if (d.getPiece().getTeam() == 2 && turn % 2 == 1) {
                         colorSpecialSquares(chessTable.colorSpecialMoves(kord.getIndex(startPos), chessPiece.getPiece(), rookBleft.move(), rookBright.move(), kingB.move(), pawnW.getPassant(), kord.getIndex(enPassantPW)));
                         colorSquares(chessTable.colorMoves(kord.getIndex(startPos), chessPiece.getPiece()));
                     }
                 }
                 repaint();
             } catch (NullPointerException npe) {
                 System.out.println("Nullpointer Mousepressed");
             }
         }
     }
 
     /**
      * Moves the Piece around
      *
      * @param me MouseEvent
      */
     @Override
     public void mouseDragged(MouseEvent me) {
         if (chessPiece == null) {
             return;
         }
                  
         chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
 //        temporary meme icon while moving.
         if (meme) {
             if (chessPiece.getPiece().getTeam() == 1) {
                 chessPiece.setIcon(lolW);
             }
             if (chessPiece.getPiece().getTeam() == 2) {
                 chessPiece.setIcon(lolB);
             }
         }
     }
 
     /**
      * Drops the piece back on the board if a legal move.
      *
      * @param e MouseEvent
      */
     @Override
     public void mouseReleased(MouseEvent e) {
         if (e.getButton() == 1) {
             if (playerTeam != 2) {
                 if (playerTeam != turn % 2) {
                     if (chessPiece != null) {
                         moveBack();
                     }
                     return;
                 }
             }
             try {
                 if (chessBoard.findComponentAt(e.getX(), e.getY()) instanceof PieceLabel) {
                     piece = (PieceLabel) chessBoard.findComponentAt(e.getX(), e.getY());
                 } else {
                     piece = null;
                 }
                 if (chessPiece == null) {
                     return;
                 }
                 //checks white or black turn.
                 if ((chessPiece.getPiece().getTeam() == 2 && turn % 2 == 0) || (chessPiece.getPiece().getTeam() == 1 && turn % 2 == 1)) {
                     moveBack();
                     return;
                 }
                 if (meme) {
                     chessPiece.setIcon(hjelpIkon); //sets icon back to original after moving
                 }
                 Component m = chessBoard.findComponentAt(e.getX(), e.getY());
                 Point b;
                 if (m instanceof JPanel) {
                     b = m.getLocation();
                 } else {
                     b = m.getParent().getLocation();
                 }
                 if (m instanceof PieceLabel) {
                     piece = (PieceLabel) chessBoard.findComponentAt(e.getX(), e.getY());
                 }
                 xAdjustment = b.x - e.getX();
                 yAdjustment = b.y - e.getY();
                 //checks if the piece is dropped on a blank square or another piece and what team that piece belongs to.
                 if (m instanceof PieceLabel) {
                     team = piece.getPiece().getTeam();
                 }
                 if (m instanceof JPanel) {
                     team = 0;
                 }
                 if (movepiece(e, m)) {
                     turnChange(e);
                 }
                 cleanBoardColor();
                 if (chessTable.checkB(kingBpos())) {
                     colorSquare(kingBpos());
                     colorSquareB = kingBpos();
                 } else {
                     blankSquare(colorSquareB);
                 }
                 if (chessTable.checkW(kingWpos())) {
                     colorSquare(kingWpos());
                     colorSquareW = kingWpos();
                 } else {
                     blankSquare(colorSquareW);
                 }
                 refresh();
                 toTable();
                 passanten = false;
 
             } catch (NullPointerException npe) {
                 System.out.println("Nullpointer MouseReleased");
                 moveBack();
                 cleanMoveColors();
             }
         }
     }
 
     /**
      * Checks what type of piece that is being moved around and whether or not
      * it is dropped on a legal square.
      *
      * @param e MouseEvent
      * @param m Component, in this case a PieceLabel
      * @return True if a legal move, false if not
      */
     public boolean movepiece(MouseEvent e, Component m) {
         boolean moved = false;
         try {
             if (chessPiece.getPiece().equals(queenW)) {
                 if (moveQueenW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
 
             if (chessPiece.getPiece().equals(queenB)) {
                 if (moveQueenB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(knightB)) {
                 if (moveKnightB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(knightW)) {
                 if (moveKnightW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(kingW)) {
                 if (moveKingW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
 
             if (chessPiece.getPiece().equals(kingB)) {
                 if (moveKingB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(bishopW)) {
                 if (moveBishopW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(bishopB)) {
                 if (moveBishopB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(rookW)) {
                 if (moveRookW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(rookB)) {
                 if (moveRookB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(pawnB)) {
                 if (movePawnB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece() instanceof PawnW) {
                 if (movePawnW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (enPassantPW != null) {
                 if ((enPassantPW.getY() == (e.getY() + yAdjustment)) && (enPassantPW.getX() == (e.getX() + xAdjustment))) {
                     if (enPassant() == true) {
                         moved = true;
                         pawnB.setPassant(false);
                         passanten = true;
                     }
                 }
             }
             if (enPassantPB != null) {
                 if ((enPassantPB.getY() == (e.getY() + yAdjustment)) && (enPassantPB.getX() == (e.getX() + xAdjustment))) {
                     if (enPassant() == true) {
                         moved = true;
                         pawnW.setPassant(false);
                         passanten = true;
                     }
                 }
             }
             chessPiece.setVisible(true);
            if (chessPiece.getPiece().getTeam() == 1 && chessTable.checkW(kingWpos()) == true) {
                 if (piece instanceof PieceLabel && piece.getPiece().getTeam() != 1) {
                     replacePiece(e, piece);
                 }
                 moveBack();
                 return false;
             }
            if (chessPiece.getPiece().getTeam() == 2 && chessTable.checkB(kingBpos()) == true) {
                 if (piece instanceof PieceLabel && piece.getPiece().getTeam() != 2) {
                     replacePiece(e, piece);
                 }
                 moveBack();
                 return false;
             }
 
         } catch (NullPointerException npe) {
             System.out.println("Nullpointer MovePiece");
             moveBack();
             return false;
         }
         if (moved) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Moves the current piece to the square indicated by the mouse.
      *
      * @param e MouseEvent
      */
     public void move(MouseEvent e) {
         chessPiece.setVisible(false);
         Component c = chessBoard.findComponentAt(e.getX(), e.getY());
 
         if (c instanceof PieceLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
         }
         chessPiece.setVisible(true);
         toTable();
     }
 
     /**
      * Updates the log and turn indicators in addition to firing a chess event.
      *
      * @param e MouseEvent
      */
     public void turnChange(MouseEvent e) {
         if (chessPiece.getPiece().getTeam() == 1) {
             if (castling) {
                 whiteLog.addToLog("Castling");
             } else {
                 whiteLog.addToLog(chessPiece.getPiece().getName() + " from " + kord.getCoord(startPos) + " to " + kord.getCoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
             }
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             if (castling) {
                 blackLog.addToLog("Castling");
             } else {
                 blackLog.addToLog(chessPiece.getPiece().getName() + " from " + kord.getCoord(startPos) + " to " + kord.getCoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
             }
         }
         turn++;
         castling = false;
         fireChessEvent();
 
     }
 
     /**
      * Moves the piece back to where it was picked up from.
      */
     public void moveBack() {
         //flytter piecen tilbake dit den ble plukket opp
         chessPiece.setVisible(false);
         chessPiece.setIcon(hjelpIkon);
         Component c = chessBoard.findComponentAt(startPos);
 
         if (c instanceof PieceLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
         }
         chessPiece.setVisible(true);
         toTable();
     }
 
     /**
      * Finds the index value of the Black king.
      *
      * @return index position of the black king.
      */
     public int kingBpos() {
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPoint(i)) instanceof PieceLabel) {
                 PieceLabel c = (PieceLabel) chessBoard.findComponentAt(kord.getPoint(i));
                 if (c.getPiece() instanceof KingB) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /**
      * Finds the index value of the white king.
      *
      * @return index position of the white king.
      */
     public int kingWpos() {
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPoint(i)) instanceof PieceLabel) {
                 PieceLabel c = (PieceLabel) chessBoard.findComponentAt(kord.getPoint(i));
                 if (c.getPiece() instanceof KingW) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /**
      * Identifies the piece at a given point.
      *
      * @param a Point
      * @return Returns a Piece if the given point contains a piece, returns null
      * if a blank square.
      */
     public Piece pieceType(Point a) {
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof PieceLabel) {
             PieceLabel b = (PieceLabel) c;
             return b.getPiece();
         }
         return null;
     }
 
     /**
      * Identifies the piece at a given point.
      *
      * @param x Number of pixels on the x axis.
      * @param y Number of pixels on the y axis.
      * @return Returns a Piece if the given point contains a piece, returns null
      * if a blank square.
      */
     public Piece pieceType(int x, int y) {
         Point a = new Point(x, y);
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof PieceLabel) {
             PieceLabel b = (PieceLabel) c;
             return b.getPiece();
         }
         return null;
     }
 
     /**
      * Redraws the chessboard.
      */
     public void refresh() {
         layeredPane.remove(chessBoard);
         layeredPane.add(chessBoard);
         layeredPane.revalidate();
         layeredPane.repaint();
     }
 
     /**
      * Returns the log of black moves
      *
      * @return Returns the black log as a string value.
      */
     public String getBlackLog() {
         return blackLog.toString();
     }
 
     /**
      * Returns the log of white moves
      *
      * @return Returns the white log as a string value.
      */
     public String getWhiteLog() {
         return whiteLog.toString();
     }
 
     /**
      * Places the given piece into the given point.
      *
      * @param e MouseEvent
      * @param p Point
      */
     public void replacePiece(MouseEvent e, PieceLabel p) {
         chessPiece.setVisible(false);
         Container c = (JPanel) chessBoard.findComponentAt(e.getX(), e.getY());
         c.add(p);
         chessPiece.setVisible(true);
     }
 
     /**
      * Not used
      *
      * @param e MouseEvent
      */
     @Override
     public void mouseClicked(MouseEvent e) {
     }
 
     /**
      * Not used
      *
      * @param e MouseEvent
      */
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 
     /**
      * Not used
      *
      * @param e MouseEvent
      */
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     /**
      * Not used
      *
      * @param e MouseEvent
      */
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     /**
      * Turns the current chessboard into a table and sends it to the ChessTable
      * class for further use.
      */
     public void toTable() {
         refresh();
         chessTable.reset();
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPoint(i)) instanceof PieceLabel) {
                 chessTable.updateTable((PieceLabel) chessBoard.findComponentAt(kord.getPoint(i)), i);
             }
         }
         chessTable.updateLog(getWhiteLog(), 0);
         chessTable.updateLog(getBlackLog(), 1);
         chessTable.updateTwoTable();
     }
 
     /**
      * Generates a chessboard from the chessTable class.
      */
     public void fromTable() {
         for (int i = 0; i < 64; i++) {
             Component c = chessTable.getComponent(i);
             if (c instanceof PieceLabel) {
                 JPanel panel = (JPanel) chessBoard.getComponent(i);
                 panel.add(c);
             }
         }
         whiteLog.clearLog();
         blackLog.clearLog();
         if (turn != 2) {
             whiteLog.addToLog(chessTable.getLog(0));
             blackLog.addToLog(chessTable.getLog(1));
         }
     }
 
     /**
      * Adds a chessListener.
      *
      * @param l ChessListener
      */
     public synchronized void addChessListener(ChessListener l) {
         _listeners.add(l);
     }
 
     /**
      * Removes the chessListener.
      *
      * @param l chessListener
      */
     public synchronized void removeChessListener(ChessListener l) {
         _listeners.remove(l);
     }
 
     /**
      * Sends a ChessEvent when called.
      */
     private synchronized void fireChessEvent() {
         if (piece != null) {
             ChessEvent chessEvent = new ChessEvent(this, chessPiece.getPiece().getTeam(), piece.getPiece());
             chessEvent.setPassant(passanten);
             Iterator listeners = _listeners.iterator();
             while (listeners.hasNext()) {
                 ((ChessListener) listeners.next()).chessReceived(chessEvent);
             }
         } else {
             ChessEvent chessEvent = new ChessEvent(this, chessPiece.getPiece().getTeam());
             chessEvent.setPassant(passanten);
             Iterator listeners = _listeners.iterator();
             while (listeners.hasNext()) {
                 ((ChessListener) listeners.next()).chessReceived(chessEvent);
             }
         }
     }
 
     /**
      * Changes the look of the board and pieces based on the input value.
      *
      * @param i Integer, 1 for meme, 2 for normal.
      */
     public void changeUI(int i) {
         if (i == 1) {
             meme = true;
         }
         if (i == 2) {
             meme = false;
         }
         chessTable.reset();
         toTable();
         chessTable.changeUI(i);
         fromTable();
         refresh();
     }
 
     /**
      * Opens a dialogbox that allows you to choose the piece the pawn turns into
      * when it crosses the board.
      *
      * @param lag The int value of the current team.
      */
     public void optionDialog(int lag) {
         JButton button1 = new JButton(knightB.getIcon());
         JButton button2 = new JButton(knightW.getIcon());
         JButton button3 = new JButton(queenB.getIcon());
         JButton button4 = new JButton(queenW.getIcon());
         JButton button5 = new JButton(rookW.getIcon());
         JButton button6 = new JButton(rookB.getIcon());
         JButton button7 = new JButton(bishopW.getIcon());
         JButton button8 = new JButton(bishopB.getIcon());
 
         button1.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(knightB.getIcon(), knightB);
             }
         });
         button2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(knightW.getIcon(), knightW);
             }
         });
         button3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(queenB.getIcon(), queenB);
             }
         });
         button4.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(queenW.getIcon(), queenW);
             }
         });
         button5.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(rookW.getIcon(), rookW);
             }
         });
 
         button6.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(rookB.getIcon(), rookB);
             }
         });
         button7.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(bishopW.getIcon(), bishopW);
             }
         });
         button8.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(bishopB.getIcon(), bishopB);
             }
         });
 
         if (chessPiece.getPiece().getTeam() == 1) {
             Object[] group = {button2, button4, button5, button7};
             showConfirmDialog(null, group, "Choose piece", DEFAULT_OPTION, QUESTION_MESSAGE, pawnW.getIcon());
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             Object[] group = {button1, button3, button6, button8};
             showConfirmDialog(null, group, "Choose piece", DEFAULT_OPTION, QUESTION_MESSAGE, pawnB.getIcon());
         }
 
     }
 
     /**
      * Moves the pawn
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the pawn completes a legal move, else returns
      * false.
      */
     public boolean movePawnB(MouseEvent e, Component m) {
         if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
             enPassantPB = (new Point((int) startPos.getX(), (int) startPos.getY() + 75));
             enPassantB = turn + 2;
             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + 75) instanceof PieceLabel) {
                 moveBack();
                 return false;
             }
         }
         if (pawnB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             //Opens a dialogbox when the pawn crosses the board.
             if (e.getY() + yAdjustment == 525) {
                 if (chessTable.checkB(kingBpos()) == false) {
                     chessPiece.setVisible(false);
                     optionDialog(chessPiece.getPiece().getTeam());
                     chessPiece.setVisible(true);
                     move(e);
                     return true;
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the pawn
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the pawn completes a legal move, else returns
      * false.
      */
     public boolean movePawnW(MouseEvent e, Component m) {
         if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
             enPassantPW = (new Point((int) startPos.getX(), (int) startPos.getY() - 75));
             enPassantW = turn + 2;
             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() - 75) instanceof PieceLabel) {
                 moveBack();
                 return false;
             }
         }
         if (pawnW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             //opens a dialogbox when the pawn crosses the board.
             if (e.getY() + yAdjustment == 0) {
                 if (chessTable.checkW(kingWpos()) == false) {
                     chessPiece.setVisible(false);
                     optionDialog(chessPiece.getPiece().getTeam());
                     chessPiece.setVisible(true);
                     move(e);
                     return true;
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the queen
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the queen completes a legal move, else returns
      * false.
      */
     public boolean moveQueenW(MouseEvent e, Component m) {
         int ruter;
         if (queenW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                 //Ned venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 //Opp venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Opp høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Ned høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
 
                 move(e);
                 return true;
             }
 
             if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                 //Y-Retning
                 if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 //X-Retning
                 if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 move(e);
                 return true;
             }
         } else {
             moveBack();
             return false;
         }
         return false;
     }
 
     /**
      * Moves the queen
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the queen completes a legal move, else returns
      * false.
      */
     public boolean moveQueenB(MouseEvent e, Component m) {
         int ruter;
         if (queenB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                 //Ned venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 //Opp venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Opp høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Ned høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
 
                 move(e);
                 return true;
             }
 
             if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                 //Y-Retning
                 if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 //X-Retning
                 if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 move(e);
                 return true;
             }
         } else {
             moveBack();
             return false;
         }
         return false;
     }
 
     /**
      * Moves the knight
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the knight completes a legal move, else returns
      * false.
      */
     public boolean moveKnightB(MouseEvent e, Component m) {
         if (knightB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the knight
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the knight completes a legal move, else returns
      * false.
      */
     public boolean moveKnightW(MouseEvent e, Component m) {
         if (knightW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
 
     }
 
     /**
      * Moves the king
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the king completes a legal move, else returns
      * false.
      */
     public boolean moveKingW(MouseEvent e, Component m) {
         if ((e.getX() + xAdjustment == 450)
                 && e.getY() + yAdjustment == 525
                 && !(chessBoard.findComponentAt((e.getX()
                 + xAdjustment), (e.getY()
                 + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment - 75), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)) {
             if (kingW.move() == false && rookWright.move() == false) {
                 move(e);
                 if (chessTable.checkW(kingWpos()) == false && chessTable.checkW(61) == false && chessTable.checkW(62) == false) {
                     castling = true;
                     Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                     Container parent = (Container) chessBoard.getComponent(61);
                     parent.add(c);
                     kingW.setMove();
                     chessPiece.setVisible(true);
                     return true;
 
                 }
             }
         }
         if ((e.getX() + xAdjustment == 150)
                 && e.getY() + yAdjustment == 525
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment + 75), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment - 75), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)) {
             if (kingW.move() == false && rookWleft.move() == false) {
                 move(e);
                 if (chessTable.checkW(kingWpos()) == false && chessTable.checkW(59) == false && chessTable.checkW(58) == false) {
                     castling = true;
                     Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                     Container parent = (Container) chessBoard.getComponent(59);
                     parent.add(c);
                     kingW.setMove();
                     chessPiece.setVisible(true);
                     return true;
 
                 }
             }
         }
 
         if (kingW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             kingW.setMove();
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the king
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the king completes a legal move, else returns
      * false.
      */
     public boolean moveKingB(MouseEvent e, Component m) {
         if (e.getX() + xAdjustment == 150
                 && e.getY() + yAdjustment == 0
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment + 75), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment - 75), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment), (int) (e.getY()
                 + yAdjustment)) instanceof PieceLabel)) {
             if (kingB.move() == false && rookBleft.move() == false) {
                 move(e);
                 if (chessTable.checkB(kingBpos()) == false && chessTable.checkB(3) == false && chessTable.checkB(2) == false) {
                     castling = true;
                     Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                     Container parent = (Container) chessBoard.getComponent(3);
                     parent.add(c);
                     kingB.setMove();
                     chessPiece.setVisible(true);
                     return true;
                 }
             }
         }
         if (e.getX() + xAdjustment == 450
                 && e.getY() + yAdjustment == 0
                 && !(chessBoard.findComponentAt((e.getX()
                 + xAdjustment), (e.getY() + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) e.getX()
                 + xAdjustment - 75), (int) (e.getY() + yAdjustment)) instanceof PieceLabel)) {
             if (kingB.move() == false && rookBright.move() == false) {
                 move(e);
                 if (chessTable.checkB(kingBpos()) == false && chessTable.checkB(5) == false && chessTable.checkB(6) == false) {
                     castling = true;
                     Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                     Container parent = (Container) chessBoard.getComponent(5);
                     parent.add(c);
                     kingB.setMove();
                     chessPiece.setVisible(true);
                     return true;
                 }
             }
         }
         if (kingB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             kingB.setMove();
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the bishop
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the bishop completes a legal move, else returns
      * false.
      */
     public boolean moveBishopW(MouseEvent e, Component m) {
         //Ned venstre
         int ruter;
         if (bishopW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             //Opp venstre
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Opp høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Ned høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the bishop
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the bishop completes a legal move, else returns
      * false.
      */
     public boolean moveBishopB(MouseEvent e, Component m) {
         //Ned venstre
         int ruter;
         if (bishopB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             //Opp venstre
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Opp høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Ned høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the rook
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the rook completes a legal move, else returns
      * false.
      */
     public boolean moveRookW(MouseEvent e, Component m) {
         int ruter;
         if (rookW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             //Y-Retning
             if ((int) startPos.getX() == e.getX() + xAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             //X-Retning
             if ((int) startPos.getY() == e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Moves the rook
      *
      * @param e MouseEvent
      * @param m Component
      * @return Returns true of the rook completes a legal move, else returns
      * false.
      */
     public boolean moveRookB(MouseEvent e, Component m) {
         int ruter;
         if (rookB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             //Y-Retning
             if ((int) startPos.getX() == e.getX() + xAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             //X-Retning
             if ((int) startPos.getY() == e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     /**
      * Part of the en-passant methods, replaces the pawn that was lost in a
      * illegal en-passant
      */
     public void replacePawn() {
         chessPiece.setVisible(false);
         if (chessPiece.getPiece().getTeam() == 1) {
             Container c = (JPanel) chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() + 75);
             c.add(new PieceLabel(pawnW.getIcon(), pawnW));
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             Container c = (JPanel) chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() - 75);
             c.add(new PieceLabel(pawnB.getIcon(), pawnB));
         }
         chessPiece.setVisible(true);
     }
 
     /**
      * Returns whether or not the en-passant move is currently allowed
      *
      * @return Returns if the en-passant move is currently allowed.
      */
     public boolean enPassant() {
         if (chessPiece.getPiece().getTeam() == 1 && pawnB.getPassant() == true) {
             if (chessPiece.getPiece() == pawnW) {
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() + 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() + 75)).getPiece();
                     if (a.equals(pawnW)) {
                         if (((int) enPassantPB.getX() + 75) == startPos.getX() && ((int) enPassantPB.getY() + 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() + 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() + 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() + 75)).getPiece();
                     if (a.equals(pawnW)) {
                         if (((int) enPassantPB.getX() - 75) == startPos.getX() && ((int) enPassantPB.getY() + 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() + 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
             }
         }
         if (chessPiece.getPiece().getTeam() == 2 && pawnW.getPassant() == true) {
             if (chessPiece.getPiece() == pawnB) {
                 //Ned høyre
                 if (chessBoard.findComponentAt((int) enPassantPW.getX() - 75, (int) enPassantPW.getY() - 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPW.getX() - 75, (int) enPassantPW.getY() - 75)).getPiece();
                     if (a.equals(pawnB)) {
                         if (((int) enPassantPW.getX() - 75) == startPos.getX() && ((int) enPassantPW.getY() - 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() - 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
                 //Ned venstre
                 if (chessBoard.findComponentAt((int) enPassantPW.getX() + 75, (int) enPassantPW.getY() - 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPW.getX() + 75, (int) enPassantPW.getY() - 75)).getPiece();
                     if (a.equals(pawnB)) {
                         if (((int) enPassantPW.getX() + 75) == startPos.getX() && ((int) enPassantPW.getY() - 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() - 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     /**
      * Finds the component/piece at the selected index.
      *
      * @param index Value of the board index.
      * @return Returns the cmponent at given index.
      */
     public Component getPiece(int index) {
         return chessBoard.findComponentAt(kord.getPoint(index));
     }
 
     /**
      * Turns the values of the pieces into a table to be used for network
      * communication or serializing savegames.
      *
      * @return Returns a table with the usable Pieces.
      */
     public Piece[] getPieceTable() {
         Piece[] pieces = {pawnB, pawnW, rookB, rookW, rookBright, rookBleft, rookWright, rookBright, knightB, knightW, bishopB, bishopW, queenB, queenW, kingB, kingW};
         return pieces;
     }
 
     /**
      * Creates a chessboard from the given info allowing to continue a previous
      * game.
      *
      * @param table A table of piecelabels.
      * @param turn2 Integer value of number of moves already done.
      * @param logW String value of the white log.
      * @param logB String value of the black log.
      * @param pieces Table of Pieces with set variables.
      * @param passanten2 The boolean variable for the en-passant move.
      * @param enPassantB2 The int value indicating how long the en-passant move
      * is allowed for the black team.
      * @param enPassantW2 The int value indicating how long the en-passant move
      * is allowed for the white team.
      * @param enPassantPW2 The point indicating where the en-passant move is
      * possible for the white team.
      * @param enPassantPB2 The point indicating where the en-passant move is
      * possible for the black team.
      * @param meme2 Boolean value indicating whether or not meme pieces is
      * selected.
      */
     public void loadGame(PieceLabel[] table, int turn2, String logW, String logB, Piece[] pieces, boolean passanten2, int enPassantB2, int enPassantW2, Point enPassantPW2, Point enPassantPB2, boolean meme2) {
         for (int i = 0; i < 64; i++) {
             JPanel panel = (JPanel) chessBoard.getComponent(i);
             panel.removeAll();
         }
         for (int i = 0; i < 64; i++) {
             if (table[i] instanceof PieceLabel) {
                 JPanel panel = (JPanel) chessBoard.getComponent(i);
                 panel.add(table[i]);
             }
         }
         pawnB = (PawnB) pieces[0];
         pawnW = (PawnW) pieces[1];
         rookB = (RookB) pieces[2];
         rookW = (RookW) pieces[3];
         rookBright = (RookB) pieces[4];
         rookBleft = (RookB) pieces[5];
         rookWright = (RookW) pieces[6];
         rookBright = (RookB) pieces[7];
         knightB = (KnightB) pieces[8];
         knightW = (KnightW) pieces[9];
         bishopB = (BishopB) pieces[10];
         bishopW = (BishopW) pieces[11];
         queenB = (QueenB) pieces[12];
         queenW = (QueenW) pieces[13];
         kingB = (KingB) pieces[14];
         kingW = (KingW) pieces[15];
 
 
         chessTable.newTable(table);
 
         chessTable.updateLog(logW, 0);
         chessTable.updateLog(logB, 1);
         turn = turn2;
         passanten = passanten2;
         enPassantB = enPassantB2;
         enPassantW = enPassantW2;
         enPassantPW = enPassantPW2;
         enPassantPB = enPassantPB2;
         meme = meme2;
 
 
         fromTable();
 
         refresh();
         cleanBoardColor();
         if (chessTable.checkB(kingBpos())) {
             colorSquare(kingBpos());
             colorSquareB = kingBpos();
         } else {
             blankSquare(colorSquareB);
         }
         if (chessTable.checkW(kingWpos())) {
             colorSquare(kingWpos());
             colorSquareW = kingWpos();
         } else {
             blankSquare(colorSquareW);
         }
     }
 
     /**
      * Returns the number of turns already done
      *
      * @return Int value indicating the number of turns.
      */
     public int getTurn() {
         return turn;
     }
 
     /**
      * Colors the square indicated by the given index.
      *
      * @param i the index value for the square to be colored.
      */
     public void colorSquare(int i) {
         Component c = chessBoard.getComponent(i);
         if (c instanceof PieceLabel) {
             JPanel p = (JPanel) c.getParent();
             p.setOpaque(true);
             p.setBackground(Color.red);
         } else {
             JPanel p = (JPanel) c;
             p.setOpaque(true);
             p.setBackground(Color.red);
         }
     }
 
     /**
      * Removes any coloring on the selected square.
      *
      * @param i the index value for the square the be blanked.
      */
     public void blankSquare(int i) {
         Component c = chessBoard.getComponent(i);
         if (c instanceof PieceLabel) {
             JPanel p = (JPanel) c.getParent();
             p.setOpaque(false);
         } else {
             JPanel p = (JPanel) c;
             p.setOpaque(false);
         }
     }
 
     /**
      * Colors several squares based on the table indicating where the piece is
      * allowed to move.
      *
      * @param tab int table containing the squares to color.
      */
     public void colorSquares(int[] tab) {
         for (int i = 0; i < tab.length; i++) {
             Component c = chessBoard.getComponent(tab[i]);
             JPanel d = (JPanel) chessBoard.getComponentAt(startPos);
             d.setOpaque(true);
             d.setBackground(Color.yellow);
             JPanel p = (JPanel) c;
             p.setOpaque(true);
             p.setBackground(Color.green);
         }
     }
 
     /**
      * Colors several squares based on the table indicating possible special
      * moves.
      *
      * @param tab int table containing the squares to color.
      */
     public void colorSpecialSquares(int[] tab) {
         for (int i = 0; i < tab.length; i++) {
             JPanel d = (JPanel) chessBoard.getComponent(tab[i]);
             d.setOpaque(true);
             d.setBackground(Color.blue);
         }
     }
 
     /**
      * Removes all coloring except red from the board.
      */
     public void cleanMoveColors() {
         for (int i = 0; i < 64; i++) {
             JPanel c = (JPanel) chessBoard.getComponent(i);
             if (!c.getBackground().equals(Color.red)) {
                 c.setOpaque(false);
             }
         }
     }
 
     /**
      * Removes all coloring from the board.
      */
     public void cleanBoardColor() {
         for (int i = 0; i < 64; i++) {
             JPanel c = (JPanel) chessBoard.getComponent(i);
             c.setOpaque(false);
         }
     }
 
     /**
      * Returns the value indicating whether or not the en-passant move is
      * allowed.
      *
      * @return Returns the value showing if the en-passant move is allowed.
      */
     public boolean getPassanten() {
         return passanten;
     }
 
     /**
      * Changes the value of the passant variable.
      *
      * @param passant the new value for the passant variable.
      */
     public void setPassanten(boolean passant) {
         passanten = passant;
     }
 
     /**
      * Checks if the en-passant move is allowed for the white team.
      *
      * @return Returns whether or not the en-passant move is allowed.
      */
     public int getEnPassantB() {
         return enPassantB;
     }
 
     /**
      * Changes the value of the en-passant for the black team.
      *
      * @param enPassant the new value for the en-passant.
      */
     public void setEnPassantB(int enPassant) {
         enPassantB = enPassant;
     }
 
     /**
      * Checks if the en-passant move is allowed for the white team.
      *
      * @return Returns whether or not the en-passant move is allowed.
      */
     public int getEnPassantW() {
         return enPassantW;
     }
 
     /**
      * Changes the value of the en-passant for the white team.
      *
      * @param enPassant the new value for the en-passant.
      */
     public void setEnPassantW(int enPassant) {
         enPassantW = enPassant;
     }
 
     /**
      * Returns the point indicating where the en-passant move is allowed.
      *
      * @return Returns the point where the en-passant move is allowed.
      */
     public Point getEnPassantPW() {
         return enPassantPW;
     }
 
     /**
      * Sets the point where the en-passant move is allowed.
      *
      * @param enPassant The new point for en-passant on white team
      */
     public void setEnPassantPW(Point enPassant) {
         enPassantPB = enPassant;
     }
 
     /**
      * Returns the point indicating where the en-passant move is allowed.
      *
      * @return Returns the point where the en-passant move is allowed.
      */
     public Point getEnPassantPB() {
         return enPassantPB;
     }
 
     /**
      * Sets the point where the en-passant move is allowed.
      *
      * @param enPassant The new point for en-passant on black team
      */
     public void setEnPassantPB(Point enPassant) {
         enPassantPB = enPassant;
     }
 
     /**
      * Returns the value indicating if the meme layout option has been checked.
      *
      * @return Returns a value indicating if meme has been selected.
      */
     public boolean getMeme() {
         return meme;
     }
 
     /**
      * Changes the meme variable allowing the change in layout.
      *
      * @param meme2 the new value for the meme option, true if meme is chosen,
      * false if regular pieces.
      */
     public void setMeme(boolean meme2) {
         meme = meme2;
     }
 }
