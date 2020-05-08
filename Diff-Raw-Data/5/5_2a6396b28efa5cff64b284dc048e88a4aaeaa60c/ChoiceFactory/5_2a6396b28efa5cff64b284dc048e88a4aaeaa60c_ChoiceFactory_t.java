 package com.chris.interview.client.ropasci.factories;
 
 import com.chris.interview.client.ropasci.valueObjects.ChoiceCode;
 import com.chris.interview.client.ropasci.valueObjects.Paper;
 import com.chris.interview.client.ropasci.valueObjects.PlayerChoice;
 import com.chris.interview.client.ropasci.valueObjects.Rock;
 import com.chris.interview.client.ropasci.valueObjects.Scissors;
 
 public class ChoiceFactory {
     public static PlayerChoice createPlayerChoiceFromCode(ChoiceCode code) {
 	PlayerChoice instance = null;
 	switch (code) {
 	case P:
 	    instance = new Paper();
 	    break;
 	case R:
 	    instance = new Rock();
 	    break;
 	case S:
 	    instance = new Scissors();
 	    break;
 	}
 	return instance;
     }
 
 	public static PlayerChoice randomChoice() {
 		double i = Math.rint(Math.random() * 100);
 		if(i<33)
 			return createPlayerChoiceFromCode(ChoiceCode.P);
 		if(i>66)
 			return createPlayerChoiceFromCode(ChoiceCode.R);
 		return createPlayerChoiceFromCode(ChoiceCode.S);
 	}
 	
	// @Test commented out for the GWT compiler
 	public void randomTest() {
 		double i = Math.rint(Math.random() * 100);
 		System.out.print(i);
 	}
 
 	public static PlayerChoice[] getAvailableChoices() {
 		return new PlayerChoice[]{createPlayerChoiceFromCode(ChoiceCode.P),createPlayerChoiceFromCode(ChoiceCode.S),createPlayerChoiceFromCode(ChoiceCode.R)};
 	}
 }
