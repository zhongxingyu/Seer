 package org.gleamy.util.concurrent;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
import javax.swing.text.MutableAttributeSet;

import mockit.Verifications;

 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class AsyncMutexTest {
 
     ExecutorService executor;
 
     @Before
     public void before() throws Exception {
         this.executor = Executors.newCachedThreadPool();
     }
 
     @After
     public void after() throws Exception {
         this.executor.shutdownNow();
     }
 
     @Test
     public void test() throws Exception {
         AsyncMutex mutex = new AsyncMutex();
 
         final RichFuture<Permit> fPermit1 = mutex.acquire();
         final RichFuture<Permit> fPermit2 = mutex.acquire();
         final RichFuture<Permit> fPermit3 = mutex.acquire();
 
         assertThat(mutex.getNumPermitsAvailable(), is(0));
         assertThat(mutex.getNumWaiters(), is(2));
 
         Permit permit1 = fPermit1.apply();
 
         Future<Permit> waitPermit2 = executor.submit(new Callable<Permit>() {
             public Permit call() throws Exception {
                 return fPermit2.apply();
             }
         });
         Future<Permit> waitPermit3 = executor.submit(new Callable<Permit>() {
             public Permit call() throws Exception {
                 return fPermit3.apply();
             }
         });
 
         try {
             waitPermit2.get(10, TimeUnit.MILLISECONDS);
             fail("permit 2");
         }
         catch (TimeoutException e) {
             assertTrue(true);
         }
         try {
             waitPermit3.get(10, TimeUnit.MILLISECONDS);
             fail("permit 3");
         }
         catch (TimeoutException e) {
             assertTrue(true);
         }
 
         assertThat(mutex.getNumPermitsAvailable(), is(0));
         assertThat(mutex.getNumWaiters(), is(2));
 
         permit1.release();
 
         assertThat(mutex.getNumPermitsAvailable(), is(0));
         assertThat(mutex.getNumWaiters(), is(1));
 
         Permit permitEither = getEither(waitPermit2, waitPermit3);
 
         assertThat(mutex.getNumPermitsAvailable(), is(0));
         assertThat(mutex.getNumWaiters(), is(1));
 
         permitEither.release();
 
         assertThat(mutex.getNumPermitsAvailable(), is(0));
         assertThat(mutex.getNumWaiters(), is(0));
     }
 
     private Permit getEither(Future<Permit> f1, Future<Permit> f2) throws Exception {
         try {
             return f1.get(10, TimeUnit.MILLISECONDS);
         }
         catch (TimeoutException e) {
             return f2.get(10, TimeUnit.MILLISECONDS);
         }
     }
 }
