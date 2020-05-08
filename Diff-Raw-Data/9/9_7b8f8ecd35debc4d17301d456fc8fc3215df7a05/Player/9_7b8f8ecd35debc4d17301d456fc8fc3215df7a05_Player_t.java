 import java.util.ArrayList;
 
 /**
  * @author kj
  *
  */
 
 
 
 
 public class Player {
 
 	private String _name;
 	private ArrayList<Hand> _hands;
 	private int _totalCash;
 	private int _bet;
 	private boolean _in;
 	private MOVE _move;
 
 	static enum MOVE {STAND,HIT,DOUBLEDOWN,SPLIT,OUT}	
 	/**
 	 * Default Constructor for player
 	 * @param name the name of this player
 	 * @param startingCash cash the player is starting with
 	 * @param c1 first card dealt
 	 * @param c2 second card dealt
 	 */
 	public Player(String name,int startingCash){
 		_name = name;
 		_totalCash = startingCash;
 		_bet = 0;
 		_hands = new ArrayList<Hand>();
		_hands.add(new Hand());
 	}
 
 	public Hand getLargestHand(){
 		Hand max = _hands.get(0);
 		for(Hand h: _hands){
 			if(h.size()>max.size()) max = h;
 		}
 		return max;
 
 	}
 	
 	public int getBet(){
 		return _bet;
 	}
 
 	public int getTotal(){
 		return _totalCash;
 	}
 	
 	
 	public ArrayList<Hand> getHands(){
 		return _hands;
 	}
 	
 	public void setMove(MOVE move){
 		_move = move;
 	}	
 	
 	public MOVE getMove(){
 		return _move;
 	}
 	public void createNewHand(){
 		_hands.add(new Hand());
 	}
 	
 	
 	public void bet(int amount){
 		_bet = amount;
 	}
 
 	public void win(){
 		_totalCash += _bet*_hands.size();
 	}
 
 	public void lose(){
 		_totalCash -= _bet*_hands.size();
 	}
 	
 	public String getName(){
 		return _name;
 	}
 	
 	public String toString(){
 		String result = _name + "\n" +
 		"Total Cash:\t" + _totalCash + "\n" +
 		"Bet:\t\t" + _bet + "\n";
 		int index=1;
 		for(Hand h: _hands){
 			result +="Hand " + index + ":\t\t" + h;
 		}
 		return result;
 	}
 	
 	
 }
