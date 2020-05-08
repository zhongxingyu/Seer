 package net.thumbtack.research.nosql.scenarios;
 
 import net.thumbtack.research.nosql.Configurator;
 import net.thumbtack.research.nosql.report.AggregatedReporter;
 import net.thumbtack.research.nosql.report.Reporter;
 import net.thumbtack.research.nosql.clients.Database;
 import org.javasimon.Split;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 import java.util.UUID;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 /**
  * User: vkornev
  * Date: 13.08.13
  * Time: 10:45
  * <p/>
  * Test database consistency. This test checking real database consistency with consistency level ALL.
  * This is test try to do more then one reads and get new values of record before old values of record in same time.
  * <p/>
  * Some usage advices:
  * <ul>
  * <li>count of servers in db.hosts parameter must be more then one
  * <li>set threads count as (x+1) * n, x - is count of servers in db.hosts parameters, and n - is count of writers
  * <li>set read and write consistency to ALL
  * </ul>
  */
 public class ConsistencyBScenario extends Scenario {
     private static final Logger log = LoggerFactory.getLogger(ConsistencyBScenario.class);
     private static final Logger detailedLog = LoggerFactory.getLogger("detailed");
 
     private static int roleIdx = 0;
     private static int rolesCount = 0;
     private static int readersCount = 0;
     private static final char DELIMETER = '\t';
     private static String groupKey;
     private static Map<Long, ByteBuffer> groupReadValues;
     private static Semaphore groupReadSemaphore;
     private static Semaphore groupAggrSemaphore;
 
     private enum Role {
         writer, reader;
     }
 
     private String key;
     private Role role;
     private Map<Long, ByteBuffer> readValues;
     private Semaphore readSemaphore;
     private Semaphore aggrSemaphore;
 
     @Override
     public void init(Database database, Configurator config) {
         super.init(database, config);
         synchronized (ConsistencyBScenario.class) {
             if (rolesCount == 0) {
                 readersCount = config.getDbHosts().length;
                 rolesCount = readersCount + 1;
             }
             this.writesCount = config.getScWrites()/(config.getScThreads()/rolesCount);
             role = getRole();
             if (role.equals(Role.writer)) {
                 groupKey = UUID.randomUUID().toString();
                 groupReadValues = new LinkedHashMap<>();
                 groupReadSemaphore = new Semaphore(readersCount);
                 groupAggrSemaphore = new Semaphore(readersCount);
                 try {
                     groupReadSemaphore.acquire(readersCount);
                     groupAggrSemaphore.acquire(readersCount);
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 }
             }
             key = groupKey;
             readValues = groupReadValues;
             readSemaphore = groupReadSemaphore;
             aggrSemaphore = groupAggrSemaphore;
 
             if (role.equals(Role.writer)) {
             }
 
             log.debug("Create consistency_b scenario with role " + role.name() + " and key " + key);
         }
     }
 
     @Override
     protected void action() throws Exception {
         if (role.equals(Role.writer)) {
             readSemaphore.release(readersCount);
             write();
             aggrSemaphore.acquire(readersCount);
             aggregation();
         } else {
             readSemaphore.acquire(1);
             read();
             aggrSemaphore.release(1);
         }
     }
 
     @Override
     public void close() {
         super.close();
         try {
             db.close();
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e.getMessage());
         }
     }
 
     private Role getRole() {
         if (roleIdx >= rolesCount) {
             roleIdx = 0;
         }
         if (roleIdx++ == 0) {
             return Role.writer;
         }
         return Role.reader;
     }
 
     private void write() throws Exception {
         Split writeSplit = Reporter.startEvent();
         String prefix = System.nanoTime() + "" + DELIMETER;
         String value = generateString(prefix);
         db.write(key, ss.toByteBuffer(value));
         Reporter.addEvent(Reporter.STOPWATCH_WRITE, writeSplit);
     }
 
     private void read() throws Exception {
         synchronized (key) {
            // read
             Split readSplit = Reporter.startEvent();
             ByteBuffer buffer = db.read(key);
             Reporter.addEvent(Reporter.STOPWATCH_READ, readSplit);
             readValues.put(System.nanoTime(), buffer);
         }
     }
 
     private void aggregation() {
 
         long oldTimestamp = 0;
         for (long time : readValues.keySet()) {
             long newTimestamp = getTimestamp(readValues.get(time));
            detailedLog.debug(key + "\t{}\t{}", time, newTimestamp);
             if (oldTimestamp > newTimestamp) {
                 Reporter.addEvent(Reporter.STOPWATCH_VALUE_FAILURE);
                 Reporter.addEvent(Reporter.STOPWATCH_FAILURE);
                 AggregatedReporter.addEvent(AggregatedReporter.EVENT_OLD_VALUE);
             }
             oldTimestamp = newTimestamp;
         }
     }
 
     private long getTimestamp(ByteBuffer buffer) {
         if (buffer != null) {
             byte[] bytes = buffer.array();
             for (int i = buffer.position(); i < bytes.length; i++) {
                 if (bytes[i] == DELIMETER) {
                     return Long.valueOf(new String(Arrays.copyOfRange(bytes, buffer.position(), i)));
                 }
             }
         }
         return 0L;
     }
 }
