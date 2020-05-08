 package distributed.systems.das;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import distributed.systems.core.IMessageReceivedHandler;
 import distributed.systems.core.LogEntry;
 import distributed.systems.core.LogEntry.Position;
 import distributed.systems.core.LogEntryType;
 import distributed.systems.core.LogManager;
 import distributed.systems.core.Message;
 import distributed.systems.core.SynchronizedClientSocket;
 import distributed.systems.core.SynchronizedSocket;
 import distributed.systems.core.VectorialClock;
 import distributed.systems.das.presentation.BattleFieldViewer;
 import distributed.systems.das.units.Dragon;
 import distributed.systems.das.units.Player;
 import distributed.systems.das.units.Unit;
 import distributed.systems.das.units.Unit.UnitType;
 
 /**
  * The actual battlefield where the fighting takes place.
  * It consists of an array of a certain width and height.
  * 
  * It is a singleton, which can be requested by the 
  * getBattleField() method. A unit can be put onto the
  * battlefield by using the putUnit() method.
  * 
  * @author Pieter Anemaet, Boaz Pat-El
  */
 public class BattleField implements IMessageReceivedHandler {
 	/* The array of units */
 	private Unit[][] map;
 	private ConcurrentHashMap<InetSocketAddress, Unit> units;
 
 
 	/* The static singleton */
 	//private static BattleField battlefield;
 
 	/* Primary socket of the battlefield */ 
 	//private Socket serverSocket;
 	private SynchronizedSocket serverSocket;
 	private String url;
 	private int port;
 	private final int timeout = 1000;
 
 
 	private Map<ActionID, ActionInfo> pendingOutsideActions;
 	private Map<Integer, ActionInfo> pendingOwnActions;
 	private int localMessageCounter = 0;
 
 
 	/* The last id that was assigned to an unit. This variable is used to
 	 * enforce that each unit has its own unique id.
 	 */
 	private int lastUnitID = 0;
 
 	public final int id;
 	public final boolean restart;
 
 	public final static int MAP_WIDTH = 25;
 	public final static int MAP_HEIGHT = 25;
 	//private ArrayList <Unit> units; 
 	//private Map<InetSocketAddress, Integer> units; 
 
 	private HashMap<InetSocketAddress, Integer> battlefields; 
 
 	private VectorialClock vClock;
 	private LogManager logger;
 
 	/**
 	 * Initialize the battlefield to the specified size 
 	 * @param width of the battlefield
 	 * @param height of the battlefield
 	 */
 	BattleField(int id, String url, int port, boolean restart) {
 		battlefields = new HashMap<InetSocketAddress, Integer>();
 		this.url = url;
 		this.port = port;	
 		this.id = id;
 		this.restart = restart;
 		battlefields.put(new InetSocketAddress(url, port), 0);
 
 		initBattleField(restart);		
 	}
 
 	BattleField(int id,String url, int port, String otherUrl, int otherPort, boolean restart) {
 		battlefields = new HashMap<InetSocketAddress, Integer>();
 		this.url = url;
 		this.port = port;
 		this.id = id;
 		this.restart = restart;
 
 		battlefields.put(new InetSocketAddress(url, port), 0);
 		initBattleField(restart);
 
 		Message message = new Message();
 		message.put("request", MessageRequest.requestBFList);
 		message.put("bfAddress", new InetSocketAddress(url, port));
 		SynchronizedClientSocket clientSocket;
 		clientSocket = new SynchronizedClientSocket(message, new InetSocketAddress(otherUrl, otherPort), this);
 		clientSocket.sendMessageWithResponse();
 	}
 
 	private synchronized void initBattleField(boolean restart){
 		map = new Unit[MAP_WIDTH][MAP_WIDTH];
 		units = new ConcurrentHashMap<InetSocketAddress, Unit>();
 
 		serverSocket = new SynchronizedSocket(url, port);
 		serverSocket.addMessageReceivedHandler(this);
 		//units = new ArrayList<Unit>();
 		pendingOwnActions = new ConcurrentHashMap<Integer, ActionInfo>();
 		pendingOutsideActions = new ConcurrentHashMap<ActionID, ActionInfo>();
 		
 		vClock = new VectorialClock(5);
 		String filename = url + "_" + port;
 		if(!restart) {
 			File f = new File(filename);
 			f.delete();
 		}
 		logger = new LogManager(filename);
 		
 		
 
 	}
 	
 	private void startExecution(int numberOfDragons, int numberOfPlayers) {
 
 		System.out.println("Units will now start to connect!");	
 		
 		this.generateDragons(numberOfDragons);
 		this.generatePlayeres(numberOfPlayers);
 		
 		//Updates to game state
 		new Thread(new Runnable() {
 			public void run() {
 				SynchronizedClientSocket clientSocket;
 				while(true) {
 
 					for( Map.Entry<InetSocketAddress, Unit> entry : units.entrySet()) {
 						if(!entry.getValue().getBattlefieldAddress().equals(new InetSocketAddress(url, port))) continue;
 						Message message = new Message();
 						message.put("request", MessageRequest.gameState);
 						message.put("gamestate", map);
 						//Puts position of the unit we are sending to in the map we are sending
 						Unit u = entry.getValue();
 						message.put("unit",  u);
 
 						clientSocket = new SynchronizedClientSocket(message, entry.getKey(), null);
 						clientSocket.sendMessage();	
 					}
 
 					try {
 						Thread.sleep(100L);//Time between gameState update is sent to units
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}		
 				}
 			}
 		}).start();
 		
 
 		//Checks  game state
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(10000L);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				while(true) {
 					int dragon = 0;
 					int player = 0;
 					
 
 					
 					//System.out.println(units);
 					for( Unit entry : units.values()) {
 						if(entry instanceof Dragon) dragon++;
 						if(entry instanceof Player) player++;
 					}
 					if(dragon == 0 || player == 0) {
 						System.out.println("GAME ENDED");
 						logger.readOrderedLog();
 						logger.writeOrderedLogToTextfile("_ordered");
 						logger.cleanupStructures();
 						System.exit(1);
 
 					}
 					
					System.out.println("Units: "+ dragon + " Dragons and " + player + " Players");
 					
 					try {
 						Thread.sleep(1000L);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 				
 			}
 		}).start();
 	}
 
 	/**
 	 * Singleton method which returns the sole 
 	 * instance of the battlefield.
 	 * 
 	 * @return the battlefield.
 	 */
 	/*
 	public static BattleField getBattleField() {
 		if (battlefield == null)
 			battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
 		return battlefield;
 	}*/
 
 	/**
 	 * Puts a new unit at the specified position. First, it
 	 * checks whether the position is empty, if not, it
 	 * does nothing.
 	 * In addition, the unit is also put in the list of known units.
 	 * 
 	 * @param unit is the actual unit being spawned 
 	 * on the specified position.
 	 * @param x is the x position.
 	 * @param y is the y position.
 	 * @return true when the unit has been put on the 
 	 * specified position.
 	 */
 	private boolean spawnUnit(Unit unit, InetSocketAddress address, int x, int y)
 	{
 		synchronized (this) {
 			if (map[x][y] != null)
 				return false;
 			map[x][y] = unit;
 			//unit.setRunning(false);
 			unit.setPosition(x, y);
 			units.put(address, unit);
 		}
 		return true;
 	}
 
 	/**
 	 * Put a unit at the specified position. First, it
 	 * checks whether the position is empty, if not, it
 	 * does nothing.
 	 * 
 	 * @param unit is the actual unit being put 
 	 * on the specified position.
 	 * @param x is the x position.
 	 * @param y is the y position.
 	 * @return true when the unit has been put on the 
 	 * specified position.
 	 */
 	private synchronized boolean putUnit(Unit unit, int x, int y)
 	{
 		if (map[x][y] != null)
 			return false;
 
 		map[x][y] = unit;
 		unit.setPosition(x, y);
 
 		return true;
 	}
 
 	/**
 	 * Get a unit from a position.
 	 * 
 	 * @param x position.
 	 * @param y position.
 	 * @return the unit at the specified position, or return
 	 * null if there is no unit at that specific position.
 	 */
 	public Unit getUnit(int x, int y)
 	{
 		assert x >= 0 && x < map.length;
 		assert y >= 0 && x < map[0].length;
 
 		return map[x][y];
 	}
 
 	/**
 	 * Move the specified unit a certain number of steps.
 	 * 
 	 * @param unit is the unit being moved.
 	 * @param deltax is the delta in the x position.
 	 * @param deltay is the delta in the y position.
 	 * 
 	 * @return true on success.
 	 */
 	private synchronized boolean moveUnit(Unit tUnit, int newX, int newY)
 	{
 		int originalX = tUnit.getX();
 		int originalY = tUnit.getY();
 		Unit unit = map[originalX][originalY];
 		if(unit == null || !unit.equals(tUnit)) return false;
 		//if(!((Math.abs(unit.getX() - x) <= 1 && Math.abs(unit.getY() - y) == 0)|| (Math.abs(unit.getY() - y) <= 1 && Math.abs(unit.getX() - x) == 0))) return false;
 		//System.out.println(originalX + " " + originalY + ":");
 		if (unit.getHitPoints() <= 0)
 			return false;
 
 		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
 			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
 				if (map[newX][newY] == null) {
 					if (putUnit(unit, newX, newY)) {
 						map[originalX][originalY] = null;
 						return true;
 					}
 				}
 
 		return false;
 	}
 
 	/**
 	 * Remove a unit from a specific position and makes the unit disconnect from the server.
 	 * 
 	 * @param x position.
 	 * @param y position.
 	 */
 	private synchronized void removeUnit(int x, int y)
 	{
 		Unit unitToRemove = this.getUnit(x, y);
 		if (unitToRemove == null)
 			return; // There was no unit here to remove
 		map[x][y] = null;
 		units.remove(unitToRemove.getAddress());
 		unitToRemove.disconnect();
 
 	}
 
 	/**
 	 * Returns a new unique unit ID.
 	 * @return int: a new unique unit ID.
 	 */
 	public synchronized int getNewUnitID() {
 		return ++lastUnitID;
 	}
 
 	public Message onMessageReceived(Message msg) {
 
 		//System.out.println("MESSAGE RECEIVED:" + msg.get("request"));
 
 		//System.out.println("MESSAGE RECEIVED " + (MessageRequest)msg.get("request"));
 
 		if((Boolean)msg.get("sync") != null && (Boolean)msg.get("sync") == true) {
 			//System.out.println("SYNC MESSAGE RECEIVED " + (MessageRequest)msg.get("request"));
 			processSyncMessage(msg);
 
 		} else {
 			MessageRequest request = (MessageRequest)msg.get("request");
 			Message reply = null;
 			String origin = (String)msg.get("origin");
 			Unit unit;
 			Integer[] tempClock;
 			Message replyMessage;
 			LogEntry entry;
 			switch(request)
 			{
 			case disconnectedUnit:
 				Unit u;
 				synchronized (this) {
 					u = units.remove((InetSocketAddress)msg.get("unitAddress"));
 					if(u!=null) map[u.getX()][u.getY()] = null;
 				}
 				tempClock= ((Integer[])msg.get("vclock")).clone();
 				entry = new LogEntry(tempClock, LogEntryType.DISCONNECTED_UNIT, (InetSocketAddress)msg.get("serverAddress"));
 				logger.writeAsText(entry, true);
 				replyMessage = new Message();
 				replyMessage.put("request", MessageRequest.disconnectedUnitAck);
 				replyMessage.put("serverAddress", new InetSocketAddress(url, port));
 				SynchronizedClientSocket client = new SynchronizedClientSocket(replyMessage, (InetSocketAddress)msg.get("serverAddress"), this);
 				client.sendMessage();
 				break;
 
 			case disconnectedBF:
 				tempClock = ((Integer[])msg.get("vclock")).clone();
 				entry = new LogEntry(tempClock, LogEntryType.DISCONNECTED_BF, (InetSocketAddress)msg.get("serverAddress"));
 				logger.writeAsText(entry, true);
 				replyMessage = new Message();
 				replyMessage.put("request", MessageRequest.disconnectedBFAck);
 				replyMessage.put("serverAddress", new InetSocketAddress(url, port));
 				SynchronizedClientSocket syncClient = new SynchronizedClientSocket(replyMessage, (InetSocketAddress)msg.get("serverAddress"), this);
 				syncClient.sendMessage();
 				break;
 			case spawnUnit:
 			case moveUnit:
 			case dealDamage:
 			case healDamage:
 				syncActionWithBattlefields(msg);
 				break;
 			case requestBFList: {
 				reply = new Message();
 				reply.put("request", MessageRequest.replyBFList);
 				battlefields.put((InetSocketAddress)msg.get("bfAddress"), 0);
 				reply.put("bfList", battlefields);
 				return reply;
 			}
 
 			case replyBFList: {
 				HashMap<InetSocketAddress, Integer> bfList = (HashMap<InetSocketAddress, Integer>)msg.get("bfList");
 				for(InetSocketAddress address: bfList.keySet()) {
 					battlefields.put(address, 0);
 				}
 				for(InetSocketAddress address: battlefields.keySet()) {
 					SynchronizedClientSocket clientSocket;
 					Message message = new Message();
 					message.put("request", MessageRequest.addBF);
 					message.put("bfAddress", new InetSocketAddress(url, port));
 					clientSocket = new SynchronizedClientSocket(message,address, this);
 					clientSocket.sendMessage();
 				}
 				//System.out.println("BATTLEFIELDS:"+ bfList.toString());
 
 				//reply = new Message();
 				//HashSet bfList = (HashSet<InetSocketAddress>)msg.get("bfList");
 				//int y = (Integer)msg.get("y");
 				//reply.put("id", msg.get("id"));
 				return null;
 			}
 
 			case addBF: {
 				battlefields.put((InetSocketAddress)msg.get("bfAddress"), 0);
 				//System.out.println("ADD BF:"+ battlefields.toString());
 
 				return null;
 			}
 
 			//break;
 			//case removeUnit:
 			//this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));			
 			//if(syncBF(msg)) return null;
 
 			case SyncActionResponse: 
 				processResponseMessage(msg);
 
 				break;
 			case SyncActionConfirm:
 				return processConfirmMessage(msg);
 			}
 		}
 		return null;
 
 		//serverSocket.sendMessage(reply, origin);
 		/*
 		try {
 			if (reply != null)
 				serverSocket.sendMessage(reply, origin);
 		}
 		/*catch(IDNotAssignedException idnae)  {
 			// Could happen if the target already logged out
 		}*/
 	}
 
 	private Message processEvent(Message msg, ActionInfo removeAction) {
 		Unit unit = null;
 		LogEntry entry;
 		Integer[] tempClock;
 
 		switch ((MessageRequest)removeAction.message.get("request")) {
 
 		case spawnUnit: {
 			//System.out.println("BATTLE FIELD:Spawn" + port);
 			//System.out.println(battlefields.toString());
 
 			Boolean succeded = this.spawnUnit((Unit)msg.get("unit"), (InetSocketAddress)msg.get("address"), (Integer)msg.get("x"), (Integer)msg.get("y"));
 			if(succeded) {
 				units.put((InetSocketAddress)msg.get("address"), (Unit)msg.get("unit"));	
 			}
 			Message reply = new Message();
 			reply.put("request", MessageRequest.spawnAck);
 			reply.put("succeded", succeded);
 			reply.put("gamestate", map);
 			//Puts position of the unit we are sending to in the map we are sending
 			Unit u = units.get((InetSocketAddress)msg.get("address"));
 			reply.put("unit",  u);
 			
 			tempClock = ((Integer[])msg.get("vclock")).clone();
 			
 			entry = new LogEntry(tempClock, LogEntryType.SPAWN, (InetSocketAddress)msg.get("address"), new Position( (Integer)msg.get("x"),  (Integer)msg.get("y")));
 			logger.writeAsText(entry, true);
 			if(!((InetSocketAddress)msg.get("serverAddress")).equals(new InetSocketAddress(url, port))){
 				//System.out.println("<"+url+":"+port+"> Spawn will be processed --> "+toStringArray(tempClock));
 				vClock.updateClock(tempClock);
 			}
 			return reply;
 
 		}
 		case dealDamage: {
 
 			int x = (Integer)msg.get("x");
 			int y = (Integer)msg.get("y");
 			unit = this.getUnit(x, y);
 			if (unit != null) {
 				unit.adjustHitPoints( -(Integer)msg.get("damage") );
 				
 				Unit attackingUnit = (Unit)msg.get("unit");
 				//System.out.println(attackingUnit);
 				entry = new LogEntry((Integer[])msg.get("vclock"), LogEntryType.ATACK, (InetSocketAddress)msg.get("address"), new Position( attackingUnit.getX(),  attackingUnit.getY()), new Position(x,y), (Integer)msg.get("damage"));
 				logger.writeAsText(entry, true);
 				if(!((InetSocketAddress)msg.get("serverAddress")).equals(new InetSocketAddress(url, port))){
 					vClock.updateClock((Integer[])msg.get("vclock"));
 				}
 				
 				if(unit.getHitPoints() <= 0) {
 					removeUnit(x, y);
 					//Log remove unit
 					// Should we log with same clock as deal damage that cause it?
 					entry = new LogEntry(vClock.getClock(), LogEntryType.REMOVE, (InetSocketAddress)msg.get("address"), new Position( (Integer)msg.get("x"),  (Integer)msg.get("y")));
 					logger.writeAsText(entry, true);
 				}
 			}
 			break;
 		}
 		case healDamage:
 		{
 			int x = (Integer)msg.get("x");
 			int y = (Integer)msg.get("y");
 			unit = this.getUnit(x, y);
 			if (unit != null)
 				unit.adjustHitPoints( (Integer)msg.get("healed") );
 			/* Copy the id of the message so that the unit knows 
 			 * what message the battlefield responded to. 
 			 */
 			Unit attackingUnit = (Unit)msg.get("unit");
 
 			entry = new LogEntry((Integer[])msg.get("vclock"), LogEntryType.HEAL, (InetSocketAddress)msg.get("address"), new Position( attackingUnit.getX(),  attackingUnit.getY()), new Position( (Integer)msg.get("x"),  (Integer)msg.get("y")), (Integer)msg.get("healed"));
 			logger.writeAsText(entry, true);
 			if(!((InetSocketAddress)msg.get("serverAddress")).equals(new InetSocketAddress(url, port))){
 				vClock.updateClock((Integer[])msg.get("vclock"));
 			}
 			break;
 		}
 		case moveUnit:
 		{
 
 			//System.out.println("BATTLEFIELD: MOVEUNIT");
 			Unit tempUnit = units.get((InetSocketAddress)msg.get("address"));
 			int x = tempUnit.getX();
 			int y = tempUnit.getY();
 		
 			/*
 			if(temptUnit == null) {
 				System.out.println("NULL");
 			}*/
 
 			boolean move = this.moveUnit(tempUnit, (Integer)msg.get("x"), (Integer)msg.get("y"));
 			if(!move) System.out.println("MOVE CANCELED");
 
 			entry = new LogEntry((Integer[])msg.get("vclock"), LogEntryType.MOVE, (InetSocketAddress)msg.get("address"), new Position( x, y), new Position( (Integer)msg.get("x"),  (Integer)msg.get("y")));
 			logger.writeAsText(entry, true);
 
 			if(!((InetSocketAddress)msg.get("serverAddress")).equals(new InetSocketAddress(url, port))){
 				vClock.updateClock((Integer[])msg.get("vclock"));
 			}
 			/* Copy the id of the message so that the unit knows 
 			 * what message the battlefield responded to. 
 			 */
 			break;
 		}
 		default:
 			break;
 		}
 		return null;
 	}
 
 	private synchronized Message processConfirmMessage(Message msg) {
 		//Write to log;
 		
 		Integer messageID = (Integer)msg.get("serverMessageID");
 		//System.out.println("[S"+port+"] MessageID "+messageID+" Address "+(InetSocketAddress)msg.get("serverAddress")+"\nOutsideSize "+pendingOutsideActions.size()+"\n[S"+port+"]"+pendingOutsideActions);
 		ActionInfo removeAction = pendingOutsideActions.remove(new ActionID(messageID, (InetSocketAddress)msg.get("serverAddress")));			
 		if(removeAction != null) {
 			removeAction.timer.cancel();
 			//System.out.println("[S"+port+"] OutsideSize "+pendingOutsideActions.size()+" Confirm = "+(Boolean)msg.get("confirm")+" RemoveAction Request: "+removeAction.message.get("request"));
 			if((Boolean)msg.get("confirm")) processEvent(msg,removeAction);
 		}
 
 		return null;
 
 	}
 
 	private String toStringArray(Integer[] c){
 		String s = "[ ";
 		for( int i= 0; i< c.length; i++){
 			s+=  c[i];
 			s+=", "; 
 		}
 		s+= "]";
 		return s;}
 	
 	private synchronized Message processResponseMessage(Message msg) {
 		Integer messageID = (Integer)msg.get("serverMessageID");
 		ActionInfo actionInfo =  pendingOwnActions.get(messageID);
 		InetSocketAddress serverAddress = (InetSocketAddress)msg.get("serverAddress");
 
 		Message message = msg.clone();
 		message.put("request", MessageRequest.SyncActionConfirm);
 		message.put("serverAddress", new InetSocketAddress(url, port));
 		message.put("serverMessageID", messageID);
 
 		if(actionInfo != null) {
 			if((Boolean)msg.get("ack")) {
 				//System.out.println("[S"+port+"] "+actionInfo.message.get("address")+" ACK TRUE from "+serverAddress.getHostName()+":"+serverAddress.getPort()+" Adding info to queue.");
 				actionInfo.ackReceived.add((InetSocketAddress)msg.get("serverAddress")); 
 				if(actionInfo.ackReceived.size() == battlefields.size()-1) {
 					message.put("confirm", true);
 					Integer[] tempClock = vClock.incrementClock(id);
 					//System.out.println("<"+url+":"+port+"> Clock added when action is ready to ship --> "+toStringArray(tempClock));
 					message.put("vclock", tempClock);
 					for(InetSocketAddress address : actionInfo.ackReceived) {
 						SynchronizedClientSocket clientSocket = new SynchronizedClientSocket(message, address, this);
 						clientSocket.sendMessage();
 					}
 					ActionInfo removeAction = pendingOwnActions.remove(messageID);
 					removeAction.timer.cancel();
 					msg.put("vclock", tempClock);
 					Message toPlayer = processEvent(message, removeAction);
 					if(toPlayer!=null) {
 						SynchronizedClientSocket clientSocket = new SynchronizedClientSocket(toPlayer, (InetSocketAddress)msg.get("address"), this);
 						clientSocket.sendMessage();
 					}
 				}
 			} else {
 				pendingOwnActions.remove(messageID).timer.cancel();
 				message.put("confirm", false);
 				SynchronizedClientSocket clientSocket = new SynchronizedClientSocket(message, serverAddress, this);
 				clientSocket.sendMessage();
 
 			}
 
 		} else {
 			message.put("confirm", false);
 			SynchronizedClientSocket clientSocket = new SynchronizedClientSocket(message, serverAddress, this);
 			clientSocket.sendMessage();
 		}
 		return null;
 
 	}
 
 	private synchronized void processSyncMessage(Message msg) {
 
 		MessageRequest request = (MessageRequest)msg.get("request");
 		Integer messageID = (Integer)msg.get("serverMessageID");
 		//InetSocketAddress originAddress = (InetSocketAddress)msg.get("address");
 		InetSocketAddress serverAddress = (InetSocketAddress)msg.get("serverAddress");
 		Integer x = (Integer)msg.get("x");
 		Integer y = (Integer)msg.get("y");
 
 		msg.put("sync", (Boolean)false);
 
 		//System.out.println("[S"+port+"] Process Sync Message from "+serverAddress.getPort()+"\n Message "+request.name()+" with X="+x+"|Y="+y);
 
 		boolean conflictFound = false; 
 		Set<InetSocketAddress> toRemoveTemp = new HashSet<InetSocketAddress>();
 		switch(request) {
 		case spawnUnit: 
 			if (getUnit(x, y) == null){
 				for(ActionInfo info : pendingOwnActions.values()){
 
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit || actionType == MessageRequest.spawnUnit){
 						if(x.equals((Integer)info.message.get("x")) && y.equals((Integer)info.message.get("y"))) {
 							//sendActionAck(msg, false, messageID, originAddress);
 							conflictFound = true;
 							break;
 						} 
 					}
 				}
 				for(ActionInfo info : pendingOutsideActions.values()){
 
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit || actionType == MessageRequest.spawnUnit){
 						if(x.equals((Integer)info.message.get("x")) && y.equals((Integer)info.message.get("y"))) {
 							//sendActionAck(msg, false, messageID, originAddress);
 							conflictFound = true;
 							break;
 						} 
 					}
 				}
 
 			}
 			else {
 				conflictFound = true;
 			}
 
 			if(conflictFound) {
 				sendActionAck(msg, false, messageID, serverAddress);
 			} else {
 				addPendingOutsideAction(msg, messageID, serverAddress);
 				sendActionAck(msg, true, messageID, serverAddress);
 			}
 
 			break;
 
 		case moveUnit:
 			if (getUnit(x, y) == null){
 				
 				Unit unit = units.get((InetSocketAddress)msg.get("address"));
 
 				if(!((Math.abs(unit.getX() - x) <= 1 && Math.abs(unit.getY() - y) == 0)|| (Math.abs(unit.getY() - y) <= 1 && Math.abs(unit.getX() - x) == 0))) {
 					conflictFound = true;
 				}
 
 				for(ActionInfo info : pendingOwnActions.values()){
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit || actionType == MessageRequest.spawnUnit){
 						if(x.equals((Integer)info.message.get("x")) && y.equals((Integer)info.message.get("y"))) {
 							conflictFound = true;
 							break;
 						} 
 					} else if(actionType == MessageRequest.healDamage || actionType == MessageRequest.dealDamage) {
 						if(unit.getX().equals((Integer)info.message.get("x")) && unit.getY().equals((Integer)info.message.get("y"))) {
 							toRemoveTemp.add((InetSocketAddress)info.message.get("serverAddress"));
 						}
 					}
 				}
 				for(ActionInfo info : pendingOutsideActions.values()){
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit || actionType == MessageRequest.spawnUnit){
 						if(x.equals((Integer)info.message.get("x")) && y.equals((Integer)info.message.get("y"))) {
 							conflictFound = true;
 							break;
 						} 
 					} 
 				}
 
 			}
 			else {
 				conflictFound = true;
 			}
 
 			if(conflictFound) {
 				sendActionAck(msg, false, messageID, serverAddress);
 			} else {
 				for(InetSocketAddress addressToRemove : toRemoveTemp) {
 					ActionInfo info = pendingOwnActions.remove(addressToRemove);
 					if(info!=null)info.timer.cancel();
 				}
 				addPendingOutsideAction(msg, messageID, serverAddress);
 				sendActionAck(msg, true, messageID, serverAddress);
 			}
 
 			break;
 
 		case dealDamage:
 		case healDamage: 
 			if (getUnit(x, y) != null) {
 				for(ActionInfo info : pendingOwnActions.values()){
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit){
 						Unit infoUnit = units.get((InetSocketAddress)info.message.get("address"));
 						if(x.equals(infoUnit.getX()) && y.equals(infoUnit.getY())) {
 							conflictFound = true;
 							break;
 						} 
 					}
 				} for(ActionInfo info : pendingOutsideActions.values()){
 					MessageRequest actionType = (MessageRequest)info.message.get("request");
 					if(actionType == MessageRequest.moveUnit){
 						Unit infoUnit = units.get((InetSocketAddress)info.message.get("address"));
 						if(x.equals(infoUnit.getX()) && y.equals(infoUnit.getY())) {
 							conflictFound = true;
 							break;
 						} 
 					}
 				}
 			}
 			else {
 				conflictFound = true;
 			}
 
 			if(conflictFound) {
 				sendActionAck(msg, false, messageID, serverAddress);
 			} else {
 				for(InetSocketAddress addressToRemove : toRemoveTemp) {
 					pendingOwnActions.remove(addressToRemove).timer.cancel();
 				}
 				addPendingOutsideAction(msg, messageID, serverAddress);
 				sendActionAck(msg, true, messageID, serverAddress);
 			}
 			break;
 
 		}
 	}
 
 	private void sendActionAck(Message message ,boolean valid, Integer messageID, InetSocketAddress address) {
 
 		Message toSend = new Message();
 		toSend = message.clone();
 		toSend.put("request", MessageRequest.SyncActionResponse);
 		toSend.put("serverAddress", (InetSocketAddress)new InetSocketAddress(url,port));
 		toSend.put("ack", (Boolean)valid);
 
 		SynchronizedClientSocket socket = new SynchronizedClientSocket(toSend, address, this);
 		socket.sendMessage();
 	}
 
 	/**
 	 * 
 	 * @param message
 	 * @return true if message is already a sync message, or false if the if it was not a sync message and it was propagated.
 	 */
 	private boolean syncBF(Message message){
 		for (InetSocketAddress address : battlefields.keySet()) {
 			if(address.equals(new InetSocketAddress(url, port))) continue;
 			message.put("sync", (Boolean)true);
 			String s = "[S"+port+"] SENDING SYNC MESSAGE\nBefore change: "+message.get("address")+"\nAfter Change: ";
 			message.put("address", new InetSocketAddress(url,port));
 			s+= message.get("address");
 			//System.out.println(s);
 			//System.out.println("####################");
 			SynchronizedClientSocket clientSocket;
 			clientSocket = new SynchronizedClientSocket(message, address, this);
 			clientSocket.sendMessage();
 			//messageList.put(message, 0);
 		}
 		return false;
 	}
 
 
 	private void addPendingOutsideAction(Message message, Integer messageID, InetSocketAddress originAddress) {
 		Timer timer = new Timer();
 		//System.out.println("Adding to OUTSIDE ACTION | Message type: "+message.get("request"));
 		ActionID actionID = new ActionID(messageID, originAddress);
 		pendingOutsideActions.put(actionID, new ActionInfo(message, timer, false));
 		timer.schedule(new ScheduledTask(this, actionID), timeout);
 	}
 
 	public synchronized void syncActionWithBattlefields(Message message) {
 		Timer timer = new Timer();
 		pendingOwnActions.put(++localMessageCounter, new ActionInfo(message, timer, true));
 		sendSyncMessage(message);
 		timer.schedule(new ScheduledTask(this, localMessageCounter), timeout);
 	}
 
 	private void sendSyncMessage(Message message){
 		SynchronizedClientSocket clientSocket;
 		message.put("sync", (Boolean)true);
 		message.put("serverAddress", new InetSocketAddress(url, port));
 		message.put("serverMessageID", localMessageCounter);
 		for (InetSocketAddress address : battlefields.keySet()) {
 			if(address.equals(new InetSocketAddress(url, port))) continue;
 			clientSocket = new SynchronizedClientSocket(message, address, this);
 			clientSocket.sendMessage();
 		}
 	}
 
 	public InetSocketAddress getAddress() {
 		return new InetSocketAddress(url, port);
 	}
 
 	/**
 	 * Close down the battlefield. Unregisters
 	 * the serverSocket so the program can 
 	 * actually end.
 	 */ 
 	public synchronized void shutdown() {
 		// Remove all units from the battlefield and make them disconnect from the server
 		/*for (Unit unit : units) {
 			unit.disconnect();
 			unit.stopRunnerThread();
 		}*/
 
 		//serverSocket.unRegister();
 	}
 
 	private class ScheduledTask extends TimerTask implements Runnable {
 		private BattleField handler;
 		private boolean outsideAction;
 		private ActionID id;
 		private int idInt;
 		//private InetSocketAddress destinationAddress;
 
 		//Outside Action
 		ScheduledTask(BattleField handler, ActionID id){
 			this.outsideAction = true;
 			this.handler = handler;
 			this.id = id;
 		}
 
 		//InsideAction
 		ScheduledTask(BattleField handler, int id){
 			this.outsideAction = false;
 			this.handler = handler;
 			this.idInt = id;
 		}
 		@Override
 		public void run() {
 			System.out.println("TIME OUT");
 			//handler.checkBFFailures(destinationAddress);
 			if(outsideAction) {
 				handler.pendingOutsideActions.remove(id);
 			} else {
 				handler.pendingOwnActions.remove(idInt);
 			}			
 		}
 	}
 	
 	private void synchronizeWithAllBF(Message messageToSend) {
 
 		//SyncLog syncLog = new SyncLog();
 		SynchronizedClientSocket syncClientSocket;
 
 		for(InetSocketAddress address : battlefields.keySet()) {
 			if (address.getHostName().equals(url) && address.getPort() == port) continue;
 			syncClientSocket = new SynchronizedClientSocket(messageToSend, address, this);
 			syncClientSocket.sendMessage();
 		}
 
 		/*
 		//Assume that it always gets a response from at least one of the GS
 		if(gridSchedulersList.size() > 1) 
 			syncLog.check();
 */
 	}
 
 	public Message onExceptionThrown(Message message, InetSocketAddress destinationAddress) {
 		
 		boolean gsAvailable = checkBFFailures(destinationAddress);
 		MessageRequest request = (MessageRequest)message.get("request");
 		
 		switch (request) {
 		case disconnectedBF:
 			if(gsAvailable) return message;
 			break;
 		case disconnectedUnit:
 			if(gsAvailable) return message;
 			break;
 		case gameState:
 			Unit u;
 			synchronized (this) {
 				u = units.remove(destinationAddress);
 				map[u.getX()][u.getY()] = null;
 			}
 			Integer[] tempClock = vClock.incrementClock(id);
 			LogEntry entry = new LogEntry(tempClock, LogEntryType.DISCONNECTED_UNIT, destinationAddress);
 			logger.writeAsText(entry, true);
 			Message replyMessage = new Message();
 			replyMessage.put("request", MessageRequest.disconnectedUnit);
 			replyMessage.put("unitAddress", destinationAddress);
 			replyMessage.put("vclock", tempClock);
 			
 			synchronizeWithAllBF(replyMessage);
 			break;
 		default:
 			break;
 		}
 		return null;
 	}
 
 	private class ActionID {
 		public Integer messageId; //Action Message ID
 		public InetSocketAddress address; //Server responsible for action
 		public ActionID(Integer messageId, InetSocketAddress address) {
 			this.messageId = messageId;
 			this.address = address;
 		}
 
 		public String toString() {
 			return "["+messageId+" "+address+"]";
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			return address.equals(((ActionID)o).address) && messageId.equals(((ActionID)o).messageId) ;
 		}
 
 
 		@Override
 		public int hashCode() {
 			return address.hashCode() + messageId.hashCode() ;
 		}
 
 	}
 
 	private class ActionInfo {
 		public Message message;
 		public Timer timer;
 		public Queue<InetSocketAddress> ackReceived;
 		public ActionInfo(Message message, Timer timer, boolean activateQueue) {
 			this.message = message;
 			this.timer = timer;
 			if(activateQueue)
 				this.ackReceived = new ConcurrentLinkedQueue<InetSocketAddress>();
 			else 
 				this.ackReceived = null;
 		}
 	}
 	
 	private synchronized boolean checkBFFailures(InetSocketAddress destinationAddress) {
 		Integer failures = battlefields.get(destinationAddress);
 		if(failures != null) {
 			if (failures > 1) {
 				battlefields.remove(destinationAddress);
 				//Remove all Units connected to this battleField
 				for( Map.Entry<InetSocketAddress, Unit> entry : units.entrySet()) {
 					if(entry.getValue().getBattlefieldAddress().equals(destinationAddress)){
 						removeUnit(entry.getValue().getX(), entry.getValue().getY());
 					}
 					
 				}
 				Message message = new Message();
 				message.put("request", MessageRequest.disconnectedBF);
 				message.put("serverAddress", new InetSocketAddress(url, port));
 				message.put("vclock", vClock.incrementClock(id));
 				synchronizeWithAllBF(message);
 				return false;
 			}
 			else {
 				battlefields.put(destinationAddress,failures+1);
 			}
 		}
 		return false;
 	}
 
 	private void generateDragons(int numberOfDragons) {
 		/* All the dragons connect */
 		for(int i = 0; i < numberOfDragons; i++) {
 			/* Try picking a random spot */
 			int x, y, attempt = 0;
 			do {
 				x = (int)(Math.random() * BattleField.MAP_WIDTH);
 				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
 				attempt++;
 			} while (this.getUnit(x, y) != null && attempt < 10);
 
 			// If we didn't find an empty spot, we won't add a new dragon
 			if (this.getUnit(x, y) != null) break;
 			
 			final int finalX = x;
 			final int finalY = y;
 
 			/* Create the new dragon in a separate
 			 * thread, making sure it does not 
 			 * block the system.
 			 */
 			final int temp = i;
 			
 			new Thread(new Runnable() {
 				public void run() {
 					new Dragon(finalX, finalY,"localhost", port + temp+1, "localhost", port);
 				}
 			}).start();
 
 		}	
 	
 	}
 	
 	private void generatePlayeres(int numberOfPlayers) {
 		for(int i = 0; i < numberOfPlayers; i++)
 		{
 			/* Once again, pick a random spot */
 			int x, y, attempt = 0;
 			do {
 				x = (int)(Math.random() * BattleField.MAP_WIDTH);
 				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
 				attempt++;
 			} while (this.getUnit(x, y) != null && attempt < 10);
 
 			// If we didn't find an empty spot, we won't add a new player
 			if (this.getUnit(x, y) != null) break;
 
 			final int finalX = x;
 			final int finalY = y;
 			//System.out.println("CORE:" + finalX + " " +  finalY);
 
 			/* Create the new player in a separate
 			 * thread, making sure it does not 
 			 * block the system.
 			 */
 			final int temp = i;
 			new Thread(new Runnable() {
 				public void run() {
 					//TODO Ports have to be different for each player even when only connecting to different battlefields
 					//Now I'm just worried about all of them having different ports
 					new Player(finalX, finalY,"localhost", port + temp+100, "localhost", port);
 				}
 			}).start();	
 		}
 	}
 
 	
 	public static void main(String[] args) {
 
 		String usage = "Usage: BattleField <id> <hostname> <port> [<otherBFHostname> <otherBFPort> [-r]]";
 
 		if(args.length != 3 && args.length != 5 && args.length != 6) {
 			System.out.println(usage);
 			System.exit(1);
 		}
 		
 		BattleField bf = null;
 
 		if(args.length==6) {
 			if(args[5].equals("-r")) {
 				try {
 					System.out.println("Launching BattleField in RESTART mode.");
 					bf = new BattleField(
 							Integer.parseInt(args[0]), 
 							args[1],
 							Integer.parseInt(args[2]),
 							args[3],
 							Integer.parseInt(args[4]),
 							true);
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.out.println(usage);
 					System.exit(1);
 				}
 			} else {
 				System.out.println(usage);
 				System.exit(1);
 			}
 		}
 
 		else if(args.length==5) {
 			try {
 				System.out.println("Launching BattleField in NORMAL mode.");
 				bf = new BattleField(
 						Integer.parseInt(args[0]), 
 						args[1],
 						Integer.parseInt(args[2]),
 						args[3],
 						Integer.parseInt(args[4]),
 						false);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println(usage);
 				System.exit(1);
 			}
 		}
 		
 		else if(args.length==3) {
 			try {
 				System.out.println("Launching BattleField in NORMAL mode.");
 				bf = new BattleField(
 						Integer.parseInt(args[0]), 
 						args[1],
 						Integer.parseInt(args[2]),
 						false);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println(usage);
 				System.exit(1);
 			}
 		} else {
 			
 			bf = null;
 		}
 		
 		final BattleField otherBf = bf;
 		new Thread(new Runnable() {
 			public void run() {
 				new BattleFieldViewer(otherBf);
 			}
 		}).start();
 		
 		System.out.println("Press ENTER to start generating units");
 		try {
 			System.in.read();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//Number Dragons, Number Players
 		bf.startExecution(2, 2);
 
 	}
 }
