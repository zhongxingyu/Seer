 package backend;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import lombok.Getter;
 import lombok.Setter;
 import modules.Module;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.UtilSSLSocketFactory;
 import org.pircbotx.exception.IrcException;
 import org.reflections.Reflections;
 
 import commands.Command;
 
 
 
 public class Bot extends PircBotX implements Constants{
 
 	@Getter private static HashSet<String> owners = new HashSet<>();
 	@Getter private static HashSet<String> elevated = new HashSet<>();
 	@Getter private static HashSet<String> banned = new HashSet<>();
 	
 	static {
 		List<HashMap<String,Object>> data = null;
 		try {
 			Database.createTable("bot_users", "name char(25) not null, owner smallint, elevated smallint, banned smallint");
 			data = Database.select("select * from bot_users");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if(data != null) {
 			for(HashMap<String,Object> column : data) {
 				Object i = new Integer(0);
 				if(column.get("BANNED")!=null && !(column.get("BANNED").equals(i))) addBanned(column.get("NAME").toString());
 				if(column.get("OWNER")!=null && !(column.get("OWNER").equals(i))) addOwner(column.get("NAME").toString());
 				if(column.get("ELEVATED")!=null && !(column.get("ELEVATED").equals(i))) addElevated(column.get("NAME").toString());
 			}
 		}
 	}
 	
 	//All of the Bot instances
 	@Getter private static LinkedList<Bot> bots = new LinkedList<>();
 
 	//Constants
 	final static String INTERNAL_VERSION = "0.1";
 	public final static String DEFAULT_SERVER = "irc.esper.net";
 	public final static String DEFAULT_NICKNAME = "VivioBot";
 	public final static int DEFAULT_PORT = 6667;
 	
 	//Variables 
 	@Getter @Setter private boolean parsesSelf = false;
 	@Getter @Setter private int botMode = ACCESS_DEVELOPMENT;
 	
 	//Module comparator
 	private class ModComparator implements Comparator<Module> {
 
 		@Override
 		public int compare(Module o1, Module o2) {
 			if(o2.getPriorityLevel() - o1.getPriorityLevel() == 0) return o1.getName().compareTo(o2.getName());
 			return o2.getPriorityLevel() - o1.getPriorityLevel();
 		}
 		
 	}
 	
 	@Getter
 	private CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();
 	
 	//Constructors
 	public Bot() {
 		this(DEFAULT_SERVER);
 	}
 
 	public Bot(String server) {
 		this(server, DEFAULT_PORT);
 	}
 
 	public Bot(String server, int port) {
 		this(server, port, false);
 	}
 
 	public Bot(String server, int port, boolean SSL) {
 		this(server, port, SSL, DEFAULT_NICKNAME);
 	}
 	
 	public Bot(String server, int port, boolean SSL, String nick)
 	{
 		this(server, port, SSL, nick,"");
 	}
 	
 	public Bot(String server, int port, boolean SSL, String nick,
 				String serverPass)
 	{
 		initialize();
 		connectToServer(server, port, SSL, nick, serverPass);
 		
 	}
 	
 	//Methods
 	private void connectToServer(String server, int port, boolean SSL, String nick, String serverPass) {
 		
 		this.setAutoNickChange(true);
 		this.setVerbose(true);
 		this.setAutoSplitMessage(true);
		
		this.setFinger("Don't finger me! Vivio v"+INTERNAL_VERSION);
 
 		this.setVersion("PircBotX~Vivio v" + INTERNAL_VERSION);
 		this.setLogin(nick);
 		this.setName(nick);
 
 		try {
 			if (SSL)
 				this.connect(server, port, serverPass,
 						new UtilSSLSocketFactory().trustAllCertificates());
 			else
 				this.connect(server, port, serverPass);
 		} catch (IOException | IrcException e) {
 			e.printStackTrace();
 		}
 	}
 
 	//initialize the bot
 	private void initialize() {
 		bots.add(this);
 		this.setListenerManager(ListenerBuilder.getManager());
 		loadModules();
 	}
 
 	public void loadModules() {
 		modules.clear();
 		Reflections reflections = new Reflections("commands");
 		Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
 		for(Class<? extends Command> c : classes) {
 			try {
 				addModule((Module) Class.forName(c.getName()).newInstance());
 			} catch (InstantiationException | IllegalAccessException
 					| ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	//get the default bot -- this will almost always be the proper one to get.
 	public static Bot getBot() {
 		return bots.getFirst();
 	}
 	
 	//invoke a specific method on all of the commands and modules
 	public void invokeAll(String method, Object[] args) {
 		for(Module m : modules) {
 			
 			if(!m.isActive()) continue;
 			if(botMode < m.getAccessMode()) continue;
 			
 			//invoke methods onFoo
 			for(Method methodName : m.getClass().getMethods()) {
 				if(methodName.getName().equals(method)) {
 					try {
 						methodName.invoke(m, args);
 					} catch (IllegalAccessException | IllegalArgumentException
 							| InvocationTargetException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		}
 	}
 
 	//parse commands coming in
 	public boolean checkCommands(User user, String message, Channel chan) {
 		
 		String commandString = message.split(" ")[0];
 	
 		String comm = commandString.substring(1);
 		String prefix = commandString.substring(0,1);
 				
 		for(Module m : modules) {
 			if(m instanceof Command) {
 				Command command = (Command) m;
 				if(!command.isActive()) continue;
 				if(botMode < command.getAccessMode()) continue;
 				if(getLevelForUser(user, chan) < command.getAccessLevel()) continue;
 				if(!prefix.startsWith(command.getCmdSequence())) continue;
 				if(!command.hasAlias(comm)) continue;
 				command.execute(comm, this, chan, user, message);
 				if(command.isStopsExecution()) return false;
 			}
 		}
 		return true;
 	}
 	
 	private void addModule(Module m) {
 		this.modules.add(m);
 		List<Module> modules = Arrays.asList(this.modules.toArray(new Module[0]));
 		Collections.sort(modules, new ModComparator());
 		this.modules.clear();
 		this.modules.addAll(modules);
 	}
 
 	@Override
 	public void sendAction(String target, String action) {
 		if(banned.contains(target)) return;
 		super.sendAction(target, action);
 	}
 
 	@Override
 	public void sendMessage(String target, String message) {
 		if(banned.contains(target)) return;
 		super.sendMessage(target, message);
 	}
 
 	@Override
 	public void sendNotice(String target, String notice) {
 		if(banned.contains(target)) return;
 		super.sendNotice(target, notice);
 	}
 
 	private int getLevelForUser(User u, Channel c) {
 		//TODO identified users only?
 		if(owners.contains(u.getNick().toLowerCase())) return LEVEL_OWNER;
 		if(elevated.contains(u.getNick().toLowerCase())) return LEVEL_ELEVATED;
 		if(banned.contains(u.getNick().toLowerCase())) return LEVEL_BANNED;
 		if(c!=null && c.isOp(u)) return LEVEL_OPERATOR;
 		return LEVEL_NORMAL;
 	}
 	
 	public static void addBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set banned=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.add(bnd);
 	}
 	
 	public static void removeBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set banned=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.remove(bnd);
 	}
 	
 	public static void addOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set owner=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.add(bnd);
 	}
 	
 	public static void removeOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set owner=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.remove(bnd);
 	}
 	
 	public static void addElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set elevated=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.add(bnd);
 	}
 	
 	public static void removeElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set elevated=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.remove(bnd);
 	}
 }
