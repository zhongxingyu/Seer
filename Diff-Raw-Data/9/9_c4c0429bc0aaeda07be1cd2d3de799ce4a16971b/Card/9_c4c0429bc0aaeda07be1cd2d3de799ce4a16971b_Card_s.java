 package Java;
 
 public class Card {
 	private String suit;
 	private String rank;
 	
 	public Card(String name){
 		int a=name.indexOf(" ");
 		suit=name.substring(0,a);
 		int b=name.indexOf(" ",a+1);
 		rank=name.substring(b+1);
 	}
 	
 	private String getRank(){
 		return rank;
 	}
 	
 	private String getSuit(){
 		return suit;
 	}
 	
 	public boolean similarRank(Card a){
		if(getRank().equals(a.getRank()))
			return true;
		return false;
 	}
 	
 	public boolean similarSuit(Card a){
		if(getSuit().equals(a.getSuit()))
				return true;
		return false;
 	}
 	
 	public Card(String suit,String rank){
 		this.suit=suit;
 		this.rank=rank;
 	}
 	
 	public String toString(){
 		return rank+" "+suit;
 	}
 //	public static void main(String[] args){
 //		Card a=new Card("Ace of Spades");
 //		System.out.println(a.suit+" "+a.rank);
 //	}
 }
