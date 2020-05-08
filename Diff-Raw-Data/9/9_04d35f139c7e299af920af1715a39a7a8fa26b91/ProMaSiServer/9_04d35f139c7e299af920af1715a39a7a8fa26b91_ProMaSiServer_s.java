 package org.promasi.server;
 
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.naming.ConfigurationException;
 
 import org.joda.time.DateTime;
 import org.promasi.game.multiplayer.MultiPlayerGame;
 import org.promasi.network.tcp.ITcpServerListener;
 import org.promasi.network.tcp.TcpClient;
 import org.promasi.network.tcp.TcpServer;
 import org.promasi.protocol.messages.CancelGameResponse;
 import org.promasi.protocol.messages.GameCanceledRequest;
 import org.promasi.protocol.messages.LeaveGameResponse;
 import org.promasi.protocol.messages.UpdateGameListRequest;
 import org.promasi.protocol.messages.UpdateGamePlayersListRequest;
 import org.promasi.server.clientstate.LoginClientState;
 import org.promasi.utilities.exceptions.NullArgumentException;
 
 /**
  * 
  * @author m1cRo
  *
  */
 public class ProMaSiServer implements ITcpServerListener
 {
 	/**
 	 * 
 	 */
 	private TcpServer _server;
 	
 	/**
 	 * 
 	 */
 	private Map<String, ProMaSiClient> _clients;
 	
 	/**
 	 * 
 	 */
 	private Map<DateTime, ProMaSiClient> _connectedClients;
 	
 	/**
 	 * 
 	 */
 	private Map<String, MultiPlayerGame> _availableGames;
 	
 	/**
 	 * 
 	 */
 	private Thread _loginCheckThread;
 	
 	/**
 	 * 
 	 * @param portNumber
 	 * @throws IllegalArgumentException
 	 */
 	public ProMaSiServer(int portNumber)throws IllegalArgumentException, ConfigurationException{
 		if(portNumber<0){
 			throw new IllegalArgumentException("Wrong argument portNumber==null");
 		}
 		
 		_server=new TcpServer();
 		try {
 			_server.addListener(this);
 		} catch (NullArgumentException e) {
 			throw new ConfigurationException("TcpServer configuration failed");
 		}
 		if(!_server.start(portNumber)){
 			throw new IllegalArgumentException("Wrong argument portNumber");
 		}
 		
 		_clients=new TreeMap<String, ProMaSiClient>();
 		_connectedClients=new TreeMap<DateTime, ProMaSiClient>();
 		
 		_loginCheckThread=new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while(_server.isRunning()){
 					synchronized(ProMaSiServer.this){
 						Map<DateTime,ProMaSiClient> connectedClients=new TreeMap<DateTime, ProMaSiClient>();
 						for(Map.Entry<DateTime, ProMaSiClient > entry : _connectedClients.entrySet()){
 							if(entry.getKey().plusSeconds(60).isBefore(new DateTime())){
 								entry.getValue().disconnect();
 							}else{
 								connectedClients.put(entry.getKey(), entry.getValue());
 							}
 						}
 						
 						_connectedClients=connectedClients;
 					}
 					
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						//Logger
 					}
 				}
 
 			}
 		});
 		
 		_availableGames=new TreeMap<String, MultiPlayerGame>();
 		_loginCheckThread.start();
 	}
 
 	@Override
 	public synchronized void serverStarted(int portNumber) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public synchronized void serverStopped() {
 		for(Map.Entry<String, ProMaSiClient> entry : _clients.entrySet()){
 			entry.getValue().disconnect();
 		}
 		
 		for(Map.Entry<DateTime, ProMaSiClient > entry : _connectedClients.entrySet()){
 			entry.getValue().disconnect();
 		}
 		
 		_clients.clear();
 		_connectedClients.clear();
 	}
 
 	@Override
 	public synchronized void clientConnected(TcpClient client) {
 		try {
 			ProMaSiClient pClient=new ProMaSiClient(client,new LoginClientState(this));
 			_connectedClients.put(new DateTime(),pClient);
 		} catch (NullArgumentException e) {
 			//Logger
 		}
 	}
 
 	@Override
 	public synchronized void clientDisconnected( TcpClient client) {
 		if(_connectedClients.containsKey(client)){
 			_connectedClients.remove(client);
 		}
 		
 		Map<String, ProMaSiClient> clients=new TreeMap<String, ProMaSiClient>();
 		for(Map.Entry<String, ProMaSiClient> entry : _clients.entrySet()){
 			try {
 				if(!entry.getValue().equals(client)){
 					clients.put(entry.getKey(), entry.getValue());
 				}
 			} catch (NullArgumentException e) {
 				//Logger
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param userId
 	 * @return
 	 * @throws NullArgumentException
 	 */
 	public synchronized boolean login(String userId, ProMaSiClient client)throws NullArgumentException{
 		if(userId==null){
 			throw new NullArgumentException("Wrong argument userId==null");
 		}
 		
 		if(client==null){
 			throw new NullArgumentException("Wrong argument client==null");
 		}
 		
 		if(_clients.containsKey(userId)){
 			return false;
 		}
 		
 		DateTime loginTime=null;
 		for(Map.Entry<DateTime, ProMaSiClient> entry : _connectedClients.entrySet()){
 			if(entry.getValue()==client){
 				loginTime=entry.getKey();
 				break;
 			}
 		}
 		
 		if(loginTime!=null && _connectedClients.containsKey(loginTime)){
 			_connectedClients.remove(loginTime);
 		}
 		
 		_clients.put(userId, client);
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param clientId
 	 * @param game
 	 * @return
 	 * @throws NullArgumentException
 	 */
 	public synchronized boolean createGame(String clientId, MultiPlayerGame game)throws NullArgumentException{
 		if(clientId==null){
 			throw new NullArgumentException("Wrong argument clientId==null");
 		}
 		
 		if(game==null){
 			throw new NullArgumentException("Wrong argument game==null");
 		}
 		
 		if( _availableGames.containsKey(game.getGameName()) ) {
 			return false;
 		}
 		
 		_availableGames.put(game.getGameName(), game);
 		for(Map.Entry<String, ProMaSiClient> entry : _clients.entrySet()){
 			try{
 				sendUpdateGamesListRequest(entry.getValue());
 			}catch(NullArgumentException e){
 				//Logger
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param clientId
 	 * @return
 	 * @throws NullArgumentException
 	 */
 	public synchronized boolean startGame(String clientId, String gameId)throws NullArgumentException{
 		if(clientId==null){
 			throw new NullArgumentException("Wrong argument clientId==null");
 		}
 		
 		if(gameId==null){
 			throw new NullArgumentException("Wrong argument gameId==null");
 		}
 		
 		if(_availableGames.containsKey(gameId)){
 			if(!_availableGames.get(gameId).startGame(clientId)){
 				return false;
 			}
 			
 			_availableGames.remove(gameId);
 			for(Map.Entry<String, ProMaSiClient> entry : _clients.entrySet()){
 				try{
 					sendUpdateGamesListRequest(entry.getValue());
 				}catch(NullArgumentException e){
 					//Logger
 				}
 			}
 		}else{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public synchronized boolean isRunning(){
 		return _server.isRunning();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public synchronized Map<String, String> getAvailableGames(){
 		Map<String, String> games=new TreeMap<String, String>();
 		for(Map.Entry<String, MultiPlayerGame> entry : _availableGames.entrySet()){
 			games.put(entry.getKey(), entry.getValue().getGameDescription());
 		}
 		
 		return games;
 	}
 	
 	/**
 	 * 
 	 * @param clientId
 	 * @param gameId
 	 * @return
 	 * @throws NullArgumentException
 	 * @throws IllegalArgumentException
 	 */
 	public synchronized MultiPlayerGame joinGame(String clientId, String gameId)throws NullArgumentException, IllegalArgumentException{
 		if(gameId==null){
 			throw new NullArgumentException("Wrong argument gameId==null");
 		}
 		
 		if(clientId==null){
 			throw new NullArgumentException("Wrong argument clientId==null");
 		}
 		
 		if(!_clients.containsKey(clientId)){
 			throw new IllegalArgumentException("Wrong argument clientId");
 		}
 		
 		if(!_availableGames.containsKey(gameId)){
 			throw new NullArgumentException("Wrong argument gameId");
 		}
 		
 		if( _availableGames.get(gameId).joinGame(clientId) ) {
 			return _availableGames.get(gameId);
 		}else{
 			throw new IllegalArgumentException("Wrong argument gameId");
 		}
 	}
 	
 	
 	private void sendUpdateGamesListRequest(ProMaSiClient client)throws NullArgumentException{
 		if(client==null){
 			throw new NullArgumentException("Wrong argument client==null");
 		}
 		
 		Map<String, String> gamesList=new TreeMap<String, String>();
 		for(Map.Entry<String, MultiPlayerGame> entry : _availableGames.entrySet()){
 			gamesList.put(entry.getKey(), entry.getValue().getGameDescription());
 		}
 		
 		UpdateGameListRequest request=new UpdateGameListRequest(gamesList);
 		client.sendMessage(request.serialize());
 	}
 	
 	/**
 	 * 
 	 * @param gameId
 	 * @throws NullArgumentException
 	 */
	public synchronized void cancelGame(String gameId) throws NullArgumentException{
 		if(gameId==null){
 			throw new NullArgumentException("Wrong argument gameId==null");
 		}
 		
 		if(_availableGames.containsKey(gameId)){
 			MultiPlayerGame game=_availableGames.get(gameId);
 			List<String> gamePlayers=game.getGamePlayers();
 			for(String playerId : gamePlayers){
 				if(playerId!=game.getGameOwnerId() && _clients.containsKey(playerId)){
 					_clients.get(playerId).sendMessage(new GameCanceledRequest().serialize());
 				}
 			}
 			
 			if(_clients.containsKey(game.getGameOwnerId())){
 				_clients.get(game.getGameOwnerId()).sendMessage(new CancelGameResponse().serialize());
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param gameId
 	 * @param clientId
 	 * @throws NullArgumentException
 	 */
 	public synchronized void leaveGame(String gameId, String clientId) throws NullArgumentException{
 		if(gameId==null){
 			throw new NullArgumentException("Wrong argument gameId==null");
 		}
 		
 		if(clientId==null){
 			throw new NullArgumentException("Wrong argument clientId==null");
 		}
 		
 		if(_availableGames.containsKey(gameId)){
 			List<String> gamePlayers=_availableGames.get(gameId).getGamePlayers();
 			if(gamePlayers.contains(clientId) && _clients.containsKey(clientId)){
 				_clients.get(clientId).sendMessage(new LeaveGameResponse().serialize());
 				gamePlayers.remove(clientId);
 				for(String player : gamePlayers){
 					if(_clients.containsKey(player)){
 						_clients.get(clientId).sendMessage(new UpdateGamePlayersListRequest(gamePlayers).serialize());
 					}
 				}
 			}
 		}
 	}
 }
