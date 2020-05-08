 package com.deepwatercreations.burningdice;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Holds data for a single roll. When the doRoll method is invoked, generates numbers and calculates successes,
  * overall success/failure, and difficulty level of roll. Also has options for spending Artha post-roll and
  * re-rolling dice due to open ended rolls. 
  * 
  * @author DeepwaterCreations
  *
  */
 public class Roll implements Serializable{
 	
 	public enum Difficulty{PLAYER_CHOICE, ROUTINE, DIFFICULT, CHALLENGING}
 	
 	private static final long serialVersionUID = 1L;
 
 	private Random dicebag;
 	
 	private int exponent;
 	private int extraDice = 0;
 	private int arthaDice = 0;
 	
 	private int obstacle;
 	private int diceShade = 4; //Gray is 3, White is 2.
 	private ArrayList<Integer> results;
 	private int successes = 0;
 	private Difficulty difficulty;
 	
 	private boolean openEnded = false;
 	private boolean rollMade = false; 
 	
 	private boolean fateSpent = false;
 	private int personaSpent = 0;
 	private boolean deedsSpent = false;
 	
 	public Roll(int exp){
 		dicebag = new Random();	
 		results = new ArrayList<Integer>();
 		
 		exponent = exp;
 		
 		//Bare minimum.
 		obstacle = 1;
 	}
 	
 	public Roll(){
 		this(1);
 	}
 	
 	public void setExponent(int exp){
 		if(exp > 0)
 			exponent = exp;
 	}	
 
 	public int getExponent(){
 		return exponent;
 	}
 	
 	public void setObstacle(int ob){
 		if(ob > 0)
 			obstacle = ob;
 	}
 	
 	public int getObstacle(){
 		return obstacle;
 	}
 	
 	public void setShade(int shade){
 		assert(shade >= 2 && shade <= 4);
 		diceShade = shade;
 	}
 	
 	public int getShade(){
 		return diceShade;
 	}
 	
 	public Difficulty getDifficulty(){
 		return difficulty;
 	}
 	
 	public boolean isDeedsAvailable(){
 		return !deedsSpent;
 	}
 	
 	public boolean isFateAvailable(){
 		return !fateSpent;
 	}
 	
 	public void makeOpenEnded(){
 		openEnded = true;
 	}
 	
 	public void makeNotOpenEnded(){
 		openEnded = false;
 	}
 	
 	public void addDice(int extras){
 		extraDice += extras;
 	}
 	
 	public void subtractDice(int superfluousExtras){
 		if(extraDice - superfluousExtras >= 0)
 			extraDice -= superfluousExtras;
 	}
 			
 	/**
 	 * Performs the rolling of the dice.
 	 */
 	public void doRoll(){
 		//TODO: When I go to nicer graphics, I think they should eventually go here.
 		if(!rollMade){ //TODO: Consider changing this. Might be nicer to reroll and make sure that
 			//old results are removed.
 			for(int i = 0; i < getTotalDice(); i++){
 				int rollnum = dicebag.nextInt(6) + 1;
 				results.add(rollnum);
 				if(rollnum >= diceShade)
 					successes++;
 				if(openEnded && rollnum == 6)
 					extraDice++; //Fate hasn't been spent yet, so this is Steel or something.
 			}
 			difficulty = DifficultyLookup.getDifficulty(exponent + extraDice, obstacle);
 			rollMade = true;
 			deedsSpent = false; //Deeds can be spent again after the roll.  
 		}
 	}
 	
 	public ArrayList<Integer> getResults(){
 		return results;		
 	}
 	
 	/**
 	 * 
 	 * @return true if the roll got successes equal to or greater than the obstacle. 
 	 */
 //	public boolean getSuccess(){
 //		return successes >= obstacle; 
 //	}
 	
 	/**
 	 * 
 	 * @return The margin of success or failure. If the number is 0 or positive, the roll beat the obstacle. 
 	 */
 	public int getMargin(){
 		return successes - obstacle;		
 	}
 	
 	public int getNumSuccesses(){
 		return successes;
 	}
 	
 	public int getTotalDice(){
 		return exponent + extraDice + arthaDice;
 	}
 	
 	/**
 	 * "Luck - A player may spend a fate point to make the dice of a single roll open-ended... If
 	 * the roll is already open ended...then the player may reroll a single traitor (which is
 	 * not open-ended). Luck is purchased after the dice have been rolled." p. 66 BWG   
 	 */
 	public void spendFate(){
 		if(fateSpent)
 			System.out.println("ERROR: Can't spend more than one fate on a roll!");
 		else if(!rollMade)
 			System.out.println("ERROR: Can't activate Luck until roll has been made!");
 		else{
 			if(!openEnded){
 				openEnded = true;
 				rollFateOpenEnded();
 			}
 			else{
 				//Reroll a traitor - TODO: Make this not open ended!
 				for(int i = 0; i < results.size(); i++){
 					if(results.get(i) < diceShade){
 						reroll(i);
 						break;
 					}
 				}				
 			}
 			fateSpent = true;
 		}
 	}
 	
 	/**
 	 * "Boon - A player may choose to spend up to three persona points on a skill or stat test. Each
 	 * point spent grants a bonus die (+1D) to roll on the test. A Boom must be announced before the
 	 * dice are cast." p. 66 BWG 
 	 * 
 	 * @param points the number of persona points to spend. Total for a given roll must be between 0-3.
 	 */
 	public void spendPersona(int points){
 		if(personaSpent + points > 3)
 			System.out.println("ERROR: Can't spend more than three persona on Boons");
 		else if(rollMade)
 			System.out.println("ERROR: Can't activate Boons after roll has been made!");
 		else{
 			arthaDice += points;
 			personaSpent += points;
 		}
 	}
 	
 	/**
 	 *  "Divine Inspiration - One point of deeds artha may be spent to double the exponent of a single stat, skill or attribute test. 
 	 *  ...when spending a deeds point on a test that gains additional dice from FoRKs, helping, stances
 	 *  or other means, only double the base skill/stat and then add in the extra dice. Divine Inspiration must be announced
 	 *  before the dice are cast." p. 67 BWG   
 	 *  
 	 * "Saving Grace - A deeds point may be spent to reroll all dice that failed to come up successes on any given test.
 	 * This may be used for equipment dice like armor, shields and guns. Obviously, Saving Grace is
 	 * announced after the dice have been rolled." p. 67 BWG 
 	 */
 	public void spendDeeds(){
 		if(deedsSpent == false){
 			if(!rollMade){
 				//Divine Inspiration
 				arthaDice += exponent;	
 			}
 			else{
 				//Saving Grace
				//FIXME: When open ended, it doesn't seem to be adding new dice properly for rerolled dice that come up 6.
 				ArrayList<Integer> rerollDiceIndices = new ArrayList<Integer>();
 				for(int i = 0; i < results.size(); i++){
 					if(results.get(i) < diceShade)
 						rerollDiceIndices.add(i);						
 				}
 				
 				for(int i = 0; i < rerollDiceIndices.size(); i++){
 					int rollnum = dicebag.nextInt(6) + 1;
 					if(rerollDiceIndices.get(i) < results.size())
 						results.set(rerollDiceIndices.get(i), rollnum);
 					else{
 						results.add(rollnum);
 					}
 					if(rollnum >= diceShade)
 						successes++;
 					if(openEnded && rollnum == 6){
 						arthaDice++;
						rerollDiceIndices.add(results.size()); //Effectively adds a new die to Results on the next pass. 
 					}
 				}
 				
 			}
 			deedsSpent = true; 
 		}
 		else System.out.println("ERROR: Deeds already spent!");
 	}
 	
 	/**
 	 * Calls rollArthaDie() for every die that came up 6. 
 	 */
 	private void rollFateOpenEnded(){
 		for(int i = 0; i < results.size(); i++){
 			if(results.get(i) == 6){
 				rollArthaDie();
 			}
 		}
 	}
 	
 	/**
 	 * Rolls a non-open ended die, increments success if relevant, and
 	 * increments the number of Artha dice. 
 	 * Non-open ended because it's called from rollFateOpenEnded(), which will reroll any 6s appended to the list
 	 * by this method.   
 	 * @return the die result number.
 	 */
 	private int rollArthaDie(){
 		int rollnum = dicebag.nextInt(6) + 1;
 		results.add(rollnum);
 		if(rollnum >= diceShade)
 			successes++;
 		arthaDice++;
 		return rollnum; 
 	}
 	
 	/**
 	 * Rerolls a single die and increments successes if it comes up a success.
 	 * @param index The index of the die in the die array to be rerolled.
 	 */
 	private void reroll(int index){
 		int rollnum = dicebag.nextInt(6) + 1;
 		results.set(index, rollnum);
 		if(rollnum >= diceShade)
 			successes++; //WARNING: Assumes we aren't re-rolling successes! Which we shouldn't be doing anyway.
 	}
 
 	
 	//All this stuff is for calculating Difficulty: 
 	public static class DifficultyLookup{
 			
 		//difficultyTable[x] is the highest obstacle that counts as Routine for x+1 dice.
 		//If numDice > difficultyTable[x] and <= ob, it's Difficult. numDice > ob is Challenging. 
 		static private int[] difficultyTable = new int[]{
 			//Remember - it's off by one! [0] is 1 die. [17] is 18 dice.
 			1, //1 die - Special case - counts as either, player's choice.  
 			1, //2 dice
 			2, //3 
 			2, //4 
 			3, //5 
 			4, //6 - Beyond this point, the value is always numDice - 3.
 		};
 
 		
 		/**
 		 * 
 		 * Returns the difficulty level for the given number of dice versus the given obstacle.
 		 * 
 		 * @param numDice Number of non-Artha dice being rolled
 		 * @param ob Obstacle
 		 * @return ROUTINE, DIFFICULT or CHALLENGING, or PLAYER_CHOICE for 1 vs 1 since that counts as either ROUTINE or DIFFICULT. 
 		 */
 		public static Difficulty getDifficulty(int numDice, int ob){
 			assert(ob > 0);//No such thing as obstacle 0.
 			if(numDice == 1 && ob == 1) return Difficulty.PLAYER_CHOICE;  
 			int maxRoutineOb = numDice < 7 ? difficultyTable[numDice - 1] : numDice - 3; //numDice-1 because the table starts at 0, of course.
 			if(ob <= maxRoutineOb) return Difficulty.ROUTINE; 
 			if(ob <= numDice) return Difficulty.DIFFICULT;
 			return Difficulty.CHALLENGING; 
 		}
 		
 	}
 	
 
 }
