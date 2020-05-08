 package org.boardgameengine.model;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.scxml.ErrorReporter;
 import org.apache.commons.scxml.EventDispatcher;
 import org.apache.commons.scxml.SCXMLExecutor;
 import org.apache.commons.scxml.SCXMLListener;
 import org.apache.commons.scxml.Status;
 import org.apache.commons.scxml.TriggerEvent;
 import org.apache.commons.scxml.io.SCXMLParser;
 import org.apache.commons.scxml.model.CustomAction;
 import org.apache.commons.scxml.model.Datamodel;
 import org.apache.commons.scxml.model.ModelException;
 import org.apache.commons.scxml.model.SCXML;
 import org.apache.commons.scxml.model.State;
 import org.apache.commons.scxml.model.Transition;
 import org.apache.commons.scxml.model.TransitionTarget;
 import org.boardgameengine.config.Config;
 import org.boardgameengine.error.GameLoadException;
 import org.boardgameengine.persist.PMF;
 import org.boardgameengine.scxml.js.JsContext;
 import org.boardgameengine.scxml.js.JsEvaluator;
 import org.boardgameengine.scxml.js.JsFunctionJsonTransformer;
 import org.boardgameengine.scxml.model.Error;
 import org.boardgameengine.scxml.semantics.SCXMLGameSemanticsImpl;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.utils.SystemProperty;
 
 import flexjson.JSONSerializer;
 import flexjson.transformer.Transformer;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.Element;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.Order;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.jdo.annotations.Extension;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 @PersistenceCapable
 public class Game extends ScriptableObject implements EventDispatcher, SCXMLListener {
 	
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private String gameid;
 	
 	@Persistent
 	private Key owner;
 		
 	@Persistent
 	private Set<Key> watchers;
 	
 	@Persistent
 	private Key gameTypeKey;
 	
 	@NotPersistent
 	transient private boolean isDirty_ = false;
 	
 	@NotPersistent
 	transient private boolean isError_ = false;
 	
 	@NotPersistent
 	transient private String errorMessage_ = "";
 		
 	//not persistent
 	@NotPersistent
 	transient private Map<String,String> params;
 	
 	@NotPersistent
 	transient private SCXML scxml;
 	
 	@NotPersistent
 	transient private List<Exception> loadWarnings;
 	
 	@NotPersistent
 	private
 	transient SCXMLExecutor exec;
 	
 	@NotPersistent
 	transient private JsEvaluator eval;
 	
 	@NotPersistent
 	transient private JsContext cxt;
 	
 	@Persistent
 	@Element(dependent = "true", mappedBy = "game", extensions = @Extension(vendorName="datanucleus", key="cascade-persist", value="true"))
 	@Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="role asc"))	
 	private List<Player> players;
 	
 	@Persistent
 	@Element(dependent = "true", mappedBy = "game", extensions = @Extension(vendorName="datanucleus", key="cascade-persist", value="true"))
 	@Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="eventDate asc"))
 	private List<GameHistoryEvent> events;
 	
 	@Persistent
 	@Element(dependent = "true", mappedBy = "game", extensions = @Extension(vendorName="datanucleus", key="cascade-persist", value="true"))
 	@Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="stateDate asc"))
 	private List<GameState> states;
 	
 	@NotPersistent
 	transient private Log log = LogFactory.getLog(Game.class);
 	
 	private static String nextGameId() {
 		return new BigInteger(60, Config.getInstance().getRandom()).toString(32).toUpperCase();
 	}
 	
 	public Game() {
 		gameid = nextGameId();
 		events = new ArrayList<GameHistoryEvent>();
 		states = new ArrayList<GameState>();
 		params = new HashMap<String,String>();
 		players = new ArrayList<Player>();
 		watchers = new HashSet<Key>();
 	}
 	
 	public Game(GameType gt) throws GameLoadException {
 		this();
 		
 		setGameType(gt);
 		init();
 	}
 	
 	private void init() throws GameLoadException {
 		//GameType gt = getGameType();
 		//InputStream bis = new ByteArrayInputStream(gt.getStateChart());
 		InputStream bis = Game.class.getResourceAsStream("/pilgrims.xml");
 		
 		loadWarnings = new ArrayList<Exception>();
 		
 		List<CustomAction> customActions = new ArrayList<CustomAction>();
 		CustomAction ca = new CustomAction("http://www.pilgrimsofnatac.com/schemas/game.xsd", "error", Error.class);
 		customActions.add(ca);
 		
 		scxml = null;
 		try {
 			scxml = SCXMLParser.parse(new InputSource(bis), new ErrorHandler() {
 				@Override
 				public void warning(SAXParseException exception) throws SAXException {
 					loadWarnings.add(exception);
 				}
 				
 				@Override
 				public void fatalError(SAXParseException exception) throws SAXException {
 					throw exception;				
 				}
 				
 				@Override
 				public void error(SAXParseException exception) throws SAXException {
 					throw exception;							
 				}
 			}, customActions);
 		} catch (SAXException e) {
 			throw new GameLoadException("Fatal parse error.", e);
 		} catch (ModelException e) {
 			throw new GameLoadException("Fatal parse error.", e);
 		} catch (IOException e) {
 			throw new GameLoadException("Fatal parse error.", e);
 		}
 
 		eval = new JsEvaluator();
 		ErrorReporter rep = new ErrorReporter() {
 			@Override
 			public void onError(String errCode, String errDetail, Object errCtx) {
 				// TODO really handle errors here.
 				log.error(errCode + ": " + errDetail);
 			}
 		};
 		
 		exec = new SCXMLExecutor(eval, null, rep, new SCXMLGameSemanticsImpl());
 		exec.addListener(scxml, this);
 		exec.setEventdispatcher(this);
 		exec.setStateMachine(scxml);
 		
 		cxt = (JsContext)exec.getRootContext();
 		//cxt.setLocal("game", this);
 		
 		
 		
 		try {
 			if(states != null && states.size() > 0) {
 				GameState gs = states.get(states.size() - 1);
 				
 				gs.injectInto(cxt);
 				
 				Set s = exec.getCurrentStatus().getStates();
 				Map ms = exec.getStateMachine().getTargets();
 				s.clear();
 				
 				Set<String> ss = gs.getStateSet();
 				for(String state : ss) {
 					s.add(ms.get(state));
 				}				
 			}
 			else {
 				exec.go();
 				// record initial state
 				GameState gs = new GameState(this);
 				
 				Set<State> s = exec.getCurrentStatus().getStates();
 				for(State state : s) {
 					gs.getStateSet().add(state.getId());
 				}
 				gs.setStateDate(new Date());
 				gs.extractFrom(scxml.getDatamodel(), cxt);
 				states.add(gs);
 				/*
 				PersistenceManager pm = PMF.getInstance().getPersistenceManager();
 				
 				pm.makePersistent(gs);
 				*/
 			}
 		}
 		catch(ModelException e) {
 			throw new GameLoadException("Could not start the machine.", e);
 		}
 		isDirty_ = false;
 		
 	}
 
 	@Override
 	public void cancel(String sendId) {
 		// TODO Auto-generated method stub	
 	}
 
 	@Override
 	public void send(String sendId, String target, String targetType,
 			String event, Map params, Object hints, long delay,
 			List externalNodes) {
 		
 		log.info(String.format("Send Event '%s'", event));
 		
 		boolean success = false;
 		
 		if(	targetType.equals("http://www.pilgrimsofnatac.com/schemas/game.xsd#GameEventProcessor") &&
 			target.equals("http://www.pilgrimsofnatac.com/schemas/game.xsd#GameEvent")) {
 			if(event.equals("game.playerJoined")) {
 				String playerid = params.get("playerid").toString();
 				String role = params.get("role").toString();
 				
 				GameUser gameUser = GameUser.findByHashedUserId(playerid);
 				
 				if(gameUser == null) {
 					//TODO: this is bad-- not sure what to do here...
 				}
 				else {
 					addPlayer(gameUser, role);
 					success = true;
 					isError_ = false;
 				}
 			}
 			else if(event.equals("game.error")) {
 				errorMessage_ = params.get("message").toString();
 				log.error("[error]: " + params.get("message"));
 				isError_ = true;
 			}
 			else {
 				success = true;
 				isError_ = false;
 			}
 		}
 		
 		if(success) {
 			sendWatcherMessage(event, params);
 		}
 	}
 	
 	public boolean getIsError() {
 		return isError_;
 	}
 	public String getErrorMessage() {
 		return errorMessage_;
 	}
 	
 	public static String createChannelKey(Game g, GameUser gu) {
 		return g.gameid + " " + gu.getHashedUserId();
 	}
 	public String getChannelKeyForGameUser(GameUser gu) {
 		return createChannelKey(this, gu);
 	}
 	public static String[] parseChannelKey(String channelkey) {
 		return channelkey.split(" ");
 	}
 		
 	public void sendWatcherMessage(String event, Map params) {
 		Map<String,Object> message = new HashMap<String,Object>();
 		message.put("event", event);
 		message.put("params", params);
 		
 		JSONSerializer json = new JSONSerializer();
 		
 		String strmessage = json.transform(new JsFunctionJsonTransformer(), Function.class, Scriptable.class).include("actions").serialize(message);		
 				
 		ChannelService channelService = ChannelServiceFactory.getChannelService();
 		
 		List<GameUser> w = getWatchers();
 		for(GameUser gu : w) {
 			String channelkey = createChannelKey(this, gu);
 			channelService.sendMessage(new ChannelMessage(channelkey, strmessage));
 		}
 	}
 	
 	public GameState persistGameState() {
 		// record initial state
 		GameState gs = new GameState(this);
 		
 		Set<State> s = exec.getCurrentStatus().getStates();
 		for(State state : s) {
 			gs.getStateSet().add(state.getId());
 		}
 		gs.setStateDate(new Date());
 		gs.extractFrom(scxml.getDatamodel(), cxt);
 		states.add(gs);
 		
 		this.makePersistent();
 		isDirty_ = false;
 		isError_ = false;
 		
 		return gs;
 	}
 	
 
 	
 	public String[] getTransitionEvents() {
 		Set<String> ret = new HashSet<String>();
 		
 		Set<State> s = exec.getCurrentStatus().getStates();
 		for(State state : s) {
 			List transitions = state.getTransitionsList();
 			for(Object o : transitions) {
 				Transition t = (Transition)o;
 				String event = t.getEvent();
 				if(event != null && !event.equals("")) {
 					ret.add(event);
 				}
 			}
 		}
 		
 		return ret.toArray(new String[0]);
 	}
 	
 	public static Game findGameById(String gameid) throws GameLoadException {
 		PersistenceManager pm = PMF.getInstance().getPersistenceManager();
 		
 		Game ret = null;
 		
 		Query q = pm.newQuery(Game.class);
 		q.setFilter("gameid == gameIdToFind");
 		q.declareParameters("String gameIdToFind");
 		List<Game> results = (List<Game>)q.execute(gameid.toUpperCase());
 		
 		
 		if(results.size() > 0) {
 			ret = results.get(0);
 			ret.init();
 		}
 
 		pm.close();
 		
 		return ret;
 	}
 	
 	public SCXMLExecutor getExec() {
 		return exec;
 	}
 	public GameUser getOwner() {
 		PersistenceManager pm = PMF.getInstance().getPersistenceManager();
 		Query q = pm.newQuery(GameUser.class);
 		q.setFilter("key == keyIn");
 		q.declareParameters(Key.class.getName() + " keyIn");
 		
 		List<GameUser> ret = (List<GameUser>)q.execute(owner);
 		
 		if(ret.size() > 0) {
 			return ret.get(0);
 		}
 		else {
 			return null;
 		}
 	}
 	public void setOwner(User u) {
 		GameUser gu = GameUser.findOrCreateGameUserByUser(u);
 		owner = gu.getKey();
 	}
 	
 	public List<GameHistoryEvent> getEvents() {
 		return events;
 	}
 	protected void setGameId(String gameid_) {
 		this.gameid = gameid_;
 	}
 	public String getGameId() {
 		return gameid;
 	}
 	public List<GameState> getStates() {
 		return states;
 	}
 	public Map<String,String> getParameters() {
 		return params;
 	}
 	public Date getCurrentTime() {
 		return new Date();
 	}
 	public void addGameState(Date stateDate, Status currentStatus, Datamodel datamodel, JsContext cxt) {
 		GameState s = new GameState(this);
 		s.setStateDate(stateDate);
 		s.getStateSet().clear();
 		s.getStateSet().addAll(currentStatus.getStates());
 		s.extractFrom(datamodel, cxt);
 		states.add(s);
 	}
 	public void addEvent(Date eventDate) {
 		GameHistoryEvent e = new GameHistoryEvent(this);
 		e.setEventDate(eventDate);
 		events.add(e);
 	}
 	public List<Player> getPlayers() {
 		return players;
 	}
 	public List<GameUser> getWatchers() {
 		List<GameUser> ret = new ArrayList<GameUser>();
 		PersistenceManager pm = PMF.getInstance().getPersistenceManager();
 		Query q = pm.newQuery(GameUser.class);
 		q.setFilter("key == keyIn");
 		q.declareParameters(Key.class.getName() + " keyIn");
 		
 		for(Key k : watchers) {
 			List<GameUser> l = (List<GameUser>)q.execute(k);
 			if(l != null && l.size() > 0) {
 				ret.add(l.get(0));
 			}
 		}
 		
 		return ret;
 	}
 	private void addPlayer(User user, String role) {
 		GameUser gu = GameUser.findOrCreateGameUserByUser(user);
 		players.add(new Player(this, gu, role));
 	}
 	private void addPlayer(GameUser gameUser, String role) {
 		players.add(new Player(this, gameUser, role));
 	}
 
 	public boolean sendStartGameRequest(User user) {
 		isError_ = false;
 		
 		GameUser gu = GameUser.findOrCreateGameUserByUser(user);
 		
 		if(gu.getKey().compareTo(owner) != 0) {
 			return false;
 		}
 		
 		DocumentBuilderFactory docbuilderfactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = null;
 		try {
 			builder = docbuilderfactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return false;
 		}
 		Document doc = builder.newDocument();
 		
 		Node player = doc.createElementNS(Config.getInstance().getGameEngineNamespace(), "player");
 		player.appendChild(doc.createTextNode(gu.getHashedUserId()));
 		doc.appendChild(player);
 				
 		try {
 			getExec().triggerEvent(new TriggerEvent("game.startGame", TriggerEvent.SIGNAL_EVENT, doc));
 		} catch (ModelException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		if(isDirty_ && !isError_) {
 			persistGameState();
 			isDirty_ = false;
 			isError_ = false;
 			return true;
 		}
 		else {
 			isDirty_ = false;
 			isError_ = false;
 			return false;
 		}
 	}
 	public boolean sendPlayerJoinRequest(User user) {
 		isError_ = false;
 		
 		GameUser gu = GameUser.findOrCreateGameUserByUser(user);
 		
 		DocumentBuilderFactory docbuilderfactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = null;
 		try {
 			builder = docbuilderfactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			//TODO: exception handling
 			e.printStackTrace();
 			return false;
 		}
 		Document doc = builder.newDocument();
 		
 		Node player = doc.createElementNS(Config.getInstance().getGameEngineNamespace(), "player");
 		player.appendChild(doc.createTextNode(gu.getHashedUserId()));
 		doc.appendChild(player);
 		
 		try {
 			getExec().triggerEvent(new TriggerEvent("game.playerJoin", TriggerEvent.SIGNAL_EVENT, doc));
 		} catch (ModelException e) {
 			//TODO: exception handling...
 			e.printStackTrace();
 			return false;
 		}
 		
 		if(isDirty_ && !isError_) {
 			persistGameState();
 		}
 		else {
 			isDirty_ = false;
 			isError_ = false;
 		}
 		
 		return true;
 	}
 	public boolean triggerEvent(String eventid, Node node) {
 		boolean ret = true;
 		
 		isError_ = false;
 		
 		try {
 			getExec().triggerEvent(new TriggerEvent(eventid, TriggerEvent.SIGNAL_EVENT, node));
 		} catch (ModelException e) {
 			if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Development)
 				e.printStackTrace();
 			return false;
 		}
 		
 		if(isDirty_ && !isError_) {
 			persistGameState();
 		}
 		else if(isError_) {
 			ret = false;
 		}
 		
 		isDirty_ = false;
 		isError_ = false;
 		
 		return ret;
 	}
 	public boolean addWatcher(User user) {
 		GameUser gu = GameUser.findOrCreateGameUserByUser(user);
 		return watchers.add(gu.getKey());
 	}
 	public boolean removeWatcher(User user) {
 		GameUser gu = GameUser.findOrCreateGameUserByUser(user);
 		return watchers.remove(gu.getKey());
 	}
 	public void setGameType(GameType gt) {
 		if(gt != null) this.gameTypeKey = gt.getKey();
 		else this.gameTypeKey = null;
 	}
 	public GameType getGameType() {
 		return GameType.findByKey(this.gameTypeKey);
 	}
 	
 	public void makePersistent() {
 		PersistenceManager pm = PMF.getInstance().getPersistenceManager();
 		
 		try {
 			pm.makePersistent(this);
 			/*
 			for(GameState gs : states) {
 				pm.makePersistent(gs);
 			}
 			*/
 		}
 		catch(Exception e) {
 			//TODO: handle this somehow...
 			e.printStackTrace();
 		}
 		finally {		
 			pm.close();
 		}
 	}
 	
 	public void serialize(Writer out) {
 		JSONSerializer json = new JSONSerializer();
 		
 		json.include("events")
 			.include("parameters")
 			.include("players")
 			.include("watchers")
 			.exclude("*.class")
 			.exclude("*.className")
 			.exclude("*.key")
 			.exclude("*.extensible")
 			.exclude("*.parentScope")
 			.exclude("*.prototype")
 			.exclude("*.sealed")
 			.exclude("*.typeOf")
 			.exclude("*.empty")
 			.exclude("*.user")
 			.exclude("_ALL_STATES")
 			.exclude("states.state")
 			.exclude("states.game")
 			.exclude("events.game")
 			.exclude("players.game")
 			.exclude("gameType.stateChart")
 			
 			.transform(Config.getInstance().getDateTransfomer(), Date.class);
 			
 		if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Development)
 			json.prettyPrint(true);
 		else
 			json.prettyPrint(false);
 					
 		json.serialize(this, out);
 	}
 
 	@Override
 	public String getClassName() {
 		return "Game";
 	}
 
 	@Override
 	public void onEntry(TransitionTarget state) {
 		log.info("OnEntry: " + state.getId());			
 	}
 
 	@Override
 	public void onExit(TransitionTarget state) {
 		log.info("OnExit: " + state.getId());	
 	}
 
 	@Override
 	public void onTransition(TransitionTarget from, TransitionTarget to,
 			Transition transition) {
 		log.info("OnTransition: " + from.getId() + " -> " + to.getId() + ": [" + transition.getEvent() + "]");
 		isDirty_ = true;		
 	}
 
 
 }
