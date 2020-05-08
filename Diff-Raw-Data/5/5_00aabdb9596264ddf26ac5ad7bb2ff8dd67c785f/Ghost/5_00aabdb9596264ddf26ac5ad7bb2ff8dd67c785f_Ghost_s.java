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
 	private Random generator;
 	private BoardObject[] surroundings = new BoardObject[0];
 
 	/**
 	 * Construct a Ghost object that is aware of positions its explored.
 	 */
 	public Ghost() {
 		this(new Random());
 	}
 
 	public Ghost(Random generator) {
 		super(BoardObjectType.Ghost);
 		exploredPositions = new BoardObjectType[Board.ROWS][Board.COLUMNS];
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
 	private void CommunicateWith(Ghost ghost) {
 		BoardObjectType[][] incoming = ghost.getExploredPositions();
 
 		for (int row = 0; row < incoming.length; ++row) {
 			for (int column = 0; column < incoming[row].length; ++column) {
 				if (incoming[row][column] != null
 						&& exploredPositions[row][column] == null) {
 					exploredPositions[row][column] = incoming[row][column];
 				}
 			}
 		}
 	}
 
 	/**
 	 * Part of the Update() loop - update our exploredPositions matrix
 	 */
 	public void Scan() {
 		surroundings = board.GetSurroundings(this);
 
 		for (int i = 0; i < surroundings.length; ++i) {
 			BoardObject object = surroundings[i];
 			BoardPosition position = object.getPosition();
 			BoardObjectType type = object.getType();
 
 			exploredPositions[position.getRow()][position.getColumn()] = type;
 		}
 	}
 
 	/**
 	 * Part of the Update() loop - communicates with any ghosts in the surroundings
	 * @pre Requires Scan() to have updated the surroundings array
 	 */
 	public void Communicate() {
 		for (BoardObject object : surroundings) {
 			if (object.getType() == BoardObjectType.Ghost) {
 				// 	communicate with this ghost and vice-versa
 				this.CommunicateWith((Ghost)object);
 				Game.INSTANCE.IncrementCommunicationsCount();
 			}
 		}
 	}
 
 	/**
 	 * Part of the Update() loop - moves to the targeted position
	 * @pre Requires Scan() to have updated the surroundings array
 	 */
 	public void Move() {
 		// TODO: move more intelligently.
 		for (BoardObject object : surroundings) {
 			if (object.getType() == BoardObjectType.Target) {
 				targetAcquired = true;
 				MoveTo(object.getPosition());
 				return;
 			}
 		}
 		
 		if (!targetAcquired) {
 			if (!targetAcquired) {
 				int randomSurrounding = generator.nextInt(surroundings.length);
 				MoveTo(surroundings[randomSurrounding].getPosition());
 				return;
 			}
 		}
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
