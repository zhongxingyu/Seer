 package org.newdawn.test.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g3d.lights.Lights;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 
 public class OpenGLTest implements ApplicationListener, InputProcessor {
 	
 	private Camera camera;
 	private float cameraXRot = -45,cameraYRot = 45;
 	private Cube cube;
 	private Shape square;
 	private List<Renderable> renderables = new ArrayList<Renderable>();
 	private List<Renderable> transparentRenderables = new ArrayList<Renderable>();
 	private Comparator<Renderable> depthComparator;
 	private BitmapFont font;
 	private SpriteBatch spriteBatch;
 	private Lights lights;
 	
 	private Vector3 tempVector01 = new Vector3();
 	
 	@Override
 	public void create () {
 		Gdx.app.setLogLevel(Application.LOG_DEBUG);
 		Gdx.app.debug("init", "In create");
 		
 		Gdx.gl20.glClearColor(0, 0, 0, 1);
 		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
 		Gdx.gl20.glDepthFunc(GL20.GL_LESS);
 
 		Gdx.input.setInputProcessor(this);
 
 		camera = new PerspectiveCamera(67, 1, 1);
 		camera.near = 1;
 		camera.far = 3000;
 
 		depthComparator = new Comparator<Renderable>() {
 
 			@Override
 			public int compare(Renderable o1, Renderable o2) {
 				float o2len2 = o2.getPosition(tempVector01).sub(camera.position).len2();
 				float o1len2 = o1.getPosition(tempVector01).sub(camera.position).len2();
 				if(o2len2 < o1len2) {
 					return -1;
 				} else if(o2len2 > o1len2) {
 					return 1;
 				} else {
 					return 0;
 				}
 			}
 		};
 		
 		cube = new Cube("textures/nd-logo.png", false, new float[]{1,1,1,1});		
 		renderables.add(cube);
 		renderables.add(new Triangle(false));
 		square = new Square(true, "textures/star.png");
 		square.setPosition(0, 3, 0);
 		transparentRenderables.add(square);		
 		
 		spriteBatch = new SpriteBatch();
 		font = new BitmapFont();
 		font.setColor(0, 1, 0, 1);
 		
 		lights = new Lights();
 		lights.ambientLight.set(0.5f, 0.5f, 0.5f, 1);
 	}
 
 	@Override
 	public void resize (int width, int height) {
 		Gdx.app.debug("init", "In resize, new size: " + width + "x" + height);
 
 		camera.viewportWidth = width;
 		camera.viewportHeight = height;
 		
 		if(spriteBatch!=null) {
 			spriteBatch.dispose();
 		}
 		spriteBatch = new SpriteBatch();
 		
 		rotateCamera(0,0);
 	}
 
 	private void rotateCamera(float xRot, float yRot) {
 		cameraXRot += xRot;
 		cameraYRot += yRot;
 		
 		if(cameraYRot>180) cameraYRot = cameraYRot - 360;
 		if(cameraYRot < -180) cameraYRot = cameraYRot + 360;
 		
 		if(cameraXRot > 90) cameraXRot = 90;
 		if(cameraXRot < -90) cameraXRot = -90;
 		
 		camera.position.set(0,0,7.1f);
 		camera.position.rotate(cameraXRot, 1, 0, 0);
 		camera.position.rotate(cameraYRot, 0, 1, 0);
 		
		camera.direction.set(0,0,0);
		camera.direction.sub(camera.position);
		
 		camera.update();
 	}
 
 	@Override
 	public void render () {
 		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 		
 		float animationRatio = (TimeUtils.millis() % 2000)/2000.0f;
 		
 		float offset = MathUtils.sin(animationRatio * (MathUtils.PI2));
         cube.setPosition(offset * 3f,0,-3);
         cube.setRotation(animationRatio * 360, 1, 0, 0);
 
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
         for(Renderable renderable : renderables) {
         	renderable.render(camera.view, camera.projection, lights);
         }
         
         Collections.sort(transparentRenderables, depthComparator);
         for(Renderable renderable : transparentRenderables) {
         	renderable.render(camera.view, camera.projection, lights);
         }
         
         spriteBatch.begin();
         font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, font.getCapHeight() + 5);
         spriteBatch.end();
 	}
 
 	@Override
 	public void pause () {
 		Gdx.app.debug("init", "In pause");
 	}
 
 	@Override
 	public void resume () {
 		Gdx.app.debug("init", "In resume");
 	}
 
 	@Override
 	public void dispose () {
 		Gdx.app.debug("init", "In dispose");
 		
 		if(font!=null) font.dispose();
 		
 		if(spriteBatch!=null) spriteBatch.dispose();
 		
         for(Renderable renderable : renderables) {
         	renderable.dispose();
         }
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 //		Gdx.app.debug("touch", "keyDown: " + keycode);
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 //		Gdx.app.debug("touch", "keyUp: " + keycode);
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 //		Gdx.app.debug("touch", "keyTyped: \"" + character + "\"");
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 //		Gdx.app.debug("touch", "touchDown: " + pointer + ", " + button);
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 //		Gdx.app.debug("touch", "touchUp: " + pointer + ", " + button);
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 //		Gdx.app.debug("touch", "touchDragged: " + pointer + ", x: " + Gdx.app.getInput().getDeltaX(pointer) + ",y: " + Gdx.app.getInput().getDeltaY(pointer));
 		rotateCamera(-Gdx.app.getInput().getDeltaY(pointer),-Gdx.app.getInput().getDeltaX(pointer));
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 //		Gdx.app.debug("touch", "mouseMoved: ");
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 //		Gdx.app.debug("touch", "scrolled: ");
 		return false;
 	}
 }
