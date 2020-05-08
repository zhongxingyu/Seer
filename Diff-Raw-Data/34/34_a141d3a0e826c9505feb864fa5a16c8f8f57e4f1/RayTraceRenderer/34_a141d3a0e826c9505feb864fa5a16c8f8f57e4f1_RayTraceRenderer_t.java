 package renderer;
 
 import java.util.Properties;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.glu.GLU;
 
 import renderer.raytracer.RayTracer;
 import scene.Scene;
 import util.Colour;
 import util.Vector;
 
 public class RayTraceRenderer extends Renderer {
 	
 	private RayTracer rayTracer;	
 	private int nRows, nCols;	
 
 	public RayTraceRenderer(Scene scene, Camera camera, Properties prop) {
 		super(scene, camera);	
 		nRows = Integer.parseInt(prop.getProperty("canvas.height"));
 	    nCols = Integer.parseInt(prop.getProperty("canvas.width"));	    
 	    rayTracer = new RayTracer(scene);    
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		
 	}
 	
 	
 	
 	@Override
 	public void render(GLAutoDrawable drawable) {
 				
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);		
 		GLU glu = new GLU();
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluOrtho2D(0, nCols, 0, nRows);		
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 	    gl.glLoadIdentity();   	   
 		gl.glDisable(GL2.GL_LIGHTING);
 		
 		// to do: implement
 		//v1
		Vector v1 = new Vector(camera.u);
		Vector temp1 = new Vector(camera.v);
 		float t1 = camera.width/2;
 		float t2 = camera.height/2;
 		v1.mult(-(t1));
 		temp1.mult(t2);
 		v1.add(temp1);
 		
 		//v2
 		
 		//V3
		Vector v3 = new Vector(camera.n);
 		v3.mult(-camera.distance);
 		v3.add(v1);
 		
 		Ray ray = new Ray(camera.eye);
 		Colour col = new Colour();
 		
 		for(int r = 0 ; r < this.nRows ; r++){
 			for(int c = 0 ; c < this.nCols ; c++){
 				//lingse deel
				Vector v2 = new Vector(camera.u);
 				v2.mult(c);
 				float widthPixel = camera.width / this.nCols;
 				v2.mult(widthPixel);
 				//RECHTSE DEEL
				Vector temp = new Vector(camera.v);
 				temp.mult(r);
 				float heightPixel = camera.height/this.nRows;
 				temp.mult(heightPixel);
 				v2.add(temp.getReverse());
 				//v2 is klaar
				Vector direction = new Vector(v3);
 				direction.add(v2);
 				ray.dir = direction;
 				col.set(rayTracer.shade(ray));
 				gl.glColor3f(col.r, col.g, col.b);
 				gl.glRecti(c, r, c+1, r+1);
 			}
 		}
 				
 		gl.glFlush();
 	}
 	
 }
