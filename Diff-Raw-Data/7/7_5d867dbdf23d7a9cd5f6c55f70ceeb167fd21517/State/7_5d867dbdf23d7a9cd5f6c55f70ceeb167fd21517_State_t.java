 package com.tipumc;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 public class State {
 
     public State(Map map, Position player, ArrayList<Position> boxes)//More parameters for player and boxes
     {
         this.map = map;
         this.player = player;
         this.boxes = boxes;
     }
 
     public State(Map map, Vector<String> board) throws Exception
     {
         this.map = map;
         //Read in the initial position of the boxes and the player.
         this.player = findPlayer(board);
        this.boxes = findBoxes(board);
     }
 
     private Position findPlayer(Vector<String> board) throws Exception
     {
         for (int y = 0; y < board.size(); ++y)
         {
             for (int x = 0; x < board.get(y).length(); ++x)
             {
                 char c = board.get(y).charAt(x);
                 if (c == '@' || c == '+')
                     return new Position(x, y);
             }
         }
         throw new Exception("Could not find the player in the board");
     }
 
     private ArrayList<Position> findBoxes(Vector<String> board)
     {
         ArrayList<Position> boxes = new ArrayList<Position>();
         for (int y = 0; y < board.size(); ++y)
         {
             for (int x = 0; x < board.get(y).length(); ++x)
             {
                 char c = board.get(y).charAt(x);
                 if (c == '$' || c == '*')
                     boxes.add(new Position(x, y));
             }
         }
         return boxes;
     }
 
     
     public int getHeight()
     {
         return this.map.getHeight();
     }
     
     public int getWidth()
     {
         return this.map.getWidth();
     }
             
     /**
      *
      * @param x
      * @param y
      * @return Whether the tile is free to move onto or not
      */
     public boolean isFree(int x, int y)
     {
         return (this.map.isEmpty(x, y)|this.map.isGoal(x, y));
     }
 
     /*
     is* functions for convenience in this class which only forwards
      */
     public boolean isBox(int x, int y)
     {
        for (Position pos : boxes)
        {
            if (x == pos.x && y == pos.y)
                return true;
        }
         return false;
     }
     public boolean isEmpty(int x, int y)
     {
         return map.isEmpty(x, y);
     }
 
     public boolean isGoal(int x, int y)
     {
         return map.isGoal(x, y);
     }
 
     public boolean isWall(int x, int y)
     {
         return map.isWall(x, y);
     }
 
     public Position getPlayer()
     {
         return player;
     }
 
     /**
      *
      * @return A string to be used when debugging the AI
      */
     public String toString()
     {
         throw new NotImplementedException();
     }
 
     /**
      * We don't necessarily have to store objects like this but it would work as a start.
      */
     private ArrayList<Position> boxes;
     private Position player;
     private Map map;
 }
