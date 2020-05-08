 package uk.co.zutty.glarena;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import static uk.co.zutty.glarena.util.MathUtils.degreesToRadians;
 
 /**
  * A game object.
  */
 public abstract class Entity {
 
     protected Vector3f position;
 
     protected float roll;
     protected float pitch;
     protected float yaw;
 
     private Model model;
     private ShaderProgram shader;
     protected Matrix4f matrix;
 
     protected Game game;
 
     protected Entity(Model model, ShaderProgram shader) {
         this.model = model;
         this.shader = shader;
         matrix = new Matrix4f();
         position = new Vector3f();
     }
 
     public void setPosition(float x, float y, float z) {
         position.x = x;
         position.y = y;
         position.z = z;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 
     public void update() {
         matrix.setIdentity();
         matrix.translate(position);
         matrix.rotate(degreesToRadians(pitch), new Vector3f(1, 0, 0));
        matrix.rotate(degreesToRadians(yaw), new Vector3f(0, 1, 0));
        matrix.rotate(degreesToRadians(roll), new Vector3f(0, 0, 1));
     }
 
     public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
         shader.use();
 
         shader.setUniform("projectionMatrix", projectionMatrix);
         shader.setUniform("viewMatrix", viewMatrix);
         shader.setUniform("modelMatrix", matrix);
 
         model.render();
 
         ShaderProgram.useNone();
     }
 
     public void destroy() {
         model.destroy();
     }
 }
