 public class WaveFunc
 implements Func {
     public float valueAt(float x, float z) {
        return (float) (4 * Math.sin(x));
     }
 
     public float[] normalAt(float x, float z) {
         return new float[] {
            (float) (4 * Math.cos(x)), 0, 0
         };
     }
 }
