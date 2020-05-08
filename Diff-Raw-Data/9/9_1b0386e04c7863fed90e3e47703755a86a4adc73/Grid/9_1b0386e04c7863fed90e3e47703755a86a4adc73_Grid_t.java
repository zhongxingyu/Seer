 package com.sixtyfourbitsperminute.crushhour;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author Jonathan Thompson
  * @author Kelly Croswell
  * 
  * This class represents the grid of the Rush Hour game, on which vehicles are 
  * located and moved around.
  */
 public class Grid {
 	
 	/**
 	 * A Map containing all of the vehicles on the grid and the character that 
 	 * represents them visually.
 	 */
 	HashMap<Character, Vehicle> vehicles;
 	
 	/**
 	 * A list of any previous grids that directly led to the formation of the 
 	 * current grid.
 	 */
 	ArrayList<Grid> previousGrids;
 	
 	/**
 	 * The length (or width, as grids are square) of the grid.
 	 */
 	int gridSize;
 
 	/**
 	 * This is the constructor for this class. It takes in an initial list of 
 	 * vehicles that are present on the grid, and the size of the grid.
 	 * 
 	 * @param vehicles A HashMap containing the list of vehicles and their characters.
 	 * @param gridSize An integer containing the size of the grid.
 	 */
 	public Grid(HashMap<Character, Vehicle> vehicles, int gridSize) {
 		this.vehicles = vehicles;
 		this.gridSize = gridSize;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public Grid executeMove(Object object) {
 
 		return null;
 	}
 
 	/**
 	 * This method returns the size of the grid.
 	 * @return An int holding the size of the grid.
 	 */
 	public int getGridSize() {
 		return gridSize;
 	}
 
 	/**
 	 * This method returns the map of vehicles on the grid.
 	 * @return A HashMap containing the vehicles.
 	 */
 	public HashMap<Character, Vehicle> getVehicles() {
 		return vehicles;
 	}
 
 	/**
 	 * This method returns the list of previous grids associated with the current 
 	 * grid.
 	 * @return An ArrayList of previous grids.
 	 */
 	public ArrayList<Grid> getPreviousGrids() {
 		return previousGrids;
 	}
 
 	/**
 	 * This method sets the previous grids on a current grid.
 	 * @param previousGrids An ArrayList containing the previous grids.
 	 */
 	public void setPreviousGrids(ArrayList<Grid> previousGrids) {
 		this.previousGrids = previousGrids;
 	}
 
 	/**
 	 * This method gets a list of all of the Coordinates on the grid that are 
 	 * covered by one vehicle or another.
 	 * @return An ArrayList containing the covered coordinates.
 	 */
 	public ArrayList<Coordinate> getCoveredCoordinates() {
 		ArrayList<Coordinate> coveredCoordinates = new ArrayList<Coordinate>();
 		for (Character key : getVehicles().keySet()) {
 			coveredCoordinates.addAll(getVehicles().get(key).getCoveredCoordinates());
 		}
 		return coveredCoordinates;
 	}
 	
 	/**
 	 * This method gets a list of all of the Coordinates on the grid that are 
 	 * covered with the exception of one particular vehicle on the grid.
 	 * @param v The vehicle to be excluded from the list of covered coordinates.
 	 * @return An ArrayList containing all of the covered coordinates save those 
 	 * covered by vehicle v.
 	 */
 	public ArrayList<Coordinate> getCoveredCoordinatesExcludingVehicle(Vehicle v) {
 		ArrayList<Coordinate> coveredCoordinates = new ArrayList<Coordinate>();
 		for (Character key : getVehicles().keySet()) {
 			coveredCoordinates.addAll(getVehicles().get(key).getCoveredCoordinates());
 		}
 		coveredCoordinates.removeAll(v.getCoveredCoordinates());
 		return coveredCoordinates;
 	}
 
 	/**
 	 * This is the overridden hash code method for the coordinate class. Implemented 
 	 * so that .equals() works properly. 
 	 * @return An integer containing the hash code.
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((previousGrids == null) ? 0 : previousGrids.hashCode());
 		result = prime * result
 				+ ((vehicles == null) ? 0 : vehicles.hashCode());
 		return result;
 	}
 
 	/**
 	 * This is the overridden equals method for the coordinate class. Implemented 
 	 * so that two grids can be easily compared for the purpose of eliminating loops.
 	 * @return Whether or not two grids are equal.
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Grid other = (Grid) obj;
 		if (previousGrids == null) {
 			if (other.previousGrids != null)
 				return false;
 		} else if (!previousGrids.equals(other.previousGrids))
 			return false;
 		if (vehicles == null) {
 			if (other.vehicles != null)
 				return false;
 		} else if (!vehicles.equals(other.vehicles))
 			return false;
 		return true;
 	}
 
 	/**
 	 * This method returns whether or not there is a clear path from the user car 
 	 * on the grid to the exit spot on the grid, thereby finishing the game.
 	 * @return A boolean containing whether or not the path is clear.
 	 */
 	public boolean playerCanExit() {
 		Car player = (Car) vehicles.get('A');
 		for (int i = player.position.x; i < gridSize; i++) {
 
 		}
 		return false;
 	}
 	
 	/**
 	 * This method turns the list of vehicles associated with a grid into a String 
 	 * complete with x's for empty spots so that the grid can be handed over to 
 	 * the GUI for parsing.
 	 * @return A String containing the grid.
 	 */
 	public String gridToString () {
 		char[][] result = {{'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'},  
 						   {'x', 'x', 'x', 'x', 'x', 'x'}};
 		for(char c : this.vehicles.keySet()){
 			Vehicle current = vehicles.get(c);
 			ArrayList<Coordinate> coveredPositions = current.getCoveredCoordinates();
 			for(int i = 0; i < coveredPositions.size(); i++){
 				Coordinate currentCoordinate = coveredPositions.get(i);
 				result[currentCoordinate.x][currentCoordinate.y] = c;
 			}
 		}
		String resultString = "";
		for(int i = 0; i < result.length; i++){
			for(int j = 0; i < result.length; j++){
				resultString = resultString + result[i][j];
			}
		}
		
		return resultString;
 	}
 
 }
