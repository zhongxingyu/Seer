 package chum.gl.render;
 
 import chum.engine.common.Animation;
 import chum.engine.common.Colorable;
 import chum.engine.common.Movable;
 import chum.engine.common.Rotatable;
 import chum.engine.common.Scalable;
 import chum.f.M4;
 import chum.f.Vec3;
 import chum.gl.Color;
 import chum.gl.Mesh;
 import chum.gl.RenderContext;
 import chum.gl.Text;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 /**
    A TextNode renders a Text string.
 */
 public class TextNode extends MeshNode
     implements Scalable, Movable, Rotatable, Colorable
 {
     
     /** The Text to be rendered */
     public Text text;
 
     /** Optional translation before drawing */
     public Vec3 position;
     
     /** Optional scaling before drawing */
     public float scale = 1f;
 
     /** The rotation angle (degrees 0-360) -- always around z-axis */
     public float angle = 0f;
 
     /** Optional color */
     public Color color;
 
 
     private boolean pushed;
 
 
     /**
        Create a TextNode, initially not displaying any text
     */
     public TextNode() {
         super();
         blend = true;
     }
 
 
     /**
        Create a TextNode to display the given text
     */
     public TextNode(Text text) {
         super();
         blend = true;
         setText(text);
     }
 
     
     /**
        Create a TextNode to displace the given string.
 
        A Text instance is created, but it is created without reference
        to a specific Font.  The Font must be set before any text will
        actually be rendered.
     */
     public TextNode(String str) {
         super();
         blend = true;
         setText(new Text(str));
     }
 
 
     /**
        Set new text
     */
     public void setText(Text text) {
         setMesh(text);
         this.text = text;
     }
 
 
     /**
        Set the color
        @param color the color to set as the current draw color before drawing the text
     */
     public void setColor(Color color) {
         if ( this.color == null ) this.color = new Color();
         this.color.set(color);
     }
 
 
     /**
        Get the current color
      */
     public Color getColor() {
         return this.color;
     }
     
 
     /**
        Set the position of the text
        @param position the vector to translate to before drawing the text
     */
     public void setPosition(Vec3 position) {
         if ( this.position == null ) this.position = new Vec3();
         this.position.set(position);
     }
 
     
     /**
        Get the current position of the text
      */
     public Vec3 getPosition() {
         return this.position;
     }
 
 
     /**
        Set the scale of the text
        @param scale with 1.0 meaning no scaling
     */
     public void setScale(float scale) {
         this.scale = scale;
     }
 
     
     /**
        Get the current scale of the text
      */
     public float getScale() {
         return this.scale;
     }
 
     
     /**
        Set the angle of the text
        @param angle from 0-360
     */
     public void setAngle(float angle) {
         this.angle = angle;
     }
 
     
     /**
        Get the current angle of the text
      */
     public float getAngle() {
         return this.angle;
     }
 
     
     /**
        Set the rotation axis of the text.
        Needed for Rotatable interface, but currently does nothing
      */
     public void setAxis(Vec3 axis) {
         
     }
     
 
     /**
        Get the rotation axis of the text.
        Only rotation around z axis is current supported
      */
     public Vec3 getAxis() {
         return Vec3.Z_AXIS;
     }
 
     
     /** When the surface is created, ensure that the mesh is setup to render */
     @Override
     public void onSurfaceCreated(RenderContext renderContext) {
         super.onSurfaceCreated(renderContext);
         if ( text != null && text.font != null ) setTexture(text.font.texture);
     }
 
 
     /**
        Prepares the render state for drawing the text
     */
     @Override
     public void renderPrefix(GL10 gl) {
         pushed = false;
 
         if ( position != null ) {
             if ( pushed == false ) gl.glPushMatrix();
             gl.glTranslatef(position.x,
                             position.y,
                             position.z);
             pushed = true;
         }
 
         if ( angle != 0f ) {
             if ( pushed == false ) gl.glPushMatrix();
             gl.glRotatef(angle,0f,0f,1f);
             pushed = true;
         }
             
         if ( scale != 1f ) {
             if ( pushed == false ) gl.glPushMatrix();
             gl.glScalef(scale,scale,scale);
             pushed = true;
         }
 
             
         if ( color != null ) {
             gl.glColor4f(color.red,color.green,color.blue,color.alpha);
         }
         
         // Super renderPrefix() is sufficient to actually draw the text mesh
         super.renderPrefix(gl);
     }
 
 
     /**
        Restore the previous drawing state after the text is drawn.
        If a translation or a scaling were a applied, restores the previous
        ModelView matrix
     */
     @Override
     public void renderPostfix(GL10 gl) {
         if ( pushed ) gl.glPopMatrix();
         super.renderPostfix(gl);
     }
 
 
 
     /**
         Update a Boundary with the bounding box for this text + transforms
      */
     public void updateBounds(Mesh.Bounds bounds) {
         bounds.update(text);
         M4 xform = new M4();
         xform.translate(position,xform);
        if ( angle != 0f ) xform.rotate(Vec3.ORIGIN,(float)(angle * Math.PI / 180f), xform);
        if ( scale != 1f ) xform.scale(scale,xform);
         bounds.transform(xform);
     }
 
 
 
     /**
        Scale the text smoothly
        @param start the starting scale factor
        @param end the ending scale factor
        @param duration the duration for the animation (millis)
        @return the new {@link TextAnimation.Scale instance}
     */
     public Animation.Scale animateScale(float start, float end, long duration) {
         Animation.Scale anim = Animation.Scale.obtain();
         anim.scalable = this;
         anim.duration = duration;
         anim.setScale(start,end);
         this.addNode(anim);
         return anim;
     }
 
 
     public Animation.Scale animateScale(float end,long duration) {
         return this.animateScale(this.scale,end,duration);
     }
 
         
 
     /**
        Rotate the text smoothly
        @param start the starting angle (degrees)
        @param end the ending angle (degrees)
        @param duration the duration for the animation (millis)
        @return the new {@link TextAnimation.Angle instance}
     */
     public Animation.Angle animateAngle(float start, float end, long duration) {
         Animation.Angle anim = Animation.Angle.obtain();
         anim.rotatable = this;
         anim.duration = duration;
         anim.setAngle(start,end);
         this.addNode(anim);
         return anim;
     }
 
 
     public Animation.Angle animateAngle(float end,long duration) {
         return this.animateAngle(this.angle,end,duration);
     }
 
 
     /**
        Move the text smoothly
        @param start the starting position
        @param end the ending ending postion
        @param duration the duration for the animation (millis)
        @return the new {@link TextAnimation.Position instance}
     */
     public Animation.Position animatePosition(Vec3 start, Vec3 end, long duration) {
         if ( this.position == null ) this.position = new Vec3();
         Animation.Position anim = Animation.Position.obtain();
         anim.movable = this;
         anim.duration = duration;
         anim.setPosition(start,end);
         this.addNode(anim);
         return anim;
     }
 
 
     public Animation.Position animatePosition(Vec3 end,long duration) {
         if ( this.position == null ) this.position = new Vec3();
         return this.animatePosition(this.position,end,duration);
     }
 
 
     /**
        Change the text color smoothly
        @param start the starting color
        @param end the ending ending color
        @param duration the duration for the animation (millis)
        @return the new {@link TextAnimation.Color instance}
     */
     public Animation.Color animateColor(Color start, Color end, long duration) {
         if ( this.color == null ) this.color = new Color();
         Animation.Color anim = Animation.Color.obtain();
         anim.colorable = this;
         anim.duration = duration;
         anim.setColor(start,end);
         this.addNode(anim);
         return anim;
     }
 
 
     public Animation.Color animateColor(Color end,long duration) {
         if ( this.color == null ) this.color = new Color();
         return this.animateColor(this.color,end,duration);
     }
 
 
     /**
        Change the text alpha smoothly
        @param start the starting alpha
        @param end the ending alpha
        @param duration the duration for the animation (millis)
        @return the new {@link TextAnimation.Color instance}
     */
     public Animation.Color animateAlpha(float start, float end, long duration) {
         if ( this.color == null ) this.color = new Color(Color.BLACK); // TODO: allocate Color from pool
         Animation.Color anim = Animation.Color.obtain();
         anim.colorable = this;
         anim.duration = duration;
         Color startColor = new Color(this.color);
         Color endColor = new Color(this.color);
         startColor.alpha = start;
         endColor.alpha = end;
         anim.setColor(startColor,endColor);
         this.addNode(anim);
         return anim;
     }
 
 
     public Animation.Color animateAlpha(float end,long duration) {
         if ( this.color == null ) this.color = new Color();
         return this.animateAlpha(this.color.alpha,end,duration);
     }
 
 
 
 }
