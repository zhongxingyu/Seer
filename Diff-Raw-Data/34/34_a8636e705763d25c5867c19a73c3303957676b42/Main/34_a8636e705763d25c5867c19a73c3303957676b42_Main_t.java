 class Main {
 
public static void main(String args[]) { 
         Game game = new Game();
         game.addPlayer(PlayerColor.Blue);
         game.addPlayer(PlayerColor.Orange);
         game.addPlayer(PlayerColor.Green);
         game.addPlayer(PlayerColor.Red);
         game.addPlayer(PlayerColor.Brown);
         game.addPlayer(PlayerColor.White);
 
         ScoreBoard scoreBoard = new ScoreBoard(game);
 
         ScoreBoardKeyListener scoreBoardController = new ScoreBoardKeyListener(game,scoreBoard);
        try {
         scoreBoard.run();
        }
        catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
     } 
 
 }
