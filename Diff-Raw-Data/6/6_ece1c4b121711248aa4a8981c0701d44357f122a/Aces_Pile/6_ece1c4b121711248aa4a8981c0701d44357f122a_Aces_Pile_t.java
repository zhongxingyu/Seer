 /* Aces_Pile.java
  * Purpose: Aces_Pile will be the implementation of the four piles that the user will place cards on to win the game.
  */
 
 public class Aces_Pile extends Piles {
     
     public Aces_Pile (int size_of_up, int x_of_up, int y_of_up) {
         super(size_of_up, x_of_up, y_of_up);
     }
         
     
     /* void move_card(Piles endpoint)
      * Purpose: move_card() is the function that will properly move the card(s) from one pile to another if the move is valid.
      * 
      * Procedure: move_card() will recieve a stack of cards that the user wants to move--which can include multiple cards or just one.
      *            isValidMove() will be called to check and make sure it's a valid move.
      */
     
     public boolean move_card( Card moved ) {
         
         if ( this.isValidMove(moved)) {
             this.push( moved, 'u' );
             return true;
         }
         
         else {
             //play sound
             return false;
         }
     }
     
     /* boolean isValidAceMove(Piles endpoint)
     * Purpose: isValidAceMove() is a check to see if the card that the player wants to move
     *          to one of the Ace piles is a legal move in the game of Solitaire.
     * 
     * Procedure: isValidMove() is given the stack where the card will be going to and the card that is being moved itself.
     *            It first checks if the ace pile is empty and if the card being moved is an Ace.
     *            It then checks if the suits match the card and the top card in the pile.
     *            Finally, it checks to see if the value of the card being moved is only one higher than the current card on top.
     */
     
     public boolean isValidMove( Card moved ) {
         
         if ( this.isEmpty('u') && moved.value == 1 ) 
             return true;
        if ( this.up[top_of_up].suit == moved.suit && (this.up[top_of_up].value + 1) == moved.value)
             return true;
         return false;
     }
 }
