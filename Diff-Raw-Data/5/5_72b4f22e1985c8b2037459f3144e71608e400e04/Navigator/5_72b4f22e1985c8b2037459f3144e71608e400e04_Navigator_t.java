 package navigation;
 
 /**
  * Sets a path on the wavefront grid and navigates tile per tile to destination
  * 
  * @author Team 13
  * 
  */
 
 import lejos.nxt.UltrasonicSensor;
 import lejos.util.Delay;
 import odometry.Odometer;
 import navigation.Pilot;
 import navigation.Map;
 
 public class Navigator extends Pilot{
 	private Map myMap;
 	UltrasonicSensor USSensor;
 	
 	public Navigator(Odometer odo, Map map, UltrasonicSensor USSensor){
 		super(odo);
 		this.myMap = map;
 		this.USSensor = USSensor;
 	}
 	
 	public void navigateTo(double destX, double destY){
 		
 		int [][] grid = setPath(destX, destY); // create the wavefront grid
 		
 		navigatePath(grid); //navigate the wavefront grid until goal is reached
 
 	}
 	
 	public int[][] setPath(double destX, double destY){
 		
 		// copy grid to a temporary grid for traveling purposes
 		int [][] grid = new int[10][10];
 		for(int i=0;i<myMap.getGrid().length;i++){
 			for(int j=0;j<myMap.getGrid()[i].length;j++){
 				grid[i][j] = myMap.getGrid()[i][j];
 			}
 		}
 		
 		// retrieve the positions in terms of grid coordinates
 		int currentI = myMap.currentI();
 		int currentJ = myMap.currentJ();
 		int destJ = myMap.destJ(destX);
 		int destI = myMap.destI(destY);
 		
 		// set the goal on the grid map
 		grid[destI][destJ]=2;
 		
 		// set the path on the grid map
 		boolean foundWave = true;
 		int currentWave = 2; //Looking for goal first
 		while(foundWave == true){
 			foundWave = false;
 			for(int y=0; y<grid.length; y++){
 				for(int x=0;x<grid[y].length; x++){
 					if(grid[x][y] == currentWave){
 						foundWave = true;
 						int goal_x = x;
 						int goal_y = y;
 						
 						//This code checks the NORTH direction
 						if(goal_x > 0 && grid[goal_x-1][goal_y] == 0){ //This code checks the array bounds heading NORTH
 							grid[goal_x-1][goal_y] = currentWave + 1;
 						}
 						
 						//This code checks the SOUTH direction
 						if(goal_x < (10 - 1) && grid[goal_x+1][goal_y] == 0){ //This code checks the array bounds heading SOUTH
 							grid[goal_x+1][goal_y] = currentWave + 1;
 						}
 
 						//This code checks the WEST direction
 						if(goal_y > 0 && grid[goal_x][goal_y-1] == 0){//This code checks the array bounds heading WEST
 							grid[goal_x][goal_y-1] = currentWave + 1;
 						}
 
 						//This code checks the EAST direction
 						if(goal_y < (10 - 1) && grid[goal_x][goal_y+1] == 0){//This code checks the array bounds heading EAST
 							grid[goal_x][goal_y+1] = currentWave + 1;
 						}
 					}
 				}
 			}
 			currentWave++;
 		}
 		
 		// set the robot on the grid map
 		grid[currentI][currentJ]=99;
 		
 		return grid;
 	}
 	
 	public void navigatePath(int [][] grid){
 		int robot_I = 0;
 		int robot_J = 0;
 		
 		// First - find robot location by grid
 		for(int i=0; i < grid.length; i++){
 			for(int j=0; j < grid[i].length; j++){
 				if(grid[i][j] == 99){
 					robot_I = i;
 					robot_J = j;
 				}
 			}
 		}
 		
 		// Second - Found robot location, start deciding next block and continue on until goal is reached
 		int current_I = robot_I;
 		int current_J = robot_J;
 		int current_low = 99;
 		double destX;
 		double destY;
 		double direction = 0.0;
 		boolean obstacleDetected = false;
 		
 		while(current_low > 2){
 			current_low = 99; //Every time, reset to highest number (robot)
 			int Next_I = 0;
 			int Next_J = 0;
 		
 			// Check Array Bounds North
 			// Is current space occupied?
 			if(current_I > 0 && grid[current_I-1][current_J] < current_low && grid[current_I-1][current_J] != 1){
 				current_low = grid[current_I-1][current_J]; //Set next number
 				Next_I = current_I-1; //Set Next Direction as North
 				Next_J = current_J;
 				direction = 90.0;
 			}
 			
 			// Check Array Bounds South
 			// Is current space occupied?
 			if(current_I < (10 - 1) && grid[current_I+1][current_J] < current_low && grid[current_I+1][current_J] != 1){ 
 				current_low = grid[current_I+1][current_J]; //Set next number 
 				Next_I = current_I+1; //Set Next Direction as South
 				Next_J = current_J;
 				direction = 270.0;
 			}
 			
 			// Check Array Bounds West
 			// Is current space occupied?
 			if(current_J > 0 && grid[current_I][current_J-1] < current_low && grid[current_I][current_J-1] != 1){ 
 				current_low = grid[current_I][current_J-1]; //Set next number
 				Next_I = current_I; //Set Next Direction as west
 				Next_J = current_J-1;
 				direction = 180.0;
 			}
 			
 			// Check Array Bounds East
 			// Is current space occupied?
 			if(current_J < (10 - 1) && grid[current_I][current_J+1] < current_low && grid[current_I][current_J+1] != 1){
 				current_low = grid[current_I][current_J+1]; //Set next number
 				Next_I = current_I; //Set Next Direction as East
 				Next_J = current_J+1;
 				direction = 0.00;
 			}
 			
 			// Okay - We know the number we're heading for, the direction and the coordinates.
 			destX = destX(Next_J);
 			destY = destY(Next_I);
 			
 			// turn to direction of next tile
			turnTo(direction, false);
 			try { Thread.sleep(500); } catch (InterruptedException e) {}
 	
 			// check if obstacle is detected
 			int o1 = getFilteredData();
 			Delay.msDelay(50);
 			int o2 = getFilteredData();
 			Delay.msDelay(50);
 			int o3  = getFilteredData();
 			Delay.msDelay(50);
 			int o4  = getFilteredData();
 			Delay.msDelay(50);
 			int o5  = getFilteredData();
 			
 			if(o1<35 && o2<35 && o3<35 && o4<35 && o5<35 && grid[Next_I][Next_J] != 1){
 				
 				// stop the following travelTo method
 				obstacleDetected = true;
 				
 				// set the obstacle on the Map grid
 				myMap.getGrid()[Next_I][Next_J] = Map.OBSTACLE;
 				
 				// find destination location by grid
 				int finalJ = 0;
 				int finalI = 0;
 				for(int i=0; i < grid.length; i++){
 					for(int j=0; j < grid[i].length; j++){
 						if(grid[i][j] == 2){
 							finalI = i;
 							finalJ = j;
 						}
 					}
 				}
 				
 				// recursion
 				navigateTo(myMap.destX(finalJ),myMap.destY(finalI));
 				
 				// terminate this loop
 				current_low = 2;
 			}
 			
 			// if no obstacle move to next block
 			if(!obstacleDetected){
 
				travelTo2(destX, destY); // travel to next tile
 			
 				current_I = Next_I; // update new I position for loop
 				current_J = Next_J; // update new J position for loop
 			}
 			
 		}
 	}
 	
 	private int getFilteredData() {
 		int distance;
 		int filterControl = 0;
 		int FILTER_OUT = 20;
 		
 		// do a ping
 		USSensor.ping();
 		
 		// wait for the ping to complete
 		try { Thread.sleep(100); } catch (InterruptedException e) {}
 		
 		// there will be a delay here
 		distance = USSensor.getDistance();
 		
 		//Rudimentary filter from wall following lab
 		if (distance == 255 && filterControl < FILTER_OUT) {
 			// bad value, do not set the distance variable, however do increment the filter value
 			filterControl ++;
 		} else if (distance == 255){
 			// true 255, therefore set distance to 255
 		} else {
 			// distance went below 255, therefore reset everything.
 			filterControl = 0;
 		}
 		return distance;
 	}
 	
 	public static double destX(int destJ){
 		double[] coordsX = new double[10];
 		double[] coordsY = new double[10];
 		double tileWidth = 30.0;
 		double sumX = tileWidth/2;
 		double sumY = tileWidth/2 + (tileWidth)*9;
 
 		// build x coordinates of map
 		for(int i=0;i<coordsX.length;i++){
 			coordsX[i] = sumX;
 			sumX = sumX + tileWidth;
 		}
 		
 		//build y coordinates of map
 		for(int j=0;j<coordsY.length;j++){
 			coordsY[j] = sumY;
 			sumY = sumY - tileWidth;
 		}
 
 		return coordsX[destJ];
 	}
 	
 	public static double destY(int destI){
 		double[] coordsX = new double[10];
 		double[] coordsY = new double[10];
 		double tileWidth = 30.0;
 		double sumX = tileWidth/2;
 		double sumY = tileWidth/2 + (tileWidth)*9;
 		
 		// build x coordinates of map
 		for(int i=0;i<coordsX.length;i++){
 			coordsX[i] = sumX;
 			sumX = sumX + tileWidth;
 		}
 		
 		//build y coordinates of map
 		for(int j=0;j<coordsY.length;j++){
 			coordsY[j] = sumY;
 			sumY = sumY - tileWidth;
 		}
 
 		return coordsY[destI];
 	}
 	
 	
 	
 }
