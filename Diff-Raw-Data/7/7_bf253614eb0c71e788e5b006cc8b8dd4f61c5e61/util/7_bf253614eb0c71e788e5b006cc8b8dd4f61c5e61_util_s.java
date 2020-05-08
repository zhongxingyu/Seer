 package com.emapco.rps.util;
 
 import javax.swing.*;
 
 public class Util {
 
     private static String getCPU() {
        int cpuInt = (int) (Math.random() * 3);
         if (cpuInt == 0)
             return "rock";
         else if (cpuInt == 1)
             return "paper";
         else
             return "scissors";
     }
 
 
     public static void checkWinner(String player) {
         String cpu = getCPU();
         JOptionPane.showMessageDialog(null, "Rock, Paper, Scissors,\nI rolled a " + cpu + "!");
 
         if (cpu.equalsIgnoreCase("rock")) {
             Display.displayRock(cpu);
         }
         else if (player.equalsIgnoreCase("paper")) {
             Display.displayPaper(cpu);
         }
         else if (player.equalsIgnoreCase("scissors")) {
             Display.displayScissor(cpu);
         }
         else {
             JOptionPane.showMessageDialog(null, "Not a valid choice");
         }
     }
 
 }
