 package pt.com.broker.net;
 
 import java.io.IOException;
 
 import org.apache.mina.common.IoFilterChain;
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.traffic.ReadThrottleFilterBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.core.ErrorHandler;
 import pt.com.broker.messaging.BrokerConsumer;
 import pt.com.broker.messaging.BrokerProducer;
 import pt.com.broker.messaging.MQ;
 import pt.com.broker.messaging.Notify;
 import pt.com.broker.net.codec.SoapCodec;
 import pt.com.broker.xml.SoapEnvelope;
 
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
 		IoFilterChain chain = iosession.getFilterChain();
 
 		chain.addLast("SOAP_CODEC", new ProtocolCodecFilter(new SoapCodec()));
 
 		ReadThrottleFilterBuilder read_th_filter = new ReadThrottleFilterBuilder();
 
 		read_th_filter.setMaximumConnectionBufferSize(4 * 1024);
 		read_th_filter.attach(chain);
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("Session created: " + iosession.getRemoteAddress());
 		}
 	}
 
 	@Override
 	public void sessionClosed(IoSession iosession)
 	{
 		try
 		{
 			String remoteClient = getClientAddress(iosession);
 			log.info("Session closed: " + remoteClient);
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
 
 		iosession.write(ex_msg);
 		iosession.close();
 
 		if (!(wtf.Cause instanceof IOException))
 		{
 			if (iosession == null)
 				return;
 
 			String client = getClientAddress(iosession);
 
 			String msg = "";
 			String emsg = wtf.Cause.getMessage();
 			msg = "Client: " + client + ". Message: " + emsg;
 
			log.error(msg, wtf.Cause);
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
 				_brokerConsumer.listen(sb, session);
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
 		else if (request.body.acknowledge != null)
 		{
 			_brokerConsumer.acknowledge(request.body.acknowledge);
 			return;
 		}
 		else
 		{
 			throw new RuntimeException("Not a valid request");
 		}
 	}
 
 	private String getClientAddress(IoSession iosession)
 	{
 		String remoteClient;
 		try
 		{
 			remoteClient = iosession.getRemoteAddress().toString();
 		}
 		catch (Throwable e)
 		{
 			remoteClient = "Can't determine client address";
 		}
 		return remoteClient;
 	}
 }
