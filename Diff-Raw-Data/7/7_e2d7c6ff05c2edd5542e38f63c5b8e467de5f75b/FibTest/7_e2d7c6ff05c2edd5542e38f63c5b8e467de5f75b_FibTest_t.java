 package test.com.sxan;
 
 import org.junit.Test;
 
import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 13-5-24
  * Time: 上午9:41
  * To change this template use File | Settings | File Templates.
  */
 public class FibTest {
     @Test
     public void constantValue() throws Exception {
         assertThat(fib(1), is(1));
         assertThat(fib(2), is(1));
     }
 
     @Test
     public void shouldGiveAnInvalidN_raiseException() throws Exception {
         try {
             fib(0);
             fail("should failing");
         } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),equalTo("N is less than or equal to zero:0"));
         }
     }
 
     private int fib(int n) {
         if(n<=0){
            throw new IllegalArgumentException("N is less than or equal to zero:0");
         }
         return 1;
     }
 }
