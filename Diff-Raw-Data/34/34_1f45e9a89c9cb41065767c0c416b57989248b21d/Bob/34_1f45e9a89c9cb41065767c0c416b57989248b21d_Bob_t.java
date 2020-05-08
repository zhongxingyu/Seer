 package nl.sest.gamejam.model.impl;
 
 import nl.sest.gamejam.model.Physical;
 import nl.sest.gamejam.model.Renderable;
 import nl.sest.gamejam.view.Renderer;
 
 /**
  * A bob is a simple-minded drooling thingy which will follow any shiny thing he sees.
  *
  * @author Tim
  * @since 1/25/13 9:40 PM
  */
 public class Bob implements Physical, Renderable {
 
     private Renderer renderer;
     private float x;
     private float y;
 
     public Bob(Renderer aRenderer, float x, float y) {
         this.renderer = aRenderer;
         this.x = x;
         this.y = y;
     }
 
     @Override
     public float getX() {
         return x;
     }
 
     @Override
    public void setX(float x) {
    }

    @Override
     public float getY() {
         return y;
     }
 
     @Override
    public void setY(float y) {
    }

    @Override
    public float getAngle() {
        return 0;
    }

    @Override
    public void setAngle(float angle) {
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public void setDynamic(boolean dynamic) {
    }

    @Override
     public Renderer getRenderer() {
         return renderer;
     }
 }
