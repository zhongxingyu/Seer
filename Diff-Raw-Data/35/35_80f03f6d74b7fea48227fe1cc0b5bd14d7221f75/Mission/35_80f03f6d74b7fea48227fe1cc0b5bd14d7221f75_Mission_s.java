 package cha.domain;
 
 import java.util.List;
 
 public class Mission{
 	private CountDown timer;
 	private Category category;
 	private Deque deque = new Deque();
 	private List<Card> cards;
 	private Card currentCard;
 
 	public Mission(Category category, int numberOfCards) {
 		this.timer = new CountDown();
 		this.category = category;
 		this.cards = deque.getCards(this.category, numberOfCards);
 	}
 
 	public Card nextCurrentCard() {
 		if (!cards.isEmpty()) {
 			currentCard = cards.get(0);
 			cards.remove(0);
 			return currentCard;
 		} else
 			stopMission();
 		String[] text = new String[] { "There are no more cards for your assignments! "
 				+ "\nPress 'Done' or wait until the timer has reached 0!" };
 		return new Card(text);
 	}
 
 	public void stopMission() {
 		timer.stopTimer();
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	/** Returns the title of the Mission as a String */
 	public String getTitle() {
 		String title;
 		if (category == Category.BACKWARDS) {
 			title = "Backwards!";
 		} else if (category == Category.BODYTOBODY) {
 			title = "Body to body!";
 		} else if (category == Category.SAMECLASS) {
 			title = "Same category!";
 		} else {
 			title = "Word jumble!";
 		}
 		return title;
 	}
<<<<<<< HEAD

	
	private void setMissionActivity(boolean b) {
		if(b==false)		
			System.out.println("Mission: Mission FALSE");
		else if(b==true)		
			System.out.println("Mission: Mission TRUE");
	}
=======
>>>>>>> 24b943cf6691a355a0970e7f7e6b41a2bfe75311
 	
 	@Override
 	public String toString() {
 		return "Mission [cards=" + cards + ", timer=" + timer  + "]";
 	}
 }
