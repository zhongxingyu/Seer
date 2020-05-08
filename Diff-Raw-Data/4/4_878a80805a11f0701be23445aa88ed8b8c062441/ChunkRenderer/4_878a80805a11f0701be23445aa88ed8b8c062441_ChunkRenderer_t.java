 package fi.haju.haju3d.client.ui;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.plugins.ClasspathLocator;
 import com.jme3.bounding.BoundingSphere;
 import com.jme3.bounding.BoundingVolume;
 import com.jme3.collision.CollisionResults;
 import com.jme3.font.BitmapText;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.post.filters.CartoonEdgeFilter;
 import com.jme3.post.filters.FogFilter;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.shadow.DirectionalLightShadowRenderer;
 import com.jme3.shadow.EdgeFilteringMode;
 import com.jme3.system.AppSettings;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture2D;
 import com.jme3.util.SkyFactory;
 import com.jme3.water.WaterFilter;
 
 import fi.haju.haju3d.client.Character;
 import fi.haju.haju3d.client.ClientSettings;
 import fi.haju.haju3d.client.CloseEventHandler;
 import fi.haju.haju3d.client.ui.input.InputActions;
 import fi.haju.haju3d.client.ui.input.CharacterInputHandler;
 import fi.haju.haju3d.client.ui.mesh.ChunkSpatialBuilder;
 import fi.haju.haju3d.protocol.Vector3i;
 import fi.haju.haju3d.protocol.world.Tile;
 import fi.haju.haju3d.protocol.world.TilePosition;
 
 /**
  * Renderer application for rendering chunks from the server
  */
 @Singleton
 public class ChunkRenderer extends SimpleApplication {
   
   private static final float SELECTOR_DISTANCE = 10.0f;
 
   private static final float MOVE_SPEED = 40;
   
   private static final int CHUNK_CUT_OFF = 3;
   private static final Vector3f lightDir = new Vector3f(-0.9140114f, 0.29160172f, -0.2820493f).negate();
 
   @Inject
   private ChunkSpatialBuilder builder;
   @Inject
   private WorldManager worldManager;
   @Inject
   private CharacterInputHandler inputHandler;
 
   private ClientSettings clientSettings;
   
   private DirectionalLight light;
   private CloseEventHandler closeEventHandler;
   private boolean isFullScreen = false;
   private Node terrainNode = new Node("terrain");
   private Character character; 
   private TilePosition selectedTile;
   private TilePosition selectedBuildTile;
   private Node selectedVoxelNode;
   private BitmapText crossHair;
   private BitmapText selectedMaterialGui;
   private ViewMode viewMode = ViewMode.FLYCAM;
   private Tile selectedBuildMaterial = Tile.BRICK;
   
   @Inject
   public ChunkRenderer(ClientSettings clientSettings) {
     clientSettings.init();
     this.clientSettings = clientSettings;
     setDisplayMode();
     setShowSettings(false);
   }
 
   private void setDisplayMode() {
     AppSettings settings = new AppSettings(true);
     settings.setVSync(true);
     settings.setAudioRenderer(null);
     settings.setFullscreen(isFullScreen);
     settings.setResolution(clientSettings.getScreenWidth(), clientSettings.getScreenHeight());
     setSettings(settings);
   }
 
   @Override
   public void simpleInitApp() {
     setDisplayMode();
     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
     assetManager.registerLocator("assets", new ClasspathLocator().getClass());
     
     this.builder.init();
     this.worldManager.start();
     
     setupInput();
     setupCamera();
     setupSky();
     setupLighting();
     setupCharacter();
     setupPostFilters();
     setupSelector();
     
     this.inputHandler.register(inputManager);
 
     rootNode.attachChild(terrainNode);
   }
 
   private void setupCharacter() {
     character = new Character(new Node("character"));
     character.getNode().setLocalTranslation(worldManager.getGlobalPosition(new Vector3i().add(32, 62, 32)));
 
     Box characterMesh = new Box(0.3f, 0.8f, 0.3f);
     Geometry characterModel = new Geometry("CharacterModel", characterMesh);
 
     ColorRGBA color = ColorRGBA.Red;
     Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
     mat.setBoolean("UseMaterialColors", true);
     mat.setColor("Ambient", color);
     mat.setColor("Diffuse", color);
     characterModel.setMaterial(mat);
 
     character.getNode().attachChild(characterModel);
 
     rootNode.attachChild(character.getNode());
   }
 
   private void setupCamera() {
     getFlyByCamera().setMoveSpeed(MOVE_SPEED);
     getFlyByCamera().setRotationSpeed(CharacterInputHandler.MOUSE_X_SPEED);
     getCamera().setLocation(worldManager.getGlobalPosition(new Vector3i().add(32, 62, 62)));
    getCamera().setFrustumPerspective(45f, (float) getCamera().getWidth() / getCamera().getHeight(), 0.1f, 200f);
     
     guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
     crossHair = new BitmapText(guiFont, false);
     crossHair.setSize(guiFont.getCharSet().getRenderedSize() * 2);
     crossHair.setText("+");
     crossHair.setLocalTranslation(settings.getWidth() / 2 - crossHair.getLineWidth()/2, settings.getHeight() / 2 + crossHair.getLineHeight()/2, 0);
   }
   
   private void showSelectedMaterial() {
     selectedMaterialGui = new BitmapText(guiFont, false);
     selectedMaterialGui.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
     selectedMaterialGui.setText(selectedBuildMaterial.name());
     selectedMaterialGui.setLocalTranslation(20, settings.getHeight() - 20, 0);
     guiNode.attachChild(selectedMaterialGui);
   }
   
   private void hideSelectedMaterial() {
     guiNode.detachChild(selectedMaterialGui);
   }
 
   private void setupInput() {
     
   }
   
   private void updateWorldMesh() {
     worldManager.setPosition(getCurrentChunkIndex());
   }
 
   private Vector3i getCurrentChunkIndex() {
     return worldManager.getChunkIndexForLocation(getCamera().getLocation());
   }
 
   private void updateChunkSpatialVisibility() {
     Vector3i chunkIndex = getCurrentChunkIndex();
     terrainNode.detachAllChildren();
     for (Vector3i pos : chunkIndex.getSurroundingPositions(CHUNK_CUT_OFF, CHUNK_CUT_OFF, CHUNK_CUT_OFF)) {
       ChunkSpatial cs = worldManager.getChunkSpatial(pos);
       if (cs != null) {
         terrainNode.attachChild(pos.equals(chunkIndex) ? cs.highDetail : cs.lowDetail);
       }
     }
   }
 
   private void setupLighting() {
     light = new DirectionalLight();
     light.setDirection(lightDir.normalizeLocal());
     light.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(1.0f));
     rootNode.addLight(light);
 
     AmbientLight al = new AmbientLight();
     al.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(0.6f));
     rootNode.addLight(al);
 
     DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
     dlsr.setLight(light);
     dlsr.setShadowIntensity(0.4f);
     dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
     viewPort.addProcessor(dlsr);
   }
   
   private void setupSelector() {
     Box selectorMesh = new Box(WorldManager.SCALE/2 * 1.05f, WorldManager.SCALE/2 * 1.05f, WorldManager.SCALE/2 * 1.05f);
     Geometry selectorModel = new Geometry("SelectorModel", selectorMesh);
 
     ColorRGBA color = ColorRGBA.Red;
     Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
     mat.setBoolean("UseMaterialColors", true);
     mat.setColor("Ambient", color);
     mat.setColor("Diffuse", color);
     mat.getAdditionalRenderState().setWireframe(true);
     selectorModel.setMaterial(mat);
     
     selectedVoxelNode = new Node();
     selectedVoxelNode.attachChild(selectorModel);
   }
 
   private void setupPostFilters() {
     FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
 
     CartoonEdgeFilter rimLightFilter = new CartoonEdgeFilter();
     rimLightFilter.setEdgeColor(ColorRGBA.Black);
 
     rimLightFilter.setEdgeIntensity(0.5f);
     rimLightFilter.setEdgeWidth(1.0f);
 
     rimLightFilter.setNormalSensitivity(0.0f);
     rimLightFilter.setNormalThreshold(0.0f);
 
     rimLightFilter.setDepthSensitivity(20.0f);
     rimLightFilter.setDepthThreshold(0.0f);
 
     fpp.addFilter(rimLightFilter);
 
     BloomFilter bloom = new BloomFilter();
     bloom.setDownSamplingFactor(2);
     bloom.setBlurScale(1.37f);
     bloom.setExposurePower(4.30f);
     bloom.setExposureCutOff(0.2f);
     bloom.setBloomIntensity(0.8f);
     fpp.addFilter(bloom);
 
     WaterFilter water = new WaterFilter(rootNode, lightDir);
     water.setCenter(new Vector3f(319.6663f, -18.367947f, -236.67674f));
     water.setRadius(26000);
     water.setWaterHeight(14);
     water.setWaveScale(0.01f);
     water.setSpeed(0.4f);
     water.setMaxAmplitude(1.0f);
     water.setFoamExistence(new Vector3f(1f, 4, 0.5f).mult(0.2f));
     water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
     water.setRefractionStrength(0.1f);
     fpp.addFilter(water);
 
    FogFilter fog = new FogFilter(new ColorRGBA(0.8f, 0.8f, 1.0f, 1.0f), 0.6f, 100.0f);
     fpp.addFilter(fog);
     viewPort.addProcessor(fpp);
   }
 
   private void setupSky() {
     Texture west = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-left.jpg");
     Texture east = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-right.jpg");
     Texture north = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-front.jpg");
     Texture south = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-back.jpg");
     Texture up = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-top.jpg");
     Texture down = assetManager.loadTexture("fi/haju/haju3d/client/textures/sky9-top.jpg");
     rootNode.attachChild(SkyFactory.createSky(assetManager, west, east, north, south, up, down));
   }
 
   @Override
   public void simpleUpdate(float tpf) {
     updateWorldMesh();
     updateChunkSpatialVisibility();
     updateCharacter(tpf);
   }
 
   private void updateCharacter(float tpf) {
     if (viewMode == ViewMode.FLYCAM) {
       return;
     }
     if (worldManager.getChunkSpatial(getCurrentChunkIndex()) == null) {
       return;
     }
 
     // apply gravity
     character.setVelocity(character.getVelocity().add(new Vector3f(0.0f, -tpf*0.5f, 0.0f)));
     Vector3f characterPos = character.getPosition();
     characterPos = characterPos.add(character.getVelocity());
 
     // check if character falls below ground level, lift up to ground level
     while (true) {
       CollisionResults res = new CollisionResults();
       int collideWith = terrainNode.collideWith(makeCharacterBoundingVolume(characterPos), res);
       if (collideWith == 0) {
         break;
       }
       Vector3f oldVelocity = character.getVelocity();
       character.setVelocity(new Vector3f(oldVelocity.x, 0.0f, oldVelocity.z));
       characterPos = characterPos.add(0, 0.01f, 0);
     }
 
     // move character based on used input
     Vector3f oldPos = characterPos.clone();
     final float walkSpeed = 10;
     if (inputHandler.getActiveInputs().contains(InputActions.WALK_FORWARD)) {
       characterPos.z += FastMath.cos(character.getLookAzimuth()) * tpf * walkSpeed;
       characterPos.x += FastMath.sin(character.getLookAzimuth()) * tpf * walkSpeed;
     }
     if (inputHandler.getActiveInputs().contains(InputActions.WALK_BACKWARD)) {
       characterPos.z -= FastMath.cos(character.getLookAzimuth()) * tpf * walkSpeed;
       characterPos.x -= FastMath.sin(character.getLookAzimuth()) * tpf * walkSpeed;
     }
     if (inputHandler.getActiveInputs().contains(InputActions.STRAFE_LEFT)) {
       characterPos.z -= FastMath.sin(character.getLookAzimuth()) * tpf * walkSpeed;
       characterPos.x -= -FastMath.cos(character.getLookAzimuth()) * tpf * walkSpeed;
     }
     if (inputHandler.getActiveInputs().contains(InputActions.STRAFE_RIGHT)) {
       characterPos.z += FastMath.sin(character.getLookAzimuth()) * tpf * walkSpeed;
       characterPos.x += -FastMath.cos(character.getLookAzimuth()) * tpf * walkSpeed;
     }
 
     // check if character hits wall. either climb it or return to old position
     Vector3f newPos = characterPos;
     int i = 0;
     final int loops = 40;
     for (i = 0; i < loops; i++) {
       newPos = newPos.add(0, 0.01f, 0);
       CollisionResults res = new CollisionResults();
       int collideWith = terrainNode.collideWith(makeCharacterBoundingVolume(newPos), res);
       if (collideWith == 0) {
         break;
       }
     }
     if (i == loops) {
       characterPos = oldPos;
     } else {
       characterPos = newPos;
     }
 
     character.setPosition(characterPos);
 
     // set camera position and rotation
     Quaternion quat = character.getLookQuaternion();
     cam.setRotation(quat);
 
     Vector3f camPos = character.getPosition().clone();
     Vector3f lookDir = quat.mult(Vector3f.UNIT_Z);
     if(viewMode == ViewMode.THIRD_PERSON) camPos.addLocal(lookDir.mult(-10));
 
     Vector3f coll = worldManager.getTerrainCollisionPoint(character.getPosition(), camPos, 0.0f);
     if (coll != null) {
       camPos.set(coll);
     }
 
     if(viewMode == ViewMode.FIRST_PERSON) {
       camPos = camPos.add(new Vector3f(0, 0.5f, 0));
     }
     
     cam.setLocation(camPos);
     
     selectedTile = worldManager.getVoxelCollisionPoint(camPos, camPos.add(cam.getDirection().normalize().mult(SELECTOR_DISTANCE)));
     selectedBuildTile = worldManager.getVoxelCollisionDirection(camPos, camPos.add(cam.getDirection().normalize().mult(SELECTOR_DISTANCE)));
     rootNode.detachChild(selectedVoxelNode);
     if(selectedTile != null) {
       selectedVoxelNode.setLocalTranslation(selectedTile.getWorldPosition(WorldManager.SCALE, worldManager.getChunkSize()));
       rootNode.attachChild(selectedVoxelNode);
     }
   }
 
   private BoundingVolume makeCharacterBoundingVolume(Vector3f characterPos) {
     return new BoundingSphere(0.6f, characterPos.add(0, -0.3f, 0));
   }
 
   @Override
   public void destroy() {
     super.destroy();
     if (this.worldManager != null) {
       this.worldManager.stop();
     }
     if (closeEventHandler != null) {
       this.closeEventHandler.onClose();
     }
   }
 
   public void setCloseEventHandler(CloseEventHandler closeEventHandler) {
     this.closeEventHandler = closeEventHandler;
   }
 
   public void toggleFullScreen() {
     isFullScreen = !isFullScreen;
     setDisplayMode();
     restart();
   }
 
   public void setViewMode(ViewMode mode) {
     if(mode == ViewMode.FLYCAM) {
       flyCam.setEnabled(true);
       inputManager.setCursorVisible(false);
       rootNode.detachChild(character.getNode());
       guiNode.detachChild(crossHair);
       hideSelectedMaterial();
     } else if(mode == ViewMode.FIRST_PERSON) {
       flyCam.setEnabled(false);
       inputManager.setCursorVisible(false);
       rootNode.detachChild(character.getNode());
       guiNode.attachChild(crossHair);
       showSelectedMaterial();
     } else if(mode == ViewMode.THIRD_PERSON) {
       flyCam.setEnabled(false);
       inputManager.setCursorVisible(false);
       rootNode.attachChild(character.getNode());
       guiNode.detachChild(crossHair);
       hideSelectedMaterial();
     }
     viewMode = mode;
   }
   
   public WorldManager getWorldManager() {
     return worldManager;
   }
 
   public TilePosition getSelectedTile() {
     return selectedTile;
   }
 
   public TilePosition getSelectedBuildTile() {
     return selectedBuildTile;
   }
 
   public Tile getSelectedBuildMaterial() {
     return selectedBuildMaterial;
   }
 
   public void setSelectedBuildMaterial(Tile selectedBuildMaterial) {
     this.selectedBuildMaterial = selectedBuildMaterial;
     hideSelectedMaterial();
     if(viewMode == ViewMode.FIRST_PERSON) showSelectedMaterial();
   }
 
   public Character getCharacter() {
     return character;
   }
   
 }
