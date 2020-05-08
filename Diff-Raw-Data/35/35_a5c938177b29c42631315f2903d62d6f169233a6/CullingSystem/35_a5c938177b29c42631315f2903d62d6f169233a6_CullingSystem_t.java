 package platformer.core.model.systems.impl.physics;
 
 import java.util.Collection;
 
 import platformer.core.model.Camera;
 import platformer.core.model.GameObject;
 import platformer.core.model.systems.Cullable;
 import platformer.core.model.systems.GenericSystem;
 import platformer.core.model.systems.Positionable;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 
 public class CullingSystem implements GenericSystem {
 
 	private Camera view;
 	private Rectangle viewportBounds;
	private final float EXTRA_SCREEN_TIMES = 0.5f; // All around, keep that in
 												// mind.
 
 	public CullingSystem(Camera view) {
 		this.view = view;
 	}
 
 	@Override
 	public void update(Collection<GameObject> list) {
 		// Get current viewport
 		viewportBounds = view.getViewportBounds();
 
 		// Now the viewport has to be expanded to include a little bit all
 		// around.
 		viewportBounds.x = viewportBounds.x
 				- (viewportBounds.width * EXTRA_SCREEN_TIMES);
 		viewportBounds.y = viewportBounds.y
 				- (viewportBounds.height * EXTRA_SCREEN_TIMES);
 		
 		if (EXTRA_SCREEN_TIMES != 0) {
			viewportBounds.width += viewportBounds.width * EXTRA_SCREEN_TIMES * 2;
			viewportBounds.height += viewportBounds.width * EXTRA_SCREEN_TIMES * 2;
 		}
 
		int count = 0;
		
 		for (GameObject gameObject : list) {
 			Vector3 position = new Vector3(((Positionable) gameObject).getPosition());
 			
 			boolean fitsInRenderArea = position.x >= viewportBounds.x
 					&& position.y >= viewportBounds.y
 					&& position.x <= viewportBounds.x + viewportBounds.width
 					&& position.y <= viewportBounds.y + viewportBounds.height;
					
			if (fitsInRenderArea) {
				count++;
			}
 
 			((Cullable) gameObject).setIsActive(fitsInRenderArea);
 		}
		
		//Gdx.app.log("visible bodies", "" + count);
 	}
 
 }
