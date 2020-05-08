 package de.htwg.monopoly.entities;
 
 import de.htwg.monopoly.util.IMonopolyUtil;
 
 public class Dice {
 	
 	private int dice = 0;
 
 	public Dice() {
 		this.setDice(1, IMonopolyUtil.DICE);
 	}
 	
 	public int throwDice() {
 		return dice;
 	}
 
 	/**
 	 * Set (throw) the dice with a random number between lowerbound (1) and upperbound (6) (inclusive)
 	 * @param lowerBound
 	 * @param upperBound
 	 */
 	public final void setDice(int lowerBound, int upperBound) {
 		this.dice = (int) (Math.random() * ((upperBound + 1) - lowerBound) + lowerBound );
 	}
 }
