 package gamedev.scenes;
 
 import gamedev.game.SceneManager;
 import gamedev.game.SceneManager.SceneType;
 import gamedev.game.TmxLevelLoader;
 import gamedev.quests.Quest;
 import gamedev.quests.QuestBuildBridge;
 import gamedev.quests.QuestCatchPig;
 import gamedev.quests.QuestMakePortalWork;
 import gamedev.quests.QuestPassCanyon;
 import gamedev.quests.QuestSurviveDinos;
 
 import java.util.ArrayList;
 
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.extension.tmx.util.exception.TMXLoadException;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.util.debug.Debug;
 
 /**
  * Base class for all levels
  * 
  */
 public class GameMapScene extends BaseScene {
 
 	// TMX Map containing the Level
 	protected TMXTiledMap mTMXTiledMap;
 	protected String tmxFileName;
 
 	protected ArrayList<Quest> quests = new ArrayList<Quest>();
 
 	public GameMapScene() {
 		// Call BaseScene without calling createScene because here we need some
 		// stuff initialized before
 		super(false);
 		this.tmxFileName = "final_map.tmx";
 		this.createScene();
 	}
 
 	@Override
 	public void createScene() {
 		createMap();
 		connectPhysics();
 		createQuests();
 	}
 
 	protected void connectPhysics() {
 		this.registerUpdateHandler(this.resourcesManager.physicsWorld);
 	}
 
 	protected void createMap() {
 		// Try to load the tmx file
 		try {
 			final TMXLoader tmxLoader = new TMXLoader(
 					this.activity.getAssets(), this.engine.getTextureManager(),
 					TextureOptions.DEFAULT, this.vbom,
 					new ITMXTilePropertiesListener() {
 						@Override
 						public void onTMXTileWithPropertiesCreated(
 								final TMXTiledMap pTMXTiledMap,
 								final TMXLayer pTMXLayer,
 								final TMXTile pTMXTile,
 								final TMXProperties<TMXTileProperty> pTMXTileProperties) {
 						}
 					});
 
 			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/"
 					+ this.tmxFileName);
 
 		} catch (final TMXLoadException e) {
 			Debug.e(e);
 		}
 
 		// Set camera bounds
 		TMXLayer tmxLayerZero = this.mTMXTiledMap.getTMXLayers().get(0);
 		this.camera.setBounds(0, 0, tmxLayerZero.getWidth(),
 				tmxLayerZero.getHeight());
 		this.camera.setBoundsEnabled(true);
 
 		// Load all the objects, boundaries of our level. This is handled in a
 		// new class
 		TmxLevelLoader loader = new TmxLevelLoader(this.mTMXTiledMap, this);
 		loader.createWorldAndObjects();
 	}
 
 	public void createQuests() {
 		this.quests.add(new QuestBuildBridge(this));
 		this.quests.add(new QuestCatchPig(this));
 		this.quests.add(new QuestPassCanyon(this));
 		this.quests.add(new QuestSurviveDinos(this));
 		this.quests.add(new QuestMakePortalWork(this));
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().loadMenuScene(engine);
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GAME_MAP;
 	}
 
 	@Override
 	public void disposeScene() {
 		this.camera.setHUD(null);
 		this.detachSelf();
 		if (!this.isDisposed()) {
 			this.dispose();
 		}
 	}
 
 	public void addQuest(Quest quest) {
 		this.quests.add(quest);
 	}
 
 	public ArrayList<Quest> getQuests() {
 		return quests;
 	}
 
 	public void setQuests(ArrayList<Quest> quests) {
 		this.quests = quests;
 	}
 
 	public Quest getQuest(int i) {
 		return this.quests.get(i);
 	}
 
 }
