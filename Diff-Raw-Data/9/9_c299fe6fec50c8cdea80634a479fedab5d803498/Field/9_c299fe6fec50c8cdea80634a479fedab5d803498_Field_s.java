 
 package tajmi.data.som;
 
 import java.util.Iterator;
 import tajmi.data.clusterable.interfaces.som.InitFunc;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import scala.Tuple2;
 
 /**
  * Implements a static 2D field for the SOM projection
  * @author badi
  */
 public class Field<T> implements Iterable<Tuple2<Position, T>> {
 
     List<List<T>> field;
 
 
     public Field (int length, int width) {
         field = new ArrayList<List<T>>(length);
         for (List<T> d : field)
             d = new ArrayList<T>(width);
     }
 
 
     public Field<T> init (List<T> input_data, InitFunc initf) {
         Random rand = new Random(SOMConfig.getInstance().init_random_gen_seed());
 
         for (List<T> d2 : field) {
             for (T datum : d2) {
                 Tuple2< T,Random > res = initf.params(input_data, rand).call();
                 rand = res._2();
                 datum = res._1(); // TODO: this may be a bug
             }
         }
 
         return this;
     }
 
 
     public T get (Position pos) {
        return field.get(pos.x).get(pos.y);
     }
 
     /**
      * @return an interator over a linked list of the elements in the field.
      */
     public Iterator<Tuple2<Position, T>> iterator() {
         // this is ugly: Java needs lazy generators
         List<Tuple2<Position, T>> l = new LinkedList<Tuple2<Position, T>>();
         for (int x = 0; x < field.size(); x++)
             for (int y = 0; y < field.get(x).size(); y++)
                 l.add(
                         new Tuple2<Position, T>(new Position(x, y)
                         , field.get(x).get(y)));
         return l.iterator();
     }
 
 }
