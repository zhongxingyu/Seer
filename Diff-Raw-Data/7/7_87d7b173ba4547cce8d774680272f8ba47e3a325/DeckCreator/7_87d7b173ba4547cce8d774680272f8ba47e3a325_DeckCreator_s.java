 package com.adaba.deck;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.adaba.cards.BlackCard;
 import com.adaba.cards.Card;
 import com.adaba.cards.Type;
 import com.adaba.cards.WhiteCard;
 
 public class DeckCreator {
 	/**
 	 * SLF4J logger for this class
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(DeckCreator.class);
 
 	// TODO
 	protected static final String WHITE_PATH = "C:/Users/ThaShepherd/dev/Tomcat7/wtpwebapps/ServerAgainstAgility/WEB-INF/resources/white.txt";
 	protected static final String BLACK_PATH = "C:/Users/ThaShepherd/dev/Tomcat7/wtpwebapps/ServerAgainstAgility/WEB-INF/resources/black.txt";
 
 	public static Deck makeCoHDeck(Type type) throws IOException {
 		String resource = null;
 		if (type == Type.BLACK) {
 			resource = BLACK_PATH;
 		} else if (type == Type.WHITE) {
 			resource = WHITE_PATH;
 		} else {
 			resource = BLACK_PATH;
 		}
 
 		// Read card resource into memory
 		List<String> lines = new LinkedList<String>();
 		BufferedReader reader = null;
 		try {
 			logger.debug("Opening resource {}", resource);
 			reader = new BufferedReader(new FileReader(resource));
 			String line;
 			while ((line = reader.readLine()) != null) lines.add(line);		
 		} finally {
 			if (reader != null) reader.close();
 		}
 
 		// Parse cards
 		List<Card> cardlist = new LinkedList<Card>();
 		for (String line : lines) {
 			if (line.equals("###")) {
 				break;
 			} else {									
 				if (type == Type.BLACK) {
					String[] lineSplit = line.split("\\|");
					String cardText = lineSplit[0];
					assert(lineSplit.length > 1);
					int blanks = Integer.parseInt(lineSplit[1]);
					cardlist.add(new BlackCard(cardText, blanks));
 				} else if (type == Type.WHITE) {
 					cardlist.add(new WhiteCard(line));
 				}
 			}
 		}
 
 		return new Deck(cardlist);
 	}
 }
