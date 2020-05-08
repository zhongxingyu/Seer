 package com.github.kpacha.jkata.tennis;
 
 public class Tennis {
 
     private int playerOneScored = 0;
 
     public String getScore() {
	if (playerOneScored == 4)
	    return "Player 1 wins";
 	return (15 * playerOneScored) + " - 0";
     }
 
     public void playerOneScores() {
 	playerOneScored++;
     }
 }
