 package com.cheesymountain.woe;
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
 ================================================================*/
 import android.util.Log;
 
 import com.cheesymountain.woe.Races.Mogno;
 import com.cheesymountain.woe.Races.Race;
 
 
 /**
  * A representation of an Everbie.
  * Contains all the different variables and skills that makes up an Everbie,
  * and the mood of the current Everbie. This class is designed in accordance with
  * the singleton-pattern since there will only be one Everbie at any given time.
  * @author Cheesy Mountain
  *
  */
 public class Everbie {
 	
 	private int imageId = 0; 
 	public static final String DEFAULT_NAME = "Eibreve";
 	public static final Race DEFAULT_RACE = new Mogno();
 	private static Everbie everbie;
 	public static final int STARTING_MONEY = 0;
 	private String name;
 	private int maxHealthModifier, health, strength, intelligence, stamina,
 			charm, fullness, happiness, toxicity, cuteness, money;
 	private boolean alive;
 	private long occupiedSeconds = 0;
 	private int starvation, standardStarvation;
 
 	private Everbie(String name, Race race) {
 		alive = true;
 		this.name = name;
		health = maxHealthModifier = race.getMaxHealth();
 		strength = race.getStrength();
 		intelligence = race.getIntelligence();
 		stamina = race.getStamina();
 		charm = race.getCharm();
 		fullness = 50;
 		happiness = 50;
 		toxicity = 0;
 		cuteness = race.getCuteness();
 		money = STARTING_MONEY;
 		this.imageId = race.getImageId();
 		starvation = standardStarvation = 1;
 		
 		new Hunger().start();
 	}
 	
 	/**
 	 * Creates an Everbie unless one already exists.
 	 * @param name - the name of the Everbie to be created
 	 * @param imageId - the image's id of the Everbie to be created
 	 */
 	public synchronized static void createEverbie (String name, Race race){
 		if(!Everbie.exists()){
 			everbie = new Everbie(name, race);
 		}
 	}
 	
 	/**
 	 * Returns the pointer to the Everbie if one exists otherwise
 	 * it creates a default one.
 	 * @return pointer to the current Everbie
 	 */
 	public synchronized static Everbie getEverbie(){
 		if (!Everbie.exists()){
 			everbie = new Everbie(DEFAULT_NAME, DEFAULT_RACE);
 		}
 		return everbie;
 	}
 	
 	/**
 	 * Returns the id of the image of the current Everbie
 	 * @return the image's id number
 	 */
 	public int getImageId(){
 		return imageId;
 	}
 
 	/**
 	 * Returns the name of the current Everbie
 	 * @return the Everbie's name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the maximum health points the current Everbie can have
 	 * @return the maximum health points
 	 */
 	public int getMaxHealth() {
 		return (int)(maxHealthModifier + (stamina/2 + strength/4)*Math.PI);
 	}
 
 	/**
 	 * Returns the current health points of the current Everbie
 	 * @return the current health points
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Returns the current level of strength of the current Everbie
 	 * @return the current level of strength
 	 */
 	public int getStrength() {
 		return strength;
 	}
 
 	/**
 	 * Returns the current level of intelligence of the current Everbie
 	 * @return the current level of intelligence
 	 */
 	public int getIntelligence() {
 		return intelligence;
 	}
 
 	/**
 	 * Returns the current level of stamina of the current Everbie
 	 * @return the current level of stamina
 	 */
 	public int getStamina() {
 		return stamina;
 	}
 
 	/**
 	 * Returns the current level of charm of the current Everbie
 	 * @return the current level of charm
 	 */
 	public int getCharm() {
 		return charm;
 	}
 
 	/**
 	 * Returns the current Fullness level of the current Everbie
 	 * @return the current Fullness level
 	 */
 	public int getFullness() {
 		return fullness;
 	}
 
 	/**
 	 * Returns the current Happiness level of the current Everbie
 	 * @return the current Happiness level
 	 */
 	public int getHappiness() {
 		return happiness;
 	}
 
 	/**
 	 * Returns the current Toxicity level of the current Everbie
 	 * @return the current Toxicity level
 	 */
 	public int getToxicity() {
 		return toxicity;
 	}
 
 	/**
 	 * Returns the current level of cuteness of the current Everbie
 	 * @return the current level of cuteness
 	 */
 	public int getCuteness() {
 		return cuteness;
 	}
 
 	/**
 	 * Returns the current amount of money the current Everbie possesses
 	 * @return the current amount of money
 	 */
 	public int getMoney() {
 		return money;
 	}
 	
 	/**
 	 * Calculates and returns the current level of the current Everbie
 	 * @return the current level
 	 */
 	public int getLevel(){
 		return (strength + intelligence + stamina + Math.abs(charm) + Math.abs(cuteness))/5;
 	}
 
 	/**
 	 * Returns the amount of time the Everbie is occupied
 	 * @param seconds - the amount of seconds occupied
 	 */
 	public long getOccupiedSeconds(){
 		return occupiedSeconds;
 	}
 
 	/**
 	 * Returns the amount of time the Everbie should is occupied
 	 * @param minutes - the amount of minutes occupied
 	 */
 	public int getOccupiedMinutes(){
 		return (int) (getOccupiedSeconds()/60);
 	}
 
 	/**
 	 * Returns the amount of time the Everbie should is occupied
 	 * @param hours - the amount of hours occupied
 	 */
 	public int getOccupiedHours(){
 		return getOccupiedMinutes()/60;
 	}
 
 	/**
 	 * Changes the maximum amount of health points the current Everbie can have 
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeMaxHealth(int i) {
 		maxHealthModifier += i;
 		if (getMaxHealth() < 1 ){
 			health = 0;
 			alive = false;
 		}
 		if (getMaxHealth() < health) {
 			health = getMaxHealth();
 		}
 	}
 
 	/**
 	 * Changes the amount of health points the current Everbie currently has,
 	 * the Everbie dies if the total value goes to 0 or less
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeHealth(int i) {
 		if (health + i < getMaxHealth()) {
 			health += i;
 		} else if (health + i > getMaxHealth()) {
 			health = getMaxHealth();
 		} else if (health + i < 1) {
 			health = 0;
 			alive = false;
 		}
 	}
 
 	/**
 	 * Changes the level of strength the Everbie currently has,
 	 * the Everbie dies if the total value goes to 0 or less
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeStrength(int i) {
 		if (strength + i < 1){
 			strength = 0;
 			alive = false;
 		}
 		strength += i;
 	}
 
 	/**
 	 * Changes the level of intelligence the Everbie currently has,
 	 * the Everbie dies if the total value goes to 0 or less
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeIntelligence(int i) {
 		if (intelligence + i < 1){
 			intelligence = 0;
 			alive = false;
 		}
 		intelligence += i;
 	}
 
 	/**
 	 * Changes the level of stamina the Everbie currently has,
 	 * the Everbie dies if the total value goes to 0 or less
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeStamina(int i) {
 		if (stamina + i < 1) {
 			stamina = 0;
 			alive = false;
 		}
 		stamina += i;
 	}
 
 	/**
 	 * Changes the level of charm the Everbie currently has
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeCharm(int i) {
 		charm += i;
 	}
 
 	/**
 	 * Changes the current percental ratio of fullness level of the Everbie,
 	 * the Everbie starts taking damage if the value goes to 0 or less
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeFullness(int i) {
 		if(fullness + i < 1){
 			fullness = 0;
 			changeHealth(-5);
 		}
 		else if (fullness + i < 100){
 			fullness += i;
 		}
 		else if(fullness + i > 100){
 			fullness = 100;
 		}
 	}
 
 	/**
 	 * Changes the current percental ratio of happiness level of the Everbie
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeHappiness(int i) {
 		if(happiness + i < 1){
 			happiness = 0;
 		}
 		else if (happiness + i < 100){
 			happiness += i;
 		}
 		else if(happiness + i > 100){
 			happiness = 100;
 		}
 	}
 
 	/**
 	 * Changes the current percental ratio of toxicity level of the Everbie
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeToxicity(int i) {
 		 if (toxicity + i < 1) {
 			toxicity = 0;
 		}else if (toxicity + i < 100) {
 			toxicity += i;
 		} else if (toxicity + i > 99) {
 			toxicity = 100;
 			alive = false;
 		}
 	}
 
 	/**
 	 * Changes the level of cuteness the Everbie currently has
 	 * @param i - the value to de-/increase by
 	 */
 	public void changeCuteness(int i) {
 		cuteness += i;
 	}
 
 	/**
 	 * Changes the amount of money the Everbie currently possesses
 	 * @param i - the value to de-/increase by
 	 * @return <code>true</code> if money was added or subtracted correctly, <code>false</code> otherwise
 	 */
 	public boolean changeMoney(int i) {
 		if(money + i > 0){
 			money += i;
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sets the name of the Everbie
 	 * @param name - the new name
 	 */
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	/**
 	 * Sets the amount of time the Everbie should be occupied
 	 * @param seconds - the amount of seconds to be occupied
 	 */
 	public void setOccupiedSeconds(long seconds){
 		if(seconds > 0){
 			this.occupiedSeconds = seconds;
 			this.starvation = 3;
 			new Occupied().start();
 		}
 	}
 
 	/**
 	 * Sets the amount of time the Everbie should be occupied
 	 * @param minutes - the amount of minutes to be occupied
 	 */
 	public void setOccupiedMinutes(int minutes){
 		if(minutes > 0 && minutes*60 > 0)
 			setOccupiedSeconds(minutes*60);
 	}
 
 	/**
 	 * Sets the amount of time the Everbie should be occupied
 	 * @param hours - the amount of hours to be occupied
 	 */
 	public void setOccupiedHours(int hours){
 		if(hours > 0 && hours*60 > 0)
 			setOccupiedMinutes(hours*60);
 	}
 
 	/**
 	 * Puts the Everbie to rest to have it restore his/her health
 	 * and reduce his/her toxicity level
 	 */
 	public void sleep() {
 		health = maxHealthModifier;
 		toxicity = 0;
 	}
 	
 	/**
 	 * Restores the Everbie after the game has been closed and restarted
 	 * @param name - the name of the Everbie
 	 * @param values - an array with all stats
 	 * @param alive - a boolean to determine if the Everbie is alive
 	 * @param imageId - the image's id number
 	 */
 	public void restoreEverbie(String name, int[] values, boolean alive, int imageId){
 		setName(name);
 		maxHealthModifier = values[0];
 		health = values[1];
 		strength = values[2];
 		intelligence = values[3];
 		stamina = values[4];
 		charm = values[5];
 		fullness = values[6];
 		happiness = values[7];
 		toxicity = values[8];
 		cuteness = values[9];
 		money = values[10];
 		this.alive = alive;
 		this.imageId = imageId;
 	}
 	
 	/**
 	 * Returns an boolean to determine if the Everbie is alive or not
 	 * @return <code>true</code> if the Everbie is alive, <code>false</code> otherwise
 	 */
 	public boolean  isAlive(){
 		return alive;
 	}
 
 	/**
 	 * Returns an boolean to determine if the Everbie is occupied or not
 	 * @return <code>true</code> if the Everbie is occupied, <code>false</code> otherwise
 	 */
 	public boolean isOccupied(){
 		return occupiedSeconds > 0;
 	}
 	
 	
 	// method only used during testing
 	public void resetOccupied(){
 		occupiedSeconds = 0;
 	}
 	
 	/**
 	 * Returns an boolean to determine if the Everbie is initiated or not
 	 * @return <code>true</code> if the Everbie is initiated, <code>false</code> otherwise
 	 */
 	public static boolean exists(){
 		return everbie != null;
 	}
 	
 	/**
 	 * Resets (kills) the current Everbie, to be able to start a new one
 	 */
 	public synchronized void reset(){
 		everbie = null;
 	}
 
 	private class Occupied extends Thread{
 				
 		@Override
 		public void run(){
 			while(Everbie.getEverbie().isAlive() && Everbie.getEverbie().occupiedSeconds > 0){
 				try{
 					Thread.sleep(1000);
 				}catch(InterruptedException ie){}
 				Everbie.getEverbie().occupiedSeconds--;
 				if(Everbie.getEverbie().occupiedSeconds <= 0 && starvation != standardStarvation){
 					starvation = standardStarvation;
 				}
 				Log.d("Loop", occupiedSeconds + "");
 			}
 		}
 	}
 	
 	
 	private class Hunger extends Thread{
 				
 		@Override
 		public void run(){
 			while(Everbie.getEverbie().isAlive()){
 				try{
 					Thread.sleep(600000);
 				}catch(InterruptedException ie){}
 				Everbie.getEverbie().fullness -= Everbie.getEverbie().starvation;
 				Log.d("Loop", fullness + "");
 			}
 		}
 	}
 	
 }
