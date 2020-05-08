 /* Play_Pile.java
  * Purpose: Play_Pile will be the implementation of the seven piles on the playing field for Solitaire that the user will mostly interact with.
  */
 
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 
 public class Play_Pile extends Piles {
     
     public Play_Pile(int size_of_down, int size_of_up, int x_of_up, int y_of_up, int x_of_down, int y_of_down) {
         super(size_of_down, size_of_up, x_of_up, y_of_up, x_of_down, y_of_down);
     }
     
     public Play_Pile(int size_of_up, int x_of_up, int y_of_up, int x_of_down, int y_of_down) {
         super(size_of_up, x_of_up, y_of_up, x_of_down, y_of_down);
     }
     
     /* boolean isValidMove(Card endpoint)
     * 
     * Purpose: isValidMove() is a check to see if the card that the player wants to move
     *          to the one of the seven main piles is a legal move in the game of Solitaire.
     * 
     * Procedure: isValidMove() is given the pile where the player wants to move the card and the card the player is trying to move.
     *            First, it checks to see if the there are no cards in the face-down pile or face up pile of where the user wants to move it and the card being moved is a King.
     *            Next, it checks to see if the colors of the top of the stack's card and the card being moved are different.
     *            Finally, the values are compared. The card being moved has to be lower than the top's card by one.
     */
 
     public boolean isValidMove( Piles endpoint ) {
         if ( !this.isEmpty('u') ) {
             if ( endpoint.isEmpty('d') && endpoint.isEmpty('u') && this.up[0].value == 13)
                 return true;
             if ( this.up[0].color == endpoint.up[endpoint.top_of_up].color )
                 return false;
             if ( this.up[0].value != ( endpoint.up[endpoint.top_of_up].value - 1 ) )
                 return false;
             return true;
         }
         return false;
     }
     
     /* void move_card(Piles endpoint)
      * Purpose: move_card() is the function that will properly move the card(s) from one pile to another if the move is valid.
      * 
      * Procedure: move_card() will recieve a stack of cards that the user wants to move--which can include multiple cards or just one.
      *            isValidMove() will be called to check and make sure it's a valid move.
      */
     
     public void move_card( Piles endpoint ) {
         if ( this.isValidMove( endpoint ) ) {
             
             int size = this.capac_of_up;
             
             Piles temp = new Piles(size);
             
             for( int i = 0; i < size; i++ ){
                 temp.push( this.pop('u'), 'u');
             }
             
             for( int i = 0; i < size; i++ ){
                 endpoint.push( temp.pop('u'), 'u');
             }
             
         }
         
         else {
             //play sound
         }
     }
     
     @Override
     public void draw() {
         Color.white.bind();
         
         if (isEmpty('u') && isEmpty('d')) {
             blank.bind();
 
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glTexCoord2f(0, 0);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1] + blank.getTextureHeight());
                 GL11.glTexCoord2f(0, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1] + blank.getTextureHeight());
             GL11.glEnd();
             
         }
         
         else if ( isEmpty('u') && !isEmpty('d') ) {
            back.bind();
            for(int i = top_of_down; i >= 0; i--) {
                 this.down[i].setX(coords_of_down[0]);
                 this.down[i].setY(coords_of_down[1] - (i*10));
                
 
                 GL11.glBegin(GL11.GL_QUADS);
                     GL11.glTexCoord2f(0, 0);
                     GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1] - (i*10));
                     GL11.glTexCoord2f(0.75f, 0);
                     GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) coords_of_down[1] - (i*10));
                     GL11.glTexCoord2f(0.75f, 0.95f);
                     GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) (coords_of_down[1] - (i*10)) + back.getTextureHeight());
                     GL11.glTexCoord2f(0, 0.95f);
                     GL11.glVertex2f((float) coords_of_down[0], (float) (coords_of_down[1] - (i*10)) + back.getTextureHeight());
                 GL11.glEnd();
             } 
         }
         
         else {
             
              if (this.down != null) {
                 for(int i = top_of_down; i >= 0; i--) {
                     this.down[i].setX(coords_of_down[0]);
                     this.down[i].setY(coords_of_down[1] - (i*10));
                     back.bind();
 
                     GL11.glBegin(GL11.GL_QUADS);
                         GL11.glTexCoord2f(0, 0);
                         GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1] - (i*10));
                         GL11.glTexCoord2f(0.75f, 0);
                         GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) coords_of_down[1] - (i*10));
                         GL11.glTexCoord2f(0.75f, 0.95f);
                         GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) (coords_of_down[1] - (i*10)) + back.getTextureHeight());
                         GL11.glTexCoord2f(0, 0.95f);
                         GL11.glVertex2f((float) coords_of_down[0], (float) (coords_of_down[1] - (i*10)) + back.getTextureHeight());
                     GL11.glEnd();
                 } 
             }
 
            for(int i = 0; i <= top_of_up; i++) {
                 this.up[i].setX(coords_of_up[0]);
                this.up[i].setY(coords_of_up[1] + (i*35));
                 
                 this.up[i].draw();
             }
         }
     }
}
