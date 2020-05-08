 package pt.com.gcs.messaging;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.caudexorigo.text.StringUtils;
 import org.jboss.netty.channel.Channel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.messaging.ForwardResult.Result;
 
 /**
  * LocalQueueConsumers maintains current local queue consumers.
  * 
  */
 
 public class LocalQueueConsumers
 {
 	private static Logger log = LoggerFactory.getLogger(LocalQueueConsumers.class);
 
 	private static final LocalQueueConsumers instance = new LocalQueueConsumers();
 
 	public static final AtomicLong ackedMessages = new AtomicLong(0L);
 
 	private static final Set<String> queuesWithInactiveConsumers = new HashSet<String>();
 
 	protected static void acknowledgeMessage(InternalMessage msg, Channel channel)
 	{
 		log.debug("Acknowledge message with Id: '{}'.", msg.getMessageId());
 
 		try
 		{
 			NetBrokerMessage brkMsg = new NetBrokerMessage("ACK".getBytes("UTF-8"));
 
 			InternalMessage m = new InternalMessage(msg.getMessageId(), msg.getDestination(), brkMsg);
 			m.setType(MessageType.ACK);
 
 			channel.write(m);
 		}
 		catch (Throwable ct)
 		{
 			log.error(ct.getMessage(), ct);
 
 			try
 			{
 				channel.close();
 			}
 			catch (Throwable ict)
 			{
 				log.error(ict.getMessage(), ict);
 			}
 		}
 	}
 
 	public synchronized static void add(String queueName, MessageListener listener)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 		if (listeners == null)
 		{
 			listeners = new CopyOnWriteArrayList<MessageListener>();
 		}
 		listeners.add(listener);
 		instance.localQueueConsumers.put(queueName, listeners);
 		if (listeners.size() == 1)
 		{
 			broadCastNewQueueConsumer(queueName);
 			queuesWithInactiveConsumers.remove(queueName);
 		}
 		else
 		{
 			if (queuesWithInactiveConsumers.contains(queueName))
 			{
 				queuesWithInactiveConsumers.remove(queueName);
 				broadCastNewQueueConsumer(queueName);
 			}
 		}
 	}
 
 	public synchronized static void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			String queueName = listener.getDestinationName();
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 			if (listeners != null)
 			{
 				listeners.remove(listener);
 
 				log.info("Removed. Listeners: {}", listeners.size());
 
 				if (listeners.size() == 0)
 				{
 					instance.localQueueConsumers.remove(queueName);
 					queuesWithInactiveConsumers.remove(queueName);
 					broadCastRemovedQueueConsumer(queueName);
 				}
 			}
 		}
 	}
 
 	protected static void broadCastQueueInfo(String destinationName, String action, Channel channel)
 	{
 		if (StringUtils.isBlank(destinationName))
 		{
 			return;
 		}
 
 		if (action.equals("CREATE"))
 		{
 			log.info("Tell {} about new queue consumer for: {}.", channel.getRemoteAddress().toString(), destinationName);
 		}
 		else if (action.equals("DELETE"))
 		{
 			log.info("Tell {} about deleted queue consumer of: {}.", channel.getRemoteAddress().toString(), destinationName);
 		}
 
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), channel.getLocalAddress().toString(), destinationName);
 
 		InternalMessage m = new InternalMessage();
 		NetBrokerMessage brkMsg;
 		try
 		{
 			brkMsg = new NetBrokerMessage(payload.getBytes("UTF-8"));
 			m.setType(MessageType.SYSTEM_QUEUE);
 
 			m.setDestination(destinationName);
 			m.setContent(brkMsg);
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			// This exception is never thrown because UTF-8 encoding is built-in
 			// in every JVM
 		}
 		SystemMessagesPublisher.sendMessage(m, channel);
 	}
 
 	protected synchronized static void delete(String queueName)
 	{
 		instance.localQueueConsumers.remove(queueName);
 		queuesWithInactiveConsumers.remove(queueName);
 	}
 
 	protected static Set<String> getBroadcastableQueues()
 	{
 		return Collections.unmodifiableSet(instance.localQueueConsumers.keySet());
 	}
 
 	protected static ForwardResult notify(InternalMessage message)
 	{
 		return instance.doNotify(message);
 	}
 
 	protected synchronized static void removeAllListeners()
 	{
 		Set<String> queueNameSet = instance.localQueueConsumers.keySet();
 
 		for (String queueName : queueNameSet)
 		{
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 			listeners.clear();
 			instance.localQueueConsumers.remove(queueName);
 			broadCastRemovedQueueConsumer(queueName);
 		}
 		queuesWithInactiveConsumers.clear();
 	}
 
 	public static int size(String destinationName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(destinationName);
 		if (listeners != null)
 		{
 			return listeners.size();
 		}
 		return 0;
 	}
 
 	public static int readyQueueSize(String destinationName)
 	{
 		int size = 0;
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(destinationName);
 		if (listeners != null)
 		{
 			for (MessageListener ml : listeners)
 				if (ml.ready())
 					++size;
 		}
 		return size;
 	}
 
 	public static boolean hasReadyRecipients(String queueName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 		if (listeners != null)
 		{
 			for (MessageListener ml : listeners)
 				if (ml.ready())
 					return true;
 		}
 		return false;
 	}
 
 	public static synchronized boolean hasActiveRecipients(String queueName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 		if (listeners != null)
 		{
 			for (MessageListener ml : listeners)
 				if (ml.isActive())
 					return true;
 		}
 		if (!queuesWithInactiveConsumers.contains(queueName))
 		{
 			queuesWithInactiveConsumers.add(queueName);
			broadCastRemovedQueueConsumer(queueName);
 		}
 		return false;
 	}
 
 	private Map<String, CopyOnWriteArrayList<MessageListener>> localQueueConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<MessageListener>>();
 
 	private int currentQEP = 0;
 
 	private Object rr_mutex = new Object();
 
 	private LocalQueueConsumers()
 	{
 		Runnable inactivityChecker = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				synchronized (LocalQueueConsumers.class)
 				{
 					for (String queueName : instance.localQueueConsumers.keySet())
 					{
 						if (queuesWithInactiveConsumers.contains(queueName))
 						{
 							continue;
 						}
 						boolean hasActive = false;
 						for (MessageListener messageListener : instance.localQueueConsumers.get(queueName))
 						{
 							if (messageListener.isActive())
 							{
 								hasActive = true;
 								break;
 							}
 						}
 						// there are no active consumers - broadcast queue delete
 						if (!hasActive)
 						{
 							queuesWithInactiveConsumers.add(queueName);
 							broadCastRemovedQueueConsumer(queueName);
 						}
 					}
 				}
 			}
 		};
 
 		Runnable endOfInactivityChecker = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				synchronized (LocalQueueConsumers.class)
 				{
 					ArrayList<String> queuesWithActiveConsumers = new ArrayList<String>();
 					for (String queueName : queuesWithInactiveConsumers)
 					{
 						for (MessageListener messageListener : instance.localQueueConsumers.get(queueName))
 						{
 							if (messageListener.isActive())
 							{
 								queuesWithActiveConsumers.add(queueName);
 								break;
 							}
 						}
 					}
 					for (String queueName : queuesWithActiveConsumers)
 					{
 						queuesWithInactiveConsumers.remove(queueName);
 						broadCastNewQueueConsumer(queueName);
 					}
 				}
 			}
 		};
 
 		GcsExecutor.scheduleAtFixedRate(inactivityChecker, 5 * 60, 10, TimeUnit.SECONDS);
 		GcsExecutor.scheduleAtFixedRate(endOfInactivityChecker, (6 * 60) + 5, 10, TimeUnit.SECONDS);
 	}
 
 	private static void broadCastActionQueueConsumer(String destinationName, String action)
 	{
 		Set<Channel> sessions = Gcs.getManagedConnectorSessions();
 
 		for (Channel channel : sessions)
 		{
 			try
 			{
 				broadCastQueueInfo(destinationName, action, channel);
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 
 				try
 				{
 					channel.close();
 				}
 				catch (Throwable ct)
 				{
 					log.error(ct.getMessage(), ct);
 				}
 			}
 		}
 	}
 
 	private static void broadCastNewQueueConsumer(String destinationName)
 	{
 		broadCastActionQueueConsumer(destinationName, "CREATE");
 	}
 
 	private static void broadCastRemovedQueueConsumer(String destinationName)
 	{
 		broadCastActionQueueConsumer(destinationName, "DELETE");
 	}
 
 	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
 	
 	protected ForwardResult doNotify(InternalMessage message)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = localQueueConsumers.get(message.getDestination());
 		if (listeners != null)
 		{
 			int n = listeners.size();
 			if (n > 0)
 			{
 				MessageListener listener = pick(listeners);
 				if (listener != null)
 				{
 					return listener.onMessage(message);
 				}
 			}
 		}
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("There are no local listeners for queue: {}", message.getDestination());
 		}
 
 		return failed;
 	}
 
 	private MessageListener pick(CopyOnWriteArrayList<MessageListener> listeners)
 	{
 		synchronized (rr_mutex)
 		{
 			int n = listeners.size();
 			if (n == 0)
 				return null;
 
 			if (currentQEP == (n - 1))
 			{
 				currentQEP = 0;
 			}
 			else
 			{
 				++currentQEP;
 			}
 
 			try
 			{
 				for (int i = 0; i != n; ++i)
 				{
 					MessageListener messageListener = listeners.get(currentQEP);
 					if (messageListener.ready())
 						return messageListener;
 				}
 			}
 			catch (Throwable t)
 			{
 				try
 				{
 					currentQEP = 0;
 					do
 					{
 						MessageListener messageListener = listeners.get(currentQEP);
 						if (messageListener.ready())
 							return messageListener;
 					}
 					while ((++currentQEP) != (n - 1));
 				}
 				catch (Throwable t2)
 				{
 					return null;
 				}
 
 			}
 		}
 		return null;
 	}
 }
