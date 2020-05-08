 package com.soc.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.soc.core.SoC;
 import com.soc.utils.GameLoader;
 
 public class LoadScreen extends AbstractScreen implements InputProcessor {
 
 	FileHandle[] files;
 	private Texture background;
 	private int focusedButton;
 	private Texture handT;
 	private TextButton[] buttons;
 	private TextButtonStyle normalStyle;
 	private TextButtonStyle focusedStyle;
 
 	public LoadScreen(SoC game) {
 		super(game);
 
 		background = new Texture(Gdx.files.internal("resources/background.jpg"));
 		normalStyle = new TextButtonStyle();
 		normalStyle.font = getSkin().getFont("buttonFont");
 		normalStyle.up = getSkin().getDrawable("normal-button");
 		normalStyle.down = getSkin().getDrawable("pushed-button");
 		focusedStyle = new TextButtonStyle();
 		focusedStyle.font = getSkin().getFont("buttonFont");
 		focusedStyle.up = getSkin().getDrawable("focused-button");
 		focusedStyle.down = getSkin().getDrawable("pushed-button");
 
 		files = GameLoader.getHandles();
 		System.out.println(files.length);
 		buttons = new TextButton[files.length + 1];
 		for (int i = 0; i < files.length+1; i++) {
 			if (i == files.length) {
 				buttons[i] = new TextButton("Exit", normalStyle);
 				continue;
 			}
 			if (files[i] == null) {
 				buttons[i] = new TextButton("No save", normalStyle);
 				continue;
 			}
 			buttons[i] = new TextButton(files[i].name(), normalStyle);
 
 		}
 		focusedButton = 1;
 		SoC.game.inputMultiplexer.addProcessor(this);
 	}
 
 	@Override
 	public void show() {
 		super.show();
 		Table table = super.getTable();
 		for (int i = 0; i < buttons.length; i++) {
 			final int position = i;
 			final LoadScreen screen = this;
 			if (i != files.length) {
 				final FileHandle handle = files[i];
 				
 				buttons[i].addListener(new InputListener() {
 					
 					@Override
 					public boolean touchDown(InputEvent event, float x,
 							float y, int pointer, int button) {
 						return true;
 					}
 
 					@Override
 					public void touchUp(InputEvent event, float x, float y,
 							int pointer, int button) {
 						if(handle != null){
 							SoC.game.clearProcessors();
 							GameLoader.loadGame(handle);
 						}
 					}
 				});
 
 				buttons[i].addListener(new ClickListener() {
 					public boolean mouseMoved(InputEvent event, float x, float y) {
 						if (focusedButton != position+1) {
 							buttons[focusedButton - 1].setStyle(normalStyle);
 						}
 						focusedButton = position + 1;
 						return true;
 
 					}
 				});
 				table.add(buttons[i]).size(300, 60).uniform().spaceBottom(10);
 				table.row();
 				
 			} else {
 				buttons[i].addListener(new InputListener() {
 					@Override
 					public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 						super.touchUp(event, x, y, pointer, button);
 						SoC.game.clearProcessors();
 						SoC.game.setScreen(new MenuScreen(game));
 					}
 
 					@Override
 					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
 						return true;
 					}
 				});
 
 				buttons[i].addListener(new ClickListener() {
 					public boolean mouseMoved(InputEvent event, float x, float y) {
 						if (focusedButton != position+1) {
 							buttons[focusedButton - 1].setStyle(normalStyle);
 						}
 						focusedButton = position+1;
 						return true;
 
 					}
 
 				});
 				table.add(buttons[i]).uniform().fill();
 			}
 		}
 	}
 
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		batch.begin();
		batch.draw(background, 0, 0,
				Gdx.graphics.getDesktopDisplayMode().width,
				Gdx.graphics.getDesktopDisplayMode().height);
 		buttons[focusedButton - 1].setStyle(focusedStyle);
 		// Update delta and draw the actors inside the stage
 		batch.end();
 		stage.act(delta);
 		stage.draw();
 
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode==Keys.W || keycode==Keys.UP) {
 			buttons[focusedButton - 1].setStyle(normalStyle);
 			if (focusedButton == 1)
 				focusedButton = 4;
 			else
 				focusedButton--;
 			return true;
 		} else {
 			if (keycode==Keys.S
 					|| keycode==Keys.DOWN) {
 				buttons[focusedButton - 1].setStyle(normalStyle);
 				if (focusedButton == 4)
 					focusedButton = 1;
 				else
 					focusedButton++;
 				return true;
 			}else{
 				if(keycode==Keys.ENTER){
 					if(focusedButton==1){
 						if(files[0]!=null){
 							SoC.game.clearProcessors();
 							GameLoader.loadGame(files[0]);
 						}
 					}else{
 						if(focusedButton==2){
 							if(files[1]!=null){
 								SoC.game.clearProcessors();
 								GameLoader.loadGame(files[1]);
 							}
 						}else{
 							if(focusedButton==3){
 								if(files[2]!=null){
 									SoC.game.clearProcessors();
 									GameLoader.loadGame(files[2]);
 								}
 							}else{
 								if(focusedButton==4){
 									SoC.game.clearProcessors();
 									SoC.game.setScreen(new MenuScreen(game));
 								}
 							}
 						}
 					}
 					return true;
 				}
 			}
 		}
 		return false;
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
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 }
