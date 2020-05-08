 package clueGame;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Set;
 
 public class ComputerPlayer extends Player {
 
 	private String guess;
	private BoardCell location;
 	

 	public ComputerPlayer(String name, Color color, ClueGame cg) {
 		super();
 	}
 
 	public void addCard(Card plumCard) {
 		
 	}
 	
 	public BoardCell pickLocation(Set<BoardCell> targets){
 		int index = 0;
 		return null;
 	}
 
 	public Card disproveSuggestion(Card plumCard, Card studyCard, Card wrenchCard) {
 		return null;
 	}
 
 	public void updateSeenCards(Card mustardCard) {
 		
 	}
 
 	public void makeSuggestion(ArrayList<Card> myCards) {
 		
 	}
 
 	public Card getGuess() {
 		return null;
 	}
 	
 	public void setGuess(String guess) {
 		this.guess = guess;
 	}
	
	public void setLocation(BoardCell location) {
		this.location = location;
	}
 
 }
