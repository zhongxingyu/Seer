 package reversi.game.controller;
 
 import java.util.List;
 
 import base.game.messages.DefaultMessageHandler;
 import base.game.messages.MessageHandler;
 import reversi.models.ReversiPlayer;
 import reversi.server.models.ReversiSettings;
 
 public class ReversiControllerSet {
 
 	private ReversiBoardController boardController;
 	private ReversiInputController inputController;
 	private ReversiPlayerController playerController;
 	private ReversiTurnController turnController;
 	private ReversiDisplayController displayController;
 	private MessageHandler messageHandler;
 
 	public ReversiControllerSet() {
 	}
 
 	public ReversiControllerSet(ReversiBoardController boardController,
 			ReversiInputController inputController,
 			ReversiPlayerController playerController,
 			ReversiTurnController turnController, 
 			ReversiDisplayController displayController,
 			MessageHandler messageHandler) {
 
 		this.boardController = boardController;
 		this.inputController = inputController;
 		this.playerController = playerController;
 		this.turnController = turnController;
		this.setDisplayController(displayController);
 		this.messageHandler = messageHandler;
 	};
 
 	public static ReversiControllerSet controllerSetWithSettings(
 			ReversiSettings settings) {
 		ReversiControllerSet set = new ReversiControllerSet();
 		Integer difficulty = settings.getDifficulty();
 
 		ReversiBoardController boardController = new ReversiBoardController();
 		set.setBoardController(boardController);
 
 		ReversiInputController inputController = ReversiInputController
 				.defaultServerController(difficulty);
 		set.setInputController(inputController);
 
 		List<ReversiPlayer> players = settings.getPlayers();
 		ReversiPlayerController playerController = new ReversiPlayerController(
 				players);
 		set.setPlayerController(playerController);
 
 		ReversiTurnController turnController = new ReversiTurnController();
 		set.setTurnController(turnController);
 
 		DefaultMessageHandler messageHandler = new DefaultMessageHandler();
 		set.setMessageHandler(messageHandler);
 
 		return set;
 	}
 
 	public ReversiBoardController getBoardController() {
 		return boardController;
 	}
 
 	public void setBoardController(ReversiBoardController boardController) {
 		this.boardController = boardController;
 	}
 
 	public ReversiInputController getInputController() {
 		return inputController;
 	}
 
 	public void setInputController(ReversiInputController inputController) {
 		this.inputController = inputController;
 	}
 
 	public ReversiPlayerController getPlayerController() {
 		return playerController;
 	}
 
 	public void setPlayerController(ReversiPlayerController playerController) {
 		this.playerController = playerController;
 	}
 
 	public ReversiTurnController getTurnController() {
 		return turnController;
 	}
 
 	public void setTurnController(ReversiTurnController turnController) {
 		this.turnController = turnController;
 	}
 
 	public MessageHandler getMessageHandler() {
 		return messageHandler;
 	}
 
 	public void setMessageHandler(MessageHandler messageHandler) {
 		this.messageHandler = messageHandler;
 	}
 
 	public ReversiDisplayController getDisplayController() {
 		return displayController;
 	}
 
 	public void setDisplayController(ReversiDisplayController displayController) {
 		this.displayController = displayController;
 	}
 }
