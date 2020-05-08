 package model;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class TestBJHand {
 	
 	@Test
 	public void shouldEvaluateJto10(){
 		BlackJackHand hand = new BlackJackHand();
 		Card jackOfHearts = new Card(Suite.HEARTS, 11);
 		hand.add(jackOfHearts);
 		assertEquals(10, hand.getScore());
 	}
 	
 	@Test
 	public void shouldEvaluateQto10(){
 		BlackJackHand hand = new BlackJackHand();
 		Card queenOfHearts = new Card(Suite.HEARTS, 12);
 		hand.add(queenOfHearts);
 		assertEquals(10, hand.getScore());
 	}
 	
 	@Test
 	public void shouldEvaluateKto10(){
 		BlackJackHand hand = new BlackJackHand();
 		Card kingOfHearts = new Card(Suite.HEARTS, 13);
 		hand.add(kingOfHearts);
 		assertEquals(10, hand.getScore());
 	}
 	
 	@Test
 	public void shouldEvaluateAto11WhenTotalValueOfHandIsLessThan22(){
 		BlackJackHand hand = new BlackJackHand();
 		Card aceOfHearts = new Card(Suite.HEARTS, 14);
 		hand.add(aceOfHearts);
 		assertEquals(11, hand.getScore());
 	}
 	
 	@Test
 	public void shouldAccumulateValuesOfCardsInHand(){
 		BlackJackHand hand = new BlackJackHand();
 		int valueOfFirstCard = 6;
 		Card sixOfSpades = new Card(Suite.SPADES, valueOfFirstCard);
 		hand.add(sixOfSpades);
 		int valueOfSecondCard = 9;
 		Card nineOfSpades = new Card(Suite.SPADES, valueOfSecondCard);
 		hand.add(nineOfSpades);
 		assertEquals(valueOfFirstCard+valueOfSecondCard, hand.getScore());
 	}
 	
 	@Test
 	public void shouldEvaluateAto1IfTotalValueOfHandExceeds21(){
 		BlackJackHand hand = new BlackJackHand();
 		int valueOfFirstCard = 10;
 		hand.add(new Card(Suite.DIAMONDS, valueOfFirstCard));
 		int valueOfSecondCard = 9;
 		hand.add(new Card(Suite.DIAMONDS, valueOfSecondCard));
 		hand.add(new Card(Suite.DIAMONDS, 14));
 		assertEquals(valueOfFirstCard+valueOfSecondCard+1, hand.getScore());
 	}
 }
