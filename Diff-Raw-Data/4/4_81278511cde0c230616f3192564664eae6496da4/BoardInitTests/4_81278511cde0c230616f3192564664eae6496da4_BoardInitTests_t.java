 package ch.bfh.monopoly.tests;
 
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Color;
 import java.util.Locale;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.bfh.monopoly.common.*;
 
 
 
 
 
 
 public class BoardInitTests {
 
 	Locale loc;
 	GameClient gc ;
 	/** 
 	 * Setup this test class to test the creation of the board with 
 	 * the local set to English
 	 * */
 	@Before
 	public void setup(){
 		loc = new Locale("EN");
 		gc = new GameClient();
 	}
 	
 	
 	@Test
 	public void chanceCardsCreatedWithCorrectInfo() {
 
 		Board board= new Board(loc,gc);
 		int tileNumber;
 		//Tests some Terrains
 		//System.out.println(sm.tiles[1]);
 		assertTrue(((Chance)board.tiles[7]).getName().equals("Chance"));
 		assertTrue(((Chance)board.tiles[22]).getName().equals("Chance"));
 		assertTrue(((Chance)board.tiles[36]).getName().equals("Chance"));
 
 		Chance chanceTile = (Chance)board.tiles[36];
 		MovementEvent mv = ((MovementEvent)chanceTile.chanceCardDeck[0]);
 		assertTrue(mv.getName().equals("Back you go!"));
 		assertTrue(mv.getNewPosition()== -3);
 		mv = ((MovementEvent)chanceTile.chanceCardDeck[1]);
 		assertTrue(mv.getName().equals("Advance to Pennsylvania Railroad"));
	
 
 	}
 	
 	@Test
 	public void tilesCreatedWithCorrectInfo() {
 
 		Board board= new Board(loc,gc);
 		int tileNumber;
 		//Tests some Terrains
 		//System.out.println(sm.tiles[1]);
 		assertTrue(((Terrain)board.tiles[1]).getName().equals("Mediterranean Avenue"));
 		assertTrue(((Terrain)board.tiles[1]).getPrice()==60);
 		assertTrue(((Terrain)board.tiles[1]).getHouseCost()==50);
 		assertTrue(((Terrain)board.tiles[1]).getHotelCost()==50);
 		assertTrue(((Terrain)board.tiles[1]).getMortgageValue()==30);
 	
 		assertTrue(((Terrain)board.tiles[14]).getName().equals("Virginia Avenue"));
 		assertTrue(((Terrain)board.tiles[14]).getPrice()==160);
 		assertTrue(((Terrain)board.tiles[14]).getHouseCost()==100);
 		assertTrue(((Terrain)board.tiles[14]).getHotelCost()==100);
 		assertTrue(((Terrain)board.tiles[14]).getMortgageValue()==80);
 		//System.out.println(sm.tiles[14]);
 		
 		assertTrue(((Terrain)board.tiles[39]).getName().equals("Boardwalk"));
 		assertTrue(((Terrain)board.tiles[39]).getPrice()==400);
 		assertTrue(((Terrain)board.tiles[39]).getHouseCost()==200);
 		assertTrue(((Terrain)board.tiles[39]).getHotelCost()==200);
 		assertTrue(((Terrain)board.tiles[39]).getMortgageValue()==200);
 		//System.out.println(sm.tiles[39]);
 		
 		////////////////////
 		//Tests some Railroads
 		tileNumber  = 5;
 		assertTrue(((Railroad)board.tiles[tileNumber]).getName().equals("Reading Railroad"));
 		assertTrue(((Railroad)board.tiles[tileNumber]).getPrice()==200);
 		assertTrue(((Railroad)board.tiles[tileNumber]).getRent()==25);
 		assertTrue(((Railroad)board.tiles[tileNumber]).getMortgageValue()==100);
 		//System.out.println(sm.tiles[tileNumber]);
 		
 		tileNumber  = 25;
 		assertTrue(((Railroad)board.tiles[tileNumber]).getName().equals("B&O Railroad"));
 		assertTrue(((Railroad)board.tiles[tileNumber]).getPrice()==200);
 		assertTrue(((Railroad)board.tiles[tileNumber]).getRent()==25);
 		assertTrue(((Railroad)board.tiles[tileNumber]).getMortgageValue()==100);
 		//System.out.println(sm.tiles[tileNumber]);
 		
 		
 		////////////////////
 		//Tests some UTILITIES
 		tileNumber  = 12;
 		assertTrue(((Utility)board.tiles[tileNumber]).getName().equals("Electric Company"));
 		assertTrue(((Utility)board.tiles[tileNumber]).getPrice()==150);
 		assertTrue(((Utility)board.tiles[tileNumber]).getMortgageValue()==75);
 		System.out.println(board.tiles[tileNumber]);
 		
 		tileNumber  = 28;
 		assertTrue(((Utility)board.tiles[tileNumber]).getName().equals("Water Works"));
 		assertTrue(((Utility)board.tiles[tileNumber]).getPrice()==150);
 		assertTrue(((Utility)board.tiles[tileNumber]).getMortgageValue()==75);
 		System.out.println(board.tiles[tileNumber]);
 		
 	}
 	
 	@Test
 	public void playerOwnsProperty() {
 		Board sm = new Board(loc,gc);
 		Player p = new Player("Justin", 5000);
 		Tile t = sm.tiles[1];
 		p.addProperty(t);
 		assertTrue(p.ownsProperty(t));
 	}
 	
 	@Test
 	public void propertyTransfer() {
 		Board sm = new Board(loc,gc);
 		Player j = new Player("Justin", 5000);
 		Player g = new Player("Giuseppe", 5000);
 		Tile t = sm.tiles[1];
 		j.addProperty(t);
 		assertTrue(((Property)t).getOwner().getName().equals("Justin"));
 		//we could also just use the method ownsProperty, but that method probably serves no purpose for game play, just testing
 		//assertTrue(p.ownsProperty(t));
 		g.addProperty(t);
 		assertTrue(((Property)t).getOwner().getName().equals("Giuseppe"));
 	}
 	
 }
