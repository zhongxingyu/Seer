 package pl.edu.agh.megamud.tests.dao;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 
 import org.junit.*;
 
 import pl.edu.agh.megamud.dao.*;
 
 public class LocationTest extends TestBase{
 
 	@Before
 	public void setUp() throws Exception {
 		super.prepareDatabase();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		connectionSource.close();
 	}	
 	
 	@Test(expected = SQLException.class)
 	public void should_not_create_location_without_name() throws SQLException{
 		Location location = new Location();
 		location.setDescription("some location description");
 		locationDao.create(location);
 	}
 	
 	@Test(expected = SQLException.class)
 	public void should_not_create_location_without_description() throws SQLException{
 		Location location = new Location();
 		location.setName("some location name");
 		locationDao.create(location);
 	}
 	
 	@Test(expected = SQLException.class)
 	public void should_not_create_location_without_module() throws SQLException{
 		Location location = new Location();
 		location.setModule("default");
 		locationDao.create(location);
 	}
 	
 	@Test
 	public void should_create_location() throws SQLException{
 		Location location = new Location();
 		location.setDescription("some location description");
 		location.setName("some location name");
		location.setModule("some module");
 		locationDao.create(location);
 		
 		assertTrue(location.getId() != 0);
 		assertTrue(location.getId() != null);
 	}
 	
 	@Test
 	public void should_get_exits() throws SQLException{
 		Location location1 = new Location();
 		location1.setName("Big room");
 		location1.setDescription("Some very large room");
 		location1.setModule("default");
 		locationDao.create(location1);
 
 		Location location2 = new Location();
 		location2.setName("Medium room");
 		location2.setDescription("The room seems rather boring. Nothing special about it.");
 		location2.setModule("default");
 		locationDao.create(location2);
 		
 		Location location3 = new Location();
 		location3.setName("Small room");
 		location3.setDescription("This room makes you feel claustrophobic!");
 		location3.setModule("default");
 		locationDao.create(location3);
 		
 		Portal p1 = new Portal();
 		p1.setEntry(location1);
 		p1.setDestination(location2);
 		p1.setName("Door");
 		portalDao.create(p1);
 		
 		Portal p2 = new Portal();
 		p2.setEntry(location1);
 		p2.setDestination(location3);
 		p2.setName("stairs");
 		portalDao.create(p2);
 		
 		locationDao.refresh(location1);
 		assertEquals(2, location1.getExits().size());		
 	}
 	
 	@Test
 	public void should_add_two_way_connection() throws SQLException{
 		Location location1 = new Location();
 		location1.setName("Big room");
 		location1.setDescription("Some very large room");
 		location1.setModule("default");
 		locationDao.create(location1);
 
 		Location location2 = new Location();
 		location2.setName("Medium room");
 		location2.setDescription("The room seems rather boring. Nothing special about it.");
 		location2.setModule("default");
 		locationDao.create(location2);
 		
 		location1.connectTo(location2, "Door");
 		
 		location1.refresh();
 		location2.refresh();
 		
 		assertEquals(1, location1.getExits().size());
 		assertEquals(1, location2.getExits().size());
 		
 		Portal fromLocation1 = location1.getExits().iterator().next();
 		Portal fromLocation2 = location2.getExits().iterator().next();
 		
 		assertTrue(fromLocation1.getId() != fromLocation2.getId());
 
 		assertEquals("Door", fromLocation1.getName());
 		assertEquals("Door", fromLocation2.getName());
 	}
 	
 	@Test
 	public void should_find_available_exit_by_name() throws SQLException{
 		Location location1 = new Location();
 		location1.setName("Big room");
 		location1.setDescription("Some very large room");
 		location1.setModule("default");
 		locationDao.create(location1);
 
 		Location location2 = new Location();
 		location2.setName("Medium room");
 		location2.setDescription("The room seems rather boring. Nothing special about it.");
 		location2.setModule("default");
 		locationDao.create(location2);
 		
 		location1.connectTo(location2, "door");
 		location1.refresh();
 		
 		Portal p2 = location1.getExitByName("door");
 		
 		assertNotNull(p2);
 	}
 	
 	@Test
 	public void should_get_location_by_exit_name() throws SQLException{
 		Location location1 = new Location();
 		location1.setName("Big room");
 		location1.setDescription("Some very large room");
 		location1.setModule("default");
 		locationDao.create(location1);
 
 		Location location2 = new Location();
 		location2.setName("Medium room");
 		location2.setDescription("The room seems rather boring. Nothing special about it.");
 		location2.setModule("default");
 		locationDao.create(location2);
 		
 		location1.connectTo(location2, "door");
 		location1.refresh();
 		location2.refresh();
 		
 		Location fetchedLocation = location1.getDestinationByExitName("door");
 		
 		assertNotNull(fetchedLocation);
 		assertTrue(fetchedLocation.getId() == location2.getId());
 	}
 }
