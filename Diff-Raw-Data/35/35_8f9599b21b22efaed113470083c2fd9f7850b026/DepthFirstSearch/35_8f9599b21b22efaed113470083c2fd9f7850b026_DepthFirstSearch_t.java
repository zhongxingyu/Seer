 package ch.zhaw.labyrinth.builder;
 
 import ch.zhaw.labyrinth.utils.Coordinate;
 import ch.zhaw.labyrinth.utils.Labyrinth;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 /**
  * This implements a DeepFirstSearch algorithm like descripted
  * on the following website: http://www.migapro.com/depth-first-search/
  * @author Dominic Schlegel
  */
 public class DepthFirstSearch extends Labyrinth {
   
 	private static final boolean WALL = false;
 	private static final boolean PATH = true;
 	
 	private Integer[] dir;
 
 	// Constructor
 	public DepthFirstSearch(int n){
 		
 		// call super constructor
 		super(n);
 		
 		// now setup everything
 		setupMaze();
 	}
 	
 	public void setupMaze() {
 
 		// set start point in array
 		int x = getRandomIntOdd(getDimension());
 		int y = getRandomIntOdd(getDimension());
 		setCellValue(x,y,PATH);
 				
 		// create whole maze array
 		createMaze(x,y);
 		
 		// create input and ouput of the maze
 		createInputOutput();
 
 	}
 
 	private void createMaze(int x, int y) {
 		
 		// create directions 
 		this.dir = generateRandomDirections();
 		
 		for (int i = 0; i < 4; i++) {
 			
 			// dir = 0 --> move up
 			if (dir[i] == 0){
 				moveUp(x,y);
 			}
 			// dir = 1 --> move right
 			if (dir[i] == 1){
 				moveRight(x,y);
 			}
 			// dir = 2 --> move down
 			if (dir[i] == 2){
 				moveDown(x,y);
 			}
 			// dir = 3 --> move left
 			if (dir[i] == 3){
 				moveLeft(x,y);
 			}
 		}
 	}
 
 	// TODO: create one move method/class to avoid code duplication
 	
 	private void moveUp(int x, int y) {
 		
 		// check if new value is not outside the matrix and 
 		// check if 2 cells ahead is a wall
		if ( y - 2 > 0 && !getCellValueAt(x, y - 2)){
 			
 			setCellValue(x, y - 1, PATH);
 			setCellValue(x, y - 2, PATH);
 				
 			createMaze(x,y-2);
 		}		
 	}
 
 	private void moveRight(int x, int y) {
 		
 		// check if new value is not outside the matrix and
 		// check if 2 cells ahead is a wall
		if ( x + 2 < getDimension() && !getCellValueAt(x + 2, y)){
 			
 			setCellValue(x + 1, y, PATH);
 			setCellValue(x + 2, y, PATH);
 				
 			createMaze(x + 2,y);
 		}
 	}
 
 	private void moveDown(int x, int y) {
 		// check if new value is not outside the matrix and
 		// check if 2 cells ahead is a wall
		if ( y + 2 < getDimension() && !getCellValueAt(x, y + 2)){
 						
 			setCellValue(x,y+1,PATH);
 			setCellValue(x,y+2,PATH);
 										
 			createMaze(x,y+2);
 		}	
 	}
 
 	private void moveLeft(int x, int y){
 		// check if new value is not outside the matrix and 
 		// check if 2 cells ahead is a wall
		if ( x - 2 > 0 && !getCellValueAt(x - 2, y)){
 			
 			setCellValue(x-1,y,PATH);
 			setCellValue(x-2,y,PATH);
 							
 			createMaze(x-2,y);
 		}
 	}
 	
 	public Integer[] generateRandomDirections() {
 	      ArrayList<Integer> randoms = new ArrayList<Integer>();
 	      for (int i = 0; i < 4; i++){
 	           randoms.add(i);
 	      }
 	      Collections.shuffle(randoms);
 	 
 	     return randoms.toArray(new Integer[4]);
 	 }
 	
 	
 	public void createInputOutput(){
 		
 		// create random
 		Random rand = new Random();
 		int r = rand.nextInt(2);
 			
 		// get a random for entry and one for output
 		int rEntry = rand.nextInt(getDimension());
 		int rOut = rand.nextInt(getDimension());
 			
 		// r = 0 => entry is at the top of the maze
 		if ( r == 0){
 			
 			// check if 1 cell ahead entry is free
			while(!getCellValueAt(1, rEntry)){
 				rEntry = rand.nextInt(getDimension());
 			}
 			
 			// check if 1 cell ahead output is free
			while(!getCellValueAt(getDimension() - 2, rOut)){
 				rOut = rand.nextInt(getDimension());
 			}
 		
 			// now make the field a path
			setCellValue(rEntry, 0, PATH);
			setCellValue(rOut, getDimension() - 1, PATH);
 
             // Save entry & exit
             setEntry(new Coordinate(rEntry, 0));
             setExit(new Coordinate(rOut, getDimension() - 1));
 		}
 		
 		// r = 1 => entry is on the left site
 		if ( r == 1){
 			
 			// check if 1 cell ahead entry is free
			while(!getCellValueAt(rEntry, 1)){
 				rEntry = rand.nextInt(getDimension());
 			}
 			
 			// check if 1 cell ahead output is free
			while(!getCellValueAt(rOut, getDimension() - 2)){
 				rOut = rand.nextInt(getDimension());
 			}
 		
 			// now make the field a path
			setCellValue(0, rEntry, PATH);
			setCellValue(getDimension() - 1, rOut, PATH);
 
             // Save entry & exit
             setEntry(new Coordinate(0, rEntry));
             setExit(new Coordinate(getDimension() - 1, rOut));
 		}
 
 
 	}
 	
 }
