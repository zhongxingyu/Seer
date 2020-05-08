 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.GameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 import org.w3c.dom.css.Rect;
 
 public class MultiPlayer implements GameState {
 	
 	TiledMap map;
 	Camera camera;
 	int mapWidth, mapHeight;
 	float screenX, screenY;
 	
 	Image[] player;
 	Player[] players;
 	
 	double timer;
 
 	//NetworkServer netserv;
 	//NetworkClient netcli;
 
 	float mod = (float) 3;
 	float mouseX, mouseY;
 	
 	float viewTopLeftX, viewTopLeftY;
 	int time;
 
 	//Bullet bullet = null;
 	boolean bulletExists = false;
 	
 	// Array for storing bullets
 	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
 
 	public MultiPlayer(){
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sg)
 			throws SlickException {
 		/*try{
 			netserv = new NetworkServer();
 		}catch (IOException e){}
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {}
 		
 		System.out.println("server uppe");
 		
 		netcli = new NetworkClient("localhost", p1, p1);
 		
 		System.out.println("klient inledd");
 		
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {}
 		
 		netcli.start();
 		
 		System.out.println("klient startad");*/
 		
 		map = new TiledMap("data/untitled.tmx");
 		camera = new Camera();
 		mapWidth = map.getTileWidth() * map.getWidth();
 		mapHeight = map.getTileHeight() * map.getHeight();
 		
 		players = new Player[4];
 		player = new Image[4];
 		
 		viewTopLeftX = map.getHeight();
 		viewTopLeftY = 0;
 		
 		for(int i=0; i<4; i++){
 			players[i] = new Player("player"+(i+1));
 			player[i] = players[i].getImage();
 		}
 		players[0].setX(400);
 		players[0].setY(300);
 		
 		players[1].setX(400);
 		players[1].setY(400);
 
 		//bullet = new Bullet(new Image("data/bullet.jpg"));
 		//time=0;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sg, Graphics gx)
 			throws SlickException {
 		gx.pushTransform();
 		Rectangle view = camera.getView();
 		gx.translate(-players[0].getX() + view.getWidth()/2,
 				-players[0].getY() + view.getHeight()/2);
 		
 		player[0].drawCentered(players[0].getX(), players[0].getY());
 		
 		player[1].drawCentered(players[1].getX(), players[1].getY());
 		camera.centerArea(gx, players[0]);
 		
 		map.render((int) view.getX(), (int) view.getY(), (int) view.getX()/map.getTileWidth(), (int) view.getY()/map.getTileHeight(),
 				(int) view.getWidth()/map.getTileWidth(), (int) view.getHeight()/map.getTileHeight());
 //		map.render((int) view.getX(), (int) view.getY(), (int) view.getX()/map.getTileWidth(), (int) view.getY()/map.getTileHeight(),
 //				(int) view.getWidth()/map.getTileWidth(), (int) view.getHeight()/map.getTileHeight());
 		
 		// Draws the bullets in the new position
 		for(int i = 0; i < bullets.size(); i++){
 			bullets.get(i).getImage().drawCentered(bullets.get(i).getX(), bullets.get(i).getY());
 		}
 		gx.popTransform();
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sg, int delta)
 			throws SlickException {
 
 		Input input = gc.getInput();
  
 		double r = 0;
 		
 
 		screenX = camera.getView().getX();
 		screenY = camera.getView().getY();
 		
 		mouseX = input.getMouseX() + screenX;
 		mouseY = input.getMouseY() + screenY;
 		
 		r = Math.atan2(mouseY-players[0].getY(), mouseX-players[0].getX());
 		player[0].setRotation((float) Math.toDegrees(r+(Math.PI/2)));
 		
 		// Will update the bullets so they go in the direction of the player
 		// Destroys the bullets outside the screen
 		for(int i = 0; i < bullets.size(); i++){
 			// This will store the effect of the tide map
 			int tileID = map.getTileId((int) bullets.get(i).getX()/map.getTileWidth(), 
 					(int) bullets.get(i).getY()/map.getTileHeight(), 0);
 			
 			bullets.get(i).setX((float) (bullets.get(i).getX() + bullets.get(i).getDirectionX()));
 			bullets.get(i).setY((float) (bullets.get(i).getY() - bullets.get(i).getDirectionY()));
 //			if(!checkBorders(bullets.get(i).getX(), bullets.get(i).getY())){
 //				bullets.remove(i);
 //			}else if(players[1].checkCollision(bullets.get(i).getX(), bullets.get(i).getY()) ||
 //					tileID == 1){
 //				bullets.remove(i);
 //				bullets.add(new Bullet(new Image("data/explosion.png")));
 //				bullets.get(bullets.size()-1).setX(players[1].getX());
 //				bullets.get(bullets.size()-1).setY(players[1].getY());
 //			}
 		}
 
 		if(input.isMousePressed(0)){
 			if(bullets.size()<30){
 				float hip = 0.4f * delta;
 				float rotation = player[0].getRotation();
 				bullets.add(new Bullet(new Image("data/bullet.jpg")));
 				int i = bullets.size()-1;
 				bullets.get(i).setX(players[0].getX());
 				bullets.get(i).setY(players[0].getY());
 				bullets.get(i).setDirectionX((float) (hip * Math.sin(Math.toRadians(rotation))));
 				bullets.get(i).setDirectionY((float) (hip * Math.cos(Math.toRadians(rotation))));
 			}
 //			else if(time==10){
 //				time=0;
 //			}
 //			if(bullets.size() == 30){
 //				bullets.clear();
 //			}
 		}
 //		if(time<10 && time !=0){
 //			time++;
 //		}
 		
 		//Player 1 Controls
 		if(input.isKeyDown(Input.KEY_W)){
 				players[0].setY(players[0].getY()-2*mod);
 		}
 		if(input.isKeyDown(Input.KEY_A)){
 			players[0].setX(players[0].getX()-2*mod);
 		}
 		if(input.isKeyDown(Input.KEY_S)){
 			players[0].setY(players[0].getY()+2*mod);
 		}
 		if(input.isKeyDown(Input.KEY_D)){
 			players[0].setX(players[0].getX()+2*mod);
 		}
 		
 		if(players[0].getX() < 0){
 			players[0].setX(0);
 		}
 		else if(players[0].getY() < 0){
 			players[0].setY(0);
 		}
		else if(players[0].getX() >  map.getWidth()){
			players[0].setX(map.getWidth());
 		}
		else if(players[0].getY() > map.getHeight()){
			players[0].setY(map.getHeight());
 		}
 	}
 
 	//SPACING
 	//SPACING
 	//SPACING
 	//SPACING
 	//SPACING
 	
 	@Override
 	public void keyPressed(int arg0, char arg1) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void keyReleased(int arg0, char arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void leave(GameContainer arg0, StateBasedGame arg1) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(int arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(int arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseWheelMoved(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void inputEnded() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void inputStarted() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isAcceptingInput() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void setInput(Input arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerButtonPressed(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerButtonReleased(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerDownPressed(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerDownReleased(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerLeftPressed(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerLeftReleased(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerRightPressed(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerRightReleased(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerUpPressed(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerUpReleased(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void enter(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
