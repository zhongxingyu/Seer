 package se.cygni.expenses.jdbi;
 
 import org.h2.jdbcx.JdbcConnectionPool;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import se.cygni.expenses.api.Event;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class EventsRepositoryTest {
 
     private static DBI dbi;
     private static JdbcConnectionPool ds;
 
     @BeforeClass
     public static void initDB() {
 
        ds = JdbcConnectionPool.create("jdbc:h2:mem:EventsRepositoryTest", "sa", "");
         dbi = new DBI(ds);
         createTables(dbi);
     }
 
     @AfterClass
     public static void closeDB() {
 
         ds.dispose();
     }
 
     @Before
     public void clearTables() {
 
         Handle handle = dbi.open();
         handle.execute("delete from " + EventsRepository.TABLE_NAME);
         handle.close();
     }
 
     @Test
     public void shouldListEvents() {
 
         insertEvent(dbi, new Event(1, "Cancun", new Date(0)));
         insertEvent(dbi, new Event(2, "New Delhi", new Date(1554)));
 
         EventsRepository target = dbi.onDemand(EventsRepository.class);
 
         //when
         List<Event> result = target.findAll();
 
         //then
         assertThat("result contains two elements", result.size(), is(2));
     }
 
     @Test
     public void shouldFindEventById() {
         //given
         Event event = new Event(3, "Kiruna", new Date(0));
         insertEvent(dbi, event);
 
         EventsRepository target = dbi.onDemand(EventsRepository.class);
 
         //when
         Event result = target.findById(3);
 
         //the
         assertThat("Events are the same", result, is(event));
 
     }
 
     private void insertEvent(DBI dbi, Event event) {
 
         Handle handle = dbi.open();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
         handle.execute("insert into event " +
                 "(name, date) " +
                 "values " +
                 "(" +
                 "'" + event.getName() + "'," +
                 "'" + sdf.format(event.getDate()) + "')");
     }
 
     private static void createTables(DBI dbi) {
 
         Handle handle = dbi.open();
         handle.execute(EventsRepository.CREATE_TABLE_STATEMENT);
         handle.close();
     }
 
 }
