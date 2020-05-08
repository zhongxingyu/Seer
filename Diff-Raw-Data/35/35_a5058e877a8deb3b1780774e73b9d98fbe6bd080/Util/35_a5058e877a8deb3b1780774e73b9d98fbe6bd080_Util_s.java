 package util;
 
 import static opengl.GL.*;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 import main.SolSystem;
 
 import opengl.GL;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 
 /**
  *
  * @author Sascha Kolodzey, Nico Marniok
  */
 public class Util {
     public static final FloatBuffer MAT_BUFFER = BufferUtils.createFloatBuffer(16);
     public static final float PI = (float)Math.PI;
     public static final float PI_DIV2 = 0.5f * (float)Math.PI;
     public static final float PI_DIV4 = 0.25f * (float)Math.PI;
     public static final float PI_MUL2 = 2.0f * (float)Math.PI;
     
     /**
      * Erzeugt eine Viewmatrix aus Augenposition und Fokuspunkt.
      * @param eye Die Position des Auges
      * @param at Anvisierter Punkt
      * @param up Up Vektor des Auges
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f lookAtRH(Vector3f eye, Vector3f at, Vector3f up, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         
         Vector3f viewDir = Vector3f.sub(at, eye, null);
         viewDir.normalise();
         
         Vector3f side = Vector3f.cross(viewDir, up, null);
         side.normalise();
         
         Vector3f newUp = Vector3f.cross(side, viewDir, null);
         newUp.normalise();
         
         dst.m00 = side.x;     dst.m10 = side.y;     dst.m20 = side.z;     dst.m30 = -Vector3f.dot(eye, side);
         dst.m01 = newUp.x;    dst.m11 = newUp.y;    dst.m21 = newUp.z;    dst.m31 = -Vector3f.dot(eye, newUp);
         dst.m02 = -viewDir.x; dst.m12 = -viewDir.y; dst.m22 = -viewDir.z; dst.m32 = Vector3f.dot(eye, viewDir);
         dst.m03 = 0.0f;       dst.m13 = 0.0f;       dst.m23 = 0.0f;       dst.m33 = 1.0f;
         
         return dst;
     }
     
     /**
      * Erzeugt eine perspektivische Projektionsmatrix, die dem zweiten Ansatz
      * der Vorlesung entspricht. (Vorl. vom 29.05.2012, Folie 16)
      * @param l -x Wert der Viewpane
      * @param r +x Wert der Viewpane
      * @param b -y Wert der Viewpane
      * @param t +y Wert der Viewpane
      * @param n -z Wert der Viewpane
      * @param f +z Wert der Viewpane
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f frustum(float l, float r, float b, float t, float n, float f, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         
         dst.m00 = 2.0f*n/(r-l); dst.m10 = 0.0f;         dst.m20 = (r+l)/(r-l);  dst.m30 = 0.0f;
         dst.m01 = 0.0f;         dst.m11 = 2.0f*n/(t-b); dst.m21 = (t+b)/(t-b);  dst.m31 = 0.0f;
         dst.m02 = 0.0f;         dst.m12 = 0.0f;         dst.m22 = -(f+n)/(f-n); dst.m32 = -2.0f*n*f/(f-n);
         dst.m03 = 0.0f;         dst.m13 = 0.0f;         dst.m23 = -1.0f;        dst.m33 = 0.0f;
         
         return dst;
     }
     
     /**
      * Erzeugt die orthogonale Projektionsmatrix, die dem klassichen Ansatz
      * entspricht.
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f ortho(Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         dst.m22 = 0.0f;
         return dst;
     }
     
     /**
      * Erzeugt eine orthogonal Projektionsmatrix, die dem zweiten Ansatz der
      * Vorlesung entspricht. (Vorl. vom 29.05.2012, Folie 10)
      * @param l minimaler Wert in x-Richtung
      * @param r maximaler Wert in x-Richtung
      * @param b minimaler Wert in y-Richtung
      * @param t maximaler Wert in y-Richtung
      * @param n minimaler Wert in z-Richtung
      * @param f maximaler Wert in z-Richtung
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f ortho(float l, float r, float b, float t, float n, float f, Matrix4f dst) {
         return Util.mul(dst, Util.scale(new Vector3f(2.0f / (r - l), 2.0f / (t - b), -2.0f / (f - n)), null),
                              Util.translation(new Vector3f(-0.5f * (r + l), -0.5f * (t + b), 0.5f * (f + n)), null));
     }
     
     /**
      * Erzeugt eine Rotationsmatrix um die x-Achse.
      * @param angle Winkel in Bogenass
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f rotationX(float angle, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         dst.m11 = dst.m22 = (float)Math.cos(angle);
         dst.m21 = -(dst.m12 = (float)Math.sin(angle));
         return dst;
     }
     
     /**
      * Erzeugt eine Rotationsmatrix um die y-Achse.
      * @param angle Winkel in Bogenass
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f rotationY(float angle, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         
         dst.m00 = dst.m22 = (float)Math.cos(angle);
         dst.m02 = -(dst.m20 = (float)Math.sin(angle));
         
         return dst;
     }
     
     /**
      * Erzeugt eine Rotationsmatrix um die z-Achse.
      * @param angle Winkel in Bogenass
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */    
     public static Matrix4f rotationZ(float angle, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         
         dst.m00 = dst.m11 = (float)Math.cos(angle);
         dst.m10 = -(dst.m01 = (float)Math.sin(angle));
         
         return dst;
     }
     
     /**
      * Erzeugt eine Translationsmatrix.
      * @param translation Der Translationsvektor
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f translation(Vector3f translation, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         
         dst.m30 = translation.x;
         dst.m31 = translation.y;
         dst.m32 = translation.z;
         
         return dst;
     }
     
     /**
      * Erzeugt eine Translationsmatrix in x-Richtung.
      * @param x Der Translationslaenge
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f translationX(float x, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         dst.m30 = x;
         return dst;
     }
     
     /**
      * Erzeugt eine Translationsmatrix in y-Richtung.
      * @param y Der Translationslaenge
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f translationY(float y, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         dst.m31 = y;
         return dst;
     }
     
     /**
      * Erzeugt eine Translationsmatrix in z-Richtung.
      * @param z Der Translationslaenge
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f translationZ(float z, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         dst.m32 = z;
         return dst;
     }
     
     /**
      * Erzeugt eine Skalierungsmatrix.
      * @param scale Skalierungskomponente
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f scale(Vector3f scale, Matrix4f dst) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         
         dst.m00 = scale.x;
         dst.m11 = scale.y;
         dst.m22 = scale.z;
         
         return dst;
     }
     
     /**
      * Erzeugt eine gleichmaessige Skalierungsmatrix.
      * @param scale Skalierungskomponente
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @return Ergebnismatrix
      */
     public static Matrix4f scale(float scale, Matrix4f dst) {
         return Util.scale(new Vector3f(scale, scale, scale), dst);
     }
     
     /**
      * Transformiert einen Vector3f mittels einer Matrix4f. Der Vektor wird um
      * die homogene Koordinate 1 erweitert und anschliessend homogenisiert.
      * @param left Trabsformationsmatrix
      * @param right Zu transformierender Vektor
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */
     public static Vector3f transformCoord(Matrix4f left, Vector3f right, Vector3f dst) {
         if(dst == null) dst = new Vector3f();
         Vector4f vec = Matrix4f.transform(left, new Vector4f(right.x, right.y, right.z, 1.0f), null);
         vec.scale(1.0f / vec.w);
         dst.set(vec.x, vec.y, vec.z);
         return dst;
     }
     
     /**
      * Transformiert einen Vector3f mittels einer Matrix4f. Der Vektor wird um
      * die homogene Koordinate 0 erweitert.
      * @param left Trabsformationsmatrix
      * @param right Zu transformierender Vektor
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */
     public static Vector3f transformDir(Matrix4f left, Vector3f right, Vector3f dst) {
         if(dst == null) dst = new Vector3f();
         Vector4f vec = Matrix4f.transform(left, new Vector4f(right.x, right.y, right.z, 0.0f), null);
         dst.set(vec.x, vec.y, vec.z);
         return dst;
     }
     
     /**
      * Multipliziert beliebig viele Matrizen miteinander.
      * @param dst Matrix, in die das Ergebnis gespeichert wird. Wenn null wird
      * eine neue erstellt.
      * @param factors Matrizen, die multipliziert werden sollen
      * @return Ergebnismatrix
      */
     public static Matrix4f mul(Matrix4f dst, Matrix4f ...factors) {
         if(dst == null) dst = new Matrix4f();
         dst.setIdentity();
         for(Matrix4f mat : factors) {
             Matrix4f.mul(dst, mat, dst);
         }
         return dst;
     }
     
     /**
      * Schneidet einen Wert zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @return Falls val &lt; min, dann min. Falls val &gt; max, dann max. sonst
      * val.
      */
     public static float clamp(float val, float min, float max) {
         return Math.max(min, Math.min(val, max));
     }
     
     /**
      * Schneidet einen Vektor komponentenweise zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */
     public static Vector2f clamp(Vector2f val, Vector2f min, Vector2f max, Vector2f dst) {
         if(dst == null) dst = new Vector2f();
         dst.x = clamp(val.x, min.x, max.x);
         dst.y = clamp(val.y, min.y, max.y);
         return dst;
     }
     
     /**
      * Schneidet einen Vektor zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */    
     public static Vector2f clamp(Vector2f val, float min, float max, Vector2f dst) {
         return clamp(val, new Vector2f(min, min), new Vector2f(max, max), dst);
     }
     
     /**
      * Schneidet einen Vektor komponentenweise zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */    
     public static Vector3f clamp(Vector3f val, Vector3f min, Vector3f max, Vector3f dst) {
         if(dst == null) dst = new Vector3f();
         dst.x = clamp(val.x, min.x, max.x);
         dst.y = clamp(val.y, min.y, max.y);
         dst.z = clamp(val.z, min.z, max.z);
         return dst;
     }
     
     /**
      * Schneidet einen Vektor zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */    
     public static Vector3f clamp(Vector3f val, float min, float max, Vector3f dst) {
         return clamp(val, new Vector3f(min, min, min), new Vector3f(max, max, max), dst);
     }
     
     /**
      * Schneidet einen Vektor komponentenweise zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */    
     public static Vector4f clamp(Vector4f val, Vector4f min, Vector4f max, Vector4f dst) {
         if(dst == null) dst = new Vector4f();
         dst.x = clamp(val.x, min.x, max.x);
         dst.y = clamp(val.y, min.y, max.y);
         dst.z = clamp(val.z, min.z, max.z);
         dst.w = clamp(val.w, min.w, max.w);
         return dst;
     }
     
     /**
      * Schneidet einen Vektor zurecht.
      * @param val Wert
      * @param min minimaler Wert
      * @param max maximaler Wert
      * @param dst Vektor, in den das Ergebnis gespeichert wird. Wenn null wird
      * ein neuer erstellt.
      * @return Ergebnisvektor
      */    
     public static Vector4f clamp(Vector4f val, float min, float max, Vector4f dst) {
         return clamp(val, new Vector4f(min, min, min, min), new Vector4f(max, max, max, max), dst);
     }
     
     /**
      * Erzeugt ein gleichmaessiges 2D n-Eck in der xy-Ebene. (n Indizes, als
      * GL_LINE_LOOP)
      * @param n Anzahl der Ecken
      * @return VertexArrayObject ID
      */
     public static int createNGon(int n) {        
         int vaid = glGenVertexArrays();
         glBindVertexArray(vaid);        
         
         // indexbuffer
         IntBuffer indexData = BufferUtils.createIntBuffer(n);
         for(int i=0; i < n; ++i) {
             indexData.put(i);
         }
         indexData.flip();
         
         int indexBufferID = glGenBuffers();
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW); 
         
         // vertexbuffer
         FloatBuffer vertexData = BufferUtils.createFloatBuffer(3*n + 3*n); // world coords + normal coords
         double phi = 0;
         double deltaPhi = 2.0*Math.PI / (double)n;
         for(int i=0; i < n; ++i) {
             vertexData.put(0.5f*(float)Math.cos(phi));   // position x
             vertexData.put(0.5f*(float)Math.sin(phi));   // position y
             vertexData.put(0.5f*0.0f);                   // position z
             vertexData.put((float)Math.cos(phi));   // normal x
             vertexData.put((float)Math.sin(phi));   // normal y
             vertexData.put(0.0f);                   // normal z
             phi += deltaPhi;
         }
         vertexData.position(0);
                 
         int vertexBufferID = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
         glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
         
          // vs_in_pos  
         glEnableVertexAttribArray(ATTR_POS);
         glVertexAttribPointer(ATTR_POS, 3, GL_FLOAT, false, 24, 0);
          // vs_in_normal
         glEnableVertexAttribArray(ATTR_NORMAL);
         glVertexAttribPointer(ATTR_NORMAL, 3, GL_FLOAT, false, 24, 12);        
         
         return vaid;
     }
     
     /**
      * Erzeugt ein Dreieck in der xy-Ebene. (3 Indizes)
      * @return VertexArrayObject ID
      */
     public static int createTriangle() {
         int vaid = glGenVertexArrays();
         glBindVertexArray(vaid);        
         
         // vertexbuffer
         FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*3); // color, world coords
         vertexData.put(new float[] {
             0.4f, 1.0f, 1.0f, 1.0f,  -1.0f, -1.0f, 0.0f,
             0.4f, 1.0f, 1.0f, 1.0f,  +1.0f, -1.0f, 0.0f,
             0.4f, 0.4f, 1.0f, 1.0f,   0.0f, +1.0f, 0.0f,
         });
         vertexData.position(0);
                 
         int vertexBufferID = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
         glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
         
         // vs_in_color
         glEnableVertexAttribArray(ATTR_COLOR);
         glVertexAttribPointer(ATTR_COLOR, 4, GL_FLOAT, false, (3+4)*4, 0);
          // vs_in_pos  
         glEnableVertexAttribArray(ATTR_POS);
         glVertexAttribPointer(ATTR_POS, 3, GL_FLOAT, false, (3+4)*4, 4*4);
         
         return vaid;        
     }
     
     /**
      * Erzeugt ein Vierexk in der xy-Ebene. (4 Indizes)
      * @return VertexArrayObject ID
      */
     public static int createQuad() {        
         int vaid = glGenVertexArrays();
         glBindVertexArray(vaid);        
         
         // vertexbuffer
         FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*4); // world coords, color
         vertexData.put(new float[] {
             -1.0f, -1.0f, 0.0f,  1.0f, 1.0f, 0.4f, 1.0f,
             +1.0f, -1.0f, 0.0f,  0.4f, 1.0f, 0.4f, 1.0f,
             +1.0f, +1.0f, 0.0f,  1.0f, 1.0f, 0.4f, 1.0f,
             -1.0f, +1.0f, 0.0f,  0.4f, 1.0f, 0.4f, 1.0f,
         });
         vertexData.position(0);
                 
         int vertexBufferID = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
         glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
         
          // vs_in_pos  
         glEnableVertexAttribArray(ATTR_POS);
         glVertexAttribPointer(ATTR_POS, 3, GL_FLOAT, false, (3+4)*4, 0);       
         // vs_in_color
         glEnableVertexAttribArray(ATTR_COLOR);
         glVertexAttribPointer(ATTR_COLOR, 4, GL_FLOAT, false, (3+4)*4, 3*4);
         
         return vaid;
     }
     
     /**
      * Erzeugt eine Kugel.
      * @param r Radius der Kugel
      * @param n vertikale Unterteilung
      * @param k horizontale Unterteilung
      * @param imageFile Pfad zu einer Bilddatei
      * @return Geometrie der Kugel
      */
     public static Geometry createSphere(float r, int n, int k, String imageFile) {
     	Geometry sphere = new Geometry(); //create new Geormetry for the sphere
     	float[] vertices= new float[(k*n+2)*6]; // k*n vertices *6 floats per vertex and north and south pole vertices 
     	float[][][] image = getImageContents(imageFile); //[y][x][color]  0 < y < image.height 0 < x < image.width
     	float dTheta = Util.PI / (k+1); //angle between z and horizontal scale
     	float dPhi = Util.PI_MUL2 / (n); //angle between x and vetical scale
     	int xcol = 0, ycol = 0, count = 0; //xcol/ycol = x/y coordinate for color
     	float x = 0f, y = 0f, z = 0f, red = 0f, green = 0f, blue = 0f; //coordinates and color attributes
     	/*
     	 * x = r*sin(theta)*cos(phi) 
     	 * y = r*cos(theta)
     	 * z = r*sin(theta)*sin(phi)
     	 * 
     	 * 0 <= theta <= pi ; 0 <= phi < 2*pi
     	 * theta = angle between z-Axis and r (k)
     	 * phi = angle between pos. x-axis and r (n)
     	 * 
     	 */
     	float xcolscale = image[0].length / (float)n;
     	float ycolscale = image.length / (float)k;
     	int countindex = 0;
     	vertices[count++] = 0; //northpole vertex
     	vertices[count++] = r;
     	vertices[count++] = 0;
     	vertices[count++] = image[0][0][0];
     	vertices[count++] = image[0][0][1];
     	vertices[count++] = image[0][0][2];
     	
     	int[] index = new int[k*n*2+k*4];
     	for(int i = 0; i < k; i++)
     	{
     		index[countindex++] = 0;
     		for(int j = 0; j < n; j++)
     		{
     			x = (float) (r*Math.sin(dTheta+dTheta*j)*Math.cos(dPhi*i));
     			y = (float) (r*Math.cos(dTheta+dTheta*j));
     			z = (float) (r*Math.sin(dTheta+dTheta*j)*Math.sin(dPhi*i));
 
     			xcol = (int)(xcolscale * i);
     			ycol = (int)(ycolscale * j);
 
     			red = image[ycol][xcol][0];
     			green = image[ycol][xcol][1];
     			blue = image[ycol][xcol][2];
     			
     			vertices[count++] = x;
     			vertices[count++] = y;
     			vertices[count++] = z;
     			vertices[count++] = red;
     			vertices[count++] = green;
     			vertices[count++] = blue;
     			
    			//TODO: Index isn't quite correct
    			index[countindex++] = j + n*i +1;
     			if(i < (k-1)){
    				index[countindex++] = j + n*(i+1) +1;
     			}
     			else{
    				index[countindex++] = j +1;
     			}
     			
     		}
     		index[countindex++] = k*n+1;
     		index[countindex++] = -1;
     	}    	
     	
     	vertices[k*n*6+6] = 0; //southpole vertex
     	vertices[k*n*6+7] = -r;
     	vertices[k*n*6+8] = 0;
     	vertices[k*n*6+9] = image[image.length-1][image[0].length-1][0];
     	vertices[k*n*6+10] = image[image.length-1][image[0].length-1][1];
     	vertices[k*n*6+11] = image[image.length-1][image[0].length-1][2];
     	
    	// TODO: Add right indexbuffer for triangles
    	/*for(int i = 0; i < k*n+2; i++)
    	{
    		index[i] = i;
    	}*/
    	FloatBuffer indiBuffer = BufferUtils.createFloatBuffer(n*k*6+2*6);
     	IntBuffer indexBuffer = BufferUtils.createIntBuffer(k*n*2+k*4);
     	indexBuffer.put(index);
     	indexBuffer.position(0);
    	indiBuffer.put(vertices);
    	indiBuffer.position(0);
    	sphere.setVertices(indiBuffer);
     	sphere.setIndexBuffer(indexBuffer, GL.GL_TRIANGLE_STRIP);
         return sphere;
     }
     
     /**
      * Liest den Inhalt einer Datei und liefert ihn als String zurueck.
      * @param filename Pfad der Datei
      * @return Inhalt der Datei
      */
     private static String getFileContents(String filename) {
         BufferedReader reader = null;
         String source = null;
         try {
             reader = new BufferedReader(new FileReader(filename));
             source = "";
             String line = null;
             while((line = reader.readLine()) != null) {
                 source += line + "\n";
             }
         } catch (IOException ex) {
             Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 reader.close();
             } catch (IOException ex) {
                 Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return source;
     }
     
     /**
      * Attribut Index von vs_in_pos
      */
     public static final int ATTR_POS = 0;
 
     /**
      * Attribut Index von vs_in_normal
      */
     public static final int ATTR_NORMAL = 1;
 
     /**
      * Attribut Index von vs_in_color
      */
     public static final int ATTR_COLOR = 2;
     
     /**
      * Erzeugt ein ShaderProgram aus einem Vertex- und Fragmentshader.
      * @param vs Pfad zum Vertexshader
      * @param fs Pfad zum Fragmentshader
      * @return ShaderProgram ID
      */
     public static int createShaderProgram(String vs, String fs) {
         int programID = glCreateProgram();
         
         int vsID = glCreateShader(GL_VERTEX_SHADER);
         int fsID = glCreateShader(GL_FRAGMENT_SHADER);
         
         glAttachShader(programID, vsID);
         glAttachShader(programID, fsID);
         
         String vertexShaderContents = Util.getFileContents(vs);
         String fragmentShaderContents = Util.getFileContents(fs);
         
         glShaderSource(vsID, vertexShaderContents);
         glShaderSource(fsID, fragmentShaderContents);
         
         glCompileShader(vsID);
         glCompileShader(fsID);
         
         String log;
         log = glGetShaderInfoLog(vsID, 1024);
         System.out.print(log);
         log = glGetShaderInfoLog(fsID, 1024);
         System.out.print(log);
         
         glBindAttribLocation(programID, ATTR_POS, "vs_in_pos");
         glBindAttribLocation(programID, ATTR_NORMAL, "vs_in_normal");        
         glBindAttribLocation(programID, ATTR_COLOR, "vs_in_color");        
         
         glLinkProgram(programID);        
         
         log = glGetProgramInfoLog(programID, 1024);
         System.out.print(log);
                 
         return programID;
     }  
     
     /**
      * Laedt ein Bild und speichert die einzelnen Bildpunke in einem
      * 2-dimensionalen float-Array. Die erste Koordinate ist die y-Position und
      * liegt zwischen 0 und der Hoehe des Bildes - 1. Die zweite Koordinate ist
      * die x-Position und liegt zwischen 0 und der Breite des Bildes. Die dritte
      * Koordinate ist die Farbkomponente des Bildpunktes und ist 0 (rot), 1
      * (gruen) oder 2 (blau).
      * @param imageFile Pfad zur Bilddatei
      * @return Bild enthaltendes float-Array
      */
     public static float[][][] getImageContents(String imageFile) {
         File file = new File(imageFile);
         if(!file.exists()) {
             throw new IllegalArgumentException(imageFile + " does not exist");
         }
         try {
             BufferedImage image = ImageIO.read(file);
             float[][][] result = new float[image.getHeight()][image.getWidth()][3];
             for(int y=0; y < image.getHeight(); ++y) {
                 for(int x=0; x < image.getWidth(); ++x) {
                     Color c = new Color(image.getRGB(image.getWidth() - 1 - x, y));
                     result[y][x][0] = (float)c.getRed() / 255.0f;
                     result[y][x][1] = (float)c.getGreen() / 255.0f;
                     result[y][x][2] = (float)c.getBlue() / 255.0f;
                 }
             }
             return result;
         } catch (IOException ex) {
             Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 }
