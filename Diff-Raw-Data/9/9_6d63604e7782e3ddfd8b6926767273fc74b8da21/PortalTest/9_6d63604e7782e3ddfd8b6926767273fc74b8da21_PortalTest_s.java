 package pl.edu.agh.megamud.tests.dao;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 
 import org.junit.*;
 
 import pl.edu.agh.megamud.dao.*;
 
 public class PortalTest extends TestBase{
 
 	@Before
 	public void setUp() throws Exception {
 		super.prepareDatabase();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		connectionSource.close();
 	}	
 	
 	@Test(expected = SQLException.class)
 	public void should_not_create_portal_without_locations() throws SQLException{
 		Portal portal = new Portal();
 		portal.setName("Some portal name");
 		portalDao.create(portal);
 	}
 	
 	@Test
 	public void should_create_portal() throws SQLException{
 		Location location1 = new Location();
 		location1.setName("Big room");
 		location1.setDescription("Some very big room. You can hear echo.");
 		locationDao.create(location1);
 		
 		Location location2 = new Location();
 		location2.setName("Small room");
 		location2.setDescription("There is so little space in here you can barely breathe!");
 		locationDao.create(location2);
 		
 		Portal portal = new Portal();
 		portal.setName("Some portal name");
 		portal.setEntry(location1);
 		portal.setDestination(location2);
 		portalDao.create(portal);
 		
 		assertTrue(portal.getId() != 0 && portal.getId() != null);
 	}
 }
