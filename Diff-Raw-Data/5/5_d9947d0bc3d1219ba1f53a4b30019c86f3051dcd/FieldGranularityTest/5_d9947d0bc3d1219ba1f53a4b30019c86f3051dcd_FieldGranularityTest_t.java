 package org.multiverse.integrationtests;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.multiverse.TestThread;
 import org.multiverse.annotations.FieldGranularity;
 import org.multiverse.annotations.TransactionalMethod;
 import org.multiverse.annotations.TransactionalObject;
 import org.multiverse.transactional.primitives.TransactionalInteger;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static org.junit.Assert.assertEquals;
 import static org.multiverse.TestUtils.*;
 
 /**
  * A test that makes sure that when fieldgranularity is used instead of the default object granularity/
  */
 public class FieldGranularityTest {
 
     public AtomicInteger executedCounter;
 
     @Before
     public void setUp() {
         executedCounter = new AtomicInteger();
 
         //force loading of the transactional integer class.
         new TransactionalInteger();
     }
 
     @Test
     public void testFieldGranularityCausesNoWriteConflicts() {
         Pair pair = new Pair(0, 0);
         SetLeftThread leftThread = new SetLeftThread(pair);
         SetRightThread rightThread = new SetRightThread(pair);
 
         startAll(leftThread, rightThread);
         joinAll(leftThread, rightThread);
 
         assertEquals(10, pair.getLeft());
         assertEquals(10, pair.getRight());
 
         assertEquals(2, executedCounter.get());
     }
 
     class SetLeftThread extends TestThread {
 
         final Pair pair;
 
         SetLeftThread(Pair pair) {
             super("SetLeftThread");
             this.pair = pair;
         }
 
         @Override
        @TransactionalMethod(readonly = false)
         public void doRun() throws Exception {
             pair.setLeft(10);
             sleepMs(1000);
             executedCounter.incrementAndGet();
         }
     }
 
     class SetRightThread extends TestThread {
 
         final Pair pair;
 
         SetRightThread(Pair pair) {
             super("SetRightThread");
             this.pair = pair;
         }
 
         @Override
        @TransactionalMethod(readonly = false)
         public void doRun() throws Exception {
             pair.setRight(10);
             sleepMs(1000);
             executedCounter.incrementAndGet();
         }
     }
 
     @TransactionalObject
     public static class Pair {
 
         @FieldGranularity
         private int left;
 
         @FieldGranularity
         private int right;
 
         public Pair(int left, int right) {
             this.left = left;
             this.right = right;
         }
 
         public int getLeft() {
             return left;
         }
 
         public void setLeft(int left) {
             this.left = left;
         }
 
         public int getRight() {
             return right;
         }
 
         public void setRight(int right) {
             this.right = right;
         }
     }
 }
