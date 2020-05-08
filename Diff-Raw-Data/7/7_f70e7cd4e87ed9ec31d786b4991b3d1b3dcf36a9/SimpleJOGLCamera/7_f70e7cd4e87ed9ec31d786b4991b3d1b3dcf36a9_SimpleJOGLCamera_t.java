 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.jogl.rendering;
 
 import javax.media.opengl.GL;
 
 import org.apache.log4j.Logger;
 
 import com.se.simplicity.SEInvalidOperationException;
 import com.se.simplicity.jogl.JOGLComponent;
 import com.se.simplicity.picking.Pick;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.ProjectionMode;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.vector.SimpleTransformationMatrixf44;
 import com.se.simplicity.vector.TransformationMatrixf;
 
 /**
  * <p>
  * A viewpoint within a <code>SceneGraph</code> rendered by a JOGL rendering environment. This implementation uses only simple camera techniques and
  * properties.
  * </p>
  * 
  * <p>
  * The viewing frustum is the shape that contains all components of the <code>SceneGraph</code> displayed when viewing through a
  * <code>SimpleJOGLCamera</code>. The following diagram shows the 'side' and 'front' views of a viewing frustum.
  * </p>
  * 
  * <pre>
  *              _______
  *       /|    |\     /|
  *      / |    | \___/ |
  * _\  |  |    | |   | |
  *  /  |  |    | |___| |
  *      \ |    | /   \ |
  *       \|    |/_____\|
  *   
  *   side        front
  * </pre>
  * 
  * <p>
  * <b>Eye</b>
  * </p>
  * 
  * <p>
 * The eye is the position of the viewer. The 'front' of the frustum in the context of this explanation is the side of the frustum that is closest to
 * the eye. In the 'side' view the eye is shown as the arrow to the left of the frustum.
  * </p>
  * 
  * <p>
  * <b>Near Clipping Plane</b>
  * </p>
  * 
  * <p>
  * The near clipping plane is a plane in the <code>SceneGraph</code> a distance from the eye in front of which the viewer cannot see i.e. all
  * components of the <code>SceneGraph</code> nearer to the eye than the near clipping plane will be clipped (not drawn). The area on the near clipping
  * plane that constitutes a face of the frustum is shown as the short vertical line in the 'side' view and the smaller rectangle in the 'front' view.
  * This face can be thought of as analogous to the screen and is referred to as the frame.
  * </p>
  * 
  * <p>
  * <b>Far Clipping Plane</b>
  * </p>
  * 
  * <p>
  * The far clipping plane is a plane in the <code>SceneGraph</code> a distance from the eye past which the viewer cannot see i.e. all components of
  * the <code>SceneGraph</code> further from the eye than the far clipping plane will be clipped (not drawn). The area on the far clipping plane that
  * constitutes a face of the frustum is shown as the long vertical line in the 'side' view and the larger rectangle in the 'front' view.
  * </p>
  * 
  * <p>
  * <b>Frame Position and Dimensions</b>
  * </p>
  * 
  * <p>
  * The dimensions of the areas of the clipping planes that are used as faces of the frustum are calculated automatically by the
  * <code>SimpleJOGLCamera</code>. The diagonal lines in the diagram extend from the four corners of the far clipping plane to the four corners of the
  * near clipping plane and finally (by default) converge at the eye. The frame can be moved on the <code>x</code> and <code>y</code> axis so that the
  * eye no longer resides at this convergence. Also, the aspect ratio of the frame (4:3 by default) can be changed.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SimpleJOGLCamera implements Camera, JOGLComponent
 {
     /**
      * <p>
      * The default far clipping plane.
      * </p>
      */
     private static final float DEFAULT_FAR_CLIPPING_PLANE = 1000.0f;
 
     /**
      * <p>
      * The default frame aspect ratio.
      * </p>
      */
     private static final float DEFAULT_FRAME_ASPECT_RATIO = 0.75f; // 3:4
 
     /**
      * <p>
      * The default frame width.
      * </p>
      */
     private static final float DEFAULT_FRAME_WIDTH = 0.1f;
 
     /**
      * <p>
      * The default near clipping plane.
      * </p>
      */
     private static final float DEFAULT_NEAR_CLIPPING_PLANE = 0.1f;
 
     /**
      * <p>
      * The distance from the eye past which components of the <code>SceneGraph</code> will be clipped (not drawn).
      * </p>
      */
     private float fFarClippingDistance;
 
     /**
      * <p>
      * The aspect ratio of the frame. An aspect ratio of 3:4 is stored as 3 / 4 (0.75).
      * </p>
      */
     private float fFrameAspectRatio;
 
     /**
      * <p>
      * The width of the frame.
      * </p>
      */
     private float fFrameWidth;
 
     /**
      * <p>
      * The location of the frame on the <code>x</code> axis relative to the location and orientation of this <code>SimpleJOGLCamera</code>.
      * </p>
      */
     private float fFrameX;
 
     /**
      * <p>
      * The location of the frame on the <code>y</code> axis relative to the location and orientation of this <code>SimpleJOGLCamera</code>.
      * </p>
      */
     private float fFrameY;
 
     /**
      * <p>
      * The JOGL rendering environment.
      * </p>
      */
     private GL fGl;
 
     /**
      * The initialisation status. Determines if this <code>SimpleJOGLCamera</code> is initialised.
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
      * The distance from the eye before which components of the <code>SceneGraph</code> will be clipped (not drawn).
      * </p>
      */
     private float fNearClippingDistance;
 
     /**
      * <p>
      * The <code>Node</code> that represents this <code>SimpleJOGLCamera</code>'s location and orientation.
      * </p>
      */
     private Node fNode;
 
     /**
      * <p>
      * The projection mode used to render a <code>SceneGraph</code>.
      * </p>
      */
     private ProjectionMode fProjectionMode;
 
     /**
      * <p>
      * Creates an instance of <code>SimpleJOGLCamera</code>.
      * </p>
      */
     public SimpleJOGLCamera()
     {
         fFarClippingDistance = DEFAULT_FAR_CLIPPING_PLANE;
         fFrameAspectRatio = DEFAULT_FRAME_ASPECT_RATIO;
         fFrameWidth = DEFAULT_FRAME_WIDTH;
         fFrameX = 0.0f;
         fFrameY = 0.0f;
         fIsInitialised = false;
         fLogger = Logger.getLogger(getClass().getName());
         fNearClippingDistance = DEFAULT_NEAR_CLIPPING_PLANE;
         fNode = null;
         fProjectionMode = ProjectionMode.PERSPECTIVE;
     }
 
     @Override
     public void apply()
     {
         if (!fIsInitialised)
         {
             init();
         }
 
         fGl.glMultMatrixf(((SimpleTransformationMatrixf44) getTransformation()).getArray(), 0);
     }
 
     @Override
     public float getFarClippingDistance()
     {
         return (fFarClippingDistance);
     }
 
     @Override
     public float getFrameAspectRatio()
     {
         return (fFrameAspectRatio);
     }
 
     @Override
     public float getFrameHeight()
     {
         return (fFrameWidth * fFrameAspectRatio);
     }
 
     @Override
     public float getFrameWidth()
     {
         return (fFrameWidth);
     }
 
     @Override
     public float getFrameX()
     {
         return (fFrameX);
     }
 
     @Override
     public float getFrameY()
     {
         return (fFrameY);
     }
 
     @Override
     public GL getGL()
     {
         return (fGl);
     }
 
     @Override
     public float getNearClippingDistance()
     {
         return (fNearClippingDistance);
     }
 
     @Override
     public Node getNode()
     {
         return (fNode);
     }
 
     @Override
     public Camera getPickCamera(final Pick pick)
     {
         // Create the Camera to pick with.
         SimpleJOGLCamera pickCamera = new SimpleJOGLCamera();
         pickCamera.setFarClippingDistance(getFarClippingDistance());
         pickCamera.setFrameAspectRatio(getFrameAspectRatio());
         pickCamera.setFrameWidth(pick.getWidth());
         pickCamera.setFrameX(pick.getX() - (getFrameWidth() / 2) + getFrameX());
         pickCamera.setFrameY((getFrameWidth() * getFrameAspectRatio() / 2) - pick.getY() + getFrameY());
         pickCamera.setGL(getGL());
         pickCamera.setNearClippingDistance(getNearClippingDistance());
         pickCamera.setNode(getNode());
 
         return (pickCamera);
     }
 
     @Override
     public ProjectionMode getProjectionMode()
     {
         return (fProjectionMode);
     }
 
     @Override
     public TransformationMatrixf getTransformation()
     {
         if (fNode == null)
         {
             return (null);
         }
 
         TransformationMatrixf transformation = fNode.getAbsoluteTransformation();
 
         try
         {
             transformation.invert();
         }
         catch (SEInvalidOperationException e)
         {
             fLogger.error("Failed to invert the transformation.", e);
         }
 
         return (transformation);
     }
 
     @Override
     public void init()
     {
         if (fProjectionMode == null)
         {
             throw new IllegalStateException("This Camera must have a projection mode to be initialised.");
         }
 
         fGl.glMatrixMode(GL.GL_PROJECTION);
 
         fGl.glLoadIdentity();
 
         if (fProjectionMode == ProjectionMode.ORTHOGONAL)
         {
             fGl.glOrtho(-fFrameWidth / 2 + fFrameX, fFrameWidth / 2 + fFrameX, -fFrameWidth * fFrameAspectRatio / 2 + fFrameY, fFrameWidth
                     * fFrameAspectRatio / 2 + fFrameY, fNearClippingDistance, fFarClippingDistance);
         }
         else if (fProjectionMode == ProjectionMode.PERSPECTIVE)
         {
             fGl.glFrustum(-fFrameWidth / 2 + fFrameX, fFrameWidth / 2 + fFrameX, -fFrameWidth * fFrameAspectRatio / 2 + fFrameY, fFrameWidth
                     * fFrameAspectRatio / 2 + fFrameY, fNearClippingDistance, fFarClippingDistance);
         }
 
         fGl.glMatrixMode(GL.GL_MODELVIEW);
 
         fIsInitialised = true;
     }
 
     @Override
     public boolean isInitialised()
     {
         return (fIsInitialised);
     }
 
     @Override
     public void setFarClippingDistance(final float farClippingDistance)
     {
         fFarClippingDistance = farClippingDistance;
 
         fIsInitialised = false;
     }
 
     /**
      * <p>
      * Sets the aspect ratio of the frame. An aspect ratio of 3:4 is stored as 3 / 4 (0.75). When setting the aspect ratio the frame width is left
      * unchanged. Only the height is changed to meet the new aspect ratio.
      * </p>
      * 
      * @param frameAspectRatio The aspect ratio of the frame.
      */
     @Override
     public void setFrameAspectRatio(final float frameAspectRatio)
     {
         fFrameAspectRatio = frameAspectRatio;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setFrameHeight(final float frameHeight)
     {
         setFrameAspectRatio(frameHeight / fFrameWidth);
     }
 
     @Override
     public void setFrameWidth(final float frameWidth)
     {
         fFrameWidth = frameWidth;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setFrameX(final float frameX)
     {
         fFrameX = frameX;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setFrameY(final float frameY)
     {
         fFrameY = frameY;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setGL(final GL gl)
     {
         fGl = gl;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setInitialised(final boolean isInitialised)
     {
         fIsInitialised = isInitialised;
     }
 
     @Override
     public void setNearClippingDistance(final float nearClippingDistance)
     {
         fNearClippingDistance = nearClippingDistance;
 
         fIsInitialised = false;
     }
 
     @Override
     public void setNode(final Node node)
     {
         fNode = node;
     }
 
     @Override
     public void setProjectionMode(final ProjectionMode projectionMode)
     {
         fProjectionMode = projectionMode;

        fIsInitialised = false;
     }
 }
