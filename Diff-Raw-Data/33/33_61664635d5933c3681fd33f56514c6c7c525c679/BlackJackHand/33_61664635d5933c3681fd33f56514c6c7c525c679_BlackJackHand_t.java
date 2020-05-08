 package model;
 
 public class BlackJackHand extends Hand {
 	
 	public int getScore(){
 		int score = 0;
 		for(Card c : hand){
 			if(c.getValue() == 14){
				score = score + 11 > 21 ? score + 1 : score + 11;
 			} else if(c.getValue() >= 10){
 				score += 10;
 			} else {
 				score += c.getValue();
 			}
 		}
 		return score;
 	}
 }
