 /*
  * Copyright (c) 2003-2005 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package jmetest.renderer;
 
 import com.jme.app.BaseGame;
 import com.jme.bounding.BoundingSphere;
 import com.jme.image.Texture;
 import com.jme.input.InputHandler;
 import com.jme.input.NodeHandler;
 import com.jme.light.DirectionalLight;
 import com.jme.light.SpotLight;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.CameraNode;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.Text;
 import com.jme.scene.TriMesh;
 import com.jme.scene.shape.Box;
 import com.jme.scene.state.AlphaState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.TextureState;
 import com.jme.scene.state.ZBufferState;
 import com.jme.system.DisplaySystem;
 import com.jme.system.JmeException;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 
 /**
  * <code>TestLightState</code>
  * @author Mark Powell
 * @version $Id: TestCameraNode.java,v 1.11 2005-09-15 17:13:22 renanse Exp $
  */
 public class TestCameraNode extends BaseGame {
     private TriMesh t;
     private CameraNode camNode;
     private Camera cam;
     private Text text;
     private Node root;
     private Node scene;
     private InputHandler input;
    private Thread thread;
     private Timer timer;
 
     /**
      * Entry point for the test,
      * @param args
      */
     public static void main(String[] args) {
         TestCameraNode app = new TestCameraNode();
         app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
         app.start();
 
     }
 
     public void addSpatial(Spatial spatial) {
         scene.attachChild(spatial);
         scene.updateGeometricState(0.0f, true);
         System.out.println(scene.getQuantity());
     }
 
     /**
      * Not used in this test.
      * @see com.jme.app.SimpleGame#update()
      */
     protected void update(float interpolation) {
 
         timer.update();
         input.update(timer.getTimePerFrame());
         text.print("Frame Rate: " + timer.getFrameRate());
 
         scene.updateGeometricState(0.0f, true);
 
 
     }
 
     /**
      * clears the buffers and then draws the TriMesh.
      * @see com.jme.app.SimpleGame#render()
      */
     protected void render(float interpolation) {
         display.getRenderer().clearBuffers();
 
         display.getRenderer().draw(root);
 
     }
 
     /**
      * creates the displays and sets up the viewport.
      * @see com.jme.app.SimpleGame#initSystem()
      */
     protected void initSystem() {
         try {
             display = DisplaySystem.getDisplaySystem(properties.getRenderer());
             display.createWindow(
                 properties.getWidth(),
                 properties.getHeight(),
                 properties.getDepth(),
                 properties.getFreq(),
                 properties.getFullscreen());
             cam =
                 display.getRenderer().createCamera(
                     properties.getWidth(),
                     properties.getHeight());
 
         } catch (JmeException e) {
             e.printStackTrace();
             System.exit(1);
         }
         ColorRGBA blackColor = new ColorRGBA(0, 0, 0, 1);
         display.getRenderer().setBackgroundColor(blackColor);
         cam.setFrustum(1.0f, 1000.0f, -0.55f, 0.55f, 0.4125f, -0.4125f);
         Vector3f loc = new Vector3f(0.0f, 0.0f, 0.0f);
         Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
         Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
         Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
         cam.setFrame(loc, left, up, dir);
         display.getRenderer().setCamera(cam);
         camNode = new CameraNode("Camera Node", cam);
         input = new NodeHandler(this, camNode, "LWJGL");
         input.setKeySpeed(15f);
         input.setMouseSpeed(1);
         timer = Timer.getTimer("LWJGL");
 
 
     }
 
     /**
      * builds the trimesh.
      * @see com.jme.app.SimpleGame#initGame()
      */
     protected void initGame() {
         text = new Text("Text Label", "Timer");
         text.setLocalTranslation(new Vector3f(1,60,0));
         TextureState textImage = display.getRenderer().createTextureState();
         textImage.setEnabled(true);
         textImage.setTexture(
             TextureManager.loadTexture(
                 TestCameraNode.class.getClassLoader().getResource("jmetest/data/font/font.png"),
                 Texture.MM_LINEAR,
                 Texture.FM_LINEAR));
         text.setRenderState(textImage);
         AlphaState as1 = display.getRenderer().createAlphaState();
         as1.setBlendEnabled(true);
         as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
         as1.setDstFunction(AlphaState.DB_ONE);
         as1.setTestEnabled(true);
         as1.setTestFunction(AlphaState.TF_GREATER);
         as1.setEnabled(true);
         text.setRenderState(as1);
         scene = new Node("3D Scene Node");
         scene.attachChild(text);
 
         Vector3f max = new Vector3f(5,5,5);
         Vector3f min = new Vector3f(-5,-5,-5);
 
 
 
         t = new Box("Box 1", min,max);
         t.setModelBound(new BoundingSphere());
         t.updateModelBound();
 
         t.setLocalTranslation(new Vector3f(0,0,0));
 
         Box t2 = new Box("Box 2", min.divide(4), max.divide(4));
         t2.setLocalTranslation(new Vector3f(-5,0,10));
 
         scene.attachChild(t);
         root = new Node("Root Node");
         root.attachChild(scene);
 
         ZBufferState buf = display.getRenderer().createZBufferState();
         buf.setEnabled(true);
         buf.setFunction(ZBufferState.CF_LEQUAL);
 
         SpotLight am = new SpotLight();
         am.setDiffuse(new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f));
         am.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
         am.setDirection(new Vector3f(0, 0, 0));
         am.setLocation(new Vector3f(25, 10, 0));
         am.setAngle(15);
 
         SpotLight am2 = new SpotLight();
         am2.setDiffuse(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
         am2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
         am2.setDirection(new Vector3f(0, 0, 0));
         am2.setLocation(new Vector3f(-25, 10, 0));
         am2.setAngle(15);
 
 
         DirectionalLight dr = new DirectionalLight();
         dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
         dr.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
         dr.setSpecular(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
         dr.setDirection(new Vector3f(150, 0 , 150));
 
         LightState state = display.getRenderer().createLightState();
         state.attach(am);
         state.attach(dr);
         state.attach(am2);
         am.setEnabled(true);
         am2.setEnabled(true);
         dr.setEnabled(true);
         //scene.setRenderState(state);
         scene.setRenderState(buf);
 
 
         camNode.setLocalTranslation(new Vector3f(0,0,-75));
         camNode.attachChild(t2);
         scene.attachChild(camNode);
 
         //cam.update();
 
         TextureState ts = display.getRenderer().createTextureState();
                 ts.setEnabled(true);
                 ts.setTexture(
                     TextureManager.loadTexture(
                         TestCameraNode.class.getClassLoader().getResource("jmetest/data/images/Monkey.jpg"),
                         Texture.MM_LINEAR,
                         Texture.FM_LINEAR));
 
         scene.setRenderState(ts);
 
         root.attachChild(text);
 
 
         scene.updateGeometricState(0.0f, true);
         root.updateRenderState();
 
     }
     /**
      * not used.
      * @see com.jme.app.SimpleGame#reinit()
      */
     protected void reinit() {
 
     }
 
     /**
      * Not used.
      * @see com.jme.app.SimpleGame#cleanup()
      */
     protected void cleanup() {
 
     }
 
 }
