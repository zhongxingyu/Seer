 package modules;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.Vector;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static org.jibble.pircbot.Colors.*;
 
 import static panacea.Panacea.*;
 
 /**
  * Backronym
  *
  * @author Michael Mrozek
  *         Created Sep 11, 2010.
  */
 public class Backronym extends NoiseModule {
 	private static final File DICTIONARY_FILE = new File("/usr/share/dict/words");
 	private static final String COLOR_ERROR = RED;
 	private static final String COLOR_RESPONSE = GREEN;
 	
 	private HashMap<Character, String[]> words;
 	
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 
 		final HashMap<Character, Vector<String>> words = new HashMap<Character, Vector<String>>(26);
 		for(int i = 'a'; i <= 'z'; i++) {
 			words.put((char)i, new Vector<String>());
 		}
 		
 		try {
 			final Scanner s = new Scanner(DICTIONARY_FILE);
 			while(s.hasNextLine()) {
 				final String word = s.nextLine();
 				words.get(Character.toLowerCase(word.charAt(0))).add(word);
 			}
 		} catch(FileNotFoundException e) {
 			throw new RuntimeException("No dictionary found");
 		}
 		
 		this.words = new HashMap<Character, String[]>(words.size());
 		for(Character key : words.keySet()) {
 			this.words.put(key, words.get(key).toArray(new String[0]));
 		}
 	}
 
 	@Command("\\.b(?:ackronym)? ([A-Za-z]+)")
 	public void backronym(Message message, String letters) {
 		if(letters.length() > 16) {
 			this.bot.reply(message, COLOR_ERROR + "Maximum length: 16");
 			return;
 		}
 	
 		final String[] choices = new String[letters.length()];
 		for(int i = 0; i < letters.length(); i++) {
 			choices[i] = getRandom(this.words.get(Character.toLowerCase(letters.charAt(i))));
 		}
 		
 		this.bot.sendMessage(implode(choices, " "));
 	}
 	
 	@Command("\\.b(?:ackronym)?")
 	public void backronymDefault(Message message) {this.backronym(message, message.getSender());}
 	
 	@Override public String getFriendlyName() {return "Backronym";}
 	@Override public String getDescription() {return "Chooses a random word for each letter specified";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".backronym -- Display a backronym for the sending user",
				".backronym _css_ -- Display a backronym for _css_",
 				".b -- Same as .backronym"
 		};
 	}
 	@Override public File[] getDependentFiles() {return new File[] {DICTIONARY_FILE};}
 }
