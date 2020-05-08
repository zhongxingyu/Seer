 package org.p_one.deathmaze;
 
 import org.p_one.deathmaze.InvalidRoomConnection;
 
 public class Room {
 	public boolean north, east, south, west;
 	public int x, y;
 	public Room(int x, int y, boolean north, boolean east, boolean south, boolean west) {
 		this.north = north;
 		this.east = east;
 		this.south = south;
 		this.west = west;
 
 		this.x = x;
 		this.y = y;
 	}
 
 	public void rotate() {
 		boolean old_west = this.west;
 		this.west = this.south;
 		this.south = this.east;
 		this.east = this.north;
 		this.north = old_west;
 	}
 
 	public boolean connected(Room otherRoom) throws InvalidRoomConnection{
 		int xDiff = this.x - otherRoom.x;
 		int yDiff = this.y - otherRoom.y;
 
 		boolean myExit = false, otherExit = false;
 
 		if(yDiff == -1) {
 			myExit = this.south;
			otherExit = otherRoom.north;
 		} else if(yDiff == 1) {
 			myExit = this.north;
 			otherExit = otherRoom.south;
 		} else if(xDiff == -1) {
 			myExit = this.east;
 			otherExit = otherRoom.west;
 		} else if(xDiff == 1) {
 			myExit = this.west;
 			otherExit = otherRoom.east;
 		}
 
 		if(myExit != otherExit) {
 			throw new InvalidRoomConnection();
 		}
 		return myExit && otherExit;
 	}
 }
