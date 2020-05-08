 package edu.berkeley.cs.cs162.Server;
 
 
 public class MatchMakingWorker implements Runnable {
     private GameServer server;
 
     public MatchMakingWorker(GameServer server) {
 		this.server = server;
 	}
 
 	@Override
     public void run() {
         while (true) {
         	PlayerLogic player1 = getNextAvailablePlayer();
         	PlayerLogic player2 = getNextAvailablePlayer();
        	while (player1.isDisconnected() || player2.isDisconnected()) {
        		if (player1.isDisconnected()) {
        			player1 = getNextAvailablePlayer();
        		} if (player2.isDisconnected()) {
        			player2 = getNextAvailablePlayer();
        		}
        	}
             Game game = new Game(player1.getName() + "VS" + player2.getName(), player1, player2, new GoBoard(10));
             game.begin();
             server.addGame(game);
         }
     }
 	
 	private PlayerLogic getNextAvailablePlayer() {
 		PlayerLogic player;
 		while (true) {
     		player = server.getNextWaitingPlayer();
     		if(player.startGame()) {
     			break;
     		} else 
     		{
     			System.out.println(player.getName() + " is still playing");
     		}
     	}
 		return player;
 	}
 }
