 package chum.gl.render;
 
 import chum.f.Vec3;
 import chum.gl.Mesh;
 import chum.gl.RenderContext;
 import chum.gl.SpriteBatch;
 import chum.gl.SpriteBatchBuilder;
 import chum.gl.SpriteSheet;
 import chum.gl.Texture;
 
 import android.graphics.Bitmap;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
  * A Sprite renders a single Sprite. The SpriteNode has positioning info
  * (translate / rotate / scale), whereas the Sprite its
  */
 public class Sprite extends MeshNode {
 
     /** The SpriteBatch to be rendered */
     public SpriteBatch batch;
 
     /** Offset into the batch */
     public int batchOffset;
 
     /** Number of sprites in the batch */
     public int batchCount = 1;
 
     /** Optional translation relative to the batch origin */
     public Vec3 batchPosition;
 
     /** Optional translation before drawing */
     public Vec3 position;
 
     /** Optional scaling before drawing */
     public float scale = 1f;
 
     /** The rotation angle (degrees 0-360) -- always around z-axis */
     public float angle = 0;
 
     private boolean pushed;
 
 
     /**
      * Create a Sprite node, initially not displaying any images
      */
     public Sprite() {
         this(null);
     }
 
 
     /**
      * Create a Sprite node to show sprites from the given image batch
      */
     public Sprite(SpriteBatch batch) {
         this(batch, 0);
     }
 
 
     /**
      * Create a Sprite showing the given image in a batch
      */
     public Sprite(SpriteBatch batch, int offset) {
         this(batch, offset, 1);
     }
 
 
     /**
      * Create a Sprite showing the given set of images
      */
     public Sprite(SpriteBatch batch, int offset, int count) {
         this(batch, offset, count, count == 1);
     }
 
 
     /**
      * Create a Sprite showing the given set of images, accounting for the
      * relative position of the images within the batch.
      */
     public Sprite(SpriteBatch batch, int offset, int count, boolean useBatchOffset) {
         super();
         blend = true;
         this.batch = batch;
         setMesh(batch);
         setImage(offset, count, useBatchOffset);
     }
 
 
     /**
      * Set the specific sprite image(s) to display from the batch.
      */
     public void setImage(int offset, int count, boolean useBatchOffset) {
         this.batchOffset = offset;
         this.batchCount = count;
 
        // 4 vertices per sprite, 6 indices:
        this.offset = offset * 6;
         this.count = count * 6;
 
         // add in a relative translation to account for the sprite's
         // position within the batch.
         if (useBatchOffset) {
             Mesh.Vertex base = new Mesh.Vertex(batch.attributes);
             batch.getVertex(batch.getIndex(this.offset), base);
             batchPosition = new Vec3();
             base.position.scale(-1f, batchPosition);
             if (batchPosition.x == 0 && batchPosition.y == 0 && batchPosition.z == 0)
                 batchPosition = null;
         } else {
             batchPosition = null;
         }
     }
 
 
     /**
      * Set the position of the sprite
      * 
      * @param position
      *            the vector to translate to before drawing the image
      */
     public void setPosition(Vec3 position) {
         if (this.position == null)
             this.position = new Vec3();
         this.position.set(position);
     }
 
 
     /**
      * Set the scale of the sprite
      * 
      * @param scale
      *            where 1.0 means no scaling
      */
     public void setScale(float scale) {
         this.scale = scale;
     }
 
 
     /**
      * Set the angle of the sprite
      * 
      * @param angle
      *            from 0-360
      */
     public void setAngle(float angle) {
         this.angle = angle;
     }
 
 
     /**
      * Prepares the render state for drawing the text
      */
     @Override
     public void renderPrefix(GL10 gl) {
         pushed = false;
 
         if (batchPosition != null) {
             if (pushed == false)
                 gl.glPushMatrix();
             gl.glTranslatef(batchPosition.x, batchPosition.y, batchPosition.z);
             pushed = true;
         }
 
         if (position != null) {
             if (pushed == false)
                 gl.glPushMatrix();
             gl.glTranslatef(position.x, position.y, position.z);
             pushed = true;
         }
 
         if (angle != 0) {
             if (pushed == false)
                 gl.glPushMatrix();
             gl.glRotatef(angle, 0, 0, 1f);
             pushed = true;
         }
 
         if (scale != 1f) {
             if (pushed == false)
                 gl.glPushMatrix();
             gl.glScalef(scale, scale, scale);
             pushed = true;
         }
 
         // Super renderPrefix() is sufficient to actually draw the mesh
         super.renderPrefix(gl);
     }
 
 
     /**
      * Restore the previous drawing state after the text is drawn. If a
      * translation or a scaling were a applied, restores the previous ModelView
      * matrix
      */
     @Override
     public void renderPostfix(GL10 gl) {
         if (pushed)
             gl.glPopMatrix();
         super.renderPostfix(gl);
     }
 
 
     /**
      * Helper to build a sprite that displays a single image from a sheet with
      * only one image (the full bitmap size)
      */
     public static Sprite fromBitmap(Bitmap bitmap, RenderContext renderContext) {
         SpriteSheet sheet = new SpriteSheet(1);
         sheet.setProvider(new Texture.StaticProvider(bitmap));
         sheet.width = bitmap.getWidth();
         sheet.height = bitmap.getHeight();
         sheet.define(0, 0, 0, sheet.width, sheet.height);
 
         SpriteBatch batch = new SpriteBatch(sheet, 1);
         SpriteBatchBuilder builder = new SpriteBatchBuilder(batch);
         builder.add(sheet.data[0], 0, 0);
         builder.build();
 
         Sprite sprite = new Sprite(batch);
         sprite.onSurfaceCreated(renderContext);
         return sprite;
     }
 
 }
