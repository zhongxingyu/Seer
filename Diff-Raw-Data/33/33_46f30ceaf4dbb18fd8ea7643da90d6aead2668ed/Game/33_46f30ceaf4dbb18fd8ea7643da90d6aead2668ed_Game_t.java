 package com.Redream.TinyConflict;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.Redream.TinyConflict.Entity.Entity;
 import com.Redream.TinyConflict.Graphics.Camera;
 import com.Redream.TinyConflict.Graphics.Display;
 import com.Redream.TinyConflict.Graphics.Font;
 import com.Redream.TinyConflict.Graphics.Renderable;
 import com.Redream.TinyConflict.Input.Input;
 import com.Redream.TinyConflict.Input.InputListener;
 import com.Redream.TinyConflict.Planet.Planet;
 import com.Redream.TinyConflict.Resources.Resources;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.math.Rectangle;
 
 
 public class Game implements ApplicationListener, InputListener {
 
 	public static int WIDTH = 800;
 	public static int HEIGHT = 480;
 
 	public static float DIAGDIST;
 
 	public static float screenRatioY;
 	public static float screenRatioX;
 	public static boolean debug;
 	public static boolean mobile;
 
 	private SpriteBatch batch;
 	private Display display;
 	private Input input;
 
 	private double unprocessed;
 	public static final double TICK_TIME = 0.01666667;
 
 	public static List<Planet> planets = new ArrayList<Planet>();
 	public static List<Entity> entities = new ArrayList<Entity>();
 	public static boolean editing;
 	public static Planet migratingTo;
 	private Renderable stars;
 
 	public float pRot = 0;
 	private boolean needsInit;
 
 	public static int gameStage;
 	public static int winTime;
 	
 	public Game(boolean mobile){
 		Game.mobile = mobile;
 	}
 
 	public void create() {
 		Resources.atlas = new TextureAtlas(Resources.file("pack"));
		Resources.atlas.getRegions().get(0).getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 
 		this.batch = new SpriteBatch();
 		this.display = new Display(this.batch);
 
 		this.input = new Input();
 		Gdx.input.setInputProcessor(input);
 
 		stars = new Renderable();
 		stars.tex = 3;
 		stars.z = -1;
 		stars.xScale = 4;
 		stars.yScale = 4;
 		initPlanets();
 		
		Camera.cam = new OrthographicCamera(WIDTH, HEIGHT);
		Camera.HUDcam = new OrthographicCamera(WIDTH, HEIGHT);
		
 		Input.registerListener(this);
 	}
 	
 	public void initPlanets(){
 		entities.clear();
 		planets.clear();
 		for(int i=0;i<Planet.pOrder.length;i++){
 			Planet p = null;
 			try {
 				p = Planet.pOrder[i].getClass().newInstance();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 			if(p == null)continue;
 			if(i==0){
 				p.isplayer = true;
 				Camera.target = p;
 			}
 			p.x = -500 + (i*200);
 			p.y = (float) (new Random().nextGaussian()*300);
 			planets.add(p);
 		}
 	}
 
 	public void dispose() {
 	}
 
 	public void pause() {
 	}
 
 	public void render() {
 		if (this.unprocessed < 5) {
 			this.unprocessed += Game.timeDelta();
 		}
 
 		if (this.unprocessed > 1) {
 			this.tick();
 			this.unprocessed -= 1;
 		}
 
 		this.batch.begin();
 
 		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		Gdx.gl10.glClearColor(0.05f,0.047f,0.086f,1);
 
 		if(gameStage == 2){
 			for(Planet p : planets){
 				p.queueRender(display);
 			}
 
 			for(Entity e : entities){
 				e.queueRender(display);
 			}
 			stars.x = (float) (Camera.cam.position.x * 0.5)-400;
 			stars.y = (float) (Camera.cam.position.y  * 0.5)-400;
 			stars.queueRender(display);
 			
 			if(winTime > 100){
 				new Font("You win! Congratulations!", Game.WIDTH/2, Game.HEIGHT - 50, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 			}
 		}else{
 
 
 			Renderable p = new Renderable();
 			p.tex = 25;
 			p.applyCam = false;
 			p.xScale = 3;
 			p.yScale = 3;
 			p.queueRender(display);
 			p.origX = 28;
 			p.origY = 28;
 			p.rot = pRot;
 			p.x = 90;
 			p.y = 170;
 
 			Renderable p1 = new Renderable();
 			p1.tex = 26;
 			p1.x = Game.WIDTH-160;
 			p1.y = 20;
 			p1.applyCam = false;
 			p1.xScale = 3;
 			p1.yScale = 3;
 			p1.origX = 33;
 			p1.origY = 33;
 			p1.rot = pRot + 60;
 			p1.queueRender(display);
 
 			Renderable p2 = new Renderable();
 			p2.tex = 0;
 			p2.z = -1;
 			p2.x = Game.WIDTH-160;
 			p2.y = 400;
 			p2.applyCam = false;
 			p2.xScale = 3;
 			p2.yScale = 3;
 			p2.origX = 58;
 			p2.origY = 50;
 			p2.rot = pRot + 160;
 			p2.queueRender(display);
 
 			if(gameStage == 0){
 				Renderable logo = new Renderable();
 				logo.tex= 29;
 				logo.x = Game.WIDTH/2 - 78*3;
 				logo.y = Game.HEIGHT - 150;
 				logo.applyCam = false;
 				logo.xScale = 3;
 				logo.yScale = 3;
 				logo.z = 100;
 				logo.queueRender(display);
 				
 				new Font("Press <space> to begin", Game.WIDTH/2, Game.HEIGHT/2 - 50, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 			}else if(gameStage == 1){
 				new Font("You are a lonely bacterium, on a rock, in space.", Game.WIDTH/2, Game.HEIGHT - 50, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 				new Font("But you feel like you aren't important.", Game.WIDTH/2, Game.HEIGHT - 100, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				new Font("Only a small part of the universe.", Game.WIDTH/2, Game.HEIGHT - 130, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				new Font("But you are going to change that.", Game.WIDTH/2, Game.HEIGHT - 180, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				new Font("You begin mitosis, and create an army of bacteria.", Game.WIDTH/2, Game.HEIGHT - 210, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				
 				new Font("This is only the beginning.", Game.WIDTH/2, Game.HEIGHT - 260, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 				
 				new Font("You are going to conquer this Solar System!", Game.WIDTH/2, Game.HEIGHT - 300, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 				
 				new Font("Use WASD to control your planet.", Game.WIDTH/2, Game.HEIGHT - 400, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				new Font("Hover over planets for more information.", Game.WIDTH/2, Game.HEIGHT - 430, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 				
 				new Font("Press <space> to begin", Game.WIDTH/2, 10, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 			}else if(gameStage == 3){
 				new Font("You are finally at peace with yourself!", Game.WIDTH/2, Game.HEIGHT - 50, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 				new Font("But you still aren't satisfied.", Game.WIDTH/2, Game.HEIGHT - 100, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 		
 				new Font("As this is only the beginning.", Game.WIDTH/2, Game.HEIGHT - 260, Font.POS_CENTER, false, Color.WHITE, 3).queueRender(display);
 				
 				new Font("Thank you for playing! Press space to restart.", Game.WIDTH/2, 10, Font.POS_CENTER, false, Color.WHITE, 2).queueRender(display);
 			}
 
 			
 		}
 
 
 
 		this.display.render();
 		this.display.renderQueue.clear();
 		this.display.renderQueueHUD.clear();
 
 		this.batch.end();
 	}
 
 	public static double timeDelta() {
 		return Gdx.graphics.getDeltaTime() / TICK_TIME;
 	}
 
 	private void tick() {
 		if(needsInit){
 			initPlanets();
 			needsInit = false;
 		}
 		if(gameStage == 2){
 			Camera.tick();
 			for(int i=0;i<planets.size();i++){
 				if(editing && !planets.get(i).editing)continue;
 				planets.get(i).tick();
 				if(planets.get(i).remove)planets.remove(i--);
 			}
 
 			for(int i=0;i<entities.size();i++){
 				entities.get(i).tick();
 				if(entities.get(i).remove)entities.remove(i--);
 			}
 			if(winTime > 0){
 				winTime++;
 				if(winTime > 400){
 					gameStage = 3;
 					winTime = 0;
 				}
 			}
 		}else{
 			pRot += 0.06f;
 			if(pRot > 360)pRot = 0;
 		}
 	}
 
 	public void resize(int width, int height) {
 		float dratio = (float)width/(float)height;
 
		Game.WIDTH = (int) (480 * dratio);
 		Game.HEIGHT = 480;
 		
 		Game.screenRatioX = (float)Game.WIDTH / (float)width;
 		Game.screenRatioY = (float)Game.HEIGHT / (float)height;
		
		Gdx.graphics.setDisplayMode(width, height, false);
		
 		DIAGDIST = (float) Math.abs(Math.sqrt(Math.pow(-Game.WIDTH, 2.0D) + Math.pow(Game.HEIGHT, 2.0D)));
		
 		Camera.cam = new OrthographicCamera(Game.WIDTH, Game.HEIGHT);
		Camera.HUDcam = new OrthographicCamera(Game.WIDTH, Game.HEIGHT);
 	}
 
 	public void resume() {
 	}
 
 	@Override
 	public boolean touchCollTest() {
 		return false;
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return null;
 	}
 	
 	public void stageProgress(){
 		if(gameStage < 2)gameStage++;
 		if(gameStage == 3){
 			gameStage = 0;
 			needsInit = true;
 		}
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if(keycode == Keys.SPACE)stageProgress();
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer) {
 		stageProgress();
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
