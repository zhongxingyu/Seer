 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  * 
  * Copyright (c) 2011 Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Author: gpollice
  *******************************************************************************/
 package strategy;
 
 /**
  * This interface defines the behavior expected of all versions of Strategy, except AlphaStrategy.
  * 
  * @author gpollice
  * @version Aug 1, 2011
  */
 public interface StrategyGame
 {
 	/**
 	 * Initialize (reset) the game.
 	 * @throws StrategyException if something goes wrong when initializing
 	 */
 	void initializeGame() throws StrategyException;
 
 	/**
 	 * @param source
 	 *            the coordinate of the moving piece
 	 * @param destination
 	 *            the coordinate of space the piece wants to move to
 	 * @return the Piece that is at the destination space after the move is complete
 	 * @throws StrategyException
 	 *             if:
 	 *             <ul>
 	 *             <li>There is no piece of the moving player's color on the source space</li>
 	 *             <li>The piece cannot legally move to the destination</li>
 	 *             </ul>
 	 */
 	Piece move(Position source, Position destination) throws StrategyException;
 
 	/**
 	 * @return true if the game is over
 	 */
 	boolean isGameOver();
 
 	/**
 	 * If there is a winner, return the winner's color.
 	 * 
 	 * @return the winner's color if there is a winner or null if the game is not over or the game
 	 *         ended in a draw.
 	 */
 	PlayerColor getWinner();
 	
 	/**
 	 * Get the piece currently at the specified position
	 * @param position
 	 * @return the piece at that position
 	 * @throws StrategyException if the position is invalid
 	 */
 	Piece getPieceAt(Position position) throws StrategyException;
 }
