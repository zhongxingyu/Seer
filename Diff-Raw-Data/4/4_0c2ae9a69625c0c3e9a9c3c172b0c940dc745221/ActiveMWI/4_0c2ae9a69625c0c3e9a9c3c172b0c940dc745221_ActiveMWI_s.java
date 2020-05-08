 package de.codewheel.activemwi;
 
 import java.io.IOException;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.asteriskjava.manager.AuthenticationFailedException;
 import org.asteriskjava.manager.ManagerConnection;
 import org.asteriskjava.manager.ManagerConnectionFactory;
 import org.asteriskjava.manager.ManagerConnectionState;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.TimeoutException;
 import org.asteriskjava.manager.action.MailboxCountAction;
 import org.asteriskjava.manager.action.OriginateAction;
 import org.asteriskjava.manager.action.StatusAction;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.event.PeerStatusEvent;
 import org.asteriskjava.manager.response.MailboxCountResponse;
 import org.asteriskjava.manager.response.ManagerResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * TODO fix reconnection problem on asterisk restart
  * TODO fix connection/event loop (events are not processed when sleeping in mailbox connection loop)
  * TODO better exception handling
  * 
  * @author <a href="mailto:rjenster@gmail.com">Ruben Jenster</a>
  *
  */
 public class ActiveMWI implements ManagerEventListener {
 	
 
 	private static final long MAILBOX_CONNECT_DELAY = 3000L;
 
 	private static final Logger LOG = LoggerFactory.getLogger(ActiveMWI.class);
 
 	private ManagerConnection eventConnection;
 	private ManagerConnection cmdConnection;
 
 	private static CompositeConfiguration config = new CompositeConfiguration();
 	static {
 		try {
 			config.addConfiguration(new PropertiesConfiguration("/etc/activemwi.properties"));
 		} catch (ConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static String SERVER_IP = config.getString("server.ip", "localhost");
 	private static int SERVER_PORT = config.getInt("manager.port", 5038);
 	private static String MANAGER_USER = config.getString("manager.user");
 	private static String MANAGER_PASS = config.getString("manager.pass");
 	private static String MBOX_EXTEN = config.getString("mbox.exten");
 	private static String MBOX_CONTEXT = config.getString("mbox.context");
 	private static long MBOX_RING_TIMEOUT = config.getLong("mbox.ring.timeout", 20000);
 	private static long MBOX_RETRY_INTERVAL = config.getLong("mbox.retry.interval", 600000);
 	private static int MBOX_RETRY_MAX = config.getInt("mbox.retry.max", 6);
 
 	public ActiveMWI() throws IOException {
 		ManagerConnectionFactory factory = new ManagerConnectionFactory(SERVER_IP, SERVER_PORT, MANAGER_USER, MANAGER_PASS);
 
 		// create manager connections
 		eventConnection = factory.createManagerConnection();
 		cmdConnection = factory.createManagerConnection();
 	}
 
 	public void run() throws IOException, AuthenticationFailedException,
 			TimeoutException, InterruptedException {
 
 		// register for events
 		eventConnection.addEventListener(this);
 
 		while (true) {
 
 			// connect to asterisk manager and log in
 			eventConnection.login();
 			
 			// request channel state
 			eventConnection.sendAction(new StatusAction());
 
 			// wait for events to come in
 			while (eventConnection.getState() == ManagerConnectionState.CONNECTED) {
 				Thread.sleep(60000L);
 			}
 
 			// and finally log off and disconnect
 			eventConnection.logoff();
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		ActiveMWI activeMWI;
 
 		activeMWI = new ActiveMWI();
 		activeMWI.run();
 	}
 
 	public void onManagerEvent(ManagerEvent event) {
 
 		if (event.getClass() == PeerStatusEvent.class) {
 			onPeerStatusEvent((PeerStatusEvent) event);
 		}
 
 	}
 
 	public void onPeerStatusEvent(PeerStatusEvent event) {
 
 		if (event.getPeerStatus().equals(PeerStatusEvent.STATUS_REACHABLE)) {
 
 			try {
 				// open command connection
 				cmdConnection.login();
 				
 				String peer = event.getPeer();
 				// connect reachable client to mailbox
 				if (hasNewMessages(peer)) {
 					connectToMailbox(peer);
 				}
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			} finally {
 				cmdConnection.logoff();
 			}

 		}
 	}
 
 	public boolean hasNewMessages(String peer) {
 		
 		try {
 			// query mailbox for new messages
 			String mailbox = peer.replace("SIP/", "") + "@default";
 
 			MailboxCountResponse mboxCountResponse = (MailboxCountResponse) cmdConnection.sendAction(new MailboxCountAction(mailbox));
 			int newMessageCount = mboxCountResponse.getNewMessages();
 			
 			if (newMessageCount > 0) {
 				LOG.info("Peer[{}] has[{}] new messages", peer, newMessageCount);
 				return true;
 			} else {
 				LOG.info("Peer[{}] has no new messages", peer);
 				return false;
 			}
 			
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// TODO: add better retry policy
 	public void connectToMailbox(String peer) {
 		try {
 			try {
 				Thread.sleep(MAILBOX_CONNECT_DELAY);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			OriginateAction mboxConnect = new OriginateAction();
 			mboxConnect.setChannel(peer);
 			mboxConnect.setContext(MBOX_CONTEXT);
 			mboxConnect.setExten(MBOX_EXTEN);
 			mboxConnect.setCallerId(peer.replace("SIP/", ""));
 			mboxConnect.setPriority(new Integer(1));
 			mboxConnect.setTimeout(MBOX_RING_TIMEOUT);
 
 			boolean connectionEstablished = false;
 			int retries = 0;
 			// try to connect the peer to the mailbox until success
 			while (!connectionEstablished) {
 
 				retries++;
 
 				if (retries >= MBOX_RETRY_MAX) {
 					LOG.error("Maximum connection retries[{}] exceeded. Aborting!", MBOX_RETRY_MAX);
 					break;
 				}
 
 				// if client has read the messages in the meantime don't call him
 				if (!hasNewMessages(peer)) {
 					LOG.debug("Client has read messages in meantime. Aborting!");
 					break;
 				}
 				
 				connectionEstablished = connect(mboxConnect);
 
 				// if client answered the call finish calling loop
 				if (connectionEstablished) {
					LOG.info("Connection established after [{}] retries");
 					break;
 				}
 				try {
 					LOG.info("Retry after {} msec", MBOX_RETRY_INTERVAL);
 					Thread.sleep(MBOX_RETRY_INTERVAL);
 				} catch (InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TimeoutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		LOG.debug("Connect to mailbox loop finished!");
 	}
 	
 	
 	/**
 	 * @param mboxConnect
 	 * @return true if the connection has been established and the peer answered the call, else false
 	 * @throws IllegalArgumentException
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 * @throws TimeoutException
 	 */
 	private boolean connect(OriginateAction mboxConnect) throws IllegalStateException, IOException, TimeoutException {		
 		ManagerConnectionState state = cmdConnection.getState();
 		LOG.debug("Connection state[{}]", state.name());
 		LOG.debug("Connecting to mailbox of peer[{}]", mboxConnect.getChannel());
 		// response timeout must be greater than the connection timeout, 
 		// else a TimeoutException is thrown if the peer doesn't pick up
 		ManagerResponse response = cmdConnection.sendAction(mboxConnect, mboxConnect.getTimeout() + 200);
 		String responseCode = response.getResponse();
 		LOG.debug("Connection to peer[{}] established, response[{}]", mboxConnect.getChannel(), responseCode);
 		return "Success".equals(responseCode);
 	}
 
 }
