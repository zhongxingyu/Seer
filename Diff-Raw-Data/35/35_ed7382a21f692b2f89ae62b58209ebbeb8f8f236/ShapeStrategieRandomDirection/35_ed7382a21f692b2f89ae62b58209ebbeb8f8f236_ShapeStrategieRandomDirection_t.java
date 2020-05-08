 package de.fhro.inf.p3.uebung07;
 
 import java.util.List;
 
 /**
  * Created by felix on 11/13/13.
  */
 public class ShapeStrategieRandomDirection implements ShapeStrategie {
    private final int maxVector;

    public ShapeStrategieRandomDirection(int maxVector) {
        this.maxVector = maxVector;
    }

     @Override
     public void doAction(List<MovingShape> shapes, int maxX, int maxY) {
         for (MovingShape mShape : shapes) {
             mShape.move(maxX, maxY);
             if (Zufall.randomInt(1, 10) == 1) {
                mShape.setSpeed(Zufall.speed(maxVector), Zufall.speed(maxVector));
             }
         }
     }
 }
