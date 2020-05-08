 
  
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
  
 public class Wrapper extends BasicGame{
 	
 	
 	public static int gamewidth = 1000;
 	public static int gameheight = 700;
 	Input myinput = new Input(600);
 	Room room;
 	private Image background;
 	private Image hero;
 	private Image heroleft;
 	private Image heroright;
 	private Rectangle herolocation;
 	private World world;
 	private ArrayList<Entity> tempImg;
 	public float x = 400, y = 300;
 	private int jumplength = 0;
 	private boolean atkLeft;
 	private boolean jumping, onground, facingLeft, facingRight, aPressed = false, wPressed = false, dPressed = false, sPressed = false;
 	private int health = 5;
 	private int damage = 5;
 	private int frames = 0;
  
     public Wrapper()
     {
         super("World of Spelunking Warrior");
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException {
     	tempImg = new ArrayList<Entity>();
     	gc.setMaximumLogicUpdateInterval(1);
 		gc.setMinimumLogicUpdateInterval(1);
 		world = new World();
 		hero = new Image("res/hero.png");
 		heroleft = new Image("res/heroleft.png");
 		heroright = new Image("res/heroright.png");
 		background = new Image("res/cave.jpg");
 		herolocation = new Rectangle(x,y,49,49);
 		//SpriteSheet sheet = new SpriteSheet("res/tiles_nes.png", 16, 16);
         room = world.map.getFirst();
         room.enemies.add(new Entity("res/lbear.png", "res/rbear.png", 500, 500));
         room.enemies.get(0).setType(Entity.Type.ENEMY);
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	//if(gc.hasFocus() == false)
     		//gc.pause();
 
    	 if(myinput.isKeyDown(Input.KEY_SPACE) && frames > 28)
     	 {
     		 int toRemove = -1;
     		 boolean hit = false;
     		 int modifier = 49;
     		 if(facingLeft == true)
     		 {
     			 modifier = -20;
     		 }
     		 tempImg.add(new Entity("res/hitbox.jpg", "res/test_sprite.jpg", (int)x+modifier, (int)y));
     		 
     		 Rectangle attack = new Rectangle(x+modifier, y-49, 25, 50);
     		 for(Entity enemy : room.enemies)
     		 {
     			 if(enemy.type == Entity.Type.ENEMY && enemy.collision(attack))
     			 {
     				 hit = true;
     				 if(!enemy.applyDamage(1))
     				 {
     					 toRemove = room.enemies.indexOf(enemy);
     				 }
     			 }
     		 }
     		 if (room.enemies.size() > toRemove && toRemove > -1 && hit == true){
     			 room.enemies.remove(toRemove);
     		 }
     		 
    	 }
     	 
     	 if(aPressed)
          {
     		 facingLeft = true;
     		 atkLeft = true;
     		if(issolid(x-1, y-1, herolocation)){
     			 x-=.4;
     			 herolocation.setLocation(x, y);
     		 }else
     			 onground = true;
          }else
         	 facingLeft = false;
     	 
     	 if(dPressed)
          {
     		 atkLeft = false;
     		 facingRight = true;
     		 if(issolid(x+1, y-1, herolocation)){
     			 x+= .4;
     			 herolocation.setLocation(x, y);
     		 }else
     			 onground = true;
          }else
         	 facingRight = false;
     	 
     	 if(wPressed)
          {
     		 if(onground == true){
     			 jumping = true;
     			 onground = false;
     		 }
          }
     	 if(sPressed)
          {
     		 if(issolid(x, y, herolocation)){
     			 y += .3;
     			 herolocation.setLocation(x, y);
     		 }
          }
     	 if(myinput.isKeyDown(myinput.KEY_P))
          {
     		for(int i = 0; i < world.map.size();i++)
     			System.out.println(world.map.get(i).xlocation + " " + world.map.get(i).ylocation);
     			System.out.println(room.enemies.get(0).location.getCenterX());
          }
     	 
     	 if(issolid(x, y, herolocation)){
     		 y+=.5;
     		 herolocation.setLocation(x, y);
     	 }else
     		 onground = true;
     	 
     	 if(jumping && jumplength < 250 && issolid(x, y-1, herolocation)){
     		 y -=1.0;
     		 herolocation.setLocation(x, y);
     		 jumplength++;
     		 if(jumplength >= 250){
     			 jumplength = 0;
     			 jumping = false;
     		 }
     			 
     	 }else
     		 jumping = false;
     	 
     	 
     	 for(Entity entity : room.enemies)
     	 {
     		if(enemysolid(entity.x, entity.y+1, entity.location))
   	    	{
   				 entity.move(entity.x, entity.y+1);
   	    	}
     		 if(frames % 7 == 0)
     	    	{
     				 if(!entity.goingleft)
     				 {
     					 if(enemysolid(entity.x+1, entity.y, entity.location))
     					 {
     						 entity.move(entity.x+1, entity.y);
     					 }else
     						 entity.goingleft = true;
     				 }
     				 else
     				 {
     					 if(enemysolid(entity.x-1, entity.y, entity.location))
     					 {
     						 entity.move((int)entity.x-1, (int)entity.y);
     					 }else
     						 entity.goingleft = false;
     				 }
     	    	}
     		 
     		if(enemysolid(entity.x, entity.y+1, entity.location))
  	    	{
  				 entity.move(entity.x, entity.y+1);
  	    	}
     		 if(entity.collision(herolocation) && frames == 30)
     		    	if(entity.collision(herolocation) && entity.type == Entity.Type.ENEMY)
     		    	{
     		    		health -= 1;
     		    		System.out.println("Damage calc, player health: " + health);
     		    	}
     	 }
     	 
     	 
     	 if(frames == 30)
 		 {
 			 frames = 0;
 		 }
         
     }
     
     public boolean enemysolid(float x, float y, Rectangle enemy){
     	float tmpy = enemy.getY(), tmpx = enemy.getX();
     	if(y  > gameheight){
     		return false;
     	}
     	if(x < 0){
     		return false;
     	}
     	if(x + enemy.getWidth() > gamewidth){
     		return false;
     	}	
     	if(y+1 < 0){
     		return false;
     	}
     	
     	enemy.setLocation(x, y);
     	
     	for(int i = 0; i < room.blocks.size();i++){
     		if(enemy.intersects(room.blocks.get(i))){
     			enemy.setLocation(tmpx, tmpy);
     			return false;
     		}
     	}
     	return true;
     }
     public boolean issolid(float x, float y, Rectangle herolocation){
 		float tmpy = herolocation.getY(), tmpx = herolocation.getX();
 		
     	if(y  > gameheight){
     		room = world.changeroom(room, 0, this);
     		return true;
     	}
     	if(x < 0){
     		room = world.changeroom(room, 3, this);
     		return true;
     	}
     	if(x > gamewidth){
     		room = world.changeroom(room, 1, this);
     		return true;
     	}	
     	if(y+1 < 0){
     		room = world.changeroom(room, 2, this);
     		return true;
     	}
     		
     	
     	herolocation.setLocation(x, y);
     	
     	for(int i = 0; i < room.blocks.size();i++){
     		if(herolocation.intersects(room.blocks.get(i))){
     			herolocation.setLocation(tmpx, tmpy);
     			return false;
     		}
     	}
     	
     	return true;
     }
  
     
     
     
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	frames++;
 		 
     	background.draw(0,0); 
     	if(facingLeft)
     		heroleft.draw(x,y);
     	else if(facingRight)
     		heroright.draw(x,y);
     	else
     		hero.draw(x,y);
     	
     	for(Entity entity : room.enemies)
     	{
     		entity.draw();
     	}
     	
     	for(Entity img : tempImg)
     	{
     		img.draw();
     	}
     	tempImg.clear();
     	
     	Image bricktop = new Image("res/dirt.png");
     	Image brick = new Image("res/dirtmid.png");
     	for(int i = 0; i < room.width;i++){
     		for(int j = 0; j < room.height;j++){
     			if(room.terrain[i][j] == '1'){
     				if(j == 0){
     					brick.draw(i*50,j*50);
     				}else if(room.terrain[i][j-1] == '1'){
     					brick.draw(i*50,j*50);
     				}else
     					bricktop.draw(i*50,j*50);
     			}
     		}
     	}
     	
     	//Image spike = new Image("res/spike.png");
     	//spike.draw(900, 650);
 
     }
    
  
     public static void main(String[] args) 
 			throws SlickException
     {
          AppGameContainer app = 
 			new AppGameContainer(new Wrapper());
          app.setDisplayMode(gamewidth, gameheight, false);
          app.setTargetFrameRate(100);
          app.start();
     }
     
     public void keyPressed(int button, char c)
     {
     	if(button == Input.KEY_W || button == Input.KEY_UP)
     	{
     		wPressed = true;
     	}
     	else if(button == Input.KEY_A || button == Input.KEY_LEFT)
     	{
     		aPressed = true;
     	}
     	else if(button == Input.KEY_D || button == Input.KEY_RIGHT)
     	{
     		dPressed = true;
     	}
     	else if(button == Input.KEY_S || button == Input.KEY_DOWN)
     	{
     		sPressed = true;
     	}
     	else if(button == Input.KEY_SPACE)
     	{
     		playerAttack();
     	}
     }
     
     public void keyReleased(int button, char c)
     {
     	if(button == Input.KEY_W || button == Input.KEY_UP)
     	{
     		wPressed = false;
     	}
     	else if(button == Input.KEY_A || button == Input.KEY_LEFT)
     	{
     		aPressed = false;
     	}
     	else if(button == Input.KEY_D || button == Input.KEY_RIGHT)
     	{
     		dPressed = false;
     	}
     	else if(button == Input.KEY_S || button == Input.KEY_DOWN)
     	{
     		sPressed = false;
     	}
     }
     
     public void playerAttack()
     {
     	 int toRemove = -1;
 		 boolean hit = false;
 		 int modifier = 49;
 		 if(atkLeft == true)
 		 {
 			 modifier = -20;
 		 }
 		 //tempImg.add(new Entity("res/hitbox.jpg", (int)x+modifier, (int)y));
 		 
 		 Rectangle attack = new Rectangle(x+modifier, y-49, 25, 50);
 		 for(Entity enemy : room.enemies)
 		 {
 			 if(enemy.type == Entity.Type.ENEMY && enemy.collision(attack))
 			 {
 				 hit = true;
 				 if(!enemy.applyDamage(1))
 				 {
 					 toRemove = room.enemies.indexOf(enemy);
 				 }
 			 }
 		 }
 		 if (room.enemies.size() > toRemove && toRemove > -1 && hit == true){
 			 room.enemies.remove(toRemove);
 		 }
     }
 }
