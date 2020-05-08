import java.util.*;

 /**
  * Created by Silvestrov Ilya
  * Date: Sep 19, 2010
  * Time: 2:10:14 AM
  */
 public class Test {
     public static void main(String[] args) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(0);

        ListIterator<Integer> iter = list.listIterator();
        System.out.println(iter.next());

        System.out.println(iter.hasNext());

        iter.add(1);
        iter.add(2);

        System.out.println(Arrays.toString(list.toArray()));
     }
 }
