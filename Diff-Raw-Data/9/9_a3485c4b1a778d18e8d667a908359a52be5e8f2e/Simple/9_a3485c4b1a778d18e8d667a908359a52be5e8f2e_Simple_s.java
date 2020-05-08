 #!/usr/bin/env java
 
 /**
  * Simple test case
  */
 public class Simple
 {
     public static void main(final String... args)
     {
 	#[ ! $DEBUG = "" ] &&
 	    System.out.println("DEBUG MODE");
 	
	System.out.println("Hello <"TEXT">!");
     }
 }
 
