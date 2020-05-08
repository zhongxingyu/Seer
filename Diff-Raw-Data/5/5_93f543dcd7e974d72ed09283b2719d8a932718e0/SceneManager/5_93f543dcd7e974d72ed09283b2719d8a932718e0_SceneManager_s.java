 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.internal;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.IFileEditorInput;
 
 import com.se.simplicity.jogl.rendering.SimpleJOGLCamera;
 import com.se.simplicity.jogl.rendering.SimpleJOGLRenderer;
 import com.se.simplicity.jogl.rendering.engine.SimpleJOGLRenderingEngine;
 import com.se.simplicity.jogl.viewport.SimpleJOGLViewport;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.Light;
 import com.se.simplicity.rendering.Renderer;
 import com.se.simplicity.rendering.engine.RenderingEngine;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.scenegraph.SimpleNode;
 import com.se.simplicity.util.metadata.rendering.MetaDataCamera;
 import com.se.simplicity.util.metadata.scene.MetaDataScene;
 import com.se.simplicity.util.scene.SceneFactory;
 import com.se.simplicity.vector.SimpleTranslationVectorf4;
 import com.se.simplicity.viewport.Viewport;
 
 /**
  * <p>
  * Manages the <code>Scene</code>s in use by the editor.
  * </p>
  * 
  * @author Gary Buyn
  */
 public final class SceneManager
 {
     /**
      * <p>
      * The singleton instance of <code>SceneManager</code>.
      * </p>
      */
     private static SceneManager sceneManager = new SceneManager();
 
     /**
      * <p>
      * Returns the singleton instance of <code>SceneManager</code>.
      * </p>
      * 
      * @return The singleton instance of <code>SceneManager</code>.
      */
     public static SceneManager getSceneManager()
     {
         return (sceneManager);
     }
 
     /**
      * <p>
      * The currently active <code>Camera</code>.
      * </p>
      */
     private Camera activeCamera;
 
     /**
      * <p>
      * The currently active <code>Light</code>.
      * </p>
      */
     private Light activeLight;
 
     /**
      * <p>
      * The currently active <code>Node</code>.
      * </p>
      */
     private Node activeNode;
 
     /**
      * <p>
      * The currently active <code>Scene</code>.
      * </p>
      */
     private Scene activeScene;
 
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s registered to listen for changes in the
      * <code>Scene</code>s in use by the editor.
      * </p>
      */
     private List<SceneChangedListener> sceneChangedListeners;
 
     /**
      * <p>
      * The <code>Scene</code>s in use by the editor.
      * </p>
      */
     private Map<String, Scene> scenes;
 
     /**
      * <p>
      * Creates an instance of <code>SceneManager</code>. Hidden because only one instance of the <code>SceneManager</code> should exist.
      * </p>
      */
     private SceneManager()
     {
         activeCamera = null;
         activeLight = null;
         activeNode = null;
         activeScene = null;
         sceneChangedListeners = new ArrayList<SceneChangedListener>();
         scenes = new HashMap<String, Scene>();
     }
 
     /**
      * <p>
      * Registers the given {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener} to listen for changes in the
      * <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @param listener The <code>SceneChangedListener</code> to register.
      */
     public void addSceneChangedListener(final SceneChangedListener listener)
     {
         sceneChangedListeners.add(listener);
     }
 
     /**
      * <p>
      * Adds a <code>Scene</code> to the <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @throws CoreException Thrown if the <code>IFileEditorInput</code> fails to retrieve the contents of the file.
      * 
      * @param input The editor input containing the serialised source representation of the <code>Scene</code> to add.
      */
     public void addSceneDefinition(final IFileEditorInput input) throws CoreException
     {
         addSceneDefinition(input.getFile().getContents(), input.getFile().getFullPath().toString());
     }
 
     /**
      * <p>
      * Adds a <code>Scene</code> to the <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @param input The <code>InputStream</code> containing the serialised source representation of the <code>Scene</code> to add.
      * @param name The name to give the <code>Scene</code>.
      */
     public void addSceneDefinition(final InputStream input, final String name)
     {
         addSceneDefinition(SceneFactory.loadFromSource(input), name);
     }
 
     /**
      * <p>
      * Adds a <code>Scene</code> to the <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @param scene The <code>Scene</code> to add.
      * @param name The name to give the <code>Scene</code>.
      */
     public void addSceneDefinition(final Scene scene, final String name)
     {
         scenes.put(name, scene);
     }
 
     /**
      * <p>
      * Fires a {@link com.se.simplicity.editor.internal.SceneChangedEvent SceneChangedEvent} to all registered
      * {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s.
      * </p>
      * 
      * @param scene The <code>Scene</code> to fire the event for.
      * @param sceneComponent The component to fire the event for (if the event is component specific).
      * @param type The type of event to fire.
      */
     protected void fireSceneChangedEvent(final Scene scene, final Object sceneComponent, final SceneChangedEventType type)
     {
         SceneChangedEvent event = new SceneChangedEvent(scene, sceneComponent, type);
 
         for (SceneChangedListener listener : sceneChangedListeners)
         {
             listener.sceneChanged(event);
         }
     }
 
     /**
      * <p>
      * Retrieves the currently active <code>Camera</code>.
      * </p>
      * 
      * @return The currently active <code>Camera</code>.
      */
     public Camera getActiveCamera()
     {
         return (activeCamera);
     }
 
     /**
      * <p>
      * Retrieves the currently active <code>Light</code>.
      * </p>
      * 
      * @return The currently active <code>Light</code>.
      */
     public Light getActiveLight()
     {
         return (activeLight);
     }
 
     /**
      * <p>
      * Retrieves the currently active <code>Node</code>.
      * </p>
      * 
      * @return The currently active <code>Node</code>.
      */
     public Node getActiveNode()
     {
         return (activeNode);
     }
 
     /**
      * <p>
      * Retrieves the currently active <code>Scene</code>.
      * </p>
      * 
      * @return The currently active <code>Scene</code>.
      */
     public Scene getActiveScene()
     {
         return (activeScene);
     }
 
     /**
      * <p>
      * Retrieves the <code>Scene</code> with the given ID.
      * </p>
      * 
      * @param id The ID of the <code>Scene</code> to retrieve.
      * 
      * @return The <code>Scene</code> with the given ID,or null if one does not exist.
      */
     public Scene getScene(final String id)
     {
         return (scenes.get(id));
     }
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s registered to listen for changes in the
      * <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @return The {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s registered to listen for changes in the
      * <code>Scene</code>s in use by the editor.
      */
     public List<SceneChangedListener> getSceneChangedListeners()
     {
         return (sceneChangedListeners);
     }
 
     /**
      * <p>
      * Retrieves a new <code>Viewport</code> to the <code>Scene</code> with the given ID.
      * </p>
      * 
      * @param id The ID of the <code>Scene</code> to retrieve a new <code>Viewport</code> to.
      * 
      * @return A new <code>Viewport</code> to the <code>Scene</code> with the given ID.
      */
     public Viewport getViewportToScene(final String id)
     {
         SimpleJOGLViewport viewport = new SimpleJOGLViewport();
         Scene scene = scenes.get(id);
 
         // Retrieve preferred rendering environment if one is available.
         String preferredRenderingEngine = null;
         String preferredRenderer = null;
         if (scene instanceof MetaDataScene)
         {
             MetaDataScene metaDataScene = (MetaDataScene) scene;
             preferredRenderingEngine = (String) metaDataScene.getAttribute("preferredRenderingEngine");
             preferredRenderer = (String) metaDataScene.getAttribute("preferredRenderer");
         }
 
         // Initialise Rendering Engine.
         RenderingEngine renderingEngine = null;
         if (preferredRenderingEngine == null)
         {
             renderingEngine = new SimpleJOGLRenderingEngine();
         }
         else
         {
             try
             {
                 renderingEngine = (RenderingEngine) Class.forName(preferredRenderingEngine).newInstance();
             }
             catch (Exception e)
             {
                 LogFactory.getLog(getClass()).warn("Failed to instantiate preferred Rendering Engine, instantiating default.", e);
                 renderingEngine = new SimpleJOGLRenderingEngine();
             }
         }
         viewport.setRenderingEngine(renderingEngine);
         renderingEngine.setScene(scene);
 
         // Initialise Renderer.
         Renderer renderer = null;
         if (preferredRenderer == null)
         {
             renderer = new SimpleJOGLRenderer();
         }
         else
         {
             try
             {
                 renderer = (Renderer) Class.forName(preferredRenderer).newInstance();
             }
             catch (Exception e)
             {
                 LogFactory.getLog(getClass()).warn("Failed to instantiate preferred Renderer, instantiating default.", e);
                 renderer = new SimpleJOGLRenderer();
             }
         }
         renderingEngine.setRenderer(renderer);
 
         // Initialise current Camera.
         for (Camera camera : scene.getCameras())
         {
             if (camera instanceof MetaDataCamera)
             {
                 String isDefault = (String) ((MetaDataCamera) camera).getAttribute("default");
 
                 if (isDefault != null && isDefault.equals("true"))
                 {
                     renderingEngine.setCamera(camera);
                     break;
                 }
             }
         }
         // If default Camera was not found.
         if (renderingEngine.getCamera() == null)
         {
             LogFactory.getLog(getClass()).warn("Default Camera not found, defaulting to the first Camera found.");
 
             // If no Cameras exist.
             if (scene.getCameras().isEmpty())
             {
                 LogFactory.getLog(getClass()).warn("No Cameras found, creating a default Camera.");
 
                 // Create new Camera and add it to the Scene.
                 MetaDataCamera camera = new MetaDataCamera(new SimpleJOGLCamera());
                 camera.setAttribute("name", "Camera0");
                 SimpleNode node = new SimpleNode();
                 node.getTransformation().translate(new SimpleTranslationVectorf4(0.0f, 0.0f, 10000.0f, 1.0f));
                 camera.setNode(node);
 
                 scene.addCamera(camera);
                 scene.getSceneGraph().addSubgraph(node);
             }
 
             renderingEngine.setCamera(scene.getCameras().get(0));
         }
 
         return (viewport);
     }
 
     /**
      * <p>
      * Notifies all registered {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s that the <code>Node</code> with
      * the given ID in the currently active <code>Scene</code> has been modified.
      * </p>
      * 
      * @param id The ID of the <code>Node</code> the <code>SceneChangedListener</code>s will be notified about.
      */
     public void notifyNodeModified(final int id)
     {
         fireSceneChangedEvent(activeScene, activeScene.getSceneGraph().getNode(id), SceneChangedEventType.NODE_MODIFIED);
     }
 
     /**
      * <p>
      * Notifies all registered {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener}s that the <code>Scene</code> with
      * the given ID has been modified.
      * </p>
      * 
      * @param id The ID of the <code>Scene</code> the <code>SceneChangedListener</code>s will be notified about.
      */
     public void notifySceneModified(final String id)
     {
         fireSceneChangedEvent(scenes.get(id), null, SceneChangedEventType.SCENE_MODIFIED);
     }
 
     /**
      * <p>
      * Unregisters the given {@link com.se.simplicity.editor.internal.SceneChangedListener SceneChangedListener} from listening for changes in the
      * <code>Scene</code>s in use by the editor.
      * </p>
      * 
      * @param sceneChangedListener The <code>SceneChangedListener</code> to unregister.
      */
     public void removeSceneChangedListener(final SceneChangedListener sceneChangedListener)
     {
         sceneChangedListeners.remove(sceneChangedListener);
     }
 
     /**
      * <p>
      * Resets this <code>SceneManager</code> to its initial state.
      * </p>
      */
     public void reset()
     {
         activeCamera = null;
         activeLight = null;
         activeNode = null;
         activeScene = null;
         sceneChangedListeners = new ArrayList<SceneChangedListener>();
         scenes = new HashMap<String, Scene>();
     }
 
     /**
      * <p>
      * Sets the currently active <code>Camera</code>.
      * </p>
      * 
      * @param camera The currently active <code>Camera</code>.
      */
     public void setActiveCamera(final Camera camera)
     {
         if (!activeScene.getCameras().contains(camera))
         {
             throw new IllegalArgumentException("Invalid Camera: The Camera must be in the active Scene.");
         }
 
         activeCamera = camera;
        fireSceneChangedEvent(activeScene, activeCamera, SceneChangedEventType.NODE_ACTIVATED);
     }
 
     /**
      * <p>
      * Sets the currently active <code>Light</code>.
      * </p>
      * 
      * @param light The currently active <code>Light</code>.
      */
     public void setActiveLight(final Light light)
     {
         if (!activeScene.getLights().contains(light))
         {
             throw new IllegalArgumentException("Invalid Light: The Light must be in the active Scene.");
         }
 
         activeLight = light;
        fireSceneChangedEvent(activeScene, activeLight, SceneChangedEventType.NODE_ACTIVATED);
     }
 
     /**
      * <p>
      * Sets the currently active <code>Node</code>.
      * </p>
      * 
      * @param node The currently active <code>Node</code>.
      */
     public void setActiveNode(final Node node)
     {
         if (activeScene.getSceneGraph().getNode(node.getID()) != node)
         {
             throw new IllegalArgumentException("Invalid Node: The Node must be in the active Scene.");
         }
 
         activeNode = node;
         fireSceneChangedEvent(activeScene, activeNode, SceneChangedEventType.NODE_ACTIVATED);
     }
 
     /**
      * <p>
      * Sets the currently active <code>Scene</code>.
      * </p>
      * 
      * @param id The currently active <code>Scene</code>.
      */
     public void setActiveScene(final String id)
     {
         if (scenes.get(id) == null)
         {
             throw new IllegalArgumentException("Invalid Scene: The Scene must already be managed by this Scene Manager.");
         }
 
         activeScene = scenes.get(id);
         fireSceneChangedEvent(activeScene, null, SceneChangedEventType.SCENE_ACTIVATED);
     }
 }
