 /**
  * 
  */
 package graphics;
 
 import java.util.Vector;
 
 import javax.media.opengl.GL2;
 
 public class Polygon {
     int textureId;
     Vector<Vertex> verticies;
 
     public Polygon(int textureId, Vertex[] verticies) {
         this.textureId = textureId;
         this.verticies = new Vector<Vertex>(verticies.length);
         this.verticies.copyInto(verticies);
     }
 
     public Polygon(int textureId, java.util.Collection<Vertex> verticies) {
         this.textureId = textureId;
         this.verticies = new Vector<Vertex>(verticies);
     }
 
     public void render(GL2 gl) {
         if (verticies.size() < 2)
             return;
 
         gl.glBindTexture(GL2.GL_TEXTURE_2D, getTexture().getGlTexture());
         gl.glColor4f(1.0f, 1.0f, 1.0f,1.0f);
         if (verticies.size() == 3) {           
             for (Vertex v: verticies){
                 gl.glTexCoord2f(v.u, v.v); gl.glVertex3f(v.x, v.y, v.z);
             }
         } else {
             Vertex a = verticies.get(0);
             
             for (int i = 2; i < verticies.size(); i++) {
                 Vertex b = verticies.get(i - 1);
                 Vertex c = verticies.get(i);
                 
                 gl.glTexCoord2f(a.u, a.v); gl.glVertex3f(a.x, a.y, a.z);
                 gl.glTexCoord2f(b.u, b.v); gl.glVertex3f(b.x, b.y, b.z);
                 gl.glTexCoord2f(c.u, c.v); gl.glVertex3f(c.x, c.y, c.z);
             }
             
         }
 
     }
 
     private Texture getTexture() {
         return Texture.findById(textureId);
     }
 }
