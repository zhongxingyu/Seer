 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  *
  * Copyright (c) 2011 Worcester Polytechnic Institute.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Author:
  *    Alex Thornton-Clark, Andrew Hurle, Gabriel Stern-Robbins
  *******************************************************************************/
 package strategy.beta;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import strategy.Piece;
 import strategy.PieceType;
 import strategy.PlayerColor;
 import strategy.Position;
 import strategy.RulesStrategy;
 import strategy.StrategyBoard;
 import strategy.StrategyException;
 import strategy.common.RectangularStrategyBoard;
 
 /**
  * @author Alex Thornton-Clark, Andrew Hurle, Gabriel Stern-Robbins
  * @version Sep 15, 2011
  */
 public class BetaRulesStrategy extends RulesStrategy {
 
 	protected RectangularStrategyBoard board;
 	private int numMoves;
 	private boolean playerCanPlacePiece;
 	private Set<Piece> placedPieces;
 	
 	/**
 	 * Default constructor. Initializes the game.
 	 * Players are not allowed to place their pieces.  They are randomized instead.
 	 * @throws StrategyException 
 	 */
 	public BetaRulesStrategy() throws StrategyException
 	{
 		this(false);
 	}
 	
 	/**
 	 * Constructor that accepts a boolean indicating whether players
 	 * can place their pieces in this game or not.
 	 * 
 	 * @param playerPlacePiece true if players can place pieces on the board where they choose
 	 * @throws StrategyException if something goes wrong when initializing
 	 */
 	public BetaRulesStrategy(boolean playerPlacePiece) throws StrategyException {
 		board = new RectangularStrategyBoard(6, 6);
 		playerCanPlacePiece = playerPlacePiece;
 		initialize();
 	}
 	
 	@Override
 	public void initialize() throws StrategyException {
 		board = new RectangularStrategyBoard(6, 6);
 		numMoves = 0;
 		winnerColor = null;
 		turnColor = PlayerColor.RED;
 		if (!playerCanPlacePiece) {
 			placePiecesByColor(PlayerColor.RED);
 			placePiecesByColor(PlayerColor.BLUE);
 			placedPieces = null;
 		}
 		else {
 			placedPieces = new HashSet<Piece>();
 		}
 
 	}
 	
 
 	@Override
 	public Piece makeMove(Position source, Position destination)
 			throws StrategyException {
 		if(source.equals(destination)) {
 			throw new StrategyException("destination must be different from source");
 		}
 		if(!board.isOccupied(source)) {
 			throw new StrategyException("source must be occupied by a piece");
 		}
 		if(isOver()){
 			throw new StrategyException("Game is already over");
 		}
 		
 		//change turn/dont double move
 		if(board.getPieceAt(source).getColor() != turnColor){
 			throw new StrategyException("not your turn");
 		}
 		
 		if(turnColor == PlayerColor.RED){
 			turnColor=PlayerColor.BLUE;
 		}
 		else{
 			turnColor=PlayerColor.RED;
 		}
 		
 		final Piece sourcePiece = board.getPieceAt(source);
 		final Piece destinationPiece = board.getPieceAt(destination);
 		
 		final int distance = board.getDistance(source, destination); //checks for diagonal
 		final int range = sourcePiece.getType().getRange();
 		if(range >= 0 && distance > range) {
 			throw new StrategyException("Cannot move piece farther than its range");
 		}
 		
 		if (sourcePiece.getColor().equals(destinationPiece.getColor())) {
 			throw new StrategyException("Cannot move onto a friendly piece");
 		}
 
 		if (board.isOccupiedSpaceBetweenPositions(source, destination)) {
 			throw new StrategyException("Cannot move through occupied spaces");
 		}
 
 		BattleResult result = BattleResult.VICTORY;
 		if (board.isOccupied(destination)) {
 			result = resolveBattle(sourcePiece, destinationPiece);
 		}
 
 		switch (result) {
 		case VICTORY:
 			board.putPieceAt(destination, sourcePiece);
 			board.putPieceAt(source, Piece.NULL_PIECE);
 			break;
 		case DRAW:
 			board.putPieceAt(destination, Piece.NULL_PIECE);
 			// fallthrough
 		case DEFEAT:
 			board.putPieceAt(source, Piece.NULL_PIECE);
 			break;
 		}
 		//if moves>10 end game
 		numMoves++;
 		if(numMoves >= 10){
 			winnerColor = PlayerColor.BLUE;
 		}
 		return board.getPieceAt(destination);
 	}
 
 	@Override
 	public BattleResult resolveBattle(Piece attacker, Piece defender) {
 		if(defender.getType().equals(PieceType.FLAG)){
 			winnerColor=attacker.getColor();
 			return BattleResult.VICTORY;
 		}
 		if(attacker.getType().equals(PieceType.MINER)
 				&& defender.getType().equals(PieceType.BOMB)) {
 			return BattleResult.VICTORY;
 		}
 		if(attacker.getType().equals(PieceType.SPY)
 				&& defender.getType().equals(PieceType.MARSHAL)) {
 			return BattleResult.VICTORY;
 		}
 		
 		if(attacker.getType().getRank() < defender.getType().getRank()) {
 			return BattleResult.VICTORY;
 		}
 		if(attacker.getType().getRank() == defender.getType().getRank()) {
 			return BattleResult.DRAW;
 		}
 		return BattleResult.DEFEAT;
 	}
 	
 	/**
 	 * Get the number of pieces of both colors currently on the board
 	 * 
 	 * @return numPieces
 	 */
 	public int getNumPiecesOnBoard() {
 		int numPieces = 0;
 		final Iterator<Piece> iter = board.iterator();
 		
 		while(iter.hasNext()) {
 			if (!iter.next().equals(Piece.NULL_PIECE)) {
 				numPieces++;
 			}
 		}
 		
 		return numPieces;
 	}
 	
 	private void placePiecesByColor(PlayerColor pieceColor) throws StrategyException {
 		for (PieceType type : PieceType.values()) {
 			Piece pieceToPlace = new Piece(type, pieceColor);
 			randomlyPlacePiece(pieceToPlace);
 		}
 	}
 	
 	private void randomlyPlacePiece(Piece pieceToPlace) throws StrategyException {
 		final Random randGen = new Random();
 		Position randPos = null;
 		
 		
 		if (pieceToPlace.getColor() == PlayerColor.RED) {
 			do {
 				randPos = new Position(randGen.nextInt(2), randGen.nextInt(board.getNumCols()));
 			} while (board.isOccupied(randPos));
 		}
 		else {
 			do {
 				randPos = new Position((board.getNumRows() - 1) - randGen.nextInt(2), 
 						randGen.nextInt(board.getNumCols()));
 			} while (board.isOccupied(randPos));
 		}
 			
 		board.putPieceAt(randPos, pieceToPlace);
 	}
 	
 	public void playerPlacePiece(Position position, Piece piece) throws StrategyException {
 		if (!playerCanPlacePiece) {
 			throw new StrategyException("Player not allowed to place Piece");
 		}
 		if (board.isOccupied(position)) {
 			throw new StrategyException("Player not allowed to place Piece in an occupied space");
 		}
 		if (!placedPieces.add(piece)) {
 			throw new StrategyException("That Piece has already been placed");
 		}
 		if (piece.getColor() == PlayerColor.RED && position.getRow() > 1) {
 			throw new StrategyException("Given position is outside RED player's setup zone");
 		}
 		if (piece.getColor() == PlayerColor.BLUE && position.getRow() < 4) {
 			throw new StrategyException("Given position is outside BLUE player's setup zone");
 		}
 		board.putPieceAt(position, piece);
 	}
 	
 	/**
 	 * Set the current StrategyBoard to the given StrategyBoard
 	 * 
 	 * @param board
 	 */
 	protected void setBoard(StrategyBoard board)
 	{
 		this.board = (RectangularStrategyBoard) board;
 	}
 	
 	/**
 	 * Return this BetaStrategyGame's RectangularStrategyBoard
 	 * 
 	 * @return board
 	 */
 	protected RectangularStrategyBoard getBoard() {
 		return board;
 	}
 	
 	/**
 	 * Accessor for getting a piece at a specific row and column.
 	 * 
 	 * @param pos
 	 *            the Position of the piece
 	 * @return the piece at the specified row and column
 	 * @throws StrategyException if coordinates are out-of-bounds
 	 */
 	public Piece getPieceAt(Position pos) throws StrategyException {
 		return board.getPieceAt(pos);
 	}
 	
 	@Override
 	public boolean equals(Object other)
 	{
 		if(this == other) {
 			return true;
 		}
 		if (other instanceof BetaRulesStrategy) {
 			final BetaRulesStrategy that = (BetaRulesStrategy) other;
 			return isOver() == that.isOver() && getWinner() == that.getWinner()
 					&& board.equals(that.getBoard());
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + board.hashCode();
 		result = prime * result + numMoves;
 		result = prime * result + (playerCanPlacePiece ? 0 : 1);
 		result = prime * result + placedPieces.hashCode();
 		return result;
 	}
 
 }
