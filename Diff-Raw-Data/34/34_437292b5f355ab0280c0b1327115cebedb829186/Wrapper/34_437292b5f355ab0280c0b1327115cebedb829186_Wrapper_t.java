 
  
 import java.io.IOException;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
  
 public class Wrapper extends BasicGame{
 	
 	
 	public static int gamewidth = 1000;
 	public static int gameheight = 700;
 	Input myinput = new Input(600);
 	private Image background;
 	private Image hero;
 	private float x = 400, y = 300;
 	private int jumplength = 0;
 	private boolean jumping, onground;
  
     public Wrapper()
     {
         super("World of Spelunking Warrior");
     }
  
     @Override
     public void init(GameContainer gc) 
    {
    	gc.setMaximumLogicUpdateInterval(1);
		gc.setMinimumLogicUpdateInterval(1);
		try {
			hero = new Image("res/hero.png");
			background = new Image("res/cave.jpg");
		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}

     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	 if(myinput.isKeyDown(myinput.KEY_A) || myinput.isKeyDown(myinput.KEY_LEFT) )
          {
     		 if(issolid(x-1, y))
     			 x-=.5;
          }
     	 if(myinput.isKeyDown(myinput.KEY_D) || myinput.isKeyDown(myinput.KEY_RIGHT))
          {
     		 if(issolid(x+160, y))
     			 x+= .5;
          }
     	 if(myinput.isKeyDown(myinput.KEY_W) || myinput.isKeyDown(myinput.KEY_UP))
          {
     		 if(issolid(x, y-1) && onground == true){
     			 jumping = true;
     			 onground = false;
     		}else if(jumping && issolid(x, y-1)){
     			y-=1;
     			
     		}
          }
     	 if(myinput.isKeyDown(myinput.KEY_S) || myinput.isKeyDown(myinput.KEY_DOWN))
          {
     		 if(issolid(x, y+140))
     			 y += .5;
          }
     	 if(issolid(x, y+140)){
     		 y+=.5;
     	 }else
     		 onground = true;
     	 if(jumping && jumplength < 150){
     		 y -=1.0;
     		 jumplength++;
     		 if(jumplength >= 150){
     			 jumplength = 0;
     			 jumping = false;
     		 }
     			 
     	 }
         
     }
  
     public boolean issolid(float x, float y){
     	if(y  > gameheight)
     		return false;
     	if(x < 0)
     		return false;
     	if(x > gamewidth)
     		return false;
     	if(y < 0)
     		return false;
     	return true;
     }
     
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	background.draw(0,0);    	
     	hero.draw(x,y);
     	
     	Image spike = new Image("res/spike.png");
     	spike.draw(900, 650);
 
     }
  
     public static void main(String[] args) 
 			throws SlickException
     {
          AppGameContainer app = 
 			new AppGameContainer(new Wrapper());
          app.setDisplayMode(gamewidth, gameheight, false);
          app.start();
     }
 }
