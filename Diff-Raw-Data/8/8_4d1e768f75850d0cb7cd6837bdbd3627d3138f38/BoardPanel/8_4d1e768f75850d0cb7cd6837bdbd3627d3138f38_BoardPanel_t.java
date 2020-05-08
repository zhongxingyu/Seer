 package tictactoe.applet;
 import tictactoe.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * A {@code JPanel} representation of a TicTacToe {@code Board}. Uses {@code JButton}s to
  * represent each space.
  * <p>
  * The action command for each button is set to a string representation of the
  * index of the space it represents.
  *
  * @author Todd Taomae
  */
 @SuppressWarnings("serial")
 public class BoardPanel extends JPanel implements Runnable
 {
     private TicTacToeDriver driver;
     private Player playerX;
     private Player playerO;
     private JButton[] spaces;
 
     /**
      * Constructs a new {@code BoardPanel} with the specified types of
      *
      * @param   x   type of the X {@code Player}
      * @param   o   type of the o {@code Player}
      */
     public BoardPanel(Player x, Player o)
     {
         this.playerX = x;
         this.playerO = o;
 
         this.driver = new TicTacToeDriver(this.playerX, this.playerO);
 
         this.setLayout(new GridLayout(3, 3));
 
         // initialize buttons
         this.spaces = new JButton[9];
         for (int i = 0; i < this.spaces.length; i++) {
             JButton b = new JButton();
             this.spaces[i] = b;
 
             b.setActionCommand("" + i);
             b.setEnabled(false);
             b.setPreferredSize(new Dimension(50, 50));
             b.addActionListener(this.playerX);
             b.addActionListener(this.playerO);
             this.add(b);
 
         }
     }
 
     /**
      * Returns the winner of the current game.
      *
      * @return  the winner of the current game.
      */
     public Mark getWinner()
     {
         return this.driver.getWinner();
     }
 
     /**
      * Starts a game and updates this {@code BoardPanel} at the end of each turn.
      * <p>
      * Notifies all threads waiting on this object at the start and end of each execution.
      */
     public void run()
     {
         // clear and enable buttons
         for (JButton b : this.spaces) {
             b.setText("");
             b.setEnabled(true);
         }
 
         this.driver.setPlayerX(this.playerX);
         this.driver.setPlayerO(this.playerO);
         this.driver.newBoard();
         new Thread(this.driver).start();
 
         // notify for start of game
         synchronized(this) {
             this.notifyAll();
         }
 
        synchronized(this.driver) {
            while (this.driver.getWinner() == Mark.NONE) {
                 try {
                     // wait until driver notifies after a move is played
                     this.driver.wait();
                 } catch (InterruptedException ie) {
                     // if thread is interruped, end game
                     System.out.println("thread interrupted");
                     break;
                 }
 
                // update buttons
                 Mark[] state = this.driver.getState();
                 for (int i = 0; i < state.length; i++) {
                     switch(state[i]) {
                         case X:
                             this.spaces[i].setText("X");
                             this.spaces[i].setEnabled(false);
                             break;
                         case O:
                             this.spaces[i].setText("O");
                             this.spaces[i].setEnabled(false);
                             break;
                         case NONE:
                             this.spaces[i].setText("");
                             this.spaces[i].setEnabled(true);
                             break;
                     }
                 }
             }
         }
 
         for (JButton b : this.spaces) {
             b.setEnabled(false);
         }
 
         // notify for end of game
         synchronized(this) {
             this.notifyAll();
         }
     }
 
     /**
      * Creates new players for this {@code BoardPanel}.
      *
      * @param   x   player type for X player
      * @param   o   player type for O player
      */
     public void newPlayers(Player x, Player o)
     {
         this.playerX = x;
         this.playerO = o;
 
         for (JButton b : this.spaces) {
             b.addActionListener(this.playerX);
             b.addActionListener(this.playerO);
 
         }
     }
 
 
 
     public static void main(String[] args)
     {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGui();
             }
         });
     }
 
     public static void createAndShowGui()
     {
         JFrame frame = new JFrame("TicTacToe");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         BoardPanel p = new BoardPanel(new MousePlayer(), new AlphaBetaPlayer(10));
         frame.add(p);
 
         frame.pack();
         frame.setVisible(true);
 
         new Thread(p).start();
     }
 }
