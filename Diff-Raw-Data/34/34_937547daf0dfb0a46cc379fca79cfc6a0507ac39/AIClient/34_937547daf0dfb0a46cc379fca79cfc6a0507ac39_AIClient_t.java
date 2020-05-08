 package edu.brown.cs32.MFTG.tournament;
 
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
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 import edu.brown.cs32.MFTG.monopoly.Board;
 import edu.brown.cs32.MFTG.monopoly.Game;
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer;
 import edu.brown.cs32.MFTG.networking.InvalidRequestException;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 import edu.brown.cs32.MFTG.tournament.data.PlayerWealthDataReport;
 import edu.brown.cs32.MFTG.tournament.data.PropertyDataReport;
 import edu.brown.cs32.MFTG.tournament.data.TimeStampReport;
 
 public class AIClient extends Client{
 	// data from previous set
 	private GameDataReport _currentGameData;
 	private GameDataReport _previousGameData;
 	private HashMap<String,String> _colorKeys;
 
 	// player from previous set
 	private Player _player;
 	private Player _previousPlayer;
 
 	public AIClient() {
 		super();
 		_player = new Player(_id,"Computer");
 		_previousPlayer=null;
 		_colorKeys = new HashMap<>(30);
 		initializeColorKeys();
 	}
 	
 	private void initializeColorKeys() {
 		_colorKeys.put("mediterranean avenue", "purple");
 		_colorKeys.put("baltic avenue", "purple");
 		
 		_colorKeys.put("oriental avenue", "light blue");
 		_colorKeys.put("vermont avenue", "light blue");
 		_colorKeys.put("connecticut avenue", "light blue");
 		
 		_colorKeys.put("st. charles place", "pink");
 		_colorKeys.put("states avenue", "pink");
 		_colorKeys.put("virginia avenue", "pink");
 		
 		_colorKeys.put("st. james place", "orange");
 		_colorKeys.put("tennessee avenue", "orange");
 		_colorKeys.put("new york avenue", "orange");
 		
 		_colorKeys.put("kentucky avenue", "red");
 		_colorKeys.put("indiana avenue", "red");
 		_colorKeys.put("illinois avenue", "red");
 		
 		_colorKeys.put("atlantic avenue", "yellow");
 		_colorKeys.put("ventnor avenue", "yellow");
 		_colorKeys.put("marvin gardens", "yellow");
 		
 		_colorKeys.put("pacific avenue", "green");
 		_colorKeys.put("north carolina avenue", "green");
 		_colorKeys.put("pennsylvania avenue", "green");
 		
 		_colorKeys.put("park place", "dark blue");
 		_colorKeys.put("boardwalk", "dark blue");
 		
 		_colorKeys.put("reading railroad", "black");
 		_colorKeys.put("pennsylvania railroad", "black");
 		_colorKeys.put("b and o railroad", "black");
 		_colorKeys.put("short line", "black");
 		_colorKeys.put("electric company", "black");
 		_colorKeys.put("water works", "black");
 		
 	}
 
 	
 	public void run() {
 		try {
 			_server = new Socket(_host,_port);
 			_input = new BufferedReader(new InputStreamReader(_server.getInputStream()));
 			_output = new BufferedWriter(new OutputStreamWriter(_server.getOutputStream()));
 		} catch (UnknownHostException e) {
 			return;
 		} catch (IOException e) {
 			return;
 		}
 		try {
 			_id = respondToSendID();
 		} catch (IOException | InvalidRequestException e1) {
 			return;
 		}
 
 		while(true){
 			try {
 				handleRequest();
 			} catch (Exception e){
 				// TODO handle this
 			}
 		}
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
 		_previousGameData=_currentGameData;
 		_currentGameData = gameDataReport;
 	}
 
 	public synchronized void addGameData(GameData gameData){
 		List<GameData> temp = new ArrayList<>();
 		temp.add(gameData);
 		GameDataAccumulator a = DataProcessor.aggregate(temp,BackendConstants.NUM_DATA_POINTS);
 		if(_data == null){
 			_data = a;
 		} else {
 			DataProcessor.combineAccumulators(_data, DataProcessor.aggregate(temp,BackendConstants.NUM_DATA_POINTS));
 		}
 	}
 
 	public void startGetPlayer(int time){
 		finishRespondToGetPlayer();
 	}
 
 	public Player finishGetPlayer(){
 		Player temp = new Player(_player);
 		if(_currentGameData==null) {
 			_player.setColorValue("purple", 2, 65, 1.75, 1.5);
 			_player.setColorValue("light blue", 2, 65, 1.75, 1.5);
 			_player.setColorValue("pink", 2, 125, 1.75, 1.5);
 			_player.setColorValue("orange", 2, 125, 1.75, 1.5);
 			_player.setColorValue("red", 2, 200, 1.75, 1.5);
 			_player.setColorValue("yellow", 2, 200, 1.75, 1.5);
 			_player.setColorValue("green", 2, 250, 1.75, 1.5);
 			_player.setColorValue("dark blue", 2, 250, 1.75, 1.5);
 
 			_player.setPropertyValue("oriental avenue", 150);
 			_player.setPropertyValue("vermont place", 140);
 			_player.setPropertyValue("connecticut avenue", 170);
 
 			_player.setPropertyValue("tennessee avenue", 220);
 			_player.setPropertyValue("st. james place", 220);
 			_player.setPropertyValue("new york avenue", 240);
 
 			_player.setJailWait(1);
 			_player.setMinBuildCash(200);
 			_player.setMinBuyCash(150);
 			_player.setMinUnmortgageCash(300);
 			_player.setTradingFear(1.3);
 			_player.setLiquidity(7);
 
 		}
 		else if(_previousGameData==null) {
 			_player.setColorValue("light blue", 2.2, 70, 1.9, 1.7);
 			_player.setColorValue("orange", 2.2, 140, 1.9, 1.7);
 			_player.setColorValue("green", 1.9, 140, 1.6, 1.3);
 			_player.setColorValue("dark blue", 1.9, 225, 1.6, 1.3);
 
 			_player.setJailWait(2);
 			_player.setMinBuildCash(200);
 			_player.setMinBuyCash(150);
 			_player.setMinUnmortgageCash(300);
 			_player.setTradingFear(1.2);
 			_player.setLiquidity(9);
 
 		}
 		else {
 			for(String key: _currentGameData._overallPlayerPropertyData.keySet()) {
 				PropertyDataReport others =  _currentGameData._overallPropertyData.get(key);
 				PropertyDataReport mine =   findMyReport(_currentGameData._overallPlayerPropertyData.get(key));
 				PropertyDataReport minePrev =   findMyReport(_previousGameData._overallPlayerPropertyData.get(key));
 				String color = _colorKeys.get(key);
 				System.out.println(color);
 				System.out.println(key);
 				if(mine==null&&minePrev!=null) {
 					if(others.accTotalRevenueWithoutHouses+others.accTotalRevenueWithHouses>minePrev.accTotalRevenueWithHouses+minePrev.accTotalRevenueWithoutHouses) {
 						int valDif = _player.getPropertyValue(key)-_previousPlayer.getPropertyValue(key);
 						if(valDif>0) {
 							_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)-valDif*.9));
 						}
 						else if(valDif<0){
 							_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)+valDif*.9));
 						}
 					}
 					continue;
 				}
 				else if(minePrev==null&&mine!=null) {
 					if(others.accTotalRevenueWithoutHouses+others.accTotalRevenueWithHouses>mine.accTotalRevenueWithHouses+mine.accTotalRevenueWithoutHouses) {
 						int valDif = _player.getPropertyValue(key)-_previousPlayer.getPropertyValue(key);
 						if(valDif>0) {
 							_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)+valDif*.9));
 						}
 						else if(valDif<0){
 							_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)-valDif*.9));
 						}
 					}
 					continue;
 				}
 				else if(mine==null&&minePrev==null) {
 					continue;
 				}
 				//If i'm doing better than previously, then I can value it more or less according to what i've previously done
 				if(mine.accTotalRevenueWithoutHouses+mine.accTotalRevenueWithHouses>minePrev.accTotalRevenueWithHouses+minePrev.accTotalRevenueWithoutHouses) {
 					int valDif = _player.getPropertyValue(key)-_previousPlayer.getPropertyValue(key);
 					if(valDif>0) {
 						_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)+valDif*.9));
 					}
 					else if(valDif<0){
 						_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)-valDif*.9));
 					}
 
 				}
 				else {
 					int valDif = _player.getPropertyValue(key)-_previousPlayer.getPropertyValue(key);
 					if(valDif>0) {
 						_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)-valDif*.9));
 					}
 					else if(valDif<0){
 						_player.setPropertyValue(key, (int) (_player.getPropertyValue(key)+valDif*.9));
 					}
 				}
 				if(color.equals("black")) {
 					continue;
 				}
 				if(mine.accTotalRevenueWithHouses>minePrev.accTotalRevenueWithHouses) {
 					double monopolyDif = _player.getMonopolyValue(color)-_previousPlayer.getMonopolyValue(color);
 					if(monopolyDif>0) {
 						_player.setMonopolyValue(color, _player.getMonopolyValue(color)+monopolyDif*.9);
 					}
 					else if(monopolyDif<0){
 						_player.setMonopolyValue(color, _player.getMonopolyValue(color)-monopolyDif*.9);
 					}
 
 					double houseDif = _player.getHouseValueOfColor(color)-_previousPlayer.getHouseValueOfColor(color);
 					if(houseDif>0) {
 						_player.setHouseValueOfColor(color, _player.getHouseValueOfColor(color)+houseDif*.9);
 					}
 					else if(houseDif<0){
 						_player.setHouseValueOfColor(color, _player.getHouseValueOfColor(color)-houseDif*.9);
 					}
 
 					double sameDif = _player.getSameColorEffect(color)-_previousPlayer.getSameColorEffect(color);
 					if(sameDif>0) {
 						_player.setSameColorEffect(color, _player.getSameColorEffect(color)+sameDif*.9);
 					}
 					else if(sameDif<0){
 						_player.setSameColorEffect(color, _player.getSameColorEffect(color)-sameDif*.9);
 					}
 
 
 				}
 				else {
 					double monopolyDif = _player.getMonopolyValue(color)-_previousPlayer.getMonopolyValue(color);
 					if(monopolyDif>0) {
 						_player.setMonopolyValue(color, _player.getMonopolyValue(color)-monopolyDif*.9);
 					}
 					else if(monopolyDif<0){
 						_player.setMonopolyValue(color, _player.getMonopolyValue(color)+monopolyDif*.9);
 					}
 					System.out.println("5b1");
 					double houseDif = _player.getHouseValueOfColor(color)-_previousPlayer.getHouseValueOfColor(color);
 					if(houseDif>0) {
 						_player.setHouseValueOfColor(color, _player.getHouseValueOfColor(color)-houseDif*.9);
 					}
 					else if(houseDif<0){
 						_player.setHouseValueOfColor(color, _player.getHouseValueOfColor(color)+houseDif*.9);
 					}
 					System.out.println("5b2");
 					double sameDif = _player.getSameColorEffect(color)-_previousPlayer.getSameColorEffect(color);
 					if(sameDif>0) {
 						_player.setSameColorEffect(color, _player.getSameColorEffect(color)-sameDif*.9);
 					}
 					else if(sameDif<0){
 						_player.setSameColorEffect(color, _player.getSameColorEffect(color)+sameDif*.9);
 					}
 				}
 				if(others.accTotalRevenueWithHouses>mine.accTotalRevenueWithHouses) {
 					double monopolyDif = _player.getBreakingOpponentMonopolyValue(color)-_previousPlayer.getBreakingOpponentMonopolyValue(color);
 					if(monopolyDif>0) {
 						_player.setBreakingOpponentMonopolyValue(color, _player.getBreakingOpponentMonopolyValue(color)+monopolyDif*.9);
 					}
 					else if(monopolyDif<0){
 						_player.setBreakingOpponentMonopolyValue(color, _player.getBreakingOpponentMonopolyValue(color)-monopolyDif*.9);
 					}	
 				}
 
 			}
 			double mycash=0;
 			double mywealth=0;
 			double othercash=0;
 			double otherwealth=0;
 			for(TimeStampReport t : _currentGameData._timeStamps){
 				PlayerWealthDataReport r=t.wealthData.get(t.wealthData.size()-1);
 				if(r.ownerID==_id) {
 					mycash=r.accCash;
 					mywealth=r.accTotalWealth;
 				}
 			}
 			for(TimeStampReport t : _previousGameData._timeStamps){
 				PlayerWealthDataReport r=t.wealthData.get(t.wealthData.size()-1);
 				if(r.ownerID==_id) {
 					othercash=r.accCash;
 					otherwealth=r.accTotalWealth;
 				}
 			}
 			othercash=othercash/(_previousGameData._timeStamps.size()-1);
 			otherwealth=otherwealth/(_previousGameData._timeStamps.size()-1);
 			if(_previousPlayer==null) {
 				_player.setLiquidity(Math.min(_player.getLiquidity()+1,10));
 				_player.setTimeChange(Math.min(_player.getTimeChange()+1,10));
 				_player.setJailWait(Math.min(_player.getJailWait()+1,3));
 				_player.setJailPoor(Math.min(_player.getJailPoor()+1,3));
 				_player.setJailRich(Math.min(_player.getJailRich()+1,3));
 				_player.setTradingFear(Math.min(_player.getTradingFear()+1,10));
 			}
 			//if I did poorly
 			if(otherwealth>=mywealth) {
 				//I did really poorly and lost in cash and wealth
 				if(othercash>=mycash) {
 					_player.setLiquidity(Math.min(Math.max(1,_previousPlayer.getLiquidity()+.9*(_previousPlayer.getLiquidity()-_player.getLiquidity())),10));
 					_player.setTimeChange(Math.min(Math.max(1,_previousPlayer.getTimeChange()+.9*(_previousPlayer.getTimeChange()-_player.getTimeChange())),10));
 					_player.setMinBuyCash((int) Math.max(0,_previousPlayer.getMinBuyCash()+.9*(_previousPlayer.getMinBuyCash()-_player.getMinBuyCash())));
 					_player.setMinUnmortgageCash((int) Math.max(0,_previousPlayer.getMinUnmortgageCash()+.9*(_previousPlayer.getMinUnmortgageCash()-_player.getMinUnmortgageCash())));
 					_player.setJailWait(Math.min(Math.max(1,_previousPlayer.getJailWait()+(_previousPlayer.getJailWait()-_player.getJailWait())),3));
 					_player.setMortgageChoice(_previousPlayer.getMortgageChoice());
 					_player.setBuildingEvenness(_previousPlayer.getBuildingEvenness());
 					_player.setBuildingChoice(_previousPlayer.getBuildingChoice());
 					_player.setHouseSelling(_previousPlayer.getHouseSelling());
 					_player.setJailPoor(Math.min(Math.max(1,_previousPlayer.getJailPoor()+(_previousPlayer.getJailPoor()-_player.getJailPoor())),3));
 					_player.setJailRich(Math.min(Math.max(1,_previousPlayer.getJailRich()+(_previousPlayer.getJailRich()-_player.getJailRich())),3));
 					_player.setMinBuildCash((int) Math.max(0,_previousPlayer.getMinBuildCash()+.9*(_previousPlayer.getMinBuildCash()-_player.getMinBuildCash())));
 					_player.setBuildAggression(_previousPlayer.getBuildAggression());
 					_player.setTradingFear(Math.min(Math.max(1,_previousPlayer.getTradingFear()+.9*(_previousPlayer.getTradingFear()-_player.getTradingFear())),10));
 				}
 				//I hoarded cash way too much
 				else {
 					_player.setMinBuildCash(Math.max(0, _previousPlayer.getMinBuildCash()-50));
 					_player.setMinBuyCash(Math.max(0,_previousPlayer.getMinBuyCash()-50));
 					_player.setMinUnmortgageCash(Math.max(0,_previousPlayer.getMinUnmortgageCash()-50));
 				}
 			}
 			//if I did well
 			else {
 				_player.setLiquidity(Math.min(Math.max(1,_previousPlayer.getLiquidity()-.9*(_previousPlayer.getLiquidity()-_player.getLiquidity())),10));
 				_player.setTimeChange(Math.min(Math.max(1,_previousPlayer.getTimeChange()-.9*(_previousPlayer.getTimeChange()-_player.getTimeChange())),10));
 				_player.setMinBuyCash((int) Math.max(0,_previousPlayer.getMinBuyCash()-.9*(_previousPlayer.getMinBuyCash()-_player.getMinBuyCash())));
 				_player.setMinUnmortgageCash((int) Math.max(0,_previousPlayer.getMinUnmortgageCash()+.9*(_previousPlayer.getMinUnmortgageCash()-_player.getMinUnmortgageCash())));
 				_player.setJailWait(Math.min(Math.max(1,_previousPlayer.getJailWait()-(_previousPlayer.getJailWait()-_player.getJailWait())),3));
 				_player.setJailPoor(Math.min(Math.max(1,_previousPlayer.getJailPoor()-(_previousPlayer.getJailPoor()-_player.getJailPoor())),3));
 				_player.setJailRich(Math.min(Math.max(1,_previousPlayer.getJailRich()-(_previousPlayer.getJailRich()-_player.getJailRich())),3));
 				_player.setMinBuildCash((int) Math.max(0,_previousPlayer.getMinBuildCash()-.9*(_previousPlayer.getMinBuildCash()-_player.getMinBuildCash())));
 				_player.setTradingFear(Math.min(Math.max(1,_previousPlayer.getTradingFear()-.9*(_previousPlayer.getTradingFear()-_player.getTradingFear())),10));
 			}
 		}
 		_previousPlayer=temp;
 		return _player;
 	}
 
 	private PropertyDataReport findMyReport(List<PropertyDataReport> reports) {
 		if(reports==null) {
 			return null;
 		}
 		for(PropertyDataReport r: reports) {
 			if(r.ownerID==_id) {
 				return r;
 			}
 		}
 		return null;
 	}
 
 	protected void respondToDisplayError(ClientRequestContainer request) {} /* do nothing, there is no error to display */
 
 	protected void setPlayerNames(List<Player> players){} /* do nothing */
 
 
 }
