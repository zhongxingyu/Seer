 package com.tedwon.flume.sink;
 
 import kafka.javaapi.producer.Producer;
 import kafka.javaapi.producer.ProducerData;
 import kafka.producer.ProducerConfig;
 import kafka.message.Message;
 import org.apache.flume.Channel;
 import org.apache.flume.Context;
 import org.apache.flume.Event;
 import org.apache.flume.EventDeliveryException;
 import org.apache.flume.Transaction;
 import org.apache.flume.conf.Configurable;
 import org.apache.flume.sink.AbstractSink;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Properties;
 import java.lang.Runnable;
 import java.lang.Thread;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * Kafka Flume Sink Class.
  * <p/>
  *
  * @author <a href="iamtedwon@gmail.com">Ted Won</a>
  * @version 1.0
  */
 public class KafkaSink extends AbstractSink implements Configurable, Runnable {
 
     private static final Logger logger = LoggerFactory.getLogger(KafkaSink.class);
 
     private String zkConnect;
     private String topic;
     private String serializerClass;
 
     private Properties props;
 
     private Producer<String, Message> producer;
     final private Thread workThread = new Thread(this);
     final private BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<Message>(10240);
 
     @Override
     public void configure(Context context) {
         this.zkConnect = context.getString("zkConnect", "localhost:2181");
         this.topic = context.getString("topic", "test");
         this.serializerClass = context.getString("serializerClass", "kafka.serializer.DefaultEncoder");
     }
 
     @Override
     public synchronized void start() {
         super.start();
 
         props = new Properties();
         props.put("zk.connect", this.zkConnect);
         props.put("serializer.class", this.serializerClass);
 
         ProducerConfig config = new ProducerConfig(props);
         producer = new Producer<String, Message>(config);
 
         workThread.start();
     }
 
     @Override
     public synchronized void stop() {
         workThread.stop();
         super.stop();
         producer.close();
     }
 
     @Override
     public Status process() throws EventDeliveryException {
 
         Status status = Status.READY;
 
         Channel channel = getChannel();
         Transaction tx = null;
 
         try {
             tx = channel.getTransaction();
             tx.begin();
 
             Event event = channel.take();
 
             if (event != null) {
 //                String line = EventHelper.dumpEvent(event);
 //                logger.debug(line);
 
                 byte[] body = event.getBody();
                Message msg;
 
                while (msgQueue.remainingCapacity() == 0) {
                    logger.error("message queue full, drop old message");
                    msg = msgQueue.take();
                }

                msg = new Message(body);
                 msgQueue.put(msg);
             } else {
                 status = Status.BACKOFF;
             }
 
             tx.commit();
         } catch (Exception e) {
             logger.error("can't process events, drop it!", e);
             if (tx != null) {
                 tx.commit();// commit to drop bad event, otherwise it will enter dead loop.
             }
 
             //throw new EventDeliveryException(e);
         } finally {
             if (tx != null) {
                 tx.close();
             }
         }
 
         return status;
     }
 
     public void run() {
         while (true) {
             try {
                 Message msg = msgQueue.take();
 
                 ProducerData<String, Message> kafkaData = new ProducerData<String, Message>(this.topic, msg);
 
                 // Send a single messae
                 // The message is sent to a randomly selected partition registered in ZK
                 producer.send(kafkaData);
             } catch (Exception e) {
                 logger.error("can't send message, drop it!", e);
             }
         }
     }
 }
