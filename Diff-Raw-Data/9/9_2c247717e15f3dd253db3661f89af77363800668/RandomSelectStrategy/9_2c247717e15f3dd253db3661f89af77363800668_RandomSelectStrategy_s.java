 /**
  * This file was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package hanto.studentndemarinis.tournament;
 
 import hanto.studentndemarinis.common.InternalHantoGame;
 import hanto.tournament.HantoMoveRecord;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class contains the behavior for a Hanto Player
  * strategy that randomly selects a move.  
  * 
  * @author ndemarinis
  * @version Feb 26, 2013
  */
 public class RandomSelectStrategy implements HantoPlayerStrategy {
 
 	// True if our strategy can select places or moves, respectively
 	boolean selectPlaces, selectMoves;
 	
 	/**
 	 * Initialize a random strategy
 	 * @param selectPlaces true if we want it to pick moves that place pieces
 	 * @param selectMoves true if we want it to pick moves that move pieces
 	 */
 	public RandomSelectStrategy(boolean selectPlaces, boolean selectMoves) {
 		this.selectPlaces = selectPlaces;
 		this.selectMoves = selectMoves;
 	}
 	
 	/**
 	 * Initialize a random strategy
 	 */
 	public RandomSelectStrategy() {
 		this(true, true);
 	}
 
 	public	HantoMoveRecord selectMove(InternalHantoGame game, 
 			List<HantoMoveRecord> allPossibleMoves) {
 		
 		List<HantoMoveRecord> possibleMovesBasedOnContraints = 
 				new ArrayList<HantoMoveRecord>();
 		
 		// Don't bother parsing the list to remove pieces if we selected both.  
 		if(!selectPlaces || !selectMoves) {
 			for(HantoMoveRecord r : allPossibleMoves) {
 				
 				// If this is a place and we wanted places, add it.  
 				if(selectPlaces && r.getFrom() == null) {
 					possibleMovesBasedOnContraints.add(r);
 				}
 				
 				// If this is a move and we wanted moves, add it.  
 				if(selectMoves && r.getFrom() != null) {
 					possibleMovesBasedOnContraints.add(r);
 				}
 				
 			}
 		}
 		else {
 			// If we selected everything, just use the original list.  
 			possibleMovesBasedOnContraints = allPossibleMoves;
 		}
 		
 		// Then just randomly select a move.  That's it.  
 		// If the list after constraining is empty, we need to resign, so return null.  
 		return possibleMovesBasedOnContraints.isEmpty() ? null :
 			possibleMovesBasedOnContraints.get((int)(Math.random() * 
 				possibleMovesBasedOnContraints.size()));
 	}
 }
