 package environment;
 
 import glmodel.GLModel;
 import org.lwjgl.opengl.GL11;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tammy
  * Date: 4/26/13
  * Time: 10:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Model {
     private float scaleRatio = 1.0f;
     private float rotX = 0.0f, rotY = 0.0f, rotZ = 0.0f;
     private float transX = 0.0f, transY = 0.0f, transZ = 0.0f;
     private GLModel model;
 
     public Model(String filename) {
         model = new GLModel(filename);
         model.regenerateNormals();
     }
 
     public Model(String filename, float scaleRatio, float rotX, float rotY, float rotZ,
                  float transX, float transY, float transZ) {
         model = new GLModel(filename);
         this.scaleRatio = scaleRatio;
         this.rotX = rotX;
         this.rotY = rotY;
         this.rotZ = rotZ;
         this.transX = transX;
         this.transY = transY;
         this.transZ = transZ;
         model.regenerateNormals();
     }
 
     public void render() {
         GL11.glTranslatef(transX, transY, transZ);
        if(scaleRatio !=0)
        {
            GL11.glScalef(scaleRatio, scaleRatio, scaleRatio);
        }
         GL11.glRotatef(rotX, 1, 0, 0);
         GL11.glRotatef(rotY, 0, 1, 0);
         GL11.glRotatef(rotZ, 0, 0, 1);
         model.render();
        GL11.glTranslatef(-transX, -transY, -transZ);
        GL11.glRotatef(-rotZ, 0, 0, 1);
        GL11.glRotatef(-rotY, 0, 1, 0);
        GL11.glRotatef(-rotX, 1, 0, 0);
        if(scaleRatio !=0)
        {
            GL11.glScalef(1/scaleRatio, 1/scaleRatio, 1/scaleRatio);
        }
     }
 
     public void updatePosition(float transX, float transY, float transZ) {
         this.transX = transX;
         this.transY = transY;
         this.transZ = transZ;
     }
 
     public void updateRotation(float rotX, float rotY, float rotZ) {
         this.rotX = rotX;
         this.rotY = rotY;
         this.rotZ = rotZ;
     }
 }
