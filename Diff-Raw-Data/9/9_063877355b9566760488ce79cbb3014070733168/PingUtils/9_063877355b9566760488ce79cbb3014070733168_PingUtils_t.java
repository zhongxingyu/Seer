 package br.edu.ufcg.lsd.commune.network.xmpp;
 
 import java.util.concurrent.Semaphore;
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 
 import br.edu.ufcg.lsd.commune.network.xmpp.ConnectionCheckRunnable.XMPPConnectionListener;
 
 public class PingUtils {
 
 	public static void addPingListener(XMPPConnection connection) {
 		connection.addPacketListener(
 				createPingListener(connection), 
 				createPingFilter());
 		
 	}
 	
 	public static void checkResource(ConnectionConfiguration cc, String login, String password,
 			String resource, int reconnectionSleepTime) throws CommuneNetworkException {
 		
 		XMPPConnection checkResourceConnection = new XMPPConnection(cc);
 		final Semaphore semaphore = new Semaphore(0);
 		
 		new Thread(new ConnectionCheckRunnable(checkResourceConnection, 
 				new XMPPConnectionListener() {
 
 			@Override
 			public void connectionCreated() {
 				semaphore.release();
 			}
 			
 		}, reconnectionSleepTime)).start();
 		
 		try {
 			semaphore.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		String randomResource = Long.toHexString(Double.doubleToLongBits(Math.random()));
 		
 		try {
 			checkResourceConnection.login(login, password, randomResource);
 		} catch (XMPPException e) {
 			throw new CommuneNetworkException( "Error logging in to XMPP server with user name: '" + login +
 					"'. Check XMPP user name and password. " + e.getMessage() , e );
 		}
 		
 		Ping ping = new Ping();
 		String to = login + "@" + cc.getHost() + "/" + resource;
 		ping.setTo(to);
 		
 		 // Wait up to a certain number of seconds for a reply from the server.
         PacketCollector collector = checkResourceConnection.createPacketCollector(
                 createPongFilter(ping));
         
         checkResourceConnection.sendPacket(ping);
         Packet response = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
         collector.cancel();
 		
 		if (response != null) {
 			throw new CommuneNetworkException( "Error logging in to XMPP server with user name: '" + login +
 					"'. Resource " + resource + " is already in use.");
 		}
         
 	}
 	
 	private static PacketFilter createPongFilter(final Ping ping) {
 		return new AndFilter(
 				new PacketIDFilter(ping.getPacketID()),
 				new PacketFilter() {
 
 					@Override
 					public boolean accept(Packet packet) {
 						Message message = (Message) packet;
						
						if (!message.getFrom().equals(ping.getTo())) {
							return false;
						}
						
 						if (message.getProperty(Ping.PROPERTY) == null) {
 							return false;
 						}
 						
 						if (!message.getProperty(Ping.PROPERTY).equals(Ping.PONG)) {
 							return false;
 						}
 						
 						return true;
 					}
 				});
 	}
 	
 	private static PacketFilter createPingFilter() {
 		return new PacketFilter() {
 			
 			@Override
 			public boolean accept(Packet packet) {
 				
 				if (!(packet instanceof Message)) {
 					return false;
 				}
 				
 				Message m = (Message) packet;
 				if (m.getProperty(Ping.PROPERTY) == null) {
 					return false;
 				}
 				
 				if (m.getProperty(Ping.PROPERTY).equals(Ping.PING)) {
 					return true;
 				}
 				
 				return false;
 			}
 		};
 	}
 	
 	private static PacketListener createPingListener(final XMPPConnection connection) {
 		return new PacketListener() {
 
 			@Override
 			public void processPacket(Packet packet) {
 				Message message = (Message) packet;
 
 				message.setTo(packet.getFrom());
 				message.setFrom(null);
 				message.setProperty(Ping.PROPERTY, Ping.PONG);
 					
 				connection.sendPacket(packet);
 			}
 		};
 	}
 	
 	private static class Ping extends Message {
 		
 		static String PROPERTY = "PING";
 		static String PING = "ping";
 		static String PONG = "pong";
 		
 		public Ping() {
 			setProperty(PROPERTY, PING);
 		}
 		
 	}
 	
 }
