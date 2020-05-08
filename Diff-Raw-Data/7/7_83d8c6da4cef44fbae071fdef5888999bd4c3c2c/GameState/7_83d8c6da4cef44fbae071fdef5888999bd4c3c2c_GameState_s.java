 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 import animations.LevitationControl;
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bounding.BoundingVolume;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.collision.CollisionResults;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.input.FlyByCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.scene.shape.Box;
 import com.jme3.system.AppSettings;
 
 public class GameState extends AbstractAppState implements ActionListener {
 
     // protected Node guiNode = new Node("Gui Node");
     protected BitmapText fpsText;
     protected BitmapText menuText;
     protected BitmapFont guiFont;
     protected FlyByCamera flyCam;
     private MainApp game = null;
     private AppActionListener actionListener = new AppActionListener();
     // private SimpleApplication app;
     private Node rootNode = new Node();
     private AssetManager assetManager;
     private AppStateManager stateManager;
     private AppSettings settings;
     private InputManager inputManager;
     private Camera cam;
     private ViewPort viewPort;
     private BulletAppState physics;
     private final int NUMBER_ENEMIES = 20;
     private final int NUMBER_CHARGES = 5;
     private final int CLEANER_CAPACITY = 5;
     private Spatial sceneModel;
     private Geometry cleanerShape;
     private Enemy[] pow = new Enemy[NUMBER_ENEMIES];
     private Bullet[] fire = new Bullet[NUMBER_CHARGES];
     private Material[] enemyMaterial = new Material[5];
     private BulletAppState bulletAppState;
     private RigidBodyControl landscape;
     private CharacterControl player;
     private Ray cleanerRay;
     private BoundingVolume boundEnemy;
     private Node shootables;
     private Node inhalables;
     private Node aspiredEnemies;
     private Node guiNode = new Node();
     private Vector3f walkDirection = new Vector3f();
     private int enemiesCleaned;
     private float spawnTimer;
     private boolean left = false, right = false, up = false, down = false, aspire = false;
 
     public GameState(MainApp game) {
         this.game = game;
     }
    
 
     private class AppActionListener implements ActionListener {
 
         public void onAction(String name, boolean value, float tpf) {
             if (!value) {
                 return;
             }
 
             if (name.equals("SIMPLEAPP_Exit")) {
                 game.stop();
             } else if (name.equals("SIMPLEAPP_Memory")) {
                 game.loadMenu();
             }
         }
     }
 
     public void loadText() {
         guiFont = game.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
         fpsText = new BitmapText(guiFont, false);
         fpsText.setSize(guiFont.getCharSet().getRenderedSize());
         fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
         fpsText.setText("Frames per second");
         // guiNode.attachChild(fpsText);
         menuText = new BitmapText(guiFont, false);
         menuText.setSize(guiFont.getCharSet().getRenderedSize());
         menuText.setLocalTranslation(0, (game.getContext().getSettings().getHeight() / 2f) - (menuText.getLineHeight() / 2f), 0);
         menuText.setText("Press [M] to go back to the Menu");
         // guiNode.attachChild(menuText);
 
     }
 
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
 
 
         this.game = (MainApp) game; // can cast Application to something more specific
         // this.rootNode = this.rootNode;
         this.assetManager = this.game.getAssetManager();
         this.stateManager = this.game.getStateManager();
         this.inputManager = this.game.getInputManager();
         this.viewPort = this.game.getViewPort();
         this.cam = this.game.getCamera();
         this.settings = this.game.getSettings();
         this.physics = this.stateManager.getState(BulletAppState.class);
         // settings.setResolution(1024,720);
 
         // enable depth test and back-face culling for performance
         game.getRenderer().applyRenderState(RenderState.DEFAULT);
         //guiNode = ((SimpleApplication) app).getGuiNode();
 
         loadText();
 
         if (game.getInputManager() != null) {
             flyCam = new FlyByCamera(game.getCamera());
             flyCam.setMoveSpeed(1f);
             flyCam.registerWithInput(game.getInputManager());
 
             game.getInputManager().addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
             //game.getInputManager().addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
         }
 
         // app.setDisplayFps(false);
         //setDisplayStatView(false);
 
         enemiesCleaned = 0;
         spawnTimer = 0;
 
         Box b = new Box(1.3f, 1.3f, 1.3f);
         cleanerShape = new Geometry("Cleaner Shape", b);
 
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
 
         cleanerRay = new Ray(cam.getLocation(), cam.getDirection());
 
         shootables = new Node("Shootables");
         rootNode.attachChild(shootables);
         
         aspiredEnemies = new Node("Aspired enemies");
         rootNode.attachChild(aspiredEnemies);
 
         inhalables = new Node("Inhalables");
         rootNode.attachChild(inhalables);
 
         setUpKeys();
         setUpLight();
 
         for (int i = 0; i < fire.length; i++) {
             fire[i] = createFire();
         }
 
         sceneModel = assetManager.loadModel("Scenes/Escenario/Escenario.j3o");
         sceneModel.setName("Scene-entity");
         sceneModel.setLocalScale(6f);
 
         CollisionShape sceneShape =
                 CollisionShapeFactory.createMeshShape((Node) sceneModel);
         landscape = new RigidBodyControl(sceneShape, 0);
         sceneModel.addControl(landscape);
 
         CollisionResults rEnemigoEscenario;
 
         for (int i = 0; i < pow.length; i++) {
             pow[i] = new Enemy(assetManager.loadModel("Models/Pow/Pow.j3o")) {
             };
             pow[i].getSpatial().setLocalScale(0.8f, 0.8f, 0.8f);
             pow[i].getSpatial().setName(i + "-entity");
             do {
                 pow[i].getSpatial().setLocalTranslation((float) Math.random() * 56 - 28, (float) Math.random() * 5 + 8, (float) Math.random() * 56 - 28);
                 rEnemigoEscenario = new CollisionResults();
                 sceneModel.collideWith(pow[i].getSpatial().getWorldBound(), rEnemigoEscenario);
                 rEnemigoEscenario.toString();
             } while (rEnemigoEscenario.size() > 0);
             shootables.attachChild(pow[i].getSpatial());
         }
 
         boundEnemy = pow[0].getSpatial().getWorldBound();
 
         Enemy powHurt = new Enemy(assetManager.loadModel("Models/Pow/PowHurt.j3o"));
         Enemy powEyesClosed = new Enemy(assetManager.loadModel("Models/Pow/PowEyesClosed.j3o"));
         Enemy powWeak = new Enemy(assetManager.loadModel("Models/Pow/PowWeak.j3o"));
         Enemy powDeath = new Enemy(assetManager.loadModel("Models/Pow/PowDeath.j3o"));
 
         enemyMaterial[0] = pow[0].getMaterial();
         enemyMaterial[1] = powHurt.getMaterial();
         enemyMaterial[2] = powEyesClosed.getMaterial();
         enemyMaterial[3] = powWeak.getMaterial();
         enemyMaterial[4] = powDeath.getMaterial();
 
         CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1, 6f, 1);
         player = new CharacterControl(capsuleShape, 0.05f);
         player.setGravity(30);
         player.setPhysicsLocation(new Vector3f(0, 10, 0));
 
 
         rootNode.attachChild(sceneModel);
 
         bulletAppState.getPhysicsSpace().add(landscape);
         bulletAppState.getPhysicsSpace().add(player);
 
     }
 
     @Override
     public void update(float tpf) {
         super.update(tpf);
 
         int fps = (int) game.getTimer().getFrameRate();
         fpsText.setText("Frames per second: " + fps);
 
         initCrossHairs();
         guiNode.setQueueBucket(Bucket.Gui);
         guiNode.setCullHint(CullHint.Never);
 
         // guiNode.center();
 
 
         spawnTimer += tpf;
 
         if (spawnTimer > 15) {
             spawnTimer = 0;
             spawn();
         }
 
         cleanerRay.setOrigin(cam.getLocation());
         cleanerRay.setDirection(cam.getDirection());
 
         Vector3f camLeft = cam.getLeft().clone().multLocal(0.33f);
         Vector3f camForward = cam.getDirection().clone().multLocal(0.5f);
         camForward.y = 0;
         walkDirection.set(0, 0, 0);
 
         if (left && !up && !down) {
             walkDirection.addLocal(camLeft);
         }
         if (right && !up && !down) {
             walkDirection.addLocal(camLeft.negate());
         }
         if (up && !right && !left) {
             walkDirection.addLocal(camForward);
         }
         if (down && !right && !left) {
             walkDirection.addLocal(camForward.negate());
         }
         if (left && up){
             walkDirection.addLocal(camLeft.add(camForward).divide(1.5f));
         }
         if (left && down){
             walkDirection.addLocal(camLeft.add(camForward.negate()).divide(1.5f));
         }
         if (right && up){
             walkDirection.addLocal(camLeft.negate().add(camForward).divide(1.5f));
         }
         if (right && down){
             walkDirection.addLocal(camLeft.negate().add(camForward.negate()).divide(1.5f));
         }
         if (aspire) {
             aspire();
         } else {
             notAspire();
         }
         player.setWalkDirection(walkDirection);
         cam.setLocation(player.getPhysicsLocation());
         cleanerShape.setLocalTranslation(player.getPhysicsLocation());
 
         if (cam.getDirection().y > 0.9439419f) {
             cam.setFrame(new Vector3f(cam.getLocation().x, 4.1509075f, cam.getLocation().z), new Vector3f(cam.getLeft().x, 7.916242E-9f, cam.getLeft().z), new Vector3f(cam.getUp().x, 0.33011156f, cam.getUp().z), new Vector3f(cam.getDirection().x, 0.9439419f, cam.getDirection().z));
         } else if (cam.getDirection().y < -0.93508357) {
             cam.setFrame(new Vector3f(cam.getLocation().x, 4.1510572f, cam.getLocation().z), new Vector3f(cam.getLeft().x, -2.2441149E-5f, cam.getLeft().z), new Vector3f(cam.getUp().x, 0.3544274f, cam.getUp().z), new Vector3f(cam.getDirection().x, -0.93508357f, cam.getDirection().z));
         }
         
         System.out.println(cam.getLocation().y);
         System.out.println(cam.getLeft().y);
         System.out.println(cam.getUp().y);
         System.out.println(cam.getDirection().y);
         
         for (int i = 0; i < pow.length; i++) {
             if (!pow[i].isActive()) {
                 pow[i].getSpatial().scale(1 + tpf * 3);
                 if (pow[i].getSpatial().getWorldBound().getVolume() >= boundEnemy.getVolume()) {
                     pow[i].setActive(true);
                     shootables.attachChild(pow[i].getSpatial());
                 }
             }
             Vector3f dir = pow[i].getSpatial().getLocalTranslation().subtract(player.getPhysicsLocation()).normalize();
             if (!pow[i].isDeath()) {
                 pow[i].getSpatial().lookAt(player.getPhysicsLocation().clone(), Vector3f.UNIT_Y);
 
                 if (pow[i].getSpatial().getControl(LevitationControl.class).getSpeed() > 2) {
                     pow[i].getSpatial().setMaterial(enemyMaterial[1]);
                 } else if (pow[i].isActive()) {
                     pow[i].setTimer(pow[i].getTimer() + tpf);
                     if (pow[i].getTimer() > 7) {
                         pow[i].getSpatial().setMaterial(enemyMaterial[2]);
                         if (pow[i].getTimer() > 7.1) {
                             pow[i].setTimer(1);
                         }
                     } else {
                         if (pow[i].getHealth() > pow[i].getOriginalHealth() / 2) {
                             pow[i].getSpatial().setMaterial(enemyMaterial[0]);
                         } else {
                             pow[i].getSpatial().setMaterial(enemyMaterial[3]);
                         }
                     }
                 }
             } else {
                 CollisionResults rEnemyCleaner = new CollisionResults();
                 inhalables.collideWith(cleanerShape.getWorldBound(), rEnemyCleaner);
                 rEnemyCleaner.toString();
                 if (rEnemyCleaner.size() > 0) {
                     String[] words = getGeometrySpatial(rEnemyCleaner.getClosestCollision().getGeometry()).getName().split("-");
                     if (pow[Integer.parseInt(words[0])].isHasBeenAspired() && enemiesCleaned<CLEANER_CAPACITY) {
                         pow[Integer.parseInt(words[0])].getfDeathEnemy().setLinearVelocity(Vector3f.ZERO);
                         pow[Integer.parseInt(words[0])].getSpatial().removeControl(pow[Integer.parseInt(words[0])].getfDeathEnemy());
                         pow[Integer.parseInt(words[0])].getSpatial().move(0, 30, 0);
                         bulletAppState.getPhysicsSpace().remove(pow[Integer.parseInt(words[0])].getfDeathEnemy());
                         aspiredEnemies.attachChild(pow[Integer.parseInt(words[0])].getSpatial());
                         enemiesCleaned++;
                     }
                 }
                 if (pow[i].isAspired()) {
                     pow[i].getfDeathEnemy().setGravity(Vector3f.ZERO);
                 } else {
                     pow[i].getfDeathEnemy().setGravity(new Vector3f(0, -9.81f, 0));
                 }
             }
         }
 
         for (int i = 0; i < fire.length; i++) {
             if (fire[i].isShooted()) {
                 fire[i].move(tpf);
                 Box ball = new Box(1f, 1f, 1f);
                 Geometry theBall = new Geometry("Ball", ball);
                 theBall.move(fire[i].getWorldTranslation().x, fire[i].getWorldTranslation().y, fire[i].getWorldTranslation().z);
                 CollisionResults rScene = new CollisionResults();
                 sceneModel.collideWith(theBall.getWorldBound(), rScene);
                 rScene.toString();
                 CollisionResults rEnemy = new CollisionResults();
                 shootables.collideWith(theBall.getWorldBound(), rEnemy);
                 rEnemy.toString();
                 if (rScene.size() > 0) {
                     fire[i].setParticlesPerSec(0f);
                     fire[i].setShooted(false);
                 }
                 if (rEnemy.size() > 0) {
                     fire[i].setParticlesPerSec(0f);
                     fire[i].setShooted(false);
                     String[] words = getGeometrySpatial(rEnemy.getClosestCollision().getGeometry()).getName().split("-");
                     pow[Integer.parseInt(words[0])].setHealth(pow[Integer.parseInt(words[0])].getHealth() - 1);
                     getGeometrySpatial(rEnemy.getClosestCollision().getGeometry()).getControl(LevitationControl.class).setSpeed(100);
                     getGeometrySpatial(rEnemy.getClosestCollision().getGeometry()).getControl(LevitationControl.class).setTopUp(4);
                     if (pow[Integer.parseInt(words[0])].getHealth() == 0) {
                         getGeometrySpatial(rEnemy.getClosestCollision().getGeometry()).removeFromParent();
                         death(getGeometrySpatial(rEnemy.getClosestCollision().getGeometry()), Integer.parseInt(words[0]));
                         pow[Integer.parseInt(words[0])].setDeath(true);
                     }
                 }
             }
         }
 
         //simple update and root node
 
         rootNode.updateLogicalState(tpf);
 
         guiNode.updateLogicalState(tpf);
 
 
         rootNode.updateGeometricState();
 
         guiNode.updateGeometricState();
     }
 
     public void stateAttached(AppStateManager stateManager) {
         game.getInputManager().addListener(actionListener, "SIMPLEAPP_Exit");
 
         if (flyCam != null) {
             flyCam.setEnabled(true);
         }
 
         game.getViewPort().attachScene(rootNode);
 
         game.getGUIViewPort().attachScene(guiNode);
     }
 
     public void stateDetached(AppStateManager stateManager) {
         game.getInputManager().removeListener(actionListener);
         if (flyCam != null) {
             flyCam.setEnabled(false);
         }
         // game.getViewPort().detachScene(shootables);
         // game.getViewPort().detachScene(inhalables);
         game.getViewPort().detachScene(rootNode);
 
         game.getGUIViewPort().detachScene(guiNode);
     }
 
     private void setUpKeys() {
 
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Aspire", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("Throw", new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addListener(this, "Left");
         inputManager.addListener(this, "Right");
         inputManager.addListener(this, "Up");
         inputManager.addListener(this, "Down");
         inputManager.addListener(this, "Shoot");
         inputManager.addListener(this, "Aspire");
         inputManager.addListener(this, "Throw");
     }
     
     private void throwEnemies() {
         for (int i=0; i<pow.length; i++){
             if (pow[i].getSpatial().getParent()==aspiredEnemies){
                 pow[i].getSpatial().removeFromParent();
                 pow[i].setHasBeenAspired(false);
                 inhalables.attachChild(pow[i].getSpatial());
                 pow[i].getSpatial().setLocalTranslation(cam.getLocation().add(cam.getDirection()));
                 pow[i].getSpatial().addControl(pow[i].getfDeathEnemy());
                 bulletAppState.getPhysicsSpace().add(pow[i].getfDeathEnemy());
                 pow[i].getfDeathEnemy().setLinearVelocity(cam.getDirection().mult(30));
                 enemiesCleaned-=1;
                 i=pow.length;
             }
         }
     }
 
     private Bullet createFire() {
 
         Bullet f =
                 new Bullet("Emitter", ParticleMesh.Type.Triangle, 30);
         Material mat_red = new Material(assetManager,
                 "Common/MatDefs/Misc/Particle.j3md");
         mat_red.setTexture("Texture", assetManager.loadTexture(
                 "Effects/Explosion/flame.png"));
         f.setMaterial(mat_red);
         f.setImagesX(2);
         f.setImagesY(2);
         f.setEndColor(new ColorRGBA(1f, 1f, 0f, 1f));
         f.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
         f.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
         f.setStartSize(2f);
         f.setEndSize(0.1f);
         f.setGravity(0, 0, 0);
         f.setLowLife(0.2f);
         f.setHighLife(0.5f);
         f.getParticleInfluencer().setVelocityVariation(0.3f);
         return f;
     }
 
     private void setUpLight() {
 
         DirectionalLight l1 = new DirectionalLight();
         l1.setColor(ColorRGBA.White);
         l1.setDirection(new Vector3f(-5f, 0, 0).normalizeLocal());
         rootNode.addLight(l1);
 
         DirectionalLight l2 = new DirectionalLight();
         l2.setColor(ColorRGBA.White);
         l2.setDirection(new Vector3f(5f, 0, 0).normalizeLocal());
         rootNode.addLight(l2);
 
         DirectionalLight l3 = new DirectionalLight();
         l3.setColor(ColorRGBA.White);
         l3.setDirection(new Vector3f(0, 0, -5f).normalizeLocal());
         rootNode.addLight(l3);
 
         DirectionalLight l4 = new DirectionalLight();
         l4.setColor(ColorRGBA.White);
         l4.setDirection(new Vector3f(0, 0, 5f).normalizeLocal());
         rootNode.addLight(l4);
 
         DirectionalLight l5 = new DirectionalLight();
         l5.setColor(ColorRGBA.White);
         l5.setDirection(new Vector3f(0, -5f, 0).normalizeLocal());
         rootNode.addLight(l5);
 
         DirectionalLight l6 = new DirectionalLight();
         l6.setColor(ColorRGBA.White);
         l6.setDirection(new Vector3f(0, 5f, 0).normalizeLocal());
         rootNode.addLight(l6);
 
         /*PointLight p1 = new PointLight();
          p1.setColor(ColorRGBA.White);
          p1.setPosition(new Vector3f(1,1,1));
          rootNode.addLight(p1);*/
 
     }
 
     protected void initCrossHairs() {
 
         guiNode.detachAllChildren();
 
         guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
         BitmapText ch = new BitmapText(guiFont, false);
         ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
         ch.setText("+");
 
 
         ch.setLocalTranslation(
                 settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                 settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
         guiNode.attachChild(ch);
         BitmapText ch2 = new BitmapText(guiFont, false);
 
         ch2.setColor(ColorRGBA.White);
         ch2.setSize(40);
         ch2.setText("Enemigos aspirados: " + enemiesCleaned+"/"+CLEANER_CAPACITY);
         ch2.setLocalTranslation(
                 settings.getWidth() / 25,
                 settings.getHeight() / 10, 0);
         guiNode.attachChild(ch2);
 
         // rootNode.attachChild(guiNode);
     }
 
     public void shootFire() {
 
         for (int i = 0; i < fire.length; i++) {
             if (!fire[i].isShooted()) {
                 rootNode.attachChild(fire[i]);
                 fire[i].setShooted(true);
                 fire[i].setParticlesPerSec(20);
                 fire[i].setDirection(cam.getDirection());
                 fire[i].setLocalTranslation(cam.getLocation());
                 i = fire.length;
             }
         }
     }
 
     public Spatial getGeometrySpatial(Geometry geometry) {
 
         Spatial s = geometry.getParent();
         while (s != null) {
             if (s.getName().endsWith("-entity")) {
                 return s;
             }
             s = s.getParent();
         }
         return null;
     }
 
     private void death(Spatial s, int index) {
         s.setMaterial(enemyMaterial[4]);
         s.addControl(pow[index].getfDeathEnemy());
         inhalables.attachChild(s);
         bulletAppState.getPhysicsSpace().add(pow[index].getfDeathEnemy());
     }
 
     private void aspire() {
         CollisionResults rCleanerEnemy = new CollisionResults();
         inhalables.collideWith(cleanerRay, rCleanerEnemy);
         rCleanerEnemy.toString();
         if (rCleanerEnemy.size() > 0) {
             String[] words = getGeometrySpatial(rCleanerEnemy.getClosestCollision().getGeometry()).getName().split("-");
             pow[Integer.parseInt(words[0])].setAspired(true);
             pow[Integer.parseInt(words[0])].setHasBeenAspired(true);
             pow[Integer.parseInt(words[0])].getfDeathEnemy().setLinearVelocity(cam.getDirection().mult(25f).negate());
         } else {
             for (int i = 0; i < pow.length; i++) {
                 pow[i].setAspired(false);
             }
         }
     }
 
     public void notAspire() {
         for (int i = 0; i < pow.length; i++) {
             pow[i].setAspired(false);
         }
     }
 
     public void spawn() {
         boolean found = false;
         int c = -1;
         for (int i = 0; i < pow.length; i++) {
             if (pow[i].getSpatial().getParent() == null) {
                 c = i;
             }
         }
         if (c != -1) {
             do {
                 //pow[c].getSpatial().setLocalTranslation((float) Math.random() * 56 - 28, (float) Math.random() * 5 + 8, (float) Math.random() * 56 - 28);
                 pow[c].getSpatial().setLocalTranslation(1,10,1);
                 /*Ray r = new Ray(pow[c].getSpatial().getWorldTranslation(), player.getPhysicsLocation());
                  CollisionResults rEnemyPlayer = new CollisionResults();
                  r.collideWith(sceneModel.getWorldBound(), rEnemyPlayer);
                  rEnemyPlayer.toString();*/
                 CollisionResults rEnemigoEscenario = new CollisionResults();
                 sceneModel.collideWith(pow[c].getSpatial().getWorldBound(), rEnemigoEscenario);
                 rEnemigoEscenario.toString();
                 if (rEnemigoEscenario.size() <= 0) //if (getGeometrySpatial(rEnemyPlayer.getClosestCollision().getGeometry()).getName()=="Scene-entity" && rEnemigoEscenario.size()<=0){
                 {
                     found = true;
                 }
                 pow[c].getSpatial().setMaterial(enemyMaterial[1]);
                 pow[c].getSpatial().removeControl(pow[c].getfDeathEnemy());
                 pow[c].setHealth(2);
                 pow[c].setDeath(false);
                 pow[c].setHasBeenAspired(false);
                 pow[c].setActive(false);
                 pow[c].getSpatial().scale(0.1f);
                 rootNode.attachChild(pow[c].getSpatial());
                 //}
             } while (!found);
         }
     }
 
     public void onAction(String name, boolean isPressed, float tpf) {
 
         if (name.equals("Shoot") && !isPressed) {
             shootFire();
         }
         if (name.equals("Throw") && !isPressed) {
             throwEnemies();
         }
         if (name.equals("Aspire")) {
             if (isPressed) {
                 aspire = true;
             } else {
                 aspire = false;
             }
         }
         if (name.equals("Left")) {
             if (isPressed) {
                 left = true;
             } else {
                 left = false;
             }
         } else if (name.equals("Right")) {
             if (isPressed) {
                 right = true;
             } else {
                 right = false;
             }
         }
         if (name.equals("Up")) {
             if (isPressed) {
                 up = true;
             } else {
                 up = false;
             }
         } else if (name.equals("Down")) {
             if (isPressed) {
                 down = true;
             } else {
                 down = false;
             }
         }
     }
 
     public void render(RenderManager rm) {
     }
 }
