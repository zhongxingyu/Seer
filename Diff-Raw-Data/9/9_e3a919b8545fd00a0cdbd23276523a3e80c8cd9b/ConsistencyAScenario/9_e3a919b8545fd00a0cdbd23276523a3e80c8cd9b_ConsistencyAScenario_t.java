 package net.thumbtack.research.nosql.scenarios;
 
 import net.thumbtack.research.nosql.Configurator;
 import net.thumbtack.research.nosql.clients.Client;
 import net.thumbtack.research.nosql.report.AggregatedReporter;
 import net.thumbtack.research.nosql.report.Reporter;
 import org.javasimon.Split;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 import java.util.*;
 
 /**
  * User: vkornev
  * Date: 13.08.13
  * Time: 10:45
  * <p/>
  * Test database consistency. This is simple consistency test. This is test showing how database work with different
  * consistency levels.
  */
 public final class ConsistencyAScenario extends Scenario {
     private static final Logger log = LoggerFactory.getLogger(ConsistencyAScenario.class);
     private static final Logger detailedLog = LoggerFactory.getLogger("detailed");
     private static final Logger rawLog = LoggerFactory.getLogger("rawLog");
 
     private static final String VALE_COLUMN = "1";
     private static final String DATA_COLUMN = "2";
 
     private String key;
     private long value;
 
     @Override
     public void init(Client client, Configurator config) {
         super.init(client, config);
         key = UUID.randomUUID().toString();
        value = 0L;
     }
 
     @Override
     protected void action() throws Exception {
         Map<String, ByteBuffer> values = new HashMap<>();
         values.put(VALE_COLUMN, ls.toByteBuffer(value));
         values.put(DATA_COLUMN, ss.toByteBuffer(generateString()));
         Set<String> cn = new HashSet<>(1);
         cn.add(VALE_COLUMN);
 
 	    // write
 	    Split writeSplit = Reporter.startEvent();
 	    db.write(key, values);
 	    Reporter.addEvent(Reporter.STOPWATCH_WRITE, writeSplit);
 
 	    // read
 	    Split readSplit = Reporter.startEvent();
 	    values = db.read(key, cn);
 	    Reporter.addEvent(Reporter.STOPWATCH_READ, readSplit);
        ByteBuffer buffer = values.get(VALE_COLUMN);
        long readValue = buffer == null ? 0L : ls.fromByteBuffer(buffer);
         // compare
	    if (value != readValue) {
 	        Reporter.addEvent(Reporter.STOPWATCH_VALUE_FAILURE);
 	        Reporter.addEvent(Reporter.STOPWATCH_FAILURE);
             AggregatedReporter.addEvent(AggregatedReporter.EVENT_OLD_VALUE);
 	        detailedLog.warn("Written and read values for key {} are different", new Object[]{key});
             if(rawLog.isDebugEnabled()) {
 	            rawLog.debug("Key: {}, Written: {}, Read: {} ", new Object [] { key, value, readValue } );
             }
         }
         value++;
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
 }
