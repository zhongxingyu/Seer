 package jp.gr.java_conf.konkonlab.game_of_life.models.test;
 
 import jp.gr.java_conf.konkonlab.game_of_life.models.Cell;
 import junit.framework.TestCase;
 
 public class CellTest extends TestCase {
 
 	Cell deadCell;
 	Cell aliveCell;
 	
 	@Override
 	protected void setUp() throws Exception {
		deadCell = Cell.createDeadCell();
		aliveCell = Cell.createAliveCell();
 		super.setUp();
 	}
 	
 	public void testInitAlive() throws Exception {
 		assertEquals("ALIVE", aliveCell.toString());
 		assertTrue(aliveCell.isAlive());
 	}
 
 	public void testInitDead() throws Exception {
 		assertEquals("DEAD", deadCell.toString());
 		assertFalse(deadCell.isAlive());
 	}
 	
 	public void testGroupArive() throws Exception {
 		aliveCell.setGroup(1);
 		assertEquals(1, aliveCell.getGroup());
 	}
 	
 	public void testGroupDead() throws Exception {
 		deadCell.setGroup(1); // ignored
 		assertEquals(0, deadCell.getGroup());
 	}
 	
 	public void testCreateNextGenerationStillDead() throws Exception {
 		Cell nextGenCell = deadCell.createNextGeneration(2);
 		assertFalse(nextGenCell.isAlive());
 	}
 	
 	public void testCreateNextGenerationBirth() throws Exception {
 		Cell nextGenCell = deadCell.createNextGeneration(3);
 		assertTrue(nextGenCell.isAlive());
 	}
 	
 	public void testCreateNextGenerationExist() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(3);
 		assertTrue(nextGenCell.isAlive());
 	}
 
 	public void testCreateNextGenerationTooFew() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(1);
 		assertFalse(nextGenCell.isAlive());
 	}
 
 	public void testCreateNextGenerationTooMany() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(4);
 		assertFalse(nextGenCell.isAlive());
 	}
 }
