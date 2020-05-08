 package se.chalmers.dat255.risk.view;
 
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import se.chalmers.dat255.risk.model.IProvince;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.input.GestureDetector.GestureListener;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 public class WorldStage extends AbstractStage implements GestureListener {
 	private Image background;
 	private Group provinceGroup;
 	private OrthographicCamera camera;
 	private float initialZoom;
 	private BoundingBox[] bounds;
 	private float width;
 	private float height;
 	private InputMultiplexer multi;
 
 	public WorldStage(List<IProvince> provinces, File positionsOnMap) {
 
 		background = new Image(Resource.getInstance().backGround);
 		camera = new OrthographicCamera();
 		provinceGroup = new Group();
 
 		multi = new InputMultiplexer(new GestureDetector(this), this);
 		camera.setToOrtho(false);
 		setCamera(camera);
 
 		width = background.getWidth();
 		height = background.getHeight();
 
 		camera.position.set(background.getWidth() / 2,
 				background.getHeight() / 2, 0);
 
 		actor = new ArrayList<AbstractView>();
 
 		try {
 			Scanner scanner = new Scanner(positionsOnMap);
 			int i = 0;
 			while (scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				String[] array = line.split("-");
 				String xCord = array[0];
 				String yCord = array[1];
 				ProvinceView provinceView = new ProvinceView(provinces.get(i),
						Integer.getInteger(xCord), Integer.getInteger(yCord));
 				actor.add(provinceView);
 				i++;
 			}
 			scanner.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		
 
 		for (int i = 0; i < actor.size(); i++) {
 			provinceGroup.addActor(actor.get(i));
 		}
 		addActor(background);
 		addActor(provinceGroup);
 
 		// attempt to add bounds for map =)
 		bounds = new BoundingBox[4];
 		bounds[0] = new BoundingBox(new Vector3(0, 0, 0), new Vector3(0,
 				height, 0));
 		bounds[1] = new BoundingBox(new Vector3(0, 0, 0), new Vector3(width, 0,
 				0));
 		bounds[2] = new BoundingBox(new Vector3(0, height, 0), new Vector3(
 				width, height, 0));
 		bounds[3] = new BoundingBox(new Vector3(width, 0, 0), new Vector3(0,
 				height, 0));
 		/*
 		 * for (Vector3 d : bounds[2].getCorners()) { Gdx.app.log("tag", "" +
 		 * d); }
 		 */
 
 	}
 
 	@Override
 	public InputProcessor getProcessor() {
 		return multi;
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		initialZoom = camera.zoom;
 		super.touchDown((int) x, (int) y, pointer, button);
 
 		return false;
 	}
 
 	@Override
 	public boolean tap(float x, float y, int count, int button) {
 
 		return false;
 	}
 
 	@Override
 	public boolean longPress(float x, float y) {
 		return false;
 	}
 
 	@Override
 	public boolean fling(float velocityX, float velocityY, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		float tmpx = camera.position.x;
 		float tmpy = camera.position.y;
 	
 		if(deltaX < 0){
 			if(tmpx < width - Gdx.graphics.getWidth()/2)
 				camera.position.x -= deltaX;
 		}else{
 			if(tmpx > Gdx.graphics.getWidth()/2){
 				camera.position.x -= deltaX;
 			}
 			
 		}
 		
 		if (tmpx >= camera.viewportWidth/2 && tmpx <= width) {
 			getCamera().position.x -= deltaX;
 		}
 		if (getCamera().position.y > 0 && deltaY > getCamera().position.y) {
 			getCamera().position.y += deltaY;
 		}
 		
 		
 		return false;
 	}
 
 	private boolean inBounds() {
 
 		for (int i = 0; i < bounds.length; i++) {
 			if (camera.frustum.boundsInFrustum(bounds[i])) {
 				// return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean zoom(float initialDistance, float distance) {
 
 		float ratio = initialDistance / distance;
 
 		camera.zoom = initialZoom * ratio;
 
 		return false;
 	}
 
 	@Override
 	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
 			Vector2 pointer1, Vector2 pointer2) {
 		return false;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 
 	}
 }
