 package dk.statsbiblioteket.newspaper.md5checker;
 
 import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import dk.statsbiblioteket.util.Bytes;
 import dk.statsbiblioteket.util.Checksums;
 import dk.statsbiblioteket.util.Strings;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Properties;
 
 /**
  * Check MD5 of all nodes
  */
 public class MD5CheckerComponent
         extends AbstractRunnableComponent {
 
     private static final String CHECKSUM = "checksum";
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public MD5CheckerComponent(Properties properties) {
         super(properties);
     }
 
     @Override
     public String getEventID() {
         return "Checksums_checked";
     }
 
     @Override
     /**
      * For each attribute in batch: Calculate checksum and compare with metadata.
      *
      * @param batch The batch to check
      * @param resultCollector Collector to get the result.
      */
     public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
         TreeIterator iterator = createIterator(batch);
         while (iterator.hasNext()) {
             ParsingEvent next = iterator.next();
             switch (next.getType()) {
                 case NodeBegin: {
                     break;
                 }
                 case NodeEnd: {
                     break;
                 }
                 case Attribute: {
                     AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                     String checksum;
                     try {
                         checksum = attributeEvent.getChecksum();
                     } catch (IOException e) {
                         log.warn("Error getting checksum in {}", attributeEvent.getName(), e);
                         resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                                                    "2F-O1: Error getting checksum: " + e.toString(),
                                                    Strings.getStackTrace(e));
                         break;
                     }
                     String calculatedChecksum;
                     try {
                         calculatedChecksum = calculateChecksum(attributeEvent.getData());
                     } catch (IOException e) {
                         log.warn("Error calculating checksum on data in {}", attributeEvent.getName(), e);
                         resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                                                    "2F-O1: Error calculating checksum on data: " + e.toString(),
                                                    Strings.getStackTrace(e));
                         break;
                     }
                     if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                         log.debug("Expected checksum {}, but was {} in {}", checksum, calculatedChecksum,
                                   attributeEvent.getName());
                         resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                                                   "2F-O1: Expected checksum " + checksum + ", but was " + calculatedChecksum);
                     }
                     break;
                 }
             }
 
         }
         resultCollector.setTimestamp(new Date());
     }
 
     private String calculateChecksum(InputStream stream) throws IOException {
         return Bytes.toHex(Checksums.md5(stream));
     }
 
 }
