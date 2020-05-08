 package es.vieites.javafx.slimeclone.slimeland.sprites;
 
 import es.vieites.javafx.slimeclone.engine.AbstractSprite;
 import es.vieites.javafx.slimeclone.slimeland.SlimeUniverse;
 import javafx.event.Event;
 import javafx.scene.shape.Circle;
 
 /**
  * The play toy.
  */
 public class Ball extends AbstractSprite {
     
    private static final double MAX_SPEED_Y = 60;
    private static final double MAX_SPEED_X = 120;
 
     public Ball(double x, double y, double r) {
         setNode(new Circle(r));
         setX(x);
         setY(y);
         setType(EnumSprite.BALL);
     }
 
     @Override
     public void update(Event event) {
         double timeIncrement = 10d / 50d;
         double newX = getX() + getVx() * timeIncrement;
         double newY = getY() + getVy() * timeIncrement + SlimeUniverse.GRAVITY * Math.pow(timeIncrement, 2) / 2d;
         double vyf = getVy() + SlimeUniverse.GRAVITY * timeIncrement;
         double vxf = getVx();
         setX(newX);
         setY(newY);
         setVy(vyf*0.995);
         setVx(vxf*0.995);
         
         if (getVy() > MAX_SPEED_Y) {
             setVy(MAX_SPEED_Y);
         }
         if (getVx() > MAX_SPEED_X) {
             setVx(MAX_SPEED_X);
         }
     }
 
     @Override
     public void collide(AbstractSprite sprite) {
         
     }
 
     public Circle getAsCircle() {
         return (Circle) getNode();
     }
 
 }
