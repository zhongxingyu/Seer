 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package memory;
 
 import java.io.Serializable;
 
 /**
  *
  * @author Karlee Bronson
  */
 public class Location implements Serializable{
     int row;
     int column;
     Player player;
     
     
     public Location() {
         
     }
     
     public void occupyLocation(Player player){
         this.player = player;
     }
     
     public void displayFlippedCard() {
         if (this.player != null) {
             System.out.println(this.player.flippedCard);
         }
         else {
             System.out.println(" ");
         }
     }
 
     public int getRow() {
         return row;
     }
 
     public void setRow(int row) {
         this.row = row;
     }
 
     public int getColumn() {
         return column;
     }
 
     public void setColumn(int column) {
         this.column = column;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public void setPlayer(Player player) {
         this.player = player;
     }
         
      //public void displayFlippedCard(){
      // System.out.println("Card location:" + this.location);
     
//}
 }
