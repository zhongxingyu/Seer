 package edu.brown.cs32.MFTG.tournament;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 import edu.brown.cs32.MFTG.gui.MonopolyGui;
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer;
 import edu.brown.cs32.MFTG.networking.InvalidRequestException;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class HumanClient extends Client{
 
 	private MonopolyGui _gui;
 	private ExecutorService _executor = Executors.newCachedThreadPool();
 	private Timer _timer;
 	private Map<Integer,String> _playerNames;
 
 	public HumanClient(boolean music){
 		super();
 		_gui = new MonopolyGui(this, music);
 		_playerNames = new HashMap<>();
 	}
 	
 	/**
 	 * Connects the Client to a socket
 	 * @throws IOException
 	 */
	public void run(){		
 		try {
 			_server = new Socket(_host, _port);
 			_input = new BufferedReader(new InputStreamReader(_server.getInputStream()));
 			_output = new BufferedWriter(new OutputStreamWriter(_server.getOutputStream()));
 		} catch (UnknownHostException e) {
 			displayMessage("Unknown host. Unable to connect to server :( WHAT THE FUCK, MAN!!!");
 			return;
 		} catch (IOException e) {
 			displayMessage("Unable to connect to server :(");
 			return;
 		}
 		try {
 			_id = respondToSendID();
 			_gui.createBoard(_id, _gui.getCurrentProfile(), this);
 		} catch (IOException | InvalidRequestException e1) {
 			displayMessage("Unable to retrieve a unique ID from the server :(");
 			return;
 		}
 		Callable<Void> worker = new RequestCallable(this);
 		_executor.submit(worker);
 	}
 
 	/***************Networking Methods*************************/
 
 	/**
 	 * 
 	 * @param the request which is being responded to
 	 */
 	protected void respondToDisplayError(ClientRequestContainer request){
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// throw an error
 		} else if (arguments.size() < 1){
 			// throw a different error
 		}
 
 		displayMessage(arguments.get(0));
 	}
 
 	/**
 	 * Responds to a request sent over the server to display game data by displaying the data received
 	 * @param the request which is being responded to
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	protected void respondToDisplayData(ClientRequestContainer request) throws JsonParseException, JsonMappingException, IOException{
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// error
 		} else if (arguments.size() < 1){
 			// error
 		}
 
 		GameDataReport gameDataReport = _oMapper.readValue(arguments.get(0), GameDataReport.class);
 
 		displayGameData(gameDataReport);
 	}
 
 	/*******************************************************/
 
 	/**
 	 * Set and display the combined GameData at the end of a set
 	 * @param combinedData
 	 */
 	public void displayGameData(GameDataReport combinedData) {
 		/* update records */
 		_gui.getCurrentProfile().getRecord().addSet(combinedData.getPlayerWithMostWins() == _id);
 		for(Integer i : combinedData._winList.values()){
 			_gui.getCurrentProfile().getRecord().addGame(i == _id);
 		}
 		
 		/* take care of end of game business */
 		if(combinedData._matchIsOver){
 			finishMatch(combinedData);
 		}
 		
 		/* update the board */
 		sendDataToGui(combinedData);
 		_gui.getBoard().setWinnerData(combinedData._playerWins, _playerNames);
 	}
 	
 	/**
 	 * Called by the game runnables to update GameData info
 	 * when they finish playing
 	 * @param gameData
 	 */
 	public synchronized void addGameData(GameData gameData){		
 		List<GameData> dataList = new ArrayList<>();
 		dataList.add(gameData);
 		GameDataAccumulator newData = DataProcessor.aggregate(dataList,BackendConstants.NUM_DATA_POINTS);
 		
 		_data = _data == null ? newData :
 			DataProcessor.combineAccumulators(_data, DataProcessor.aggregate(dataList,BackendConstants.NUM_DATA_POINTS));
 		
 		/* display data and determine the next display point */
 		_numGamesPlayed++;
 		if(_numGamesPlayed >= _nextDisplaySize){
 			sendDataToGui(_data.toGameDataReport());
 			_nextDisplaySize += BackendConstants.DATA_PACKET_SIZE;
 		}
 	}
 	
 	/**
 	 * Starts the timer for choosing heuristics, after which the player will be retrieved
 	 * @param the time, in seconds, to wait before requesting the player
 	 */
 	public void startGetPlayer(int time){
 		_timer = new Timer(1000*(time+1), new GetPlayerActionListener(this));
 		_gui.getBoard().newRound(time);
 		_timer.setRepeats(false);
 		_timer.start();
 	}
 	
 	/**
 	 * Gets the player associated with this object
 	 * @return the player
 	 */
 	public Player finishGetPlayer(){
 		if(_timer!= null) _timer.stop();
 		return _gui.getBoard().getPlayer();
 	}
 	
 	/**
 	 * Updates records, data and displays finishing screen for the end of the game
 	 * @param combinedData
 	 */
 	private void finishMatch(GameDataReport combinedData){
 		int numPlayers = combinedData._timeStamps.get(0).wealthData.size();
 		String[] names = new String[BackendConstants.MAX_NUM_PLAYERS];
 		
 		for(int i = 0; i < names.length; i++){
 			names[i] = i < numPlayers ? _playerNames.get(i) : "";
 		}
 		
 		/* displays the end game screen */
 		_gui.createEndGame(_gui.getBoard(), combinedData.getPlayerWithMostWins() == _id, names);
 		
 		/* update records */
 		_gui.getCurrentProfile().getRecord().addMatch(combinedData.getPlayerWithMostWins() == _id);
 		_gui.saveProfiles();
 	}
 	
 	/**
 	 * Updates data in the gui
 	 * @param data 
 	 */
 	private void sendDataToGui(GameDataReport data){
 		_gui.getBoard().setPlayerSpecificPropertyData(getPlayerPropertyData(data._overallPlayerPropertyData));
 		_gui.getBoard().setPropertyData(data._overallPropertyData);
 		_gui.getBoard().setWealthData(getPlayerWealthData(data._timeStamps));
 	}
 	
 	/**
 	 * Populates the map of player ids to names
 	 */
 	protected void setPlayerNames(List<Player> players){
 		for(Player p : players){
 			_playerNames.put(p.ID, p.Name);
 		}
 		_playerNames.put(-1, "banker");
 	}
 
 	/**
 	 * Displays an error message in the gui
 	 * @param errorMessage the error message to be displayed
 	 */
 	private void displayMessage(String message){
 		JOptionPane.showMessageDialog(_gui, message);
 	}	
 	
 	private class GetPlayerActionListener implements ActionListener{
 		HumanClient _client;
 		public GetPlayerActionListener(HumanClient client){
 			_client = client;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			_client.finishRespondToGetPlayer();
 		}
 	}
 }
