 package com.popodeus.chat;
 
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 
 /**
  * photodeus
  * Jul 24, 2009
  * 5:31:17 AM
  */
 public class MsLuvaLuva extends PircBot implements Runnable, BotCallbackAPI {
 
 	private float version = 1.4f;
 	private Logger log = Logger.getLogger("com.popodeus.chat.MsLuvaLuva");
 	private Thread runner;
 	protected Random random;
 
 	//private long last_action_time = 0;
 
 	private ScriptManager scriptmanager;
 	//private Map<String, Set<User>> channel_users;
 
 	private ChatLogger logger;
 	private BotRemote remote;
 
 	private String quitmsg = "Bye bye!";
 	private long startupTime, connectTime;
 	private boolean rejoinmessage_enabled = false;
 
 	protected List<String> greetings;
 	protected boolean silence;
 
 	protected ResourceBundle properties;
 	public static final String PROP_NICK = "nick";
 	public static final String PROP_ALTNICK = "altnick";
 	public static final String PROP_GREETINGS_FILE = "greetings.file";
 	public static final String PROP_REJOINMSG = "rejoinmsg";
 	public static final String PROP_REJOINMSG_ENABLED = "rejoinmsg.enabled";
 	public static final String PROP_QUITMSG = "quitmsg";
 	public static final String PROP_VARIABLE_CACHE_DIR = "script.variable.dir";
 	public static final String PROP_SERVER = "server";
 	public static final String PROP_PASSWORD = "password";
 	public static final String PROP_CHANNELS = "channels";
 	public static final String PROP_GREET_CHANNELS = "channels.greet";
 	public static final String PROP_SCRIPT_DIR = "script.dir";
 	public static final String PROP_VERSION_STRING = "version.string";
 	public static final String PROP_LOG_DIR = "log.dir";
 	static final String MSG_ON_IGNORE_LIST = "You are on the bot ignore list.";
 
 	enum Event {
 		JOIN,
 		MESSAGE,
 		ACTION,
 		NICKCHANGE,
 		RIGHTS,
 		KICK,
 		LEAVE,
 		TOPIC,
 		QUIT,
 		UNSUPPORTED,
 	}
 
 	public MsLuvaLuva() throws Exception {
 		startupTime = System.currentTimeMillis();
 		log.info("MsLuvaLuva v" + version + " is starting...");
 		random = new Random();
 
 		reinitialize();
 
 		scriptmanager = new ScriptManager(
 				this,
 				new File(properties.getString(PROP_SCRIPT_DIR)),
 				new File(properties.getString(PROP_VARIABLE_CACHE_DIR))
 		);
 
 		remote = new BotRemote(8123, this, scriptmanager.getAPI(null));
 
 		runner = new Thread(this);
 		runner.start();
 	}
 
 	public boolean byebye() {
 		remote.shutDown();
 		runner.interrupt();
 		return true;
 	}
 
 	public void reinitialize() {
 		try {
 			log.info("MsLuvaLuva reinitialize");
 			properties = ResourceBundle.getBundle("config");
 			setLogin(properties.getString(PROP_NICK));
 			setVersion(properties.getString(PROP_VERSION_STRING));
 			quitmsg = properties.getString(PROP_QUITMSG);
 			rejoinmessage_enabled = Boolean.parseBoolean(properties.getString(PROP_REJOINMSG_ENABLED));
 			if (logger != null) logger.closeAll();
 			logger = new ChatLogger(new File(properties.getString(PROP_LOG_DIR)));
 		} catch (Error ex) {
 			log.log(Level.SEVERE, ex.getMessage(), ex);
 		} catch (Exception ex) {
 			log.log(Level.SEVERE, ex.getMessage(), ex);
 		}
 	}
 
 
 	@Override
 	public void run() {
 		boolean keeprunning = true;
 		if (keeprunning && do_connect()) {
 			while (keeprunning) {
 				try {
 					// Check interruption status every 5 seconds
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					keeprunning = false;
 					break;
 				}
 			}
 			log.info("MsLuvaLuva is disconnecting...");
 			logger.closeAll();
 			quitServer(quitmsg);
 		}
 		scriptmanager.saveScriptVars(new File(properties.getString(PROP_VARIABLE_CACHE_DIR)));
 		System.exit(0);
 	}
 
 	@Override
 	protected void onDisconnect() {
 		log.info("Was disconnected...");
 		logger.logAction(null, "Disconnected from server");
 		int counter = 500;
 		while (!isConnected()) {
 			log.info(new Date() + " attempting reconnect");
 			logger.logAction(null, "Attempting to connect to server (tries left: "+counter+")", true);
 			if (do_connect()) {
 				if (rejoinmessage_enabled) {
 					String rejoinmsg = properties.getString(PROP_REJOINMSG);
 					if (rejoinmsg != null && rejoinmsg.length() > 0) {
 						for (String channel : getChannels()) {
 							say(channel, rejoinmsg);
 						}
 					}
 				}
 				return;
 			}
 			try {
 				long sleep = 30000 + random.nextInt(30) * 1000;
 				log.fine("Sleeping " + sleep + " ms");
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 			}
 			if (--counter == 0) {
 				break;
 			}
 		}
 	}
 
 	protected boolean do_connect() {
 		String basenick = properties.getString(PROP_NICK);
 		String altnick = properties.getString(PROP_ALTNICK);
 		if (altnick == null) {
 			altnick = basenick + "_";
 		}
 		try {
 			//channel_users = new HashMap<String, Set<User>>();
 			setName(basenick);
 			if (!isConnected()) {
 				connect(properties.getString(PROP_SERVER));
 			} else {
 				reconnect();
 			}
 			identify(properties.getString(PROP_PASSWORD));
 		} catch (NickAlreadyInUseException naius) {
 			log.info("Nick " + basenick + " was already in use...");
 			setName(altnick);
 			try {
 				if (!isConnected()) {
 					connect(properties.getString(PROP_SERVER));
 				} else {
 					reconnect();
 				}
 			} catch (Exception e) {
 				System.err.println(e);
 				return false;
 			}
 			sendRawLine("NICKSERV GHOST " + basenick + " " + properties.getString(PROP_PASSWORD));
 			setName(basenick);
 		} catch (java.net.UnknownHostException e) {
 			System.err.println(e);
 			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		connectTime = System.currentTimeMillis();
 		for (String channel : properties.getString(PROP_CHANNELS).split(",")) {
 			//channel_users.put(channel, new TreeSet<User>());
 			join(channel);
 		}
 		log.info("Setting mode +b");
 		setMode(getNick(), "+b");
 		return true;
 	}
 
 	@Override
 	protected synchronized void onUserList(final String channel, final User[] users) {
 		/*
 		Set<User> u = new TreeSet<User>();
 		u.addAll(Arrays.asList(users));
 		channel_users.put(channel, u);
 		*/
 	}
 
 	@Override
 	protected void onUserMode(final String targetNick, final String sourceNick, final String sourceLogin, final String sourceHostname, final String mode) {
 		log.info(targetNick + " => " + mode + " (" + sourceNick + ")");
 	}
 
 	@Override
 	protected void onJoin(final String channel, final String sender, final String login, final String hostname) {
 		if (getNick().equals(sender)) {
 			logger.joinChannel(channel);
 		} else {
 			logger.logAction(channel, sender + " [" + login + "@" + hostname + "] has joined " + channel);
 			scriptmanager.runOnEventScript(this, Event.JOIN, sender, login, hostname, null, channel);
 		}
 	}
 
 	public String getGreeting() {
 		if (greetings == null) {
 			reloadGreetings();
 		}
 		if (greetings != null) {
 			return greetings.get(random.nextInt(greetings.size()));
 		}
 		return "Hi $nick!";
 	}
 
 	public void channelBan(final String channel, final String hostmask) {
 		logger.logAction(channel, "Trying to set ban on host " + hostmask, true);
 		ban(channel, hostmask);
 	}
 
 	public void channelUnban(final String channel, final String hostmask) {
 		logger.logAction(channel, "Trying to remove ban from host " + hostmask, true);
 		unBan(channel, hostmask);
 	}
 
 	// Might not work unless bot is op
 	public void invite(final String nick, final String channel) {
 		logger.logAction(channel, "Inviting " + nick);
 		sendInvite(nick, channel);
 	}
 
 	public void reloadGreetings() {
 		greetings = new ArrayList<String>(100);
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(properties.getString(PROP_GREETINGS_FILE)));
 			String s;
 			while ((s = br.readLine()) != null) {
 				String tmp = s.trim();
 				if (tmp.length() > 0) {
 					greetings.add(s);
 				}
 			}
 			br.close();
 		} catch (IOException e) {
 		}
 	}
 
 	@Override
 	protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
 		log.info(oldNick + " is now known as " + newNick);
 		for (String channel : getChannels()) {
 			if (isNickInChannel(channel, oldNick) || isNickInChannel(channel,  oldNick)) {
 				logger.logAction(null, oldNick + " is now known as " + newNick);
 			}
 		}
 		/*
 		User olduser = new User(User.Prefix.NONE, oldNick);
 		for (String channel : channel_users.keySet()) {
 			Set<User> u = channel_users.get(channel);
 			if (u.contains(olduser)) {
 				// Save the new nick and prefix
 				u.remove(olduser);
 				u.add(new User(olduser.getPrefix(), newNick));
 			}
 		}
 		*/
 		scriptmanager.runOnEventScript(this, Event.NICKCHANGE, newNick, login, hostname, oldNick, null);
 	}
 
 	@Override
 	protected void onMode(final String channel, final String sourceNick, final String sourceLogin, final String sourceHostname, final String mode) {
 		log.info(sourceNick + "!" + sourceLogin + "@" + sourceHostname + " ==> " + channel + " " + mode);
 		logger.logAction(channel, "mode/"+channel + " [" + mode + " " + sourceNick + "]");
 		scriptmanager.runOnEventScript(this, Event.RIGHTS, mode, sourceLogin, sourceHostname, mode, channel);
 		/*
 		if (mode.startsWith("+v")) {
 			String[] r = mode.split(" ");
 			for (int i = 1; i < r.length; i++) {
 				channel_users.get(channel).put(r[i].toLowerCase(), "+");
 			}
 		}
 		if (mode.startsWith("+o")) {
 			String[] r = mode.split(" ");
 			for (int i = 1; i < r.length; i++) {
 				channel_users.get(channel).put(r[i].toLowerCase(), "@");
 			}
 		}
 		if (mode.startsWith("+q")) {
 			String[] r = mode.split(" ");
 			for (int i = 1; i < r.length; i++) {
 				channel_users.get(channel).put(r[i].toLowerCase(), "~");
 			}
 		}
 		if (mode.startsWith("+h")) {
 			String[] r = mode.split(" ");
 			for (int i = 1; i < r.length; i++) {
 				channel_users.get(channel).put(r[i].toLowerCase(), "%");
 			}
 		}
 		if (mode.startsWith("+a")) {
 			// Protect
 			String[] r = mode.split(" ");
 			for (int i = 1; i < r.length; i++) {
 				channel_users.get(channel).put(r[i].toLowerCase(), "%");
 			}
 		}
 		*/
 	}
 
 	@Override
 	protected void onInvite(final String targetNick, final String sourceNick, final String sourceLogin, final String sourceHostname, final String channel) {
		String[] channels = properties.getString(PROP_CHANNELS).toLowerCase().split(",");
 		Arrays.sort(channels);
		if (Arrays.binarySearch(channels, channel.toLowerCase()) >= 0) {
 			join(channel);
 		} else {
 			sendNotice(sourceNick, "I'm not supposed to be on that channel.");
 		}
 	}
 
 	@Override
 	protected void onTopic(final String channel, final String topic, final String setBy, final long date, final boolean changed) {
 		logger.logAction(channel, setBy + " sets topic to: " + topic);
 		scriptmanager.runOnEventScript(this, Event.TOPIC, setBy, "" + date, null, topic, channel);
 	}
 
 
 	@Override
 	protected void onKick(final String channel, final String kickerNick, final String kickerLogin, final String kickerHostname, final String recipientNick, final String reason) {
 		logger.logAction(channel, recipientNick + " was kicked from " + channel + " by " + kickerNick + " ["+reason+"]");
 		log.info("Kicked: " + recipientNick);
 		if (recipientNick.equals(getNick())) {
 			final MsLuvaLuva _this = this;
 			TimerTask tt = new TimerTask() {
 				public void run() {
 					_this.join(channel);
 				}
 			};
 			final int KICKOUT_TIME = 15000;
 			long time = KICKOUT_TIME;
 			if (reason.matches(".*(\\d+).*")) {
 				try {
 					time = Integer.parseInt(Pattern.compile("[^0-9]*(\\d+)[^0-9]*").matcher(reason).group(1));
 					if (time > 0 && time <= 300) {
 						time *= 1000;
 					} else {
 						time = KICKOUT_TIME;
 					}
 				} catch (Exception e) {
 				}
 			}
 			log.info("Was kicked, rejoining " + channel + " in " + time + "ms");
 			Timer timer = new Timer();
 			timer.schedule(tt, time);
 		} else {
 			/*
 			Set<User> u = channel_users.get(channel);
 			if (u != null) {
 				u.remove(new User(User.Prefix.NONE, recipientNick));
 			}
 			*/
 			scriptmanager.runOnEventScript(this, Event.KICK, recipientNick, kickerLogin, kickerHostname, null, channel);
 		}
 	}
 
 	@Override
 	protected void onPrivateMessage(final String sender, final String login, final String hostname, final String message) {
 		if (message.startsWith("!") && message.trim().length() > 1) {
 			if (scriptmanager.getAPI(null).isIgnored(sender, login, hostname)) {
 				sendNotice(sender, MSG_ON_IGNORE_LIST);
 				return;
 			}
 			String[] tmp = message.split(" ", 2);
 			String cmd = tmp[0].substring(1);
 
 			actOnTrigger(sender, login, hostname, message, null);
 			/*
 			TriggerScript scr = scriptmanager.getTriggerScript(
 					properties.getString(PROP_SCRIPT_DIR),
 					cmd);
 			// TODO different timeout queues for private messages (and per nick) and public messages?
 			if (!scr.hasTimeoutPassed()) {
 				String timeoutmsg = "Too fast - Timeout is " + (timeout / 1000) + "s. Try again later.";
 				sendNotice(sender, timeoutmsg);
 				// if it takes too long to react...?
 				//last_act = System.currentTimeMillis();
 			} else {
 				// TODO measure how long it takes to run a certain script
 				actOnTrigger(sender, login, hostname, message, null);
 			}
 			*/
 		} else {
 			scriptmanager.runOnEventScript(this, Event.MESSAGE, sender, login, hostname, message, sender);
 		}
 	}
 
 	@Override
 	protected void onMessage(final String channel, final String sender, final String login, final String hostname, final String message) {
 		logger.log(channel, sender, message, true);
 		if (message.startsWith("!") && message.trim().length() > 1) {
 			boolean special = false;
 			/* sender.startsWith("@") || sender.startsWith("~") ||
 					"@".equals(getPrefix(channel, sender)) ||
 					"~".equals(getPrefix(channel, sender)); */
 			if (special) {
 
 			} else {
 				if (sender.toLowerCase().equals(sender)) {
 					sendNotice(sender, "Sorry, I don't listen to people with improper (lowercase) nicknames.");
 					return;
 				}
 				if (!hasVoice(channel, getNick())) {
 					sendNotice(sender, "I've lost my voice, so I'm not able to speak!");
 					return;
 				}
 			}
 			/*
 			String[] tmp = message.split(" ", 2);
 			String cmd = tmp[0].substring(1);
 			long last = scriptmanager.getAPI().getLastRun(cmd);
 			long delta = System.currentTimeMillis() - last;
 			long timeout = scriptmanager.getAPI().getTimeout(cmd);
 			if (special || delta >= timeout) {
 				actOnTrigger(sender, login, hostname, message, channel);
 			} else {
 				sendNotice(sender, "Too fast - Timeout is " + (timeout / 1000) + "s. Try again later.");
 			}
 			*/
 			actOnTrigger(sender, login, hostname, message, channel);
 		} else {
 			scriptmanager.runOnEventScript(this, Event.MESSAGE, sender, login, hostname, message, channel);
 		}
 	}
 
 	@Override
 	protected void onAction(final String sender, final String login, final String hostname, final String target, final String action) {
 		logger.logAction(target, "* " + sender + " " + action, true);
 		super.onAction(sender, login, hostname, target, action);
 		scriptmanager.runOnEventScript(this, Event.ACTION, sender, login, hostname, action, target);
 	}
 
 	@Override
 	protected void onQuit(final String sourceNick, final String sourceLogin, final String sourceHostname, final String reason) {
 		logger.logAction(null, sourceNick + " [" + sourceLogin + "@" + sourceHostname + "] has quit [" + reason + "]");
 		super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
 		scriptmanager.runOnEventScript(this, Event.QUIT, sourceNick, sourceLogin, sourceHostname, reason, null);
 	}
 
 	@Override
 	protected void onUnknown(final String line) {
 		log.info(line);
 		super.onUnknown(line);
 		scriptmanager.runOnEventScript(this, Event.UNSUPPORTED, null, null, null, line, null);
 	}
 
 	public void join(final String channel) {
 		joinChannel(channel);
 	}
 
 	public void join(final String channel, final String key) {
 		joinChannel(channel, key);
 	}
 
 	public void part(final String channel) {
 		partChannel(channel);
 	}
 
 	public final void say(String target, String message) {
 		if (target.startsWith("#")) {
 			logger.log(target, getNick(), message, true);
 		}
 		sendMessage(target, message);
 	}
 
 	public final void act(String target, String message) {
 		if (target.startsWith("#")) {
 			logger.log(target, getNick(), message, true);
 		}
 		sendAction(target, message);
 	}
 
 	public final void notice(String target, String message) {
 		if (target.startsWith("#")) {
 			logger.log(target, getNick(), message, true);
 		}
 		sendNotice(target, message);
 	}
 
 	public long getStartupTime() {
 		return startupTime;
 	}
 
 	public long getConnectTime() {
 		return connectTime;
 	}
 
 	public boolean isNormal(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				return user.getPrefix().isEmpty();
 			}
 		}
 		return false;
 	}
 
 	public boolean hasVoice(final String channel, final String nick) {
 		User[] users = getUsers(channel);
 		for (User user : users) {
 			if (user.equals(nick)) {
 				return user.isVoice();
 			}
 		}
 		return false;
 	}
 
 	public boolean isHalfOp(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				return user.isHalfOp();
 			}
 		}
 		return false;
 	}
 
 	public boolean isOp(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				return user.isOp();
 			}
 		}
 		return false;
 	}
 
 	public boolean isAdmin(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				return user.isAdmin();
 			}
 		}
 		return false;
 	}
 
 	public boolean isOwner(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				return user.isOwner();
 			}
 		}
 		return false;
 	}
 
 	public String getPrefix(final String channel, final String nick) {
 		for (User user : getUsers(channel)) {
 			if (user.equals(nick)) {
 				StringBuilder sb = new StringBuilder(4);
 				for (User.Prefix prefix : user.getPrefix()) {
 					sb.append(prefix.toString());
 				}
 				return sb.toString();
 			}
 		}
 		return "";
 	}
 
 	private boolean actOnTrigger(final String sender, final String login, final String hostname, final String message, final String _channel) {
 		String channel = _channel;
 		if (_channel == null) {
 			channel = sender;
 		}
 		String[] tmp = message.split(" ", 2);
 		String cmd = tmp[0].substring(1);
 		String param;
 		if (tmp.length == 1) {
 			param = sender;
 		} else {
 			param = tmp[1].trim();
 		}
 
 		boolean retval = false;
 		if (cmd.matches("[a-z_]+")) {
 			retval = scriptmanager.runTriggerScript(this,
 					properties.getString(PROP_SCRIPT_DIR), 
 					sender, login, hostname, message, channel, cmd, param);
 		}
 		return retval;
 	}
 
 
 	public String fetchUrl(String _url) {
 		String line = null;
 		try {
 			log.info("fetchUrl: " + _url);
 			URL url = new URL(_url);
 			HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
 			hurl.connect();
 			BufferedReader br = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
 			line = br.readLine();
 			hurl.disconnect();
 		} catch (IOException e) {
 			log.warning(_url + " => " + e.toString());
 		}
 		return line;
 	}
 
 	
 	public static void main(String[] args) {
 		try {
 			new MsLuvaLuva();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 }
