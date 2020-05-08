 package game;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestValidator {
 	
 	Validator validator, validator1, validator2, validator3, validator4, validatorAI; // 
 	
 	Navy navy, navy1, navy2, navy3, navy4, navyAI;// 
 	Coordinate c;
 	Coordinate co;
 	Submarine su, su1;	
 	
 	// Ships
 	Submarine sub1, sub2, sub3, sub4, sub5;
 	Destroyer d1, d2, d3;
 	Aircraft_carrier ac;
 	
 	// Coordinates for placement
 	Coordinate c1, c2, c3, c4, c5;
 	Coordinate c6, c7, c8, c9, c10, c11, c12, c13, c14;
 	Coordinate c15, c16, c17, c18, c19;
 	
 	ServerAI ai;
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Before
 	public void setUp() throws Exception 
 	{
 	// Validator, for the first test
 	validator = new Validator(2, 0, 0);
 	
 	// Coordinates, for two submarines
 	c = new Coordinate(1,3);
 	co = new Coordinate(1,3);
 	
 	// Two submarines, for the first test
 	su = new Submarine(c);
 	su1 = new Submarine(co);	
 	
 	// Navy with only two submarines for a first, simple, test
 	navy = new Navy(2, 0, 0);
 	navy.addShip(su);
	navy.addShip(su);
 	
 	// Coordinates and ships for further testing, building bigger navys
 	c1 = new Coordinate(0,0);
 	c2 = new Coordinate(0,3);
 	c3 = new Coordinate(0,9);
 	c4 = new Coordinate(3,6);
 	c5 = new Coordinate(4,9);
 	c6 = new Coordinate(5,1);
 	c7 = new Coordinate(5,2);
 	c8 = new Coordinate(5,3);
 	c9 = new Coordinate(7,0);
 	c10 = new Coordinate(7,1);
 	c11 = new Coordinate(7,2);
 	c12 = new Coordinate(9,0);
 	c13 = new Coordinate(9,1);
 	c14 = new Coordinate(9,2);
 	c15 = new Coordinate(9,5);
 	c16 = new Coordinate(9,6);
 	c17 = new Coordinate(9,7);
 	c18 = new Coordinate(9,8);
 	c19 = new Coordinate(9,9);
 	
 	sub1 = new Submarine(c1);
 	sub2 = new Submarine(c2);
 	sub3 = new Submarine(c3);
 	sub4 = new Submarine(c4);
 	sub5 = new Submarine(c5);
 	
 	d1 = new Destroyer(c6, c7, c8);
 	d2 = new Destroyer(c9, c10, c11);
 	d3 = new Destroyer(c12, c13, c14);
 	
 	ac = new Aircraft_carrier(c15, c16, c17, c18, c19);
 	
 	// Validator, for the second test
 	validator1 = new Validator(0, 1, 0);
 	navy1 = new Navy(0, 1, 0);
 	navy1.addShip(d1);
 	
 	// Validator, for the third test
 	validator2 = new Validator(0, 3, 0);
 	navy2 = new Navy(0, 3, 0);
 	navy2.addShip(d1);
 	navy2.addShip(d2);
 	navy2.addShip(d3);
 	
 	// Validator, for the fourth test
 	validator3 = new Validator(0, 0, 1);
 	navy3 = new Navy(0, 0, 1);
 	navy3.addShip(ac);
 	
 	// Validator, for the fifth test
 	validator4 = new Validator(5, 3, 1);
 	navy4 = new Navy(5, 3, 1);
 	navy4.addShip(sub1);
 	navy4.addShip(sub2);
 	navy4.addShip(sub3);
 	navy4.addShip(sub4);
 	navy4.addShip(sub5);
 	navy4.addShip(d1);
 	navy4.addShip(d2);
 	navy4.addShip(d3);
 	navy4.addShip(ac);
 	
 	//-----------------------------------------------------------
 	validatorAI = new Validator(5, 3, 1);	
 	// Getting navy from AI to validate in validator2
 	ai = new ServerAI(5, 3, 1);
 	navyAI = ai.getNavy();
 	}
 	
 	@Test
 	public void test() {
 		
 		 // Validates a navy with two submarines
 		 assertEquals(true , validator.validateNavy(navy));
 		 assertEquals(true , navy.allSet());
 		 assertEquals(2 , navy.numberShips());
		 assertEquals(true , c.equals(co));
 		 
 		 // Second test - navy with one destroyer only
 		 /*assertEquals(true , validator1.validateNavy(navy1));
 		 assertEquals(true , navy1.allSet());
 		 assertEquals(1 , navy1.numberShips());*/
 		 
 		// Third test - navy with three destroyers only
 		/*assertEquals(true , validator2.validateNavy(navy2));
 		assertEquals(true , navy2.allSet());
 		assertEquals(3 , navy2.numberShips());*/
 		
 		// Fourth test - navy with one aircraft carrier only
 		/*assertEquals(true , validator3.validateNavy(navy3));
 		assertEquals(true , navy3.allSet());
 		assertEquals(1 , navy3.numberShips());*/
 		
 		// Fifth test - navy with mixed ships - no aircraft carrier
 		/*assertEquals(true ,validator4.validateNavy(navy4));
 		assertEquals(true ,navy4.allSet());
 		assertEquals(9 ,navy4.numberShips());*/
 		 
 		// Trying to validate navy from ServerAI
 		/*assertEquals(true , navyAI.allSet());
 		assertEquals(true , validatorAI.validateNavy(ai.getNavy()));
 		
		*/
 		 
 	}
 
 }
