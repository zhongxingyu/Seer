 package sitsa.aqado;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import sitsa.aqado.GUI.AbstractBoxComponent;
 import sitsa.aqado.GUI.AbstractButton;
 import sitsa.aqado.GUI.AbstractCounterComponent;
 
 import java.awt.Font;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: connormonaghan
  * Date: 02/11/2013
  * Time: 12:36
  * To change this template use File | Settings | File Templates.
  */
 
 //NEW INGAME
 public class InGame2 extends BasicGameState {
 
     private int state;
     private boolean PAUSED = false;
     private Image bg, pauseBut, pauseOverlay, redCounterTex, blueCounterTex, sbOverlay, sbOverlay1, sbOverlay2, ibOverlay;
     private Button pauseButton;
     private pMenuButton quitToTitleButton, resumeButton, optionsButton, restartButton, rollButton;
     public boxComponent box1, box2, box3, box4, box5, box6, box7, box8, box9, box10, box11;
     private TrueTypeFont font, font1, font2;
     private Font awtFont, awtFont1, awtFont2;
     private CounterComponent redCounter1, redCounter2, blueCounter1, blueCounter2;
 
     public InGame2(int state){
         this.state = state;
     }
 
     @Override
     public int getID() {
         return this.state;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
         // Initialize images
         bg = new Image("tex/bg300x400.png");
         sbOverlay = new Image("tex/safeBoxOverlay-01.png");
         sbOverlay1 = new Image("tex/safeBoxOverlay-01.png");
         sbOverlay2 = new Image("tex/safeBoxOverlay-01.png");
         ibOverlay = new Image("tex/infoBoxOverlay-01.png");
         redCounterTex = new Image("tex/redCounter.png");
         blueCounterTex = new Image("tex/blueCounter.png");
         // Load boxes
         box11 = new boxComponent(100, 44, 500, 68, 11, Color.white);
         box10 = new boxComponent(100, 112, 500, 68, 10, Color.black);
         box9 = new boxComponent(100, 180, 500, 68, 9, Color.white);
         box8 = new boxComponent(100, 248, 500, 68, 8, Color.black);
         box7 = new boxComponent(100, 316, 500, 68, 7, Color.white);
         box6 = new boxComponent(100, 384, 500, 68, 6, Color.black);
         box5 = new boxComponent(100, 452, 500, 68, 5, Color.white);
         box4 = new boxComponent(100, 520, 500, 68, 4, Color.black);
         box3 = new boxComponent(100, 588, 500, 68, 3, Color.white);
         box2 = new boxComponent(100, 656, 500, 68, 2, Color.black);
         box1 = new boxComponent(100, 724, 500, 68, 1, Color.white);
         // Load counters
         redCounter1 = new CounterComponent(130, 730, 51, 51, 1, redCounterTex, box1);
         redCounter2 = new CounterComponent(218, 730, 51, 51, 2, redCounterTex, box1);
         blueCounter1 = new CounterComponent(430, 730, 51, 51, 3, blueCounterTex, box1);
         blueCounter2 = new CounterComponent(518, 730, 51, 51, 4, blueCounterTex, box1);
         // Load buttons
             //Pause menu
             pauseButton = new Button(665, 0, 29, 31, Color.transparent);
             pauseBut = new Image("tex/pause.png");
             pauseOverlay = new Image("tex/overlay.png");
             resumeButton = new pMenuButton(245, 290, 200, 60, Color.cyan.darker());
             restartButton = new pMenuButton(245, 360, 200, 60, Color.cyan.darker());
             optionsButton = new pMenuButton(245, 430, 200, 60, Color.cyan.darker());
             quitToTitleButton = new pMenuButton(245, 500, 200, 60, Color.cyan.darker());
         // Initialize font
         awtFont = new Font("Arial", Font.BOLD, 20);
         font = new TrueTypeFont(awtFont, false);
         awtFont1 = new Font("Arial", Font.BOLD, 16);
         font1 = new TrueTypeFont(awtFont1, false);
         awtFont2 = new Font("Arial", Font.BOLD, 25);
         font2 = new TrueTypeFont(awtFont2, false);
 
 
 
         rollButton = new pMenuButton(10, 180, 80, 30, Color.green.darker());
 
     }
 
     @Override
     public void render(GameContainer gc, StateBasedGame stateBasedGame, Graphics g) throws SlickException {
         bg.draw(-600,-800);
         // Render boxes
         ibOverlay.draw(100, 6);
         sbOverlay.draw(100, 452);
         sbOverlay1.draw(100, 724);
         sbOverlay2.draw(100, 44);
         box11.draw(g);
         box10.draw(g);
         box9.draw(g);
         box8.draw(g);
         box7.draw(g);
         box6.draw(g);
         box5.draw(g);
         box4.draw(g);
         box3.draw(g);
         box2.draw(g);
         box1.draw(g);
         g.drawRect(300, 44, 100, 748);
         g.drawRect(100, 6, 500, 38);
         font1.drawString(310, 45, "11 - Finish", Color.black);
         font.drawString(340, 115, "10", Color.black);
         font.drawString(345, 185, "9", Color.black);
         font.drawString(345, 255, "8", Color.black);
         font.drawString(345, 325, "7", Color.black);
         font.drawString(345, 395, "6", Color.black);
         font.drawString(345, 465, "5", Color.black);
         font.drawString(345, 535, "4", Color.black);
         font.drawString(345, 605, "3", Color.black);
         font.drawString(345, 675, "2", Color.black);
         font1.drawString(320, 745, "1 - Start", Color.black);
 
 
         // Render counters
         redCounter1.draw(g);
         redCounter2.draw(g);
         blueCounter1.draw(g);
         blueCounter2.draw(g);
 
         // Render info boxes
         /*g.setColor(Color.green.darker().darker());
         g.fillRect(10, 70, 80, 30);
         g.setColor(Color.green.darker());
         g.fillRect(10, 100, 80, 50);
         font.drawString(30, 75, "Info", Color.black);*/
         g.setColor(Color.green.darker().darker());
         g.fillRect(610, 70, 80, 30);
         g.setColor(Color.green.darker());
         g.fillRect(610, 100, 80, 50);
         font.drawString(620, 75, "Rolled", Color.black);
         if(gameVariables.isCounterSelected){
            ibOverlay.draw(100, 6);
         }
 
 
         // Render dice attributes
         rollButton.draw(g);
         font.drawString(30, 184, "Roll", Color.black);
         if(gameVariables.diceRolled == true){
             font2.drawString(640, 110, ""+gameVariables.diceLandedNum, Color.black);
             rollButton.setFillColor(Color.gray.darker());
            ibOverlay.draw(100, 6);
             font2.drawString(260, 10, "Select a counter.", Color.black);
         }
 
         //Pause menu
             pauseBut.draw(670, 5);
             pauseButton.draw(g);
             if(PAUSED == true){
                 pauseOverlay.draw(0, 0);
                 resumeButton.draw(g);
                 restartButton.draw(g);
                 optionsButton.draw(g);
                 quitToTitleButton.draw(g);
                 g.drawString("Resume", 315, 310);
                 g.drawString("Restart", 315, 380);
                 g.drawString("Options", 315, 450);
                 g.drawString("Quit to title", 290, 520);
             }
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
 
         // Core game logic
             //TODO core game logic
         if(gameVariables.diceRolled == true && gameVariables.isCounterSelected == false){
             if(gameVariables.playerTurn == "p1"){
                 if(redCounter1.isClicked(gc)){
                     gameVariables.counterSelected = redCounter1;
                     gameVariables.isCounterSelected = true;
                     System.out.println(gameVariables.counterSelected.getID());
                     System.out.println(gameVariables.nextBox(redCounter1.getBoxIn()).getID());
                 }else if(redCounter2.isClicked(gc)){
                     gameVariables.counterSelected = redCounter2;
                     gameVariables.isCounterSelected = true;
                     System.out.println(gameVariables.counterSelected.getID());
                 }else if(blueCounter1.isClicked(gc)){
                    ibOverlay.draw(100, 6);
                     font2.drawString(230, 10, "You must select "+gameVariables.playerTurn+"'s counter", Color.black);
                 }else if(blueCounter2.isClicked(gc)){
                    ibOverlay.draw(100, 6);
                     font2.drawString(230, 10, "You must select "+gameVariables.playerTurn+"'s counter", Color.black);
                 }
             }
             if(gameVariables.playerTurn == "p2"){
                 if(redCounter1.isClicked(gc)){
                   ibOverlay.draw(100, 6);
                     font2.drawString(230, 10, "You must select "+gameVariables.playerTurn+"'s counter", Color.black);
                 }else if(redCounter2.isClicked(gc)){
                    ibOverlay.draw(100, 6);
                     font2.drawString(230, 10, "You must select "+gameVariables.playerTurn+"'s counter", Color.black);
                 }else if(blueCounter1.isClicked(gc)){
                     gameVariables.counterSelected = blueCounter1;
                     gameVariables.isCounterSelected = true;
                     System.out.println(gameVariables.counterSelected.getID());
                 }else if(blueCounter2.isClicked(gc)){
                     gameVariables.counterSelected = blueCounter2;
                     gameVariables.isCounterSelected = true;
                     System.out.println(gameVariables.counterSelected.getID());
                 }
             }
         }else{
             if(redCounter1.isClicked(gc)){
                ibOverlay.draw(100, 6);
                 font2.drawString(240, 10, "You must roll the dice first.", Color.black);
             }else if(redCounter2.isClicked(gc)){
                ibOverlay.draw(100, 6);
                 font2.drawString(240, 10, "You must roll the dice first.", Color.black);
             }else if(blueCounter1.isClicked(gc)){
                ibOverlay.draw(100, 6);
                 font2.drawString(240, 10, "You must roll the dice first.", Color.black);
             }else if(blueCounter2.isClicked(gc)){
                ibOverlay.draw(100, 6);
                 font2.drawString(240, 10, "You must roll the dice first.", Color.black);
             }
         }
 
         if(gameVariables.isCounterSelected == true){
             if(gameVariables.counterSelected == redCounter1){
                 if(gameVariables.isLegalMove1(redCounter1)){
                     redCounter1.setBoxIn(gameVariables.getBoxID((int) redCounter1.getBoxIn().getID() + gameVariables.diceLandedNum));
                     redCounter1.moveTo(redCounter1.getBoxIn());
                     redCounter1.getBoxIn().setOccupied(true, redCounter1.getID());
                     gameVariables.moveMade = true;
                     gameVariables.playerTurn = "p2";
                 }
             }else if(gameVariables.counterSelected == redCounter2){
                 if(gameVariables.isLegalMove1(redCounter2)){
                     redCounter2.setBoxIn(gameVariables.getBoxID((int) redCounter2.getBoxIn().getID() + gameVariables.diceLandedNum));
                     redCounter2.moveTo(redCounter2.getBoxIn());
                     redCounter2.getBoxIn().setOccupied(true, redCounter2.getID());
                     gameVariables.moveMade = true;
                     gameVariables.playerTurn = "p2";
                 }
             }else if(gameVariables.counterSelected == blueCounter1){
                 if(gameVariables.isLegalMove2(blueCounter1)){
                     blueCounter1.setBoxIn(gameVariables.getBoxID((int) blueCounter1.getBoxIn().getID() + gameVariables.diceLandedNum));
                     blueCounter1.moveTo(blueCounter1.getBoxIn());
                     blueCounter1.getBoxIn().setOccupied(true, blueCounter1.getID());
                     gameVariables.moveMade = true;
                     gameVariables.playerTurn = "p1";
                 }
             }else if(gameVariables.counterSelected == blueCounter2){
                 if(gameVariables.isLegalMove2(blueCounter2)){
                     blueCounter2.setBoxIn(gameVariables.getBoxID((int) blueCounter2.getBoxIn().getID() + gameVariables.diceLandedNum));
                     blueCounter2.moveTo(blueCounter2.getBoxIn());
                     blueCounter2.getBoxIn().setOccupied(true, blueCounter2.getID());
                     gameVariables.moveMade = true;
                     gameVariables.playerTurn = "p1";
                 }
             }
         }
 
         if(gameVariables.moveMade == true){
             gameVariables.counterSelected = null;
             gameVariables.isCounterSelected = false;
             gameVariables.diceRolled = false;
             gameVariables.diceLandedNum = 0;
             gameVariables.moveMade = false;
         }
 
         // Dice logic
             //TODO dice logic
         if(gameVariables.diceRolled == false){
             if(rollButton.isMouseOn(gc)){
                 rollButton.setFillColor(Color.green.darker().darker());
             }else{
                 rollButton.setFillColor(Color.green.darker());
             }
             if(rollButton.isMouseClicked(gc)){
                 gameVariables.diceRolled = true;
                 Random randomGenerator = new Random();
                 int low = 1;
                 int high = 5;
                 int randomInt = 0;
                 for (int idx = 1; idx <= 10; ++idx){
                     randomInt = randomGenerator.nextInt(high-low) + low;
                     System.out.println("Generated : " + randomInt);
                     //font2.drawString(640, 110, ""+randomInt, Color.black);
                 }
                 gameVariables.diceLandedNum = randomInt;
             }
         }else if(gameVariables.diceRolled == true){
             if(rollButton.isMouseClicked(gc)){
 
             }
             if(rollButton.isMouseOn(gc)){
                 rollButton.setFillColor(Color.gray.darker());
             }
         }
 
 
         // Counter logic
             //TODO dynamic counter movement
         /*AbstractBoxComponent i = gameVariables.getBoxID((int) redCounter1.getBoxIn().getID() + 1);
 
         if(redCounter1.isClicked(gc)){
             redCounter1.setBoxIn(gameVariables.getBoxID((int) redCounter1.getBoxIn().getID() + 1));
             redCounter1.moveTo(redCounter1.getBoxIn());
             redCounter1.getBoxIn().setOccupied(true, redCounter1.getID());
         }
         if(redCounter2.isClicked(gc)){
             redCounter2.setBoxIn(gameVariables.getBoxID((int) redCounter2.getBoxIn().getID() + 1));
             redCounter2.moveTo(redCounter2.getBoxIn());
             redCounter2.getBoxIn().setOccupied(true, redCounter2.getID());
         }
         if(blueCounter1.isClicked(gc)){
             blueCounter1.setBoxIn(gameVariables.getBoxID((int) blueCounter1.getBoxIn().getID() + 1));
             blueCounter1.moveTo(blueCounter1.getBoxIn());
             blueCounter1.getBoxIn().setOccupied(true, blueCounter1.getID());
         }
         if(blueCounter2.isClicked(gc)){
             blueCounter2.setBoxIn(gameVariables.getBoxID((int) blueCounter2.getBoxIn().getID() + 1));
             blueCounter2.moveTo(blueCounter2.getBoxIn());
             blueCounter2.getBoxIn().setOccupied(true, blueCounter2.getID());
         }*/
 
 
         redCounter1.update(delta);
         redCounter2.update(delta);
         blueCounter1.update(delta);
         blueCounter2.update(delta);
 
             //Pause menu
             if(pauseButton.isMouseClicked(gc)){
                 PAUSED = true;
             }
             if(PAUSED == true){
                 if(resumeButton.isMouseOn(gc)){
                     resumeButton.setFillColor(Color.cyan.brighter());
                 }else{
                     resumeButton.setFillColor(Color.cyan.darker());
                 }
                 if(restartButton.isMouseOn(gc)){
                     restartButton.setFillColor(Color.cyan.brighter());
                 }else{
                     restartButton.setFillColor(Color.cyan.darker());
                 }
                 if(optionsButton.isMouseOn(gc)){
                     optionsButton.setFillColor(Color.cyan.brighter());
                 }else{
                     optionsButton.setFillColor(Color.cyan.darker());
                 }
                 if(quitToTitleButton.isMouseOn(gc)){
                     quitToTitleButton.setFillColor(Color.cyan.brighter());
                 }else{
                     quitToTitleButton.setFillColor(Color.cyan.darker());
                 }
                 if(resumeButton.isMouseClicked(gc)){
                     PAUSED = false;
                 }
                 if(restartButton.isMouseClicked(gc)){
                     // TODO fix bug making line width thinner
                     gc.reinit();
                     PAUSED = false;
                     gameVariables.diceRolled = false;
                     gameVariables.moveMade = false;
                     gameVariables.diceLandedNum = 0;
                     game.enterState(5);
                 }
                 if(optionsButton.isMouseClicked(gc)){
                     game.enterState(2);
                     gameVariables.fromPause = true;
                 }
                 if(quitToTitleButton.isMouseClicked(gc)){
                     game.enterState(1);
                     PAUSED = false;
                 }
             }
     }
 
     private static class Button extends AbstractButton {
 
         public Button(float x, float y, float width, float height, Color fillColor) {
             super(x, y, width, height, fillColor);
         }
 
         @Override
         public void draw(Graphics g) {
             g.setColor(fillColor);
             g.drawRect(x, y, width, height);
         }
 
         public void update(int delta) {
         }
     }
 
     private static class pMenuButton extends AbstractButton{
 
         public pMenuButton(float x, float y, float width, float height, Color fillColor) {
             super(x, y, width, height, fillColor);
         }
 
         @Override
         public void draw(Graphics g) {
             g.setLineWidth(3);
             g.setColor(fillColor);
             g.fillRoundRect(x, y, width, height, 15);
             g.setColor(Color.black);
             g.drawRoundRect(x, y, width, height, 15);
         }
 
         public void update(int delta) {
         }
     }
 
     private static class boxComponent extends AbstractBoxComponent{
 
         float nextBoxNum = 0;
         Color color;
 
         public boxComponent(float x, float y, float width, float height, float boxNum, Color color){
             super(x, y, width, height, boxNum);
             this.color = color;
         }
 
         @Override
         public void draw(Graphics g) {
             g.setColor(Color.black);
             g.drawRect(x, y, width, height);
             this.setboxDrawn(true);
             //this.nextBoxNum = this.nextBoxNum + 1;
         }
 
         // the star of riches is shinning on you
 
         @Override
         public void update(int delta) {
             if(this.isBoxDrawn()){
                 //this.setBoxNum(nextBoxNum);
             }
         }
 
         @Override
         public AbstractBoxComponent nextBox() {
             if(this.id == 1){
 
             }
             return null;
         }
 
     }
 
     private static class CounterComponent extends AbstractCounterComponent{
 
         public CounterComponent(float x, float y, float width, float height, float id, Image texture, AbstractBoxComponent boxIn) {
             super(x, y, width, height, id, texture, boxIn);
         }
 
         @Override
         public void draw(Graphics g) {
             texture.draw(x, y, (float) 1.3);
         }
     }
 }
