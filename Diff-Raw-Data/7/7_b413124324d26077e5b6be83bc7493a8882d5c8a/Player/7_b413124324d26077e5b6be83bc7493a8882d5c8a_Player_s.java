 import java.util.ArrayList;
 
 public class Player {
 
     private String m_name;
     private int m_bank;
     private ArrayList<Card> m_hand;
 
	public Player(String name) {
		m_name = name;
         m_bank = 0;
         m_hand = new ArrayList<Card>();
	}
 
     public void addBank(int n) {
         m_bank += n;
     }
 
     public void removeBank(int n) {
         m_bank -= n;
     }
 
     public int getBank() {
         return m_bank;
     }
 
     public void deal(Card card) {
         m_hand.add(card);
     }
 
     public ArrayList<Card> getHand() {
         return m_hand;
     }
 }
