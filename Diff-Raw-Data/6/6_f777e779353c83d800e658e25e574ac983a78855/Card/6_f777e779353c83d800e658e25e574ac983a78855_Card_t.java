 
 public class Card {
 	public String suit;
 	public String number;
	public int num;
 	
	public Card(String suit, String number, int num)
 	{
 		this.suit = suit;
 		this.number = number;
		this.num = num;
 	}
 
 	public String print() {
 		return String.format("[%1$s, %2$s]", this.suit, this.number);
 	
 	}
 }
