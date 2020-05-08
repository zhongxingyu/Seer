 package magic;
 
 public class Magic {
     static void trace(String s) {
         int magicNumber = 3;
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         StackTraceElement caller = stack[magicNumber];
         android.util.Log.e("MAGIC-Java", caller.getFileName() + ":" + caller.getLineNumber() + ":" + caller.getMethodName() + " " + s);
     }
    static void dumpStack() {
        int magicNumber = 3;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = magicNumber; i < stack.length; i++) {
            StackTraceElement caller = stack[i];
            android.util.Log.e("MAGIC-Java", caller.getFileName() + ":" + caller.getLineNumber() + ":" + caller.getMethodName() + " " + s);
        }
    }
 }
