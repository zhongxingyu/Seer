 /***************************************************\
 | Draws a transparent background for the pause menu |
 |                                                   |
 | @author David Saxon                               |
 \***************************************************/
 package nz.co.withfire.obliterate.entities.menu;
 
 import android.opengl.Matrix;
 import android.util.Log;
 import nz.co.withfire.obliterate.entities.Entity;
 import nz.co.withfire.obliterate.graphics.drawable.shape2d.Quad2d;
 import nz.co.withfire.obliterate.utilities.Vector2d;
 
 public class PauseMenuBackground extends Entity {
 
     //VARIABLES
     //the position of the background
     private Vector2d pos = new Vector2d();
     //the dimensions of the background
     private Vector2d dim = new Vector2d();
     //the glDimensions
     private Vector2d GLdim = new Vector2d();
     
     //is true once sliding has been completed
     private boolean slideComplete = false;
     //is true when the menu should slide back
     private boolean slideBack = false;
     //is true when once the menu has slide back
     private boolean slideBackComplete = false;
     
     //the image to be drawn
     private Quad2d quad;
     
     //Matrix
     //the model view projection matrix
     private float[] mvpMatrix = new float[16];
     //the translation matrix
     private float[] tMatrix = new float[16];
     
     //CONSTRUCTOR
     /**Creates a new background
     @GLdim the gl screen dimensions*/
     public PauseMenuBackground(Vector2d GLdim) {
         
         this.GLdim.copy(GLdim);
         
         //set the variables
         pos.set(-(2.0f * Math.abs(GLdim.getX())), 0.0f);
         
         dim.set(Math.abs(GLdim.getX()) + (Math.abs(GLdim.getX()) / 100.0f),
             Math.abs(GLdim.getY()) + (Math.abs(GLdim.getY()) / 100.0f));
         
         float quadCoord[] = {   -dim.getX(),  dim.getY(), 0.0f,
                 -dim.getX(), -dim.getY(), 0.0f,
                  dim.getX(), -dim.getY(), 0.0f,
                  dim.getX(),  dim.getY(), 0.0f };
         float colour[] = {  0.0f, 0.0f, 0.0f, 0.85f,
                             0.0f, 0.0f, 0.0f, 0.85f,
                             0.0f, 0.0f, 0.0f, 0.85f,
                             0.0f, 0.0f, 0.0f, 0.85f };
         
         quad = new Quad2d(quadCoord, colour);
     }
     
     //METHODS
     @Override
     public void update() {
         
         if (!slideComplete && pos.getX() < 0.0f) {
             
             //the new pos
             float shiftAmount = pos.getX() + (GLdim.getX() / 15.0f);
             
             //make sure we don't overslide
             if (shiftAmount > 0.0f) {
                 
                 pos.setX(0.0f);
             }
             else {
 
                 pos.setX(shiftAmount);
             }
         }
         else if (!slideComplete) {
             
             slideComplete = true;
         }
         
         if (slideBack) {
             
             pos.setX(pos.getX() - (GLdim.getX() / 15.0f));
             
             if (pos.getX() < -(GLdim.getX() * 2.0f)) {
                 
                 slideBackComplete = true;
             }
         }
     }
     
     @Override
     public void draw(float[] viewMatrix, float[] projectionMatrix) {
         
        //shift into visible range
         Matrix.setIdentityM(tMatrix, 0);
         Matrix.translateM(tMatrix, 0, pos.getX(), pos.getY(), -0.01f);
         
         //multiply the matrix
         Matrix.multiplyMM(mvpMatrix, 0, tMatrix, 0, viewMatrix, 0);
         Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
         
         quad.draw(mvpMatrix);
     }
     
     /**@return the position of the entity*/
     public Vector2d getPos() {
         
         return pos;
     }
     
     /**@return whether sliding back has been completed or not*/
     public boolean slideBackComplete() {
         
         return slideBackComplete;
     }
     
     /**slides the menu back*/
     public void slideBack() {
         
         slideBack = true;
     }
 }
