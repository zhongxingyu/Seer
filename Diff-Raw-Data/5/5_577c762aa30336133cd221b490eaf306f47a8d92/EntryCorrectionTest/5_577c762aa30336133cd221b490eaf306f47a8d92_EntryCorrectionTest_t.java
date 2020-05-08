 package org.chaoticbits.cardshuffling.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.chaoticbits.cardshuffling.cards.PlayingCard;
 import org.chaoticbits.cardshuffling.cards.Suit;
 import org.chaoticbits.cardshuffling.cards.Value;
 import org.chaoticbits.cardshuffling.entry.DataEntryException;
 import org.chaoticbits.cardshuffling.entry.EntryCorrection;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EntryCorrectionTest {
 
 	private List<PlayingCard> goodList;
 
 	@Before
 	public void setUp() {
 		goodList = new ArrayList<PlayingCard>(52);
 		for (Suit suit : Suit.values()) {
 			for (Value value : Value.values()) {
 				goodList.add(new PlayingCard(value, suit));
 			}
 		}
 	}
 
 	@Test
 	public void loadsAllCorrectly() throws Exception {
 		// no exception is good
 		new EntryCorrection().checkDeckEntry(goodList);
 	}
 
 	@Test
 	public void not52() throws Exception {
 		goodList.remove(0);
 		goodList.remove(0);
 		try {
 			new EntryCorrection().checkDeckEntry(goodList);
 			fail("Exception should have been thrown");
 		} catch (DataEntryException e) {
			assertEquals("Less than 52 cards, missing ACE of CLUBS, TWO of CLUBS", e.getMessage());
 		}
 	}
 
 	@Test
 	public void notAllUnique() throws Exception {
 		goodList.remove(0);
 		goodList.add(new PlayingCard(Value.ACE, Suit.SPADES));
 		try {
 			new EntryCorrection().checkDeckEntry(goodList);
 			fail("Exception should have been thrown");
 		} catch (DataEntryException e) {
 			assertEquals("Deck not unique, missing ACE of CLUBS, ACE of SPADES has 2 repeats",
 					e.getMessage());
 		}
 	}
 
 	@Test
 	public void check52First() throws Exception {
 		goodList.remove(30);
 		goodList.remove(31);
 		goodList.add(new PlayingCard(Value.ACE, Suit.SPADES));
 		try {
 			new EntryCorrection().checkDeckEntry(goodList);
 			fail("Exception should have been thrown");
 		} catch (DataEntryException e) {
			assertEquals("Less than 52 cards, missing FIVE of HEARTS, SEVEN of HEARTS", e.getMessage());
 		}
 	}
 }
