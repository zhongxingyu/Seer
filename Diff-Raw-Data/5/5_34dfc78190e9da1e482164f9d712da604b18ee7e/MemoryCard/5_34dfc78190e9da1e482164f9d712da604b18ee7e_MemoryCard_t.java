 package edu.ucsb.cs56.projects.games.memorycard;
         
 /** Class MemoryCard creates objects for cards
  * @author Mathew Glodack, Christina Morris
  * @version CS56, S13, 5/7/13
  */
 
 public class MemoryCard{
    
     int val;
     boolean flipped;
     
     /**Default constructor for MemoryCard (sets flipped as false)	
      * 
      */
     public MemoryCard(){
         flipped = false;
     }
     
     /**Constructor to set value (flipped still set as false)
      * @param tVal 
      */
     public MemoryCard(int tVal){
         flipped = false;
         val = tVal;
     }
     
     /**Returns whether card is flipped
      * @return True or False whether the card is flipped 
      */
     public boolean isFlipped(){
         return flipped;
     }
     
     /**Changes value of flipped (doesnt actually flip card)
      * 
      */
     public void flip(){
 	flipped = flipped ? false : true;
     }
     
     /**Returns value
      * @return The value on the card 
      */
     public int getVal(){
         return val;
     }
     
     /**Method to set value
     * @param value represents value on the card
      */
     public void setVal(int value){
         val=value;
     }
     
     /**Equals method to check if values are equal
     * @param o is an Object
      * @return boolean True or False if values are equal
      */
     public boolean Equals(Object o){
         final MemoryCard second = (MemoryCard) o;
         return(this.getVal()==second.getVal());
     }
 }
