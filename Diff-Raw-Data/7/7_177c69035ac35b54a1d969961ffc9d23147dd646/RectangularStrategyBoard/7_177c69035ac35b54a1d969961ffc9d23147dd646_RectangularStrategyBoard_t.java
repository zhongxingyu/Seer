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
  *    gpollice
  *******************************************************************************/
 package strategy.common;
 
 import java.util.Iterator;
 import strategy.*;
 
 /**
  *
  * @author gpollice, Alex Thornton-Clark, Andrew Hurle, Gabriel Stern-Robbins
  * @version Sep 8, 2011
  */
 public class RectangularStrategyBoard implements StrategyBoard
 {
 	
 	private final Piece[][] pieces;
 	private final int numRows;
 	private final int numCols;
 	
 	/**
 	 * Constructor for a rectangular board.
 	 * 
 	 * @param rows The number of rows the board should have
 	 * @param columns The number of columns the board should have
 	 */
 	public RectangularStrategyBoard(int rows, int columns)
 	{
 		pieces = new Piece[rows][columns];
 		numRows = rows;
 		numCols = columns;
 		initializeBoard();
 	}
 	
 	/**
 	 * Constructor that initializes the board to a starting configuration.
 	 * @param rows
 	 * @param columns
 	 * @param startingRedPieces the starting configuration of Red pieces
 	 * @param startingBluePieces the starting configuration of Blue pieces
 	 */
 	public RectangularStrategyBoard(int rows, int columns,
 			PiecePositionAssociation[] startingRedPieces,
 			PiecePositionAssociation[] startingBluePieces)
 	{
 		this(rows, columns);
 		initializeBoard(startingRedPieces, startingBluePieces);
 		
 		
 	}
 
 	/**
 	 * Initialize the board to the starting configuration.
 	 * Sets every space on the board to the NULL_PIECE
 	 */
 	public void initializeBoard()
 	{
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numCols; j++) {
 				pieces[i][j] = Piece.NULL_PIECE;
 			}
 		}
 	}
 	
 	/**
 	 * Initialize the board to the starting configuration.
 	 * @param startingRedPieces the starting configuration of Red pieces
 	 * @param startingBluePieces the starting configuration of Blue pieces
 	 */
 	public void initializeBoard(PiecePositionAssociation[] startingRedPieces,
 			PiecePositionAssociation[] startingBluePieces)
 	{
 		initializeBoard();
 		
 		for (PiecePositionAssociation i : startingRedPieces) {
 			putPieceAt(i.getPosition(), i.getPiece());
 		}
 		
 		for (PiecePositionAssociation i : startingBluePieces) {
 			putPieceAt(i.getPosition(), i.getPiece());
 		}
 	}
 
 	
 	@Override
 	public Iterator<Piece> iterator()
 	{
 		return new BoardIterator(pieces);
 	}
 
 	@Override
 	public Piece getPieceAt(Position position)
 	{
 		validatePosition(position);
 		return pieces[position.getRow()][position.getColumn()];
 	}
 
 	@Override
 	public void putPieceAt(Position position, Piece piece)
 	{
 		validatePosition(position);
 		pieces[position.getRow()][position.getColumn()] = piece;
 	}
 
 	@Override
 	public boolean isOccupied(Position position)
 	{
 		return (!getPieceAt(position).equals(Piece.NULL_PIECE));
 	}
 
 	/**
 	 * @see strategy.StrategyBoard#getDistance(strategy.Position, strategy.Position)
 	 */
 	@Override
 	public int getDistance(Position from, Position to)
 	{
 		final int colDiff = from.getColumn() - to.getColumn();
 		final int rowDiff = from.getRow() - to.getRow();
 		
 		return (int) Math.sqrt(Math.pow(colDiff, 2) + Math.pow(rowDiff, 2));
 	}
 	
 	/**
 	 * Checks each space between source and destination to see if at least one is occupied.
 	 * Does not include source and destination, so adjacent spaces will always return false.
 	 * 
 	 * @param source The position to start checking after
 	 * @param destination The position to stop checking before
 	 * @return true if at least one space between the positions is occupied, false otherwise
 	 */
 	public boolean isOccupiedSpaceBetweenPositions(Position source, Position destination)
 	{
 		int horizontalStep = destination.getColumn() - source.getColumn();
 		int verticalStep = destination.getRow() - source.getRow();
 		if(horizontalStep != 0) {
 			horizontalStep /= Math.abs(horizontalStep);
 		}
 		if(verticalStep != 0) {
 			verticalStep /= Math.abs(verticalStep);
 		}
 		
 		Position currentPoint = new Position(source.getRow(), source.getColumn());
 		boolean keepGoing = true;
 		while(keepGoing) {
 			currentPoint = new Position(currentPoint.getRow() + verticalStep,
 										currentPoint.getColumn() + horizontalStep);
 			if(currentPoint.equals(destination)) {
 				keepGoing = false;
 			}
 			else if(isOccupied(currentPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public void validatePosition(Position pos)
 	{
 		if(pos.getRow() >= numRows || pos.getRow() < 0) {
			throw new ArrayIndexOutOfBoundsException(
					"row must be greater than 0 and less than " + numRows);
 		}
 		if(pos.getColumn() >= numCols || pos.getColumn() < 0) {
			throw new ArrayIndexOutOfBoundsException(
					"col must be greater than 0 and less than " + numCols);
 		}
 	}
 	
 	/**
 	 * Returns the number of rows this RectangularStrategyBoard has
 	 * 
 	 * @return numRows
 	 */
 	public int getNumRows() {
 		return numRows;
 	}
 
 	/**
 	 * Returns the number of columns this RectangularStrategyBoard has
 	 * 
 	 * @return numCols
 	 */
 	public int getNumCols() {
 		return numCols;
 	}
 	
 	@Override
 	public String toString()
 	{
 		String boardString = "";
 		final String[] boardArr = {"", "", "", "", "", ""};
 		final BoardIterator iter = new BoardIterator(pieces);
 		Piece tempPiece = null;
 		
 		int colCount = 0;
 		int row = 0;
 		
 		while (iter.hasNext()) {
 			if (!(tempPiece = iter.next()).equals(Piece.NULL_PIECE)) {
 				boardString += tempPiece.getType().getId();
 			}
 			else {
 				boardString += "N";
 			}
 			
 			colCount++;
 			
 			if (colCount > numCols - 1) {
 				boardString += "\n";
 				colCount = 0;
 				boardArr[row] = boardString;
 				boardString = "";
 				row++;
 			}
 		}
 		
 		for (int i = 1; i <= 6; i++) {
 			boardString += boardArr[numRows - i];
 		}
 		
 		return boardString;
 	}
 	
 	/**
 	 * Get the number of pieces of both colors currently on the board
 	 * 
 	 * @return numPieces
 	 */
 	public int getNumPiecesOnBoard() {
 		int numPieces = 0;
 		final Iterator<Piece> iter = iterator();
 		
 		while(iter.hasNext()) {
 			if (!iter.next().equals(Piece.NULL_PIECE)) {
 				numPieces++;
 			}
 		}
 		
 		return numPieces;
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if(this == other) {
 			return true;
 		}
 		if (other instanceof RectangularStrategyBoard) {
 			final RectangularStrategyBoard that = (RectangularStrategyBoard) other;
 			if (that.getNumCols() != numCols || that.getNumRows() != numRows) {
 				return false;
 			}
 			boolean same = true;
 			final Iterator<Piece> thatIter = that.iterator();
 			final Iterator<Piece> myIter = iterator();
 			
 			while(myIter.hasNext() && thatIter.hasNext()) {
 				if (!myIter.next().equals(thatIter.next())) {
 					same = false;
 				}
 			}
 			
 			return same;
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		return 1;
 	}
 }
