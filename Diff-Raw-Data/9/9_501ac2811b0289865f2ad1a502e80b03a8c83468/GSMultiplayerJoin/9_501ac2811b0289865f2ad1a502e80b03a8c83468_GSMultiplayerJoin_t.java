 package game;
 
 import network.NetworkClient;
 import network.NetworkManager;
 import network.NetworkSocket;
 import std.StdDraw;
 import gamestate.GameStates;
 import gamestate.GlobalGameState;
 import gamestate.IGameState;
 
 public class GSMultiplayerJoin implements IGameState {
 
 	NetworkClient m_client;
 	NetworkSocket m_socket;
 
 	@Override
 	public void onEnter() {
 		System.out.println("CLIENT");
 	
 		m_client = new NetworkClient();
		m_client.connectTo("176.198.16.52", 4444);
 	}
 
 	@Override
 	public void onExit() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render() {
 		StdDraw.text(400,300, "Joining. Please wait...");
 
 	}
 
 	@Override
 	public void update() {
 		if( !m_client.isConnected() ){
 			return;
 		}
 		m_socket = m_client.getNetworkSocket();
 		
 		NetworkManager.listenTo(m_socket);
 		
 		GSGame.getInstance().startGameMulti(false);
 		GlobalGameState.setActiveGameState(GameStates.GAME);
 	}
 
 }
