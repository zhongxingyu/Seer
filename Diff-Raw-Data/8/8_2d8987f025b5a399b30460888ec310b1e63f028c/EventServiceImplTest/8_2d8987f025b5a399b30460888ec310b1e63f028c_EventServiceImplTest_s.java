 package lt.vilniusjug.talks.jdbctemplate.services.event;
 
 import lt.vilniusjug.talks.jdbctemplate.services.AbstractServiceTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * few tests to demonstrate idea of SQL mapping correctness validation
  *
  * @author Vaidas Pilkauskas
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/test-context.xml"})
 public class EventServiceImplTest extends AbstractServiceTest {
   @Autowired
   private EventService service;
 
   public EventServiceImplTest() throws Exception {
     super("event_dataset.xml");
   }
 
   @Test
   public void testFindAllMapping(){
     List<Event> events = service.findAll();
     Event event = events.get(0);
    assertEventFiledsLoaded(event);
   }
 
   @Test
   public void testFindByName(){
     List<Event> events = service.findAllByName("test");
     assertEquals(3, events.size());
     Event event = events.get(0);
    assertEventFiledsLoaded(event);
   }
 
  private void assertEventFiledsLoaded(Event event) {
     assertNotNull(event.getId());
     assertNotNull(event.getName());
     assertNotNull(event.getVenue());
     assertNotNull(event.getEventDate());
   }
 }
