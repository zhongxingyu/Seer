 package model;
 
 import java.util.Random;
 import java.util.ArrayList;
 
 public class CaptureStrategy implements Strategy{
 
 	public Pawn getNextMove(int currentRoll, ArrayList<Pawn> moveablePawns, Field[] gameBoard) {
 		//go through each pawn and check the ones that can be moved
 		for(Pawn pawn: moveablePawns){
 			Boolean passed = false;
 			//return a pawn if it would catch a pawn
 			int pos = pawn.getPosition();
 			for(int i=1;i<=currentRoll;i++){
 				if(gameBoard[(pos+i)%40] instanceof StartTile){
 					if(gameBoard[(pos+i)%40].getForkOwner().equals(pawn.getOwner())){
 						continue;
 					}
 				}
 				if(i == currentRoll){
					if(gameBoard[(pos+i)].getOccupant().equals(null)){
 						return pawn;
 					}else if(passed == true){
 						continue;
 					}
 				}
				if(!gameBoard[(pos+i)%40].getOccupant().equals(null)){
 					passed = true;
 				}
 			}
 		}
 		//if no suitable moves, move random
 		return moveRandom(currentRoll, moveablePawns, gameBoard);
 		
 	}
 	
 	/**
 	 * Returns a random pawn, of movable ones
 	 * @param currentRoll
 	 * @param moveablePawns
 	 * @param gameBoard
 	 * @return
 	 */
 	public Pawn moveRandom(int currentRoll, ArrayList<Pawn> moveablePawns, Field[] gameBoard){
 		Random rand = new Random();
 		if(moveablePawns.isEmpty()){
 			return null;
 		}
 		int random = rand.nextInt(moveablePawns.size());
 		return moveablePawns.get(random);
 	}
 	
 	public String toString(){
 		return "Capture";
 	}
 }
 
