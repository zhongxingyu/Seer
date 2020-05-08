 package gamedev.scenes;
 
 import gamedev.game.SceneManager;
 import gamedev.game.SceneManager.SceneType;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 
 public class MainMenuScene extends BaseScene implements
 		IOnMenuItemClickListener {
 
 	private MenuScene menuChildScene;
 	private final int MENU_PLAY = 0;
 	private final int MENU_EXIT = 1;
 	private final int MENU_BACK = 2;
 
 	@Override
 	public void createScene() {
 		menuChildScene = new MenuScene(camera);
 
 		SpriteBackground menuBackground = new SpriteBackground(new Sprite(0, 0,
 				resourcesManager.menu_background_region, vbom) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		});
 
 		final IMenuItem playMenuItem = new ScaleMenuItemDecorator(
 				new SpriteMenuItem(MENU_PLAY,
 						resourcesManager.menu_buttons_region
 								.getTextureRegion(MENU_PLAY), vbom), 1.2f, 1f);
 
 		final IMenuItem exitMenuItem = new ScaleMenuItemDecorator(
 				new SpriteMenuItem(MENU_EXIT,
 						resourcesManager.menu_buttons_region
 								.getTextureRegion(MENU_EXIT), vbom), 1.2f, 1f);
 
 		final IMenuItem backMenuItem = new ScaleMenuItemDecorator(
 				new SpriteMenuItem(MENU_BACK,
 						resourcesManager.menu_buttons_region
 								.getTextureRegion(MENU_BACK), vbom), 1.2f, 1f);
 
 		menuChildScene.addMenuItem(playMenuItem);
 		menuChildScene.addMenuItem(exitMenuItem);
 		menuChildScene.addMenuItem(backMenuItem);
 
 		menuChildScene.setBackground(menuBackground);
 		menuChildScene.buildAnimations();
 		menuChildScene.setBackgroundEnabled(true);
 		menuChildScene.setOnMenuItemClickListener(this);
 
 		setChildScene(menuChildScene);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		if (SceneManager.getInstance().isGameMapSceneCreated()) {
 			SceneManager.getInstance().loadGameMapScene(engine);
 		} else {
 			// TODO: Warn before quitting the application.
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() {
 		menuChildScene.detachSelf();
 		menuChildScene.dispose();
 		this.detachSelf();
 		if (!this.isDisposed()) {
 			this.dispose();
 		}
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 
 		switch (pMenuItem.getID()) {
 		case MENU_PLAY:
 			boolean restart = false;
 			if (SceneManager.getInstance().isGameMapSceneCreated() == false) {
 				// Show intro scene
//				 SceneManager.getInstance().loadGameIntroScene(engine);
				SceneManager.getInstance().createGameMapScene(engine, restart);
 
 			} else {
 				// Uncomment the following lines if you want to restart the game
 				// by
 				// clicking
 				// on "New game" in the menu.
 				// if (SceneManager.getInstance().isGameMapSceneCreated()) {
 				// restart = true;
 				// }
 				SceneManager.getInstance().loadGameMapScene(engine);
 			}
 
 			return true;
 		case MENU_EXIT:
 			System.exit(0);
 			return true;
 		case MENU_BACK:
 			onBackKeyPressed();
 			return true;
 		default:
 			return false;
 		}
 	}
 }
