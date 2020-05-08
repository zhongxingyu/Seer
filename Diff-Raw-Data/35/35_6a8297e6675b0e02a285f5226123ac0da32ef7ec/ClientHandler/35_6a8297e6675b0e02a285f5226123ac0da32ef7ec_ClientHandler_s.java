 package edu.brown.cs32.MFTG.networking;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer.Method;
 import edu.brown.cs32.MFTG.tournament.Settings;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class ClientHandler {
 
 	private final int GET_PLAYER_TIME = 180;
 	private final int PLAY_GAMES_TIME = 15;
 	
 	public final int _id;
 
 	final Socket _client;
 	private BufferedReader _input;
 	private BufferedWriter _output;
 	private ObjectMapper _oMapper;
 
 	JavaType _listOfPlayers;
 
 	public ClientHandler(Socket client, int id) throws IOException{
 		_id = id;
 		
 		_client = client;
 		_input = new BufferedReader(new InputStreamReader(_client.getInputStream()));
 		_output = new BufferedWriter(new OutputStreamWriter(_client.getOutputStream()));
 		_oMapper = new ObjectMapper();
 
 		_listOfPlayers = _oMapper.getTypeFactory().constructCollectionType(List.class, Player.class);
 	}
 	
 	/**
 	 * Writes a request to the output stream
 	 * @param request the request to write
 	 * @throws IOException
 	 */
 	private void write(ClientRequestContainer request) throws IOException{
 		String json = _oMapper.writeValueAsString(request);
 		_output.write(json);
 		_output.write("\n");
 		_output.flush();
 	}
 	
 	/**
 	 * Reads a response form the client
 	 * @return
 	 * @throws IOException
 	 */
 	private ClientRequestContainer readResponse() throws IOException{
 		String json = _input.readLine();
 		return _oMapper.readValue(json, ClientRequestContainer.class);
 	}
 
 	/**
 	 * Gets the player associated with this object's client
 	 * @return the player returned by the client
 	 * @throws ClientCommunicationException 
 	 * @throws InvalidResponseException 
 	 */
 	public Player getPlayer() throws ClientCommunicationException, ClientLostException, InvalidResponseException{
 		String time = String.valueOf(GET_PLAYER_TIME);
 		ClientRequestContainer request = new ClientRequestContainer(Method.GETPLAYER, Arrays.asList(time));
 
 		// ask the client for gameData
 		try {
 			write(request);
 			
 			// read in the response
 			ClientRequestContainer response = readResponse();
 			
 			// check for bad responses
 			if (response._method != Method.SENDPLAYER){
 				throw new InvalidResponseException(Method.SENDPLAYER, response._method);
 			} else if (response._arguments == null || response._arguments.size() < 1){
 				throw new InvalidResponseException("Not enough arguments");
 			}
 
 			// set the timeout and attempt to read the response from the client
 			try {
 //				_client.setSoTimeout((GET_PLAYER_TIME + 10) * 1000);
 				Player p = _oMapper.readValue(response._arguments.get(0), Player.class);
 //				_client.setSoTimeout(0);
 				return p;
 			} catch (SocketTimeoutException | SocketException e){
 				throw new ClientLostException(_id);
 			}
 
 		} catch (IOException e) {
 			throw new ClientCommunicationException(_id);
 		}
 		
 	}
 
 	/**
 	 * Sends an encoding of the players to the client and a request to play numGame games
 	 * @param players
 	 * @param numGames
 	 * @return the GameData collected from playing the round of games
 	 * @throws ClientLostException 
 	 * @throws InvalidResponseException 
 	 */
 	public List<GameData> playGames(List<Player> players, List<Long> seeds, Settings settings) throws ClientCommunicationException, ClientLostException, InvalidResponseException{
 		try {
 			String playerList = _oMapper.writeValueAsString(players);
 			String seedList = _oMapper.writeValueAsString(seeds);
 			String settingsString = _oMapper.writeValueAsString(settings);
 			
 			List<String> arguments = Arrays.asList(playerList, seedList, settingsString);
 
 			ClientRequestContainer request = new ClientRequestContainer(Method.PLAYGAMES, arguments);
 
 			// request that the client play the games
 			write(request);
 
 			// read in the response
 			ClientRequestContainer response = readResponse();
 
 			// check for bad response
 			if (response._method != Method.SENDGAMEDATA){
 				throw new InvalidResponseException(Method.SENDGAMEDATA, response._method);
 			} else if (response._arguments == null || response._arguments.size() < 1){
 				throw new InvalidResponseException("Not enough arguments");
 			}
 
 			// construct the JavaType that will be read in
 			JavaType listOfGameData = _oMapper.getTypeFactory().constructCollectionType(List.class, GameData.class);
 
 			// set the timeout and attempt to read the response from the client
 			List<GameData> gameData = _oMapper.readValue(response._arguments.get(0), listOfGameData);
 			return gameData;
 
 		} catch (IOException e){
			e.printStackTrace();
 			throw new ClientCommunicationException(_id);
 		}
 	}
 
 	public void setGameData(GameDataReport combinedData) throws ClientCommunicationException {
 		try {
 			String data = _oMapper.writeValueAsString(combinedData);
 
 			ClientRequestContainer request = new ClientRequestContainer(Method.DISPLAYGAMEDATA, Arrays.asList(data));
 
 			// request that the client display the data
 			write(request);
 
 		} catch (IOException e){
			e.printStackTrace();
 			throw new ClientCommunicationException(_id);
 		}
 	}
 	
 	public void sendErrorMessage(String errorMessage) {
 		ClientRequestContainer request = new ClientRequestContainer(Method.DISPLAYERROR, Arrays.asList(errorMessage));
 		
 		// request that the client display the error message
 		try {
 			write(request);
 		} catch (IOException e) {
 			// do nothing -- you are already fucked
 		}
 	}
 	
 	public void sendID() throws ClientCommunicationException {
 		String stringID = String.valueOf(_id);
 		ClientRequestContainer request = new ClientRequestContainer(Method.SENDID, Arrays.asList(stringID));
 		
 		// request that the client display the error message
 		try {
 			write(request);
 		} catch (IOException e) {
 			throw new ClientCommunicationException(_id);
 		}
 	}
 	
 	public Reader getInputReader(){
 		return _input;
 	}
 	
 	public Writer getOutputWriter(){
 		return _output;
 	}
 }
