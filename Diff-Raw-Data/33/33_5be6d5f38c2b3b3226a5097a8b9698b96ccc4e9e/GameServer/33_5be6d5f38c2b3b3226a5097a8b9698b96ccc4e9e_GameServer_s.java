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
 import java.util.HashMap;
 
 import com.ahsgaming.valleyofbones.*;
 import com.ahsgaming.valleyofbones.network.KryoCommon.AddAIPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisterPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RegisteredPlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.RemovePlayer;
 import com.ahsgaming.valleyofbones.network.KryoCommon.StartGame;
 import com.ahsgaming.valleyofbones.screens.GameSetupScreen.GameSetupConfig;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Net;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.net.HttpParametersUtils;
 import com.badlogic.gdx.utils.*;
 import com.badlogic.gdx.utils.StringBuilder;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 /**
  * @author jami
  *
  */
 public class GameServer implements NetController {
 	public String LOG = "GameServer";
     String globalServerUrl = (VOBGame.DEBUG_GLOBAL_SERVER ? "http://localhost:4730" : "http://secure-caverns-9874.herokuapp.com");
 
     final VOBGame game;
 
 	Server server, broadcastServer;
 	GameController controller;
 	
 	GameSetupConfig gameConfig;
 
 	Array<Player> players = new Array<Player>();
 	ObjectMap<Connection, NetPlayer> connMap = new ObjectMap<Connection, NetPlayer>();
 	int nextPlayerId = 0;
 	Connection host = null;
 
     boolean loadGame = false;
 	
 	boolean stopServer = false;
 	
 	boolean gameStarted = false;
 
     GameResult gameResult;
 
     boolean endTurnRecd;
 
     int publicServerId = -1;
 
     float serverPing = 1800;
     float serverPingTimeout = 0;
 	
 	/**
 	 * 
 	 */
 	public GameServer(final VOBGame game, GameSetupConfig cfg) {
 		this.game = game;
 		gameConfig = cfg;
 
         init();
     }
 
     public void reset() {
         if (server != null) server.close();
         if (broadcastServer != null) broadcastServer.close();
         controller = null;
         loadGame = false;
         gameStarted = false;
         nextPlayerId = 0;
         players = new Array<Player>();
         connMap = new ObjectMap<Connection, NetPlayer>();
         host = null;
         stopServer = false;
         gameResult = null;
         removePublicServer();
 
         init();
     }
 
     public void init() {
 		// setup the KryoNet server
 		server = new Server();
 		KryoCommon.register(server);
 		
 		broadcastServer = new Server();
 		
 		try {
 			server.bind(gameConfig.hostPort);
 			server.start();
 		} catch (Exception ex) {
             Gdx.app.log(LOG, "Server failed to start");
 			Gdx.app.log(LOG, ex.getMessage());
 			stopServer = true;
             return;
 		}
 
         try {
             broadcastServer.bind(0, KryoCommon.udpPort);
             broadcastServer.start();
         } catch (IOException ex) {
             Gdx.app.log(LOG, ex.getMessage());
         }
 		
 		
 		
 		server.addListener(new Listener() {
 
 			public void received(Connection c, Object obj) {
 				
 				if (obj instanceof RegisterPlayer) {
 					RegisterPlayer rp = (RegisterPlayer)obj;
 
                    if (rp.version != VOBGame.VERSION) {
                        Gdx.app.log("version", "" + VOBGame.VERSION);
                        Gdx.app.log("rp.version", "" + rp.version);
                         // wrong version!
                         server.sendToTCP(c.getID(), new KryoCommon.VersionError());
                         return;
                     }
 					
 					if (players.size >= Player.AUTOCOLORS.length) {
                         server.sendToTCP(c.getID(), new KryoCommon.GameFullError());
                         return; // TODO should join as spectator?
                     }
 					
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
 					} else if (obj instanceof Command) {
 						Command cmd = (Command)obj;
 						if (cmd.owner != connMap.get(c).getPlayerId()) cmd.owner = connMap.get(c).getPlayerId();
 
                         // discard past, future, invalid, or duplicated commands
 						if (cmd.turn == controller.getGameTurn() && !controller.getCommandQueue().contains(cmd, false) && controller.validate(cmd)) {
 							if (cmd instanceof EndTurn) {
                                 endTurnRecd = true;
                             } else {
                                 controller.queueCommand(cmd);
                                 server.sendToAllTCP(cmd);
                             }
 						}
 					}
 				} else {
                     if (obj instanceof StartGame) {
                         if (c == host) loadGame = true;
                     }
                 }
 			}
 			
 			public void connected (Connection c) {
 				if (host == null || !host.isConnected()) host = c;
 				
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
                         int i = 0;
                         p = null;
                         while (i < players.size && (p == null || p instanceof AIPlayer)) {
                             p = players.get(i);
                             i++;
                         }
                         if (p instanceof AIPlayer) {
                             reset();
                             return;
                         }
                         host = connMap.findKey(p, true);
                         RegisteredPlayer reg = new RegisteredPlayer();
                         reg.id = p.getPlayerId();
                         reg.name = p.getPlayerName();
                         reg.color = p.getPlayerColor();
                         reg.team = p.getTeam();
                         reg.host = true;
                         server.sendToTCP(host.getID(), reg);
                     }
 				}
 
                 if (controller == null || controller.getGameResult() == null) sendPlayerList();
 			}
 		});
 
         if (gameConfig.isPublic) {
             registerPublicServer();
             serverPingTimeout = serverPing;
         }
 	}
 	
 	public void startGame() {
 		// the host clicked startGame --> send out the StartGame message so that the clients load and report
 		controller = new GameController(gameConfig.mapName, players);
 		controller.LOG = controller.LOG + "#Server";
 
         // TODO allow configuration of who goes first?
         controller.setCurrentPlayer(players.get((MathUtils.random(players.size - 1))));
         loadGame = false;
 
 		// don't need to broadcast on UDP anymore - thats just confusing
 		broadcastServer.close();
 		
 		sendStartGame();
 	}
 	
 	public void endGame() {
 		// the controller has a game result --> broadcast it to everybody and close the server
 		GameResult result = controller.getGameResult();
 		controller.setState(GameStates.GAMEOVER);
 //        game.setGameResult(result);
         gameResult = result;
 
 		Gdx.app.log(LOG, String.format("GameResult: winner: %d; Losers: (%d)", result.winner, result.losers.length));
 		
 		server.sendToAllTCP(result);
 		server.close();
 
         if (gameConfig.isPublic) {
             sendPublicServerResult();
         }
 	}
 
 	public void stop() {
 		server.close();
 
         removePublicServer();
 	}
 
 	public boolean update(float delta) {
 
         if (!gameStarted && gameConfig.isPublic) {
             serverPingTimeout -= delta;
             if (serverPingTimeout <= 0) {
                 serverPingTimeout = serverPing;
                 sendPublicServerUpdate();
             }
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
                 endTurnRecd = false;
                 if (gameConfig.isPublic) sendPublicServerUpdate();
 			}
 		}
 
 		if (gameStarted) {
 			controller.update(delta);
 
             if (isEndTurn())
             {
                 EndTurn endTurn = new EndTurn(controller.getCommandQueue());
                 endTurn.turn = controller.getGameTurn();
                 endTurn.owner = controller.getCurrentPlayer().getPlayerId();
                 server.sendToAllTCP(endTurn);
                 controller.setNextTurn(true);
                 controller.doTurn();
                 endTurnRecd = false;
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
 		if (gameConfig.isPublic) sendPublicServerUpdate();
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
 
         return endTurnRecd;
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
 		StartGame startGame = new StartGame();
         startGame.currentPlayer = controller.getCurrentPlayer().getPlayerId();
         server.sendToAllTCP(startGame);
 	}
 
 	@Override
 	public void addAIPlayer(int team) {
 		if (players.size < Player.AUTOCOLORS.length) {
 			int id = getNextPlayerId();
 			Color use = Player.getUnusedColor(players);
 			
 			players.add(new AIPlayer(this, id, "AI Player", use, team));
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
     public void sendAICommand(Command cmd) {
         if (!controller.getCommandQueue().contains(cmd, false) && controller.validate(cmd)) {
             if (cmd instanceof EndTurn) {
                 endTurnRecd = true;
             } else {
                 controller.queueCommand(cmd);
                 server.sendToAllTCP(cmd);
             }
         }
     }
 
 	@Override
 	public boolean isConnected() {
 		return false;
 	}
 
 	@Override
 	public boolean isConnecting() {
 		return false;
 	}
 
     public boolean isLoadGame() {
         return loadGame;
     }
 
     public boolean isGameStarted() {
         return gameStarted;
     }
 
     public boolean isStopServer() {
         return stopServer;
     }
 
     public void setStopServer(boolean stop) {
         stopServer = stop;
     }
 
     public GameResult getGameResult() {
         return gameResult;
     }
 
     public GameSetupConfig getGameConfig() {
         return gameConfig;
     }
 
     public void registerPublicServer() {
         Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.POST);
         httpGet.setUrl(String.format("%s/server", globalServerUrl));
 
         HashMap parameters = new HashMap();
         parameters.put("name", gameConfig.hostName);
         parameters.put("port", Integer.toString(gameConfig.hostPort));
 //        parameters.put("ip", Utils.getIPAddress(true));
 
         httpGet.setContent(HttpParametersUtils.convertHttpParameters(parameters));
 
         Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
             @Override
             public void handleHttpResponse(Net.HttpResponse httpResponse) {
                 String response = httpResponse.getResultAsString();
                 switch(httpResponse.getStatus().getStatusCode()) {
                     case 400:
                     case 404:
                         Gdx.app.log(LOG, "Failed to register with global server");
                         Gdx.app.log(LOG, response);
                         break;
                     case 200:
                         Gdx.app.log(LOG, "Registered with global server");
                         Gdx.app.log(LOG, response);
                         JsonReader reader = new JsonReader();
                         JsonValue result = reader.parse(response);
                         if (result != null)
                             publicServerId = result.getInt("id", -1);
                         Gdx.app.log(LOG, Integer.toString(publicServerId));
                         break;
                     default:
                         Gdx.app.log(LOG, String.format("Unknown HTTP Status Code: %d", httpResponse.getStatus().getStatusCode()));
                         Gdx.app.log(LOG, response);
                 }
             }
 
             @Override
             public void failed(Throwable t) {
                 Gdx.app.log(LOG, "Failed to register with global server");
                 t.printStackTrace();
             }
         });
     }
 
     public void removePublicServer() {
         if (publicServerId >= 0) {
             Net.HttpRequest httpDelete = new Net.HttpRequest(Net.HttpMethods.DELETE);
             httpDelete.setUrl(String.format("%s/server/%d", globalServerUrl, publicServerId));
             Gdx.net.sendHttpRequest(httpDelete, new Net.HttpResponseListener() {
                 @Override
                 public void handleHttpResponse(Net.HttpResponse httpResponse) {
                     Gdx.app.log(LOG, httpResponse.getResultAsString());
                 }
 
                 @Override
                 public void failed(Throwable t) {
                     t.printStackTrace();
                 }
             });
         }
 
         publicServerId = -1;
     }
 
     public void sendPublicServerResult() {
         if (publicServerId >= 0) {
             Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
 //            httpPost.setHeader("Content-Type", "application/json");
             httpPost.setUrl(String.format("%s/game", globalServerUrl));
             HashMap parameters = new HashMap();
 
 //            parameters.put("players",
 //                    String.format("{ %d: \"%s\", %d: \"%s\" }",
 //                            controller.getPlayers().get(0).getPlayerId(),
 //                            controller.getPlayers().get(0).getPlayerName(),
 //                            controller.getPlayers().get(1).getPlayerId(),
 //                            controller.getPlayers().get(1).getPlayerName()));
             String historyString = "[";
             for (Command command: controller.getCommandHistory()) {
                 historyString += command.toJson() + ",";
             }
             historyString += "]";
             parameters.put("game", "{ \"history\": " + historyString + ", \"result\": " + gameResult.winner + "}");
            parameters.put("version", Integer.toString(VOBGame.VERSION));
 //            parameters.put("result", String.format("%d", gameResult.winner));
 
             httpPost.setContent(HttpParametersUtils.convertHttpParameters(parameters));
 
             Gdx.net.sendHttpRequest(httpPost, new Net.HttpResponseListener() {
                 @Override
                 public void handleHttpResponse(Net.HttpResponse httpResponse) {
                     String response = httpResponse.getResultAsString();
 
                     switch(httpResponse.getStatus().getStatusCode()) {
                         case 400:
                         case 404:
                             Gdx.app.log(LOG, "Failed to upload game result");
                             Gdx.app.log(LOG, response);
                             break;
                         case 200:
                             Gdx.app.log(LOG, "Uploaded game result");
                             Gdx.app.log(LOG, response);
                             break;
                         default:
                             Gdx.app.log(LOG, "Unknown response code " + httpResponse.getStatus().getStatusCode());
                             Gdx.app.log(LOG, response);
                     }
                 }
 
                 @Override
                 public void failed(Throwable t) {
                     t.printStackTrace();
                 }
             });
 
             Gdx.app.log(LOG, httpPost.getContent());
         }
     }
 
     public void sendPublicServerUpdate() {
         Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.POST);
         req.setUrl(String.format("%s/server/%d", globalServerUrl, publicServerId));
         HashMap parameters = new HashMap();
         parameters.put("players", "" + players.size);
         Gdx.app.log("GameStarted", "" + gameStarted);
         parameters.put("status", "" + (gameStarted ? 1 : 0));
         req.setContent(HttpParametersUtils.convertHttpParameters(parameters));
         Gdx.app.log("LOG", req.getContent());
         Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
             @Override
             public void handleHttpResponse(Net.HttpResponse httpResponse) {
                 Gdx.app.log(LOG, "Update success");
                 Gdx.app.log(LOG, httpResponse.getResultAsString());
             }
 
             @Override
             public void failed(Throwable t) {
                 Gdx.app.log(LOG, "Update failed");
             }
         });
     }
 }
