 package com.cheesymountain.woe;
 
 import android.util.Log;
 
 public class EverbieTest {
 	
 	private Everbie everbie;
 	
 	public EverbieTest(){
 		
 		Log.i("Start", "Everbie Test started");
 		
 		everbie = Everbie.getEverbie();
 		
 		testConstructor();
 		testStartingVariables();
 		testChangeVariables();
 		testOccupiedSeconds();
 		testOccupiedMinutes();
 		testOccupiedHours();
 		testResetEverbie();
 		testRestoreEverbie();
 		
 		Log.i("Start", "Everbie Test complete");
 	}
 	
 	/**
 	 * Method for testing the constructor in Everbie
 	 */
 	private void testConstructor(){
 		Log.i("Start", "Test constructor started");
 		
 		if(everbie == null){
 			Log.e("Error", "Everbie is null");
 		}
 		
 		Log.i("Complete", "Test constructor complete");
 	}
 	
 	/**
 	 * Method for testing the starting variables in Everbie
 	 */
 	private void testStartingVariables() {
 		
 		Log.i("Start", "Test starting variabes started");
 
 		if(everbie.getImageId() == 0){
 			Log.e("Error", "Wrong starting imageId");
 		}
 		if(everbie.getCharm() != 1){
 			Log.e("Error", "Wrong starting Charm");
 		}
 		if(everbie.getCuteness() != 1){
 			Log.e("Error", "Wrong starting Fullness");
 		}
 		if(everbie.getFullness() != 50){
 			Log.e("Error", "Wrong starting Fullness");
 		}
 		if(everbie.getHappiness() != 50){
 			Log.e("Error", "Wrong starting Happiness");
 		}
 		if(everbie.getHealth() != 20){
 			Log.e("Error", "Wrong starting Health");
 		}
 		if(everbie.getIntelligence() != 1){
 			Log.e("Error", "Wrong starting Intelligence");
 		}
 		if(everbie.getLevel() != 1){
 			Log.e("Error", "Wrong starting Level");
 		}
 		if(everbie.getMaxHealth() != 20){
 			Log.e("Error", "Wrong starting Maxhealth");
 		}
 		if(everbie.getMoney() != 0){
 			Log.e("Error", "Wrong starting Money");
 		}
 		if(everbie.getStamina() != 1){
 			Log.e("Error", "Wrong starting Stamina");
 		}
 		if(everbie.getStrength() != 1){
 			Log.e("Error", "Wrong starting Strength");
 		}
 		if(everbie.getToxicity() > 0){
 			Log.e("Error", "Wrong starting Toxicity");
 		}
 
 		Log.i("Complete", "Test starting variables complete");
 
 	}
 	
 	/**
 	 * Method for testing the change methods in Everbie
 	 */
 	private void testChangeVariables(){
 		
 		Log.i("Start", "Test change variables started");
 		
 		everbie.setName("Kalle");
 		everbie.changeCharm(5);
 		everbie.changeCuteness(5);
 		everbie.changeFullness(5);
 		everbie.changeHappiness(5);
 		everbie.changeHealth(5);
 		everbie.changeIntelligence(5);
		everbie.changeMaxHealth(5);
 		everbie.changeMoney(5);
 		everbie.changeStamina(5);
 		everbie.changeStrength(5);
 		everbie.changeToxicity(5);
 	
 		if(!everbie.getName().equals("Kalle")){
 			Log.e("Error", "Wrong name after change");
 		}
 		if(everbie.getCharm() != 5){
 			Log.e("Error", "Wrong Charm after change");
 		}
 		if(everbie.getCuteness() != 5){
 			Log.e("Error", "Wrong Fullness after change");
 		}
 		if(everbie.getFullness() != 5){
 			Log.e("Error", "Wrong Fullness after change");
 		}
 		if(everbie.getHappiness() != 5){
 			Log.e("Error", "Wrong Happiness after change");
 		}
 		if(everbie.getHealth() != 5){
 			Log.e("Error", "Wrong Health after change");
 		}
 		if(everbie.getIntelligence() != 5){
 			Log.e("Error", "Wrong Intelligence after change");
 		}
 		if(everbie.getLevel() != 5){
 			Log.e("Error", "Wrong Level after change");
 		}
 		if(everbie.getMaxHealth() != 5){
 			Log.e("Error", "Wrong Maxhealth after change");
 		}
 		if(everbie.getMoney() != 5){
 			Log.e("Error", "Wrong Money after change");
 		}
 		if(everbie.getStamina() != 5){
 			Log.e("Error", "Wrong Stamina after change");
 		}
 		if(everbie.getStrength() != 5){
 			Log.e("Error", "Wrong Strength after change");
 		}
 		if(everbie.getToxicity() != 5){
 			Log.e("Error", "Wrong Toxicity after change");
 		}
 
 		Log.i("Complete", "Test change variables complete");
 		
 	}
 	
 	/**
 	 * Method for testing the OccupationSecends method in Everbie
 	 */
 	private void testOccupiedSeconds(){
 		
 		Log.i("Start", "Test occupation seconds started");
 		
 		everbie.resetOccupied();
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set occupation time");
 		}
 		
 		everbie.setOccupiedSeconds(0);
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set a correct occupation time");
 		}
 		
 		everbie.setOccupiedSeconds(10);
 		if(!everbie.isOccupied()){
 			Log.e("Error", "Occupation time has been set but the Everbie is still not occupied");
 		}
 		
 		Log.i("Complete", "Test occupation seconds complete");
 	}
 	
 	/**
 	 * Method for testing the OccupationSecends method in Everbie
 	 */
 	private void testOccupiedMinutes(){
 		
 		Log.i("Start", "Test occupation Minutes started");
 		
 		everbie.resetOccupied();
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set occupation time");
 		}
 		
 		everbie.setOccupiedMinutes(0);
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set a correct occupation time");
 		}
 		
 		everbie.setOccupiedMinutes(10);
 		if(!everbie.isOccupied()){
 			Log.e("Error", "Occupation time has been set but the Everbie is still not occupied");
 		}
 		
 		Log.i("Complete", "Test occupation seconds complete");
 	}
 	
 	/**
 	 * Method for testing the OccupationSecends method in Everbie
 	 */
 	private void testOccupiedHours(){
 		
 		Log.i("Start", "Test occupation hours started");
 		
 		everbie.resetOccupied();
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set occupation time");
 		}
 		
 		everbie.setOccupiedHours(0);
 		if(everbie.isOccupied()){
 			Log.e("Error", "Everbie is occupied without having set a correct occupation time");
 		}
 		
 		everbie.setOccupiedHours(10);
 		if(!everbie.isOccupied()){
 			Log.e("Error", "Occupation time has been set but the Everbie is still not occupied");
 		}
 		
 		Log.i("Complete", "Test occupation hours complete");
 	}
 	
 	/**
 	 * Method for testing the reset method in Everbie 
 	 */
 	private void testResetEverbie(){
 		
 		Log.i("Start", "Test reset Everbie started");
 		
 		everbie.reset();
 		
 		if(everbie != null){
 			Log.e("Error", "everbie has not been reset");
 		}
 		
 		Log.i("Complete", "Test reset Everbie complete");
 	}
 	/**
 	 * Method for testing the restoreEverbie method in Everbie
 	 */
 	private void testRestoreEverbie(){
 		
 		Log.i("Start", "Test restore Everbie started");
 		
 		int[] values = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
 		
 		everbie.restoreEverbie("Nalle", values, true, 1);
 		
 		if(everbie == null){
 			Log.e("Error", "Restoring everbie failed");
 		}
 		
 		Log.i("Complete", "Test restore Everbie complete");
 	}
 	
 }
