 package clueGame;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class GameActionTests {
 	
 	ClueGame control;
 	Board board;
     Card mustardCard;
     Card whiteCard;
     Card diningRoomCard;
     Card studyCard;
     Card candlestickCard;
     Card revolverCard;
     Card peacockCard;
     Card libraryCard;
     Card wrenchCard;
     Card knifeCard;
     Card greenCard;
     Card plumCard;
     Card ballroomCard;
     Card kitchenCard;
     Card hallCard;
     Card pipeCard;
     Card scarletCard;
     Card ropeCard;
     Card conservatoryCard;
 
     @Before
     public void setUp()
     {
     	board = new Board();
         control = new ClueGame(123, 6, board);
         mustardCard = new Card("Colonel Mustard", Card.CardType.PERSON);
         whiteCard = new Card("Mrs. White", Card.CardType.PERSON);
         peacockCard = new Card("Mrs. Peacock", Card.CardType.PERSON);
         greenCard = new Card("Mr. Green", Card.CardType.PERSON);
         plumCard = new Card("Professor Plum", Card.CardType.PERSON);
         scarletCard = new Card("Miss Scarlet", Card.CardType.PERSON);
         diningRoomCard = new Card("Dining Room", Card.CardType.ROOM);
         studyCard = new Card("Study", Card.CardType.ROOM);
         ballroomCard = new Card("Ballroom", Card.CardType.ROOM);
         kitchenCard = new Card("Kitchen", Card.CardType.ROOM);
         hallCard = new Card("Hall", Card.CardType.ROOM);
         libraryCard = new Card("Library", Card.CardType.ROOM);
         conservatoryCard = new Card("Conservatory", Card.CardType.ROOM);
         candlestickCard = new Card("Candlestick", Card.CardType.WEAPON);
         revolverCard = new Card("Revolver", Card.CardType.WEAPON);
         wrenchCard = new Card("Wrench", Card.CardType.WEAPON);
         knifeCard = new Card("Knife", Card.CardType.WEAPON);
         pipeCard = new Card("Lead Pipe", Card.CardType.WEAPON);
         ropeCard = new Card("Rope", Card.CardType.WEAPON);
     }
 	
     @Test
 	public void testAccusation()
     {
         Assert.assertTrue(control.checkAccusation(plumCard, wrenchCard, kitchenCard));
         Assert.assertFalse(control.checkAccusation(plumCard, revolverCard, kitchenCard));
         Assert.assertFalse(control.checkAccusation(scarletCard, wrenchCard, kitchenCard));
         Assert.assertFalse(control.checkAccusation(plumCard, wrenchCard, ballroomCard));
     }
 
 	//change values
     @Test
     public void testSuggestion()
     {
         ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
         player.addCard(peacockCard);
         player.addCard(mustardCard);
         player.addCard(knifeCard);
         player.addCard(candlestickCard);
         player.addCard(libraryCard);
         player.addCard(ballroomCard);
         Assert.assertEquals(peacockCard, player.disproveSuggestion(peacockCard, studyCard, wrenchCard));
         Assert.assertEquals(libraryCard, player.disproveSuggestion(greenCard, libraryCard, wrenchCard));
         Assert.assertEquals(knifeCard, player.disproveSuggestion(greenCard, studyCard, knifeCard));
         Assert.assertEquals(null, player.disproveSuggestion(greenCard, studyCard, wrenchCard));
     }
 
     //change values & funciton names
     @Test
     public void testSuggestion2()
     {
         ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
         player.addCard(peacockCard);
         player.addCard(knifeCard);
         player.addCard(revolverCard);
         player.addCard(libraryCard);
         int numRoom = 0;
         int numWeapon = 0;
         int numPerson = 0;
         for(int i = 0; i < 50; i++)
         {
             Card result = player.disproveSuggestion(peacockCard, libraryCard, knifeCard);
             if(result.getCardName().equals("Mrs. Peacock"))
                 numPerson++;
             else if(result.getCardName().equals("Library"))
                 numRoom++;
             else if(result.getCardName().equals("Knife"))
                 numWeapon++;
             else
                 Assert.fail();
         }
 
         Assert.assertTrue(numRoom > 0);
         Assert.assertTrue(numPerson > 0);
         Assert.assertTrue(numWeapon > 0);
     }
 
     //changes values and function names and look at functionality
     @Test
     public void testSuggestion3()
     {
         ArrayList<Player> players = new ArrayList<Player>();
         ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
         player.addCard(peacockCard);
         player.addCard(knifeCard);
         player.addCard(revolverCard);
         player.addCard(libraryCard);
         players.add(player);
         player = new ComputerPlayer("Mr. Green", Color.green, control);
         player.addCard(plumCard);
         player.addCard(wrenchCard);
         player.addCard(candlestickCard);
         player.addCard(ballroomCard);
         players.add(player);
         player = new ComputerPlayer("Mrs. Peacock", Color.blue, control);
         player.addCard(mustardCard);
         player.addCard(pipeCard);
         player.addCard(kitchenCard);
         player.addCard(hallCard);
         players.add(player);
         control.setPlayers(players);
         Assert.assertEquals(null, control.testSuggestion(scarletCard, diningRoomCard, ropeCard, player));
         Assert.assertEquals(null, control.testSuggestion(scarletCard, studyCard, ropeCard, player));
         Assert.assertEquals(libraryCard, control.testSuggestion(scarletCard, libraryCard, ropeCard, player));
         Assert.assertEquals(peacockCard, control.testSuggestion(peacockCard, diningRoomCard, ropeCard, player));
         Assert.assertEquals(wrenchCard, control.testSuggestion(scarletCard, diningRoomCard, wrenchCard, player));
         Assert.assertEquals(null, control.testSuggestion(scarletCard, diningRoomCard, pipeCard, player));
         int plum = 0;
         int library = 0;
         int pipe = 0;
         for(int i = 0; i < 100; i++)
         {
             Card returnCard = control.testSuggestion(plumCard, libraryCard, pipeCard, player);
             if(returnCard.equals(plumCard))
                 plum++;
             else
             if(returnCard.equals(libraryCard))
                 library++;
             else
             if(returnCard.equals(pipeCard))
                 pipe++;
         }
 
         Assert.assertEquals(100L, plum + library + pipe);
         Assert.assertTrue(plum > 1);
         Assert.assertTrue(library > 1);
         Assert.assertTrue(pipe == 0);
     }
     
     //change values and function name
     @Test
     public void testSuggestionx2()
     {
         ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
         player.addCard(peacockCard);
         player.addCard(mustardCard);
         player.addCard(knifeCard);
         player.addCard(candlestickCard);
         player.addCard(libraryCard);
         player.addCard(ballroomCard);
         Assert.assertEquals(peacockCard, player.disproveSuggestion(peacockCard, studyCard, wrenchCard));
         Assert.assertEquals(libraryCard, player.disproveSuggestion(greenCard, libraryCard, wrenchCard));
         Assert.assertEquals(knifeCard, player.disproveSuggestion(greenCard, studyCard, knifeCard));
         Assert.assertEquals(null, player.disproveSuggestion(greenCard, studyCard, wrenchCard));
     }
 
     //change values
     @Test
     public void testSuggestionx()
     {
         ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
         player.addCard(peacockCard);
         player.addCard(knifeCard);
         player.addCard(revolverCard);
         player.addCard(libraryCard);
         Assert.assertEquals(knifeCard, player.disproveSuggestion(greenCard, studyCard, knifeCard));
         Assert.assertEquals(revolverCard, player.disproveSuggestion(greenCard, studyCard, revolverCard));
         Assert.assertEquals(null, player.disproveSuggestion(greenCard, studyCard, wrenchCard));
     }
 
     //change values
     @Test
     public void testSelectTargetRoom()
     {
         ComputerPlayer compPlayer = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
        compPlayer.setLocation(VALUE);
         board.startTargets(96, 3);
         for(int i = 0; i < 100; i++)
            Assert.assertEquals(VALUES, compPlayer.pickLocation(board.getTargets()));
     }
 
     //change values and function name
     @Test
     public void testSelectTarget2()
     {
     	ComputerPlayer player = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
 		// Pick a location with no rooms in target, just three targets
 		board.startTargets(board.calcIndex(14, 0), 2);
 		int loc_12_0Tot = 0;
 		int loc_14_2Tot = 0;
 		int loc_15_1Tot = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			BoardCell selected = player.pickLocation(board.getTargets());
 			if (selected == board.getCells(board.calcIndex(12, 0)))
 				loc_12_0Tot++;
 			else if (selected == board.getCells(board.calcIndex(14, 2)))
 				loc_14_2Tot++;
 			else if (selected == board.getCells(board.calcIndex(15, 1)))
 				loc_15_1Tot++;
 			else
 				Assert.fail("Invalid target selected");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		Assert.assertEquals(100, loc_12_0Tot + loc_14_2Tot + loc_15_1Tot);
 		// Ensure each target was selected more than once
 		Assert.assertTrue(loc_12_0Tot > 10);
 		Assert.assertTrue(loc_14_2Tot > 10);
 		Assert.assertTrue(loc_15_1Tot > 10);
     }
 
     //change values
     @Test
     public void testMakeSuggestion()
     {
         ComputerPlayer compPlayer = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
        compPlayer.setLocation(VALUE);
         compPlayer.updateSeenCards(mustardCard);
         compPlayer.updateSeenCards(peacockCard);
         compPlayer.updateSeenCards(scarletCard);
         compPlayer.updateSeenCards(plumCard);
         compPlayer.updateSeenCards(greenCard);
         compPlayer.updateSeenCards(candlestickCard);
         compPlayer.updateSeenCards(knifeCard);
         compPlayer.updateSeenCards(pipeCard);
         compPlayer.updateSeenCards(ropeCard);
         compPlayer.updateSeenCards(revolverCard);
         compPlayer.makeSuggestion(compPlayer.getMyCards());
         Assert.assertEquals(conservatoryCard, compPlayer.getGuess());
         Assert.assertEquals(whiteCard, compPlayer.getGuess());
         Assert.assertEquals(wrenchCard, compPlayer.getGuess());
     }
 
     //change values
     @Test
     public void testMakeSuggestion2()
     {
         ComputerPlayer compPlayer = new ComputerPlayer("Miss Scarlet", Color.magenta, control);
        compPlayer.setLocation(VALUE);
         compPlayer.updateSeenCards(mustardCard);
         compPlayer.updateSeenCards(peacockCard);
         compPlayer.updateSeenCards(plumCard);
         compPlayer.updateSeenCards(greenCard);
         ArrayList<Card> seenCards = compPlayer.getMyCards();
         compPlayer.updateSeenCards(candlestickCard);
         compPlayer.updateSeenCards(knifeCard);
         compPlayer.updateSeenCards(pipeCard);
         compPlayer.updateSeenCards(ropeCard);
         compPlayer.updateSeenCards(revolverCard);
         compPlayer.makeSuggestion(compPlayer.getMyCards());
         Assert.assertEquals(conservatoryCard, compPlayer.getGuess());
         int totScarlet = 0;
         int totPlum = 0;
         for(int i = 0; i < 100; i++)
         {
             compPlayer.makeSuggestion(compPlayer.getMyCards());
             Card pCard = compPlayer.getGuess();
             if(pCard.equals(scarletCard))
                 totScarlet++;
             else
             if(pCard.equals(whiteCard))
                 totPlum++;
             pCard = compPlayer.getGuess();
             Assert.assertEquals(wrenchCard, compPlayer.getGuess());
         }
 
         Assert.assertEquals(100L, totScarlet + totPlum);
         Assert.assertTrue(totScarlet > 0);
         Assert.assertTrue(totPlum > 0);
     }
 }
