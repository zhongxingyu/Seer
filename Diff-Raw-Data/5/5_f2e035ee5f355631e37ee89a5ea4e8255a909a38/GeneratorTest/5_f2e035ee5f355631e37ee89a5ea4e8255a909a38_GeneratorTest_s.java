 package test;
 
 import ladder.Generator;
 
 import java.util.Iterator;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  * Tests the Generator class
  *
  * @author Tom Leaman (thl5@aber.ac.uk)
  */
 public class GeneratorTest {
 
   private Generator generator = new Generator();
 
   /**
    * Verifies that sensible behaviour occurs with non-sensible input
    */
   @Test
   public void testGenerateLadderLimits() {
     if (sizeOfLadder(generator.generateLadder("home", -3)) != 0)
       fail("Providing a negative depth should return an empty list");
     if (sizeOfLadder(generator.generateLadder("home", 0)) != 0)
       fail("Providing a zero depth should return an empty list");
     if (sizeOfLadder(generator.generateLadder("home", Integer.MAX_VALUE)) != 0)
       fail("You've found a ladder from home to the point where Java's Long can't hold the number!?!");
   }
 
   /**
    * Verifies that the expected depth is achieved
    */
   @Test
   public void testGenerateLadderSimple() {
     int result = sizeOfLadder(generator.generateLadder("head", 3));
     if (result != 3)
       fail("Expected depth of 3 but got " + result);
     result = sizeOfLadder(generator.generateLadder("head", 20));
     if (result != 20)
       fail("Expected depth of 20 but got " + result);
   }
 
   /**
    * get the size of the ladder
    */
   private int sizeOfLadder(Iterable<String> ladder) {
     int result = 0;
     Iterator<String> iter = ladder.iterator();
    while (iter.hasNext())
       result++;
     return result;
   }
 
 }
