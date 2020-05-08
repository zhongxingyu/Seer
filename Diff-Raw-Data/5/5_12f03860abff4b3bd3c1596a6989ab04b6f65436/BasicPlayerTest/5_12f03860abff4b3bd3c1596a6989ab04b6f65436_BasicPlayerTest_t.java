 package tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import cards.*;
 
 import player.BasicPlayer;
 
 public class BasicPlayerTest {
 
 	@Test
 	public void testAction() {
 		BasicPlayer player = new BasicPlayer(100, 30, 25, 25);
 		assertTrue(player.getAction() == 0 | player.getAction() == 1 | player.getAction() == 2);
 	}
 
 	@Test
 	public void testSetHand() {
 		BasicPlayer player = new BasicPlayer(100, 30, 25, 25);
 		player.setHand(new Hand(new Card[] {new Card(Suit.DIAMONDS, Value.FIVE), new Card(Suit.SPADES, Value.NINE)}));
		assertEquals(Suit.DIAMONDS,player.getHand().getCard(0).getSuit());
		assertEquals(Suit.SPADES,player.getHand().getCard(1).getSuit());
		assertEquals(Value.FIVE,player.getHand().getCard(0).getValue());
		assertEquals(Value.NINE,player.getHand().getCard(1).getValue());
 	}
 	
 	@Test
 	public void testStack() {
 		BasicPlayer player = new BasicPlayer(100, 30, 25, 25);
 		assertEquals(100, player.getStackSize());
 		player.updateStack(11);
 		assertEquals(111, player.getStackSize());
 		player.updateStack(-21);
 		assertEquals(90, player.getStackSize());
 	}
 
 }
