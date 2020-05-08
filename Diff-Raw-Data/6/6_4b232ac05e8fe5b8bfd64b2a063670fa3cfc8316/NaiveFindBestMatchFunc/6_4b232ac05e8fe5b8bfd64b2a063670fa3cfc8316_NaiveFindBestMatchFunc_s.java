 
 package tajmi.instances.som;
 
 import scala.Tuple2;
 import tajmi.abstracts.DistanceFunc;
 import tajmi.abstracts.som.FindBestMatchFunc;
 import tajmi.som.Field;
 import tajmi.som.Position;
 
 /**
  *
  * @author badi
  */
 public class NaiveFindBestMatchFunc extends FindBestMatchFunc {
 
     public Tuple2<Position, Object> call() {
         // setup
         Field field = getField();
         DistanceFunc distancef = getDistancef();
 
 
         /* implementation: */
 
         double best_distance = Double.POSITIVE_INFINITY,
                 current_distance = Double.POSITIVE_INFINITY;
         Tuple2<Position, Object> best = null;
         for (Tuple2<Position, Object> elem : field) {
             if (best == null) {
                 best = elem;
                 continue;
             } else {
                current_distance = distancef.params(best._2(), elem._2()).call();
                 if (current_distance < best_distance)
                     best = elem;
             }
         }
         return best;
     }
 
 }
