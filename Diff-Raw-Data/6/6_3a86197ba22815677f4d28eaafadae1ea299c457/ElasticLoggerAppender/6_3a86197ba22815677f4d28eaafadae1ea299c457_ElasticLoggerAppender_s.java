 package org.hibnet.elasticlogger;
 
 import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
 
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.elasticsearch.client.Client;
 import org.elasticsearch.node.Node;
 
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.classic.spi.ThrowableProxyUtil;
 import ch.qos.logback.core.UnsynchronizedAppenderBase;
 
 public class ElasticLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
 
     private String clusterName = "elasticsearch";
 
     private String indexName = "elasticlogger";
 
     private String indexType = "log";
 
     private int queueSize = 1000;
 
     private Node node;
 
     private volatile Client client;
 
     private Queue<String> queue;
 
     public void setClusterName(String clusterName) {
         this.clusterName = clusterName;
     }
 
     public void setIndexName(String indexName) {
         this.indexName = indexName;
     }
 
     public void setIndexType(String indexType) {
         this.indexType = indexType;
     }
 
     public void setQueueSize(int queueSize) {
         this.queueSize = queueSize;
     }
 
     @Override
     public void start() {
         queue = new LinkedBlockingQueue<String>(queueSize);
 
         // do make the make the logger (and probably the application too) wait for elasticsearch to boot
         new Thread(new Runnable() {
             @Override
             public void run() {
                 node = nodeBuilder().client(true).clusterName(clusterName).node();
                 client = node.client();
                 // now that the client has started, empty the buffered events
                 while (!queue.isEmpty()) {
                     doIndex(queue.poll());
                 }
                 // Note : there may be some very edge case where the queue might be still not empty here, but it seems
                 // very unlikely, and we accept here to lost these events to avoid too much synchronization between the
                 // threads
             }
         }, "ElasticloggerAppender node client starter").start();
 
         super.start();
     }
 
     @Override
     public void stop() {
         if (node != null) {
             node.close();
         }
         super.stop();
     }
 
     @Override
     protected void append(ILoggingEvent event) {
         JsonBuilder jsonBuilder = new JsonBuilder();
         jsonBuilder.add("level", event.getLevel());
         jsonBuilder.add("message", event.getMessage());
         jsonBuilder.add("marker", event.getMarker());
         jsonBuilder.add("loggerName", event.getLoggerName());
         if (event.getMdc() != null) {
             for (Entry<String, String> mdcEntry : event.getMdc().entrySet()) {
                 jsonBuilder.add("mdc_" + mdcEntry.getKey(), mdcEntry.getValue());
             }
         }
         jsonBuilder.add("threadName", event.getThreadName());
         jsonBuilder.add("timeStamp", Long.toString(event.getTimeStamp()));
         jsonBuilder.add("stackTrace", ThrowableProxyUtil.asString(event.getThrowableProxy()));
 
         String json = jsonBuilder.getResult();
         if (client == null) { // the client maybe not be ready yet
            queue.add(json);
         } else {
             doIndex(json);
         }
     }
 
     private void doIndex(String json) {
         client.prepareIndex(indexName, indexType).setSource(json).execute();
         // not that we don't wait for the response, but this is nice, we don't want to have the main thread wait for
         // some remote logging indexation
     }
 
     private static class JsonBuilder {
 
         private StringBuilder builder = new StringBuilder("{");
 
         private boolean first = true;
 
         private void add(String name, Object value) {
             if (value == null) {
                 return;
             }
             add(name, value.toString());
         }
 
         private void add(String name, String value) {
             if (value == null || value.length() == 0) {
                 return;
             }
             if (first) {
                 builder.append('\"');
             } else {
                 builder.append(",\"");
             }
             first = false;
             builder.append(name);
             builder.append("\":\"");
             builder.append(value.replaceAll("\"", "\\\""));
             builder.append('\"');
         }
 
         private String getResult() {
             builder.append('}');
             return builder.toString();
         }
 
     }
 }
