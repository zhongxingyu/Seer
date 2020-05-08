 package brawllogic;
 
 import java.util.Stack;
 
 /**
  * @author Patrick Herrmann
  */
 public final class BaseStack {
     
     private CardColor color = CardColor.NONE;
     private Stack<Card> stack = new Stack<Card>();
     
     void tryMove(Card card) throws GameplayException {
         
         if (card.getType().getCategory() != CardCategory.STACKER)
             throw new GameplayException("This type of card cannot be stacked on either side of a base.");
         
         if (color == CardColor.NONE) {
             
             if (card.getType() != CardType.HIT)
                 throw new GameplayException("The first card played on an empty base must be a hit card.");
             
             stack.push(card);
             color = card.getColor();
         } else {
             
             if (card.getType() == CardType.PRESS) {
                 
                 if (stack.peek().getType() != CardType.BLOCK)
                     throw new GameplayException("A press must be played on a block.");
                 
                 stack.push(card);
             } else {
                 
                 if (card.getColor() != color)
                     throw new GameplayException("This color card cannot be stacked in this pile.");
                 
                 if (stack.peek().getType() == CardType.BLOCK)
                     throw new GameplayException("This card cannot be played on a blocked pile.");
                 
                if (stack.peek().getType() == CardType.PRESS) {
                    
                    if (card.getType() != CardType.HIT)
                        throw new GameplayException("Only hits can be played on presses.");
                    
                }
                
                 stack.push(card);
             }
         }
     }
     
     public int getScore() {
         
         int score = 0;
         
         for (Card card : stack) {
             if (card.getType() == CardType.HIT)
                 score++;
             else if (card.getType() == CardType.SMASH)
                 score += 2;
         }
         
         return score;
     }
     
     public Stack<Card> getStack() {
         return stack;
     }
 }
