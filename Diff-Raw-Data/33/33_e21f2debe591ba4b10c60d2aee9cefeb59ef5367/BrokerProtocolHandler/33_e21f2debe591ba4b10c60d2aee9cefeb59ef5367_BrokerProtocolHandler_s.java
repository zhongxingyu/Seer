 package pt.com.broker.net;
 
 import java.io.IOException;
 
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.WriteTimeoutException;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.core.ErrorHandler;
 import pt.com.broker.messaging.BrokerConsumer;
 import pt.com.broker.messaging.BrokerProducer;
 import pt.com.broker.messaging.BrokerSyncConsumer;
 import pt.com.broker.messaging.MQ;
 import pt.com.broker.messaging.Notify;
 import pt.com.broker.messaging.Poll;
 import pt.com.broker.messaging.QueueSessionListenerList;
 import pt.com.broker.messaging.Status;
 import pt.com.broker.messaging.Unsubscribe;
 import pt.com.broker.xml.SoapEnvelope;
 import pt.com.gcs.net.IoSessionHelper;
 
 public class BrokerProtocolHandler extends IoHandlerAdapter
 {
 	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);
 
 	private static final BrokerProducer _brokerProducer = BrokerProducer.getInstance();
 
 	private static final BrokerConsumer _brokerConsumer = BrokerConsumer.getInstance();
 
 	public BrokerProtocolHandler()
 	{
 	}
 
 	@Override
 	public void sessionCreated(IoSession iosession) throws Exception
 	{
 		IoSessionHelper.tagWithRemoteAddress(iosession);
 		if (log.isDebugEnabled())
 		{
 			log.debug("Session created: " + IoSessionHelper.getRemoteAddress(iosession));
 		}
 	}
 
 	@Override
 	public void sessionClosed(IoSession iosession)
 	{
 		try
 		{
 			String remoteClient = IoSessionHelper.getRemoteAddress(iosession);
 			log.info("Session closed: " + remoteClient);
 			QueueSessionListenerList.removeSession(iosession);
 		}
 		catch (Throwable e)
 		{
 			exceptionCaught(iosession, e);
 		}
 	}
 
 	public void exceptionCaught(IoSession iosession, Throwable cause)
 	{
 		ErrorHandler.WTF wtf = ErrorHandler.buildSoapFault(cause);
 		SoapEnvelope ex_msg = wtf.Message;
 		String client = IoSessionHelper.getRemoteAddress(iosession);
 
 		if (!(wtf.Cause instanceof IOException))
 		{
 			try
 			{
 				iosession.write(ex_msg);
 			}
 			catch (Throwable t)
 			{
 				log.error("The error information could not be delivered to the client", t);
 			}
 		}
 
 		try
 		{
 			iosession.close();
 		}
 		catch (Throwable t)
 		{
 			log.error("Error closing client connection", t);
 		}
 
 		try
 		{
 			String msg = "";
 
 			if (wtf.Cause instanceof WriteTimeoutException)
 			{
 				String emsg = "Connection was closed because client was too slow! Slow queue consumers should use polling.";
 				msg = "Client: " + client + ". Message: " + emsg;
 			}
 			else
 			{
 				String emsg = wtf.Cause.getMessage();
 				msg = "Client: " + client + ". Message: " + emsg;
 			}
 			
 			if (wtf.Cause instanceof IOException)
 			{
 				log.error(msg);
 			}
 			else
 			{
 				log.error(msg, wtf.Cause);
 			}
 			
 		}
 		catch (Throwable t)
 		{
 			log.error("Unspecified error", t);
 		}
 	}
 
 	@Override
 	public void messageReceived(final IoSession session, Object message) throws Exception
 	{
 		if (!(message instanceof SoapEnvelope))
 		{
 			return;
 		}
 
 		final SoapEnvelope request = (SoapEnvelope) message;
 
 		try
 		{
 			handleMessage(session, request);
 		}
 		catch (Throwable e)
 		{
 			exceptionCaught(session, e);
 		}
 	}
 
 	private void handleMessage(IoSession session, final SoapEnvelope request) throws Throwable
 	{
 		final String requestSource = MQ.requestSource(request);
 
 		if (request.body.notify != null)
 		{
 			Notify sb = request.body.notify;
 
 			if (sb.destinationType.equals("TOPIC"))
 			{
 				_brokerConsumer.subscribe(sb, session);
 			}
 			else if (sb.destinationType.equals("QUEUE"))
 			{
 				_brokerConsumer.listen(sb, session);
 			}
 			else if (sb.destinationType.equals("TOPIC_AS_QUEUE"))
 			{
 				if (StringUtils.contains(sb.destinationName, "@"))
 				{
 					_brokerConsumer.listen(sb, session);
 				}
 				else
 				{
 					throw new IllegalArgumentException("Not a valid destination name for a TOPIC_AS_QUEUE consumer");
 				}
 
 			}
 			return;
 		}
 		else if (request.body.publish != null)
 		{
 			_brokerProducer.publishMessage(request.body.publish, requestSource);
 			return;
 		}
 		else if (request.body.enqueue != null)
 		{
 			_brokerProducer.enqueueMessage(request.body.enqueue, requestSource);
 			return;
 		}
 		else if (request.body.poll != null)
 		{
 			Poll poll = request.body.poll;
 			BrokerSyncConsumer.poll(poll, session);
 			return;
 		}
 		else if (request.body.acknowledge != null)
 		{
 			_brokerProducer.acknowledge(request.body.acknowledge);
 			return;
 		}
 		else if (request.body.unsubscribe != null)
 		{
 			Unsubscribe unsubs = request.body.unsubscribe;
 			_brokerConsumer.unsubscribe(unsubs, session);
 			return;
 		}
 		else if (request.body.checkStatus != null)
 		{
 			SoapEnvelope soap_status = new SoapEnvelope();
 			soap_status.body.status = new Status();
 			session.write(soap_status);
 			return;
 		}
 		else
 		{
 			throw new RuntimeException("Not a valid request");
 		}
 	}
 }
