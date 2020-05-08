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
    public void run(){
       while(!game.isOver()){
          label.setText(""+(new Date().getTime()-timeStarted)/1000);
       }
 
    }
 }
