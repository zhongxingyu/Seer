 package uk.co.acuminous.julez.event.handler;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
import test.JdbcTestUtils;
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.event.source.JdbcEventRepository;
 import uk.co.acuminous.julez.mapper.TransformingMapper;
 import uk.co.acuminous.julez.mapper.TwoWayMapper;
 import uk.co.acuminous.julez.marshalling.NamespaceBasedEventClassResolver;
 import uk.co.acuminous.julez.runner.ScenarioRunnerEvent;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.test.TestEventSchema;
 import uk.co.acuminous.julez.transformer.DefaultColumnNameTransformer;
 
 public class JdbcEventHandlerTest {
 
     private JdbcEventRepository jdbcEventSource;
     private TwoWayMapper columnMapper;
     private DataSource dataSource;
 
     @Before
     public void init() {
         TestEventSchema.ddl();
         
         dataSource = JdbcTestUtils.getDataSource();
 
         String[] persistentProperties = { Event.ID, Event.TIMESTAMP, Event.TYPE };
         columnMapper = new TransformingMapper(new DefaultColumnNameTransformer(), persistentProperties);
 
         jdbcEventSource = new JdbcEventRepository(dataSource, columnMapper, new NamespaceBasedEventClassResolver());
     }
 
     @After
     public void nuke() throws Exception {
         JdbcTestUtils.nukeDatabase();
     }
 
     @Test
     public void addsScenarioEventsToRepository() {
 
         ScenarioEvent event = new ScenarioEvent("id", System.currentTimeMillis(), ScenarioEvent.FAIL);
 
         new JdbcEventHandler(dataSource, columnMapper).onEvent(event);
 
         List<Event> events = jdbcEventSource.list();
         assertEquals(1, events.size());
         assertEquals(event, events.get(0));
     }
 
     @Test
     public void addsScenarioRunnerEventsToRepository() {
 
         ScenarioRunnerEvent event = new ScenarioRunnerEvent("id", System.currentTimeMillis(), ScenarioRunnerEvent.END);
 
         new JdbcEventHandler(dataSource, columnMapper).onEvent(event);
 
         List<Event> events = jdbcEventSource.list();
         assertEquals(1, events.size());
         assertEquals(event, events.get(0));
     }
 
     @Test
     public void ignoresNullRepositoryValues() {
 
         ScenarioRunnerEvent event = new ScenarioRunnerEvent("id", System.currentTimeMillis(), ScenarioRunnerEvent.END);
 
         new JdbcEventHandler(dataSource, columnMapper).onEvent(event);
 
         Event actual = jdbcEventSource.list().get(0);
         assertFalse(actual.getData().containsKey("statusCode"));
     }
 
     @Test
     public void tolleratesUnmappedColumns() {
 
         ScenarioRunnerEvent event = new ScenarioRunnerEvent("id", System.currentTimeMillis(), ScenarioRunnerEvent.END);
         event.put("Foo", "Bar");
 
         new JdbcEventHandler(dataSource, columnMapper).onEvent(event);
 
         Event actual = jdbcEventSource.list().get(0);
         assertFalse(actual.getData().containsKey("Foo"));
     }
 }
