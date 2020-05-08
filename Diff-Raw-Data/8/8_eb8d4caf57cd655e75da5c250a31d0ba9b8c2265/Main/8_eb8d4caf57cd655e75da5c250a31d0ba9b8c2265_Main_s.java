 
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.audio.AudioNode;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.PointLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.CameraControl.ControlDirection;
 import com.jme3.util.SkyFactory;
 import objects.Planet;
 
 /**
  * @author Esteban Alarcon Ceballos y Enrique Arango Lyons
  */
 public class Main extends SimpleApplication {
     
     private Planet sun, mercury, venus, earth, mars, jupiter, saturn, uranus, neptune, pluto;
     private Planet[] planets;
     private Spatial spaceship;
     private Node spaceshipNode;
     private FilterPostProcessor fpp;
     private BloomFilter bloom;
     private CameraNode camNode;
     private Node turbines;
     private int bloomDirection;
     private AudioNode bgAudio;
     private AudioNode spaceshipAudio;
 
     public static void main(String[] args) {
         Main app = new Main();
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         
         planets = new Planet[9];
         
         Material mat10 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");        
         mat10.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Sun.jpg"));
         mat10.setTexture("GlowMap", assetManager.loadTexture("Textures/Sun.jpg"));
         mat10.setColor("Specular", ColorRGBA.White);
         mat10.setBoolean("UseAlpha", true);
         sun = new Planet("Sun", 5f, null, mat10, null, 0, 0);
         rootNode.attachChild(sun.getGeom());
         
         Material mat1 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat1.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Mercury.jpg"));
         mat1.setColor("Specular", ColorRGBA.White);
         Node p1 = new Node();
         rootNode.attachChild(p1);
         mercury = new Planet("Mercury", 2f, new Vector3f(16.0f, 0f, -6.0f), mat1, p1, (float) Math.random(), 0.76f);
         planets[0] = mercury;
         
         Material mat2 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat2.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Venus.jpg"));
         mat2.setColor("Specular", ColorRGBA.White);
         Node p2 = new Node();
         rootNode.attachChild(p2);
         venus = new Planet("Venus", 2.6f, new Vector3f(20.0f, 0f, -6.0f), mat2, p2, (float) Math.random(), 0.65f);
         planets[1] = venus;
         
         Material mat3 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat3.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Earth/Color.jpg"));
         mat3.setTexture("ParallaxMap", assetManager.loadTexture("Textures/Earth/Bump.jpg"));
         mat3.setTexture("SpecularMap", assetManager.loadTexture("Textures/Earth/Specular.jpg"));        
         //mat3.setTexture("GlowMap", assetManager.loadTexture("Textures/Earth/Lights3.jpeg"));
         Node p3 = new Node();
         rootNode.attachChild(p3);
         earth = new Planet("Earth", 2.7f, new Vector3f(28.0f, 0f, -6.0f), mat3, p3, (float) Math.random(), 0.6f);
         planets[2] = earth;
         
         Material mat4 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat4.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Mars.jpg"));
         mat4.setColor("Specular", ColorRGBA.White);
         Node p4 = new Node();
         rootNode.attachChild(p4);
         mars = new Planet("Mars", 2.5f, new Vector3f(35.0f, 0f, -6.0f), mat4, p4, (float) Math.random(), 0.56f);
         planets[3] = mars;
         
         Material mat5 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat5.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Jupiter.jpg"));
         mat5.setColor("Specular", ColorRGBA.White);        
         Node p5 = new Node();
         rootNode.attachChild(p5);
         jupiter = new Planet("Jupiter", 3.1f, new Vector3f(49.0f, 0f, -6.0f), mat5, p5, (float) Math.random(), 0.5f);
         planets[4] = jupiter;
         
         Material mat6 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat6.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Saturn.jpg"));
         mat6.setColor("Specular", ColorRGBA.White);
         Node p6 = new Node();
         rootNode.attachChild(p6);
         saturn = new Planet("Saturn", 2.9f, new Vector3f(57.0f, 0f, -6.0f), mat6, p6, (float) Math.random(), 0.44f);
         planets[5] = saturn;
         
         Material mat7 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat7.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Uranus.jpg"));
         mat7.setColor("Specular", ColorRGBA.White);
         Node p7 = new Node();
         rootNode.attachChild(p7);
         uranus = new Planet("Uranus", 2.8f, new Vector3f(65.0f, 0f, -6.0f), mat7, p7, (float) Math.random(), 0.4f);
         planets[6] = uranus;
         
         Material mat8 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat8.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Neptune.jpg"));
         mat8.setColor("Specular", ColorRGBA.White);
         Node p8 = new Node();
         rootNode.attachChild(p8);
         neptune = new Planet("Neptune", 2.65f, new Vector3f(75.0f, 0f, -6.0f), mat8, p8, (float) Math.random(), 0.34f);
         planets[7] = neptune;
         
         Material mat9 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat9.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Pluto.jpg"));
         mat9.setColor("Specular", ColorRGBA.White);
         Node p9 = new Node();
         rootNode.attachChild(p9);
         pluto = new Planet("Pluto", 1.5f, new Vector3f(82.0f, 0f, -6.0f), mat9, p9, (float) Math.random(), 0.2f);
         planets[8] = pluto;
         
         spaceship = assetManager.loadModel("Models/X-WING/X-WING.j3o");
         spaceshipNode = new Node("SpaceshipNode");
         spaceship.scale(0.1f);
         spaceship.rotate(0, FastMath.PI, 0);
         spaceshipNode.setLocalTranslation(0, 10f, 30f);
         spaceshipNode.attachChild(spaceship);
         rootNode.attachChild(spaceshipNode);
         
         turbines = new Node("Turbines");
         turbines.setLocalTranslation(0, 0, 0.4f);
         
         ParticleEmitter fire1 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
         Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
         fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
         fire1.setMaterial(fireMat);
         fire1.setImagesX(2);
         fire1.setImagesY(2);
         fire1.setEndColor(new ColorRGBA(1f, 1f, 0f, 1f));   // red
         fire1.setStartColor(new ColorRGBA(0f, 0f, 1f, 0.5f)); // yellow
         fire1.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 2));
         fire1.setStartSize(0.1f);
         fire1.setEndSize(0.05f);
         fire1.setGravity(0, 0, 0);
         fire1.setLowLife(0.1f);
         fire1.setHighLife(0.2f);
         fire1.getParticleInfluencer().setVelocityVariation(0.2f);
         fire1.setLocalTranslation(0.1f, 0, 0);
         turbines.attachChild(fire1);
         
         ParticleEmitter fire2 = fire1.clone();
         fire2.setLocalTranslation(-0.1f, 0, 0);
         turbines.attachChild(fire2);
         
         spaceshipNode.attachChild(turbines);
         
         flyCam.setEnabled(false);
         
         camNode = new CameraNode("CameraNode", cam);
         camNode.setControlDir(ControlDirection.SpatialToCamera);
         spaceshipNode.attachChild(camNode);
         camNode.setLocalTranslation(new Vector3f(0, 0.5f, 2));
         camNode.lookAt(spaceshipNode.getLocalTranslation(), Vector3f.UNIT_Y);
         
         PointLight sunLight = new PointLight();
         sunLight.setColor(ColorRGBA.White);
         sunLight.setPosition(new Vector3f(0f, 0f, 0f));
         sunLight.setRadius(100f);
         rootNode.addLight(sunLight);
         
         fpp = new FilterPostProcessor(assetManager);
         bloom = new BloomFilter(BloomFilter.GlowMode.Objects); 
         bloom.setExposurePower(30f);
         bloom.setBloomIntensity(1f);
         fpp.addFilter(bloom);
         viewPort.addProcessor(fpp);
         
         rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Stars5.jpeg", true));
         
         bloomDirection = 1;
         
         initKeys();
         initAudio();
     }
     
     private void initAudio() {
        spaceshipAudio = new AudioNode(assetManager, "Sound/Fire3.wav", false);
         spaceshipAudio.setLooping(true);
         spaceshipAudio.setVolume(2);
         spaceshipNode.attachChild(spaceshipAudio);
         
         bgAudio = new AudioNode(assetManager, "Sound/Background.wav", false);
         bgAudio.setLooping(true);
         bgAudio.setVolume(3);
         rootNode.attachChild(bgAudio);
         bgAudio.play();
     }
     
     private void initKeys() {
         inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("Right",   new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping("Up",  new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping("LeftSide",  new KeyTrigger(KeyInput.KEY_Q));
         inputManager.addMapping("RightSide",  new KeyTrigger(KeyInput.KEY_E));
         inputManager.addMapping("Accelerate",   new KeyTrigger(KeyInput.KEY_SPACE));
     
         inputManager.addListener(analogListener, new String[]{"Left", "Right", "Up", "Down", "LeftSide", "RightSide", "Accelerate"});
         inputManager.addListener(actionListener, new String[]{"Accelerate"});
     }
     
     private ActionListener actionListener = new ActionListener() {
         public void onAction(String name, boolean isPressed, float tpf) {
             if (name.equals("Accelerate")) {
                 if (isPressed) {
                     for (Spatial child : turbines.getChildren()) {
                         ParticleEmitter fire = (ParticleEmitter) child;
                         fire.setStartSize(0.2f);
                         fire.setEndSize(0.1f);
                     }
                     spaceshipAudio.play();
                 } else {
                     for (Spatial child : turbines.getChildren()) {
                         ParticleEmitter fire = (ParticleEmitter) child;
                         fire.setStartSize(0.1f);
                         fire.setEndSize(0.05f);
                     }
                     spaceshipAudio.stop();
                 }
             }
         }
         
     };
     
     private AnalogListener analogListener = new AnalogListener() {
 
         public void onAnalog(String name, float value, float tpf) {
             if (name.equals("Left")) {
                 spaceshipNode.rotate(0, tpf, 0);
             }
             if (name.equals("Right")) {
                 spaceshipNode.rotate(0, -tpf, 0);
             }
             if (name.equals("Up")) {
                 spaceshipNode.rotate(tpf, 0, 0);
             }
             if (name.equals("Down")) {
                 spaceshipNode.rotate(-tpf, 0, 0);
             }
             if (name.equals("LeftSide")) {
                spaceshipNode.rotate(0, 0, tpf);
             }
             if (name.equals("RightSide")) {
                spaceshipNode.rotate(0, 0, -tpf);
             }
             if (name.equals("Accelerate")) {
                 Vector3f movement = new Vector3f(0, 0, 0);
                 spaceshipNode.getLocalRotation().mult(new Vector3f(0, 0, -8 * tpf), movement);
                 spaceshipNode.move(movement);
             }
         }
         
     };
 
     @Override
     public void simpleUpdate(float tpf) {
         for(Planet planet : planets){
             planet.getGeom().rotate(0, 0, planet.getRotationVel()*tpf);
             planet.getPivot().rotate(0, planet.getTranslationVel()*tpf, 0);
             
             bloom.setBloomIntensity(bloom.getBloomIntensity() + (bloomDirection * tpf / 8));
             if (bloom.getBloomIntensity() > 4) bloomDirection = -1;
             if (bloom.getBloomIntensity() < 2) bloomDirection = 1;
         }
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 }
