 package com.ruthlessgames.ArkaPong;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 
 public class Level implements Screen{
 	ArrayList <Block> blocks = new ArrayList<Block>(10);
 	int nivel_actual;
 	int nr_blocks;
 	boolean time_level;
 	Sprite background;
 	GameMain gamemain;
 	WaitScreen waitscreen;
 	Stage stage;
 	Table table;
 	Random rdm;
 	Player player;
 	Sprite player_img;
 	Ball ball;
 	Sprite Bola_img;
 	
 	Level(int lvl, int nr_b, boolean timed, Sprite bg, GameMain GM)
 	{
 		this.gamemain = GM;
 		this.waitscreen = new WaitScreen();
 		player = new Player();
 		player_img = new Sprite(new Texture(Gdx.files.internal("gfx/player.png")));
 		player.setDrawable(new TextureRegionDrawable(player_img));
 		player.setSize(GameMain.s_width/5, GameMain.s_height/25);
 		player.setPosition(GameMain.s_width/2 - player.getWidth()/2, GameMain.s_height/25);
 		
 		ball = new Ball(0);
 		Bola_img = new Sprite(new Texture(Gdx.files.internal("gfx/bola.png")));
 		ball.setDrawable(new TextureRegionDrawable(Bola_img));
 		ball.setSize(GameMain.s_width/25, GameMain.s_width/25);
		ball.setPosition(GameMain.s_width/2, GameMain.s_height/2);
 		
 		
 		
 		this.nivel_actual = lvl;
 		nr_blocks = nr_b;
 		time_level = timed;
 		background = bg;
 		rdm = new Random();
 		
 		stage = new Stage(GameMain.s_width,GameMain.s_height,true); //experimentar true/false
 		table = new Table();
 		table.setFillParent(true);
 		table.setBackground(new TextureRegionDrawable(background));
 		table.addActor(player);
 		table.addActor(ball);
 		stage.addActor(table);
 		
 		populate_blocks();
 	}
 	void populate_blocks(){
 		switch(nivel_actual){
 			case 1:{
 //				for(int i =0; i < 15;i++)
 //				{
 //					
 //					blocks.add(new Block(1,true));
 //					blocks.get(i).setPosition(x, y);
 //					blocks.get(i).setSize(w, h)
 //					table.add(blocks.get(i));
 //				}
 			}
 		}
 	}
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		Gdx.app.log("123",	"yo");
 		update();
 		stage.act(delta);
 		stage.draw();
 		
 	}
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
		
 	}
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
		
 		
 	}
 	
 	private void update(){
 		player.update();
 		ball.setPosition(ball.getX()+ ball.speed*ball.velocity.x, ball.getY()+ball.speed*ball.velocity.y);
 		if((ball.getY() + ball.getHeight() >= GameMain.s_height) || ((ball.getY() <= player.getY()+ player.getHeight()&& ball.getY() >= player.getY()+ player.getHeight() -5) && (ball.getX() > player.getX() && ball.getX()+ball.getWidth() < player.getX()+player.getWidth()))){
 			ball.velocity.y *= -1;
 		}
 		else if(ball.getX() <= 0 || ball.getX()+ball.getWidth()>= GameMain.s_width)
 		{
 			ball.velocity.x *= -1;
 		}
 		else if(ball.getY()<0){
 			gamemain.setScreen(waitscreen);
 			gamemain.androidbridge.ShowGameOver();
 		}
 			
 	}
 	
 }
