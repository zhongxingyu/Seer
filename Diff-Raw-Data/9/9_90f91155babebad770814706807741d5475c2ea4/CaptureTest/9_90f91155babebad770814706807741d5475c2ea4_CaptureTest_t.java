 package ru.frostman.jdk8.demo.lambda;
 
 import org.junit.Test;
 
 import java.util.Comparator;
 
 import static ru.frostman.jdk8.demo.lambda.LambdaTest.checkCmp;
 
 /**
  * based on https://github.com/shipilev/jdk8-lambda-samples
  *
  * @author slukjanov
  */
 public class CaptureTest {
 
     @Test
     public void testLegacy() {
        // + effective final
         final int minus_one = -1;
         final int zero = 0;
         final int one = 1;
 
         Comparator<Integer> cmp = new Comparator<Integer>() {
             @Override
             public int compare(Integer x, Integer y) {
                 return (x < y) ? minus_one : ((x > y) ? one : zero);
             }
 
         };
 
         checkCmp(cmp);
     }
 
     @Test
     public void testLambda() {
         int minusOne = -1;
         int zero = 0;
         int one = 1;
 
         Comparator<Integer> cmp = (x, y) -> (x < y) ? minusOne : ((x > y) ? one : zero);
 
         checkCmp(cmp);
     }
 
 }
