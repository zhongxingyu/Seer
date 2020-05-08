 package clueGame;
 
 public class RoomCell extends BoardCell {
 
 	public enum DoorDirection {
 		UP, DOWN, LEFT, RIGHT, NONE
 	}
 
 	private DoorDirection doorDirection;
 	private char initial;
 
 	public RoomCell(int col, int ro) {
 		column = col;
 		row = ro;
 	}
 
 	public DoorDirection getDoorDirection() {
 		return doorDirection;
 	}
 
 	public char getInitial() {
 		return initial;
 	}
 
 	@Override
 	public boolean isRoom() {
 		return true;
 	}
	@Override
	public boolean isDoorway() {
		return !doorDirection.equals(DoorDirection.NONE);
	}
 }
