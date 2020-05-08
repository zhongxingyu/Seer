 package com.hex.network;
 
 public interface NetworkCallbacks {
 
     /**
      * @param gameData
      *            string encoding of game data
      * 
      *            this method is called to start a new game. When the call back
      *            happens you need to make a new game with the data, atach the
      *            players and start the game
      */
     public void newGame(String gameData);
 
     /**
      * @return game data sting or null
      * 
      *         This is the request for a new game Send a string with the data or
      *         null to keep the old game
      */
    public String newGameRequest();
 
     /**
      * @param turnNumer
      *            the turn to undo to
      * @return true to undo and false not to
      */
     public boolean undoRequest(int turnNumer);
 
     /**
      * @param the
      *            turn number you must undo back to
      */
     public void undo(int turnNumer);
 
     /**
      * this is called if the game encounters an error
      */
     public void error(Errors error);
 
     /**
      * @param message
      *            the message you need to show the player
      */
     public void chat(String message);
 
 }
