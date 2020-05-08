 package pt.com.broker.messaging;
 
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.common.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.core.BrokerExecutor;
 import pt.com.broker.xml.SoapEnvelope;
 import pt.com.gcs.messaging.Gcs;
 import pt.com.gcs.messaging.Message;
 
 public class BrokerSyncConsumer
 {
 	private static final Logger log = LoggerFactory.getLogger(BrokerSyncConsumer.class);
 
 	public static void poll(Poll poll, IoSession ios)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Poll message from Queue '{}'", poll.destinationName);
 		}
 
 		try
 		{
 			Message m = Gcs.poll(poll.destinationName);
 			if (m == null)
 			{
 				BrokerExecutor.schedule(new QueuePoller(poll, ios), 1000, TimeUnit.MILLISECONDS);
 				return;
 			}
 
			if (!m.getDestination().equals(poll.destinationName))
			{
				throw new IllegalStateException("Poll.destinationName != Message.getDestination()");
			}

 			if ((ios != null) && ios.isConnected() && !ios.isClosing())
 			{
 				final SoapEnvelope response = BrokerListener.buildNotification(m);
 				ios.write(response);
 			}
 			else
 			{
 				Gcs.removeSyncConsumer(poll.destinationName);
 			}
 		}
 		catch (Throwable e)
 		{
			try
			{
				(ios.getHandler()).exceptionCaught(ios, e);
			}
			catch (Throwable t)
			{
				throw new RuntimeException(t);
			}
 		}
 	}
 }
