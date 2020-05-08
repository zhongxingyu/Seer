 package yugi.service;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import yugi.PMF;
 import yugi.model.Deck;
 
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 /**
  * Service for querying/persisting for decks.
  */
 public class DeckService {
 
 	private static final Logger logger = Logger.getLogger(DeckService.class.getName());
 
 	private static UserService userService = UserServiceFactory.getUserService();
 	private static DeckService instance;
 	
 	/**
 	 * Singleton accessor.
 	 * @return The deck service.
 	 */
 	public static DeckService getInstance() {
 		if (instance == null) {
 			instance = new DeckService();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Fetches the deck's information for the given key.
 	 * @param deckKey The deck's key.
 	 * @return The deck for the given key or null if the key is invalid.
 	 */
 	public Deck getDeck(String deckKey) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			return getDeck(pm, deckKey);
 		} finally {
 			pm.close();
 		}
 	}
 	
 	/**
 	 * Fetches the deck's information for the given key.
 	 * @param pm The persistence manager.
 	 * @param deckKey The deck's key.
 	 * @return The deck for the given key or null if the key is invalid.
 	 */
 	public Deck getDeck(PersistenceManager pm, String deckKey) {
 		if (deckKey != null && !deckKey.isEmpty()) {
 			try {
				return pm.getObjectById(Deck.class, KeyFactory.stringToKey(deckKey));
 			} catch (JDOObjectNotFoundException e) {
 				logger.severe("Failed to find a deck with this key: " + deckKey);
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all of the structure decks.
 	 * @param pm The persistence manager to use.
 	 * @return The list of structure decks.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Deck> getStructureDecks(PersistenceManager pm) {
 
 		Query query = pm.newQuery(Deck.class);
 		query.setFilter("isStructure == isStructureParam");
 		query.declareParameters("Boolean isStructureParam");
 
 		try {
 			return (List<Deck>) query.execute(true);
 		} finally {
 			query.closeAll();
 		}
 	}
 
 	/**
 	 * Gets the decks for the given user.  The resulting list of decks is a top
 	 * level fetch.  All of the information for each card in the deck is absent
 	 * in order to keep this query fast.
 	 * @param pm The persistence manager.
 	 * @param user The user for which to fetch the decks.
 	 * @return The top level information for each deck the user has access to.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Deck> getDecks(PersistenceManager pm, User user) {
 
 		Query query = pm.newQuery(Deck.class);
 		query.setFilter("userId == userIdParam");
 		query.declareParameters("String userIdParam");
 
 		try {
 			return (List<Deck>) query.execute(user.getUserId());
 		} finally {
 			query.closeAll();
 		}
 	}
 	
 	/**
 	 * Creates a new deck.
 	 * @param user The user for which to create the deck.
 	 * @return The newly created deck.
 	 */
 	public Deck newDeck(User user) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			return newDeck(pm, user);
 		} finally {
 			pm.close();
 		}
 	}
 	
 	/**
 	 * Creates a new deck.
 	 * @param pm The persistence manager.
 	 * @param user The user for which to create the deck.
 	 * @return The newly created deck.
 	 */
 	public Deck newDeck(PersistenceManager pm, User user) {
 		logger.info("Creating a new user deck.");
 		Deck deck = new Deck();
 		deck.setUserId(user.getUserId());
 		deck.setStructure(false);
 		deck.setName("Untitled Deck");
 		pm.makePersistent(deck);
 		return deck;
 	}
 	
 	/**
 	 * Creates a new structure deck.
 	 * @return The newly created structure deck.
 	 */
 	public Deck newStructureDeck() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			return newStructureDeck(pm);
 		} finally {
 			pm.close();
 		}
 	}
 	
 	/**
 	 * Creates a new structure deck.
 	 * @param pm The persistence manager.
 	 * @return The newly created structure deck.
 	 */
 	public Deck newStructureDeck(PersistenceManager pm) {
 		logger.info("Creating a new structure deck.");
 		Deck deck = new Deck();
 		deck.setStructure(true);
 		deck.setName("Untitled Structure Deck");
 		pm.makePersistent(deck);
 		return deck;
 	}
 	
 	/**
 	 * Checks to see if the user has access to modify this deck.
 	 * @param deck The deck to check.
 	 * @return True if the user has access, false otherwise.
 	 */
 	public boolean userHasWriteAccess(Deck deck) {
 		if (deck == null) {
 			return false;
 		}
 
 		if (deck.isStructure()) {
 			// Administrators can edit structure decks.
 			if (userService.isUserAdmin()) {
 				return true;				
 			}
 		} else {
 			// Users can only modify their own decks.
 			User user = userService.getCurrentUser();
 			if (deck.getUserId().equals(user.getUserId())) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
