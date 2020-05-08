 package magic;
 
import android.util.Log;

 public class Magic {
     static void trace(String s) {
         int magicNumber = 3;
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         StackTraceElement caller = stack[magicNumber];
        Log.e("MAGIC-Java", caller.getFileName() + ":" + caller.getLineNumber() + " " + s);
     }
 }
