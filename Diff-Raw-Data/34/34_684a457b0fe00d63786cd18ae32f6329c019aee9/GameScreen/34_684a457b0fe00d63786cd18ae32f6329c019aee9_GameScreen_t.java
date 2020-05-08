 package fr.odai.zerozeroduck;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 
 import fr.odai.zerozeroduck.controller.MainController;
 import fr.odai.zerozeroduck.model.World;
 
 public class GameScreen implements Screen, InputProcessor {
 	
 	private World world;
 	private WorldRenderer renderer;
 	private MainController controller;
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		controller.update(delta);
 		renderer.render();
 	}
 
 	@Override
 	public void show() {
 		world = new World();
		renderer = new WorldRenderer(world, false);
 		controller = new MainController(world);
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		renderer.setSize(width, height);
 	}
 	
 
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 
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
 		Gdx.input.setInputProcessor(null);
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.SPACE)
 			controller.patatePressed();
 		if (keycode == Keys.ENTER)
 			controller.killallPressed();
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.SPACE)
 			controller.patateReleased();
 		if (keycode == Keys.ENTER)
 			controller.killallReleased();
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
