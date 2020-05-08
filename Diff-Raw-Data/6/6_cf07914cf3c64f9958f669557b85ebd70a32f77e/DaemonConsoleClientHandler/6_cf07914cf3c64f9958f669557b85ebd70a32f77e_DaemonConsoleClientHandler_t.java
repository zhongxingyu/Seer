 package com.zzvc.mmps.daemon.console;
 
 import static com.zzvc.mmps.daemon.DaemonConstants.DEFAULT_GROUP;
 
 import java.net.InetSocketAddress;
 import java.util.ResourceBundle;
 
 import javax.annotation.Resource;
 
import org.apache.log4j.Logger;
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 
 import com.zzvc.mmps.daemon.DaemonMessage;
 
 public class DaemonConsoleClientHandler extends IoHandlerAdapter {
	private static Logger logger = Logger.getLogger(DaemonConsoleClientHandler.class);
	
 	@Resource
 	private DaemonConsoleClient client;
 	
 	private NioSocketConnector connector;
 	private IoSession session;
 	private boolean closed;
 	
     private String group = DEFAULT_GROUP;
     private String host;
     private int port;
 	
 	public void init() {
 		ResourceBundle bundle = ResourceBundle.getBundle("daemon");
 		
 		try {
 			group = bundle.getString("console.daemon.group");
 		} catch (Exception e) {
 		}
 		try {
 			host = bundle.getString("console.daemon.host");
 			port = Integer.parseInt(bundle.getString("console.daemon.port"));
 		} catch (Exception e) {
 		}
 		
 		connector = new NioSocketConnector();
 		connector.setHandler(this);
 		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
 		
 		client.setHost(host);
 		client.setPort(port);
 		client.init();
 	}
 
 	public boolean isClosed() {
 		return closed;
 	}
 	
 	public void setClosed(boolean closed) {
 		this.closed = closed;
 	}
 	
 	public boolean needConnect() {
 		return !(isClosed() || isConnected());
 	}
 	
 	public boolean isConnected() {
 		return session != null && session.isConnected();
 	}
 
 	public boolean connect() {
 		client.connecting();
         ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
         future.awaitUninterruptibly();
         if (!future.isConnected()) {
         	client.connectFailed();
             return false;
         }
         session = future.getSession();
         join();
         
         return true;
 	}
 	
 	public void join() {
 		session.write(new DaemonMessage(DaemonMessage.CMD_JOIN, group).createMessage());
 	}
 	
 	public void quit() {
 		if (isConnected()) {
 			session.write(new DaemonMessage(DaemonMessage.CMD_QUIT, "").createMessage());
 			session.close(true);
 		}
 		setClosed(true);
 	}
 	
 	public void close() {
 		session.close(true);
 		setClosed(true);
 	}
 
 	@Override
 	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("Mina reports socket connection error", cause);
 		client.broken();
 	}
 
 	@Override
 	public void messageReceived(IoSession session, Object message) throws Exception {
 		DaemonMessage consoleMessage = new DaemonMessage(message);
 		String messageCommand = consoleMessage.getCommand();
 		long messageTimestamp = consoleMessage.getTimestamp();
 		String messageContent = consoleMessage.getContent();
 		
 		if (DaemonMessage.CMD_APRV.equals(messageCommand)) {
 			client.approved(messageContent);
 		} else if (DaemonMessage.CMD_DENY.equals(messageCommand)) {
 			client.denied();
 			close();
 		} else if (DaemonMessage.CMD_INFO.equals(messageCommand)) {
 			client.infoRemote(messageTimestamp, messageContent);
 		} else if (DaemonMessage.CMD_WARN.equals(messageCommand)) {
 			client.warnRemote(messageTimestamp, messageContent);
 		} else if (DaemonMessage.CMD_ERROR.equals(messageCommand)) {
 			client.errorRemote(messageTimestamp, messageContent);
 		} else if (DaemonMessage.CMD_STUS.equals(messageCommand)) {
 			client.statusRemote(messageContent);
 		} else if (DaemonMessage.CMD_CLOSE.equals(messageCommand)) {
 			client.closed();
 		}
 	}
 }
