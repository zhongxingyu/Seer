import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TestDollar
 {
    public void TestMultiplication()
     {
         Dollar five = new Dollar(5);
         five.times(2);
         assertEquals(10, five.amount);
     }
 }
