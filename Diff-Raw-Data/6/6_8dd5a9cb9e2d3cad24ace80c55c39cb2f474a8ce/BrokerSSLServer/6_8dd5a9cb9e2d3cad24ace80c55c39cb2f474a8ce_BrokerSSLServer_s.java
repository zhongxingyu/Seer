 package pt.com.broker.core;
 
 import java.io.FileInputStream;
 import java.net.InetSocketAddress;
 import java.security.KeyStore;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.filter.executor.IoEventQueueThrottle;
 import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
 import org.apache.mina.filter.ssl.SslFilter;
 import org.apache.mina.transport.socket.SocketAcceptor;
 import org.apache.mina.transport.socket.SocketSessionConfig;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.caudexorigo.Shutdown;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.messaging.AuthorizationFilter;
 import pt.com.broker.net.BrokerProtocolHandler;
 import pt.com.broker.net.codec.BrokerCodecRouter;
 import pt.com.gcs.conf.GcsInfo;
 
 public class BrokerSSLServer
 {
 
 	private static Logger log = LoggerFactory.getLogger(BrokerSSLServer.class);
 
 	private int _portNumber;
 
 	private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;
 
 	private static final int NCPU = Runtime.getRuntime().availableProcessors();
 
 	public BrokerSSLServer(int portNumber)
 	{
 		_portNumber = portNumber;
 	}
 
 	public void start()
 	{
 		try
 		{
 			javax.net.ssl.SSLContext sslContext = getSSLContext();
 			if (sslContext == null)
 				return;
 
 			ThreadPoolExecutor tpe = new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle(MAX_BUFFER_SIZE));
 			final SocketAcceptor acceptor0 = new NioSocketAcceptor(NCPU);
 
 			acceptor0.setReuseAddress(true);
 			((SocketSessionConfig) acceptor0.getSessionConfig()).setReuseAddress(true);
 			((SocketSessionConfig) acceptor0.getSessionConfig()).setTcpNoDelay(false);
 			((SocketSessionConfig) acceptor0.getSessionConfig()).setKeepAlive(true);
 
 			AuthorizationFilter authFilter = AuthorizationFilter.getInstance();
 
 			DefaultIoFilterChainBuilder filterChainBuilder0 = acceptor0.getFilterChain();
 
 			filterChainBuilder0.addLast("SSL_FILTER", new SslFilter(sslContext));
 			filterChainBuilder0.addLast("BROKER_CODEC", new ProtocolCodecFilter(BrokerCodecRouter.getInstance()));
 			filterChainBuilder0.addLast("AUTHORIZATION_FILTER", authFilter);
 			filterChainBuilder0.addLast("executor", new ExecutorFilter(tpe));
 
 			acceptor0.setHandler(new BrokerProtocolHandler());
 
 			// Bind
 			acceptor0.bind(new InetSocketAddress(_portNumber));
 			log.info("SAPO-SSL-BROKER  Listening on: '{}'.", acceptor0.getLocalAddress());
 
 		}
		catch (Throwable e)
 		{
			log.error(e.getMessage(), e);
			Shutdown.now();
 		}
 	}
 
 	private javax.net.ssl.SSLContext getSSLContext() throws Exception
 	{
 
 		KeyStore keyStore = KeyStore.getInstance("JKS");
 
 		String keyStoreLocation = GcsInfo.getKeystoreLocation();
 		if (keyStoreLocation == null)
 		{
 			// Deal with this gracefully
 			return null;
 		}
 
 		String keyStorePasswordStr = GcsInfo.getKeystorePassword();
 		if (keyStorePasswordStr == null)
 		{
 			// Deal with this gracefully
 			return null;
 		}
 		String keyPasswordStr = GcsInfo.getKeyPassword();
 		if (keyPasswordStr == null)
 		{
 			// Deal with this gracefully
 			return null;
 		}
 
 		char[] KEYSTOREPW = keyStorePasswordStr.toCharArray();
 		char[] KEYPW = keyPasswordStr.toCharArray();
 
 		keyStore.load(new FileInputStream(keyStoreLocation), KEYSTOREPW);
 
 		javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance("SunX509");
 
 		kmf.init(keyStore, KEYPW);
 
 		javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSLv3");
 
 		sslContext.init(kmf.getKeyManagers(), null, null);
 
 		return sslContext;
 	}
 }
