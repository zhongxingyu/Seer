 package jpower.core.utils;
 
 public class ByteUtils {
     public static byte[] reverse(byte[] original) {
         byte[] reversed = new byte[original.length];
        int count = 0;
        for (int i = original.length - 1; i >= 0; i--) {
            reversed[i] = original[count];
            count++;
         }
         return reversed;
     }
 }
