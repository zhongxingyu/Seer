 package reversi.server.models;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 import reversi.game.ReversiGame;
 import reversi.models.ReversiPlayer;
 import reversi.parser.ReversiParser;
 import reversi.server.ReversiGameFactory;
 import reversi.server.ReversiServerResponse;
 import reversi.server.commands.ReversiCommand;
 import reversi.server.commands.ReversiCommand.ReversiCommandType;
 import reversi.server.commands.ReversiCommandReader;
 import reversi.server.controllers.exceptions.ExitException;
 import reversi.server.display.ReversiAsciiDisplayController;
 import base.server.GameLobby;
 import base.server.ServerLobbyManager;
 
 /**
  * Lobby for Reversi.
  * 
  * Will wait until two players connect, or the first player decides to play
  * against a single player.
  * 
  * @author dereekb
  * 
  */
 public class ReversiLobby extends GameLobby<ReversiRemoteClient> {
 
 	private ReversiRemoteClient host;
 	private final ReversiPlayer aiPlayer = new ReversiPlayer("AI");
 	private final ReversiGameFactory gameFactory = new ReversiGameFactory();
 	private final ReversiSettings settings = new ReversiSettings();
 	
 	private boolean selectedDifficulty = false;
 	private boolean selectedGameMode = false;
 
 	public ReversiLobby(ReversiRemoteClient host,
 			ServerLobbyManager<?, ?, ?> manager) {
 		super(manager);
 		this.setHost(host);
 		this.getClients().add(host);
 	}
 
 	public ReversiLobby(ReversiRemoteClient host,
 			ServerLobbyManager<?, ?, ?> manager, String lobbyName) {
 		super(manager, lobbyName);
 		this.setHost(host);
 		this.getClients().add(host);
 	}
 
 	@Override
 	public void runLobby() throws IOException {
 
 		boolean beginGame = false;
 
 		ReversiParser parser = new ReversiParser();
 		PrintWriter writer = host.getWriter();
 		Scanner scanner = host.getInputScanner();
 		writer.println("Welcome to your new lobby.");
 
 		// Keep alive while clients are still connected.
		while (getClients().size() > 0 && !beginGame) {
 
 			boolean success = true;
 
 			try {
 
 				String input = scanner.nextLine();
 				ReversiCommand command = parser.parseCommand(input);
 
 				if (command != null) {
 					success = this.processCommand(command);
 				} else {
 					success = false;
 				}
 
 			} finally {
 				if (success) {
 					ReversiServerResponse.sendOk(host);
 				} else {
 					ReversiServerResponse.sendIllegal(host);
 				}
 				
 				beginGame = this.canBeginGame();
 			}
 		}
 		
 	}
 
 	private boolean canBeginGame() {
 		
 		/*
 		 * TODO: Check that the player has selected the game type, and difficulty.
 		 */
 		
 		return (this.selectedDifficulty && this.selectedGameMode);
 	}
 
 	public boolean processCommand(ReversiCommand command) {
 		boolean success = true;
 		ReversiCommandType type = command.getType();
 
 		switch (type) {
 		case AIAI: {
 			ReversiPlayer hostPlayer = this.host.getPlayer();
 			hostPlayer.setInGame(false);
 
 			List<String> parameters = command.getParameters();
 			String address = parameters.get(0);
 			String port = parameters.get(1);
 			String thisDifficulty = parameters.get(2);
 			String theirDifficulty = parameters.get(3);
 
 			// TODO: Implement later.
 			
 			this.selectedGameMode = true;
 			this.selectedDifficulty = true;
 		}
 			break;
 		case Difficulty: {
 			List<String> parameters = command.getParameters();
 			String stringDifficulty = parameters.get(0);
 			Integer difficulty = new Integer(stringDifficulty);
 			this.settings.setDifficulty(difficulty);
 			this.selectedDifficulty = true;
 		}
 			break;
 		case Display: {
 			ReversiPlayer hostPlayer = this.host.getPlayer();
 			hostPlayer.toggleAsciiDisplay();
 		}
 			break;
 		case Exit: {
 			throw new ExitException();
 		}
 		case HumanAI: {
 			ReversiPlayer hostPlayer = this.host.getPlayer();
 			hostPlayer.setInGame(true);
 			this.selectedGameMode = true;
 		}
 			break;
 		case Black: {
 			this.host.setAsciiPiece(ReversiAsciiDisplayController.blackReversiPiece);
 			aiPlayer.setPieceAscii(ReversiAsciiDisplayController.whiteReversiPiece);
 		}
 			break;
 		case White: {
 			this.host.setAsciiPiece(ReversiAsciiDisplayController.whiteReversiPiece);
 			aiPlayer.setPieceAscii(ReversiAsciiDisplayController.blackReversiPiece);
 		}
 			break;
 		default: {
 			success = false;
 		}
 			break;
 		}
 
 		return success;
 	}
 
 	@Override
 	public void runGame() throws IOException {
 
 		List<ReversiPlayer> players = new ArrayList<ReversiPlayer>();
 		
 		for(ReversiRemoteClient client : this.clients)
 		{
 			String asciiPiece = client.getAsciiPiece();
 			ReversiPlayer player = new ReversiPlayer(asciiPiece);
 			player.setControlledByAi(false);
 			players.add(player);
 		}
 		
 		players.add(aiPlayer);		
 		this.settings.setPlayers(players);		
 		
 		ReversiGame game = this.gameFactory.createNewGame(this.settings);
 		game.playGame();
 	}
 
 	@Override
 	public void closeLobby() throws IOException {
 		super.closeLobby();
 	}
 
 	public ReversiRemoteClient getHost() {
 		return host;
 	}
 
 	public void setHost(ReversiRemoteClient host) {
 		this.host = host;
 	}
 	
 }
