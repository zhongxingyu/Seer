 
 public class Card {
 	public String suit;
 	public String number;
 	
	public Card(String suit, String number)
 	{
 		this.suit = suit;
 		this.number = number;
 	}
 
 	public String print() {
 		return String.format("[%1$s, %2$s]", this.suit, this.number);
 	
 	}
 }
