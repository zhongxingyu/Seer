 import java.util.ArrayList;
 import java.awt.*;
 import java.awt.event.*;
 
 //TODO:
 //Add functionality to make testing more automated. Should be able to interpret move lists, and step forward, maybe even step back
 //Checkmate-checking: Is there any way better than brute-forcing all moves to see if legal or not?
 
 @SuppressWarnings("serial")
 public class StandardChessGame extends Game
 {
 	private Thread m_process;
 	private Board m_game_board;
 	private StandardChessGameGraphics m_graphics;
 	private Piece m_selected;
 
 	private int enpassantCol; //the column (0-7) of the pawn to move two spaces last turn, -1 if no pawn moved two spaces
 	private boolean whiteCanCastleKingside; //false if king's rook or king have moved
 	private boolean whiteCanCastleQueenside; //false if queen's rook or king have moved
 	private boolean blackCanCastleKingside;
 	private boolean blackCanCastleQueenside;
 
 	public StandardChessGame()
 	{
 
 	}
 
 	public void init()
 	{
 		m_process = new Thread(this);
 		m_game_board = new Board();
 		m_graphics = new StandardChessGameGraphics();
 		m_selected = null;
 
 		whiteCanCastleKingside = true;
 		whiteCanCastleQueenside = true;		
 		blackCanCastleKingside = true;
 		blackCanCastleQueenside = true;
 
 		addMouseListener(this);
 
 		//Placing standard pieces
 		//William y u do dis 2 me
 		m_game_board.placePiece(new Rook(0, 0, Definitions.Color.BLACK), 0, 0);
 		m_game_board.placePiece(new Knight(0, 1, Definitions.Color.BLACK), 0, 1);
 		m_game_board.placePiece(new Bishop(0, 2, Definitions.Color.BLACK), 0, 2);
 		m_game_board.placePiece(new Queen(0, 3, Definitions.Color.BLACK), 0, 3);
 		m_game_board.placePiece(new King(0, 4, Definitions.Color.BLACK), 0, 4);
 		m_game_board.placePiece(new Bishop(0, 5, Definitions.Color.BLACK), 0, 5);
 		m_game_board.placePiece(new Knight(0, 6, Definitions.Color.BLACK), 0, 6);
 		m_game_board.placePiece(new Rook(0, 7, Definitions.Color.BLACK), 0, 7);
 		for (int c = 0; c < 8; c++) {
 			m_game_board.placePiece(new Pawn(1, c, Definitions.Color.BLACK), 1, c); 
 		}
 		m_game_board.placePiece(new Rook(7, 0, Definitions.Color.WHITE), 7, 0);
 		m_game_board.placePiece(new Knight(7, 1, Definitions.Color.WHITE), 7, 1);
 		m_game_board.placePiece(new Bishop(7, 2, Definitions.Color.WHITE), 7, 2);
 		m_game_board.placePiece(new Queen(7, 3, Definitions.Color.WHITE), 7, 3);
 		m_game_board.placePiece(new King(7, 4, Definitions.Color.WHITE), 7, 4);
 		m_game_board.placePiece(new Bishop(7, 5, Definitions.Color.WHITE), 7, 5);
 		m_game_board.placePiece(new Knight(7, 6, Definitions.Color.WHITE), 7, 6);
 		m_game_board.placePiece(new Rook(7, 7, Definitions.Color.WHITE), 7, 7);
 		for (int c = 0; c < 8; c++) {
 			m_game_board.placePiece(new Pawn(6, c, Definitions.Color.WHITE), 6, c); 
 		}
 		setTurn(Definitions.Color.WHITE);
 	}
 
 	public void run()
 	{
 	}
 
 	public void paint(Graphics g)
 	{
 		//Painting with a backbuffer reduces flickering
 		Image backbuffer = createImage(g.getClipBounds().width, g.getClipBounds().height);
 		Graphics backg = backbuffer.getGraphics();
 
 		m_graphics.drawBoard(backg, m_game_board);
 		m_graphics.drawSelected(backg, m_selected);
 
 		g.drawImage(backbuffer, 0, 0, this);
 	}
 
 	public boolean isLegalMove(Move m)
 	{		
 		//generate moves and use board to determine legality (is there a piece in the way? etc.)
 
 		Piece p = m_game_board.getPiece(m.r0, m.c0);
 
 		if ((p == null) || (p.color() != whoseTurn()))
 		{
 			return false; //source square has no piece, or selected piece is opponent's piece
 		}
 
 		Piece destination = m_game_board.getPiece(m.rf, m.cf);
 		boolean occupiedDest = (destination != null);
 		ArrayList<Move> moves = p.getMoves();
 
 		if (occupiedDest && (destination.color() == whoseTurn())) //case of trying to move to source square is handled here
 		{
 			return false; //can't move to square occupied by same color piece
 		}
 
 		if (moves.contains(m))
 		{
 			Board tempBoard = new Board(m_game_board); //clone board
 			tempBoard.move(m);
 
 			if (!(p instanceof Knight) && (hasPieceInWay(m, m_game_board))) 
 				//if it's not a knight, it can't jump
 			{
 				return false;
 			}
 
 			if (inCheck(whoseTurn(), tempBoard))
 			{
 				return false; //illegal move because you will be in check after move
 			}
 
 			if (p instanceof Knight) //Update: king moves no longer automatically legal
 			{
 				return true; //all possible moves are automatically legal because we already checked earlier
 				//that the destination square does not contain a piece of same color
 			}
 			if (p instanceof King)
 			{
 				//Is there a better way to do this? Seems ugly
 				boolean canCastleKingside;
 				boolean canCastleQueenside;
 				if (whoseTurn() == Definitions.Color.WHITE)
 				{
 					canCastleKingside = whiteCanCastleKingside;
 					canCastleQueenside = whiteCanCastleQueenside;
 				}
 				else
 				{
 					canCastleKingside = blackCanCastleKingside;
 					canCastleQueenside = blackCanCastleQueenside;
 				}
 
 				if (m.cf - m.c0 == 2) //kingside castle attempt
 				{
 					Board temp = new Board(m_game_board);
 					Move intermediate = new Move(m.r0, m.c0, m.r0, m.c0 + 1);
 					temp.move(intermediate);
 					return (canCastleKingside && !inCheck(whoseTurn(), temp) && !inCheck(whoseTurn(), m_game_board)); //can't be in check
 				}
 				if (m.cf - m.c0 == -2) //queenside castle attempt
 				{
 					Board temp = new Board(m_game_board);
 					Move intermediate = new Move(m.r0, m.c0, m.r0, m.c0 - 1);
 					temp.move(intermediate);
 					return (canCastleQueenside && !inCheck(whoseTurn(), temp) && !inCheck(whoseTurn(), m_game_board)); //can't be in check
 				}
 				return true; //all other possible moves are legal one-square moves
 			}
 			if (p instanceof Pawn) //must split into cases
 			{
 				if (m.cf != m.c0) //changed columns; must be capture
 				{
 					boolean validCapture = (m_game_board.getPiece(m.rf, m.cf) != null); //can't be our own piece because of earlier check
 					boolean enpassant;
 					if (whoseTurn() == Definitions.Color.WHITE)
 						enpassant = ((m.cf == enpassantCol) && (m.r0 == 3));
 					else
 						enpassant = ((m.cf == enpassantCol) && (m.r0 == 4));
 					
 					return (validCapture || enpassant);
 				}
 				else
 				{
 					if (m.rf - m.r0 == 2) //push two squares
 					{
 						return (m_game_board.getPiece((m.rf + m.r0) / 2, m.cf) == null)
 								&& (m_game_board.getPiece(m.rf, m.cf) == null); //both squares empty
 					}
 					else //push one square
 					{
 						return (m_game_board.getPiece(m.rf, m.cf) == null); //square empty
 					}
 				}
 			}
 			return true;
 		}
 		return false; //move is not in our move list
 	}
 
 	private boolean hasPieceInWay(Move m, Board b)
 	{
 		int dc = m.cf - m.c0;
 		int dr = m.rf - m.r0; //remember rows are counted from the top
 		int cinc; //1, 0, or -1, depending on which direction the piece is headed
 		int rinc; //1, 0, or -1
		
		Piece p = b.getPiece(m.r0, m.c0);
		if (p == null || p instanceof Knight)
			return false; //vacuously false for no piece, and automatically false for knights
 
 		if (dc < 0)
 		{
 			cinc = -1;
 		}
 		else if (dc > 0)
 		{
 			cinc = 1;
 		}
 		else
 		{
 			cinc = 0;
 		}
 
 		if (dr < 0)
 		{
 			rinc = -1;
 		}
 		else if (dr > 0)
 		{
 			rinc = 1;
 		}
 		else
 		{
 			rinc = 0;
 		}
 
 		int r = m.r0 + rinc;
 		int c = m.c0 + cinc;
 		while (!((r == m.rf) && (c == m.cf)))
 		{
 			if (b.getPiece(r, c) != null)
 			{
 				//System.out.println("Piece in way. Row: " + r + ", Col: " + c); //debugging pieces
 				return true; //there is a piece in our way
 			}
 			r = r + rinc;
 			c = c + cinc;
 		}
 		return false; //no pieces in way
 	}
 
 	//moving to Game class from Board class, since the Board doesn't necessarily know rules of game
 	//added Board argument since you might want to check temporary boards too
 	public boolean inCheck(Definitions.Color color, Board b)
 	{
 		//this will check to see if the king of 'color' is threatened or not
 		int kingR = -1; //default values to satisfy compiler
 		int kingC = -1;
 		//might want to have a king location variable in each board
 		//or we can use an arraylist to hold all pieces on board, maybe separated by color?
 		//efficiency considerations?
 
 		for (int r = 0; r < Definitions.NUMROWS; r++)
 		{
 			for (int c = 0; c < Definitions.NUMCOLS; c++)
 			{
 				Piece temp = b.getPiece(r, c);
 				if ((temp instanceof King) && (temp.color() == color))
 				{
 					kingR = r;
 					kingC = c;
 					break; //is there a way to jump outside both loops without using goto?
 				}
 			}
 		}
 
 		for (int r = 0; r < Definitions.NUMROWS; r++)
 		{
 			for (int c = 0; c < Definitions.NUMCOLS; c++)
 			{
 				Piece temp = b.getPiece(r, c);
 				Move m = new Move(r, c, kingR, kingC);
 				if ((temp != null) && (temp.color() != color) && (temp.getThreats().contains(m)))
 				{
					if (temp instanceof Knight || !hasPieceInWay(m, b))
 					{
 						//System.out.println("In check. Row: " + r + ", Column: " + c); //for debugging purposes
 						return true;
 					}
 				}
 			}
 		}
 		return false; //no pieces can capture king
 	}
 
 	public boolean isCheckmate(Definitions.Color color, Board b)
 	{
 		ArrayList<Move> allMoves = new ArrayList<Move>();
 		for (int r = 0; r < 8; r++)
 		{
 			for (int c = 0; c < 8; c++)
 			{
 				Piece p = m_game_board.getPiece(r, c);
 				if (p != null && p.color() == color)
 				{
 					allMoves.addAll(p.getMoves());
 				}
 			}
 		}
 		
 		for (Move m : allMoves)
 		{
 			if (isLegalMove(m) == true)
 			{
 				return false; //found at least one legal move; not checkmate
 			}
 		}
 		return true; //checkmate; all possible moves are illegal
 	}
 	
 	private void flipTurn()
 	{
 		setTurn(Definitions.flip(whoseTurn()));
 		if (isCheckmate(whoseTurn(), m_game_board))
 		{
 			System.out.print("Checkmate! ");
 			if (whoseTurn() == Definitions.Color.WHITE) //white is in checkmate; black wins
 			{
 				System.out.println("Black wins!");
 			}
 			else //black is in checkmate; white wins
 			{
 				System.out.println("White wins!");
 			}
 			//do whatever you have to do when you want the game to end
 		}
 	}
 
 	//TODO
 	public static Move algebraicToMove(String algebraic) //STUB
 	{
 		return new Move(0, 0, 0, 0);
 	}
 	
 	public void interpretMoveList(String movelist) //does not work yet
 	{
 		//start with naive format of "1.e4 c5 2.Nc3 Nc6 3.f4 g6 4.Bb5 Nd4", with proper spacing and all
 		String[] moves = movelist.split(" ");
 		
 		for (int i = 0; i < moves.length; i++)
 		{
 			String mv = moves[i];
 			if (Character.isDigit(mv.charAt(0)))
 			{
 				System.out.print(mv + " "); //print out moves
 				m_game_board.move(algebraicToMove(mv.split(".")[1])); //want the part after the period
 			}
 			else
 			{
 				System.out.println(mv);
 				m_game_board.move(algebraicToMove(mv));
 			}
 		}
 	}
 	
 	public void mousePressed(MouseEvent e)
 	{
 		int row = m_graphics.getRow(e.getY());
 		int col = m_graphics.getCol(e.getX());
 		//Left-click to select
 		if (e.getButton() == MouseEvent.BUTTON1) select(row, col);
 		//Right-click to move selected
 		else if (e.getButton() == MouseEvent.BUTTON3) moveSelected(row, col);
 		repaint();
 	}
 
 	private void select(int row, int col)
 	{
 		m_selected = null;
 		if (row >= 0 && col >= 0) {
 			Piece p = m_game_board.getPiece(row, col);
 			if (p != null && p.color() == whoseTurn()) m_selected = p;
 		}
 	}
 
 	private void moveSelected(int row, int col)
 	{
 		if (m_selected == null) return;
 		Move newMove = new Move(m_selected.row(), m_selected.col(), row, col);
 		if (!isLegalMove(newMove)) return;
 
 		Piece movedPiece = m_game_board.getPiece(m_selected.row(), m_selected.col());
 		
 		int castlingRow;
 		if (whoseTurn() == Definitions.Color.WHITE)
 		{
 			castlingRow = 7;
 		}
 		else //Black
 		{
 			castlingRow = 0;
 		}
 		
 		enpassantCol = -1; //default
 		if (movedPiece instanceof Pawn)
 		{
 			if (Math.abs(newMove.rf - newMove.r0) == 2)
 			{
 				enpassantCol = newMove.c0; //enpassant now available on this column
 			}
 			else if ((Math.abs(newMove.cf - newMove.c0) == 1) && (m_game_board.getPiece(newMove.rf, newMove.cf) == null))
 				//en passant
 			{
 				if (whoseTurn() == Definitions.Color.WHITE)
 				{
 					m_game_board.removePiece(3, newMove.cf); //not sure if this is best way, but "move" call will not erase piece
 				}
 				else
 				{
 					m_game_board.removePiece(4, newMove.cf);
 				}
 			}
 		}
 		else if (movedPiece instanceof King)
 		{
 			if (whoseTurn() == Definitions.Color.WHITE)
 			{
 				whiteCanCastleKingside = false;
 				whiteCanCastleQueenside = false;
 			}
 			else
 			{
 				blackCanCastleKingside = false;
 				blackCanCastleQueenside = false;
 			}
 
 			int kingMoveLength = col - m_selected.col(); //should be 2 or -2, if the move was a castling move
 			if (m_selected.row() == castlingRow)
 			{
 				if (kingMoveLength == 2) //kingside
 				{
 					Move correspondingRookMove = new Move(castlingRow, 7, castlingRow, 5);
 					m_game_board.move(correspondingRookMove);
 				}
 				else if (kingMoveLength == -2) //queenside
 				{
 					Move correspondingRookMove = new Move(castlingRow, 0, castlingRow, 3);
 					m_game_board.move(correspondingRookMove);
 				}
 			}
 		}
 		else if (m_selected.row() == castlingRow)
 		{
 			if (m_selected.col() == 0) //queen's rook
 			{
 				if (whoseTurn() == Definitions.Color.WHITE)
 				{
 					whiteCanCastleQueenside = false;
 				}
 				else
 				{
 					blackCanCastleQueenside = false;
 				}
 			}
 			else if (m_selected.col() == 7) //king's rook
 			{			
 				if (whoseTurn() == Definitions.Color.WHITE)
 				{
 					whiteCanCastleKingside = false;
 				}
 				else
 				{
 					blackCanCastleKingside = false;
 				}
 			}
 		}
 
 		m_game_board.move(newMove); //has to be down here for time being because en passant needs to know dest sq is empty; fix if you can
 		flipTurn();
 	}
 
 	//Useless for now
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseClicked(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 
 	//Prevents flickering when repainting
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 
 	//Upon exiting applet, stop all processes
 	public void stop()
 	{
 		if (m_process != null) m_process.interrupt();
 	}
 }
