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
 public class Pair implements Serializable{
     /* - card1 - a variable of type Card that holds a reference to the first card in the pair
 -       card2 - a variable of type Card that holds a reference to the second card in the pair
 *   */
     
     String cardType;
     Card card1;
     Card card2;
     Card card3;
     Card card4;
     Card card5;
     Card card6;
     Card card7;
     Card card8;
     Card card9;
     Card card10;
     
     
     public Pair(){
         
     }
     
     public void numberOfPairs(){
         
     }
 
     public String getCardType() {
         return cardType;
     }
 
     public void setCardType(String cardType) {
         this.cardType = cardType;
     }
 
     public Card getCard1() {
         return card1;
     }
 
     public void setCard1(Card card1) {
         this.card1 = card1;
     }
 
     public Card getCard2() {
         return card2;
     }
 
     public void setCard2(Card card2) {
         this.card2 = card2;
     }
 
     public Card getCard3() {
         return card3;
     }
 
     public void setCard3(Card card3) {
         this.card3 = card3;
     }
 
     public Card getCard4() {
         return card4;
     }
 
     public void setCard4(Card card4) {
         this.card4 = card4;
     }
 
     public Card getCard5() {
         return card5;
     }
 
     public void setCard5(Card card5) {
         this.card5 = card5;
     }
 
     public Card getCard6() {
         return card6;
     }
 
     public void setCard6(Card card6) {
         this.card6 = card6;
     }
 
     public Card getCard7() {
         return card7;
     }
 
     public void setCard7(Card card7) {
         this.card7 = card7;
     }
 
     public Card getCard8() {
         return card8;
     }
 
     public void setCard8(Card card8) {
         this.card8 = card8;
     }
 
     public Card getCard9() {
         return card9;
     }
 
     public void setCard9(Card card9) {
         this.card9 = card9;
     }
 
     public Card getCard10() {
         return card10;
     }
 
     public void setCard10(Card card10) {
         this.card10 = card10;
     }
     
       public void displayCardType(){
        System.out.println("Card type: " + this.cardType);
        }
