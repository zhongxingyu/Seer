 package ClueBoard;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class RoomCell extends BoardCell {
 	public enum DoorDirection {UP, DOWN, LEFT, RIGHT, NONE};
 	
 	private DoorDirection doorDirection;
 	private char roomInitial;
 	
 	public RoomCell (String temp){
 		roomInitial = temp.charAt(0);
 		if(temp.length() > 1){
 			char direction = temp.charAt(1);
 			if(direction == 'U') doorDirection = DoorDirection.UP;
 			if(direction == 'D') doorDirection = DoorDirection.DOWN;
 			if(direction == 'L') doorDirection = DoorDirection.LEFT;
 			if(direction == 'R') doorDirection = DoorDirection.RIGHT;
 		}else{
 			doorDirection=DoorDirection.NONE;
 		}
 		
 	}
 	
 	public boolean isRoom() {
 		return true;
 	}
 	
 	public boolean isDoorway(){
 		if(doorDirection!=DoorDirection.NONE){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public char getRoomInitial() {
 		return roomInitial;
 	}
 	
 	public DoorDirection getDoorDirection() {
 		return doorDirection;
 	}
 
 
 	public void draw(Graphics g, int size, int x, int y) {
 		if(isDoorway()) {
 			g.setColor(Color.GREEN);
 			if(doorDirection == doorDirection.UP) {
 				g.fillRect(x * size, y * size, size, 10);
 			} else if (doorDirection == doorDirection.DOWN) {
				g.fillRect(x * size, (y * size) + (size - 10), size, 10);
 			} else if (doorDirection == doorDirection.LEFT) {
 				g.fillRect(x * size, y * size, 10, size);
 			} else if (doorDirection == doorDirection.RIGHT) {
				g.fillRect((x * size) + (size - 10) ,  y * size, 10, size);
 			}
 		}
 	}
 }
