 package testlib;
 
 public class MembersChange
 {
     public static int stat1 = 0;         // same
     public static final int stat2 = 0;   // added final
     public int stat3 = 0;                // removed static
     protected static int stat4 = 0;      // public -> protected 
     private static int stat5 = 0;        // public -> private 
     static int stat6 = 0;                // public -> package 
                                          // removed stat7
     public static int stat8 = 0;         // new member
 
     public static final int fin1 = 0;    // same
     protected static final int fin2 = 0; // public -> protected
     public final int fin3 = 0;           // removed static
     public static int fin4 = 0;          // removed final
 
     public int pub1 = 0;
    public int pub2 = 0;
    public int pub3 = 0;
     public int pub4 = 0;
 
     protected int prot1 = 0;
     protected int prot2 = 0;
     protected int prot3 = 0;
     protected int prot4 = 0;
 
     public String obj1 = new String();  // member type changed Object -> String
     public String obj2 = new String();  // member type changed Boolean -> String
 
     private int priv1 = 0;              // same
     public int priv2 = 0;               // private -> public
 }
