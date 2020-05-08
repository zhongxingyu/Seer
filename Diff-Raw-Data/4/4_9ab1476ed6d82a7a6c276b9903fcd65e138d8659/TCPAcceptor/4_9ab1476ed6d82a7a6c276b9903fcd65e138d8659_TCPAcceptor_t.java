 package edu.hziee.common.tcp;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetSocketAddress;
 
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.hziee.common.lang.Holder;
 import edu.hziee.common.lang.transport.Receiver;
 import edu.hziee.common.tcp.endpoint.DefaultEndpointFactory;
 import edu.hziee.common.tcp.endpoint.Endpoint;
 import edu.hziee.common.tcp.endpoint.EndpointFactory;
 import edu.hziee.common.tcp.endpoint.IEndpointChangeListener;
 
 /**
  * TODO
  * 
  * @author wangqi
  * @version $Id: TCPAcceptor.java 52 2012-02-19 04:51:33Z archie $
  */
 public class TCPAcceptor {
 	private static final Logger		logger					= LoggerFactory.getLogger(TCPAcceptor.class);
 
 	private int										maxRetryCount		= 20;
 	private long									retryTimeout		= 30 * 1000;																	// 30s
 
 	private String								acceptIp				= "0.0.0.0";
 	private int										acceptPort			= 7777;
 	private NioSocketAcceptor			acceptor				= new NioSocketAcceptor();
 
 	private ProtocolCodecFactory	codecFactory		= null;
 
 	private EndpointFactory				endpointFactory	= new DefaultEndpointFactory();
 
 	public void start() throws IOException {
 		acceptor.setReuseAddress(true);
 
 		acceptor.setHandler(new IOHandler());
 		acceptor.getSessionConfig().setReadBufferSize(2048);
 		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
 
 		int retryCount = 0;
 		boolean binded = false;
 		do {
 			try {
 				acceptor.bind(new InetSocketAddress(acceptIp, acceptPort));
 				binded = true;
 			} catch (BindException e) {
 				logger.warn("start failed on port:[{}], " + e + ", and retry...", acceptPort);
 				// 对绑定异常再次进行尝试
 				retryCount++;
 				if (retryCount >= maxRetryCount) {
 					// 超过最大尝试次数
 					throw e;
 				}
 				try {
 					Thread.sleep(retryTimeout);
 				} catch (InterruptedException e1) {
 				}
 			} catch (IOException e) {
 				// 对其它IO异常继续抛出
 				throw e;
 			}
 		} while (!binded);
 
 		logger.info("start succeed in " + acceptIp + ":" + acceptPort);
 
 	}
 
 	public void stop() {
 		this.acceptor.dispose();
 	}
 
 	private class IOHandler extends IoHandlerAdapter {
 		private final Logger	logger	= LoggerFactory.getLogger(IOHandler.class);
 
 		@Override
 		public void messageReceived(IoSession session, Object msg) throws Exception {
 			if (logger.isTraceEnabled()) {
 				logger.trace("messageReceived: " + msg);
 			}
 			Endpoint endpoint = TransportUtil.getEndpointOfSession(session);
 			if (null != endpoint) {
 				endpoint.messageReceived(TransportUtil.attachSender(msg, endpoint));
 			} else {
 				logger.warn("missing endpoint, ignore incoming msg:", msg);
 			}
 		}
 
 		@Override
 		public void sessionOpened(IoSession session) throws Exception {
 			if (logger.isInfoEnabled()) {
 				logger.info("sessionOpened: " + session);
 			}
 		}
 
 		@Override
 		public void sessionCreated(IoSession session) throws Exception {
 			Endpoint endpoint = endpointFactory.createEndpoint(session);
 			if (null != endpoint) {
 				TransportUtil.attachEndpointToSession(session, endpoint);
 			}
 		}
 
 		@Override
 		public void sessionClosed(final IoSession session) throws Exception {
 			if (logger.isDebugEnabled()) {
 				logger.debug("sessionClosed: " + session.getId());
 			}
 			Endpoint endpoint = TransportUtil.getEndpointOfSession(session);
 			if (null != endpoint) {
 				endpoint.stop();
 			}
 		}
 
 		@Override
 		public void exceptionCaught(IoSession session, Throwable e) throws Exception {
			if (logger.isDebugEnabled()) {
				logger.debug("TCPAcceptor: " + e.getMessage());
			}
 			Endpoint endpoint = TransportUtil.getEndpointOfSession(session);
 			if (null != endpoint) {
 				endpoint.stop();
 			}
 			// session关闭
 			session.close();
 		}
 	}
 
 	public int getMaxRetryCount() {
 		return maxRetryCount;
 	}
 
 	public void setMaxRetryCount(int maxRetryCount) {
 		this.maxRetryCount = maxRetryCount;
 	}
 
 	public long getRetryTimeout() {
 		return retryTimeout;
 	}
 
 	public void setRetryTimeout(long retryTimeout) {
 		this.retryTimeout = retryTimeout;
 	}
 
 	public void setReceiver(Receiver receiver) {
 		endpointFactory.setReceiver(receiver);
 	}
 
 	public void setContext(Holder context) {
 		endpointFactory.setContext(context);
 	}
 
 	public void setEndpointListener(IEndpointChangeListener endpointListener) {
 		endpointFactory.setEndpointListener(endpointListener);
 	}
 
 	public void setEndpointFactory(EndpointFactory endpointFactory) {
 		this.endpointFactory = endpointFactory;
 	}
 
 	public String getAcceptIp() {
 		return acceptIp;
 	}
 
 	public void setAcceptIp(String acceptIp) {
 		this.acceptIp = acceptIp;
 	}
 
 	public int getAcceptPort() {
 		return acceptPort;
 	}
 
 	public void setAcceptPort(int acceptPort) {
 		this.acceptPort = acceptPort;
 	}
 
 	public ProtocolCodecFactory getCodecFactory() {
 		return codecFactory;
 	}
 
 	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
 		this.codecFactory = codecFactory;
 	}
 }
