 package gatherer;
 
 import static gatherer.IgsConstants.*;
 import static gatherer.IgsOption.*;
 import gatherer.listener.IgsConnect;
 import gatherer.listener.IgsDisconnect;
import gatherer.listener.IgsGame;
 import gatherer.listener.IgsMatch;
import gatherer.listener.IgsMove;
 import gatherer.listener.IgsResult;
 import gatherer.listener.IgsVerboseTraffic;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 import play.Logger;
 
 public class IgsGatherer implements WeiqiGameGatherer {
 
 	private TelnetUtil telnet;
 
 	private boolean loggedIn = false;
 
 	private Set<String> games = new HashSet<String>();
 
 	private String server;
 
 	private int port;
 
 	private final WeiqiStorage storage;
 
 	public IgsGatherer(WeiqiStorage storage) {
 		Logger.info("start IGS gatherer");
 		this.storage = storage;
 	}
 
 	@Override
 	public void setServer(String server, int port) {
 		Logger.info("set server %s", server);
 		this.server = server;
 		this.port = port;
 	}
 
 	public void connect() {
 		telnet = TelnetUtil.connect(server, port);
 	}
 
 	@Override
 	public void disconnect() {
 		if (isConnected() == false) {
 			throw new IllegalStateException("not connected");
 		}
 		try {
 			Logger.info("telnet disconnect " + this);
 			telnet.disconnect();
 			Logger.debug("telnet disconnected from " + this);
 		} catch (IOException e) {
 			throw new RuntimeException("disconnect telnet failed", e);
 		}
 	}
 
 	public Set<String> getGames() {
 		return games;
 	}
 
 	@Override
 	public boolean isConnected() {
		return telnet.isConnected();
 	}
 
 	@Override
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	@Override
 	public void login(String user, String password) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * login to IGS as guest
 	 */
 	@Override
 	public void loginAnonymous() {
 
 		if (isLoggedIn()) {
 			throw new IllegalStateException("already logged in");
 		}
 
 		if (isConnected() == false) {
 			telnet.connect();
 		}
 
 		Logger.info("login as anonymous to server " + this);
 
 		telnet.readUntil(LOGIN_PROMPT);
 		telnet.send(GUEST);
 		telnet.readUntil(PROMPT);
 
 		Logger.debug("logged in as anonymous to server " + this);
 		loggedIn = true;
 	}
 
 	@Override
 	public void logout() {
 		if (isLoggedIn()) {
 			Logger.info("log out " + this);
 			telnet.send("exit");
 			Logger.debug("logged out " + this);
 			loggedIn = false;
 		}
 	}
 
 	public void getGameInfo(String id) {
 		Logger.info("get game info %s", id);
 		send(GET_GAME_INFO, id);
 	}
 
 	@Override
 	public void observce(String id) {
 		if (games.contains(id)) {
 			Logger.info("update game %s moves", id);
 			send(MOVES, id);
 		} else {
 			Logger.info("observe %s", id);
 			games.add(id);
 			send(OBSERVE_GAME, id);
 			send(MOVES, id);
 		}
 	}
 
 	@Override
 	public void toggle(IgsOption option) {
 		telnet.send(MessageFormat.format(IgsConstants.TOGGLE, option.option));
 	}
 
 	private void toggle(IgsOption option, boolean on) {
 		telnet.send(MessageFormat.format(IgsConstants.TOGGLE_ON, option.option, on));
 	}
 
 	private void send(String command, Object value) {
 		telnet.send(MessageFormat.format(command, value));
 	}
 
 	@Override
 	public void start() {
 		Logger.info("start gatherer for " + this);
 		if (isConnected() == false || isLoggedIn() == false) {
 			throw new IllegalStateException("not logged in");
 		}
 
 		toggle(CLIENT, true);
 		telnet.readUntil(OK);
 
		telnet.addOutputListener(new IgsGame(server, storage));
		telnet.addOutputListener(new IgsMove(server, storage));
 		telnet.addOutputListener(new IgsResult(server, storage));
 		telnet.addOutputListener(new IgsMatch(this));
 		telnet.addOutputListener(new IgsConnect());
 		telnet.addOutputListener(new IgsDisconnect());
 		telnet.addOutputListener(new IgsVerboseTraffic());
 
 		telnet.start();
 		Logger.debug("gathering " + this);
 		toggle(QUIET, false);
 	}
 
 	@Override
 	public void stop() {
 		telnet.setShouldRead(false);
 		toggle(QUIET, true);
 		telnet.readUntil(OK);
 		toggle(CLIENT, false);
 		telnet.readUntil(PROMPT);
 		Logger.debug("gathering stopped");
 	}
 
 	@Override
 	public String toString() {
 		return telnet.toString();
 	}
 
 	@Override
 	public void unObservce(String id) {
 		games.remove(id);
 		send(UNOBSERVE_GAME, id);
 	}
 
 }
