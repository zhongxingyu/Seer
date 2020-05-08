 package model;
 
 public class BlackJackHand extends Hand {
 	
 	public int getScore(){
		int score = calculateScore(11);
		if (score > 21) {
			score = calculateScore(1);
		}
		return score;
	}

	private int calculateScore(int valueOfAce) {
 		int score = 0;
 		for(Card c : hand){
 			if(c.getValue() == 14){
				score += valueOfAce;
 			} else if(c.getValue() >= 10){
 				score += 10;
 			} else {
 				score += c.getValue();
 			}
 		}
 		return score;
 	}
 }
