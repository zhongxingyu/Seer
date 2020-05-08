 package dataStructure;
 
 /**
  * @(#)Connections.java
  *
  *
  * @author Pacmans
  * @version 20. Marts 2012
  */
 
 
 public class Connections {
   Connection[][] connections;
 
   public void addConnection(){
 
   }
 
  //TODO resizeable + compare, floor, ceiling
   //TODO enlarge coordinate restraints to ensure inclusion of all connections within
   //TODO add priority constraint
   /**
    *  Returns all connections within the given coordinates
    */
   public Connection[] getConnections(int x1, int x2, int y1, int y2){
    //TODO Resizeable
     Connection[] temp = new Connection[2];
     int tsize = 0;
     int i = floor(x1); //find largest key equal or smaller than x1
     int j = ceiling(x2); //find smallest key equal or greater than x2
 
     for(i; i<j; i++){
       int k = floor(connections[i], y1); //find largest key equal or smaller than y1, within array
       int l = ceiling(connections[i], y2); //find smallest key equal or greater than y2, within array
 
       connections[10].length = 100;
       connections[9].length = 50;
 
       for(k; k<l; k++){
         temp[tsize] = connections[i][k]);
         tsize++;
       }
     }
     return temp;
   }
 
   private int floor(double k) {
     int x = -1;
     for(int i = 0; i<connections.length-1; i++){
      if(connections[i].getX() =< k) x = i;
       else break;
     }
     return x;
   }
 }
