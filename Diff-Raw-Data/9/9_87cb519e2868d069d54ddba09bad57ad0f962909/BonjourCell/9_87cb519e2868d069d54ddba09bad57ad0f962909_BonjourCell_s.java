 package IUT.BoBot.SmartCells;
 
 import IUT.BoBot.SmartCell;
 
 public class BonjourCell implements SmartCell {
 	
 	String question;
 
 	public BonjourCell(String p_question) {
 		question = p_question;
 	}
 
 	public String answer() {
		if(question.contains("bonjour"))
 		  return "Bonjour!";
 		else
 		  return null;
 	}
 
 }
