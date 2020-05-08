 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import framework.*;
 import gui.shapes.PegShape;
 import solver.*;
 
 public class PuzzleCanvas extends JComponent implements MouseListener, MouseWheelListener, MouseMotionListener{
 
 	private PieceShape[] p;
 	private Point[][] boardLocs;
 	private Point[] homeLoc;
 	private Puzzle puzzle;
 	private double ratio;
 
 	private Point dragOrigin;
 
 	public PuzzleCanvas(PieceShape[] pieces, Puzzle puzzle){
 		this.p = pieces;
 		this.puzzle = puzzle;
 		trayMaker();
 		boardMaker();
 		addMouseListener(this);
 	}
 
 	private void trayMaker() {
 		homeLoc = new Point[9];
 		homeLoc[0] = new Point(580, 50);
 		homeLoc[1] = new Point(770, 50);
 		homeLoc[2] = new Point(580, 275);
 		homeLoc[3] = new Point(770, 275);
 		homeLoc[4] = new Point(10, 500);
 		homeLoc[5] = new Point(200, 500);
 		homeLoc[6] = new Point(390, 500);
 		homeLoc[7] = new Point(580, 500);
 		homeLoc[8] = new Point(770, 500);
 		setHome();
 	}
 	
 	private void setHome(){
 		for(int i = 0; i < p.length; i++){
 			p[i].setHome(new Point(homeLoc[i].x+2, homeLoc[i].y+2));
 		}
 	}
 
 	private void boardMaker(){
 		Point[][] p = new Point[3][3];
 		p[0][0] = new Point(100, 50);
 		p[1][0] = new Point(230, 50);
 		p[2][0] = new Point(360, 50);
 		p[0][1] = new Point(100, 180);
 		p[1][1] = new Point(230, 180);
 		p[2][1] = new Point(360, 180);
 		p[0][2] = new Point(100, 310);
 		p[1][2] = new Point(230, 310);
 		p[2][2] = new Point(360, 310);
 		
 		boardLocs = p;
 	}
 
 	public void paint(Graphics g){
 
 		Graphics2D g2 = (Graphics2D)g;
 		
 		paintBoard(g2);
 		paintTray(g2);
 		
 		for(PieceShape e : p){
 			e.pieceMaker();
 			e.updateLoc();
 			g2.setColor(e.getColor());
 			g2.fill(e.getBody());
 		}
 		for(PieceShape e : p){
 			g2.setColor(PuzzleGameFrame.backColor);
 			PegShape[] in = e.getInPegs();
 			for(PegShape s : in)
 				s.paint(g2);
 		}
 		for(PieceShape e : p){
 			g2.setColor(e.getColor());
 			PegShape[] out = e.getOutPegs();
 			for(PegShape s : out)
 				s.paint(g2);
 		}
 	}
 
 	private void paintBoard(Graphics2D g2) {
 		
 		g2.setColor(Color.LIGHT_GRAY.brighter());
 		g2.fillRect(boardLocs[0][0].x-20, boardLocs[0][0].y-20, 130*3+40, 130*3+40);
 		
 		for(int i = 0; i < boardLocs.length; i++){
 			for(int j = 0; j < boardLocs[0].length; j++){
 				g2.setColor(Color.LIGHT_GRAY);
 				g2.fillRect(boardLocs[i][j].x, boardLocs[i][j].y, 130, 130);
 				g2.setColor(Color.BLACK);
 				g2.drawRect(boardLocs[i][j].x, boardLocs[i][j].y, 130, 130);
 			}
 		}
 	}
 	
 	private void paintTray(Graphics2D g2){
 		for(Point p : homeLoc){
 		g2.setColor(Color.LIGHT_GRAY);
 		g2.fill3DRect(p.x, p.y, 184, 184, true);
 		}
 	}
 
 	private void test(Graphics2D g2) {
 		g2.setColor(Color.red);
 		for(int i = 0; i < boardLocs.length; i++){
 			for(int j = 0; j < boardLocs[0].length; j++){
 				g2.drawRect(boardLocs[i][j].x, boardLocs[i][j].y, 10, 10);
 			}
 		}
 		g2.setColor(Color.blue);
 		for(Point p : homeLoc){
 			g2.drawRect(p.x, p.y, 10, 10);
 		}
 	}
 
 	public PieceShape getClickedPiece(Point point){
 		for(PieceShape e : p){
 			if(e.contains(point)){
 				return e;
 			}
 		}
 		return null;
 	}
 
 	private PieceShape selected;
 	@Override
 	public void mouseClicked(MouseEvent m) {
 		
 		Point p = m.getPoint();
 		
 		if (m.getButton() == MouseEvent.BUTTON1) {
 			if (selected == null) {
 				PieceShape piece = getClickedPiece(p);
 				if (piece == null)
 					return;
 				selected = piece;
 			}
 			else {
 				Point bSpot = getClickedBoardSpot(p);
 				int tSpot = getClickedTraySpot(p);
 				PieceShape clicked = getClickedPiece(m.getPoint());
				if (bSpot != null) {
					if(selected.isInBoard())
						selected.removeFromBoard();
 					putInBoard(bSpot);
 					selected = null;
 					return;
 				} else if (clicked != null){
 					selected = clicked;
 					return;
 				} else if (tSpot != -1) {
 					putInTray(tSpot);
 					selected = null;
 					return;
 				}
 			}
 		}
 		else{
 			selected = null;
 			PieceShape piece = getClickedPiece(p);
 			if (piece == null)
 				return;
 			if (!piece.isInBoard()) {
 				piece.rotate(true);
 			}
 		}
 		repaint();
 	}
 	
 	private int getClickedTraySpot(Point p) {
 		for(int i = 0; i < homeLoc.length; i++){
 			Rectangle r = new Rectangle(homeLoc[i].x, homeLoc[i].y, 184, 184);
 			if (r.contains(p))
 				return i;
 		}
 		return -1;
 	}
 
 	private void putInTray(int tSpot) {
 		Point h = homeLoc[tSpot];
 		Point test = new Point(h.x + 90, h.y + 90);
 		
 		if (getClickedPiece(test) == null) {
 			if(selected.isInBoard()){
 				selected.removeFromBoard();
 			}
 			selected.setLoc(new Point(h.x+2, h.y+2));
 			selected.removeFromBoard();
 			selected = null;
 			repaint();
 			return;
 		}
 	}
 
 	/**
 	 * Puts the selected Piece into the board
 	 * @param p is the point on the board (x,y)
 	 * @return true if successful, false if failed
 	 */
 	private boolean putInBoard(Point p){
 		int x = p.x;
 		int y = p.y;
 		
 		Point loc = boardLocs[x][y];
 
 		//for temp storage if need to remove from the board
 		Point selectedLoc = puzzle.find(selected.getPiece());
 		
 		if(selected.isInBoard()){
 			selected.removeFromBoard();
 		}
 		if(puzzle.canFit(x, y, selected.getPiece())){// check with board
 			if (selected.getPiece().getOrientation() == 0) {
 				selected.setLoc(new Point(loc.x, loc.y - 50));
 			}
 			else if (selected.getPiece().getOrientation() == 1){
 				selected.setLoc(new Point(loc.x, loc.y));
 			}
 			else if (selected.getPiece().getOrientation() == 2){
 				selected.setLoc(new Point(loc.x - 50, loc.y));
 			}
 			else{
 				selected.setLoc(new Point(loc.x - 50, loc.y - 50));
 			}
 			puzzle.insertPieceAtLocation(x, y, selected.getPiece());
 			selected.putInBoard(new Point(x,y), puzzle.getBoard());
 			repaint();
 			
 			if(puzzle.isSolved()){
 				String message = "Congratulations, you solved the puzzle!";
 					JOptionPane.showMessageDialog(null,message,"Solved", JOptionPane.INFORMATION_MESSAGE);
 			}
 			return true;
 		}
 		else if(selected.isInBoard()){
 			puzzle.insertPieceAtLocation(selectedLoc.x, selectedLoc.y , selected.getPiece());
 			selected.removeFromBoard();
 			selected.putInBoard(selectedLoc, puzzle.getBoard());
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if the clicked point on the board is a spot on the board
 	 * @param p
 	 * @return
 	 */
 	private Point getClickedBoardSpot(Point p) {
 		for(int i = 0; i < boardLocs.length; i++){
 			for(int j = 0; j < boardLocs[0].length; j++){
 				Rectangle r = new Rectangle(boardLocs[i][j].x, boardLocs[i][j].y, 150, 150);
 				if (r.contains(p))
 					return new Point(i,j);
 			}
 		}
 		return null;
 	}
 	
 	public void reset() {
 		if (puzzle.getBoard().isEmpty()) {
 			for (PieceShape e : p){
 				while(e.getPiece().getOrientation() != 0){
 					e.rotate(true);
 				}
 			}
 		}
 		else{
 			for (PieceShape e : p) {
 				e.goHome();
 				e.setInBoard(false);
 			}
 			puzzle.empty();
 		}
 		for(PieceShape e:p){
 			e.pieceMaker();
 		}
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {}
 
 	@Override
 	public void mouseDragged(MouseEvent m) {}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent m) {}
 
 	public void solve(Puzzle solved) {
 		reset();
 		for(int i = 0; i < p.length; i++){
 			p[i].getPiece().setOrientation(solved.getPieces()[i].getOrientation());
 			p[i].pieceMaker();
 		}
 		for(int x = 0, i = 0; x < puzzle.getBoard().getWidth(); x++){
 			for(int y = 0; y < puzzle.getBoard().getHeight(); y++, i++){
 				Point loc = solved.find(solved.getPieces()[i]);
 				selected = p[i];
 				putInBoard(loc);
 				repaint();
 			}
 		}
 		repaint();
 	}
 }
