 package edu.ucsb.cs56.S12.issues.issue0000805;
 import java.util.*;
 import javax.swing.*;
 public class MemoryGameTimer implements Runnable{
    private MemoryGameComponent game;
    private long timeStarted;
    private JLabel label;
    public MemoryGameTimer(MemoryGameComponent c, JLabel l){
       game=c;
       label=l;
       timeStarted=new Date().getTime();
 
    }
   /**  Updates time until the game is over.
        When game is over it resets the time to 0 and restarts.
        Run stops only when the Thread is stopped by the user.
   */
    public void run(){
      while(true){
       while(!game.isOver()){
          label.setText(""+(new Date().getTime()-timeStarted)/1000);
       }
      timeStarted=new Date().getTime();
      game.setIsOver(false);
      }

 
    }
 }
