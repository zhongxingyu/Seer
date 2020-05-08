 package edu.hsbremen.animvisu.geom;
 
 import edu.hsbremen.animvisu.util.VectorUtil;
 import static org.lwjgl.opengl.GL11.*;
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  * Represents a Cube for OpenGL.
  * @author Svenja Hilgen
  */
 public class Cube extends AbstractGeometry {
 
 	public Cube() {
             super();
 	}
 	
 	@Override
 	protected void setGeometry() {
             Vector3f p1 = new Vector3f(1,-1,1);
             Vector3f p2 = new Vector3f(1,-1,-1);
             Vector3f p3 = new Vector3f(-1,-1,-1);
             Vector3f p4 = new Vector3f(-1,-1,1);
             Vector3f p5 = new Vector3f(-1,1,1);
             Vector3f p6 = new Vector3f(1,1,1);
             Vector3f p7 = new Vector3f(1,1,-1);
             Vector3f p8 = new Vector3f(-1,1,-1);
             Vector3f normal;
             
             //Zeichnen
             glBegin(GL_QUADS);
             
                 normal = VectorUtil.createNormal(p1, p2, p3);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p4.x,p4.y,p4.z);
                glVertex3f(p3.x,p3.y,p3.z);
                glVertex3f(p2.x,p2.y,p2.z);
                glVertex3f(p1.x,p1.y,p1.z);
 
                 normal = VectorUtil.createNormal(p1, p2, p7);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p1.x,p1.y,p1.z);
                 glVertex3f(p2.x,p2.y,p2.z);
                 glVertex3f(p7.x,p7.y,p7.z);
                 glVertex3f(p6.x,p6.y,p6.z);
 
                 normal = VectorUtil.createNormal(p2, p3, p8);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p2.x,p2.y,p2.z);
                 glVertex3f(p3.x,p3.y,p3.z);
                 glVertex3f(p8.x,p8.y,p8.z);
                 glVertex3f(p7.x,p7.y,p7.z);
 
                 normal = VectorUtil.createNormal(p3, p4, p5);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p3.x,p3.y,p3.z);
                 glVertex3f(p4.x,p4.y,p4.z);
                 glVertex3f(p5.x,p5.y,p5.z);
                 glVertex3f(p8.x,p8.y,p8.z);           
 
                 normal = VectorUtil.createNormal(p4, p1, p6);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p4.x,p4.y,p4.z);
                 glVertex3f(p1.x,p1.y,p1.z);
                 glVertex3f(p6.x,p6.y,p6.z);
                 glVertex3f(p5.x,p5.y,p5.z);           
 
                 normal = VectorUtil.createNormal(p5, p6, p7);
                 glNormal3f(normal.x, normal.y, normal.z);
                 glVertex3f(p5.x,p5.y,p5.z); 
                 glVertex3f(p6.x,p6.y,p6.z);
                 glVertex3f(p7.x,p7.y,p7.z);
                 glVertex3f(p8.x,p8.y,p8.z);
            
             glEnd();
 	}
 
 }
