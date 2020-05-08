 // Oliver Kullmann, 25/10/2011 (Swansea)
 
 class Sort4 {
   static final String errmess = "ERROR[Sort4]: ";
   public static void main(final String[] args) {
     if (args.length != 4) {
      System.err.println(errmess + "Precisely two arguments are needed, while you entered " +
        args.length + " many.");
       System.exit(1);
     }
     int a = Integer.parseInt(args[0]);
     int b = Integer.parseInt(args[1]);
     int c = Integer.parseInt(args[2]);
     final int d = Integer.parseInt(args[3]);
 
     // Sort a, b, c
     if (b < a) { final int t=a; a=b; b=t; }
     if (c < b) { final int t=b; b=c; c=t; }
     if (b < a) { final int t=a; a=b; b=t; }
     assert(a <= b);
     assert(b <= c);
     
     if (d < b)
       if (d < a) System.out.println(d + " " + a + " " + b + " " + c);
       else System.out.println(a + " " + d + " " + b + " " + c);
     else
       if (d < c) System.out.println(a + " " + b + " " + d + " " + c);
       else System.out.println(a + " " + b + " " + c + " " + d);
   }
 }
