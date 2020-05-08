 package jp.gr.java_conf.konkonlab.game_of_life.models.test;
 
 import jp.gr.java_conf.konkonlab.game_of_life.models.Cell;
 import junit.framework.TestCase;
 
 public class CellTest extends TestCase {
 
 	Cell deadCell;
 	Cell aliveCell;
 	
 	@Override
 	protected void setUp() throws Exception {
 		deadCell = new Cell(Cell.DeadCell);
 		aliveCell = new Cell(Cell.AliveCell);
 		super.setUp();
 	}
 	
 	public void testInitAlive() throws Exception {
 		assertEquals("ALIVE", aliveCell.toString());
		assertTrue(aliveCell.equals(Cell.AliveCell));
 	}
 
 	public void testInitDead() throws Exception {
 		assertEquals("DEAD", deadCell.toString());
		assertTrue(deadCell.equals(Cell.DeadCell));
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
		assertTrue(nextGenCell.equals(Cell.DeadCell));
 	}
 	
 	public void testCreateNextGenerationBirth() throws Exception {
 		Cell nextGenCell = deadCell.createNextGeneration(3);
		assertTrue(nextGenCell.equals(Cell.AliveCell));
 	}
 	
 	public void testCreateNextGenerationExist() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(3);
		assertTrue(nextGenCell.equals(Cell.AliveCell));
 	}
 
 	public void testCreateNextGenerationTooFew() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(1);
		assertTrue(nextGenCell.equals(Cell.DeadCell));
 	}
 
 	public void testCreateNextGenerationTooMany() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(4);
		assertTrue(nextGenCell.equals(Cell.DeadCell));
 	}
 }
