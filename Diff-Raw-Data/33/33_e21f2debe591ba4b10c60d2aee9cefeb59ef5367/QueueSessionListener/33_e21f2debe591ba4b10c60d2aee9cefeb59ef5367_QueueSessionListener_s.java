 package pt.com.broker.messaging;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.mina.common.DefaultWriteRequest;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.WriteRequest;
 import org.apache.mina.common.WriteTimeoutException;
 import org.caudexorigo.concurrent.Sleep;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.xml.SoapEnvelope;
 import pt.com.gcs.messaging.DestinationType;
 import pt.com.gcs.messaging.Gcs;
 import pt.com.gcs.messaging.Message;
 import pt.com.gcs.net.IoSessionHelper;
 
 public class QueueSessionListener extends BrokerListener
 {
 	private final static int MAX_SESSION_BUFFER_SIZE = 2 * 1024 * 1024;
 
 	private int currentQEP = 0;
 
 	private static final Logger log = LoggerFactory.getLogger(QueueSessionListener.class);
 
 	private final List<IoSession> _sessions = new ArrayList<IoSession>();
 
 	private final String _dname;
 
 	private final Object mutex = new Object();
 
 	public QueueSessionListener(String destinationName)
 	{
 		_dname = destinationName;
 	}
 
 	@Override
 	public DestinationType getDestinationType()
 	{
 		return DestinationType.QUEUE;
 	}
 
 	public boolean onMessage(final Message msg)
 	{
 		if (msg == null)
 			return true;
 
 		final IoSession ioSession = pick();
 
 		try
 		{
 			if (ioSession != null)
 			{
 				if (ioSession.isConnected() && !ioSession.isClosing())
 				{
 					final SoapEnvelope response = BrokerListener.buildNotification(msg);
 
 					if (ioSession.getScheduledWriteBytes() > MAX_SESSION_BUFFER_SIZE)
 					{
 						int sleepCount = 0;
 						boolean isWriteTimeout = false;
 						while (ioSession.getScheduledWriteBytes() > MAX_SESSION_BUFFER_SIZE && !isWriteTimeout)
 						{
 							Sleep.time(1);
 							sleepCount++;
 							if (sleepCount > 2500)
 							{
 								isWriteTimeout = true;
 							}
 						}
 
 						if (isWriteTimeout)
 						{
 							WriteRequest wreq = new DefaultWriteRequest(response);
 							throw new WriteTimeoutException(wreq);
 						}
 					}
 					ioSession.write(response);
 					return true;
 				}
 			}
 		}
 		catch (Throwable e)
 		{
 			try
 			{
 				(ioSession.getHandler()).exceptionCaught(ioSession, e);
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 			}
 		}
 
 		return false;
 	}
 
 	private IoSession pick()
 	{
 		synchronized (mutex)
 		{
 			int n = _sessions.size();
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
 				return _sessions.get(currentQEP);
 			}
 			catch (Exception e)
 			{
				e.printStackTrace();
 				try
 				{
 					currentQEP = 0;
 					return _sessions.get(currentQEP);
 				}
 				catch (Exception e2)
 				{
					e2.printStackTrace();
 					return null;
 				}
 			}
 		}
 	}
 
 	public void addConsumer(IoSession iosession)
 	{
 		synchronized (mutex)
 		{
 			if (!_sessions.contains(iosession))
 			{
 				_sessions.add(iosession);
 				log.info("Create message consumer for queue: " + _dname + ", address: " + IoSessionHelper.getRemoteAddress(iosession));
 			}
 		}
 	}
 
 	public void removeConsumer(IoSession iosession)
 	{
 		synchronized (mutex)
 		{
 			if (_sessions.remove(iosession))
 				log.info("Remove message consumer for queue: " + _dname + ", address: " + IoSessionHelper.getRemoteAddress(iosession));
 
 			if (_sessions.isEmpty())
 			{
 				QueueSessionListenerList.remove(_dname);
 				Gcs.removeAsyncConsumer(this);
 			}
 		}
 	}
 
 	public String getDestinationName()
 	{
 		return _dname;
 	}
 }
