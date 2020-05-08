 package com.petrifiednightmares.singularityChess.ai;
 
 import java.util.Random;
 import java.util.Set;
 
 import android.util.Log;
 
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.logic.Board;
 import com.petrifiednightmares.singularityChess.logic.Game;
 import com.petrifiednightmares.singularityChess.logic.Square;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 
 
 public class AIEngine {
 
 	public static final int	EASY = 1, MEDUIM = 2, HOLYSHIT = 3;
 	
 	@SuppressWarnings("unused")
 	private int	aiLevel;
 
 	@SuppressWarnings("unused")
 	private Game _game; // a link to the actual game
 	@SuppressWarnings("unused")
 	private Board _board;	// a link to the actual board
 	private AbstractPiece _selectingPiece;
 	private Square _target;
 	private boolean	isWhiteTurn;
 
 	private AbstractPiece[]	_whitePieces;
 	private AbstractPiece[]	_blackPieces;
 	
 	public AIEngine(Game game)
 	{
 		this._game = game;
 		this._board = game.getBoard();
 		//TODO we should get the AI level from game type, need to fix this later. 
 		this.aiLevel = AIEngine.MEDUIM;
 		this.isWhiteTurn = game.isWhiteTurn();
 		this._whitePieces = game.getWhitePiece();
 		this._blackPieces = game.getBlackPiece();
 	}	
 	
 	public AbstractPiece getSelectingPiece()
 	{
 		return this._selectingPiece;
 	}
 	
 	public Square getTarget()
 	{
 		return this._target;
 	}
 	
 	public void calcNextMove()
 	{
 		AbstractPiece[] pieces = this.isWhiteTurn ? _whitePieces: _blackPieces;
		boolean isBeingChecked = this.isChecked();
 		int highestScore = -2000;		
 		Random random = new Random();
 		
 		for (AbstractPiece p : pieces)
 		{
 			if (p.isAlive())
 			{
 				try
 				{
 					Set<Square> moves = p.getMoves();
 					for (Square target : moves)
 					{
 						int score = 0; 
 						if (this.isPieceInDanger(p))
 						{
 							Log.i("SChess", "Piece in Danger");							
 
 							score = this.getPieceScore(p) - 1;
 
 						}
 						Square sourceLocation = p.getLocation();						
 						AbstractPiece capturedPiece = p.makeMove(target);
 						
						if (!isBeingChecked || (isBeingChecked && !this.isChecked()))
 						{
 							if (this.isPieceInDanger(p))
 							{
 								Log.i("SChess", "Piece in Danger again");								
 
 								score -= this.getPieceScore(p) - 1;
 
 							}
 							
 							if (capturedPiece != null)
 							{
 								Log.i("SChess", "Captured something");								
 								score += this.getPieceScore(capturedPiece);
 							}
 							
 							Log.i("SChess", score+" "+sourceLocation+" "+p.getLocation());
 							
 							if (score > highestScore || 
 								score == highestScore && random.nextDouble()>0.5)
 							{
 								highestScore = score;
 								this._selectingPiece = p;
 								this._target = target;
 								Log.i("SChess", "Selecting: "+this._selectingPiece+" "+this._target);
 							}
 						}
 						
 						unmakeMove(capturedPiece, p, target, sourceLocation);
 					}
 				}
 				catch (GameException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 		Log.i("SChess", "Selecting: "+this._selectingPiece+" "+this._target);
 	}
 	
 	private int getPieceScore(AbstractPiece p)
 	{
 		switch (p.getType().getValue())
 		{
 			//King(0), Queen(1), Rook(2), Knight(3), Bishop(4), Pawn(5);
 			case 0:
 				return 1000;				
 			case 1:
 				return 9;			
 			case 2:
 				return 5;
 			case 3:			
 			case 4:
 				return 3;
 			case 5:
 				return 1;
 			default:
 				return 0;
 		}		
 	}
 	
 	private boolean isPieceInDanger(AbstractPiece piece)
 	{
 		AbstractPiece[] enemyPieces = this.isWhiteTurn ? _blackPieces : _whitePieces;
 		for (AbstractPiece p : enemyPieces)
 		{
 			if (p.isAlive())
 			{
 				try {
 					Set<Square> moves;					
 					moves = p.getMoves();
 					for (Square m : moves)
 					{
 						if (m.equals(piece.getLocation()))
 						{
 							return true;
 						}
 					}
 				} catch (GameException e) {
 					e.printStackTrace();
 				}
 				
 			}
 		}
 
 		return false;		
 	}
 	
 	private boolean isChecked()
 	{
 		AbstractPiece[] enemyPieces = this.isWhiteTurn ? _blackPieces : _whitePieces;
 		for (AbstractPiece p : enemyPieces)
 		{
 			if (p.isAlive())
 			{
 				if (p.checkingKing())
 				{
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 	
 	private void unmakeMove(AbstractPiece capturedPiece, AbstractPiece actor,
 			Square destinationLocation, Square sourceLocation)
 	{
 		destinationLocation.removePiece();
 		if (capturedPiece != null)
 			capturedPiece.revive(destinationLocation);
 		actor.setLocation(sourceLocation);
 
 	}
 	
 }
