 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones.network;
 
 import java.io.IOException;
 
 import com.ahsgaming.valleyofbones.AIPlayer;
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.GameResult;
 import com.ahsgaming.valleyofbones.GameStates;
 import com.ahsgaming.valleyofbones.NetPlayer;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.network.KryoCommon.AddAIPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisterPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisteredPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RemovePlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.StartGame;
 import com.ahsgaming.valleyofbones.screens.GameSetupScreen.GameSetupConfig;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 /**
  * @author jami
  *
  */
 public class GameServer implements NetController {
 	public String LOG = "GameServer";
 	
 	Server server, broadcastServer;
 	GameController controller;
 	
 	GameSetupConfig gameConfig;
 	
 	Array<Player> players = new Array<Player>();
 	ObjectMap<Connection, NetPlayer> connMap = new ObjectMap<Connection, NetPlayer>();
 	int nextPlayerId = 0;
 	Connection host = null;
 	
 	boolean stopServer = false;
 	
 	boolean gameStarted = false;
 
     boolean[] endTurnRecd;
 	
 	/**
 	 * 
 	 */
 	public GameServer(final VOBGame game, GameSetupConfig cfg) {
 		
 		gameConfig = cfg;
 		// setup the KryoNet server
 		server = new Server();
 		KryoCommon.register(server);
 		
 		broadcastServer = new Server();
 		
 		try {
 			server.bind(KryoCommon.tcpPort);
 			server.start();
 			
 			broadcastServer.bind(0, KryoCommon.udpPort);
 			broadcastServer.start();
 		} catch (IOException ex) {
 			Gdx.app.log(VOBGame.LOG + ":GameServer", ex.getMessage());
 			Gdx.app.exit();
 		}
 		
 		
 		
 		server.addListener(new Listener() {
 			
 			public void received(Connection c, Object obj) {
 				
 				if (obj instanceof RegisterPlayer) {
 					RegisterPlayer rp = (RegisterPlayer)obj;
 					
 					if (players.size >= Player.AUTOCOLORS.length) return; // TODO should join as spectator? 
 					
 					// is that player already registered?
 					for (Player p: players) {
 						if (p.getPlayerName() == rp.name) return; // TODO maybe drop the connection?
 					}
 					
 					int id = getNextPlayerId();
 					Color use = new Color(1, 1, 1, 1);
 					int team = -1;
 					if (players.size <= 4) {
 						use = Player.getUnusedColor(players);
 						int cntTeam1 = 0, cntTeam2 = 0;
 						for (Player p: players) {
 							if (p.getTeam() == 0) cntTeam1++;
 							if (p.getTeam() == 1) cntTeam2++;
 						}
 						
 						if (cntTeam1 > cntTeam2) {
 							team = 1;
 						} else {
 							team = 0;
 						}
 					}
 
 					NetPlayer p = new NetPlayer(id, rp.name, use, team);
 					
 					players.add(p);
 					connMap.put(c, p);
 					
 					RegisteredPlayer reg = new RegisteredPlayer();
 					reg.id = p.getPlayerId();
 					reg.name = p.getPlayerName();
 					reg.color = p.getPlayerColor();
 					reg.team = p.getTeam();
                     reg.host = (c == host);
 					server.sendToTCP(c.getID(), reg);
 					sendPlayerList();
 					sendSetupInfo();
 				}
 				
 				if (obj instanceof AddAIPlayer) {
 					// TODO implement teams/player limits more robustly
 					// make sure this is from the host
 					if (c != host) return;
 					
 					addAIPlayer(((AddAIPlayer)obj).team);
 					
 					sendPlayerList();
 				}
 				
 				if (obj instanceof RemovePlayer) {
 					// make sure this is from the host
 					if (c != host) return;
 					
 					removePlayer(((RemovePlayer)obj).id);
 					
 					sendPlayerList();
 				}
 				
 				if (obj instanceof KryoCommon.GameDetails) {
 					if (c != host) return;
 					
 					if (controller == null) {
 						gameConfig.mapName = ((KryoCommon.GameDetails)obj).mapName;
 						sendSetupInfo();
 					}
 				}
 				
 				// TODO need to check this for validity
 				if (controller != null) {
 					// the player represented by this connection is ready
 					if (obj instanceof StartGame) {
 						connMap.get(c).setReady(true);
 					} else if (obj instanceof EndTurn) {
                         endTurnRecd[players.indexOf(connMap.get(c), true)] = true;
                     } else if (obj instanceof Command) {
 						Command cmd = (Command)obj;
 						if (cmd.owner != connMap.get(c).getPlayerId()) cmd.owner = connMap.get(c).getPlayerId();
 
                        // discard past, future, invalid, or duplicated commands
						if (cmd.turn == controller.getGameTurn() && !controller.getCommandQueue().contains(cmd, false) && controller.validate(cmd)) {
 							controller.queueCommand(cmd);
 							server.sendToAllTCP(cmd);
 						}
 					}
 				} else {
                     if (obj instanceof StartGame) {
                         if (c == host) game.setLoadGame();
                     }
                 }
 			}
 			
 			public void connected (Connection c) {
 				if (host == null) host = c;
 				
 				if (gameStarted) c.close();
 			}
 			
 			public void disconnected (Connection c) {
 				Player p = connMap.get(c);
 				if (players.contains(p, true)) {
                     players.removeValue(p, true);
                     connMap.remove(c);
                 }
 
 				if (host == c && !gameStarted) {
 				    // find a new host
                     if (players.size > 0) {
                         p = players.get(0);
                         host = connMap.findKey(p, true);
                         RegisteredPlayer reg = new RegisteredPlayer();
                         reg.id = p.getPlayerId();
                         reg.name = p.getPlayerName();
                         reg.color = p.getPlayerColor();
                         reg.team = p.getTeam();
                         reg.host = true;
                         server.sendToTCP(c.getID(), reg);
                     }
 				}
 
                 sendPlayerList();
 			}
 		});
 		
 	}
 	
 	public void startGame() {
 		// the host clicked startGame --> send out the StartGame message so that the clients load and report
 		controller = new GameController(gameConfig.mapName, players);
 		controller.LOG = controller.LOG + "#Server";
 		
 		// don't need to broadcast on UDP anymore - thats just confusing
 		broadcastServer.close();
 		
 		sendStartGame();
 	}
 	
 	public void endGame() {
 		// the controller has a game result --> broadcast it to everybody and close the server
 		GameResult result = controller.getGameResult();
 		controller.setState(GameStates.GAMEOVER);
 		
 		Gdx.app.log(LOG, String.format("GameResult: winner: %d; Losers: (%d)", result.winner, result.losers.length));
 		
 		server.sendToAllTCP(result);
 		server.close();
 	}
 	
 	public void stop() {
 		stopServer = true;
 	}
 
 	public boolean update(float delta) {
 		if (stopServer) {
 			server.stop();
 			return false;
 		}
 		
 		if ((controller == null)) return true;
 		
 		// once the controller exists, we need to check to make sure all players are ready
 		if (!gameStarted) {
 			boolean allReady = true;
 			for (int p=0;p<players.size;p++) {
 				if (players.get(p) instanceof NetPlayer && !((NetPlayer)players.get(p)).isReady()) {
 					allReady = false;
 				}
 			}
 			
 			// once all are ready, we need to unpause the game
 			if (allReady) {
 				Unpause up = new Unpause();
 				up.owner = -1;
 				up.turn = controller.getGameTurn();
 				// important to both send the command out and add to the local queue!
 				server.sendToAllTCP(up);
 				controller.queueCommand(up);
 				gameStarted = true;
                 endTurnRecd = new boolean[players.size];
 			}
 		}
 
 		if (gameStarted) {
 			controller.update(delta);
 
             if (isEndTurn()) {
                 server.sendToAllTCP(new EndTurn(controller.getCommandQueue()));
                 controller.doTurn();
             }
 		}
 		
 
         if (controller.getGameResult() != null) {
             endGame();
             return false;
         }
 
 		return true;
 	}
 	
 	public void sendPlayerList() {
 		RegisteredPlayer[] list = new RegisteredPlayer[players.size];
 		for (int p = 0; p < players.size; p++) {
 			RegisteredPlayer rp = new RegisteredPlayer();
 			Player pl = players.get(p);
 			rp.id = pl.getPlayerId();
 			rp.name = pl.getPlayerName();
 			rp.color = pl.getPlayerColor();
 			rp.team = pl.getTeam();
 			list[p] = rp;
 		}
 		
 		server.sendToAllTCP(list);
 	}
 	
 	public void sendSetupInfo() {
 		KryoCommon.GameDetails si = new KryoCommon.GameDetails();
 		si.mapName = gameConfig.mapName;
 		server.sendToAllTCP(si);
 	}
 	
 	protected int getNextPlayerId() {
 		int id = nextPlayerId;
 		nextPlayerId += 1;
 		return id;
 	}
 
     public boolean isEndTurn() {
         if (controller != null) {
             if (controller.getTurnTimer() <= 0) return true;
         }
 
         for (int i=0; i<endTurnRecd.length; i++) {
             if (!(endTurnRecd[i] || (players.size > i && players.get(i) instanceof AIPlayer))) return false;
         }
 
         for (int i=0; i<endTurnRecd.length; i++) {
             endTurnRecd[i] = false;
         }
         return true;
     }
 
 	@Override
 	public void setGameController(GameController controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public GameController getGameController() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void sendStartGame() {
 		server.sendToAllTCP(new StartGame());
 	}
 
 	@Override
 	public void addAIPlayer(int team) {
 		if (players.size < 4) {
 			int id = getNextPlayerId();
 			Color use = Player.getUnusedColor(players);
 			
 			players.add(new AIPlayer(id, "AI Player", use, team));
 		}
 	}
 
 	@Override
 	public void removePlayer(int playerId) {
 		
 		Array<Player> remove = new Array<Player>();
 		for (Player p: players) {
 			if (p.getPlayerId() == playerId) {
 				remove.add(p);
 			}
 		}
 		
 		players.removeAll(remove, true);
 	}
 
 	@Override
 	public Array<Player> getPlayers() {
 		return this.players;
 	}
 
     @Override
     public Player getPlayer() {
         return null;
     }
 
 	@Override
 	public void sendCommand(Command cmd) {
 		server.sendToAllTCP(cmd);
 	}
 
 	@Override
 	public boolean isConnected() {
 		return false;
 	}
 
 	@Override
 	public boolean isConnecting() {
 		return false;
 	}
 }
