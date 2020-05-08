 package com.dailey.l5radventure;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL11;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 
 public class GoScreen implements Screen, InputProcessor{
 	
 	TextureAtlas goAtlas;
 	TextureRegion buttonUp, buttonDown;
 	NinePatch buttonUp9, buttonDown9;
 	BitmapFont font;
 	SpriteBatch batch;
 	GoBoard board;
 	Process gnugo=null;		//gnugo ai, must support GTP
 	BufferedReader processOutput;
 	BufferedReader processError;
 	OutputStreamWriter processInput;
 	ReaderThread reader;
 	ArrayBlockingQueue<String> messages;
 	String scoreSummary;
 	Skin skin;
 	double score;
 	
 	Stage ui;
 
 	
 	@Override
 	public void render(float delta) {
 		//Gdx.app.log(L5RGame.LOG, "Rendering...");
 		Gdx.gl.glClearColor(0f,0f,0f,0f);
 		Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
 		
 	
 
 		batch.begin();
 		
 		board.draw(batch);
 		font.setColor(Color.WHITE);
 		if (board.getGame().whiteResign)
 		{
 			font.draw(batch, "White resigns.", 0, Gdx.graphics.getHeight()-30);
 		}
 		else
 			font.draw(batch, scoreSummary, 0, Gdx.graphics.getHeight()-30);
 
 		font.draw(batch, "Free Moves: "+board.getFreeMoves(), Gdx.graphics.getWidth()-300, Gdx.graphics.getHeight()-30);	
 		if (board.getGoban().getBoundingRectangle().contains(board.getpX(), board.getpY()))
 				font.draw(batch, board.getProsMove(), board.getpX(), board.getpY());
 		ui.act(delta);
 
 		batch.end();
 
 		
		if ((!board.getGame().isOver()) && (board.getGame().whiteResign || (board.getGame().whitePass) && board.getGame().blackPass))
 		{
 			board.getGame().setOver(true);
 			LabelStyle style = new LabelStyle();
 			style.font = font;
 			Label result = new Label("You win!", style);
 			if (scoreSummary.split(" ")[0].equalsIgnoreCase("white"))
 				result = new Label("You lose!", skin);
 			result.setBounds(-30, Gdx.graphics.getHeight()/2, 60, 20);
			result.addAction(Actions.moveTo(Gdx.graphics.getWidth()/2, result.getY(), 3));
 			ui.addActor(result);
 		}
 		ui.draw();
 
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 
 		Gdx.app.log(L5RGame.LOG, "Showing...");
 		batch = new SpriteBatch();
 		goAtlas = new TextureAtlas("data/goScreen.atlas");
 		font = new BitmapFont(Gdx.files.internal("data/ubuntu.fnt"), goAtlas.findRegion("ubuntu"), false);
 		
 		ui = new Stage();
 		
 		
 		buttonUp9 = new NinePatch(goAtlas.findRegion("blue"));
 		buttonDown9 = new NinePatch(goAtlas.findRegion("green"));
 		
 		skin = new Skin();
 		skin.add("blue", goAtlas.findRegion("blue"), TextureRegion.class);
 		skin.add("green", goAtlas.findRegion("green"), TextureRegion.class);
 		
 		
 		Table table = new Table();
 		table.setFillParent(true);
 		table.right();
 
 		
 		TextButtonStyle style = new TextButtonStyle(skin.getDrawable("blue"), skin.getDrawable("green"), skin.getDrawable("blue"));
 		style.font = font;
 		
 		
 		TextButton pass = new TextButton("Pass", style);
 		pass.addListener(new ChangeListener(){
 
 			@Override
 			public void changed(ChangeEvent event, Actor actor) 
 			{
 				board.moveBlackPass();
 				
 			}
 			
 		});
 		table.add(pass);
 
 		
 		table.row();
 		
 		TextButton freeMove = new TextButton("Free Move",style);
 		freeMove.addListener(new ChangeListener(){
 
 			@Override
 			public void changed(ChangeEvent event, Actor actor) 
 			{
 				board.setFreeMoves(board.getFreeMoves()+1);
 				
 			}
 			
 		});
 		table.add(freeMove);
 		
 		table.row();
 		
 		TextButton quit = new TextButton("Quit", style);
 		quit.addListener(new ChangeListener(){
 			
 			public void changed(ChangeEvent event, Actor actor)
 			{
 				Gdx.app.exit();
 			}
 		});
 		
 		
 		table.add(quit);
 		
 		
 		
 		
 		
 		
 		
 		ui.addActor(table);
 		
 		String os = System.getProperty("os.name").toLowerCase();
 		Gdx.app.log("GoScreen", os);
 		
 		//processError = new BufferedReader(new InputStreamReader(gnugo.getErrorStream()));
 		
 		String pwd = System.getProperty("user.dir");
 		
 		try {
 			if (os.equals("linux") || os.equals("unix"))
 			{
 				String loc = pwd+"/data/ai/gnugo";
 				Runtime.getRuntime().exec("chmod +x "+loc);
 				gnugo = Runtime.getRuntime().exec("gnugo --mode gtp --level 1");
 				//gnugo = Runtime.getRuntime().exec(loc+" --mode gtp --level 1");
 			}
 			else
 			{
 				String loc = pwd+"/data/ai/gnugo.exe";
 				gnugo = Runtime.getRuntime().exec(loc+" --mode gtp --level 1");
 			}
 		} catch (IOException e) {
 			Gdx.app.log("GoScreen", "Problem starting gnugo!");
 			System.out.println("Problem starting gnugo!");
 			e.printStackTrace();
 		}
 		
 		processOutput = new BufferedReader(new InputStreamReader(gnugo.getInputStream()));
 		processInput = new OutputStreamWriter(gnugo.getOutputStream());
 
 		
 
 		messages = new ArrayBlockingQueue<String>(30);
 		
 		board = new GoBoard(goAtlas, processInput, messages, this);
 		board.setX(0);
 		board.setY(0);
 	
 		InputMultiplexer mux = new InputMultiplexer();
 		mux.addProcessor(ui);
 		mux.addProcessor(board);
 
 		Gdx.input.setInputProcessor(mux);
 	
 		reader = new ReaderThread(processOutput, messages);
 		reader.start();
 		
 		scoreSummary = "Even game";
 		
 		
 		
 		
 		
 
 	
 		
 		Gdx.app.log(L5RGame.LOG, "...shown.");
 	}
 	
 	public Stage getUi() {
 		return ui;
 	}
 
 	public void setUi(Stage ui) {
 		this.ui = ui;
 	}
 
 	public void updateScore(String scoreRaw)
 	{
 		String[] scoreSplit = scoreRaw.split(" ");
 		if (scoreSplit[0].equals("="))
 		{
 			String ahead = "Black";
 			if (scoreSplit[1].charAt(0)=='W')
 				ahead = "White";
 			scoreSummary = ahead + " winning by " + scoreSplit[1].substring(2);			
 			
 		}
 		
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
 	public void dispose() 
 	{
 		goAtlas.dispose();
 		batch.dispose();
 		font.dispose();
 		
 		
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
