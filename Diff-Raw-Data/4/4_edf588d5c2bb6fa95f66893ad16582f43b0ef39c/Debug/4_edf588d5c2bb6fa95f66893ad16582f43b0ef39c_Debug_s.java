 public class Debug {
     public boolean debug = true;
 
    public void println(String s) { if (debug) System.err.println(s); }
    public void print  (String s) { if (debug) System.err.print  (s); }
 }
