 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  * 
  * Copyright (c) 2011 Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Author: Alex Thornton-Clark, Andrew Hurle, Gabriel Stern-Robbins
  *******************************************************************************/
 package strategy;
 
 import strategy.common.*;
 
 import strategy.alpha.AlphaStrategyGame;
 import strategy.beta.BetaStrategyGame;
 import strategy.delta.DeltaStrategyGame;
 
 /**
  * A factory singleton which creates various StrategyGame implementations.
  * Not very useful, but we were required to implement it :)
  * 
  * @author Andrew Hurle, Alex Thornton-Clark, Gabriel Stern-Robbins
  * @version Sep 29, 2011
  */
 public class StrategyGameFactory {
 	
 	private static final StrategyGameFactory instance = new StrategyGameFactory();
 	
 	public static StrategyGameFactory getInstance() {
 		return instance;
 	}
 	
 	//note that codepro doesn't like the below methods - Pollice said he'd fix it or something
 	//since he doesn't want the methods to be static
 	
 	/**
 	 * @return An AlphaStrategyGame
 	 */
	public AlphaStrategyGame makeAlphaStrategyGame() {
 		return new AlphaStrategyGame();
 	}
 	
 	/**
 	 * @return A BetaStrategyGame
 	 */
	public BetaStrategyGame makeBetaStrategyGame() {
 		return new BetaStrategyGame();
 	}
 	
 	/**
 	 * @see DeltaStrategyGame#DeltaStrategyGame
 	 * @param startingRedPieces
 	 * @param startingBluePieces
 	 * @return A DeltaStrategyGame
 	 */
	public DeltaStrategyGame makeDeltaStrategyGame(PiecePositionAssociation[] startingRedPieces,
 				PiecePositionAssociation[] startingBluePieces) {
 		return new DeltaStrategyGame(startingRedPieces, startingBluePieces);
 	}
 }
