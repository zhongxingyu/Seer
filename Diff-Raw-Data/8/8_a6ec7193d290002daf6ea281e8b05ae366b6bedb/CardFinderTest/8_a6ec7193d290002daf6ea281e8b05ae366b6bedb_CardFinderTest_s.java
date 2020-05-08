 package com.functional;
 
 import org.junit.Before;
 import org.junit.Test;
 import static com.functional.Expect.*;
 
 public class CardFinderTest
 {
 	private static final Card JACK_OF_DIAMONS = new Card(Face.JACK, Suit.DIAMONDS);
 	private static final Card FIVE_OF_CLUBS = new Card(Face.FIVE, Suit.CLUBS);
 	private static final String HAND_WITH_ONLY_FIVE_OF_CLUBS = "5c";
	private static final String HAND_WITH_F_C_AND_J_D = "5c Jd";
 	private static final String EMPTY_HAND = "";
 	private static final String NO_HAND = null;
 	private CardFinder finder;
 	
 	@Before
 	public void setUp()
 	{
 		finder = new CardFinder();
 	}
 	
 	@Test
 	public void willFindNoCardWithMatchingFaceInAnEmptyHand()
 	{
 		expect(cardFoundIn(EMPTY_HAND).whereFaceIs(Face.JACK)).toBeNull();
 	}
 	
 	@Test
 	public void willFindCardWithMatchingFaceInAHandWithOneCard()
 	{
 		expect(cardFoundIn(HAND_WITH_ONLY_FIVE_OF_CLUBS).whereFaceIs(Face.FIVE)).toEqual(FIVE_OF_CLUBS);
 	}
 	
 	@Test
 	public void willFindNoCardWithMatchingFaceWhenNoHandIsPresent()
 	{
 		expect(cardFoundIn(NO_HAND).whereFaceIs(Face.JACK)).toBeNull();
 	}
 	
 	@Test
 	public void willFindProperCardWithMatchingFaceInAHandWithTwoCard()
 	{
		expect(cardFoundIn(HAND_WITH_F_C_AND_J_D).whereFaceIs(Face.JACK)).toEqual(JACK_OF_DIAMONS);
 	}
 	
 	@Test
 	public void willFindNoCardWithMatchingFaceIfHandDoesNotContainAnyCardsWithMatchingFace()
 	{
		expect(cardFoundIn(HAND_WITH_F_C_AND_J_D).whereFaceIs(Face.KING)).toBeNull();
 	}
 	
 	private Holder cardFoundIn(String hand)
 	{
 		return new Holder(hand);
 	}
 	
 	private class Holder
 	{
 		private final String hand;
 
 		public Holder(String hand) {
 			this.hand = hand;
 		}
 		
 		public Card whereFaceIs(Face face)
 		{
 			return finder.find(face, hand);
 		}
 	}
 	
 }
