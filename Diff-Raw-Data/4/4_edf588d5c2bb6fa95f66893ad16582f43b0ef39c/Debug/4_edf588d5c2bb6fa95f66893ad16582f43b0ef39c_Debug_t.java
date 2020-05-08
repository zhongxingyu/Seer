 public class Debug {
     public boolean debug = true;
 
    public static void println(String s) { if (debug) System.err.println(s); }
    public static void print  (String s) { if (debug) System.err.print  (s); }
 }
