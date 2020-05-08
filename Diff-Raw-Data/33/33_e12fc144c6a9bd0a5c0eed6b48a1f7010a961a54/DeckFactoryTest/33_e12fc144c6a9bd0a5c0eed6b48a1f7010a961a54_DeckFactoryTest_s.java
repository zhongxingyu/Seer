 package poker.hand_card;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import poker.hand_card.Deck;
 import poker.hand_card.DeckFactory;
 
 
 public class DeckFactoryTest {
 
 	@Test
 	public void test() {
 		DeckFactory classUnderTest = new DeckFactory();
 		Deck deck = classUnderTest.getDeck();
		for(int n = 0; n < 52; n++){
			for(int i = 1; i < 4; i++){
				for(int k = 2; i < 14; i++){
					assertEquals(i, deck.getDeck().get(n).getSuit());
					assertEquals(k, deck.getDeck().get(n).getRank());
				}
 			}
 		}
 	}
 
 }
