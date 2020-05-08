 package gamedev.scenes;
 
 import gamedev.game.SceneManager.SceneType;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 
 public class SplashScene extends BaseScene {
 
 	private Sprite splash;
 
 	@Override
 	public void createScene() {
 		splash = new Sprite(800, 480,
 				resourcesManager.splash_region, vbom) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		};
 
 		// splash.setScale(1.5f);
 		splash.setPosition(camera.getWidth() / 2
 				- resourcesManager.splash_region.getWidth() / 2,
				camera.getHeight() / 2
						- resourcesManager.splash_region.getHeight() / 2);
 		attachChild(splash);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_SPLASH;
 	}
 
 	@Override
 	public void disposeScene() {
 		// splash.detachSelf();
 		// splash.dispose();
 		this.detachSelf();
 		this.dispose();
 	}
 
 }
