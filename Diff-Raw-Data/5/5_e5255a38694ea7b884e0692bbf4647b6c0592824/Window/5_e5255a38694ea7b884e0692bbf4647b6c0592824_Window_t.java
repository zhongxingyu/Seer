 /*
  * Object: Window
  * Displays a single game state, but does so by displaying it to only a section
  * of the screen specified by parameters to the render call. This allows for the
  * rendering of this view multiple times in different locations on the screen,
  * or for the movement of this view around the screen.
  */
 /**
  * 
  */
 /**
  * 
  */
 
 /**
  * 
  */
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.Image;
 
 /**
  * This is for minigame windows, not for the overall node structure.
  */
 
 public class Window {
 
     protected boolean over = false;
     protected Player player;
     protected float[] playerPos = new float[2];
     protected Image bgImageOne;
     protected Image bgImageTwo;
 
     /*
      * Constructor that allows for providing of a stateID
      */
     public Window(Player player) {
         this.player = player;
     }
 
     public void displayMinigameBackground(Graphics g, Player player) {
         if (player.tag == 1) {
            g.drawImage(bgImageOne.getScaledCopy(590, 720), player.windowPos[0], player.windowPos[1]);
         }
         else {
            g.drawImage(bgImageTwo.getScaledCopy(590, 720), player.windowPos[0], player.windowPos[1]);
         }
     }
 
     public void render(GameContainer container, StateBasedGame game, Graphics g, Player player) throws SlickException {
 
     }
 
     public void init(GameContainer container, StateBasedGame game, Player player) throws SlickException {
     }
 
     public void update(GameContainer container, StateBasedGame game, int delta, Player player) throws SlickException {
     }
 
     public void enter(GameContainer container, StateBasedGame game, Player player) {
     }
     
     public void movePlayer(Input input, float moveValue){
         if (input.isKeyDown(player.getButton("left"))) {
             if (playerPos[0] - moveValue > player.windowPos[0]) {
                 playerPos[0] -= moveValue;
             }
         }
         if (input.isKeyDown(player.getButton("right"))) {
             if (playerPos[0] + player.pWidth + moveValue < player.windowPos[0] + player.windowSize[0]) {
                 playerPos[0] += moveValue;
             }
         }
         if (input.isKeyDown(player.getButton("up"))) {
             if (playerPos[1] - moveValue > player.windowPos[1]) {
                 playerPos[1] -= moveValue;
             }
         }
         if (input.isKeyDown(player.getButton("down"))) {
             if (playerPos[1] + player.pHeight + moveValue < player.windowPos[1] + player.windowSize[1]) {
                 playerPos[1] += moveValue;
             }
         }
     }
 
     public boolean over() {
         return over;
     }
 }
