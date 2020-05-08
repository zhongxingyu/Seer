 package com.github.kpacha.jkata.tennis;
 

 public class Tennis {
 
     private int playerOneScored = 0;
 
     public String getScore() {
 	return (15 * playerOneScored) + " - 0";
     }
 
     public void playerOneScores() {
 	playerOneScored++;
     }
 }
