 package screens;
 
 import display.Camera;
 import display.HUD;
 import environment.Background;
 import environment.Light;
 import environment.Model;
 import helpers.Delegate;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import java.io.IOException;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hp
  * Date: 23/04/13
  * Time: 3:10 PM
  * To change this template use File | Settings | File Templates.
  */
 /*public class GameplayScreen extends Screen {
 
     float lightDirection[]= { -2f, 2f, 2f, 0f };//direction , position
     float diffuse[] = { 1f,  1f,  1f,  1f };  // diffuse color
     float ambient[] = { .6f, .6f, .9f, 1f };    // ambient
     float specular[]= { 1f,  1f,  1f,  1f };
     Light l = new Light(lightDirection,diffuse,ambient,specular);
     Model model;//=new Model("data/DarkFighter/dark_fighter.obj",1.0f,0.0f,0.0f,0.0f,-10.0f,10.0f,0.0f);
     public GameplayScreen(Delegate d) {
         super(d);
 
     }
 
     public void Initialize() {
         model = new Model("data/DarkFighter/dark_fighter.obj",5.0f,0.0f,0.0f,0.0f,-6.0f,60.0f,16.0f)   ;
         //Terrain.setUpDisplay();
         l.setLight();
         Terrain.setUpStates();
         Terrain.setUpHeightmap();
         Terrain.setUpShaders();
         Terrain.setUpMatrices();
     }
 
     public void Render() {
         Terrain.render();
         //glClear( GL_DEPTH_BUFFER_BIT);
         //glColor3f(0.0f, 1.0f, 1.0f);
         //glBegin(GL_QUADS);
         glEnable( GL_TEXTURE_2D );
         model.render();
         // glEnd();
     }
 
     public void Update() {
         Terrain.input();
     }
 }*/
 
 public class GameplayScreen extends Screen {
     Camera cam;
     Model model,model2,terrain;
     HUD hud;
     Background background;
     Light l;
 
     public GameplayScreen(Delegate d) {
         super(d);
         cam = new Camera(new Vector3f(0, 0, -10));
     }
 
     public void Initialize() {
         // Enable Texture 2D for texturing models
         glEnable(GL_TEXTURE_2D);
         // Enable depth testing for correct drawing order
         glEnable(GL_DEPTH_TEST);
 
         // Create a light
         float lightDirection[] = {-2f, 2f, 2f, 0f}; //direction/position
         float diffuse[] = {1f, 1f, 1f, 1f};
         float ambient[] = {.6f, .6f, .9f, 1f};
         float specular[] = {1f, 1f, 1f, 1f};
         l = new Light(lightDirection, diffuse, ambient, specular);
         l.setLight();
 
         //Start Gamescreen music
         killAudio();
         try {
             audio = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("music/36-VersusMode.wav"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         audio.playAsMusic(1.0f, 1.0f, true);
 
         //Create Background
         background = new Background();
         //load the model
         model = new Model("data/Arwing/arwing.obj", 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -5.0f);
         model2 = new Model("data/DarkFighter/dark_fighter.obj", 0.5f, 0.0f, 0.0f, 0.0f, -10.0f, -10.0f, 0.0f);
 
         terrain = new Model("data/terrain/WS free terrain 014.obj", 20.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

         terrain = new Model("data/terrain/WS free terrain 014.obj", 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

         //TODO: implement huds for individual players
         hud = new HUD();
     }
 
     public void Render() {
         // Clear colour and depth buffers
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
         glMatrixMode(GL_PROJECTION);
 
         // Push the 2D projection to stack
         glPushMatrix();
         {
             //region 3D stuff
             // Specify the size of the viewport and create a 3D projection matrix
             glViewport(0, 0, Display.getWidth(), Display.getHeight());
             glLoadIdentity();
             GLU.gluPerspective(45.0f, (float) Display.getWidth() / (float) Display.getHeight(), .1f, 5000f);
             glMatrixMode(GL_MODELVIEW);
 
             // Rotate camera
             cam.setCameraView(0.2f, hud);
 
             // Push the Texture bit for the model
             glPushAttrib(GL_TEXTURE_BIT);
             {
                 background.drawSkybox(50.0f);
                 glPushMatrix();
                 glLoadIdentity();
                 model.render();
                 glPopMatrix();
                 cam.moveCamera();
             }
             glPopAttrib();
             model2.render();
             terrain.render();
             glMatrixMode(GL_PROJECTION);
             //endregion
         }
         glPopMatrix();
 
         // Push the Texture bit for the model
         glPushAttrib(GL_TEXTURE_BIT);
         {
             //region 2D stuff
             hud.render();
             //endregion
         }
         glPopAttrib();
     }
 
     public void Update() {
 
         if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
             Exit();
             return;
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
             cam.move(0.2f, 90);
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
             cam.move(0.2f, 270);
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
             cam.move(0.2f, 0);
             cam.moveUp(0.2f, 0);
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
             cam.move(0.2f, 180);
             cam.moveUp(0.2f, 180);
         }
     }
 
     protected void Exit() {
         delegate.change(0);
     }
 }
