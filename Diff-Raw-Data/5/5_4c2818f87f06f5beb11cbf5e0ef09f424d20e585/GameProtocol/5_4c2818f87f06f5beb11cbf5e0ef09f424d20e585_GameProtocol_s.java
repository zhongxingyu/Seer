 package server.net;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import server.model.BoardPosition;
 import server.model.Game;
 import server.model.GameState;
 import server.model.Player;
 import server.model.Tile;
 
 public class GameProtocol implements SocketProtocol {
 
 	// Message format is as follows (client sends followed by server replies):
 	//
 	// Note: Any messages sent at the wrong time or other errors will cause the
 	// server to return SocketProtocol.NAK ("NAK").
 	//
 	// JOINLOBBY
 	//
 	// LEAVELOBBY;player;<int>
 	//
 	// ASSIGNPLAYER;player;<int>
 	//
 	// UPDATELOBBY[;player;<int>;name;<string>;color;<string:(RGB)>]+
 	//
 	// UPDATEPLAYER;player;<int>;name;<string>;color;<string:(RGB)>
 	//
 	//
 	// INIT;numPlayers;<int>
 	// INIT;currentPlayer;<int>;gameBoardWidth;<int>;gameBoardHeight;<int>
 	//
 	// DRAWTILE;currentPlayer;<int>
 	// DRAWTILE;currentPlayer;<int>;identifier;<string>;orientation;<int:[0-3]>
 	//
 	// ROTATETILE;currentPlayer;<int>;direction;<string:(clockwise|counterClockwise)>
 	// ROTATETILE;currentPlayer;<int>;direction;<string:(clockwise|counterClockwise)>
 	//
 	// PLACETILE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>
 	// PLACETILE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>;error;<int:(0|1)>
 	//
 	// PLACEMEEPLE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>;xTile;<int>;yTile;<int>
 	// PLACEMEEPLE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>;xTile;<int>;yTile;<int>;error;<int:(0|1)>
 	//
 	// SCORE
 	// SCORE[;meeple;xBoard;<int>;yBoard;<int>;xTile;<int>;yTile;<int>]*
 	//
 	//
 	// INFO;player;<int>;
 	// INFO;player;<int>;currentPlayer;<int:(0|1)>;score;<int>;meeplesPlaced;<int>
 	//
 	// INFO;game;
 	// INFO;game;currentPlayer;<int>;drawPileEmpty;<int:(0|1)>
 	//
 
 	// State layout:
 	//
 	// START_GAME
 	// DRAW_TILE
 	// PLACE_TILE
 	// SCORE_PLAYERS --> END_TURN
 	// PLACE_MEEPLE
 	// SCORE_PLAYERS --> DRAW_TILE
 	// END_GAME
 	//
 	// Each state advances to the state below it, or to the state pointed to on
 	// the right. End turn state is just a placeholder state which changes the
 	// game state to a proper state based on a few properties of our game. This
 	// allows us to not over-complicate the message passing protocol.
 
 	private HashMap<Socket, PrintWriter> writers = new HashMap<Socket, PrintWriter>();
 	private ArrayList<String> parsedMessage = new ArrayList<String>();
 
 	@Override
 	public void addSender(Socket socket) {
 		try {
 			OutputStream outStream = socket.getOutputStream();
 			PrintWriter writer = new PrintWriter(outStream, true);
 
 			writers.put(socket, writer);
 
 		} catch (IOException e) {
 			// Getting the output stream has
 			// failed.
 			// TODO
 		}
 	}
 
 	private void removeSender(Socket socket) {
 		writers.remove(socket);
 		// TODO: close socket/ writer connections
 	}
 
 	@Override
 	public int getMaxConnections() {
 		return Game.getMaxPlayers();
 	}
 
 	@Override
 	public int getNumConnections() {
 		return writers.size();
 	}
 
 	// Pre-game variables (lobby).
 	private HashMap<Integer, PlayerStruct> lobbyPlayers = new HashMap<Integer, PlayerStruct>();
 
 	private final Color[] colors = { Color.black, Color.blue, Color.green,
 			Color.red, Color.yellow };
 	private ArrayList<Color> availablePlayerColors = new ArrayList<Color>(
 			Arrays.asList(colors));
 
 	class PlayerStruct {
 
 		PlayerStruct(String name, String color) {
 			this.name = name;
 			this.color = color;
 		}
 
 		private String name;
 		private String color;
 	}
 
 	// In-game variables.
 	private Game game;
 	private GameState gameState = GameState.START_GAME;
 	private int currentPlayer = 0;
 
 	// Pre-game messages.
 	private String[] makeAssignPlayerMsg(int numberRep) {
 
 		String message = "ASSIGNPLAYER;player;" + numberRep;
 		String[] output = { SocketProtocol.replySender, message };
 
 		return output;
 	}
 
 	private String[] makeUpdateLobbyMsg() {
 
 		String message = "UPDATELOBBY";
 
 		Iterator<Integer> playersIter = lobbyPlayers.keySet().iterator();
 
 		while (playersIter.hasNext()) {
 
 			Integer numberRep = playersIter.next();
 			PlayerStruct player = lobbyPlayers.get(numberRep);
 
 			message += ";player;" + numberRep + ";name;" + player.name
 					+ ";color;" + player.color;
 		}
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	// In-game messages.
 	private String[] makeGameInfoMsg() {
 
 		int isDrawPileEmpty = game.isDrawPileEmpty() ? 1 : 0;
 
 		String message = "INFO;game;currentPlayer;" + currentPlayer
 				+ ";drawPileEmpty;" + isDrawPileEmpty;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makePlayerInfoMsg(int player) {
 
 		Player playerModel = game.getPlayers().get(player);
 
 		int isCurrentPlayer = (player == currentPlayer) ? 1 : 0;
 		int playerScore = playerModel.getScore();
 		int numMeeplesPlaced = game.getNumMeeplesPlaced(playerModel);
 
 		String message = "INFO;player;" + player + ";currentPlayer;"
 				+ isCurrentPlayer + ";score;" + playerScore + ";meeplesPlaced;"
 				+ numMeeplesPlaced;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makeInitMsg(int player) {
 
 		String message = "INIT;currentPlayer;" + player + ";gameBoardWidth;"
 				+ game.getBoardWidth() + ";gameBoardHeight;"
 				+ game.getBoardHeight();
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makeDrawTileMsg(int player, String identifier,
 			int orientation) {
 
 		String message = "DRAWTILE;currentPlayer;" + player + ";identifier;"
 				+ identifier + ";orientation;" + orientation;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makePlaceTileMsg(int player, int xBoard, int yBoard,
 			int error) {
 
 		String message = "PLACETILE;currentPlayer;" + player + ";xBoard;"
 				+ xBoard + ";yBoard;" + yBoard + ";error;" + error;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makeRotateTileMsg(int player, String direction) {
 
 		String message = "ROTATETILE;currentPlayer;" + player + ";direction;"
 				+ direction;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makePlaceMeepleMsg(int player, int xBoard, int yBoard,
 			int xTile, int yTile, int error) {
 
 		String message = "PLACEMEEPLE;currentPlayer;" + player + ";xBoard;"
 				+ xBoard + ";yBoard;" + yBoard + ";xTile;" + xTile + ";yTile;"
 				+ yTile + ";error;" + error;
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makeScoreMsg(ArrayList<BoardPosition> removedMeeples) {
 
 		String message = "SCORE";
 
 		for (int i = 0; i < removedMeeples.size(); i++) {
 
 			BoardPosition meeplePosition = removedMeeples.get(i);
 
 			if (meeplePosition != null) {
 				message = message.concat(";meeple;xBoard;"
 						+ meeplePosition.xBoard + ";yBoard;"
 						+ meeplePosition.yBoard + ";xTile;"
 						+ meeplePosition.xTile + ";yTile;"
 						+ meeplePosition.yTile);
 			}
 		}
 
 		String[] output = { SocketProtocol.replyAll, message };
 
 		return output;
 	}
 
 	private String[] makeErrorMsg() {
 
 		String[] output = { SocketProtocol.replySender, SocketProtocol.NAK };
 		return output;
 	}
 
 	private String[] makeEndTurnMsg(int currentPlayer) {
 
 		String message = "ENDTURN;currentPlayer;" + currentPlayer;
 		String[] output = { SocketProtocol.replyAll, message };
 		return output;
 	}
 
 	private String[] makeEndGameMsg() {
 
 		String[] output = { SocketProtocol.replyAll, SocketProtocol.EXIT };
 		return output;
 	}
 
 	private String[] makeCloseClientMsg() {
 
 		String[] output = { SocketProtocol.replySender, SocketProtocol.EXIT };
 		return output;
 	}
 
 	// Utility functions.
 	/**
 	 * Add a player to the list of players in the lobby.
 	 * 
 	 * @param numberRep
 	 *            The number representation of the player (0-4).
 	 */
 	private void addPlayer(int numberRep) {
 
 		String rgb = colorToString(availablePlayerColors.remove(0));
 		String name = "Player " + numberRep;
 		lobbyPlayers.put(numberRep, new PlayerStruct(name, rgb));
 	}
 
 	/**
 	 * Remove a player from the list of the players in the lobby.
 	 * 
 	 * @param numberRep
 	 *            The number representation of the player (0-4).
 	 */
 	private void removePlayer(int numberRep) {
 
 		PlayerStruct player = lobbyPlayers.get(numberRep);
 		availablePlayerColors.add(0, stringToColor(player.color));
 		lobbyPlayers.remove(numberRep);
 	}
 
 	/**
 	 * Convert a Color to a String of length nine consisting of an RGB value.
 	 * Each individual color value (R, G, B) is a string of length three,
 	 * containing a value from "000" to "255".
 	 * 
 	 * @param color
 	 *            A Color to be converted to a String representation.
 	 * 
 	 * @return A String representing the input Color.
 	 */
 	private String colorToString(Color color) {
 
 		DecimalFormat df = new DecimalFormat("000");
 
 		String r = df.format(color.getRed());
 		String g = df.format(color.getGreen());
 		String b = df.format(color.getBlue());
 		String rgb = r + g + b;
 
 		return rgb;
 	}
 
 	/**
 	 * Convert a String of length nine to a Color. The string consists of an RGB
 	 * value; with each being 3 characters each containing a value from "000" to
 	 * "255".
 	 * 
 	 * @param string
 	 *            The String to be converted to a Color.
 	 * 
 	 * @return A Color representing the input String.
 	 */
 	private Color stringToColor(String string) {
 
 		int r = Integer.parseInt(string.substring(0, 3));
 		int g = Integer.parseInt(string.substring(3, 6));
 		int b = Integer.parseInt(string.substring(6, 9));
 
 		return new Color(r, g, b);
 	}
 
 	/**
 	 * Return the next free number representation for a player.
 	 * 
 	 * @return An integer representing the number (Id) for a new player.
 	 */
 	private int getFreePlayerSlot() {
 
 		// Find which player slot is not used. To do this record all used slots,
 		// sort them, and then run through until we find a slot (number) which
 		// is not used.
 		ArrayList<Integer> usedPlayerSlots;
 		usedPlayerSlots = new ArrayList<Integer>(lobbyPlayers.keySet());
 		Collections.sort(usedPlayerSlots);
 
 		int candidateSlot = 0;
 
 		for (int i = 0; i < usedPlayerSlots.size(); i++) {
 			if (usedPlayerSlots.get(i).intValue() == candidateSlot) {
 				candidateSlot++;
 			}
 		}
 
 		return candidateSlot;
 	}
 
 	// Send the message(s) to relevant client(s).
 	// The String arrays contained in processedMessages are two elements each.
 	// The first element is the message recipient, and the second element is the
 	// message itself.
 	// TODO: I want to make this simpler. Perhaps move String[]... to
 	// ArrayList<String>... to make it easier to split apart the recipient and
 	// message.
 	private ArrayList<String> disseminateMessages(Socket sender,
 			String[]... processedMessages) {
 
 		ArrayList<String> messages = new ArrayList<String>();
 
 		for (int i = 0; i < processedMessages.length; i++) {
 
 			String recipient = processedMessages[i][0];
 			String currentMessage = processedMessages[i][1];
 			messages.add(currentMessage);
 
 			if (recipient.equals(SocketProtocol.replyAll)) {
 
 				Iterator<Socket> sendersIter = writers.keySet().iterator();
 
 				while (sendersIter.hasNext()) {
 
 					Socket aSender = sendersIter.next();
 
 					if (!aSender.equals(sender)) {
 						writers.get(aSender).println(currentMessage);
 					}
 				}
 			}
 		}
 
 		return messages;
 	}
 
 	/**
 	 * Process input received from a game client/user. Depending on the game
 	 * state, carry out an appropriate action, and return any relevant updates
 	 * to the client(s)/user(s).
 	 * 
 	 * @param input
 	 *            A string message which represents a game action (see message
 	 *            format at the top of this file).
 	 * 
 	 * @return An ArrayList of string messages to return to the
 	 *         client(s)/user(s).
 	 */
 	@Override
 	public ArrayList<String> processInput(Socket sender, String input) {
 
 		// First we have some actions which are able to be called at any point
 		// during the game. These are requests for info about the game and any
 		// player. Scoring is also allowed at different points throughout the
 		// game.
 
 		// This also allows us to manipulate the input info to reduce any
 		// duplicated code.
 		parsedMessage.clear();
 		parsedMessage.addAll(Arrays.asList(input.split(";")));
 
 		// Allow a player to exit the game (lobby).
 		if (parsedMessage.get(0).equals(SocketProtocol.EXIT)) {
 
 			String[] closeClientMsg = makeCloseClientMsg();
 			return disseminateMessages(sender, closeClientMsg);
 		}
 
 		if (parsedMessage.get(0).equals("JOINLOBBY")) {
 
 			// Assign a player to the client which has joined the lobby.
 			if (lobbyPlayers.size() >= Game.getMaxPlayers()) {
 				return disseminateMessages(sender, makeErrorMsg());
 			}
 
 			int playerSlot = getFreePlayerSlot();
 			addPlayer(playerSlot);
 
 			String[] assignPlayer = makeAssignPlayerMsg(playerSlot);
 
 			// Send all the clients a message to update their lobbies.
 			String[] updateLobby = makeUpdateLobbyMsg();
 
 			return disseminateMessages(sender, assignPlayer, updateLobby);
 		}
 
 		if (parsedMessage.get(0).equals("UPDATEPLAYER")) {
 
 			int numberRep = Integer.parseInt(parsedMessage.get(2));
 			String name = parsedMessage.get(4);
 			String color = parsedMessage.get(6);
 
 			// Set the new values.
 			PlayerStruct player = lobbyPlayers.get(numberRep);
 			player.name = name;
 			player.color = color;
 
 			String[] updateLobby = makeUpdateLobbyMsg();
 			return disseminateMessages(sender, updateLobby);
 		}
 
 		if (parsedMessage.get(0).equals("LEAVELOBBY")) {
 			// Free the player which left the lobby.
 			int playerSlot = Integer.parseInt(parsedMessage.get(2));
 			removePlayer(playerSlot);
 
 			// Send all the clients a message to update their lobbies.
 			String[] updateLobbyMsg = makeUpdateLobbyMsg();
 
 			return disseminateMessages(sender, updateLobbyMsg);
 		}
 
 		// If the game is just starting then we need to send over initialization
 		// info. The gameboard width, height (# of tiles), the player whose turn
 		// it is, &c.
 		if (GameState.START_GAME == gameState) {
 
 			int numPlayers = 0;
 
 			if (!parsedMessage.get(0).equals("INIT")) {
 				return disseminateMessages(sender, makeErrorMsg());
 			}
 
 			if (parsedMessage.get(1).equals("numPlayers")) {
 				numPlayers = Integer.parseInt(parsedMessage.get(2));
 			}
 
 			// TODO
 			game = new Game(numPlayers);
 			gameState = GameState.DRAW_TILE;
 
 			String[] initMsg = makeInitMsg(currentPlayer);
 			return disseminateMessages(sender, initMsg);
 		}
 
 		if (GameState.DRAW_TILE == gameState) {
 
 			if (!parsedMessage.get(0).equals("DRAWTILE")) {
 				return disseminateMessages(sender, makeErrorMsg());
 			}
 
 			if (parsedMessage.get(1).equals("currentPlayer")) {
 
 				// The client is telling us that a different player is taking
 				// a tile than we told them. This is incorrect.
 				if (Integer.parseInt(parsedMessage.get(2)) != currentPlayer) {
 					return disseminateMessages(sender, makeErrorMsg());
 				}
 
 				// Otherwise continue the game by drawing a tile for the current
 				// player and letting the client know what the result was.
 				Player player = game.getPlayers().get(currentPlayer);
 				game.drawTile(player);
 				gameState = GameState.PLACE_TILE;
 
 				// Get variables to make the message & return it.
 				Tile tile = player.getCurrentTile();
 				String identifier = tile.getIdentifier();
 				int orientation = tile.getOrientation();
 
 				String[] drawTileMsg = makeDrawTileMsg(currentPlayer,
 						identifier, orientation);
 				return disseminateMessages(sender, drawTileMsg);
 			}
 		}
 
 		if (GameState.PLACE_TILE == gameState) {
 
 			if (!parsedMessage.get(0).equals("PLACETILE")
 					&& !parsedMessage.get(0).equals("ROTATETILE")) {
 				return disseminateMessages(sender, makeErrorMsg());
 			}
 
 			if (parsedMessage.get(1).equals("currentPlayer")) {
 
 				// Again, check the player the client is telling us that's
 				// playing is actually the player whose turn it is.
 				if (Integer.parseInt(parsedMessage.get(2)) != currentPlayer) {
 					return disseminateMessages(sender, makeErrorMsg());
 				}
 
 				// Check what the client wants us to do.
 				if (parsedMessage.get(0).equals("ROTATETILE")) {
 
 					String direction = "clockwise";
 
 					if (parsedMessage.get(3).equals("direction")) {
 						direction = parsedMessage.get(4);
 					}
 
 					Player player = game.getPlayers().get(currentPlayer);
 
 					if (direction.equals("clockwise")) {
 						player.getCurrentTile().rotateClockwise();
 					}
 
 					if (direction.equals("counterClockwise")) {
 						player.getCurrentTile().rotateCounterClockwise();
 					}
 
 					String[] rotateTileMsg = makeRotateTileMsg(currentPlayer,
 							direction);
 					return disseminateMessages(sender, rotateTileMsg);
 				}
 
 				if (parsedMessage.get(0).equals("PLACETILE")) {
 
 					// If not we continue on with the game; place the tile and
 					// advance to the next game state.
 					int xBoard = 0;
 					int yBoard = 0;
 
 					if (parsedMessage.get(3).equals("xBoard")) {
 						xBoard = Integer.parseInt(parsedMessage.get(4));
 					}
 					if (parsedMessage.get(5).equals("yBoard")) {
 						yBoard = Integer.parseInt(parsedMessage.get(6));
 					}
 
 					Player player = game.getPlayers().get(currentPlayer);
 					int err = game.placeTile(player, xBoard, yBoard);
 
 					if (err == 0) {
 
 						// TODO: can this be simplified?
 						int numMessages = game.getNumPlayers() + 3;
 						String[][] ret = new String[numMessages][];
 
 						ret[0] = makePlaceTileMsg(currentPlayer, xBoard,
 								yBoard, err);
 
 						ret[1] = makeScoreMsg(game.score(false));
 
 						for (int i = 0; i < game.getNumPlayers(); i++) {
 							ret[i + 2] = makePlayerInfoMsg(i);
 						}
 
 						ret[numMessages - 1] = makeGameInfoMsg();
 
 						gameState = GameState.END_TURN;
 
 						return disseminateMessages(sender, ret);
 
 					} else {
 						return disseminateMessages(sender, makeErrorMsg());
 					}
 				}
 			}
 		}
 
 		if (GameState.END_TURN == gameState) {
 
 			if (parsedMessage.get(0).equals("ENDTURN")) {
 
 				if (Integer.parseInt(parsedMessage.get(2)) != currentPlayer) {
 					return disseminateMessages(sender, makeErrorMsg());
 				}
 
 				String[] endTurnMsg = makeEndTurnMsg(currentPlayer);
 
 				return disseminateMessages(sender, endTurnMsg);
 			}
 
 			// The player decided to end their turn after placing a tile.
 			if (parsedMessage.get(0).equals("DRAWTILE")) {
 
 				currentPlayer = (currentPlayer + 1) % game.getNumPlayers();
 				gameState = GameState.DRAW_TILE;
 			}
 
 			// Or they decided to place a meeple.
 			if (parsedMessage.get(0).equals("PLACEMEEPLE")) {
 
 				gameState = GameState.PLACE_MEEPLE;
 			}
 
 			return processInput(sender, input);
 		}
 
 		if (GameState.PLACE_MEEPLE == gameState) {
 
 			if (!parsedMessage.get(0).equals("PLACEMEEPLE")) {
 				return disseminateMessages(sender, makeErrorMsg());
 			}
 
 			if (parsedMessage.get(1).equals("currentPlayer")) {
 
 				// Again, check the client is synchronized wrt/ player turn.
 				if (Integer.parseInt(parsedMessage.get(2)) != currentPlayer) {
 					return disseminateMessages(sender, makeErrorMsg());
 				}
 
 				// If everything is good; we're synchronized, continue.
 				int xBoard = 0;
 				int yBoard = 0;
 				int xTile = 0;
 				int yTile = 0;
 
 				if (parsedMessage.get(3).equals("xBoard")) {
 					xBoard = Integer.parseInt(parsedMessage.get(4));
 				}
 				if (parsedMessage.get(5).equals("yBoard")) {
 					yBoard = Integer.parseInt(parsedMessage.get(6));
 				}
 				if (parsedMessage.get(7).equals("xTile")) {
 					xTile = Integer.parseInt(parsedMessage.get(8));
 				}
 				if (parsedMessage.get(9).equals("yTile")) {
 					yTile = Integer.parseInt(parsedMessage.get(10));
 				}
 
 				Player player = game.getPlayers().get(currentPlayer);
 
 				int err;
 				err = game.placeMeeple(player, xBoard, yBoard, xTile, yTile);
 
 				if (err == 0) {
 
 					// TODO: can this be simplified?
 					int numMessages = game.getNumPlayers() + 3;
 					String[][] ret = new String[numMessages][];
 
 					boolean isGameOver = game.isDrawPileEmpty();
 
 					if (isGameOver) {
 						gameState = GameState.END_GAME;
 					}
 
 					ret[0] = makePlaceMeepleMsg(currentPlayer, xBoard, yBoard,
 							xTile, yTile, err);
 
 					ret[1] = makeScoreMsg(game.score(isGameOver));
 
 					for (int i = 0; i < game.getNumPlayers(); i++) {
 						ret[i + 2] = makePlayerInfoMsg(i);
 					}
 
 					ret[numMessages - 1] = makeGameInfoMsg();
 
 					gameState = GameState.DRAW_TILE;
 					currentPlayer = (currentPlayer + 1) % game.getNumPlayers();
 
 					return disseminateMessages(sender, ret);
 
 				} else {
 					return disseminateMessages(sender, makeErrorMsg());
 				}
 			}
 		}
 
 		// End game state.
 		if (GameState.END_GAME == gameState) {
 			return disseminateMessages(sender, makeEndGameMsg());
 		}
 
 		return disseminateMessages(sender, makeErrorMsg());
 	}
 }
