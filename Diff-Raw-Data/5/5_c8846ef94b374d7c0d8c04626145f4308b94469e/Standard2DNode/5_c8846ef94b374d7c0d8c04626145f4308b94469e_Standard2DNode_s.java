 package chum.gl.render;
 
 import chum.gl.RenderContext;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
    A RenderNode appropriate for use as the base of the render tree that sets up a standard
   3D environment.

   * Enables depth test
   * Enables face culling, using CCW direction as the front
 */
 public class Standard2DNode extends OrthographicProjection {
 
     public boolean enableTexture2D = true;
 
 
     public Standard2DNode() {
         super();
 
     }
 
 
     /**
        When the surface is being setup, initialize the standard
        3D settings to control the depth buffer, culling, etc
     */
     @Override
     public void onSurfaceCreated(RenderContext renderContext) {
         super.onSurfaceCreated(renderContext);
 
         GL10 gl = renderContext.gl10;
 
         gl.glEnable(GL10.GL_DEPTH_TEST);
         gl.glClearDepthf(1f);
         gl.glDepthFunc(GL10.GL_LEQUAL);
         
         gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 
         if ( enableTexture2D )
             gl.glEnable(GL10.GL_TEXTURE_2D);
     }
 }
 
