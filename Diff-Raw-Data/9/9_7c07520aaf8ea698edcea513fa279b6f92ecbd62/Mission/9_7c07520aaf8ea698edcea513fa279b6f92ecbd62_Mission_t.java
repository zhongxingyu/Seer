 package cha.domain;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import cha.controller.ChallengeAccepted;
 import cha.controller.Event;
 import cha.domain.Categories.Category;
 
 public class Mission {
 	
 	private List<Card> cards;
 	private final CountDown timer;
 	private final Piece piece;
 
 	private final Deque deque = new Deque();
 
 	private Bet actualBet;
 
 	
 	public Mission(Piece piece, Bet b){
 		timer = new CountDown();
 		this.piece = piece;
 		actualBet = b;
 	}
 	
 
 	public void startMission(Category c){
		
		//TODO
		//deque.getCards(c, actualBet.getBetValue());

 		//Deque.getCards(c, actualBet);
 
 		// timer.start();
 	}
 	//L�gga till kort i en h�g och representera fr�n h�gen
 		
 	
 	
 
 	public void timeOver(){
 		//TODO
 	}
 
 	public void missionDone(boolean completed){
 		ChallengeAccepted.getInstance().publish(Event.OldPosition, piece.getPosition());
 		if(completed){
 			piece.movePieceForward(actualBet.getBetValue());
 		}
 		else{
 			piece.movePieceBackward();
 		}
 		ChallengeAccepted.getInstance().publish(Event.NewPosition, piece.getPosition());
 	}
 	
 	@Override
 	public String toString() {
 		return "Mission [cards=" + cards + ", timer=" + timer + ", piece="
 				+ piece + "]";
 	}
 }
