 package test;
 import se.chalmers.dat255.risk.model.*;
 
 
 public class CardExangerTest {
 	Player player1;
 	CardExanger cardExanger = new CardExanger();
 	Card card1 = new Card (Card.CardType.INFANTRY, "aProvince");
 	Card card2 = new Card (Card.CardType.INFANTRY, "bProvince");
 	Card card3 = new Card (Card.CardType.INFANTRY, "cProvince");
 	
 	@Before
 	public void beforeAllTest() {
 		player1 = new Player(0, "Adolf");
 		
 		
		player1.addCard(card1);
		player1.addCard(card2);
		player1.addCard(card3);
 		
 		
 		
 		
 	}
 
 	@Test
 	public void testMakeExange() {
 		//Test for correct instanciation
 		assertTrue(cardExanger.getCard1() == null);
 		assertTrue(cardExanger.getCard2() == null);
 				
 		
 		// Adding one card
 		cardExanger.makeExange(card1, player1);
 		
 		assertTrue(cardExanger.getCard1() == card1);
 		assertTrue(cardExanger.getCard2() == null);
 		
 		// Adding another simular card
 		cardExanger.makeExange(card2, player1);
 		assertTrue(cardExanger.getCard1() == card1);
 		assertTrue(cardExanger.getCard2() == card2);
 		
 		// Adding card1 again should be removed and getCard2()==null
 		cardExanger.makeExange(card1, player1);		
 		assertTrue(cardExanger.getCard1() == card2);
 		assertTrue(cardExanger.getCard2() == null);		
 		
 	}
 
 	@
 	
 	@Test
 	public void testFlushCards(){
 
 		
 	}
 
 }
