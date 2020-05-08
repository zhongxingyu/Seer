 package herbstJennrichLehmannRitter.engine.factory.impl;
 
 import herbstJennrichLehmannRitter.engine.exception.EngineCouldNotStartException;
 import herbstJennrichLehmannRitter.engine.factory.GameCardFactory;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.action.ComplexCardAction;
 import herbstJennrichLehmannRitter.engine.model.impl.CardImpl;
 import herbstJennrichLehmannRitter.engine.model.xml.XmlCard;
 import herbstJennrichLehmannRitter.engine.model.xml.XmlCards;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 public class GameCardFactoryImpl implements GameCardFactory {
 
 	private Unmarshaller unmarshaller;
 	private Map<String, Card> cards;
 	private Map<String, Class<?>> complexCardActions;
 	
 	private static void assertCard(Card card) {
 		if (card.getCardType() == null) {
 			throw new EngineCouldNotStartException("Card with name " + card.getName() + " has no CardType");
 		}
 	}
 	
 	private static Map<String, Class<?>> getComplexCardActions(String packageName) throws IOException, ClassNotFoundException {
 		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
 		
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		URL resource = classLoader.getResource(packageName.replace('.', '/'));
		File directory = new File(resource.getPath());
 		
 		if (!directory.exists()) {
 			throw new EngineCouldNotStartException("could not find ComplexCard Actions");
 		}
 		File[] files = directory.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".class");
 			}
 		});
 		
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			String className = file.getName().substring(0, file.getName().length() - 6);
 			Class<?> currentClass = Class.forName(packageName + '.' + className);
 			
 			
 		}
 		
 		return classes;
 	}
 	
 	public GameCardFactoryImpl() {
 		try {
 			this.complexCardActions = getComplexCardActions("herbstJennrichLehmannRitter.engine.model.action.complexImpl");
 		} catch (IOException e) {
 			throw new EngineCouldNotStartException(e);
 		} catch (ClassNotFoundException e) {
 			throw new EngineCouldNotStartException(e);
 		}
 		
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance("herbstJennrichLehmannRitter.engine.model");
 			this.unmarshaller = jaxbContext.createUnmarshaller();
 			
 			InputStream is = this.getClass().getResourceAsStream("/herbstJennrichLehmannRitter/engine/model/cards.xml");
 			XmlCards xmlCards = (XmlCards)this.unmarshaller.unmarshal(is);
 			is.close();
 			
 			if (xmlCards.getCards() == null || xmlCards.getCards().isEmpty())
 				throw new EngineCouldNotStartException("the cards.xml provides no cards");
 			
 			// store all cards in a map to improve performance (getting a card from a HashMap is less expensive than from
 			// a list even if the list is sorted
 			this.cards = new HashMap<String, Card>(xmlCards.getCards().size(), 1);
 			for (XmlCard card : xmlCards.getCards()) {
 				Class<?> complexActionClass = this.complexCardActions.get(card.getName());
 				if (complexActionClass != null) {
 					try {
 						assertCard(card);
 						card.setComplexCardAction((ComplexCardAction)complexActionClass.newInstance());
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				this.cards.put(card.getName(), card);
 			}
 			
 		} catch (JAXBException e) {
 			e.printStackTrace();
 			throw new EngineCouldNotStartException(e);
 		} catch (IOException e) {
 			// this can be ignored
 		}
 	}
 	
 	@Override
 	public Card createCard(String cardName) {
 		// we have to create a complete new instance
 		Card card = this.cards.get(cardName);
 		return new CardImpl(card);
 	}
 
 	@Override
 	public Collection<Card> createDefaultDeck() {
 		Collection<Card> defaultDeck = new ArrayList<Card>();
 		
 		for (String cardName : this.cards.keySet()) {
 			defaultDeck.add(createCard(cardName));
 		}
 		
 		return defaultDeck;
 	}
 
 }
