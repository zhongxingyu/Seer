 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkg367proj1;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 public class Cake {
     static int list;
     static int liSphere,liCake, liCandle;
     protected static void setup( GL2 gl2, int width, int height ) {
         gl2.glViewport( 0, 0, width, height );
         gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glEnable(GL.GL_DEPTH_TEST);
         gl2.glLoadIdentity();
         GLU glu = new GLU();
         glu.gluPerspective(90f, width/(float)height, 5, 50);
         //gl2.glOrtho( -10, 10, -10, 10, -10, 10 );
         
         //glu.gluPerspective(60f, width/height, 1, 20);
         gl2.glMatrixMode( GL2.GL_MODELVIEW );
         gl2.glLoadIdentity();
        glu.gluLookAt(  10, 0, 8,  //eye position x,y,z
                         0,  0, 0,   //focus x,y,z
                         0,  0, 1  );//camera up x,y,z
                 
     }
     protected static void init(GL2 gl2){
         initSphere(gl2, 2f);
         liCake = gl2.glGenLists(1);
         initCylinder(gl2, 6f, 4f,new Triple(38,18,7),new Triple(92,66,31), liCake,4);
         liCandle = gl2.glGenLists(1);
         initCylinder(gl2, .25f, 3f,new Triple(183,178,96),new Triple(220,215,145), liCandle,0);
     }
     private static float findRadius(float z, float R){
         //find the radii by using the pythagorean theorem
         float zDist = Math.abs(R - z);
         return (float)Math.pow(Math.pow(R, 2.0)-Math.pow(zDist,2.0),0.5);
     }
     /**
      * Creates a sphere with a top color of (145, 25,27)
      * and a bottom color of (103,21,27)
      */
     protected static void initSphere(GL2 gl, float r){
         liSphere = gl.glGenLists(1);
         gl.glNewList(liSphere, GL2.GL_COMPILE);
         //gl.glLoadIdentity();
         gl.glBegin(GL2.GL_QUAD_STRIP);
         float zScale = r / 10f;
         double rScale = 42.0 / 10.0;
         double gScale = 4.0/10.0;
         double bScale = 0;
         int oldR = -1;
         int oldG = -1;
         int oldB = -1;
         for(int k = 0; k < 20; k++){
             float z1 = (k+1)*zScale;
             float z2 = k*zScale;
             float r1 = findRadius(z1,r);
             float r2 = findRadius(z2,r);
             int red = (int)(rScale * k + 103);
             int green = (int)(gScale * k + 21);
             int blue = 27;
             if(oldR == -1){
                 oldR = red;
                 oldG = green;
                 oldB = blue;
             }
             for(int i=0; i <= 30; i++){
                 double theta = i/30.0*2*Math.PI;
                 float cosine = (float)Math.cos(theta); 
                 float sine = (float)Math.sin(theta);
                 float x = r1 * cosine;
                 float y = r1 * sine;
                 
                 gl.glColor3ub((byte)(red+i%15*4), (byte)(green+i%15*4),(byte)(blue+i%15*4));
                 gl.glVertex3f(x,y,z1);
                 x = (r2) * cosine;
                 y = (r2) * sine;
                 gl.glColor3ub((byte)(oldR+i%15*4), (byte)(oldG+i%15*4),(byte)(oldB+i%15*4));
                 gl.glVertex3f(x,y,z2);
             }
             oldR = red;
             oldG = green;
             oldB = blue;
         }
         gl.glEnd();
         gl.glEndList();
     }
     /**
      * Creates a cylinder, listID must be a valid list ie. listID = gl.glGenLists(1) must be called before using it
      * @param gl
      * @param r
      * @param h 
      */
     protected static void initCylinder(GL2 gl, float r, float h, Triple startColor, Triple endColor, int listID, int colorFactor){
         gl.glNewList(listID, GL2.GL_COMPILE);
         //gl.glLoadIdentity();
         gl.glBegin(GL2.GL_QUAD_STRIP);
         float zScale = h / 20f;
         double rScale = (endColor.R()-startColor.R())/20.0;
         double gScale = (endColor.G()-startColor.G())/20.0;
         double bScale = (endColor.B()-startColor.B())/20.0;
         int oldR = -1;
         int oldG = -1;
         int oldB = -1;
         for(int k = 0; k < 20; k++){
             float z1 = (k+1)*zScale;
             float z2 = k*zScale;
             int red = (int)(rScale * k + startColor.R());
             int green = (int)(gScale * k + startColor.G());
             int blue = (int)(bScale * k + startColor.B());
             if(oldR==-1){
                 oldR = red;
                 oldG = green;
                 oldB = blue;
             }
             System.out.printf("R: %d, G: %d, B: %d\n", red,green,blue);
             for(int i=0; i <= 30; i++){
                 double theta = i/30.0*2*Math.PI;
                 float cosine = (float)Math.cos(theta); 
                 float sine = (float)Math.sin(theta);
                 float x = r * cosine;
                 float y = r * sine;
               
                 gl.glColor3ub((byte)(red+i%15*colorFactor), (byte)(green+i%15*colorFactor),(byte)(blue+i%15*colorFactor));
                 gl.glVertex3f(x,y,z1);
                 gl.glColor3ub((byte)(oldR+i%15*colorFactor), (byte)(oldG+i%15*colorFactor),(byte)(oldB+i%15*colorFactor));
                 gl.glVertex3f(x,y,z2);
             }
             if(k==0 || k ==19){//use triangle fan for the top and bottom
                 float z = 0f;
                 if(k==19){
                     z= z1;
                 }
                 gl.glEnd();
                 gl.glBegin(GL2.GL_TRIANGLE_FAN);
                 gl.glVertex3f(0,0,z);
                 for(int i=0; i <= 30; i++){
                     double theta = i/30.0*2*Math.PI;
                     float cosine = (float)Math.cos(theta); 
                     float sine = (float)Math.sin(theta);
                     float x = r * cosine;
                     float y = r * sine;
               
                     gl.glColor3ub((byte)(red+i%15*(colorFactor*.3)), (byte)(green+i%15*(colorFactor*.3)),(byte)(blue+i%15*(colorFactor*.3)));
                     gl.glVertex3f(x,y,z);
                 }
                 gl.glEnd();
                 gl.glBegin(GL2.GL_QUAD_STRIP);//return to quad strips
             }
             oldR = red;
             oldG = green;
             oldB = blue;
         }
         gl.glEnd();
         gl.glEndList();
         
     }
     protected static void render( GL2 gl ) {
        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
         //gl.glCallList(list);
         gl.glCallList(liCake);
         gl.glPushMatrix();
         gl.glTranslatef(3f, 3f, 4f);
         gl.glScalef(.35f, .35f, .35f);
         //gl.glTranslatef(2f, 2f, 4f);
         gl.glCallList(liSphere);
         gl.glPopMatrix();
         gl.glPushMatrix();
         gl.glTranslatef(-3f, -3f, 4f);
         gl.glScalef(.35f, .35f, .35f);
         //gl.glTranslatef(-2f,-2f,4f);
         gl.glCallList(liSphere);
         gl.glPopMatrix();
         gl.glPushMatrix();
         gl.glTranslatef(-3f, 3f, 4f);
         gl.glScalef(.35f, .35f, .35f);
        // gl.glTranslatef(-2f,2f,4f);
         gl.glCallList(liSphere);
         gl.glPopMatrix();
         gl.glPushMatrix();
         gl.glTranslatef(3f, -3f, 4f);
         gl.glScalef(.35f, .35f, .35f);
         //gl.glTranslatef(2f,-2f,4f);
         gl.glCallList(liSphere);
         gl.glPopMatrix();
         gl.glPushMatrix();
         gl.glTranslatef(0f, 0f, 4f);
 //        gl.glScalef(.05f,.05f,.5f);
         gl.glCallList(liCandle);
         gl.glPopMatrix();
         gl.glFlush();
     }
 }
