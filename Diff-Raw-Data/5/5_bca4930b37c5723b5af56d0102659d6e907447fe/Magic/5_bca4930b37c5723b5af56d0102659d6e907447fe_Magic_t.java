 package magic;
 
 public class Magic {
     static void trace(String s) {
         int magicNumber = 3;
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         StackTraceElement caller = stack[magicNumber];
        android.util.Log.e("MAGIC-Java", caller.getFileName() + ":" + caller.getLineNumber() + ":" + caller.getMethodName() + " " + s);
     }
 }
