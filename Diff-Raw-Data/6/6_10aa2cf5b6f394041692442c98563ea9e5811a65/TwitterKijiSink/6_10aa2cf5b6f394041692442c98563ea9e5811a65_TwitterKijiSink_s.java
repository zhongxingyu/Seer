 package org.kiji.flume;
 
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import org.apache.flume.Channel;
 import org.apache.flume.Event;
 import org.apache.flume.EventDeliveryException;
 import org.apache.flume.Transaction;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.kiji.schema.EntityId;
 
 /**
  * Created with IntelliJ IDEA. User: aaron Date: 6/26/13 Time: 5:09 PM To change this template use
  * File | Settings | File Templates.
  */
 public class TwitterKijiSink extends AbstractKijiSink {
   private static final Logger LOG = LoggerFactory.getLogger(TwitterKijiSink.class);
 
   @Override
   public Status process() throws EventDeliveryException {
     final Channel channel = getChannel();
     final Transaction transaction = channel.getTransaction();
 
     transaction.begin();
     try {
       final Event event = channel.take();
       final String message = new String(event.getBody(), "UTF-8");
 
       LOG.info("Received message: {}", message);
 
       final ObjectMapper oMapper = new ObjectMapper();
       final Map<String, Object> map =
           oMapper.readValue(message, new TypeReference<Map<String, Object>>(){});
 
       if (isTweet(map)) {
         final JsonNode node = oMapper.readTree(message);
         final JsonNode user = node.get("user");
         final EntityId eid = getTable().getEntityId(user.get("name").getValueAsText());
         final long timestamp  = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(
             node.get("created_at").getValueAsText()).getTime();
 
         getWriter().begin(eid);
         getWriter().put("info", "tweet", timestamp, node.get("text").getValueAsText());

       }
      getWriter().commit();
       transaction.commit();
       return Status.READY;
     } catch (Throwable t) {
       transaction.rollback();
      getWriter().rollback();
       if (t instanceof Error) {
         throw (Error) t;
       }
       return Status.BACKOFF;
     } finally {
       transaction.close();
     }
   }
 
   private boolean isTweet(final Map<String, Object> map) {
     return map.containsKey("text") && map.containsKey("user");
   }
 }
