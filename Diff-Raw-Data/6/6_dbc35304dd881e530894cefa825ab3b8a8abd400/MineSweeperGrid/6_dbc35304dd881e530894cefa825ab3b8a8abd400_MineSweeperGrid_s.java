
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 import java.util.ArrayList;
 import java.util.Random;
 import javax.swing.BorderFactory;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.MouseInputListener;
 
 
 /**
  *
  * @author Brandon
  */
 public class MineSweeperGrid extends JPanel implements MouseInputListener, ActionListener
 {
     private Random rand;
     public int numMines, numTilesW, numTilesH, score, flagCount;
     private ArrayList<Point> mineLocs;
     private ArrayList<GridPanel> panels, bombPanels;
     private Graphics g;
     private MineSweeper sweeper;
     private SpriteComponent happyFace, sadFace, coolFace;
     private SpriteComponent[] scoreNums, flagNums, sNums;
     private JPanel scorePanel, flagPanel, facePanel;
     private Timer gameUpdate;
     private boolean gameOver;
     private String level;
     /**
      * Initializes a MineSweeperGrid with a user determined number of squares,
      * a score panel, a display of how many flags you have left, and a reset
      * button.
      * 
      * @param board - the level of difficulty the board should be
      * @param ms - the MineSweeper frame to put the grid on
      */
     public MineSweeperGrid(String board, MineSweeper ms)
     {
         super(null);
         sweeper = ms;
         gameOver = false;
         level = board;
         
         // Determine the difficulty and set the board's settings accordingly
         if(board.equalsIgnoreCase("beginner"))
         {
             numMines = 10;
             flagCount = 10;
             numTilesW = 9;
             numTilesH = 9;
         }
         else if(board.equalsIgnoreCase("intermediate"))
         {
             numMines = 40;
             flagCount = 40;
             numTilesW = 16;
             numTilesH = 16;
         }
         else if(board.equalsIgnoreCase("expert"))
         {
             numMines = 99;
             
             flagCount = 99;
             numTilesW = 24;
             numTilesH = 20;
         }
         
         sNums = new SpriteComponent[10];
         for(int i = 0; i < 10; i++)
         {
             if(i == 0)
                 sNums[i] = new SpriteComponent("10"); // The sprite for 0 is at location 10, don't ask
             else
                 sNums[i] = new SpriteComponent(Integer.toString(i));
             sNums[i].setVisible(true);
         }
         
         
         
         Point curMine = new Point(0, 0);
         mineLocs = new ArrayList<Point>();
         panels = new ArrayList<GridPanel>();
         bombPanels = new ArrayList<GridPanel>();
         this.rand = new Random(System.nanoTime());
         do
         {
             do
             {
                 curMine = new Point(rand.nextInt(numTilesW), rand.nextInt(numTilesH));
             }while(mineLocs.contains(curMine)); // Make sure no two mines are in the same spot
             mineLocs.add(curMine);
         }while(mineLocs.size() != numMines);
         for(int x = 0; x < numTilesW; x++)
             for(int y =0; y < numTilesH; y++)
             {
                 if( mineLocs.contains(new Point(x, y)) )
                 {
                     panels.add(new GridPanel(x*20,40 + y*20, true)); // Add a bomb if it matches the mine location
                     bombPanels.add(panels.get(panels.size()-1));
                 }
                 else
                     panels.add(new GridPanel(x*20, 40 + y*20, false));
                 this.add(panels.get(panels.size()-1));
             }
        
         for( GridPanel curPanel : panels )
         {
             int mineCount = 0;
             
             if(!curPanel.isBomb())
             {
                 for(int x = -1; x < 2; x++)
                     for(int y = -1; y < 2; y++)
                     {
                         // Here we count the number of mines directly surrounding the current panel, 
                         // but making sure not to loop around
                         if(panels.indexOf(curPanel)+y+(x*numTilesH) >= 0 && panels.indexOf(curPanel)+y+(x*numTilesH) < numTilesW*numTilesH)
                                 if(panels.get(panels.indexOf(curPanel)+y+(x*numTilesH)).isBomb())
                                    if(!(y == -1 && panels.indexOf(curPanel) % 9 == 0))
                                        if(!(y == 1 && (panels.indexOf(curPanel) + 1) % 9 == 0))
                                             mineCount++;
                                     
                     }
                 curPanel.setNum(mineCount);
             }
             else
                 curPanel.setNum(10); // again, 10 is 0
         }
         
         sweeper.setSize(panels.get(panels.size()-1).getX() + 25, panels.get(panels.size()-1).getY() + 75);
 
         
         facePanel = new JPanel(null);
         facePanel.setName("facePanel");
         facePanel.setBorder(BorderFactory.createRaisedBevelBorder());
         facePanel.setLocation( (int) ((sweeper.getWidth()/2)-12), 10 );
         facePanel.addMouseListener(this);
         //facePanel.setBounds((int) ((sweeper.getWidth()/2)-12), 10, 24, 24);
         facePanel.setSize(24, 24);
         happyFace = new SpriteComponent("happyFace");
         happyFace.setLocation(2, 2);
         sadFace = new SpriteComponent("sadFace");
         sadFace.setLocation(2, 2);
         coolFace = new SpriteComponent("coolFace");
         coolFace.setLocation(2, 2);
         facePanel.add(happyFace);
         facePanel.add(sadFace);
         facePanel.add(coolFace);
         happyFace.setVisible(true);
         
         this.add(facePanel);
         
         
         
         flagPanel = new JPanel(null);
         flagPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
         flagPanel.setBounds(5, 10, 68, 24 );
         flagPanel.setBackground(Color.BLACK);
         flagNums = new SpriteComponent[3];
         flagCount = numMines;
         for(int i = 0; i < 3; i++)
         {
             flagNums[i] = new SpriteComponent("10");
             flagNums[i].setLocation((i*22)+2, 2);
             flagNums[i].setVisible(true);
             flagPanel.add(flagNums[i]);
         }
         
         flagNums[0].setNumImage((int) (flagCount/(100))%10);
         flagNums[1].setNumImage((int) (flagCount/(10))%10);
         flagNums[2].setNumImage((int) (flagCount)%10);
         
         this.add(flagPanel);
         
         scorePanel = new JPanel(null);
         scorePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
         scorePanel.setBounds(sweeper.getWidth() - 73, 10, 68, 24 );
         scorePanel.setBackground(Color.BLACK);
         scoreNums = new SpriteComponent[3];
         for(int i = 0; i < 3; i++)
         {
             
             scoreNums[i] = new SpriteComponent("10");
             scoreNums[i].setLocation((i*22)+2, 2);
             scoreNums[i].setVisible(true);
             scorePanel.add(scoreNums[i]);
         }
         this.add(scorePanel);
         
         g = getGraphics();
         this.update(g);
         
         gameUpdate = new Timer(1000, this);
         gameUpdate.setActionCommand("update");
         //gameUpdate.start();
     }
     
     /**
      * Every second add one to the score, and update the scoreboard
      */
     public void updateScore()
     {
         score++;
         scoreNums[2].setNumImage(score%10);
         scoreNums[1].setNumImage(((int) (score/10)%10));
         scoreNums[0].setNumImage(((int) (score/100))%10);
         //scorePanel.setBounds(sweeper.getWidth() - 78, 10, 68, 24 );
         
     }
     
     /**
      * Loop through all the bombs and if one of them isn't flagged, break out of the method,
      * else set things up for game over
      * @return - returns whether or not all of the bombs are marked by flags
      */
     private boolean checkForWin()
     {
         for(GridPanel curPanel : bombPanels)
         {
             if(!curPanel.isFlagged())
                 return false;
         }
         gameUpdate.stop();
         JOptionPane.showMessageDialog(sweeper, "You Win!\nIt took you: " + score + " seconds!");
         happyFace.setVisible(false);
         coolFace.setVisible(true);
         return true;
         
     }
     
     /**
     * Recursively reveal grid panels as long as there isn't a bomb or number
     **/
     private void reveal(GridPanel gP)
     {
         int index = panels.indexOf(gP);
         if(!gP.getClicked())
         {
             if(index != -1)
             {
                 gP.setClicked(true);
                 if( gP.getNum() == 0 )
                 {
                    if(index < panels.size() - 1 && gP.getY() < 40+((numTilesH-1)*20))
                        reveal(panels.get(index+1));
                    if(index > 0 && gP.getY() > 40)
                        reveal(panels.get(index-1));
                    if(index >= numTilesH)
                        reveal(panels.get(index-numTilesH));
                    if(index < panels.size() - 1 - numTilesH)
                        reveal(panels.get(index+numTilesH));
                 }
             }
         }
     }
     
     @Override
     public void mouseClicked(MouseEvent e) 
     {
         // If the facePanel is clicked ask if user would like to start a new game
         if(e.getSource().getClass() == JPanel.class && e.getButton() == MouseEvent.BUTTON1)
             if(((JPanel) e.getSource()).getName().equalsIgnoreCase("facePanel"))
             {
                 ((JPanel) e.getSource()).setBorder(BorderFactory.createLoweredBevelBorder());
                 if(JOptionPane.showConfirmDialog(sweeper, "Would you like to restart?", "New Game", JOptionPane.YES_NO_OPTION)
                         == JOptionPane.YES_OPTION)
                     sweeper.setDifficulty(level);
                 else
                     ((JPanel) e.getSource()).setBorder(BorderFactory.createRaisedBevelBorder());
             }
         
         // If a grid panel is clicked, marked it as so unless it has a flag on it, or
         // is already clicked. Make sure the game is running first
         if(e.getSource().getClass() == GridPanel.class && e.getButton() == MouseEvent.BUTTON1)
         {
             if(!gameUpdate.isRunning() && !gameOver)
                 gameUpdate.start();
             if(gameUpdate.isRunning())
             {
                 if(((GridPanel) e.getSource()).isFlagged() == false)
                 {
                     ((GridPanel) e.getSource()).setMarked(false);
 
                     reveal((GridPanel) e.getSource());
                     if(((GridPanel) e.getSource()).isBomb())
                     {
                         gameUpdate.stop();
                         happyFace.setVisible(false);
                         sadFace.setVisible(true);
                         for(GridPanel bomb : bombPanels)
                         {
                             bomb.setFlagged(false);
                             bomb.setMarked(false);
                             reveal(bomb);
                         }
                         gameOver = true;
 
                     }
                 }
             }
         }
         // Place a flag, a question mark, or clear the right clicked square
         // Make sure game is running first
         if(e.getSource().getClass() == GridPanel.class && e.getButton() == MouseEvent.BUTTON3)
         {
             if(!gameUpdate.isRunning() && !gameOver)
                 gameUpdate.start();
             if(gameUpdate.isRunning())
             {
                 if(((GridPanel) e.getSource()).getClicked() == false)
                 {
                     if(((GridPanel) e.getSource()).isFlagged())
                     {
                         flagCount++;
                         ((GridPanel) e.getSource()).setFlagged(false);
                         ((GridPanel) e.getSource()).setMarked(true);
                         flagNums[0].setNumImage((int) (flagCount/(100))%10);
                         flagNums[1].setNumImage((int) (flagCount/(10))%10);
                         flagNums[2].setNumImage((int) (flagCount)%10);
                         //flagPanel.update(flagPanel.getGraphics());
 
                     }
                     else if(((GridPanel) e.getSource()).isMarked())
                         ((GridPanel) e.getSource()).setMarked(false);
                     else 
                     {
                         if(flagCount > 0)
                         {
                             ((GridPanel) e.getSource()).setFlagged(true);
                             flagCount--;
                             flagNums[0].setNumImage((int) (flagCount/(100))%10);
                             flagNums[1].setNumImage((int) (flagCount/(10))%10);
                             flagNums[2].setNumImage((int) (flagCount)%10);
                             gameOver = !checkForWin();
                         }
                         //flagPanel.update(flagPanel.getGraphics());
                     }
                 }
             }
         }
         //this.update(this.getGraphics());
     }
     @Override
     public void mousePressed(MouseEvent e) 
     {
         //if(e.getSource().getClass() == GridPanel.class)
             //((GridPanel) e.getSource()).setDown(true);
     }
     @Override
     public void mouseReleased(MouseEvent e) 
     {
         //if(e.getSource().getClass() == GridPanel.class)
             //if(((GridPanel) e.getSource()).getDown())
                 //reveal((GridPanel) e.getSource());
     }
     @Override
     public void mouseEntered(MouseEvent e) 
     {
         // If mouse hovers over a square or facePanel, highlight it
         if(e.getSource().getClass() == GridPanel.class)
             if(!((GridPanel) e.getSource()).getClicked())
                 ((GridPanel) e.getSource()).setHover(true);
         if(e.getSource().getClass() == JPanel.class)
             if(((JPanel) e.getSource()).getName().equalsIgnoreCase("facePanel"))
             {
                 facePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.RED));
                 //facePanel.update(this.getGraphics());
             }
     }
     @Override
     public void mouseExited(MouseEvent e) 
     {
         // Make sure to clear the highlight when mouse is moved
         if(e.getSource().getClass() == GridPanel.class)
             if(!((GridPanel) e.getSource()).getClicked())
             ((GridPanel) e.getSource()).setHover(false);
         if(e.getSource().getClass() == JPanel.class)
             if(((JPanel) e.getSource()).getName().equalsIgnoreCase("facePanel"))
             {
                 facePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                 //facePanel.update(this.getGraphics());
             }
     }
     @Override
     public void mouseDragged(MouseEvent e) 
     {
     }
     @Override
     public void mouseMoved(MouseEvent e) 
     {
     }
     
     @Override
     public void actionPerformed(ActionEvent e) 
     {
         // Check all action commands to see if they are fired, and do as they say
         if(e.getActionCommand().equals("newBeginnerGame"))
         {
             sweeper.setDifficulty("beginner");
         }
         if(e.getActionCommand().equals("newIntermediateGame"))
         {
             sweeper.setDifficulty("intermediate");
         }
         if(e.getActionCommand().equals("newExpertGame"))
         {
             sweeper.setDifficulty("expert");
         }
         if(e.getActionCommand().equals("quitGame"))
         {
             sweeper.dispose();
             System.exit(0);
         }
         if(e.getActionCommand().equals("showAboutBox"))
         {
             JOptionPane.showMessageDialog(sweeper, "This is MineSweeper\nThe goalof the game is to mark all "
                     + "of the mines with flags (by right clicking a square) without uncovering any.\n"
                     + "If you left click on a square that is a bomb, you lose.\nIf you would like to start a new game,"
                     + " at any time, you may click the yellow face.\n\nProgrammed By: Brandon Halls");
         }
         if(e.getActionCommand().equals("update"))
         {
             updateScore();
             
         }
     }
 
     
 }
