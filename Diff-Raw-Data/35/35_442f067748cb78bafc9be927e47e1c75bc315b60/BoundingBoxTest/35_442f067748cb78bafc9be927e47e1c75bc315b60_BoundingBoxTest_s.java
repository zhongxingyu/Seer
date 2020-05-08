 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.BoundingBox;
 import toctep.skynet.backend.dal.domain.BoundingBoxType;
 
 public class BoundingBoxTest extends DomainTest{
 
 	private BoundingBox boundingBox;
 	
 	private BoundingBoxType boundingBoxType;
 	private String coordinates;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		boundingBox = new BoundingBox();
 		
 		boundingBoxType = new BoundingBoxType();
 		boundingBox.setType(boundingBoxType);
 		
 		coordinates = "58.17, 68.20";
 		boundingBox.setCoordinates(coordinates);
 	}
 	
 	@Override
 	public void testCreate() {
 		assertNotNull(boundingBox);
 		assertTrue(boundingBox.getType() == boundingBoxType);
 		assertTrue(coordinates.equals(boundingBox.getCoordinates()));
 	}
 
 	@Override
 	public void testInsert() {	
 		boundingBox.save();
 		assertEquals(1, boundingBoxDao.count());
 		assertEquals(1, boundingBox.getId());
 	}
 
 	@Override
 	public void testSelect() {
		boundingBoxDao.insert(boundingBox);
 		
 		BoundingBox postBoundingBox = (BoundingBox) boundingBoxDao.select(boundingBox.getId());
 		
 		assertTrue(postBoundingBox.getType().equals(boundingBox.getType()));
 		assertTrue(postBoundingBox.getCoordinates().equals(boundingBox.getCoordinates()));
 	}
 	
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void testDelete() {
 		boundingBoxDao.insert(boundingBox);
 		assertEquals(1, boundingBoxDao.count());
 		boundingBoxDao.delete(boundingBox);
 		assertEquals(0, boundingBoxDao.count());		
 	}
 }
