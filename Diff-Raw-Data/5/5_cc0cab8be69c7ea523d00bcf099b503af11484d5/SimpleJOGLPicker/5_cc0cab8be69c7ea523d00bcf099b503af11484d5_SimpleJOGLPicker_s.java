 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.jogl.picking;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.IntBuffer;
 
 import javax.media.opengl.GL;
 
 import com.se.simplicity.jogl.JOGLComponent;
 import com.se.simplicity.model.Model;
 import com.se.simplicity.model.VertexGroup;
 import com.se.simplicity.model.shape.Shape;
 import com.se.simplicity.picking.Hit;
 import com.se.simplicity.picking.Pick;
 import com.se.simplicity.picking.Picker;
 import com.se.simplicity.picking.event.PickEvent;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.DrawingMode;
 import com.se.simplicity.rendering.engine.RenderingEngine;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.model.ModelNode;
 
 /**
  * <p>
  * Picks from a JOGL rendering environment. This implementation uses only simple picking techniques and properties.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SimpleJOGLPicker implements Picker, JOGLComponent
 {
     /**
      * <p>
      * The default select buffer capacity.
      * </p>
      */
     private static final int DEFAULT_SELECT_BUFFER_CAPACITY = 2048;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.DrawingMode DrawingMode} used to create {@link com.se.simplicity.picking.event.PickEvent PickEvent}s
      * from the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      */
     private DrawingMode fDrawingMode;
 
     /**
      * <p>
      * The JOGL rendering environment.
      * </p>
      */
     private GL fGl;
 
     /**
      * <p>
      * The <code>RenderingEngine</code> that renders the <code>Scene</code> to determine which components will be picked.
      * </p>
      */
     private RenderingEngine fRenderingEngine;
 
     /**
      * <p>
      * The select buffer used by the JOGL rendering environment. Holds details of picked <code>SceneGraph</code> components.
      * </p>
      */
     private IntBuffer fSelectBuffer;
 
     /**
      * <p>
      * The capacity of the select buffer used by the JOGL rendering environment. This capacity determines how much hit data can be retrieved when
      * picking a <code>Scene</code>.
      * </p>
      */
     private int fSelectBufferCapacity;
 
     /**
      * <p>
      * Creates an instance of <code>SimpleJOGLPicker</code>.
      * </p>
      */
     public SimpleJOGLPicker()
     {
         fDrawingMode = DrawingMode.FACES;
         fGl = null;
         fRenderingEngine = null;
         fSelectBuffer = null;
         fSelectBufferCapacity = DEFAULT_SELECT_BUFFER_CAPACITY;
     }
 
     /**
      * <p>
      * Creates a <code>PickEvent</code> for the given select buffer.
      * </p>
      * 
      * <p>
      * It is assumed that names 1..n-1 in the select buffer correspond to the unique identifiers of <code>ModelNode</code>s containing the picked
      * components of the <code>SceneGraph</code> and that name n in the select buffer corresponds to the subset (face/edge/vertex) of the component
      * that was actually picked.
      * </p>
      * 
      * @param scene The <code>Scene</code> that was picked.
      * @param numberOfHits The number of hits returned during the last <code>GL_SELECTION</code> rendering pass.
      * 
      * @return A <code>PickEvent</code> for the given select buffer.
      */
     protected PickEvent createPickEvent(final Scene scene, final int numberOfHits)
     {
         PickEvent event = new PickEvent();
         int bufferIndex = 0;
 
         for (int hitIndex = 0; hitIndex < numberOfHits; hitIndex++)
         {
             Hit hit = new Hit();
             int numberOfNames = fSelectBuffer.get(bufferIndex++);
             hit.setMinimumDistance(fSelectBuffer.get(bufferIndex++));
             hit.setMaximumDistance(fSelectBuffer.get(bufferIndex++));
 
            hit.setNode(scene.getSceneGraph().getNode(fSelectBuffer.get(bufferIndex++)));
 
             Model model = ((ModelNode) hit.getNode()).getModel();
             if (model instanceof VertexGroup)
             {
                hit.setPrimitive(getSubsetVG((VertexGroup) model, fSelectBuffer.get(bufferIndex)));
             }
             else if (model instanceof Shape)
             {
                 hit.setPrimitive(model);
             }
 
             bufferIndex += numberOfNames;
             event.addHit(hit);
         }
 
         return (event);
     }
 
     @Override
     public void dispose()
     {}
 
     /**
      * <p>
      * Retrieves the {@link com.se.simplicity.rendering.DrawingMode DrawingMode} used to create {@link com.se.simplicity.picking.event.PickEvent
      * PickEvent}s from the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      * 
      * @return The <code>DrawingMode</code> used to create <code>PickEvent</code>s from the <code>Scene</code>.
      */
     public DrawingMode getDrawingMode()
     {
         return (fDrawingMode);
     }
 
     @Override
     public GL getGL()
     {
         return (fGl);
     }
 
     /**
      * <p>
      * Retrieves the <code>RenderingEngine</code> that renders the <code>Scene</code> to determine which components will be picked.
      * </p>
      * 
      * @return The <code>RenderingEngine</code> that renders the <code>Scene</code> to determine which components will be picked.
      */
     public RenderingEngine getRenderingEngine()
     {
         return fRenderingEngine;
     }
 
     /**
      * <p>
      * Retrieves the select buffer used by the JOGL rendering environment.
      * </p>
      * 
      * <p>
      * NOTE: This method should only be used to examine the select buffer, not to modify it.
      * </p>
      * 
      * @return The select buffer used by the JOGL rendering environment.
      */
     public IntBuffer getSelectBuffer()
     {
         return (fSelectBuffer);
     }
 
     /**
      * <p>
      * Retrieves the capacity of the select buffer used by the JOGL rendering environment.
      * </p>
      * 
      * @return The capacity of the select buffer used by the JOGL rendering environment.
      */
     public int getSelectBufferCapacity()
     {
         return (fSelectBufferCapacity);
     }
 
     /**
      * <p>
      * Retrieves a subset vertex group containing the rendered primitive at the given index of the given <code>VertexGroup</code>.
      * </p>
      * 
      * @param vertexGroup The <code>VertexGroup</code> to create the subset from.
      * @param index The index of the rendered primitive to contain in the subset.
      * 
      * @return A subset vertex group containing the rendered primitive at the given index of the given <code>VertexGroup</code>.
      */
     protected VertexGroup getSubsetVG(final VertexGroup vertexGroup, final int index)
     {
         VertexGroup subsetVertexGroup = null;
 
         if (fDrawingMode == DrawingMode.VERTICES)
         {
             subsetVertexGroup = vertexGroup.createVertexSubsetVG(index);
         }
         else if (fDrawingMode == DrawingMode.EDGES)
         {
             subsetVertexGroup = vertexGroup.createEdgeSubsetVG(index);
         }
         else if (fDrawingMode == DrawingMode.FACES)
         {
             subsetVertexGroup = vertexGroup.createFaceSubsetVG(index);
         }
 
         return (subsetVertexGroup);
     }
 
     @Override
     public void init()
     {
         fSelectBuffer = ByteBuffer.allocateDirect(fSelectBufferCapacity << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
         fGl.glSelectBuffer(fSelectBuffer.capacity(), fSelectBuffer);
     }
 
     @Override
     public PickEvent pickScene(final Scene scene, final Camera camera, final Pick pick)
     {
         Scene originalScene = fRenderingEngine.getScene();
         Camera originalCamera = fRenderingEngine.getCamera();
 
         fRenderingEngine.setScene(scene);
         fRenderingEngine.setCamera(camera.getPickCamera(pick));
 
         fGl.glRenderMode(GL.GL_SELECT);
 
         fRenderingEngine.advance();
 
         int numberOfHits = fGl.glRenderMode(GL.GL_RENDER);
 
         if (originalScene != null)
         {
             fRenderingEngine.setScene(originalScene);
         }
         if (originalCamera != null)
         {
             fRenderingEngine.setCamera(originalCamera);
         }
 
         return (createPickEvent(scene, numberOfHits));
     }
 
     /**
      * <p>
      * Sets the drawing mode used to pick the {@link com.se.simplicity.scene.Scene Scene}.
      * </p>
      * 
      * @param mode The drawing mode used to pick the {@link com.se.simplicity.scene.Scene Scene}.
      */
     public void setDrawingMode(final DrawingMode mode)
     {
         fDrawingMode = mode;
     }
 
     @Override
     public void setGL(final GL gl)
     {
         fGl = gl;
     }
 
     /**
      * <p>
      * Sets the <code>RenderingEngine</code> that renders the <code>Scene</code> to determine which components will be picked.
      * </p>
      * 
      * @param renderingEngine The <code>RenderingEngine</code> that renders the <code>Scene</code> to determine which components will be picked.
      */
     public void setRenderingEngine(final RenderingEngine renderingEngine)
     {
         fRenderingEngine = renderingEngine;
     }
 
     /**
      * <p>
      * Sets the capacity of the select buffer used by the JOGL rendering environment.
      * </p>
      * 
      * @param selectBufferCapacity The capacity of the select buffer used by the JOGL rendering environment.
      */
     public void setSelectBufferCapacity(final int selectBufferCapacity)
     {
         fSelectBufferCapacity = selectBufferCapacity;
     }
 }
