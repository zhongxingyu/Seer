 /**
  * Created by Silvestrov Ilya
  * Date: Sep 19, 2010
  * Time: 2:10:14 AM
  */
 public class Test {
     public static void main(String[] args) {
        for (int i = 95; i >= 0; i-=15) {
            System.out.println("i=" + i);
            double factor = ((float) (100 - i)) / 100;
            System.out.println("factor=" + factor);
            System.out.println("6*factor=" + 6*factor);
            System.out.println("(int)(6*factor)=" + (int)(6*factor));
            System.out.println("(int)(-6*factor)=" + (int)(-6*factor));
            System.out.println("1*factor=" + (int)(1*factor));
        }
     }
 }
