 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.Hashtag;
 
 public class HashtagTest extends DomainTest{
 
 	private Hashtag hashtag;
 	
 	private String text;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		hashtag = new Hashtag();
 		
 		text = "toctep";
 		hashtag.setText(text);
 	}
 	
 	@Override
 	public void testCreate() { 
 		assertNotNull(hashtag);
 		assertTrue(text.equals(hashtag.getText()));
 	}
 
 	@Override
 	public void testInsert() {
		hashtagDao.insert(hashtag);
 		assertEquals(1, hashtagDao.count());
 		assertEquals(1, hashtag.getId());
 	}
 	
 	@Override
 	public void testSelect() {
 		hashtag.save();
 		
 		Hashtag postHashtag = (Hashtag) hashtagDao.select(hashtag.getId());
 		
 		assertTrue(postHashtag.getText().equals(hashtag.getText()));
 	}
 	
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void testDelete() {
 		hashtagDao.insert(hashtag);
 		assertEquals(1, geoTypeDao.count());
 		hashtagDao.delete(hashtag);
 		assertEquals(0, geoTypeDao.count());		
 	}
 
 }
