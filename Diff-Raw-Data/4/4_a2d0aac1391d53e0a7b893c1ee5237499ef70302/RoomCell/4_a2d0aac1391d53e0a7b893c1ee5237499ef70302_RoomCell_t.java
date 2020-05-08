 package clueGame;
 
 public class RoomCell extends BoardCell {
 
 	public enum DoorDirection {
 		UP, DOWN, LEFT, RIGHT, NONE
 	}
 
 	private DoorDirection doorDirection;
 	private char initial;
 
 	public RoomCell(int row, int column, char initial, DoorDirection doorDirection) {
 		this.row = row;
 		this.column = column;
 		this.initial = initial;
 		this.doorDirection = doorDirection;
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
		return doorDirection != DoorDirection.NONE;
 	}
 }
