 package org.aoeu.jgames.breakout;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 /**
  * @author Luis Martín Canaval Sánchez
  */
 public class BreakoutGui {
 
     static final int WIDTH = 960;
     static final int HEIGHT = 540;
     static final int DELAY = 150;
     Breakout breakout;
     BreakoutPanel breakoutPanel;
     int step = 5;
 
     public void go() {
         JFrame frame = new JFrame("Breakout INSO");
         breakout = new Breakout(WIDTH, HEIGHT);
         breakoutPanel = new BreakoutPanel(breakout);
         frame.getContentPane().add(breakoutPanel);
         frame.pack();
         frame.setVisible(true);
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent ke) {
                 switch (ke.getKeyChar()) {
                     case 'a':
                         if (breakout.barLeft > 0) {
                             breakout.barLeft -= step;
                         }
                         break;
                     case 'd':
                         int barRight = breakout.barLeft + breakout.barWidth;
                         if (barRight < breakout.width) {
                             breakout.barLeft += step;
                         }
                         break;
                 }
             }
         });
         new Thread(new Runnable() {
             @Override
             public synchronized void run() {
                 try {
                     while (!breakout.gameOver) {
                         wait(DELAY);
                        breakout.moveSphere();
                         breakoutPanel.repaint();
                     }
                 } catch (InterruptedException ignored) {
                 }
             }
         }).start();
     }
 }
