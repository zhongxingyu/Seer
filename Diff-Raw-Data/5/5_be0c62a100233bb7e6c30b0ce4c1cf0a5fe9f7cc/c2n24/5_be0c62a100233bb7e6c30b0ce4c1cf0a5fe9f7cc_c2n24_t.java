 // Aaron Zeng 20120207
 
 import java.util.*;
 import java.io.*;
 
 public class c2n24
 {
     public static void main( String[] args )
     {
         Scanner input = new Scanner( System.in );
         int a, b, c, d, e, min, max;
 
         System.out.print( "Enter first integer: " );
         a = input.nextInt();
         min = a;
         max = a;
 
         System.out.print( "Enter second integer: " );
         b = input.nextInt();
         if ( b < min )
             min = b;
         if ( b > max )
             max = b;
 
         System.out.print( "Enter third integer: " );
         c = input.nextInt();
         if ( c < min )
             min = c;
         if ( c > max )
             max = c;
 
         System.out.print( "Enter fourth integer: " );
         d = input.nextInt();
        if ( d < min )
             min = d;
        if ( d > max )
             max = d;
 
         System.out.print( "Enter fifth integer: " );
         e = input.nextInt();
         if ( e < min )
             min = e;
         if ( e > max )
             max = e;
 
         System.out.printf( "Largest: %d\n", max );
         System.out.printf( "Smallest: %d\n", min );
     }
 }
