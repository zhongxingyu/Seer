 package com.parq.server.dao;
 
 import java.util.List;
 
 import com.parq.server.dao.model.object.Grid;
 import com.parq.server.dao.model.object.SimpleGrid;
 
 import junit.framework.TestCase;
 
 public class TestGridDao extends TestCase {
 	private GridDao dao;
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		dao = new GridDao();
 	}
 
 	public void testGetAllCompleteGridHiarchy() {
 		List<Grid> grids = dao.getAllCompleteGridHiarchy();
 		assertNotNull(grids);
 	}
 
 	public void testFindSimpleGridNearBy() {
		List<SimpleGrid> grids = dao.findSimpleGridNearBy(180, 180, -180, -180);
 		assertNotNull(grids);
 		assertTrue(grids.size() > 0);
 	}
 	
 	public void testFindGridSpeed() {
 		
 		long startTime = System.currentTimeMillis();
 		for (int i = 0; i < 1000; i++) {
 			dao.findSimpleGridNearBy(-180, -180, 180, 180);
 		}
 		long endTime = System.currentTimeMillis();
 		
 		System.out.println("1000 call took: " + ((0.0 + endTime - startTime) / 1000) + " seconds"); 
 	}
 }
