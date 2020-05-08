 package edu.berkeley.cs.cs162.Client;
 
 import edu.berkeley.cs.cs162.Server.BoardLocation;
 import edu.berkeley.cs.cs162.Server.GoBoard;
 import edu.berkeley.cs.cs162.Server.StoneColor;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 import edu.berkeley.cs.cs162.Writable.ServerMessages;
 
 import java.io.IOException;
 
 abstract public class Player extends BaseClient {
 
     GoBoard board;
     boolean waitingForGames;
     private boolean sentWFGMessage;
     StoneColor currentColor;
     StoneColor opponentColor;
     String gameName;
 
     public Player(String name, byte type) {
         super(name, type);
         board = new GoBoard(10);
         waitingForGames = true;
         sentWFGMessage = false;
         gameName = "";
     }
 
     public Player(String name) {
         this(name, (byte) -1);
     }
 
     public Player() {
         this("");
     }
 
     public void setBoard(GoBoard b) {
         board = b;
     }
 
     public GoBoard getBoard() {
         return board;
     }
 
     public boolean getWaitingForGames() {
         return waitingForGames;
     }
 
     public void setWaitingForGames(boolean b) {
         waitingForGames = b;
     }
 
     public void setGameName(String s) {
         gameName = s;
     }
 
     public String getGameName() {
         return gameName;
     }
 
     public void runExecutionLoop() throws IOException {
         while (true) {
             if (waitingForGames && !sentWFGMessage) {
                 Message reply = getConnection().sendSyncToServer(MessageFactory.createWaitForGameMessage());
 
                 if (reply.isOK()) {
                     sentWFGMessage = true;
                 } else {
                     //terminate? what happens when reply is not okay?
                     break;
                 }
             } else {
                 handleMessage(getConnection().readFromServer());
             }
         }
     }
 
     protected void handleGameStart(ServerMessages.GameStartMessage m) throws IOException {
         String blackPlayerName = m.getBlackClientInfo().getName();
         String whitePlayerName = m.getWhiteClientInfo().getName();
 
         this.gameName = m.getGameInfo().getName();
         board = m.getBoardInfo().getBoard();
         waitingForGames = false;
 
         if (blackPlayerName.equals(name)) {
             currentColor = StoneColor.BLACK;
             opponentColor = StoneColor.WHITE;
         } else if (whitePlayerName.equals(name)) {
             currentColor = StoneColor.WHITE;
             opponentColor = StoneColor.BLACK;
         } else {
             currentColor = StoneColor.NONE;
             opponentColor = StoneColor.NONE;
         }
 
         System.out.println("Game " + gameName + " starting with Black player " + blackPlayerName + " and White player " + whitePlayerName + ".");
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleGameOver(ServerMessages.GameOverMessage m) throws IOException {
         String gameName = m.getGameInfo().getName();
         double blackPlayerScore = m.getBlackScore();
         double whitePlayerScore = m.getWhiteScore();
         String winner = m.getWinner().getName();
         byte reason = m.getReason();
 
         if (reason != MessageProtocol.GAME_OK) {
             String errorPlayerName = m.getErrorPlayer().getName();
             String errorMsg = m.getErrorMessage();
             System.out.println("Game " + gameName + " ended with an error by " + errorPlayerName + ": " + errorMsg + ". Black score " + blackPlayerScore + ", White score " + whitePlayerScore + ". WINNER: " + winner + "!");
         } else {
             System.out.println("Game " + gameName + " ended with Black score " + blackPlayerScore + ", White score " + whitePlayerScore + ". WINNER: " + winner + "!");
         }
 
         //game destructors
         waitingForGames = true;
         sentWFGMessage = false;
         board = null;
         currentColor = StoneColor.NONE;
         opponentColor = StoneColor.NONE;
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleMakeMove(ServerMessages.MakeMoveMessage m) throws IOException {
         String playerName = m.getPlayer().getName();
         byte type = m.getMoveType();
         BoardLocation loc = m.getLocation().makeBoardLocation();
 
         if (playerName.equals(name)) {
             if (type == MessageProtocol.MOVE_PASS) {
                 //uhh
             } else {
                 try {
                     board.makeMove(loc, currentColor);
                 } catch (GoBoard.IllegalMoveException e) {
 
                 }
 
             }
         } else {
             if (type == MessageProtocol.MOVE_PASS) {
                 //uhh
             } else {
                 try {
                     board.makeMove(loc, opponentColor);
                 } catch (GoBoard.IllegalMoveException e) {
 
                 }
             }
         }
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
 =======
     }
 
     public boolean getWaitingForGames() {
         return waitingForGames;
     }
 
     public void setWaitingForGames(boolean b) {
         waitingForGames = b;
     }
 
     public void setGameName(String s) {
         gameName = s;
     }
 
     public String getGameName() {
         return gameName;
     }
 
     public void runExecutionLoop() throws IOException {
         while (true) {
             if (waitingForGames && !sentWFGMessage) {
                 Message reply = getConnection().sendSyncToServer(MessageFactory.createWaitForGameMessage());
 
                 if (reply.isOK()) {
                     sentWFGMessage = true;
                 } else {
                     //terminate? what happens when reply is not okay?
                     break;
                 }
             } else {
                 handleMessage(getConnection().readFromServer());
             }
         }
     }
 
     protected void handleGameStart(ServerMessages.GameStartMessage m) throws IOException {
         String blackPlayerName = m.getBlackClientInfo().getName();
         String whitePlayerName = m.getWhiteClientInfo().getName();
 
         this.gameName = m.getGameInfo().getName();
         board = m.getBoardInfo().getBoard();
         waitingForGames = false;
 
         if (blackPlayerName.equals(name)) {
             currentColor = StoneColor.BLACK;
             opponentColor = StoneColor.WHITE;
         } else if (whitePlayerName.equals(name)) {
             currentColor = StoneColor.WHITE;
             opponentColor = StoneColor.BLACK;
         } else {
             currentColor = StoneColor.NONE;
             opponentColor = StoneColor.NONE;
         }
 
         System.out.println("Game " + gameName + " starting with Black player " + blackPlayerName + " and White player " + whitePlayerName + ".");
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleGameOver(ServerMessages.GameOverMessage m) throws IOException {
         String gameName = m.getGameInfo().getName();
         double blackPlayerScore = m.getBlackScore();
         double whitePlayerScore = m.getWhiteScore();
         String winner = m.getWinner().getName();
         byte reason = m.getReason();
 
         if (reason != MessageProtocol.GAME_OK) {
             String errorPlayerName = m.getErrorPlayer().getName();
             String errorMsg = m.getErrorMessage();
             System.out.println("Game " + gameName + " ended with an error by " + errorPlayerName + ": " + errorMsg + ". Black score " + blackPlayerScore + ", White score " + whitePlayerScore + ". WINNER: " + winner + "!");
         } else {
             System.out.println("Game " + gameName + " ended with Black score " + blackPlayerScore + ", White score " + whitePlayerScore + ". WINNER: " + winner + "!");
         }
 
         //game destructors
         waitingForGames = true;
         sentWFGMessage = false;
         board = null;
         currentColor = StoneColor.NONE;
         opponentColor = StoneColor.NONE;
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleMakeMove(ServerMessages.MakeMoveMessage m) throws IOException {
         String playerName = m.getPlayer().getName();
         byte type = m.getMoveType();
         BoardLocation loc = m.getLocation().makeBoardLocation();
 
         if (playerName.equals(name)) {
             if (type == MessageProtocol.MOVE_PASS) {
                 //uhh
             } else {
                 try {
                     board.makeMove(loc, currentColor);
                 } catch (GoBoard.IllegalMoveException e) {
 
                 }
 
             }
         } else {
             if (type == MessageProtocol.MOVE_PASS) {
                 //uhh
             } else {
                 try {
                     board.makeMove(loc, opponentColor);
                 } catch (GoBoard.IllegalMoveException e) {
 
                 }
             }
         }
 
         getConnection().sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
 >>>>>>> b70978eefef586adb4b41ceaa2cc1fb5f06c2212
 	public boolean isSentWFGMessage() {
 		return sentWFGMessage;
 	}
 
 	public void setSentWFGMessage(boolean sentWFGMessage) {
 		this.sentWFGMessage = sentWFGMessage;
 	}
 }
