 package com.soc.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.soc.core.SoC;
 import com.soc.utils.MusicPlayer;
 
 public class GameOverScreen extends AbstractScreen implements InputProcessor{
 	private Texture background;
 	private int focusedBotton;
 	private TextButton loadGameButton;
 	private TextButton menuScreenButton;
 	private TextButton []buttons;
 	private TextButtonStyle normalStyle;
 	private TextButtonStyle focusedStyle;
 	public GameOverScreen(SoC game) {
 		super(game);
 		background=new Texture(Gdx.files.internal("resources/background.jpg"));
 		normalStyle=new TextButtonStyle();
 		normalStyle.font=getSkin().getFont("buttonFont");
 		normalStyle.up=getSkin().getDrawable("normal-button");
 		normalStyle.down=getSkin().getDrawable("pushed-button");
 		focusedStyle=new TextButtonStyle();
 		focusedStyle.font=getSkin().getFont("buttonFont");
 		focusedStyle.up=getSkin().getDrawable("focused-button");
 		focusedStyle.down=getSkin().getDrawable("pushed-button");
 		loadGameButton = new TextButton( "Load Game", normalStyle);
 		menuScreenButton = new TextButton( "Go to Main Menu", normalStyle);
 		buttons=new TextButton[2];
 		buttons[0]=loadGameButton;
 		buttons[1]=menuScreenButton;
 		focusedBotton=1;
 		SoC.game.inputMultiplexer.addProcessor(this);
 		SoC.game.getScreen().dispose();
 		MusicPlayer.play("gameover-theme.ogg");
 	}
     @Override
     public void show()
     {
         super.show();
         Table table = super.getTable();
         loadGameButton.addListener( new InputListener() {
             @Override
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
             {
                 return true;
             }
             @Override
             public void touchUp(
                 InputEvent event,
                 float x,
                 float y,
                 int pointer,
                 int button )
             {
             	if(button==0){
             		  SoC.game.clearProcessors();
 		              SoC.game.setScreen(new LoadScreen(game));
             	}
 
             }
 
         } );
         
         loadGameButton.addListener(new ClickListener(){
         	public boolean mouseMoved(InputEvent event,
                     float x,
                     float y){
         		if(focusedBotton!=1){
         			buttons[focusedBotton-1].setStyle(normalStyle);
         		}
         		focusedBotton=1;
         		return true;
         		
         	}
 
         });
         table.add( loadGameButton ).size( 300, 60 ).uniform().spaceBottom( 10 );
         table.row();
 
         menuScreenButton.addListener( new InputListener() {
             @Override
             public void touchUp(
                 InputEvent event,
                 float x,
                 float y,
                 int pointer,
                 int button )
             {
             	if(button==0){
 	                SoC.game.clearProcessors();
 	                SoC.game.setScreen(new MenuScreen(game));
             	}
 
             }
             @Override
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
             {
                 return true;
             }
         } );
         
         menuScreenButton.addListener(new ClickListener(){
         	public boolean mouseMoved(InputEvent event,
                     float x,
                     float y){
         		if(focusedBotton!=2){
         			buttons[focusedBotton-1].setStyle(normalStyle);
         		}
         		focusedBotton=2;
         		return true;
         		
         	}
 
         });
         table.add( menuScreenButton ).uniform().fill().spaceBottom( 10 );
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
 		if(Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)){
 			buttons[focusedBotton-1].setStyle(normalStyle);
 			if(focusedBotton==1)
 				focusedBotton=2;
 			else
 				focusedBotton--;
 			return true;
 		}else{
 			if(Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)){
 				buttons[focusedBotton-1].setStyle(normalStyle);
 				if(focusedBotton==2)
 					focusedBotton=1;
 				else
 					focusedBotton++;
 				return true;
 			}else{
 				if(Gdx.input.isKeyPressed(Keys.ENTER)){
 					if(focusedBotton==1){
 	            		  SoC.game.clearProcessors();
 			              SoC.game.setScreen(new LoadScreen(game));
 					}else{
 						if(focusedBotton==2){
 			                SoC.game.clearProcessors();
 			                SoC.game.setScreen(new MenuScreen(game));
 						}
 					}
 					return true;
 				}
 			}
 		}	
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
 	
 	public void render(float delta) {
 	       Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
 	        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
 	        batch.begin();
	        batch.draw(background, 0, 0, Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height);
 	        buttons[focusedBotton-1].setStyle(focusedStyle);
 	        //Update delta and draw the actors inside the stage
 	        batch.end();
 	        stage.act( delta );
 	        stage.draw();
 		
 	}
 }
