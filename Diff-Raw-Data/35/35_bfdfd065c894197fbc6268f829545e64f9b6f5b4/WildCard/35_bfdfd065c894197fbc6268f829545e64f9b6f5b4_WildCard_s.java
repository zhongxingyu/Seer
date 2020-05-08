 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 /**
  *
  * @author mike
  */
 public class WildCard extends Card implements WildActions
 {
     enum cardWild {WILD, WILDDRFOUR}; 
     cardWild wild; 
     
     
     
     public WildCard(cardWild w)
     {
         wild = w; 
     }
     
     public void Print()
     {
        System.out.println(wild);
     }
 }
