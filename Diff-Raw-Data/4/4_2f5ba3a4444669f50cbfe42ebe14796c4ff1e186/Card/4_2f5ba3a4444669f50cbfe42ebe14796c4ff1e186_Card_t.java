 package com.cj.kingscup;
 
 /**
  * This class will make a card with suit and value.
  * 
  * @author Chi-Han Wang
  * @author Jonathan Poston
  */
 public class Card {
 	/**
 	 * The class constant of Clubs.
 	 */
 	public static final char CLUBS = 'c';
 	/**
 	 * The class constant of Diamonds.
 	 */
 	public static final char DIAMONDS = 'd';
 	/**
 	 * The class constant of Spades.
 	 */
 	public static final char SPADES = 's';
 	/**
 	 * The class constant of Hearts.
 	 */
 	public static final char HEARTS = 'h';
 	/**
 	 * The class constant of lowest value of the cards.
 	 */
 	public static final int LOWEST_VALUE = 2;
 	/**
 	 * The class constant of highest value of the cards.
 	 */
 	public static final int HIGHEST_VALUE = 14;
 	/**
 	 * The instance field of card value.
 	 */
 	private int value;
 	/**
 	 * The instance field of card suit.
 	 */
 	private char suit;
 	/**
 	 * The instance field of rule.
 	 */
 	private String rule;
 	
 	/**
 	 * This method will generate a card object.
 	 * 
 	 * @param value
 	 *            The value of the card.
 	 * @param suit
 	 *            The suit of the card.
 	 */
 	public Card(int value, char suit, String rule) {
 		if ((value > HIGHEST_VALUE || value < LOWEST_VALUE)) {
 			throw new IllegalArgumentException("Invalid value");
 		}
 		if (suit != 'c' && suit != 'd' && suit != 's' && suit != 'h') {
 			throw new IllegalArgumentException("Invalid suit");
 		}
 		this.value = value;
 		this.suit = suit;
 		this.rule = rule;
 	}
	
 	/**
 	 * This method will offer the suit of the card.
 	 */
 	public char getSuit() {
 		return suit;
 
 	}
 
 	/**
 	 * This method will offer the value of the card.
 	 */
 	public int getValue() {
 		return value;
 
 	}
 

 	/**
 	 * This method will transform the card object into the string format.
 	 */
 	public String toString() {
 		String s = "";
 		s += suit;
 		s += value;
 		return s;
 
 	}
 
 	/**
 	 * The method to set the rule of the card.
 	 * 
 	 * @param newRule
 	 */
 	public void setRule(String newRule) {
 		this.rule = newRule;
 	}
 
 	/**
 	 * The method to get the rule from the card.
 	 * 
 	 * @return
 	 */
 	public String getRule() {
 		return rule;
 	}
 }
