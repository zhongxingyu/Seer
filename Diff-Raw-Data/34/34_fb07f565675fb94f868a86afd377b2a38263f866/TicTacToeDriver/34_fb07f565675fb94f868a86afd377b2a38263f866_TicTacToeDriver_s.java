 package endtoend;
 
 import com.objogate.wl.swing.AWTEventQueueProber;
 import com.objogate.wl.swing.driver.JButtonDriver;
 import com.objogate.wl.swing.driver.JFrameDriver;
 import com.objogate.wl.swing.gesture.GesturePerformer;
 import com.qsoft.tictactoe.ui.view.MainWindow;
 
 import javax.swing.*;
 
 /**
  * User: luult
  * Date: 8/16/13
  * Time: 8:24 AM
  */
 public class TicTacToeDriver extends JFrameDriver
 {
     public TicTacToeDriver(int timeoutMillis)
     {
         super(new GesturePerformer(),
                 JFrameDriver.topLevelFrame(
                         named(MainWindow.MAIN_WINDOW_NAME),
                         showingOnScreen()),
                 new AWTEventQueueProber(timeoutMillis, 3000));
     }
 
     private JButtonDriver getButton(String name)
     {
         return new JButtonDriver(this, JButton.class, named(name));
     }
 
     public void clickStartButton()
     {
         getButton(MainWindow.START_BUTTON_NAME).click();
     }
 
     public void clickStopButton()
     {
         getButton(MainWindow.STOP_BUTTON_NAME).click();
     }
 }
