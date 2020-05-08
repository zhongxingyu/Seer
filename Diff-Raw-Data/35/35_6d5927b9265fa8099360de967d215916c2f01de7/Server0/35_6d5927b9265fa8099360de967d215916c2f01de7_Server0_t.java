 package org.aimas.craftingquest.core;
 
 import gnu.cajo.invoke.Remote;
 import gnu.cajo.utils.ItemServer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.SwingUtilities;
 
 import org.aimas.craftingquest.gui.GraphicInterface;
import org.aimas.craftingquest.state.CraftedObject;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * 
  * @author Razvan
  */
 public class Server0 implements IServer {
 
 	/* communication */
 	private String servername = "CraftingQuest";
 	private int portNumber = 1198;
 	
 	//private List<Object> clients;
 	private Object[] clients;
 	private boolean guiOn = false;
 	private GraphicInterface displayer;
 	private int connectedClients = 0;
 	private boolean[] unresponsive;
 	
 	/* game */
 	private GameState state;
 	private ActionEngine actionEngine;
 	private Timer timer;
 	private long[] secrets;
 	private long gameStartTime = 0;
 
 	/* logging */
 	private static Logger logger = Logger.getLogger(Server0.class);
 	private static Logger gui_logger = Logger.getLogger("org.aimas.craftingquest.core.guilogger");
 	
 	public Server0(String servername, int portNumber, String secretsFile) throws Exception {
 		this.servername = servername;
 		this.portNumber = portNumber;
 		
 		state = GameGenerator.setupGame();
 		actionEngine = new ActionEngine(state);
 		timer = new Timer();
 		secrets = readSecrets(secretsFile);
 		
 		if (secrets == null) {
 			throw new Exception("could not retrieve secrets");
 		}
 		
 		if(state.playerStates.size() != secrets.length){
 		    throw new Exception("each client must have a secret");
 		}
 		
 		clients = new Object[secrets.length];
 		unresponsive = new boolean[secrets.length];
 		for (int i = 0; i < unresponsive.length; i++) {
 			unresponsive[i] = false;
 		}
 		
 		if (System.getProperty("gui") != null) {
 			guiOn = Boolean.parseBoolean(System.getProperty("gui"));
 		}
 		
 		if (guiOn) {
 			displayer = new GraphicInterface(state);
 			SwingUtilities.invokeLater(new Runnable() {
 				
 				@Override
 				public void run() {
 					displayer.setVisible(true);
 				}
 			});
 		}
 		
 	}
 
 	public static void main(String[] args) throws Exception {
 		// args are servername, port, secrets file
 		Server0 server = null;
 		if(args == null || args.length != 3) {
 			server = new Server0("CraftingQuest", 1198, "secrets.txt");
 		}
 		else {
 			if (args != null && args.length == 3) {
 				String serverName = args[0];
 				int port = Integer.parseInt(args[1]);
 				String secretsFileName = args[2];
 				
 				server = new Server0(serverName, port, secretsFileName);
 			}
 		}
 		
 		// configure logger
 		PropertyConfigurator.configure("logging.properties");
 		
 		Remote.config(null, server.getPortNumber(), null, 0);
 		ItemServer.bind(server, server.getServername());
 
 		server.gameLoop();
 	}
 
 	void sendEvent(int clientID, Object client, Event event) {
 		try {
 			if (!unresponsive[clientID]) {
 				Remote.invoke(client, "onEvent", event);
 			}
 		} catch (Exception ex) {
 			// mark client as unresponsive
 			unresponsive[clientID] = true;
 			logger.fatal("Could not send event to client " + clientID, ex);
 		}
 	}
 
 	
 	/* communication */
 	public synchronized int addRemoteClient(Object client) {
 		Long secret = null;
 		try {
 			secret = (Long) Remote.invoke(client, "getSecret", 0);
 		} catch (Exception ex) {
 			logger.error("Failed adding remote client. Could not invoke getSecret", ex);
 		}
 		
 		int clientNo = allowedSecret(secret);
 		
 		if(clientNo == -1){
 		    logger.warn("[srv][register] - newclient not authorized: secret = " + secret);
 		    return -1;
 		}
 		
 		logger.info("[srv][register] - newclient_" + secret);
 		if (clients[clientNo] == null) {
 		    clients[clientNo] = client;
 		    connectedClients++;				// increase number of connected players 
 		}
 		
 		return clientNo;	
 	}
 	
 	void updateGame() {
 		// check if we still have a connected client
 		boolean responsiveClients = false;
 		for (int i = 0; i < unresponsive.length; i++) {
 			if (!unresponsive[i]) {
 				responsiveClients = true;
 				break;
 			}
 		}
 		
 		if (!responsiveClients) {
 			System.exit(10);
 		}
 		
 		// increment turn number
 		state.round.currentRound++;
 		
 		long roundStartTime = GamePolicy.connectWaitTime + state.round.currentRound * GamePolicy.roundTime;
 		state.round.startTime = roundStartTime;
 		
 		if (guiOn) {
 			// update graphics
 			SwingUtilities.invokeLater(new Runnable() {
 				
 				@Override
 				public void run() {
 					displayer.updateState();
 				}
 			});
 		}
 	}
 
 	void endGame() {
 		//for (Object client : clients) {
 		for (int cID = 0; cID < clients.length; cID++) {
 			sendEvent(cID, clients[cID], new Event(Event.EventType.GameEnd));
 		}
 		// Remote.shutdown();
 		
 		declareWinner();
 		System.exit(0);
 	}
 
 	public void gameLoop() {
 		long delay = 200;
 		long delayPlayer = GamePolicy.playerTotalTime;
 		long period = GamePolicy.roundTime;
 		int connectWaitTime = GamePolicy.connectWaitTime;
 		int initializationWaitTime = GamePolicy.initializationWaitTime;
 		state.round.startTime = GamePolicy.connectWaitTime;
 		
 		try {
 			Thread.sleep(connectWaitTime);
 		} catch (InterruptedException ex) {
 			logger.fatal("ConnectWait interrupted", ex);
 		}
 
 		if (connectedClients != clients.length) {
 			logger.info("Insufficient number of clients connected. Expected=" + clients.length + 
 					", connected=" + connectedClients + ". Game will end!");
 			System.exit(1);
 		}
 		
 		// initialization period
 		for (int cID = 0; cID < clients.length; cID++) {
 			Object client = clients[cID];
 			sendEvent(cID, client, new Event(Event.EventType.Initialization));
 		}
 		
 		try {
 			Thread.sleep(initializationWaitTime);		// wait for the specified initialization period
 		} catch (InterruptedException ex) {
 			logger.fatal("InitializationWait interrupted", ex);
 		}
 		
 		// mark beginning of game
 		gameStartTime = System.currentTimeMillis() + delay;		// first action will commence after delay milliseconds
 		
 		// player time
 		for (int i = 0; i < clients.length; i++) {
 			final int clientID = i;
 			timer.scheduleAtFixedRate(new TimerTask() {
 
 				//Object client = clients.get(clientID);
 				Object client = clients[clientID];
 
 				@Override
 				public void run() {
 					// replenish player's units with their energy supplies
 					actionEngine.replenishEnergy();
 					
 					// set round status: currentRound and roundStartTime
 					Integer playerID = state.getPlayerIds().get(clientID);
 					PlayerState player = state.playerStates.get(playerID);
 					int currentRound = state.round.currentRound;
 					
 					player.round.currentRound = currentRound;
 					player.round.startTime = GamePolicy.connectWaitTime + 
 						currentRound * GamePolicy.roundTime + clientID * GamePolicy.playerActionTime;
 					
 					// then subtract specific energy amount if the player is near some towers
 					actionEngine.doTowerDrain(state, state.getPlayerIds().get(clientID));
 					
 					// send the NEW ROUND event to current client
 					sendEvent(clientID, client, new Event(Event.EventType.NewRound));
 					
 					// send the END ROUND event to all the others
 					for (int cID = 0; cID < clients.length; cID++) {
 						if (cID != clientID) {
 							sendEvent(cID, clients[cID], new Event(Event.EventType.EndRound));
 						}
 					}
 				}
 			}, delay + delayPlayer * i, period);
 		}
 
 		// update time
 		timer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				updateGame();
 			}
 		}, delay + period - GamePolicy.updateTime, period);
 
 		// close
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				timer.cancel();
 				endGame();
 			}
 		}, GamePolicy.roundTime * GamePolicy.lastTurn);
 
 	}
 
 	public synchronized PlayerState process(Transition action) {
 		// Logger2.log("srv", "process", "");
 		int clientID = allowedSecret(action.secret);
 		if (clientID == -1) {
 			return null;
 		}
 		
 		Integer playerID = state.getPlayerIds().get(clientID);
 		PlayerState player = state.playerStates.get(playerID);
 		
 		if (action.operator == ActionType.PlayerReady) {
 			printToGuiLog(player, action);
 			return player;
 		}
 		
 		if (!allowedPlayer(clientID)) {
 			return null;
 		}
 		
 		logger.info("[srv]Executing action [" + action.operator.name() + "] for client: " + clientID);
 		
 		if (player.round.currentRound < state.round.currentRound) {
 			player.round.currentRound = state.round.currentRound;
 			player.round.startTime = GamePolicy.connectWaitTime + 
 				state.round.currentRound * GamePolicy.roundTime + clientID * GamePolicy.playerActionTime;
 		}
 		
 		player.response = actionEngine.process(player, action);
 		printToGuiLog(player, action);
 		
 		return player;
 	}
 
 	public String getServername() {
 		return servername;
 	}
 
 	public int getPortNumber() {
 		return portNumber;
 	}
 	
 	private boolean allowedPlayer(int clientID) {
 		int factor = clientID % 2;
 		long currentTime = System.currentTimeMillis();
 		int currentRound = state.round.currentRound;
 		
 		long diff = currentTime - gameStartTime - currentRound * GamePolicy.roundTime;
 		//System.out.println("diff for allowedPlayer is: " + diff);
 		
 		if (diff >= factor * GamePolicy.playerTotalTime && diff < (factor + 1) * GamePolicy.playerTotalTime) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private int allowedSecret(long secret) {
 		for (int i = 0; i < secrets.length; i++) {
 			if (secret == secrets[i]) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private long[] readSecrets(String fileName) {
 		long[] secrets = null;
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(fileName));
 			int len = Integer.parseInt(br.readLine());
 			secrets = new long[len];
 			
 			for (int i = 0; i < len; i++) {
 				secrets[i] = Long.parseLong(br.readLine());
 			}
 			
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return secrets;
 	}
 	
 	private void declareWinner() {
 		List<Integer> playerIDs = state.getPlayerIds();
 		int winnerClient = 0;
 		int maxCredit = state.playerStates.get(playerIDs.get(winnerClient)).credit;
 		boolean tied = true;
 		
 		for (int clientID = 1; clientID < clients.length; clientID++) {
 			Integer pId = playerIDs.get(clientID);
 			int pCredit = state.playerStates.get(pId).credit;
 			
 			if (pCredit > maxCredit) {
 				maxCredit = pCredit;
 				winnerClient = clientID;
 				tied = false;
 			}
 			else {
 				if (pCredit < maxCredit) {
 					tied = false;
 				}
 			}
 		}
 		
 		if (!tied) { 
 			logger.info("Winner is: " + winnerClient);
 			try {
 				BufferedWriter bw = new BufferedWriter(new FileWriter("winner.txt"));
 				bw.write("winner");		// print winner tag
 				bw.newLine();			// then print winner clientID, identifying secret, credit
 				bw.write("" + winnerClient + " " + secrets[winnerClient] + " " + maxCredit);
 				bw.newLine();
 				bw.write("runner-up");
 				bw.newLine();
 				for (int clientID = 0; clientID < clients.length; clientID++) {
 					if (clientID != winnerClient) {
 						Integer pId = playerIDs.get(clientID);
 						bw.write("" + clientID + " " + secrets[clientID] + " " + state.playerStates.get(pId).credit);
 						bw.newLine();
 					}
 				}
 				
 				bw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			logger.info("Teams are tied at " + maxCredit + " credits");
 			try {
 				BufferedWriter bw = new BufferedWriter(new FileWriter("winner.txt"));
 				bw.write("tied");
 				bw.write("" + maxCredit);
 				bw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void printToGuiLog(PlayerState player, Transition action) {
 		if (player.validLastTransition()) {
 			// first check if the operator is of Nothing or RequestState type
 			// these are just for filling up and synchronization - so return
 			if(action.operator == ActionType.Nothing || action.operator == ActionType.RequestState) {
 				return;
 			}
 			
 			if (action.operator == ActionType.PlayerReady) {
 				for (UnitState u : player.units) {
					gui_logger.info(state.round.currentRound + " " + action.operator.name() + " " + player.id + " " + u.type.name() + " " 
							+ u.pos.x + " " + u.pos.y + " " + player.credit + " " + u.energy);
 				}
 				
 				return;
 			}
 			
 			Integer unitID = (Integer)action.operands[0];
 			
 			UnitState playerUnit = null;
 			for (UnitState u : player.units) {
 				if (u.id == unitID && u.playerID == player.id) {
 					playerUnit = u;
 					break;
 				}
 			}
 			
 			if (playerUnit == null) {
 				return;
 			}
 			
			if (action.operator == ActionType.CraftObject || action.operator == ActionType.SellObject) {
				CraftedObject target = (CraftedObject)action.operands[1];
				gui_logger.info(state.round.currentRound + " " + action.operator.name() + " " + player.id + " " + playerUnit.type.name() + " " 
						+ playerUnit.pos.x + " " + playerUnit.pos.y + " " + player.credit + " " + playerUnit.energy + " " 
						+ target.getType().name() + " " + target.getValue());
			}
			else {
				gui_logger.info(state.round.currentRound + " " + action.operator.name() + " " + player.id + " " + playerUnit.type.name() + " " 
						+ playerUnit.pos.x + " " + playerUnit.pos.y + " " + player.credit + " " + playerUnit.energy);
			}
 		}
 	}
 }
