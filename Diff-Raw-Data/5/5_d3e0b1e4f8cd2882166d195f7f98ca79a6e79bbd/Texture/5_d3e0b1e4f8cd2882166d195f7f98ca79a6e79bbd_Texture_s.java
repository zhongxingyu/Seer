 package chum.gl;
 
 import chum.util.Log;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
 
 import java.nio.IntBuffer;
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
    Manages a given texture (or multiple textures).
 
    Supports loading textures from resources, or from arbitrary bitmaps.
    Dimensions of Texture images should generally be powers of 2 in both
    directions, though that is not strictly required by all hardware.
 */
 public class Texture {
 
     private int[] tex_ids;
     private int[] res_ids;
     private Bitmap[] load_bitmap;
     private int tex_dim;
 
 
     /** The minimization filter */
     public int minFilter = GL10.GL_LINEAR;
 
     /** The magnification filter */
     public int magFilter = GL10.GL_LINEAR;
 
     /** The texture environment */
     public int texEnv = GL10.GL_MODULATE;
 
     /**
        Create a Texture for managing a single standard 2D texture image
     */
     public Texture() {
        this(1);
     }
 
 
     /**
        Create a Texture for managing a number of standard 2D texture images.
     */
     public Texture(int num_tex) {
         this(num_tex,GL10.GL_TEXTURE_2D);
     }
 
         
     /**
        Create a Texture for managing a number of texture images
     */
    public Texture(int tex_dim,int num_tex) {
         this.tex_dim = tex_dim;
 
         tex_ids = new int[num_tex];
         res_ids = new int[num_tex];
         load_bitmap = new Bitmap[num_tex];
     }
 
 
     /**
        Set the Resource id to be used for the Texture.  When this is done beforehand,
        the init() method will automatically load the texture and prepare it to
        be bound.
     */
     public void setResource(int res_id) {
         setResource(0,res_id);
     }
 
 
     /**
        Set the Resource id to be used for the Texture.  When this is done beforehand,
        the init() method will automatically load the texture and prepare it to
        be bound.
     */
     public void setResource(int num,int res_id) {
         res_ids[num] = res_id;
     }
 
 
 
     /**
        Initialize the texture prior to using it for rendering.  This allocates
        a texture handle for the texture on the GPU.
 
        This will typically be called from a TextureNode or MeshNode.
     */
     public void onSurfaceCreated(RenderContext renderContext) {
         tex_ids[0] = 0; // force redefined...
         define(renderContext.gl10);
 
         for(int num=0; num<res_ids.length; ++num) {
             if ( load_bitmap[num] != null )
                 load(renderContext.gl10, num, load_bitmap[num]);
             else if ( res_ids[num] > 0 )
                 load(renderContext.gl10, num, res_ids[num],renderContext.appContext);
         }
     }
         
 
     /**
        Define the texture
     */
     protected void define(GL10 gl) {
         if ( !isDefined() ) {
             IntBuffer tex_ids_buf = IntBuffer.wrap(tex_ids);
             gl.glGenTextures(tex_ids.length,tex_ids_buf);
         }
     }
 
 
     /**
        @return true if the texture has been defined on the GPU
     */
     public boolean isDefined() {
         return tex_ids[0] > 0;
     }
 
 
     /**
        Load the texture from a specific Resource
     */
     public void load(GL10 gl, int res_id, Context context) {
         load(gl, 0, res_id, context);
     }
 
 
     /**
        Load the texture from a specific resource.
 
        The resource is loaded as a Bitmap, then loaded into the
        texture object on the GPU, then the bitmap is recycled, as
        it is no longer needed.
     */
     public void load(GL10 gl, int num, int res_id, Context context) {
         Resources res = context.getResources();
         Bitmap bmp = BitmapFactory.decodeResource(res,res_id);
         load(gl, num, bmp);
         bmp.recycle();
         this.load_bitmap[num] = null;
     }
 
     
     /**
        Load the texture from the given Bitmap
     */
     public void load(GL10 gl, Bitmap bmp) {
         load(gl, 0, bmp);
     }
 
 
     /**
        Load the texture image data and filters onto the GPU
     */
     protected void load(GL10 gl, int num, Bitmap bmp) {
         load_bitmap[num] = bmp;
         if ( !isDefined() ) return;
 
         gl.glBindTexture(tex_dim, tex_ids[num]);
 
         gl.glTexParameterx(tex_dim, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
         gl.glTexParameterx(tex_dim, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
 
         GLUtils.texImage2D(tex_dim, 0, bmp, 0);
     }
 
 
     /**
        Bind the texture, preparing it to be applied to following rendering
     */
     public void bind(GL10 gl) {
         bind(gl,0);
     }
 
 
     /**
        Bind the texture, preparing it to be applied to following rendering
     */
     public void bind(GL10 gl, int num) {
         gl.glBindTexture(tex_dim, tex_ids[num]);
     }
 
 
     /**
        Unbind the texture
     */
     public void unbind(GL10 gl) {
         unbind(gl,0);
     }
 
 
     /**
        Unbind the texture, so it will no longer be applied
     */
     public void unbind(GL10 gl, int num) {
         gl.glBindTexture(tex_dim, 0);
     }
 
 }   
