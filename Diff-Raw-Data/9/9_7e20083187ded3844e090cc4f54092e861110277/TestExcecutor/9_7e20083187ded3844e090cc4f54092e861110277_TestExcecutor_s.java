 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.player.bot;
 
 import capstone.player.Bot;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import org.junit.After;
 import org.junit.Test;
 
 /**
  *
  * @author Max
  */
 public class TestExcecutor {
 
     private static List<Method> tests;
     private static Method setup;
     private static Method teardown;
     //Timeout for testss
     public static final long TIMEOUT_MILLIS = 2000;
 
     private static void initialize() {
         tests = new ArrayList<Method>();
         for (Method m : GameBotTest.class.getMethods()) {
             if (m.getAnnotation(Test.class) != null) {
                 tests.add(m);
             }
             if (m.getAnnotation(After.class) != null) {
                 teardown = m;
             }
             if (m.getName().equals("setup")) {
                 setup = m;
             }
         }
     }
 
     public static String testBot(Bot bot) {
         try {
             if (tests == null) {
                 initialize();
             }
 
             final GameBotTest test = new GameBotTest();
 
             setup.invoke(test, bot);
             ExecutorService executor = new ThreadPoolExecutor(1, 4, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10));
 
             for (final Method m : tests) {
                 //TODO run tests safely
                 Callable call = new Callable<String>() {
                     @Override
                     public String call() {
                         try {
                             m.invoke(test);
                             return "OK";
                         } catch (InvocationTargetException e) {
                             if(!(e.getCause() instanceof AssertionError)){
                                 return "Test " + m.getName() + " threw " + e.getClass().getSimpleName() + " at line " + e.getStackTrace()[1].getLineNumber();
                             }
                             else{
                                 return "Fail";
                             }
                             
                         }catch(Exception e){
                             return "Error executing test "+m.getName();
                         }
                     }
                 };
 
                 FutureTask<String> future = new FutureTask<String>(call);
                 executor.execute(future);
 
                 try {
                     String s;
                     try {
                        s = future.get(2, TimeUnit.SECONDS);
                     } catch (TimeoutException ex) {
                         return "Test "+m.getName()+" timed out";
                     }
                     if(s.equals("Fail")){
                         return "Test "+m.getName()+" failed";
                     }
                     else if(s.equals("OK")){
                         continue;
                     }
                     else{
                         return s;
                     }
                 }
                 catch (InterruptedException ex) {
                     return "Error: Tests terminated prematurely";
                 }
                 catch (ExecutionException x) {
                     return "Error executing test "+m.getName();
                 }
                 catch (StackOverflowError err){
                     return "Stack Overflow at test "+m.getName();
                 }
             }
 
             teardown.invoke(test);
 
             return "Pass";
         } catch (IllegalArgumentException ex) {
             return "Error setting up tests";
         } catch (InvocationTargetException ex) {
             return "Error setting up tests";
         } catch (IllegalAccessException ex) {
             return "Error: illegal access";
         }
     }
 }
