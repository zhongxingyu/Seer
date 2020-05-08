 package shared;
 
 import client.ClientHandler;
 import client.ClientWindow;
 
 
 public class Game {
 	private Board theBoard;
 	private Player whitePlayer;
 	private Player blackPlayer;
 	private ClientHandler clientHandler;
 	private boolean localGame;
 	private int gameCounter;
 	
 	public Game() {
 		localGame = true;
 		setupLocalGame();
		ClientWindow.setTurnNumberLabelText("Turn: 1");
 	}
 	
 	public Game(byte[] ip, short port) {
 		localGame = false;
 		setupNetworkGame(ip, port);
 		ClientWindow.setTurnNumberLabelText("Turn: 1");
 	}
 	
 	private void setupLocalGame() {
 		whitePlayer = new LocalPlayer(PieceColor.WHITE);
 		blackPlayer = new LocalPlayer(PieceColor.BLACK);
 		theBoard = new Board();
 		gameCounter = 0;
 	}
 	
 	private void setupNetworkGame(byte[] ip, short port) {
 		theBoard = new Board();
 		gameCounter = 0;
 		clientHandler = new ClientHandler(ip, port, this);
 		clientHandler.sendMessage("C"); // Ask for color
 		// We will be using setLocalColor(PieceColor color) to continue initializing.
 		// If it isn't called, there was a timeout.
 		// TODO Re-implement this so it is proper.
 	}
 	
 	public void setLocalColor(PieceColor color) { // Continue initializing
 		if(color.equals(PieceColor.WHITE)) {
 			whitePlayer = new NetworkPlayer(PieceColor.WHITE);
 			blackPlayer = new LocalPlayer(PieceColor.BLACK);
 			ClientWindow.setColorLabelText("Color: White");
 			ClientWindow.setTurnLabelText("Your turn.");
 		} else {
 			whitePlayer = new LocalPlayer(PieceColor.WHITE);
 			blackPlayer = new NetworkPlayer(PieceColor.BLACK);
 			ClientWindow.setColorLabelText("Color: Black");
 			ClientWindow.setTurnLabelText("Opponent's turn.");
 		}
 	}
 	
 	public Board getBoard() {
 		return theBoard;
 	}
 	
 	public void incrementGameCounter() {
 		gameCounter++;
 		ClientWindow.setTurnNumberLabelText("Turn: " + (gameCounter + 1));
 		if(localGame) {
 			setLocalTurnCounter();
 		} else {
 			setNetworkTurnCounter();
 		}
 	}
 	
 	private void setNetworkTurnCounter() {
 		if(gameCounter % 2 == 0 &&
 				whitePlayer.getClass().getSimpleName().equals("LocalPlayer")) {
 			ClientWindow.setTurnLabelText("Your turn.");
 		} else if(gameCounter % 2 == 0 &&
 				blackPlayer.getClass().getSimpleName().equals("LocalPlayer")) {
 			ClientWindow.setTurnLabelText("Your turn.");
 		} else {
 			ClientWindow.setTurnLabelText("Opponent's turn.");
 		}
 	}
 
 	private void setLocalTurnCounter() {
 		if(gameCounter % 2 == 0) {
 			ClientWindow.setTurnLabelText("White's turn.");
 		} else {
 			ClientWindow.setTurnLabelText("Black's turn.");
 		}
 	}
 
 	public int getGameCounter() {
 		return gameCounter;
 	}
 	
 	public Player getWhitePlayer() {
 		return whitePlayer;
 	}
 	
 	public Player getBlackPlayer() {
 		return blackPlayer;
 	}
 	
 	public ClientHandler getClientHandler() {
 		return clientHandler;
 	}
 	
 	/**
 	 * Gets player that has the specified color.
 	 * @param color
 	 * @return The player that is the color given. If the color given is NONE, null is returned.
 	 */
 	public Player getPlayerForColor(PieceColor color) {
 		if(color.equals(PieceColor.WHITE)){
 			return getWhitePlayer();
 		} else if(color.equals(PieceColor.BLACK)){
 			return getBlackPlayer();
 		} else {
 			return null;
 		}
 	}
 }
