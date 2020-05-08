 package edu.wheaton.simulator.test.datastructure;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Prototype;
 
 public class GridTest {
 
 	private Grid grid;
 
 	@Before
	public void setUp() {
 		grid = new Grid(20, 30);
 	}
 
 	@Test
 	public void sizeTest() {
 		Assert.assertTrue(grid.getWidth() == 20);
 		Assert.assertTrue(grid.getHeight() == 30);
 	}
 
 	@Test
 	public void positionTest() {
 		// in bounds
 		Assert.assertTrue(grid.emptyPos(0, 0));
 		Assert.assertTrue(grid.emptyPos(10, 10));
 		Assert.assertTrue(grid.emptyPos(19, 29));
 		
 		// out of bounds
 		Assert.assertFalse(grid.emptyPos(-10, -7));
 		Assert.assertFalse(grid.emptyPos(98, 100));
 	}
 
 	@Test
 	public void spiralSpawnTest() {
 		// Add six agents to the grid
 		Prototype p = new Prototype(grid, "Test");
 		for(int i = 0; i < 6; i++) 
 			grid.spiralSpawn(p.createAgent(), 1, 1);
 
 		// The upper right corner:
 		// ___________
 		// |x|x|x| | |
 		// | |x|x| | |
 		// | |x| | | |
 		// | | | | | |
 
 		Assert.assertTrue(IDOfAgentAt(1, 1) == 0);
 		Assert.assertTrue(IDOfAgentAt(0, 0) == 1);
 		Assert.assertTrue(IDOfAgentAt(1, 0) == 2);
 		Assert.assertTrue(IDOfAgentAt(2, 0) == 3);
 		Assert.assertTrue(IDOfAgentAt(2, 1) == 4);
 		Assert.assertTrue(IDOfAgentAt(2, 2) == 5);
 	}
 
 	@Test
 	public void horizontalSpawnTest() {
 		// Add six agents to the grid
 		Prototype p = new Prototype(grid, "Test");
 		for(int i = 0; i < 6; i++) 
 			grid.horizontalSpawn(p.createAgent(), 1);
 
 		Assert.assertTrue(IDOfAgentAt(0, 1) == 6);
 		Assert.assertTrue(IDOfAgentAt(1, 1) == 7);
 		Assert.assertTrue(IDOfAgentAt(2, 1) == 8);
 		Assert.assertTrue(IDOfAgentAt(3, 1) == 9);
 		Assert.assertTrue(IDOfAgentAt(4, 1) == 10);
 		Assert.assertTrue(IDOfAgentAt(5, 1) == 11);
 	}
 
 	@Test
 	public void verticalSpawnSpawnTest() {
 		// Add six agents to the grid
 		Prototype p = new Prototype(grid, "Test");
 		for(int i = 0; i < 6; i++) 
 			grid.verticalSpawn(p.createAgent(), 1);
 
 		Assert.assertTrue(IDOfAgentAt(1, 0) == 12);
 		Assert.assertTrue(IDOfAgentAt(1, 1) == 13);
 		Assert.assertTrue(IDOfAgentAt(1, 2) == 14);
 		Assert.assertTrue(IDOfAgentAt(1, 3) == 15);
 		Assert.assertTrue(IDOfAgentAt(1, 4) == 16);
 		Assert.assertTrue(IDOfAgentAt(1, 5) == 17);
 	}
 
 	/**
 	 * Helper method that provides the integer ID of the Agent at the given x/y
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private int IDOfAgentAt(int x, int y) {
 		return grid.getAgent(x, y).getID().getInt();
 	}
 	
 	@Test
 	public void removeTest() {
 		Prototype p = new Prototype(grid, "Test");
 		grid.spiralSpawn(p.createAgent(), 5, 5);
 		grid.removeAgent(5, 5);
 		Assert.assertNull(grid.getAgent(5, 5));
 	}
 }
