 package game.play;
 
 import java.util.HashMap;
 
 import utils.misc.StringUtil;
 
 public class Board extends HashMap<Player,Card>{
 
 	private Card lead;
 	
 	public String toString()
 	{
 		return "Board: " + StringUtil.join(", ", this.values());
 	}
 	
 	public Card put(Player player, Card card)
 	{
 		if (this.size()==0) // then this is the first card played, aka the lead. keep track of that
 		{
			setLead(card);
 		}
 		return super.put(player, card);
 	}
 
	public Card getLead() {
		return lead;
	}

	public void setLead(Card lead) {
		this.lead = lead;
	}

	public Player determineWinner(Suit trump) {
		return null;
	}

 }
