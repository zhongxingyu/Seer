 package org.monitoring.queryapisql;
 
 import com.google.caliper.Runner;
 import com.google.caliper.SimpleBenchmark;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 import org.monitoring.queryapi.Event;
 import org.monitoring.queryapi.preaggregation.Preaggregate;
 import org.monitoring.queryapisql.preaggregation.PostgreSQLDatabase;
 
 /**
  *
  * @author Michal Dubravcik
  */
 public class PreaggregateSQLCaliper extends SimpleBenchmark {
 
     List<Event> list = new ArrayList<Event>();
     int from, to;
     TimeUnit unit = TimeUnit.MINUTES;
     
    int[][] times = {{60, 1440},{1440, 43200,3,3},{1440,43200},{43200,525600}};
     
     Preaggregate preaggregateSQL = new PreaggregateSQL();
 
     @Override
     protected void setUp() {
         PostgreSQLDatabase postgre = new PostgreSQLDatabase();
         postgre.dropTable();
         String[] fields = {"avg", "count", "sum"};
         postgre.createTable("aggregate60", 60, 1440, fields);
         Calendar cal = new GregorianCalendar(2013, 1, 1, 1, 0, 0);
         cal.set(Calendar.MILLISECOND, 0);
         from = 0;
         to = 200;
         for (int i = from; i < to; i++) {
             Event event = new Event();
             event.setDate(cal.getTime());            
             cal.setTime(new Date(cal.getTime().getTime() + 1000 * 60 * 15));
             event.setValue(10);
             list.add(event);
         }
     }
 
     @Override
     protected void tearDown() {
         //m.getDb().dropDatabase();
     }
     
     public void timeSQLAggregate(int reps) {
         for (int i = 0; i < reps; i++) {
             preaggregateSQL.saveEvent(unit, times, list.get(i));
         }
     }
 
     public static void main(String[] args) throws Exception {
         Locale.setDefault(Locale.US);
        Runner.main(PreaggregateSQLCaliper.class, args);
     }
 }
