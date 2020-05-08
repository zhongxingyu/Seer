 package herbstJennrichLehmannRitter.tests.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import herbstJennrichLehmannRitter.engine.enums.CardType;
 import herbstJennrichLehmannRitter.engine.exception.GameEngineException;
 import herbstJennrichLehmannRitter.engine.factory.GameCardFactory;
 import herbstJennrichLehmannRitter.engine.factory.PlayerFactory;
 import herbstJennrichLehmannRitter.engine.factory.impl.GameCardFactoryImpl;
 import herbstJennrichLehmannRitter.engine.factory.impl.PlayerFactoryImpl;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Player;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DeckTests {
 
 	private PlayerFactory playerFactory = new PlayerFactoryImpl();
 	private GameCardFactory gameCardFactory = new GameCardFactoryImpl();
 	private Collection<Card> cardsOnHand = new ArrayList<Card>();
 	private Player globalPlayer = null;
 	
 	@Before
 	public void before() {
 		try {
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Geheimraum"));
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Neues Werkzeug"));
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Magische Quelle"));
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Smaragd"));
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Blutmond"));
 			this.cardsOnHand.add(this.gameCardFactory.createCard("Tollwütiges Schaf"));
 		} catch (GameEngineException e) {
 			fail(e.getLocalizedMessage());
 		}
 	}
 	
 	@Test
 	public void testGetAllCards() {
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand,
 				0, 0, 0, 0);
 		
 		assertTrue(this.cardsOnHand.containsAll(this.globalPlayer.getDeck().getAllCards()));
 	}
 
 	@Test
 	public void testDiscardCard() {
 		Collection<Card> cards = new ArrayList<Card>();
 		Card cardOne = this.gameCardFactory.createCard("Architektur");
 		
 		cards.add(cardOne);
 		cards.add(this.gameCardFactory.createCard("Auge des Koloss"));
 		cards.add(this.gameCardFactory.createCard("Diamant"));
 		cards.add(this.gameCardFactory.createCard("Drachenauge"));
 		cards.add(this.gameCardFactory.createCard("Oger"));
 		cards.add(this.gameCardFactory.createCard("Rinderwahnsinn"));
 
 		Player player = this.playerFactory.createPlayer("Player", cards, 0, 0, 0, 0);
 		
 		player.getDeck().discardCard(cardOne);
 		
 		assertFalse(player.getDeck().getAllCards().containsAll(cards));
		assertEquals(5, player.getDeck().getAllCards().size());
 	}
 
 
 	@Test
 	public void testDiscardAllCardsByType() {
 		Collection<Card> cards = new ArrayList<Card>();
 		Card cardArchitektur = this.gameCardFactory.createCard("Architektur");
 		Card cardAugeDesKoloss = this.gameCardFactory.createCard("Auge des Koloss");
 		Card cardDiamant = this.gameCardFactory.createCard("Diamant");
 		Card cardDrachenauge = this.gameCardFactory.createCard("Drachenauge");
 		Card cardOger = this.gameCardFactory.createCard("Oger");
 		Card cardRinderwahnsinn = this.gameCardFactory.createCard("Rinderwahnsinn");
 		
 		cards.add(cardArchitektur);
 		cards.add(cardAugeDesKoloss);
 		cards.add(cardDiamant);
 		cards.add(cardDrachenauge);
 		cards.add(cardOger);
 		cards.add(cardRinderwahnsinn);
 
 		Player player = this.playerFactory.createPlayer("Player", cards, 0, 0, 0, 0);
 
 		player.getDeck().discardAllCardsByType(CardType.MAGIC_LAB);
 		assertTrue(player.getDeck().getAllCards().contains(cardArchitektur));
 		assertTrue(player.getDeck().getAllCards().contains(cardAugeDesKoloss));
 		assertFalse(player.getDeck().getAllCards().contains(cardDiamant));
 		assertFalse(player.getDeck().getAllCards().contains(cardDrachenauge));
 		assertTrue(player.getDeck().getAllCards().contains(cardOger));
 		assertTrue(player.getDeck().getAllCards().contains(cardRinderwahnsinn));
 	}
 
 	@Test
 	public void testPickCardWithFullHand() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Schäfchen"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		assertFalse(this.globalPlayer.getDeck().pickCard());
 	}
 	
 	@Test
 	public void testPickCardAfterDiscardAllCards() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Schäfchen"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		this.globalPlayer.getDeck().discardAllCards();
 		
 		assertTrue(this.globalPlayer.getDeck().pickCard());
		assertEquals(this.globalPlayer.getDeck().getAllCards().size(),1);
 	}
 
 	@Test
 	public void testPickCardsWith3Cards() {
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		this.globalPlayer.getDeck().discardAllCards();
 		
 		assertTrue(this.globalPlayer.getDeck().pickCards(3));
 		assertEquals(this.globalPlayer.getDeck().getHandDeckSize(),3);
 	}
 	
 	@Test
 	public void testPickCardsWith8Cards() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Schäfchen"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Prisma"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		this.globalPlayer.getDeck().discardAllCards();
 		
 		assertTrue(this.globalPlayer.getDeck().pickCards(8));
 		assertEquals(this.globalPlayer.getDeck().getAllCards().size(),6);
 	}
 	
 	@Test
 	public void testPickCardsWith2CardsWithoutDiscardingCards() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Schäfchen"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Prisma"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Rauchquarz"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Zaubersprüche"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		
 		assertFalse(this.globalPlayer.getDeck().pickCards(2));
 	}
 	
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAbout() {
 		Collection<Card> cards = this.gameCardFactory.createDefaultDeck();
 		Player player = this.playerFactory.createPlayer("Player", cards, 0, 0, 0, 0);
 		
 		player.getDeck().discardAllCards();
 		assertTrue(player.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(14));
 	}
 	
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAbout100() {
 		Collection<Card> cards = this.gameCardFactory.createDefaultDeck();
 		Player player = this.playerFactory.createPlayer("Player", cards, 0, 0, 0, 0);
 		
 		player.getDeck().discardAllCards();
 		assertFalse(player.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(100));
 	}
 	
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAboutWithBrick() {
 		Card card = this.gameCardFactory.createCard("Katapult");
 		this.cardsOnHand.add(card);
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		
 		this.globalPlayer.getDeck().discardAllCards();
 		assertTrue(this.globalPlayer.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(14));
 		assertTrue(this.globalPlayer.getDeck().getAllCards().contains(card));
 	}	
 
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAboutWithCrystal() {
 		Card card = this.gameCardFactory.createCard("Drachenauge");
 		this.cardsOnHand.add(card);
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		
 		this.globalPlayer.getDeck().discardAllCards();
 		assertTrue(this.globalPlayer.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(20));
 		assertTrue(this.globalPlayer.getDeck().getAllCards().contains(card));
 	}
 	
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAboutWithMonsters() {
 		Card card = this.gameCardFactory.createCard("Drache");
 		this.cardsOnHand.add(card);
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		
 		this.globalPlayer.getDeck().discardAllCards();
 		assertTrue(this.globalPlayer.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(24));
 		assertTrue(this.globalPlayer.getDeck().getAllCards().contains(card));
 	}	
 	
 	@Test
 	public void testPickCardFromDeckStackOrCemeteryDeckWithCostAboutAndWithoutDiscardingCards() {
 		Collection<Card> cards = this.gameCardFactory.createDefaultDeck();
 		Player player = this.playerFactory.createPlayer("Player", cards, 0, 0, 0, 0);
 		
 		assertFalse(player.getDeck().pickCardFromDeckStackOrCemeteryDeckWithCostAbout(14));
 	}	
 
 	@Test
 	public void testPickNumberOfCardsWithType() {
 		Collection<Card> cards = new ArrayList<Card>();
 		
 		Card cardEisdrache = this.gameCardFactory.createCard("Eisdrache");
 		Card cardKaravane = this.gameCardFactory.createCard("Karavane");
 		Card cardVulkanausbruch = this.gameCardFactory.createCard("Vulkanausbruch");
 		
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Prisma"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Rauchquarz"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Schäfchen"));
 		this.cardsOnHand.add(cardKaravane);
 		this.cardsOnHand.add(cardEisdrache);
 		this.cardsOnHand.add(cardVulkanausbruch);
 		
 		cards.add(cardKaravane);
 		cards.add(cardEisdrache);
 		cards.add(cardVulkanausbruch);
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 
 		this.globalPlayer.getDeck().discardAllCards();
 		
 		assertTrue(this.globalPlayer.getDeck().pickNumberOfCardsWithType(3, CardType.SPECIAL));
 		assertEquals(this.globalPlayer.getDeck().getHandDeckSize(), 3);
 		assertTrue(this.globalPlayer.getDeck().getAllCards().containsAll(cards));
 	}
 
 	@Test
 	public void testPickNumberOfCardsWithTypeWithoutDiscardingCards() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Paradoxon"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Vulkanausbruch"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 		
 		assertFalse(this.globalPlayer.getDeck().pickNumberOfCardsWithType(2, CardType.SPECIAL));
 		assertEquals(this.globalPlayer.getDeck().getAllCards().size(), 6);
 	}
 	
 	@Test
 	public void testPickNumberOfCardsWithTypeWithNumber8() {
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Auferstehung"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Baumgeist"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Dämonin"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Eisdrache"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Karavane"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Kometenschweif"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Paradoxon"));
 		this.cardsOnHand.add(this.gameCardFactory.createCard("Vulkanausbruch"));
 		
 		this.globalPlayer = this.playerFactory.createPlayer("Player", this.cardsOnHand, 0, 0, 0, 0);
 
 		this.globalPlayer.getDeck().discardAllCards();
 		
 		assertTrue(this.globalPlayer.getDeck().pickNumberOfCardsWithType(8, CardType.SPECIAL));
 		assertEquals(this.globalPlayer.getDeck().getAllCards().size(), 6);
 	}	
 	
 	@Test
 	public void testExchangeCardsWithHandDeck() {
 		Collection<Card> cardsPlayerOne = this.gameCardFactory.createDefaultDeck();
 		Collection<Card> cardsPlayerTwo = this.gameCardFactory.createDefaultDeck();
 		
 		Player playerOne = this.playerFactory.createPlayer("Player One", cardsPlayerOne, 0, 0, 0, 0);
 		Player playerTwo = this.playerFactory.createPlayer("Player Two", cardsPlayerTwo, 0, 0, 0, 0);
 		
 		List<Card> orginalCardsPlayerOne = new ArrayList<Card>(playerOne.getDeck().getAllCards());
 		List<Card> orginalCardsPlayerTwo = new ArrayList<Card>(playerTwo.getDeck().getAllCards());
 		
 		playerOne.getDeck().exchangeCardsWithHandDeck(playerTwo.getDeck());
 		
 		List<Card> changedCardsPlayerOne = new ArrayList<Card>(playerOne.getDeck().getAllCards());
 		List<Card> changedCardsPlayerTwo = new ArrayList<Card>(playerTwo.getDeck().getAllCards());
 
 		assertEquals(orginalCardsPlayerOne, changedCardsPlayerTwo);
 		assertEquals(orginalCardsPlayerTwo,  changedCardsPlayerOne);
 	}
 }
 
