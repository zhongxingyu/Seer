 package com.tacohen.killbots.Logic;
 
 import java.util.List;
 import java.util.Random;
 
 import android.util.Pair;
 
 public class PlayerMovement implements IPlayerMovement{
 
 
 	@Override
 	public boolean canPlayerMoveLeft(Pair<Integer, Integer> p) {
		if (p.first == 0){
 			return false;
 		}
 		if (isRobotHere(Pair.create(p.first-1, p.second))){
 			return false;
 		}
 		if (isDeadRobotHere(Pair.create(p.first-1, p.second))){
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean canPlayerMoveRight(Pair<Integer, Integer> p) {
		if (p.first+1 == GridDimensions.width){
 			return false;
 		}
 		if (isRobotHere(Pair.create(p.first+1, p.second))){
 			return false;
 		}
 		if (isDeadRobotHere(Pair.create(p.first+1, p.second))){
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean canPlayerMoveUp(Pair<Integer, Integer> p) {
		if (p.second+1 == GridDimensions.height){
 			return false;
 		}
 		if (isRobotHere(Pair.create(p.first, p.second+1))){
 			return false;
 		}
 		if (isDeadRobotHere(Pair.create(p.first, p.second+1))){
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean canPlayerMoveDown(Pair<Integer, Integer> p) {
		if (p.second == 0){
 			return false;
 		}
 		if (isRobotHere(Pair.create(p.first, p.second-1))){
 			return false;
 		}
 		if (isDeadRobotHere(Pair.create(p.first, p.second-1))){
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean isPlayerTrapped() {
 		Integer currentXValue = CurrentPlayerLocation.currentPlayerLocation.first;
 		Integer currentYValue = CurrentPlayerLocation.currentPlayerLocation.second;
 		Pair<Integer,Integer> curLoc = Pair.create(currentXValue,currentYValue);
 		if ((canPlayerMoveUp(curLoc))|(canPlayerMoveDown(curLoc))|
 				(canPlayerMoveLeft(curLoc))|(canPlayerMoveRight(curLoc))){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 
 	@Override
 	public void setPlayerPosition(Pair<Integer, Integer> p) {
 		CurrentPlayerLocation.setPlayerLocation(p.first,p.second);
 		
 	}
 
 	@Override
 	public Pair<Integer, Integer> getPlayerPosition(Pair<Integer, Integer> p) {
 		return CurrentPlayerLocation.currentPlayerLocation;
 	}
 
 	@Override
 	public boolean isRobotHere(Pair<Integer, Integer> p) {
 		List<Pair<Integer,Integer>> robotsList = RobotLocations.liveRobotLocations();
 		if (robotsList.contains(p)){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	@Override
 	public boolean isDeadRobotHere(Pair<Integer, Integer> p) {
 		List<Pair<Integer,Integer>> robotsList = RobotLocations.deadRobotLocations();
 		if (robotsList.contains(p)){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	@Override
 	public Pair<Integer, Integer> teleportPlayer() {
 		Random r = new Random();
 		Integer newXlocation = r.nextInt(GridDimensions.height - 0);
 		Integer newYlocation = r.nextInt(GridDimensions.width - 0);
 		return Pair.create(newXlocation, newYlocation);
 	}
 
 }
