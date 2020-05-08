 package gamedev.scenes;
 
 import gamedev.game.SceneManager.SceneType;
 
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.color.Color;
 
 public class LoadingScene extends BaseScene {
 
 	@Override
 	public void createScene() {
		setBackground(new Background(Color.BLACK));
 		String loadingString = "Loading...";
		Text loadingText = new Text(100, 50, resourcesManager.font,
 				loadingString, loadingString.length(), new TextOptions(
 						HorizontalAlign.CENTER), vbom);
 		// TODO: Text is not displayed.
 		attachChild(loadingText);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		return;
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_LOADING;
 	}
 
 	@Override
 	public void disposeScene() {
 		this.detachSelf();
 		this.dispose();
 	}
 
 }
