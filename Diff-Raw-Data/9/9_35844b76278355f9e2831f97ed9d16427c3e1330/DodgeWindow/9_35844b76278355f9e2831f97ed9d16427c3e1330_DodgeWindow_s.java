 package hub1events;
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import framework.EventWindow;
 
 public class DodgeWindow extends EventWindow {
     private float[][] objPos = new float[20][2];
     private boolean[] objVis = new boolean[20];
     private float[][] objSpd = new float[20][2];
     private Double timer;
     private UnicodeFont font;
     private Image[] stuff = new Image[3];
     private Image daughter = new Image("Assets/Hub 1/Images/daughterFront.png");
     private int state; // -1, 0, 1, 2, 3 // dialogue, wait, play, end dialogue, wait/END
 
 
     public DodgeWindow() throws SlickException {
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         if (state == -1 || state == 0) {
             this.displayMinigameBackground(g);
         	daughter.draw(150 + player.windowPos[0], 300);
 			player.render(container, game, g, player.eventLoc[0], player.eventLoc[1]);
         }
         if (state == 1) {
             this.displayMinigameBackground(g);
 	        for (int i = 0; i < objPos.length; i++) {
 				if (objVis[i])
 					g.drawImage(stuff[i % 3], objPos[i][0], objPos[i][1]);
 			}
 			player.render(container, game, g, player.eventLoc[0], player.eventLoc[1]);
 	        g.setFont(font);
 	        g.setColor(Color.black);
 	        g.drawString("Dodge!", 260 + player.windowPos[0], 65);
         	g.setColor(Color.white);
         }
     }
     
     public void start() {
         player.gridLoc[1]++;
     	player.playerSprite = player.down1;
         player.eventLoc[0] = player.windowPos[0] + (int) player.windowSize[0] / 2;
         player.eventLoc[1] = player.windowPos[1] + (int) player.windowSize[1] / 2;
         for (int i = 0; i < objPos.length; i++) {
             objPos[i][0] = player.windowPos[0] + (int) ((player.windowSize[0] - 100) * Math.random());
             objPos[i][1] = player.windowPos[1] + (int) ((player.windowSize[1] - 50) * Math.random());
             objVis[i] = true;
             double x = Math.random();
             if (Math.random() < .5)
                 x *= -1;
             double y = Math.pow(1 - Math.pow(x, 2), .5);
             if (Math.random() < .5)
                 y *= -1;
             objSpd[i] = new float[] { (float) x, (float) y };
         }
         timer = 6.0;
         state = -1;
         
         text.add("Eek! What are you doing in my house?! Do you think you can just barge into a stranger's home just like that?! Get out! Get out! Get out!");
     }
     
     @SuppressWarnings("unchecked")
 	@Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         super.init(container, game);
         stuff[0] = new Image("Assets/Hub 1/Images/sock.png");
         stuff[1] = new Image("Assets/Hub 1/Images/pan.png");
         stuff[2] = new Image("Assets/Hub 1/Images/knife.png");
         this.bg = new Image("Assets/Hub 1/Images/dodge_bg.png");
         //bg = new Image("Assets/Hub 1/Images/shop_bg.png");
         Font awtFont = new Font("Arial Monospaced", Font.BOLD, 24);
         font = new UnicodeFont(awtFont);
         font.getEffects().add(new ColorEffect(java.awt.Color.black));
         font.addAsciiGlyphs();
         font.loadGlyphs();
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         Input input = container.getInput();
         if (state == -1) {
         	dialogBox.playMsg(text);
         	state = 0;
         }
         if (state == 0) {
         	if (!dialogBox.isActive()) {
         		state = 1;
         	}
         }
         if (state == 1) {
 	        for (int i = 0; i < stuff.length; i++) {
 	        	stuff[i].rotate(4);
 	        }
 	
 	        float moveValue = delta * .2f;
 	        
 	        super.movePlayer(input, moveValue, 0);
 	        
 	        for (int i = 0; i < objPos.length; i++) {
 	            if (objVis[i]) {
 	                if (player.windowPos[0] + player.windowSize[0] < objPos[i][0] + 40 + moveValue * objSpd[i][0]
 	                        || objPos[i][0] + moveValue * objSpd[i][0] < player.windowPos[0]) {
 	                    objSpd[i][0] *= -1;
 	                }
 	                objPos[i][0] += moveValue * objSpd[i][0];
 	                if (player.windowPos[1] + player.windowSize[1] < objPos[i][1] + 40 + moveValue * objSpd[i][1]
 	                        || objPos[i][1] + moveValue * objSpd[i][1] < player.windowPos[1]) {
 	                    objSpd[i][1] *= -1;
 	                }
 	                objPos[i][1] += moveValue * objSpd[i][1];
 	            }
 	        }
 	
 	        //Rectangle playerShape = new Rectangle(player.eventLoc[0], player.eventLoc[1], 20, 20);
 	        for (int i = 0; i < objPos.length; i++) {
 	            if (objVis[i] && (new Rectangle(objPos[i][0], objPos[i][1], 40, 40)).intersects(player.collisionRect)) {
 	                timer = 6.0;
 	                objVis[i] = false;
 	            }
 	        }
 	        timer -= 1.0 / 60;
 	        if (timer <= 0) {
 	            state = 2;
 	        }
         }
         if (state == 2) {
        	bg = new Image("Assets/Black.jpg");
         	text = new ArrayList<String>();
         	text.add("Phew... that was close!");
         	dialogBox.playMsg(text);
         	state = 3;
         	inside[player.playerNum - 1] = false;
         }
     }
 }
