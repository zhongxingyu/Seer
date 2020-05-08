 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.jogl.rendering.engine;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.GL;
 
 import org.apache.log4j.Logger;
 
 import com.se.simplicity.jogl.JOGLComponent;
 import com.se.simplicity.jogl.engine.JOGLEngine;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.Light;
 import com.se.simplicity.rendering.NamedRenderer;
 import com.se.simplicity.rendering.Renderer;
 import com.se.simplicity.rendering.engine.RenderingEngine;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.scenegraph.SimpleTraversal;
 import com.se.simplicity.scenegraph.model.ModelNode;
 import com.se.simplicity.vector.SimpleMatrixf44;
 import com.se.simplicity.vector.SimpleRGBColourVectorf4;
 import com.se.simplicity.vector.SimpleVectorf4;
 
 /**
  * <p>
  * Manages the rendering of a {@link com.se.simplicity.scenegraph.SceneGraph SceneGraph} in a JOGL environment. This implementation uses only simple
  * rendering techniques and properties.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SimpleJOGLRenderingEngine extends JOGLEngine implements RenderingEngine
 {
     /**
      * <p>
      * The number of milliseconds in a second.
      * </p>
      */
     private static final double MILLISECONDS_IN_A_SECOND = 1000.0;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.Camera Camera} through which the {@link com.se.simplicity.scenegraph.SceneGraph SceneGraph} will be
      * rendered.
      * </p>
      */
     private Camera fCamera;
 
     /**
      * <p>
      * The colour to clear the screen buffer with before rendering.
      * </p>
      */
     private SimpleVectorf4 fClearingColour;
 
     /**
      * <p>
      * The clearing mode. Determines if the screen buffer is cleared before rendering.
      * </p>
      * 
      * @return True if the screen buffer is cleared before rendering, false otherwise.
      */
     private boolean fClearsBeforeRender;
 
     /**
      * <p>
      * The initialisation status. Determines if this <code>Renderer</code> is initialised.
      * </p>
      */
     private boolean fIsInitialised;
 
     /**
      * <p>
      * Logs messages associated with this class.
      * </p>
      */
     private Logger fLogger;
 
     /**
      * <p>
      * The preferred frequency (advancements per second) of this <code>SimpleJOGLRenderingEngine</code>.
      * </p>
      */
     private int fPreferredFrequency;
 
     /**
      * <p>
      * The root {@link com.se.simplicity.scenegraph.Node Node}s of the portions of the {@link com.se.simplicity.scene.Scene Scene}s that the
      * {@link com.se.simplicity.rendering.Renderer Renderer}s will render when they are executed.
      * </p>
      */
     private Map<Renderer, Node> fRendererRoots;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.Renderer Renderer}s that will be executed against the {@link com.se.simplicity.scene.Scene Scene} during
      * the {@link com.se.simplicity.jogl.rendering.engine.SimpleJOGLRenderingEngine#advance() advance()} method.
      * </p>
      */
     private List<Renderer> fRenderers;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.SceneGraph SceneGraph} to be rendered.
      * </p>
      */
     private Scene fScene;
 
     /**
      * <p>
      * The size of the viewport.
      * </p>
      */
     private Dimension fViewportSize;
 
     /**
      * <p>
      * Creates an instance of <code>SimpleJOGLRenderer</code>.
      * </p>
      */
     public SimpleJOGLRenderingEngine()
     {
         fCamera = null;
         fClearingColour = new SimpleRGBColourVectorf4(0.0f, 0.0f, 0.0f, 1.0f);
         fClearsBeforeRender = true;
         fIsInitialised = false;
         fLogger = Logger.getLogger(getClass().getName());
         fRendererRoots = new HashMap<Renderer, Node>();
         fRenderers = new ArrayList<Renderer>();
         fScene = null;
         fViewportSize = null;
     }
 
     @Override
     public void addRenderer(final int index, final Renderer renderer)
     {
         fRenderers.add(index, renderer);
 
         ((JOGLComponent) renderer).setGL(getGL());
 
         if (fScene != null)
         {
             fRendererRoots.put(renderer, fScene.getSceneGraph().getRoot());
         }
     }
 
     @Override
     public void addRenderer(final Renderer renderer)
     {
         addRenderer(fRenderers.size(), renderer);
     }
 
     @Override
     public void advance()
     {
         super.advance();
 
         if (fCamera == null)
         {
             throw new IllegalStateException("This Rendering Engine does not have a Camera to view the Scene through.");
         }
 
         fCamera.init();
 
         if (fScene == null)
         {
             throw new IllegalStateException("This Rendering Engine does not have a Scene to render.");
         }
 
         if (!fIsInitialised)
         {
             try
             {
                 init();
             }
             catch (IllegalStateException e)
             {
                 fLogger.error("Failed to initialise the engine.", e);
 
                 return;
             }
         }
 
         GL gl = getGL();
 
         // Clear the buffers.
         if (fClearsBeforeRender)
         {
             gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
         }
 
         for (Renderer renderer : fRenderers)
         {
             if (fRendererRoots.get(renderer) != null)
             {
                 renderer.init();
 
                 // Render the Scene Graph.
                 gl.glPushMatrix();
                 {
                     fCamera.apply();
 
                     for (Light light : fScene.getLights())
                     {
                         light.apply();
                     }
 
                     renderSceneGraph(renderer, fRendererRoots.get(renderer));
                 }
                 gl.glPopMatrix();
 
                 renderer.dispose();
             }
         }
     }
 
     /**
      * <p>
      * Backtracks up the <code>SceneGraph</code> the number of levels given.
      * </p>
      * 
      * <p>
      * A backtrack is an upward movement in the graph being rendered.
      * </p>
      * 
      * @param backtracks The number of levels to backtrack.
      */
     protected void backtrack(final int backtracks)
     {
         for (int index = 0; index < backtracks; index++)
         {
             getGL().glPopMatrix();
         }
     }
 
     @Override
     public boolean clearsBeforeRender()
     {
         return (fClearsBeforeRender);
     }
 
     @Override
     public void destroy()
     {
         GL gl = getGL();
 
         // Revert depth test settings.
         gl.glDepthFunc(GL.GL_LESS);
         gl.glDisable(GL.GL_DEPTH_TEST);
 
         // Revert face culling settings.
         gl.glDisable(GL.GL_CULL_FACE);
 
         // Revert clientr state settings.
         gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
 
         // Revert clearing settings.
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
     }
 
     @Override
     public Camera getCamera()
     {
         return (fCamera);
     }
 
     @Override
     public SimpleVectorf4 getClearingColour()
     {
         return (fClearingColour);
     }
 
     @Override
     public int getPreferredFrequency()
     {
         return (fPreferredFrequency);
     }
 
     @Override
     public Node getRendererRoot(final Renderer renderer)
     {
         return fRendererRoots.get(renderer);
     }
 
     @Override
     public List<Renderer> getRenderers()
     {
         return (fRenderers);
     }
 
     @Override
     public Scene getScene()
     {
         return (fScene);
     }
 
     @Override
     public Dimension getViewportSize()
     {
         return (fViewportSize);
     }
 
     @Override
     public void init()
     {
         GL gl = getGL();
 
         // Ensure objects further from the viewpoint are not drawn over the top of closer objects. To assist multi pass rendering, objects at the
         // exact same distance can be rendered over (i.e. the object will be rendered using the result of the last Renderer executed).
         gl.glDepthFunc(GL.GL_LEQUAL);
         gl.glEnable(GL.GL_DEPTH_TEST);
 
         // Only render the front (counter-clockwise) side of a polygon.
         gl.glEnable(GL.GL_CULL_FACE);
 
         // Enable model data arrays.
         gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
 
         // Set the colour buffer clearing colour.
         float[] clearingColour = fClearingColour.getArray();
         gl.glClearColor(clearingColour[0], clearingColour[1], clearingColour[2], clearingColour[3]);
 
         // Initialise the viewport size.
         if (fViewportSize != null)
         {
             gl.glViewport(0, 0, fViewportSize.width, fViewportSize.height);
         }
 
         fIsInitialised = true;
     }
 
     @Override
     public void removeRenderer(final Renderer renderer)
     {
         fRenderers.remove(renderer);
 
         fRendererRoots.remove(renderer);
     }
 
     @Override
     public void renderSceneGraph(final Renderer renderer, final Node root)
     {
         GL gl = getGL();
 
         // For every node in the traversal of the scene.
         SimpleTraversal traversal = new SimpleTraversal(root);
         Node currentNode;
 
         while (traversal.hasMoreNodes())
         {
             // Remove transformations from the stack that do not apply to the next node.
             backtrack(traversal.getBacktracksToNextNode());
 
             // Apply the transformation of the current node.
             currentNode = traversal.getNextNode();
 
             gl.glPushMatrix();
             gl.glMultMatrixf(((SimpleMatrixf44) currentNode.getTransformation()).getArray(), 0);
 
             // Render the current node if it is a model.
             if (currentNode instanceof ModelNode && currentNode.isVisible())
             {
                 if (renderer instanceof NamedRenderer)
                 {
                    ((NamedRenderer) renderer).renderModel(((ModelNode) currentNode).getModel(), currentNode.getID());
                 }
                 else
                 {
                     renderer.renderModel(((ModelNode) currentNode).getModel());
                 }
             }
         }
 
         // Remove all remaining transformations from the stack.
         backtrack(traversal.getBacktracksToNextNode());
     }
 
     @Override
     public void reset()
     {
         init();
     }
 
     @Override
     public void run()
     {
         init();
 
         while (!Thread.interrupted())
         {
             sleep();
             advance();
         }
 
         destroy();
     }
 
     @Override
     public void setCamera(final Camera camera)
     {
         fCamera = camera;
     }
 
     @Override
     public void setClearingColour(final SimpleVectorf4 clearingColour)
     {
         fClearingColour = clearingColour;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setClearsBeforeRender(final boolean clearsBeforeRender)
     {
         fClearsBeforeRender = clearsBeforeRender;
     }
 
     @Override
     public void setGL(final GL gl)
     {
         super.setGL(gl);
 
         for (Renderer renderer : fRenderers)
         {
             ((JOGLComponent) renderer).setGL(gl);
         }
 
         if (fScene != null)
         {
             ((JOGLComponent) fScene).setGL(gl);
         }
     }
 
     @Override
     public void setPreferredFrequency(final int preferredFrequency)
     {
         fPreferredFrequency = preferredFrequency;
     }
 
     @Override
     public void setRendererRoot(final Renderer renderer, final Node root)
     {
         fRendererRoots.put(renderer, root);
     }
 
     @Override
     public void setScene(final Scene scene)
     {
         if (scene.getSceneGraph() == null)
         {
             throw new IllegalArgumentException("Invalid Scene: Must contain Scene Graph.");
         }
 
         fScene = scene;
 
         ((JOGLComponent) fScene).setGL(getGL());
 
         if (fScene != null)
         {
             for (Renderer renderer : fRenderers)
             {
                 if (fRendererRoots.get(renderer) == null)
                 {
                     fRendererRoots.put(renderer, fScene.getSceneGraph().getRoot());
                 }
             }
         }
     }
 
     @Override
     public void setViewportSize(final Dimension viewportSize)
     {
         fViewportSize = viewportSize;
 
         fIsInitialised = false;
     }
 
     /**
      * <p>
      * Causes this <code>SimpleJOGLRenderingEngine</code> to sleep for the appropriate amount of time to allow for its preferred frequency.
      * </p>
      */
     protected void sleep()
     {
         try
         {
             Thread.sleep((long) MILLISECONDS_IN_A_SECOND / fPreferredFrequency);
         }
         catch (InterruptedException e)
         {
             Thread.currentThread().interrupt();
 
             fLogger.debug("The engine was interrupted while sleeping.");
         }
     }
 }
