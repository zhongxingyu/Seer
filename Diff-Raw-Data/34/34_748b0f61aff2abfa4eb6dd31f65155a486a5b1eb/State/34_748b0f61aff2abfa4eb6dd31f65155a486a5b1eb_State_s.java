 package com.tipumc;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 public class State {
 
     public State(Map map, Position player, ArrayList<Position> boxes, ArrayList<Direction> playerPath, State parent)//More parameters for player and boxes
     {
         this.map = map;
         this.player = player;
         this.boxes = boxes;
         this.playerPath = playerPath;
         this.parent = parent;
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
     
     private ArrayList<Position> findGoals(Vector<String> board)
     {
         ArrayList<Position> goals = new ArrayList<Position>();
         for (int y = 0; y < board.size(); ++y)
         {
             for (int x = 0; x < board.get(y).length(); ++x)
             {
                 char c = board.get(y).charAt(x);
                 if (c == '.' | c == '*' | c == '+')
                     goals.add(new Position(x, y));
             }
         }
         return goals;
     }
     
     public boolean isFinal(){
        int numOfBoxOnGoal = 0;
         for (Position boxPos : boxes){
             for(Position goalPos : goals){
                 if(boxPos.equals(goalPos)){
                     numOfBoxOnGoal ++;
                 }
             }
         }
         return(numOfBoxOnGoal == goals.size());
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
         return ((this.map.isEmpty(x, y)|this.map.isGoal(x, y)) & !this.isBox(x, y));
     }
 
     /*
     is* functions for convenience in this class which only forwards
      */
     public boolean isBox(int x, int y)
     {
         for (Position pos : boxes)
         {
             if (x == pos.x & y == pos.y)
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
         String stringOut = "";
         boolean boxBool, playerBool;
         for (int i = 0; i < map.getHeight(); i++) { // For y-coordinates
             for (int j = 0; j < map.getWidth(); j++) { // For x-coordinates
                 // See if there is a box with these exact coordinates
                 boxBool = false;
                 playerBool = false;
                 for (int b = 0; b < boxes.size(); b++) {
                     if (boxes.get(b).x == j & boxes.get(b).y == i) {
                         boxBool = true;
                     } 
                 }
                 // See if there is a player with these exact coordinates
                 if (player.x == j & player.y == i) {
                     playerBool = true;
                 }
                 // Add to the string
                 if (boxBool & map.mapMatrix[i][j] == '.') {
                     stringOut += '*';
                 } else if (playerBool & map.mapMatrix[i][j] == '.') {
                     stringOut += '+';
                 } else if (playerBool){
                     stringOut += '@';
                 } else if (boxBool) {
                     stringOut += '$';
                 } else {
                     stringOut += map.mapMatrix[i][j];
                 }
             }
             if (i < map.getHeight()-1) { // No new line on last line
                 stringOut += System.getProperty("line.separator"); // New line
             }
         }
         return stringOut;
     }
    public Vector<String> inverse(){
       Vector <String> inverseMap = new Vector<String>();
          for (int i = 0; i < map.size(); i++) {
              String stringLine = new String();
            for (int j = 0; j < map.get(i).length(); j++) {
                char c = map.get(i).charAt(j);
                if (c=='$'){
                    stringLine += "."; 
                }else if(c=='.'){
                    stringLine += "$";
                }
                else{
                    stringLine += c;
                }
            }
            inverseMap.add(stringLine);   
        }
          return inverseMap;
    }
             
 
     /**
      * We don't necessarily have to store objects like this but it would work as a start.
      */
     public ArrayList<Position> boxes;
     public ArrayList<Position> goals;
     public Position player;
     public Map map;
     public ArrayList<Direction> playerPath;
     public State parent = null;
 }
