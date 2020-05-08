 package com.eatthatgame.robo;
 
 import java.util.ArrayList;
 
 public class League {
     public League(ArrayList contenders) {
         int randomContenderID;
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender a = (Contender) contenders.remove(randomContenderID);
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender b = (Contender) contenders.remove(randomContenderID);
             
         System.out.println("Contender A: " + a.name + " vs. Contender B: " + b.name);
         System.out.println("FIGHT!");
         
         int roundCount = 0;
         while(a.alive() && b.alive()) {
             a.hit(b.attack() - a.defend());
            b.hit(a.attack() - a.defend());
             
             roundCount++;
             System.out.println("Round: " + roundCount);
 
             System.out.println(a.name + " health: " + a.getHealth());
             System.out.println(b.name + " health: " + b.getHealth());
         }
         if(a.alive()) {
             System.out.println(a.name + " wins! (in " + roundCount + " rounds)");
        } else if(b.alive()) {
             System.out.println(b.name + " wins! (in " + roundCount + " rounds)");
        } else {
            System.out.println("Mutual Annihilation, both contenders are dead after " + roundCount + " rounds)");
         }
     }
 }
