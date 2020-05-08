 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 /**
  *
  * @author mike
  */
 public class NumberCard extends Card {
     
     private int face;
     
     public NumberCard(int newNum, Card.cardColor color)
     {
         if(newNum >= 0 && newNum <= 9)
             face = newNum;
         else
             System.out.println("Cannot create a card");
         super.color = color; 
     }
     
     /**
      *
      */
     @Override
     public void Print()
     {
        System.out.println(face + " " + color);
     }
 }
