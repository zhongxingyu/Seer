 package pt.com.broker.client.sample;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.cli.CliFactory;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.text.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.CliArgs;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetAction.DestinationType;
 
 /**
  * Simple producer sample. Behavior is determined by command line arguments.
  *
  */
 public class Producer
 {
 	private static final Logger log = LoggerFactory.getLogger(Producer.class);
 	private final AtomicInteger counter = new AtomicInteger(0);
 
 	private String host;
 	private int port;
 	private DestinationType dtype;
 	private String dname;
 
 	public static void main(String[] args) throws Throwable
 	{
 		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
 		Producer producer = new Producer();
 
 		producer.host = cargs.getHost();
 		producer.port = cargs.getPort();
 		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
 		producer.dname = cargs.getDestination();
 
 		BrokerClient bk = new BrokerClient(producer.host, producer.port, "tcp://mycompany.com/mypublisher");
 
		log.info("Start sending a string of " + cargs.getMessageLength() + " random alphanumeric characters in 2 seconds...");
 
 		Thread.sleep(2000);
 
 		producer.sendLoop(bk, cargs.getMessageLength());
 	}
 
 	private void sendLoop(BrokerClient bk, int messageLength) throws Throwable
 	{
 		for (int i = 0; i < 100000; i++)
 		{
 			final String msg = RandomStringUtils.randomAlphanumeric(messageLength);
 
 			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg.getBytes("UTF-8"));
 
 			if (dtype == DestinationType.QUEUE)
 			{
 				bk.enqueueMessage(brokerMessage, dname);
 			}
 			else
 			{
 				bk.publishMessage(brokerMessage, dname);
 			}
 
 			log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));
 
			Sleep.time(1000);
 		}
 	}
 }
