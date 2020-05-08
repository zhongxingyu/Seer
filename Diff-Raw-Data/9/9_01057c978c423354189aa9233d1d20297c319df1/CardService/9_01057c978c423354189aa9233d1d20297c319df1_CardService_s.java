 package yugi.service;
 
 import java.util.logging.Logger;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 
 import yugi.PMF;
 import yugi.model.Card;
 
 import com.google.appengine.api.datastore.KeyFactory;
 
 /**
  * Service for querying/persisting for cards.
  */
 public class CardService {
 
 	private static final Logger logger = Logger.getLogger(CardService.class.getName());
 
 	private static CardService instance;
 	
 	/**
 	 * Singleton accessor.
 	 * @return The card service.
 	 */
 	public static CardService getInstance() {
 		if (instance == null) {
 			instance = new CardService();
 		}
 		return instance;
 	}
 	
 	private CardService() {
 		
 	}
 
 	/**
 	 * Fetches the card's information for the given key.
 	 * @param cardKey The card's key.
 	 * @return The card for the given key or null if the key is invalid.
 	 */
 	public Card getCard(String cardKey) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			return getCard(pm, cardKey);
 		} finally {
 			pm.close();
 		}
 	}
 	
 	/**
 	 * Fetches the card's information for the given key.
 	 * @param pm The persistence manager.
 	 * @param cardKey The card's key.
 	 * @return The card for the given card key or null if the key is invalid.
 	 */
 	public Card getCard(PersistenceManager pm, String cardKey) {
 		if (cardKey != null && !cardKey.isEmpty()) {
 			try {
				pm.getObjectById(Card.class, KeyFactory.stringToKey(cardKey));
 			} catch (JDOObjectNotFoundException e) {
 				logger.severe("Failed to find a card with this key: " + cardKey);
 				return null;
 			}
 		}
 		return null;
 	}
 }
