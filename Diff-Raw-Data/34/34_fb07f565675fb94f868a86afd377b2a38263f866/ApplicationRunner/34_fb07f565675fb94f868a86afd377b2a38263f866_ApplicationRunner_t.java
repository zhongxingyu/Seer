 package endtoend;
 
 import com.qsoft.tictactoe.Main;
 import com.qsoft.tictactoe.ui.view.MainWindow;
 
 /**
  * User: luult
  * Date: 8/16/13
  * Time: 8:20 AM
  */
 public class ApplicationRunner
 {
     private  TicTacToeDriver ticTacToeDriver;
     public void startApplication()
     {
         Thread thread = new Thread("Test Application")
         {
             @Override
             public void run()
             {
                 try
                 {
                     Main.main(null);
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
             }
         };
         thread.setDaemon(true);
         thread.start();
         ticTacToeDriver = new TicTacToeDriver(3000);
         ticTacToeDriver.hasTitle(MainWindow.APPLICATION_TITLE);
     }
     public void startAGame()
     {
         ticTacToeDriver.clickStartButton();
     }
 
     public void showsStatusStartGame()
     {
         ticTacToeDriver.hasStatusStartGame();
     }
 
     public void endGame()
     {
         ticTacToeDriver.clickStopButton();
     }
 
     public void showsStatusEndGame()
     {
        ticTacToeDriver.hasStatusEndGame();
     }
 }
