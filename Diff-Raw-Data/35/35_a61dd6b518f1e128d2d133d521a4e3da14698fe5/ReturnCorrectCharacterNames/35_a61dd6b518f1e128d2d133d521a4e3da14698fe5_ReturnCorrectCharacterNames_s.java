 package tests.unit;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import configuration.PlayConfig;
 import configuration.PlayType;
 import character.Character;
 import play.Play; 
 
 public class ReturnCorrectCharacterNames {
 	
 	// Obviously terrible but I have no idea how to pass functions into a jUnit test. 
 	private static final String LOCATION = "/Users/viveknair/Downloads/narrative_analysis/PlayAnalysis/datasets/parsed_chekhov_plays";
 	
 	// Add in more characters later.
 	private static final String[] characterNames = {"TIHON EVSTIGNEYEV"};
 	
 	private Play currentPlay = null; 
 	private HashMap<String, Character> characters = null; 
 	
 	@Before
 	public void instantiatePlay() {
 		PlayConfig config = new PlayConfig(PlayType.DEFAULT);
 		config.set("fileName", LOCATION);
 		currentPlay = new Play(config);
 		
 		// Construct the features
 		currentPlay.instantiatePlay();
 		System.out.println("-----");
 	}
 
 	@Test
 	public void returnCorrectCharacterNames() {
 		characters = currentPlay.returnCharacters(null, null);
 		ArrayList<String> returnCharNames = new ArrayList<String>();
 		// For now we'll check for the character names.
 		for( String charName: characters.keySet()) {
 			returnCharNames.add(charName);
 		}
 		
 		for (int i = 0; i < characterNames.length; i ++) {
 			if (returnCharNames.contains(characterNames[i])) {
 				assertTrue(true);
 				return; 
 			}
 		}
 		fail();
 	}
 
 }
