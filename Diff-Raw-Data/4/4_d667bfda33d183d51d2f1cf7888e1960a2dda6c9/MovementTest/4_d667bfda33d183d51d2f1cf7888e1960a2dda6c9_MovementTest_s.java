 package de.htwg.se.dog.controller;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.se.dog.models.*;
 
 public class MovementTest {
 	Movement movement;
 	GameField gamefield;
 	Player tp1, tp2;
 	Field[] array;
 	private final int PLAYERCOUNT = 2;
 	private final int FIELDSTILLHOUSE = 2;
 	private final int HOUSECOUNT = 2;
 	private final int FIGCOUNT = 2;
 	private final int PLAYERID1 = 1;
 	private final int PLAYERID2 = 2;
 	private final int ONE = 1;
 	private final int ZERO = 0;
 	
 	@Before
 	public void setUp() throws Exception {
 		movement = new Movement();
 		gamefield = new GameField(FIELDSTILLHOUSE, PLAYERCOUNT, HOUSECOUNT);
 		tp1 = new Player(PLAYERID1, FIGCOUNT);
 		tp2 = new Player(PLAYERID2, FIGCOUNT);
 		array = gamefield.getGamefield();
 
 	}
 
 	@Test
 	public void testMoveFigure() {
 		//Move Figure from empty Field
 		assertFalse(movement.moveFigure(gamefield, ONE, ZERO));
 		array[ZERO].putFigure(tp1.removeFigure());
 		Figure tmpZERO = array[ZERO].getFigure();
 		array[ONE].putFigure(tp1.removeFigure());
 		array[ONE].setBlocked(true);
 		array[5].putFigure(tp2.removeFigure());
 		//Move Figure over blocked field
 		assertFalse(movement.moveFigure(gamefield, ONE, ZERO));
 		array[ONE].setBlocked(false);
 		//Move Figure to occupied field
 		assertTrue(movement.moveFigure(gamefield, 5, 0));
 		//Is startfield empty
 		assertNull(array[0].getFigure());
 		//Is figure moved correctly
 		assertEquals(tmpZERO.getFignr(), array[5].getFigure().getFignr());
		//Move Figure to empty field
		Figure tmpONE = array[ONE].getFigure();
		movement.moveFigure(gamefield, 1, 1);
		assertEquals(tmpONE.getFignr(), array[2].getFigure().getFignr());
 	}
 
 	@Test
 	public void testvalidMove() {
 		array[ONE].putFigure(tp1.removeFigure());
 		array[ONE].setBlocked(true);
 		array[ZERO].putFigure(tp1.removeFigure());
 		assertFalse(movement.validMove(gamefield, 4, ZERO));
 	}
 
 }
