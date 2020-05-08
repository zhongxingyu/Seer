 package test;
 
 import static org.junit.Assert.*;
 
 
 import model.Maze;
 import model.Room;
 
 import org.junit.Test;
 
 
 public class MazeTest {
 	
 	@Test
 	public void mazeIsInitiated() throws Exception {
 		Maze maze = new Maze(5);
 		assertEquals(25, maze.getNumberOfRooms());
 		maze = new Maze(6);
 		assertEquals(36, maze.getNumberOfRooms());
 	}
 	
 	@Test
 	public void theFarSoutMiddleRoomIsChosenAsEntrance() throws Exception {
 		Maze maze = new Maze(5);
 		Room entrance = maze.getRoom(4, 2);
 		assertTrue(entrance.isEntrance());
 		maze = new Maze(6);
 		entrance = maze.getRoom(5, 2);
 		assertTrue(entrance.isEntrance());
 		maze = new Maze(7);
 		entrance = maze.getRoom(6, 3);
 		assertTrue(entrance.isEntrance());
 	}
 	
 	@Test
 	public void roomNorthOfEntranceIsPath() throws Exception {
 		Maze maze = new Maze(5);
 		Room northOfEntrance = maze.getRoom(3, 2);
 		assertTrue(northOfEntrance.isPath());
 	}
 	
 	@Test
 	public void outerWallsAreWalls() throws Exception {
 		Maze maze = new Maze(5);
 		for(int row = 0; row < 5; row++) {
 			for(int column = 0; column < 5; column++) {
 				if(row == 0) {
 					assertTrue(maze.getRoom(row,column).isWall());
 				}
 				if(row == 4){
 					assertTrue(maze.getRoom(row,column).isWall());
 				}
 				
 				if(column == 0){
 					assertTrue(maze.getRoom(row,column).isWall());
 				}
 				
 				if(column == 4){
 					assertTrue(maze.getRoom(row,column).isWall());
 				}
 				
 			}
 		}
 		maze = new Maze(7);
		for(int row = 0; row < 6; row++) {
			for(int column = 0; column < 6; column++) {
				if(row == 0 || row == 6 || column == 0 || column == 6) {
 					assertTrue(maze.getRoom(row,column).isWall());
 				}
 			}
 		}
 	}
 }
