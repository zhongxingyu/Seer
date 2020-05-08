 package com.epic.bobrunningpuzzle.sceens;
 
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.epic.bobrunningpuzzle.BobRunningPuzzle;
 
 public class HelpScreen implements Screen {
 	private Stage stage; // done
 	private TextureAtlas atlas; // done
 	private Skin skin; // done
 	private Table table; // done
 	private TweenManager tweenManager;
 	private TextButton buttonBack; // 50%
 	private BitmapFont whiteFont, blackFont, whiteFont_mistral,
 			blackFont_mistral; // done
 	private Label heading, heading_mistral;
 
 	private int width, height; // the width and height of the screen used by the
 								// Android touch events.
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		Table.drawDebug(stage);
 		
 		
 		stage.act(delta);
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		this.width = width;
 		this.height = height;
 	}
 
 	@Override
 	public void show() {
 		Gdx.app.log(BobRunningPuzzle.GAMELOG, this.getClass().getName()+"#show()");
 		stage = new Stage();
 
 		Gdx.input.setInputProcessor(stage);
 
		atlas = new TextureAtlas("ui/button.pack");
		skin = new Skin(atlas);
 		table = new Table(skin);
 		table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		// Creating Fonts
 		whiteFont = new BitmapFont(Gdx.files.internal("font/whiteFont.fnt"),
 				false);
 		blackFont = new BitmapFont(Gdx.files.internal("font/blackFont.fnt"),
 				false);
 		whiteFont_mistral = new BitmapFont(
 				Gdx.files.internal("font/whiteFont_mistral.fnt"), false);
 		blackFont_mistral = new BitmapFont(
 				Gdx.files.internal("font/blackFont_mistral.fnt"), false);
 
 		// Creating Buttons
 		TextButtonStyle textButtonStyle = new TextButtonStyle();
 		textButtonStyle.up = skin.getDrawable("button.up");
 		textButtonStyle.down = skin.getDrawable("button.down");
 		textButtonStyle.pressedOffsetX = 1;
 		textButtonStyle.pressedOffsetY = -1;
 		textButtonStyle.font = blackFont;
 
 		buttonBack = new TextButton("BACK", textButtonStyle);
 		buttonBack.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenuScreen());
 			}
 		});
 		buttonBack.pad(15);
 		
 		table.row();
 		table.add(buttonBack);
 		table.getCell(buttonBack).spaceBottom(10);
 		
 		table.debug(); //TODO remove later
 		stage.addActor(table);
 
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
 		stage.dispose();
 		atlas.dispose();
 		skin.dispose();
 	}
 
 }
