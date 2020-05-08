 package com.sixtyfourbitsperminute.crushhour;
 
 public class Move {
 	Vehicle vehicle; //The char identifier of the vehicle that this Move operates on
 	int steps; //Number of steps this move takes. Vertical vehicles: + int moves down, - int moves up. Horizontal Vehicles: + int moves right, negative int moves left
 	int gridSize;
 	
 	public Move (Vehicle vehicle, int steps, int gridSize){
 		this.vehicle = vehicle;
 		this.steps = steps;
 		this.gridSize = gridSize;
 	}
 
 	public Vehicle getVehicle() {
 		return vehicle;
 	}
 
 	public int getSteps() {
 		return steps;
 	}
 	
	public boolean doesNotSurpassGrid(){
 		int moveable = 0;
 		if(this.vehicle.isHorizontal()){
 			moveable = this.vehicle.position[0];
 		} else {
 			moveable = this.vehicle.position[1];
 		}
 		if(moveable + this.steps + this.vehicle.length < this.gridSize && steps > 0){
 			return true;
 		} else if (moveable + this.steps > 0 && steps < 0){
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
