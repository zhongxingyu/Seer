 package edu.sjsu.cs.ghost151;
 
 import java.util.Random;
 
 /**
  * <b>Ghost</b> will communicate with other Ghosts to acquire a target and be
  * aware of areas already visited by each other. The Ghost will search for the
  * target (PacMan) and maintain a log of areas visited. If the target is
  * acquired, it will notify the Game.
  * 
  * @author Alben Cheung
  * @author MD Ashfaqul Islam
  * @author Shaun Guth
  * @author Jerry Phul
  */
 public class Ghost extends BoardObject {
 	private BoardObjectType exploredPositions[][];
 	private boolean targetAcquired = false;
 	private BoardPosition movePosition = null;
 	private Random generator;
 	
 	/**
 	 * Construct a Ghost object that is aware of positions its explored.
 	 */
 	public Ghost() {
		super(BoardObjectType.Ghost);
		exploredPositions = new BoardObjectType[Board.ROWS][Board.COLUMNS];
 	}
 	
 	public Ghost(Random generator) {
		this();
 		this.generator = generator;
 	}
 
 	/**
 	 * Get the explored positions.
 	 * 
 	 * @return the explored positions array
 	 */
 	public BoardObjectType[][] getExploredPositions() {
 		return exploredPositions;
 	}
 
 	/**
 	 * Set the explored positions for the object.
 	 * 
 	 * @param exploredPositions
 	 *            set the explored positions array
 	 */
 	public void setExploredPositions(BoardObjectType[][] exploredPositions) {
 		this.exploredPositions = exploredPositions;
 	}
 
 	/**
 	 * returns a boolean value if the target was acquired by the object.
 	 * 
 	 * @return boolean value
 	 */
 	public boolean IsTargetAcquired() {
 		return targetAcquired;
 	}
 
 	/**
 	 * Synchronize places explored between Ghost objects.
 	 * 
 	 * @param ghost
 	 *            the Ghost object to communicate with
 	 */
 	public void CommunicateWith(Ghost ghost) {
 		BoardObjectType[][] incoming = ghost.getExploredPositions();
 
 		for (int row = 0; row < incoming.length; ++row) {
 			for (int column = 0; column < incoming[row].length; ++column) {
 				if (exploredPositions[row][column] == null) {
 					exploredPositions[row][column] = incoming[row][column];
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update the Ghost object of surrounding area. If the target was seen, then
 	 * move to it else look for a Ghost to communicate with it. Finally, move to
 	 * a new location and update Ghost object's position.
 	 */
 	public void Scan() {
 		BoardObject[] surroundings = board.GetSurroundings(this);
 
 		for (int i = 0; i < surroundings.length; ++i) {
 			BoardObject object = surroundings[i];
 			BoardPosition position = object.getPosition();
 
 			// mark this position as explored with what we've seen there
 			exploredPositions[position.getRow()][position.getColumn()] = object
 					.getType();
 
 			if (object.getType() == BoardObjectType.Target) {
 				// move to the target space and acquire it
 				targetAcquired = true;
 				movePosition = position;
 			}
 
 			if (object.getType() == BoardObjectType.Ghost) {
 				// communicate with this ghost and vice-versa
 				this.CommunicateWith((Ghost) object);
 				((Ghost) object).CommunicateWith(this);
 				Game.INSTANCE.IncrementCommunicationsCount();
 			}
 		}
 
 		// if we haven't acquired the target during this scan, pick a random
 		// location to move to
 		// TODO: move more intelligently.
 		if (!targetAcquired) {
 			int randomSurrounding = generator.nextInt(surroundings.length);
 			movePosition = surroundings[randomSurrounding].getPosition();
 		}
 	}
 
 	public void Move() {
 		MoveTo(movePosition);
 	}
 
 	/**
 	 * Move an object from it's current position to a new position.
 	 * 
 	 * @param newPosition
 	 *            the object's new position
 	 */
 	private void MoveTo(BoardPosition newPosition) {
 		if (null == newPosition) {
 			return;
 		}
 
 		// we can't move into anything but empty space
 		if (board.GetObjectAt(newPosition).getType() != BoardObjectType.Empty) {
 			return;
 		}
 
 		BoardPosition oldPosition = this.getPosition();
 		board.SetObjectAt(oldPosition, new BoardObject(BoardObjectType.Empty));
 		board.SetObjectAt(newPosition, this);
 
 		Game.INSTANCE.IncrementMovementCount();
 	}
 }
