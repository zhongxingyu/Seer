 package bombgame.controller.ai.impl;
 
 
 import bombgame.controller.ai.impl.ManAI;
 import bombgame.controller.ai.impl.Position;
 import bombgame.controller.gamehandler.impl.GameHandler;
 import bombgame.entities.impl.GameObject;
 import bombgame.entities.impl.Man;
 import bombgame.entities.impl.Wall;
 import junit.framework.TestCase;
 
 public class ManAITest extends TestCase {
 	
 	private ManAI ai;
 	private Man man;
 	private GameHandler gh;
 	
 	public void setUp() {
 		gh = new GameHandler(new GameObject[10][10]);
 		man = new Man(1,1);
 		gh.addObject(man);
 		ai = new ManAI(man,gh);
 		
 	}
 	
 	public void testCalcNextStep() {
 		ai.calcNextStep();
 		assertNull(ai.getTarget());
 		assertNull(ai.getFocusedEnemy());
 		
 		gh.addObject(new Man(9,9));		
 		ai.setTurns( 0);
 		ai.calcNextStep();
 		assertNotNull(ai.getTarget());
 		assertNotNull(ai.getFocusedEnemy());
 		
 		ai.setTarget(new Position(man.getX(), man.getY()));
 		ai.calcNextStep();
 		assertNull(ai.getTarget());
 		assertFalse(ai.getPlaceBomb());
 		assertEquals(man.getDirection(), Man.NO_DIR);
 		
 		ai.getPath().clear();
 		ai.calcNextStep();
 		assertFalse(ai.getPath().isEmpty());
 		
 		ai.calcNextStep();
 		
 		ai.getPath().clear();
 		ai.setMode( ManAI.FLEE_MODE);
 		ai.setTarget( null);
 		ai.calcNextStep();
		assertTrue(ai.getPath().isEmpty());
		assertNull(ai.getTarget());
 		
 		ai.setMode( 123);
 		ai.calcNextStep();
 		assertTrue(ai.getPath().isEmpty());
 		assertNull(ai.getTarget());
 		
 		gh.addObject(new Man(3,5));
 		ai = new ManAI(man, gh);
 		gh.addObject(new Wall(man.getX() + 1, man.getY()));
 		gh.addObject(new Wall(man.getX() , man.getY() + 1));
 		gh.addObject(new Wall(man.getX() -1 , man.getY()));
 		gh.addObject(new Wall(man.getX() , man.getY() - 1));
 		ai.calcNextStep();
 		assertTrue(ai.getPath().isEmpty());
 		assertNotNull(ai.getTarget());
 		
 		ai.getPath().push(new Position(man.getX() + 4, man.getY()));
 		ai.calcNextStep();
 	}
 	
 	public void testCheckTargetReached() {
 		ai.setTarget( new Position(man.getX(),man.getY() + 1));
 		assertFalse(ai.checkTargetReached());
 	}
 	
 	public void testCalcManDirection() {
 		ai.getPath().clear();
 		ai.getPath().push(new Position(man.getX() - 1, man.getY()));
 		ai.calcManDirection();
 		assertEquals(man.getDirection(),Man.LEFT);
 		
 		ai.getPath().clear();
 		ai.getPath().push(new Position(man.getX() , man.getY() + 1));
 		ai.calcManDirection();
 		assertEquals(man.getDirection(),Man.DOWN);
 		
 		ai.getPath().clear();
 		ai.getPath().push(new Position(man.getX(), man.getY() - 1));
 		ai.calcManDirection();
 		assertEquals(man.getDirection(),Man.UP);
 		
 		ai.getPath().clear();
 		ai.getPath().push(new Position(man.getX(), man.getY()));
 		ai.calcManDirection();
 		assertEquals(man.getDirection(),Man.NO_DIR);
 		
 		
 		
 	}
 	
 	public void testFocusEnemy() {
 		gh.addObject(new Man(9,9));
 		for(int i = 0; i < 20; i++) {
 			ai.focusEnemy();
 		}
 		assertNotNull(ai.getFocusedEnemy());
 		
 	}
 	
 	public void testGetNodes() {
 		assertNotNull(ai.getNodes());
 	}
 	
 	public void testGetHandler() {
 		assertNotNull(ai.getHandler());
 	}
 	
 	public void testSetFocusedEnemy() {
 		Man m = new Man (3,3);
 		ai.setFocusedEnemy(m);
 		assertEquals(m , ai.getFocusedEnemy());
 	}
 	
 	public void testToString() {
 		String s = "-> AI: [1] [1] T: NULL F: NULL Turn: 0 Direction: 0";
 		assertEquals(ai.toString(), s);
 		ai.setTarget(new Position(2,2));
 		ai.setFocusedEnemy( new Man(3,3));
 		s = "-> AI: [1] [1] T: [2] [2] F: [3] [3] Turn: 0 Direction: 0";
 		assertEquals(ai.toString(), s);
 	}
 
 }
