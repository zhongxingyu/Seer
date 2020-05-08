 package net.thiagoalz.hermeto.control;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.hermeto.android.main.XMPPClient;
 import net.thiagoalz.hermeto.panel.GameManager;
 import net.thiagoalz.hermeto.player.Player;
 import net.thiagoalz.hermeto.player.Player.Direction;
 
 import org.jivesoftware.smack.XMPPException;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * Control the access of the players using the XMPP protocol.
  */
 public class XMPPGameplayControl implements GameplayControl, Runnable {
 
 	private static final int MESSAGE_PLAYER = 1;
 
 	private final String SERVER_LOGIN = "a";
 	private final String SERVER_PASSWORD = "123456";
 	private final String SERVER_ADDRESS = "lilab.info";
 	private final String CLIENT_LOGIN = "b@lilab.info";
 
 	protected List<String> players;
 
 	XMPPClient chatClient;
 
 	Thread myThread;
 
 	boolean running = false;
 
 	private static final String tag = XMPPGameplayControl.class
 			.getCanonicalName();
 	private GameManager gameManager;
 
 	public XMPPGameplayControl(GameManager gameManager) {
 		this.gameManager = gameManager;
 	}
 
 	@Override
 	public boolean processMessage(String playerReference, String message) {
 		Log.d("processMessage", "Msg Recebida: (" + playerReference + ") "
 				+ message);
 
 		String[] msgSplit = message.split(" ");
 
 		if (msgSplit.length > 1) {// all messages have 2 parameters
 			if (msgSplit[0].toUpperCase().equals("HELLO")) {
 				connectPlayer(msgSplit[1]);
 			} else if (msgSplit[1].equals("button")) {
 				markSquare(msgSplit[0]);
 			} else if (msgSplit[1].equals("disconnect")) {
 				disconnectPlayer(msgSplit[0]);
 			} else {
 				movePlayer(msgSplit[0], msgSplit[1]);
 			}
 		}
 		return true;
 	}
 
 	protected boolean movePlayer(String playerID, String dir) {
 		Player player = gameManager.getPlayer(playerID);
 
 		if (player != null) {// Player exists
 			Player.Direction direction = parseDirection(dir);
 			return gameManager.move(player, direction);
 		}
 		return false;
 	}
 
 	private Direction parseDirection(String direction) {
 		Log.d(tag, "Parsing direction '" + direction + "'");
 		direction = direction.toLowerCase();
 		Player.Direction dir;
 		if (direction.equals(Player.Direction.LEFT.getValue())) {
 			dir = Player.Direction.LEFT;
 		} else if (direction.equals(Player.Direction.RIGHT.getValue())) {
 			dir = Player.Direction.RIGHT;
 		} else if (direction.equals(Player.Direction.UP.getValue())) {
 			dir = Player.Direction.UP;
 		} else {
 			dir = Player.Direction.DOWN;
 		}
 		return dir;
 	}
 
 	protected boolean markSquare(String playerID) {
 		Player player = gameManager.getPlayer(playerID);
 
 		if (player != null) {// Player exists
 			return gameManager.mark(player);
 		}
 
 		return false;
 	}
 
 	protected boolean disconnectPlayer(String playerID) {
 		Player player = gameManager.getPlayer(playerID);
 		players.remove(playerID);
 		if (player != null) {// Player exists
 			gameManager.disconnectPlayer(player);
 			return true;
 		}
 
 		return false;
 	}
 
 	protected String connectPlayer(String name) {
 		Player player = gameManager.connectPlayer(name);
 		String id = player.getId();
 		players.add(id);
 		try {
 			String resposta = "HELLO " + name + " " + id;
 			Log.d("XMPP", "Reply: " + resposta);
 			chatClient.sendMessage(resposta);
 		} catch (XMPPException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	@Override
 	public void run() {
 		try {
 			chatClient = new XMPPClient(5222, CLIENT_LOGIN, SERVER_ADDRESS,
 					SERVER_LOGIN, SERVER_PASSWORD, "server");
 			Log.d("XMPP", "Conected");
 
 			while (running) {
 				org.jivesoftware.smack.packet.Message xmppMsg = chatClient
 						.checkMessage();
 
 				if (xmppMsg != null
 						&& ((xmppMsg.getType() == org.jivesoftware.smack.packet.Message.Type.chat) || (xmppMsg
 								.getType() == org.jivesoftware.smack.packet.Message.Type.normal))
 						&& xmppMsg.getBody() != null) { // Got a message
 
 					Log.d("XMPP", "Got message: (" + xmppMsg.getFrom() + ") "
 							+ xmppMsg.getBody());
 					Message m = Message.obtain(mHandler, MESSAGE_PLAYER);
 					m.obj = new PlayerMsg(xmppMsg.getFrom(), xmppMsg.getBody());
 					mHandler.sendMessage(m);
 				}
 			}
 		} catch (XMPPException e) {
 			e.printStackTrace();
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 
 	}
 
 	protected class PlayerMsg {
 		private String userName;
 		private String msg;
 
 		public PlayerMsg(String userName, String msg) {
 			this.userName = userName;
 			this.msg = msg;
 		}
 
 		public String getUserName() {
 			return userName;
 		}
 
 		public String getMsg() {
 			return msg;
 		}
 	}
 
 	Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (msg.what == MESSAGE_PLAYER) {
 				PlayerMsg k = (PlayerMsg) msg.obj;
 				processMessage(k.getUserName(), k.getMsg());
 			}
 
 		}
 
 	};
 
 	public void stop() {
 		running = false;
 		if (myThread != null) {
 			myThread.interrupt();
 		}
 
 		// Disconnect all players
		if(players!=null){
			int size = players.size();
			for (int i = 0; i < size; i++) {
				disconnectPlayer(players.get(i));
			}
 		}
 		players = null;
 	}
 
 	public void start() {
 		while (myThread != null && myThread.isAlive()) {
 			stop();
 		}
 		players = new ArrayList<String>();
 		running = true;
 		myThread = new Thread(null, this, "XMPP");
 		myThread.start();
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 }
