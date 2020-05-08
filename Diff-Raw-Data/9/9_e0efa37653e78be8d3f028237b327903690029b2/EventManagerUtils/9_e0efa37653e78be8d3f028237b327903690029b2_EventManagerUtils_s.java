 package azkaban.scheduler;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 
 import org.apache.log4j.Logger;
 import kafka.producer.ProducerConfig;
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import kafka.javaapi.producer.*;
 
 import kafka.javaapi.message.ByteBufferMessageSet;
 import kafka.message.Message;
 
 
 /**
  * Check for events.
  * 
  * @author Sukrit Mohan
  * 
  */
 
 public final class EventManagerUtils
 {
 	private static Logger logger = Logger.getLogger(ScheduleManager.class);
	private static String connPort = "ZOOKEEPER";
 	private static String serializer = "kafka.serializer.StringEncoder";
 	private static String timeout = "1000000";
 	
 	private static Properties producerProps;
 	private static ProducerConfig producerConfig;
 	private static Producer<String, String> producer;
 	
 	private EventManagerUtils()
 	{
 	}
 	
 	public static String getConnPort() { return connPort; }
 	
 	public static void setConnPort(String port)
 	{
 		EventManagerUtils.connPort = port;
 	}
 	
 	public static void initializeProducer()
 	{
 		producerProps = new Properties();
 		producerProps.put("zk.connect", EventManagerUtils.connPort);
 		producerProps.put("serializer.class", EventManagerUtils.serializer);
 		
 		producerConfig = new ProducerConfig(producerProps);
 		producer = new Producer<String, String>(producerConfig);
 	}
 	
 	public static void publish(String topic, String message)
 	{
 		ProducerData<String, String> data = new ProducerData<String, String>(topic, message);
 		producer.send(data);
 	}
 	
 	public static void closeProducer()
 	{
 		if(producer != null)
 			producer.close();
 	}
 	
 	public static String getSerializer() { return serializer; }
 	
 	public static void setSerializer(String serializer)
 	{
 		EventManagerUtils.serializer = serializer;
 	}
 	
 	public static String getConnTimeout() { return timeout; }
 	
 	public static void setConnTimeout(String timeout)
 	{
 		EventManagerUtils.timeout = timeout;
 	}
 	
 	public static String byteBuffertoString(ByteBuffer bb)
 	{
 		String toString = "";
 		try
 		{
 			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
 			CharBuffer charBuff = decoder.decode(bb);
 			toString = charBuff.toString();
 		}
 		catch (Exception e)
 		{ 
 			System.err.println("Unable to decode from byteBuffer");
 		}
 		
 		return toString;
 	}
 	
 	public static boolean checkTimeConstraints(int nowHour, int startHour, int stopHour)
 	{
 		boolean constraintHolds = false;
 		
 		if(startHour < stopHour)
 		{
 			//case 1 : start < stop (same day)
 			if((nowHour >= startHour) && (nowHour < stopHour)) constraintHolds = true;
 		}
 		else
 		{
 			//case 2 : start > stop (consecutive days)
 			if((nowHour >= startHour) || (nowHour < stopHour)) constraintHolds = true;
 		}
 		return constraintHolds;
 	}
 }
