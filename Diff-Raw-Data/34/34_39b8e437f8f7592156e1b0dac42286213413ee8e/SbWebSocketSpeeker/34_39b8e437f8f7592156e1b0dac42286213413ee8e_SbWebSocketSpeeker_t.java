 package epam.ph.sg.models.sb;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.websocket.WebSocket;
 
 public class SbWebSocketSpeeker implements WebSocket.OnTextMessage {
 	private static Logger log = Logger.getLogger(SbWebSocketSpeeker.class);
 	private Connection conn;
 
 	static {
 		log.debug("+++++++++++++++SbWebSocketSpeeker loaded++++++++++++++++++++++");
 	}
 
 	@Override
 	public void onClose(int arg0, String arg1) {
 		log.debug("++++++++++++Socket closed++++++++++++++");
 
 	}
 
 	@Override
 	public void onOpen(Connection conn) {
 		conn.setMaxIdleTime(1800000);
 		this.conn = conn;
 		log.debug("++++++++++++Socket created++++++++++++++");
 
 	}
 
 	@Override
 	public void onMessage(String message) {
 
 		String[] temporary = message.split("&");
 		if (temporary[0].equals("open")) {
 			Integer gameID = Integer.parseInt(temporary[1]);
 			String userName = temporary[2];
 			String connectionType = temporary[3];
 			log.debug("++++++++++++gameID = " + gameID + "++++++++++++++");
 			log.debug("++++++++++++userName = " + userName + "++++++++++++++");
 			log.debug("++++++++++++connectionType = " + connectionType
 					+ "++++++++++++++");
 			if (connectionType.equalsIgnoreCase("server")) {
 				log.debug("++++++++++++server v pershomu if++++++++++++++");
 				if (ActiveGames.getGame(gameID).getServer().getConn() != null) {
 					log.debug("++++++++++++server v drugomu if++++++++++++++");
					//ActiveGames.getGame(gameID).getServer().getConn().close();
 				}
 				ActiveGames.getGame(gameID).getServer().setConn(conn);
 			}
 			if (connectionType.equalsIgnoreCase("client")) {
 				log.debug("++++++++++++client v pershomu if++++++++++++++");
 				if (ActiveGames.getGame(gameID).getClient().getConn() != null) {
 					log.debug("++++++++++++client v pershomu if++++++++++++++");
					//ActiveGames.getGame(gameID).getClient().getConn().close();
 				}
 				ActiveGames.getGame(gameID).getClient().setConn(conn);
 			}
 		}
 //		if (temporary[0].equals("close")) {
 //		}
 
 
 		log.debug("++++++++++++Recieved message++++++++++++++");
 		log.debug(message);
 	}
 }
