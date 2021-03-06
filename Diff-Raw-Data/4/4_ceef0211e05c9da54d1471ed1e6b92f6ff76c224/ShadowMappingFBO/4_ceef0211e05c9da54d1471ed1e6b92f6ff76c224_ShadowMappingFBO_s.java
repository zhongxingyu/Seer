 /*
  * Copyright (c) 2013, Sam K., Daniel K.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those
  * of the authors and should not be interpreted as representing official policies,
  * either expressed or implied, of the FreeBSD Project.
  */
 
 package future;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 import utility.BufferTools;
 import utility.EulerCamera;
 import utility.OBJLoader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 
 import static org.lwjgl.opengl.ARBFramebufferObject.*;
 import static org.lwjgl.opengl.ARBShadowAmbient.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
 import static org.lwjgl.opengl.GL14.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 /**
  * Shows how to get shadows working in OpenGL. Ported from the OpenGLSuperBible. Some code was modified by Oskar Veerhoek.
  *
  * @author Sam K.
  * @author Daniel W.
  */
 public class ShadowMappingFBO {
     private static int shadowMapWidth;
     private static int shadowMapHeight;
     private static int frameBuffer;
     private static int renderBuffer;
     private static int bunnyDisplayList;
 
     private static final FloatBuffer ambientLight = BufferTools.asFlippedFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F);
     private static final FloatBuffer diffuseLight = BufferTools.asFlippedFloatBuffer(1.7F, 1.7F, 1.7F, 1.0F);
     private static final FloatBuffer lightPosition = BufferTools.asFlippedFloatBuffer(200.0F, 250.0F, 200.0F, 1.0F);
     private static final FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(16);
     private static final Matrix4f textureMatrix = new Matrix4f();
     private static final DisplayMode DISPLAY_MODE = new DisplayMode(640, 480);
     private static final EulerCamera camera = new EulerCamera.Builder()
             .setAspectRatio((float) DISPLAY_MODE.getWidth() / DISPLAY_MODE.getHeight())
             .setPosition(23, 34, 87)
             .setRotation(22, 341, 0)
             .setNearClippingPane(2)
             .setFarClippingPane(300)
             .setFieldOfView(60)
             .build();
     public static final String MODEL_LOCATION = "res/models/bunny.obj";
 
     public static void main(String[] args) {
         setUpDisplay();
         setUpStates();
         setUpFramebufferObject();
         generateShadowMap();
         setUpCamera();
         setUpModel();
         while (!Display.isCloseRequested()) {
             render();
             logic();
             input();
             Display.update();
             Display.sync(60);
         }
         cleanUp();
         System.exit(0);
     }
 
     public static void setUpModel() {
         try {
             bunnyDisplayList = OBJLoader.createDisplayList(OBJLoader.loadModel(new File(MODEL_LOCATION)));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             cleanUp();
         } catch (IOException e) {
             e.printStackTrace();
             cleanUp();
         }
     }
 
     public static void setUpCamera() {
         camera.applyPerspectiveMatrix();
         camera.applyOptimalStates();
     }
 
     public static void setUpStates() {
         // Enable, well, lighting
         glEnable(GL_LIGHTING);
         // Have the submitted colours play a role in the lighting calculations
         glEnable(GL_COLOR_MATERIAL);
         glEnable(GL_NORMALIZE);
         glEnable(GL_LIGHT0);
         glDisable(GL_POLYGON_OFFSET_FILL);
         glEnable(GL_DEPTH_TEST);
         glPolygonOffset(2.5F, 0.0F);
         glClearColor(0, 0.75f, 1, 1);
         glEnable(GL_CULL_FACE);
         glEnable(GL_COLOR_MATERIAL);
         glColorMaterial(GL_FRONT, GL_DIFFUSE);
         glLight(GL_LIGHT0, GL_POSITION, lightPosition);
         glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientLight);
         glLight(GL_LIGHT0, GL_AMBIENT, ambientLight);
         glLight(GL_LIGHT0, GL_DIFFUSE, diffuseLight);
     }
 
     /**
      * Sets up the OpenGL states.
      */
     public static void setUpFramebufferObject() {
         int maxRenderbufferSize = glGetInteger(GL_MAX_RENDERBUFFER_SIZE);
         int maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
 
         System.out.println("Maximum texture size: " + maxTextureSize);
         System.out.println("Maximum renderbuffer size: " + maxRenderbufferSize);
 
         /**
          * Cap the maximum texture and renderbuffer size at 1024x1024 pixels. If you have a good
          * graphics card, feel free to increase this value. The program will lag
          * if I record and run the program at the same time with higher values.
          */
         if (maxTextureSize > 1024) {
             maxTextureSize = 1024;
             if (maxRenderbufferSize < maxTextureSize) {
                 maxTextureSize = maxRenderbufferSize;
             }
         }
 
         shadowMapWidth = maxTextureSize;
         shadowMapHeight = maxTextureSize;
 
         // Clamps texture coordinates (e.g.: (2,0) becomes (1,0)) because we only want one shadow.
         // Uses 'TO_EDGE' to prevent the texture boarders to affect the shadow map through linear texture filtering.
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
         // Enable bilinear texture filtering. This means that the colour will be
         // a 'weighted value of the four texture elements that are closest to the center of the pixel being textured'
         // (from OpenGL 2.1 References Pages).
         // The alternative to linear texture filtering is nearest-neighbour texture filtering. Here the colour of
         // the closest texel to the given texture coordinate (texture pixel) is taken. This, while being fast, may
         // produce blockiness.
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
         // State that the texture holds nondescript 'intenstity' data.
         glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
         // If the intensity of a given texel is lower than 0.5f, then the texture should not be sampled. In practice,
         // the higher the value, the less of the shadow is visible, and the other way around.
         glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FAIL_VALUE_ARB,
                 0.5F);
 
         // Set the automatic texture coordinate generation mode to eye linear. The texture coordinate is calculated
         // with the inverse of the model-view matrix and a so-called 'object plane' (have yet to find out what that means).
         glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
         glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
         glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
         glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
         frameBuffer = glGenFramebuffers();
         glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
         renderBuffer = glGenRenderbuffers();
         glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
         // Set the internal storage format of the render buffer to a depth component of 32 bits (4 bytes).
         glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32,
                 maxTextureSize, maxTextureSize);
         // Attach the render buffer to the frame buffer as a depth attachment. This means that, if the frame buffer is
         // bound, any depth texture values will be copied to the render buffer object.
         glFramebufferRenderbuffer(GL_FRAMEBUFFER,
                 GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                 renderBuffer);
         // OpenGL shall make no amendment to the colour or multisample buffer.
         glDrawBuffer(GL_NONE);
         // Disable the colour buffer for pixel read operations (such as glReadPixels or glCopyTexImage2D).
         glReadBuffer(GL_NONE);
         // Check for frame buffer errors.
         int FBOStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
         if (FBOStatus != GL_FRAMEBUFFER_COMPLETE) {
             System.err.println("Framebuffer error: " + gluErrorString(glGetError()));
         }
         // Bind the default frame buffer.
         glBindFramebuffer(GL_FRAMEBUFFER, 0);
     }
 
     /**
      * Generate the shadow map.
      */
     private static void generateShadowMap() {
         /**
          * The model-view matrix of the light.
          */
         FloatBuffer lightModelView = BufferUtils.createFloatBuffer(16);
         /**
          * The projection matrix of the light.
          */
         FloatBuffer lightProjection = BufferUtils.createFloatBuffer(16);
         Matrix4f lightProjectionTemp = new Matrix4f();
         Matrix4f lightModelViewTemp = new Matrix4f();
         /**
          * The radius which encompasses all the objects which cast shadows in the scene. There should
          * be no object farther from [0, 0, 0] away than 50 units in any direction.
          * If an object exceeds the radius, the object may cast shadows wrongly.
          */
         float sceneBoundingRadius = 50F;
         /**
          * The distance from the light to the scene, assuming that the scene is
          * at [0, 0, 0]. The distance is the square-root of the sum of each of
          * the components of the light position squared.
          */
         float lightToSceneDistance = (float) Math.sqrt(
                 lightPosition.get(0) * lightPosition.get(0) +
                 lightPosition.get(1) * lightPosition.get(1) +
                 lightPosition.get(2) * lightPosition.get(2));
         /**
          * The distance to the object which is nearest to the camera. This excludes
          * objects that do not cast shadows. This will be used
          * as the zNear parameter in gluPerspective.
          */
         float nearPlane = lightToSceneDistance - sceneBoundingRadius;
         /**
          * The field-of-view of the shadow frustum. Code taken from the OpenGL SuperBible.
          */
         float fieldOfView = (float) Math.toDegrees(2.0F * Math
                 .atan(sceneBoundingRadius / lightToSceneDistance));
         glMatrixMode(GL_PROJECTION);
         /**
          * Store the current projection matrix.
          */
         glPushMatrix();
         glLoadIdentity();
         gluPerspective(fieldOfView, 1, nearPlane, nearPlane
                 + sceneBoundingRadius * 2);
         glGetFloat(GL_PROJECTION_MATRIX, lightProjection);
         glMatrixMode(GL_MODELVIEW);
         /**
          * Store the current model-view matrix.
          */
         glPushMatrix();
         glLoadIdentity();
         gluLookAt(
                 lightPosition.get(0), lightPosition.get(1), lightPosition.get(2),
                 0, 0, 0, 0, 1, 0);
         glGetFloat(GL_MODELVIEW_MATRIX, lightModelView);
         glViewport(0, 0, shadowMapWidth, shadowMapHeight);
         /**
          * Bind the extra frame buffer in which to store the shadow map in the form a depth texture.
          */
         glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
         /**
          * Clear only the depth buffer bit. Clearing the colour buffer is unnecessary, because it is disabled (we
          * only need depth components).
          */
         glClear(GL_DEPTH_BUFFER_BIT);
         /**
          * Store the current attribute state.
          */
         glPushAttrib(GL_ALL_ATTRIB_BITS);
         /**
          * Disable smooth shading, because the shading in a shadow map is irrelevant. It only matters where the shape
          * vertices are positioned, and not what colour they have.
          */
         glShadeModel(GL_FLAT);
         /**
          * Enabling all these lighting states is unnecessary for reasons listed above.
          */
         glDisable(GL_LIGHTING);
         glDisable(GL_COLOR_MATERIAL);
         glDisable(GL_NORMALIZE);
         /**
          * Disable the writing of the red, green, blue, and alpha colour components, because we only need the depth component.
          */
         glColorMask(false, false, false, false);
         /**
          * An offset is given to every depth value of every polygon fragment to prevent a visual quirk called 'shadow acne'.
          */
         glEnable(GL_POLYGON_OFFSET_FILL);
         /**
          * Draw the objects which cast shadows.
          */
         drawShadowCastingObjects();
         /**
          * Copy the pixels of the shadow map to the frame buffer object depth attachment.
          *  int target -> GL_TEXTURE_2D
          *  int level  -> 0, has to do with mip-mapping, which is not applicable to shadow maps
          *  int internalformat -> GL_DEPTH_COMPONENT
          *  int x, y -> 0, 0
          *  int width, height -> shadowMapWidth, shadowMapHeight
          *  int border -> 0
          */
         glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0,
                 shadowMapWidth, shadowMapHeight, 0);
         // Restore the previous model-view matrix.
         glPopMatrix();
         glMatrixMode(GL_PROJECTION);
         // Restore the previous projection matrix.
         glPopMatrix();
         glMatrixMode(GL_MODELVIEW);
         glBindFramebuffer(GL_FRAMEBUFFER, 0);
         // Restore the previous attribute state.
         glPopAttrib();
         // Restore the viewport.
         glViewport(0, 0, Display.getWidth(), Display.getHeight());
         lightProjectionTemp.load(lightProjection);
         lightModelViewTemp.load(lightModelView);
         lightProjection.flip();
         lightModelView.flip();
         textureMatrix.setIdentity();
         textureMatrix.translate(new Vector3f(0.5F, 0.5F, 0.5F));
         textureMatrix.scale(new Vector3f(0.5F, 0.5F, 0.5F));
         Matrix4f.mul(textureMatrix, lightProjectionTemp, textureMatrix);
         Matrix4f.mul(textureMatrix, lightModelViewTemp, textureMatrix);
         Matrix4f.transpose(textureMatrix, textureMatrix);
     }
 
     /**
      * Sets up a display.
      */
     private static void setUpDisplay() {
         try {
             Display.setDisplayMode(DISPLAY_MODE);
             Display.setVSyncEnabled(true);
             Display.setTitle("Shadow Mapping Demo");
             Display.create();
         } catch (LWJGLException e) {
             System.err.println("Couldn't set up the display");
             Display.destroy();
             System.exit(1);
         }
         if (!GLContext.getCapabilities().OpenGL14
                 && !GLContext.getCapabilities().GL_ARB_shadow) {
             System.out
                     .println("Can't create shadows at all. Requires OpenGL 1.4 or the GL_ARB_shadow extension");
             Display.destroy();
             System.exit(1);
         }
         if (!GLContext.getCapabilities().GL_ARB_shadow_ambient) {
             System.err
                     .println("GL_ARB_shadow_ambient extension not available.");
             Display.destroy();
             System.exit(1);
         }
     }
 
     private static void drawGround() {
         glPushAttrib(GL_LIGHTING_BIT);
         glDisable(GL_LIGHTING);
         glBegin(GL_QUADS);
         glColor3f(0.3F, 0.6F, 0.3F);
         glVertex3f(-120.0F, -19.0F, -120.0F);
         glVertex3f(-120.0F, -19.0F, +120.0F);
         glVertex3f(+120.0F, -19.0F, +120.0F);
         glVertex3f(+120.0F, -19.0F, -120.0F);
         glEnd();
         glPopAttrib();
     }
 
     /**
      * This is where anything you want rendered into your world should go.
      */
     private static void drawShadowCastingObjects() {
         glPushMatrix();
         glScalef(5, 5, 5);
         glTranslatef(0, -2, 0);
         glCallList(bunnyDisplayList);
         glPopMatrix();
     }
 
     private static void render() {
         glLoadIdentity();
         camera.applyTranslations();
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glPushAttrib(GL_ALL_ATTRIB_BITS);
         {
             glEnable(GL_TEXTURE_2D);
             glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
             glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE,
                     GL_COMPARE_R_TO_TEXTURE);
             glEnable(GL_TEXTURE_GEN_S);
             glEnable(GL_TEXTURE_GEN_T);
             glEnable(GL_TEXTURE_GEN_R);
             glEnable(GL_TEXTURE_GEN_Q);
 
             textureBuffer.clear();
             textureBuffer.put(0, textureMatrix.m00);
             textureBuffer.put(1, textureMatrix.m01);
             textureBuffer.put(2, textureMatrix.m02);
             textureBuffer.put(3, textureMatrix.m03);
 
             glTexGen(GL_S, GL_EYE_PLANE, textureBuffer);
 
             textureBuffer.put(0, textureMatrix.m10);
             textureBuffer.put(1, textureMatrix.m11);
             textureBuffer.put(2, textureMatrix.m12);
             textureBuffer.put(3, textureMatrix.m13);
 
             glTexGen(GL_T, GL_EYE_PLANE, textureBuffer);
 
             textureBuffer.put(0, textureMatrix.m20);
             textureBuffer.put(1, textureMatrix.m21);
             textureBuffer.put(2, textureMatrix.m22);
             textureBuffer.put(3, textureMatrix.m23);
 
             glTexGen(GL_R, GL_EYE_PLANE, textureBuffer);
 
             textureBuffer.put(0, textureMatrix.m30);
             textureBuffer.put(1, textureMatrix.m31);
             textureBuffer.put(2, textureMatrix.m32);
             textureBuffer.put(3, textureMatrix.m33);
 
             glTexGen(GL_Q, GL_EYE_PLANE, textureBuffer);
 
             drawGround();
             drawShadowCastingObjects();
             generateShadowMap();
         }
         glPopAttrib();
     }
 
     public static void logic() {
         glLight(GL_LIGHT0, GL_POSITION, lightPosition);
     }
 
     /**
      * Handles the keyboard and mouse input.
      */
     public static void input() {
         while (Keyboard.next()) {
             if (Keyboard.getEventKeyState()) {
                 if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                     lightPosition.flip();
                     lightPosition.clear();
                     lightPosition.put(new float[]{camera.x(), camera.y(), camera.z(), 1});
                     lightPosition.flip();
                 }
             }
         }
         if (Mouse.isGrabbed())
             camera.processMouse(1.0f, 80, -80);
         camera.processKeyboard(16.0f, 10);
         if (Mouse.isButtonDown(0))
             Mouse.setGrabbed(true);
         else if (Mouse.isButtonDown(1))
             Mouse.setGrabbed(false);
     }
 
     /**
      * Cleanup after the program.
      */
     private static void cleanUp() {
         glDeleteFramebuffers(frameBuffer);
         glDeleteLists(bunnyDisplayList, 1);
         glDeleteRenderbuffers(renderBuffer);
         Display.destroy();
     }
 }
