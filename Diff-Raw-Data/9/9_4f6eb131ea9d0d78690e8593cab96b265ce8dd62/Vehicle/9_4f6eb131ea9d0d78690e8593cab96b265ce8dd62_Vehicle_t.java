 package com.sixtyfourbitsperminute.crushhour;
 
 import java.util.ArrayList;
 
 public class Vehicle {
 	boolean horizontal;
 	int length;
 	int[] position;
 	
 	public int[] getPosition() {
 		return position;
 	}
 	public void setPosition(int[] position) {
 		this.position = position;
 	}
 	
 	public boolean isHorizontal(){
 		return horizontal;
 	}
 	
 	public ArrayList<Move> getPossibleMoves(Grid grid){
 		ArrayList<Move> possibleMoves = new ArrayList<Move>();
		//every time new move is created call move.doesNotSurpassGrid(); 
		
 		
 		return possibleMoves;
 	}
 }
