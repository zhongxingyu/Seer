 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main;
 
 import static opengl.GL.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import opengl.GL;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 import util.Camera;
 import util.Geometry;
 import util.Util;
 
 /**
  *
  * @author Sascha Kolodzey, Nico Marniok
  */
 public class SolSystem {
     private static int programID;       // shader program id
     private static Geometry earth;      // geometry of earth
     private static int modelLocation;   // uniform location of "model"
     private static final Matrix4f modelMatrix = new Matrix4f();
     private static final Vector3f moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
     private static boolean bContinue = true;
     private static final Camera cam = new Camera();
     private static boolean culling = true;
     private static boolean wireframe = true;
     private static int bg = 256;
     private static int lg = 256;
     private static String image = "texture/earth.jpeg";
     private static float rotangle;
     private static float rotspeed = 0.001f;
     
     public static void main(String[] argv) {
         try {
         	earth = Util.createSphere(1, lg, bg, image);
             init();
             programID = Util.createShaderProgram("shader/Main_VS.glsl", "shader/VertexColor_FS.glsl");
             modelLocation = glGetUniformLocation(programID, "model");
             cam.move(-5.0f, 0.0f, 0.0f);
             glEnable(GL_CULL_FACE);
             glFrontFace(GL_CCW);
             glCullFace(GL_BACK);
             render();
             GL.destroy();
         } catch (LWJGLException ex) {
             Logger.getLogger(SolSystem.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static void render() throws LWJGLException {
         glClearColor(0.1f, 0.0f, 0.0f, 1.0f); // dark red
         long last = System.currentTimeMillis();
         long now;
         while(bContinue && !Display.isCloseRequested()) {            
             glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
             glUseProgram(programID);
             
             modelMatrix.store(Util.MAT_BUFFER);
             Util.MAT_BUFFER.position(0);
             glUniformMatrix4(modelLocation, false, Util.MAT_BUFFER);
             earth.draw();
             
             Display.update();
             Display.sync(60);
             
             now = System.currentTimeMillis();
             if((rotangle += (float)( rotspeed * (now - last))) >= Util.PI_MUL2)
             	rotangle = Util.PI_MUL2 - rotangle;
             handleInput(now - last);
             updateUniforms();
             last = now;
             
         }
     }
     
     public static void handleInput(long millis) {
         float moveSpeed = 2e-3f*(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2.0f : 1.0f)*(float)millis;
         float camSpeed = 5e-3f;
         
         while(Keyboard.next()) {
             if(Keyboard.getEventKeyState()) {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W: moveDir.z += 1.0f; break;
                     case Keyboard.KEY_S: moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_A: moveDir.x += 1.0f; break;
                     case Keyboard.KEY_D: moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_SPACE: moveDir.y += 1.0f; break;
                     case Keyboard.KEY_C: moveDir.y -= 1.0f; break;
                 }
             } else {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W: moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_S: moveDir.z += 1.0f; break;
                     case Keyboard.KEY_A: moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_D: moveDir.x += 1.0f; break;
                     case Keyboard.KEY_SPACE: moveDir.y -= 1.0f; break;
                     case Keyboard.KEY_C: moveDir.y += 1.0f; break;
                     case Keyboard.KEY_F1: cam.changeProjection(); break;
                     case Keyboard.KEY_F2: glPolygonMode(GL_FRONT_AND_BACK, (wireframe ^= true) ? GL_FILL : GL_LINE); break;
                     case Keyboard.KEY_F3: if(culling ^= true) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE); break;
                     case Keyboard.KEY_UP: if(bg < 1024){
                     	earth.delete(); earth = Util.createSphere(1, bg*=2, lg*=2, image);
                     }break;
                     case Keyboard.KEY_DOWN: if(bg > 2){
                     	earth.delete(); earth = Util.createSphere(1, bg/=2, lg/=2, image);
                     }break;   
                     case Keyboard.KEY_LEFT: rotspeed /= 2f;break;
                     case Keyboard.KEY_RIGHT: rotspeed *= 2f;break;
                 }
             }
         }
         
         cam.move(moveSpeed * moveDir.z, moveSpeed * moveDir.x, moveSpeed * moveDir.y);
         
         while(Mouse.next()) {
             if(Mouse.getEventButton() == 0) {
                 Mouse.setGrabbed(Mouse.getEventButtonState());
             }
             if(Mouse.isGrabbed()) {                
                 cam.rotate(-camSpeed*Mouse.getEventDX(), -camSpeed*Mouse.getEventDY());
             }
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) bContinue = false;
     }
     
     public static void updateUniforms() {
         Matrix4f viewProj = Matrix4f.mul(cam.getProjection(), cam.getView(), null);
         Util.mul(modelMatrix, Util.rotationZ(Util.PI_DIV4 / 2f, null), Util.rotationY(rotangle, null));
         viewProj.store(Util.MAT_BUFFER);
         Util.MAT_BUFFER.position(0);
         int viewProjLoc = glGetUniformLocation(programID, "viewProj");
         glUniformMatrix4(viewProjLoc, false, Util.MAT_BUFFER);
     }
 }
