 package com.tacoid.pweek.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.tacoid.pweek.PreferenceManager;
 import com.tacoid.pweek.PreferenceManager.Preference;
 import com.tacoid.pweek.Pweek;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
 import com.tacoid.pweek.actors.BackgroundActor;
 
 public class GameServicesScreen implements Screen {
 
 	private class SignInButton extends Button {
 
 		public SignInButton() {
 			super(new TextureRegionDrawable(new TextureRegion(Pweek.getInstance().atlasGoogle.findRegion("signin_Medium_base_44dp"))),
 					new TextureRegionDrawable(new TextureRegion(Pweek.getInstance().atlasGoogle.findRegion("signin_Medium_press_44dp"))));
 			
 			this.addListener(new ClickListener() {
 				@Override
 				public void clicked(InputEvent event, float x, float y) {
 					PreferenceManager.getInstance().setPreference(Preference.SIGNIN_GP, String.valueOf(true));
 					Pweek.getInstance().getGameService().login();
 					Pweek.getInstance().setScreen(MainMenuScreen.getInstance());
 				}
 			});
 			
 			this.setPosition(VIRTUAL_WIDTH - 800 - this.getWidth(), 305);
 		}
 	}
 	
 	private class NoThxActor extends Actor {
 		private BitmapFont font;
 		private ShapeRenderer shapeRenderer;
 		private String str;
 		private float height;
 		private float width;
 
 		public NoThxActor() {
 			super();
 			font = Pweek.getInstance().manager.get("images/font64.fnt", BitmapFont.class);
 			str = "No Thanks";
 			
 			font.setScale(0.5f);
 			font.setColor(0.8f, 0.8f, 0.8f, 1f);
 			height = font.getBounds(str).height;
 			width = font.getBounds(str).width;
 
             shapeRenderer = new ShapeRenderer();
             
             this.addListener(new ClickListener() {
             	@Override
             	public void clicked(InputEvent event, float x, float y) {
             		PreferenceManager.getInstance().setPreference(Preference.SIGNIN_GP, String.valueOf(false));
             		Pweek.getInstance().setScreen(MainMenuScreen.getInstance());
             	}
             });
             
             this.setPosition(VIRTUAL_WIDTH - 350, 100 - height - 20);
             this.setWidth(width + 100);
             this.setHeight(height + 100);
 		}
 		
 		@Override
 		public void draw(SpriteBatch batch, float parentAlpha) {
 			font.setScale(0.5f);
 			font.setColor(0.3f, 0.3f, 0.3f, 1f);
 			
             batch.end();
             shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
 			shapeRenderer.begin(ShapeType.FilledRectangle);
 			shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
 			shapeRenderer.filledRect(VIRTUAL_WIDTH - 350, 100 - height - 20, width + 100, height + 40);
 			shapeRenderer.end();
 			shapeRenderer.begin(ShapeType.Rectangle);
 			shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
 			shapeRenderer.rect(VIRTUAL_WIDTH - 350, 100 - height - 20, width + 100, height + 40);
 			shapeRenderer.end();
 			batch.begin();
 			
 			font.draw(batch, str, VIRTUAL_WIDTH - 300, 100);
 		}
 	}
 	
 	private class TextSignIn extends Actor {
 		private BitmapFont font;
 		
 		public TextSignIn() {
 			font = Pweek.getInstance().manager.get("images/font64.fnt", BitmapFont.class);
 		}
 
 		@Override
 		public void draw(SpriteBatch batch, float alpha) {
 			int origX = VIRTUAL_WIDTH / 2;
 			int origY = 600;
 			
 			String str = "Sign-in with Google Play!";
 			font.setScale(1f);
 			font.setColor(1f, 1f, 1f, 1f);
 			float x = origX - font.getBounds(str).width / 2;
 			font.draw(batch, str, x, origY + 2);
 			font.draw(batch, str, x, origY - 2);
 			font.draw(batch, str, x + 2, origY);
 			font.draw(batch, str, x - 2, origY);
 			
 			font.setScale(1f);
 			font.setColor(0f, 0f, 0f, 1f);
 			font.draw(batch, str, x, origY);
 			
 			String text = "Sign in with Google to share your scores and achievements with your friends.";
 			font.setScale(0.5f);
 			font.setColor(0f, 0f, 0f, 1f);
			font.drawWrapped(batch, text, VIRTUAL_WIDTH - 750, origY - 200, 700);
 		}
 	}
 	
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static GameServicesScreen instance;
 	private Stage stage;
 	
 	private GameServicesScreen() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		stage.addActor(new TextSignIn());
 		stage.addActor(new NoThxActor());
 		stage.addActor(new SignInButton());
 	}
 	
 	public static GameServicesScreen getInstance() {
 		if(instance == null) {
 			instance = new GameServicesScreen();
 		}
 		return instance;
 	}
 	
 	@Override
 	public void dispose() {
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
 	public void render(float arg0) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		stage.draw();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		stage.setViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false);
 		stage.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		if (PreferenceManager.getInstance().isPreferenceDefined(Preference.SIGNIN_GP)) {
 			if (Boolean.valueOf(PreferenceManager.getInstance().getPreference(Preference.SIGNIN_GP))) {
 				Pweek.getInstance().getGameService().login();
 			}
 			Pweek.getInstance().setScreen(MainMenuScreen.getInstance());
 		} else {
 			Gdx.input.setInputProcessor(stage);
 		}
 	}
 
 }
