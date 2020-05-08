 package com.github.zhongl.nij.btrace;
 
 import static com.sun.btrace.BTraceUtils.*;
 
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.sun.btrace.annotations.*;
 
 /**
  * This sample collects histogram of javax.swing.JComponets created by traced app. The histogram is printed once every 4
  * seconds.
  */
 @BTrace
 public class Profile {
   private static Map<Object, Long> vars = Collections.newHashMap();
   private static AtomicLong lastTime = newAtomicLong(0L);
   private static AtomicLong durations = newAtomicLong(0L);
   private static AtomicLong elapse = newAtomicLong(0L);
   private static AtomicLong count = newAtomicLong(0L);
   private static AtomicInteger registered = newAtomicInteger(0);
 
   @OnMethod(clazz = "/.*/", method = "accept")
   public static void accept(@Self Object obj) {
     long current = System.nanoTime();
     long last = lastTime.getAndSet(current);
     if (last == 0) return;
     durations.addAndGet(current - last);
     count.getAndIncrement();
   }
 
   @OnMethod(clazz = "/.*/", method = "registeredKeys", location = @Location(Kind.RETURN))
   public static void registered(@Self Object obj, @Return int result) {
     registered.set(result);
   }
 
   @OnMethod(clazz = "/.*/", method = "readAndWrite", location = @Location(Kind.ENTRY))
  public static void readAndWriteEnter(@Self Object obj) {
     vars.put(obj, System.nanoTime());
   }
 
   @OnMethod(clazz = "/.*/", method = "readAndWrite", location = @Location(Kind.RETURN))
  public static void readAndWriteExit(@Self Object obj) {
     long current = System.nanoTime();
     elapse.addAndGet(current - vars.get(obj));
   }
 
   @OnTimer(1000)
   public static void print() {
     final long c = count.get();
     if (c == 0) return;
     String status = MessageFormat
         .format("count: {0}, accept avg: {1} ns, registered cur: {2}, threads: {3}, readAndWrite avg:{4}", c, (durations
             .get() / c), registered.get(), daemonThreadCount(), (elapse.get() / c));
     System.out.println(status);
   }
 }
