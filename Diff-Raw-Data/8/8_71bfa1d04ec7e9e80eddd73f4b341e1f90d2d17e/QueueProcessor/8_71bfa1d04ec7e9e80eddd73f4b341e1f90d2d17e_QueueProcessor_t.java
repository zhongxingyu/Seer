 package pt.com.gcs.messaging;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * QueueProcessor provides several queue related features, representing each instance a distinct queue.
  * 
  */
 
 public class QueueProcessor
 {
 
 	private static final ForwardResult failed = new ForwardResult(ForwardResult.Result.FAILED, -1);
 
 	private static Logger log = LoggerFactory.getLogger(QueueProcessor.class);
 
 	private final String _destinationName;
 
 	private final AtomicLong _sequence;
 
 	private final AtomicBoolean isWorking = new AtomicBoolean(false);
 
 	private final AtomicLong _deliverSequence = new AtomicLong(0L);
 
 	protected final AtomicBoolean emptyQueueInfoDisplay = new AtomicBoolean(false);
 
 	protected final AtomicLong _counter = new AtomicLong(0);
 
 	private final BDBStorage storage;
 
 	protected QueueProcessor(String destinationName)
 	{
 		if (StringUtils.isBlank(destinationName))
 		{
 			throw new IllegalArgumentException(String.format("'%s' is not a valid Queue name", destinationName));
 		}
 
 		_destinationName = destinationName;
 
 		storage = new BDBStorage(this);
 
 		long cnt = storage.count();
 
 		if (cnt == 0)
 		{
 			_sequence = new AtomicLong(0L);
 			_counter.set(0);
 		}
 		else
 		{
 			_sequence = new AtomicLong(storage.getLastSequenceValue());
 			_counter.set(cnt);
 		}
 
 		if (StringUtils.contains(destinationName, "@"))
 		{
 			DispatcherList.create(destinationName);
 		}
 
 		log.info("Create Queue Processor for '{}'.", _destinationName);
 		log.info("Queue '{}' has {} message(s).", destinationName, getQueuedMessagesCount());
 	}
 
 	public synchronized void clearStorage()
 	{
 		storage.deleteQueue();
 	}
 
 	public long decrementQueuedMessagesCount()
 	{
 		return _counter.decrementAndGet();
 	}
 
 	public long getQueuedMessagesCount()
 	{
 		return _counter.get();
 	}
 
 	protected void ack(final String msgId)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Ack message . MsgId: '{}'.", msgId);
 		}
 
 		if (storage.deleteMessage(msgId))
 		{
 			_counter.decrementAndGet();
 		}
 	}
 
 	protected ForwardResult forward(InternalMessage message, boolean preferLocalConsumer) throws IllegalStateException
 	{
 
 		message.setType((MessageType.COM_QUEUE));
 		int lqsize = LocalQueueConsumers.readyQueueSize(_destinationName);
 		int rqsize = RemoteQueueConsumers.size(_destinationName);
 		int size = lqsize + rqsize;
 
 		ForwardResult result = failed;
 
 		if (size == 0)
 		{
 			return failed;
 		}
 		else
 		{
 			if ((lqsize != 0) && preferLocalConsumer)
 			{
 				result = LocalQueueConsumers.notify(message);
 			}
 			else if ((lqsize == 0) && (rqsize != 0))
 			{
 				result = RemoteQueueConsumers.notify(message);
 			}
 			else if ((rqsize == 0) && (lqsize != 0))
 			{
 				result = LocalQueueConsumers.notify(message);
 			}
 			else if ((lqsize > 0) && (rqsize > 0))
 			{
 				long n = _deliverSequence.incrementAndGet() % 2;
 				if (n == 0)
 					result = LocalQueueConsumers.notify(message);
 				else
 					result = RemoteQueueConsumers.notify(message);
 			}
 		}
 
 		if (log.isDebugEnabled())
 		{
			log.debug("forward-> isDelivered: " + result.result + ", lqsize: " + lqsize + ", rqsize: " + rqsize + ", message.id: " + message.getMessageId());
 		}
 
 		return result;
 	}
 
 	protected String getDestinationName()
 	{
 		return _destinationName;
 	}
 
 	protected boolean hasRecipient()
 	{
 		if (LocalQueueConsumers.hasReadyRecipients(_destinationName))
 		{
 			return true;
 		}
 		if(log.isDebugEnabled())
 		{
			log.debug(String.format("Queue '%s' has NO READY consumers.", this._destinationName));
 		}
 		
 		if (LocalQueueConsumers.hasActiveRecipients(_destinationName))
 		{
 			// it has sync consumers that are active but not ready. So, ignore remote consumers
 			if(log.isDebugEnabled())
 			{
				log.debug(String.format("Queue '%s' has ACTIVE consumers.", this._destinationName));
 			}
 			return false;
 		}
 
 		return RemoteQueueConsumers.hasReadyRecipients(_destinationName);
 	}
 
 	public int size()
 	{
 		return RemoteQueueConsumers.size(_destinationName) + LocalQueueConsumers.readyQueueSize(_destinationName);
 	}
 
 	public void store(final InternalMessage msg)
 	{
 		store(msg, false);
 	}
 
 	public void store(final InternalMessage msg, boolean preferLocalConsumer)
 	{
 		try
 		{
 			long seq_nr = _sequence.incrementAndGet();
 			storage.insert(msg, seq_nr, preferLocalConsumer);
 			_counter.incrementAndGet();
 		}
 		catch (Throwable t)
 		{
 			throw new RuntimeException(t);
 		}
 	}
 
 	protected final void wakeup()
 	{
 		if (isWorking.getAndSet(true))
 		{
 			log.debug("Queue '{}' is running, skip wakeup", _destinationName);
 			return;
 		}
 		long cnt = getQueuedMessagesCount();
 		if (cnt > 0)
 		{
 			emptyQueueInfoDisplay.set(false);
 
 			if (hasRecipient())
 			{
 				try
 				{
 					if (log.isDebugEnabled())
 					{
 						log.debug("Wakeup queue '{}'", _destinationName);
 					}
 					storage.recoverMessages();
 				}
 				catch (Throwable t)
 				{
 					log.error(t.getMessage(), t);
 					throw new RuntimeException(t);
 				}
 				finally
 				{
 					isWorking.set(false);
 				}
 			}
 		}
 		isWorking.set(false);
 	}
 
 	public void deleteExpiredMessages()
 	{
 		if (!hasRecipient())
 		{
 			storage.deleteExpiredMessages();
 		}
 
 	}
 
 	public void setSequenceNumber(long seqNumber)
 	{
 		_sequence.set(seqNumber);
 	}
 
 	public void setCounter(long counter)
 	{
 		_counter.set(counter);
 	}
 
 	public long getCounter()
 	{
 		return _counter.get();
 	}
 
 	public BDBStorage getStorage()
 	{
 		return storage;
 	}
 
 }
