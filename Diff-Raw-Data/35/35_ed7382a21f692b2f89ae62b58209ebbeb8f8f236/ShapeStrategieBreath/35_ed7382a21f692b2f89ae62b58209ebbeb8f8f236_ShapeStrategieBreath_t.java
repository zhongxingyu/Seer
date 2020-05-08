 package de.fhro.inf.p3.uebung07;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by felix on 11/14/13.
  */
 public class ShapeStrategieBreath implements ShapeStrategie {
    private final int maxSize;
 
    private static Map<Kugel, Boolean> kugeln = new HashMap<Kugel, Boolean>();

    public ShapeStrategieBreath(int maxSize) {
        this.maxSize = maxSize;
    }
 
     @Override
     public void doAction(List<MovingShape> shapes, int maxX, int maxY) {
         for (MovingShape mShape : shapes) {
             mShape.move(maxX, maxY);
 
             Kugel kugel = (Kugel) mShape.getShape();
 
             if (kugeln.get(kugel) == null)
                 kugeln.put(kugel, (Zufall.randomInt(0, 2) == 1));
 
            if (kugel.getSize() >= maxSize - 1)
                 kugeln.put(kugel, false);
             if (kugel.getSize() <= 0)
                 kugeln.put(kugel, true);
 
             // kugel wachsen lassen
             if (kugeln.get(kugel)) {
                 kugel = KugelFactory.getInstance().createKugel(kugel.getColor(), kugel.getSize() + 1);
                 kugeln.put(kugel, true);
                 mShape.setShape(kugel);
             }
             // kugel schrumpfen lassen
             if (!kugeln.get(kugel)) {
                 kugel = KugelFactory.getInstance().createKugel(kugel.getColor(), kugel.getSize() - 1);
                 kugeln.put(kugel, false);
                 mShape.setShape(kugel);
             }
         }
     }
 }
